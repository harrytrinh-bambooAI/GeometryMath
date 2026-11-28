import { api } from '../../../shared/api/client.ts';

/** Mức trợ giúp AI theo bài (US-S5E3-04, C5-01 mục "Trợ giúp của AI"). T-261128-58. */

export type HelpLevel = 'off' | 'hints' | 'hints_concepts';

export interface HelpLevelView {
  assignmentId: string;
  targetKind: 'practice' | 'graded';
  title: string;
  helpLevel: HelpLevel;
  helpLevelLabel: string;
  summary: string;
  isDefault: boolean;
}

export const HELP_LEVELS: Array<{ value: HelpLevel; label: string }> = [
  { value: 'off', label: 'Tắt' },
  { value: 'hints', label: 'Chỉ gợi ý' },
  { value: 'hints_concepts', label: 'Gợi ý và khái niệm' },
];

const url = (id: string) => `/api/v1/teacher/assignments/${id}/help-level`;

export const getHelpLevel = (id: string) => api.get<HelpLevelView>(url(id));
export const putHelpLevel = (id: string, helpLevel: HelpLevel) => api.put<HelpLevelView>(url(id), { helpLevel });
