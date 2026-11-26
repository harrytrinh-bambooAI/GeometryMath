import { randomUUID } from 'node:crypto';
import type { APIRequestContext, APIResponse, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

export { API, bearer };

/**
 * Tiện ích E2E của S3-E1 / S3-E5 (T-261126-28). Mỗi ca dựng "thế giới" riêng qua móc /test-hooks/s3e1/world (giáo viên,
 * lớp chặng 3 có mã HK-, học sinh có mã người dùng ở các trạng thái, mật khẩu chung MatKhau#2026) nên các ca chạy song
 * song không giẫm lên nhau hay lên dữ liệu mẫu chung F-S3 (910_s3_core.sql). Ca chỉ đọc dùng thẳng F-S3.
 */
export const PASSWORD = 'MatKhau#2026';

export interface Person {
  id: string;
  displayName: string;
  userCode: string;
  password: string;
  token: string;
  role: 'TEACHER' | 'STUDENT';
}
export interface Klass {
  id: string;
  name: string;
  code: string;
}
export interface World {
  orgId: string;
  people: Record<string, Person>;
  classes: Record<string, Klass>;
}
export interface ClassSpec {
  key?: string;
  teacher: string;
  name: string;
  grade?: number;
  active?: string[];
  pending?: string[];
  left?: string[];
  rejected?: string[];
}

export const MSG = {
  s01: 'Mã lớp chưa đúng hoặc lớp đã khóa. Em hỏi lại thầy cô nhé.',
  s02: 'Em đang chờ thầy cô duyệt. Trong lúc chờ, em vẫn dùng Tự học.',
  s03: 'Em đã ở trong lớp này rồi.',
  s04: 'Tên lớp chưa có hoặc dài quá 60 ký tự.',
  s05: 'Mật khẩu tạm đã hết hạn. Nhờ thầy cô đặt lại.',
  s07: 'Không tìm thấy nội dung này.',
  s08: 'Đã thử quá nhiều lần. Vui lòng thử lại sau ít phút.',
  s83: 'Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn.',
  s84: 'Em cần đọc thông báo riêng tư để tiếp tục.',
} as const;

export const CLASS_CODE = /^HK-[A-HJ-NP-Z2-9]{4}$/;
export const TEMP_PASSWORD = /^[a-hj-km-np-z2-9]{3}-[a-hj-km-np-z2-9]{3}-[a-hj-km-np-z2-9]{2}$/;

/** Thế giới mặc định: cô Lan (11A1: Bình An, Chi Mai Đang học; Thu Hà, Hoàng Nam Chờ duyệt; Văn Tú Đã rời),
 * thầy Khôi (10B: Mi Na, Chi Mai), học sinh Gia Bảo chưa vào lớp nào. */
export const DEFAULT: { teachers: string[]; students: string[]; classes: ClassSpec[] } = {
  teachers: ['Cô Lan', 'Thầy Khôi'],
  students: ['Gia Bảo'],
  classes: [
    { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An', 'Chi Mai'], pending: ['Thu Hà', 'Hoàng Nam'], left: ['Văn Tú'] },
    { key: '11A2', teacher: 'Cô Lan', name: '11A2', active: ['Quốc Bảo'] },
    { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na', 'Chi Mai'] },
  ],
};

export async function world(
  request: APIRequestContext,
  spec: { teachers?: string[]; students?: string[]; classes?: ClassSpec[] } = DEFAULT,
): Promise<World> {
  const r = await request.post(`${API}/test-hooks/s3e1/world`, { data: spec });
  expect(r.status(), 'móc thế giới S3-E1 (máy chủ profile dev)').toBe(200);
  return (await r.json()) as World;
}

const T = '/api/v1/teacher/my-classes';
export const teacherApi = {
  list: (rq: APIRequestContext, t: string) => rq.get(`${API}${T}`, { headers: bearer(t) }),
  create: (rq: APIRequestContext, t: string, data: Record<string, unknown>) => rq.post(`${API}${T}`, { headers: bearer(t), data }),
  detail: (rq: APIRequestContext, t: string, c: string) => rq.get(`${API}${T}/${c}`, { headers: bearer(t) }),
  resetCode: (rq: APIRequestContext, t: string, c: string) => rq.post(`${API}${T}/${c}/code/reset`, { headers: bearer(t) }),
  settings: (rq: APIRequestContext, t: string, c: string, data: unknown) => rq.put(`${API}${T}/${c}/settings`, { headers: bearer(t), data }),
  archive: (rq: APIRequestContext, t: string, c: string) => rq.post(`${API}${T}/${c}/archive`, { headers: bearer(t) }),
  pending: (rq: APIRequestContext, t: string, c: string) => rq.get(`${API}${T}/${c}/pending`, { headers: bearer(t) }),
  students: (rq: APIRequestContext, t: string, c: string, q = '') => rq.get(`${API}${T}/${c}/students${q}`, { headers: bearer(t) }),
  student: (rq: APIRequestContext, t: string, c: string, u: string) => rq.get(`${API}${T}/${c}/students/${u}`, { headers: bearer(t) }),
  decide: (rq: APIRequestContext, t: string, c: string, u: string, d: 'approve' | 'reject' | 'remove') =>
    rq.post(`${API}${T}/${c}/members/${u}/${d}`, { headers: bearer(t) }),
  resetPassword: (rq: APIRequestContext, t: string, c: string, u: string) =>
    rq.post(`${API}${T}/${c}/students/${u}/password-reset`, { headers: bearer(t) }),
};

export const join = (rq: APIRequestContext, t: string, code: string) =>
  rq.post(`${API}/api/v1/student/class-joins`, { headers: bearer(t), data: { code } });
export const myClasses = (rq: APIRequestContext, t: string) => rq.get(`${API}/api/v1/student/classes`, { headers: bearer(t) });
export const practiceSets = (rq: APIRequestContext, t: string) => rq.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(t) });

export async function codeLogin(rq: APIRequestContext, userCode: string, password: string) {
  return rq.post(`${API}/api/v1/auth/code-login`, { data: { userCode, password } });
}

export async function hook(rq: APIRequestContext, path: string, data: unknown) {
  const r = await rq.post(`${API}/test-hooks/${path}`, { data });
  expect(r.status(), `móc ${path}`).toBe(200);
  return (await r.json()) as Record<string, unknown>;
}

export async function denials(rq: APIRequestContext, actorId: string): Promise<number> {
  const r = await rq.get(`${API}/test-hooks/s3e1/denials?actorId=${actorId}`);
  return ((await r.json()) as { count: number }).count;
}

export async function err(r: APIResponse) {
  return (await r.json()) as { code: string; message: string; details?: Array<{ field: string; message: string }> };
}

/** Giáo viên soạn và giao một bộ (S2-E8) gồm các đề kho 's3_tuan' cho lớp. */
export async function assignSet(rq: APIRequestContext, teacher: Person, classId: string, title = 'Bộ bài tuần 15', codes = ['TUAN-11', 'TUAN-12']) {
  const p = await rq.get(`${API}/api/v1/teacher/practice/problems`, { headers: bearer(teacher.token) });
  expect(p.status()).toBe(200);
  const items = ((await p.json()) as { items: Array<{ code: string; problemVersionId: string }> }).items;
  const pick = codes.map((c) => items.find((i) => i.code === c)?.problemVersionId).filter(Boolean);
  expect(pick.length, 'đề kho s3_tuan').toBe(codes.length);
  const d = await rq.post(`${API}/api/v1/teacher/practice-sets`, {
    headers: bearer(teacher.token),
    data: {
      title,
      releaseAt: new Date(Date.now() + 7_200_000).toISOString(),
      items: pick.map((v) => ({ problemVersionId: v, answerKind: 'exact', tolerance: null, maxAttempts: 3, releaseAt: null })),
    },
  });
  expect(d.status(), await d.text()).toBe(201);
  const set = (await d.json()) as { id: string };
  const a = await rq.post(`${API}/api/v1/teacher/practice-sets/${set.id}/assign`, {
    headers: bearer(teacher.token),
    data: { classId, confirmPastRelease: false },
  });
  expect(a.status(), await a.text()).toBe(200);
  return (await a.json()) as { id: string; title: string; items: Array<{ assignmentId: string; position: number }> };
}

export const ask = (rq: APIRequestContext, t: string, assignmentId: string, question = 'Em nên bắt đầu từ đâu?') =>
  rq.post(`${API}/api/v1/student/assignments/${assignmentId}/questions`, {
    headers: bearer(t),
    data: { question, requestId: randomUUID() },
  });

export async function aiCalls(rq: APIRequestContext, userId: string): Promise<number> {
  const r = await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as unknown[]).length;
}

/** Đăng nhập sẵn trên trình duyệt bằng thẻ của người trong thế giới. */
export async function signIn(page: Page, p: Person) {
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [p.token, JSON.stringify({ role: p.role, userId: p.id, displayName: p.displayName })] as const,
  );
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
}
