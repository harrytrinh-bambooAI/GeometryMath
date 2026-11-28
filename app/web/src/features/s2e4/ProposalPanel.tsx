import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon.tsx';
import type { ProposalView } from './api.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Một đề xuất của AI đang chờ (BR-S2E4-02): đã đổi gì (TRƯỚC / SAU), dựa trên nguồn nào, chỗ nào AI chưa chắc.
 * "Sửa tay" cho sửa phần SAU rồi mới Nhận; "Bác" giữ nguyên nháp.
 */
export function ProposalPanel({
  proposal,
  busy,
  onAccept,
  onReject,
}: {
  proposal: ProposalView;
  busy: boolean;
  onAccept: (after?: string) => void;
  onReject: () => void;
}) {
  const [editing, setEditing] = useState(false);
  const [after, setAfter] = useState(proposal.after);
  const edited = editing && after.trim() !== '' && after.trim() !== proposal.after;

  return (
    <section className="card s2e4-card" aria-label="Đề xuất của AI" data-testid="s2e4-proposal" data-target={proposal.target}>
      <div className="s2e4-row">
        <h2 className="s2e4-h">Đề xuất của AI · so sánh trước / sau</h2>
        <span className="s2e4-grow" />
        <span className="chip yel">
          <Icon name="warn" />
          Máy chưa kiểm
        </span>
      </div>
      <div className="s2e4-diff">
        <div className="s2e4-diff-cell before">
          <span className="s2e4-cap">TRƯỚC · {proposal.target === 'answer' ? 'đáp số đề ghi' : 'bản nháp hiện tại'}</span>
          <span className="s2e4-diff-text" data-math data-testid="s2e4-proposal-before">
            <MathText text={proposal.before} />
          </span>
        </div>
        <div className="s2e4-diff-cell after">
          <span className="s2e4-cap">SAU · đề xuất</span>
          {editing ? (
            <input
              type="text"
              value={after}
              maxLength={500}
              aria-label="Sửa tay phần sau"
              onChange={(e) => setAfter(e.target.value)}
              data-math
              data-testid="s2e4-edit-input"
            />
          ) : (
            <span className="s2e4-diff-text" data-math data-testid="s2e4-proposal-after">
              <MathText text={proposal.after} />
            </span>
          )}
        </div>
      </div>
      <p className="s2e4-sub" data-testid="s2e4-proposal-source">
        Nguồn: {proposal.source}
      </p>
      {proposal.unsure && (
        <p className="s2e4-sub s2e4-unsure" data-testid="s2e4-proposal-unsure">
          <Icon name="warn" inline />
          Chưa chắc: {proposal.unsure}
        </p>
      )}
      <div className="s2e4-row">
        <button type="button" className="btn" disabled={busy || (editing && after.trim() === '')} onClick={() => onAccept(edited ? after.trim() : undefined)} data-testid="s2e4-accept">
          <Icon name="check" />
          Nhận
        </button>
        <button type="button" className="btn ghost" disabled={busy || editing} onClick={() => setEditing(true)} data-testid="s2e4-edit">
          <Icon name="pencil" />
          Sửa tay
        </button>
        <button type="button" className="btn ghost" disabled={busy} onClick={onReject} data-testid="s2e4-reject">
          <Icon name="x" />
          Bác
        </button>
        <span className="s2e4-grow" />
        <span className="s2e4-sub muted">Chưa có gì được lưu cho tới khi cô bấm Nhận</span>
      </div>
    </section>
  );
}
