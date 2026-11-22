import { api, apiRequest } from '../../shared/api/client.ts';
import { getToken } from '../../shared/auth/session.ts';
import type { Scene } from '../../shared/scene/types.ts';

/**
 * Gọi máy chủ của trang Làm bài (E3). Cảnh lấy theo hợp đồng E2 (`app/docs/SCENE-CONTRACT.md` §4);
 * trạng thái khóa, sự kiện thao tác, mở bài, gợi ý là điểm cuối của E3.
 */
export interface StudentAssignmentInfo {
  id: string;
  problemCode: string;
  statement: string;
  className: string;
  releaseAt: string | null;
  releaseState: 'unset' | 'upcoming' | 'open';
}
export interface LockInfo {
  locked: boolean;
  kind: 'class' | 'self';
  releaseAt?: string | null;
}
export interface SelfStudyInfo {
  assignmentId: string;
  problemVersionId: string;
  statement: string;
  scene: Scene;
}
export interface TeacherProblem {
  versionId: string;
  code: string;
  statement: string;
  label: string;
  scene: Scene;
}

const enc = encodeURIComponent;
export const e3Api = {
  assignment: (id: string) => api.get<StudentAssignmentInfo>(`/api/v1/student/assignments/${enc(id)}`),
  scene: (id: string) => api.get<Scene>(`/api/v1/student/assignments/${enc(id)}/scene`),
  selfStudy: (id: string) => api.get<SelfStudyInfo>(`/api/v1/student/self-study/${enc(id)}`),
  lock: (id: string) => api.get<LockInfo>(`/api/v1/student/assignments/${enc(id)}/lock`),
  open: (id: string, requestId: string) => api.post<{ status: string }>(`/api/v1/student/assignments/${enc(id)}/open`, { requestId }),
  hint: (id: string, level: 1 | 2) =>
    api.post<{ level: number; body: string }>(`/api/v1/student/assignments/${enc(id)}/hints/${level}`, {
      requestId: crypto.randomUUID(),
    }),
  ask: (id: string, question: string) =>
    api.post<{ id: number; question: string; reply: string }>(`/api/v1/student/assignments/${enc(id)}/questions`, {
      question,
      requestId: crypto.randomUUID(),
    }),
  solution: (id: string) => api.get<{ finalAnswer?: string }>(`/api/v1/student/assignments/${enc(id)}/solution`),
  teacherProblem: (versionId: string) => api.get<TeacherProblem>(`/api/v1/teacher/problems/${enc(versionId)}`),
  templateScene: (code: string, params: Record<string, number>) =>
    api.post<Scene>(`/api/v1/geometry/templates/${enc(code)}/scene`, { params }),
};

export interface ToolEventRow {
  tool: number;
  at: string;
}
/** Gửi một lô sự kiện thao tác; `keepalive` để lô cuối vẫn đi khi rời trang. */
export function sendToolEvents(id: string, events: ToolEventRow[], keepalive = false): Promise<unknown> {
  const body = { batchId: crypto.randomUUID(), events };
  if (!keepalive) return apiRequest(`/api/v1/student/assignments/${enc(id)}/figure-events`, { method: 'POST', body });
  const token = getToken();
  return fetch(`/api/v1/student/assignments/${enc(id)}/figure-events`, {
    method: 'POST',
    keepalive: true,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify(body),
  }).catch(() => undefined);
}
