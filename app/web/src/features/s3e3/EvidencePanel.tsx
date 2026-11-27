import { useState } from 'react';
import { Link } from 'react-router';
import { formatVnTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { resultsApi, useLoad, type Evidence } from './api.ts';
import { fold, MSG, STATUS_ICON, STATUS_LABEL, stepLabel } from './format.ts';
import type { Picked } from './ResultsPage.tsx';

/**
 * Khung "BẰNG CHỨNG · {em} · Đề n" của một ô (US-S3E3-02): lượt gửi đáp số, gợi ý đã dùng, câu hỏi nguyên văn, bước em
 * dừng. Chữ do học sinh gõ luôn là chữ thuần (React không diễn giải HTML, BR-S3E3-21). Khung chỉ đọc: không có nút
 * sửa dữ liệu; nút "Nhắn em" của bản mẫu không làm ở chặng 3 (QA-S3E3-03).
 */
export function EvidencePanel(p: { classId: string; setId: string; picked: Picked; refresh: number; onRefreshed: () => void }) {
  const [ev, retry] = useLoad<Evidence>(`s3e3-ev-${p.setId}-${p.picked.userId}-${p.picked.item}-${p.refresh}`, (s) =>
    resultsApi.evidence(p.classId, p.setId, p.picked.item, p.picked.userId, false, s),
  );
  if (ev.state === 'loading')
    return (
      <section className="card s3e3-card" data-testid="s3e3-evidence" aria-busy="true">
        <p className="muted">Đang tải bằng chứng…</p>
      </section>
    );
  if (ev.state === 'error')
    return (
      <section className="card s3e3-card" data-testid="s3e3-evidence">
        <div className="alert" role="alert">
          Chưa tải được bằng chứng.{' '}
          <button className="btn ghost sm" onClick={retry}>
            Thử lại
          </button>
        </div>
      </section>
    );
  const e = ev.data;
  const work = `bai-lam?lop=${encodeURIComponent(p.classId)}&bo=${encodeURIComponent(p.setId)}&de=${e.item.position}&hs=${encodeURIComponent(e.userId)}`; // PO-74: hs=<userId>, không mã người dùng
  return (
    <section className="card s3e3-card" data-testid="s3e3-evidence" data-status={e.status}>
      <div className="s3e3-kicker" data-testid="s3e3-ev-title">
        BẰNG CHỨNG · {e.displayName} · Đề {e.item.position}
      </div>
      <div className="s3e3-head">
        <span className="mono" data-testid="s3e3-ev-code">
          {e.userCode}
        </span>
        {e.left && (
          <span className="chip" data-testid="s3e3-ev-left">
            đã rời
          </span>
        )}
        <span className={`s3e3-cell ${e.status} s3e3-key`} data-testid="s3e3-ev-status">
          <Icon name={STATUS_ICON[e.status]} inline />
          {STATUS_LABEL[e.status]}
        </span>
      </div>
      {e.status === 'todo' ? (
        <p className="empty" data-testid="s3e3-ev-empty">
          {MSG.notOpened}
        </p>
      ) : (
        <EvidenceBody e={e} />
      )}
      <div className="s3e3-head">
        {e.status !== 'todo' && (
          <Link className="btn ghost sm" to={work} data-testid="s3e3-open-work">
            <Icon name="eye" />
            Mở bài làm
          </Link>
        )}
        <button className="btn ghost sm" data-testid="s3e3-ev-refresh" onClick={p.onRefreshed}>
          <Icon name="redo" />
          Làm mới
        </button>
        <span className="s3e3-grow" />
        <span className="muted" data-testid="s3e3-ev-updated">
          Cập nhật lúc {formatVnTime(e.updatedAt)}
        </span>
      </div>
    </section>
  );
}

/** Các tương tác đã xảy ra của một ô; dùng chung cho khung bằng chứng và trang "Mở bài làm" chỉ đọc. */
export function EvidenceBody({ e }: { e: Evidence }) {
  return (
    <div className="s3e3-ev-body">
      {e.openedAt && (
        <div className="s3e3-line" data-testid="s3e3-ev-opened">
          <span className="mono">{formatVnTime(e.openedAt)}</span> mở đề
        </div>
      )}
      {e.hints.map((h, i) => (
        <div className="s3e3-line" key={`h${i}`} data-testid="s3e3-ev-hint">
          <span className="mono">{formatVnTime(h.at)}</span> xin gợi ý mức {h.level}
        </div>
      ))}
      {e.questions.map((q, i) => (
        <QuestionLine key={`q${i}`} at={q.at} text={q.text} />
      ))}
      {e.attempts.map((a, i) => (
        <div className="s3e3-line" key={`a${i}`} data-testid="s3e3-ev-attempt" data-matched={a.matched}>
          <span className="mono">{formatVnTime(a.at)}</span> gửi đáp số{' '}
          <b data-math className="s3e3-plain">
            {a.value}
          </b>{' '}
          · {a.matched ? 'khớp' : 'không khớp'}
        </div>
      ))}
      <div className="s3e3-line muted" data-testid="s3e3-ev-step">
        Bước em dừng: {stepLabel(e.stoppedStep)}
        {e.hints.length > 0 ? ` · đã dùng ${e.hints.length} gợi ý` : ''}
      </div>
    </div>
  );
}

function QuestionLine({ at, text }: { at: string; text: string }) {
  const [open, setOpen] = useState(false);
  const f = fold(text, open);
  return (
    <div className="s3e3-line" data-testid="s3e3-ev-question">
      <span className="mono">{formatVnTime(at)}</span> hỏi:{' '}
      <span className="s3e3-plain" data-testid="s3e3-ev-question-text">
        {f.shown}
        {f.folded ? '…' : ''}
      </span>
      {f.folded && (
        <button className="btn ghost sm" data-testid="s3e3-ev-more" onClick={() => setOpen(true)}>
          Xem thêm
        </button>
      )}
    </div>
  );
}
