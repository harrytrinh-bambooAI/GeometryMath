import { useState } from 'react';
import type { ReactNode } from 'react';
import { tid } from '../../shared/testid.ts';
import type { LockState } from '../../shared/viewer/types.ts';
import { e3Api } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

export type PanelTab = 'hint' | 'solution' | 'ask';
export const PANEL_TABS: Array<{ id: PanelTab; label: string }> = [
  { id: 'hint', label: 'Gợi ý' },
  { id: 'solution', label: 'Lời giải' },
  { id: 'ask', label: 'Hỏi' },
];

/**
 * CHỖ CẮM cho E4 (Gợi ý, Lời giải, Hỏi): E4 truyền `slots` để thay nội dung từng tab. E3 chỉ dựng khung (tab, bảng
 * trượt điện thoại) và bản tối thiểu của tab Gợi ý (gọi `/hints/{level}`) để trạng thái "Gợi ý 1 đã xem" giữ được khi
 * xoay máy (TC-GEO-0346). Trạng thái tab và gợi ý đã xem nằm ở trang (không mất khi đổi bố cục).
 */
export interface PanelSlots {
  hint?: ReactNode;
  solution?: ReactNode;
  ask?: ReactNode;
}
export interface HintState {
  shown: Array<{ level: number; body: string }>;
}

export function WorkspacePanel(props: {
  assignmentId: string;
  lock: LockState;
  tab: PanelTab;
  onTab: (t: PanelTab) => void;
  open: boolean;
  onToggle: () => void;
  hints: HintState;
  onHints: (h: HintState) => void;
  slots?: PanelSlots;
}) {
  const { tab, onTab, open, onToggle } = props;
  return (
    <section className={`e3-panel${open ? ' e3-panel-open' : ''}`} data-testid={tid('e3', 'panel')} data-open={open}>
      <button type="button" className="e3-grip" aria-label={open ? 'Thu bảng' : 'Mở bảng'} data-testid={tid('e3', 'panel', 'grip')} onClick={onToggle}>
        <span />
      </button>
      <div className="e3-tabs" role="tablist" aria-label="Bảng hỗ trợ">
        {PANEL_TABS.map((t) => (
          <button
            key={t.id}
            type="button"
            role="tab"
            className="e3-tab"
            aria-selected={tab === t.id}
            data-testid={tid('e3', 'tab', t.id)}
            onClick={() => onTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </div>
      <div className="e3-panel-body" role="tabpanel" data-testid={tid('e3', 'tabpanel', tab)}>
        {tab === 'hint' && (props.slots?.hint ?? <HintTab id={props.assignmentId} hints={props.hints} onHints={props.onHints} />)}
        {tab === 'solution' &&
          (props.slots?.solution ?? (
            <p className="muted">{props.lock === 'locked' ? <Icon name="lock" inline /> : null}{props.lock === 'locked' ? 'Lời giải sẽ hiện khi đến giờ mở.' : 'Lời giải từng bước (E4).'}</p>
          ))}
        {tab === 'ask' && (props.slots?.ask ?? <p className="muted">Hỏi trợ lý về bài này (E4).</p>)}
      </div>
    </section>
  );
}

function HintTab({ id, hints, onHints }: { id: string; hints: HintState; onHints: (h: HintState) => void }) {
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const next = hints.shown.length + 1;
  const ask = async () => {
    setBusy(true);
    setErr(null);
    try {
      const h = await e3Api.hint(id, next as 1 | 2);
      onHints({ shown: [...hints.shown, { level: h.level, body: h.body }] });
    } catch {
      setErr('Chưa lấy được gợi ý. Em thử lại nhé.');
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="e3-hints">
      {hints.shown.map((h) => (
        <div key={h.level} className="card e3-hint" data-testid={tid('e3', 'hint', h.level)}>
          <strong>Gợi ý {h.level}</strong>
          <p><MathText text={h.body} /></p>
        </div>
      ))}
      {next <= 2 && (
        <button type="button" className="btn" disabled={busy} data-testid={tid('e3', 'hint', 'next')} onClick={() => void ask()}><Icon name="hint" />
          Xem gợi ý {next}
        </button>
      )}
      {err && (
        <p className="e3-err" role="alert">
          {err}
        </p>
      )}
    </div>
  );
}
