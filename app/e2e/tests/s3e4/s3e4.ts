import { randomUUID } from 'node:crypto';
import type { APIRequestContext, APIResponse, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, signIn, world, type ClassSpec, type Person, type World } from '../s3e1/s3e1.ts';

/**
 * Tiện ích E2E của S3-E4 và US-S3E5-01 (T-261126-31). Mỗi ca dựng "thế giới" riêng qua móc /test-hooks/s3e1/world
 * (cô Lan, thầy Khôi, lớp, học sinh có mã người dùng ở các trạng thái) rồi soạn, giao bộ bằng API thật của S3-E2 với
 * đề kho 's3_tuan' (seed 910: phiên bản 910101…910106) — các ca và các dự án Playwright không giẫm nhau, không đụng
 * dữ liệu mẫu chung F-S3. Mọi mốc giờ là tương đối so với giờ máy chủ (T0); nhãn giờ tính lại trong ca kiểm.
 */
export { API, bearer, signIn };
export type { Person, World };

export const MSG = {
  s61: 'Lớp sẽ thấy lời giải ngay. Mở ngay?',
  s62: 'Chưa gửi được. Kiểm tra mạng rồi thử lại.',
  s63: 'Giờ mới phải sau thời điểm hiện tại. Muốn mở ngay, dùng Mở ngay.',
  s64: 'Đã mở',
  s65: 'Chưa có việc cần làm hôm nay.',
  s66: 'Bạn chưa có lớp. Tạo lớp đầu tiên.',
  s67: 'Lời giải đã mở, không đổi giờ được.',
} as const;

/** Phiên bản đề TUAN thứ n (1…6) của bộ 's3_tuan'. */
export const V = (n: number) => `00000000-0000-7000-8003-0000009101${String(n).padStart(2, '0')}`;
export const hours = (n: number) => new Date(Date.now() + n * 3_600_000).toISOString();
/** Mốc giờ tròn phút sau n giờ (ô datetime-local không có giây). */
export const hoursRound = (n: number) => new Date(Math.floor((Date.now() + n * 3_600_000) / 60_000) * 60_000).toISOString();
export const ms = (iso: string | null | undefined) => (iso ? new Date(iso).getTime() : null);

const p2 = (n: number) => String(n).padStart(2, '0');
const vn = (iso: string) => new Date(new Date(iso).getTime() + 7 * 3_600_000);
/** "HH:mm, dd/MM/yyyy" giờ Việt Nam. */
export function vnAt(iso: string): string {
  const d = vn(iso);
  return `${p2(d.getUTCHours())}:${p2(d.getUTCMinutes())}, ${p2(d.getUTCDate())}/${p2(d.getUTCMonth() + 1)}/${d.getUTCFullYear()}`;
}
/** "T6 04/12/2026 20:00" giờ Việt Nam. */
export function dueLabel(iso: string): string {
  const d = vn(iso);
  const day = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][d.getUTCDay()];
  return `${day} ${p2(d.getUTCDate())}/${p2(d.getUTCMonth() + 1)}/${d.getUTCFullYear()} ${p2(d.getUTCHours())}:${p2(d.getUTCMinutes())}`;
}

export const ACTIVE_8 = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh'];

/** Thế giới nhỏ: cô Lan (11A1: 3 em Đang học, 1 Chờ duyệt, 1 Đã rời; 11A2: 1 em), thầy Khôi (10B: 1 em). */
export function smallWorld(request: APIRequestContext, over: Partial<ClassSpec> = {}): Promise<World> {
  return world(request, {
    teachers: ['Cô Lan', 'Thầy Khôi'],
    students: [],
    classes: [
      { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An', 'Chi Mai', 'Khánh Linh'], pending: ['Thu Hà'], left: ['Văn Tú'], ...over },
      { key: '11A2', teacher: 'Cô Lan', name: '11A2', active: ['Quốc Bảo'] },
      { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
    ],
  });
}

/** Thế giới như F-S3-010…041: 11A1 8 em Đang học, 3 Chờ duyệt, 1 Đã rời; 11A2 3 em; 10B của thầy Khôi. */
export function fullWorld(request: APIRequestContext): Promise<World> {
  return world(request, {
    teachers: ['Cô Lan', 'Thầy Khôi'],
    students: [],
    classes: [
      { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ACTIVE_8, pending: ['Hoàng Nam', 'Thu Hà', 'Tuấn Anh'], left: ['Văn Tú'] },
      { key: '11A2', teacher: 'Cô Lan', name: '11A2', active: ['Quốc Bảo', 'Thảo Vy', 'Tuấn Kiệt'] },
      { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
    ],
  });
}

export const lan = (w: World) => w.people['Cô Lan']!;
export const khoi = (w: World) => w.people['Thầy Khôi']!;
export const who = (w: World, name: string) => w.people[name]!;
export const klass = (w: World, key: string) => w.classes[key]!;

export interface SetDetail {
  id: string;
  title: string;
  status: string;
  releaseAt?: string | null;
  scheduleVersion: number;
  items: Array<{ position: number; assignmentId?: string | null; releaseAt?: string | null; releaseState: string }>;
}
export interface SetOpts {
  title?: string;
  items?: number[];
  releaseAt?: string | null;
  dueAt?: string | null;
}

const T = `${API}/api/v1/teacher`;
export const SETS = `${T}/assignment-sets`;

export async function draftSet(rq: APIRequestContext, teacher: Person, classId: string, o: SetOpts = {}): Promise<SetDetail> {
  const r = await rq.post(SETS, {
    headers: bearer(teacher.token),
    data: {
      title: o.title ?? 'Bộ bài tuần 15',
      classId,
      dueAt: o.dueAt === undefined ? hours(24) : o.dueAt,
      releaseAt: o.releaseAt === undefined ? hoursRound(48) : o.releaseAt,
      items: (o.items ?? [1, 2]).map((n) => ({ problemVersionId: V(n), releaseAt: null })),
    },
  });
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as SetDetail;
}

export async function assignDraft(rq: APIRequestContext, teacher: Person, setId: string): Promise<SetDetail> {
  const a = await rq.post(`${SETS}/${setId}/assign`, { headers: bearer(teacher.token), data: { requestKey: randomUUID(), confirmPastRelease: true } });
  expect(a.status(), await a.text()).toBe(200);
  return (await a.json()) as SetDetail;
}

/** Soạn rồi giao một bộ (mặc định: 2 đề, hạn sau 24 giờ, lời giải mở sau 48 giờ = Sắp mở). */
export async function assignedSet(rq: APIRequestContext, teacher: Person, classId: string, o: SetOpts = {}): Promise<SetDetail> {
  const d = await draftSet(rq, teacher, classId, o);
  return assignDraft(rq, teacher, d.id);
}

/** Một tương tác của học sinh trên một đề (gửi đáp số) — em đó không còn là "chưa mở bài". */
export async function interact(rq: APIRequestContext, student: Person, assignmentId: string) {
  const r = await rq.post(`${API}/api/v1/student/practice-items/${assignmentId}/attempts`, {
    headers: bearer(student.token),
    data: { answer: '1', requestKey: randomUUID() },
  });
  expect(r.status(), await r.text()).toBe(200);
}

export interface AssignedLine {
  setId: string;
  title: string;
  classId: string;
  className: string;
  dueAt?: string | null;
  overdue: boolean;
  notOpenedCount: number;
  releaseState: string;
  releaseAt?: string | null;
}
export interface Overview {
  hasClasses: boolean;
  assigned: AssignedLine[];
  pending: { total: number; byClass: Array<{ classId: string; className: string; count: number }> };
}
export interface Follow {
  setId: string;
  title: string;
  className: string;
  releaseState: string;
  releaseAt?: string | null;
  version: number;
  notOpened: Array<{ displayName: string; userCode?: string | null }>;
  serverNow: string;
}
export interface AuditEvent {
  id: number;
  type: string;
  actorCode?: string | null;
  actorRole?: string | null;
  targetType: string;
  targetId: string;
  classId: string;
  before?: string | null;
  after?: string | null;
  result: string;
  at: string;
}
export interface Note {
  id: string;
  kind: string;
  body: string;
  setId?: string | null;
  createdAt: string;
  read: boolean;
}

export const api = {
  overview: (rq: APIRequestContext, t: string) => rq.get(`${T}/overview`, { headers: bearer(t) }),
  follow: (rq: APIRequestContext, t: string, setId: string) => rq.get(`${SETS}/${setId}/follow`, { headers: bearer(t) }),
  openNow: (rq: APIRequestContext, t: string, setId: string, data: Record<string, unknown> = {}) =>
    rq.post(`${SETS}/${setId}/open-now`, { headers: bearer(t), data }),
  changeTime: (rq: APIRequestContext, t: string, setId: string, data: Record<string, unknown>) =>
    rq.post(`${SETS}/${setId}/release-time`, { headers: bearer(t), data }),
  audit: (rq: APIRequestContext, t: string, classId: string) => rq.get(`${T}/classes/${classId}/audit-events`, { headers: bearer(t) }),
  notes: (rq: APIRequestContext, t: string, cursor?: string) =>
    rq.get(`${API}/api/v1/notifications${cursor ? `?cursor=${encodeURIComponent(cursor)}` : ''}`, { headers: bearer(t) }),
  unread: (rq: APIRequestContext, t: string) => rq.get(`${API}/api/v1/notifications/unread-count`, { headers: bearer(t) }),
};

export async function ok<R>(r: APIResponse): Promise<R> {
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as R;
}
export const follow = async (rq: APIRequestContext, t: string, setId: string) => ok<Follow>(await api.follow(rq, t, setId));
export const auditOf = async (rq: APIRequestContext, t: string, classId: string) => (await ok<{ items: AuditEvent[] }>(await api.audit(rq, t, classId))).items;
export const notesOf = async (rq: APIRequestContext, t: string) => (await ok<{ items: Note[]; nextCursor?: string | null }>(await api.notes(rq, t))).items;
export const unreadOf = async (rq: APIRequestContext, t: string) => (await ok<{ count: number }>(await api.unread(rq, t))).count;

export async function err(r: APIResponse) {
  return (await r.json()) as { code: string; message: string; details?: Array<{ field: string; message: string }> };
}

export interface Denied {
  type: string;
  actorCode?: string | null;
  actorRole?: string | null;
  targetType: string;
  result: string;
  bare: boolean;
}
/** Bản ghi Từ chối (BR-S3E5-07) của một người gọi — móc dev/test, sản phẩm không có API nào trả chúng. */
export async function deniedOf(rq: APIRequestContext, actorId: string): Promise<Denied[]> {
  const r = await rq.get(`${API}/test-hooks/s3e4/denied?actorId=${actorId}`);
  return (await ok<{ items: Denied[] }>(r)).items;
}

/** n thông báo Chưa đọc dựng sẵn cho một người (móc dev/test). */
export async function seedNotes(rq: APIRequestContext, userId: string, count: number, ageDays = 0) {
  const r = await rq.post(`${API}/test-hooks/s3e4/notifications/seed`, { data: { userId, count, ageDays } });
  expect(r.status(), await r.text()).toBe(200);
}

export async function resetPassword(rq: APIRequestContext, teacher: Person, classId: string, studentId: string) {
  return rq.post(`${T}/my-classes/${classId}/students/${studentId}/password-reset`, { headers: bearer(teacher.token) });
}

export async function open(page: Page, p: Person, path: string) {
  await signIn(page, p);
  await page.goto(path);
}

export async function noHorizontalScroll(page: Page) {
  const out = await page.evaluate(() => {
    const w = document.documentElement.clientWidth;
    const wide = [...document.querySelectorAll('body *')]
      .filter((e) => e.getBoundingClientRect().right > w + 1)
      .slice(0, 6)
      .map((e) => `${e.tagName.toLowerCase()}.${String(e.className).slice(0, 40)}[${e.getAttribute('data-testid') ?? ''}]`);
    return { overflow: document.documentElement.scrollWidth - w, wide };
  });
  expect(out.overflow, `không cuộn ngang (tràn: ${out.wide.join(' | ')})`).toBeLessThanOrEqual(0);
}

/** Giá trị ô datetime-local theo giờ của trình duyệt cho một mốc ISO. */
export function localValue(page: Page, iso: string): Promise<string> {
  return page.evaluate((v) => {
    const d = new Date(v);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }, iso);
}
