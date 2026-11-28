import { expect, test } from '../../support/fixtures.ts';
import { A, openPlane, tapVertices } from './s2e2.ts';

/**
 * S2-E2 · màn làm bài phẳng ở theme mặc định C "Giấy kẻ ô" (T-261126-50) trên điện thoại (dự án mobile-iphone, mobile-pixel;
 * ui-chromium/ui-webkit cũng chạy). Kiểm máy phần đo được của TC-S2E2-028 (Manual): một cột, không cuộn ngang,
 * nút chạm ≥ 44 điểm; nội dung toán (đề, nhãn đỉnh, số đo) dùng var(--font-math). Ảnh màn đính kèm báo cáo Playwright.
 */
test('S2-E2 màn hình phẳng theme C: không cuộn ngang, chạm ≥ 44, chữ toán dùng --font-math', async ({ page, request }, testInfo) => {
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await openPlane(page, request, A.PL03);
  await expect(page.getByTestId('s2e2-figure')).toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'c');
  await page.getByTestId('s2e2-tool-length').click();
  await tapVertices(page, 'A', 'B');
  await expect(page.getByTestId('s2e2-measure-length-AB')).toHaveText('AB = 6');

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, 'không cuộn ngang').toBeLessThanOrEqual(0);

  const vw = page.viewportSize()!.width;
  if (vw < 820) {
    // một cột: hình ở trên, công cụ và gợi ý ở dưới
    const fig = (await page.getByTestId('s2e2-figure').boundingBox())!;
    const tools = (await page.getByTestId('s2e2-tools').boundingBox())!;
    const hints = (await page.getByTestId('s2e2-hints').boundingBox())!;
    expect(tools.y).toBeGreaterThan(fig.y + fig.height - 1);
    expect(hints.y).toBeGreaterThan(tools.y);
  }
  for (const id of ['s2e2-tool-length', 's2e2-tool-angle', 's2e2-hint-open', 's2e2-figure-vertex-A', 's2e2-figure-vertex-C']) {
    const b = (await page.getByTestId(id).boundingBox())!;
    expect(Math.min(b.width, b.height), id).toBeGreaterThanOrEqual(44);
  }
  const fonts = await page.evaluate(() => {
    const math = getComputedStyle(document.documentElement).getPropertyValue('--font-math').trim();
    const f = (tid: string) => getComputedStyle(document.querySelector(`[data-testid="${tid}"]`)!).fontFamily;
    const label = getComputedStyle(document.querySelector('[data-testid="s2e2-figure-vertex-A"] .s2d-label')!).fontFamily;
    return { math, statement: f('s2e2-statement'), measure: f('s2e2-measure-length-AB'), label };
  });
  const norm = (s: string) => s.replace(/["'\s]/g, '');
  expect(fonts.math).not.toBe('');
  expect(norm(fonts.statement)).toBe(norm(fonts.math));
  expect(norm(fonts.measure)).toBe(norm(fonts.math));
  expect(norm(fonts.label)).toBe(norm(fonts.math));
  await testInfo.attach(`s2e2-theme-c-${testInfo.project.name}.png`, { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
});
