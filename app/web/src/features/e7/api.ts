import { api } from '../../shared/api/client.ts';

/** Kiểu dữ liệu và lời gọi API của E7 (Theo dõi lớp, mục "Theo dõi"). */
export interface StudentRow {
  userId: string;
  displayName: string;
  opened: boolean;
  k: number;
  label: string;
  hintLevel: number;
  askCount: number;
  toolCount: number;
  activeMinutes: number;
  status: string;
}
export interface QuestionGroup {
  question: string;
  names: string[];
  count: number;
  lastAskedAt: string;
}
export interface AskedQuestion {
  id: number;
  userId: string;
  name: string;
  question: string;
  askedAt: string;
}
export interface Monitor {
  assignmentId: string;
  problemCode: string;
  problemTitle: string;
  className: string;
  assignedAt: string;
  releaseAt: string | null;
  releaseState: 'unset' | 'upcoming' | 'open';
  studentCount: number;
  openedCount: number;
  hintUsers: number;
  questionCount: number;
  figureMinutes: number;
  students: StudentRow[];
  questionGroups: QuestionGroup[];
  askedQuestions: AskedQuestion[];
}
export interface Assigned {
  id: string;
  problemCode: string;
  problemTitle: string;
  classId: string;
  className: string;
  assignedAt: string;
  releaseAt: string | null;
  releaseState: 'unset' | 'upcoming' | 'open';
}

const enc = encodeURIComponent;

export const e7Api = {
  monitor: (id: string) => api.get<Monitor>(`/api/v1/teacher/assignments/${enc(id)}/monitor`),
  assigned: (cursor?: string | null) =>
    api.get<{ items: Assigned[]; nextCursor: string | null }>('/api/v1/teacher/assigned', {
      query: cursor ? { cursor } : {},
    }),
};
