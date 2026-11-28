import type { FeatureModule } from '../types.ts';
import { ComposePage } from './ComposePage.tsx';
import { PlayPage, ResultPage, StudentListPage } from './StudentPages.tsx';
import { ReportsPage, TeacherListPage } from './TeacherPages.tsx';
import './s2e8.css';

/**
 * S2-E8 — Bài luyện điền đáp số (T-261126-21; câu trắc nghiệm: T-261126-25). Màn theo Design/prototype/s23.js, s23c.js.
 * Học sinh: /hs/bai-luyen (danh sách) · /hs/bai-luyen/:id (C2-06 làm bài, bàn phím toán) · /hs/bai-luyen/:id/ket-qua (C2-07).
 * Giáo viên: /gv/bai-luyen (danh sách) · /gv/bai-luyen/moi, /gv/bai-luyen/:id (C2-08 soạn, xem trước, giao) ·
 * /gv/bai-luyen/bao-cham-sai (báo "Em nghĩ đáp số này đúng").
 * data-testid bắt đầu bằng 's2e8-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e8',
  title: 'Bài luyện điền đáp số',
  teacherRoutes: [
    { path: 'bai-luyen', element: <TeacherListPage /> },
    { path: 'bai-luyen/moi', element: <ComposePage /> },
    { path: 'bai-luyen/bao-cham-sai', element: <ReportsPage /> },
    { path: 'bai-luyen/:id', element: <ComposePage /> },
  ],
  studentRoutes: [
    { path: 'bai-luyen', element: <StudentListPage /> },
    { path: 'bai-luyen/:id', element: <PlayPage /> },
    { path: 'bai-luyen/:id/ket-qua', element: <ResultPage /> },
  ],
  teacherNav: [{ to: 'bai-luyen', label: 'Bài luyện', testId: 's2e8-nav-bai-luyen', icon: 'pencil' }],
  studentNav: [{ to: 'bai-luyen', label: 'Bài luyện', testId: 's2e8-nav-bai-luyen-hs', icon: 'pencil' }],
};
