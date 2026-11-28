/** S2-E3 · quy tắc thuần của màn "Thêm đề" (C2-02), không chạm DOM. */

export const MIN_CHARS = 10;
export const MAX_CHARS = 2000;
/** Ảnh tối đa 5 MB = 5 242 880 byte (BR-S2E3-06, QA-S2E3-03). */
export const MAX_IMAGE_BYTES = 5_242_880;
/** Mỗi đoạn tải ảnh (thân yêu cầu của máy chủ ≤ 1 MB). */
export const CHUNK_BYTES = 1_000_000;

export const MSG = {
  empty: 'Hãy dán hoặc gõ đề',
  short: 'Đề quá ngắn (tối thiểu 10 ký tự)',
  long: 'Đề tối đa 2000 ký tự',
  oneImage: 'Mỗi lần tải một ảnh',
  imageTooBig: 'Ảnh tối đa 5 MB',
  badType: 'Chỉ nhận ảnh JPG hoặc PNG',
  noText: 'Máy không đọc được chữ trong ảnh, hãy gõ lại đề',
  rate: 'Bạn thêm đề hơi nhanh, thử lại sau',
  aiFigure: 'Cần xem lại · hình do AI dựng',
  askAiFigure: 'Hình do AI dựng, chưa kiểm chứng. Vẫn giao?',
  textOnly: 'bản nháp chữ',
  unsureHint: 'Ô vàng là chỗ AI chưa chắc. Nhãn "Cần xem lại" giữ nguyên đến khi máy kiểm lại đạt.',
} as const;

/** Số ký tự (điểm mã) của đề sau khi cắt khoảng trắng đầu cuối (BR-S2E3-02). */
export function charCount(text: string): number {
  return [...text.trim()].length;
}

/** Lỗi độ dài của đề; null = bấm "Dựng hình" được. */
export function lengthIssue(text: string): string | null {
  const n = charCount(text);
  if (n === 0) return MSG.empty;
  if (n < MIN_CHARS) return MSG.short;
  if (n > MAX_CHARS) return MSG.long;
  return null;
}

/** Chọn ảnh: chỉ ảnh đầu tiên; cảnh báo khi chọn nhiều ảnh hoặc ảnh quá 5 MB (BR-S2E3-06). */
export function pickImage<T extends { size: number }>(files: readonly T[]): { file: T | null; message: string | null } {
  if (files.length === 0) return { file: null, message: null };
  const file = files[0]!;
  if (file.size > MAX_IMAGE_BYTES) return { file: null, message: MSG.imageTooBig };
  return { file, message: files.length > 1 ? MSG.oneImage : null };
}

/** Nhãn trạng thái một bước (chờ / đang chạy / xong / lỗi). */
export function stageText(state: string): string {
  switch (state) {
    case 'running':
      return 'Đang chạy…';
    case 'done':
      return 'Xong';
    case 'error':
      return 'Lỗi';
    default:
      return 'Chờ';
  }
}
