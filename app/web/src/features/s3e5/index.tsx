import type { FeatureModule } from '../types.ts';

/**
 * S3-E5 — Nhật ký thao tác nhạy cảm, riêng tư, công tắc AI theo lớp. Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-28, 31, 32 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e5-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e5',
  title: 'Nhật ký thao tác nhạy cảm, riêng tư, công tắc AI theo lớp',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
