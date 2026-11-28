/** Logic thuần của màn trợ lý soạn đề nháp (US-S5E2-06). */

export interface DraftLike {
  id: string;
  preselected: boolean;
  status: 'draft' | 'approved';
  machineState: 'verified' | 'needs_review';
}

/** Đề chọn sẵn lúc mở: chỉ đề "Đã kiểm chứng" còn nháp (BR-S5E2-15). */
export function initialSelection(items: DraftLike[]): Set<string> {
  return new Set(items.filter((i) => i.preselected && i.status === 'draft').map((i) => i.id));
}

/** Đề duyệt được: còn nháp và đã kiểm chứng (đề "Cần xem lại" không duyệt thẳng). */
export function approvable(item: DraftLike): boolean {
  return item.status === 'draft' && item.machineState === 'verified';
}

/** Số đề xin soạn: 1…10. */
export function countProblem(n: number): string | null {
  return Number.isInteger(n) && n >= 1 && n <= 10 ? null : 'Số đề từ 1 đến 10.';
}
