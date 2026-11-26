import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { MSG, RECOVERY_CODE, fillRegister, ipOf, useIp } from './s2e1.ts';

/** US-S2E1-01 — màn "Tạo tài khoản" và màn mã (C3-01, DQ-15, DQ-16) trên web và iPad. */

test.beforeEach(async ({ page }, testInfo) => {
  await useIp(page, ipOf(testInfo)); // mỗi test một IP: giới hạn 5 lần đăng ký/giờ/IP không giẫm nhau
});

/** Đếm yêu cầu đăng ký trình duyệt gửi đi. */
function countRegisterCalls(page: Page): () => number {
  let n = 0;
  page.on('request', (r) => {
    if (r.method() === 'POST' && r.url().includes('/api/v1/auth/register')) n++;
  });
  return () => n;
}

test('Giáo viên đăng ký và nhận hai mã @TC-S2E1-001', async ({ page }) => {
  await page.goto('/gv/dang-ky');
  await fillRegister(page, { role: 'teacher', name: 'Cô Trang', password: 'Trang@2026x' });
  await expect(page.getByTestId('s2e1-privacy')).toBeChecked();
  await expect(page.getByText('(bản 1.0)')).toBeVisible();
  await page.getByTestId('s2e1-register-submit').click();
  await expect(page.getByTestId('s2e1-codes')).toBeVisible();
  await expect(page.getByTestId('s2e1-user-code')).toHaveText(/^GV-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/);
  await expect(page.getByTestId('s2e1-recovery-code')).toHaveText(RECOVERY_CODE);
  await expect(page.getByTestId('s2e1-copy-user-code')).toHaveText('Sao chép');
  await expect(page.getByTestId('s2e1-copy-recovery-code')).toHaveText('Sao chép');
  await expect(page.getByTestId('s2e1-print')).toHaveText('In / lưu ảnh');
  await expect(page.getByTestId('s2e1-warning')).toContainText(
    'Mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản',
  );
});

test('Học sinh đăng ký nhận mã người dùng và mã khôi phục @TC-S2E1-002', async ({ page }) => {
  await page.goto('/hs/dang-ky');
  await fillRegister(page, { role: 'student', name: 'An', password: 'AnHoc#2026' });
  await page.getByTestId('s2e1-register-submit').click();
  await expect(page.getByTestId('s2e1-user-code')).toHaveText(/^HS-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/);
  await expect(page.getByTestId('s2e1-recovery-code')).toHaveText(RECOVERY_CODE);
  await expect(page.getByTestId('s2e1-student-note')).toHaveText(
    'Quên mật khẩu thì dùng mã khôi phục này; nếu đã vào lớp, thầy cô cũng đặt lại được',
  );
});

test('Nút Tiếp tục chỉ mở khi đã tick "Tôi đã lưu mã" @TC-S2E1-003', async ({ page }) => {
  await page.goto('/gv/dang-ky');
  await fillRegister(page, { role: 'teacher', name: 'Cô Trang', password: 'Trang@2026x' });
  await page.getByTestId('s2e1-register-submit').click();
  const go = page.getByTestId('s2e1-continue');
  await expect(go).toBeDisabled();
  await go.click({ force: true }); // lần 1: chưa tick
  await expect(page).toHaveURL(/\/gv\/dang-ky$/);
  await expect(page.getByTestId('s2e1-codes')).toBeVisible();
  await page.getByTestId('s2e1-saved').check();
  await expect(go).toBeEnabled();
  await go.click();
  await expect(page).toHaveURL(/\/gv\/kho-de$/);
  await expect(page.getByTestId('e1-catalog')).toBeVisible();
});

test('Tên rỗng hoặc chỉ khoảng trắng bị chặn @TC-S2E1-007', async ({ page }) => {
  const calls = countRegisterCalls(page);
  await page.goto('/hs/dang-ky');
  for (const name of ['', '   ']) {
    await fillRegister(page, { role: 'student', name, password: 'AnHoc#2026' });
    await page.getByTestId('s2e1-register-submit').click();
    await expect(page.getByTestId('s2e1-name-error')).toHaveText(MSG.nameEmpty);
  }
  expect(calls()).toBe(0);
  await expect(page.getByTestId('s2e1-codes')).toHaveCount(0);
});

test('Tên đúng 60 ký tự được nhận @TC-S2E1-008', async ({ page }) => {
  const name = 'A'.repeat(60);
  await page.goto('/hs/dang-ky');
  await fillRegister(page, { role: 'student', name, password: 'AnHoc#2026' });
  const resp = page.waitForResponse((r) => r.url().includes('/api/v1/auth/register'));
  await page.getByTestId('s2e1-register-submit').click();
  const body = (await (await resp).json()) as { displayName: string };
  expect(body.displayName).toBe(name);
  await expect(page.getByTestId('s2e1-codes')).toContainText(name);
});

test('Tên 61 ký tự bị chặn @TC-S2E1-009', async ({ page }) => {
  const calls = countRegisterCalls(page);
  await page.goto('/hs/dang-ky');
  await fillRegister(page, { role: 'student', name: 'A'.repeat(61), password: 'AnHoc#2026' });
  await page.getByTestId('s2e1-register-submit').click();
  await expect(page.getByTestId('s2e1-name-error')).toHaveText(MSG.nameLong);
  expect(calls()).toBe(0);
  await expect(page.getByTestId('s2e1-codes')).toHaveCount(0);
});

test('Mật khẩu 7 ký tự bị chặn @TC-S2E1-012', async ({ page }) => {
  const calls = countRegisterCalls(page);
  await page.goto('/hs/dang-ky');
  await fillRegister(page, { role: 'student', name: 'An', password: 'Abcd123' });
  await page.getByTestId('s2e1-register-submit').click();
  await expect(page.getByTestId('s2e1-password-error')).toHaveText(MSG.password);
  expect(calls()).toBe(0);
  await expect(page.getByTestId('s2e1-codes')).toHaveCount(0);
});

test('Bấm Đăng ký liên tiếp chỉ tạo một tài khoản @TC-S2E1-018', async ({ page }) => {
  const calls = countRegisterCalls(page);
  await page.goto('/hs/dang-ky');
  await fillRegister(page, { role: 'student', name: 'Bấm đôi', password: 'AnHoc#2026' });
  await page.getByTestId('s2e1-register-submit').dblclick();
  await expect(page.getByTestId('s2e1-codes')).toBeVisible();
  await expect(page.getByTestId('s2e1-user-code')).toHaveCount(1);
  expect(calls()).toBe(1);
});
