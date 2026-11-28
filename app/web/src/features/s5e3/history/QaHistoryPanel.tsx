import { useEffect, useState } from 'react';
import { formatVnDateTime } from '../../../shared/time/vnTime.ts';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { qaFeedback, qaHistory, qaReport, type FeedbackType, type QaReport, type QaRow } from './historyApi.ts';
import { DECISION_LABEL, FEEDBACK_LABEL, noteProblem, percentText } from './historyLogic.ts';
import { MathText } from '../../../shared/math/MathText.tsx';

/**
 * Lịch sử hỏi đáp của một bài cho giáo viên (US-S5E3-06): lọc theo quyết định, xem câu hỏi / câu trả lời đã hiện / nguồn /
 * luật, đánh dấu "chặn sai" hoặc "lẽ ra phải chặn" kèm ghi chú; báo cáo tỉ lệ theo loại nguồn. Không xếp hạng học sinh
 * (PO-04). Thành phần gắn vào màn bài có chấm khi chủ màn cho phép (T-261128-63 không sửa index).
 */
export function QaHistoryPanel({ assignmentId }: { assignmentId: string }) {
  const [decision, setDecision] = useState('');
  const [rows, setRows] = useState<QaRow[]>([]);
  const [cursor, setCursor] = useState<string | null>(null);
  const [report, setReport] = useState<QaReport | null>(null);
  const [notes, setNotes] = useState<Record<string, string>>({});
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);

  const more = async (next: string) => {
    try {
      const p = await qaHistory(assignmentId, decision || undefined, next);
      setRows((prev) => [...prev, ...p.items]);
      setCursor(p.nextCursor);
    } catch (e) {
      setError(e);
    }
  };

  useEffect(() => {
    let live = true;
    qaHistory(assignmentId, decision || undefined)
      .then((p) => {
        if (!live) return;
        setRows(p.items);
        setCursor(p.nextCursor);
      })
      .catch(setError);
    qaReport(assignmentId)
      .then((r) => live && setReport(r))
      .catch(setError);
    return () => {
      live = false;
    };
  }, [assignmentId, decision]);

  const mark = async (row: QaRow, type: FeedbackType) => {
    const note = notes[row.id] ?? '';
    if (noteProblem(note)) return;
    try {
      const r = await qaFeedback(row.id, type, note);
      setToast(r.message);
      setRows((prev) =>
        prev.map((x) =>
          x.id === row.id
            ? {
                ...x,
                feedback: { type, typeLabel: FEEDBACK_LABEL[type], note },
              }
            : x,
        ),
      );
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="card s5e3-history" data-testid="s5e3-history">
      <h2>Lịch sử hỏi đáp</h2>
      {report && (
        <p className="s5e3-muted" data-testid="s5e3-history-report">
          n = {report.n} · {report.teacherDocs.label} {percentText(report.teacherDocs.percent)} ·{' '}
          {report.lectures.label} {percentText(report.lectures.percent)} · {report.noSource.label}{' '}
          {percentText(report.noSource.percent)} · {report.blocked.label} {percentText(report.blocked.percent)}
        </p>
      )}
      <label>
        Quyết định{' '}
        <select value={decision} onChange={(e) => setDecision(e.target.value)} data-testid="s5e3-history-filter">
          <option value="">Tất cả</option>
          {Object.entries(DECISION_LABEL).map(([k, v]) => (
            <option key={k} value={k}>
              {v}
            </option>
          ))}
        </select>
      </label>
      <ul className="s5e3-history-list">
        {rows.map((r) => {
          const note = notes[r.id] ?? '';
          const bad = noteProblem(note);
          return (
            <li key={r.id} data-testid="s5e3-history-row">
              <div className="s5e3-muted">
                {formatVnDateTime(r.at)} · {r.studentName}
              </div>
              <div>
                <strong>Hỏi:</strong> <MathText text={r.question} />
              </div>
              <div>
                <strong>Đã hiện:</strong> <MathText text={r.reply} />
              </div>
              <div className="s5e3-muted">
                {r.source ? `${r.source.typeLabel} · ${r.source.label}` : 'Không nguồn'} ·{' '}
                {DECISION_LABEL[r.decision ?? ''] ?? '—'}
                {r.rule ? ` · ${r.rule}` : ''}
                {r.feedback ? ` · Đã báo: ${r.feedback.typeLabel}` : ''}
              </div>
              <input
                value={note}
                placeholder="Ghi chú (≤ 200 ký tự)"
                onChange={(e) => setNotes({ ...notes, [r.id]: e.target.value })}
              />
              {bad && <span role="alert">{bad}</span>}
              <button type="button" onClick={() => void mark(r, 'false_block')}>
                {FEEDBACK_LABEL.false_block}
              </button>
              <button type="button" onClick={() => void mark(r, 'should_block')}>
                {FEEDBACK_LABEL.should_block}
              </button>
            </li>
          );
        })}
      </ul>
      {cursor && (
        <button type="button" onClick={() => void more(cursor)}>
          Xem thêm
        </button>
      )}
      {toast && <p role="status">{toast}</p>}
      {error !== null && <ErrorNotice error={error} />}
    </section>
  );
}
