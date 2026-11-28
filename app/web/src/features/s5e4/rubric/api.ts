import { api } from '../../../shared/api/client.ts';

/** S5-E4 · rubric có phiên bản (US-S5E4-01, C5-02). T-261128-57. */

export interface Criterion {
  ord?: number;
  name: string;
  description: string;
  maxPoints: number;
  directionDependent: boolean;
}

export interface Rubric {
  id: string;
  code: string;
  version: number;
  versionId: string;
  currentVersion: number;
  name: string;
  template: string | null;
  scale: number;
  total: number;
  criteria: Criterion[];
  guide: string[];
  used: boolean;
  message?: string;
}

export interface RubricLine {
  id: string;
  code: string;
  name: string;
  version: number;
  versionId: string;
  scale: number;
}

const R = '/api/v1/teacher/rubrics';

export const listRubrics = () => api.get<{ items: RubricLine[] }>(R);
export const getRubric = (id: string) => api.get<Rubric>(`${R}/${id}`);
export const createRubric = (body: { fromTemplate?: string; name: string }) => api.post<Rubric>(R, body);
export const saveRubric = (id: string, body: { name: string; criteria: Criterion[] }) => api.put<Rubric>(`${R}/${id}`, body);
