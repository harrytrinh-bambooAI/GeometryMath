import { api } from '../../../shared/api/client.ts';

/** S5-E3 · giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" / "lẽ ra phải chặn" (US-S5E3-06). T-261128-63. */

export type FeedbackType = 'false_block' | 'should_block';

export interface QaRow {
  id: string;
  at: string;
  studentName?: string;
  question: string;
  replyKind: string;
  reply: string;
  source: null | { type: string; typeLabel: string; label: string };
  decision?: 'allowed' | 'blocked' | 'error_closed' | null;
  rule?: string | null;
  feedback?: null | {
    type: FeedbackType;
    typeLabel: string;
    note: string | null;
  };
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

export interface QaReport {
  targetId: string;
  n: number;
  teacherDocs: Share;
  lectures: Share;
  noSource: Share;
  blocked: Share;
  blockedByRule: Array<Share & { rule: string }>;
}

const T = '/api/v1/teacher';

export const qaHistory = (assignmentId: string, decision?: string, cursor?: string) => {
  const q = new URLSearchParams();
  if (decision) q.set('decision', decision);
  if (cursor) q.set('cursor', cursor);
  const s = q.toString();
  return api.get<QaPage>(`${T}/assignments/${encodeURIComponent(assignmentId)}/qa${s ? `?${s}` : ''}`);
};
export const qaReport = (assignmentId: string) =>
  api.get<QaReport>(`${T}/assignments/${encodeURIComponent(assignmentId)}/qa/report`);
export const qaFeedback = (interactionId: string, type: FeedbackType, note: string) =>
  api.post<{ message: string }>(`${T}/qa/${encodeURIComponent(interactionId)}/feedback`, { type, note });
