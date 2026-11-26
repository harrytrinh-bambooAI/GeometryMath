import { expect, test } from '../../support/fixtures.ts';
import { bearer } from '../e1/e1.ts';
import { API, MSG, RECOVERY_CODE, USER_CODE, ageRegisterLimit, ipOf, login, register, registerRaw } from './s2e1.ts';
import type { Err, Registered } from './s2e1.ts';

/** US-S2E1-01 — đăng ký bằng tên hiển thị + vai (PO-61), gọi thẳng máy chủ. */

const body = (over: Record<string, unknown> = {}) => ({
  displayName: 'An',
  role: 'student',
  password: 'AnHoc#2026',
  privacyAck: true,
  privacyVersion: '1.0',
  ...over,
});

test('Mã khôi phục không hiện lại lần hai @TC-S2E1-004', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const r = await registerRaw(request, ip, body({ displayName: 'Cô Trang', role: 'teacher', password: 'Trang@2026x' }));
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const reg = (await r.json()) as Registered;
  expect(reg.recoveryCode).toMatch(RECOVERY_CODE);
  const again = await login(request, reg.userCode, 'Trang@2026x');
  expect(again.status()).toBe(200);
  const text = await again.text();
  expect(text).not.toContain(reg.recoveryCode);
  expect(text).not.toMatch(/RC-/);
  const token = ((await again.json()) as { token: string }).token;
  const me = await request.get(`${API}/api/v1/account/me`, { headers: bearer(token) });
  expect(me.status()).toBe(200);
  const meText = await me.text();
  expect(meText).not.toMatch(/RC-/);
  expect(JSON.parse(meText)).toMatchObject({ userCode: reg.userCode, hasRecoveryCode: true });
});

test('Hai người cùng tên có hai mã khác nhau @TC-S2E1-005', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const a = await register(request, ip, { name: 'An', password: 'AnHoc#2026' });
  const b = await register(request, ip, { name: 'An', password: 'AnKhac#2026' });
  expect(a.userCode).toMatch(/^HS-/);
  expect(b.userCode).toMatch(USER_CODE);
  expect(b.userCode).not.toBe(a.userCode);
  expect(b.userId).not.toBe(a.userId);
  for (const [u, pw] of [
    [a, 'AnHoc#2026'],
    [b, 'AnKhac#2026'],
  ] as const) {
    const r = await login(request, u.userCode, pw);
    expect(r.status()).toBe(200);
    expect(((await r.json()) as { userId: string }).userId).toBe(u.userId);
  }
});

test('Yêu cầu đăng ký chỉ chứa các trường cho phép @TC-S2E1-006', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const allowed = body({ displayName: 'Bình' });
  expect(Object.keys(allowed).sort()).toEqual(['displayName', 'password', 'privacyAck', 'privacyVersion', 'role']);
  const ok = await registerRaw(request, ip, allowed);
  expect(ok.status()).toBe(200);
  // trường thừa (email, điện thoại, ngày sinh, trường, ảnh): BR-S3E5-34 (PO-61, US-S3E5-03-SC3) thay hợp đồng cũ
  // "bỏ qua" — bị từ chối VALIDATION_FAILED, không tạo tài khoản
  const extra = await registerRaw(request, ip, {
    ...body({ displayName: 'Bình 2' }),
    email: 'binh@example.com',
    phone: '0912345678',
    birthDate: '2010-01-01',
    school: 'THPT A',
    photo: 'data:image/png;base64,AAAA',
  });
  expect(extra.status()).toBe(400);
  const rejected = (await extra.json()) as { code: string; details: { field: string }[] };
  expect(rejected.code).toBe('VALIDATION_FAILED');
  expect(['email', 'phone', 'birthDate', 'school', 'photo']).toContain(rejected.details[0]!.field);
  const reg = (await ok.json()) as Registered;
  const me = await request.get(`${API}/api/v1/account/me`, { headers: bearer(reg.token) });
  const text = await me.text();
  for (const leaked of ['binh@example.com', '0912345678', '2010-01-01', 'THPT A', 'base64']) expect(text).not.toContain(leaked);
  expect(Object.keys(JSON.parse(text)).sort()).toEqual(['classes', 'displayName', 'hasRecoveryCode', 'privacyVersion', 'role', 'userCode']);
});

test('Tên 1 ký tự và khoảng trắng đầu cuối được cắt @TC-S2E1-010', async ({ request }, testInfo) => {
  const r = await registerRaw(request, ipOf(testInfo), body({ displayName: '  B  ' }));
  expect(r.status()).toBe(200);
  expect(((await r.json()) as Registered).displayName).toBe('B');
});

test('Mật khẩu 8 ký tự được nhận @TC-S2E1-011', async ({ request }, testInfo) => {
  const r = await registerRaw(request, ipOf(testInfo), body({ password: 'Abcd1234' }));
  expect(r.status()).toBe(200);
  const reg = (await r.json()) as Registered;
  expect((await login(request, reg.userCode, 'Abcd1234')).status()).toBe(200);
});

test('Mật khẩu 64 nhận, 65 bị chặn @TC-S2E1-013', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const ok = await registerRaw(request, ip, body({ displayName: 'Dài 64', password: 'a'.repeat(64) }));
  expect(ok.status()).toBe(200);
  const bad = await registerRaw(request, ip, body({ displayName: 'Dài 65', password: 'a'.repeat(65) }));
  expect(bad.status()).toBe(400);
  const e = (await bad.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.details?.[0]).toMatchObject({ field: 'password', message: MSG.password });
  expect(JSON.stringify(e)).not.toContain('userCode');
});

test('Chưa tick thông báo riêng tư thì không đăng ký được @TC-S2E1-014', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  for (const ack of [false, null]) {
    const r = await registerRaw(request, ip, body({ displayName: 'Chưa tick', privacyAck: ack }));
    expect(r.status()).toBe(400);
    expect(((await r.json()) as Err).code).toBe('VALIDATION_FAILED');
  }
  // không tài khoản nào được tạo: IP vẫn còn đủ 5 lượt đăng ký
  for (let i = 0; i < 5; i++) expect((await registerRaw(request, ip, body({ displayName: `Sau ${i}` }))).status()).toBe(200);
});

test('Vai không hợp lệ bị từ chối @TC-S2E1-015', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  for (const role of ['admin', '']) {
    const r = await registerRaw(request, ip, body({ displayName: 'Vai lạ', role }));
    expect(r.status(), role).toBe(400);
    const e = (await r.json()) as Err;
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details?.[0]?.field).toBe('role');
  }
  for (let i = 0; i < 5; i++) expect((await registerRaw(request, ip, body({ displayName: `Sau ${i}` }))).status()).toBe(200);
});

test('Lần đăng ký thứ 5 trong giờ nhận, lần thứ 6 bị giới hạn @TC-S2E1-016', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  for (let i = 1; i <= 5; i++) {
    const r = await registerRaw(request, ip, body({ displayName: `Bạn ${i}` }));
    expect(r.status(), `lần ${i}`).toBe(200);
  }
  const sixth = await registerRaw(request, ip, body({ displayName: 'Bạn 6' }));
  expect(sixth.status()).toBe(429);
  const wait = Number(sixth.headers()['retry-after']);
  expect(wait).toBeGreaterThan(0);
  expect(wait).toBeLessThanOrEqual(3600);
  const e = (await sixth.json()) as Err & { userCode?: string };
  expect(e.code).toBe('RATE_LIMITED');
  expect(e.userCode).toBeUndefined();
  await ageRegisterLimit(request, ip, wait); // "chờ theo Retry-After"
  const after = await registerRaw(request, ip, body({ displayName: 'Bạn 7' }));
  expect(after.status()).toBe(200);
});
