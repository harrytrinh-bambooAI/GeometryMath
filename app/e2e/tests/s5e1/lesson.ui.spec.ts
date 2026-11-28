import { expect, test } from '../../support/fixtures.ts';
import { signIn, world } from '../s3e1/s3e1.ts';
import { F020, aiScenario, awaitPlan, createPlan } from './s5e1.ts';

/** US-S5E1-01 (form C5-08) và US-S5E1-03 (màn kết quả) trên trình duyệt; AI giả lập theo kịch bản. */

test('Đổi Tiết PPCT thì số tiết và kiến thức tích sẵn được điền lại @TC-S5E1-01', async ({ page, request }) => {
  const w = await world(request);
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/giao-an/moi');
  await expect(page.getByTestId('s5e1-form')).toBeVisible();
  await page.getByTestId('s5e1-chapter').selectOption('7');
  await page.getByTestId('s5e1-lesson').selectOption('26');
  await page.getByTestId('s5e1-period').selectOption({ label: 'Tiết 1/2' });
  await expect(page.getByTestId('s5e1-sotiet')).toHaveValue('1');
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-010')).toBeChecked();
  await expect(page.getByTestId('s5e1-kt-KT11-C07-CT-003')).toBeChecked();
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-012')).not.toBeChecked();
  await expect(page.getByTestId('s5e1-style-truyen-thong')).toHaveAttribute('aria-checked', 'true');
  // đổi sang "Tiết 1-2/3" của bài 25
  await page.getByTestId('s5e1-lesson').selectOption('25');
  await page.getByTestId('s5e1-period').selectOption({ label: 'Tiết 1-2/3' });
  await expect(page.getByTestId('s5e1-sotiet')).toHaveValue('2');
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-007')).toBeChecked();
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-008')).toBeChecked();
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-009')).not.toBeChecked();
  await expect(page.getByTestId('s5e1-kt-KT11-C07-DN-010')).toHaveCount(0);
});

test('Mục có unsure mang nhãn "AI chưa chắc"; đề xuất gom vào khung ⚠ @TC-S5E1-17', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  await aiScenario(request, lan, 'unsure');
  const c = await createPlan(request, lan, F020);
  await awaitPlan(request, lan, c.draftId);
  await signIn(page, lan);
  await page.goto(`/gv/giao-an/${c.draftId}`);
  await expect(page.getByTestId('s5e1-result')).toHaveAttribute('data-status', 'ready');
  await expect(page.getByTestId('s5e1-unsure-HD4')).toHaveText('AI chưa chắc');
  for (const ma of ['HD1', 'HD2', 'HD3']) await expect(page.getByTestId(`s5e1-unsure-${ma}`)).toHaveCount(0);
  const box = page.getByTestId('s5e1-proposals');
  await expect(box.getByRole('heading')).toHaveText('⚠ AI đề xuất (1)');
  await expect(box.getByTestId('s5e1-proposal-0')).toContainText("Tổng 50' > 45'");
  await expect(box.getByTestId('s5e1-proposal-accept-0')).toHaveText('Nhận');
  await expect(box.getByTestId('s5e1-proposal-skip-0')).toHaveText('Bỏ qua');
  // đề xuất chưa được áp: thời lượng giữ nguyên Σ 45'
  await expect(page.getByTestId('s5e1-hd-HD4')).toContainText("5'");
  await expect(page.getByTestId('s5e1-total')).toContainText("Σ 45'");
});

test('AI hỏi lại đúng một câu khi ghi chú mâu thuẫn với form @TC-S5E1-18', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  await aiScenario(request, lan, 'question');
  await signIn(page, lan);
  await page.goto('/gv/giao-an/moi');
  await page.getByTestId('s5e1-chapter').selectOption('7');
  await page.getByTestId('s5e1-lesson').selectOption('26');
  await page.getByTestId('s5e1-period').selectOption({ label: 'Tiết 1/2' });
  await page.getByTestId('s5e1-doituong-kha').click();
  await page.getByTestId('s5e1-options').click();
  await page.getByTestId('s5e1-ghichu').fill('dạy gộp hai tiết');
  await page.getByTestId('s5e1-submit').click();
  await expect(page).toHaveURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
  await expect(page.getByTestId('s5e1-result')).toHaveAttribute('data-status', 'question', { timeout: 15_000 });
  await expect(page.getByTestId('s5e1-question')).toHaveCount(1);
  await expect(page.getByTestId('s5e1-question')).toContainText('Thầy cô muốn soạn 1 hay 2 tiết?');
  await expect(page.getByTestId('s5e1-answer')).toBeFocused();
  await expect(page.getByTestId('s5e1-open-field')).toHaveText('Mở trường Số tiết');
  await expect(page.getByTestId('s5e1-sec-tien-trinh')).toHaveCount(0);
  await expect(page.getByTestId('s5e1-sec-muc-tieu')).toHaveCount(0);
});
