import type { FeatureModule } from '../types.ts';

/**
 * S3-E4 — Tổng quan giáo viên, theo dõi nhanh, thông báo trong ứng dụng. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-31 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e4-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e4',
  title: 'Tổng quan giáo viên, theo dõi nhanh, thông báo trong ứng dụng',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
