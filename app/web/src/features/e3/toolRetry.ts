/**
 * Chính sách gửi lại lô thao tác trên hình (FIX4b m1, soát E7): chỉ gửi lại khi lỗi mạng, 5xx, 408 hoặc 429, tối đa
 * MAX_RETRIES lần; lỗi 4xx khác (học sinh rời lớp, bài đổi, lô sai) thì bỏ lô, không gửi lại mãi. Lỗi mạng khi thiết bị
 * đang ngoại tuyến không tính vào số lần (lô được gửi lại ngay khi có mạng, US-GEO-703-FC3).
 * Trả số lần đã thử sau lỗi này, hoặc null khi bỏ lô.
 */
export const MAX_RETRIES = 12;

export function nextAttempt(error: unknown, attempts: number, online: boolean): number | null {
  const status =
    typeof error === 'object' && error !== null && typeof (error as { status?: unknown }).status === 'number'
      ? (error as { status: number }).status
      : 0;
  if (status >= 400 && status < 500 && status !== 408 && status !== 429) return null;
  if (status === 0 && !online) return attempts;
  return attempts + 1 > MAX_RETRIES ? null : attempts + 1;
}
