import { expect, test } from '../../support/fixtures.ts';
import { NAMES, asg, box, golden, signInAs } from './e7.ts';

/** E7 — Theo dõi lớp trên điện thoại của giáo viên (F-GEO-103; dự án ui-iphone, WebKit màn dọc). */
test('Xem nhanh trên điện thoại @TC-GEO-0706', async ({ page, request }) => {
  const b = await box(request, golden());
  await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(b, 'BG-1')}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
  for (const c of ['opened', 'hints', 'questions', 'figure']) await expect(page.getByTestId(`e7-card-${c}`)).toBeInViewport({ ratio: 0.1 });
  await expect(page.getByTestId('e7-lock')).toBeVisible();
  for (const n of NAMES) await expect(page.locator('[data-testid^="e7-name-"]', { hasText: n })).toBeVisible();
  // không phải cuộn ngang trang
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
  expect(overflow).toBeLessThanOrEqual(1);
  const btn = page.getByTestId('e7-lock').getByRole('button', { name: 'Mở ngay' });
  await btn.scrollIntoViewIfNeeded();
  await expect(btn).toBeEnabled();
  const box2 = await btn.boundingBox();
  expect(box2!.width).toBeGreaterThanOrEqual(44);
  await btn.click(); // bấm được
});
