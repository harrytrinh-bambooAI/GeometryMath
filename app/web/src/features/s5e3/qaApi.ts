import { api } from '../../shared/api/client.ts';

/** S5-E3 · hỏi đáp của học sinh theo kho (US-S5E3-03, 04, 05) + màn giáo viên đặt mức trợ giúp. T-261128-58. */

export interface QaSource {
  type: 'kb' | 'lecture';
  typeLabel: string;
  docTitle?: string;
  title?: string;
  version?: number;
  sectionTitle?: string;
  label: string;
}

export interface QaReply {
  interactionId: string;
  kind: string;
  reply: string;
  message?: string;
  retry: boolean;
  source: QaSource | null;
}

export interface QaSourceView {
  type: string;
  typeLabel: string;
  label: string;
  docTitle: string;
  version: number;
  sectionTitle: string;
  body: string;
}

export interface Privacy {
  version: string;
  text: string;
  acked: boolean;
}

const QA = '/api/v1/student/kb-qa';

export const getPrivacy = () => api.get<Privacy>(`${QA}/privacy`);
export const ackPrivacy = (version: string) => api.post<Privacy>(`${QA}/privacy`, { version });
export const askQa = (assignmentId: string, question: string) => api.post<QaReply>(QA, { assignmentId, question });
export const qaSource = (interactionId: string) => api.get<QaSourceView>(`${QA}/${encodeURIComponent(interactionId)}/source`);

export interface GradedLine {
  id: string;
  code: string;
  title: string;
  className: string;
}

export const listGraded = () => api.get<{ items: GradedLine[] }>('/api/v1/teacher/graded-assignments');
