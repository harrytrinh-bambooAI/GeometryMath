import type { FeatureModule } from '../types.ts';

/**
 * S2-E5 — Soát và duyệt lời giải có phiên bản. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-20 (US-S2E5-05: T-261126-22) (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e5-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e5',
  title: 'Soát và duyệt lời giải có phiên bản',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
