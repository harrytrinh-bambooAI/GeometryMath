import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../../support/fixtures.ts';
import { API, bearer, world, type Person, type World } from '../../s3e1/s3e1.ts';
import { bc01, rb01, type Graded, type Rubric } from '../s5e4.ts';
import { ack, aiOn, due, P1, P2, submitOk, waitOcr, type Submission } from '../submit/submit.ts';

/**
 * Tiện ích E2E hàng chờ chấm, chấm tay, trả bài (T-261128-60; US-S5E4-05, 06, 07). Bài nộp đi luồng thật của T-59
 * (tải theo đoạn → POST /api/v1/submissions → đọc chữ nền, AI giả lập). Thế giới riêng mỗi ca: cô Lan chủ 11A1 với 5 em
 * của F-S5-010 (Bình An, Chi Mai, Đức Huy, Gia Bảo, Hà My) để dòng tóm tắt khớp "1 chưa nộp"; thầy Khôi chủ 10B 3 em.
 * RB-01 (mẫu Tính toán): 1 Hướng 2 · 2 Các bước 4 · 3 Kết quả 2 · 4 Hình 1 · 5 Trình bày 1.
 */
export const FIVE = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My'];
export const QUEUE = `${API}/api/v1/teacher/grading/queue`;
export const GA = `${API}/api/v1/teacher/graded-assignments`;
export const SUB = `${API}/api/v1/submissions`;

export const MSG = {
  s37: (name: string, max: string) => `Điểm tiêu chí ${name} phải từ 0 đến ${max}, bước 0,25.`,
  s38: 'Đã duyệt điểm. Học sinh sẽ thấy khi thầy cô bấm Trả bài.',
  s39: (title: string) => `Em có bài mới được trả: ${title}.`,
  updated: (title: string) => `Điểm bài ${title} đã được thầy cô cập nhật`,
  waitReturn: 'Đang chờ thầy cô trả bài',
} as const;

export interface Ctx {
  w: World;
  lan: Person;
  khoi: Person;
  c11: { id: string };
  c10: { id: string };
  rb: Rubric;
  bc: Graded;
}

export async function gradingWorld(rq: APIRequestContext): Promise<Ctx> {
  const w = await world(rq, {
    teachers: ['Cô Lan', 'Thầy Khôi'],
    students: [],
    classes: [
      { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: FIVE },
      { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na', 'Tí Nị', 'Bo Bo'] },
    ],
  });
  const lan = w.people['Cô Lan']!;
  const khoi = w.people['Thầy Khôi']!;
  const c11 = w.classes['11A1']!;
  const c10 = w.classes['10B']!;
  const rb = await rb01(rq, lan);
  const bc = await bc01(rq, lan, c11.id, rb.id);
  return { w, lan, khoi, c11, c10, rb, bc };
}

/** Học sinh đọc thông báo riêng tư rồi nộp; chờ đọc chữ nền xong (bài rời trạng thái Đang xử lý). */
export async function submitAndRead(rq: APIRequestContext, teacher: Person, p: Person, assignmentId: string, files: Buffer[] = [P1, P2]): Promise<Submission> {
  await ack(rq, p);
  const s = await submitOk(rq, p, assignmentId, files);
  await waitOcr(rq, teacher, s.id);
  return s;
}

/**
 * Dữ liệu SC1 (F-S5-052…056): BN-01 Bình An 2 trang, BN-02 Chi Mai 3 trang, BN-03 Đức Huy "Chưa đọc được" (AI lỗi cả
 * lượt gọi lại), Hà My nộp muộn; Gia Bảo chưa nộp. AI chấm của lớp bật để đọc chữ nền chạy.
 */
export async function sc1(rq: APIRequestContext, c: Ctx) {
  const { w, lan, c11, bc } = c;
  await aiOn(rq, lan, c11.id);
  await due(rq, w.orgId, bc.id, 3600, true);
  const bn01 = await submitAndRead(rq, lan, w.people['Bình An']!, bc.id, [P1, P2]);
  const bn02 = await submitAndRead(rq, lan, w.people['Chi Mai']!, bc.id, [P1, P2, P2]);
  const huy = w.people['Đức Huy']!;
  await rq.post(`${API}/test-hooks/ai`, { data: { userId: huy.id, mode: 'error' } });
  let bn03: Submission;
  try {
    bn03 = await submitAndRead(rq, lan, huy, bc.id, [P1, P2]);
  } finally {
    await rq.post(`${API}/test-hooks/ai`, { data: { userId: huy.id, mode: 'ok' } });
  }
  await due(rq, w.orgId, bc.id, -420, true);
  const bn05 = await submitAndRead(rq, lan, w.people['Hà My']!, bc.id, [P1]);
  return { bn01, bn02, bn03, bn05 };
}

export async function ok<T>(r: APIResponse, status = 200): Promise<T> {
  expect(r.status(), await r.text()).toBe(status);
  return (await r.json()) as T;
}

export interface Item {
  submissionId: string;
  code: string;
  studentName: string;
  status: string;
  statusLabel: string;
  flags: Array<{ code: string; label: string }>;
  submittedAt: string;
  version: number;
}
export interface Queue {
  state: string;
  dueAt?: string;
  summary: { waiting: number; processing: number; notSubmitted: number; approved: number; returned: number };
  summaryLine: string;
  items: Item[];
}

export const queue = (rq: APIRequestContext, t: Person, q: Record<string, string>) =>
  rq.get(`${QUEUE}?${new URLSearchParams(q)}`, { headers: bearer(t.token) });

export interface Review extends Item {
  versionId: string;
  criteria: Array<{ ord: number; name: string; maxPoints: number }>;
  grade?: { seq: number; total: number; comment: string; criteria: Array<{ ord: number; points: number }> };
  nextSubmissionId: string | null;
  message?: string;
}

export const review = async (rq: APIRequestContext, t: Person, id: string) =>
  ok<Review>(await rq.get(`${API}/api/v1/teacher/grading/submissions/${id}`, { headers: bearer(t.token) }));

/** Điểm theo thứ tự RB-01: Hướng, Các bước, Kết quả, Hình, Trình bày. */
export const gradeRaw = (rq: APIRequestContext, t: Person, id: string, version: number, pts: number[], comment = '') =>
  rq.post(`${SUB}/${id}/grade`, {
    headers: bearer(t.token),
    data: { version, comment, criteria: pts.map((p, k) => ({ ord: k + 1, points: p })) },
  });

/** Duyệt đạt; trả bài nộp sau duyệt. */
export async function approve(rq: APIRequestContext, t: Person, id: string, pts: number[], comment = '') {
  const v = await review(rq, t, id);
  return ok<Review>(await gradeRaw(rq, t, id, v.version, pts, comment));
}

export const returnAll = (rq: APIRequestContext, t: Person, assignmentId: string) =>
  rq.post(`${GA}/${assignmentId}/return`, { headers: bearer(t.token) });
export const undo = (rq: APIRequestContext, t: Person, assignmentId: string) =>
  rq.post(`${GA}/${assignmentId}/return/undo`, { headers: bearer(t.token) });
export const result = (rq: APIRequestContext, p: Person, submissionId: string) =>
  rq.get(`${SUB}/${submissionId}/result`, { headers: bearer(p.token) });
export const solution = (rq: APIRequestContext, p: Person, assignmentId: string) =>
  rq.get(`${API}/api/v1/student/graded-assignments/${assignmentId}/solution`, { headers: bearer(p.token) });

export async function notes(rq: APIRequestContext, p: Person) {
  return (await ok<{ items: Array<{ kind: string; body: string }> }>(await rq.get(`${API}/api/v1/notifications`, { headers: bearer(p.token) }))).items;
}

export async function hookPost(rq: APIRequestContext, path: string, data: unknown) {
  const r = await rq.post(`${API}/test-hooks/s5e4/return/${path}`, { data });
  expect(r.status(), `móc ${path}`).toBe(200);
}
