import type { FeatureModule } from '../types.ts';
import { LessonResultPage } from './LessonResultPage.tsx';
import { NewLessonPage } from './NewLessonPage.tsx';
import './s5e1.css';

/**
 * S5-E1 — Trợ lý soạn giáo án. Chỗ cắm do T-261128-51 tạo; chủ: T-261128-53 (form, màn kết quả), 54 (chat vá, duyệt,
 * xuất, danh sách) (PLAN_code-s5 §5).
 *   /gv/giao-an/moi   C5-08 "Soạn giáo án mới" (US-S5E1-01)
 *   /gv/giao-an/:id   màn kết quả dựng theo kind (US-S5E1-03); :id = mã GA-nnnn hoặc id
 * data-testid bắt đầu bằng 's5e1-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's5e1',
  title: 'Trợ lý soạn giáo án',
  teacherRoutes: [
    { path: 'giao-an/moi', element: <NewLessonPage /> },
    { path: 'giao-an/:id', element: <LessonResultPage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'giao-an/moi', label: 'Giáo án', testId: 's5e1-nav-giao-an', icon: 'file' }],
  studentNav: [],
};
