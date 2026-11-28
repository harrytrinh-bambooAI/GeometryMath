import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon.tsx';
import type { AnswersView } from './api.ts';
import { MathText } from '../../shared/math/MathText.tsx';

interface Props {
  answers: AnswersView;
  busy?: boolean;
  /** "Dùng đáp số máy" (US-S2E3-04-SC5): phiên bản mới mang đáp số máy rồi kiểm lại. */
  onUseMachine?: () => void;
  /** "Sửa đáp số đề ghi" (US-S2E3-04-SC6): lưu đáp số mới; giáo viên bấm "Kiểm tra lại" sau đó. */
  onSetStated?: (value: string) => void;
  testPrefix?: string;
}

/**
 * Bảng đối chiếu ba đáp số "Đề ghi / AI giải / Máy tính lại" (S2-E3, BR-S2E3-17, 18; Design c2-02-lech-dap-so).
 * Còn lệch thì hiện ba cách xử lý; không có đường tắt tới nút Duyệt.
 */
export function AnswerTable({ answers, busy, onUseMachine, onSetStated, testPrefix = 's2e5' }: Props) {
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState('');
  const bad = answers.state === 'mismatch';
  const t = (s: string) => `${testPrefix}-${s}`;
  const row = (label: string, v: string | null | undefined, source: string, key: string, hot = false) => (
    <tr className={hot ? 's2e5-ans-bad' : undefined}>
      <th scope="row">{label}</th>
      <td className="s2e5-ans-val" data-math data-testid={t(`answer-${key}`)}>
        <MathText text={v ?? 'chưa có'} />
      </td>
      <td className="s2e5-sub muted">{source}</td>
    </tr>
  );
  return (
    <section className="card s2e5-answers" data-testid={t('answers')} data-state={answers.state}>
      <h3 className="s2e5-cap">Đối chiếu đáp số</h3>
      <table className="s2e5-ans-table">
        <thead>
          <tr>
            <th scope="col" />
            <th scope="col">Đáp số</th>
            <th scope="col">Nguồn</th>
          </tr>
        </thead>
        <tbody>
          {row('Đề ghi', answers.stated, 'Chữ trong đề thầy cô dán', 'stated')}
          {row('AI giải', answers.ai, 'Bước cuối của lời giải', 'ai')}
          {row('Máy tính lại', answers.computed, 'Engine tính trên hình', 'computed', bad)}
        </tbody>
      </table>
      {bad && (
        <div className="s2e5-ans-actions">
          {onUseMachine && answers.computed && (
            <button type="button" className="btn" disabled={busy} onClick={onUseMachine} data-testid={t('use-machine')}>
              <Icon name="check" />
              Dùng đáp số máy ({answers.computed})
            </button>
          )}
          {onSetStated && !editing && (
            <button type="button" className="btn ghost" disabled={busy} onClick={() => setEditing(true)} data-testid={t('edit-stated')}>
              <Icon name="pencil" />
              Sửa đáp số đề ghi
            </button>
          )}
          {onSetStated && editing && (
            <form
              className="s2e5-row"
              onSubmit={(e) => {
                e.preventDefault();
                if (value.trim()) {
                  onSetStated(value.trim());
                  setEditing(false);
                }
              }}
            >
              <label className="s2e5-sub" htmlFor={t('stated-input')}>
                Đáp số đề ghi
              </label>
              <input
                id={t('stated-input')}
                className="s2e5-ans-input"
                value={value}
                onChange={(e) => setValue(e.target.value)}
                data-math
                data-testid={t('stated-input')}
              />
              <button type="submit" className="btn" disabled={busy || !value.trim()} data-testid={t('stated-save')}>
                <Icon name="save" />
                Lưu đáp số
              </button>
            </form>
          )}
          <p className="s2e5-sub muted">Không có nút Duyệt khi còn lệch. Sửa xong bấm "Kiểm tra lại".</p>
        </div>
      )}
    </section>
  );
}
