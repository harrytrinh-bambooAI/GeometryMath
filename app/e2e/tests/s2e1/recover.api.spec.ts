import { expect, test } from '../../support/fixtures.ts';
import { bearer, lane } from '../e1/e1.ts';
import { HKG01 } from '../e2/e2.ts';
import { API, FX, MSG, RECOVERY_CODE, ipOf, login, recover, register } from './s2e1.ts';
import type { Err } from './s2e1.ts';

/** US-S2E1-03 — giáo viên và học sinh tự khôi phục mật khẩu bằng mã khôi phục (PO-61, PO-66 G3). */

type Recovered = { userCode: string; role: string; recoveryCode: string };

async function wrongRecovery(request: Parameters<typeof recover>[0], code: string, times: number) {
  for (let i = 1; i <= times; i++) {
    const r = await recover(request, code, `RC-ZZZZ-ZZZZ-ZZ${'ABCDEFGHJK'[i % 10]}${'23456789'[i % 8]}`, 'MatKhauMoi#1');
    expect(r.status(), `lần sai ${i}`).toBe(400);
    expect(((await r.json()) as Err).message).toBe(MSG.recovery);
  }
}

test('Mật khẩu cũ và mã khôi phục cũ hết hiệu lực @TC-S2E1-032', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'Cô Trang', role: 'teacher', password: 'Trang@2026x' });
  const r = await recover(request, u.userCode, u.recoveryCode, 'Trang@2027y');
  expect(r.status()).toBe(200);
  const rec = (await r.json()) as Recovered;
  expect(rec.recoveryCode).toMatch(RECOVERY_CODE);
  expect(rec.recoveryCode).not.toBe(u.recoveryCode);
  const old = await login(request, u.userCode, 'Trang@2026x');
  expect(old.status()).toBe(400);
  expect(((await old.json()) as Err).message).toBe(MSG.login);
  const reuse = await recover(request, u.userCode, u.recoveryCode, 'Trang@2028z');
  expect(reuse.status()).toBe(400);
  expect(((await reuse.json()) as Err)).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.recovery });
  expect((await login(request, u.userCode, 'Trang@2027y')).status()).toBe(200);
});

test('Sai 9 lần chưa khóa, sai 10 lần khóa đường khôi phục @TC-S2E1-034', async ({ request }, testInfo) => {
  // nhánh A: sai 9 lần, lần 10 đúng thì thành công
  const a = await register(request, ipOf(testInfo), { name: 'Cô A', role: 'teacher' });
  await wrongRecovery(request, a.userCode, 9);
  expect((await recover(request, a.userCode, a.recoveryCode, 'MatKhauMoi#A')).status()).toBe(200);
  // nhánh B: sai 10 lần thì khóa 15 phút; lần 11 đúng vẫn bị từ chối; đăng nhập bằng mật khẩu cũ vẫn được
  const b = await register(request, ipOf(testInfo, 'b'), { name: 'Cô B', role: 'teacher' });
  await wrongRecovery(request, b.userCode, 9);
  const tenth = await recover(request, b.userCode, 'RC-ZZZZ-ZZZZ-ZZZZ', 'MatKhauMoi#B');
  expect(tenth.status()).toBe(429);
  const eleventh = await recover(request, b.userCode, b.recoveryCode, 'MatKhauMoi#B');
  expect(eleventh.status()).toBe(429);
  expect(Number(eleventh.headers()['retry-after'])).toBeGreaterThan(800);
  expect(((await eleventh.json()) as Err).code).toBe('RATE_LIMITED');
  expect((await login(request, b.userCode, b.password)).status()).toBe(200);
});

test('Học sinh Tự học khôi phục được bằng mã khôi phục của mình @TC-S2E1-035', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'An', password: 'AnHoc#2026' });
  // lịch sử Tự học trước khi quên mật khẩu
  const self = await request.post(`${API}/api/v1/student/self-study`, {
    headers: { ...bearer(u.token), 'X-Geo-Test-Lane': lane(testInfo) },
    data: { statement: HKG01 },
  });
  expect(self.status(), await self.text()).toBe(200);
  const assignmentId = ((await self.json()) as { assignmentId: string }).assignmentId;

  const r = await recover(request, u.userCode, u.recoveryCode, 'AnMoi#2027');
  expect(r.status()).toBe(200);
  expect(((await r.json()) as Recovered).recoveryCode).toMatch(RECOVERY_CODE);
  const s = await login(request, u.userCode, 'AnMoi#2027');
  expect(s.status()).toBe(200);
  const session = (await s.json()) as { token: string; userId: string };
  expect(session.userId).toBe(u.userId);
  const again = await request.get(`${API}/api/v1/student/self-study/${assignmentId}`, { headers: bearer(session.token) });
  expect(again.status()).toBe(200);
  expect(await again.text()).toContain('S.ABCD');
});

test('Mật khẩu mới 7 ký tự bị chặn, 8 ký tự nhận @TC-S2E1-036', async ({ request }, testInfo) => {
  const u = await register(request, ipOf(testInfo), { name: 'Cô Trang', role: 'teacher' });
  const short = await recover(request, u.userCode, u.recoveryCode, 'Abc1234');
  expect(short.status()).toBe(400);
  expect(((await short.json()) as Err).details?.[0]).toMatchObject({ field: 'newPassword', message: MSG.password });
  const ok = await recover(request, u.userCode, u.recoveryCode, 'Abcd1234'); // mã khôi phục vẫn còn hiệu lực
  expect(ok.status()).toBe(200);
  expect((await login(request, u.userCode, 'Abcd1234')).status()).toBe(200);
});

test('Học sinh Chờ duyệt khôi phục được mật khẩu @TC-S2E1-045', async ({ request }) => {
  const binh = FX('F-S2-003');
  // đặt lại về đúng mật khẩu cũ để ca khác dùng Bình (TC-S2E1-028) không bị ảnh hưởng
  const r = await recover(request, binh.userCode, binh.recoveryCode, binh.password);
  expect(r.status(), await r.text()).toBe(200);
  const s = await login(request, binh.userCode, binh.password);
  expect(s.status()).toBe(200);
  const session = (await s.json()) as { token: string; userId: string };
  expect(session.userId).toBe(binh.id); // không phải tạo tài khoản mới
  const me = await request.get(`${API}/api/v1/account/me`, { headers: bearer(session.token) });
  expect(((await me.json()) as { classes: Array<{ className: string; status: string }> }).classes).toEqual([
    { className: '11A1', status: 'pending' },
  ]);
});

test('Mã khôi phục của người khác không dùng được cho mã HS khác @TC-S2E1-046', async ({ request }, testInfo) => {
  const ip = ipOf(testInfo);
  const an = await register(request, ip, { name: 'An', password: 'AnHoc#2026' });
  const binh = await register(request, ip, { name: 'Bình', password: 'BinhHoc#2026' });
  const cross = await recover(request, an.userCode, binh.recoveryCode, 'AnMoi#2027');
  expect(cross.status()).toBe(400);
  expect(((await cross.json()) as Err).message).toBe(MSG.recovery);
  expect((await login(request, an.userCode, 'AnHoc#2026')).status()).toBe(200); // mật khẩu của An không đổi
  // lần sai đó tính vào bộ đếm khôi phục của An: thêm 8 lần sai vẫn chưa khóa, lần thứ 10 thì khóa
  await wrongRecovery(request, an.userCode, 8);
  expect((await recover(request, an.userCode, 'RC-ZZZZ-ZZZZ-ZZZZ', 'AnMoi#2027')).status()).toBe(429);
  expect((await recover(request, binh.userCode, binh.recoveryCode, 'BinhMoi#2027')).status()).toBe(200);
});
