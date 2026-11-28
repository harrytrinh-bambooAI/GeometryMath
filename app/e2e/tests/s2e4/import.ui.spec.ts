import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, FX10, FX11, LIB_FX10, MSG, ORG_LIB, THREE, bearer, docx, draftsUrl, ok, pdf, pickFile, signIn, teacher, teacherLib, tokenFor } from './s2e4.ts';

/** S2-E4 — C2-04b…d "Nhập từ tệp" (US-S2E4-02, 03): tách từng bài, chỉ bài tick thành nháp, pdf có lớp chữ, pdf ảnh quét, trùng Kho chung. */

const item = (page: Page, i: number) => page.getByTestId(`s2e4-item-${i}`);
const check = (page: Page, i: number) => page.getByTestId(`s2e4-item-check-${i}`);
const DOCX = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';

test('@TC-S2E4-012 tách đúng từng bài tập của tệp docx', async ({ page, request }, info) => {
  await signIn(page, request, teacher(12, info), '/gv/nhap-tu-tep');
  await expect(page.getByTestId('s2e4-import')).toBeVisible();
  await pickFile(page, 'GiaoAn.docx', docx(THREE), DOCX);
  await expect(page.getByTestId('s2e4-import-items').locator('> li')).toHaveCount(3);
  for (const i of [1, 2, 3]) {
    await expect(check(page, i)).toBeVisible();
    await expect(check(page, i)).toBeChecked();
  }
  await expect(item(page, 1)).toContainText(FX10);
  await expect(item(page, 2)).toContainText(FX11);
  await expect(page.getByTestId('s2e4-import-notes')).toContainText(MSG.theory);
  await expect(page.getByTestId('s2e4-import-items')).not.toContainText('Pythagore'); // đoạn lý thuyết không thành mục
  // chưa có đề nào vào Kho, chưa có nháp nào
  const t = await tokenFor(request, teacher(12, info));
  expect((await ok<{ items: unknown[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items).toHaveLength(0);
  expect((await ok<{ items: unknown[] }>(await request.get(`${API}/api/v1/teacher/library?shelf=mine`, { headers: bearer(t) }))).items).toHaveLength(0);
});

test('@TC-S2E4-013 chỉ bài được tick mới thành nháp', async ({ page, request }, info) => {
  await signIn(page, request, teacher(13, info), '/gv/nhap-tu-tep');
  await pickFile(page, 'GiaoAn.docx', docx(THREE), DOCX);
  await expect(item(page, 3)).toBeVisible();
  await check(page, 2).uncheck(); // tick bài 1 và 3
  await expect(page.getByTestId('s2e4-import-count')).toContainText('2 bài');
  const saved = page.waitForResponse((r) => r.url().includes('/drafts') && r.request().method() === 'POST');
  await page.getByTestId('s2e4-import-save').click();
  expect((await saved).status()).toBe(200);
  await expect(page.getByTestId('s2e4-import-done')).toContainText('Đã tạo 2 nháp');
  for (const i of [1, 3]) {
    await expect(item(page, i)).toHaveAttribute('data-state', 'created');
    await expect(page.getByTestId(`s2e4-item-human-${i}`)).toHaveText(MSG.ai);
    await expect(page.getByTestId(`s2e4-item-machine-${i}`)).toHaveText(MSG.needsReview);
  }
  await expect(item(page, 2)).toHaveAttribute('data-state', 'new'); // bài 2 không tạo
  await expect(page.getByTestId('s2e4-item-human-2')).toHaveCount(0);
  const t = await tokenFor(request, teacher(13, info));
  const drafts = (await ok<{ items: Array<{ title: string }> }>(await request.get(draftsUrl, { headers: bearer(t) }))).items;
  expect(drafts).toHaveLength(2);
  expect(drafts.map((d) => d.title).sort()).toEqual([THREE[5]!.replace('Bài 3. ', ''), FX10].sort());
});

test('@TC-S2E4-014 tệp pdf có lớp chữ được tách như docx', async ({ page, request }, info) => {
  await signIn(page, request, teacher(14, info), '/gv/nhap-tu-tep');
  await pickFile(page, 'de-on-tap.pdf', pdf('hai-bai'), 'application/pdf');
  await expect(page.getByTestId('s2e4-import-items').locator('> li')).toHaveCount(2);
  await expect(item(page, 1)).toContainText(FX10);
  await expect(item(page, 2)).toContainText('Hình vuông ABCD có AB = 6; tính AC.');
  await expect(check(page, 1)).toBeChecked();
  await expect(page.getByTestId('s2e4-import-error')).toHaveCount(0);
});

test('@TC-S2E4-015 pdf ảnh quét bị từ chối', async ({ page, request }, info) => {
  await signIn(page, request, teacher(15, info), '/gv/nhap-tu-tep');
  await pickFile(page, 'ban-quet.pdf', pdf('anh-quet'), 'application/pdf');
  await expect(page.getByTestId('s2e4-import-error')).toHaveText(MSG.noTextLayer);
  await expect(page.getByTestId('s2e4-import-items')).toHaveCount(0);
  await expect(page.locator('[data-testid^="s2e4-item-"]')).toHaveCount(0);
  await expect(page.getByTestId('s2e4-import-name')).toHaveText('ban-quet.pdf'); // tên tệp vẫn hiện để chọn lại
});

test('@TC-S2E4-021 bài trùng đề trong Kho chung hiện đã có trong kho', async ({ page, request }, info) => {
  await signIn(page, request, teacherLib(21, info), '/gv/nhap-tu-tep', ORG_LIB);
  await pickFile(page, 'GiaoAn.docx', docx([`Bài 1. ${FX10}`, `Bài 2. ${FX11}`]), DOCX);
  await expect(item(page, 1)).toHaveAttribute('data-state', 'exists');
  await expect(page.getByTestId('s2e4-item-note-1')).toHaveText(MSG.exists);
  await expect(check(page, 1)).not.toBeChecked(); // không tick sẵn
  await expect(check(page, 2)).toBeChecked();
  await expect(page.getByTestId('s2e4-item-note-2')).toHaveCount(0);
  await expect(page.getByTestId('s2e4-import-dup-count')).toContainText('1 bài');
  // liên kết mở đề trong kho
  const link = page.getByTestId('s2e4-item-link-1');
  await expect(link).toHaveAttribute('href', `/gv/kho-de-cua-toi/kho-chung/${LIB_FX10}`);
  await link.click();
  await expect(page.getByTestId('s2e6-shared')).toBeVisible();
  await expect(page.getByTestId('s2e6-shared')).toContainText('AB = 5, AC = 12, BC = 13');
});
