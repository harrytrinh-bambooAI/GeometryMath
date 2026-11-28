import { useEffect, useState } from 'react';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../../shared/ui/Icon.tsx';
import { HELP_LEVELS, getHelpLevel, putHelpLevel, type HelpLevel, type HelpLevelView } from './HelpLevelApi.ts';

/**
 * Mục "Trợ giúp của AI" của một bài (US-S5E3-04-SC3): mặc định Tắt cho bài có chấm, Gợi ý và khái niệm cho bài luyện
 * (QS5-12); chọn rồi Lưu, bài hiện "Trợ giúp: …". Máy chủ kiểm mức này ở mỗi lượt hỏi. data-testid 's5e4-help-*'.
 */
export function HelpLevelPanel({ assignmentId }: { assignmentId: string }) {
  const [view, setView] = useState<HelpLevelView | null>(null);
  const [level, setLevel] = useState<HelpLevel>('off');
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getHelpLevel(assignmentId)
      .then((v) => {
        setView(v);
        setLevel(v.helpLevel);
      })
      .catch(setError);
  }, [assignmentId]);

  const save = async () => {
    setBusy(true);
    setError(null);
    try {
      const v = await putHelpLevel(assignmentId, level);
      setView(v);
      setOpen(false);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="s5e4-help" data-testid={`s5e4-help-${assignmentId}`}>
      {view && (
        <span className="chip" data-testid="s5e4-help-summary">
          {view.summary}
        </span>
      )}
      <button type="button" className="btn" onClick={() => setOpen((o) => !o)} aria-expanded={open} data-testid="s5e4-help-toggle">
        <Icon name="settings" /> Trợ giúp của AI
      </button>
      {open && view && (
        <fieldset className="s5e4-help-box" data-testid="s5e4-help-box">
          <legend>Trợ giúp của AI</legend>
          <label>
            Mức trợ giúp{' '}
            <select value={level} onChange={(e) => setLevel(e.target.value as HelpLevel)} data-testid="s5e4-help-select">
              {HELP_LEVELS.map((h) => (
                <option key={h.value} value={h.value}>
                  {h.label}
                </option>
              ))}
            </select>
          </label>
          <button type="button" className="btn primary" disabled={busy} onClick={() => void save()} data-testid="s5e4-help-save">
            <Icon name="save" /> Lưu
          </button>
        </fieldset>
      )}
      {error !== null && <ErrorNotice error={error} />}
    </div>
  );
}
