import type { FeatureModule } from '../types.ts';
import { DocPage } from './DocPage.tsx';
import { LibraryPage } from './LibraryPage.tsx';
import { QueuePage } from './QueuePage.tsx';
import { SharedPage } from './SharedPage.tsx';

/**
 * S2-E6 — Kho đề: Của tôi / Kho chung, người soát nội dung (T-261126-23, khung C2-01).
 *   /gv/kho-de-cua-toi                  hai ngăn + lọc loại hình (US-S2E6-01)
 *   /gv/kho-de-cua-toi/de/:id           đề Của tôi: nhãn, trạng thái kho, "Đề xuất vào Kho chung", "Trình chiếu" (S2-E7)
 *   /gv/kho-de-cua-toi/kho-chung/:id    đề Kho chung: lời giải xem ngay (BR-S2E6-14), sao chép về Của tôi
 *   /gv/kho-de-cua-toi/hang-doi-soat    hàng đợi của người soát nội dung (US-S2E6-02)
 * data-testid bắt đầu bằng 's2e6-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e6',
  title: 'Kho đề: Của tôi / Kho chung, người soát nội dung',
  teacherRoutes: [
    { path: 'kho-de-cua-toi', element: <LibraryPage /> },
    { path: 'kho-de-cua-toi/de/:id', element: <DocPage /> },
    { path: 'kho-de-cua-toi/kho-chung/:id', element: <SharedPage /> },
    { path: 'kho-de-cua-toi/hang-doi-soat', element: <QueuePage /> },
  ],
  studentRoutes: [],
  teacherNav: [{ to: 'kho-de-cua-toi', label: 'Kho của tôi', testId: 's2e6-nav-kho', icon: 'layers' }],
  studentNav: [],
};
