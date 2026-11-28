import { apiRequest } from '../../shared/api/client.ts';
import type { Scene2D } from '../../shared/scene2d/geometry2d.ts';

/** S2-E8 — máy khách API bài luyện. Dữ liệu học sinh nhận KHÔNG có đáp số, sai số hay kiểu đáp số (BR-S2E8-12). */

const enc = encodeURIComponent;

export type ItemState = 'todo' | 'doing' | 'correct' | 'review';

export interface SetLine {
  id: string;
  title: string;
  status: 'draft' | 'assigned' | 'closed';
  className: string | null;
  itemCount: number;
  releaseAt?: string | null;
}

export interface PlayItem {
  assignmentId: string;
  position: number;
  statement: string;
  problemKind: string;
  maxAttempts: number;
  attemptsLeft: number;
  state: ItemState;
  hint: string | null;
  lastAnswer: string | null;
  canonical: string | null;
  canReport: boolean;
  reported: boolean;
}

export interface PlayView {
  id: string;
  title: string;
  status: 'assigned' | 'closed';
  className: string;
  current: number;
  items: PlayItem[];
}

export interface AttemptView {
  outcome: 'correct' | 'wrong' | 'syntax';
  message: string;
  reason: string | null;
  canonical: string | null;
  note: string | null;
  hint: string | null;
  attemptsLeft: number;
  maxAttempts: number;
  state: ItemState;
  canReport: boolean;
}

export interface ResultItem {
  position: number;
  assignmentId: string;
  state: ItemState;
  label: string;
  solutionState: 'locked' | 'unset' | 'open';
  solutionLabel: string;
  releaseAt: string | null;
}

export interface ResultView {
  id: string;
  title: string;
  status: string;
  correct: number;
  review: number;
  remaining: number;
  summary: string;
  pending: string | null;
  note: string | null;
  items: ResultItem[];
}

export interface SolutionView {
  finalAnswer: string;
  steps: Array<{ title?: string; body?: string; why?: string }>;
}

export interface AssignmentLabels {
  verifyStatus: string;
}

export interface Candidate {
  problemId: string;
  problemVersionId: string;
  code: string;
  title: string;
  statement: string;
  problemKind: string;
  gradable: boolean;
}

export interface ItemInput {
  problemVersionId: string;
  answerKind: 'exact' | 'approx';
  tolerance: number | null;
  maxAttempts: number;
  releaseAt?: string | null;
}

export interface SetInput {
  title: string;
  releaseAt: string | null;
  items: ItemInput[];
}

export interface ItemView {
  position: number;
  problemId: string;
  problemVersionId: string;
  code: string;
  statement: string;
  problemKind: string;
  answerKind: 'exact' | 'approx';
  tolerance: number | null;
  maxAttempts: number;
  releaseAt: string | null;
  assignmentId: string | null;
}

export interface SetDetail {
  id: string;
  title: string;
  status: 'draft' | 'assigned' | 'closed';
  classId: string | null;
  className: string | null;
  releaseAt: string | null;
  assignedAt: string | null;
  items: ItemView[];
}

export interface PreviewCheck {
  outcome: 'correct' | 'wrong' | 'syntax';
  canonical: string | null;
  reason: string | null;
  note: string | null;
  hint: string | null;
}

export interface ReportView {
  id: string;
  studentName: string;
  studentCode: string | null;
  setTitle: string;
  position: number;
  submitted: string;
  note: string | null;
  canonical: string;
  status: 'pending' | 'confirmed' | 'kept';
  statusLabel: string;
  createdAt: string;
}

export interface PlaneFigure {
  scene: Scene2D | null;
  machineLabel?: string;
  humanLabel?: string;
}

export const practiceApi = {
  // học sinh
  mySets: (signal?: AbortSignal) => apiRequest<{ items: SetLine[] }>('/api/v1/student/practice-sets', { signal }),
  open: (id: string, signal?: AbortSignal) => apiRequest<PlayView>(`/api/v1/student/practice-sets/${enc(id)}`, { signal }),
  result: (id: string, signal?: AbortSignal) =>
    apiRequest<ResultView>(`/api/v1/student/practice-sets/${enc(id)}/result`, { signal }),
  attempt: (assignmentId: string, answer: string, requestKey: string) =>
    apiRequest<AttemptView>(`/api/v1/student/practice-items/${enc(assignmentId)}/attempts`, {
      method: 'POST',
      body: { answer, requestKey },
    }),
  report: (assignmentId: string, note: string) =>
    apiRequest<{ message: string }>(`/api/v1/student/practice-items/${enc(assignmentId)}/report`, {
      method: 'POST',
      body: { note },
    }),
  /** Hình của câu: mỗi câu là một hàng bài giao MVP nên dùng lại màn hình phẳng của S2-E2 (không có đáp số). */
  figure: (assignmentId: string, signal?: AbortSignal) =>
    apiRequest<PlaneFigure>(`/api/v1/student/assignments/${enc(assignmentId)}/plane`, { signal }),
  /** Lời giải qua cổng giờ mở của MVP (khóa thì máy chủ từ chối, không có dữ liệu). */
  solution: (assignmentId: string) =>
    apiRequest<SolutionView>(`/api/v1/student/assignments/${enc(assignmentId)}/solution`),
  labels: (assignmentId: string) => apiRequest<AssignmentLabels>(`/api/v1/student/assignments/${enc(assignmentId)}`),

  // giáo viên
  problems: (signal?: AbortSignal) => apiRequest<{ items: Candidate[] }>('/api/v1/teacher/practice/problems', { signal }),
  classes: (signal?: AbortSignal) =>
    apiRequest<{ items: Array<{ id: string; name: string }> }>('/api/v1/teacher/practice/classes', { signal }),
  sets: (signal?: AbortSignal) => apiRequest<{ items: SetLine[] }>('/api/v1/teacher/practice-sets', { signal }),
  set: (id: string, signal?: AbortSignal) => apiRequest<SetDetail>(`/api/v1/teacher/practice-sets/${enc(id)}`, { signal }),
  create: (body: SetInput) => apiRequest<SetDetail>('/api/v1/teacher/practice-sets', { method: 'POST', body }),
  update: (id: string, body: SetInput) =>
    apiRequest<SetDetail>(`/api/v1/teacher/practice-sets/${enc(id)}`, { method: 'PUT', body }),
  assign: (id: string, classId: string, confirmPastRelease: boolean) =>
    apiRequest<SetDetail>(`/api/v1/teacher/practice-sets/${enc(id)}/assign`, {
      method: 'POST',
      body: { classId, confirmPastRelease },
    }),
  previewCheck: (id: string, position: number, answer: string) =>
    apiRequest<PreviewCheck>(`/api/v1/teacher/practice-sets/${enc(id)}/preview/check`, {
      method: 'POST',
      body: { position, answer },
    }),
  reports: (signal?: AbortSignal) => apiRequest<{ items: ReportView[] }>('/api/v1/teacher/practice-reports', { signal }),
  decide: (id: string, decision: 'confirm' | 'keep') =>
    apiRequest<ReportView>(`/api/v1/teacher/practice-reports/${enc(id)}/decision`, { method: 'POST', body: { decision } }),
};
