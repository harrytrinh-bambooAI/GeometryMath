import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { libraryApi, type LibraryDetail } from './api.ts';
import { CardLabels } from './CardLabels.tsx';
import { useFetch } from './useFetch.ts';
import './s2e6.css';

/**
 * Đề Kho chung (giáo viên): đề bài, gợi ý và lời giải xem ngay kèm nhãn hai trục (BR-S2E6-14, PO-63); "Sao chép về
 * Của tôi" (tính vào giới hạn 20 đề / 60 phút, BR-S2E6-13).
 */
export function SharedPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [fetched, reload] = useFetch<LibraryDetail>(`shared:${id}`, (signal) => libraryApi.shared(id, signal));
  const [showSolution, setShowSolution] = useState(false);
  const [copyError, setCopyError] = useState<unknown>(null);

  if (fetched.state === 'error') return <ErrorNotice error={fetched.error} onRetry={reload} testId="s2e6-shared-error" />;
  if (fetched.state === 'loading') return <p className="muted">Đang tải đề…</p>;
  const item = fetched.data;

  const copy = async () => {
    setCopyError(null);
    try {
      const c = await libraryApi.copy(item.id);
      navigate(`/gv/kho-de-cua-toi/de/${c.id}`);
    } catch (e) {
      setCopyError(e);
    }
  };

  return (
    <div className="s2e6-page" data-testid="s2e6-shared">
      <div className="s2e6-head">
        <Link className="btn ghost" to="/gv/kho-de-cua-toi">
          <Icon name="prev" />
          Kho đề
        </Link>
        <h1 className="s2e6-title">{item.title}</h1>
      </div>
      <div className="card s2e6-detail">
        <CardLabels card={item} testPrefix="s2e6-shared" />
        <p className="s2e6-statement" data-math>
          {item.statement}
        </p>
        <p className="s2e6-help">
          <Icon name="hint" />
          Gợi ý: {item.hint}
        </p>
        <div className="s2e6-row">
          <button type="button" className="btn" data-testid="s2e6-shared-solution" onClick={() => setShowSolution((s) => !s)}>
            <Icon name="eye" />
            {showSolution ? 'Ẩn lời giải' : 'Xem lời giải'}
          </button>
          <button type="button" className="btn ghost" data-testid="s2e6-shared-copy" onClick={copy}>
            <Icon name="copy" />
            Sao chép về Của tôi
          </button>
        </div>
        {copyError ? <ErrorNotice error={copyError} testId="s2e6-copy-error" /> : null}
        {showSolution ? (
          <ol className="s2e6-steps" data-testid="s2e6-shared-steps">
            {item.steps.map((s, i) => (
              <li key={i} data-math>
                {s}
              </li>
            ))}
          </ol>
        ) : null}
      </div>
    </div>
  );
}
