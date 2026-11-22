import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { addMidpointSD, openWorkspace, snap, tapEdge, tapPoint, tid, tool } from './e3.ts';

/** US-GEO-306 — Thao tác trên iPhone màn dọc (dự án ui-iphone: WebKit iPhone 13, F-GEO-101; BG-1, học sinh An). */

const MAIN = ['select', 'point', 'line', 'plane', 'angle', 'reset'];
const EXTRA = ['extend', 'length', 'axes', 'clear'];
const visibleTools = async (page: Page) => {
  const out: string[] = [];
  for (const b of await page.locator('[data-testid^="viewer-tool-"]').all()) {
    if (await b.isVisible()) out.push((await b.getAttribute('data-testid'))!.replace('viewer-tool-', ''));
  }
  return out;
};
const box = async (page: Page, testId: string) => (await page.getByTestId(testId).boundingBox())!;

test('Bố cục điện thoại @TC-GEO-0341', async ({ page, request }) => {
  await openWorkspace(page, request);
  const canvas = await box(page, 'viewer-canvas');
  const toolbar = await box(page, 'viewer-toolbar');
  const tabs = await Promise.all(['hint', 'solution', 'ask'].map((t) => box(page, `e3-tab-${t}`)));
  expect(canvas.y + canvas.height).toBeLessThanOrEqual(toolbar.y + 1); // hình ở trên, thanh công cụ ngay dưới hình
  expect(toolbar.y - (canvas.y + canvas.height)).toBeLessThan(30);
  for (const t of tabs) expect(t.y).toBeGreaterThanOrEqual(toolbar.y + toolbar.height - 1); // bảng dưới cùng
  await expect(page.getByRole('tab')).toHaveText(['Gợi ý', 'Lời giải', 'Hỏi']);
  const rowY = await Promise.all([...MAIN, 'more'].map(async (t) => Math.round((await box(page, `viewer-tool-${t}`)).y)));
  expect(new Set(rowY).size, 'sáu nút chính và "Thêm" trên một hàng').toBe(1);
  const { sw, cw } = await page.evaluate(() => ({ sw: document.documentElement.scrollWidth, cw: document.documentElement.clientWidth }));
  expect(sw).toBeLessThanOrEqual(cw); // không phải cuộn ngang
});

test('Sáu nút chính và nút "Thêm" trên iPhone @TC-GEO-0345', async ({ page, request }) => {
  await openWorkspace(page, request);
  expect(await visibleTools(page)).toEqual([...MAIN, 'more']);
  await page.getByTestId('viewer-tool-more').click();
  expect((await visibleTools(page)).sort()).toEqual([...MAIN, ...EXTRA, 'more'].sort());
  // chín công cụ và "Xóa hết" đều dùng được, kết quả như trên iPad
  await tool(page, 'select');
  await tapPoint(page, 'A');
  expect((await snap(page)).selection?.label).toBe('Điểm A');
  await addMidpointSD(page);
  await tool(page, 'line');
  await tapPoint(page, 'A');
  await tapPoint(page, 'C');
  await tool(page, 'plane');
  for (const n of ['S', 'B', 'D']) await tapPoint(page, n);
  await tool(page, 'extend');
  await tapEdge(page, 'SC');
  await tool(page, 'length');
  await tapPoint(page, 'S');
  await tapPoint(page, 'B');
  await tool(page, 'angle');
  for (const n of ['S', 'A', 'D']) await tapPoint(page, n);
  await tool(page, 'axes');
  const s = await snap(page);
  expect(s.points.some((p) => p.name === 'M')).toBe(true);
  expect(s.edges.some((e) => e.id === 'seg:AC')).toBe(true);
  expect(s.planes).toHaveLength(1);
  expect(s.extensions).toEqual([expect.objectContaining({ kind: 'edge', target: 'SC' })]);
  expect(s.measures.map((m) => `${m.label} = ${m.text}`)).toEqual(['SB = a√2', 'góc SAD = 90°']);
  expect(s.axes).toHaveLength(3);
  await page.getByTestId('viewer-view-top').click();
  await tool(page, 'reset');
  expect((await snap(page)).camera).toEqual({ yaw: 30, pitch: 20, zoom: 1, panX: 0, panY: 0 });
  await tool(page, 'clear');
  const c = await snap(page);
  expect(c.points.filter((p) => p.user)).toHaveLength(0);
  expect(c.edges.filter((e) => e.kind !== 'edge')).toHaveLength(0);
  expect(c.planes).toHaveLength(0);
  expect(c.measures).toHaveLength(0);
});

test('Xoay ngang máy giữa chừng @TC-GEO-0346', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await page.getByTestId('e3-tab-hint').click();
  await page.getByTestId('e3-hint-next').click();
  await expect(page.getByTestId('e3-hint-1')).toBeVisible();
  await page.evaluate(() => ((window as unknown as { __noReload: boolean }).__noReload = true));
  const portrait = page.viewportSize()!;
  await page.setViewportSize({ width: portrait.height, height: portrait.width }); // xoay ngang
  await expect.poll(async () => (await snap(page)).viewport.width).toBeGreaterThan(portrait.width * 0.5);
  expect((await snap(page)).points.some((p) => p.name === 'M')).toBe(true);
  await page.setViewportSize(portrait); // xoay lại dọc
  await expect(page.getByTestId('e3-tab-hint')).toHaveAttribute('aria-selected', 'true');
  await expect(page.getByTestId('e3-hint-1')).toBeVisible();
  expect((await snap(page)).points.some((p) => p.name === 'M')).toBe(true);
  expect(await page.evaluate(() => (window as unknown as { __noReload?: boolean }).__noReload)).toBe(true); // không tải lại
});

test('Vùng chạm đủ lớn @TC-GEO-0351', async ({ page, request }) => {
  await openWorkspace(page, request);
  const ids = [...(await visibleTools(page)).map((t) => `viewer-tool-${t}`), tid('e3', 'tab', 'hint'), tid('e3', 'tab', 'solution'), tid('e3', 'tab', 'ask')];
  expect(ids).toHaveLength(10);
  for (const id of ids) {
    const b = await box(page, id);
    expect(b.width, `${id} rộng`).toBeGreaterThanOrEqual(44);
    expect(b.height, `${id} cao`).toBeGreaterThanOrEqual(44);
  }
});
