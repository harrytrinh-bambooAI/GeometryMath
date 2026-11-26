/** S2-E5 · hàm thuần của trình soạn lời giải (BR-S2E5-01, 02, 10). Không phụ thuộc React. */

export const MAX_STEPS = 30;
export const MAX_STEP_CHARS = 2000;

export const MSG = {
  noChange: 'Chưa có thay đổi để lưu',
  tooLong: 'Bước tối đa 2000 ký tự',
  maxSteps: 'Tối đa 30 bước',
  minSteps: 'Lời giải cần ít nhất 1 bước',
  emptyBox: 'Không có gì chờ duyệt',
  confirmApprove: 'Nhãn máy vẫn Cần xem lại. Vẫn duyệt?',
  machineNote: 'Máy đã kiểm tra phần tính toán',
} as const;

export interface DraftStep {
  /** Khóa ổn định của ô soạn (không phải số thứ tự). */
  key: string;
  text: string;
}

export interface DraftIssue {
  /** Chỉ số bước lỗi (từ 0), hoặc null khi lỗi của cả lời giải. */
  index: number | null;
  message: string;
}

/** Số ký tự theo điểm mã (khớp máy chủ: codePointCount). */
export function charCount(s: string): number {
  return Array.from(s).length;
}

/** Lỗi đầu tiên chặn lưu, hoặc null khi lưu được (BR-S2E5-01). */
export function validateDraft(steps: readonly string[]): DraftIssue | null {
  if (steps.length === 0) return { index: null, message: MSG.minSteps };
  if (steps.length > MAX_STEPS) return { index: null, message: MSG.maxSteps };
  for (let i = 0; i < steps.length; i++) {
    const t = steps[i]!.trim();
    if (t.length === 0) return { index: i, message: `Bước ${i + 1} đang trống` };
    if (charCount(t) > MAX_STEP_CHARS) return { index: i, message: MSG.tooLong };
  }
  return null;
}

/** Bản nháp có khác phiên bản đã lưu không (so sau khi cắt khoảng trắng đầu cuối). */
export function isDirty(saved: readonly string[], draft: readonly string[]): boolean {
  if (saved.length !== draft.length) return true;
  return saved.some((s, i) => s !== draft[i]!.trim());
}

/** Chỉ số bước đầu tiên khác bản đã lưu; bước đó và các bước sau hiện "Chưa kiểm" (BR-S2E5-10). */
export function firstChanged(saved: readonly string[], draft: readonly string[]): number {
  let i = 0;
  while (i < saved.length && i < draft.length && saved[i] === draft[i]!.trim()) i++;
  return i;
}

/** Chuyển một phần tử từ vị trí `from` tới vị trí `to` (kéo đổi thứ tự). */
export function move<T>(list: readonly T[], from: number, to: number): T[] {
  const out = list.slice();
  if (from < 0 || from >= out.length || to < 0 || to >= out.length || from === to) return out;
  const [x] = out.splice(from, 1);
  out.splice(to, 0, x!);
  return out;
}

/** Chèn một bước mới sau bước thứ `afterNo` (1…n; 0 = lên đầu). */
export function insertAfter<T>(list: readonly T[], afterNo: number, item: T): T[] {
  const out = list.slice();
  out.splice(Math.max(0, Math.min(afterNo, out.length)), 0, item);
  return out;
}

/** Câu chặn duyệt theo số bước lệch (BR-S2E5-17). */
export function blockedByFails(failCount: number): string {
  return `Chặn duyệt: còn ${failCount} bước lệch`;
}
