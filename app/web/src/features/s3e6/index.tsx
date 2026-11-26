import type { FeatureModule } from '../types.ts';
import { ClassDetailWithArchive } from './ClassDetailWithArchive.tsx';
import { MyAssignmentsWithArchived } from './MyAssignmentsWithArchived.tsx';
import { PresentFromResultsPage } from './PresentFromResultsPage.tsx';
import { ResultsWithActions } from './ResultsWithActions.tsx';
import './s3e6.css';

/**
 * S3-E6 — Chữa bài sang Trình chiếu, xuất kết quả, lưu trữ lớp, nhắc việc. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-32 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e6-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 * T-32 (US-S3E6-01, 02): bọc Kết quả lớp (S3-E3) bằng "Xuất kết quả" + nút "Chữa bài này" trên thẻ nhận định; màn
 * Trình chiếu từ kết quả /gv/chua-bai; bọc màn một lớp (S3-E1) bằng Lưu trữ / Bỏ lưu trữ; bọc Bài của em (E1 + cổng
 * riêng tư S3-E5) bằng mục "Lớp đã lưu trữ". Route cùng đường dẫn: epic sau thay epic trước.
 */
export const feature: FeatureModule = {
  id: 's3e6',
  title: 'Chữa bài sang Trình chiếu, xuất kết quả, lưu trữ lớp, nhắc việc',
  teacherRoutes: [
    { path: 'ket-qua-lop', element: <ResultsWithActions /> },
    { path: 'chua-bai', element: <PresentFromResultsPage /> },
    { path: 'lop-hoc/:id', element: <ClassDetailWithArchive /> },
  ],
  studentRoutes: [{ path: 'bai-cua-em', element: <MyAssignmentsWithArchived /> }],
  teacherNav: [],
  studentNav: [],
};
