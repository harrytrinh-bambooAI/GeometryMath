import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer } from '../../s3e1/s3e1.ts';
import { bc01, rb01, BC01_DUE } from '../s5e4.ts';
import { ack, submitOk, P1 } from '../submit/submit.ts';
import { FIVE, gradingWorld, ok, queue, sc1, type Queue } from '../review/grading.ts';

/**
 * US-S5E4-05 — hàng chờ chấm theo bài (T-261128-60): bài có cờ trước rồi theo giờ nộp, dòng tóm tắt, số bài chờ cho
 * điện thoại (không tên học sinh), lớp của giáo viên khác NOT_FOUND, bài chưa ai nộp thì rỗng kèm hạn nộp.
 */

test('Hàng chờ BC-01 sau hạn: cờ trước, rồi theo giờ nộp, tóm tắt, không cột xếp hạng @TC-S5E4-23', async ({ request }) => {
  const c = await gradingWorld(request);
  const { bn01, bn02, bn03, bn05 } = await sc1(request, c);
  const q = await ok<Queue>(await queue(request, c.lan, { classId: c.c11.id, assignmentId: c.bc.id }));
  expect(q.items.map((i) => i.submissionId)).toEqual([bn03.id, bn05.id, bn01.id, bn02.id]);
  expect(q.items[0]!.flags.map((f) => f.label)).toEqual(['Chưa đọc được']);
  expect(q.items[1]!.flags.map((f) => f.label)).toEqual(['Nộp muộn']);
  expect(q.items[2]!.flags).toEqual([]);
  expect(q.items[3]!.flags).toEqual([]);
  expect(q.summaryLine).toBe('4 chờ chấm · 1 chưa nộp · 0 đã duyệt');
  expect(q.state).toBe('ready');
  // không có cột điểm hay xếp hạng
  for (const i of q.items) {
    expect(Object.keys(i)).not.toEqual(expect.arrayContaining(['score']));
    expect(Object.keys(i).some((k) => /rank|score|total|point/i.test(k))).toBe(false);
  }
});

test('Số bài chờ cho màn điện thoại theo từng bài, không có tên học sinh @TC-S5E4-24', async ({ request }) => {
  const c = await gradingWorld(request);
  await sc1(request, c);
  const r = await request.get(`${API}/api/v1/teacher/grading/queue/summary`, { headers: bearer(c.lan.token) });
  const text = await r.text();
  expect(r.status(), text).toBe(200);
  const s = JSON.parse(text) as { items: Array<{ assignmentId: string; waiting: number; processing: number }> };
  expect(s.items.find((x) => x.assignmentId === c.bc.id)).toMatchObject({ waiting: 4, processing: 0 });
  for (const name of FIVE) expect(text).not.toContain(name);
  expect(r.headers()['cache-control']).toContain('no-store');
});

test('Hàng chờ không có bài của lớp khác: NOT_FOUND @TC-S5E4-25', async ({ request }) => {
  const c = await gradingWorld(request);
  // 10B của thầy Khôi có 3 bài Chờ chấm
  const rb = await rb01(request, c.khoi);
  const bk = await bc01(request, c.khoi, c.c10.id, rb.id);
  for (const n of ['Mi Na', 'Tí Nị', 'Bo Bo']) {
    const p = c.w.people[n]!;
    await ack(request, p);
    await submitOk(request, p, bk.id, [P1]);
  }
  const own = await ok<Queue>(await queue(request, c.khoi, { classId: c.c10.id }));
  expect(own.items).toHaveLength(3);
  const r = await queue(request, c.lan, { classId: c.c10.id });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  const r2 = await queue(request, c.lan, { assignmentId: bk.id });
  expect(r2.status()).toBe(404);
});

test('Trống khi chưa có bài nộp: danh sách rỗng, state empty kèm hạn nộp @TC-S5E4-26', async ({ request }) => {
  const c = await gradingWorld(request);
  const q = await ok<Queue>(await queue(request, c.lan, { assignmentId: c.bc.id }));
  expect(q.items).toEqual([]);
  expect(q.state).toBe('empty');
  expect(q.dueAt).toBe(BC01_DUE);
  expect(q.summary.notSubmitted).toBe(5);
});
