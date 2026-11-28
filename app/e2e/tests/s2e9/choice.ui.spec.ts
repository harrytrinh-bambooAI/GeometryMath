import { randomUUID } from 'node:crypto';
import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, attempt, bearer, noHorizontalScroll, openPlay, report, sandbox, signIn, student, type Sandbox } from '../s2e8/s2e8.ts';
import { ANGLES, MSG, assignedWithChoices, choiceAttempt, ok, optionId, teacherChoices, type ChoiceAttempt } from './s2e9.ts';

/**
 * S2-E9 · US-S2E9-03 câu trắc nghiệm trong bài luyện: giáo viên soạn ở C2-08 (web), học sinh làm ở C2-06 (iPad):
 * chọn một lựa chọn rồi "Kiểm tra", không bàn phím toán, lựa chọn sai bị làm mờ, không lộ lựa chọn đúng.
 */

const fb = (page: Page) => page.getByTestId('s2e8-feedback-text');
const chip = (page: Page) => page.getByTestId('s2e8-attempts');
const option = (page: Page, text: string) => page.getByTestId('s2e9-options').getByRole('radio', { name: text, exact: true });
const check = (page: Page) => page.getByTestId('s2e9-choice-check');

async function openCompose(page: Page, sb: Sandbox) {
  await signIn(page, sb.teacher, 'TEACHER', 'Cô Hạnh');
  await page.goto('/gv/bai-luyen/moi');
  await expect(page.getByTestId('s2e8-compose')).toBeVisible();
}

async function teacherSetIds(request: APIRequestContext, sb: Sandbox): Promise<string[]> {
  const r = await request.get(`${API}/api/v1/teacher/practice-sets`, { headers: bearer(sb.teacher.token) });
  return ((await r.json()) as { items: Array<{ id: string }> }).items.map((x) => x.id);
}

/** Tick đề SP-01 (FX-14), đổi câu 1 sang "Trắc nghiệm" và nhập các lựa chọn. */
async function composeChoice(page: Page, texts: string[], correct: number | null) {
  await page.getByTestId('s2e8-problem-tick-SP-01').check();
  await page.getByTestId('s2e8-compose-title').fill('Trắc nghiệm góc');
  await page.getByTestId('s2e9-item-type-1').selectOption('choice');
  await expect(page.getByTestId('s2e9-choices-1')).toBeVisible();
  for (let k = 2; k < texts.length; k++) await page.getByTestId('s2e9-choice-add-1').click();
  for (const [k, text] of texts.entries()) await page.getByTestId(`s2e9-choice-text-1-${k + 1}`).fill(text);
  if (correct != null) await page.getByTestId(`s2e9-choice-correct-1-${correct}`).check();
}

test('@TC-S2E9-029 soạn câu trắc nghiệm 4 lựa chọn', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  await composeChoice(page, ANGLES, 2);
  await expect(page.getByTestId('s2e8-item-attempts-1')).toHaveValue('1'); // mặc định 1 lượt
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveText('Đã lưu nháp');
  const [setId] = await teacherSetIds(request, sb);
  const [choice] = await teacherChoices(request, sb.teacher.token, setId!);
  expect(choice!.options.map((o) => o.text)).toEqual(ANGLES);
  expect(choice!.options.filter((o) => o.correct).map((o) => o.text)).toEqual(['45°']); // đúng 1 lựa chọn đúng
  expect(choice!.maxAttempts).toBe(1);
  // mở lại bài nháp: câu vẫn là trắc nghiệm với đủ 4 lựa chọn, đáp án đúng còn tick
  await page.goto(`/gv/bai-luyen/${setId}`);
  await expect(page.getByTestId('s2e9-item-type-1')).toHaveValue('choice');
  await expect(page.getByTestId('s2e9-choice-text-1-4')).toHaveValue('90°');
  await expect(page.getByTestId('s2e9-choice-correct-1-2')).toBeChecked();
  await noHorizontalScroll(page);
});

test('@TC-S2E9-036 không có lựa chọn đúng bị chặn', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  await composeChoice(page, ANGLES, null);
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-error')).toHaveText(MSG.choiceNoCorrect);
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveCount(0);
  expect(await teacherSetIds(request, sb)).toHaveLength(0); // không lưu
  // chọn đáp án đúng rồi thì lưu được
  await page.getByTestId('s2e9-choice-correct-1-2').check();
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveText('Đã lưu nháp');
});

test('@TC-S2E9-038 lựa chọn trùng nhau bị chặn', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  await composeChoice(page, ['45°', ' 45° ', '60°'], 1);
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-error')).toHaveText(MSG.choiceDuplicate);
  expect(await teacherSetIds(request, sb)).toHaveLength(0); // không lưu
});

test('@TC-S2E9-030 chọn đúng thì Đúng', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }, { code: 'LC-01' }], [{ position: 1, texts: ANGLES, correct: '45°' }]);
  await openPlay(page, sb, set.id);
  await expect(page.getByTestId('s2e9-options').getByRole('radio')).toHaveText(ANGLES);
  await expect(page.getByTestId('s2e8-keypad')).toHaveCount(0); // không bàn phím toán ở câu trắc nghiệm
  await expect(page.getByTestId('s2e8-answer')).toHaveCount(0);
  await expect(chip(page)).toHaveText('Còn 1/1 lượt');
  const box = await option(page, '45°').boundingBox();
  expect(box!.height).toBeGreaterThanOrEqual(44); // vùng chạm
  await option(page, '45°').click();
  await check(page).click();
  await expect(fb(page)).toHaveText(MSG.right);
  await expect(page.getByTestId('s2e8-question')).toHaveAttribute('data-state', 'correct');
  await expect(page.getByTestId('s2e8-seg-1')).toHaveAttribute('data-state', 'correct');
  await page.getByTestId('s2e8-next').click(); // sang câu kế được
  await expect(page.getByTestId('s2e8-counter')).toHaveText('Câu 2/2');
  await expect(page.getByTestId('s2e8-keypad')).toBeVisible(); // câu điền thì bàn phím toán hiện lại
  await noHorizontalScroll(page);
});

test('@TC-S2E9-031 chọn sai trừ lượt, hết lượt thì xem lại cùng thầy cô', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°' }]);
  await openPlay(page, sb, set.id);
  await option(page, '60°').click();
  await check(page).click();
  await expect(fb(page)).toHaveText(MSG.wrongLast);
  await expect(chip(page)).toHaveText('Còn 0/1 lượt');
  await expect(option(page, '60°')).toHaveAttribute('data-dimmed', 'true');
  expect(Number(await option(page, '60°').evaluate((el) => getComputedStyle(el).opacity))).toBeLessThan(1); // làm mờ
  await expect(page.getByTestId('s2e8-review')).toHaveText('Xem lại cùng thầy cô');
  await expect(page.getByTestId('s2e8-question')).toHaveAttribute('data-state', 'review');
  // không nói lựa chọn nào đúng: không lựa chọn nào được tô "đúng", phản hồi không nhắc 45
  await expect(page.getByTestId('s2e8-feedback')).not.toContainText('45');
  await expect(page.locator('[data-testid^="s2e9-option-"][data-picked="true"]')).toHaveCount(0);
  await expect(check(page)).toHaveCount(0); // hết lượt: không kiểm tra nữa
  for (const text of ANGLES) await expect(option(page, text)).toBeDisabled();
});

test('@TC-S2E9-032 câu có nhiều lượt: lựa chọn sai bị làm mờ', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°', maxAttempts: 2 }]);
  await openPlay(page, sb, set.id);
  await expect(chip(page)).toHaveText('Còn 2/2 lượt');
  await option(page, '60°').click();
  await check(page).click();
  await expect(fb(page)).toHaveText('Chưa đúng. Còn 1 lượt.');
  await expect(chip(page)).toHaveText('Còn 1/2 lượt');
  await expect(option(page, '60°')).toHaveAttribute('data-dimmed', 'true');
  await expect(option(page, '60°')).toBeDisabled(); // không chọn lại được
  for (const text of ['30°', '45°', '90°']) await expect(option(page, text)).toBeEnabled();
  await expect(check(page)).toBeDisabled(); // phải chọn lại một lựa chọn khác
  // vào lại bài: lựa chọn sai vẫn mờ (trạng thái lưu ở máy chủ)
  await page.reload();
  await expect(page.getByTestId('s2e8-question')).toBeVisible();
  await expect(option(page, '60°')).toBeDisabled();
  await expect(chip(page)).toHaveText('Còn 1/2 lượt');
  await option(page, '45°').click();
  await check(page).click();
  await expect(fb(page)).toHaveText(MSG.right);
});

test('@TC-S2E9-033 chưa chọn thì nút Kiểm tra khóa', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°' }]);
  const sent: string[] = [];
  page.on('request', (r) => {
    if (r.method() === 'POST' && /practice-items/.test(r.url())) sent.push(r.url());
  });
  await openPlay(page, sb, set.id);
  await expect(check(page)).toBeDisabled();
  await check(page).click({ force: true }); // bấm vào nút đang khóa
  await page.waitForTimeout(300);
  expect(sent).toEqual([]); // không yêu cầu nào gửi
  await expect(page.getByTestId('s2e8-feedback')).toHaveCount(0);
  await expect(chip(page)).toHaveText('Còn 1/1 lượt');
  await option(page, '30°').click();
  await expect(check(page)).toBeEnabled();
});

test('@TC-S2E9-042 bài luyện hỗn hợp: kết quả đếm chung hai kiểu câu', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set, choices } = await assignedWithChoices(
    request,
    sb,
    [{ code: 'LC-01' }, { code: 'SP-01' }, { code: 'LA-01' }, { code: 'LB-01' }, { code: 'HCN-01' }],
    [
      { position: 2, texts: ANGLES, correct: '45°' },
      { position: 4, texts: ['1/2', '1/3', '2/3'], correct: '1/2' },
    ],
  );
  const an = student(sb, 'An').token;
  // 3 câu điền đúng, 1 câu trắc nghiệm đúng, 1 câu trắc nghiệm hết lượt
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await attempt(request, an, set.items[2]!.assignmentId, '4√3');
  await attempt(request, an, set.items[4]!.assignmentId, '10');
  const right = await ok<ChoiceAttempt>(await choiceAttempt(request, an, set.items[1]!.assignmentId, optionId(choices[0]!, '45°'), randomUUID()));
  expect(right.outcome).toBe('correct');
  const wrong = await ok<ChoiceAttempt>(await choiceAttempt(request, an, set.items[3]!.assignmentId, optionId(choices[1]!, '2/3'), randomUUID()));
  expect(wrong.state).toBe('review');
  await signIn(page, student(sb, 'An'), 'STUDENT', 'An');
  await page.goto(`/hs/bai-luyen/${set.id}/ket-qua`);
  await expect(page.getByTestId('s2e8-result-summary')).toHaveText('4 câu đúng · 1 câu cần xem lại');
  await expect(page.getByTestId('s2e8-result-row-2')).toHaveAttribute('data-state', 'correct');
  await expect(page.getByTestId('s2e8-result-row-4')).toHaveAttribute('data-state', 'review');
  await expect(page.getByTestId('s2e8-result')).not.toContainText(/điểm|%|\/\s*10/i); // không điểm
});

test('@TC-S2E9-043 nút Em nghĩ đáp số này đúng không hiện ở câu trắc nghiệm', async ({ page, request }) => {
  const sb = await sandbox(request);
  const { set, choices } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°' }]);
  const an = student(sb, 'An').token;
  const item = set.items[0]!;
  await choiceAttempt(request, an, item.assignmentId, optionId(choices[0]!, '90°'), randomUUID()); // hết lượt
  await openPlay(page, sb, set.id);
  await expect(page.getByTestId('s2e8-question')).toHaveAttribute('data-state', 'review');
  await expect(option(page, '90°')).toHaveAttribute('data-dimmed', 'true');
  await expect(page.getByTestId('s2e8-report-open')).toHaveCount(0);
  await expect(page.getByText('Em nghĩ đáp số này đúng')).toHaveCount(0);
  const r = await report(request, an, item.assignmentId, 'em nghĩ 90° đúng');
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
});
