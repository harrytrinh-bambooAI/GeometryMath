import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';

export { CONFIRM_WORD, canConfirm } from './confirm.ts';

/** Mã người dùng của phiên hiện tại (ghi ở khóa 'geo.e1.me' lúc đăng nhập). */
export function currentUserId(): string | null {
  try {
    const raw = sessionStorage.getItem('geo.e1.me');
    if (!raw) return null;
    const me = JSON.parse(raw) as { userId?: string };
    return me.userId ?? null;
  } catch {
    return null;
  }
}

export const accountApi = {
  /** Xóa tài khoản của chính mình; idempotent theo requestId. */
  deleteAccount: (userId: string, password: string, confirm: string, requestId: string) =>
    api.post<{ deleted: boolean }>(`/api/v1/account/users/${encodeURIComponent(userId)}/delete`, {
      password,
      confirm,
      requestId,
    }),
};

export function fieldError(e: unknown, field: string): string | null {
  if (!(e instanceof ApiError)) return null;
  return e.details?.find((d) => d.field === field)?.message ?? null;
}

export function messageOf(e: unknown): string {
  return e instanceof ApiError && e.message ? e.message : 'Đã có lỗi xảy ra, vui lòng thử lại.';
}
