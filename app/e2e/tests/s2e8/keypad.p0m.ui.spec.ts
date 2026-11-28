import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { answerBox, assigned, noHorizontalScroll, openPlay, pasteInto, sandbox, tap } from './s2e8.ts';

/**
 * S2-E8 · US-S2E8-01 bàn phím toán (C2-06, DQ-11). Tên tệp *.p0m.ui.spec.ts: chạy cả trên điện thoại (mobile-iphone,
 * mobile-pixel) ngoài ui-chromium / ui-webkit (iPad). Ô đáp số không phải thẻ input nên không bật bàn phím hệ thống.
 */

async function open(page: Page, request: Parameters<typeof sandbox>[0]) {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  await openPlay(page, sb, set.id);
  return { sb, set };
}
const clear = (page: Page) => page.getByTestId('s2e8-key-xoa-het').click();

test('@TC-S2E8-001 phím số chèn chữ số vào ô', async ({ page, request }) => {
  await open(page, request);
  await expect(answerBox(page)).toHaveText('');
  await tap(page, '45');
  await expect(answerBox(page)).toHaveText('45');
});

test('@TC-S2E8-002 phím căn và phân số chèn đúng ký hiệu', async ({ page, request }) => {
  await open(page, request);
  await tap(page, '4√3');
  await expect(answerBox(page)).toHaveText('4√3');
  await clear(page);
  await tap(page, '1/2');
  await expect(answerBox(page)).toHaveText('1/2');
});

test('@TC-S2E8-003 phím π, mũ và độ chèn đúng', async ({ page, request }) => {
  await open(page, request);
  for (const s of ['2π', '2^3', '45°']) {
    await clear(page);
    await tap(page, s);
    await expect(answerBox(page)).toHaveText(s);
  }
});

test('@TC-S2E8-004 dấu phẩy nhập số thập phân', async ({ page, request }) => {
  await open(page, request);
  await tap(page, '0,5');
  await expect(answerBox(page)).toHaveText('0,5');
});

test('@TC-S2E8-005 xóa lùi bỏ ký tự ngay trước con trỏ', async ({ page, request }) => {
  await open(page, request);
  await tap(page, '4√3');
  await tap(page, '⌫⌫');
  await expect(answerBox(page)).toHaveText('4');
});

test('@TC-S2E8-006 chèn giữa chuỗi theo vị trí con trỏ', async ({ page, request }) => {
  await open(page, request);
  await tap(page, '45');
  // chạm vào nửa trái của ký tự thứ hai: con trỏ nằm giữa hai chữ số
  await answerBox(page).locator('[data-i="1"]').click({ position: { x: 1, y: 6 } });
  await expect(answerBox(page)).toHaveAttribute('data-caret', '1');
  await tap(page, '°');
  await expect(answerBox(page)).toHaveText('4°5');
});

test('@TC-S2E8-007 Xóa hết làm trống ô', async ({ page, request }) => {
  await open(page, request);
  await tap(page, '4√3');
  await expect(page.getByTestId('s2e8-check')).toBeEnabled();
  await clear(page);
  await expect(answerBox(page)).toHaveText('');
  await expect(page.getByTestId('s2e8-check')).toBeDisabled();
});

test('@TC-S2E8-008 ô trống thì nút Kiểm tra khóa', async ({ page, request }) => {
  let sent = 0;
  await page.route('**/api/v1/student/practice-items/*/attempts', (r) => {
    sent++;
    return r.continue();
  });
  await open(page, request);
  const check = page.getByTestId('s2e8-check');
  await expect(check).toBeDisabled();
  await check.click({ force: true });
  await page.waitForTimeout(300);
  expect(sent, 'không gửi yêu cầu').toBe(0);
  await expect(page.getByTestId('s2e8-feedback')).toHaveCount(0);
});

test('@TC-S2E8-009 đủ 50 ký tự vẫn nhập được', async ({ page, request }) => {
  await open(page, request);
  await pasteInto(page, '1'.repeat(49));
  await expect(answerBox(page)).toHaveText('1'.repeat(49));
  await tap(page, '2');
  await expect(answerBox(page)).toHaveText('1'.repeat(49) + '2');
  await expect(page.getByTestId('s2e8-max')).toHaveCount(0);
});

test('@TC-S2E8-010 ký tự thứ 51 không được chèn', async ({ page, request }) => {
  await open(page, request);
  await pasteInto(page, '1'.repeat(50));
  await tap(page, '2');
  await expect(answerBox(page)).toHaveText('1'.repeat(50));
  await expect(page.getByTestId('s2e8-max')).toHaveText('Tối đa 50 ký tự');
  await noHorizontalScroll(page);
});

test('@TC-S2E8-012 chuỗi dán bị lọc theo bảng phím', async ({ page, request }) => {
  const dialogs: string[] = [];
  page.on('dialog', (d) => {
    dialogs.push(d.message());
    void d.dismiss();
  });
  await open(page, request);
  await pasteInto(page, '45 độ <b>');
  await expect(answerBox(page)).toHaveText('45');
  expect(dialogs).toEqual([]);
  await expect(page.getByTestId('s2e8-feedback')).toHaveCount(0);
});

test('S2-E8 bàn phím toán theme C: 6 cột cùng thứ tự, phím ≥ 44, không cuộn ngang, chữ toán dùng --font-math', async ({ page, request }, testInfo) => {
  // phần đo được bằng máy của TC-S2E8-013 (Manual: thiết bị thật) và BR-S2E8-01, 04
  await open(page, request);
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'c');
  await noHorizontalScroll(page);
  const keys = page.locator('[data-testid="s2e8-keypad"] button');
  await expect(keys).toHaveCount(27);
  const labels = await keys.allInnerTexts();
  expect(labels.slice(0, 24).map((s) => s.trim())).toEqual([
    '7', '8', '9', '(', ')', '⌫',
    '4', '5', '6', '+', '−', '√',
    '1', '2', '3', '×', '÷', 'π',
    '0', ',', '°', '/', '^', 'a',
  ]);
  expect(labels.slice(24).map((s) => s.trim())).toEqual(['x', 'Xóa hết', 'Kiểm tra']);
  const boxes = await keys.evaluateAll((els) => els.map((e) => e.getBoundingClientRect()).map((r) => ({ x: r.x, y: r.y, w: r.width, h: r.height })));
  for (const b of boxes) expect(Math.min(b.w, b.h), 'phím cao và rộng không dưới 44 điểm').toBeGreaterThanOrEqual(44);
  // hàng đầu có đúng 6 phím cùng một hàng (6 cột)
  expect(new Set(boxes.slice(0, 6).map((b) => Math.round(b.y))).size).toBe(1);
  expect(Math.round(boxes[6]!.y)).toBeGreaterThan(Math.round(boxes[0]!.y));
  const fonts = await page.evaluate(() => {
    const math = getComputedStyle(document.documentElement).getPropertyValue('--font-math').trim();
    const f = (tid: string) => getComputedStyle(document.querySelector(`[data-testid="${tid}"]`)!).fontFamily;
    return { math, answer: f('s2e8-answer'), key: f('s2e8-key-7'), statement: f('s2e8-statement') };
  });
  const norm = (s: string) => s.replace(/["'\s]/g, '');
  expect(fonts.math).not.toBe('');
  for (const f of [fonts.answer, fonts.key, fonts.statement]) expect(norm(f)).toBe(norm(fonts.math));
  // chữ phụ ≥ 14 px (PO-66 G17)
  const small = await page.evaluate(() =>
    [...document.querySelectorAll('[data-testid="s2e8-question"] *')]
      .filter((e) => e.children.length === 0 && (e.textContent ?? '').trim() !== '' && !e.closest('[data-testid="s2e8-figure"]'))
      .map((e) => parseFloat(getComputedStyle(e).fontSize))
      .filter((px) => px < 14),
  );
  expect(small, 'không có chữ dưới 14 px').toEqual([]);
  if (page.viewportSize()!.width < 820) {
    // điện thoại: ô đáp số và bàn phím cùng nằm trong màn khi cuộn tới bàn phím
    await page.getByTestId('s2e8-check').scrollIntoViewIfNeeded();
    await expect(answerBox(page)).toBeInViewport();
  }
  await testInfo.attach(`s2e8-ban-phim-${testInfo.project.name}.png`, { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
});
