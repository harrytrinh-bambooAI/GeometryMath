import type { FeatureModule } from '../types.ts';
import { KhoDePage } from './KhoDePage.tsx';
import { ProblemDetailPage } from './ProblemDetailPage.tsx';
import './e5.css';

/**
 * E5 — Kho đề của Giáo viên mẫu.
 * Giáo viên: /gv/kho-de (Kho đề: lọc, thẻ đề, tải theo trang; thay bản tối thiểu của E1),
 * /gv/de/:versionId (Chi tiết đề & giao bài: hình 3D, đáp số, lời giải từng bước, gợi ý; thay trang tạm của E2).
 * Mục rail "Kho đề" vẫn khai ở E1 (e1-nav-kho-de) để không đổi ca E1. data-testid của epic bắt đầu bằng 'e5-';
 * các mã 'e1-…', 'e2-problem-…' giữ nguyên cho ca E1, E2 đang dùng.
 */
export const feature: FeatureModule = {
  id: 'e5',
  title: 'Kho đề của Giáo viên mẫu',
  teacherRoutes: [
    { path: 'kho-de', element: <KhoDePage /> },
    { path: 'de/:versionId', element: <ProblemDetailPage /> },
  ],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
