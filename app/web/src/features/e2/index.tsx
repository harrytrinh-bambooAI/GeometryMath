import type { FeatureModule } from '../types.ts';
import { ProblemDetailPage } from './ProblemDetailPage.tsx';
import { SelfStudyPage, SelfStudyResultPage } from './SelfStudyPage.tsx';
import './e2.css';

/**
 * E2 — Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn.
 * Học sinh: /hs/tu-hoc (Tự học: nhập đề + sáu hình mẫu), /hs/tu-hoc/:id (bài tự học đã dựng).
 * Giáo viên: /gv/de/:versionId (trang chi tiết đề: nhãn máy + hình, không đổi được nhãn).
 * Khung hình ở đây là bản tạm (SVG); bộ xem 3D tương tác là của E3.
 */
export const feature: FeatureModule = {
  id: 'e2',
  title: 'Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn',
  teacherRoutes: [{ path: 'de/:versionId', element: <ProblemDetailPage /> }],
  studentRoutes: [
    { path: 'tu-hoc', element: <SelfStudyPage /> },
    { path: 'tu-hoc/:id', element: <SelfStudyResultPage /> },
  ],
  teacherNav: [],
  studentNav: [{ to: 'tu-hoc', label: 'Tự học', testId: 'e2-nav-tu-hoc' }],
};
