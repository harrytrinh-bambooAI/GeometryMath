import { useState } from 'react';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import type { DocView, ProposalView } from './api.ts';
import { TrustLabels } from './TrustLabels.tsx';
import { MSG } from './logic.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { AssignedBox } from './AssignedBox.tsx';

interface Props {
  doc: DocView;
  /** Bản nháp đang có thay đổi chưa lưu: không duyệt, không nhận đề xuất. */
  dirty: boolean;
  busy: boolean;
  onApprove: (confirmNeedsReview: boolean) => void;
  onProposal: (action: 'accept' | 'dismiss', p: ProposalView) => void;
  onEditProposal: (p: ProposalView) => void;
  onViewVersion: (version: number) => void;
  /** US-S2E5-05: sau "Áp dụng cho lớp". */
  onApplied: (d: DocView) => void;
}

/** Hộp duyệt chung của C2-03 (US-S2E5-03, 04): nhãn hai trục, đề xuất của AI, lịch sử phiên bản, nút Duyệt. */
export function ReviewBox({ doc, dirty, busy, onApprove, onProposal, onEditProposal, onViewVersion, onApplied }: Props) {
  const [confirming, setConfirming] = useState(false);
  const v = doc.version;
  const a = doc.approve;
  const locked = !a.allowed || dirty || busy;

  return (
    <aside className="card s2e5-box" data-testid="s2e5-box" aria-label="Hộp duyệt">
      <h2 className="s2e5-h">Hộp duyệt</h2>

      <section className="s2e5-sec">
        <h3 className="s2e5-cap">Nhãn hai trục</h3>
        <TrustLabels machineLabel={v.machineLabel} machineNote={v.machineNote} humanLabel={v.humanLabel} where="box" />
        <p className="s2e5-sub muted">Trục máy do máy gắn. Trục người do thầy cô gắn. Hai trục độc lập.</p>
        {doc.inMyShelf && (
          <span className="chip" data-testid="s2e5-shelf">
            Của tôi
          </span>
        )}
      </section>

      <section className="s2e5-sec">
        <h3 className="s2e5-cap">Đề xuất của AI</h3>
        {doc.proposals.length === 0 ? (
          <p className="s2e5-sub muted" data-testid="s2e5-proposals-empty">
            {MSG.emptyBox}
          </p>
        ) : (
          <ul className="s2e5-list">
            {doc.proposals.map((p) => (
              <li key={p.id} className="s2e5-proposal" data-testid="s2e5-proposal">
                <p className="s2e5-sub muted">Thêm bước sau bước {p.afterStep}</p>
                <p className="s2e5-proposal-text" data-math data-testid="s2e5-proposal-text">
                  {p.content}
                </p>
                <div className="s2e5-row">
                  <button type="button" className="btn" disabled={dirty || busy || !v.latest} onClick={() => onProposal('accept', p)} data-testid="s2e5-proposal-accept"><Icon name="check" />
                    Nhận
                  </button>
                  <button type="button" className="btn ghost" disabled={dirty || busy || !v.latest} onClick={() => onEditProposal(p)} data-testid="s2e5-proposal-edit"><Icon name="pencil" />
                    Sửa
                  </button>
                  <button type="button" className="btn ghost" disabled={busy} onClick={() => onProposal('dismiss', p)} data-testid="s2e5-proposal-dismiss"><Icon name="x" />
                    Bỏ
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="s2e5-sec">
        <h3 className="s2e5-cap">Lịch sử phiên bản</h3>
        <ul className="s2e5-list" data-testid="s2e5-history">
          {doc.history.map((h, i) =>
            h.kind === 'version' && h.version ? (
              <li key={`v${h.version}`}>
                <button
                  type="button"
                  className={`s2e5-hist${h.version === v.version ? ' on' : ''}`}
                  aria-current={h.version === v.version}
                  onClick={() => onViewVersion(h.version!)}
                  data-testid={`s2e5-history-v${h.version}`}
                >
                  <span className="s2e5-hist-title" data-testid={`s2e5-history-desc-v${h.version}`}>
                    v{h.version} · {h.description}
                  </span>
                  <span className="s2e5-sub muted">
                    {h.author}
                    {h.authorCode ? ` (${h.authorCode})` : ''} · {h.at ? formatVnDateTime(h.at) : ''} · {h.humanLabel}
                  </span>
                </button>
              </li>
            ) : (
              <li key={`p${i}`} className="s2e5-hist-event" data-testid="s2e5-history-event">
                <span className="s2e5-hist-title">{h.description}</span>
                <span className="s2e5-sub muted">{h.at ? formatVnDateTime(h.at) : ''}</span>
              </li>
            ),
          )}
        </ul>
      </section>

      {doc.assignments.length > 0 && (
        <AssignedBox doc={doc} latestApproved={v.latest && a.approved} busy={busy || dirty} onApplied={onApplied} />
      )}

      <section className="s2e5-sec s2e5-approve">
        {a.approved ? (
          <p className="s2e5-ban grn" data-testid="s2e5-approved">
            Giáo viên đã duyệt v{v.version}
            {v.approvedCode ? ` · ${v.approvedCode}` : ''}
            {v.approvedAt ? ` · ${formatVnDateTime(v.approvedAt)}` : ''}
          </p>
        ) : a.blockedReason ? (
          <p className="s2e5-ban red" role="status" data-testid="s2e5-blocked"><Icon name="lock" inline />
            {a.blockedReason}
            {v.finalState === 'mismatch' && v.finalNote ? `. ${v.finalNote}` : ''}
          </p>
        ) : a.needsConfirm ? (
          <p className="s2e5-ban yel" data-testid="s2e5-approve-hint"><Icon name="eye" inline />
            Duyệt lúc này sẽ giữ nhãn "Cần xem lại" cho đến khi máy kiểm đạt.
          </p>
        ) : null}
        {!a.approved && (
          <button
            type="button"
            className="btn s2e5-approve-btn"
            disabled={locked}
            onClick={() => (a.needsConfirm ? setConfirming(true) : onApprove(false))}
            data-testid="s2e5-approve"
          ><Icon name="shield" />
            Duyệt lời giải này
          </button>
        )}
        {dirty && !a.approved && <p className="s2e5-sub muted">Lưu thay đổi trước khi duyệt.</p>}
      </section>

      {confirming && (
        <div className="s2e5-overlay">
          <div className="card s2e5-dialog" role="alertdialog" aria-modal="true" aria-labelledby="s2e5-confirm-title" data-testid="s2e5-confirm">
            <p id="s2e5-confirm-title" className="s2e5-dialog-title">
              {MSG.confirmApprove}
            </p>
            <div className="s2e5-row">
              <button
                type="button"
                className="btn"
                onClick={() => {
                  setConfirming(false);
                  onApprove(true);
                }}
                data-testid="s2e5-confirm-yes"
              >
                Vẫn duyệt
              </button>
              <button type="button" className="btn ghost" onClick={() => setConfirming(false)} data-testid="s2e5-confirm-no">
                Quay lại
              </button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
}
