import { expect, test } from '../../support/fixtures.ts';
import { API, bearer } from '../s3e1/s3e1.ts';
import { SUB } from '../s5e4/submit/submit.ts';
import { MSG, approve, gradeCalls, gradeJson, gradeRaw, mock, proposalRaw, queue, setup, submit, view, waitAi } from './s5e6b.ts';

/**
 * S5-E6b (T-261128-62) — AI không chấm (US-S5E6-02), màn chấm có đề xuất qua API (US-S5E6-04-FC2), chạy song song
 * (US-S5E6-05). Bài nộp đi luồng thật (US-S5E4-03 → đọc chữ nền → job nền chấm task `grade`, giả lập theo kịch bản).
 */

// ------------------------------------------------------------------ US-S5E6-02

test('AI từ chối vì trang mờ @TC-S5E6-09', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  const reason = 'Trang 2 mờ, không đọc được bước 2–3';
  await mock(request, c.w.people['Đức Huy']!, [JSON.stringify({ kind: 'refuse', reason })]);
  const { sub } = await submit(request, c, 'Đức Huy');
  const v = await waitAi(request, c.lan, sub.id);
  expect(v.status).toBe('waiting');
  expect(v.statusLabel).toBe('Chờ chấm');
  expect(v.ai).toMatchObject({ aiStatus: 'no_grade', aiStatusLabel: 'AI không chấm', reason, message: MSG.s45(reason), retryable: true });
  expect(v.ai!.aiFlags).toContain('AI không chấm');
  expect(v.ai).not.toHaveProperty('total');
  const p = await (await proposalRaw(request, c.lan, sub.id)).json();
  expect(p).toMatchObject({ status: 'no_grade', message: MSG.s45(reason) });
  expect(p).not.toHaveProperty('total');
  expect(p).not.toHaveProperty('criteria');
});

test('Độ tin cậy thấp thì không hiện điểm @TC-S5E6-10', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, [gradeJson([2, 4, 2, 1, 1], 0.55)]);
  const { sub } = await submit(request, c, 'Bình An');
  const v = await waitAi(request, c.lan, sub.id);
  expect(v.ai).toMatchObject({ aiStatus: 'no_grade', reason: 'độ tin cậy thấp (0,55)' });
  expect(v.ai!.aiFlags).toContain('AI không chấm');
  const p = await (await proposalRaw(request, c.lan, sub.id)).json();
  expect(p.status).toBe('no_grade');
  for (const k of ['total', 'criteria', 'confidence']) expect(p).not.toHaveProperty(k);
});

test('Biên độ tin cậy tổng: 0,60 được chấm, 0,59 thì không @TC-S5E6-11', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, [gradeJson([2, 4, 2, 1, 1], 0.6)]);
  await mock(request, c.w.people['Chi Mai']!, [gradeJson([2, 4, 2, 1, 1], 0.59)]);
  const a = await submit(request, c, 'Bình An');
  const b = await submit(request, c, 'Chi Mai');
  const va = await waitAi(request, c.lan, a.sub.id);
  expect(va.ai).toMatchObject({ aiStatus: 'proposed', aiFlags: [], acceptAll: true });
  const pa = await (await proposalRaw(request, c.lan, a.sub.id)).json();
  expect(pa).toMatchObject({ status: 'proposed', total: 10, confidence: 0.6 });
  const vb = await waitAi(request, c.lan, b.sub.id);
  expect(vb.ai).toMatchObject({ aiStatus: 'no_grade', reason: 'độ tin cậy thấp (0,59)' });
});

test('Cổng AI hết giờ hai lần @TC-S5E6-12', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  const an = c.w.people['Bình An']!;
  await mock(request, an, ['error', 'error']);
  const { sub } = await submit(request, c, 'Bình An');
  const v = await waitAi(request, c.lan, sub.id);
  expect(v.ai).toMatchObject({ aiStatus: 'no_grade', reason: 'AI tạm thời không phản hồi', retryable: true });
  expect(await gradeCalls(request, an.id)).toHaveLength(2);
  // "Thử chấm lại": AI trả lời bình thường → có đề xuất
  await mock(request, an, ['F-S5-090']);
  const r = await request.post(`${SUB}/${sub.id}/proposal/retry`, { headers: bearer(c.lan.token) });
  expect(r.status(), await r.text()).toBe(202);
  await expect.poll(async () => (await view(request, c.lan, sub.id)).ai?.aiStatus, { timeout: 30_000 }).toBe('proposed');
  // bài vẫn chấm tay được
  const g = await approve(request, c.lan, sub.id, [2, 3, 2, 1, 1]);
  expect(g.grade).toMatchObject({ total: 9 });
  // học sinh không gọi được "Thử chấm lại"
  const s = await request.post(`${SUB}/${sub.id}/proposal/retry`, { headers: bearer(an.token) });
  expect(s.status()).toBe(403);
});

test('Lớp tắt AI chấm thì không gọi AI @TC-S5E6-13', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  const r = await request.put(`${API}/api/v1/teacher/my-classes/${c.c11.id}/ai-switches`, { headers: bearer(c.lan.token), data: { aiGrading: false } });
  expect(r.status(), await r.text()).toBe(200);
  const na = c.w.people['Ngọc Anh']!;
  const { submitOk, ack, P1, P2 } = await import('../s5e4/submit/submit.ts');
  await ack(request, na);
  const sub = await submitOk(request, na, c.bc.id, [P1, P2]);
  await expect
    .poll(async () => (await queue(request, c.lan, c.bc.id)).find((x) => x.submissionId === sub.id)?.ai?.aiStatusLabel, { timeout: 15_000 })
    .toBe('AI đang tắt');
  await new Promise((res) => setTimeout(res, 3000)); // hơn một chu kỳ job nền
  expect(await gradeCalls(request, na.id)).toHaveLength(0);
});

// ------------------------------------------------------------------ US-S5E6-04 (API)

test('Gọi API duyệt cả bài có cờ bị từ chối @TC-S5E6-22', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Chi Mai']!, ['s5e6-kq2']); // F-S5-091 nhưng Kết quả 2/2; bản đọc chữ không khớp engine
  const { sub } = await submit(request, c, 'Chi Mai');
  // bản đọc chữ v2 có đáp số a√3/3 (BN-02) để engine báo Lệch
  const put = await request.put(`${SUB}/${sub.id}/ocr`, { headers: bearer(c.lan.token), data: { baseVersion: 1, pages: [{ pageNo: 2, segments: [{ text: 'Vậy d(A,(SBC)) = a√3/3' }] }] } });
  expect(put.status(), await put.text()).toBe(200);
  await expect.poll(async () => (await view(request, c.lan, sub.id)).ai?.aiFlags ?? [], { timeout: 30_000 }).toContain('AI và engine lệch');
  const v = await view(request, c.lan, sub.id);
  expect(v.ai!.acceptAll).toBe(false);
  const r = await gradeRaw(request, c.lan, sub.id, { acceptAll: true });
  expect(r.status(), await r.text()).toBe(409);
  expect((await r.json()).code).toBe('CONFLICT');
  const after = await view(request, c.lan, sub.id);
  expect(after.grade).toBeUndefined();
  expect(after.status).toBe('waiting');
});

test('Nhận cả bài qua API cho bài không cờ ghi nguồn AI đề xuất @TC-S5E6-20', async ({ request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await submit(request, c, 'Bình An');
  const v = await waitAi(request, c.lan, sub.id);
  expect(v.ai).toMatchObject({ aiStatus: 'proposed', acceptAll: true });
  const r = await gradeRaw(request, c.lan, sub.id, { version: v.version, acceptAll: true, comment: '' });
  expect(r.status(), await r.text()).toBe(200);
  const g = (await r.json()) as { status: string; grade: { total: number }; ai: { source: string; actions: Array<{ action: string }> } };
  expect(g.status).toBe('approved');
  expect(g.grade.total).toBe(10);
  expect(g.ai.source).toBe(MSG.source);
  expect(g.ai.actions.map((a) => a.action)).toEqual(['accept', 'accept', 'accept', 'accept', 'accept']);
});

// ------------------------------------------------------------------ US-S5E6-05

/** BC-01 Chạy song song; BN-02 (Chi Mai) AI đã chấm F-S5-091. */
async function parallelBn02(request: Parameters<typeof setup>[0]) {
  const c = await setup(request, 'parallel');
  await mock(request, c.w.people['Chi Mai']!, ['F-S5-091']);
  const { sub } = await submit(request, c, 'Chi Mai');
  await waitAi(request, c.lan, sub.id);
  return { c, sub };
}

test('Trước khi duyệt không thấy đề xuất @TC-S5E6-23', async ({ request }) => {
  const { c, sub } = await parallelBn02(request);
  const p = await proposalRaw(request, c.lan, sub.id);
  expect(p.status()).toBe(409);
  const body = await p.json();
  expect(body.code).toBe('CONFLICT');
  expect(body.message).toBe(MSG.s47);
  const v = await view(request, c.lan, sub.id);
  expect(v.ai).toEqual({ aiDone: true, shadow: true, message: MSG.s47 });
  const raw = JSON.stringify(v);
  for (const k of ['"score"', '"reason"', '"confidence"', '"aiFlags"', '"aiStatus"', 'AI và engine lệch']) expect(raw).not.toContain(k);
  // tín hiệu AI của bài có chấm cũng không lộ bài song song chưa duyệt
  const s = await request.get(`${API}/api/v1/teacher/graded-assignments/${c.bc.id}/ai-signals`, { headers: bearer(c.lan.token) });
  expect(s.status()).toBe(200);
  expect(((await s.json()) as { items: unknown[] }).items).toEqual([]);
});

test('Sau khi duyệt hiện so sánh và lưu cặp @TC-S5E6-24', async ({ request }) => {
  const { c, sub } = await parallelBn02(request);
  const g = await approve(request, c.lan, sub.id, [2, 2.5, 0, 1, 0.5]);
  expect(g.grade).toMatchObject({ total: 6 });
  const r = await request.get(`${SUB}/${sub.id}/comparison`, { headers: bearer(c.lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  const cmp = (await r.json()) as { pairs: number; criteria: Array<{ name: string; aiPoints: number; teacherPoints: number; differs: boolean }> };
  expect(cmp.pairs).toBe(5);
  expect(cmp.criteria.filter((x) => x.differs).map((x) => [x.name, x.aiPoints, x.teacherPoints])).toEqual([
    ['Các bước', 3, 2.5],
    ['Trình bày', 1, 0.5],
  ]);
  // sau khi duyệt mới xem được đề xuất
  expect((await proposalRaw(request, c.lan, sub.id)).status()).toBe(200);
});

test('Hàng chờ ở chế độ song song không lộ trạng thái AI @TC-S5E6-25', async ({ request }) => {
  const c = await setup(request, 'parallel');
  await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
  await mock(request, c.w.people['Chi Mai']!, ['s5e6-kq2']);
  await mock(request, c.w.people['Đức Huy']!, [JSON.stringify({ kind: 'refuse', reason: 'Trang 2 mờ, không đọc được bước 2–3' })]);
  const bn01 = (await submit(request, c, 'Bình An')).sub;
  const bn02 = (await submit(request, c, 'Chi Mai')).sub;
  const bn03 = (await submit(request, c, 'Đức Huy')).sub;
  for (const s of [bn01, bn02, bn03]) await waitAi(request, c.lan, s.id);
  const items = await queue(request, c.lan, c.bc.id);
  expect(items.map((x) => x.submissionId)).toEqual([bn01.id, bn02.id, bn03.id]);
  for (const it of items) {
    expect(it.ai).toEqual({ aiDone: true, shadow: true, message: MSG.s47 });
    const raw = JSON.stringify(it);
    for (const k of ['aiFlags', 'confidence', 'aiStatus', 'AI không chấm', 'AI và engine lệch']) expect(raw).not.toContain(k);
  }
});

test('Sửa điểm sau khi thấy đề xuất không đổi cặp đã lưu @TC-S5E6-26', async ({ request }) => {
  const { c, sub } = await parallelBn02(request);
  await approve(request, c.lan, sub.id, [2, 2.5, 0, 1, 0.5]);
  await request.get(`${SUB}/${sub.id}/comparison`, { headers: bearer(c.lan.token) });
  const g = await approve(request, c.lan, sub.id, [2, 3, 0, 1, 0.5]);
  expect(g.grade).toMatchObject({ seq: 2, total: 6.5 });
  const cmp = (await (await request.get(`${SUB}/${sub.id}/comparison`, { headers: bearer(c.lan.token) })).json()) as {
    pairs: number;
    criteria: Array<{ name: string; aiPoints: number; teacherPoints: number }>;
  };
  expect(cmp.pairs).toBe(5);
  expect(cmp.criteria.find((x) => x.name === 'Các bước')).toMatchObject({ aiPoints: 3, teacherPoints: 2.5 });
});
