import { createHash } from 'node:crypto';
import type { APIRequestContext, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, err, teacherApi, world } from '../s3e1/s3e1.ts';

/** US-S3E5-03 — danh sách trường được phép: máy chủ từ chối trường thừa, tên hiển thị như email/điện thoại. */

const MSG82 = 'Tên hiển thị không nên là email hay số điện thoại.';

/** Mỗi ca một địa chỉ mạng riêng: bộ đếm đăng ký theo mạng (BR-S2E1-06) không chặn ca khác. */
const ipOf = (t: TestInfo, n = 0) => {
  const h = createHash('sha256').update(`s3e5-fields|${t.titlePath.join('/')}|${t.project.name}|${n}`).digest();
  return `10.${190 + (h[0]! % 40)}.${h[1]}.${(h[2]! % 250) + 1}`;
};

const BASE = { role: 'student', password: 'MatKhau#2026', privacyAck: true, privacyVersion: '1.0' };

const register = (rq: APIRequestContext, t: TestInfo, data: Record<string, unknown>, n = 0) =>
  rq.post(`${API}/api/v1/auth/register`, { data, headers: { 'X-Forwarded-For': ipOf(t, n) } });

test('Đăng ký có trường thừa bị từ chối @TC-S3E5-29', async ({ request }, testInfo) => {
  for (const [i, [field, value]] of [
    ['email', 'mai@example.org'],
    ['phone', '0912345678'],
  ].entries()) {
    const r = await register(request, testInfo, { displayName: 'Mai', ...BASE, [field!]: value }, i);
    expect(r.status()).toBe(400);
    const e = await err(r);
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details?.[0]?.field).toBe(field);
    // không tạo tài khoản: phản hồi không có mã người dùng; giá trị không quay lại phản hồi
    const text = JSON.stringify(e);
    expect(text).not.toContain(value!);
    expect(text).not.toContain('userCode');
  }
});

test('Biên số điện thoại: 9 chữ số bị chặn, 8 chữ số được nhận @TC-S3E5-34', async ({ request }, testInfo) => {
  const nine = await register(request, testInfo, { displayName: '0912 345 678', ...BASE }, 0);
  expect(nine.status()).toBe(400);
  const e = await err(nine);
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.details?.[0]).toEqual({ field: 'displayName', message: MSG82 });
  for (const [i, name] of ['Lớp 12345678', 'Minh 10A'].entries()) {
    const r = await register(request, testInfo, { displayName: name, ...BASE }, i + 1);
    expect(r.status(), await r.text()).toBe(200);
    expect(((await r.json()) as { displayName: string }).displayName).toBe(name);
  }
});

test('Tên có dấu tiếng Việt và ký tự thường gặp vẫn được nhận @TC-S3E5-35', async ({ request }, testInfo) => {
  for (const [i, name] of ['Nguyễn Thị Ánh Tuyết', 'Đức-Huy (11A1)'].entries()) {
    const r = await register(request, testInfo, { displayName: name, ...BASE }, i);
    expect(r.status(), await r.text()).toBe(200);
    const reg = (await r.json()) as { displayName: string; token: string };
    expect(reg.displayName).toBe(name);
    const me = await request.get(`${API}/api/v1/account/me`, { headers: bearer(reg.token) });
    expect(((await me.json()) as { displayName: string }).displayName).toBe(name);
  }
});

test('Thân yêu cầu có khóa biến thể tên cấm @TC-S3E5-36', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const before = ((await (await teacherApi.list(request, lan)).json()) as { items: unknown[] }).items.length;
  for (const extra of [{ schoolName: 'THPT Mẫu A' }, { Truong: 'THPT Mẫu A' }]) {
    const r = await teacherApi.create(request, lan, { name: '12C', grade: 12, ...extra });
    expect(r.status()).toBe(400);
    const e = await err(r);
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details?.[0]?.field).toBe(Object.keys(extra)[0]);
  }
  const after = (await (await teacherApi.list(request, lan)).json()) as { items: Array<{ name: string }> };
  expect(after.items.length).toBe(before);
  expect(JSON.stringify(after)).not.toContain('THPT Mẫu A');
});

test('Mã trường kỹ thuật không lộ ra ngoài @TC-S3E5-39', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const an = w.people['Bình An']!;
  const c11 = w.classes['11A1']!.id;
  const calls = [
    [`/api/v1/account/me`, lan.token],
    [`/api/v1/account/me`, an.token],
    [`/api/v1/account/privacy`, an.token],
    [`/api/v1/teacher/my-classes`, lan.token],
    [`/api/v1/teacher/my-classes/${c11}`, lan.token],
    [`/api/v1/teacher/my-classes/${c11}/students`, lan.token],
    [`/api/v1/teacher/my-classes/${c11}/pending`, lan.token],
    [`/api/v1/student/classes`, an.token],
    [`/api/v1/teacher/assignment-sets`, lan.token],
    [`/api/v1/student/assignment-sets`, an.token],
    [`/api/v1/teacher/overview`, lan.token],
    [`/api/v1/teacher/class-results/classes`, lan.token],
    [`/api/v1/notifications`, an.token],
  ] as const;
  for (const [path, token] of calls) {
    const r = await request.get(`${API}${path}`, { headers: bearer(token) });
    expect(r.status(), path).toBeLessThan(500);
    const text = await r.text();
    expect(text, path).not.toMatch(/"org_?id"/i);
    expect(text, path).not.toContain(w.orgId);
    expect(text, path).not.toMatch(/"(school|truong)[A-Za-z_]*"/i);
  }
  const r = await teacherApi.create(request, lan.token, { name: '12D', grade: 12, org_id: w.orgId });
  expect(r.status()).toBe(400);
  expect((await err(r)).details?.[0]?.field).toBe('org_id');
});
