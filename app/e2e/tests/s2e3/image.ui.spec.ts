import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { FX10, MSG, aiScenario, jpeg, signIn, stage, teacher } from './s2e3.ts';

/** S2-E3 — C2-02 tab "Tải ảnh" (US-S2E3-02): chữ đọc từ ảnh, ô chưa chắc tô vàng, xác nhận, lưu nháp, ảnh trắng, nhiều ảnh. */

const file = (name: string, marker: string) => ({ name, mimeType: 'image/jpeg', buffer: jpeg(marker) });

async function openImageTab(page: Page, request: APIRequestContext, tc: number, info: TestInfo) {
  await signIn(page, request, teacher(tc, info), '/gv/them-de');
  await page.getByTestId('s2e3-tab-image').click();
  await expect(page.getByTestId('s2e3-drop')).toBeVisible();
}

async function choose(page: Page, files: Array<ReturnType<typeof file>>) {
  const done = page.waitForResponse((r) => r.url().includes('/image?') && r.request().method() === 'PUT');
  await page.getByTestId('s2e3-file').setInputFiles(files);
  await done;
}

const unsure = (page: Page) => page.locator('[data-testid^="s2e3-cell-"][data-state="unsure"]');

test('@TC-S2E3-010 ảnh rõ thì hiện chữ đã đọc, không có ô vàng, nút Dựng hình mở', async ({ page, request }, info) => {
  await openImageTab(page, request, 10, info);
  await choose(page, [file('de-pl01.jpg', 'S2E3-CLEAR')]);
  await expect(page.getByTestId('s2e3-statement')).toHaveValue(FX10);
  await expect(page.getByTestId('s2e3-statement')).toBeEditable();
  await expect(unsure(page)).toHaveCount(0);
  await expect(page.getByTestId('s2e3-build')).toBeEnabled();
  await expect(page.getByTestId('s2e3-build')).toHaveText('Dựng hình');
});

test('@TC-S2E3-011 ô chưa chắc tô vàng, nút xác nhận khóa, nhãn máy Cần xem lại', async ({ page, request }, info) => {
  await openImageTab(page, request, 11, info);
  await choose(page, [file('de-mo.jpg', 'S2E3-BLURRY')]);
  await expect(unsure(page)).toHaveCount(2);
  await expect(page.getByTestId('s2e3-cell-2')).toHaveClass(/s2e3-unsure/);
  await expect(page.getByTestId('s2e3-cell-3')).toHaveClass(/s2e3-unsure/);
  await expect(page.getByTestId('s2e3-cell-0')).not.toHaveClass(/s2e3-unsure/);
  await expect(page.getByTestId('s2e3-confirm-text')).toBeDisabled();
  await expect(page.getByTestId('s2e3-machine')).toHaveText(MSG.needsReview);
});

test('@TC-S2E3-013 sửa hết ô vàng mới xác nhận được; sau xác nhận nhãn máy vẫn Cần xem lại', async ({ page, request }, info) => {
  await aiScenario(request, teacher(13, info), 's2e3:slow-build');
  await openImageTab(page, request, 13, info);
  await choose(page, [file('de-mo.jpg', 'S2E3-BLURRY')]);
  await page.getByTestId('s2e3-cell-input-2').fill('SA = a√2.');
  let saved = page.waitForResponse((r) => r.url().includes('/problem-drafts/') && r.request().method() === 'PUT');
  await page.getByTestId('s2e3-cell-ok-2').click();
  await saved;
  await expect(unsure(page)).toHaveCount(1);
  saved = page.waitForResponse((r) => r.url().includes('/problem-drafts/') && r.request().method() === 'PUT');
  await page.getByTestId('s2e3-cell-ok-3').click(); // bấm "đúng"
  await saved;
  await expect(unsure(page)).toHaveCount(0);
  await expect(page.getByTestId('s2e3-confirm-text')).toBeEnabled();
  await expect(page.getByTestId('s2e3-confirm-text')).toHaveText('Xác nhận chữ và dựng tiếp');
  await page.getByTestId('s2e3-confirm-text').click();
  await expect(page.getByTestId('s2e3-confirmed')).toHaveText('Đã xác nhận chữ');
  await expect(page.getByTestId('s2e3-statement')).toHaveValue(/SA = a√2\./);
  await expect(stage(page, 'build')).toHaveAttribute('data-state', 'running');
  await expect(page.getByTestId('s2e3-machine').first()).toHaveText(MSG.needsReview);
  await aiScenario(request, teacher(13, info), '');
});

test('@TC-S2E3-014 chưa xác nhận ô vàng vẫn lưu được nháp, mở lại còn nguyên', async ({ page, request }, info) => {
  await openImageTab(page, request, 14, info);
  await choose(page, [file('de-mo.jpg', 'S2E3-BLURRY')]);
  await page.getByTestId('s2e3-save-draft').click();
  await expect(page.getByTestId('s2e3-notice')).toHaveText('Đã lưu nháp');
  await page.reload();
  await expect(unsure(page)).toHaveCount(2);
  await expect(page.getByTestId('s2e3-stages')).toHaveCount(0); // không dựng hình
  await expect(page.getByTestId('s2e3-shelf')).toContainText('Chưa vào');
  await page.goto('/gv/de-cua-toi');
  await expect(page.locator('[data-testid^="s2e3-item-"]')).toHaveAttribute('data-status', 'editing');
});

test('@TC-S2E3-015 ảnh không có chữ: yêu cầu gõ lại, ô sửa để trống, không có đề đoán', async ({ page, request }, info) => {
  await openImageTab(page, request, 15, info);
  await choose(page, [file('trang.jpg', 'S2E3-BLANK')]);
  await expect(page.getByTestId('s2e3-notice')).toHaveText(MSG.noText);
  await expect(page.getByTestId('s2e3-statement')).toHaveValue('');
  await expect(page.locator('[data-testid^="s2e3-cell-"]')).toHaveCount(0);
});

test('@TC-S2E3-019 chọn hai ảnh một lúc chỉ đọc ảnh đầu, báo "Mỗi lần tải một ảnh"', async ({ page, request }, info) => {
  await openImageTab(page, request, 19, info);
  await choose(page, [file('mot.jpg', 'S2E3-CLEAR'), file('hai.jpg', 'S2E3-BLURRY')]);
  await expect(page.getByTestId('s2e3-notice')).toHaveText(MSG.oneImage);
  await expect(page.getByTestId('s2e3-statement')).toHaveValue(FX10);
  await expect(unsure(page)).toHaveCount(0);
});
