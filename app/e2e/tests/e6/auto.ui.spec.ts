import type { Page, Response } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { LEAK_HKG01, LEAK_HKG04, MSG, api, asg, expectNoLeak, signIn, stu, twoClasses } from './e6.ts';
import type { Person, Sb } from './e6.ts';

/**
 * E6 — lời giải tự hiện khi tới giờ mở trong lúc học sinh đang xem (US-GEO-612). Giờ mở R đặt vài giây sau giờ máy chủ
 * (lớp riêng của móc e6/sandbox, release = số giây; D-GEO-169) thay cho R = T0 + 60 giây để ca chạy nhanh; mọi mốc giờ
 * so với R do máy chủ trả. Kỳ vọng "không muộn hơn R + 10 giây" giữ nguyên.
 */
test.describe.configure({ timeout: 90_000 });

async function bg4(request: Parameters<typeof twoClasses>[0], seconds: number) {
  const sb = await twoClasses(request, [], [{ key: 'BG-4', code: 'HKG-04', release: String(seconds) }]);
  const v = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-4'))).json()) as { releaseAt: string; serverNow: string };
  return { sb, R: new Date(v.releaseAt).getTime(), skew: Date.now() - new Date(v.serverNow).getTime() };
}
async function solutionTab(page: Page, p: Person, name: string, id: string) {
  await signIn(page, p, name, 'student');
  await page.goto(`/hs/lam-bai/${id}`);
  await page.getByTestId('e3-tab-solution').click();
}
function lockResponses(page: Page): Response[] {
  const out: Response[] = [];
  page.on('response', (r) => {
    if (/\/assignments\/[^/]+\/lock$/.test(new URL(r.url()).pathname)) out.push(r);
  });
  return out;
}

test('Tới giờ thì lời giải tự mở, không tải lại @TC-GEO-0687', async ({ page, request }) => {
  const { sb, R, skew } = await bg4(request, 12);
  await solutionTab(page, stu(sb, 'Khánh'), 'Khánh', asg(sb, 'BG-4'));
  await expect(page.getByTestId('e6-lock')).toHaveAttribute('data-state', 'upcoming');
  let navigations = 0;
  page.on('framenavigated', () => navigations++);
  await expect(page.getByTestId('e6-toast-opened')).toHaveText(MSG['55'], { timeout: 40_000 });
  const at = Date.now() - skew;
  expect(at).toBeLessThanOrEqual(R + 10_000);
  await expect(page.getByTestId('e6-lock')).toHaveCount(0);
  await expect(page.getByTestId('e4-solution-answer')).toContainText('15π');
  expect(navigations).toBe(0);
});

test('Đếm ngược trên thẻ khóa giảm dần, không bao giờ âm @TC-GEO-0688', async ({ page, request }) => {
  const { sb } = await bg4(request, 9);
  await solutionTab(page, stu(sb, 'Khánh'), 'Khánh', asg(sb, 'BG-4'));
  const seen: number[] = [];
  for (let i = 0; i < 20; i++) {
    const left = page.getByTestId('e6-lock-left');
    if ((await left.count()) === 0) break;
    const t = (await left.textContent()) ?? '';
    expect(t).not.toContain('-');
    const m = /Còn (\d+) giây/.exec(t);
    if (m) seen.push(Number(m[1]));
    await page.waitForTimeout(1_000);
  }
  expect(seen.length).toBeGreaterThanOrEqual(3);
  for (let i = 1; i < seen.length; i++) expect(seen[i]!).toBeLessThanOrEqual(seen[i - 1]!);
  expect(Math.min(...seen)).toBeGreaterThanOrEqual(0);
});

test('Giáo viên mở ngay khi học sinh đang xem @TC-GEO-0689', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = asg(sb, 'BG-1');
  await solutionTab(page, stu(sb, 'An'), 'An', id);
  await expect(page.getByTestId('e6-lock')).toHaveAttribute('data-state', 'upcoming');
  await page.waitForTimeout(3_000);
  const v = (await (await api.releaseView(request, sb.teacher.token, id)).json()) as { releaseRev: number };
  const t = Date.now();
  expect((await api.release(request, sb.teacher.token, id, { openNow: true, expectedRev: v.releaseRev })).status()).toBe(200);
  await expect(page.getByTestId('e6-toast-opened')).toHaveText(MSG['55'], { timeout: 15_000 });
  expect(Date.now() - t).toBeLessThanOrEqual(10_000);
  await expect(page.getByTestId('e4-steps')).toBeVisible();
});

test('"Bài của em" đang mở cũng đổi nhãn @TC-GEO-0690', async ({ page, request }) => {
  const { sb, R, skew } = await bg4(request, 10);
  await signIn(page, stu(sb, 'Khánh'), 'Khánh', 'student', '9B');
  await page.goto('/hs/bai-cua-em');
  const label = page.getByTestId('e1-assignment-HKG-04-release');
  await expect(label).toContainText('Lời giải mở lúc');
  await expect(label).toHaveText('Lời giải đã mở', { timeout: 40_000 });
  expect(Date.now() - skew).toBeLessThanOrEqual(R + 10_000);
});

test('Mất mạng đúng lúc tới giờ: thẻ khóa giữ, có mạng lại thì mở trong 10 giây @TC-GEO-0693', async ({ page, request, context }) => {
  const { sb, R, skew } = await bg4(request, 10);
  await solutionTab(page, stu(sb, 'Khánh'), 'Khánh', asg(sb, 'BG-4'));
  await expect(page.getByTestId('e6-lock')).toHaveAttribute('data-state', 'upcoming');
  await context.setOffline(true);
  const back = R + skew + 8_000; // mất mạng qua R
  await page.waitForTimeout(Math.max(0, back - Date.now()));
  await expect(page.getByTestId('e6-lock')).toBeVisible();
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  await context.setOffline(false);
  const online = Date.now();
  await expect(page.getByTestId('e6-toast-opened')).toHaveText(MSG['55'], { timeout: 15_000 });
  expect(Date.now() - online).toBeLessThanOrEqual(10_000);
  await expect(page.getByTestId('e4-solution-answer')).toContainText('15π');
});

test('Đồng hồ thiết bị nhanh 30 giây: chỉ mở theo giờ máy chủ @TC-GEO-0694', async ({ page, request }) => {
  const { sb, R, skew } = await bg4(request, 20);
  await page.clock.install({ time: new Date(Date.now() + 30_000) });
  await solutionTab(page, stu(sb, 'Khánh'), 'Khánh', asg(sb, 'BG-4'));
  // đồng hồ iPad đã qua R (giờ máy chủ mới khoảng R − 15 giây): vẫn khóa
  await page.waitForTimeout(5_000);
  expect(Date.now() - skew).toBeLessThan(R);
  await expect(page.getByTestId('e6-lock')).toHaveAttribute('data-state', 'upcoming');
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  await expect(page.getByTestId('e6-toast-opened')).toHaveText(MSG['55'], { timeout: 40_000 });
  const at = Date.now() - skew;
  expect(at).toBeGreaterThanOrEqual(R);
  expect(at).toBeLessThanOrEqual(R + 10_000);
});

test('Trang đang chờ khóa hỏi lại trạng thái không quá 8 giây một lần @TC-GEO-0697', async ({ page, request }) => {
  test.setTimeout(200_000);
  const sb: Sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = asg(sb, 'BG-1');
  const seen = lockResponses(page);
  const times: number[] = [];
  page.on('request', (r) => {
    if (/\/assignments\/[^/]+\/lock$/.test(new URL(r.url()).pathname)) times.push(Date.now());
  });
  await solutionTab(page, stu(sb, 'An'), 'An', id);
  await page.waitForTimeout(120_000);
  const gaps = times.slice(1).map((t, i) => t - times[i]!);
  expect(gaps.length).toBeGreaterThan(10);
  for (const g of gaps) expect(g).toBeLessThanOrEqual(8_000);
  const before = await Promise.all(seen.map((r) => r.text().catch(() => '')));
  expectNoLeak(before.join('\n'), LEAK_HKG01);
  const v = (await (await api.releaseView(request, sb.teacher.token, id)).json()) as { releaseRev: number };
  const t = Date.now();
  expect((await api.release(request, sb.teacher.token, id, { openNow: true, expectedRev: v.releaseRev })).status()).toBe(200);
  await expect(page.getByTestId('e6-toast-opened')).toHaveText(MSG['55'], { timeout: 15_000 });
  expect(Date.now() - t).toBeLessThanOrEqual(10_000);
});

test('Phản hồi trạng thái khóa không cho lưu đệm @TC-GEO-0864', async ({ page, request }) => {
  const { sb, R, skew } = await bg4(request, 8);
  const seen = lockResponses(page);
  await solutionTab(page, stu(sb, 'Khánh'), 'Khánh', asg(sb, 'BG-4'));
  await page.waitForTimeout(Math.max(0, R + skew + 10_000 - Date.now()));
  expect(seen.length).toBeGreaterThan(1);
  for (const r of seen) expect((await r.allHeaders())['cache-control']).toContain('no-store');
  const pre = await Promise.all(seen.filter((r) => r.status() === 200).map((r) => r.text()));
  expectNoLeak(pre.join('\n'), LEAK_HKG04);
});
