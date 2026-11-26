import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import type { CodeSession } from '../../shared/auth/codeSession.ts';

/** Gọi máy chủ S2-E1 (đăng ký, đăng nhập bằng mã, khôi phục). Thân yêu cầu chỉ chứa trường PO-61 cho phép. */
export type Role = 'teacher' | 'student';

export interface Registered extends CodeSession {
  recoveryCode: string;
}

export interface LoginResult extends Partial<CodeSession> {
  role: 'TEACHER' | 'STUDENT';
  userCode: string;
  mustChangePassword: boolean;
}

export interface Notice {
  version: string;
  title: string;
  body: string;
}

export const s2e1Api = {
  notice: () => api.get<Notice>('/api/v1/auth/privacy-notice', { anonymous: true }),
  register: (b: { displayName: string; role: Role; password: string; privacyAck: boolean; privacyVersion: string }) =>
    api.post<Registered>('/api/v1/auth/register', b, { anonymous: true }),
  login: (userCode: string, password: string) =>
    api.post<LoginResult>('/api/v1/auth/code-login', { userCode, password }, { anonymous: true }),
  firstPassword: (userCode: string, password: string, newPassword: string) =>
    api.post<LoginResult>('/api/v1/auth/first-password', { userCode, password, newPassword }, { anonymous: true }),
  recover: (userCode: string, recoveryCode: string, newPassword: string) =>
    api.post<{ userCode: string; role: 'TEACHER' | 'STUDENT'; recoveryCode: string }>(
      '/api/v1/auth/recover',
      { userCode, recoveryCode, newPassword },
      { anonymous: true },
    ),
};

/** Lỗi gắn ô (details[].field) hoặc null. */
export function fieldErrorOf(e: unknown, field: string): string | null {
  if (!(e instanceof ApiError)) return null;
  return e.details?.find((d) => d.field === field)?.message ?? null;
}

export function messageOf(e: unknown): string {
  if (e instanceof ApiError && e.message) return e.message;
  return 'Đã có lỗi xảy ra, vui lòng thử lại.';
}

export function codeOf(e: unknown): string {
  return e instanceof ApiError ? e.code : 'UNKNOWN';
}

/** Kiểm phía giao diện, cùng luật với máy chủ (BR-S2E1-02, -03). */
export const MSG = {
  nameEmpty: 'Hãy nhập tên hiển thị.',
  nameLong: 'Tên hiển thị tối đa 60 ký tự.',
  password: 'Mật khẩu từ 8 đến 64 ký tự.',
  codeEmpty: 'Hãy nhập mã người dùng.',
  passwordEmpty: 'Hãy nhập mật khẩu.',
  recoveryEmpty: 'Hãy nhập mã khôi phục.',
  expired: 'Phiên đã hết. Vui lòng vào lại.',
} as const;

export function checkName(name: string): string | null {
  const n = name.trim();
  if (!n) return MSG.nameEmpty;
  if ([...n].length > 60) return MSG.nameLong;
  return null;
}

export function checkPassword(pw: string): string | null {
  const n = [...pw].length;
  return n < 8 || n > 64 ? MSG.password : null;
}
