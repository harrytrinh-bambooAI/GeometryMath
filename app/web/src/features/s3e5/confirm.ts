/** US-S3E5-05: chữ xác nhận phải gõ đúng để mở nút xóa (BR-S3E5-50). */
export const CONFIRM_WORD = 'XÓA';

/** hasPassword = false (em vào lớp bằng tên, PO-72): chỉ cần gõ đúng chữ XÓA, không hỏi mật khẩu. */
export function canConfirm(password: string, confirm: string, hasPassword = true): boolean {
  return (!hasPassword || password.length > 0) && confirm.trim() === CONFIRM_WORD;
}
