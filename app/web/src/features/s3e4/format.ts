import { formatVnDate, formatVnTime } from '../../shared/time/vnTime.ts';

/** S3-E4 — hàm thuần của Tổng quan, Theo dõi nhanh, Thông báo (kiểm bằng node:test ở format.test.ts). */

/** Số lớn hiển thị "99+" từ 100 trở lên (BR-S3E4-03, BR-S3E4-23; QA-S3E4-03). */
export function badge(n: number): string {
  return n >= 100 ? '99+' : String(n);
}

/** "HH:mm, dd/MM/yyyy" giờ Việt Nam (quy ước chung của AC chặng 3). */
export function vnAt(iso: string): string {
  return `${formatVnTime(iso)}, ${formatVnDate(iso)}`;
}

/** Giờ tối thiểu của ô "Đổi giờ": giờ máy chủ + 1 phút, làm tròn lên phút (BR-S3E4-12). */
export function minReleaseIso(serverNowIso: string): string {
  const t = new Date(serverNowIso).getTime() + 60_000;
  return new Date(Math.ceil(t / 60_000) * 60_000).toISOString();
}

export type AuditType = 'EARLY_RELEASE' | 'RELEASE_TIME_CHANGE' | 'PASSWORD_RESET';
export const AUDIT_LABEL: Record<AuditType, string> = {
  EARLY_RELEASE: 'Mở ngay',
  RELEASE_TIME_CHANGE: 'Đổi giờ',
  PASSWORD_RESET: 'Đặt lại mật khẩu',
};

/** Dòng nhật ký: "21:00, 05/12/2026 · Mở ngay · GV-… · trước 08:00, 06/12/2026 → sau 21:00, 05/12/2026". */
export function auditLine(e: { type: AuditType; actorCode?: string | null; targetType: string; targetId: string; before?: string | null; after?: string | null; at: string }): string {
  const parts = [vnAt(e.at), AUDIT_LABEL[e.type] ?? e.type];
  if (e.actorCode) parts.push(e.actorCode);
  if (e.type === 'PASSWORD_RESET') parts.push(`cho ${e.targetId}`);
  else parts.push(`trước ${e.before ? vnAt(e.before) : 'chưa đặt'} → sau ${e.after ? vnAt(e.after) : 'chưa đặt'}`);
  return parts.join(' · ');
}
