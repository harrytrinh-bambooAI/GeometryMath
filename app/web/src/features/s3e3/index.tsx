import type { FeatureModule } from '../types.ts';
import { ResultsPage } from './ResultsPage.tsx';
import { WorkPage } from './WorkPage.tsx';
import './s3e3.css';

/**
 * S3-E3 — Kết quả lớp và "Cả lớp vướng ở đâu" (T-261126-30). Màn theo Design/prototype s23b.js (C3-04).
 * Giáo viên: /gv/ket-qua-lop?lop=&bo= (bảng lớp × bài, bằng chứng một ô, nhận định gộp / dữ liệu thô) ·
 * /gv/ket-qua-lop/bai-lam?lop=&bo=&de=&hs=<userId> ("Mở bài làm" chỉ đọc). Học sinh không có màn nào ở epic này.
 * data-testid bắt đầu bằng 's3e3-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e3',
  title: 'Kết quả lớp và "Cả lớp vướng ở đâu"',
  teacherRoutes: [
    { path: 'ket-qua-lop', element: <ResultsPage /> },
    { path: 'ket-qua-lop/bai-lam', element: <WorkPage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'ket-qua-lop', label: 'Kết quả lớp', testId: 's3e3-nav-ket-qua-lop', icon: 'chart' }],
  studentNav: [],
};
