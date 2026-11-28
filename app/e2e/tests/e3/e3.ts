import type { APIRequestContext, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, BG, PV, bearer, devToken, ids, signInPage } from '../e1/e1.ts';

/**
 * Tiện ích E2E của E3 (Thao tác hình 3D). Kiểm hình bằng móc chỉ đọc `window.__geoScene` (PO-52; bản e2e build với
 * VITE_GEO_TEST_HOOKS=1). Toạ độ màn hình trong móc là px theo khung nhìn (dùng thẳng cho chuột/chạm).
 * Dữ liệu riêng E3: app/db/seed/e2e/300_e3_core.sql (tổ chức "Nhóm thử E3", 4 lớp 11E3-k, học sinh "An", bài HKG-01
 * Sắp mở) — dùng cho ca bấm "Mở ngay" để không đổi BG-1 của các ca khác đang chạy song song.
 */
export { API, BG, PV, bearer, devToken };

export const MSG = {
  '19': 'Giá trị phải là số lớn hơn 0.',
  '23': 'Hai đường thẳng này chéo nhau nên không có giao điểm.',
  '24': 'Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở.',
  '25': 'Ba điểm thẳng hàng nên chưa xác định được mặt phẳng.',
  '26': 'Điểm này đã có trên hình.',
} as const;

const E3_ORG = '00000000-0000-7000-8000-000000e30001';
const E3_TEACHER = '00000000-0000-7000-8000-000000e30003';
const e3Id = (n: string) => `00000000-0000-7000-8000-000000e3${n}`;

/**
 * Làn dữ liệu E3 riêng (seed 300_e3: k = 1…c, hex): TC-0334 → 1 (chromium) / 2 (webkit); TC-0352 → 3 / 4;
 * TC-0309 → 5 / 6. Lượt chạy lại (retry) dùng làn +6 để không gặp bài đã "Mở ngay" hay đã có thao tác ở lượt trước
 * (soát E3 #5).
 */
export function e3Lane(testInfo: TestInfo, tc: '0334' | '0352' | '0309') {
  const base = { '0334': 1, '0352': 3, '0309': 5 }[tc];
  const k = base + (testInfo.project.name === 'ui-chromium' ? 0 : 1) + (testInfo.retry > 0 ? 6 : 0);
  const h = k.toString(16);
  return { assignmentId: e3Id(`004${h}`), studentId: e3Id(`002${h}`), classId: e3Id(`001${h}`), k };
}

async function tokenRaw(request: APIRequestContext, orgId: string, userId: string, role: string) {
  const r = await request.post(`${API}/dev/token`, { data: { orgId, userId, role } });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { token: string }).token;
}

/** Giáo viên E3 bấm "Mở ngay" (POST release openNow) cho bài của làn E3; rev đọc từ danh sách bài của lớp (khóa lạc quan). */
export async function openNowE3(request: APIRequestContext, assignmentId: string) {
  const t = await tokenRaw(request, E3_ORG, E3_TEACHER, 'TEACHER');
  const classId = e3Id(`001${assignmentId.slice(-1)}`);
  const list = await request.get(`${API}/api/v1/teacher/classes/${classId}/assignments`, { headers: { Authorization: `Bearer ${t}` } });
  expect(list.status(), await list.text()).toBe(200);
  const items = ((await list.json()) as { items: Array<{ id: string; releaseRev: number }> }).items;
  const expectedRev = items.find((a) => a.id === assignmentId)?.releaseRev;
  expect(expectedRev, `releaseRev của ${assignmentId}`).toBeDefined();
  const r = await request.post(`${API}/api/v1/teacher/assignments/${assignmentId}/release`, {
    headers: { Authorization: `Bearer ${t}` },
    data: { openNow: true, expectedRev },
  });
  expect(r.status(), await r.text()).toBe(200);
}

/** Tiến độ của học sinh làn E3 trên Theo dõi lớp (giáo viên E3; đệm sống 5 giây). */
export async function laneProgressE3(request: APIRequestContext, assignmentId: string) {
  const t = await tokenRaw(request, E3_ORG, E3_TEACHER, 'TEACHER');
  const r = await request.get(`${API}/api/v1/teacher/assignments/${assignmentId}/monitor`, { headers: { Authorization: `Bearer ${t}` } });
  expect(r.status(), await r.text()).toBe(200);
  const m = (await r.json()) as { students: Array<{ opened: boolean; toolCount: number }> };
  return m.students[0]!;
}

/** Đăng nhập học sinh E3 (làn riêng) trên trình duyệt. */
export async function signInE3Student(page: Page, request: APIRequestContext, studentId: string) {
  const token = await tokenRaw(request, E3_ORG, studentId, 'STUDENT');
  const me = { role: 'STUDENT', userId: studentId, displayName: 'An' };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
}

/** Học sinh An (F-GEO-020) mở trang Làm bài của một bài; chờ hình sẵn sàng. */
export async function openWorkspace(page: Page, request: APIRequestContext, assignmentId = BG('BG-1'), user = 'F-GEO-020') {
  await signInPage(page, request, user, '11A1');
  await gotoWorkspace(page, assignmentId);
}
export async function gotoWorkspace(page: Page, assignmentId: string) {
  await page.goto(`/hs/lam-bai/${assignmentId}`);
  await expect(page.getByTestId('e3-workspace')).toBeVisible();
  await waitScene(page);
}
export async function waitScene(page: Page) {
  await expect.poll(async () => (await scene(page))?.ready ?? false, { timeout: 15_000 }).toBe(true);
}

export interface Snap {
  ready: boolean;
  tool: string;
  opCount: number;
  camera: { yaw: number; pitch: number; zoom: number; panX: number; panY: number };
  viewport: { left: number; top: number; width: number; height: number };
  axes: Array<{ axis: string; from: number[]; to: number[] }>;
  points: Array<{ name: string; pos: number[]; screen: number[]; user: boolean }>;
  edges: Array<{ id: string; label: string; kind: string; ends: string[]; from: number[]; to: number[]; screen: number[][]; style: string; hiddenRatio: number }>;
  faces: Array<{ id: string; label: string; kind: string; screen: number[][]; triangles: number[][]; area: number }>;
  planes: Array<{ id: string; through: string[]; polygon: number[][] }>;
  traces: Array<{ id: string; plane: string; face: string; screen: number[][] }>;
  extensions: Array<{ id: string; kind: string; target: string }>;
  measures: Array<{ id: string; kind: string; label: string; text: string; hidden: boolean }>;
  selection: { kind: string; id: string; label: string } | null;
  pending: unknown[];
  lock: string;
  pointMenu: { line: string; t: number } | null;
}
export const scene = (page: Page) => page.evaluate(() => (window as unknown as { __geoScene?: unknown }).__geoScene as Snap | undefined);
export async function snap(page: Page): Promise<Snap> {
  const s = await scene(page);
  expect(s, 'móc __geoScene phải bật (VITE_GEO_TEST_HOOKS=1)').toBeTruthy();
  return s!;
}

/**
 * Bật một công cụ (không bấm lại nếu đang bật, vì bấm lại là tắt); lệnh "axes", "reset", "clear" thì luôn bấm.
 * Trên điện thoại công cụ phụ nằm trong "Thêm".
 */
export async function tool(page: Page, id: string) {
  if (!['axes', 'reset', 'clear'].includes(id) && (await snap(page)).tool === id) return;
  const b = page.getByTestId(`viewer-tool-${id}`);
  if (!(await b.isVisible())) await page.getByTestId('viewer-tool-more').click();
  await b.click();
}

export async function tapAt(page: Page, x: number, y: number) {
  await page.mouse.click(x, y);
}

export async function tapPoint(page: Page, name: string) {
  const p = (await snap(page)).points.find((q) => q.name === name);
  expect(p, `điểm ${name}`).toBeTruthy();
  await tapAt(page, p!.screen[0]!, p!.screen[1]!);
}

/** Chạm cạnh/đoạn tại tỉ lệ t (mặc định 0,4 để tránh trùng giao điểm/trung điểm). */
export async function tapEdge(page: Page, id: string, t = 0.4) {
  const e = (await snap(page)).edges.find((q) => q.id === id);
  expect(e, `cạnh ${id}`).toBeTruthy();
  const [a, b] = e!.screen as [number[], number[]];
  await tapAt(page, a[0]! + (b[0]! - a[0]!) * t, a[1]! + (b[1]! - a[1]!) * t);
}

/** Chạm vào bên trong mặt: trọng tâm tam giác lớn nhất của mặt (đúng cả với mặt lõm). */
export async function tapFace(page: Page, id: string) {
  const f = (await snap(page)).faces.find((q) => q.id === id);
  expect(f, `mặt ${id}`).toBeTruthy();
  if (f!.screen.length <= 4) {
    // mặt lồi: trọng tâm các đỉnh trên màn (bộ chọn ưu tiên mặt mà chỗ chạm gần trọng tâm nhất)
    const n = f!.screen.length;
    await tapAt(page, f!.screen.reduce((a, p) => a + p[0]!, 0) / n, f!.screen.reduce((a, p) => a + p[1]!, 0) / n);
    return;
  }
  const tris = f!.triangles.length ? f!.triangles : [[0, 1, 2]];
  // mặt nhiều đỉnh (có thể lõm): trọng tâm diện tích của các tam giác tô
  let sa = 0;
  let sx = 0;
  let sy = 0;
  for (const t of tris) {
    const [a, b, c] = t.map((i) => f!.screen[i]!) as [number[], number[], number[]];
    const area = Math.abs((b[0]! - a[0]!) * (c[1]! - a[1]!) - (c[0]! - a[0]!) * (b[1]! - a[1]!)) / 2;
    sa += area;
    sx += (area * (a[0]! + b[0]! + c[0]!)) / 3;
    sy += (area * (a[1]! + b[1]! + c[1]!)) / 3;
  }
  await tapAt(page, sx / sa, sy / sa);
}

/** Kéo trên khung hình (xoay) bằng chuột. */
export async function drag(page: Page, dx: number, dy = 0) {
  const v = (await snap(page)).viewport;
  const x = v.left + v.width * 0.75;
  const y = v.top + v.height * 0.82;
  await page.mouse.move(x, y);
  await page.mouse.down();
  for (let i = 1; i <= 10; i++) await page.mouse.move(x + (dx * i) / 10, y + (dy * i) / 10);
  await page.mouse.up();
}

/**
 * Chụm/dang hai ngón trên khung hình: hai con trỏ cảm ứng (PointerEvent pointerType 'touch') đi từ khoảng cách d0 tới
 * d1 quanh tâm khung. Playwright không có cử chỉ hai ngón nên phát sự kiện con trỏ trực tiếp vào khung hình.
 */
export async function pinch(page: Page, d0: number, d1: number, steps = 12) {
  await page.getByTestId('viewer-canvas').evaluate(
    (el, a) => {
      const r = el.getBoundingClientRect();
      const cx = r.left + r.width / 2;
      const cy = r.top + r.height / 2;
      const fire = (type: string, id: number, x: number) =>
        el.dispatchEvent(new PointerEvent(type, { pointerId: id, clientX: x, clientY: cy, bubbles: true, pointerType: 'touch', isPrimary: id === 1 }));
      fire('pointerdown', 1, cx - a.d0 / 2);
      fire('pointerdown', 2, cx + a.d0 / 2);
      for (let i = 1; i <= a.steps; i++) {
        const d = a.d0 + ((a.d1 - a.d0) * i) / a.steps;
        fire('pointermove', 1, cx - d / 2);
        fire('pointermove', 2, cx + d / 2);
      }
      fire('pointerup', 1, cx - a.d1 / 2);
      fire('pointerup', 2, cx + a.d1 / 2);
    },
    { d0, d1, steps },
  );
}

/** Góc lệch nhỏ nhất giữa hai góc (độ). */
export const angDiff = (a: number, b: number) => Math.abs(((((a - b) % 360) + 540) % 360) - 180);

/** Trung điểm M của SD (công cụ "Điểm" → cạnh SD → "Trung điểm"). */
export async function addMidpointSD(page: Page) {
  await tool(page, 'point');
  await tapEdge(page, 'SD');
  await page.getByTestId('viewer-point-mid').click();
  await expect.poll(async () => (await snap(page)).points.some((p) => p.name === 'M')).toBe(true);
}

export const toast = (page: Page) => page.getByTestId('viewer-toast');

/** data-testid như hàm tid() của web (bỏ dấu, ký tự lạ thành '-'), ví dụ ('viewer','face','(SBD)') → 'viewer-face-SBD'. */
export function tid(area: string, ...parts: Array<string | number>): string {
  const norm = (s: string | number) =>
    String(s)
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .replace(/[^A-Za-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  return [area, ...parts].map(norm).filter(Boolean).join('-');
}
export const fixture = ids;
