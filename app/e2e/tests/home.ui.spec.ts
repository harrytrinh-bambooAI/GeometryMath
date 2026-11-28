import { expect, test } from '../support/fixtures.ts';

// Test khung (@TC-KHUNG-03): mở trang chủ qua giao diện thật (bản build, proxy tới máy chủ), vào vùng Giáo viên.
test('trang chủ mở, máy chủ sẵn sàng, giờ Việt Nam, vào vùng Giáo viên @TC-KHUNG-03 @smoke', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByTestId('home-title')).toHaveText('Hình Học Không Gian Cùng AI');
  await expect(page.getByTestId('home-server-status')).toHaveAttribute('data-status', 'UP');
  await expect(page.getByTestId('home-server-status')).toHaveText('Máy chủ sẵn sàng');
  await expect(page.getByTestId('home-clock')).toHaveText(/^Giờ Việt Nam: \d{2}:\d{2} \d{2}\/\d{2}\/\d{4}$/);

  await page.getByTestId('home-role-teacher').click();
  await expect(page).toHaveURL(/\/gv$/);
  await expect(page.getByTestId('gv-title')).toHaveText('Giáo viên');
  for (const e of ['e1', 'e2', 'e3', 'e4', 'e5', 'e6', 'e7']) {
    await expect(page.getByTestId(`gv-overview-${e}`)).toBeVisible();
  }
});
