import { expect, test } from '../../../support/fixtures.ts';
import { signIn } from '../../s3e1/s3e1.ts';
import { gradingWorld, sc1 } from '../review/grading.ts';

/** US-S5E4-05-SC1 trên màn C5-04 (T-261128-60): lọc lớp 11A1, bài BC-01; cờ trước, theo giờ nộp; không cột xếp hạng. */
test('Hàng chờ BC-01 sau hạn trên C5-04 @TC-S5E4-23', async ({ page, request }) => {
  const c = await gradingWorld(request);
  const { bn01, bn02, bn03, bn05 } = await sc1(request, c);
  await signIn(page, c.lan);
  await page.goto('/gv/hang-cho');
  await page.getByTestId('s5e4-queue-class').selectOption(c.c11.id);
  await page.getByTestId('s5e4-queue-assignment').selectOption(c.bc.id);
  await expect(page.getByTestId('s5e4-queue-summary')).toHaveText('4 chờ chấm · 1 chưa nộp · 0 đã duyệt');
  const rows = page.locator('.s5e4-queue-row');
  await expect(rows).toHaveCount(4);
  const codes = [bn03, bn05, bn01, bn02].map((s) => s.code);
  for (let k = 0; k < 4; k++) await expect(rows.nth(k).locator('td').first()).toHaveText(codes[k]!);
  await expect(rows.nth(0)).toContainText('Chưa đọc được');
  await expect(rows.nth(0)).toContainText('Đức Huy');
  await expect(rows.nth(1)).toContainText('Nộp muộn');
  await expect(rows.nth(1)).toContainText('Hà My');
  await expect(rows.nth(2).getByTestId('s5e4-flag-late')).toHaveCount(0);
  const head = page.getByTestId('s5e4-queue-list').locator('thead');
  await expect(head).not.toContainText(/Điểm|Hạng|Xếp/);
});
