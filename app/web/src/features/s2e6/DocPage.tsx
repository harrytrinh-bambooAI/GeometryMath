import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { requestPresentFullscreen } from '../s2e7/fullscreen.ts';
import { libraryApi, type DocDetail } from './api.ts';
import { CardLabels } from './CardLabels.tsx';
import { useFetch } from './useFetch.ts';
import './s2e6.css';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Một đề "Của tôi" (C2-01 › đề): nhãn, trạng thái kho, lịch sử gửi soát (BR-S2E6-06), "Đề xuất vào Kho chung"
 * (khóa kèm "Hãy duyệt lời giải trước" khi trục người chưa "Giáo viên đã duyệt", BR-S2E6-05), lối vào C2-03 và
 * "Trình chiếu" (S2-E7). Người soát mở được đề đang Chờ soát (chỉ xem).
 */
export function DocPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [fetched, reload] = useFetch<DocDetail>(`doc:${id}`, (signal) => libraryApi.doc(id, signal));
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState<unknown>(null);

  if (fetched.state === 'error') return <ErrorNotice error={fetched.error} onRetry={reload} testId="s2e6-doc-error" />;
  if (fetched.state === 'loading') return <p className="muted" data-testid="s2e6-doc-loading">Đang tải đề…</p>;
  const doc = fetched.data;

  const submit = async () => {
    setBusy(true);
    setActionError(null);
    try {
      await libraryApi.submit(doc.id);
      reload();
    } catch (e) {
      setActionError(e);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="s2e6-page" data-testid="s2e6-doc" data-status={doc.status}>
      <div className="s2e6-head">
        <Link className="btn ghost" to="/gv/kho-de-cua-toi" data-testid="s2e6-back">
          <Icon name="prev" />
          Kho đề
        </Link>
        <h1 className="s2e6-title">{doc.title}</h1>
      </div>
      <div className="card s2e6-detail">
        <CardLabels card={doc} testPrefix="s2e6-doc" />
        <p className="s2e6-statement" data-math data-testid="s2e6-doc-statement">
          <MathText text={doc.statement} />
        </p>
        {doc.reason ? (
          <p className="s2e6-reason" data-testid="s2e6-doc-reason">
            <Icon name="info" />
            Lý do từ chối: {doc.reason}
          </p>
        ) : null}
        {doc.owner ? (
          <div className="s2e6-row">
            <button
              type="button"
              className="btn"
              data-testid="s2e6-submit"
              disabled={!doc.canSubmit || busy}
              aria-describedby={doc.submitBlockedReason ? 's2e6-submit-why' : undefined}
              onClick={submit}
            >
              <Icon name="send" />
              Đề xuất vào Kho chung
            </button>
            {doc.submitBlockedReason ? (
              <span id="s2e6-submit-why" className="s2e6-help" data-testid="s2e6-submit-why">
                {doc.submitBlockedReason}
              </span>
            ) : null}
            <Link className="btn ghost" to={`/gv/soat-loi-giai/${doc.id}`} data-testid="s2e6-open-review">
              <Icon name="pencil" />
              Soát lời giải
            </Link>
            <button
              type="button"
              className="btn ghost"
              data-testid="s2e6-present"
              onClick={() => {
                requestPresentFullscreen(); // xin toàn màn hình ngay trong thao tác bấm (BR-S2E7-01)
                navigate(`/gv/trinh-chieu/${doc.id}`);
              }}
            >
              <Icon name="eye" />
              Trình chiếu
            </button>
          </div>
        ) : null}
        {actionError ? <ErrorNotice error={actionError} testId="s2e6-submit-error" /> : null}
      </div>
      {doc.owner && doc.history.length > 0 ? (
        <div className="card s2e6-detail">
          <h2 className="s2e6-sub">Lịch sử gửi soát</h2>
          <ul className="s2e6-history" data-testid="s2e6-history">
            {doc.history.map((h, i) => (
              <li key={i} data-testid={`s2e6-history-${h.status}`}>
                <b>{h.statusLabel}</b> · {formatVnDateTime(h.at)}
                {h.reason ? ` · ${h.reason}` : ''}
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </div>
  );
}
