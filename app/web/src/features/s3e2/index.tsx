import type { FeatureModule } from '../types.ts';
import { ComposeSetPage } from './ComposeSet.tsx';
import { StudentHomePage, StudentSetPage } from './StudentSets.tsx';
import { TeacherSetsPage } from './TeacherSets.tsx';
import './s3e2.css';

/**
 * S3-E2 — Bộ bài, lịch mở lời giải, Bài của em theo bộ (T-261126-29). Màn theo Design/prototype s23b.js (C3-03),
 * s23d.js (đóng bộ, lưu trữ bộ). Một mô hình bộ bài với bài luyện S2-E8 (PO-66 G1): học sinh "Làm bài" ở /hs/bai-luyen/:id.
 * Giáo viên: /gv/bo-bai (Bài đang giao, Nháp, Đã đóng, Bộ đã lưu trữ) · /gv/bo-bai/moi, /gv/bo-bai/:id (soạn, lịch, giao).
 * Học sinh: /hs/bo-bai (Bài của em theo lớp → bộ) · /hs/bo-bai/:id.
 * data-testid bắt đầu bằng 's3e2-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e2',
  title: 'Bộ bài, lịch mở lời giải, Bài của em theo bộ',
  teacherRoutes: [
    { path: 'bo-bai', element: <TeacherSetsPage /> },
    { path: 'bo-bai/moi', element: <ComposeSetPage /> },
    { path: 'bo-bai/:id', element: <ComposeSetPage /> },
  ],
  studentRoutes: [
    { path: 'bo-bai', element: <StudentHomePage /> },
    { path: 'bo-bai/:id', element: <StudentSetPage /> },
  ],
  teacherNav: [{ to: 'bo-bai', label: 'Bộ bài', testId: 's3e2-nav-bo-bai', icon: 'layers' }],
  studentNav: [{ to: 'bo-bai', label: 'Bộ bài', testId: 's3e2-nav-bo-bai-hs', icon: 'layers' }],
};
