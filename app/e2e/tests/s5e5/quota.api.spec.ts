import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, ask, assignSet, bearer, hook } from '../s3e1/s3e1.ts';
import { setup as s5e4Setup } from '../s5e4/s5e4.ts';
import { P1, P2, SUB, ack, aiOn, submitOk, waitOcr } from '../s5e4/submit/submit.ts';
import { approve, gradeCalls, mock, setup as gradingSetup, waitAi } from '../s5e6/s5e6b.ts';
import { jobs } from './insights.ts';
import { KILL, MSG, counter, readCounter, notifications, s5e5World, stamp, switches, usage } from './s5e5.ts';

/**
 * US-S5E5-04 — công tắc AI theo lớp, tắt khẩn, hạn mức lượt/token, báo cáo dùng AI (máy chủ thật, AI giả lập).
 * T-261128-64: TC-19, 23, 25, 27 chuyển từ bộ chạy chấm nền giả (/test-hooks/s5e5/grading-run, /ocr-retry) sang luồng
 * chấm thật: nộp bài (US-S5E4-03) → đọc chữ nền (grade_ocr) → AI đề xuất (grade, S5-E6) → "Đọc lại"; cổng hạn mức
 * AiQuota, cổng AI và sổ ai_call_log thật.
 */

type Err = { code: string; message: string; details?: Array<{ field: string; message: string }> };

async function setup(request: Parameters<typeof s5e5World>[0]) {
  const w = await s5e5World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const set = await assignSet(request, lan, c11.id, 'Bộ bài luyện 11A1');
  return { w, lan, c11, a11: set.items[0]!.assignmentId };
}

test('Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký @TC-S5E5-19', async ({ request }) => {
  // đường thật (T-261128-64): BC-01 "AI đề xuất", AI chấm 11A1 bật; BN-01 của Bình An đang chờ chấm nền (việc grade đã
  // đăng ký, lượt gọi AI đang chạy: giả lập trả lời sau 3 giây)
  const c = await gradingSetup(request, 'ai_proposal');
  const an = c.w.people['Bình An']!;
  await hook(request, 'ai', { userId: an.id, delayMs: 3000 });
  try {
    await ack(request, an);
    const bn01 = await submitOk(request, an, c.bc.id, [P1]);
    await expect
      .poll(async () => (await jobs(request, c.w.orgId, bn01.id)).some((j) => j.task === 'grade' && j.status === 'pending'), {
        timeout: 30_000,
        intervals: [100, 200],
      })
      .toBe(true);
    const r = await request.put(switches(c.c11.id), { headers: bearer(c.lan.token), data: { aiGrading: false } });
    expect(r.status()).toBe(200);
    const b = (await r.json()) as { aiGrading: boolean; message: string; cancelledJobs: number };
    expect(b.aiGrading).toBe(false);
    expect(b.message).toBe(MSG.s59('11A1'));
    expect(b.cancelledJobs).toBeGreaterThanOrEqual(1);
    expect((await jobs(request, c.w.orgId, bn01.id)).find((j) => j.task === 'grade')!.status).toBe('cancelled');
    // lượt gọi đang chạy trả về sau khi huỷ: không lưu đề xuất; bài thành "AI không chấm", chấm tay
    const v = await waitAi(request, c.lan, bn01.id);
    expect(v.ai).toMatchObject({ aiStatus: 'no_grade', reason: 'AI chấm của lớp đang tắt' });
    expect((await gradeCalls(request, an.id)).length).toBe(1);
  } finally {
    await hook(request, 'ai', { userId: an.id, delayMs: 0 });
  }
  // AuditEvent: người thực hiện cô Lan, trước bật, sau tắt
  const audit = (await (await request.get(`${API}/test-hooks/s5e5/audit?orgId=${c.w.orgId}&classId=${c.c11.id}`)).json()) as Array<{
    actorId: string; actorRole: string; switch: string; before: boolean; after: boolean;
  }>;
  expect(audit[0]).toMatchObject({ actorId: c.lan.id, actorRole: 'teacher', switch: 'ai_grading', before: true, after: false });
});

test('Báo cáo dùng AI 30 ngày của lớp @TC-S5E5-20', async ({ request }) => {
  const { w, lan, c11 } = await setup(request);
  await counter(request, w.orgId, 'class', c11.id, 'turn.kb_qa', 100, 3);
  await counter(request, w.orgId, 'class', c11.id, 'turn.kb_qa', 20, 0);
  await counter(request, w.orgId, 'class', c11.id, 'turn.grade', 40, 5);
  await counter(request, w.orgId, 'class', c11.id, 'blocked', 5, 1);
  await counter(request, w.orgId, 'class', c11.id, 'turn.kb_qa', 999, 45); // ngoài 30 ngày
  const r = await request.get(usage(c11.id, 30), { headers: bearer(lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const u = (await r.json()) as { days: number; tasks: Array<{ task: string; turns: number }>; blocked: number };
  expect(u.days).toBe(30);
  expect(u.tasks).toEqual(expect.arrayContaining([{ task: 'kb_qa', turns: 120 }, { task: 'grade', turns: 40 }]));
  expect(u.blocked).toBe(5);
  // không có tên hay mã học sinh
  const text = JSON.stringify(u);
  for (const name of ['Bình An', 'Chi Mai', 'HS-']) expect(text).not.toContain(name);
  for (const p of Object.values(w.people)) if (p.role === 'STUDENT') expect(text).not.toContain(p.id);
});

test('Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được @TC-S5E5-21', async ({ request }) => {
  const { w, c11, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  await counter(request, w.orgId, 'class', c11.id, 'qa', 300);
  const before = await aiCalls(request, an.id);
  const r = await ask(request, an.token, a11);
  expect(r.status()).toBe(429);
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('RATE_LIMITED');
  expect(e.message).toBe(MSG.s60('11A1', 300));
  expect(await aiCalls(request, an.id)).toBe(before);
  // gửi đáp số bài luyện vẫn được chấm tự động
  const att = await request.post(`${API}/api/v1/student/practice-items/${a11}/attempts`, {
    headers: bearer(an.token),
    data: { answer: '1', requestKey: randomUUID() },
  });
  expect(att.status(), await att.text()).toBeLessThan(300);
  expect(((await att.json()) as { outcome?: string }).outcome).toBeTruthy();
});

test('Học sinh và giáo viên khác không đổi được công tắc @TC-S5E5-22', async ({ request }) => {
  const { w, lan, c11 } = await setup(request);
  const an = w.people['Bình An']!;
  const khoi = w.people['Thầy Khôi']!;
  const s = await request.put(switches(c11.id), { headers: bearer(an.token), data: { aiGrading: true } });
  expect(s.status()).toBe(403);
  expect(((await s.json()) as Err).code).toBe('FORBIDDEN');
  const k = await request.put(switches(c11.id), { headers: bearer(khoi.token), data: { aiGrading: true } });
  expect(k.status()).toBe(404);
  expect(((await k.json()) as Err).code).toBe('NOT_FOUND');
  const g = (await (await request.get(switches(c11.id), { headers: bearer(lan.token) })).json()) as {
    aiGrading: boolean; assistant: boolean;
  };
  expect(g).toMatchObject({ aiGrading: false, assistant: true }); // mặc định QS5-26, không đổi
});

test('Lượt chấm thứ 201 của lớp: bài thành "AI không chấm", vẫn chấm tay @TC-S5E5-23', async ({ request }) => {
  // đường thật (T-261128-64): đọc chữ nền + AI đề xuất xin lượt qua AiQuota mỗi trang
  const c = await gradingSetup(request, 'ai_proposal');
  // biên: 199 → Chi Mai nộp 1 trang: đọc chữ trang 1 là lượt 200 (nhận), grade trang 1 là lượt 201 (không nhận)
  await counter(request, c.w.orgId, 'class', c.c11.id, 'grade', 199);
  const mai = c.w.people['Chi Mai']!;
  await ack(request, mai);
  const edge = await submitOk(request, mai, c.bc.id, [P1]);
  expect((await waitOcr(request, c.lan, edge.id)).status).toBe('done');
  const ve = await waitAi(request, c.lan, edge.id);
  expect(ve.ai).toMatchObject({ aiStatus: 'no_grade', reason: 'lớp đã hết lượt AI hôm nay' });
  expect(await readCounter(request, c.w.orgId, 'class', c.c11.id, 'grade')).toBe(200);
  expect((await gradeCalls(request, mai.id)).length).toBe(0);
  // 200/200: Ngọc Anh nộp bài 2 trang — không có AiCallLog grade / grade_ocr mới
  const na = c.w.people['Ngọc Anh']!;
  await ack(request, na);
  const before = await aiCalls(request, na.id);
  const bn = await submitOk(request, na, c.bc.id, [P1, P2]);
  const o = await waitOcr(request, c.lan, bn.id);
  expect(o.status).toBe('failed'); // không nhận lượt đọc chữ: không gọi AI
  // màn chấm hiện lý do hạn mức thay cho "Chưa đọc được" (Dev-S5E5-10, T-261128-68)
  expect(o.statusLabel).toBe('AI không chấm: lớp đã hết lượt AI hôm nay');
  expect(o.reason).toBe('lớp đã hết lượt AI hôm nay');
  const q = (await (await request.get(`${API}/api/v1/teacher/grading/queue?assignmentId=${c.bc.id}`, { headers: bearer(c.lan.token) })).json()) as {
    items: Array<{ submissionId: string; flags: Array<{ label: string }> }>;
  };
  expect(q.items.find((i) => i.submissionId === bn.id)!.flags.map((f) => f.label)).toContain('AI không chấm: lớp đã hết lượt AI hôm nay');
  expect(await aiCalls(request, na.id)).toBe(before);
  expect(await readCounter(request, c.w.orgId, 'class', c.c11.id, 'grade')).toBe(200);
  // cô Lan chấm tay được cả hai bài
  for (const id of [edge.id, bn.id]) expect((await approve(request, c.lan, id, [2, 4, 2, 1, 1])).status).toBe('approved');
});

test('Một học sinh hết 30 lượt hỏi, bạn cùng lớp vẫn hỏi được @TC-S5E5-24', async ({ request }) => {
  const { w, c11, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  const mai = w.people['Chi Mai']!;
  await counter(request, w.orgId, 'student', an.id, 'qa', 30);
  await counter(request, w.orgId, 'class', c11.id, 'qa', 45);
  const r = await ask(request, an.token, a11);
  expect(r.status()).toBe(429);
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  expect(((await r.json()) as Err).code).toBe('RATE_LIMITED');
  const ok = await ask(request, mai.token, a11);
  expect(ok.status(), await ok.text()).toBe(200);
  expect(await readCounter(request, w.orgId, 'class', c11.id, 'qa')).toBe(46);
});

test('Gọi lại tự động không tính thêm lượt; mỗi trang chấm tính một lượt @TC-S5E5-25', async ({ request }) => {
  // đường thật (T-261128-64): BN-01 2 trang → đọc chữ nền (2 lượt grade_ocr) → AI đề xuất (2 lượt grade); giả lập grade
  // lỗi lần 1, đạt lần 2 (gọi lại trong cùng lượt)
  const c = await gradingSetup(request, 'ai_proposal');
  const an = c.w.people['Bình An']!;
  await mock(request, an, ['error', 'F-S5-090']);
  const before = await aiCalls(request, an.id);
  await ack(request, an);
  const bn01 = await submitOk(request, an, c.bc.id, [P1, P2]);
  const v = await waitAi(request, c.lan, bn01.id);
  expect(v.ai?.aiStatus).toBe('proposed');
  expect(await readCounter(request, c.w.orgId, 'class', c.c11.id, 'grade')).toBe(4); // 2 + 2, không tính lần gọi lại
  expect((await gradeCalls(request, an.id)).length).toBe(2); // AiCallLog ghi đủ mọi lần gọi (lỗi + gọi lại)
  expect(await aiCalls(request, an.id)).toBe(before + 4); // 2 grade_ocr + 2 grade
});

test('Đạt 80 % trần token thì giáo viên được cảnh báo một lần @TC-S5E5-26', async ({ request }) => {
  const { w, lan, c11 } = await setup(request);
  await counter(request, w.orgId, 'class', c11.id, 'tokens', 1_590_000);
  const quotaNotes = async () => (await notifications(request, lan)).filter((n) => n.kind === 'ai_quota');
  expect(await quotaNotes()).toHaveLength(0);
  const t1 = (await hook(request, 's5e5/tokens', { orgId: w.orgId, classId: c11.id, tokens: 20_000 })) as { n: number };
  expect(t1.n).toBe(1_610_000);
  const after1 = await quotaNotes();
  expect(after1).toHaveLength(1);
  expect(after1[0]!.body).toContain('11A1');
  expect(after1[0]!.body).toContain('80 %');
  await hook(request, 's5e5/tokens', { orgId: w.orgId, classId: c11.id, tokens: 20_000 });
  expect(await quotaNotes()).toHaveLength(1);
});

test('"Đọc lại" lần thứ 4 bị chặn @TC-S5E5-27', async ({ request }) => {
  // đường thật (T-261128-64): POST /api/v1/submissions/{id}/ocr/retry (đọc chữ nền lỗi → "Đọc lại")
  const c = await s5e4Setup(request);
  const an = c.w.people['Bình An']!;
  await aiOn(request, c.lan, c.c11.id);
  // đọc chữ nền chạy dưới danh tính học sinh; "Đọc lại" chạy dưới danh tính giáo viên: cả hai lỗi
  await hook(request, 'ai', { userId: an.id, mode: 'error' });
  await hook(request, 'ai', { userId: c.lan.id, mode: 'error' });
  try {
    await ack(request, an);
    const bn01 = await submitOk(request, an, c.bc.id, [P1]);
    expect((await waitOcr(request, c.lan, bn01.id)).status).toBe('failed');
    // đã bấm "Đọc lại" 2 lần (bộ đếm 2/3): lần 3 còn được (biên), lần 4 bị chặn
    await hook(request, 's5e5/ocr-retry', { orgId: c.w.orgId, submissionId: bn01.id, used: 2 });
    const third = await request.post(`${SUB}/${bn01.id}/ocr/retry`, { headers: bearer(c.lan.token) });
    expect(third.status(), await third.text()).toBe(200);
    expect((await waitOcr(request, c.lan, bn01.id)).status).toBe('failed');
    const before = await aiCalls(request, an.id);
    const beforeLan = await aiCalls(request, c.lan.id);
    const fourth = await request.post(`${SUB}/${bn01.id}/ocr/retry`, { headers: bearer(c.lan.token) });
    expect(fourth.status()).toBe(429);
    expect(((await fourth.json()) as Err).code).toBe('RATE_LIMITED');
    expect(await aiCalls(request, an.id)).toBe(before); // không gọi grade_ocr
    expect(await aiCalls(request, c.lan.id)).toBe(beforeLan);
  } finally {
    await hook(request, 'ai', { userId: an.id, mode: 'ok' });
    await hook(request, 'ai', { userId: c.lan.id, mode: 'ok' });
  }
});

test('Người vận hành tắt khẩn task kb_qa toàn hệ thống @TC-S5E5-28', async ({ request }) => {
  const { w, lan, a11 } = await setup(request);
  const khoi = w.people['Thầy Khôi']!;
  const set10 = await assignSet(request, khoi, w.classes['10B']!.id, 'Bộ 10B');
  const a10 = set10.items[0]!.assignmentId;
  const an = w.people['Bình An']!;
  const mina = w.people['Mi Na']!;
  // người vận hành: tài khoản được cấp vai Vận hành (ai_ops_grant); làn kiểm thử riêng để không tắt AI của ca song song
  const op = w.people['Thầy Khôi']!;
  await hook(request, 's5e5/operator', { userId: op.id });
  const lane = { 'X-Geo-Test-Lane': stamp() };
  // giáo viên không có vai Vận hành: FORBIDDEN
  const denied = await request.put(KILL, { headers: { ...bearer(lan.token), ...lane }, data: { task: 'kb_qa', off: true } });
  expect(denied.status()).toBe(403);
  const r = await request.put(KILL, { headers: { ...bearer(op.token), ...lane }, data: { task: 'kb_qa', off: true } });
  expect(r.status(), await r.text()).toBe(200);
  try {
    const t0 = Date.now();
    const beforeAn = await aiCalls(request, an.id);
    const beforeMi = await aiCalls(request, mina.id);
    for (const [who, a] of [
      [an, a11],
      [mina, a10],
    ] as const) {
      const q = await request.post(`${API}/api/v1/student/assignments/${a}/questions`, {
        headers: { ...bearer(who.token), ...lane },
        data: { question: 'Em nên bắt đầu từ đâu?', requestId: randomUUID() },
      });
      expect(q.status()).toBe(403);
      const e = (await q.json()) as Err;
      expect(e.message).toContain('AI đang tạm tắt');
      expect(e.details?.[0]?.message).toBe('AI_OFF_SYSTEM');
    }
    expect(Date.now() - t0).toBeLessThan(10_000); // hiệu lực ≤ 10 giây
    expect(await aiCalls(request, an.id)).toBe(beforeAn);
    expect(await aiCalls(request, mina.id)).toBe(beforeMi);
    // lượt không mang làn này không bị ảnh hưởng (ca khác chạy song song)
    const other = await ask(request, an.token, a11);
    expect(other.status(), await other.text()).toBe(200);
    const audit = (await (await request.get(`${API}/test-hooks/s5e5/audit?orgId=${w.orgId}&task=kb_qa`)).json()) as Array<{
      actorId: string; actorRole: string; before: boolean; after: boolean;
    }>;
    expect(audit[0]).toMatchObject({ actorId: op.id, actorRole: 'operator', before: true, after: false });
  } finally {
    await request.put(KILL, { headers: { ...bearer(op.token), ...lane }, data: { task: 'kb_qa', off: false } });
  }
});

test('Đọc chữ nền cộng token AI vào trần token của lớp; chạm trần thì không gọi AI @TC-S5E5-26', async ({ request }) => {
  // REVIEW-S5 mục 8 (T-261128-71): grade_ocr đi qua cổng hạn mức như hỏi đáp kho
  const c = await s5e4Setup(request);
  const an = c.w.people['Bình An']!;
  await aiOn(request, c.lan, c.c11.id);
  await ack(request, an);
  const tokens = () => readCounter(request, c.w.orgId, 'class', c.c11.id, 'tokens');
  expect(await tokens()).toBe(0);
  const bn01 = await submitOk(request, an, c.bc.id, [P1]);
  expect((await waitOcr(request, c.lan, bn01.id)).status).toBe('done');
  const after1 = await tokens();
  expect(after1).toBeGreaterThan(0); // lượt OCR (mô phỏng) đã cộng token vào trần lớp
  // bài thứ hai: bộ đếm tăng tiếp (N lượt OCR → tổng tăng đúng)
  const mai = c.w.people['Chi Mai']!;
  await ack(request, mai);
  const bn02 = await submitOk(request, mai, c.bc.id, [P1]);
  expect((await waitOcr(request, c.lan, bn02.id)).status).toBe('done');
  expect(await tokens()).toBeGreaterThan(after1);
  // chạm trần token (2 000 000): bài mới không gọi AI, ghi lý do hạn mức
  await counter(request, c.w.orgId, 'class', c.c11.id, 'tokens', 2_000_000);
  const huy = c.w.people['Đức Huy']!;
  await ack(request, huy);
  const before = await aiCalls(request, huy.id);
  const bn03 = await submitOk(request, huy, c.bc.id, [P1]);
  const o = await waitOcr(request, c.lan, bn03.id);
  expect(o.status).toBe('failed');
  expect((o as unknown as { reason?: string }).reason).toBe('lớp đã hết hạn mức token AI hôm nay');
  expect(await aiCalls(request, huy.id)).toBe(before);
});
