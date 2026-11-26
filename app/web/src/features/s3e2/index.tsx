import type { FeatureModule } from '../types.ts';

/**
 * S3-E2 — Bộ bài, lịch mở lời giải, Bài của em theo bộ. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-29 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e2-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e2',
  title: 'Bộ bài, lịch mở lời giải, Bài của em theo bộ',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
