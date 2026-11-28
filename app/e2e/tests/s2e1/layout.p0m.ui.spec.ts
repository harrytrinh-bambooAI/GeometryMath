import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { fillRegister, ipOf, useIp } from './s2e1.ts';

/**
 * Bố cục màn C3-01 (S2-E1) ở theme mặc định trên mọi thiết bị, kể cả điện thoại (*.p0m): không tràn ngang, ô bấm
 * (nút, ô nhập, chọn vai) cao ≥ 44 px (epic-common v1.3, PO-66 G17). Ảnh chụp đính kèm làm bằng chứng.
 */
test.beforeEach(async ({ page }, testInfo) => {
  await useIp(page, ipOf(testInfo));
});

async function checkLayout(page: Page, name: string) {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, `${name}: tràn ngang`).toBeLessThanOrEqual(0);
  const small = await page.evaluate(() =>
    [...document.querySelectorAll<HTMLElement>('[data-testid^="s2e1-"]')]
      .filter((el) => el.matches('button.btn, input:not([type="checkbox"]), [role="radio"]'))
      .filter((el) => el.offsetParent !== null && el.getBoundingClientRect().height < 44)
      .map((el) => `${el.dataset.testid}:${Math.round(el.getBoundingClientRect().height)}`),
  );
  expect(small, `${name}: ô bấm thấp hơn 44 px`).toEqual([]);
  await test.info().attach(name, { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
}

test('S2-E1 bố cục đăng ký, màn mã, đăng nhập, khôi phục không tràn và đủ 44 px', async ({ page }) => {
  await page.goto('/hs/dang-ky');
  await expect(page.getByTestId('s2e1-register')).toBeVisible();
  await checkLayout(page, 'dang-ky');
  await fillRegister(page, { role: 'student', name: 'Nguyễn Thị Bình An', password: 'AnHoc#2026' });
  await page.getByTestId('s2e1-register-submit').click();
  await expect(page.getByTestId('s2e1-codes')).toBeVisible();
  await checkLayout(page, 'ma-nguoi-dung');
  await page.goto('/hs/dang-nhap-ma');
  await checkLayout(page, 'dang-nhap');
  await page.goto('/hs/khoi-phuc');
  await checkLayout(page, 'khoi-phuc');
});
