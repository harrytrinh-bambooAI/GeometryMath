/** Thông điệp của bộ xem (nguyên văn `work/05_dictionary.md`, chép trong `E3/stories_ac.md`). */
export const VIEWER_MSG = {
  '19': 'Giá trị phải là số lớn hơn 0.',
  '23': 'Hai đường thẳng này chéo nhau nên không có giao điểm.',
  '24': 'Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở.',
  '25': 'Ba điểm thẳng hàng nên chưa xác định được mặt phẳng.',
  '26': 'Điểm này đã có trên hình.',
} as const;
export type ViewerMsgCode = keyof typeof VIEWER_MSG;

/** Câu không có mã trong từ điển (hướng dẫn thao tác, không phải lỗi nghiệp vụ). */
export const VIEWER_TEXT = {
  parallel: 'Hai đường thẳng này song song nên không có giao điểm.',
  pickTwoPoints: 'Chạm điểm thứ hai để vẽ đoạn, hoặc chạm hai đường để tìm giao điểm.',
  pickLine: 'Chạm một cạnh hoặc đoạn để thêm điểm trên đó.',
  angleNeedsPoints: 'Đo góc: chạm lần lượt ba điểm, điểm giữa là đỉnh góc.',
} as const;
