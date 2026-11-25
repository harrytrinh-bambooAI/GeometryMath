import { expect, test } from '../../support/fixtures.ts';
import { devToken } from '../e1/e1.ts';
import { API, asg, bearer, box, golden, st } from './e7.ts';

/**
 * FIX4b m5 (soát E7): phân quyền đọc sự kiện của một học sinh (US-GEO-704) theo mẫu TC-GEO-0710/0719: học sinh 403,
 * giáo viên lớp khác và trường khác 404, người không thuộc lớp 404; không lộ dữ liệu.
 */
const url = (id: string, userId: string) => `${API}/api/v1/teacher/assignments/${id}/students/${userId}/events`;

test('FIX4b m5 đọc sự kiện: học sinh 403, giáo viên lớp khác/trường khác 404, người ngoài lớp 404', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const an = st(b, 'An');
  const ok = await request.get(url(id, an.id), { headers: bearer(b.teacher.token) });
  expect(ok.status(), await ok.text()).toBe(200);

  const asStudent = await request.get(url(id, an.id), { headers: bearer(an.token) });
  expect(asStudent.status()).toBe(403);
  expect((await asStudent.json()).code).toBe('FORBIDDEN');

  const other = await box(request, golden());
  const otherTeacher = await request.get(url(id, an.id), { headers: bearer(other.teacher.token) });
  expect(otherTeacher.status()).toBe(404);
  expect(await otherTeacher.text()).not.toContain('occurredAt');

  const otherSchool = await request.get(url(id, an.id), { headers: bearer(await devToken(request, 'F-GEO-003')) });
  expect(otherSchool.status()).toBe(404);

  const stranger = await request.get(url(id, st(other, 'An').id), { headers: bearer(b.teacher.token) });
  expect(stranger.status()).toBe(404);
});

test('FIX4b m8 đọc sự kiện: khoảng thời gian trên 366 ngày bị từ chối', async ({ request }) => {
  const b = await box(request, golden());
  const to = new Date().toISOString();
  const from = new Date(Date.now() - 400 * 86_400_000).toISOString();
  const r = await request.get(`${url(asg(b, 'BG-1'), st(b, 'An').id)}?from=${from}&to=${to}`, { headers: bearer(b.teacher.token) });
  expect(r.status()).toBe(400);
  expect((await r.json()).code).toBe('VALIDATION_FAILED');
});
