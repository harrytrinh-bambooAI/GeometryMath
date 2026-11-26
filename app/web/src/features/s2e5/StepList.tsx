import { useState } from 'react';
import type { CheckState, StepView } from './api.ts';
import { MAX_STEPS, MSG, type DraftStep } from './logic.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

const CHIP: Record<CheckState, { cls: string; label: string }> = {
  pass: { cls: 'chip grn', label: 'Đạt' },
  fail: { cls: 'chip red', label: 'Lệch' },
  unverifiable: { cls: 'chip', label: 'Không kiểm được' },
  unchecked: { cls: 'chip yel', label: 'Chưa kiểm' },
};

interface Props {
  draft: DraftStep[];
  /** Các bước của phiên bản đã lưu kèm StepCheck. */
  saved: StepView[];
  /** Chỉ số bước đầu tiên đã sửa so với bản lưu: từ đó trở đi hiện "Chưa kiểm". */
  changedFrom: number;
  readOnly: boolean;
  issueIndex: number | null;
  onChange: (index: number, text: string) => void;
  onAddAfter: (no: number) => void;
  onDelete: (index: number) => void;
  onMove: (from: number, to: number) => void;
}

/** Danh sách bước của lời giải: sửa, thêm, xóa, kéo đổi thứ tự; mỗi bước kèm kết quả StepCheck (US-S2E5-01, 02). */
export function StepList({ draft, saved, changedFrom, readOnly, issueIndex, onChange, onAddAfter, onDelete, onMove }: Props) {
  const [dragFrom, setDragFrom] = useState<number | null>(null);
  const full = draft.length >= MAX_STEPS;

  return (
    <ol className="s2e5-steps" data-testid="s2e5-steps">
      {draft.map((d, i) => {
        const no = i + 1;
        const s = i < changedFrom ? saved[i] : undefined;
        const check: CheckState = s?.check ?? 'unchecked';
        const chip = CHIP[check];
        const invalid = issueIndex === i;
        return (
          <li
            key={d.key}
            className={`s2e5-step${check === 'fail' ? ' bad' : ''}${invalid ? ' invalid' : ''}`}
            data-testid={`s2e5-step-${no}`}
            data-check={check}
            onDragOver={(e) => {
              if (dragFrom !== null) e.preventDefault();
            }}
            onDrop={(e) => {
              e.preventDefault();
              if (dragFrom !== null) onMove(dragFrom, i);
              setDragFrom(null);
            }}
          >
            <div className="s2e5-step-head">
              {!readOnly && (
                <span
                  className="s2e5-grip"
                  draggable
                  role="button"
                  tabIndex={-1}
                  aria-label={`Kéo để đổi thứ tự bước ${no}`}
                  title="Kéo để đổi thứ tự"
                  data-testid={`s2e5-step-grip-${no}`}
                  onDragStart={(e) => {
                    e.dataTransfer.effectAllowed = 'move';
                    e.dataTransfer.setData('text/plain', String(i));
                    setDragFrom(i);
                  }}
                  onDragEnd={() => setDragFrom(null)}
                >
                  ⋮⋮
                </span>
              )}
              <span className="s2e5-num" aria-hidden="true">
                {no}
              </span>
              <span className={chip.cls} data-testid={`s2e5-step-check-${no}`}>
                {chip.label}
              </span>
              {!readOnly && (
                <span className="s2e5-step-actions">
                  <button type="button" className="btn ghost" disabled={i === 0} onClick={() => onMove(i, i - 1)} aria-label={`Đưa bước ${no} lên`} data-testid={`s2e5-step-up-${no}`}><Icon name="up" />
                    Lên
                  </button>
                  <button type="button" className="btn ghost" disabled={i === draft.length - 1} onClick={() => onMove(i, i + 1)} aria-label={`Đưa bước ${no} xuống`} data-testid={`s2e5-step-down-${no}`}><Icon name="down" />
                    Xuống
                  </button>
                  <button type="button" className="btn ghost" onClick={() => onDelete(i)} aria-label={`Xóa bước ${no}`} data-testid={`s2e5-step-delete-${no}`}><Icon name="trash" />
                    Xóa
                  </button>
                </span>
              )}
            </div>
            {readOnly ? (
              <p className="s2e5-step-text" data-math data-testid={`s2e5-step-text-${no}`}>
                {d.text}
              </p>
            ) : (
              <textarea
                className="s2e5-step-input"
                data-math
                rows={2}
                value={d.text}
                aria-label={`Bước ${no}`}
                aria-invalid={invalid}
                onChange={(e) => onChange(i, e.target.value)}
                data-testid={`s2e5-step-text-${no}`}
              />
            )}
            {s?.reason && check !== 'pass' && (
              <p className={`s2e5-reason${check === 'fail' ? ' bad' : ''}`} data-math data-testid={`s2e5-step-reason-${no}`}>
                {s.reason}
              </p>
            )}
            {!readOnly && (
              <button type="button" className="btn ghost s2e5-add" disabled={full} onClick={() => onAddAfter(no)} title={full ? MSG.maxSteps : undefined} data-testid={`s2e5-step-add-after-${no}`}>
                + Thêm bước
              </button>
            )}
          </li>
        );
      })}
    </ol>
  );
}
