import { apiRequest } from '../../shared/api/client.ts';
import type { AuditType } from './format.ts';

/** S3-E4 — máy khách API: Tổng quan giáo viên, Theo dõi nhanh (Mở ngay, Đổi giờ), nhật ký lớp, thông báo trong ứng dụng. */

const enc = encodeURIComponent;

export type ReleaseState = 'unset' | 'upcoming' | 'open';

export interface AssignedLine {
  setId: string;
  title: string;
  classId: string;
  className: string;
  dueAt?: string | null;
  overdue: boolean;
  notOpenedCount: number;
  releaseState: ReleaseState;
  releaseAt?: string | null;
}
export interface Overview {
  hasClasses: boolean;
  assigned: AssignedLine[];
  pending: { total: number; byClass: Array<{ classId: string; className: string; count: number }> };
  serverNow: string;
}
export interface Follow {
  setId: string;
  title: string;
  status: string;
  classId: string;
  className: string;
  dueAt?: string | null;
  overdue: boolean;
  releaseState: ReleaseState;
  releaseAt?: string | null;
  version: number;
  notOpened: Array<{ displayName: string; userCode?: string | null }>;
  serverNow: string;
}
export interface AuditEvent {
  id: number;
  type: AuditType;
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
export interface Notification {
  id: string;
  kind: 'new_set' | 'solution_open' | 'release_changed' | 'due_soon'; // due_soon: nhắc sắp hết hạn US-S3E6-03
  body: string;
  setId?: string | null;
  createdAt: string;
  read: boolean;
}

const T = '/api/v1/teacher';
const N = '/api/v1/notifications';

export const overviewApi = {
  overview: (signal?: AbortSignal) => apiRequest<Overview>(`${T}/overview`, { signal }),
  follow: (setId: string, signal?: AbortSignal) => apiRequest<Follow>(`${T}/assignment-sets/${enc(setId)}/follow`, { signal }),
  /** Không gửi giờ của thiết bị: giờ mở là giờ máy chủ lúc xử lý (BR-S3E4-11). */
  openNow: (setId: string, requestKey: string, version: number) =>
    apiRequest<Follow>(`${T}/assignment-sets/${enc(setId)}/open-now`, { method: 'POST', body: { requestKey, version } }),
  changeTime: (setId: string, releaseAt: string, requestKey: string, version: number) =>
    apiRequest<Follow>(`${T}/assignment-sets/${enc(setId)}/release-time`, { method: 'POST', body: { releaseAt, requestKey, version } }),
  audit: (classId: string, signal?: AbortSignal) => apiRequest<{ items: AuditEvent[] }>(`${T}/classes/${enc(classId)}/audit-events`, { signal }),
};

export const notifyApi = {
  list: (cursor?: string | null, signal?: AbortSignal) =>
    apiRequest<{ items: Notification[]; nextCursor?: string | null }>(N, { query: cursor ? { cursor } : undefined, signal }),
  unread: (signal?: AbortSignal) => apiRequest<{ count: number }>(`${N}/unread-count`, { signal }),
  read: (id: string) => apiRequest<Notification>(`${N}/${enc(id)}/read`, { method: 'POST' }),
  readAll: () => apiRequest<{ updated: number }>(`${N}/read-all`, { method: 'POST' }),
};

/** Thông điệp MSG-S3-61…67 (stories_ac.md S3-E4). */
export const MSG = {
  s61: 'Lớp sẽ thấy lời giải ngay. Mở ngay?',
  s62: 'Chưa gửi được. Kiểm tra mạng rồi thử lại.',
  s63: 'Giờ mới phải sau thời điểm hiện tại. Muốn mở ngay, dùng Mở ngay.',
  s64: 'Đã mở',
  s65: 'Chưa có việc cần làm hôm nay.',
  s66: 'Bạn chưa có lớp. Tạo lớp đầu tiên.',
  s67: 'Lời giải đã mở, không đổi giờ được.',
} as const;

export const RELEASE_LABEL: Record<ReleaseState, string> = {
  unset: 'Chưa đặt giờ',
  upcoming: 'Sắp mở',
  open: MSG.s64,
};
