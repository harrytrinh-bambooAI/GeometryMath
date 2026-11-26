import { mkdirSync } from 'node:fs';
import path from 'node:path';
import type { Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, devToken, signInPage } from '../e1/e1.ts';

/**
 * T-261126-50 · hệ theme: C "Giấy kẻ ô" mặc định (lệnh owner 26/11); đổi A/B/C/D trong Cài đặt (giáo viên + học sinh);
 * lưu theo tài khoản (GET/PUT /api/v1/me/settings) + localStorage dự phòng; tải lại vẫn giữ.
 * Không phụ thuộc mạng: chặn Google Fonts, chữ rơi về font hệ thống trong token --font.
 * Mỗi (test × dự án) dùng một học sinh riêng chưa test nào khác dùng, để chạy song song không giẫm nhau.
 */
const EVIDENCE = path.resolve(import.meta.dirname, '../../../../artifacts/GEO/evidence/theme');
const THEME_BG: Record<string, string> = {
  a: 'rgb(244, 250, 247)',
  b: 'rgb(22, 22, 22)',
  c: 'rgb(251, 248, 238)',
  d: 'rgb(248, 250, 252)',
};

const STUDENT_BY_PROJECT: Record<string, string> = { 'ui-chromium': 'F-GEO-025-2', 'ui-webkit': 'F-GEO-025-3' };

async function offlineFonts(page: Page) {
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
}

const htmlTheme = (page: Page) => page.locator('html');
const bodyBg = (page: Page) => page.evaluate(() => getComputedStyle(document.body).backgroundColor);

function studentFor(testInfo: TestInfo): string {
  const ref = STUDENT_BY_PROJECT[testInfo.project.name];
  test.skip(!ref, 'chỉ chạy ở ui-chromium và ui-webkit');
  return ref!;
}

test('Học sinh đổi theme trong Cài đặt, tải lại vẫn giữ, lưu theo tài khoản', async ({ page, request }, testInfo) => {
  const ref = studentFor(testInfo);
  await offlineFonts(page);
  await signInPage(page, request, ref);
  const token = await devToken(request, ref);
  const auth = { Authorization: `Bearer ${token}` };
  // về trạng thái đầu: mặc định C
  expect((await request.put(`${API}/api/v1/me/settings`, { headers: auth, data: { theme: 'c' } })).status()).toBe(200);

  await page.goto('/hs');
  await page.getByTestId('hs-nav-settings').click();
  await expect(page).toHaveURL(/\/hs\/cai-dat$/);
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'c');
  await expect(page.getByTestId('settings-theme-c-radio')).toBeChecked();
  await expect(page.getByTestId('settings-theme').getByRole('radio')).toHaveCount(4);

  await page.getByTestId('settings-theme-b').click();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'b');
  await expect(page.getByTestId('settings-theme-status')).toHaveText('Đã lưu theo tài khoản.');
  expect(await bodyBg(page)).toBe(THEME_BG.b);

  await page.reload();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'b');
  await expect(page.getByTestId('settings-theme-b-radio')).toBeChecked();

  // máy chủ giữ theo tài khoản: xóa bản dự phòng trên máy, tải lại vẫn là B
  const res = await request.get(`${API}/api/v1/me/settings`, { headers: auth });
  expect(await res.json()).toEqual({ theme: 'b' });
  await page.evaluate(() => localStorage.removeItem('geo.theme'));
  await page.reload();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'b');
  expect(await bodyBg(page)).toBe(THEME_BG.b);

  // trả về mặc định
  await page.getByTestId('settings-theme-c').click();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'c');
  await expect(page.getByTestId('settings-theme-status')).toHaveText('Đã lưu theo tài khoản.');
});

test('Giáo viên có Cài đặt, mặc định C; cảm ứng có vùng chạm ≥ 44 px', async ({ page, request }) => {
  await offlineFonts(page);
  await signInPage(page, request, 'F-GEO-003');
  await page.goto('/gv/cai-dat');
  await expect(page.getByTestId('gv-settings')).toBeVisible();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'c');
  expect(await bodyBg(page)).toBe(THEME_BG.c);
  await expect(page.getByTestId('gv-nav-settings')).toHaveText('Cài đặt');
  for (const t of ['a', 'b', 'c', 'd']) await expect(page.getByTestId(`settings-theme-${t}`)).toBeVisible();

  const coarse = await page.evaluate(() => matchMedia('(pointer: coarse)').matches);
  // C (mặc định): nút cao 44 px; cảm ứng (iPad) vùng chạm ≥ 44 (STYLES.md, quy tắc chung)
  const ctlH = await page.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--ctl-h').trim());
  expect(ctlH).toBe('44px');
  if (coarse) {
    const navH = (await page.getByTestId('gv-nav-settings').boundingBox())!.height;
    expect(navH).toBeGreaterThanOrEqual(44);
  }
});

test('Chưa đăng nhập: lưu trên thiết bị (localStorage), tải lại vẫn giữ', async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== 'ui-chromium', 'không có trạng thái máy chủ, một trình duyệt là đủ');
  await offlineFonts(page);
  await page.goto('/hs/cai-dat');
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'c');
  await page.getByTestId('settings-theme-a').click();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'a');
  await expect(page.getByTestId('settings-theme-status')).toHaveText('Đã lưu trên thiết bị này.');
  await page.reload();
  await expect(htmlTheme(page)).toHaveAttribute('data-theme', 'a');
  expect(await bodyBg(page)).toBe(THEME_BG.a);
});

test('Ảnh một màn MVP (Kho đề) ở 4 theme', async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== 'ui-chromium', 'ảnh bằng chứng chụp một lần trên Chromium máy tính');
  await offlineFonts(page);
  await page.setViewportSize({ width: 1440, height: 900 });
  await signInPage(page, request, 'F-GEO-003');
  let theme = 'd';
  // theme của tài khoản giả lập ở trình duyệt: không ghi tùy chọn của Giáo viên mẫu dùng chung trên máy chủ
  await page.route('**/api/v1/me/settings', (r) => r.fulfill({ json: { theme } }));
  mkdirSync(EVIDENCE, { recursive: true });
  for (const t of ['a', 'b', 'c', 'd']) {
    theme = t;
    await page.goto('/gv/kho-de');
    await expect(htmlTheme(page)).toHaveAttribute('data-theme', t);
    await expect(page.getByTestId('e1-catalog')).toBeVisible();
    expect(await bodyBg(page)).toBe(THEME_BG[t]);
    await page.screenshot({ path: path.join(EVIDENCE, `kho-de-${t}.png`), fullPage: false });
    await testInfo.attach(`kho-de-${t}`, { path: path.join(EVIDENCE, `kho-de-${t}.png`), contentType: 'image/png' });
  }
});
