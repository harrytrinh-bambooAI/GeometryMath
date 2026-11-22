import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { MSG, addMidpointSD, drag, openWorkspace, snap, tapAt, tapEdge, tapFace, tapPoint, tid, toast, tool } from './e3.ts';

/** US-GEO-302 (chạm chọn, tô màu) và US-GEO-303 (vẽ đoạn, mặt phẳng, giao điểm, giao tuyến, mở rộng) trên HKG-01. */

const seg = async (page: Page, a: string, b: string) =>
  (await snap(page)).edges.find((e) => e.kind === 'segment' && [...e.ends].sort().join('') === [a, b].sort().join(''));
const userPoints = async (page: Page) => (await snap(page)).points.filter((p) => p.user);
const near = (a: number[], b: number[], eps = 1e-6) => a.every((v, i) => Math.abs(v - b[i]!) <= eps);

async function drawSegment(page: Page, a: string, b: string) {
  await tapPoint(page, a);
  await tapPoint(page, b);
  await expect.poll(async () => Boolean(await seg(page, a, b))).toBe(true);
}

test('Chạm chọn một cạnh @TC-GEO-0310', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapEdge(page, 'SD');
  const s = await snap(page);
  expect(s.selection).toMatchObject({ kind: 'edge', id: 'SD', label: 'Cạnh SD' });
  await expect(page.getByTestId('viewer-selection')).toHaveText('Cạnh SD');
  const line = page.getByTestId('viewer-line-SD');
  await expect(line).toHaveAttribute('data-selected', 'true');
  const width = (id: string) => page.getByTestId(`viewer-line-${id}`).locator('line').first().evaluate((el) => parseFloat(getComputedStyle(el).strokeWidth));
  expect(await width('SD')).toBeGreaterThan(await width('SC')); // dày hơn các cạnh khác
  const color = (id: string) => page.getByTestId(`viewer-line-${id}`).locator('line').first().evaluate((el) => getComputedStyle(el).stroke);
  expect(await color('SD')).not.toBe(await color('SC')); // màu nổi bật
});

test('Chạm chọn một mặt @TC-GEO-0311', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapFace(page, 'SAD');
  expect((await snap(page)).selection).toMatchObject({ kind: 'face', id: 'SAD', label: 'Mặt SAD' });
  await expect(page.getByTestId('viewer-selection')).toHaveText('Mặt SAD');
  const face = page.getByTestId('viewer-face-SAD');
  const op = await face.evaluate((el) => parseFloat(getComputedStyle(el).fillOpacity));
  expect(op).toBeGreaterThan(0.1); // bán trong suốt
  expect(op).toBeLessThan(1);
});

test('Chạm chọn một điểm @TC-GEO-0312', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapPoint(page, 'A');
  expect((await snap(page)).selection).toMatchObject({ kind: 'point', id: 'A', label: 'Điểm A' });
  await expect(page.getByTestId('viewer-selection')).toHaveText('Điểm A');
  await expect(page.getByTestId('viewer-point-A')).toHaveClass(/gv-selected/);
});

test('Chạm ra ngoài hình @TC-GEO-0315', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapEdge(page, 'SD');
  expect((await snap(page)).selection?.id).toBe('SD');
  const ops = (await snap(page)).opCount;
  const v = (await snap(page)).viewport;
  await tapAt(page, v.left + 30, v.top + v.height * 0.45);
  const s = await snap(page);
  expect(s.selection).toBeNull();
  expect(s.opCount).toBe(ops); // không ghi thêm thao tác
  await expect(page.getByTestId('viewer-selection')).toHaveCount(0);
});

test('Vẽ đoạn nối hai điểm @TC-GEO-0316', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'line');
  await drawSegment(page, 'A', 'C');
  const ac = (await seg(page, 'A', 'C'))!;
  expect(ac.from).toEqual([0, 0, 0]);
  expect(ac.to).toEqual([1, 1, 0]); // nằm trên mặt đáy
  expect(ac.style).toBe('dashed'); // sau các mặt bên ở góc nhìn lúc mới mở
});

test('Giao điểm của hai đường cắt nhau có tên @TC-GEO-0317', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'line');
  await drawSegment(page, 'A', 'C');
  expect(await userPoints(page)).toHaveLength(0);
  await drawSegment(page, 'B', 'D');
  const pts = await userPoints(page);
  expect(pts).toHaveLength(1);
  expect(pts[0]!.name).toBe('M');
  expect(near(pts[0]!.pos, [0.5, 0.5, 0])).toBe(true);
  await drawSegment(page, 'M', 'S'); // điểm mới làm đầu mút cho đoạn tiếp theo
});

test('Dựng mặt phẳng qua ba điểm @TC-GEO-0318', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'plane');
  for (const n of ['S', 'B', 'D']) await tapPoint(page, n);
  const s = await snap(page);
  expect(s.planes).toHaveLength(1);
  expect([...s.planes[0]!.through].sort()).toEqual(['B', 'D', 'S']);
  const f = s.faces.find((x) => x.kind === 'plane')!;
  expect(f.label).toContain('SBD');
  const op = await page.getByTestId(tid('viewer', 'face', f.id)).evaluate((el) => parseFloat(getComputedStyle(el).fillOpacity));
  expect(op).toBeGreaterThan(0);
  expect(op).toBeLessThan(1);
});

test('Giao tuyến của mặt phẳng mới với mặt của hình @TC-GEO-0319', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'plane');
  for (const n of ['S', 'B', 'D']) await tapPoint(page, n);
  const s = await snap(page);
  const tr = s.traces.find((t) => t.face === 'ABCD');
  expect(tr, 'giao tuyến với (ABCD)').toBeTruthy();
  const b = s.points.find((p) => p.name === 'B')!.screen;
  const d = s.points.find((p) => p.name === 'D')!.screen;
  const ends = tr!.screen.map((p) => p.map((v) => Math.round(v)).join(',')).sort();
  expect(ends).toEqual([b, d].map((p) => p.map((v) => Math.round(v)).join(',')).sort()); // đoạn BD
  await expect(page.getByTestId(tid('viewer', 'trace', tr!.id))).toBeVisible();
});

test('Mở rộng một mặt @TC-GEO-0320', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'extend');
  await tapFace(page, 'ABCD');
  const s = await snap(page);
  expect(s.extensions).toEqual([expect.objectContaining({ kind: 'face', target: 'ABCD' })]);
  const ext = s.faces.find((f) => f.kind === 'extended')!;
  const base = s.faces.find((f) => f.id === 'ABCD')!;
  expect(ext.area).toBeGreaterThan(base.area * 1.5); // rộng ra ngoài bốn cạnh
  const op = await page.getByTestId(tid('viewer', 'face', ext.id)).evaluate((el) => parseFloat(getComputedStyle(el).fillOpacity));
  expect(op).toBeLessThan(1);
});

test('Kéo dài một cạnh @TC-GEO-0321', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'extend');
  await tapEdge(page, 'SD');
  const s = await snap(page);
  expect(s.extensions).toEqual([expect.objectContaining({ kind: 'edge', target: 'SD' })]);
  const ext = s.edges.find((e) => e.kind === 'extended')!;
  // S = (0,0,1), D = (0,1,0): đường SD là (0, t, 1 − t); phần kéo dài vượt cả t < 0 (qua S) và t > 1 (qua D)
  const ts = [ext.from[1]!, ext.to[1]!].sort((a, b) => a - b);
  expect(ts[0]!).toBeLessThan(0);
  expect(ts[1]!).toBeGreaterThan(1);
  for (const p of [ext.from, ext.to]) expect(Math.abs(p[0]!) + Math.abs(p[1]! + p[2]! - 1)).toBeLessThan(1e-6);
  expect(s.edges.find((e) => e.id === 'SD')!.style).toBe('solid'); // đoạn gốc vẫn nét liền
});

test('Hai đường chéo nhau @TC-GEO-0322', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'extend');
  await tapEdge(page, 'SB');
  await tapEdge(page, 'AD');
  expect((await snap(page)).extensions.map((e) => e.target).sort()).toEqual(['AD', 'SB']);
  await tool(page, 'line');
  await tapEdge(page, 'SB', 0.3);
  await tapEdge(page, 'AD', 0.3);
  await expect(toast(page)).toHaveText(MSG['23']);
  await expect(toast(page)).toHaveAttribute('data-code', 'MSG-GEO-23');
  expect(await userPoints(page)).toHaveLength(0);
});

test('Ba điểm thẳng hàng @TC-GEO-0323', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await tool(page, 'plane');
  for (const n of ['S', 'M', 'D']) await tapPoint(page, n);
  await expect(toast(page)).toHaveText(MSG['25']);
  expect((await snap(page)).planes).toHaveLength(0);
});

test('Không có điều khiển kéo mặt phẳng @TC-GEO-0324', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'plane');
  for (const n of ['S', 'B', 'D']) await tapPoint(page, n);
  const before = await snap(page);
  const pf = before.faces.find((f) => f.kind === 'plane')!;
  // chạm giữ rồi kéo từ giữa mặt phẳng
  const [x, y] = [pf.screen.reduce((a, p) => a + p[0]!, 0) / pf.screen.length, pf.screen.reduce((a, p) => a + p[1]!, 0) / pf.screen.length];
  await page.mouse.move(x, y);
  await page.mouse.down();
  await page.waitForTimeout(600);
  for (let i = 1; i <= 10; i++) await page.mouse.move(x - i * 12, y);
  await page.mouse.up();
  const after = await snap(page);
  expect(after.planes[0]!.polygon).toEqual(before.planes[0]!.polygon); // mặt phẳng không dịch chuyển
  expect(after.camera.yaw).not.toBe(before.camera.yaw); // hình chỉ xoay như bình thường
  await expect(page.locator('[data-testid*="handle"]')).toHaveCount(0);
  await drag(page, 0); // không lỗi khi kéo tiếp
});
