import { api } from '../../shared/api/client.ts';
import type { Scene, SelfStudy, SolidTemplate } from '../../shared/scene/types.ts';

/** Gọi API của E2 (app/docs/SCENE-CONTRACT.md §4). */
export const e2Api = {
  templates: () => api.get<{ items: SolidTemplate[] }>('/api/v1/geometry/templates'),
  templateScene: (code: string, params: Record<string, number>) =>
    api.post<Scene>(`/api/v1/geometry/templates/${encodeURIComponent(code)}/scene`, { params }),
  build: (statement: string) => api.post<SelfStudy>('/api/v1/student/self-study', { statement }),
  selfStudy: (id: string) => api.get<SelfStudy>(`/api/v1/student/self-study/${encodeURIComponent(id)}`),
  assignmentScene: (id: string) => api.get<Scene>(`/api/v1/student/assignments/${encodeURIComponent(id)}/scene`),
  teacherProblem: (versionId: string) =>
    api.get<TeacherProblem>(`/api/v1/teacher/problems/${encodeURIComponent(versionId)}`),
};

export interface TeacherProblem {
  versionId: string;
  code: string;
  statement: string;
  verifyStatus: 'verified' | 'needs_review';
  label: string;
  scene?: Scene;
}
