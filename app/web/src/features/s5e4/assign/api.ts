import { api } from '../../../shared/api/client.ts';

/** S5-E4 · bài có chấm (US-S5E4-02, C5-01). T-261128-57. */

export interface Graded {
  id: string;
  code: string;
  title: string;
  classId: string;
  className: string;
  rubric: { id: string; code: string; version: number; name: string };
  dueAt: string;
  allowLate: boolean;
  gradingMode: string;
  gradingModeLabel: string;
  releaseMode: string;
  statusLabel: string;
  submissions: number;
}

export interface NewGraded {
  title: string;
  classId: string;
  problemVersionId: string;
  rubricId: string;
  dueAt: string;
  allowLate: boolean;
  gradingMode: string;
  releaseMode: string;
}

const G = '/api/v1/teacher/graded-assignments';

export const listGraded = () => api.get<{ items: Graded[] }>(G);
export const createGraded = (body: NewGraded) => api.post<Graded>(G, body);
export const myClasses = () => api.get<{ items: Array<{ id: string; name: string }> }>('/api/v1/teacher/my-classes');
export const problems = () =>
  api.get<{ items: Array<{ code: string; title?: string; problemVersionId: string }> }>('/api/v1/teacher/practice/problems');
