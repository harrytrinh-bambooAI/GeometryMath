import type { APIRequestContext, Locator, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, signIn, walk } from '../s2e8/s2e8.ts';

/**
 * Tiện ích E2E của S3-E3 (Kết quả lớp, "Cả lớp vướng ở đâu"). Mỗi ca tự dựng Cô Lan + Thầy Khôi + lớp + học sinh +
 * bộ bài + bảng trạng thái (F-S3-051) qua /test-hooks/s3e3/sandbox (tổ chức thử của seed 930, đề BOB-01…22 của seed
 * 920): các ca và các dự án Playwright không giẫm nhau. Đáp mẫu của bộ gộp giả lập đặt theo giáo viên qua
 * /test-hooks/s3e3/stuck-script; chế độ quá giờ của nhà cung cấp giả lập qua /test-hooks/ai.
 */
export { API, bearer, walk };

export type Cell = 'Đúng' | 'Xem lại' | 'Đang làm' | 'Chưa làm';
export interface Person {
  id: string;
  token: string;
  userCode: string;
}
export interface SetOut {
  id: string;
  title: string;
  items: Array<{ position: number; assignmentId: string | null }>;
}
export interface ClassOut {
  id: string;
  name: string;
  students: Record<string, Person>;
  sets: SetOut[];
}
export interface Sandbox {
  orgId: string;
  teacher: Person;
  otherTeacher: Person;
  classes: ClassOut[];
  otherClasses: ClassOut[];
}
export interface Ev {
  student: string;
  item: number;
  kind: 'open' | 'hint' | 'ask' | 'answer';
  level?: number;
  text?: string;
  correct?: boolean;
  at?: string;
}
export interface StudentSpec {
  name: string;
  status?: 'active' | 'pending' | 'left';
  shared?: boolean;
}
export interface SetSpec {
  title: string;
  status?: 'assigned' | 'draft';
  items?: number;
  cells?: Record<string, Cell[]>;
  events?: Ev[];
}
export interface ClassSpec {
  name: string;
  students?: StudentSpec[];
  sets?: SetSpec[];
  bulkStudents?: number;
}

export const NAMES = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh'];
/** F-S3-051: bảng trạng thái 11A1 × bộ tuần 15 (Đề 1…4). */
export const BOARD: Record<string, Cell[]> = {
  'Bình An': ['Đúng', 'Xem lại', 'Đúng', 'Xem lại'],
  'Chi Mai': ['Đúng', 'Xem lại', 'Xem lại', 'Chưa làm'],
  'Đức Huy': ['Đúng', 'Đúng', 'Xem lại', 'Đúng'],
  'Gia Bảo': ['Xem lại', 'Xem lại', 'Đang làm', 'Chưa làm'],
  'Hà My': ['Đúng', 'Đúng', 'Đúng', 'Đúng'],
  'Khánh Linh': ['Chưa làm', 'Chưa làm', 'Chưa làm', 'Chưa làm'],
  'Minh Quân': ['Đúng', 'Xem lại', 'Xem lại', 'Xem lại'],
  'Ngọc Anh': ['Đúng', 'Đúng', 'Xem lại', 'Đang làm'],
};
export const STATUS: Record<Cell, string> = { Đúng: 'correct', 'Xem lại': 'review', 'Đang làm': 'doing', 'Chưa làm': 'todo' };
export const COLUMNS = ['Đề 1 · SC-đáy', 'Đề 2 · d(A,SBC)', 'Đề 3 · hai mặt phẳng', 'Đề 4 · thể tích'];
export const WEEK15 = 'Bộ bài tuần 15';
export const active = (names: string[]): StudentSpec[] => names.map((name) => ({ name }));

export async function sandbox(request: APIRequestContext, classes: ClassSpec[], otherClasses: ClassSpec[] = []): Promise<Sandbox> {
  const r = await request.post(`${API}/test-hooks/s3e3/sandbox`, { data: { classes, otherClasses } });
  expect(r.status(), 'móc sandbox S3-E3 (máy chủ profile dev)').toBe(200);
  return (await r.json()) as Sandbox;
}

/** Lớp 11A1 với bảng F-S3-051; opts đổi ô, thêm học sinh, tương tác hoặc bộ khác. */
export function class051(opts: { cells?: Record<string, Cell[]>; events?: Ev[]; students?: StudentSpec[]; moreSets?: SetSpec[] } = {}): ClassSpec {
  return {
    name: '11A1',
    students: [...active(NAMES), ...(opts.students ?? [])],
    sets: [{ title: WEEK15, items: 4, cells: { ...BOARD, ...(opts.cells ?? {}) }, events: opts.events ?? [] }, ...(opts.moreSets ?? [])],
  };
}
export const board051 = (request: APIRequestContext, opts: Parameters<typeof class051>[0] = {}, otherClasses: ClassSpec[] = []) =>
  sandbox(request, [class051(opts)], otherClasses);

export const R = `${API}/api/v1/teacher/class-results`;
export const setUrl = (sb: Sandbox, ci = 0, si = 0) => `${R}/${sb.classes[ci]!.id}/sets/${sb.classes[ci]!.sets[si]!.id}`;
/** PO-74 (BR-S3E3-40): bằng chứng theo userId (UUID); mã người dùng không vào chuỗi truy vấn. */
export const evidenceUrl = (sb: Sandbox, item: number, userId: string, ci = 0, si = 0) =>
  `${setUrl(sb, ci, si)}/evidence?item=${item}&userId=${encodeURIComponent(userId)}`;

export async function getJson<T>(request: APIRequestContext, url: string, token: string): Promise<T> {
  const r = await request.get(url, { headers: bearer(token) });
  expect(r.status(), url).toBe(200);
  return (await r.json()) as T;
}

/** Thêm một tương tác của học sinh sau khi dựng (em làm tiếp trong lúc giáo viên đang xem). */
export async function event(request: APIRequestContext, sb: Sandbox, ev: Ev, ci = 0, si = 0) {
  const cls = sb.classes[ci]!;
  const r = await request.post(`${API}/test-hooks/s3e3/events`, {
    data: {
      assignmentId: cls.sets[si]!.items[ev.item - 1]!.assignmentId,
      userId: cls.students[ev.student]!.id,
      kind: ev.kind,
      level: ev.level ?? null,
      text: ev.text ?? null,
      correct: ev.correct ?? null,
      at: ev.at ?? null,
    },
  });
  expect(r.status()).toBe(200);
}

export interface ScriptInsight {
  step?: number;
  text: string;
  works: number[];
  label?: string;
}
/** Đáp mẫu của bộ gộp giả lập cho giáo viên của sandbox; đề không nêu thì bộ gộp trả "không có nhận định". */
export async function script(request: APIRequestContext, sb: Sandbox, byItem: Record<number, ScriptInsight[]>, items = 20) {
  const replies: Record<number, string> = {};
  for (let i = 1; i <= items; i++) replies[i] = JSON.stringify({ insights: (byItem[i] ?? []).map((x) => ({ step: 1, ...x })) });
  const r = await request.post(`${API}/test-hooks/s3e3/stuck-script`, { data: { userId: sb.teacher.id, replies } });
  expect(r.status()).toBe(200);
}

/** Số lượt gọi nhà cung cấp AI giả lập của một người dùng. */
export async function aiCalls(request: APIRequestContext, userId: string): Promise<number> {
  const r = await request.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as unknown[]).length;
}
export async function aiMode(request: APIRequestContext, userId: string, mode: 'ok' | 'timeout' | 'error') {
  expect((await request.post(`${API}/test-hooks/ai`, { data: { mode, userId } })).status()).toBe(200);
}

export async function openResults(page: Page, sb: Sandbox, ci = 0, si: number | null = 0) {
  await signIn(page, { ...sb.teacher, userCode: null }, 'TEACHER', 'Cô Lan');
  const cls = sb.classes[ci]!;
  await page.goto(`/gv/ket-qua-lop?lop=${cls.id}${si === null ? '' : `&bo=${cls.sets[si]!.id}`}`);
  await expect(page.getByTestId('s3e3-page')).toBeVisible();
}
export async function openBoard(page: Page, sb: Sandbox, ci = 0, si: number | null = 0) {
  await openResults(page, sb, ci, si);
  await expect(page.getByTestId('s3e3-board')).toBeVisible();
}

export const rows = (page: Page): Locator => page.getByTestId('s3e3-row');
export const rowOf = (page: Page, person: Person): Locator => page.locator(`[data-testid="s3e3-row"][data-code="${person.userCode}"]`);
export const cellOf = (page: Page, person: Person, item: number): Locator =>
  rowOf(page, person).locator(`[data-testid="s3e3-cell"][data-item="${item}"]`);
export const rowNames = (page: Page) => rows(page).locator('b').allTextContents();
export const student = (sb: Sandbox, name: string, ci = 0): Person => {
  const p = sb.classes[ci]!.students[name];
  if (!p) throw new Error(`Không có học sinh ${name} trong sandbox`);
  return p;
};
export const insight = (page: Page, item: number): Locator => page.locator(`[data-testid="s3e3-insight"][data-position="${item}"]`);
export const raw = (page: Page, item: number): Locator => page.locator(`[data-testid="s3e3-raw"][data-position="${item}"]`);
export async function stuckReady(page: Page) {
  await expect(page.getByTestId('s3e3-stuck-note')).toBeVisible({ timeout: 30_000 });
}
