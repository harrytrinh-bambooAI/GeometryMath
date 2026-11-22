import type { Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';
import { BG, MSG, openWorkspace, pinch, snap, tapEdge, tapFace, tapPoint, tid, toast, tool, waitScene } from './e3.ts';

/**
 * US-GEO-307 — Sửa lỗi hình đã biết (BR-GEO-162…164). Vùng tô kiểm bằng hình học của chính phần tử SVG đã vẽ
 * (SVGGeometryElement.isPointInFill tại điểm mẫu trong/ngoài mặt), kèm ảnh chụp khung hình làm bằng chứng.
 */

type P = [number, number];
const lerp = (a: P, b: P, t: number): P => [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t];

/** Toạ độ trong khung SVG (móc trả toạ độ trang). */
async function local(page: Page, names: string[]): Promise<Record<string, P>> {
  const s = await snap(page);
  return Object.fromEntries(
    names.map((n) => {
      const p = s.points.find((q) => q.name === n)!.screen;
      return [n, [p[0]! - s.viewport.left, p[1]! - s.viewport.top] as P];
    }),
  );
}
async function inFill(page: Page, faceId: string, pts: P[]): Promise<boolean[]> {
  return page.getByTestId(tid('viewer', 'face', faceId)).evaluate((el, ps) => {
    const g = el as SVGGeometryElement;
    const svg = g.ownerSVGElement!;
    return ps.map(([x, y]) => {
      const p = svg.createSVGPoint();
      p.x = x;
      p.y = y;
      return g.isPointInFill(p);
    });
  }, pts);
}
async function evidence(page: Page, testInfo: TestInfo, name: string) {
  await testInfo.attach(name, { body: await page.getByTestId('viewer-canvas').screenshot(), contentType: 'image/png' });
}

test('Tên mặt không lặp chữ @TC-GEO-0347', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapFace(page, 'SAB');
  await expect(page.getByTestId('viewer-selection')).toHaveText('Mặt SAB');
  expect(await page.locator('body').innerText()).not.toContain('Mặt Mặt');
});

test('Mặt bốn đỉnh tô kín @TC-GEO-0348', async ({ page, request }, testInfo) => {
  await openWorkspace(page, request);
  await tool(page, 'select');
  await tapFace(page, 'ABCD');
  expect((await snap(page)).selection?.id).toBe('ABCD');
  const { A, B, C, D } = await local(page, ['A', 'B', 'C', 'D']);
  const quad = [A!, B!, C!, D!];
  const center: P = [(A![0] + B![0] + C![0] + D![0]) / 4, (A![1] + B![1] + C![1] + D![1]) / 4];
  const inside: P[] = [center, ...quad.map((v) => lerp(v, center, 0.12))];
  const outside: P[] = [];
  for (let i = 0; i < 4; i++) {
    const mid = lerp(quad[i]!, quad[(i + 1) % 4]!, 0.5);
    inside.push(lerp(mid, center, 0.08), lerp(lerp(quad[i]!, quad[(i + 1) % 4]!, 0.2), center, 0.08)); // sát mỗi cạnh: không hở
    outside.push(lerp(mid, center, -0.12)); // ngoài mỗi cạnh: không tràn
    outside.push(lerp(quad[i]!, center, -0.12));
  }
  expect(await inFill(page, 'ABCD', inside)).toEqual(inside.map(() => true));
  expect(await inFill(page, 'ABCD', outside)).toEqual(outside.map(() => false));
  await evidence(page, testInfo, 'mat-day-ABCD');
});

test('Hai đường chéo nhau trên hình lập phương @TC-GEO-0349', async ({ page, request }) => {
  await openWorkspace(page, request, BG('BG-2'));
  await tool(page, 'line');
  for (const [a, b] of [['A', 'C'], ['B′', 'D′']]) {
    await tapPoint(page, a!);
    await tapPoint(page, b!);
  }
  await expect.poll(async () => (await snap(page)).edges.filter((e) => e.kind === 'segment').length).toBe(2);
  const before = (await snap(page)).points.length;
  await tapEdge(page, 'seg:AC', 0.3);
  await tapEdge(page, 'seg:B′D′', 0.3);
  await expect(toast(page)).toHaveText(MSG['23']);
  expect((await snap(page)).points).toHaveLength(before); // không có điểm nào ở khoảng giữa hai đường
});

test('Mặt lõm @TC-GEO-0350', async ({ page, request }, testInfo) => {
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await page.goto('/hs/thu-hinh/lang-tru-l');
  await expect(page.getByTestId('e3-sandbox')).toBeVisible();
  await waitScene(page);
  const base = (await snap(page)).faces.find((f) => f.kind === 'face' && !f.id.includes('′') && f.id.length === 6)!;
  expect(base, 'mặt đáy chữ L').toBeTruthy();
  expect(base.area).toBeCloseTo(3, 5); // diện tích hình chữ L
  await pinch(page, 100, 250); // phóng to để chỗ chạm giữa mặt cách xa đỉnh D (vùng chạm điểm 18 px)
  await tool(page, 'select');
  await tapFace(page, base.id);
  expect((await snap(page)).selection?.id).toBe(base.id);
  // Đáy A(0,0) B(2,0) … F(0,2): ảnh chiếu song song giữ tỉ lệ nên điểm (u, v) = A + u/2·AB + v/2·AF
  const { A, B, F } = await local(page, ['A', 'B', 'F']);
  const at = (u: number, v: number): P => [
    A![0] + (u / 2) * (B![0] - A![0]) + (v / 2) * (F![0] - A![0]),
    A![1] + (u / 2) * (B![1] - A![1]) + (v / 2) * (F![1] - A![1]),
  ];
  const inside = [at(0.5, 0.5), at(1.5, 0.5), at(0.5, 1.5), at(1.8, 0.8), at(0.8, 1.8)];
  const notch = [at(1.5, 1.5), at(1.2, 1.2), at(1.8, 1.8), at(1.9, 1.1), at(1.1, 1.9)]; // ô lõm (1,1)…(2,2)
  expect(await inFill(page, base.id, inside)).toEqual(inside.map(() => true));
  expect(await inFill(page, base.id, notch)).toEqual(notch.map(() => false));
  await evidence(page, testInfo, 'day-chu-L');
});
