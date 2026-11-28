import { randomUUID } from 'node:crypto';
import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, world, type ClassSpec, type Person, type World } from '../s3e1/s3e1.ts';

/**
 * Tiện ích E2E S5-E5 (T-261128-55): công tắc / hạn mức AI của lớp (US-S5E5-04) và nháp thông báo trợ lý soạn
 * (US-S5E5-01). Mỗi ca dựng thế giới riêng qua móc S3-E1 (chạy song song an toàn): cô Lan chủ 11A1 với 8 em Đang học
 * (F-S5-010) + Văn Tú Đã rời; thầy Khôi chủ 10B (Mi Na). AI giả lập (task class_notice, bộ chạy chấm nền giả của móc
 * /test-hooks/s5e5 vì chấm nền S5-E4/E6 chưa có ở đợt W2).
 */
export const EIGHT = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh'];

export const S5E5_WORLD: { teachers: string[]; students: string[]; classes: ClassSpec[] } = {
  teachers: ['Cô Lan', 'Thầy Khôi'],
  students: [],
  classes: [
    { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: EIGHT, left: ['Văn Tú'] },
    { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
  ],
};

export const s5e5World = (rq: APIRequestContext): Promise<World> => world(rq, S5E5_WORLD);

export const NOTICES = `${API}/api/v1/teacher/notices`;
export const switches = (classId: string) => `${API}/api/v1/teacher/my-classes/${classId}/ai-switches`;
export const usage = (classId: string, days = 30) => `${API}/api/v1/teacher/my-classes/${classId}/ai-usage?days=${days}`;
export const KILL = `${API}/api/v1/admin/ai/kill`;

/** BC-01: hạn T6 11/12/2026 20:00 giờ Việt Nam (F-S5-052). */
export const BC01_DUE = '2026-12-11T13:00:00Z';
export const BC01_TITLE = 'Kiểm tra 15 phút – Khoảng cách';

export const MSG = {
  s06: 'Hôm nay thầy cô đã dùng hết 30 lượt soạn với AI. Lượt mới mở lúc 00:00.',
  s55: (n: number, lop: string) => `Đã gửi thông báo tới ${n} em lớp ${lop}.`,
  s56: 'Thông báo tối đa 1 000 ký tự.',
  s59: (lop: string) => `Đã tắt AI chấm cho lớp ${lop}. Bài mới nộp sẽ chấm tay.`,
  s60: (lop: string, n: number) => `Lớp ${lop} đã dùng hết ${n} lượt AI hôm nay. Lượt mới mở lúc 00:00.`,
  aiLine: 'Soạn với trợ lý AI, thầy cô đã duyệt',
} as const;

/** Bộ bài BC-01 có hạn nộp, giao cho lớp (đường S3-E2 thật: soạn nháp → giao). */
export async function bc01(rq: APIRequestContext, teacher: Person, classId: string, dueAt = BC01_DUE) {
  const p = await rq.get(`${API}/api/v1/teacher/practice/problems`, { headers: bearer(teacher.token) });
  expect(p.status()).toBe(200);
  const items = ((await p.json()) as { items: Array<{ code: string; problemVersionId: string }> }).items;
  const pick = items.filter((i) => i.code === 'TUAN-11' || i.code === 'TUAN-12').map((i) => i.problemVersionId);
  expect(pick.length, 'đề kho s3_tuan').toBe(2);
  const d = await rq.post(`${API}/api/v1/teacher/assignment-sets`, {
    headers: bearer(teacher.token),
    data: { title: BC01_TITLE, classId, dueAt, releaseAt: null, items: pick.map((v) => ({ problemVersionId: v, releaseAt: null })) },
  });
  expect(d.status(), await d.text()).toBe(201);
  const set = (await d.json()) as { id: string };
  const a = await rq.post(`${API}/api/v1/teacher/assignment-sets/${set.id}/assign`, {
    headers: bearer(teacher.token),
    data: { requestKey: randomUUID(), confirmPastRelease: true },
  });
  expect(a.status(), await a.text()).toBe(200);
  return (await a.json()) as { id: string; items: Array<{ assignmentId: string | null; position: number }> };
}

export interface Draft {
  id: string;
  status: string;
  humanLabel: string;
  body: string;
  unsure: Array<{ text: string; label: string; why: string }>;
  recipients: number | null;
  message?: string;
}

export async function draft(rq: APIRequestContext, t: Person, data: Record<string, unknown>) {
  return rq.post(NOTICES, { headers: bearer(t.token), data });
}

export async function draftOk(rq: APIRequestContext, t: Person, classId: string, refId: string, kind = 'due') {
  const r = await draft(rq, t, { classId, kind, refId });
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as Draft;
}

export async function send(rq: APIRequestContext, t: Person, id: string, body?: string) {
  return rq.post(`${NOTICES}/${id}/send`, { headers: bearer(t.token), data: body === undefined ? {} : { body } });
}

export async function aiScenario(rq: APIRequestContext, p: Person, scenario: string) {
  await hook(rq, 'ai', { userId: p.id, scenario });
}

export async function counter(
  rq: APIRequestContext,
  orgId: string,
  scope: 'class' | 'student' | 'teacher',
  scopeId: string,
  metric: string,
  n: number,
  daysAgo = 0,
) {
  await hook(rq, 's5e5/counter', { orgId, scope, scopeId, metric, n, daysAgo });
}

export async function readCounter(rq: APIRequestContext, orgId: string, scope: string, scopeId: string, metric: string) {
  const r = await rq.get(`${API}/test-hooks/s5e5/counter?orgId=${orgId}&scope=${scope}&scopeId=${scopeId}&metric=${metric}`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as { n: number }).n;
}

export interface Notification {
  id: string;
  kind: string;
  body: string;
}

export async function notifications(rq: APIRequestContext, p: Person): Promise<Notification[]> {
  const r = await rq.get(`${API}/api/v1/notifications`, { headers: bearer(p.token) });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { items: Notification[] }).items;
}

export const stamp = () => `lane-${randomUUID()}`;
