/** Bộ lọc loại hình (trùng kiểu KindFilter của api.ts; tệp này thuần để chạy bằng node --test). */
export type KindFilter = 'all' | 'space' | 'plane';

/** Câu chữ của Kho đề (stories_ac S2-E6, nguyên văn). */
export const MSG = {
  emptyMine: 'Chưa có đề riêng',
  emptyLibrary: 'Kho chung chưa có đề',
  noMatch: 'Không có đề phù hợp',
  invalidCursor: 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  approveFirst: 'Hãy duyệt lời giải trước',
  reasonRange: 'Lý do từ 5 đến 500 ký tự',
};

export const KIND_FILTERS: ReadonlyArray<{ value: KindFilter; label: string }> = [
  { value: 'all', label: 'Tất cả' },
  { value: 'space', label: 'Không gian' },
  { value: 'plane', label: 'Phẳng' },
];

/** Lý do từ chối 5…500 ký tự (đếm theo ký tự Unicode, bỏ khoảng trắng hai đầu; BR-S2E6-08). */
export function reasonError(reason: string): string | null {
  const n = [...reason.trim()].length;
  return n < 5 || n > 500 ? MSG.reasonRange : null;
}

/** Lớp màu của chip nhãn: nhãn máy, nhãn người, trạng thái kho. */
export function chipTone(label: string): string {
  switch (label) {
    case 'Đã kiểm chứng':
    case 'Giáo viên đã duyệt':
    case 'Đã vào Kho chung':
      return 'grn';
    case 'Cần xem lại':
    case 'Chờ soát':
      return 'yel';
    case 'Bị từ chối':
      return 'red';
    default:
      return '';
  }
}
