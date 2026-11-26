import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { BL1_ANSWERS, HINT_Q2, answerBox, assigned, attempt, noHorizontalScroll, openPlay, play, sandbox, signIn, student, submit, tap } from './s2e8.ts';

/**
 * S2-E8 · US-S2E8-03 làm bài luyện (C2-06): lượt thử, gợi ý mức 1, "xem lại cùng thầy cô", giữ trạng thái, mất mạng.
 * Tên tệp *.p0m.ui.spec.ts: chạy cả trên điện thoại. Mỗi ca dựng lớp và bài BL-1 riêng.
 */

const counter = (page: Page) => page.getByTestId('s2e8-counter');
const chip = (page: Page) => page.getByTestId('s2e8-attempts');
const fb = (page: Page) => page.getByTestId('s2e8-feedback-text');
const seg = (page: Page, n: number) => page.getByTestId(`s2e8-seg-${n}`);
const next = (page: Page) => page.getByTestId('s2e8-next');

test('@TC-S2E8-056 mở bài hiện câu 1 với đủ thành phần', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openPlay(page, sb, set.id);
  await expect(page.getByTestId('s2e8-title')).toHaveText('Góc trong hình chóp');
  await expect(counter(page)).toHaveText('Câu 1/5');
  await expect(chip(page)).toHaveText('Còn 3/3 lượt');
  await expect(page.locator('[data-testid="s2e8-progress"] i')).toHaveCount(5);
  await expect(seg(page, 1)).toHaveAttribute('data-state', 'current');
  for (const n of [2, 3, 4, 5]) await expect(seg(page, n)).toHaveAttribute('data-state', 'todo');
  await expect(page.getByTestId('s2e8-figure')).toBeVisible();
  await expect(answerBox(page)).toHaveText('');
  await expect(page.getByTestId('s2e8-keypad')).toBeVisible();
  await expect(page.getByTestId('s2e8-report-open')).toHaveCount(0);
  // màu đoạn tiến độ: vàng đang làm khác xám chưa làm
  const colors = await page.evaluate(() => [1, 2].map((n) => getComputedStyle(document.querySelector(`[data-testid="s2e8-seg-${n}"]`)!).backgroundColor));
  expect(colors[0]).not.toBe(colors[1]);
});

test('@TC-S2E8-057 đáp số đúng thì xác nhận và sang câu kế', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await attempt(request, student(sb, 'An').token, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await expect(counter(page)).toHaveText('Câu 2/5');
  await submit(page, '45');
  await expect(fb(page)).toHaveText('Đúng rồi!');
  await expect(seg(page, 2)).toHaveAttribute('data-state', 'correct');
  await next(page).click();
  await expect(counter(page)).toHaveText('Câu 3/5');
  await expect(chip(page)).toHaveText('Còn 3/3 lượt');
});

test('@TC-S2E8-058 đáp số sai hiện gợi ý mức 1 và trừ lượt', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await attempt(request, student(sb, 'An').token, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await submit(page, '60°');
  await expect(fb(page)).toHaveText('Chưa đúng. Còn 2 lượt.');
  await expect(page.getByTestId('s2e8-hint')).toContainText(HINT_Q2);
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  await expect(answerBox(page)).toHaveText('60°');
  await expect(answerBox(page)).toHaveAttribute('aria-invalid', 'true');
  const border = await answerBox(page).evaluate((el) => {
    const probe = document.createElement('i');
    probe.style.color = 'var(--red)';
    document.body.append(probe);
    const red = getComputedStyle(probe).color;
    probe.remove();
    return { border: getComputedStyle(el).borderTopColor, red };
  });
  expect(border.border, 'ô đáp số viền đỏ').toBe(border.red);
  for (const id of ['s2e8-feedback', 's2e8-hint']) await expect(page.getByTestId(id)).not.toContainText(/45|lớn hơn|nhỏ hơn/);
});

test('@TC-S2E8-059 sai cú pháp báo chỗ sai và không trừ lượt', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await attempt(request, an, set.items[1]!.assignmentId, '60');
  await openPlay(page, sb, set.id);
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  await submit(page, '√');
  await expect(fb(page)).toHaveText('Đáp số chưa viết đúng: thiếu số sau dấu căn (ký tự 1). Lượt thử chưa bị trừ.');
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  await expect(page.getByTestId('s2e8-hint')).toHaveCount(1); // gợi ý cũ vẫn đó, không có gợi ý mới
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(2);
});

test('@TC-S2E8-060 hết lượt đánh dấu xem lại cùng thầy cô và khóa nhập', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await attempt(request, an, set.items[1]!.assignmentId, '60');
  await attempt(request, an, set.items[1]!.assignmentId, '61');
  await openPlay(page, sb, set.id);
  await expect(chip(page)).toHaveText('Còn 1/3 lượt');
  await submit(page, '70');
  await expect(fb(page)).toHaveText('Chưa đúng. Em xem lại cùng thầy cô nhé.');
  await expect(chip(page)).toHaveText('Còn 0/3 lượt');
  await expect(page.getByTestId('s2e8-review')).toHaveText('Xem lại cùng thầy cô');
  await expect(answerBox(page)).toHaveAttribute('aria-disabled', 'true');
  await expect(page.getByTestId('s2e8-check')).toHaveCount(0); // bàn phím và "Kiểm tra" khóa
  await expect(next(page)).toBeEnabled();
  for (const id of ['s2e8-feedback', 's2e8-hint', 's2e8-review']) for (const el of await page.getByTestId(id).all()) await expect(el).not.toContainText(/45|Đáp số:/);
});

test('@TC-S2E8-061 hết lượt vẫn xem được gợi ý mức 1', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  for (const a of ['60', '61', '62']) await attempt(request, an, set.items[1]!.assignmentId, a);
  await openPlay(page, sb, set.id);
  await page.getByTestId('s2e8-prev').click(); // câu 3 → câu 2 đã hết lượt
  await expect(counter(page)).toHaveText('Câu 2/5');
  await expect(page.getByTestId('s2e8-review')).toBeVisible();
  await page.getByTestId('s2e8-hint-open').click();
  await expect(page.getByTestId('s2e8-hint')).toContainText(HINT_Q2);
  for (const id of ['s2e8-feedback', 's2e8-hint', 's2e8-review']) for (const el of await page.getByTestId(id).all()) await expect(el).not.toContainText(/45|Đáp số:/);
});

test('@TC-S2E8-062 chip lượt đổi 3, 2, 1, 0 theo từng lần Sai', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await attempt(request, student(sb, 'An').token, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await expect(chip(page)).toHaveText('Còn 3/3 lượt');
  await submit(page, '60');
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  await expect(page.getByTestId('s2e8-hint')).toBeVisible();
  await submit(page, '61');
  await expect(chip(page)).toHaveText('Còn 1/3 lượt');
  await expect(page.getByTestId('s2e8-hint')).toBeVisible();
  await submit(page, '62');
  await expect(chip(page)).toHaveText('Còn 0/3 lượt');
  await expect(page.getByTestId('s2e8-review')).toBeVisible();
  await expect(seg(page, 2)).toHaveAttribute('data-state', 'review');
});

test('@TC-S2E8-063 thoát giữa chừng rồi vào lại giữ trạng thái', async ({ page, request, context }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openPlay(page, sb, set.id);
  await submit(page, BL1_ANSWERS[0]!);
  await next(page).click();
  await submit(page, '60');
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  await page.close(); // đóng rồi mở lại, đăng nhập lại, mở BL-1
  const again = await context.newPage();
  await openPlay(again, sb, set.id);
  await expect(counter(again)).toHaveText('Câu 2/5');
  await expect(chip(again)).toHaveText('Còn 2/3 lượt');
  await expect(seg(again, 1)).toHaveAttribute('data-state', 'correct');
  await expect(again.getByTestId('s2e8-hint')).toContainText(HINT_Q2);
});

test('@TC-S2E8-064 mất mạng khi gửi không trừ lượt và giữ đáp số', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await tap(page, '60');
  const keys: string[] = [];
  await page.route('**/api/v1/student/practice-items/*/attempts', (r) => {
    keys.push((r.request().postDataJSON() as { requestKey: string }).requestKey);
    return r.abort('internetdisconnected');
  });
  await page.getByTestId('s2e8-check').click();
  await expect(fb(page)).toHaveText('Chưa gửi được, đáp số của em vẫn còn');
  await expect(page.getByTestId('s2e8-retry')).toBeVisible();
  await expect(answerBox(page)).toHaveText('60');
  await expect(chip(page)).toHaveText('Còn 3/3 lượt');
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(3);
  // có mạng lại: "Thử lại" gửi CÙNG mã chống lặp, trừ đúng một lượt
  await page.unroute('**/api/v1/student/practice-items/*/attempts');
  const sent = page.waitForRequest('**/api/v1/student/practice-items/*/attempts');
  await page.getByTestId('s2e8-retry').click();
  expect(((await sent).postDataJSON() as { requestKey: string }).requestKey).toBe(keys[0]);
  await expect(fb(page)).toHaveText('Chưa đúng. Còn 2 lượt.');
  await expect(chip(page)).toHaveText('Còn 2/3 lượt');
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(2);
});

test('@TC-S2E8-065 đang tải hiện khung giữ chỗ', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  let release: () => void = () => {};
  const gate = new Promise<void>((r) => (release = r));
  await page.route(`**/api/v1/student/practice-sets/${set.id}`, async (r) => {
    await gate; // mạng chậm
    await r.continue();
  });
  await signIn(page, student(sb, 'An'), 'STUDENT', 'An');
  await page.goto(`/hs/bai-luyen/${set.id}`);
  await expect(page.getByTestId('s2e8-loading')).toBeVisible();
  await expect(page.getByTestId('s2e8-keypad-placeholder')).toBeVisible();
  await expect(page.getByTestId('s2e8-answer-placeholder')).toHaveAttribute('aria-disabled', 'true');
  await expect(page.getByTestId('s2e8-answer')).toHaveCount(0); // ô đáp số chưa nhận chữ
  await expect(page.getByTestId('s2e8-figure')).toHaveCount(0); // không hiện hình rỗng
  release();
  await expect(page.getByTestId('s2e8-question')).toBeVisible();
  await expect(page.getByTestId('s2e8-loading')).toHaveCount(0);
});

test('@TC-S2E8-068 câu đã đúng không làm lại', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await page.getByTestId('s2e8-prev').click();
  await expect(counter(page)).toHaveText('Câu 1/5');
  await expect(page.getByTestId('s2e8-question')).toHaveAttribute('data-state', 'correct');
  await expect(answerBox(page)).toHaveAttribute('aria-disabled', 'true');
  await expect(page.getByTestId('s2e8-keypad')).toHaveCount(0);
  await expect(page.getByTestId('s2e8-feedback')).toContainText('Câu này em đã làm đúng');
  await answerBox(page).click({ force: true });
  await page.keyboard.type('7');
  await expect(answerBox(page)).toHaveText('');
  expect((await play(request, an, set.id)).items[0]!.state).toBe('correct');
});

test('@TC-S2E8-070 làm hết 5 câu thì sang kết quả', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  for (const i of [0, 1, 2, 3]) await attempt(request, an, set.items[i]!.assignmentId, BL1_ANSWERS[i]!);
  await openPlay(page, sb, set.id);
  await expect(counter(page)).toHaveText('Câu 5/5');
  await submit(page, BL1_ANSWERS[4]!);
  await expect(page.getByTestId('s2e8-finish')).toHaveText('Xem kết quả');
  await page.getByTestId('s2e8-finish').click();
  await expect(page).toHaveURL(new RegExp(`/hs/bai-luyen/${set.id}/ket-qua$`));
  await expect(page.getByTestId('s2e8-result-summary')).toHaveText('5 câu đúng · 0 câu cần xem lại');
});

test('@TC-S2E8-071 bài 1 câu: tiến độ một đoạn, câu cuối', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { title: 'Bài một câu', items: [{ code: 'LC-01' }] });
  await openPlay(page, sb, set.id);
  await expect(counter(page)).toHaveText('Câu 1/1');
  await expect(page.locator('[data-testid="s2e8-progress"] i')).toHaveCount(1);
  await expect(page.getByTestId('s2e8-next')).toHaveCount(0);
  await submit(page, '6');
  await expect(page.getByTestId('s2e8-finish')).toHaveText('Xem kết quả');
});

test('@TC-S2E8-072 bài 20 câu thanh tiến độ 20 đoạn', async ({ page, request }) => {
  const sb = await sandbox(request);
  const items = [{ code: 'LC-01' }, { code: 'LA-01' }, { code: 'LB-01' }, { code: 'LF-01' }, ...Array.from({ length: 16 }, (_, i) => ({ code: `HCN-${String(i + 1).padStart(2, '0')}` }))];
  const set = await assigned(request, sb, { title: 'Bài hai mươi câu', items });
  await openPlay(page, sb, set.id);
  await expect(counter(page)).toHaveText('Câu 1/20');
  await expect(page.locator('[data-testid="s2e8-progress"] i')).toHaveCount(20);
  await noHorizontalScroll(page);
  const bar = (await page.getByTestId('s2e8-progress').boundingBox())!;
  const last = (await seg(page, 20).boundingBox())!;
  expect(last.x + last.width, 'đoạn 20 nằm trong thanh').toBeLessThanOrEqual(bar.x + bar.width + 1);
  expect(bar.x + bar.width).toBeLessThanOrEqual(page.viewportSize()!.width);
});

test('@TC-S2E8-112 luồng C đầy đủ: đúng, sai cú pháp, sai, hết lượt, khác dạng viết, xem kết quả', async ({ page, request }, testInfo) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openPlay(page, sb, set.id);
  // câu 1: đúng
  await submit(page, '6');
  await expect(fb(page)).toHaveText('Đúng rồi!');
  await next(page).click();
  // câu 2: sai cú pháp (còn 3/3), rồi sai ba lần → hết lượt
  await submit(page, '√');
  await expect(chip(page)).toHaveText('Còn 3/3 lượt');
  for (const [a, left] of [['60', 2], ['61', 1], ['62', 0]] as const) {
    await submit(page, a);
    await expect(chip(page)).toHaveText(`Còn ${left}/3 lượt`);
  }
  await expect(page.getByTestId('s2e8-review')).toBeVisible();
  await testInfo.attach(`s2e8-het-luot-${testInfo.project.name}.png`, { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
  await noHorizontalScroll(page);
  await next(page).click();
  // câu 3: đúng nhưng khác dạng viết → kèm dạng chuẩn
  await submit(page, '√48');
  await expect(fb(page)).toHaveText('Đúng rồi! Dạng chuẩn: 4√3');
  await next(page).click();
  // câu 4, 5: đúng
  await submit(page, '1/2');
  await next(page).click();
  await submit(page, '10');
  await page.getByTestId('s2e8-finish').click();
  await expect(page.getByTestId('s2e8-result-summary')).toHaveText('4 câu đúng · 1 câu cần xem lại');
  await expect(page.getByTestId('s2e8-result')).not.toContainText(/điểm|%|hạng/i);
  await expect(page.getByTestId('s2e8-result-row-2').getByTestId('s2e8-result-solution')).toHaveAttribute('data-solution', 'locked');
  await expect(page.getByTestId('s2e8-result-row-2')).toContainText('Lời giải mở lúc');
  await testInfo.attach(`s2e8-ket-qua-${testInfo.project.name}.png`, { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
});
