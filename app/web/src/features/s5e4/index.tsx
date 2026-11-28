import type { FeatureModule } from '../types.ts';
import { GradedPage } from './assign/GradedPage.tsx';
import { RubricPage } from './rubric/RubricPage.tsx';
import { SubmitPage } from './submit/SubmitPage.tsx';
import './rubric/s5e4.css';

/**
 * S5-E4 — Rubric, bài có chấm, nộp bài viết tay, chấm tay, trả bài. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-57, 59, 60 (cờ geo.s5.grading.enabled) (PLAN_code-s5 §5).
 *   /gv/rubric          C5-02 "Rubric" (US-S5E4-01) — T-261128-57
 *   /gv/bai-co-cham     C5-01 "Bài có chấm" (US-S5E4-02) — T-261128-57
 *   /hs/nop-bai         C5-03 "Nộp bài" (US-S5E4-03, 09) — T-261128-59
 * Cờ tắt ở máy chủ: API 404, màn báo lỗi (menu do khung quyết định hiện/ẩn).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e4-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e4',
  title: 'Rubric, bài có chấm, nộp bài viết tay, chấm tay, trả bài',
  teacherRoutes: [
    { path: 'rubric', element: <RubricPage /> },
    { path: 'bai-co-cham', element: <GradedPage /> },
  ],
  studentRoutes: [{ path: 'nop-bai', element: <SubmitPage /> }],
  teacherNav: [
    { to: 'rubric', label: 'Rubric', testId: 's5e4-nav-rubric', icon: 'check' },
    { to: 'bai-co-cham', label: 'Bài chấm', testId: 's5e4-nav-bai-co-cham', icon: 'assign' },
  ],
  studentNav: [{ to: 'nop-bai', label: 'Nộp bài', testId: 's5e4-nav-nop-bai', icon: 'camera' }],
};
