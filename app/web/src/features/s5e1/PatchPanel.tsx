import { useState } from 'react';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { acceptPatch, sendChat, skipPatch, type ChatReply } from './api.ts';
import { sectionLabel } from './logic.ts';

/**
 * Chat vá bản nháp (US-S5E1-04, giao-dien.md §2–3): mục đang chọn → câu chat → bản vá trước → sau từng dòng có lý do;
 * Nhận / Bỏ từng dòng hoặc "Nhận tất cả". Giáo án chỉ đổi khi bấm Nhận (BR-S5E1-10); mỗi lần Nhận lên một phiên bản.
 */
export function PatchPanel({
  planId,
  selected,
  locked,
  onClear,
  onChanged,
}: {
  planId: string;
  selected: string | null;
  locked: string[];
  onClear: () => void;
  onChanged: () => void;
}) {
  const [message, setMessage] = useState('');
  const [reply, setReply] = useState<ChatReply | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const isLocked = selected !== null && locked.includes(selected);

  const run = (p: Promise<ChatReply>, changed: boolean) => {
    setBusy(true);
    setError(null);
    p.then((r) => {
      setReply(r);
      if (changed) onChanged();
    })
      .catch(setError)
      .finally(() => setBusy(false));
  };
  const send = () => {
    run(sendChat(planId, selected, message.trim()), false);
    setMessage('');
  };
  const pending = reply?.lines?.filter((l) => l.decision === 'pending') ?? [];

  return (
    <section className="card s5e1-card s5e1-patch" data-testid="s5e1-patch-chat">
      <h2 className="s5e1-h">
        <Icon name="spark" /> Chat sửa giáo án
      </h2>
      <div className="s5e1-row">
        <span className="small muted">Mục đang chọn:</span>
        <span className="chip" data-testid="s5e1-selected">
          {selected ? sectionLabel(selected) : 'Cả giáo án'}
        </span>
        {selected && (
          <button type="button" className="btn ghost" onClick={onClear} data-testid="s5e1-selected-clear">
            <Icon name="x" />
            Bỏ chọn
          </button>
        )}
      </div>
      {isLocked && (
        <p className="small" role="status" data-testid="s5e1-selected-locked">
          Mục này đang khoá. Mở khoá trước khi sửa.
        </p>
      )}
      <textarea
        rows={2}
        aria-label="Yêu cầu sửa"
        maxLength={500}
        placeholder="Ví dụ: Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút"
        data-testid="s5e1-chat-input"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
      />
      <div className="s5e1-row">
        <span className="s5e1-grow" />
        <button
          type="button"
          className="btn primary"
          data-testid="s5e1-chat-send"
          disabled={busy || isLocked || !message.trim()}
          onClick={send}
        >
          <Icon name="send" />
          Gửi
        </button>
      </div>
      {busy && (
        <p className="small muted" aria-busy="true">
          AI đang đề xuất, máy chủ đang kiểm bản vá…
        </p>
      )}
      {error !== null && <ErrorNotice error={error} testId="s5e1-chat-error" />}
      {reply && reply.kind !== 'patch' && (
        <p className="s5e1-bubble" data-testid={`s5e1-patch-${reply.kind}`}>
          <strong>AI ▸ </strong>
          {reply.text}
        </p>
      )}
      {reply?.kind === 'patch' && reply.lines && (
        <div className="s5e1-patch-lines" data-testid="s5e1-patch">
          <div className="s5e1-row">
            <strong>Bản vá ({reply.lines.length} dòng)</strong>
            <span className="s5e1-grow" />
            <button
              type="button"
              className="btn primary"
              data-testid="s5e1-patch-accept-all"
              disabled={busy || pending.length === 0}
              onClick={() => run(acceptPatch(planId, reply.patchId!, null), true)}
            >
              <Icon name="check" />
              Nhận tất cả
            </button>
          </div>
          {reply.lines.map((l) => (
            <div key={l.index} className="s5e1-patch-line" data-testid={`s5e1-patch-line-${l.index}`} data-decision={l.decision}>
              <div className="small s5e1-label">{l.label}</div>
              <div className="s5e1-diff">
                <del data-testid={`s5e1-patch-before-${l.index}`}>{l.before ?? '—'}</del>
                <span aria-hidden="true"> → </span>
                <ins data-testid={`s5e1-patch-after-${l.index}`}>{l.after ?? '(xoá)'}</ins>
              </div>
              <div className="small muted" data-testid={`s5e1-patch-why-${l.index}`}>
                Lý do: {l.lyDo}
              </div>
              {l.decision === 'pending' ? (
                <div className="s5e1-row">
                  <button
                    type="button"
                    className="btn ghost"
                    data-testid={`s5e1-patch-accept-${l.index}`}
                    disabled={busy}
                    onClick={() => run(acceptPatch(planId, reply.patchId!, [l.index]), true)}
                  >
                    Nhận
                  </button>
                  <button
                    type="button"
                    className="btn ghost"
                    data-testid={`s5e1-patch-skip-${l.index}`}
                    disabled={busy}
                    onClick={() => run(skipPatch(planId, reply.patchId!, [l.index]), false)}
                  >
                    Bỏ
                  </button>
                </div>
              ) : (
                <span className="chip">{l.decision === 'accepted' ? 'Đã nhận' : 'Đã bỏ'}</span>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
