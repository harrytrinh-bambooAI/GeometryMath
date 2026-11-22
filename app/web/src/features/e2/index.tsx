import type { FeatureModule } from '../types.ts';
import { SelfStudyPage, SelfStudyResultPage } from './SelfStudyPage.tsx';
import './e2.css';

/**
 * E2 — Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn.
 * Học sinh: /hs/tu-hoc (Tự học: nhập đề + sáu hình mẫu), /hs/tu-hoc/:id (bài tự học đã dựng).
 * Giáo viên: trang chi tiết đề /gv/de/:versionId nay do E5 cắm (features/e5/ProblemDetailPage.tsx, giữ testid e2-problem-…).
 * Khung hình ở đây là bản tạm (SVG); bộ xem 3D tương tác là của E3.
 */
export const feature: FeatureModule = {
  id: 'e2',
  title: 'Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn',
  teacherRoutes: [],
  studentRoutes: [
    { path: 'tu-hoc', element: <SelfStudyPage /> },
    { path: 'tu-hoc/:id', element: <SelfStudyResultPage /> },
  ],
  teacherNav: [],
  studentNav: [{ to: 'tu-hoc', label: 'Tự học', testId: 'e2-nav-tu-hoc' }],
};
