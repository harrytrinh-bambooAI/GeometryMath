import type { FeatureModule } from '../types.ts';

/**
 * S3-E1 — Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-28 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e1-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e1',
  title: 'Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
