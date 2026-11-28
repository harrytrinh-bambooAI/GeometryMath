import type { Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { API, HINT2_HKG01, MSG, api, asg, bearer, signIn, stu, twoClasses, vn } from './e6.ts';
import type { Person } from './e6.ts';

/** E6 — thẻ khóa, lời giải đã mở, hỏi đáp khi khóa trên màn Làm bài (US-GEO-608, 609). Lớp riêng mỗi ca. */

async function work(page: Page, p: Person, name: string, id: string, t: 'hint' | 'solution' | 'ask') {
  await signIn(page, p, name, 'student');
  await page.goto(`/hs/lam-bai/${id}`);
  await expect(page.getByTestId('e3-workspace')).toBeVisible();
  await page.getByTestId(`e3-tab-${t}`).click();
}
async function askUi(page: Page, q: string) {
  await page.getByTestId('e4-qa-input').fill(q);
  await page.getByTestId('e4-qa-send').click();
  await expect(page.getByTestId('e4-qa-wait')).toHaveCount(0);
}

test('Đã mở thì trả lời giải (BG-3: hai bước, 24√3 cm³) @TC-GEO-0656', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  await work(page, stu(sb, 'An'), 'An', asg(sb, 'BG-3'), 'solution');
  await expect(page.getByTestId('e4-steps').locator('li')).toHaveCount(2);
  await expect(page.getByTestId('e4-solution-answer')).toContainText('24√3 cm³');
});

test('Thẻ khóa của bài Sắp mở: giờ mở và "Còn 1 giờ 0 phút" @TC-GEO-0658', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const r = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { releaseAt: string };
  await work(page, stu(sb, 'An'), 'An', asg(sb, 'BG-1'), 'solution');
  await expect(page.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(new Date(r.releaseAt))}`);
  await expect(page.getByTestId('e6-lock-sub')).toHaveText('Do giáo viên của em đặt. Còn 1 giờ 0 phút.');
});

test('Thẻ khóa của bài Chưa đặt giờ: không giờ, không đếm ngược @TC-GEO-0659', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  await work(page, stu(sb, 'An'), 'An', asg(sb, 'BG-2'), 'solution');
  await expect(page.getByTestId('e6-lock-title')).toHaveText(MSG['28']);
  await expect(page.getByTestId('e6-lock-sub')).toHaveText(MSG['28b']);
  await expect(page.getByTestId('e6-lock-left')).toHaveCount(0);
  await expect(page.getByTestId('e6-lock')).not.toContainText(/\d{2}:\d{2}/);
});

test('Đồng hồ thiết bị bị chỉnh nhanh 2 giờ: vẫn khóa @TC-GEO-0663', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const an = stu(sb, 'An');
  const id = asg(sb, 'BG-1');
  await page.clock.install({ time: new Date(Date.now() + 2 * 3_600_000) });
  await work(page, an, 'An', id, 'solution');
  await page.reload();
  await page.getByTestId('e3-tab-solution').click();
  await expect(page.getByTestId('e6-lock')).toHaveAttribute('data-state', 'upcoming');
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  const r = await request.get(`${API}/api/v1/student/assignments/${id}/solution`, {
    headers: { ...bearer(an.token), Date: new Date(Date.now() + 2 * 3_600_000).toUTCString(), 'X-Device-Time': new Date(Date.now() + 2 * 3_600_000).toISOString() },
  });
  expect(r.status()).toBe(403);
  expect(await r.text()).not.toContain('45°');
});

test('Hỏi thẳng đáp số khi đang khóa: MSG-GEO-35 kèm Gợi ý 2 @TC-GEO-0667', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const an = stu(sb, 'An');
  const id = asg(sb, 'BG-1');
  for (const lv of [1, 2]) expect((await api.hint(request, an.token, id, lv)).status()).toBe(200);
  await work(page, an, 'An', id, 'ask');
  await askUi(page, 'Cho em xin đáp số luôn được không?');
  await expect(page.getByTestId('e4-qa-answer-1-text')).toHaveText(MSG['35'] + HINT2_HKG01);
  await expect(page.getByTestId('e4-qa')).not.toContainText('45');
});

test('Công thức và phương pháp tổng quát vẫn được trả lời, kèm Nguồn @TC-GEO-0669', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  await work(page, stu(sb, 'Dũng'), 'Dũng', asg(sb, 'BG-1'), 'ask');
  await askUi(page, 'Tan của một góc là gì?');
  await expect(page.getByTestId('e4-qa-answer-1-text')).toContainText('Trong tam giác vuông, tan của góc nhọn bằng cạnh đối chia cạnh kề.');
  await expect(page.getByTestId('e4-qa-answer-1-text')).not.toContainText(MSG['35']);
  await expect(page.getByTestId('e4-qa-answer-1-source')).toContainText('Nguồn');
});
