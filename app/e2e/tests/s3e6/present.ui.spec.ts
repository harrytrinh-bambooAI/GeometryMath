import { expect, test } from '../../support/fixtures.ts';
import { board051, insight, openBoard, raw, script, stuckReady, student } from '../s3e3/s3e3.ts';
import { API, MSG, T3, bearer, setRelease } from './s3e6.ts';

/** US-S3E6-01 trên trình duyệt: "Chữa bài này" từ thẻ "Cả lớp vướng ở đâu" sang Trình chiếu. */

test('Bấm "Chữa bài này" mở Trình chiếu đúng đề và bước @TC-S3E6-01', async ({ page, request }) => {
  const sb = await board051(request);
  await setRelease(request, sb.classes[0]!.sets[0]!.id, -60); // lời giải Đề 3 đã mở
  await script(request, sb, { 3: [{ step: 2, text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  const card = insight(page, 3);
  await card.getByTestId('s3e6-fix').click();
  await expect(page).toHaveURL(/\/gv\/chua-bai\?/);
  await expect(page.getByTestId('s3e6-present-title')).toContainText('Đề 3');
  await expect(page.getByTestId('s3e6-present')).toHaveAttribute('data-step', '2');
  await expect(page.getByTestId('s3e6-present-step-2')).toBeVisible();
  await expect(page.getByTestId('s3e6-present-locked')).toHaveCount(0);
  await page.getByTestId('s3e6-present-prev').click();
  await expect(page.getByTestId('s3e6-present')).toHaveAttribute('data-step', '1');
  await page.getByTestId('s3e6-present-next').click();
  await expect(page.getByTestId('s3e6-present')).toHaveAttribute('data-step', '2');
  await page.getByTestId('s3e6-present-exit').click();
  const cls = sb.classes[0]!;
  await expect(page).toHaveURL(new RegExp(`/gv/ket-qua-lop\\?lop=${cls.id}&bo=${cls.sets[0]!.id}&the=3`));
  await expect(page.getByTestId('s3e3-page')).toBeVisible();
});

test('Lời giải còn khóa thì hỏi lại trước khi chiếu @TC-S3E6-02', async ({ page, request }) => {
  const sb = await board051(request);
  const set = sb.classes[0]!.sets[0]!;
  await setRelease(request, set.id, 24 * 60); // Sắp mở
  await script(request, sb, { 3: [{ step: 2, text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await insight(page, 3).getByTestId('s3e6-fix').click();
  await expect(page.getByTestId('s3e6-ask')).toContainText(MSG.s91);
  await page.getByTestId('s3e6-ask-cancel').click();
  await expect(page.getByTestId('s3e6-ask')).toHaveCount(0);
  await expect(page).toHaveURL(/\/gv\/ket-qua-lop/);
  await insight(page, 3).getByTestId('s3e6-fix').click();
  await expect(page.getByTestId('s3e6-ask')).toContainText(MSG.s91);
  await page.getByTestId('s3e6-ask-continue').click();
  await expect(page.getByTestId('s3e6-present-locked')).toHaveText(MSG.chip);
  // Bình An vẫn không lấy được lời giải
  const an = student(sb, 'Bình An');
  const sol = await request.get(`${API}/api/v1/student/assignment-sets/${set.id}/items/3/solution`, { headers: bearer(an.token) });
  // khóa: máy chủ trả 200 với trạng thái Sắp mở, không có đáp số hay bước (US-S3E2 SolutionView)
  expect(sol.status()).toBe(200);
  const lockedBody = (await sol.json()) as { releaseState: string; finalAnswer?: string; steps?: unknown };
  expect(lockedBody.releaseState).toBe('upcoming');
  expect(lockedBody.finalAnswer ?? null).toBeNull();
  expect(lockedBody.steps ?? null).toBeNull();
});

test('Thẻ dữ liệu thô không có nút @TC-S3E6-05', async ({ page, request }) => {
  // Ngọc Anh chưa mở Đề 4: Đề 4 chỉ có 4 bài làm -> dữ liệu thô
  const sb = await board051(request, { cells: { 'Ngọc Anh': ['Đúng', 'Đúng', 'Xem lại', 'Chưa làm'] } });
  await script(request, sb, { 3: [{ step: 2, text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(raw(page, 4)).toBeVisible();
  await expect(raw(page, 4).getByTestId('s3e6-fix')).toHaveCount(0);
  await expect(insight(page, 3).getByTestId('s3e6-fix')).toHaveCount(1);
});

test('Đề đã sửa phiên bản, bước không còn @TC-S3E6-06', async ({ page, request }) => {
  const sb = await board051(request);
  await setRelease(request, sb.classes[0]!.sets[0]!.id, -60);
  // thẻ trỏ bước 5 nhưng phiên bản hiện hành của Đề 3 chỉ còn 2 bước
  await script(request, sb, { 3: [{ step: 5, text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await insight(page, 3).getByTestId('s3e6-fix').click();
  await expect(page.getByTestId('s3e6-present-notice')).toHaveText(MSG.s92);
  await expect(page.getByTestId('s3e6-present')).toHaveAttribute('data-step', '1');
  await expect(page.getByTestId('s3e6-present-statement')).not.toBeEmpty();
});
