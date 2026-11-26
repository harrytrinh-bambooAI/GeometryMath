import type { FeatureModule } from '../types.ts';
import { ChatPage } from './ChatPage.tsx';
import { DraftListPage } from './DraftListPage.tsx';
import { ImportPage } from './ImportPage.tsx';
import './s2e4.css';

/**
 * S2-E4 — Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng (T-261126-24; Design/prototype/s23c.js c2-04a…d).
 *   /gv/chinh-nhap       mục "Nháp": chọn nháp để trao đổi với AI
 *   /gv/chinh-nhap/:id   C2-04a: chat, đề xuất trước / sau, Nhận / Sửa tay / Bác, Hoàn tác
 *   /gv/nhap-tu-tep      C2-04b…d: nạp docx / pdf, tách từng bài, đối chiếu trùng, tạo nháp
 * data-testid bắt đầu bằng 's2e4-'. Không sửa features/index.ts, types.ts, router.tsx.
 */
export const feature: FeatureModule = {
  id: 's2e4',
  title: 'Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng',
  teacherRoutes: [
    { path: 'chinh-nhap', element: <DraftListPage /> },
    { path: 'chinh-nhap/:id', element: <ChatPage /> },
    { path: 'nhap-tu-tep', element: <ImportPage /> },
  ],
  studentRoutes: [],
  teacherNav: [
    { to: 'chinh-nhap', label: 'Chỉnh nháp', testId: 's2e4-nav-chinh-nhap', icon: 'pencil' },
    { to: 'nhap-tu-tep', label: 'Nhập tệp', testId: 's2e4-nav-nhap-tep', icon: 'file' },
  ],
  studentNav: [],
};
