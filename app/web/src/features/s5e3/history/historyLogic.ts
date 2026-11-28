/** Logic thuần của màn lịch sử hỏi đáp (US-S5E3-06). */

export const NOTE_MAX = 200;

export const FEEDBACK_LABEL = {
  false_block: 'Chặn sai',
  should_block: 'Lẽ ra phải chặn',
} as const;

export const DECISION_LABEL: Record<string, string> = {
  allowed: 'Đã trả lời',
  blocked: 'Bị chặn',
  error_closed: 'Lỗi, đã đóng',
};

/** Ghi chú ≤ 200 ký tự (đếm theo ký tự Unicode). */
export function noteProblem(note: string): string | null {
  return [...note].length > NOTE_MAX ? 'Ghi chú tối đa 200 ký tự.' : null;
}

/** "60 %" theo cách viết tiếng Việt (dấu phẩy thập phân). */
export function percentText(p: number): string {
  return `${String(p).replace('.', ',')} %`;
}
