/** Logic thuần của màn kho tri thức (T-261128-56). */

export type KbStatus = 'draft' | 'pending' | 'active' | 'archived';

export const MAX_BYTES = 10 * 1024 * 1024;
export const ACCEPT = '.docx,.pdf,.md';
/** MSG-S5-22. */
export const MSG_22 = 'Tệp tối đa 10 MB, định dạng docx, pdf hoặc md.';

/** Kiểm trước ở trình duyệt (máy chủ kiểm lại): null = được tải. */
export function fileProblem(name: string, size: number): string | null {
  const ext = name.toLowerCase().split('.').pop() ?? '';
  if (!['docx', 'pdf', 'md'].includes(ext) || name.indexOf('.') < 0) return MSG_22;
  if (size < 1 || size > MAX_BYTES) return MSG_22;
  return null;
}

/** Thao tác kế tiếp được phép theo trạng thái phiên bản (Nháp → Chờ duyệt → Đang dùng → Lưu trữ). */
export function nextAction(status: KbStatus): 'submit' | 'activate' | null {
  if (status === 'draft') return 'submit';
  if (status === 'pending') return 'activate';
  return null;
}

/** Hạn hiệu lực: ngày (yyyy-mm-dd, giờ Việt Nam) → hết 23:59:59 ngày đó, ISO UTC; rỗng = không hạn. */
export function endOfDayVn(date: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return null;
  const t = Date.parse(`${date}T23:59:59+07:00`);
  return Number.isNaN(t) ? null : new Date(t).toISOString();
}

export function statusChip(status: KbStatus): string {
  return { draft: 'chip', pending: 'chip warn', active: 'chip ok', archived: 'chip muted' }[status];
}
