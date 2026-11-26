import type { FeatureModule } from '../types.ts';

/**
 * S2-E1 — Đăng ký, đăng nhập bằng mã và khôi phục. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-18 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e1-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e1',
  title: 'Đăng ký, đăng nhập bằng mã và khôi phục',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
