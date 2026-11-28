import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../../support/fixtures.ts';
import { API, bearer, hook, type Person } from '../../s3e1/s3e1.ts';
import { createMd, json, publish, type DocView } from '../../s5e3/kb.ts';

/**
 * Tiện ích E2E US-S5E2-06 — trợ lý soạn đề nháp từ tài liệu Đang dùng (T-261128-63). Mỗi ca dựng thế giới riêng
 * (s5e3World: cô Lan chủ 11A1, thầy Khôi chủ 10B); TL-01 v2 / TL-02 Chờ duyệt dựng bằng API kho tri thức thật (kb.ts);
 * AI giả lập task exam_draft (MockAiProvider mốc T-261128-63): thứ tự AI trả 2 đề dựng được, 1 đề engine không dựng
 * được, rồi các đề dựng được; kịch bản "s5e2-draft-mismatch" = đề đầu ghi đáp số a√3/3 trong khi engine tính a√6/3.
 */
export const DRAFTS = `${API}/api/copilot/exam-drafts`;

export const MSG = {
  s06: 'Hôm nay thầy cô đã dùng hết 30 lượt soạn với AI. Lượt mới mở lúc 00:00.',
  s14: 'Đề nháp chưa qua kiểm chứng: không chọn sẵn, đặt cuối danh sách.',
  s15: 'Chưa có tài liệu Đang dùng nào để soạn đề.',
} as const;

/** Đáp số ENGINE của 4 đề dựng được theo thứ tự (SA = a√2, a, a√3, 2a; khoảng cách từ A đến (SBC)). */
export const ENGINE_ANSWERS = ['a√6/3', 'a√2/2', 'a√3/2', '2a√5/5'];

export interface DraftItem {
  id: string;
  batchId: string;
  ord: number;
  statement: string;
  sourceLabel: string;
  sourceSection: string | null;
  source: string;
  machineLabel: 'Đã kiểm chứng' | 'Cần xem lại';
  machineState: 'verified' | 'needs_review';
  reviewReason: string | null;
  humanLabel: 'AI soạn' | 'Giáo viên đã duyệt';
  status: 'draft' | 'approved';
  preselected: boolean;
  answer: string | null;
  answerSource: 'engine' | null;
  aiAnswer: string | null;
  approvedAt: string | null;
}

export interface DraftBatch {
  id: string;
  classId: string;
  source: { docId: string; docCode: string; version: number; label: string };
  requested: number;
  items: DraftItem[];
  message: string | null;
}

export type Err = { code: string; message: string };

export const requestDrafts = (rq: APIRequestContext, t: Person, data: Record<string, unknown>): Promise<APIResponse> =>
  rq.post(DRAFTS, { headers: bearer(t.token), data });

export async function drafts(rq: APIRequestContext, t: Person, docId: string, classId: string, count = 5) {
  return json<DraftBatch>(await requestDrafts(rq, t, { docId, classId, count }), 201);
}

export const approve = (rq: APIRequestContext, t: Person, itemId: string) =>
  rq.post(`${DRAFTS}/items/${itemId}/approve`, { headers: bearer(t.token) });

export async function batch(rq: APIRequestContext, t: Person, id: string) {
  return json<DraftBatch>(await rq.get(`${DRAFTS}/${id}`, { headers: bearer(t.token) }), 200);
}

export async function bank(rq: APIRequestContext, t: Person) {
  return json<{ items: DraftItem[] }>(await rq.get(`${DRAFTS}/bank`, { headers: bearer(t.token) }), 200);
}

export async function aiScenario(rq: APIRequestContext, p: Person, scenario: string) {
  await hook(rq, 'ai', { userId: p.id, scenario });
}

/** TL-09 của thầy Khôi (F-S5-041): v1 Đang dùng. */
export async function tl09(rq: APIRequestContext, khoi: Person): Promise<DocView> {
  const d = await json<DocView>(
    await createMd(
      rq,
      khoi,
      'Hệ thức lượng 10B',
      '# Hệ thức lượng 10B\n\n## Định lí côsin\na² = b² + c² − 2bc·cos A.\n',
    ),
    201,
  );
  return publish(rq, khoi, d.id, 1);
}

export async function aiPurposes(rq: APIRequestContext, userId: string): Promise<string[]> {
  const r = await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as Array<{ purpose: string }>).map((c) => c.purpose);
}
