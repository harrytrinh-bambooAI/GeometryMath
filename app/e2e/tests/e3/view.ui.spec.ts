import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';
import { PV, addMidpointSD, angDiff, drag, pinch, openWorkspace, snap, tapPoint, tool, waitScene } from './e3.ts';

/** US-GEO-301 — Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz (HKG-01, BG-1, học sinh An). */

const INITIAL = { yaw: 30, pitch: 20, zoom: 1, panX: 0, panY: 0 };
const dist = (a: number[], b: number[]) => Math.hypot(a[0]! - b[0]!, a[1]! - b[1]!);
const pt = async (page: import('@playwright/test').Page, n: string) => (await snap(page)).points.find((p) => p.name === n)!.screen;
const wheel = async (page: import('@playwright/test').Page, dy: number, times = 1) => {
  const v = (await snap(page)).viewport;
  await page.mouse.move(v.left + v.width / 2, v.top + v.height / 2);
  for (let i = 0; i < times; i++) await page.mouse.wheel(0, dy);
};

test('Kéo để xoay @TC-GEO-0300', async ({ page, request }) => {
  await openWorkspace(page, request);
  const before = await snap(page);
  expect(before.camera).toEqual(INITIAL);
  await drag(page, -200);
  const after = await snap(page);
  expect(after.camera.yaw).not.toBe(before.camera.yaw); // quay quanh trục thẳng đứng
  expect(after.camera.pitch).toBe(before.camera.pitch);
  await page.waitForTimeout(400);
  expect((await snap(page)).camera).toEqual(after.camera); // dừng khi nhấc ngón
  // nhãn S, A, B, C, D đi theo đỉnh
  for (const n of ['S', 'A', 'B', 'C', 'D']) {
    const box = (await page.getByTestId(`viewer-label-${n}`).boundingBox())!;
    const p = after.points.find((q) => q.name === n)!.screen;
    expect(dist([box.x + box.width / 2, box.y + box.height / 2], p)).toBeLessThan(30);
  }
  expect(after.opCount).toBe(0); // xoay không là thao tác trên hình
});

test('Lăn chuột để thu phóng trên máy tính @TC-GEO-0302', async ({ page, request }, testInfo) => {
  test.skip(testInfo.project.name !== 'ui-chromium', 'ca máy tính có chuột (F-GEO-102); iPad không có con lăn');
  await signInPage(page, request, 'F-GEO-003');
  await page.goto(`/gv/de/${PV('HKG-01')}/hinh`);
  await expect(page.getByTestId('e3-teacher-figure')).toBeVisible();
  await waitScene(page);
  const y0 = await page.evaluate(() => window.scrollY);
  const d0 = dist(await pt(page, 'S'), await pt(page, 'A'));
  await wheel(page, -240);
  await expect.poll(async () => (await snap(page)).camera.zoom).toBeGreaterThan(1);
  expect(dist(await pt(page, 'S'), await pt(page, 'A'))).toBeGreaterThan(d0); // hình to lên
  expect(await page.evaluate(() => window.scrollY)).toBe(y0); // trang không cuộn
});

test('"Nhìn từ trên" @TC-GEO-0303', async ({ page, request }) => {
  await openWorkspace(page, request);
  await drag(page, -150, 40);
  await page.getByTestId('viewer-view-top').click();
  const [s, a, b, c, d] = await Promise.all(['S', 'A', 'B', 'C', 'D'].map((n) => pt(page, n)));
  expect(dist(s!, a!)).toBeLessThan(1.5); // S trùng vị trí A
  const ab = dist(a!, b!);
  for (const [p, q] of [[b, c], [c, d], [d, a]] as const) expect(Math.abs(dist(p!, q!) - ab)).toBeLessThan(1.5);
  expect(Math.abs(dist(a!, c!) - dist(b!, d!))).toBeLessThan(1.5); // hai đường chéo bằng nhau ⇒ hình vuông
});

test('"Nhìn từ trước" @TC-GEO-0304', async ({ page, request }) => {
  await openWorkspace(page, request);
  await drag(page, 120, -30);
  await page.getByTestId('viewer-view-front').click();
  const [s, a, b, c, d] = await Promise.all(['S', 'A', 'B', 'C', 'D'].map((n) => pt(page, n)));
  expect(Math.abs(s![0]! - a![0]!)).toBeLessThan(1); // SA thẳng đứng
  expect(s![1]!).toBeLessThan(a![1]! - 20);
  for (const p of [b, c, d]) expect(Math.abs(p![1]! - a![1]!)).toBeLessThan(1); // đáy là một đoạn nằm ngang
});

test('"Đặt lại" về góc nhìn ban đầu @TC-GEO-0305', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await tool(page, 'point'); // tắt công cụ để kéo là xoay
  await page.getByTestId('viewer-view-free').click();
  await drag(page, -160, 30);
  await pinch(page, 60, 180); // phóng to
  expect((await snap(page)).camera).not.toEqual(INITIAL);
  await page.getByTestId('viewer-tool-reset').click();
  const s = await snap(page);
  expect(s.camera).toEqual(INITIAL);
  expect(s.points.some((p) => p.name === 'M')).toBe(true);
});

test('Cạnh khuất vẽ nét đứt và tính lại khi xoay @TC-GEO-0306', async ({ page, request }) => {
  await openWorkspace(page, request);
  const styles = async () => Object.fromEntries((await snap(page)).edges.map((e) => [e.id, e.style]));
  const s0 = await styles();
  expect(s0).toEqual({ SA: 'dashed', AB: 'dashed', AD: 'dashed', SB: 'solid', SC: 'solid', SD: 'solid', BC: 'solid', CD: 'solid' });
  await drag(page, -450); // nửa vòng (0,4°/px)
  expect(angDiff((await snap(page)).camera.yaw, 210)).toBeLessThan(10);
  const s1 = await styles();
  expect(s1.BC).toBe('dashed'); // C ra phía sau: các cạnh tại C bị che
  expect(s1.CD).toBe('dashed');
  expect(s1.AB).toBe('solid'); // A ra phía trước
  expect(s1.AD).toBe('solid');
  await expect(page.locator('[data-testid="viewer-line-BC"]')).toHaveAttribute('data-style', 'dashed');
});

test('Bật, tắt trục Oxyz @TC-GEO-0307', async ({ page, request }) => {
  await openWorkspace(page, request);
  const p0 = (await snap(page)).points;
  expect((await snap(page)).axes).toHaveLength(0);
  await tool(page, 'axes');
  const s1 = await snap(page);
  expect(s1.axes.map((a) => a.axis).sort()).toEqual(['x', 'y', 'z']);
  for (const a of ['x', 'y', 'z']) await expect(page.getByTestId(`viewer-axis-${a}`)).toContainText(a);
  await tool(page, 'axes');
  const s2 = await snap(page);
  expect(s2.axes).toHaveLength(0);
  expect(s2.points).toEqual(p0); // hình không đổi
});

test('Thu phóng có giới hạn @TC-GEO-0308', async ({ page, request }) => {
  await openWorkspace(page, request);
  const visible = async () => {
    const s = await snap(page);
    const v = s.viewport;
    return s.points.some((p) => p.screen[0]! >= v.left && p.screen[0]! <= v.left + v.width && p.screen[1]! >= v.top && p.screen[1]! <= v.top + v.height);
  };
  await pinch(page, 400, 40); // chụm liên tục để thu nhỏ
  await pinch(page, 400, 40);
  const zMin = (await snap(page)).camera.zoom;
  await pinch(page, 400, 40);
  expect((await snap(page)).camera.zoom).toBe(zMin); // dừng ở mức nhỏ nhất
  expect(zMin).toBeGreaterThan(0);
  expect(await visible()).toBe(true);
  for (let i = 0; i < 3; i++) await pinch(page, 40, 400); // dang liên tục để phóng to
  const zMax = (await snap(page)).camera.zoom;
  await pinch(page, 40, 400);
  expect((await snap(page)).camera.zoom).toBe(zMax); // dừng ở mức lớn nhất
  expect(zMax).toBeGreaterThan(zMin);
  expect(await visible()).toBe(true);
  await page.getByTestId('viewer-tool-reset').click();
  expect((await snap(page)).camera).toEqual(INITIAL);
});

test('"Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn @TC-GEO-0353', async ({ page, request }) => {
  await openWorkspace(page, request);
  await addMidpointSD(page);
  await tool(page, 'line');
  await tapPoint(page, 'A');
  await tapPoint(page, 'C');
  await expect.poll(async () => (await snap(page)).edges.some((e) => e.id === 'seg:AC')).toBe(true);
  await page.getByTestId('viewer-view-free').click();
  await drag(page, -140, 20);
  const cam = (await snap(page)).camera;
  expect(cam).not.toEqual(INITIAL);
  await tool(page, 'clear');
  const s = await snap(page);
  expect(s.points.map((p) => p.name).sort()).toEqual(['A', 'B', 'C', 'D', 'S']);
  expect(s.edges.map((e) => e.id).sort()).toEqual(['AB', 'AD', 'BC', 'CD', 'SA', 'SB', 'SC', 'SD']);
  expect(s.camera).toEqual(cam);
});
