import { useState } from 'react';
import { useParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { useToolEvents } from './useToolEvents.ts';
import { useLockState, useWorkspace } from './useWorkspace.ts';
import { WorkspacePanel, type HintState, type PanelSlots, type PanelTab } from './WorkspacePanel.tsx';
import './e3.css';

/**
 * Không gian làm bài của học sinh `/hs/lam-bai/:id` (bài giao của lớp và bài tự học; US-GEO-301…307).
 * - Hình 3D (bộ xem dùng chung) + trạng thái khóa hỏi lại mỗi 5 giây (US-GEO-305-SC6).
 * - Sự kiện thao tác gom lô (BR-GEO-144: xoay/thu phóng/đặt lại không ghi), sự kiện mở bài một lần.
 * - Bảng Gợi ý / Lời giải / Hỏi: cột phải trên iPad/máy tính, bảng trượt dưới trên điện thoại (BR-GEO-159). Bố cục chỉ
 *   đổi bằng CSS nên xoay máy không mất hình vẽ thêm, tab đang mở hay gợi ý đã xem (TC-GEO-0346).
 * `slots`: chỗ cắm nội dung tab của E4.
 */
export function WorkspacePage({ slots }: { slots?: PanelSlots }) {
  const { id = '' } = useParams();
  const { data, error } = useWorkspace(id);
  const lock = useLockState(id, data !== null);
  const onToolEvent = useToolEvents(data ? id : null);
  const [tab, setTab] = useState<PanelTab>('hint');
  const [sheetOpen, setSheetOpen] = useState(false);
  const [hints, setHints] = useState<HintState>({ shown: [] });

  if (error) return <ErrorNotice error={error} testId={tid('e3', 'workspace', 'error')} />;
  if (!data) return <p className="muted">Đang tải…</p>;

  const banner = data.scene.verification.warning?.text; // MSG-GEO-16 cố định trên khung hình (BR-GEO-137)
  return (
    <div className="e3-workspace" data-testid={tid('e3', 'workspace')} data-lock={lock} data-kind={data.kind}>
      <header className="e3-head">
        <h2>
          {data.title} <span className="muted small">· {data.subtitle}</span>
        </h2>
        <p className="e3-statement" data-testid={tid('e3', 'statement')}>
          {data.statement}
        </p>
      </header>
      <div className="e3-main">
        <Figure3DViewer solid={data.solid} lock={lock} onToolEvent={onToolEvent} banner={banner} className="e3-viewer" />
      </div>
      <WorkspacePanel
        assignmentId={id}
        lock={lock}
        tab={tab}
        onTab={(t) => {
          setTab(t);
          setSheetOpen(true);
        }}
        open={sheetOpen}
        onToggle={() => setSheetOpen((o) => !o)}
        hints={hints}
        onHints={setHints}
        slots={slots}
      />
    </div>
  );
}
