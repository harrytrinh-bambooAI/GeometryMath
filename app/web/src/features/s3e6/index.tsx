import type { FeatureModule } from '../types.ts';

/**
 * S3-E6 — Chữa bài sang Trình chiếu, xuất kết quả, lưu trữ lớp, nhắc việc. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-32 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e6-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e6',
  title: 'Chữa bài sang Trình chiếu, xuất kết quả, lưu trữ lớp, nhắc việc',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
