import type { Page } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { HKG, MSG, PV, api, asg, cls, msg41, msg42, signIn, stu, twoClasses, vn, vnInputs } from './e6.ts';
import type { Sb } from './e6.ts';

/** E6 — "Chi tiết đề & giao bài": chọn lớp, trợ giúp, giờ mở (US-GEO-602, 603, 604). Giáo viên mới mỗi ca. */

async function openAssign(page: Page, sb: Sb, code: string) {
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/giao-bai/${PV(code)}`);
  await expect(page.getByTestId('e6-assign')).toBeVisible();
}
const classRadio = (page: Page, name: string) => page.getByTestId(`e6-class-${name}`);
const submit = (page: Page) => page.getByTestId('e6-assign-submit');
async function mineCodes(request: Parameters<typeof api.mine>[0], token: string) {
  const r = (await (await api.mine(request, token)).json()) as { items: Array<{ problemCode: string; releaseState: string; releaseAt?: string; allowHint: boolean; allowQa: boolean; id: string }> };
  return r.items;
}

test('Danh sách lớp ở bước 1 @TC-GEO-0605', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto('/gv/kho-de');
  await page.getByTestId('e1-problem-HKG-06-assign').click();
  await expect(page.getByRole('heading', { name: 'Chi tiết đề & giao bài' })).toBeVisible();
  const step = page.getByTestId('e1-assign-step-class');
  await expect(step.getByRole('heading')).toHaveText('1 · Chọn lớp');
  await expect(step.locator('label')).toHaveText(['11A1 · THPT (5 HS)', '9B · THCS (5 HS)']);
});

test('Giao cho một lớp @TC-GEO-0606', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '9B').click();
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41('9B'));
  const kh = await mineCodes(request, stu(sb, 'Khánh').token);
  expect(kh.find((x) => x.problemCode === 'HKG-06')?.releaseState).toBe('unset');
  expect((await mineCodes(request, stu(sb, 'An').token)).map((x) => x.problemCode)).not.toContain('HKG-06');
});

test('Nút giao ghi tên lớp đang chọn @TC-GEO-0607', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await expect(submit(page)).toBeDisabled();
  await classRadio(page, '11A1').click();
  await expect(submit(page)).toHaveText('Giao bài cho 11A1');
  await expect(submit(page)).toBeEnabled();
});

test('Giao lại cùng đề cho cùng lớp @TC-GEO-0609', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const before = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { releaseAt: string };
  await openAssign(page, sb, 'HKG-01');
  await classRadio(page, '11A1').click();
  const resp = page.waitForResponse((r) => r.url().endsWith('/api/v1/teacher/assignments') && r.request().method() === 'POST');
  await submit(page).click();
  expect((await resp).status()).toBe(409);
  await expect(page.getByTestId('e6-assign-error')).toHaveText(msg42('11A1'));
  const an = await mineCodes(request, stu(sb, 'An').token);
  expect(an.filter((x) => x.problemCode === 'HKG-01')).toHaveLength(1);
  const after = (await (await api.releaseView(request, sb.teacher.token, asg(sb, 'BG-1'))).json()) as { releaseAt: string };
  expect(after.releaseAt).toBe(before.releaseAt);
});

test('Bấm đúp nút giao: một bài, một thông báo @TC-GEO-0610', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '9B').click();
  await submit(page).dblclick();
  await expect(page.getByTestId('e6-assign-done')).toHaveCount(1);
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41('9B'));
  await expect(page.getByTestId('e6-assign-error')).toHaveCount(0);
  expect((await mineCodes(request, stu(sb, 'Khánh').token)).filter((x) => x.problemCode === 'HKG-06')).toHaveLength(1);
});

test('Không có giao theo bộ @TC-GEO-0611', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto('/gv/kho-de');
  await expect(page.getByTestId('e1-problem-HKG-06-assign')).toBeVisible();
  await expect(page.locator('input[type="checkbox"]')).toHaveCount(0);
  await expect(page.getByText(/giao bộ đề/i)).toHaveCount(0);
  await page.goto(`/gv/giao-bai/${PV('HKG-06')}`);
  await expect(page.getByTestId('e6-assign-problem')).toContainText('HKG-06');
  await expect(page.getByText(/giao bộ đề/i)).toHaveCount(0);
});

test('Mặc định cả hai công tắc bật @TC-GEO-0612', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  const step = page.getByTestId('e6-assign-step-help');
  await expect(step).toContainText('Gợi ý mức 1 và 2');
  await expect(step).toContainText('Hỏi đáp (không nêu đáp số)');
  await expect(page.getByTestId('e6-switch-hint')).toBeChecked();
  await expect(page.getByTestId('e6-switch-qa')).toBeChecked();
});

async function assignWith(page: Page, sb: Sb, cls: string, opts: { hint?: boolean; qa?: boolean }) {
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, cls).click();
  if (opts.hint === false) await page.getByTestId('e6-switch-hint').uncheck();
  if (opts.qa === false) await page.getByTestId('e6-switch-qa').uncheck();
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41(cls));
}

test('Tắt gợi ý rồi giao @TC-GEO-0613', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignWith(page, sb, '9B', { hint: false });
  const k = stu(sb, 'Khánh');
  const id = (await mineCodes(request, k.token)).find((x) => x.problemCode === 'HKG-06')!.id;
  await page.context().clearCookies();
  const p2 = await page.context().newPage();
  await signIn(p2, k, 'Khánh', 'student', '9B');
  await p2.goto(`/hs/lam-bai/${id}`);
  await p2.getByTestId('e3-tab-hint').click();
  await expect(p2.getByTestId('e3-tabpanel-hint')).toContainText(MSG['32']);
  await p2.getByTestId('e3-tab-ask').click();
  await expect(p2.getByTestId('e3-tabpanel-ask')).not.toContainText(MSG['33']);
  expect((await api.ask(request, k.token, id, 'Tan của một góc là gì?')).status()).toBe(200);
});

test('Tắt hỏi đáp rồi giao @TC-GEO-0614', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await assignWith(page, sb, '11A1', { qa: false });
  const an = stu(sb, 'An');
  const id = (await mineCodes(request, an.token)).find((x) => x.problemCode === 'HKG-06')!.id;
  const p2 = await page.context().newPage();
  await signIn(p2, an, 'An', 'student', '11A1');
  await p2.goto(`/hs/lam-bai/${id}`);
  await p2.getByTestId('e3-tab-ask').click();
  await expect(p2.getByTestId('e3-tabpanel-ask')).toContainText(MSG['33']);
  await p2.getByTestId('e3-tab-hint').click();
  await expect(p2.getByTestId('e3-tabpanel-hint').getByRole('button', { name: 'Xem gợi ý 1' })).toBeVisible();
});

test('Tắt cả hai vẫn giao được (BG-5) @TC-GEO-0615', async ({ page, request }) => {
  const sb = await twoClasses(request, [], [{ key: 'BG-5', code: 'HKG-05', release: 'unset', allowHint: false, allowQa: false }]);
  await signIn(page, stu(sb, 'Khánh'), 'Khánh', 'student', '9B');
  await page.goto(`/hs/lam-bai/${asg(sb, 'BG-5')}`);
  await expect(page.getByTestId('e3-statement')).toHaveText(HKG['05']);
  await expect(page.locator('[data-testid^="viewer-tool-"]').first()).toBeVisible();
  await page.getByTestId('e3-tab-hint').click();
  await expect(page.getByTestId('e3-tabpanel-hint')).toContainText(MSG['32']);
  await page.getByTestId('e3-tab-ask').click();
  await expect(page.getByTestId('e3-tabpanel-ask')).toContainText(MSG['33']);
  await page.getByTestId('e3-tab-solution').click();
  await expect(page.getByTestId('e6-lock-title')).toHaveText(MSG['28']);
});

test('Không đổi được công tắc sau khi giao @TC-GEO-0617', async ({ page, request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  await signIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(sb, 'BG-1')}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
  await expect(page.getByRole('switch')).toHaveCount(0);
  await page.goto(`/gv/de/${PV('HKG-01')}`);
  await expect(page.getByTestId('e5-part-assign')).toBeVisible();
  await expect(page.getByRole('switch')).toHaveCount(0);
  // kiểm mã trước khi đọc thân: lỗi 5xx (máy tải cao) báo rõ thay vì "items undefined" (T-261126-43)
  const mine = await api.mine(request, stu(sb, 'An').token);
  expect(mine.status(), await mine.text()).toBe(200);
  const v = (await mine.json()) as { items: Array<{ allowHint: boolean; allowQa: boolean }> };
  expect(v.items[0]).toMatchObject({ allowHint: true, allowQa: true });
  expect(cls(sb, '11A1').id).toBeTruthy();
});

// ---------------------------------------------------------------- bước 3 · Thời điểm mở lời giải (US-GEO-604)
async function setTime(page: Page, at: Date) {
  const v = vnInputs(at);
  await page.getByTestId('e6-release-date').fill(v.date);
  await page.getByTestId('e6-release-time').fill(v.time);
}
async function studentLock(page: Page, sb: Sb, who: string) {
  const s = stu(sb, who);
  const id = (await mineCodes(page.request, s.token)).find((x) => x.problemCode === 'HKG-06')!.id;
  const p2 = await page.context().newPage();
  await signIn(p2, s, who, 'student');
  await p2.goto(`/hs/lam-bai/${id}`);
  await p2.getByTestId('e3-tab-solution').click();
  return { p2, id };
}

test('Đặt giờ tương lai: Sắp mở, thẻ khóa ghi đúng giờ @TC-GEO-0618', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '11A1').click();
  const at = new Date(Math.ceil((Date.now() + 60 * 60_000) / 60_000) * 60_000);
  await setTime(page, at);
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41('11A1'));
  const { p2 } = await studentLock(page, sb, 'An');
  await expect(p2.getByTestId('e6-lock-title')).toHaveText(`Lời giải mở lúc ${vn(at)}`);
});

test('Để trống giờ mở: MSG-GEO-45, Chưa đặt giờ @TC-GEO-0619', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '11A1').click();
  await expect(page.getByTestId('e6-release-info')).toHaveText(MSG['45']);
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toBeVisible();
  const { p2 } = await studentLock(page, sb, 'An');
  await expect(p2.getByTestId('e6-lock-title')).toHaveText(MSG['28']);
  await expect(p2.getByTestId('e6-lock-sub')).toHaveText(MSG['28b']);
});

test('Dòng nhắc trước khi giao (19:00) @TC-GEO-0620', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await page.getByTestId('e6-release-date').fill('2030-05-20');
  await page.getByTestId('e6-release-time').fill('19:00');
  await expect(page.getByTestId('e6-release-info')).toHaveText(
    'Trước 19:00 ngày 20/05, học sinh chỉ thấy đề, hình và gợi ý. Lời giải và đáp số được giữ ở máy chủ.',
  );
});

test('"Ngay bây giờ": Đã mở ngay sau khi giao @TC-GEO-0621', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '11A1').click();
  await page.getByTestId('e6-release-now').click();
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41('11A1'));
  const items = await mineCodes(request, stu(sb, 'An').token);
  expect(items.find((x) => x.problemCode === 'HKG-06')?.releaseState).toBe('open');
  const { p2 } = await studentLock(page, sb, 'An');
  await expect(p2.getByTestId('e4-steps')).toBeVisible();
  await expect(p2.getByTestId('e4-solution-answer')).toBeVisible();
});

test('Giờ Việt Nam, lưu UTC (19:00 → 12:00 UTC) @TC-GEO-0622', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '11A1').click();
  await page.getByTestId('e6-release-date').fill('2030-05-20');
  await page.getByTestId('e6-release-time').fill('19:00');
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toBeVisible();
  const it = (await mineCodes(request, stu(sb, 'An').token)).find((x) => x.problemCode === 'HKG-06')!;
  expect(it.releaseAt).toBe('2030-05-20T12:00:00Z');
  const { p2 } = await studentLock(page, sb, 'An');
  await expect(p2.getByTestId('e6-lock-title')).toHaveText('Lời giải mở lúc 19:00, 20/05/2030');
});

test('Thiết bị ở múi giờ khác vẫn hiện giờ Việt Nam @TC-GEO-0623', async ({ browser, request }) => {
  const sb = await twoClasses(request);
  const id = ((await (await api.assign(request, sb.teacher.token, {
    classId: cls(sb, '11A1').id, problemVersionId: PV('HKG-06'), releaseAt: '2030-05-20T12:00:00Z',
  })).json()) as { id: string }).id;
  const ctx = await browser.newContext({ timezoneId: 'Asia/Tokyo', locale: 'vi-VN' });
  const p = await ctx.newPage();
  await signIn(p, stu(sb, 'An'), 'An', 'student');
  await p.goto(`/hs/lam-bai/${id}`);
  await p.getByTestId('e3-tab-solution').click();
  await expect(p.getByTestId('e6-lock-title')).toHaveText('Lời giải mở lúc 19:00, 20/05/2030');
  await ctx.close();
});

test('Giờ ở quá khứ: VALIDATION_FAILED, MSG-GEO-43, không tạo bài @TC-GEO-0624', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  await classRadio(page, '11A1').click();
  await setTime(page, new Date(Date.now() - 60_000));
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-error')).toHaveText(MSG['43']);
  expect((await mineCodes(request, stu(sb, 'An').token)).map((x) => x.problemCode)).not.toContain('HKG-06');
});

test('Không có "Sau buổi học tới" @TC-GEO-0626', async ({ page, request }) => {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  const step = page.getByTestId('e6-assign-step-time');
  await expect(step.getByRole('button')).toHaveText(['Ngay bây giờ']);
  await expect(step).not.toContainText('Sau buổi học tới');
});

/** FIX4a: "Ngay bây giờ" lũy đẳng khi bấm lại sau lỗi giữa chừng (soát E6 M2). */
async function nowRetry(page: Page, request: Parameters<typeof api.mine>[0], lose: 'assign' | 'release') {
  const sb = await twoClasses(request);
  await openAssign(page, sb, 'HKG-06');
  let first = true;
  const pattern = lose === 'assign' ? '**/api/v1/teacher/assignments' : '**/api/v1/teacher/assignments/*/release';
  await page.route(pattern, async (route) => {
    if (route.request().method() !== 'POST' || !first) return route.fallback();
    first = false;
    await route.fetch(); // máy chủ đã ghi, phản hồi mất trên đường về
    await route.abort('failed');
  });
  await classRadio(page, '11A1').click();
  await page.getByTestId('e6-release-now').click();
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-error')).toBeVisible();
  await submit(page).click();
  await expect(page.getByTestId('e6-assign-done')).toHaveText(msg41('11A1'));
  await expect(page.getByTestId('e6-assign-error')).toHaveCount(0);
  const items = (await mineCodes(request, stu(sb, 'An').token)).filter((x) => x.problemCode === 'HKG-06');
  expect(items).toHaveLength(1);
  expect(items[0]?.releaseState).toBe('open');
}

test('Dev-E6-02 "Ngay bây giờ": mất phản hồi lệnh giao, bấm lại → một bài, đã mở, không MSG-42', async ({ page, request }) => {
  await nowRetry(page, request, 'assign');
});

test('Dev-E6-02 "Ngay bây giờ": mất phản hồi lệnh mở, bấm lại → một bài, đã mở, không MSG-48', async ({ page, request }) => {
  await nowRetry(page, request, 'release');
});
