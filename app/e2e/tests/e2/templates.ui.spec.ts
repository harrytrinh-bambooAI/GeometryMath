import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, API, withLock } from '../e1/e1.ts';
import { AN, CARDS, CARD_CODE, MSG19, iphone, openSelfStudy } from './e2.ts';

/** US-GEO-202 (hình mẫu có tham số, không gọi AI) và US-GEO-205-SC2 (sáu thẻ trên màn Tự học). */

async function pickCone(page: Page) {
  await page.getByTestId('e2-template-cone').click();
  await expect(page.getByTestId('e2-explorer-cone').getByTestId('e2-scene')).toBeVisible();
}

const params = async (page: Page) =>
  JSON.parse((await page.getByTestId('e2-scene-params').getAttribute('data-params')) ?? '{}') as Record<string, number>;

test('Chọn hình mẫu và thấy hình ngay, không gọi AI @TC-GEO-0208', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  const calls = async () =>
    ((await (await request.get(`${API}/test-hooks/ai/calls?userId=${AN}&limit=100`)).json()) as unknown[]).length;
  await withLock(AN_AI_LOCK, async () => {
    const n = await calls();
    await page.getByTestId('e2-template-cone').click();
    const ex = page.getByTestId('e2-explorer-cone');
    await expect(ex.getByTestId('e2-scene')).toBeVisible();
    await expect(ex.getByTestId('e2-scene')).toHaveAttribute('data-template', 'cone');
    await expect(ex.getByText('Bán kính đáy')).toBeVisible();
    await expect(ex.getByText('Chiều cao')).toBeVisible();
    await expect(page.getByTestId('e2-param-r')).toHaveValue('2'); // tham số mặc định
    await expect(page.getByTestId('e2-ai-wait')).toHaveCount(0); // không có màn chờ AI
    expect(await calls()).toBe(n);
  });
});

test('Đổi tham số thì hình đổi theo @TC-GEO-0209', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  await pickCone(page);
  await page.getByTestId('e2-param-r').fill('3');
  await page.getByTestId('e2-param-h').fill('4');
  await expect.poll(() => params(page)).toEqual({ r: 3, h: 4 });
  // công cụ Đo dài: đường sinh SM
  await page.getByTestId('e2-measure-p').selectOption('S');
  await page.getByTestId('e2-measure-q').selectOption('M');
  await expect(page.getByTestId('e2-measure-result')).toHaveText('SM = 5');
});

test('Nhận số thập phân dấu phẩy @TC-GEO-0210', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  await pickCone(page);
  await page.getByTestId('e2-param-r').fill('2,5');
  await expect.poll(async () => (await params(page)).r).toBe(2.5);
  await expect(page.getByTestId('e2-param-r')).toHaveValue('2,5');
  await expect(page.getByTestId('e2-param-r-error')).toHaveCount(0);
});

test('Tham số không hợp lệ @TC-GEO-0212', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  await pickCone(page);
  await page.getByTestId('e2-param-r').fill('3');
  await expect.poll(async () => (await params(page)).r).toBe(3);
  for (const bad of ['0', '−2', 'abc']) {
    await page.getByTestId('e2-param-r').fill(bad);
    await expect(page.getByTestId('e2-param-r-error')).toHaveText(MSG19);
    expect((await params(page)).r).toBe(3); // hình giữ nguyên bán kính 3
  }
});

test('Sáu thẻ hình mẫu trên màn "Tự học" @TC-GEO-0231', async ({ page, request }, testInfo) => {
  await openSelfStudy(page, request, testInfo);
  await expect(page.getByText('Hoặc chọn một hình mẫu')).toBeVisible();
  await expect(page.getByTestId('e2-templates').getByRole('button')).toHaveText(CARDS);
});

async function allSixBuildAndRotate(page: Page) {
  for (const name of CARDS) {
    await page.getByTestId('e2-templates').getByRole('button', { name, exact: true }).click();
    const frame = page.getByTestId(`e2-explorer-${CARD_CODE[name]}`).getByTestId('e2-scene');
    await expect(frame).toBeVisible();
    expect(Number(await frame.getAttribute('data-points'))).toBeGreaterThan(0);
    const yaw = await frame.getAttribute('data-yaw');
    await frame.getByTestId('e2-scene-rotate-right').click();
    await expect(frame).not.toHaveAttribute('data-yaw', yaw ?? '');
  }
}

test('Sáu hình mẫu đều dựng được @TC-GEO-0211', async ({ page, request, browser }, testInfo) => {
  // iPad (dự án ui-webkit) hoặc máy tính (ui-chromium)
  await openSelfStudy(page, request, testInfo);
  await allSixBuildAndRotate(page);
  // iPhone (F-GEO-101)
  const ctx = await iphone(browser);
  try {
    const phone = await ctx.newPage();
    await openSelfStudy(phone, request, testInfo);
    await allSixBuildAndRotate(phone);
    await testInfo.attach('iphone.png', { body: await phone.screenshot(), contentType: 'image/png' });
  } finally {
    await ctx.close();
  }
});
