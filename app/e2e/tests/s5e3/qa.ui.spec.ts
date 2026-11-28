import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, signIn } from '../s3e1/s3e1.ts';
import { bc01, rb01 } from '../s5e4/s5e4.ts';
import { json } from './kb.ts';
import { MSG, PRIVACY_VERSION, Q_SC1, helpUrl, setup } from './qa.ts';

/**
 * US-S5E3-03 / 04 trên trình duyệt (iPad ở dự án ui-webkit): màn "Hỏi đáp" của học sinh (thông báo riêng tư lần đầu,
 * nguồn bấm mở đúng mục) và mục "Trợ giúp của AI" của giáo viên. AI giả lập F-S5-094.
 */

test('Bấm nguồn mở đúng mục tài liệu @TC-S5E3-13', async ({ page, request }) => {
  const { an, bc } = await setup(request);
  await signIn(page, an);
  await page.goto(`/hs/hoi-dap?bai=${bc.id}`);
  await page.getByTestId('s5e3-qa-question').fill(Q_SC1);
  await page.getByTestId('s5e3-qa-send').click();
  await expect(page.getByTestId('s5e3-qa-answer')).toHaveAttribute('data-kind', 'concept');
  const src = page.getByTestId('s5e3-qa-source');
  await expect(src).toContainText('Theo: Ghi chú Khoảng cách 11A1 · v2');
  await src.click();
  const reader = page.getByTestId('s5e3-qa-reader');
  await expect(page.getByTestId('s5e3-qa-reader-title')).toHaveText('Hệ thức 1/AH²');
  await expect(reader).toContainText('Ghi chú Khoảng cách 11A1 · v2');
  await expect(page.getByTestId('s5e3-qa-reader-body')).toContainText('1/AH² = 1/SA² + 1/AK²');
  // chỉ mục được dẫn, không thấy các mục khác của tài liệu
  await expect(reader).not.toContainText('Góc giữa hai mặt phẳng');
  await expect(reader).not.toContainText('hình chiếu vuông góc của M');
});

test('Lần hỏi đầu phải xác nhận thông báo riêng tư @TC-S5E3-18', async ({ page, request }) => {
  const { w, bc } = await setup(request);
  const mai = w.people['Chi Mai']!; // chưa đọc MSG-S5-26
  await signIn(page, mai);
  await page.goto(`/hs/hoi-dap?bai=${bc.id}`);
  await expect(page.getByTestId('s5e3-qa-msg26')).toHaveText(MSG.s26);
  await page.getByTestId('s5e3-qa-question').fill('Khoảng cách từ điểm đến mặt phẳng là gì?');
  await expect(page.getByTestId('s5e3-qa-send')).toBeDisabled();
  await page.getByTestId('s5e3-qa-ack').click();
  await expect(page.getByTestId('s5e3-qa-privacy')).toHaveCount(0);
  const sent = page.waitForResponse((r) => r.url().endsWith('/api/v1/student/kb-qa') && r.request().method() === 'POST');
  await page.getByTestId('s5e3-qa-send').click();
  expect((await sent).status()).toBe(200);
  await expect(page.getByTestId('s5e3-qa-answer')).toBeVisible();
  const p = await json<{ version: string; acked: boolean }>(
    await request.get(`${API}/api/v1/student/kb-qa/privacy`, { headers: bearer(mai.token) }),
    200,
  );
  expect(p).toMatchObject({ version: PRIVACY_VERSION, acked: true });
});

test('Giáo viên đặt mức trợ giúp khi tạo bài @TC-S5E3-26', async ({ page, request }) => {
  const { w } = await setup(request, 'off');
  const lan = w.people['Cô Lan']!;
  // bài có chấm mới tạo (chưa đặt mức): mặc định Tắt
  const rb = await rb01(request, lan);
  const fresh = await bc01(request, lan, w.classes['11A1']!.id, rb.id);
  const def = await json<{ helpLevel: string; isDefault: boolean }>(await request.get(helpUrl(fresh.id), { headers: bearer(lan.token) }), 200);
  expect(def).toMatchObject({ helpLevel: 'off', isDefault: true });
  await signIn(page, lan);
  await page.goto('/gv/tro-giup-ai');
  const panel = page.getByTestId(`s5e4-help-${fresh.id}`);
  await expect(panel.getByTestId('s5e4-help-summary')).toHaveText('Trợ giúp: Tắt');
  await panel.getByTestId('s5e4-help-toggle').click();
  await expect(panel.getByTestId('s5e4-help-box')).toContainText('Trợ giúp của AI');
  await expect(panel.getByTestId('s5e4-help-select')).toHaveValue('off');
  await panel.getByTestId('s5e4-help-select').selectOption({ label: 'Chỉ gợi ý' });
  await panel.getByTestId('s5e4-help-save').click();
  await expect(panel.getByTestId('s5e4-help-summary')).toHaveText('Trợ giúp: Chỉ gợi ý');
  const after = await json<{ helpLevel: string }>(await request.get(helpUrl(fresh.id), { headers: bearer(lan.token) }), 200);
  expect(after.helpLevel).toBe('hints');
});
