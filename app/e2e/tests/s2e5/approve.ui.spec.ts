import { expect, test } from '../../support/fixtures.ts';
import { API, MSG, RIGHT, S1, S3_RIGHT, S4_RIGHT, TRANG, TRANG_CODE, TRANG_S5, apiCheck, apiSave, apiView, bearer, classId, clickAndWait, docId, expectVersion, openReview, sp01Version, stepCheck, stepCount, stepText, token } from './s2e5.ts';

/** S2-E5 — C2-03: hộp duyệt chung, nhận / sửa / bỏ đề xuất của AI, chặn duyệt khi còn bước lệch (US-S2E5-04). */

const PROPOSAL = 'Vậy tam giác SAC vuông cân tại A.';

test('@TC-S2E5-039 duyệt khi máy Đã kiểm chứng: không hộp cảnh báo, ghi mã người duyệt', async ({ page, request }, info) => {
  const d = docId(39, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, RIGHT);
  await apiCheck(request, t, d, 2);
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.verified);
  await clickAndWait(page, 's2e5-approve', '/approve');
  await expect(page.getByTestId('s2e5-confirm')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-human-box')).toHaveText(MSG.approved);
  await expect(page.getByTestId('s2e5-approved')).toContainText('Giáo viên đã duyệt v2');
  await expect(page.getByTestId('s2e5-approved')).toContainText(TRANG_CODE);
  await expect(page.getByTestId('s2e5-approved')).toContainText(/\d{2}:\d{2} \d{2}\/\d{2}\/\d{4}/);
  await expect(page.getByTestId('s2e5-shelf')).toHaveText('Của tôi');
  const now = await apiView(request, t, d);
  expect(now.version.approvedCode).toBe(TRANG_CODE);
  expect(now.version.version).toBe(2);
  expect(now.inMyShelf).toBe(true);
});

test('@TC-S2E5-040 duyệt khi Cần xem lại phải xác nhận cảnh báo', async ({ page, request }, info) => {
  const d = docId(40, info);
  await apiSave(request, await token(request, TRANG), d, 1, RIGHT); // chưa kiểm, không bước Không đạt
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-approve-hint')).toContainText('giữ nhãn "Cần xem lại"');
  await page.getByTestId('s2e5-approve').click();
  await expect(page.getByTestId('s2e5-confirm')).toContainText(MSG.confirm);
  await clickAndWait(page, 's2e5-confirm-yes', '/approve');
  await expect(page.getByTestId('s2e5-human-box')).toHaveText(MSG.approved);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.needsReview);
});

test('@TC-S2E5-041 hủy ở hộp cảnh báo thì không duyệt', async ({ page, request }, info) => {
  const d = docId(41, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, RIGHT);
  await openReview(page, request, d);
  await page.getByTestId('s2e5-approve').click();
  await expect(page.getByTestId('s2e5-confirm')).toBeVisible();
  await page.getByTestId('s2e5-confirm-no').click();
  await expect(page.getByTestId('s2e5-confirm')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-human-box')).toHaveText(MSG.editing);
  const now = await apiView(request, t, d);
  expect(now.version.approvedAt ?? null).toBeNull();
  expect(now.version.humanLabel).toBe(MSG.editing);
  expect(now.inMyShelf).toBe(false);
});

test('@TC-S2E5-043 0 bước lệch thì Duyệt mở, 1 bước lệch thì khóa', async ({ page, request }, info) => {
  const d = docId(43, info);
  await openReview(page, request, d);
  // A: v1 chưa kiểm, không có bước Không đạt
  await expect(page.getByTestId('s2e5-approve')).toBeEnabled();
  await expect(page.getByTestId('s2e5-blocked')).toHaveCount(0);
  // B: sau khi kiểm có đúng 1 bước Không đạt
  await clickAndWait(page, 's2e5-check', '/check');
  await expect(stepCheck(page, 3)).toHaveText('Lệch');
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
  await expect(page.getByTestId('s2e5-blocked')).toContainText('Chặn duyệt: còn 1 bước lệch');
});

test('@TC-S2E5-044 còn 3 bước lệch thì thông báo đúng số', async ({ page, request }, info) => {
  const d = docId(44, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, [S1, 'ABCD vuông cạnh a nên AC = a√3.', 'SA = 2a.', 'tan SCA = SA / AC = 5.', 'Vậy góc cần tìm là góc SCA.']);
  expect((await apiCheck(request, t, d, 2)).version.failCount).toBe(3);
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-blocked')).toContainText('Chặn duyệt: còn 3 bước lệch');
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
  await expect(page.locator('.s2e5-step.bad')).toHaveCount(3);
});

test('@TC-S2E5-045 nhận đề xuất của AI thành phiên bản mới', async ({ page, request }, info) => {
  const d = docId(45, info);
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-proposal')).toHaveCount(1);
  await expect(page.getByTestId('s2e5-proposal-text')).toHaveText(PROPOSAL);
  await clickAndWait(page, 's2e5-proposal-accept', '/accept');
  await expectVersion(page, 2);
  await expect(stepCount(page)).toHaveCount(5);
  await expect(stepText(page, 3)).toHaveValue(PROPOSAL);
  await expect(page.getByTestId('s2e5-proposal')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-proposals-empty')).toHaveText(MSG.emptyBox);
  await expect(page.getByTestId('s2e5-history-desc-v2')).toContainText('nhận đề xuất của AI');
});

test('@TC-S2E5-046 sửa đề xuất mở soạn với nội dung đề xuất, chưa tạo phiên bản tới khi Lưu', async ({ page, request }, info) => {
  const d = docId(46, info);
  const t = await token(request, TRANG);
  await openReview(page, request, d);
  await page.getByTestId('s2e5-proposal-edit').click();
  await expect(stepCount(page)).toHaveCount(5);
  await expect(stepText(page, 3)).toHaveValue(PROPOSAL);
  await expectVersion(page, 1);
  expect((await apiView(request, t, d)).latestVersion).toBe(1); // chưa có phiên bản mới
  await stepText(page, 3).fill('Tam giác SAC vuông cân tại A vì SA = AC.');
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 2);
  await expect(page.getByTestId('s2e5-proposals-empty')).toBeVisible();
  const now = await apiView(request, t, d);
  expect(now.version.steps[2]!.text).toBe('Tam giác SAC vuông cân tại A vì SA = AC.');
  expect(now.proposals).toEqual([]);
});

test('@TC-S2E5-047 bỏ đề xuất thì loại và còn dấu vết', async ({ page, request }, info) => {
  const d = docId(47, info);
  const t = await token(request, TRANG);
  await openReview(page, request, d);
  await clickAndWait(page, 's2e5-proposal-dismiss', '/dismiss');
  await expect(page.getByTestId('s2e5-proposal')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-history-event')).toContainText('đã bỏ');
  await expectVersion(page, 1);
  const now = await apiView(request, t, d);
  expect(now.latestVersion).toBe(1);
  expect(now.history.filter((h) => h.kind === 'proposal')).toHaveLength(1);
});

test('@TC-S2E5-052 hộp duyệt rỗng: nút Duyệt vẫn theo trạng thái lời giải', async ({ page, request }, info) => {
  const d = docId(52, info);
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-proposals-empty')).toHaveText(MSG.emptyBox);
  await expect(page.getByTestId('s2e5-approve')).toBeEnabled();
  await clickAndWait(page, 's2e5-check', '/check');
  await expect(page.getByTestId('s2e5-proposals-empty')).toHaveText(MSG.emptyBox);
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
});

test('@TC-S2E5-065 luồng đầy đủ: AI soạn sai, sửa, kiểm, duyệt, giao', async ({ page, request }, info) => {
  const d = docId(65, info);
  const t = await token(request, TRANG_S5);
  await openReview(page, request, d, TRANG_S5);
  // 1. kiểm lần 1: bước 3 đỏ, Duyệt khóa
  await clickAndWait(page, 's2e5-check', '/check');
  await expect(stepCheck(page, 3)).toHaveText('Lệch');
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
  // 2. sửa bước 3 và 4, lưu v2
  await stepText(page, 3).fill(S3_RIGHT);
  await stepText(page, 4).fill(S4_RIGHT);
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 2);
  // 3. kiểm lại: mọi bước có phép tính Đạt, nhãn máy "Đã kiểm chứng"
  await clickAndWait(page, 's2e5-check', '/check');
  for (const no of [2, 3, 4]) await expect(stepCheck(page, no)).toHaveText('Đạt');
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.verified);
  await expect(page.getByTestId('s2e5-machine-note-box')).toHaveText(MSG.machineNote);
  // 4. duyệt
  await clickAndWait(page, 's2e5-approve', '/approve');
  await expect(page.getByTestId('s2e5-human-box')).toHaveText(MSG.approved);
  // 5. giao cho lớp 11A1 (API giao bài của MVP): bài giao gắn với v2
  const give = await request.post(`${API}/api/v1/teacher/assignments`, {
    headers: bearer(t),
    data: { assignmentId: crypto.randomUUID(), classId: classId(info), problemVersionId: sp01Version(info), releaseAt: new Date(Date.now() + 2 * 3600_000).toISOString() },
  });
  expect(give.status(), await give.text()).toBe(201);
  await page.reload();
  await expect(page.getByTestId('s2e5-assigned')).toContainText('Đã giao cho 11A1 (v2)');
  await expect(page.locator('[data-testid^="s2e5-history-v"]')).toHaveCount(2);
  const now = await apiView(request, t, d);
  expect(now.assignments).toEqual([{ className: '11A1', version: 2 }]);
  expect(now.history.filter((h) => h.kind === 'version').map((h) => h.version)).toEqual([2, 1]);
});
