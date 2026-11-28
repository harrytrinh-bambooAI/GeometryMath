import { expect, test } from '../../support/fixtures.ts';
import { AN_S5, API, ASSIGNED_BG, ASSIGNED_DOC, MSG, RIGHT, S1, TRANG, TRANG_S5, WRONG_V2, apiCheck, apiSave, apiView, bearer, clickAndWait, docId, expectVersion, openReview, stepCheck, stepText, token } from './s2e5.ts';
import { signInPage } from '../e1/e1.ts';

/** S2-E5 — C2-03: StepCheck từng bước (US-S2E5-02) và nhãn hai trục (US-S2E5-03). */

test('@TC-S2E5-015 bước 3 sai tô đỏ kèm lý do, các bước khác Đạt', async ({ page, request }, info) => {
  const d = docId(15, info);
  await apiSave(request, await token(request, TRANG), d, 1, WRONG_V2);
  await openReview(page, request, d);
  await clickAndWait(page, 's2e5-check', '/check');
  await expect(page.getByTestId('s2e5-step-3')).toHaveAttribute('data-check', 'fail');
  await expect(page.getByTestId('s2e5-step-3')).toHaveClass(/bad/);
  await expect(stepCheck(page, 3)).toHaveText('Lệch');
  await expect(page.getByTestId('s2e5-step-reason-3')).toHaveText('ghi √2 ≈ 1,41; máy tính lại 1');
  await expect(stepCheck(page, 2)).toHaveText('Đạt');
  await expect(stepCheck(page, 4)).toHaveText('Đạt'); // đạt theo giá trị bước 3 (54,7°): lỗi không lan
  await expect(page.getByTestId('s2e5-summary')).toContainText('1 bước lệch');
});

test('@TC-S2E5-016 sửa đúng bước 3 rồi kiểm lại thì mọi bước có phép tính Đạt', async ({ page, request }, info) => {
  const d = docId(16, info);
  await apiSave(request, await token(request, TRANG), d, 1, RIGHT);
  await openReview(page, request, d);
  await clickAndWait(page, 's2e5-check', '/check');
  for (const no of [2, 3, 4]) await expect(stepCheck(page, no)).toHaveText('Đạt');
  await expect(page.locator('[data-testid^="s2e5-step-"][data-check="fail"]')).toHaveCount(0);
  await expect(page.locator('.s2e5-step.bad')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-summary')).toHaveText(MSG.machineNote);
  await expect(page.getByTestId('s2e5-machine-note-box')).toHaveText(MSG.machineNote);
});

test('@TC-S2E5-017 bước chỉ chữ là Không kiểm được, không tính là lệch', async ({ page, request }, info) => {
  const d = docId(17, info);
  await openReview(page, request, d);
  await expect(stepText(page, 1)).toHaveValue(S1);
  await clickAndWait(page, 's2e5-check', '/check');
  await expect(stepCheck(page, 1)).toHaveText('Không kiểm được');
  await expect(stepCheck(page, 1)).not.toHaveClass(/red|grn|yel/); // màu xám
  await expect(page.getByTestId('s2e5-step-reason-1')).toHaveText('bước không có phép tính');
  await expect(page.getByTestId('s2e5-summary')).toContainText('1 bước lệch'); // chỉ bước 3 của v1
  await expect(page.getByTestId('s2e5-blocked')).toContainText('Chặn duyệt: còn 1 bước lệch');
});

test('@TC-S2E5-023 xem v1 hiện kết quả kiểm của v1, không phải của v2', async ({ page, request }, info) => {
  const d = docId(23, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, WRONG_V2);
  await apiCheck(request, t, d, 2);
  await openReview(page, request, d);
  await expect(stepCheck(page, 3)).toHaveText('Lệch');
  await page.getByTestId('s2e5-history-v1').click();
  await expectVersion(page, 1);
  for (const no of [1, 2, 3, 4]) await expect(stepCheck(page, no)).toHaveText('Chưa kiểm');
  await page.getByTestId('s2e5-history-v2').click();
  await expectVersion(page, 2);
  await expect(stepCheck(page, 3)).toHaveText('Lệch');
  await expect(stepCheck(page, 2)).toHaveText('Đạt');
});

test('@TC-S2E5-028 lời giải AI chưa kiểm hiện Cần xem lại và AI soạn cùng nhau', async ({ page, request }, info) => {
  await openReview(page, request, docId(28, info));
  for (const where of ['figure', 'box']) {
    await expect(page.getByTestId(`s2e5-machine-${where}`)).toHaveText(MSG.needsReview);
    await expect(page.getByTestId(`s2e5-machine-${where}`)).toHaveClass(/yel/);
    await expect(page.getByTestId(`s2e5-human-${where}`)).toHaveText(MSG.ai);
    await expect(page.getByTestId(`s2e5-human-${where}`)).toHaveClass(/s2e5-pur/);
    await expect(page.getByTestId(`s2e5-machine-note-${where}`)).toHaveCount(0);
  }
  await expect(page.getByTestId('s2e5-page')).not.toContainText(MSG.verified);
  // giao diện không có nút nào đổi nhãn máy (BR-S2E5-15)
  await expect(page.getByTestId('s2e5-labels-box').locator('button, input, select')).toHaveCount(0);
  await expect(page.getByTestId('s2e5-labels-figure').locator('button, input, select')).toHaveCount(0);
});

test('@TC-S2E5-029 kiểm đạt toàn bộ hiện Đã kiểm chứng kèm dòng chú thích', async ({ page, request }, info) => {
  const d = docId(29, info);
  await apiSave(request, await token(request, TRANG), d, 1, RIGHT);
  await openReview(page, request, d);
  await clickAndWait(page, 's2e5-check', '/check');
  for (const where of ['figure', 'box']) {
    await expect(page.getByTestId(`s2e5-machine-${where}`)).toHaveText(MSG.verified);
    await expect(page.getByTestId(`s2e5-machine-note-${where}`)).toHaveText(MSG.machineNote);
    await expect(page.getByTestId(`s2e5-human-${where}`)).toHaveText(MSG.editing); // chưa phải "Giáo viên đã duyệt"
  }
});

test('@TC-S2E5-030 duyệt không đổi nhãn máy', async ({ page, request }, info) => {
  const d = docId(30, info);
  await apiSave(request, await token(request, TRANG), d, 1, RIGHT); // chưa kiểm: nhãn máy "Cần xem lại"
  await openReview(page, request, d);
  await page.getByTestId('s2e5-approve').click();
  await expect(page.getByTestId('s2e5-confirm')).toContainText(MSG.confirm);
  await clickAndWait(page, 's2e5-confirm-yes', '/approve');
  await expect(page.getByTestId('s2e5-human-box')).toHaveText(MSG.approved);
  await expect(page.getByTestId('s2e5-human-figure')).toHaveText(MSG.approved);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e5-machine-figure')).toHaveText(MSG.needsReview);
});

test('@TC-S2E5-035 sửa sau khi Đã kiểm chứng thì về Cần xem lại, bản cũ giữ nhãn', async ({ page, request }, info) => {
  const d = docId(35, info);
  const t = await token(request, TRANG);
  await apiSave(request, t, d, 1, RIGHT);
  await apiCheck(request, t, d, 2);
  await openReview(page, request, d);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.verified);
  await stepText(page, 2).fill('AC = a√3');
  await clickAndWait(page, 's2e5-save', '/versions');
  await expectVersion(page, 3);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e5-machine-note-box')).toHaveCount(0);
  await expect(stepCheck(page, 1)).toHaveText('Không kiểm được'); // bước trước giữ kết quả
  for (const no of [2, 3, 4]) await expect(stepCheck(page, no)).toHaveText('Chưa kiểm');
  await page.getByTestId('s2e5-history-v2').click();
  await expectVersion(page, 2);
  await expect(page.getByTestId('s2e5-machine-box')).toHaveText(MSG.verified);
  // kiểm lại v3: AC = a√3 lệch hình, nhãn máy vẫn "Cần xem lại"
  expect((await apiCheck(request, t, d, 3)).version.machineLabel).toBe(MSG.needsReview);
});

test('@TC-S2E5-036 học sinh thấy nhãn của phiên bản đã giao, không thấy nhãn của bản mới hơn', async ({ page, request }) => {
  // điều kiện: v2 đã giao "Cần xem lại" + "Giáo viên đã duyệt"; v3 mới hơn "Đã kiểm chứng" + "Đang sửa" chưa áp dụng
  const teacher = await token(request, TRANG_S5);
  const v3 = await apiView(request, teacher, ASSIGNED_DOC);
  expect([v3.version.version, v3.version.machineLabel, v3.version.humanLabel]).toEqual([3, MSG.verified, MSG.editing]);
  expect(v3.assignments).toEqual([{ className: '11A1', version: 2 }]);
  await signInPage(page, request, AN_S5);
  await page.goto(`/hs/nhan-loi-giai/${ASSIGNED_BG}`);
  await expect(page.getByTestId('s2e5-student')).toHaveAttribute('data-version', '2');
  await expect(page.getByTestId('s2e5-machine-student')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e5-human-student')).toHaveText(MSG.approved);
  await expect(page.getByTestId('s2e5-student')).not.toContainText(MSG.verified);
  await expect(page.getByTestId('s2e5-machine-note-student')).toHaveCount(0);
  // dữ liệu trả về cho học sinh chỉ có nhãn: không bước giải, không StepCheck, không đáp số
  const an = await token(request, AN_S5);
  const r = await request.get(`${API}/api/v1/student/assignments/${ASSIGNED_BG}/solution-labels`, { headers: bearer(an) });
  expect(r.status()).toBe(200);
  expect(Object.keys((await r.json()) as object).sort()).toEqual(['assignmentId', 'humanLabel', 'machineLabel', 'version']);
  expect(await r.text()).not.toMatch(/45|tan|steps|check/);
  expect(r.headers()['cache-control']).toContain('no-store');
  // sổ của giáo viên vẫn không mở được với học sinh
  expect([403, 404]).toContain((await request.get(`${API}/api/v1/teacher/solutions/${ASSIGNED_DOC}`, { headers: bearer(an) })).status());
});
