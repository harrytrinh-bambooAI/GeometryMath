import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, ask, assignSet, bearer, hook } from '../s3e1/s3e1.ts';
import { KILL, MSG, counter, readCounter, notifications, s5e5World, stamp, switches, usage } from './s5e5.ts';

/**
 * US-S5E5-04 — công tắc AI theo lớp, tắt khẩn, hạn mức lượt/token, báo cáo dùng AI (máy chủ thật, AI giả lập).
 * Chấm nền (S5-E4/E6) chưa có ở đợt W2: ca chấm đi qua bộ chạy chấm nền giả của móc /test-hooks/s5e5/grading-run, dùng
 * đúng cổng hạn mức (AiQuota.admitGrading mỗi trang), cổng AI và sổ ai_call_log thật.
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
  const { w, lan, c11 } = await setup(request);
  // 11A1 AI chấm bật (mặc định QS5-26 là tắt)
  const on = await request.put(switches(c11.id), { headers: bearer(lan.token), data: { aiGrading: true } });
  expect(on.status(), await on.text()).toBe(200);
  // BN-01 đang chờ chấm nền
  const bn01 = randomUUID();
  const job = (await hook(request, 's5e5/jobs', { orgId: w.orgId, classId: c11.id, refId: bn01 })) as { jobId: string };
  const r = await request.put(switches(c11.id), { headers: bearer(lan.token), data: { aiGrading: false } });
  expect(r.status()).toBe(200);
  const b = (await r.json()) as { aiGrading: boolean; message: string; cancelledJobs: number };
  expect(b.aiGrading).toBe(false);
  expect(b.message).toBe(MSG.s59('11A1'));
  expect(b.cancelledJobs).toBe(1);
  const st = await request.get(`${API}/test-hooks/s5e5/jobs/${job.jobId}?orgId=${w.orgId}`);
  expect(((await st.json()) as { status: string }).status).toBe('cancelled');
  // việc nền chạy tiếp sau khi bị huỷ: không có đề xuất mới, không gọi AI
  const before = await aiCalls(request, lan.id);
  const run = (await hook(request, 's5e5/grading-run', {
    orgId: w.orgId, teacherId: lan.id, classId: c11.id, submissionId: bn01, pages: 2, jobId: job.jobId,
  })) as { status: string; proposals: unknown[] };
  expect(run.status).toBe('cancelled');
  expect(run.proposals).toEqual([]);
  expect(await aiCalls(request, lan.id)).toBe(before);
  // AuditEvent: người thực hiện cô Lan, trước bật, sau tắt
  const audit = (await (await request.get(`${API}/test-hooks/s5e5/audit?orgId=${w.orgId}&classId=${c11.id}`)).json()) as Array<{
    actorId: string; actorRole: string; switch: string; before: boolean; after: boolean;
  }>;
  expect(audit[0]).toMatchObject({ actorId: lan.id, actorRole: 'teacher', switch: 'ai_grading', before: true, after: false });
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
  const { w, lan, c11 } = await setup(request);
  await request.put(switches(c11.id), { headers: bearer(lan.token), data: { aiGrading: true } });
  // biên: 199 còn chấm 1 trang
  await counter(request, w.orgId, 'class', c11.id, 'grade', 199);
  const edge = (await hook(request, 's5e5/grading-run', {
    orgId: w.orgId, teacherId: lan.id, classId: c11.id, submissionId: randomUUID(), pages: 1,
  })) as { status: string; reason?: string };
  expect(edge.status).toBe('ai_skipped'); // trang 1 grade_ocr là lượt thứ 200; grade trang 1 là lượt 201
  expect(await readCounter(request, w.orgId, 'class', c11.id, 'grade')).toBe(200);
  // 200/200: Ngọc Anh nộp bài 2 trang
  const before = await aiCalls(request, lan.id);
  const r = (await hook(request, 's5e5/grading-run', {
    orgId: w.orgId, teacherId: lan.id, classId: c11.id, submissionId: randomUUID(), pages: 2,
  })) as { status: string; flag: string; reason: string; aiCalls: number };
  expect(r.status).toBe('ai_skipped');
  expect(r.flag).toBe('AI không chấm');
  expect(r.reason).toBe('lớp đã hết lượt AI hôm nay');
  expect(r.aiCalls).toBe(0);
  expect(await aiCalls(request, lan.id)).toBe(before); // không có AiCallLog grade / grade_ocr mới
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
  const { w, lan, c11 } = await setup(request);
  await request.put(switches(c11.id), { headers: bearer(lan.token), data: { aiGrading: true } });
  // lượt gọi: ocr trang 1, ocr trang 2, grade trang 1 (lỗi), grade trang 1 (gọi lại, đạt), grade trang 2
  await hook(request, 'ai', { userId: lan.id, qaReplies: ['ok', 'ok', 'error', 'ok', 'ok'] });
  const before = await aiCalls(request, lan.id);
  const r = (await hook(request, 's5e5/grading-run', {
    orgId: w.orgId, teacherId: lan.id, classId: c11.id, submissionId: randomUUID(), pages: 2,
  })) as { status: string; proposals: string[]; aiCalls: number };
  expect(r.status).toBe('graded');
  expect(r.proposals).toHaveLength(2);
  expect(await readCounter(request, w.orgId, 'class', c11.id, 'grade')).toBe(4);
  expect(await aiCalls(request, lan.id)).toBe(before + 5); // AiCallLog ghi đủ mọi lần gọi
  await hook(request, 'ai', { userId: lan.id, qaReplies: [] });
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
  const { w, lan } = await setup(request);
  const bn01 = randomUUID();
  // lần 3 còn được (biên), lần 4 bị chặn
  await hook(request, 's5e5/ocr-retry', { orgId: w.orgId, submissionId: bn01, used: 2 });
  const third = await request.post(`${API}/test-hooks/s5e5/ocr-retry`, { data: { orgId: w.orgId, teacherId: lan.id, submissionId: bn01 } });
  expect(third.status(), await third.text()).toBe(200);
  expect(((await third.json()) as { n: number }).n).toBe(3);
  const before = await aiCalls(request, lan.id);
  const fourth = await request.post(`${API}/test-hooks/s5e5/ocr-retry`, { data: { orgId: w.orgId, teacherId: lan.id, submissionId: bn01 } });
  expect(fourth.status()).toBe(429);
  expect(((await fourth.json()) as Err).code).toBe('RATE_LIMITED');
  expect(await aiCalls(request, lan.id)).toBe(before); // không gọi grade_ocr
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
