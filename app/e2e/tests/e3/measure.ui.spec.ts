import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { signInPage } from '../e1/e1.ts';
import {
  API,
  BG,
  MSG,
  bearer,
  devToken,
  e3Lane,
  gotoWorkspace,
  openNowE3,
  openWorkspace,
  signInE3Student,
  snap,
  tapEdge,
  tapFace,
  tapPoint,
  tid,
  toast,
  tool,
  waitScene,
} from './e3.ts';

/** US-GEO-305 — Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi khi lời giải khóa (BR-GEO-155…158). */

const lastMeasure = async (page: Page) => (await snap(page)).measures.at(-1)!;
const measureChip = async (page: Page) => page.getByTestId(tid('viewer', 'measure', (await lastMeasure(page)).id));

async function angle(page: Page, a: string, o: string, b: string) {
  await tool(page, 'angle');
  const n = (await snap(page)).measures.length;
  for (const x of [a, o, b]) await tapPoint(page, x);
  await expect.poll(async () => (await snap(page)).measures.length).toBe(n + 1);
}
async function expectHidden(page: Page) {
  const m = await lastMeasure(page);
  expect(m.hidden).toBe(true);
  await expect(toast(page)).toHaveText(MSG['24']);
  await expect(toast(page)).toHaveAttribute('data-code', 'MSG-GEO-24');
  await expect(await measureChip(page)).not.toContainText(/\d/); // không có số
}

test('Đo độ dài một yếu tố trung gian khi đang khóa @TC-GEO-0331', async ({ page, request }) => {
  await openWorkspace(page, request);
  expect((await snap(page)).lock).toBe('locked');
  await tool(page, 'length');
  await tapPoint(page, 'S');
  await tapPoint(page, 'D');
  await expect(await measureChip(page)).toHaveText('SD = a√2');
});

test('Đo một góc trung gian khi đang khóa @TC-GEO-0332', async ({ page, request }) => {
  await openWorkspace(page, request);
  await angle(page, 'S', 'A', 'D');
  await expect(await measureChip(page)).toHaveText('góc SAD = 90°');
});

test('Đo khoảng cách từ điểm tới mặt @TC-GEO-0333', async ({ page, request }) => {
  await openWorkspace(page, request);
  await tool(page, 'length');
  await tapPoint(page, 'S');
  await tapFace(page, 'ABCD');
  const m = await lastMeasure(page);
  await expect(await measureChip(page)).toHaveText('d(S, (ABCD)) = a');
  // đoạn vuông góc từ S xuống đáy trùng SA
  const line = page.getByTestId(tid('viewer', 'mdraw', m.id));
  const xy = await line.evaluate((el) => ['x1', 'y1', 'x2', 'y2'].map((k) => parseFloat(el.getAttribute(k)!)));
  const s = await snap(page);
  const loc = (n: string) => s.points.find((p) => p.name === n)!.screen.map((v, i) => v - (i === 0 ? s.viewport.left : s.viewport.top));
  const ends = [[xy[0]!, xy[1]!], [xy[2]!, xy[3]!]];
  for (const [p, q] of [[ends[0], loc('S')], [ends[1], loc('A')]] as const) expect(Math.hypot(p![0]! - q[0]!, p![1]! - q[1]!)).toBeLessThan(1.5);
});

test('Sau giờ mở, đại lượng đề hỏi đo được @TC-GEO-0334', async ({ page, request }, testInfo) => {
  const lane = e3Lane(testInfo, '0334');
  await openNowE3(request, lane.assignmentId); // Giáo viên bấm "Mở ngay" (bài HKG-01 của làn E3, như BG-1)
  await signInE3Student(page, request, lane.studentId);
  await gotoWorkspace(page, lane.assignmentId);
  await expect(page.getByTestId('e3-workspace')).toHaveAttribute('data-lock', 'open');
  await angle(page, 'S', 'D', 'A');
  await expect(await measureChip(page)).toHaveText('góc SDA = 45°');
});

test('Số đo dùng dấu phẩy @TC-GEO-0335', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-020', '11A1');
  await page.goto('/hs/hinh-mau/cone?r=2,5&h=4');
  await expect(page.getByTestId('e3-template-figure')).toBeVisible();
  await waitScene(page);
  await tool(page, 'length');
  await tapPoint(page, 'S');
  await tapPoint(page, 'M'); // đường sinh SM
  const chip = await measureChip(page);
  const text = (await chip.textContent())!;
  const value = text.split('=')[1]!.trim();
  expect(value.startsWith('4,7')).toBe(true); // √22,25 ≈ 4,717
  expect(value).not.toContain('.');
});

test('Đo đại lượng đề hỏi khi đang khóa @TC-GEO-0336', async ({ page, request }) => {
  await openWorkspace(page, request);
  await angle(page, 'S', 'D', 'A');
  await expectHidden(page);
  const m = await lastMeasure(page);
  await expect(page.getByTestId(tid('viewer', 'mdraw', m.id))).toBeVisible(); // cung góc tại D được vẽ
  expect(await page.locator('body').innerText()).not.toContain('45');
  expect(await page.content()).not.toMatch(/45°|45\s*độ/);
});

test('Đo đại lượng cùng vai trò khi đang khóa @TC-GEO-0337', async ({ page, request }) => {
  await openWorkspace(page, request);
  await angle(page, 'A', 'S', 'D');
  await expectHidden(page);
});

test('Bài Chưa đặt giờ cũng ẩn @TC-GEO-0338', async ({ page, request }) => {
  await openWorkspace(page, request, BG('BG-2'));
  expect((await snap(page)).lock).toBe('locked');
  await tool(page, 'line');
  for (const [a, b] of [['A', 'C'], ['B′', 'D′']]) {
    await tapPoint(page, a!);
    await tapPoint(page, b!);
  }
  await expect.poll(async () => (await snap(page)).edges.filter((e) => e.kind === 'segment').length).toBe(2);
  await tool(page, 'length');
  await tapEdge(page, 'seg:AC', 0.3);
  await tapEdge(page, 'seg:B′D′', 0.3);
  await expect.poll(async () => (await snap(page)).measures.length).toBe(1);
  expect((await lastMeasure(page)).kind).toBe('length');
  await expectHidden(page);
});

test('Bài tự học đang khóa cũng ẩn @TC-GEO-0339', async ({ page, request }) => {
  const token = await devToken(request, 'F-GEO-020');
  const bg1 = (await (await request.get(`${API}/api/v1/student/assignments/${BG('BG-1')}`, { headers: bearer(token) })).json()) as { statement: string };
  const r = await request.post(`${API}/api/v1/student/self-study`, { headers: bearer(token), data: { statement: bg1.statement } });
  expect(r.status(), await r.text()).toBe(200);
  const self = (await r.json()) as { assignmentId: string };
  await openWorkspace(page, request, self.assignmentId);
  await expect(page.getByTestId('e3-workspace')).toHaveAttribute('data-kind', 'self');
  await expect(page.getByTestId('e3-workspace')).toHaveAttribute('data-lock', 'locked');
  await angle(page, 'S', 'D', 'A');
  await expectHidden(page);
});

test('Bài chuyển Đã mở khi đang mở sẵn công cụ đo @TC-GEO-0352', async ({ page, request }, testInfo) => {
  const lane = e3Lane(testInfo, '0352');
  await signInE3Student(page, request, lane.studentId);
  await gotoWorkspace(page, lane.assignmentId);
  await angle(page, 'S', 'D', 'A');
  await expectHidden(page);
  await openNowE3(request, lane.assignmentId);
  const t0 = Date.now();
  await expect(page.getByTestId('e3-workspace')).toHaveAttribute('data-lock', 'open', { timeout: 10_000 });
  expect(Date.now() - t0).toBeLessThanOrEqual(10_000); // không muộn hơn 10 giây, không tải lại
  await angle(page, 'S', 'D', 'A');
  await expect(await measureChip(page)).toHaveText('góc SDA = 45°');
});
