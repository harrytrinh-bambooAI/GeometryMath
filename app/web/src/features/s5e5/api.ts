import { api } from '../../shared/api/client.ts';

/** S5-E5 · nháp thông báo trợ lý soạn (US-S5E5-01) + công tắc / dùng AI của lớp (US-S5E5-04). T-261128-55. */

export interface ClassLine {
  id: string;
  name: string;
}

export interface SetSource {
  id: string;
  title: string;
  dueLabel: string | null;
}

export interface Sources {
  classId: string;
  className: string;
  kinds: Array<{ kind: string; label: string }>;
  sets: SetSource[];
}

export interface Unsure {
  text: string;
  label: string;
  why: string;
}

export interface NoticeDraft {
  id: string;
  classId: string;
  className: string;
  kind: string;
  kindLabel: string;
  title: string | null;
  dueLabel: string | null;
  status: 'draft' | 'sent';
  humanLabel: string;
  body: string;
  unsure: Unsure[];
  recipients: number | null;
  maxLength: number;
  message?: string;
}

export interface Switches {
  classId: string;
  className: string;
  aiGrading: boolean;
  assistant: boolean;
  studentAi: boolean;
  message?: string;
}

export interface Usage {
  days: number;
  tasks: Array<{ task: string; turns: number }>;
  blocked: number;
  tokens: number;
}

const N = '/api/v1/teacher/notices';
const C = (id: string) => `/api/v1/teacher/my-classes/${id}`;

export const myClasses = () => api.get<{ items: ClassLine[] }>('/api/v1/teacher/my-classes');
export const sources = (classId: string) => api.get<Sources>(`${N}/sources`, { query: { classId } });
export const createDraft = (body: { classId: string; kind: string; refId: string | null; note: string | null }) =>
  api.post<NoticeDraft>(N, body);
export const sendDraft = (id: string, body: string) => api.post<NoticeDraft>(`${N}/${id}/send`, { body });
export const getSwitches = (classId: string) => api.get<Switches>(`${C(classId)}/ai-switches`);
export const putSwitches = (classId: string, body: Partial<Pick<Switches, 'aiGrading' | 'assistant'>>) =>
  api.put<Switches>(`${C(classId)}/ai-switches`, body);
export const getUsage = (classId: string) => api.get<Usage>(`${C(classId)}/ai-usage`, { query: { days: 30 } });
