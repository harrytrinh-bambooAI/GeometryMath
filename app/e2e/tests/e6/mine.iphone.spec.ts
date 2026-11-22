import { test, expect } from '../../support/fixtures.ts';
import { signIn, stu, twoClasses } from './e6.ts';

/** E6 — "Bài của em" trên iPhone màn dọc (US-GEO-606-SC6; F-GEO-101, dự án ui-iphone). */
test('Trên iPhone: ba thẻ một cột, đủ nhãn, không cuộn ngang @TC-GEO-0644', async ({ page, request }) => {
  const sb = await twoClasses(request, [
    { key: 'BG-1', code: 'HKG-01' },
    { key: 'BG-2', code: 'HKG-02', release: 'unset' },
    { key: 'BG-3', code: 'HKG-03', release: 'open' },
  ]);
  await signIn(page, stu(sb, 'An'), 'An', 'student', '11A1');
  await page.goto('/hs/bai-cua-em');
  const cards = page.locator('[data-testid^="e6-card-HKG-"]:not([data-testid*="-bar"]):not([data-testid*="-action"]):not([data-testid*="-topic"])');
  await expect(cards).toHaveCount(3);
  const xs = new Set<number>();
  for (const code of ['HKG-01', 'HKG-02', 'HKG-03']) {
    const c = page.getByTestId(`e6-card-${code}`);
    await expect(page.getByTestId(`e1-assignment-${code}-release`)).toBeVisible();
    await expect(page.getByTestId(`e1-assignment-${code}-progress`)).toBeVisible();
    await expect(page.getByTestId(`e6-card-${code}-action`)).toBeVisible();
    xs.add(Math.round((await c.boundingBox())!.x));
  }
  expect(xs.size).toBe(1);
  expect(await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth)).toBeLessThanOrEqual(0);
});
