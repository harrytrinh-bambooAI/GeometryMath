import { Link, useSearchParams } from 'react-router';
import { formatVnTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { resultsApi, useLoad, type Evidence } from './api.ts';
import { EvidenceBody } from './EvidencePanel.tsx';
import { columnLabel, MSG, STATUS_ICON, STATUS_LABEL } from './format.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * "Mở bài làm" (BR-S3E3-24): bài của một em ở chế độ CHỈ ĐỌC — đề bài và các tương tác đã có; không ô nhập đáp số,
 * không nút xin gợi ý hay hỏi, chỉ gọi API đọc nên không sinh tương tác và không gửi thông báo nào cho em.
 */
export function WorkPage() {
  const [params] = useSearchParams();
  const classId = params.get('lop') ?? '';
  const setId = params.get('bo') ?? '';
  const item = Number(params.get('de') ?? '0');
  const userId = params.get('hs') ?? ''; // PO-74 (BR-S3E3-40): userId UUID, không mã người dùng
  const [ev, retry] = useLoad<Evidence>(`s3e3-work-${setId}-${item}-${userId}`, (s) =>
    resultsApi.evidence(classId, setId, item, userId, true, s),
  );
  const back = `/gv/ket-qua-lop?lop=${encodeURIComponent(classId)}&bo=${encodeURIComponent(setId)}`;
  return (
    <div className="s3e3-page" data-testid="s3e3-work">
      <div className="s3e3-head">
        <Link className="btn ghost sm" to={back} data-testid="s3e3-work-back">
          <Icon name="prev" />
          Kết quả lớp
        </Link>
        <span className="chip" data-testid="s3e3-readonly">
          <Icon name="lock" inline />
          Chỉ đọc
        </span>
      </div>
      {ev.state === 'loading' && <p className="muted">Đang mở bài làm…</p>}
      {ev.state === 'error' && (
        <div className="alert" role="alert" data-testid="s3e3-work-error">
          Chưa mở được bài làm.{' '}
          <button className="btn ghost sm" onClick={retry}>
            Thử lại
          </button>
        </div>
      )}
      {ev.state === 'ok' && (
        <section className="card s3e3-card">
          <h1 className="s3e3-title" data-testid="s3e3-work-title">
            Bài làm của {ev.data.displayName} · {columnLabel(ev.data.item)}
          </h1>
          <div className="s3e3-head">
            <span className="mono">{ev.data.userCode}</span>
            {ev.data.left && (
              <span className="chip" data-testid="s3e3-work-left">
                đã rời
              </span>
            )}
            <span className={`s3e3-cell ${ev.data.status} s3e3-key`} data-testid="s3e3-work-status">
              <Icon name={STATUS_ICON[ev.data.status]} inline />
              {STATUS_LABEL[ev.data.status]}
            </span>
            <span className="s3e3-grow" />
            <span className="muted">Cập nhật lúc {formatVnTime(ev.data.updatedAt)}</span>
          </div>
          {ev.data.statement && (
            <p className="s3e3-statement" data-math data-testid="s3e3-work-statement">
              <MathText text={ev.data.statement} />
            </p>
          )}
          {ev.data.status === 'todo' ? <p className="empty">{MSG.notOpened}</p> : <EvidenceBody e={ev.data} />}
        </section>
      )}
    </div>
  );
}
