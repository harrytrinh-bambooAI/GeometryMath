import { api } from '../../../shared/api/client.ts';

/** S5-E4c · hàng chờ chấm (C5-04), chấm tay (C5-05), trả bài và kết quả (C5-06). T-261128-60. */

export interface Flag {
  code: 'unreadable' | 'late';
  label: string;
  detail?: string;
}

export interface QueueItem {
  submissionId: string;
  code: string;
  assignmentId: string;
  studentId: string;
  studentName: string;
  status: 'processing' | 'waiting' | 'approved' | 'returned';
  statusLabel: string;
  flags: Flag[];
  submittedAt: string;
  pages: number;
  version: number;
  /** Phần AI (S5-E6, sau cờ ai-grading): có khi bài dùng AI; chạy song song trước khi duyệt chỉ có aiDone. */
  ai?: { aiDone?: boolean; aiStatus?: string; aiFlags?: string[] };
}

export interface QueueAssignment {
  id: string;
  code: string;
  title: string;
  classId: string;
  className: string;
  dueAt: string;
  status: string;
}

export interface Queue {
  state: 'empty' | 'done' | 'processing' | 'ready';
  dueAt?: string;
  assignmentId?: string;
  returnable?: boolean;
  summary: { waiting: number; processing: number; notSubmitted: number; approved: number; returned: number };
  summaryLine: string;
  assignments: QueueAssignment[];
  items: QueueItem[];
}

export interface Criterion {
  ord: number;
  name: string;
  description?: string;
  maxPoints: number;
}

export interface GradeView {
  seq: number;
  total: number;
  comment: string;
  criteria: Array<Criterion & { points: number }>;
  approvedBy: string;
  approvedAt: string;
}

export interface ReviewView extends QueueItem {
  versionId: string;
  submissionVersion: number;
  assignment: { id: string; code: string; title: string; classId: string; className: string; scale: number; rubricCode: string; rubricVersion: number; rubricName: string; status: string };
  criteria: Criterion[];
  grade?: GradeView;
  prevSubmissionId: string | null;
  nextSubmissionId: string | null;
  message?: string;
}

export interface ReturnStatus {
  assignmentId: string;
  status: string;
  statusLabel: string;
  approved: number;
  waiting: number;
  returnedCount: number;
  returnedAt: string | null;
  undoUntil: string | null;
  canUndo: boolean;
  message?: string;
}

export interface Result {
  id: string;
  code: string;
  assignmentId: string;
  title: string;
  status: 'waiting' | 'approved' | 'returned';
  statusLabel: string;
  message: string;
  criteria?: Array<Criterion & { points: number }>;
  score?: number;
  scale?: number;
  comment?: string;
  versionId?: string;
  pages?: number;
  solution?: { status: string; finalAnswer?: string; steps?: Array<{ title?: string; body?: string }>; message?: string };
}

const T = '/api/v1/teacher';

export function queue(f: { classId?: string; assignmentId?: string }) {
  const q = new URLSearchParams();
  if (f.classId) q.set('classId', f.classId);
  if (f.assignmentId) q.set('assignmentId', f.assignmentId);
  const s = q.toString();
  return api.get<Queue>(`${T}/grading/queue${s ? `?${s}` : ''}`);
}

export const review = (id: string) => api.get<ReviewView>(`${T}/grading/submissions/${id}`);

export const grade = (id: string, body: { version: number; criteria: Array<{ ord: number; points: number }>; comment: string }) =>
  api.post<ReviewView>(`/api/v1/submissions/${id}/grade`, body);

export const pageUrl = (versionId: string, n: number) => api.get<{ url: string }>(`/api/v1/submissions/${versionId}/pages/${n}/url`);

export const returnAll = (assignmentId: string) => api.post<ReturnStatus & { returned: number }>(`${T}/graded-assignments/${assignmentId}/return`);
export const undoReturn = (assignmentId: string) => api.post<ReturnStatus>(`${T}/graded-assignments/${assignmentId}/return/undo`);
export const returnStatus = (assignmentId: string) => api.get<ReturnStatus>(`${T}/graded-assignments/${assignmentId}/return`);

export const myGraded = () => api.get<{ items: Array<{ id: string; title: string; dueAt: string }> }>('/api/v1/student/graded-assignments');
export const mine = (assignmentId: string) =>
  api.get<{ submission?: { id: string } }>(`/api/v1/submissions/mine?assignmentId=${encodeURIComponent(assignmentId)}`);
export const result = (submissionId: string) => api.get<Result>(`/api/v1/submissions/${submissionId}/result`);
