import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';
import { AN_S5, ASSIGNED_BG, MSG, docId, openReview } from './s2e5.ts';

/**
 * S2-E5 · theme mặc định C "Giấy kẻ ô" trên điện thoại (dự án mobile-iphone, mobile-pixel; ui-chromium/ui-webkit cũng
 * chạy). Kiểm máy phần đo được của TC-S2E5-038 (Manual: cần iPhone thật xét cắt chữ): hai nhãn không bị cắt, dòng chú
 * thích xuống hàng gọn, không cuộn ngang, chữ phụ ≥ 14 px, nội dung toán dùng var(--font-math), nút chạm ≥ 44.
 */
const norm = (s: string) => s.replace(/["'\s]/g, '');

async function noOverflow(page: import('@playwright/test').Page) {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, 'không cuộn ngang').toBeLessThanOrEqual(0);
}

test('@TC-S2E5-038 học sinh: hai nhãn của phiên bản đã giao hiện đủ trên điện thoại, không cuộn ngang', async ({ page, request }) => {
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await signInPage(page, request, AN_S5);
  await page.goto(`/hs/nhan-loi-giai/${ASSIGNED_BG}`);
  await expect(page.getByTestId('s2e5-student')).toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'c');
  await expect(page.getByTestId('s2e5-machine-student')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e5-human-student')).toHaveText(MSG.approved);
  await noOverflow(page);
  const vw = page.viewportSize()!.width;
  for (const id of ['s2e5-machine-student', 's2e5-human-student', 's2e5-student-note']) {
    const m = await page.getByTestId(id).evaluate((el) => {
      const r = el.getBoundingClientRect();
      return { left: r.left, right: r.right, clipped: el.scrollWidth > el.clientWidth + 1, size: parseFloat(getComputedStyle(el).fontSize) };
    });
    expect(m.left, id).toBeGreaterThanOrEqual(0);
    expect(m.right, id).toBeLessThanOrEqual(vw + 0.5);
    expect(m.clipped, `${id} không bị cắt chữ`).toBe(false);
    expect(m.size, `${id} chữ ≥ 14 px`).toBeGreaterThanOrEqual(14);
  }
});

test('S2-E5 C2-03 theme C: không cuộn ngang, chạm ≥ 44, chữ toán dùng --font-math', async ({ page, request }, info) => {
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await openReview(page, request, docId(38, info)); // chỉ đọc: không lưu, không kiểm
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'c');
  await expect(page.getByTestId('s2e5-machine-figure')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e5-human-figure')).toHaveText(MSG.ai);
  await noOverflow(page);
  for (const id of ['s2e5-save', 's2e5-check', 's2e5-approve', 's2e5-step-delete-1', 's2e5-step-add-after-1', 's2e5-history-v1']) {
    const b = (await page.getByTestId(id).boundingBox())!;
    expect(Math.min(b.width, b.height), id).toBeGreaterThanOrEqual(44);
  }
  const fonts = await page.evaluate(() => {
    const math = getComputedStyle(document.documentElement).getPropertyValue('--font-math').trim();
    const f = (tid: string) => getComputedStyle(document.querySelector(`[data-testid="${tid}"]`)!).fontFamily;
    const sub = parseFloat(getComputedStyle(document.querySelector('.s2e5-sub')!).fontSize);
    return { math, statement: f('s2e5-statement'), step: f('s2e5-step-text-3'), sub };
  });
  expect(fonts.math.length).toBeGreaterThan(0);
  expect(norm(fonts.statement)).toBe(norm(fonts.math));
  expect(norm(fonts.step)).toBe(norm(fonts.math));
  expect(fonts.sub).toBeGreaterThanOrEqual(14);
});
