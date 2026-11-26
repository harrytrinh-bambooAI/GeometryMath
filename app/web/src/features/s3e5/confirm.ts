/** US-S3E5-05: chữ xác nhận phải gõ đúng để mở nút xóa (BR-S3E5-50). */
export const CONFIRM_WORD = 'XÓA';

export function canConfirm(password: string, confirm: string): boolean {
  return password.length > 0 && confirm.trim() === CONFIRM_WORD;
}
