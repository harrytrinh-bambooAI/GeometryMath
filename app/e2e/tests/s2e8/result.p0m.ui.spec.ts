import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BL1_ANSWERS, assigned, attempt, bearer, exhaust, noHorizontalScroll, sandbox, signIn, student, walk, type Sandbox, type SetDetail } from './s2e8.ts';

/**
 * S2-E8 · US-S2E8-04 kết quả bài luyện (C2-07): chỉ "x câu đúng · y câu cần xem lại", không điểm, phần trăm hay xếp
 * hạng; lời giải khóa tới giờ mở. Tên tệp *.p0m.ui.spec.ts: chạy cả trên điện thoại.
 */

/** plan theo thứ tự câu: ok = làm đúng, out = hết lượt, skip = chưa làm. */
async function doItems(request: APIRequestContext, sb: Sandbox, set: SetDetail, plan: Array<'ok' | 'out' | 'skip'>) {
  const an = student(sb, 'An').token;
  for (const [i, p] of plan.entries()) {
    if (p === 'ok') await attempt(request, an, set.items[i]!.assignmentId, BL1_ANSWERS[i]!);
    else if (p === 'out') await exhaust(request, an, set.items[i]!);
  }
}

async function openResult(page: Page, sb: Sandbox, setId: string) {
  await signIn(page, student(sb, 'An'), 'STUDENT', 'An');
  await page.goto(`/hs/bai-luyen/${setId}/ket-qua`);
  await expect(page.getByTestId('s2e8-result')).toBeVisible();
}

const summary = (page: Page) => page.getByTestId('s2e8-result-summary');

test('@TC-S2E8-075 làm hết thì hiện số câu đúng và câu cần xem lại', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await doItems(request, sb, set, ['ok', 'out', 'ok', 'ok', 'out']);
  await openResult(page, sb, set.id);
  await expect(summary(page)).toHaveText('3 câu đúng · 2 câu cần xem lại');
  await expect(page.locator('[data-testid^="s2e8-result-row-"]')).toHaveCount(5);
  await expect(page.getByTestId('s2e8-result-row-1')).toHaveAttribute('data-state', 'correct');
  await expect(page.getByTestId('s2e8-result-row-2')).toHaveAttribute('data-state', 'review');
  await expect(page.getByTestId('s2e8-result-pending')).toHaveCount(0);
  await noHorizontalScroll(page);
});

test('@TC-S2E8-077 chưa làm xong thì báo còn bao nhiêu câu', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await doItems(request, sb, set, ['ok', 'out', 'ok', 'skip', 'skip']);
  await openResult(page, sb, set.id);
  await expect(page.getByTestId('s2e8-result-pending')).toHaveText('Chưa làm xong: còn 2 câu');
  await expect(summary(page)).toHaveText('2 câu đúng · 1 câu cần xem lại');
});

test('@TC-S2E8-079 tới giờ mở thì xem được lời giải', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await doItems(request, sb, set, ['ok', 'out', 'skip', 'skip', 'skip']);
  // giờ máy chủ ≥ release_at của câu 2 (móc D-GEO-169: giờ mở = bây giờ)
  const hook = await request.post(`${API}/test-hooks/e6/release`, { data: { assignmentId: set.items[1]!.assignmentId, seconds: 0 } });
  expect(hook.status()).toBe(200);
  await openResult(page, sb, set.id);
  const row = page.getByTestId('s2e8-result-row-2');
  await expect(row.getByTestId('s2e8-result-solution')).toHaveText('Lời giải đã mở');
  await row.getByTestId('s2e8-solution-open-2').click();
  const sol = page.getByTestId('s2e8-solution-2');
  await expect(sol).toBeVisible();
  await expect(sol.getByTestId('s2e8-solution-step')).toHaveCount(2);
  await expect(sol).toContainText('Đáp số: 45°');
  await expect(sol.getByTestId('s2e8-label-machine')).toHaveText(/Đã kiểm chứng|Cần xem lại/);
  await expect(sol.getByTestId('s2e8-label-human')).toHaveText(/AI soạn|Giáo viên đã duyệt/);
  // câu còn khóa (câu 1) không có nút xem lời giải
  await expect(page.getByTestId('s2e8-solution-open-1')).toHaveCount(0);
});

test('@TC-S2E8-081 chưa đặt giờ mở thì ghi Thầy cô chưa đặt giờ mở', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { releaseAt: null });
  await doItems(request, sb, set, ['ok', 'out', 'skip', 'skip', 'skip']);
  await openResult(page, sb, set.id);
  const chip = page.getByTestId('s2e8-result-row-2').getByTestId('s2e8-result-solution');
  await expect(chip).toHaveText('Thầy cô chưa đặt giờ mở');
  await expect(chip).toHaveAttribute('data-solution', 'unset');
  await expect(page.getByTestId('s2e8-solution-open-2')).toHaveCount(0);
  const sol = await request.get(`${API}/api/v1/student/assignments/${set.items[1]!.assignmentId}/solution`, { headers: bearer(student(sb, 'An').token) });
  expect(sol.status(), 'lời giải khóa tới khi có giờ').toBe(403);
});

test('@TC-S2E8-083 chưa làm câu nào', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openResult(page, sb, set.id);
  await expect(page.getByTestId('s2e8-result-pending')).toHaveText('Chưa làm xong: còn 5 câu');
  await expect(summary(page)).toHaveText('0 câu đúng · 0 câu cần xem lại');
  await expect(page.getByTestId('s2e8-result-note')).toHaveText('Em chưa làm câu nào');
});

test('@TC-S2E8-084 làm đúng cả 5 câu', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await doItems(request, sb, set, ['ok', 'ok', 'ok', 'ok', 'ok']);
  await openResult(page, sb, set.id);
  await expect(summary(page)).toHaveText('5 câu đúng · 0 câu cần xem lại');
  await expect(page.getByTestId('s2e8-result')).not.toContainText(/điểm|hạng|%|giỏi nhất|đứng đầu/i);
  await expect(page.getByTestId('s2e8-result-note')).toHaveCount(0);
});

test('@TC-S2E8-085 không câu nào đúng', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await doItems(request, sb, set, ['out', 'out', 'out', 'out', 'out']);
  await openResult(page, sb, set.id);
  await expect(summary(page)).toHaveText('0 câu đúng · 5 câu cần xem lại');
  await expect(page.getByTestId('s2e8-result-note')).toHaveText('Em hỏi thầy cô nhé');
  await expect(page.getByTestId('s2e8-result')).not.toContainText(/kém|tệ|thất bại|trượt|điểm|hạng/i);
  // trước giờ mở, trình duyệt không giữ lời giải hay đáp số của bài (US-S2E8-04-FC3)
  const stored = await page.evaluate(() => JSON.stringify({ ...localStorage }) + JSON.stringify({ ...sessionStorage }));
  expect(stored).not.toMatch(/finalAnswer|"steps"|Đáp số:|4√3|45°/);
});
