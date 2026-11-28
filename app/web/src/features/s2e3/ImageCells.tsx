import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon.tsx';
import type { CellView } from './api.ts';
import { MSG } from './logic.ts';

interface Props {
  cells: CellView[];
  busy: boolean;
  /** Lưu trạng thái ô: sửa chữ một ô hoặc bấm "đúng" (BR-S2E3-08). */
  onChange: (cells: Array<{ text: string; state?: string }>) => void;
}

/**
 * Chữ đọc từ ảnh: ô có độ tin cậy dưới 0,80 tô vàng (BR-S2E3-07, DQ-02). Ô vàng sửa trực tiếp hoặc bấm "đúng".
 * Chữ hiển thị luôn là văn bản thuần (React không chèn HTML).
 */
export function ImageCells({ cells, busy, onChange }: Props) {
  const [edits, setEdits] = useState<Record<number, string>>({});
  const send = (index: number, patch: { text?: string; state?: string }) =>
    onChange(cells.map((c) => (c.index === index ? { text: patch.text ?? c.text, state: patch.state } : { text: c.text })));

  return (
    <div className="s2e3-cells" data-testid="s2e3-cells">
      <div className="s2e3-cap">ĐỀ ĐÃ CHÉP LẠI · sửa trực tiếp</div>
      <p className="s2e3-cell-line" data-math>
        {cells.map((c) =>
          c.state === 'unsure' ? (
            <span key={c.index} className="s2e3-cell s2e3-unsure" data-testid={`s2e3-cell-${c.index}`} data-state={c.state}>
              <input
                className="s2e3-cell-input"
                aria-label={`Ô chưa chắc ${c.index + 1}`}
                value={edits[c.index] ?? c.text}
                onChange={(e) => setEdits({ ...edits, [c.index]: e.target.value })}
                data-math
                data-testid={`s2e3-cell-input-${c.index}`}
              />
              <button
                type="button"
                className="btn ghost s2e3-cell-btn"
                disabled={busy}
                onClick={() => {
                  const t = (edits[c.index] ?? c.text).trim();
                  send(c.index, t !== c.text ? { text: t } : { state: 'confirmed' });
                }}
                data-testid={`s2e3-cell-ok-${c.index}`}
              >
                <Icon name="check" />
                {(edits[c.index] ?? c.text).trim() !== c.text ? 'Lưu sửa' : 'đúng'}
              </button>
            </span>
          ) : (
            <span key={c.index} className="s2e3-cell" data-testid={`s2e3-cell-${c.index}`} data-state={c.state}>
              {c.text}{' '}
            </span>
          ),
        )}
      </p>
      {cells.some((c) => c.state === 'unsure') && (
        <p className="s2e3-unsure-note" data-testid="s2e3-unsure-note">
          <Icon name="warn" inline />
          {MSG.unsureHint}
        </p>
      )}
    </div>
  );
}
