import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { asg, box, golden, signInAs } from './e7.ts';
import type { Box } from './e7.ts';

/**
 * FIX4b M1 (Dev-E7-06, soát E7): bấm "Mở ngay"/"Đổi giờ" ngay trên Theo dõi lớp thì trang tự tải lại dữ liệu của mình
 * (thẻ khóa, dòng "còn …", cột Tình trạng), không cần F5 (BR-GEO-269, 268).
 */
async function openMonitor(page: Page, b: Box, key: string) {
  await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(b, key)}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
}
const status = (page: Page) => page.locator('[data-testid^="e7-status-"]').allTextContents();

test('Dev-E7-06 bấm "Mở ngay" trên Theo dõi lớp: thẻ khóa biến mất, Tình trạng đổi ngay', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b, 'BG-1');
  await expect(page.getByTestId('e7-lock-left')).toHaveText('còn 1 giờ 0 phút');
  expect(await status(page)).toEqual(['Đang chờ lời giải', 'Đang làm', 'Tự làm, không cần gợi ý', 'Đang chờ lời giải', 'Chưa bắt đầu']);
  await page.getByTestId('e7-lock').getByRole('button', { name: 'Mở ngay' }).click();
  await page.getByTestId('e6-confirm-ok').click();
  await expect(page.getByTestId('e6-toast')).toBeVisible();
  await expect(page.getByTestId('e7-lock')).toHaveCount(0);
  await expect(page.getByTestId('e7-lock-left')).toHaveCount(0);
  await expect(page.getByTestId('e7-release')).toBeVisible();
  await expect.poll(() => status(page)).toEqual(['Đang làm', 'Đang làm', 'Tự làm, không cần gợi ý', 'Đang làm', 'Chưa bắt đầu']);
});

test('Dev-E7-06 bấm "Đổi giờ" trên Theo dõi lớp: dòng "còn …" theo giờ mới ngay', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b, 'BG-2');
  await expect(page.getByTestId('e7-lock').getByTestId('e6-release-card-when')).toHaveText('Chưa đặt giờ mở');
  await expect(page.getByTestId('e7-lock-left')).toHaveCount(0);
  // ngày mai theo giờ Việt Nam, 08:00
  const vnTomorrow = new Date(Date.now() + 7 * 3600_000 + 24 * 3600_000).toISOString().slice(0, 10);
  await page.getByTestId('e7-lock').getByRole('button', { name: 'Đổi giờ' }).click();
  await page.getByTestId('e6-change-date').fill(vnTomorrow);
  await page.getByTestId('e6-change-clock').fill('08:00');
  await page.getByTestId('e6-change-save').click();
  await expect(page.getByTestId('e7-lock').getByTestId('e6-release-card-when')).toHaveText(/^Mở lúc 08:00, /);
  await expect(page.getByTestId('e7-lock-left')).toHaveText(/^còn \d+ giờ \d+ phút$/);
  await expect(page.getByTestId('e7-lock')).toHaveAttribute('data-state', 'upcoming');
});
