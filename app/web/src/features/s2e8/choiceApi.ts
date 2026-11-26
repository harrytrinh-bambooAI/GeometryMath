import { apiRequest } from '../../shared/api/client.ts';
import type { ChoiceAttemptView, ChoiceOptionDraft, StudentChoice, TeacherChoice } from './choice.ts';

/** S2-E9 (US-S2E9-03) — máy khách API câu trắc nghiệm của bài luyện. */
const enc = encodeURIComponent;

export const choiceApi = {
  ofSet: (setId: string, signal?: AbortSignal) =>
    apiRequest<{ items: TeacherChoice[] }>(`/api/v1/teacher/practice-sets/${enc(setId)}/choices`, { signal }),
  save: (setId: string, position: number, options: ChoiceOptionDraft[], maxAttempts: number) =>
    apiRequest<TeacherChoice>(`/api/v1/teacher/practice-sets/${enc(setId)}/items/${position}/choice`, {
      method: 'PUT',
      body: { options: options.map((o) => ({ text: o.text, correct: o.correct })), maxAttempts },
    }),
  remove: (setId: string, position: number) =>
    apiRequest<void>(`/api/v1/teacher/practice-sets/${enc(setId)}/items/${position}/choice`, { method: 'DELETE' }),
  forStudent: (setId: string, signal?: AbortSignal) =>
    apiRequest<{ items: StudentChoice[] }>(`/api/v1/student/practice-sets/${enc(setId)}/choices`, { signal }),
  attempt: (assignmentId: string, choiceId: string, requestKey: string) =>
    apiRequest<ChoiceAttemptView>(`/api/v1/student/practice-items/${enc(assignmentId)}/choice-attempts`, {
      method: 'POST',
      body: { choiceId, requestKey },
    }),
};
