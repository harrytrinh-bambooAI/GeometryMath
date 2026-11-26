import { expect, test } from '../../support/fixtures.ts';
import { board051, student } from '../s3e3/s3e3.ts';
import { API, MSG, bearer, csvLines, exportUrl } from './s3e6.ts';

/** US-S3E6-02 qua API: tệp xuất chỉ có trường được phép, giới hạn 10 lần/giờ, quyền. */

test('Tệp xuất chỉ có trường được phép @TC-S3E6-14', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(exportUrl(sb), { headers: bearer(sb.teacher.token) });
  expect(r.status()).toBe(200);
  expect(r.headers()['content-type']).toContain('text/csv');
  const text = await r.text();
  const lines = csvLines(text);
  const header = lines[0]!.split(',');
  expect(header.slice(0, 3)).toEqual(['Tên hiển thị', 'Mã người dùng', 'Đã rời']);
  expect(header.slice(3).every((h) => /^Đề \d+$/.test(h))).toBe(true);
  const allowedCells = new Set(['Đúng', 'Xem lại', 'Đang làm', 'Chưa làm']);
  for (const l of lines.slice(1)) {
    const c = l.split(',');
    expect(c[1]).toMatch(/^HS-[A-Z0-9]{4}-[A-Z0-9]{4}$/);
    expect(['', 'Có']).toContain(c[2]);
    for (const v of c.slice(3)) expect(allowedCells.has(v)).toBe(true);
  }
  // không câu hỏi, đáp số, IP, mã lớp, tên giáo viên
  expect(text).not.toMatch(/HK-[A-Z0-9]{4}|Cô Lan|\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|\?/);
});

test('Xuất quá 10 lần trong 1 giờ @TC-S3E6-20', async ({ request }) => {
  const sb = await board051(request);
  for (let i = 0; i < 10; i++) {
    expect((await request.get(exportUrl(sb), { headers: bearer(sb.teacher.token) })).status()).toBe(200);
  }
  const limited = await request.get(exportUrl(sb), { headers: bearer(sb.teacher.token) });
  expect(limited.status()).toBe(429);
  expect(Number(limited.headers()['retry-after'])).toBeGreaterThan(0);
  const e = (await limited.json()) as { code: string; message: string };
  expect(e.code).toBe('RATE_LIMITED');
  expect(e.message).toBe(MSG.s94);
  // hết giờ giới hạn: xuất lại được
  const aged = await request.post(`${API}/test-hooks/s3e6/exports/age`, { data: { teacherId: sb.teacher.id, seconds: 3601 } });
  expect(aged.status()).toBe(200);
  expect((await request.get(exportUrl(sb), { headers: bearer(sb.teacher.token) })).status()).toBe(200);
});

test('Giáo viên khác không xuất được lớp 11A1 @TC-S3E6-21', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(exportUrl(sb), { headers: bearer(sb.otherTeacher.token) });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  expect(text).not.toContain('Bình An');
});

test('Học sinh không xuất được @TC-S3E6-22', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(exportUrl(sb), { headers: bearer(student(sb, 'Bình An').token) });
  expect(r.status()).toBe(403);
  expect(r.headers()['content-type']).not.toContain('text/csv');
});
