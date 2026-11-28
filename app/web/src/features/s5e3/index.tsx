import type { FeatureModule } from '../types.ts';
import { HelpLevelPage } from './HelpLevelPage.tsx';
import { KbPage } from './KbPage.tsx';
import { QaPage } from './QaPage.tsx';
import { useParams } from 'react-router';
import { QaHistoryPanel } from './history/QaHistoryPanel.tsx';
import './s5e3.css';

/**
 * S5-E3 — Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án. Chỗ cắm rỗng do T-261128-51 tạo; chủ: T-261128-56, 58, 63 (history/) (PLAN_code-s5 §5).
 *   /gv/kho-tri-thuc   "Kho tri thức" (US-S5E3-01 thêm tài liệu, phiên bản, duyệt; US-S5E3-02 gắn lớp có hạn) — T-261128-56
 *   /gv/tro-giup-ai    "Trợ giúp AI" mức trợ giúp theo bài có chấm (US-S5E3-04-SC3) — T-261128-58
 *   /hs/hoi-dap?bai=   "Hỏi đáp" theo kho, kèm nguồn, thông báo riêng tư (US-S5E3-03, 05) — T-261128-58
 * Thêm màn: khai báo route/mục rail ở đây (path tương đối của /gv hoặc /hs), data-testid bắt đầu bằng 's5e3-'.
 * Không sửa features/index.ts, types.ts, router.tsx.
 */
/** US-S5E3-06: /gv/kho-tri-thuc/lich-su/:assignmentId — lịch sử hỏi đáp theo bài (T-261128-63). */
function QaHistoryRoute() {
  const { assignmentId = '' } = useParams();
  return <QaHistoryPanel assignmentId={assignmentId} />;
}

export const feature: FeatureModule = {
  id: 's5e3',
  title: 'Kho tri thức, hỏi đáp theo kho, kiểm soát đáp án',
  teacherRoutes: [
    { path: 'kho-tri-thuc', element: <KbPage /> },
    { path: 'kho-tri-thuc/lich-su/:assignmentId', element: <QaHistoryRoute /> },
    { path: 'tro-giup-ai', element: <HelpLevelPage /> },
  ],
  studentRoutes: [{ path: 'hoi-dap', element: <QaPage /> }],
  teacherNav: [
    { to: 'kho-tri-thuc', label: 'Kho TT', testId: 's5e3-nav-kho-tri-thuc', icon: 'file' },
    { to: 'tro-giup-ai', label: 'Trợ giúp AI', testId: 's5e3-nav-tro-giup-ai', icon: 'hint' },
  ],
  studentNav: [{ to: 'hoi-dap', label: 'Hỏi đáp', testId: 's5e3-nav-hoi-dap', icon: 'hint' }],
};
