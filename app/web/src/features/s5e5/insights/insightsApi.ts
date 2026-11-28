import { api } from '../../../shared/api/client.ts';

/** S5-E5b · "Cả lớp hay sai ở đâu" (US-S5E5-02) và "Nên nhắc lại trên lớp" (US-S5E5-03). T-261128-64. */

export interface CriterionStat {
  ord: number;
  name: string;
  maxPoints: number;
  lost: number;
  works: number;
  avgPoints: number;
}

export interface Evidence {
  work: number;
  label: string;
  submissionId: string;
}

export interface Insight {
  n: number;
  text: string;
  criterion: string | null;
  label: string | null;
  evidence: Evidence[];
}

export interface ErrorSummary {
  assignmentId: string;
  code: string;
  title: string;
  approved: number;
  minApproved: number;
  status: 'ok' | 'too_few';
  mode?: 'insight' | 'ai_off' | 'failed';
  message: string | null;
  raw: null | {
    works: number;
    criteria: CriterionStat[];
    labels: Array<{ label: string; works: number }>;
    engine: { match: number; mismatch: number; notFound: number };
  };
  insights: Insight[];
}

export interface Suggestion {
  code: string;
  problemVersionId: string;
  statement: string;
  sameDang: boolean;
  distractorMatch: boolean;
  distractor: string | null;
}

export interface Suggestions {
  assignmentId: string;
  n: number;
  label: string | null;
  familyLabel: string | null;
  items: Suggestion[];
  message: string | null;
  docs: Array<{ id: string; code: string; title: string; version: number }>;
}

const T = '/api/v1/teacher';

export const errorSummary = (assignmentId: string) =>
  api.get<ErrorSummary>(`${T}/graded-assignments/${encodeURIComponent(assignmentId)}/error-summary`);

export const suggestions = (assignmentId: string, n: number) =>
  api.get<Suggestions>(`${T}/graded-assignments/${encodeURIComponent(assignmentId)}/error-summary/${n}/suggestions`);

/** "Tạo bộ luyện": bộ bài S3-E2 trạng thái Nháp, chưa giao (BR-S5E5-09). */
export const createPracticeSet = (title: string, problemVersionIds: string[]) =>
  api.post<{ id: string }>(`${T}/assignment-sets`, {
    title,
    classId: null,
    dueAt: null,
    releaseAt: null,
    items: problemVersionIds.map((v) => ({ problemVersionId: v, releaseAt: null })),
  });
