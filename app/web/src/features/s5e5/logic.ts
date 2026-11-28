/** S5-E5 · logic thuần của màn thông báo / AI của lớp (T-261128-55). */

/** Số ký tự hiện dưới ô soạn: "120 / 1 000 ký tự". */
export function lengthLabel(body: string, max: number): string {
  const fmt = (n: number) => String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  return `${fmt(body.length)} / ${fmt(max)} ký tự`;
}

/** Vượt 1 000 ký tự thì không cho Gửi (MSG-S5-56 phía máy chủ). */
export function overLimit(body: string, max: number): boolean {
  return body.length > max;
}

const TASKS: Record<string, string> = {
  kb_qa: 'Hỏi đáp',
  grade: 'Chấm',
  grade_ocr: 'Đọc chữ',
  class_notice: 'Thông báo',
  lesson_plan: 'Giáo án',
  exam_draft: 'Đề',
  class_errors: 'Lỗi sai',
};

/** Nhãn tiếng Việt của task AI trên màn "Dùng AI" (BR-S5E5-13). */
export function taskLabel(task: string): string {
  return TASKS[task] ?? task;
}
