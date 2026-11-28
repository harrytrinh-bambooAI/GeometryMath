import { expect, test } from '../../../support/fixtures.ts';
import { signIn } from '../../s3e1/s3e1.ts';
import { ack, submitOk, P1, P2 } from '../submit/submit.ts';
import { gradingWorld, MSG, review } from './grading.ts';

/**
 * US-S5E4-06-SC1 trên màn C5-05 (T-261128-60): ảnh 3 trang bên trái, rubric 5 tiêu chí bên phải, tổng tự tính 7; Duyệt
 * hiện MSG-S5-38, bài sang Đã duyệt, mở bài kế tiếp trong hàng chờ. Phím tắt QS5-23 (A = Duyệt) trên bài kế tiếp.
 */
test('Chấm và duyệt BN-02 trên C5-05 @TC-S5E4-27', async ({ page, request }) => {
  const c = await gradingWorld(request);
  const an = c.w.people['Bình An']!;
  const mai = c.w.people['Chi Mai']!;
  await ack(request, an);
  const bn01 = await submitOk(request, an, c.bc.id, [P1, P2]);
  await ack(request, mai);
  const bn02 = await submitOk(request, mai, c.bc.id, [P1, P2, P2]);

  await signIn(page, c.lan);
  await page.goto(`/gv/cham/${bn02.id}`);
  const pages = page.getByTestId('s5e4-review-pages').locator('img');
  await expect(pages).toHaveCount(3);
  await expect(page.getByTestId('s5e4-review-page-3')).toBeVisible();
  const rubric = page.getByTestId('s5e4-review-rubric');
  await expect(rubric.locator('input')).toHaveCount(5);
  // ảnh bên trái, rubric bên phải
  const left = await page.getByTestId('s5e4-review-pages').boundingBox();
  const right = await rubric.boundingBox();
  expect(left!.x).toBeLessThan(right!.x);
  const pts = ['2', '3', '0', '1', '1'];
  for (let k = 0; k < 5; k++) await page.getByTestId(`s5e4-crit-input-${k + 1}`).fill(pts[k]!);
  await expect(page.getByTestId('s5e4-review-total')).toHaveText('Tổng: 7 / 10');
  await page.getByTestId('s5e4-review-comment').fill('Xem lại cách tính SB');
  await page.getByTestId('s5e4-review-approve').click();
  await expect(page).toHaveURL(new RegExp(`/gv/cham/${bn01.id}$`));
  await expect(page.getByTestId('s5e4-review-toast')).toHaveText(MSG.s38);
  const v = await review(request, c.lan, bn02.id);
  expect(v.status).toBe('approved');
  expect(v.statusLabel).toBe('Đã duyệt');
  expect(v.grade).toMatchObject({ total: 7, comment: 'Xem lại cách tính SB' });

  // phím tắt: điền điểm BN-01 rồi bấm A (ngoài ô nhập) để Duyệt
  const full = ['2', '4', '2', '1', '1'];
  for (let k = 0; k < 5; k++) await page.getByTestId(`s5e4-crit-input-${k + 1}`).fill(full[k]!);
  await page.getByTestId('s5e4-review-title').click();
  await page.keyboard.press('a');
  await expect(page.getByTestId('s5e4-review-toast')).toHaveText(MSG.s38);
  await expect(page.getByTestId('s5e4-review-status')).toHaveText('Đã duyệt');
});
