import type { Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { MSG, api, asg, confirmText, msg46, msg47, releaseIn, signIn, stu, twoClasses, vn, vnInputs } from './e6.ts';
import type { Sb } from './e6.ts';

/** E6 — thẻ vàng "Lời giải đang khóa" trên Theo dõi lớp: "Mở ngay", "Đổi giờ" (US-GEO-605). Lớp riêng mỗi ca. */

async function monitor(page: Page, sb: Sb, key: string) {
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(sb, key)}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
}
async function view(sb: Sb, key: string, request: Parameters<typeof api.releaseView>[0]) {
  return (await (await api.releaseView(request, sb.teacher.token, asg(sb, key))).json()) as {
    releaseAt?: string;
    releaseState: string;
    releaseRev: number;
    serverNow: string;
  };
}
async function changeTo(page: Page, at: Date) {
  await page.getByTestId('e6-change-time').click();
  const v = vnInputs(at);
  await page.getByTestId('e6-change-date').fill(v.date);
  await page.getByTestId('e6-change-clock').fill(v.time);
  await page.getByTestId('e6-change-save').click();
}
const minute = (ms: number) => new Date(Math.ceil(ms / 60_000) * 60_000);
async function anLock(page: Page, sb: Sb, key: string, who = 'An') {
  const p2 = await page.context().newPage();
  await signIn(p2, stu(sb, who), who, 'student');
  await p2.goto(`/hs/lam-bai/${asg(sb, key)}`);
  await p2.getByTestId('e3-tab-solution').click();
  return p2;
}

test('Đổi giờ khi Sắp mở @TC-GEO-0627', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  await monitor(page, sb, 'BG-1');
  const v = await view(sb, 'BG-1', request);
  const at = minute(new Date(v.releaseAt!).getTime() + 60 * 60_000);
  await changeTo(page, at);
  await expect(page.getByTestId('e6-toast')).toHaveText(msg47(vn(at)));
  await expect(page.getByTestId('e6-release-card-when')).toHaveText(`Mở lúc ${vn(at)}`);
  const p2 = await anLock(page, sb, 'BG-1');
  await expect(p2.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(at)}`);
});

test('Đặt giờ lần đầu cho bài Chưa đặt giờ @TC-GEO-0628', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  await monitor(page, sb, 'BG-2');
  await expect(page.getByTestId('e6-release-card')).toHaveAttribute('data-state', 'unset');
  const at = minute(Date.now() + 30 * 60_000);
  await changeTo(page, at);
  await expect(page.getByTestId('e6-toast')).toHaveText(msg47(vn(at)));
  expect((await view(sb, 'BG-2', request)).releaseState).toBe('upcoming');
  const p2 = await anLock(page, sb, 'BG-2');
  await expect(p2.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(at)}`);
});

test('Mở ngay có hộp xác nhận @TC-GEO-0629 @smoke', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  await monitor(page, sb, 'BG-1');
  await page.getByTestId('e6-open-now').click();
  await expect(page.getByTestId('e6-confirm-text')).toHaveText(confirmText('11A1'));
  await expect(page.getByTestId('e6-confirm').getByRole('button')).toHaveText(['Mở ngay', 'Hủy']);
  expect((await view(sb, 'BG-1', request)).releaseState).toBe('upcoming');
});

test('Xác nhận mở ngay: giờ mở = giờ máy chủ lúc xác nhận @TC-GEO-0630', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  await monitor(page, sb, 'BG-1');
  await page.getByTestId('e6-open-now').click();
  const t1 = Date.now();
  await page.getByTestId('e6-confirm-ok').click();
  await expect(page.getByTestId('e6-toast')).toHaveText(msg46('11A1'));
  const t2 = Date.now();
  await expect(page.getByTestId('e6-release-card')).toHaveCount(0);
  await expect(page.getByTestId('e6-open-now')).toHaveCount(0);
  await expect(page.getByTestId('e6-change-time')).toHaveCount(0);
  const v = await view(sb, 'BG-1', request);
  expect(v.releaseState).toBe('open');
  const r = new Date(v.releaseAt!).getTime();
  expect(r).toBeGreaterThanOrEqual(t1 - 2_000);
  expect(r).toBeLessThanOrEqual(t2 + 2_000);
  const s = await api.solution(request, stu(sb, 'An').token, asg(sb, 'BG-1'));
  expect(s.status()).toBe(200);
  const body = (await s.json()) as { finalAnswer: string; steps: unknown[] };
  expect(body.finalAnswer).toBe('45°');
  expect(body.steps).toHaveLength(3);
});

test('Hủy mở ngay: không đổi, không ghi nhật ký @TC-GEO-0631', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const before = await view(sb, 'BG-1', request);
  await monitor(page, sb, 'BG-1');
  await page.getByTestId('e6-open-now').click();
  await page.getByTestId('e6-confirm-cancel').click();
  await expect(page.getByTestId('e6-confirm')).toHaveCount(0);
  const after = await view(sb, 'BG-1', request);
  expect(after).toMatchObject({ releaseState: 'upcoming', releaseAt: before.releaseAt });
  const log = (await (await api.releaseLog(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { items: unknown[] };
  expect(log.items).toHaveLength(0);
});

test('Mở ngay từ Chưa đặt giờ @TC-GEO-0632', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  await monitor(page, sb, 'BG-2');
  await page.getByTestId('e6-open-now').click();
  await page.getByTestId('e6-confirm-ok').click();
  await expect(page.getByTestId('e6-toast')).toHaveText(msg46('11A1'));
  const p2 = await anLock(page, sb, 'BG-2');
  await expect(p2.getByTestId('e4-solution-answer')).toHaveText('Đáp số: a');
  const s = (await (await api.solution(request, stu(sb, 'An').token, asg(sb, 'BG-2'))).json()) as { finalAnswer: string };
  expect(s.finalAnswer).toBe('a');
});

test('Đổi giờ khi đã Đã mở: không có thẻ vàng; gửi thẳng nhận CONFLICT @TC-GEO-0635', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  await monitor(page, sb, 'BG-3');
  await expect(page.getByTestId('e6-release-opened')).toBeVisible();
  await expect(page.getByTestId('e6-release-card')).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Mở ngay' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Đổi giờ' })).toHaveCount(0);
  const v = await view(sb, 'BG-3', request);
  const r = await api.release(request, sb.teacher.token, asg(sb, 'BG-3'), {
    releaseAt: new Date(Date.now() + 3_600_000).toISOString(),
    expectedRev: v.releaseRev,
  });
  expect(r.status()).toBe(409);
  expect(await r.json()).toMatchObject({ code: 'CONFLICT', message: MSG['48'] });
  expect((await view(sb, 'BG-3', request)).releaseState).toBe('open');
  expect((await api.solution(request, stu(sb, 'An').token, asg(sb, 'BG-3'))).status()).toBe(200);
});

test('Không có thao tác xóa giờ đã đặt @TC-GEO-0638', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const before = await view(sb, 'BG-1', request);
  await monitor(page, sb, 'BG-1');
  await page.getByTestId('e6-change-time').click();
  const dlg = page.getByTestId('e6-change');
  await expect(dlg.getByRole('button')).toHaveText(['Lưu', 'Hủy']);
  await expect(page.getByTestId('e6-change-save')).toBeDisabled();
  const r = await api.release(request, sb.teacher.token, asg(sb, 'BG-1'), { releaseAt: null, expectedRev: before.releaseRev });
  expect(r.status()).toBe(400);
  expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  expect(await view(sb, 'BG-1', request)).toMatchObject({ releaseState: 'upcoming', releaseAt: before.releaseAt });
});

test('Giờ mở trôi qua trong lúc đang mở hộp "Đổi giờ" @TC-GEO-0699', async ({ page, request }) => {
  const sb = await twoClasses(request, [], [{ key: 'BG-4', code: 'HKG-04', release: '600' }]);
  await monitor(page, sb, 'BG-4');
  await page.getByTestId('e6-change-time').click();
  const at = vnInputs(new Date(Date.now() + 10 * 60_000 + 60_000));
  await page.getByTestId('e6-change-date').fill(at.date);
  await page.getByTestId('e6-change-clock').fill(at.time);
  const r = await releaseIn(request, asg(sb, 'BG-4'), 2); // móc D-GEO-169: R = giờ máy chủ + 2 giây
  await page.waitForTimeout(4_000); // lưu lúc R + 2 giây
  await page.getByTestId('e6-change-save').click();
  await expect(page.getByTestId('e6-change-error')).toHaveText(MSG['48']);
  const v = await view(sb, 'BG-4', request);
  expect(v.releaseState).toBe('open');
  expect(new Date(v.releaseAt!).getTime()).toBe(r.getTime());
  const log = (await (await api.releaseLog(request, sb.teacher.token, asg(sb, 'BG-4'))).json()) as { items: unknown[] };
  expect(log.items).toHaveLength(0);
});
