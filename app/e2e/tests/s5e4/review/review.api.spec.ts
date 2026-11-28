import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer } from '../../s3e1/s3e1.ts';
import { ack, submitOk, P1, P2 } from '../submit/submit.ts';
import { approve, gradeRaw, gradingWorld, MSG, ok, review, SUB, type Ctx } from './grading.ts';

/**
 * US-S5E4-06 — chấm tay theo rubric, Duyệt thành điểm chính thức, nhật ký trước/sau (T-261128-60). Bài nộp luồng thật.
 */

async function grades(rq: Parameters<typeof review>[0], c: Ctx, submissionId: string) {
  const r = await rq.get(`${API}/test-hooks/s5e4/return/grades?${new URLSearchParams({ orgId: c.w.orgId, submissionId })}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as { grades: number; withStudent: number };
}

async function submitted(rq: Parameters<typeof review>[0], c: Ctx, name: string, files = [P1, P2]) {
  const p = c.w.people[name]!;
  await ack(rq, p);
  return submitOk(rq, p, c.bc.id, files);
}

test('Sửa điểm đã duyệt tạo ApprovedGrade mới, nhật ký trước 3 · sau 3,5, người thực hiện cô Lan @TC-S5E4-28', async ({ request }) => {
  const c = await gradingWorld(request);
  const bn02 = await submitted(request, c, 'Chi Mai', [P1, P2, P2]);
  const first = await approve(request, c.lan, bn02.id, [2, 3, 0, 1, 1], 'Xem lại cách tính SB');
  expect(first.grade!.total).toBe(7);
  expect(first.status).toBe('approved');
  const edited = await approve(request, c.lan, bn02.id, [2, 3.5, 0, 1, 1], 'Xem lại cách tính SB');
  expect(edited.grade!.total).toBe(7.5);
  expect(edited.grade!.seq).toBe(2);
  expect((await grades(request, c, bn02.id)).grades).toBe(2);
  const h = await ok<{ items: Array<{ action: string; actorId: string; actorName: string; totalBefore: number | null; totalAfter: number; changes: Array<{ name: string; before: number | null; after: number }> }> }>(
    await request.get(`${SUB}/${bn02.id}/grade/history`, { headers: bearer(c.lan.token) }),
  );
  expect(h.items.map((x) => x.action)).toEqual(['approve', 'edit']);
  const e = h.items[1]!;
  expect(e.changes).toEqual([{ ord: 2, name: 'Các bước', before: 3, after: 3.5 }]);
  expect(e.totalBefore).toBe(7);
  expect(e.totalAfter).toBe(7.5);
  expect(e.actorId).toBe(c.lan.id);
  expect(e.actorName).toBe(c.lan.displayName);
});

test('Điểm vượt tối đa hoặc lẻ 0,1 bị từ chối: VALIDATION_FAILED, MSG-S5-37 @TC-S5E4-29', async ({ request }) => {
  const c = await gradingWorld(request);
  const bn01 = await submitted(request, c, 'Bình An');
  const v = await review(request, c.lan, bn01.id);
  const over = await gradeRaw(request, c.lan, bn01.id, v.version, [2, 4.5, 2, 1, 1]);
  expect(over.status()).toBe(400);
  const e1 = (await over.json()) as { code: string; message: string; details: Array<{ field: string; message: string }> };
  expect(e1.code).toBe('VALIDATION_FAILED');
  expect(e1.message).toBe(MSG.s37('Các bước', '4'));
  const odd = await gradeRaw(request, c.lan, bn01.id, v.version, [2, 4, 2, 0.6, 1]);
  expect(odd.status()).toBe(400);
  const e2 = (await odd.json()) as { code: string; message: string };
  expect(e2.code).toBe('VALIDATION_FAILED');
  expect(e2.message).toBe(MSG.s37('Hình', '1'));
  expect((await grades(request, c, bn01.id)).grades).toBe(0);
});

test('Giáo viên không chủ lớp không duyệt được: NOT_FOUND, không có ApprovedGrade @TC-S5E4-30', async ({ request }) => {
  const c = await gradingWorld(request);
  const bn01 = await submitted(request, c, 'Bình An');
  const r = await gradeRaw(request, c.khoi, bn01.id, 1, [2, 4, 2, 1, 1]);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  expect((await request.get(`${API}/api/v1/teacher/grading/submissions/${bn01.id}`, { headers: bearer(c.khoi.token) })).status()).toBe(404);
  // học sinh cũng không gọi được (sai vai)
  const an = c.w.people['Bình An']!;
  expect((await gradeRaw(request, an, bn01.id, 1, [2, 4, 2, 1, 1])).status()).toBe(403);
  expect((await grades(request, c, bn01.id)).grades).toBe(0);
});

test('Hai lần duyệt đồng thời cùng version: một thành công, một CONFLICT, đúng 1 ApprovedGrade @TC-S5E4-31', async ({ request }) => {
  const c = await gradingWorld(request);
  const bn01 = await submitted(request, c, 'Bình An');
  const v = await review(request, c.lan, bn01.id);
  expect(v.version).toBe(1);
  const [a, b] = await Promise.all([
    gradeRaw(request, c.lan, bn01.id, 1, [2, 4, 2, 1, 1]),
    gradeRaw(request, c.lan, bn01.id, 1, [2, 3, 2, 1, 1]),
  ]);
  expect([a.status(), b.status()].sort()).toEqual([200, 409]);
  const loser = a.status() === 409 ? a : b;
  expect(((await loser.json()) as { code: string }).code).toBe('CONFLICT');
  expect((await grades(request, c, bn01.id)).grades).toBe(1);
});
