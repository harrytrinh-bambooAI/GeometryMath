import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { createRubric, getRubric, listRubrics, saveRubric, type Criterion, type Rubric, type RubricLine } from './api.ts';
import { MSG_30, canSave, fmt, total, totalLabel } from './logic.ts';

/**
 * C5-02 "Rubric" (US-S5E4-01): danh sách rubric, tạo từ mẫu Tính toán / Chứng minh, màn soạn có tổng điểm sống; lệch thang
 * thì dòng tổng màu cảnh báo, nút Lưu mờ, MSG-S5-30. Rubric đã dùng cho bài đã giao: Lưu tạo phiên bản mới (MSG-S5-31).
 * data-testid bắt đầu bằng 's5e4-'.
 */
export function RubricPage() {
  const [params, setParams] = useSearchParams();
  const id = params.get('id') ?? '';
  const [lines, setLines] = useState<RubricLine[]>([]);
  const [rubric, setRubric] = useState<Rubric | null>(null);
  const [name, setName] = useState('');
  const [rows, setRows] = useState<Criterion[]>([]);
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    listRubrics()
      .then((r) => setLines(r.items))
      .catch(setError);
  }, [rubric]);

  useEffect(() => {
    if (!id) return;
    getRubric(id)
      .then(load)
      .catch(setError);
  }, [id]);

  function load(r: Rubric) {
    setRubric(r);
    setName(r.name);
    setRows(r.criteria.map((c) => ({ ...c })));
  }

  async function fromTemplate(code: string) {
    setBusy(true);
    setError(null);
    try {
      const r = await createRubric({ fromTemplate: code, name: code === 'tinh-toan' ? 'Tính toán' : 'Chứng minh' });
      setParams({ id: r.id });
      load(r);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  async function save() {
    if (!rubric) return;
    setBusy(true);
    setError(null);
    try {
      const r = await saveRubric(rubric.id, { name, criteria: rows });
      load(r);
      setToast(r.message ?? 'Đã lưu rubric.');
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  const points = rows.map((r) => r.maxPoints);
  const scale = rubric?.scale ?? 10;
  const ok = canSave(points, scale) && name.trim().length > 0;
  const off = total(points) !== scale;

  return (
    <div className="s5e4-page" data-testid="s5e4-rubric-page">
      <h1 className="s5e4-title">Rubric</h1>
      {error ? <ErrorNotice error={error} testId="s5e4-error" /> : null}
      <div className="card s5e4-card">
        <div className="s5e4-row">
          <button type="button" className="btn" disabled={busy} onClick={() => fromTemplate('tinh-toan')} data-testid="s5e4-new-tinh-toan">
            Tạo từ mẫu Tính toán
          </button>
          <button type="button" className="btn" disabled={busy} onClick={() => fromTemplate('chung-minh')} data-testid="s5e4-new-chung-minh">
            Tạo từ mẫu Chứng minh
          </button>
        </div>
        <ul className="s5e4-list" data-testid="s5e4-rubric-list">
          {lines.map((l) => (
            <li key={l.id}>
              <button type="button" className="btn ghost" onClick={() => setParams({ id: l.id })} data-testid={`s5e4-rubric-${l.code}`}>
                {l.code} · {l.name} · v{l.version}
              </button>
            </li>
          ))}
        </ul>
      </div>
      {rubric && rubric.id === id ? (
        <div className="card s5e4-card" data-testid="s5e4-editor">
          <div className="s5e4-row">
            <label className="s5e4-field s5e4-grow">
              <span>Tên rubric</span>
              <input value={name} onChange={(e) => setName(e.target.value)} maxLength={120} data-testid="s5e4-name" />
            </label>
            <span className="s5e4-muted" data-testid="s5e4-version">
              {rubric.code} · phiên bản {rubric.version}
              {rubric.used ? ' · đã dùng cho bài đã giao' : ''}
            </span>
          </div>
          <table className="s5e4-table">
            <thead>
              <tr>
                <th>Tiêu chí</th>
                <th>Mô tả mức đạt</th>
                <th>Điểm tối đa</th>
                <th>Phụ thuộc hướng</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r, i) => (
                <tr key={i} data-testid={`s5e4-row-${i}`}>
                  <td>
                    <input
                      value={r.name}
                      maxLength={60}
                      onChange={(e) => setRows(rows.map((x, j) => (j === i ? { ...x, name: e.target.value } : x)))}
                      data-testid={`s5e4-crit-name-${i}`}
                    />
                  </td>
                  <td>
                    <input
                      value={r.description}
                      maxLength={500}
                      onChange={(e) => setRows(rows.map((x, j) => (j === i ? { ...x, description: e.target.value } : x)))}
                    />
                  </td>
                  <td>
                    <input
                      type="number"
                      step={0.25}
                      min={0.25}
                      value={Number.isFinite(r.maxPoints) ? r.maxPoints : ''}
                      onChange={(e) => setRows(rows.map((x, j) => (j === i ? { ...x, maxPoints: e.target.valueAsNumber } : x)))}
                      aria-label={`Điểm ${r.name}`}
                      data-testid={`s5e4-crit-points-${i}`}
                    />
                  </td>
                  <td>
                    <input
                      type="checkbox"
                      checked={r.directionDependent}
                      onChange={(e) => setRows(rows.map((x, j) => (j === i ? { ...x, directionDependent: e.target.checked } : x)))}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="s5e4-row">
            <strong className={off ? 's5e4-total s5e4-warn' : 's5e4-total'} data-testid="s5e4-total" data-warn={off ? 'true' : 'false'}>
              Tổng {totalLabel(points, scale)}
            </strong>
            {off ? (
              <span className="s5e4-error" role="alert" data-testid="s5e4-msg30">
                {MSG_30}
              </span>
            ) : null}
            <span className="s5e4-grow" />
            <button type="button" className="btn primary" disabled={!ok || busy} onClick={save} data-testid="s5e4-save">
              Lưu
            </button>
          </div>
          {toast ? (
            <div className="s5e4-toast" role="status" data-testid="s5e4-toast">
              {toast}
            </div>
          ) : null}
          <details>
            <summary>Hướng dẫn chấm (luật chặn)</summary>
            <ol data-testid="s5e4-guide">
              {rubric.guide.map((g) => (
                <li key={g}>{g}</li>
              ))}
            </ol>
          </details>
          <span className="s5e4-muted">Thang {fmt(scale)} điểm, mỗi tiêu chí bước 0,25.</span>
        </div>
      ) : null}
    </div>
  );
}
