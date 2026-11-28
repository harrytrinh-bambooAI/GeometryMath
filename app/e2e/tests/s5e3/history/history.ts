import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../../support/fixtures.ts';
import { API, bearer, hook, type Person } from '../../s3e1/s3e1.ts';
import { bc01, rb01 } from '../../s5e4/s5e4.ts';
import { s5e3World } from '../kb.ts';

/**
 * Tiện ích E2E US-S5E3-06 — giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" / "lẽ ra phải chặn" (T-261128-63).
 * Mỗi ca dựng thế giới riêng (cô Lan chủ 11A1: Bình An, Chi Mai; thầy Khôi chủ 10B) và bài có chấm BC-01 thật (S5-E4).
 * Lượt hỏi đáp của BC-01 ghi sẵn qua móc /test-hooks/s5e3/history/seed (ai_interaction + ai_policy_decision như bộ kiểm
 * KS-1…KS-5 đã ghi; nguồn kb = "TL-01 v2 · Hệ thức 1/AH²", lecture = "Mặt phẳng trung trực").
 */
export const qaUrl = (id: string) => `${API}/api/v1/teacher/assignments/${id}/qa`;
export const reportUrl = (id: string) => `${API}/api/v1/teacher/assignments/${id}/qa/report`;
export const feedbackUrl = (id: string) => `${API}/api/v1/teacher/qa/${id}/feedback`;
export const mineUrl = (assignment: string) => `${API}/api/v1/student/qa/mine?assignment=${assignment}`;
export const SUMMARY = `${API}/api/v1/admin/ai/qa-feedback`;

export const MSG25 = 'Đã ghi báo cáo. Cảm ơn thầy cô.';
export const REFUSED = 'Câu trả lời chưa an toàn để hiện. Em thử hỏi cách khác hoặc xem gợi ý.';

export interface SeedRow {
  studentId: string;
  decision?: 'allowed' | 'blocked' | 'error_closed';
  rule?: string;
  sourceType?: 'kb' | 'lecture' | null;
  question?: string;
}

export interface QaRow {
  id: string;
  at: string;
  studentName?: string;
  question: string;
  replyKind: string;
  reply: string;
  source: null | { type: string; typeLabel: string; label: string };
  decision?: string | null;
  rule?: string | null;
  feedback?: null | { type: string; typeLabel: string; note: string | null };
}

export interface QaPage {
  items: QaRow[];
  nextCursor: string | null;
}

export interface Share {
  label: string;
  count: number;
  percent: number;
}

export interface Report {
  targetId: string;
  n: number;
  teacherDocs: Share;
  lectures: Share;
  noSource: Share;
  blocked: Share;
  blockedByRule: Array<Share & { rule: string }>;
}

/** Thế giới + BC-01 của cô Lan trên 11A1. */
export async function historySetup(rq: APIRequestContext) {
  const w = await s5e3World(rq);
  const lan = w.people['Cô Lan']!;
  const khoi = w.people['Thầy Khôi']!;
  const an = w.people['Bình An']!;
  const mai = w.people['Chi Mai']!;
  const c11 = w.classes['11A1']!;
  const rb = await rb01(rq, lan);
  const bc = await bc01(rq, lan, c11.id, rb.id);
  return { w, lan, khoi, an, mai, c11, bc };
}

export async function seed(
  rq: APIRequestContext,
  orgId: string,
  classId: string,
  targetId: string,
  rows: SeedRow[],
): Promise<string[]> {
  const r = await hook(rq, 's5e3/history/seed', {
    orgId,
    classId,
    targetId,
    rows,
  });
  return r.ids as string[];
}

export async function list(rq: APIRequestContext, t: Person, assignmentId: string, query = '') {
  const r = await rq.get(`${qaUrl(assignmentId)}${query}`, {
    headers: bearer(t.token),
  });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as QaPage;
}

/** Tổng hợp đánh dấu của người vận hành, lọc theo trường của thế giới; số đếm của (luật, loại). */
export async function summaryCount(rq: APIRequestContext, op: Person, orgId: string, rule: string, type: string) {
  const r = await rq.get(`${SUMMARY}?orgId=${orgId}`, {
    headers: bearer(op.token),
  });
  expect(r.status(), await r.text()).toBe(200);
  const items = (
    (await r.json()) as {
      items: Array<{ rule: string | null; type: string; n: number }>;
    }
  ).items;
  return items.find((i) => i.rule === rule && i.type === type)?.n ?? 0;
}
