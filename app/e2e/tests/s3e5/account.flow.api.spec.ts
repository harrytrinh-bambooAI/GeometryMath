import { createHash } from 'node:crypto';
import type { TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, err } from '../s3e1/s3e1.ts';

/**
 * US-S3E5-03 FC1, ca LOẠI B của `account.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E"): chỗ nhập ở màn đăng ký do
 * ca loại A TC-S3E5-22 / s2e1 register giữ; ca này chỉ kiểm quy tắc tên hiển thị bằng gọi API, cuối luồng mở màn Đăng ký
 * (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const ipOf = (t: TestInfo) => {
  const h = createHash('sha256').update(`s3e5-acct|${t.titlePath.join('/')}|${t.project.name}`).digest();
  return `10.${110 + (h[0]! % 40)}.${h[1]}.${(h[2]! % 250) + 1}`;
};

test('Tên hiển thị là email @TC-S3E5-33', async ({ request, browser }, testInfo) => {
  const r = await request.post(`${API}/api/v1/auth/register`, {
    headers: { 'X-Forwarded-For': ipOf(testInfo).replace(/\.\d+$/, '.7') },
    data: { displayName: 'mai@example.org', role: 'student', password: 'MaiMai#2026', privacyAck: true, privacyVersion: '1.0' },
  });
  expect(r.status()).toBe(400);
  const e = await err(r);
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(JSON.stringify(e)).toContain('Tên hiển thị không nên là email hay số điện thoại.');
  expect(JSON.stringify(e)).not.toMatch(/HS-/); // không cấp mã người dùng
  await captureFinalState(browser, testInfo, async (page) => {
    await page.route(/fonts\.(googleapis|gstatic)\.com/, (x) => x.abort());
    await page.goto('/hs/dang-ky');
    await expect(page.getByTestId('s2e1-name')).toBeVisible();
  });
});
