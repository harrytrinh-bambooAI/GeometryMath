import type { FeatureModule } from '../types.ts';
import { AssignPage } from './AssignPage.tsx';
import { PreviewPage } from './PreviewPage.tsx';
import './e6.css';

/**
 * E6 — Giao bài và khóa lời giải.
 * Giáo viên: /gv/giao-bai/:versionId ("Chi tiết đề &amp; giao bài": chọn lớp, trợ giúp, giờ mở; thay bản tối thiểu của E1),
 * /gv/xem-truoc/:versionId?classId=&releaseAt= (Xem như học sinh). Thẻ vàng "Mở ngay" / "Đổi giờ" (ReleaseCard.tsx) cắm
 * vào Theo dõi lớp (E1/E7). Học sinh: thẻ "Bài của em" (AssignmentCards.tsx, cắm vào trang E1), thẻ khóa và tự mở khi
 * tới giờ (LockCard.tsx, useReleaseWatch.ts, cắm vào tab Lời giải của E4).
 * data-testid của epic bắt đầu bằng 'e6-' (mã 'e1-…' cũ giữ nguyên cho ca E1).
 */
export const feature: FeatureModule = {
  id: 'e6',
  title: 'Giao bài và khóa lời giải',
  teacherRoutes: [
    { path: 'giao-bai/:versionId', element: <AssignPage /> },
    { path: 'xem-truoc/:versionId', element: <PreviewPage /> },
  ],
  studentRoutes: [],
  teacherNav: [],
  studentNav: [],
};
