import type { FeatureModule } from '../types.ts';
import { StudentHomePage, StudentSetPage } from '../s3e2/StudentSets.tsx';
import { FollowPage } from './FollowPage.tsx';
import { NotificationsPage, WithBell } from './Notifications.tsx';
import { OverviewPage } from './OverviewPage.tsx';
import './s3e4.css';

/**
 * S3-E4 — Tổng quan giáo viên, theo dõi nhanh, thông báo trong ứng dụng (T-261126-31; kèm nhật ký AuditEvent của
 * US-S3E5-01 ở màn Theo dõi nhanh). Bản mẫu chặng 2–3 không vẽ C3-05/C3-06 (DQ.md "Chưa vẽ"): dựng theo AC bằng lớp
 * dùng chung và token theme.
 * Giáo viên: /gv/tong-quan (C3-05) · /gv/theo-doi-nhanh?set=… (C3-06: em chưa mở bài, Mở ngay, Đổi giờ, nhật ký của lớp).
 * Học sinh: /hs/thong-bao; chấm thông báo gắn trên /hs/bo-bai và /hs/bo-bai/:id (bọc màn S3-E2: route cùng đường dẫn,
 * epic sau thay epic trước — màn gốc giữ nguyên bên trong).
 * data-testid bắt đầu bằng 's3e4-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's3e4',
  title: 'Tổng quan giáo viên, theo dõi nhanh, thông báo trong ứng dụng',
  teacherRoutes: [
    { path: 'tong-quan', element: <OverviewPage /> },
    { path: 'theo-doi-nhanh', element: <FollowPage /> },
  ],
  studentRoutes: [
    { path: 'thong-bao', element: <NotificationsPage /> },
    {
      path: 'bo-bai',
      element: (
        <WithBell>
          <StudentHomePage />
        </WithBell>
      ),
    },
    {
      path: 'bo-bai/:id',
      element: (
        <WithBell>
          <StudentSetPage />
        </WithBell>
      ),
    },
  ],
  teacherNav: [
    { to: 'tong-quan', label: 'Tổng quan', testId: 's3e4-nav-tong-quan', icon: 'home' },
    { to: 'theo-doi-nhanh', label: 'Xem nhanh', testId: 's3e4-nav-theo-doi-nhanh', icon: 'eye' },
  ],
  studentNav: [{ to: 'thong-bao', label: 'Thông báo', testId: 's3e4-nav-thong-bao', icon: 'info' }],
};
