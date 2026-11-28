import type { FeatureModule } from '../types.ts';
import { AiReviewPage } from './review/AiReviewPage.tsx';

/**
 * S5-E6 — AI đề xuất điểm theo rubric, chạy song song, giáo viên duyệt. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-61, 62, 65 (kpi/; cờ geo.s5.ai-grading.enabled) (PLAN_code-s5 §5).
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e6-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 * T-261128-62: /gv/cham-ai/:id — màn chấm có đề xuất AI (US-S5E6-04), AI không chấm (US-S5E6-02), chạy song song (US-S5E6-05).
 */
export const feature: FeatureModule = {
  id: 's5e6',
  title: 'AI đề xuất điểm theo rubric, chạy song song, giáo viên duyệt',
  teacherRoutes: [{ path: 'cham-ai/:id', element: <AiReviewPage /> }],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
