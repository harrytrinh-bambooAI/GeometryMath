import { ApiError } from '../../shared/api/errors.ts';

/** Thông điệp cho người dùng (nguyên văn work/05_dictionary.md). */
export const MSG = {
  '02': 'Phiên đã hết. Vui lòng vào lại.',
  '03': 'Bạn không có quyền thực hiện thao tác này.',
  '04': 'Không tìm thấy nội dung này.',
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '09': 'Em hãy nhập tên hiển thị.',
  '10': 'Tên hiển thị tối đa 60 ký tự.',
  '11': 'Mã lớp không đúng. Em hỏi lại giáo viên nhé.',
  '12': 'Mã giáo viên không đúng.',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '57': 'Hệ thống đang đông. Vui lòng thử lại sau vài giây.',
  '59': 'Đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau ít phút.',
} as const;

/** Câu hiện trên màn cho một lỗi máy chủ (COM_LOI): mã lỗi → MSG-GEO-nn. */
export function errorText(e: unknown): string {
  if (!(e instanceof ApiError)) return 'Đã có lỗi xảy ra, vui lòng thử lại.';
  switch (e.code) {
    case 'UNAUTHENTICATED':
      return MSG['02'];
    case 'FORBIDDEN':
      return MSG['03'];
    case 'NOT_FOUND':
      return MSG['04'];
    case 'INVALID_CURSOR':
      return MSG['05'];
    case 'SERVICE_OVERLOADED':
      return MSG['57'];
    case 'RATE_LIMITED':
      return e.message || MSG['57'];
    default:
      return e.message;
  }
}

/** Thông điệp gắn với một ô nhập (details[].field) nếu có. */
export function fieldError(e: unknown, field: string): string | null {
  if (!(e instanceof ApiError)) return null;
  const d = e.details.find((x) => x.field === field);
  return d ? d.message : null;
}
