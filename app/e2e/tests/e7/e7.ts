import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e4/e4.ts';

/**
 * Tiện ích E2E của E7 (Theo dõi lớp, ghi sự kiện). Dữ liệu vàng: artifacts/GEO/work/fixture.md §4 (F-GEO-050…058).
 * Mỗi ca dựng GIÁO VIÊN + LỚP RIÊNG bằng móc `POST /test-hooks/e7/sandbox` (tổ chức "Nhóm thử E7", seed 700) với đúng số
 * liệu của fixture, nên ca đổi trạng thái (Hà mở bài, Mở ngay, hỏi thêm) không giẫm nhau giữa các dự án Playwright và
 * không đổi dữ liệu vàng mà E1…E6 đọc.
 */
export { API, bearer };

export interface Person {
  id: string;
  token: string;
}
export interface Box {
  orgId: string;
  teacher: Person;
  classes: Array<{ id: string; name: string; students: Record<string, Person>; assignments: Record<string, string> }>;
}
export interface Spec {
  classes: Array<{ name?: string; level?: string; students: string[]; assignments?: Array<{ key: string; code: string; release?: string }> }>;
  progress?: Array<{ assignment: string; student: string; hint: number; asks: number; tools: number; mins: number; solution?: boolean }>;
  questions?: Array<{ assignment: string; student: string; text: string; minutesAgo: number }>;
}

export const NAMES = ['An', 'Bình', 'Chi', 'Dũng', 'Hà'];

/** F-GEO-050…053: An 2 gợi ý, 3 câu, 4 thao tác, 18 phút; Bình 1/2/2/11; Chi 0/0/6/9; Dũng 2/2/3/21; Hà chưa mở. */
export const PROGRESS_BG1 = [
  { assignment: 'BG-1', student: 'An', hint: 2, asks: 3, tools: 4, mins: 18 },
  { assignment: 'BG-1', student: 'Bình', hint: 1, asks: 2, tools: 2, mins: 11 },
  { assignment: 'BG-1', student: 'Chi', hint: 0, asks: 0, tools: 6, mins: 9 },
  { assignment: 'BG-1', student: 'Dũng', hint: 2, asks: 2, tools: 3, mins: 21 },
];
/** F-GEO-055…058: bảy câu hỏi của lớp trên BG-1 (cũ trước). */
export const QUESTIONS_BG1 = [
  ['An', 'Vì sao A là hình chiếu của S?'],
  ['Dũng', 'Vì sao A là hình chiếu của S?'],
  ['Bình', 'Góc cần tìm là góc nào trên hình?'],
  ['Bình', 'Góc cần tìm là góc nào trên hình?'],
  ['An', 'Góc cần tìm là góc nào trên hình?'],
  ['An', 'Cho em xin đáp số luôn được không?'],
  ['Dũng', 'Tan của một góc là gì?'],
].map(([student, text], k) => ({ assignment: 'BG-1', student: student!, text: text!, minutesAgo: 30 - k }));

/** Lớp 11A1 năm em, BG-1 (HKG-01) Sắp mở, BG-2 Chưa đặt giờ, BG-3 Đã mở; tiến độ, câu hỏi F-GEO-050…058 trên BG-1. */
export function golden(extra: Partial<Spec> = {}): Spec {
  return {
    classes: [
      {
        name: '11A1',
        students: NAMES,
        assignments: [
          { key: 'BG-1', code: 'HKG-01', release: 'upcoming' },
          { key: 'BG-2', code: 'HKG-02', release: 'unset' },
          { key: 'BG-3', code: 'HKG-03', release: 'open' },
        ],
      },
      ...(extra.classes ?? []),
    ],
    progress: [...PROGRESS_BG1, ...(extra.progress ?? [])],
    questions: [...QUESTIONS_BG1, ...(extra.questions ?? [])],
  };
}

export async function box(request: APIRequestContext, spec: Spec): Promise<Box> {
  const r = await request.post(`${API}/test-hooks/e7/sandbox`, { data: spec });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Box;
}
export const asg = (b: Box, key: string) => b.classes.flatMap((c) => Object.entries(c.assignments)).find(([k]) => k === key)![1];
export const st = (b: Box, name: string, cls = 0) => b.classes[cls]!.students[name]!;

export interface MonitorJson {
  studentCount: number;
  openedCount: number;
  hintUsers: number;
  questionCount: number;
  figureMinutes: number;
  releaseState: string;
  students: Array<{ userId: string; displayName: string; opened: boolean; hintLevel: number; askCount: number; toolCount: number; activeMinutes: number; status: string; k: number }>;
  questionGroups: Array<{ question: string; names: string[]; count: number }>;
  askedQuestions: Array<{ userId: string; question: string }>;
}
export async function monitor(request: APIRequestContext, token: string, id: string): Promise<MonitorJson> {
  const r = await request.get(`${API}/api/v1/teacher/assignments/${id}/monitor`, { headers: bearer(token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as MonitorJson;
}
/** Đọc lại Theo dõi lớp sau khi bỏ đệm 5 giây (như máy chủ vừa khởi động lại). */
export async function freshMonitor(request: APIRequestContext, token: string, id: string): Promise<MonitorJson> {
  expect((await request.post(`${API}/test-hooks/e7/restart`, { data: { assignmentId: id } })).status()).toBe(200);
  return monitor(request, token, id);
}
export const row = (m: MonitorJson, name: string) => m.students.find((s) => s.displayName === name)!;

export async function events(request: APIRequestContext, assignmentId: string, userId: string): Promise<Record<string, number>> {
  const r = await request.get(`${API}/test-hooks/e7/events?assignmentId=${assignmentId}&userId=${userId}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as Record<string, number>;
}

export const send = {
  open: (rq: APIRequestContext, t: string, id: string, requestId = crypto.randomUUID()) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/open`, { headers: bearer(t), data: { requestId } }),
  tools: (rq: APIRequestContext, t: string, id: string, tools: number[], batchId = crypto.randomUUID(), extra: Record<string, unknown> = {}) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/figure-events`, {
      headers: bearer(t),
      data: { batchId, events: tools.map((tool) => ({ tool, at: new Date().toISOString() })), ...extra },
    }),
};

/** Đăng nhập trình duyệt bằng thẻ (giáo viên hoặc học sinh của lớp riêng). */
export async function signInAs(page: import('@playwright/test').Page, p: Person, displayName: string, role: 'teacher' | 'student') {
  const me = { role, userId: p.id, displayName, className: '11A1' };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [p.token, JSON.stringify(me)] as const,
  );
}
