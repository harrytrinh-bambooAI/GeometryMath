import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN, TRANG, applyUrl, bearer, bg1, doc, pinned, signIn, tokenFor, v3, view } from './apply.ts';

/** US-S2E5-05 — C2-03 hộp "Đã giao": bản đã giao bất biến, sửa tạo phiên bản mới, "Áp dụng cho lớp" / "Giữ v2". */

const line = (page: Page, cls: string) => page.getByTestId(`s2e5-assigned-${cls}`).getByTestId('s2e5-assigned-line');

test('@TC-S2E5-053 sửa đề đã giao tạo v3, BG-1 và BG-2 vẫn dùng v2, có lựa chọn cho từng bài giao', async ({ page, request }, info) => {
  const d = doc(53, info);
  await signIn(page, request, TRANG, 'TEACHER', `/gv/soat-loi-giai/${d}`);
  await expect(page.getByTestId('s2e5-page')).toHaveAttribute('data-version', '2');
  await page.getByTestId('s2e5-step-text-4').fill('Vậy góc giữa SC và mặt phẳng (ABCD) bằng 45°.');
  const saved = page.waitForResponse((r) => r.url().endsWith('/versions') && r.request().method() === 'POST');
  await page.getByTestId('s2e5-save').click();
  await saved;
  await expect(page.getByTestId('s2e5-page')).toHaveAttribute('data-version', '3');
  await expect(line(page, '11A1')).toHaveText('Đã giao cho 11A1 (v2)');
  await expect(line(page, '11A2')).toHaveText('Đã giao cho 11A2 (v2)');
  for (const c of ['11A1', '11A2']) {
    await expect(page.getByTestId(`s2e5-apply-${c}`)).toHaveText('Áp dụng cho lớp');
    await expect(page.getByTestId(`s2e5-keep-${c}`)).toHaveText('Giữ v2');
  }
  // v3 chưa duyệt: nút áp dụng khóa kèm "Duyệt v3 trước" (US-S2E5-05-FC2)
  await expect(page.getByTestId('s2e5-apply-11A1')).toBeDisabled();
  await expect(page.getByTestId('s2e5-apply-hint-11A1')).toHaveText('Duyệt v3 trước');
  const t = await tokenFor(request, TRANG);
  const now = await view(request, t, d);
  expect(now.latestVersion).toBe(3);
  expect([pinned(now, '11A1'), pinned(now, '11A2')]).toEqual([2, 2]);
});

test('@TC-S2E5-054 hộp đã giao hiện đúng phiên bản đang dùng và chú thích bất biến', async ({ page, request }, info) => {
  await signIn(page, request, TRANG, 'TEACHER', `/gv/soat-loi-giai/${doc(54, info)}`);
  await expect(line(page, '11A1')).toHaveText('Đã giao cho 11A1 (v2)');
  await expect(line(page, '11A2')).toHaveText('Đã giao cho 11A2 (v2)');
  await expect(page.getByTestId('s2e5-assigned-note')).toHaveText('Bản đã giao không sửa được, sửa sẽ tạo phiên bản mới');
});

test('@TC-S2E5-057 duyệt v3 rồi áp dụng cho BG-1: 11A1 dùng v3, 11A2 vẫn v2', async ({ page, request }, info) => {
  const d = doc(57, info);
  await v3(request, await tokenFor(request, TRANG), d);
  await signIn(page, request, TRANG, 'TEACHER', `/gv/soat-loi-giai/${d}`);
  await expect(page.getByTestId('s2e5-apply-11A1')).toBeEnabled();
  const applied = page.waitForResponse((r) => r.url().endsWith('/apply') && r.request().method() === 'POST');
  await page.getByTestId('s2e5-apply-11A1').click();
  await applied;
  await expect(line(page, '11A1')).toHaveText('Đã giao cho 11A1 (v3)');
  await expect(line(page, '11A2')).toHaveText('Đã giao cho 11A2 (v2)');
  await expect(page.getByTestId('s2e5-apply-confirm')).toHaveCount(0);
});

test('@TC-S2E5-059 chọn Giữ v2 cho cả hai bài giao thì không đổi, v3 chỉ ở "Của tôi"', async ({ page, request }, info) => {
  const d = doc(59, info);
  const t = await tokenFor(request, TRANG);
  await v3(request, t, d);
  await signIn(page, request, TRANG, 'TEACHER', `/gv/soat-loi-giai/${d}`);
  for (const c of ['11A1', '11A2']) {
    await page.getByTestId(`s2e5-keep-${c}`).click();
    await expect(page.getByTestId(`s2e5-kept-${c}`)).toContainText('chỉ nằm ở "Của tôi"');
    await expect(line(page, c)).toHaveText(`Đã giao cho ${c} (v2)`);
  }
  await expect(page.getByTestId('s2e5-shelf')).toHaveText('Của tôi');
  const now = await view(request, t, d);
  expect([pinned(now, '11A1'), pinned(now, '11A2')]).toEqual([2, 2]);
});

test('@TC-S2E5-060 áp dụng cho bài đã có 2 học sinh xem lời giải v2 phải xác nhận', async ({ page, request }, info) => {
  const d = doc(60, info);
  const t = await tokenFor(request, TRANG);
  await v3(request, t, d);
  await signIn(page, request, TRANG, 'TEACHER', `/gv/soat-loi-giai/${d}`);
  await page.getByTestId('s2e5-apply-11A1').click();
  await expect(page.getByTestId('s2e5-apply-warning')).toHaveText('2 học sinh đã xem bản v2. Vẫn áp dụng?');
  await page.getByTestId('s2e5-apply-no').click();
  await expect(line(page, '11A1')).toHaveText('Đã giao cho 11A1 (v2)');
  expect(pinned(await view(request, t, d), '11A1')).toBe(2);
  await page.getByTestId('s2e5-apply-11A1').click();
  await page.getByTestId('s2e5-apply-yes').click();
  await expect(line(page, '11A1')).toHaveText('Đã giao cho 11A1 (v3)');
  expect(pinned(await view(request, t, d), '11A1')).toBe(3);
});

test('@TC-S2E5-064 học sinh đang mở bài thấy ổn định, tải lại thấy nhãn theo v3', async ({ page, request }, info) => {
  const d = doc(64, info);
  const t = await tokenFor(request, TRANG);
  await v3(request, t, d);
  await signIn(page, request, AN, 'STUDENT', `/hs/nhan-loi-giai/${bg1(64, info)}`);
  const card = page.getByTestId('s2e5-student');
  await expect(card).toHaveAttribute('data-version', '2');
  const before = await card.innerText();
  const r = await request.post(applyUrl(d, bg1(64, info)), { headers: bearer(t), data: { version: 3, confirm: true } });
  expect(r.status(), await r.text()).toBe(200);
  await page.waitForTimeout(1_500);
  await expect(card).toHaveAttribute('data-version', '2'); // chưa tải lại: không đổi dưới tay học sinh
  expect(await card.innerText()).toBe(before);
  await page.reload();
  await expect(page.getByTestId('s2e5-student')).toHaveAttribute('data-version', '3');
  await expect(page.getByTestId('s2e5-human-student')).toHaveText('Giáo viên đã duyệt');
});
