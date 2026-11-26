import { expect, test } from '../../support/fixtures.ts';
import { API, FX, MSG, ipOf, login, register, xff } from './s2e1.ts';
import type { Err } from './s2e1.ts';

/**
 * US-S2E1-02 — đăng nhập bằng mã + mật khẩu; khóa 10 lần sai liên tiếp trong 15 phút theo tài khoản (PO-53).
 * Ca làm khóa tài khoản dùng tài khoản tự đăng ký riêng (không đụng FX-02 mà ca UI đăng nhập).
 */

async function wrong(request: Parameters<typeof login>[0], code: string, times: number, ip?: string) {
  for (let i = 1; i <= times; i++) {
    const r = await request.post(`${API}/api/v1/auth/code-login`, {
      headers: ip ? xff(ip) : undefined,
      data: { userCode: code, password: `sai-${i}-mat-khau` },
    });
    expect(r.status(), `lần sai ${i}`).toBe(400);
    expect(((await r.json()) as Err).message).toBe(MSG.login);
  }
}

test('Mã gõ chữ thường và có dấu cách vẫn vào được @TC-S2E1-021', async ({ request }) => {
  const an = FX('F-S2-002');
  const r = await login(request, ` ${an.userCode.toLowerCase()} `, an.password);
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const s = (await r.json()) as { token: string; userId: string; displayName: string; userCode: string };
  expect(s.userId).toBe(an.id);
  expect(s.displayName).toBe('An');
  expect(s.userCode).toBe(an.userCode);
  expect(s.token).toBeTruthy();
});

test('Mã không tồn tại trả đúng cùng một phản hồi @TC-S2E1-023', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'An dò' });
  const ghost = await login(request, 'HS-ZZZZ-ZZZZ', 'MatKhau#2026');
  const real = await login(request, u.userCode, 'sai-mat-khau-1');
  expect(ghost.status()).toBe(real.status());
  const a = (await ghost.json()) as Err;
  const b = (await real.json()) as Err;
  expect(a.code).toBe('VALIDATION_FAILED');
  expect(a.code).toBe(b.code);
  expect(a.message).toBe(MSG.login);
  expect(a.message).toBe(b.message);
  expect(Object.keys(a).sort()).toEqual(Object.keys(b).sort());
  expect(a.details ?? []).toEqual(b.details ?? []);
  expect(a.details ?? []).toHaveLength(0); // không chỉ ra ô nào sai
});

test('Sai 9 lần chưa bị khóa @TC-S2E1-024', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'An chín' });
  await wrong(request, u.userCode, 9);
  const ok = await login(request, u.userCode, u.password);
  expect(ok.status()).toBe(200);
  // bộ đếm về 0: thêm 9 lần sai vẫn chưa khóa
  await wrong(request, u.userCode, 9);
  expect((await login(request, u.userCode, u.password)).status()).toBe(200);
});

test('Sai lần thứ 10 thì khóa @TC-S2E1-025', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'An mười' });
  await wrong(request, u.userCode, 9);
  const tenth = await login(request, u.userCode, 'sai-lan-10');
  expect(tenth.status()).toBe(429);
  expect(((await tenth.json()) as Err)).toMatchObject({ code: 'RATE_LIMITED', message: MSG.locked });
  const eleventh = await login(request, u.userCode, u.password);
  expect(eleventh.status()).toBe(429);
  const retry = Number(eleventh.headers()['retry-after']);
  expect(retry).toBeGreaterThan(800);
  expect(retry).toBeLessThanOrEqual(900);
  const body = (await eleventh.json()) as Err & { token?: string };
  expect(body.code).toBe('RATE_LIMITED');
  expect(body.token).toBeUndefined();
});

test('Đăng nhập đúng ở lần thứ 9 làm bộ đếm về 0 @TC-S2E1-027', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'An đếm' });
  await wrong(request, u.userCode, 9);
  expect((await login(request, u.userCode, u.password)).status()).toBe(200);
  await wrong(request, u.userCode, 9); // không cộng dồn qua lần đúng: vẫn 400, chưa 429
  expect((await login(request, u.userCode, u.password)).status()).toBe(200);
});

test('Khóa tài khoản này không ảnh hưởng tài khoản khác cùng máy @TC-S2E1-028', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const locked = await register(request, ip, { name: 'An bị khóa' });
  await wrong(request, locked.userCode, 9, ip);
  const tenth = await request.post(`${API}/api/v1/auth/code-login`, {
    headers: xff(ip),
    data: { userCode: locked.userCode, password: 'sai-lan-10' },
  });
  expect(tenth.status()).toBe(429);
  const binh = FX('F-S2-003');
  const r = await request.post(`${API}/api/v1/auth/code-login`, {
    headers: xff(ip),
    data: { userCode: binh.userCode, password: binh.password },
  });
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { userId: string }).userId).toBe(binh.id);
});
