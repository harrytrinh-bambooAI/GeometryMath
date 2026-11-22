import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { MSG, addMidpointSD, openWorkspace, snap, tapEdge, tapPoint, tool } from './e3.ts';

/** US-GEO-304 — Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh (HKG-01, F-GEO-076). */

const userPoints = async (page: Page) => (await snap(page)).points.filter((p) => p.user);
const near = (a: number[], b: number[], eps = 1e-6) => a.every((v, i) => Math.abs(v - b[i]!) <= eps);

async function ratioOnSD(page: Page, m: string, n: string) {
  await tool(page, 'point');
  await tapEdge(page, 'SD');
  await page.getByTestId('viewer-point-ratio').click();
  await page.getByTestId('viewer-ratio-from').selectOption('S');
  await page.getByTestId('viewer-ratio-m').fill(m);
  await page.getByTestId('viewer-ratio-n').fill(n);
  await page.getByTestId('viewer-ratio-ok').click();
}

test('Thêm trung điểm @TC-GEO-0325', async ({ page, request }) => {
  await openWorkspace(page, request);
  expect(await userPoints(page)).toHaveLength(0);
  await addMidpointSD(page);
  const pts = await userPoints(page);
  expect(pts).toHaveLength(1);
  expect(pts[0]!.name).toBe('M');
  expect(near(pts[0]!.pos, [0, 0.5, 0.5])).toBe(true);
  await expect(page.getByTestId('viewer-label-M')).toHaveText('M');
});

test('Thêm điểm chia tỉ lệ @TC-GEO-0326', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await ratioOnSD(page, '1', '2');
  await expect.poll(async () => (await userPoints(page)).map((p) => p.name)).toEqual(['M', 'N']);
  const n = (await userPoints(page)).find((p) => p.name === 'N')!;
  expect(near(n.pos, [0, 1 / 3, 2 / 3], 1e-5)).toBe(true);
  const s = [0, 0, 1];
  const d = [0, 1, 0];
  const len = (a: number[], b: number[]) => Math.hypot(a[0]! - b[0]!, a[1]! - b[1]!, a[2]! - b[2]!);
  expect(len(s, n.pos)).toBeCloseTo(len(n.pos, d) / 2, 5); // SN = ND / 2
});

test('Thêm điểm tự do trên cạnh @TC-GEO-0327', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'point');
  await tapEdge(page, 'SC', 0.37);
  await page.getByTestId('viewer-point-free').click();
  await expect.poll(async () => (await userPoints(page)).length).toBe(1);
  const p = (await userPoints(page))[0]!;
  expect(p.name).toMatch(/^[A-Z]/);
  // SC: S = (0,0,1), C = (1,1,0) ⇒ (t, t, 1 − t) với 0 ≤ t ≤ 1
  const t = p.pos[0]!;
  expect(Math.abs(p.pos[1]! - t)).toBeLessThan(1e-6);
  expect(Math.abs(p.pos[2]! - (1 - t))).toBeLessThan(1e-6);
  expect(t).toBeGreaterThan(0.2);
  expect(t).toBeLessThan(0.55); // gần chỗ chạm (37 % chiều dài trên màn)
});

test('Điểm mới dùng được cho công cụ khác @TC-GEO-0328', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await tool(page, 'line');
  await tapPoint(page, 'A');
  await tapPoint(page, 'M');
  await expect
    .poll(async () => (await snap(page)).edges.some((e) => e.kind === 'segment' && [...e.ends].sort().join('') === 'AM'))
    .toBe(true);
});

test('Tỉ lệ không hợp lệ @TC-GEO-0329', async ({ page, request }) => {
  await openWorkspace(page, request);
  for (const m of ['0', '-1']) {
    await ratioOnSD(page, m, '2');
    await expect(page.getByTestId('viewer-ratio-error')).toHaveText(MSG['19']);
    expect(await userPoints(page)).toHaveLength(0);
    await page.getByTestId('viewer-point-close').click();
    await tool(page, 'point'); // tắt công cụ, lần sau bật lại
  }
});

test('Điểm trùng điểm đã có @TC-GEO-0330', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await tool(page, 'point');
  await tool(page, 'point');
  await tapEdge(page, 'SD', 0.3);
  await page.getByTestId('viewer-point-mid').click();
  await expect(page.getByText(MSG['26'])).toBeVisible();
  expect((await userPoints(page)).map((p) => p.name)).toEqual(['M']);
});
