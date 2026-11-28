import { expect, test } from '../../support/fixtures.ts';
import { aiCalls, bearer, hook } from '../s3e1/s3e1.ts';
import { KILL, counter, readCounter, stamp } from '../s5e5/s5e5.ts';
import { Q_SC1, QA, ack, ask, askRaw, setup } from './qa.ts';

/**
 * REVIEW-S5 #2 (T-261128-70): hỏi đáp theo kho `POST /api/v1/student/kb-qa` đi qua cùng hạn mức (học sinh 30, lớp 300
 * mỗi ngày) và tắt khẩn `kb_qa` như hỏi trợ lý trên bài (BR-S5E5-12, 16). Máy chủ thật, AI giả lập.
 */

type Err = { code: string; message: string };

test('Hỏi đáp theo kho: lượt thứ 31 của em bị chặn, bạn cùng lớp vẫn hỏi được @TC-S5E5-24', async ({ request }) => {
  const { w, an, c11, bc } = await setup(request);
  const mai = w.people['Chi Mai']!;
  await ack(request, mai);
  await counter(request, w.orgId, 'student', an.id, 'qa', 30);
  await counter(request, w.orgId, 'class', c11.id, 'qa', 45);
  const before = await aiCalls(request, an.id);
  const r = await askRaw(request, an, bc.id, Q_SC1);
  expect(r.status()).toBe(429);
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  expect(((await r.json()) as Err).code).toBe('RATE_LIMITED');
  expect(await aiCalls(request, an.id)).toBe(before);
  await ask(request, mai, bc.id, Q_SC1);
  expect(await readCounter(request, w.orgId, 'class', c11.id, 'qa')).toBe(46);
  expect(await readCounter(request, w.orgId, 'student', mai.id, 'qa')).toBe(1);
});

test('Hỏi đáp theo kho: lượt thứ 301 của lớp bị chặn @TC-S5E5-21', async ({ request }) => {
  const { w, an, c11, bc } = await setup(request);
  await counter(request, w.orgId, 'class', c11.id, 'qa', 300);
  const before = await aiCalls(request, an.id);
  const r = await askRaw(request, an, bc.id, Q_SC1);
  expect(r.status()).toBe(429);
  expect(((await r.json()) as Err).code).toBe('RATE_LIMITED');
  expect(await aiCalls(request, an.id)).toBe(before);
});

test('Hỏi đáp theo kho: tắt khẩn kb_qa thì FORBIDDEN, không gọi AI @TC-S5E5-28', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  const op = w.people['Thầy Khôi']!;
  await hook(request, 's5e5/operator', { userId: op.id });
  const lane = { 'X-Geo-Test-Lane': stamp() };
  const r = await request.put(KILL, { headers: { ...bearer(op.token), ...lane }, data: { task: 'kb_qa', off: true } });
  expect(r.status(), await r.text()).toBe(200);
  try {
    const before = await aiCalls(request, an.id);
    const q = await request.post(QA, { headers: { ...bearer(an.token), ...lane }, data: { assignmentId: bc.id, question: Q_SC1 } });
    expect(q.status()).toBe(403);
    expect(((await q.json()) as Err).message).toContain('AI đang tạm tắt');
    expect(await aiCalls(request, an.id)).toBe(before);
    // lượt không mang làn này không bị ảnh hưởng
    await ask(request, an, bc.id, Q_SC1);
  } finally {
    await request.put(KILL, { headers: { ...bearer(op.token), ...lane }, data: { task: 'kb_qa', off: false } });
  }
});
