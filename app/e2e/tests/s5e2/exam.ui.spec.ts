import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';
import { KT15, LAN, createSet, importBank, lanToken, type ExamSet } from './s5e2.ts';

/** US-S5E2-02 (form C5-08) và US-S5E2-04 (bộ đề nháp: thay câu cùng ô, nhãn hai trục) trên trình duyệt. */

test.beforeAll(async ({ request }) => {
  await importBank(request);
});

test.beforeEach(async ({ page, request }) => {
  await signInPage(page, request, LAN);
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
});

test('Chọn đề 15 phút, form điền theo ma trận chuẩn @TC-S5E2-05', async ({ page }) => {
  await page.goto('/gv/ra-de');
  await page.getByTestId('s5e2-loai-de').selectOption(KT15);
  await expect(page.getByTestId('s5e2-loai-de').locator('option:checked')).toContainText('Kiểm tra 15 phút');
  await expect(page.getByTestId('s5e2-thoi-gian')).toHaveValue('15');
  await expect(page.getByTestId('s5e2-phan-P1-so-cau')).toHaveValue('6');
  await expect(page.getByTestId('s5e2-phan-P1-diem')).toHaveValue('1');
  await expect(page.getByTestId('s5e2-phan-P2-so-cau')).toHaveValue('2');
  await expect(page.getByTestId('s5e2-phan-P2-diem')).toHaveValue('2');
  await expect(page.getByTestId('s5e2-phan-P1')).toContainText('Trắc nghiệm 4 lựa chọn');
  await expect(page.getByTestId('s5e2-phan-P2')).toContainText('Trả lời ngắn');
  await expect(page.getByTestId('s5e2-tong-diem')).toContainText('10 / 10');
  await expect(page.getByTestId('s5e2-so-ma')).toHaveValue('4');
  // Trường cố định (tổng điểm 10, cấu trúc, đảo câu/phương án) chỉ là chữ, không có ô sửa.
  await expect(page.getByTestId('s5e2-co-dinh')).toContainText('Tổng điểm 10 (cố định)');
  await expect(page.getByTestId('s5e2-co-dinh').locator('input, select, textarea')).toHaveCount(0);
  await expect(page.getByTestId('s5e2-form').locator('input[name="tong-diem"], [data-testid="s5e2-tong-diem"] input')).toHaveCount(0);
});

test('Thay một câu cùng ô @TC-S5E2-16', async ({ page, request }) => {
  const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
  expect(r.status()).toBe(201);
  const set = (await r.json()) as ExamSet;
  const before = set.variants[0]!.cau;
  await page.goto(`/gv/ra-de/${set.id}`);
  await expect(page.getByTestId('s5e2-nhan-nguoi')).toHaveText('Máy lắp');
  await page.getByTestId('s5e2-ma-101').click();
  const q3 = page.getByTestId('s5e2-cau-3');
  await expect(q3).toHaveAttribute('data-id-cau', before[2]!.id_cau);
  const replaced = page.waitForResponse((res) => res.url().endsWith(`/api/exams/${set.id}/replace`) && res.request().method() === 'POST');
  await page.getByTestId('s5e2-cau-3-thay').click();
  expect((await replaced).status()).toBe(200);
  await expect(q3).not.toHaveAttribute('data-id-cau', before[2]!.id_cau);
  await expect(q3).toHaveAttribute('data-chu-de', before[2]!.chu_de);
  await expect(q3).toHaveAttribute('data-dang', before[2]!.dang_tx);
  await expect(q3).toHaveAttribute('data-muc-do', before[2]!.muc_do);
  await expect(q3).toHaveAttribute('data-diem', String(before[2]!.diem));
  await expect(page.getByTestId('s5e2-cau-3-nhan-may')).toHaveText('Đã kiểm chứng');
  for (const q of before.filter((x) => x.so !== 3)) {
    await expect(page.getByTestId(`s5e2-cau-${q.so}`)).toHaveAttribute('data-id-cau', q.id_cau);
  }
});
