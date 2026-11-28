/**
 * S2-E9 (US-S2E9-03) — câu trắc nghiệm trong bài luyện: kiểu dữ liệu, kiểm tra khi soạn (BR-S2E9-11, 12) (máy khách API: choiceApi.ts).
 * Dữ liệu học sinh nhận chỉ có mã + chữ của lựa chọn, KHÔNG có cờ đúng (BR-S2E9-14).
 */

/** Trạng thái một câu (trùng ItemState của api.ts; khai lại để tệp này thuần, chạy được bằng node --test). */
export type ChoiceItemState = 'todo' | 'doing' | 'correct' | 'review';

export const CHOICE_MIN = 2;
export const CHOICE_MAX = 5;
export const CHOICE_TEXT_MAX = 100;

export const CHOICE_MSG = {
  count: 'Cần từ 2 đến 5 lựa chọn',
  empty: 'Lựa chọn đang trống',
  long: 'Lựa chọn tối đa 100 ký tự',
  noCorrect: 'Hãy chọn một đáp án đúng',
  manyCorrect: 'Chỉ một đáp án đúng',
  duplicate: 'Hai lựa chọn giống nhau',
} as const;

/** Một lựa chọn đang soạn. */
export interface ChoiceOptionDraft {
  text: string;
  correct: boolean;
}

export interface TeacherChoice {
  position: number;
  maxAttempts: number;
  options: Array<{ id: string; text: string; correct: boolean }>;
}

/** Lựa chọn của một câu trên màn làm bài (không có cờ đúng). */
export interface RunnerChoice {
  options: Array<{ id: string; text: string }>;
  /** lựa chọn đã bị chấm Sai: làm mờ, không chọn lại */
  dimmed: string[];
  /** lựa chọn đã chọn Đúng */
  chosen: string | null;
}

export interface StudentChoice extends RunnerChoice {
  assignmentId: string;
  position: number;
}

export interface ChoiceAttemptView {
  outcome: 'correct' | 'wrong';
  message: string;
  attemptsLeft: number;
  maxAttempts: number;
  state: ChoiceItemState;
  dimmed: string[];
  canReport: boolean;
}

/** Chuẩn hóa chữ để so trùng: NFC, gộp khoảng trắng, không phân biệt hoa thường (giống máy chủ). */
export const normalizeChoice = (text: string): string => text.normalize('NFC').trim().replace(/\s+/g, ' ').toLowerCase();

/** Số lượt tối đa của câu có n lựa chọn: n − 1 (BR-S2E9-12). */
export const maxChoiceAttempts = (optionCount: number): number => Math.max(1, optionCount - 1);

/** Kiểm tra câu trắc nghiệm trước khi lưu; trả câu báo lỗi đầu tiên hoặc null. */
export function validateChoice(options: ChoiceOptionDraft[], attempts: number): string | null {
  if (options.length < CHOICE_MIN || options.length > CHOICE_MAX) return CHOICE_MSG.count;
  const seen = new Set<string>();
  for (const o of options) {
    const text = o.text.trim();
    if (text === '') return CHOICE_MSG.empty;
    if ([...text].length > CHOICE_TEXT_MAX) return CHOICE_MSG.long;
    const key = normalizeChoice(text);
    if (seen.has(key)) return CHOICE_MSG.duplicate;
    seen.add(key);
  }
  const correct = options.filter((o) => o.correct).length;
  if (correct === 0) return CHOICE_MSG.noCorrect;
  if (correct > 1) return CHOICE_MSG.manyCorrect;
  const max = maxChoiceAttempts(options.length);
  if (attempts < 1 || attempts > max) return `Tối đa ${max} lượt cho câu ${options.length} lựa chọn`;
  return null;
}

/** Hai lựa chọn trống mặc định khi đổi câu sang kiểu trắc nghiệm. */
export const emptyChoice = (): ChoiceOptionDraft[] => [
  { text: '', correct: false },
  { text: '', correct: false },
];
