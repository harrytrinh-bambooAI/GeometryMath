import type { Locator, Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { api, asg, confirmText, msg46, signIn, twoClasses } from './e6.ts';

/** E6 — "Mở ngay" trên điện thoại của giáo viên (US-GEO-605-SC10, D-GEO-147; dự án ui-iphone, WebKit iPhone dọc). */

async function inViewport(page: Page, l: Locator) {
  const b = (await l.boundingBox())!;
  const vp = page.viewportSize()!;
  expect(b.x).toBeGreaterThanOrEqual(0);
  expect(b.y).toBeGreaterThanOrEqual(0);
  expect(b.x + b.width).toBeLessThanOrEqual(vp.width + 0.5);
  expect(b.y + b.height).toBeLessThanOrEqual(vp.height + 0.5);
}

test('"Mở ngay" trên điện thoại: hộp xác nhận nằm trọn màn hình @TC-GEO-0867', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(sb, 'BG-2')}`);
  await page.getByTestId('e6-open-now').click();
  const dlg = page.getByTestId('e6-confirm');
  await expect(page.getByTestId('e6-confirm-text')).toHaveText(confirmText('11A1'));
  await inViewport(page, dlg);
  await inViewport(page, page.getByTestId('e6-confirm-ok'));
  await inViewport(page, page.getByTestId('e6-confirm-cancel'));
  const scroll = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
  expect(scroll).toBeLessThanOrEqual(0);
  await page.getByTestId('e6-confirm-ok').click();
  await expect(page.getByTestId('e6-toast')).toHaveText(msg46('11A1'));
  const v = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-2'))).json()) as { releaseState: string };
  expect(v.releaseState).toBe('open');
});
