import { expect, test } from '../../support/fixtures.ts';
import { signIn } from '../s3e1/s3e1.ts';
import { MSG, rb01, s5e4World } from './s5e4.ts';

/** US-S5E4-01 trên trình duyệt: màn soạn rubric C5-02 hiện tổng điểm sống và chặn lưu khi lệch thang. */

test('Màn soạn rubric hiện tổng điểm sống và chặn lưu khi lệch @TC-S5E4-03', async ({ page, request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const rb = await rb01(request, lan);
  await signIn(page, lan);
  await page.goto(`/gv/rubric?id=${rb.id}`);
  await expect(page.getByTestId('s5e4-editor')).toBeVisible();
  const total = page.getByTestId('s5e4-total');
  await expect(total).toHaveText('Tổng 10 / 10');
  await expect(total).toHaveAttribute('data-warn', 'false');
  await expect(page.getByTestId('s5e4-save')).toBeEnabled();
  // đổi "Kết quả" từ 2 thành 3
  await expect(page.getByTestId('s5e4-crit-name-2')).toHaveValue('Kết quả');
  await page.getByTestId('s5e4-crit-points-2').fill('3');
  await expect(total).toHaveText('Tổng 11 / 10');
  await expect(total).toHaveAttribute('data-warn', 'true');
  await expect(total).toHaveClass(/s5e4-warn/);
  await expect(page.getByTestId('s5e4-save')).toBeDisabled();
  await expect(page.getByTestId('s5e4-msg30')).toHaveText(MSG.s30);
  // trả về 2: hết cảnh báo
  await page.getByTestId('s5e4-crit-points-2').fill('2');
  await expect(total).toHaveText('Tổng 10 / 10');
  await expect(page.getByTestId('s5e4-msg30')).toHaveCount(0);
});
