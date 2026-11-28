import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import {
  createPracticeSet,
  errorSummary,
  suggestions as loadSuggestions,
  type ErrorSummary,
  type Suggestions,
} from './insightsApi.ts';
import { labelName, lostText, points, practiceTitle, toggle } from './insightsLogic.ts';

/**
 * /gv/bai-co-cham/:id/loi-cua-lop — thẻ "Cả lớp hay sai ở đâu" (US-S5E5-02): số liệu thô theo tiêu chí / loại lỗi,
 * nhận định ẩn danh với căn cứ "Bài k" (bấm mở màn chấm bài đó); không cột xếp hạng, không phần trăm. Mỗi nhận định có
 * "Nên nhắc lại trên lớp" (US-S5E5-03): gợi ý tất định, giáo viên tick rồi "Tạo bộ luyện" (bộ S3-E2 Nháp, chưa giao)
 * hoặc "Mở trình chiếu". T-261128-64.
 */
export function ClassErrorsPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState<ErrorSummary | null>(null);
  const [open, setOpen] = useState<number | null>(null);
  const [sug, setSug] = useState<Suggestions | null>(null);
  const [picked, setPicked] = useState<string[]>([]);
  const [created, setCreated] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let live = true;
    errorSummary(id)
      .then((d) => live && setData(d))
      .catch(setError);
    return () => {
      live = false;
    };
  }, [id]);

  const remind = async (n: number) => {
    setOpen(n);
    setSug(null);
    setPicked([]);
    setCreated(null);
    try {
      setSug(await loadSuggestions(id, n));
    } catch (e) {
      setError(e);
    }
  };

  const createSet = async () => {
    if (!data || picked.length === 0) return;
    try {
      const s = await createPracticeSet(practiceTitle(data.code, data.title), picked);
      setCreated(s.id);
    } catch (e) {
      setError(e);
    }
  };

  return (
    <section className="card s5e5-errors" data-testid="s5e5-errors">
      <h2>Cả lớp hay sai ở đâu</h2>
      {data && (
        <p className="s5e5-muted">
          {data.code} · {data.title} · {data.approved} bài đã duyệt
        </p>
      )}
      {data?.message && (
        <p role="status" data-testid="s5e5-errors-message">
          {data.message}
        </p>
      )}
      {data?.raw && (
        <>
          <table className="s5e5-errors-table" data-testid="s5e5-errors-raw">
            <thead>
              <tr>
                <th>Tiêu chí</th>
                <th>Số bài mất điểm</th>
                <th>Điểm trung bình</th>
              </tr>
            </thead>
            <tbody>
              {data.raw.criteria.map((c) => (
                <tr key={c.ord}>
                  <td>{c.name}</td>
                  <td>{lostText(c.lost, c.works)}</td>
                  <td>
                    {points(c.avgPoints)}/{points(c.maxPoints)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="s5e5-muted" data-testid="s5e5-errors-labels">
            {data.raw.labels
              .filter((l) => l.works > 0)
              .map((l) => `${labelName(l.label)}: ${l.works} bài`)
              .join(' · ') || 'Chưa có loại lỗi từ đề xuất AI đã duyệt.'}
          </p>
        </>
      )}
      {data && data.insights.length > 0 && (
        <ol className="s5e5-errors-insights" data-testid="s5e5-errors-insights">
          {data.insights.map((i) => (
            <li key={i.n} data-testid={`s5e5-insight-${i.n}`}>
              <p>{i.text}</p>
              <p className="s5e5-muted">
                {i.criterion ? `Tiêu chí ${i.criterion}` : ''}
                {i.label ? ` · ${labelName(i.label)}` : ''} · AI soạn, giáo viên xem lại
              </p>
              <div className="s5e5-errors-evidence">
                Căn cứ:{' '}
                {i.evidence.map((e) => (
                  <button
                    key={e.work}
                    type="button"
                    data-testid={`s5e5-evidence-${i.n}-${e.work}`}
                    onClick={() => void navigate(`/gv/cham/${e.submissionId}`)}
                  >
                    {e.label}
                  </button>
                ))}
              </div>
              <button type="button" data-testid={`s5e5-remind-${i.n}`} onClick={() => void remind(i.n)}>
                Nên nhắc lại trên lớp
              </button>
            </li>
          ))}
        </ol>
      )}
      {open !== null && sug && (
        <div className="card s5e5-remind" data-testid="s5e5-remind">
          <h3>Nên nhắc lại trên lớp</h3>
          {sug.message && <p className="s5e5-muted">{sug.message}</p>}
          <ul>
            {sug.items.map((s) => (
              <li key={s.problemVersionId}>
                <label>
                  <input
                    type="checkbox"
                    data-testid={`s5e5-pick-${s.code}`}
                    checked={picked.includes(s.problemVersionId)}
                    onChange={() => setPicked(toggle(picked, s.problemVersionId))}
                  />{' '}
                  {s.code} · {s.statement}
                  {s.distractorMatch && s.distractor ? ` (nhiễu: ${s.distractor})` : ''}
                </label>
              </li>
            ))}
          </ul>
          {sug.docs.length > 0 && (
            <ul data-testid="s5e5-remind-docs">
              {sug.docs.map((d) => (
                <li key={d.id}>
                  Tài liệu {d.code} · {d.title} (v{d.version})
                </li>
              ))}
            </ul>
          )}
          <button type="button" data-testid="s5e5-create-set" disabled={picked.length === 0} onClick={() => void createSet()}>
            Tạo bộ luyện
          </button>{' '}
          <Link to="/gv/chua-bai" data-testid="s5e5-present">
            Mở trình chiếu
          </Link>
          {created && (
            <p role="status" data-testid="s5e5-created">
              Đã tạo bộ luyện nháp ({picked.length} câu), chưa giao lớp nào.{' '}
              <Link to={`/gv/bo-bai/${created}`}>Mở bộ luyện</Link>
            </p>
          )}
        </div>
      )}
      {error !== null && <ErrorNotice error={error} />}
    </section>
  );
}
