import { useEffect, useState } from 'react';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { mine, myGraded, pageUrl, result, type Result } from '../review/api.ts';
import { vn } from '../review/logic.ts';
import '../review/review.css';

/**
 * S5-E4 · kết quả bài có chấm của học sinh C5-06 (US-S5E4-07). T-261128-60. Trước khi trả: chỉ trạng thái; sau khi
 * trả: điểm từng tiêu chí, tổng, nhận xét, ảnh bài của em và lời giải. Không có gì của AI, không điểm của bạn.
 */
export function ResultPage() {
  const [items, setItems] = useState<Array<{ id: string; title: string; result: Result | null }>>([]);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    (async () => {
      const g = await myGraded();
      const out: Array<{ id: string; title: string; result: Result | null }> = [];
      for (const a of g.items) {
        const m = await mine(a.id);
        out.push({ id: a.id, title: a.title, result: m.submission ? await result(m.submission.id) : null });
      }
      setItems(out);
    })().catch(setError);
  }, []);

  return (
    <section className="s5e4-page" data-testid="s5e4-result">
      <h1 className="s5e4-title">Kết quả bài có chấm</h1>
      {error ? <ErrorNotice error={error} testId="s5e4-result-error" /> : null}
      {items.length === 0 && !error && <p className="s5e4-muted">Chưa có bài có chấm.</p>}
      {items.map((a) => (
        <article key={a.id} className="card s5e4-card" data-testid={`s5e4-result-${a.id}`}>
          <h2 className="s5e4-title" style={{ fontSize: 18 }}>
            {a.title}
          </h2>
          {!a.result && <span className="s5e4-muted">Em chưa nộp bài này.</span>}
          {a.result && <ResultBody r={a.result} />}
        </article>
      ))}
    </section>
  );
}

function ResultBody({ r }: { r: Result }) {
  const [urls, setUrls] = useState<string[]>([]);
  useEffect(() => {
    if (r.status !== 'returned' || !r.versionId || !r.pages) return;
    const vid = r.versionId;
    (async () => {
      const out: string[] = [];
      for (let n = 1; n <= (r.pages ?? 0); n++) out.push((await pageUrl(vid, n)).url);
      setUrls(out);
    })().catch(() => setUrls([]));
  }, [r]);

  if (r.status !== 'returned') {
    return (
      <span className="s5e4-muted" data-testid="s5e4-result-status">
        {r.statusLabel} · {r.message}
      </span>
    );
  }
  return (
    <>
      <table className="s5e4-table" data-testid="s5e4-result-criteria">
        <tbody>
          {r.criteria?.map((c) => (
            <tr key={c.ord}>
              <td>{c.name}</td>
              <td>
                {vn(c.points)} / {vn(c.maxPoints)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="s5e4-total" data-testid="s5e4-result-score">
        Tổng: {vn(r.score ?? 0)} / {vn(r.scale ?? 10)}
      </p>
      {r.comment && <p data-testid="s5e4-result-comment">Nhận xét: {r.comment}</p>}
      <div className="s5e4-pages">
        {urls.map((u, k) => (
          <img key={u} src={u} alt={`Trang ${k + 1}`} />
        ))}
      </div>
      {r.solution?.status === 'OK' && (
        <div data-testid="s5e4-result-solution">
          <strong>Lời giải</strong>
          <ol>
            {r.solution.steps?.map((s, k) => (
              <li key={k}>{s.body ?? s.title}</li>
            ))}
          </ol>
          <p>Đáp số: {r.solution.finalAnswer}</p>
        </div>
      )}
    </>
  );
}
