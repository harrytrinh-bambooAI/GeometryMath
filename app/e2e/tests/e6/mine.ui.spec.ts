import type { APIRequestContext, Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { API, HINT1_HKG01, HINT2_HKG01, MSG, api, asg, bearer, sandbox, signIn, stu, twoClasses, vn } from './e6.ts';
import type { Person, Sb } from './e6.ts';

/** E6 — "Bài của em": giờ mở và tiến độ (US-GEO-606; khung Design/mvp-04). Lớp riêng mỗi ca; tiến độ dựng qua API thật. */

async function openBai(rq: APIRequestContext, p: Person, id: string) {
  expect((await rq.post(`${API}/api/v1/student/assignments/${id}/open`, { headers: bearer(p.token), data: { requestId: crypto.randomUUID() } })).status()).toBeLessThan(300);
}
/** An: BG-1 2/3 (Sắp mở), BG-2 0/3 (Chưa đặt giờ, chưa mở), BG-3 3/3 (Đã mở); Chi: đã mở BG-1, chưa xem gợi ý. */
async function fixture(rq: APIRequestContext): Promise<Sb> {
  const sb = await twoClasses(rq, [
    { key: 'BG-1', code: 'HKG-01' },
    { key: 'BG-2', code: 'HKG-02', release: 'unset' },
    { key: 'BG-3', code: 'HKG-03', release: 'open' },
  ]);
  const an = stu(sb, 'An');
  await openBai(rq, an, asg(sb, 'BG-1'));
  for (const lv of [1, 2]) expect((await api.hint(rq, an.token, asg(sb, 'BG-1'), lv)).status()).toBe(200);
  await openBai(rq, an, asg(sb, 'BG-3'));
  expect((await api.openSolution(rq, an.token, asg(sb, 'BG-3'))).status()).toBe(200);
  await openBai(rq, stu(sb, 'Chi'), asg(sb, 'BG-1'));
  return sb;
}
async function mine(page: Page, p: Person, name: string) {
  await signIn(page, p, name, 'student', '11A1');
  await page.goto('/hs/bai-cua-em');
  await expect(page.getByTestId('e1-greeting')).toHaveText(`Chào ${name}`);
}
const card = (page: Page, code: string) => page.getByTestId(`e6-card-${code}`);

test('Bài Sắp mở, đang làm dở @TC-GEO-0639', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  const v = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { releaseAt: string };
  await expect(page.getByTestId('e1-assignment-HKG-01-release')).toHaveText(`Lời giải mở lúc ${vn(new Date(v.releaseAt))}`);
  await expect(page.getByTestId('e6-card-HKG-01-bar')).toHaveAttribute('data-k', '2');
  await expect(page.getByTestId('e6-card-HKG-01-bar').locator('i.on')).toHaveCount(2);
  await expect(page.getByTestId('e1-assignment-HKG-01-progress')).toContainText('Đã xem gợi ý 2');
  await expect(page.getByTestId('e6-card-HKG-01-action')).toHaveText('Làm tiếp');
});

test('Bài Chưa đặt giờ, chưa bắt đầu @TC-GEO-0640', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  await expect(page.getByTestId('e1-assignment-HKG-02-release')).toHaveText('Lời giải đang khóa');
  await expect(page.getByTestId('e6-card-HKG-02-bar').locator('i.on')).toHaveCount(0);
  await expect(page.getByTestId('e1-assignment-HKG-02-progress')).toContainText('Chưa bắt đầu');
  await expect(page.getByTestId('e6-card-HKG-02-action')).toHaveText('Bắt đầu');
});

test('Bài Đã mở, đã xem lời giải @TC-GEO-0641', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  await expect(page.getByTestId('e1-assignment-HKG-03-release')).toHaveText('Lời giải đã mở');
  await expect(page.getByTestId('e6-card-HKG-03-bar').locator('i.on')).toHaveCount(3);
  await expect(page.getByTestId('e1-assignment-HKG-03-progress')).toContainText('Đã xem lời giải');
  await expect(page.getByTestId('e6-card-HKG-03-action')).toHaveText('Xem lại');
});

test('Đã mở bài nhưng chưa dùng gợi ý @TC-GEO-0642', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'Chi'), 'Chi');
  await expect(page.getByTestId('e1-assignment-HKG-01-progress')).toContainText('Đã mở bài');
  await expect(page.getByTestId('e6-card-HKG-01-bar').locator('i.on')).toHaveCount(0);
  await expect(page.getByTestId('e6-card-HKG-01-action')).toHaveText('Làm tiếp');
});

test('Bấm vào một bài: màn Làm bài với hai gợi ý đã xem @TC-GEO-0643', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  await page.getByTestId('e6-card-HKG-01-action').click();
  await expect(page.getByTestId('e3-workspace')).toBeVisible();
  await expect(page.locator('.e3-head h2')).toContainText('HKG-01');
  await expect(page.locator('.e3-head h2')).toContainText('Bài được giao · lớp 11A1');
  await page.getByTestId('e3-tab-hint').click();
  await expect(page.getByTestId('e4-hint-1')).toContainText(HINT1_HKG01);
  await expect(page.getByTestId('e4-hint-2')).toContainText(HINT2_HKG01);
});

test('Lớp chưa có bài: MSG-GEO-50, vẫn có "Tự khám phá" @TC-GEO-0645', async ({ page, request }) => {
  const sb = await sandbox(request, [{ name: '10A', students: ['Yến'] }]);
  await mine(page, stu(sb, 'Yến'), 'Yến');
  await expect(page.getByTestId('e6-empty')).toHaveText(MSG['50']);
  await expect(page.getByTestId('e4-explore-block')).toBeVisible();
});

test('Không có thẻ chuỗi ngày và phần trăm theo dạng @TC-GEO-0647', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  await expect(page.getByTestId('e6-card-HKG-01')).toBeVisible();
  const body = page.getByTestId('e1-my-assignments');
  await expect(body).not.toContainText('ngày liên tiếp');
  await expect(body).not.toContainText('Em đang học tới đâu');
  await expect(body).not.toContainText('%');
  await expect(body).not.toContainText(/xếp hạng/i);
});

test('Dòng "Hôm nay em có n bài cần làm" @TC-GEO-0869', async ({ page, request }) => {
  const sb = await fixture(request);
  await mine(page, stu(sb, 'An'), 'An');
  await expect(page.getByTestId('e6-todo')).toHaveText('Hôm nay em có 2 bài cần làm');
});

test('Không còn bài cần làm @TC-GEO-0877', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const an = stu(sb, 'An');
  expect((await api.openSolution(request, an.token, asg(sb, 'BG-3'))).status()).toBe(200);
  await mine(page, an, 'An');
  await expect(page.getByTestId('e6-todo')).toHaveText('Hôm nay em không có bài nào cần làm.');
  await expect(page.getByTestId('e1-my-assignments')).not.toContainText('có 0 bài');
});
