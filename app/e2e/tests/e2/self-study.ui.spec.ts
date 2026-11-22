import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, withLock } from '../e1/e1.ts';
import { F061, F064, MSG16, MSG17, openSelfStudy, selfStudyCount } from './e2.ts';

/** US-GEO-204 (đề lạ dùng tọa độ AI kèm cảnh báo; đề hình phẳng). Mỗi lượt dựng là một lượt gọi AI của An. */

async function buildForeign(page: Page, request: APIRequestContext, testInfo: TestInfo) {
  await openSelfStudy(page, request, testInfo);
  await page.getByTestId('e2-statement').fill(F061);
  await withLock(AN_AI_LOCK, async () => {
    await page.getByTestId('e2-build').click();
    await expect(page.getByTestId('e2-self-study-result')).toBeVisible();
  });
}

async function expectWarned(page: Page) {
  const frame = page.getByTestId('e2-scene');
  await expect(frame).toBeVisible();
  await expect(frame).toHaveAttribute('data-coord-source', 'ai');
  await expect(page.getByTestId('e2-problem-label')).toHaveText('Cần xem lại');
  await expect(frame.getByTestId('e2-scene-label')).toHaveText('Cần xem lại');
  await expect(frame.getByTestId('e2-scene-warning')).toHaveText(MSG16);
}

test('Đề lạ có hình kèm cảnh báo @TC-GEO-0224', async ({ page, request }, testInfo) => {
  await buildForeign(page, request, testInfo);
  await expectWarned(page);
  expect(Number(await page.getByTestId('e2-scene').getAttribute('data-points'))).toBe(6);
});

test('Cảnh báo luôn hiện @TC-GEO-0225', async ({ page, request }, testInfo) => {
  await buildForeign(page, request, testInfo);
  await expectWarned(page);
  // xoay hình bằng kéo
  const svg = page.getByTestId('e2-scene-svg');
  const box = (await svg.boundingBox())!;
  const yaw = await page.getByTestId('e2-scene').getAttribute('data-yaw');
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + box.width / 2 + 80, box.y + box.height / 2 + 10, { steps: 5 });
  await page.mouse.up();
  await expect(page.getByTestId('e2-scene')).not.toHaveAttribute('data-yaw', yaw ?? '');
  await expectWarned(page);
  // đổi tab Hỏi đáp rồi quay lại
  await page.getByTestId('e2-tab-hoi-dap').click();
  await expect(page.getByTestId('e2-qa')).toBeVisible();
  await page.getByTestId('e2-tab-hinh').click();
  await expectWarned(page);
  // tải lại trang
  await page.reload();
  await expectWarned(page);
  // không có nút đóng cảnh báo
  const warning = page.getByTestId('e2-scene-warning');
  await expect(warning.getByRole('button')).toHaveCount(0);
  await expect(page.getByRole('button', { name: /đóng|close|×/i })).toHaveCount(0);
});

test('Đề hình phẳng @TC-GEO-0229', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  await page.getByTestId('e2-statement').fill(F064);
  const [before, after] = await withLock(AN_AI_LOCK, async () => {
    const b = await selfStudyCount(request);
    await page.getByTestId('e2-build').click();
    await expect(page.getByTestId('e2-statement-error')).toHaveText(MSG17);
    return [b, await selfStudyCount(request)] as const;
  });
  await expect(page.getByTestId('e2-self-study-result')).toHaveCount(0);
  await expect(page.getByTestId('e2-self-study').getByTestId('e2-scene')).toHaveCount(0);
  await expect(page.getByTestId('e2-statement')).toHaveValue(F064);
  expect(after).toBe(before);
});
