import { useState } from 'react';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { approveDraft, createDrafts, type DraftBatch } from './draftApi.ts';
import { approvable, countProblem, initialSelection } from './draftLogic.ts';
import { MathText } from '../../../shared/math/MathText.tsx';

/**
 * Trợ lý soạn đề nháp từ một tài liệu Đang dùng (US-S5E2-06, C5-08): xin N đề, xem nhãn hai trục (máy "Đã kiểm chứng" /
 * "Cần xem lại", người "AI soạn"), nguồn "TL-xx vN", đáp số do engine; đề đạt kiểm chứng chọn sẵn; Duyệt đưa đề đã chọn
 * vào ngân hàng "Của tôi". Thành phần gắn vào màn Kho tri thức / Ra đề khi chủ màn cho phép (T-261128-63 không sửa index).
 */
export function ExamDraftPanel({ docId, classId }: { docId: string; classId: string }) {
  const [count, setCount] = useState(5);
  const [batch, setBatch] = useState<DraftBatch | null>(null);
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const countError = countProblem(count);

  const run = async () => {
    setBusy(true);
    setError(null);
    try {
      const b = await createDrafts(docId, classId, count);
      setBatch(b);
      setPicked(initialSelection(b.items));
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const approveSelected = async () => {
    if (!batch) return;
    setBusy(true);
    setError(null);
    try {
      const items = [...batch.items];
      for (let i = 0; i < items.length; i++) {
        const it = items[i]!;
        if (picked.has(it.id) && approvable(it)) items[i] = await approveDraft(it.id);
      }
      setBatch({ ...batch, items });
      setPicked(new Set());
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const toggle = (id: string) => {
    const next = new Set(picked);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setPicked(next);
  };

  return (
    <section className="card s5e2-draft" data-testid="s5e2-draft">
      <h2>Trợ lý soạn đề nháp</h2>
      <label>
        Số đề{' '}
        <input
          type="number"
          min={1}
          max={10}
          value={count}
          onChange={(e) => setCount(Number(e.target.value))}
          data-testid="s5e2-draft-count"
        />
      </label>
      {countError && <p role="alert">{countError}</p>}
      <button
        type="button"
        disabled={busy || countError !== null}
        onClick={() => void run()}
        data-testid="s5e2-draft-run"
      >
        Soạn đề
      </button>
      {batch && (
        <>
          <p className="s5e2-muted">Nguồn: {batch.source.label}</p>
          {batch.message && <p role="status">{batch.message}</p>}
          <ol className="s5e2-draft-list">
            {batch.items.map((it) => (
              <li key={it.id} data-testid={`s5e2-draft-item-${it.ord}`} data-state={it.machineState}>
                <label>
                  <input
                    type="checkbox"
                    checked={picked.has(it.id)}
                    disabled={!approvable(it)}
                    onChange={() => toggle(it.id)}
                  />{' '}
                  <MathText text={it.statement} />
                </label>
                <div className="s5e2-muted">
                  <span className="chip">{it.machineLabel}</span> <span className="chip">{it.humanLabel}</span> ·{' '}
                  {it.source}
                </div>
                <div>
                  Đáp số (máy tính): <strong>{it.answer ?? '—'}</strong>
                  {it.reviewReason && <em> · {it.reviewReason}</em>}
                </div>
              </li>
            ))}
          </ol>
          <button
            type="button"
            disabled={busy || picked.size === 0}
            onClick={() => void approveSelected()}
            data-testid="s5e2-draft-approve"
          >
            Duyệt đề đã chọn
          </button>
        </>
      )}
      {error !== null && <ErrorNotice error={error} />}
    </section>
  );
}
