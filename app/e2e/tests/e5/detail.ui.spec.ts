import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BG, bearer, devToken, signInPage } from '../e1/e1.ts';
import { iphone } from '../e2/e2.ts';
import { snap, waitScene } from '../e3/e3.ts';
import { F031, F031_HINTS, F037, MSG, PV, PV_TEST, noHorizontalScroll, openBank, signInLane } from './e5.ts';

/** US-GEO-503 — Xem một đề: hình, đáp số, lời giải từng bước, gợi ý (trang "Chi tiết đề & giao bài"). */

async function openDetail(page: Page, versionId: string) {
  await page.goto(`/gv/de/${versionId}`);
  await expect(page.getByTestId('e2-problem-detail')).toBeVisible();
}

async function expectFullSolution(page: Page) {
  await expect(page.getByTestId('e5-answer')).toHaveText('45°');
  for (let i = 0; i < 3; i++) await expect(page.getByTestId(`e5-step-${i + 1}`).locator('.e5-step-body')).toHaveText(F037[i]!);
  await expect(page.getByTestId('e5-step-4')).toHaveCount(0);
}

test('Bốn phần của trang chi tiết HKG-01 @TC-GEO-0515', async ({ page, request }) => {
  await signInLane(page, request);
  await openBank(page);
  await page.getByTestId('e5-open-HKG-01').click();
  await expect(page).toHaveURL(new RegExp(`/gv/de/${PV('HKG-01')}$`));
  await expect(page.getByRole('heading', { name: 'Chi tiết đề & giao bài' })).toBeVisible();
  await expect(page.getByTestId('e2-problem-statement')).toHaveText(F031);
  await waitScene(page);
  const s = await snap(page);
  expect(s.points.map((p) => p.name).sort()).toEqual(expect.arrayContaining(['A', 'B', 'C', 'D', 'S']));
  for (const n of ['S', 'A', 'B', 'C', 'D']) await expect(page.getByTestId(`viewer-label-${n}`)).toBeVisible();
  await expectFullSolution(page);
  await expect(page.getByTestId('e5-hint-1').locator('p')).toHaveText(F031_HINTS[0]!);
  await expect(page.getByTestId('e5-hint-2').locator('p')).toHaveText(F031_HINTS[1]!);
});

test('Giáo viên thấy lời giải dù BG-1 đang Sắp mở @TC-GEO-0516', async ({ page, request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const mine = (await (await request.get(`${API}/api/v1/student/assignments`, { headers: bearer(an) })).json()) as {
    items: Array<{ id: string; releaseState: string }>;
  };
  expect(mine.items.find((a) => a.id === BG('BG-1'))?.releaseState).toBe('upcoming');
  await signInPage(page, request, 'F-GEO-003');
  await openDetail(page, PV('HKG-01'));
  await expectFullSolution(page);
});

test('Chip đáp số khớp tính toán chỉ ở đề đã kiểm chứng @TC-GEO-0517', async ({ page, request }) => {
  await signInLane(page, request);
  await openDetail(page, PV('HKG-01'));
  await expect(page.getByTestId('e5-answer-match')).toHaveText('Đáp số khớp tính toán: 45°');
  await openDetail(page, PV_TEST(45));
  await expect(page.getByTestId('e2-problem-label')).toHaveText('Cần xem lại');
  await expect(page.getByTestId('e5-answer')).toBeVisible();
  await expect(page.getByTestId('e5-answer-match')).toHaveCount(0);
  await expect(page.locator('body')).not.toContainText('Đáp số khớp tính toán');
});

test('Hình xoay được trên trang chi tiết @TC-GEO-0518', async ({ page, request }) => {
  await signInLane(page, request);
  await openDetail(page, PV('HKG-01'));
  await waitScene(page);
  const before = (await snap(page)).camera;
  // điện thoại (mobile-*): khung hình nằm dưới đề, cuộn tới trước khi kéo; khung phải rộng gần hết bề ngang (T-261126-02)
  await page.getByTestId('viewer').scrollIntoViewIfNeeded();
  const box = (await page.getByTestId('viewer').boundingBox())!;
  expect(box.width, 'khung hình không bị co').toBeGreaterThan(Math.min(300, page.viewportSize()!.width * 0.6));
  const cx = box.x + box.width / 2;
  const cy = box.y + box.height / 2;
  await page.mouse.move(cx, cy);
  await page.mouse.down();
  await page.mouse.move(cx + 80, cy + 30, { steps: 8 });
  await page.mouse.up();
  await expect.poll(async () => (await snap(page)).camera.yaw).not.toBeCloseTo(before.yaw, 3);
  expect((await snap(page)).opCount, 'xoay không phải thao tác vẽ').toBe(0);
});

test('Trang chi tiết trên điện thoại: xếp dọc đúng thứ tự, không cuộn ngang @TC-GEO-0519', async ({ browser, request }) => {
  const ctx = await iphone(browser);
  try {
    const page = await ctx.newPage();
    await signInLane(page, request);
    await openDetail(page, PV('HKG-01'));
    const ys: number[] = [];
    for (const part of ['statement', 'figure', 'solution', 'hints', 'assign']) {
      const b = (await page.getByTestId(`e5-part-${part}`).boundingBox())!;
      ys.push(b.y);
    }
    expect([...ys].sort((a, b) => a - b), 'đề, hình, lời giải, gợi ý, giao bài').toEqual(ys);
    await noHorizontalScroll(page);
  } finally {
    await ctx.close();
  }
});

test('Đề không có lời giải: MSG-GEO-54, đề và hình vẫn hiện @TC-GEO-0520', async ({ page, request }) => {
  await signInLane(page, request);
  await openDetail(page, PV_TEST(48));
  await expect(page.getByTestId('e5-solution-empty')).toHaveText(MSG['54']);
  await expect(page.getByTestId('e2-problem-statement')).toContainText('Mặt cầu ngoại tiếp hình lập phương cạnh a.');
  await expect(page.getByTestId('viewer')).toBeVisible();
  await waitScene(page);
});

test('Không có "Sửa (sắp có)", "Trình chiếu", "Concept · Chặng 2" @TC-GEO-0521', async ({ page, request, browser }) => {
  const check = async (p: Page) => {
    await signInLane(p, request);
    await openDetail(p, PV('HKG-01'));
    const text = await p.locator('body').innerText();
    for (const bad of ['Sửa (sắp có)', 'Trình chiếu', 'Concept · Chặng 2']) expect(text).not.toContain(bad);
    await expect(p.getByRole('button', { name: /Sửa|Trình chiếu/ })).toHaveCount(0);
  };
  await check(page);
  const ctx = await iphone(browser);
  try {
    await check(await ctx.newPage());
  } finally {
    await ctx.close();
  }
});

test('Mã đề không tồn tại: màn hiện MSG-GEO-04 @TC-GEO-0523', async ({ page, request }) => {
  await signInLane(page, request);
  await page.goto('/gv/de/00000000-0000-7000-8000-0000e5999999');
  await expect(page.getByTestId('e2-problem-error')).toHaveText(MSG['04']);
  await expect(page.getByTestId('e2-problem-error')).toHaveAttribute('data-error-code', 'NOT_FOUND');
});
