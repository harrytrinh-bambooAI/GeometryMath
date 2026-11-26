import { expect, test } from '../../support/fixtures.ts';
import { API, FX, MSG, RECOVERY_CODE, ipOf, login, loginUI, register, revokeSessions, setTempPassword, useIp } from './s2e1.ts';

/**
 * US-S2E1-02 (đăng nhập bằng mã), US-S2E1-03 (khôi phục), US-S2E1-04-SC4 (đặt mật khẩu mới lần đầu) — màn C3-01.
 * FX-01/FX-02 chỉ dùng cho ca không làm bẩn; ca đổi mật khẩu tự đăng ký tài khoản riêng.
 */

test.beforeEach(async ({ page }, testInfo) => {
  await useIp(page, ipOf(testInfo));
});

test('Học sinh đăng nhập đúng @TC-S2E1-019 @smoke', async ({ page }) => {
  const an = FX('F-S2-002');
  await loginUI(page, 'hs', an.userCode, an.password);
  await expect(page).toHaveURL(/\/hs\/bai-cua-em$/);
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
});

test('Giáo viên đăng nhập đúng @TC-S2E1-020 @smoke', async ({ page }) => {
  const trang = FX('F-S2-001');
  await loginUI(page, 'gv', trang.userCode, trang.password);
  await expect(page).toHaveURL(/\/gv\/kho-de$/);
  await expect(page.getByTestId('e1-catalog')).toBeVisible();
  await expect(page.getByTestId('e1-teacher-name')).toContainText('Cô Trang');
});

test('Sai mật khẩu hiện câu chung @TC-S2E1-022', async ({ page }) => {
  const an = FX('F-S2-002');
  await loginUI(page, 'hs', an.userCode, 'SaiMatKhau#1');
  await expect(page.getByTestId('s2e1-login-error')).toHaveText(MSG.login);
  await expect(page).toHaveURL(/\/hs\/dang-nhap-ma$/);
  await expect(page.getByTestId('s2e1-login-code-error')).toHaveCount(0);
  await expect(page.getByTestId('s2e1-login-password-error')).toHaveCount(0);
});

test('Để trống một trong hai ô @TC-S2E1-029', async ({ page }) => {
  let calls = 0;
  page.on('request', (r) => {
    if (r.url().includes('/api/v1/auth/code-login')) calls++;
  });
  await page.goto('/hs/dang-nhap-ma');
  await page.getByTestId('s2e1-login-password').fill('AnHoc#2026');
  await page.getByTestId('s2e1-login-submit').click();
  await expect(page.getByTestId('s2e1-login-code-error')).toHaveText('Hãy nhập mã người dùng.');
  await page.getByTestId('s2e1-login-code').fill(FX('F-S2-002').userCode);
  await page.getByTestId('s2e1-login-password').fill('');
  await page.getByTestId('s2e1-login-submit').click();
  await expect(page.getByTestId('s2e1-login-password-error')).toHaveText('Hãy nhập mật khẩu.');
  await expect(page.getByTestId('s2e1-login-code-error')).toHaveCount(0);
  expect(calls).toBe(0); // không gửi yêu cầu nên không tính một lần sai
});

test('Phiên hết hạn thì yêu cầu bị từ chối @TC-S2E1-030', async ({ page, request }, testInfo) => {
  const u = await register(request, ipOf(testInfo, 'api'), { name: 'An phiên', password: 'AnHoc#2026' });
  await loginUI(page, 'hs', u.userCode, 'AnHoc#2026');
  await expect(page).toHaveURL(/\/hs\/bai-cua-em$/);
  await revokeSessions(request, u.userCode); // phiên hết hạn ở máy chủ
  await page.getByTestId('hs-nav-overview').click();
  const rejected = page.waitForResponse((r) => r.url().includes('/api/v1/student/') && r.status() === 401);
  await page.getByTestId('e1-nav-bai-cua-em').click();
  expect((await rejected).status()).toBe(401);
  // cùng phiên gọi thẳng máy chủ: mã lỗi UNAUTHENTICATED (trang đã chuyển nên không đọc lại được thân phản hồi trên)
  const direct = await request.get(`${API}/api/v1/account/me`, { headers: { Authorization: `Bearer ${u.token}` } });
  expect(direct.status()).toBe(401);
  expect(((await direct.json()) as { code: string }).code).toBe('UNAUTHENTICATED');
  await expect(page).toHaveURL(/\/hs\/dang-nhap-ma\?het-phien=1$/);
  await expect(page.getByTestId('s2e1-expired')).toHaveText(MSG.expired);
});

test('Khôi phục đúng đặt được mật khẩu mới @TC-S2E1-031', async ({ page, request }, testInfo) => {
  const u = await register(request, ipOf(testInfo, 'api'), { name: 'Cô Trang', role: 'teacher', password: 'Trang@2026x' });
  await page.goto('/gv/dang-nhap-ma');
  await page.getByTestId('s2e1-to-recover').click();
  await expect(page).toHaveURL(/\/gv\/khoi-phuc$/);
  await page.getByTestId('s2e1-recover-user-code').fill(u.userCode);
  await page.getByTestId('s2e1-recover-code').fill(u.recoveryCode);
  await page.getByTestId('s2e1-recover-password').fill('Trang@2027y');
  await page.getByTestId('s2e1-recover-submit').click();
  const fresh = page.getByTestId('s2e1-new-recovery-code');
  await expect(fresh).toHaveText(RECOVERY_CODE);
  expect(await fresh.textContent()).not.toBe(u.recoveryCode);
  await expect(page.getByTestId('s2e1-continue')).toBeDisabled();
  await page.getByTestId('s2e1-saved').check();
  await page.getByTestId('s2e1-continue').click();
  await expect(page).toHaveURL(/\/gv\/kho-de$/);
  expect((await login(request, u.userCode, 'Trang@2027y')).status()).toBe(200);
});

test('Sai mã khôi phục hiện câu chung @TC-S2E1-033', async ({ page, request }, testInfo) => {
  const u = await register(request, ipOf(testInfo, 'api'), { name: 'Cô Trang', role: 'teacher', password: 'Trang@2026x' });
  await page.goto('/gv/khoi-phuc');
  await page.getByTestId('s2e1-recover-user-code').fill(u.userCode);
  await page.getByTestId('s2e1-recover-code').fill('RC-2222-3333-4444');
  await page.getByTestId('s2e1-recover-password').fill('Trang@2027y');
  await page.getByTestId('s2e1-recover-submit').click();
  await expect(page.getByTestId('s2e1-recover-error')).toHaveText(MSG.recovery);
  expect((await login(request, u.userCode, 'Trang@2026x')).status()).toBe(200); // mật khẩu không đổi
});

test('Tài khoản chuyển phải đặt mật khẩu mới ở lần đăng nhập đầu @TC-S2E1-043', async ({ page, request }, testInfo) => {
  const u = await register(request, ipOf(testInfo, 'api'), { name: 'An', password: 'AnHoc#2026' });
  await setTempPassword(request, u.userCode, 'Tam-2026-AB'); // như sau lệnh chuyển MVP (BR-S2E1-15)
  await loginUI(page, 'hs', u.userCode, 'Tam-2026-AB');
  await expect(page).toHaveURL(/\/hs\/dat-mat-khau$/);
  await expect(page.getByTestId('s2e1-first-password')).toContainText('Đặt mật khẩu mới');
  await expect(page.getByTestId('e1-greeting')).toHaveCount(0);
  await page.getByTestId('s2e1-first-password-input').fill('AnMoi#2027');
  await page.getByTestId('s2e1-first-password-submit').click();
  await expect(page).toHaveURL(/\/hs\/bai-cua-em$/);
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  const s = await login(request, u.userCode, 'AnMoi#2027');
  expect(s.status()).toBe(200);
  expect(((await s.json()) as { mustChangePassword: boolean }).mustChangePassword).toBe(false);
});
