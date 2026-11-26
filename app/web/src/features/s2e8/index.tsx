import type { FeatureModule } from '../types.ts';

/**
 * S2-E8 — Bài luyện điền đáp số. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-21 (câu trắc nghiệm: T-261126-25) (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's2e8-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e8',
  title: 'Bài luyện điền đáp số',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
