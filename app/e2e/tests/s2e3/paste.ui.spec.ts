import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { FX10, MSG, bearer, draftsUrl, ok, signIn, teacher, tokenFor } from './s2e3.ts';

/** S2-E3 — C2-02 tab "Dán chữ" (US-S2E3-01): chép lại để sửa, ký hiệu toán, độ dài, chữ thuần, nháp ở máy chủ. */

const box = (page: Page) => page.getByTestId('s2e3-statement');
const buildBtn = (page: Page) => page.getByTestId('s2e3-build');

/** Gõ đề và chờ bản nháp lưu xong ở máy chủ. */
async function typeAndSave(page: Page, text: string) {
  const saved = page.waitForResponse((r) => r.url().includes('/api/v1/teacher/problem-drafts') && ['POST', 'PUT'].includes(r.request().method()) && r.status() === 200);
  await box(page).fill(text);
  await saved;
}

test('@TC-S2E3-001 dán đề FX-10 thì thấy ô "Đề đã chép lại" đúng chữ, sửa được, có nút Dựng hình', async ({ page, request }, info) => {
  await signIn(page, request, teacher(1, info), '/gv/them-de');
  await expect(page.getByTestId('s2e3-page')).toBeVisible();
  await expect(page.getByTestId('s2e3-tab-text')).toHaveAttribute('aria-selected', 'true');
  await expect(page.getByText('Đề đã chép lại')).toBeVisible();
  await typeAndSave(page, FX10);
  await expect(box(page)).toHaveValue(FX10);
  await expect(box(page)).toBeEditable();
  await expect(buildBtn(page)).toBeEnabled();
  await expect(buildBtn(page)).toHaveText('Dựng hình');
});

test('@TC-S2E3-002 ký hiệu toán giữ nguyên khi dán', async ({ page, request }, info) => {
  const s = 'SA ⊥ (ABCD), SA = a√2, góc 45°';
  await signIn(page, request, teacher(2, info), '/gv/them-de');
  await typeAndSave(page, s);
  await expect(box(page)).toHaveValue(s);
  await page.reload();
  await expect(box(page)).toHaveValue(s); // bản lưu ở máy chủ cũng nguyên ký hiệu
  const t = await tokenFor(request, teacher(2, info));
  const items = (await ok<{ items: Array<{ title: string }> }>(await request.get(draftsUrl, { headers: bearer(t) }))).items;
  expect(items[0]!.title).toBe(s);
});

test('@TC-S2E3-003 ô trống không cho dựng, không tạo tác vụ', async ({ page, request }, info) => {
  await signIn(page, request, teacher(3, info), '/gv/them-de');
  await expect(box(page)).toHaveValue('');
  await expect(buildBtn(page)).toBeDisabled();
  await expect(page.getByTestId('s2e3-length')).toHaveText(MSG.empty);
  await expect(page.getByTestId('s2e3-stages')).toHaveCount(0);
  const t = await tokenFor(request, teacher(3, info));
  expect((await ok<{ items: unknown[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items).toHaveLength(0);
});

test('@TC-S2E3-004 đề 9 ký tự bị chặn, 10 ký tự được nhận', async ({ page, request }, info) => {
  await signIn(page, request, teacher(4, info), '/gv/them-de');
  await box(page).fill('Tam giác A');
  await box(page).fill('Tam giácA');
  await expect(buildBtn(page)).toBeDisabled();
  await expect(page.getByTestId('s2e3-length')).toHaveText(MSG.short);
  await box(page).fill('Tam giác A');
  await expect(buildBtn(page)).toBeEnabled();
  await expect(page.getByTestId('s2e3-length')).toHaveText('10 / 2000 ký tự');
});

test('@TC-S2E3-005 đề đúng 2000 ký tự được nhận', async ({ page, request }, info) => {
  await signIn(page, request, teacher(5, info), '/gv/them-de');
  const long = (FX10 + ' ').repeat(60).slice(0, 2000);
  await typeAndSave(page, long);
  await expect(buildBtn(page)).toBeEnabled();
  await expect(page.getByTestId('s2e3-length')).toHaveText('2000 / 2000 ký tự');
  await expect(page.getByText(MSG.long)).toHaveCount(0);
  // 2001 ký tự: lỗi độ dài, nút khóa (giao diện của TC-S2E3-006)
  await box(page).fill(long + 'x');
  await expect(page.getByTestId('s2e3-length')).toHaveText(MSG.long);
  await expect(buildBtn(page)).toBeDisabled();
});

test('@TC-S2E3-007 mã HTML và script hiện thành chữ, không thực thi', async ({ page, request }, info) => {
  const s = '<script>alert(1)</script> Tam giác ABC';
  let dialogs = 0;
  page.on('dialog', (d) => {
    dialogs++;
    void d.dismiss();
  });
  await signIn(page, request, teacher(7, info), '/gv/them-de');
  await typeAndSave(page, s);
  await expect(box(page)).toHaveValue(s);
  await page.reload();
  await expect(box(page)).toHaveValue(s);
  await page.goto('/gv/de-cua-toi');
  await expect(page.getByTestId('s2e3-mine')).toContainText(s);
  expect(dialogs).toBe(0);
  expect(await page.locator('script', { hasText: 'alert(1)' }).count()).toBe(0);
});

test('@TC-S2E3-008 nháp chữ còn khi rời màn rồi quay lại', async ({ page, request }, info) => {
  const half = 'Tam giác ABC có AB = 5, AC = 12';
  await signIn(page, request, teacher(8, info), '/gv/them-de');
  await typeAndSave(page, half);
  await page.getByTestId('e1-nav-kho-de').click();
  await expect(page).toHaveURL(/\/gv\/kho-de/);
  await page.getByTestId('s2e3-nav-them-de').click();
  await expect(box(page)).toHaveValue(half);
});
