import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';

/** Gọi máy chủ S3-E1 (lớp, mã lớp, duyệt, đặt lại mật khẩu) và công tắc AI lớp (US-S3E5-04). */
export interface ClassLine {
  id: string;
  name: string;
  grade: number;
  academicYear: string | null;
  code: string;
  status: 'active' | 'archived';
  activeCount: number;
  pendingCount: number;
}

export interface ClassDetail extends ClassLine {
  codeJoinEnabled: boolean;
  approvalRequired: boolean;
  aiEnabled: boolean;
}

export interface Pending {
  userId: string;
  displayName: string;
  userCode: string;
  requestedAt: string;
}

export interface Student {
  userId: string;
  displayName: string;
  userCode: string;
  status: 'active' | 'left';
  joinedAt: string;
}

export interface MyClass {
  className: string;
  status: 'pending' | 'active' | 'left' | 'rejected';
}

const base = '/api/v1/teacher/my-classes';
const enc = encodeURIComponent;

export const s3e1Api = {
  list: () => api.get<{ items: ClassLine[] }>(base),
  create: (b: { name: string; grade: number; academicYear: string }) => api.post<ClassLine>(base, b),
  detail: (id: string) => api.get<ClassDetail>(`${base}/${enc(id)}`),
  resetCode: (id: string) => api.post<ClassDetail>(`${base}/${enc(id)}/code/reset`),
  settings: (id: string, b: { codeJoinEnabled?: boolean; aiEnabled?: boolean }) =>
    api.put<ClassDetail>(`${base}/${enc(id)}/settings`, b),
  pending: (id: string) => api.get<{ items: Pending[] }>(`${base}/${enc(id)}/pending`),
  students: (id: string, cursor?: string) =>
    api.get<{ items: Student[]; nextCursor: string | null }>(
      `${base}/${enc(id)}/students?limit=100${cursor ? `&cursor=${enc(cursor)}` : ''}`,
    ),
  decide: (id: string, userId: string, decision: 'approve' | 'reject' | 'remove') =>
    api.post<{ userId: string; status: string }>(`${base}/${enc(id)}/members/${enc(userId)}/${decision}`),
  resetPassword: (id: string, userId: string) =>
    api.post<{ tempPassword: string; expiresAt: string }>(`${base}/${enc(id)}/students/${enc(userId)}/password-reset`),
  join: (code: string) => api.post<{ status: string; className: string; message: string }>('/api/v1/student/class-joins', { code }),
  myClasses: () => api.get<{ items: MyClass[] }>('/api/v1/student/classes'),
};

export const MSG = {
  s01: 'Mã lớp chưa đúng hoặc lớp đã khóa. Em hỏi lại thầy cô nhé.',
  s02: 'Em đang chờ thầy cô duyệt. Trong lúc chờ, em vẫn dùng Tự học.',
  s03: 'Em đã ở trong lớp này rồi.',
  s04: 'Tên lớp chưa có hoặc dài quá 60 ký tự.',
  s08: 'Đã thử quá nhiều lần. Vui lòng thử lại sau ít phút.',
  rejected: 'Em chưa vào được lớp này. Em hỏi lại thầy cô nhé.',
};

export function fieldError(e: unknown, field: string): string | null {
  return e instanceof ApiError ? (e.details.find((d) => d.field === field)?.message ?? null) : null;
}

export function messageOf(e: unknown): string {
  return e instanceof ApiError && e.message ? e.message : 'Đã có lỗi xảy ra, vui lòng thử lại.';
}

export const GRADES = [6, 7, 8, 9, 10, 11, 12];
