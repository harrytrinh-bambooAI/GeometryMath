import type { FeatureModule } from '../types.ts';
import { MyAssignmentsPage } from '../e1/MyAssignmentsPage.tsx';
import { StudentAssignmentPage } from '../e1/StudentAssignmentPage.tsx';
import { KhoDePage } from '../e5/KhoDePage.tsx';
import { ClassAiGuard } from './ClassAiGuard.tsx';
import { PrivacyGate } from './PrivacyGate.tsx';
import { PrivacyNoticePage } from './PrivacyNoticePage.tsx';
import { SettingsWithDelete } from './SettingsWithDelete.tsx';
import './s3e5.css';

/**
 * S3-E5 — riêng tư, công tắc AI theo lớp (phần của T-261126-28: US-S3E5-02, 04; 01 ở T-31, 03/05 ở T-32).
 * Không thêm màn mới: bọc màn đầu sau đăng nhập (/hs/bai-cua-em, /gv/kho-de) bằng cổng thông báo riêng tư và màn một bài
 * (/hs/bai/:id) bằng công tắc AI của lớp. T-32 (US-S3E5-05): Cài đặt (/gv|hs/cai-dat) thêm mục "Xóa tài khoản" (bọc màn
 * Cài đặt chung), màn xem lại thông báo riêng tư /gv|hs/rieng-tu. Route cùng đường dẫn: epic sau thay epic trước (features/index.ts), màn gốc
 * của E1/E5 giữ nguyên bên trong.
 */
export const feature: FeatureModule = {
  id: 's3e5',
  title: 'Nhật ký thao tác nhạy cảm, riêng tư, công tắc AI theo lớp',
  teacherRoutes: [
    {
      path: 'kho-de',
      element: (
        <PrivacyGate>
          <KhoDePage />
        </PrivacyGate>
      ),
    },
    { path: 'cai-dat', element: <SettingsWithDelete area="gv" /> },
    { path: 'rieng-tu', element: <PrivacyNoticePage /> },
  ],
  studentRoutes: [
    {
      path: 'bai-cua-em',
      element: (
        <PrivacyGate>
          <MyAssignmentsPage />
        </PrivacyGate>
      ),
    },
    {
      path: 'bai/:id',
      element: (
        <ClassAiGuard>
          <StudentAssignmentPage />
        </ClassAiGuard>
      ),
    },
    { path: 'cai-dat', element: <SettingsWithDelete area="hs" /> },
    { path: 'rieng-tu', element: <PrivacyNoticePage /> },
  ],
  teacherNav: [],
  studentNav: [],
};
