import type { Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { snap, tapPoint, toast, tool, waitScene } from '../e3/e3.ts';
import { HINT1_HKG01, HINT2_HKG01, HKG, LEAK_HKG01, MSG, PV, api, asg, expectNoLeak, signIn, twoClasses, vn, vnInputs } from './e6.ts';
import type { Sb } from './e6.ts';

/** E6 — "Xem như học sinh" (US-GEO-607). Giáo viên mới mỗi ca; dữ liệu trả về của màn xem trước được ghi lại toàn bộ. */

async function assignPage(page: Page, sb: Sb, code: string) {
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/giao-bai/${PV(code)}`);
  await expect(page.getByTestId('e6-assign')).toBeVisible();
}
async function openPreview(page: Page) {
  await page.getByTestId('e6-preview-open').click();
  await expect(page.getByTestId('e6-preview')).toBeVisible();
}
function recordAll(page: Page): () => Promise<{ text: string; urls: string[] }> {
  const bodies: Promise<string>[] = [];
  const urls: string[] = [];
  page.on('response', (r) => {
    if (r.url().includes('/api/')) {
      urls.push(r.url());
      bodies.push(r.text().catch(() => ''));
    }
  });
  return async () => ({ text: (await Promise.all(bodies)).join('\n'), urls });
}

test('Mở xem trước: đề, hình, công cụ, ba tab, dải MSG-GEO-51 @TC-GEO-0649', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignPage(page, sb, 'HKG-06');
  await openPreview(page);
  await expect(page.getByTestId('e6-preview-banner')).toContainText(MSG['51']);
  await expect(page.getByTestId('e6-preview-exit')).toHaveText('Thoát xem trước');
  await expect(page.getByTestId('e6-preview-statement')).toHaveText(HKG['06']);
  await waitScene(page);
  await expect(page.locator('[data-testid^="viewer-tool-"]').first()).toBeVisible();
  for (const t of ['hint', 'solution', 'ask']) await expect(page.getByTestId(`e3-tab-${t}`)).toBeVisible();
});

test('Lời giải khóa trong xem trước (chưa giao, chưa chọn giờ) @TC-GEO-0650', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignPage(page, sb, 'HKG-06');
  const all = recordAll(page);
  await openPreview(page);
  await page.getByTestId('e3-tab-solution').click();
  await expect(page.getByTestId('e6-lock-title')).toHaveText(MSG['28']);
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  expectNoLeak((await all()).text, ['2,598', '√3/2', 'π√3']);
});

test('Gợi ý xem được, không ghi sự kiện @TC-GEO-0651', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const mon = async () => JSON.stringify(await (await api.monitor(request, sb.teacher.token, asg(sb, 'BG-1'))).json());
  const before = await mon();
  await assignPage(page, sb, 'HKG-01');
  await openPreview(page);
  await waitScene(page);
  await page.getByTestId('e6-preview-hint-next').click();
  await expect(page.getByTestId('e6-preview-hint-1')).toContainText(HINT1_HKG01);
  await page.getByTestId('e6-preview-hint-next').click();
  await expect(page.getByTestId('e6-preview-hint-2')).toContainText(HINT2_HKG01);
  await tool(page, 'point');
  const box = (await page.locator('.e3-viewer canvas, .e3-viewer svg').first().boundingBox())!;
  await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
  await page.getByTestId('e6-preview-exit').click();
  await expect(page).toHaveURL(/\/gv\/de\//);
  expect(await mon()).toBe(before);
});

test('Thoát xem trước: về trang chi tiết đề với lời giải đầy đủ @TC-GEO-0652', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignPage(page, sb, 'HKG-06');
  await openPreview(page);
  await page.getByTestId('e6-preview-exit').click();
  await expect(page).toHaveURL(new RegExp(`/gv/de/${PV('HKG-06')}$`));
  await expect(page.getByTestId('e5-part-solution')).toBeVisible();
  await expect(page.getByTestId('e5-part-solution')).toContainText('Đáp số');
});

test('Công cụ đo trong xem trước ẩn đại lượng đề hỏi @TC-GEO-0655', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignPage(page, sb, 'HKG-01');
  const all = recordAll(page);
  await openPreview(page);
  await waitScene(page);
  await tool(page, 'angle');
  const n = (await snap(page)).measures.length;
  for (const x of ['S', 'D', 'A']) await tapPoint(page, x);
  await expect.poll(async () => (await snap(page)).measures.length).toBe(n + 1);
  const m = (await snap(page)).measures.at(-1)!;
  expect(m.hidden).toBe(true);
  await expect(toast(page)).toHaveText(MSG['24']);
  expectNoLeak((await all()).text, LEAK_HKG01);
});

test('Đã chọn giờ ở bước 3: thẻ khóa ghi giờ đang chọn @TC-GEO-0870', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignPage(page, sb, 'HKG-06');
  await page.getByTestId('e6-class-9B').click();
  const at = new Date(Math.ceil((Date.now() + 2 * 3_600_000) / 60_000) * 60_000);
  const v = vnInputs(at);
  await page.getByTestId('e6-release-date').fill(v.date);
  await page.getByTestId('e6-release-time').fill(v.time);
  const all = recordAll(page);
  await openPreview(page);
  await page.getByTestId('e3-tab-solution').click();
  await expect(page.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(at)}`);
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  expect((await all()).urls.some((u) => /\/solution/.test(u))).toBe(false);
});

test('Đề đã giao: theo giờ mở của chính bài giao @TC-GEO-0871', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const r = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { releaseAt: string };
  await assignPage(page, sb, 'HKG-01');
  await page.getByTestId('e6-class-11A1').click();
  await openPreview(page);
  await page.getByTestId('e3-tab-solution').click();
  await expect(page.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(new Date(r.releaseAt))}`);
  await expect(page.getByTestId('e6-lock-left')).toHaveText('Còn 1 giờ 0 phút.');
});
