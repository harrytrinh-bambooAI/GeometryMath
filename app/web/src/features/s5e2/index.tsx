import type { FeatureModule } from '../types.ts';
import { ExamFormPage } from './ExamFormPage.tsx';
import { ExamSetPage } from './ExamSetPage.tsx';
import './s5e2.css';

/**
 * S5-E2 — Ra đề theo ma trận và trợ lý soạn bộ đề. Chủ: T-261128-52 (form, lắp đề, bộ đề nháp), 54 (export/), 63
 * (draft/) (PLAN_code-s5 §5). Màn C5-08 /gv/ra-de (form), /gv/ra-de/:id (bộ đề nháp: thay câu, nhãn hai trục, duyệt).
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e2',
  title: 'Ra đề theo ma trận và trợ lý soạn bộ đề',
  teacherRoutes: [
    { path: 'ra-de', element: <ExamFormPage /> },
    { path: 'ra-de/:id', element: <ExamSetPage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'ra-de', label: 'Ra đề', testId: 's5e2-nav-ra-de', icon: 'file' }],
  studentNav: [],
};
