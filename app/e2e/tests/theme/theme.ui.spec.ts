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
 * T-261126-44: test ảnh soát thêm chữ ≥ 14 px (PO-66 G17), không tràn ngang, theme C nội dung toán dùng --font-math.
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

/**
 * T-261126-44 · soát một màn sau khi dựng: chữ có nội dung < 14 px (PO-66 G17; trừ nhãn thanh dọc --fs-rail, chữ trong
 * svg tính theo viewBox, ô xem trước theme, chỉ số trên/dưới) và phần tử tràn ngang (trang, nút, chip, ô lọc, nút bộ xem).
 */
async function auditScreen(page: Page): Promise<{ small: string[]; overflow: string[] }> {
  return page.evaluate(() => {
    const small: string[] = [];
    const overflow: string[] = [];
    const root = document.documentElement;
    if (root.scrollWidth > root.clientWidth + 1) overflow.push(`trang ${root.scrollWidth} > ${root.clientWidth}`);
    const name = (el: Element) => `${el.tagName.toLowerCase()}.${el.getAttribute('class') ?? ''}[${el.getAttribute('data-testid') ?? ''}]`;
    for (const el of Array.from(document.querySelectorAll<HTMLElement>('#root *'))) {
      if (el.closest('svg, .rail, .theme-preview, sub, sup')) continue;
      const box = el.getBoundingClientRect();
      if (box.width < 2 || box.height < 2) continue;
      const cs = getComputedStyle(el);
      if (cs.visibility === 'hidden' || cs.display === 'none') continue;
      if (el.matches('.btn, .chip, .e5-opt, .gv-tool, .gv-view') && el.scrollWidth > el.clientWidth + 1) {
        overflow.push(`${name(el)} ${el.scrollWidth} > ${el.clientWidth}`);
      }
      const ownText = Array.from(el.childNodes).some((n) => n.nodeType === Node.TEXT_NODE && (n.textContent ?? '').trim() !== '');
      const field = el.matches('input:not([type="checkbox"]):not([type="radio"]), textarea, select');
      if ((ownText || field) && Number.parseFloat(cs.fontSize) < 14) small.push(`${name(el)} ${cs.fontSize}`);
    }
    return { small: small.slice(0, 20), overflow: overflow.slice(0, 20) };
  });
}

const fontFamilyOf = (page: Page, selector: string) =>
  page.locator(selector).first().evaluate((el) => getComputedStyle(el).fontFamily);

test('Ảnh màn MVP (Kho đề, Chi tiết đề) ở 4 theme; chữ ≥ 14 px, không tràn; theme C: nội dung toán dùng --font-math', async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== 'ui-chromium', 'ảnh bằng chứng chụp một lần trên Chromium máy tính');
  test.slow();
  // Không chặn Google Fonts ở test này: có mạng thì ảnh và phép đo bố cục dùng đúng chữ tay Bad Script của theme C;
  // mất mạng thì chữ rơi về font hệ thống (ghi vào annotation `bad-script`), các kiểm tra còn lại vẫn chạy.
  await page.setViewportSize({ width: 1440, height: 900 });
  await signInPage(page, request, 'F-GEO-003');
  let theme = 'd';
  // theme của tài khoản giả lập ở trình duyệt: không ghi tùy chọn của Giáo viên mẫu dùng chung trên máy chủ
  await page.route('**/api/v1/me/settings', (r) => r.fulfill({ json: { theme } }));
  mkdirSync(EVIDENCE, { recursive: true });
  const shoot = async (name: string) => {
    await page.evaluate(() => Promise.race([document.fonts.ready, new Promise((r) => setTimeout(r, 5000))]));
    const audit = await auditScreen(page);
    expect(audit.small, `${name}: chữ < 14 px`).toEqual([]);
    expect(audit.overflow, `${name}: tràn ngang`).toEqual([]);
    await page.screenshot({ path: path.join(EVIDENCE, `${name}.png`), fullPage: false });
    await testInfo.attach(name, { path: path.join(EVIDENCE, `${name}.png`), contentType: 'image/png' });
  };
  for (const t of ['a', 'b', 'c', 'd']) {
    theme = t;
    await page.goto('/gv/kho-de');
    await expect(htmlTheme(page)).toHaveAttribute('data-theme', t);
    await expect(page.getByTestId('e1-catalog')).toBeVisible();
    await expect(page.locator('.e5-statement').first()).toBeVisible();
    expect(await bodyBg(page)).toBe(THEME_BG[t]);
    const fsSm = await page.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--fs-sm').trim());
    expect(fsSm, `theme ${t}: --fs-sm`).toBe('14px');
    if (t === 'c') {
      const loaded = await page.evaluate(() =>
        Promise.race([
          document.fonts.load("16px 'Bad Script'", 'Đề bài').then((f) => f.length > 0, () => false),
          new Promise<boolean>((r) => setTimeout(() => r(false), 5000)),
        ]),
      );
      testInfo.annotations.push({ type: 'bad-script', description: loaded ? 'đã tải font Bad Script' : 'không tải được (mất mạng), dùng font dự phòng' });
      expect(await fontFamilyOf(page, 'body')).toMatch(/^"?Bad Script/);
      expect(await fontFamilyOf(page, '.e5-statement')).toMatch(/^"?Be Vietnam Pro/);
    }
    await shoot(`kho-de-${t}`);

    await page.locator('.e5-statement').first().click();
    await expect(page.getByTestId('e5-part-statement')).toBeVisible();
    await expect(page.getByTestId('e2-problem-statement')).toBeVisible();
    await expect(htmlTheme(page)).toHaveAttribute('data-theme', t);
    await expect(page.getByTestId('viewer')).toBeVisible();
    if (t === 'c') {
      expect(await fontFamilyOf(page, '[data-testid="e2-problem-statement"]')).toMatch(/^"?Be Vietnam Pro/);
      // nhãn đỉnh của hình 3D và đáp số: có trên đề nào thì kiểm trên đề đó
      for (const sel of ['.gv-point text', '[data-testid="e5-answer"]', '.e5-step-body']) {
        if ((await page.locator(sel).count()) > 0) expect(await fontFamilyOf(page, sel), `font toán: ${sel}`).toMatch(/^"?Be Vietnam Pro/);
      }
      expect(await fontFamilyOf(page, '.e5-detail-head h2')).toMatch(/^"?Bad Script/);
    }
    await shoot(`chi-tiet-de-${t}`);
  }
});
