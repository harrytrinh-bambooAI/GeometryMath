import { expect, test } from '../../support/fixtures.ts';
import { S1, S2, S3_RIGHT, S3_V1, S4_V1, TRANG, TRANG_CODE, WRONG_V2, apiSave, apiView, clickAndWait, docId, expectVersion, openReview, stepCount, stepText, token } from './s2e5.ts';

/** S2-E5 — C2-03: sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới (US-S2E5-01). */

const NEW_STEP = 'Vậy tam giác SAC vuông cân tại A';

test('@TC-S2E5-001 sửa bước 3 rồi lưu thành v2, v1 còn nguyên', async ({ page, request }, info) => {
  const d = docId(1, info);
  await openReview(page, request, d);
  await expectVersion(page, 1);
  await stepText(page, 3).fill(S3_RIGHT);
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 2);
  await expect(stepText(page, 3)).toHaveValue(S3_RIGHT);
  await expect(page.getByTestId('s2e5-history-desc-v2')).toHaveText('v2 · bạn sửa bước 3');
  // v1 vẫn xem được với bước 3 cũ
  await page.getByTestId('s2e5-history-v1').click();
  await expectVersion(page, 1);
  await expect(stepText(page, 3)).toHaveText(S3_V1);
  await expect(stepText(page, 3)).toContainText('= √2');
  await expect(page.getByTestId('s2e5-save')).toHaveCount(0); // bản cũ chỉ đọc
});

test('@TC-S2E5-002 thêm một bước ở giữa thì đánh số lại, lưu thành v3', async ({ page, request }, info) => {
  const d = docId(2, info);
  await apiSave(request, await token(request, TRANG), d, 1, [S1, S2, S3_RIGHT, S4_V1]);
  await openReview(page, request, d);
  await expectVersion(page, 2);
  await expect(stepCount(page)).toHaveCount(4);
  await page.getByTestId('s2e5-step-add-after-2').click();
  await expect(stepCount(page)).toHaveCount(5);
  await stepText(page, 3).fill(NEW_STEP);
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 3);
  await expect(stepCount(page)).toHaveCount(5);
  await expect(stepText(page, 3)).toHaveValue(NEW_STEP);
  await expect(stepText(page, 4)).toHaveValue(S3_RIGHT);
  await expect(page.getByTestId('s2e5-step-5')).toBeVisible();
  await expect(page.getByTestId('s2e5-history-desc-v3')).toHaveText('v3 · bạn thêm bước 3');
});

test('@TC-S2E5-003 xóa một bước và kéo đổi thứ tự: một lần lưu một phiên bản', async ({ page, request }, info) => {
  const d = docId(3, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, ['bước A', 'bước B', 'bước C', 'bước D', 'bước E']);
  await openReview(page, request, d);
  await expect(stepCount(page)).toHaveCount(5);
  await page.getByTestId('s2e5-step-delete-3').click();
  await expect(stepCount(page)).toHaveCount(4);
  // kéo bước 4 lên trước bước 3
  await page.getByTestId('s2e5-step-grip-4').dragTo(page.getByTestId('s2e5-step-3'));
  await expect(stepText(page, 3)).toHaveValue('bước E');
  await expect(stepText(page, 4)).toHaveValue('bước D');
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 3);
  const now = await apiView(request, t, d);
  expect(now.latestVersion).toBe(3); // nhiều thay đổi, một phiên bản
  expect(now.version.steps.map((s) => `${s.no}:${s.text}`)).toEqual(['1:bước A', '2:bước B', '3:bước E', '4:bước D']);
});

test('@TC-S2E5-004 lịch sử phiên bản đủ thông tin và mới nhất ở trên', async ({ page, request }, info) => {
  const d = docId(4, info);
  await apiSave(request, await token(request, TRANG), d, 1, WRONG_V2);
  await openReview(page, request, d);
  const rows = page.getByTestId('s2e5-history').locator('li');
  await expect(rows).toHaveCount(2);
  await expect(rows.nth(0)).toContainText('v2 · bạn sửa bước 3');
  await expect(rows.nth(0)).toContainText('Cô Trang');
  await expect(rows.nth(0)).toContainText(TRANG_CODE);
  await expect(rows.nth(0)).toContainText(/\d{2}:\d{2} \d{2}\/\d{2}\/\d{4}/);
  await expect(rows.nth(1)).toContainText('v1 · AI soạn · máy chưa kiểm');
  await expect(rows.nth(1)).toContainText(/\d{2}:\d{2} \d{2}\/\d{2}\/\d{4}/);
});

test('@TC-S2E5-005 dùng bản cũ tạo phiên bản mới là bản sao', async ({ page, request }, info) => {
  const d = docId(5, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, [S1, S2, S3_RIGHT, S4_V1]);
  await openReview(page, request, d);
  await page.getByTestId('s2e5-history-v1').click();
  await expectVersion(page, 1);
  await clickAndWait(page, 's2e5-reuse', '/reuse');
  await expectVersion(page, 3);
  await expect(page.getByTestId('s2e5-history-desc-v3')).toContainText('sao từ v1');
  await expect(stepText(page, 3)).toHaveValue(S3_V1);
  await expect(page.locator('[data-testid^="s2e5-history-v"]')).toHaveCount(3);
  const [v1, v2, v3] = [await apiView(request, t, d, 1), await apiView(request, t, d, 2), await apiView(request, t, d, 3)];
  expect(v3.version.steps.map((s) => s.text)).toEqual(v1.version.steps.map((s) => s.text));
  expect(v2.version.steps[2]!.text).toBe(S3_RIGHT); // v1, v2 còn nguyên
});

test('@TC-S2E5-006 lưu khi chưa đổi gì không tạo phiên bản', async ({ page, request }, info) => {
  const d = docId(6, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, WRONG_V2);
  await openReview(page, request, d);
  await expectVersion(page, 2);
  await page.getByTestId('s2e5-save').click();
  await expect(page.getByTestId('s2e5-notice')).toHaveText('Chưa có thay đổi để lưu');
  await expectVersion(page, 2);
  expect((await apiView(request, t, d)).latestVersion).toBe(2);
});

test('@TC-S2E5-007 bước trống bị chặn, viền đỏ, không tạo phiên bản', async ({ page, request }, info) => {
  const d = docId(7, info);
  await openReview(page, request, d);
  for (const blank of ['', '   ']) {
    await stepText(page, 3).fill(blank);
    await page.getByTestId('s2e5-save').click();
    await expect(page.getByTestId('s2e5-issue')).toHaveText('Bước 3 đang trống');
    await expect(page.getByTestId('s2e5-step-3')).toHaveClass(/invalid/);
    await expect(stepText(page, 3)).toHaveAttribute('aria-invalid', 'true');
  }
  await expectVersion(page, 1);
  expect((await apiView(request, await token(request, TRANG), d)).latestVersion).toBe(1);
});

test('@TC-S2E5-008 bước đúng 2000 ký tự được lưu', async ({ page, request }, info) => {
  const d = docId(8, info);
  const long = 'k'.repeat(2000);
  await openReview(page, request, d);
  await stepText(page, 2).fill(long);
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 2);
  await expect(stepText(page, 2)).toHaveValue(long);
  // 2001 ký tự: giao diện chặn với đúng câu thông báo
  await stepText(page, 2).fill(`${long}k`);
  await page.getByTestId('s2e5-save').click();
  await expect(page.getByTestId('s2e5-issue')).toHaveText('Bước tối đa 2000 ký tự');
  await expectVersion(page, 2);
});

test('@TC-S2E5-011 xóa còn 1 bước được, xóa bước cuối cùng bị chặn', async ({ page, request }, info) => {
  const d = docId(11, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, [S1, S2]);
  await openReview(page, request, d);
  await expect(stepCount(page)).toHaveCount(2);
  await page.getByTestId('s2e5-step-delete-2').click();
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 3);
  await expect(stepCount(page)).toHaveCount(1);
  await page.getByTestId('s2e5-step-delete-1').click();
  await expect(page.getByTestId('s2e5-issue')).toHaveText('Lời giải cần ít nhất 1 bước');
  await expect(stepCount(page)).toHaveCount(1);
  expect((await apiView(request, t, d)).latestVersion).toBe(3);
});
