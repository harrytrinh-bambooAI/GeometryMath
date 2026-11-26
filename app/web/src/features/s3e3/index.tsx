import type { FeatureModule } from '../types.ts';

/**
 * S3-E3 — Kết quả lớp và "Cả lớp vướng ở đâu". Chỗ cắm rỗng do T-261126-41 tạo; chủ: T-261126-30 (PLAN_code-s23 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's3e3-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e3',
  title: 'Kết quả lớp và "Cả lớp vướng ở đâu"',
  teacherRoutes: [],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
