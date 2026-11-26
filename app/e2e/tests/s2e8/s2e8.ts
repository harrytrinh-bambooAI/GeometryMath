import { randomUUID } from 'node:crypto';
import type { APIRequestContext, APIResponse, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E8 (bài luyện điền đáp số). Dữ liệu: app/db/seed/e2e/830_s2e8_core.sql (đề luyện bộ 's2e8_luyen').
 * Mỗi ca tự dựng giáo viên + lớp 11A1 (An, Bình) + lớp 11A2 (Chi) qua móc /test-hooks/s2e8/sandbox, rồi soạn và giao
 * bài bằng API thật: các ca và các dự án Playwright không giẫm nhau.
 * BL-1 (FX-20): câu 1 LC-01 (6) · câu 2 SP-01 = FX-14 (45°, xấp xỉ ±0,5, 3 lượt) · câu 3 LA-01 (4√3) · câu 4 LB-01 (1/2)
 * · câu 5 HCN-01 (10); giờ mở lời giải T0 + 2 giờ.
 */
export { API, bearer };

export interface Person {
  id: string;
  token: string;
  userCode: string | null;
}
export interface Sandbox {
  orgId: string;
  teacher: Person;
  otherTeacher: Person;
  classes: Array<{ id: string; name: string; students: Record<string, Person> }>;
}
export interface ItemView {
  position: number;
  assignmentId: string;
  maxAttempts: number;
  answerKind: string;
  tolerance: number | null;
  releaseAt: string | null;
  problemVersionId: string;
}
export interface SetDetail {
  id: string;
  title: string;
  status: string;
  classId: string | null;
  releaseAt: string | null;
  items: ItemView[];
}
export interface ItemSpec {
  code: string;
  answerKind?: 'exact' | 'approx';
  tolerance?: number;
  maxAttempts?: number;
  releaseAt?: string | null;
}

export const HINT_Q2 = 'Góc giữa SC và mặt đáy là góc giữa SC và hình chiếu của nó lên đáy. Hình chiếu của SC là đoạn nào?';
export const BL1: ItemSpec[] = [
  { code: 'LC-01' },
  { code: 'SP-01', answerKind: 'approx', tolerance: 0.5, maxAttempts: 3 },
  { code: 'LA-01' },
  { code: 'LB-01' },
  { code: 'HCN-01' },
];
/** Đáp số đúng của BL-1 theo thứ tự câu (chỉ ca kiểm biết). */
export const BL1_ANSWERS = ['6', '45', '4√3', '1/2', '10'];

export const hours = (n: number) => new Date(Date.now() + n * 3_600_000).toISOString();

export async function sandbox(
  request: APIRequestContext,
  classes: Array<{ name: string; students: string[] }> = [
    { name: '11A1', students: ['An', 'Bình'] },
    { name: '11A2', students: ['Chi'] },
  ],
): Promise<Sandbox> {
  const r = await request.post(`${API}/test-hooks/s2e8/sandbox`, { data: { classes } });
  expect(r.status(), 'móc sandbox S2-E8 (máy chủ profile dev)').toBe(200);
  return (await r.json()) as Sandbox;
}

export const student = (sb: Sandbox, name: string): Person => {
  for (const c of sb.classes) if (c.students[name]) return c.students[name]!;
  throw new Error(`Không có học sinh ${name} trong sandbox`);
};

const versionCache = new Map<string, Record<string, string>>();

/** Mã đề → id phiên bản hiện hành (qua API đề cho màn soạn bài). */
export async function versions(request: APIRequestContext, teacherToken: string): Promise<Record<string, string>> {
  const r = await request.get(`${API}/api/v1/teacher/practice/problems`, { headers: bearer(teacherToken) });
  expect(r.status()).toBe(200);
  const out: Record<string, string> = {};
  for (const p of ((await r.json()) as { items: Array<{ code: string; problemVersionId: string }> }).items) out[p.code] = p.problemVersionId;
  versionCache.set(teacherToken, out);
  return out;
}

export async function setBody(request: APIRequestContext, teacherToken: string, title: string, items: ItemSpec[], releaseAt: string | null) {
  const v = versionCache.get(teacherToken) ?? (await versions(request, teacherToken));
  return {
    title,
    releaseAt,
    items: items.map((i) => ({
      problemVersionId: v[i.code] ?? '00000000-0000-7000-8002-00000083ffff',
      answerKind: i.answerKind ?? 'exact',
      tolerance: i.tolerance ?? null,
      maxAttempts: i.maxAttempts ?? 3,
      releaseAt: i.releaseAt ?? null,
    })),
  };
}

/** Tạo bài nháp (trả phản hồi thô để ca biên kiểm mã lỗi). */
export async function createDraft(
  request: APIRequestContext,
  teacherToken: string,
  title: string,
  items: ItemSpec[],
  releaseAt: string | null = hours(2),
): Promise<APIResponse> {
  return request.post(`${API}/api/v1/teacher/practice-sets`, {
    headers: bearer(teacherToken),
    data: await setBody(request, teacherToken, title, items, releaseAt),
  });
}

export async function assign(request: APIRequestContext, teacherToken: string, setId: string, classId: string, confirmPastRelease = false) {
  return request.post(`${API}/api/v1/teacher/practice-sets/${setId}/assign`, {
    headers: bearer(teacherToken),
    data: { classId, confirmPastRelease },
  });
}

/** Soạn và giao một bài cho lớp đầu tiên của sandbox; mặc định là BL-1. */
export async function assigned(
  request: APIRequestContext,
  sb: Sandbox,
  opts: { items?: ItemSpec[]; title?: string; releaseAt?: string | null } = {},
): Promise<SetDetail> {
  const d = await createDraft(
    request,
    sb.teacher.token,
    opts.title ?? 'Góc trong hình chóp',
    opts.items ?? BL1,
    opts.releaseAt === undefined ? hours(2) : opts.releaseAt,
  );
  expect(d.status(), await d.text()).toBe(201);
  const draft = (await d.json()) as SetDetail;
  const a = await assign(request, sb.teacher.token, draft.id, sb.classes[0]!.id);
  expect(a.status(), await a.text()).toBe(200);
  return (await a.json()) as SetDetail;
}

export interface Attempt {
  outcome: 'correct' | 'wrong' | 'syntax';
  message: string;
  reason: string | null;
  canonical: string | null;
  note: string | null;
  hint: string | null;
  attemptsLeft: number;
  maxAttempts: number;
  state: string;
  canReport: boolean;
}

export async function attemptRaw(request: APIRequestContext, token: string | null, assignmentId: string, answer: string, key: string = randomUUID(), extra: Record<string, unknown> = {}) {
  return request.post(`${API}/api/v1/student/practice-items/${assignmentId}/attempts`, {
    headers: token ? bearer(token) : {},
    data: { answer, requestKey: key, ...extra },
  });
}

export async function attempt(request: APIRequestContext, token: string, assignmentId: string, answer: string): Promise<Attempt> {
  const r = await attemptRaw(request, token, assignmentId, answer);
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Attempt;
}

/** Làm cho một câu hết lượt bằng các đáp số sai. */
export async function exhaust(request: APIRequestContext, token: string, item: ItemView) {
  for (let i = 0; i < item.maxAttempts; i++) await attempt(request, token, item.assignmentId, String(900 + i));
}

export interface PlayItem {
  assignmentId: string;
  position: number;
  attemptsLeft: number;
  maxAttempts: number;
  state: string;
  hint: string | null;
  canReport: boolean;
  reported: boolean;
}
export interface PlayView {
  id: string;
  title: string;
  status: string;
  current: number;
  items: PlayItem[];
}

export async function play(request: APIRequestContext, token: string, setId: string): Promise<PlayView> {
  const r = await request.get(`${API}/api/v1/student/practice-sets/${setId}`, { headers: bearer(token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as PlayView;
}

export interface ResultView {
  correct: number;
  review: number;
  remaining: number;
  summary: string;
  pending: string | null;
  note: string | null;
  items: Array<{ position: number; state: string; label: string; solutionState: string; solutionLabel: string; releaseAt: string | null }>;
}

export async function result(request: APIRequestContext, token: string, setId: string): Promise<ResultView> {
  const r = await request.get(`${API}/api/v1/student/practice-sets/${setId}/result`, { headers: bearer(token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as ResultView;
}

export async function report(request: APIRequestContext, token: string, assignmentId: string, note?: string) {
  return request.post(`${API}/api/v1/student/practice-items/${assignmentId}/report`, {
    headers: bearer(token),
    data: note === undefined ? {} : { note },
  });
}

/** Mọi khóa và mọi giá trị chuỗi trong JSON (đệ quy) — để kiểm "dữ liệu trả về không có đáp số". */
export function walk(v: unknown, keys: string[] = [], strings: string[] = []): { keys: string[]; strings: string[] } {
  if (typeof v === 'string') strings.push(v);
  else if (Array.isArray(v)) v.forEach((x) => walk(x, keys, strings));
  else if (v && typeof v === 'object')
    for (const [k, x] of Object.entries(v)) {
      keys.push(k);
      walk(x, keys, strings);
    }
  return { keys, strings };
}

const SECRET_KEYS = /^(answer|answers|answerSnapshot|finalAnswer|solution|steps|tolerance|tol|answerKind|acceptedForms|canonicalAnswer)$/i;

/** Dữ liệu trả về cho học sinh không có đáp số 45 ở mọi dạng viết, không có sai số, kiểu đáp số hay bước giải. */
export function expectNoAnswer45(body: unknown, where: string) {
  const { keys, strings } = walk(body);
  expect(keys.filter((k) => SECRET_KEYS.test(k)), `${where}: khóa lộ đáp số`).toEqual([]);
  for (const s of strings) {
    if (/^[0-9a-f]{8}-[0-9a-f]{4}-/.test(s) || /^\d{4}-\d{2}-\d{2}T/.test(s)) continue; // mã và mốc giờ
    expect(s, `${where}: chuỗi lộ đáp số`).not.toMatch(/(^|[^0-9,.])45([^0-9]|$)|90\s*\/\s*2|bốn mươi lăm|tan\s*=?\s*1|±|0[,.]5/i);
  }
}

/** Đăng nhập sẵn trên trình duyệt bằng thẻ của sandbox (như signInPage của E1). */
export async function signIn(page: Page, person: Person, role: 'STUDENT' | 'TEACHER', displayName: string) {
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [person.token, JSON.stringify({ role, userId: person.id, displayName })] as const,
  );
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
}

/** An mở bài luyện trên trình duyệt. */
export async function openPlay(page: Page, sb: Sandbox, setId: string, name = 'An') {
  await signIn(page, student(sb, name), 'STUDENT', name);
  await page.goto(`/hs/bai-luyen/${setId}`);
  await expect(page.getByTestId('s2e8-question')).toBeVisible();
}

const KEY_NAMES: Record<string, string> = {
  '(': 'mo-ngoac',
  ')': 'dong-ngoac',
  '⌫': 'xoa-lui',
  '+': 'cong',
  '-': 'tru',
  '√': 'can',
  '×': 'nhan',
  '÷': 'chia',
  π: 'pi',
  ',': 'phay',
  '°': 'do',
  '/': 'phan-so',
  '^': 'mu',
};

/** Chạm lần lượt các phím của bàn phím toán. */
export async function tap(page: Page, keys: string) {
  for (const k of [...keys]) await page.getByTestId(`s2e8-key-${KEY_NAMES[k] ?? k}`).click();
}

export const answerBox = (page: Page) => page.getByTestId('s2e8-answer');

/** Gõ đáp số rồi bấm Kiểm tra, chờ phản hồi. */
export async function submit(page: Page, keys: string) {
  await page.getByTestId('s2e8-key-xoa-het').click();
  await tap(page, keys);
  await page.getByTestId('s2e8-check').click();
  await expect(page.getByTestId('s2e8-feedback')).toBeVisible();
}

/** Dán một chuỗi vào ô đáp số (sự kiện paste như người dùng dán từ bộ nhớ tạm). */
export async function pasteInto(page: Page, text: string) {
  await answerBox(page).evaluate((el, t) => {
    const dt = new DataTransfer();
    dt.setData('text/plain', t);
    el.dispatchEvent(new ClipboardEvent('paste', { clipboardData: dt, bubbles: true, cancelable: true }));
  }, text);
}

export async function noHorizontalScroll(page: Page) {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, 'không cuộn ngang').toBeLessThanOrEqual(0);
}
