import { expect, test } from '../../support/fixtures.ts';
import { API, PASSWORD, bearer, codeLogin, err, teacherApi, world } from '../s3e1/s3e1.ts';
import { LOGIN_MSG, deleteAccount } from './account.ts';

/** US-S3E5-05 — xóa tài khoản qua API (mỗi ca dựng thế giới riêng, không đụng dữ liệu mẫu chung F-S3). */

test('Giáo viên không còn lớp Đang dạy xóa được tài khoản @TC-S3E5-54', async ({ request }) => {
  const w = await world(request);
  const khoi = w.people['Thầy Khôi']!;
  const archived = await teacherApi.archive(request, khoi.token, w.classes['10B']!.id);
  expect(archived.status(), await archived.text()).toBe(200);
  const r = await deleteAccount(request, khoi.token, khoi.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(r.status(), await r.text()).toBe(200);
  expect(((await r.json()) as { deleted: boolean }).deleted).toBe(true);
  // mã không đăng nhập, không khôi phục
  const login = await codeLogin(request, khoi.userCode, PASSWORD);
  expect(login.status()).toBe(400);
  expect((await err(login)).message).toBe(LOGIN_MSG);
  const recover = await request.post(`${API}/api/v1/auth/recover`, {
    data: { userCode: khoi.userCode, recoveryCode: 'RC-AAAA-BBBB-CCCC', newPassword: 'MatKhau#2027' },
  });
  expect(recover.status()).toBeGreaterThanOrEqual(400);
  expect(recover.status()).toBeLessThan(500);
  // thẻ cũ hết hiệu lực
  expect((await request.get(`${API}/api/v1/account/me`, { headers: bearer(khoi.token) })).status()).toBe(401);
});

test('Giáo viên còn lớp Đang dạy bị chặn xóa @TC-S3E5-57', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const r = await deleteAccount(request, lan.token, lan.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(r.status()).toBe(409);
  const e = await err(r);
  expect(e.code).toBe('CONFLICT');
  expect(e.message).toContain('11A1');
  expect(e.message).toContain('11A2');
  // tài khoản không đổi
  expect((await codeLogin(request, lan.userCode, PASSWORD)).status()).toBe(200);
  expect((await teacherApi.list(request, lan.token)).status()).toBe(200);
});

test('Sai mật khẩu không xóa được @TC-S3E5-58', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  const r = await deleteAccount(request, an.token, an.id, { password: 'SaiMatKhau#1', confirm: 'XÓA' });
  expect(r.status()).toBe(400);
  const e = await err(r);
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.details?.[0]?.field).toBe('password');
  // tài khoản không đổi; lần sai tính vào bộ đếm khóa đăng nhập: thêm 9 lần sai đăng nhập thì khóa
  expect((await request.get(`${API}/api/v1/account/me`, { headers: bearer(an.token) })).status()).toBe(200);
  for (let i = 0; i < 8; i++) {
    expect((await codeLogin(request, an.userCode, 'SaiMatKhau#1')).status()).toBe(400);
  }
  const locked = await codeLogin(request, an.userCode, 'SaiMatKhau#1');
  expect(locked.status(), 'lần sai thứ 10 (1 ở hộp xóa + 9 đăng nhập) thì khóa').toBe(429);
});

test('Người khác không xóa được tài khoản của em @TC-S3E5-60', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  const lan = w.people['Cô Lan']!;
  const r = await deleteAccount(request, lan.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(r.status()).toBe(404);
  expect((await err(r)).code).toBe('NOT_FOUND');
  expect((await codeLogin(request, an.userCode, PASSWORD)).status()).toBe(200);
  const students = await teacherApi.students(request, lan.token, w.classes['11A1']!.id);
  expect(await students.text()).toContain('Bình An');
});
