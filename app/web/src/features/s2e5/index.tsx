import type { FeatureModule } from '../types.ts';
import { PinnedLabelsPage } from './PinnedLabelsPage.tsx';
import { SolutionListPage } from './SolutionListPage.tsx';
import { SolutionReviewPage } from './SolutionReviewPage.tsx';

/**
 * S2-E5 — Soát và duyệt lời giải có phiên bản (T-261126-20; US-S2E5-05 thuộc T-261126-22, PLAN_code-s23 §3).
 *   /gv/soat-loi-giai        danh sách đề đang soát của giáo viên
 *   /gv/soat-loi-giai/:id    C2-03: sửa từng bước thành phiên bản mới, StepCheck, nhãn hai trục, hộp duyệt
 *   /hs/nhan-loi-giai/:id    học sinh: nhãn hai trục của phiên bản lời giải đã giao cho bài (:id = mã bài giao)
 * data-testid bắt đầu bằng 's2e5-'. Không sửa features/index.ts, types.ts, router.tsx. Chưa thêm mục rail: lối vào
 * từ Kho đề / "Của tôi" thuộc S2-E3, S2-E6.
 */
export const feature: FeatureModule = {
  id: 's2e5',
  title: 'Soát và duyệt lời giải có phiên bản',
  teacherRoutes: [
    { path: 'soat-loi-giai', element: <SolutionListPage /> },
    { path: 'soat-loi-giai/:id', element: <SolutionReviewPage /> },
  ],
  studentRoutes: [{ path: 'nhan-loi-giai/:id', element: <PinnedLabelsPage /> }],
  teacherNav: [],
  studentNav: [],
};
