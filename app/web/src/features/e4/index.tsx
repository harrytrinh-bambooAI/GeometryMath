import type { FeatureModule } from '../types.ts';
import { ExplorePage } from './Explore.tsx';
import { HistoryPage } from './HistoryPage.tsx';
import './e4.css';

/**
 * E4 — Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học.
 * Học sinh: /hs/lich-su (nhật ký hỏi đáp, US-GEO-410), /hs/kham-pha/:code (khám phá hình mẫu, US-GEO-403).
 * Nội dung tab Gợi ý / Lời giải / Hỏi của trang Làm bài (E3 `/hs/lam-bai/:id`) ở slots.tsx; hộp bài giảng ở Lecture.tsx;
 * khối "Tự khám phá" (Explore.tsx) cắm vào "Bài của em" (E1); đọc ảnh đề, màn chờ, "Thử lại" cắm vào "Tự học" (E2).
 * data-testid của epic bắt đầu bằng 'e4-' (xem app/web/README.md).
 */
export const feature: FeatureModule = {
  id: 'e4',
  title: 'Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học',
  teacherRoutes: [],
  studentRoutes: [
    { path: 'lich-su', element: <HistoryPage /> },
    { path: 'kham-pha/:code', element: <ExplorePage /> },
  ],
  teacherNav: [],
  studentNav: [{ to: 'lich-su', label: 'Lịch sử', testId: 'e4-nav-lich-su', icon: 'clock' }],
};
