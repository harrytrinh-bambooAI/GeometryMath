import { useEffect, useState } from 'react';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { formatVnDateTime } from '../../../shared/time/vnTime.ts';
import { listRubrics, type RubricLine } from '../rubric/api.ts';
import { createGraded, listGraded, myClasses, problems, type Graded } from './api.ts';

/**
 * C5-01 "Bài có chấm" (US-S5E4-02): giao bài với đề kho, rubric phiên bản hiện hành, hạn nộp, nộp muộn, chế độ chấm
 * (Chấm tay mặc định; Chạy song song / AI đề xuất máy chủ kiểm cổng), công bố khi giáo viên bấm Trả bài.
 */
export function GradedPage() {
  const [items, setItems] = useState<Graded[]>([]);
  const [classes, setClasses] = useState<Array<{ id: string; name: string }>>([]);
  const [rubrics, setRubrics] = useState<RubricLine[]>([]);
  const [probs, setProbs] = useState<Array<{ code: string; problemVersionId: string }>>([]);
  const [form, setForm] = useState({ title: '', classId: '', problemVersionId: '', rubricId: '', due: '', allowLate: true, gradingMode: 'manual' });
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);

  const reload = () => listGraded().then((r) => setItems(r.items)).catch(setError);

  useEffect(() => {
    reload();
    myClasses().then((r) => setClasses(r.items)).catch(setError);
    listRubrics().then((r) => setRubrics(r.items)).catch(setError);
    problems().then((r) => setProbs(r.items)).catch(setError);
  }, []);

  async function submit() {
    setBusy(true);
    setError(null);
    try {
      await createGraded({
        title: form.title,
        classId: form.classId,
        problemVersionId: form.problemVersionId,
        rubricId: form.rubricId,
        dueAt: new Date(`${form.due}:00+07:00`).toISOString().replace('.000Z', 'Z'),
        allowLate: form.allowLate,
        gradingMode: form.gradingMode,
        releaseMode: 'on_return',
      });
      await reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  const set = (k: string, v: string | boolean) => setForm({ ...form, [k]: v });
  const ready = form.title && form.classId && form.problemVersionId && form.rubricId && form.due;

  return (
    <div className="s5e4-page" data-testid="s5e4-graded-page">
      <h1 className="s5e4-title">Bài có chấm</h1>
      {error ? <ErrorNotice error={error} testId="s5e4-error" /> : null}
      <div className="card s5e4-card">
        <div className="s5e4-row">
          <label className="s5e4-field s5e4-grow">
            <span>Tên bài</span>
            <input value={form.title} maxLength={120} onChange={(e) => set('title', e.target.value)} data-testid="s5e4-ga-title" />
          </label>
          <label className="s5e4-field">
            <span>Lớp</span>
            <select value={form.classId} onChange={(e) => set('classId', e.target.value)} data-testid="s5e4-ga-class">
              <option value="">Chọn lớp</option>
              {classes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="s5e4-row">
          <label className="s5e4-field">
            <span>Đề</span>
            <select value={form.problemVersionId} onChange={(e) => set('problemVersionId', e.target.value)} data-testid="s5e4-ga-problem">
              <option value="">Chọn đề</option>
              {probs.map((p) => (
                <option key={p.problemVersionId} value={p.problemVersionId}>
                  {p.code}
                </option>
              ))}
            </select>
          </label>
          <label className="s5e4-field">
            <span>Rubric</span>
            <select value={form.rubricId} onChange={(e) => set('rubricId', e.target.value)} data-testid="s5e4-ga-rubric">
              <option value="">Chọn rubric</option>
              {rubrics.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.code} · {r.name} · v{r.version}
                </option>
              ))}
            </select>
          </label>
          <label className="s5e4-field">
            <span>Hạn nộp</span>
            <input type="datetime-local" value={form.due} onChange={(e) => set('due', e.target.value)} data-testid="s5e4-ga-due" />
          </label>
          <label className="s5e4-field">
            <span>Chế độ chấm</span>
            <select value={form.gradingMode} onChange={(e) => set('gradingMode', e.target.value)} data-testid="s5e4-ga-mode">
              <option value="manual">Chấm tay</option>
              <option value="parallel">Chạy song song</option>
              <option value="ai_proposal">AI đề xuất</option>
            </select>
          </label>
          <label className="s5e4-row">
            <input type="checkbox" checked={form.allowLate} onChange={(e) => set('allowLate', e.target.checked)} />
            <span>Cho nộp muộn (có nhãn)</span>
          </label>
        </div>
        <span className="s5e4-muted">Điểm và lời giải công bố khi thầy cô bấm Trả bài.</span>
        <div className="s5e4-row">
          <button type="button" className="btn primary" disabled={!ready || busy} onClick={submit} data-testid="s5e4-ga-submit">
            Giao bài
          </button>
        </div>
      </div>
      <table className="s5e4-table card" data-testid="s5e4-ga-list">
        <thead>
          <tr>
            <th>Mã</th>
            <th>Bài</th>
            <th>Lớp</th>
            <th>Rubric</th>
            <th>Hạn nộp</th>
            <th>Chế độ</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          {items.map((g) => (
            <tr key={g.id}>
              <td>{g.code}</td>
              <td>{g.title}</td>
              <td>{g.className}</td>
              <td>
                {g.rubric.code} v{g.rubric.version}
              </td>
              <td>{formatVnDateTime(g.dueAt)}</td>
              <td>{g.gradingModeLabel}</td>
              <td>{g.statusLabel}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
