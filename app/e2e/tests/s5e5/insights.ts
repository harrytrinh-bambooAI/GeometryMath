import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, type Person } from '../s3e1/s3e1.ts';
import { P1, P2, ack, submitOk } from '../s5e4/submit/submit.ts';
import { approve, gradeJson, mock, setup, waitAi, type Ctx } from '../s5e6/s5e6b.ts';

/**
 * Tiện ích E2E S5-E5b (T-261128-64): "Cả lớp hay sai ở đâu" (US-S5E5-02) và "Nên nhắc lại trên lớp" (US-S5E5-03).
 * Dữ liệu đi đường thật: BC-01 (D-KC-01, RB-01) chế độ "AI đề xuất" của thế giới riêng (S5-E6b) → học sinh nộp ảnh →
 * đọc chữ nền → AI đề xuất (giả lập theo kịch bản của từng em) → cô Lan duyệt (Nhận / Sửa). Task class_errors giả lập
 * (MockAiProvider mốc 64): 2 nhận định tất định từ tin nhắn ẩn danh; "s5e5-errors-name" thêm nhận định có tên + phần trăm.
 */
export const summaryUrl = (assignmentId: string) => `${API}/api/v1/teacher/graded-assignments/${assignmentId}/error-summary`;
export const suggestionsUrl = (assignmentId: string, n: number) => `${summaryUrl(assignmentId)}/${n}/suggestions`;

export const MSG = {
  s57: 'Cần ít nhất 5 bài đã duyệt để tổng hợp lỗi của lớp.',
  s58: 'AI tạm không tổng hợp được. Đây là số liệu thô theo tiêu chí.',
  noBank: 'Kho chưa có bài cùng dạng',
} as const;

/** Đầu ra `grade` dạng F-S5-090 với điểm cho trước và loại lỗi ở tiêu chí ord (nguyên văn cho giả lập). */
export function gradeWith(pts: number[], label?: string, ord = 2): string {
  const g = JSON.parse(gradeJson(pts)) as { criteria: Array<Record<string, unknown>> };
  if (label) g.criteria[ord - 1]!.loi = label;
  return JSON.stringify(g);
}

/** Một bài: em nào, giả lập grade trả gì, cô Lan duyệt điểm nào, nhận xét gì. */
export interface Work {
  name: string;
  reply: string;
  pts: number[];
  comment?: string;
  pages?: Buffer[];
}

/**
 * Sáu bài BC-01 (TC-S5E5-09): 3 bài mất điểm "Các bước" loại sai-tinh-toan (F-S5-091), 1 bài sai-hinh-dung, 2 bài đủ
 * điểm. Thứ tự nộp cố định (mã BN tăng dần → "Bài 1…6"): Bài 2 là Chi Mai (sai-tinh-toan).
 */
export const SIX: Work[] = [
  { name: 'Bình An', reply: 'F-S5-090', pts: [2, 4, 2, 1, 1] },
  { name: 'Chi Mai', reply: 'F-S5-091', pts: [2, 3, 0, 1, 1] },
  { name: 'Đức Huy', reply: 'F-S5-091', pts: [2, 3, 0, 1, 1] },
  { name: 'Gia Bảo', reply: 'F-S5-091', pts: [2, 3, 0, 1, 1] },
  { name: 'Hà My', reply: gradeWith([2, 3, 2, 1, 1], 'sai-hinh-dung'), pts: [2, 3, 2, 1, 1] },
  { name: 'Khánh Linh', reply: 'F-S5-090', pts: [2, 4, 2, 1, 1] },
];

/** Nộp lần lượt (mã BN theo thứ tự), chờ AI đề xuất rồi duyệt; trả id bài nộp theo thứ tự. */
export async function gradeAll(rq: APIRequestContext, c: Ctx, works: Work[]): Promise<string[]> {
  const subs: Array<{ st: Person; id: string; w: Work }> = [];
  for (const w of works) {
    const st = c.w.people[w.name]!;
    await mock(rq, st, [w.reply]);
    await ack(rq, st);
    const sub = await submitOk(rq, st, c.bc.id, w.pages ?? [P1, P2]);
    subs.push({ st, id: sub.id, w });
  }
  await Promise.all(
    subs.map(async ({ id, w }) => {
      await waitAi(rq, c.lan, id);
      await approve(rq, c.lan, id, w.pts, { comment: w.comment ?? '' });
    }),
  );
  return subs.map((s) => s.id);
}

/** BC-01 "AI đề xuất" của thế giới mới với các bài đã duyệt. */
export async function graded(rq: APIRequestContext, works: Work[] = SIX): Promise<Ctx & { subs: string[] }> {
  const c = await setup(rq, 'ai_proposal');
  const subs = await gradeAll(rq, c, works);
  return { ...c, subs };
}

export interface Summary {
  status: 'ok' | 'too_few';
  approved: number;
  mode?: 'insight' | 'ai_off' | 'failed';
  message: string | null;
  check?: string;
  raw: null | {
    works: number;
    criteria: Array<{ ord: number; name: string; lost: number; works: number; avgPoints: number; maxPoints: number }>;
    labels: Array<{ label: string; works: number }>;
    engine: Record<string, number>;
  };
  insights: Array<{ n: number; text: string; criterion: string | null; label: string | null; evidence: Array<{ work: number; label: string; submissionId: string }> }>;
}

export async function summary(rq: APIRequestContext, t: Person, assignmentId: string): Promise<Summary> {
  const r = await rq.get(summaryUrl(assignmentId), { headers: bearer(t.token) });
  expect(r.status(), await r.text()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  return (await r.json()) as Summary;
}

export interface Suggestions {
  label: string | null;
  family: string | null;
  items: Array<{ code: string; problemVersionId: string; statement: string; sameDang: boolean; distractorMatch: boolean; distractor: string | null }>;
  message: string | null;
  docs: Array<{ id: string; code: string; title: string }>;
}

export async function suggestions(rq: APIRequestContext, t: Person, assignmentId: string, n = 1): Promise<Suggestions> {
  const r = await rq.get(suggestionsUrl(assignmentId, n), { headers: bearer(t.token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Suggestions;
}

/** Tin nhắn đã gửi task class_errors của giáo viên (móc dev/test). */
export async function errorPrompts(rq: APIRequestContext, teacherId: string): Promise<string[]> {
  const r = await rq.get(`${API}/test-hooks/s5e5b/error-prompts?teacherId=${teacherId}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as string[];
}

/** Các lượt tổng hợp của bài (mới nhất trước): chế độ và kết quả kiểm máy chủ. */
export async function errorRuns(rq: APIRequestContext, orgId: string, assignmentId: string) {
  const r = await rq.get(`${API}/test-hooks/s5e5b/error-runs?orgId=${orgId}&assignmentId=${assignmentId}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as Array<{ mode: string; check: string; removed: number }>;
}

/** Việc AI nền đã đăng ký cho bài nộp (chấm nền thật). */
export async function jobs(rq: APIRequestContext, orgId: string, refId: string) {
  const r = await rq.get(`${API}/test-hooks/s5e5b/jobs?orgId=${orgId}&refId=${refId}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as Array<{ id: string; task: string; status: string }>;
}

/** Nạp dữ liệu đề de-thi (lệnh nạp thật, lũy đẳng) cho gợi ý từ ngân hàng. */
export async function bank(rq: APIRequestContext) {
  const r = await rq.post(`${API}/test-hooks/s5e2/import`, { data: { mode: 'commit' }, timeout: 120_000 });
  expect(r.status(), await r.text()).toBe(200);
}

/** Cấp bộ đề kho 's5_kc' (seed s5_700: ba đề khoảng cách từ điểm đến mặt phẳng) cho trường của thế giới. */
export async function kcBank(rq: APIRequestContext, orgId: string) {
  await hook(rq, 's5e5b/bank-access', { orgId, bank: 's5_kc' });
}

export { P1, P2, hook };
