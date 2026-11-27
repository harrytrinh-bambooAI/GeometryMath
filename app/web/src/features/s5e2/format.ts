import { ApiError } from '../../shared/api/errors.ts';

/** S5-E2: nhãn và định dạng thuần (không gọi mạng, test được bằng node:test). */
export const TYPE_LABEL: Record<string, string> = {
  tn4: 'Trắc nghiệm 4 lựa chọn',
  'dung-sai': 'Đúng/sai',
  'tra-loi-ngan': 'Trả lời ngắn',
  'tu-luan': 'Tự luận',
};

export const MACHINE_LABEL: Record<string, string> = {
  'da-kiem-chung': 'Đã kiểm chứng',
  'can-xem-lai': 'Cần xem lại',
};

/** Thông điệp lỗi của một ô nhập (details[].field), nếu có. */
export function fieldError(e: unknown, field: string): string | null {
  if (!(e instanceof ApiError)) return null;
  return e.details?.find((d) => d.field === field)?.message ?? null;
}

/** Điểm dạng Việt Nam: 1,5 · 0,25. */
export const vn = (n: number) => String(Math.round(n * 1000) / 1000).replace('.', ',');
