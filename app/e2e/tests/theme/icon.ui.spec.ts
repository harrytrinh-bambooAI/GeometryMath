import { mkdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';

/**
 * Icon chung theo theme (lệnh owner 26/11/2026, DECISIONS dòng "Icon"; nguồn Design/styles v0.2).
 * - MỘT bộ Material Symbols Rounded đặc cho cả 4 theme; font TỰ LƯU trong app (chặn hẳn Google Fonts mà icon vẫn vẽ).
 * - Màu icon = màu chữ của khối chứa (currentColor), tức token của theme đang chọn; icon mang nghĩa đạt tương phản
 *   như chữ (≥ 4,5:1, STYLES.md "Quy tắc chung").
 * - Icon là trang trí: aria-hidden, không thêm chữ vào nút/chip.
 * Ảnh bằng chứng: artifacts/GEO/evidence/theme/icon-<theme>.png (Kho đề) và icon-bo-<theme>.png (cả bộ glyph).
 */
const EVIDENCE = path.resolve(import.meta.dirname, '../../../../artifacts/GEO/evidence/theme');
const GLYPHS = readFileSync(
  path.resolve(import.meta.dirname, '../../../web/src/assets/fonts/material-symbols-rounded-subset.txt'),
  'utf8',
).trim().split(',');
/** Cỡ icon theo theme (token --ic, --ic-chip; Design/styles/<theme>/style.css luật .ms/.ic). */
const SIZE: Record<string, { ic: number; chip: number }> = {
  a: { ic: 24, chip: 18 },
  b: { ic: 20, chip: 16 },
  c: { ic: 22, chip: 16 },
  d: { ic: 18, chip: 14 },
};

async function blockGoogleFonts(page: Page): Promise<string[]> {
  const asked: string[] = [];
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => {
    asked.push(r.request().url());
    return r.abort();
  });
  return asked;
}

/** Font icon đã tải từ chính nguồn của app (không phải Google). */
async function iconFontLoaded(page: Page): Promise<{ loaded: boolean; urls: string[] }> {
  return page.evaluate(async () => {
    const faces = await document.fonts.load("500 24px 'Material Symbols Rounded'", 'home');
    const urls = performance
      .getEntriesByType('resource')
      .map((e) => e.name)
      .filter((n) => /material-symbols-rounded-subset.*\.woff2/.test(n));
    return { loaded: faces.length > 0 && document.fonts.check("500 24px 'Material Symbols Rounded'", 'home'), urls };
  });
}

interface IconFacts {
  hidden: boolean;
  text: string;
  w: number;
  h: number;
  fits: boolean;
  color: string;
  parentColor: string;
  family: string;
  ratio: number;
}

/** Số đo của một icon: kích thước, glyph có khớp ô 1em không (chữ ghép đã thành hình), màu, tương phản với nền thật. */
async function facts(page: Page, selector: string): Promise<IconFacts> {
  return page.locator(selector).first().evaluate((el) => {
    const rgba = (s: string): [number, number, number, number] => {
      const m = s.match(/[\d.]+/g)!.map(Number);
      return [m[0]!, m[1]!, m[2]!, m[3] ?? 1];
    };
    const over = (top: [number, number, number, number], under: [number, number, number]): [number, number, number] =>
      [0, 1, 2].map((i) => top[i]! * top[3] + under[i]! * (1 - top[3])) as [number, number, number];
    // nền thật: chồng các lớp nền từ <html> xuống phần tử chứa icon
    const chain: Element[] = [];
    for (let n: Element | null = el.parentElement; n; n = n.parentElement) chain.unshift(n);
    let bg: [number, number, number] = [255, 255, 255];
    for (const n of chain) {
      const c = rgba(getComputedStyle(n).backgroundColor);
      if (c[3] > 0) bg = over(c, bg);
    }
    const lum = (c: [number, number, number]) => {
      const [r, g, b] = c.map((v) => {
        const x = v / 255;
        return x <= 0.03928 ? x / 12.92 : ((x + 0.055) / 1.055) ** 2.4;
      });
      return 0.2126 * r! + 0.7152 * g! + 0.0722 * b!;
    };
    const cs = getComputedStyle(el);
    const fg = over(rgba(cs.color), bg);
    const [hi, lo] = [lum(fg), lum(bg)].sort((x, y) => y - x);
    const box = el.getBoundingClientRect();
    return {
      hidden: el.getAttribute('aria-hidden') === 'true',
      text: el.textContent ?? '',
      w: Math.round(box.width * 10) / 10,
      h: Math.round(box.height * 10) / 10,
      fits: el.scrollWidth <= el.clientWidth + 1,
      color: cs.color,
      parentColor: getComputedStyle(el.parentElement!).color,
      family: cs.fontFamily,
      ratio: Math.round(((hi! + 0.05) / (lo! + 0.05)) * 100) / 100,
    };
  });
}

/** Màu thật của một token theme (ví dụ --pri-t) dưới dạng rgb(...). */
const tokenColor = (page: Page, token: string) =>
  page.evaluate((t) => {
    const probe = document.createElement('i');
    probe.style.color = `var(${t})`;
    document.body.append(probe);
    const c = getComputedStyle(probe).color;
    probe.remove();
    return c;
  }, token);

/** Mỗi liên kết trên thanh điều hướng có ĐÚNG một icon (không mục nào thiếu, không mục nào thừa). */
async function expectEveryRailLinkHasOneIcon(page: Page, railTestId: string): Promise<void> {
  const rail = page.getByTestId(railTestId);
  await expect(rail.locator('a').first()).toBeVisible();
  const perLink = await rail.locator('a').evaluateAll((links) =>
    links.map((a) => ({ id: a.getAttribute('data-testid') ?? a.textContent ?? '', icons: a.querySelectorAll('.ms').length })));
  expect(perLink.length, `${railTestId}: có mục điều hướng`).toBeGreaterThanOrEqual(4);
  expect(perLink.filter((l) => l.icons !== 1), `${railTestId}: mục không có đúng một icon`).toEqual([]);
  await expect(rail.locator('.ms')).toHaveCount(perLink.length);
}

test('Icon ở 4 theme: một bộ Material Symbols Rounded tự lưu, màu theo token theme, đủ tương phản; ảnh bằng chứng', async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== 'ui-chromium', 'ảnh bằng chứng chụp một lần trên Chromium máy tính');
  test.slow();
  const asked = await blockGoogleFonts(page);
  await page.setViewportSize({ width: 1440, height: 900 });
  await signInPage(page, request, 'F-GEO-003');
  let theme = 'c';
  // theme của tài khoản giả lập ở trình duyệt: không ghi tùy chọn của Giáo viên mẫu dùng chung trên máy chủ
  await page.route('**/api/v1/me/settings', (r) => r.fulfill({ json: { theme } }));
  mkdirSync(EVIDENCE, { recursive: true });
  const shoot = async (name: string) => {
    const file = path.join(EVIDENCE, `${name}.png`);
    await page.screenshot({ path: file, fullPage: false });
    await testInfo.attach(name, { path: file, contentType: 'image/png' });
  };

  for (const t of ['a', 'b', 'c', 'd']) {
    theme = t;
    await page.goto('/gv/kho-de');
    await expect(page.locator('html')).toHaveAttribute('data-theme', t);
    await expect(page.getByTestId('e1-catalog')).toBeVisible();
    await expect(page.locator('.e5-label .ms').first()).toBeVisible();

    const font = await iconFontLoaded(page);
    expect(font.loaded, `theme ${t}: font icon đã tải`).toBe(true);
    expect(font.urls.length, `theme ${t}: tệp font icon`).toBeGreaterThan(0);
    for (const u of font.urls) expect(new URL(u).origin, 'font icon phục vụ từ chính app').toBe(new URL(page.url()).origin);

    // Thanh dọc: MỌI mục (logo + mục của từng epic + Cài đặt) có đúng một icon; chữ của mục không đổi.
    // Không đếm cứng số mục: epic mới cắm thêm mục rail (ví dụ S2-E8 "Bài luyện") mà không sửa test này.
    const rail = page.getByTestId('gv-rail');
    await expectEveryRailLinkHasOneIcon(page, 'gv-rail');
    for (const id of ['shell-home', 'e1-nav-kho-de', 's2e8-nav-bai-luyen', 'gv-nav-settings']) {
      await expect(rail.getByTestId(id).locator('.ms'), `mục ${id} có icon`).toHaveCount(1);
    }
    await expect(page.getByTestId('s2e8-nav-bai-luyen')).toHaveText('Bài luyện');
    await expect(page.getByTestId('s2e8-nav-bai-luyen').locator('.ms')).toHaveAttribute('data-icon', 'edit');
    await expect(page.getByTestId('gv-nav-settings')).toHaveText('Cài đặt');
    await expect(page.getByTestId('e1-nav-kho-de')).toHaveText('Kho đề');
    await expect(page.getByTestId('e1-nav-kho-de').locator('.ms')).toHaveAttribute('data-icon', 'description');

    const active = await facts(page, '[data-testid="e1-nav-kho-de"] .ms');
    const idle = await facts(page, '[data-testid="gv-nav-settings"] .ms');
    const chip = await facts(page, '.e5-label .ms');
    const btn = await facts(page, '[data-testid$="-assign"] .ms');
    for (const [where, f, px] of [['mục đang chọn', active, SIZE[t]!.ic], ['mục thường', idle, SIZE[t]!.ic], ['chip nhãn', chip, SIZE[t]!.chip],
      ['nút Giao bài', btn, SIZE[t]!.chip]] as const) {
      const at = `theme ${t} · ${where}`;
      expect(f.hidden, `${at}: aria-hidden`).toBe(true);
      expect(f.text, `${at}: không thêm chữ`).toBe('');
      expect(f.family, `${at}: bộ icon`).toMatch(/Material Symbols Rounded/);
      expect(f.fits, `${at}: glyph vừa ô 1em`).toBe(true);
      expect(f.w, `${at}: rộng`).toBe(px);
      expect(f.h, `${at}: cao`).toBe(px);
      expect(f.color, `${at}: màu = màu chữ khối chứa`).toBe(f.parentColor);
      expect(f.ratio, `${at}: tương phản ${f.ratio}:1`).toBeGreaterThanOrEqual(4.5);
    }
    // màu đi theo token của theme đang chọn
    expect(active.color, `theme ${t}: mục đang chọn dùng --pri-t`).toBe(await tokenColor(page, '--pri-t'));
    expect(idle.color, `theme ${t}: mục thường dùng --muted`).toBe(await tokenColor(page, '--muted'));
    const status = await page.locator('.e5-label').first().getAttribute('data-status');
    expect(chip.color, `theme ${t}: chip nhãn`).toBe(await tokenColor(page, status === 'verified' ? '--grn' : '--yel-t'));
    await shoot(`icon-${t}`);

    // Cả bộ glyph của tệp font: mọi tên đều thành hình (không rơi về chữ thường)
    const sheet = await page.evaluate((names) => {
      const host = document.createElement('div');
      host.id = 'icon-sheet';
      host.className = 'card';
      host.style.cssText = 'position:fixed;left:120px;top:40px;width:720px;z-index:99;display:grid;grid-template-columns:repeat(8,1fr);gap:14px;color:var(--text)';
      for (const n of [...names, 'khong_co_icon_nay']) {
        const cell = document.createElement('span');
        cell.className = 'chip';
        cell.style.cssText = 'justify-content:center;padding:10px 0';
        const i = document.createElement('span');
        i.className = 'ms';
        i.dataset.icon = n;
        i.style.fontSize = 'var(--ic)';
        cell.append(i);
        host.append(cell);
      }
      document.body.append(host);
      return Array.from(host.querySelectorAll<HTMLElement>('.ms')).map((i) => ({ n: i.dataset.icon!, fits: i.scrollWidth <= i.clientWidth + 1 }));
    }, GLYPHS);
    expect(sheet.filter((s) => !s.fits).map((s) => s.n), `theme ${t}: glyph thiếu trong font`).toEqual(['khong_co_icon_nay']);
    await page.locator('#icon-sheet .chip').last().evaluate((el) => el.remove());
    await shoot(`icon-bo-${t}`);
  }
  expect(asked.filter((u) => /Material|Symbols/i.test(decodeURIComponent(u))), 'không gọi Google cho font icon').toEqual([]);
});

test('Icon trên thanh điều hướng học sinh (mọi thiết bị): font tự lưu, aria-hidden, vùng chạm giữ ≥ 44 px', async ({ page }) => {
  await blockGoogleFonts(page);
  await page.goto('/hs/cai-dat');
  await expect(page.getByTestId('hs-settings')).toBeVisible();
  const rail = page.getByTestId('hs-rail');
  // Tổng quan, Bài của em, Tự học, Lịch sử, Bài luyện, Cài đặt (+ logo, ẩn trên điện thoại): mọi mục đúng một icon.
  // Không đếm cứng số mục (epic mới cắm thêm mục rail); các mục đã biết được kiểm đích danh ở vòng lặp dưới.
  await expectEveryRailLinkHasOneIcon(page, 'hs-rail');
  await expect(rail.locator('a:not(.logo) .ms').first()).toBeVisible();
  await expect(page.getByTestId('hs-nav-settings')).toHaveText('Cài đặt');
  await expect(page.getByTestId('s2e8-nav-bai-luyen-hs')).toHaveText('Bài luyện');
  expect((await iconFontLoaded(page)).loaded).toBe(true);
  for (const id of ['hs-nav-overview', 'e1-nav-bai-cua-em', 'e2-nav-tu-hoc', 'e4-nav-lich-su', 's2e8-nav-bai-luyen-hs', 'hs-nav-settings']) {
    const f = await facts(page, `[data-testid="${id}"] .ms`);
    expect(f.hidden, `${id}: aria-hidden`).toBe(true);
    expect(f.fits, `${id}: glyph vừa ô`).toBe(true);
    expect(f.color, `${id}: màu theo khối chứa`).toBe(f.parentColor);
    expect(f.ratio, `${id}: tương phản ${f.ratio}:1`).toBeGreaterThanOrEqual(4.5);
  }
  if (await page.evaluate(() => matchMedia('(pointer: coarse)').matches)) {
    expect((await page.getByTestId('hs-nav-settings').boundingBox())!.height).toBeGreaterThanOrEqual(44);
  }
  // không tràn ngang sau khi thêm icon vào thanh điều hướng
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth + 1)).toBe(true);
});
