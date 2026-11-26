import { randomUUID } from 'node:crypto';
import type { APIRequestContext, APIResponse, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, signIn, walk } from '../s2e8/s2e8.ts';

/**
 * Tiện ích E2E của S3-E2 (bộ bài chặng 3). Dữ liệu: app/db/seed/e2e/920_s3e2_core.sql (bộ đề 's3e2_kho', 22 đề phẳng
 * BOB-01…22; Đề 3 "Cần xem lại"). Mỗi ca tự dựng Cô Lan + Thầy Khôi + lớp + học sinh Đang học / Chờ duyệt qua
 * /test-hooks/s3e2/sandbox rồi soạn, giao, đóng bộ bằng API thật: các ca và các dự án Playwright không giẫm nhau.
 * Đề 1 SC-đáy (45) · Đề 2 d(A,SBC) (12/5) · Đề 3 hai mặt phẳng (60, Cần xem lại) · Đề 4 thể tích (12) · Đề k (k = 5…22)
 * diện tích hình chữ nhật k × (k + 1).
 */
export { API, bearer, signIn, walk };

export interface Person {
  id: string;
  token: string;
}
export interface ClassOut {
  id: string;
  name: string;
  students: Record<string, Person>;
  pending: Record<string, Person>;
}
export interface Sandbox {
  orgId: string;
  teacher: Person;
  otherTeacher: Person;
  classes: ClassOut[];
  otherClasses: ClassOut[];
}
export interface Item {
  position: number;
  code: string;
  title: string;
  needsReview: boolean;
  ownReleaseAt?: string | null;
  releaseAt?: string | null;
  releaseState: string;
  assignmentId?: string | null;
}
export interface SetDetail {
  id: string;
  title: string;
  status: string;
  classId?: string | null;
  dueAt?: string | null;
  releaseAt?: string | null;
  releaseBeforeDue: boolean;
  scheduleVersion: number;
  items: Item[];
}

/** id phiên bản của đề BOB-nn (seed 920). */
export const V = (n: number) => `00000000-0000-7000-8003-${String(922000 + n).padStart(12, '0')}`;
/** Đáp số đúng của đề n. */
export const ANSWER = (n: number) => (n === 1 ? '45' : n === 2 ? '12/5' : n === 3 ? '60' : n === 4 ? '12' : String(n * (n + 1)));
export const TITLE = (n: number) =>
  n === 1 ? 'Đề 1 SC-đáy' : n === 2 ? 'Đề 2 d(A,SBC)' : n === 3 ? 'Đề 3 hai mặt phẳng' : n === 4 ? 'Đề 4 thể tích' : `Đề ${n} hình chữ nhật`;
export const CODE = (n: number) => `BOB-${String(n).padStart(2, '0')}`;
export const hours = (n: number) => new Date(Date.now() + n * 3_600_000).toISOString();
export const ELEVEN_A1 = ['Bình An', 'Chi Mai', 'Đức Huy'];
export const PENDING = ['Hoàng Nam', 'Thu Hà'];

export async function sandbox(
  request: APIRequestContext,
  classes: Array<{ name: string; students: string[]; pending?: string[] }> = [{ name: '11A1', students: ELEVEN_A1, pending: PENDING }],
  otherClasses: Array<{ name: string; students: string[] }> = [{ name: '10B', students: ['Mi Na'] }],
): Promise<Sandbox> {
  const r = await request.post(`${API}/test-hooks/s3e2/sandbox`, { data: { classes, otherClasses } });
  expect(r.status(), 'móc sandbox S3-E2 (máy chủ profile dev)').toBe(200);
  return (await r.json()) as Sandbox;
}

export const T = `${API}/api/v1/teacher/assignment-sets`;
export const S = `${API}/api/v1/student/assignment-sets`;

export function body(opts: { title?: string; classId?: string | null; dueAt?: string | null; releaseAt?: string | null; items?: number[]; itemReleases?: Record<number, string> }) {
  return {
    title: opts.title ?? 'Bộ bài tuần 16',
    classId: opts.classId ?? null,
    dueAt: opts.dueAt ?? null,
    releaseAt: opts.releaseAt ?? null,
    items: (opts.items ?? [1, 2, 3, 4]).map((n) => ({ problemVersionId: V(n), releaseAt: opts.itemReleases?.[n] ?? null })),
  };
}

export async function createRaw(request: APIRequestContext, token: string, b: ReturnType<typeof body>): Promise<APIResponse> {
  return request.post(T, { headers: bearer(token), data: b });
}

export async function draft(request: APIRequestContext, sb: Sandbox, opts: Parameters<typeof body>[0] = {}): Promise<SetDetail> {
  const r = await createRaw(request, sb.teacher.token, body({ classId: sb.classes[0]!.id, ...opts }));
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as SetDetail;
}

export async function assignRaw(request: APIRequestContext, token: string, setId: string, requestKey: string = randomUUID()) {
  return request.post(`${T}/${setId}/assign`, { headers: bearer(token), data: { requestKey, confirmPastRelease: true } });
}

export async function assigned(request: APIRequestContext, sb: Sandbox, opts: Parameters<typeof body>[0] = {}): Promise<SetDetail> {
  const d = await draft(request, sb, { releaseAt: hours(48), dueAt: hours(24), ...opts });
  const a = await assignRaw(request, sb.teacher.token, d.id);
  expect(a.status(), await a.text()).toBe(200);
  return (await a.json()) as SetDetail;
}

export async function teacherSet(request: APIRequestContext, token: string, id: string): Promise<SetDetail> {
  const r = await request.get(`${T}/${id}`, { headers: bearer(token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as SetDetail;
}

export interface StudentHome {
  groups: Array<{ classId: string; className: string; sets: Array<{ id: string; title: string; done: number; total: number; overdue: boolean; releaseState: string }> }>;
}
export async function home(request: APIRequestContext, token: string): Promise<StudentHome> {
  const r = await request.get(S, { headers: bearer(token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as StudentHome;
}

export async function attempt(request: APIRequestContext, token: string, assignmentId: string, answer: string) {
  return request.post(`${API}/api/v1/student/practice-items/${assignmentId}/attempts`, {
    headers: bearer(token),
    data: { answer, requestKey: randomUUID() },
  });
}

/** Giờ mở của một câu đã giao = giờ máy chủ + n giây (móc E6, đường thật vẫn là fn_set_release). */
export async function releaseIn(request: APIRequestContext, assignmentId: string, seconds: number): Promise<string> {
  const r = await request.post(`${API}/test-hooks/e6/release`, { data: { assignmentId, seconds } });
  expect(r.status(), await r.text()).toBe(200);
  return ((await r.json()) as { releaseAt: string }).releaseAt;
}

export async function teacherPage(page: Page, sb: Sandbox, path: string) {
  await signIn(page, { ...sb.teacher, userCode: null }, 'TEACHER', 'Cô Lan');
  await page.goto(path);
}
export async function studentPage(page: Page, person: Person, name: string, path: string) {
  await signIn(page, { ...person, userCode: null }, 'STUDENT', name);
  await page.goto(path);
}

/** Giá trị ô datetime-local theo giờ của trình duyệt cho một mốc ISO. */
export function localValue(page: Page, iso: string): Promise<string> {
  return page.evaluate((v) => {
    const d = new Date(v);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }, iso);
}

export async function noHorizontalScroll(page: Page) {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, 'không cuộn ngang').toBeLessThanOrEqual(0);
}
