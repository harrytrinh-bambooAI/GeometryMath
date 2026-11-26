import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon.tsx';
import { saveTextOnly, supplement, type DraftView } from './api.ts';
import { MSG } from './logic.ts';

type Way = 'more' | 'manual' | 'text';

/**
 * Engine không dựng được hình (US-S2E3-04-SC1, BR-S2E3-15; Design c2-02-engine-khong-dung): đề lưu với "Cần xem lại ·
 * hình do AI dựng" và ba lối đi: bổ sung điều kiện, dựng tay, lưu bản nháp chữ (chưa giao được).
 */
export function Unbuildable({ draft, busy, act }: { draft: DraftView; busy: boolean; act: (call: () => Promise<DraftView>, info?: string) => void }) {
  const [way, setWay] = useState<Way>('more');
  const [cond, setCond] = useState('');
  const opt = (k: Way, title: string, sub: string, icon: 'pencil' | 'cube' | 'file') => (
    <button type="button" className={`s2e3-opt${way === k ? ' on' : ''}`} aria-pressed={way === k} onClick={() => setWay(k)} data-testid={`s2e3-way-${k}`}>
      <Icon name={icon} />
      <span>
        <b>{title}</b>
        <span className="s2e3-sub muted"> {sub}</span>
      </span>
    </button>
  );
  return (
    <section className="card s2e3-card" data-testid="s2e3-unbuildable">
      <p className="s2e3-ban red" role="status">
        <Icon name="warn" inline />
        Máy chưa dựng được hình từ đề này.
      </p>
      <span className="chip yel" data-testid="s2e3-ai-figure">
        <Icon name="eye" />
        {MSG.aiFigure}
      </span>
      <h2 className="s2e3-h">Chọn cách đi tiếp</h2>
      {opt('more', 'Bổ sung điều kiện', 'Thêm điều kiện để máy dựng được, ví dụ SM = 2·MD', 'pencil')}
      {opt('manual', 'Dựng tay', 'Thầy cô đặt điểm trong trình dựng hình; máy kiểm tra giúp', 'cube')}
      {opt('text', 'Lưu bản nháp chữ', 'Chưa có hình, mang nhãn "Cần xem lại", chưa giao được cho học sinh', 'file')}
      {way === 'more' && (
        <form
          className="s2e3-row"
          onSubmit={(e) => {
            e.preventDefault();
            if (cond.trim()) act(() => supplement(draft.id, cond.trim()));
          }}
        >
          <input className="s2e3-input" aria-label="Điều kiện bổ sung" value={cond} onChange={(e) => setCond(e.target.value)} data-math data-testid="s2e3-condition" />
          <button type="submit" className="btn" disabled={busy || !cond.trim()} data-testid="s2e3-rebuild">
            <Icon name="cube" />
            Dựng lại
          </button>
        </form>
      )}
      {way === 'manual' && (
        <p className="s2e3-sub muted" data-testid="s2e3-manual-note">
          Trình dựng tay mở từ "Của tôi" ở bản sau; đề vẫn giữ nhãn "Cần xem lại".
        </p>
      )}
      {way === 'text' && (
        <button type="button" className="btn" disabled={busy} onClick={() => act(() => saveTextOnly(draft.id), 'Đã lưu bản nháp chữ. Bản nháp chữ chưa giao được.')} data-testid="s2e3-save-text-only">
          <Icon name="save" />
          Lưu bản nháp chữ
        </button>
      )}
    </section>
  );
}
