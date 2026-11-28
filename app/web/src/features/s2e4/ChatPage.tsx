import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { Scene2DView } from '../../shared/scene2d/Scene2DView.tsx';
import type { Scene2D } from '../../shared/scene2d/geometry2d.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { acceptProposal, fetchChat, recheck, rejectProposal, sendMessage, undoEdit, type ChatView, type MessageView } from './api.ts';
import { charCount, MAX_MESSAGE_CHARS, messageIssue, MSG } from './logic.ts';
import { ProposalPanel } from './ProposalPanel.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * C2-04a "Chỉnh nháp với AI" (US-S2E4-01; Design/prototype/s23c.js c2-04a-chat-chinh-nhap). Trái: trao đổi (AI hỏi lại
 * hoặc nêu đề xuất). Phải: nháp hiện tại + đề xuất đang chờ với TRƯỚC / SAU, nguồn, chỗ chưa chắc; Nhận / Sửa tay / Bác;
 * Hoàn tác (tối đa 10 bước). AI không tự lưu: nháp chỉ đổi khi giáo viên bấm Nhận, và chỉ vào giáo trình khi Duyệt.
 */
export function ChatPage() {
  const { id = '' } = useParams();
  const [view, setView] = useState<ChatView | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  const [fail, setFail] = useState<string | null>(null);
  const endRef = useRef<HTMLDivElement | null>(null);

  const load = useCallback(() => {
    fetchChat(id)
      .then((v) => {
        setError(null);
        setView(v);
      })
      .catch(setError);
  }, [id]);
  useEffect(load, [load]);
  useEffect(() => {
    endRef.current?.scrollIntoView?.({ block: 'nearest' });
  }, [view?.messages.length]);

  const run = (action: () => Promise<ChatView>, after?: () => void) => {
    setBusy(true);
    setFail(null);
    action()
      .then((v) => {
        setView(v);
        after?.();
      })
      .catch((e: unknown) => setFail(e instanceof ApiError ? e.message : MSG.generic))
      .finally(() => setBusy(false));
  };

  if (error) return <ErrorNotice error={error} onRetry={load} testId="s2e4-chat-load-error" />;
  if (!view) return <p className="muted" aria-busy="true">Đang tải…</p>;

  const issue = messageIssue(text);
  const pending = [...view.messages].reverse().find((m) => m.proposal?.state === 'pending') ?? null;
  const result = view.draft.result;
  const send = () => {
    if (!issue.sendable || !view.canSend || busy) return;
    run(() => sendMessage(id, text.trim()), () => setText(''));
  };

  return (
    <div className="s2e4-page" data-testid="s2e4-chat" data-draft={view.draft.id}>
      <header className="s2e4-row">
        <h1 className="s2e4-title">Chỉnh nháp với AI</h1>
        <span className="s2e4-grow" />
        <Link className="btn ghost" to="/gv/chinh-nhap" data-testid="s2e4-chat-back">
          <Icon name="back" />
          Danh sách nháp
        </Link>
      </header>
      {fail && (
        <p className="alert" role="alert" data-testid="s2e4-chat-error">
          {fail}
        </p>
      )}
      <div className="s2e4-cols">
        <section className="card s2e4-card s2e4-thread-card" aria-label="Trao đổi">
          <div className="s2e4-row">
            <h2 className="s2e4-h">Trao đổi</h2>
            <span className="s2e4-grow" />
            <span className="chip" data-testid="s2e4-chat-saved">
              <Icon name="spark" />
              {view.saved ? 'Đã vào Của tôi' : `${view.humanLabel} · chưa lưu`}
            </span>
          </div>
          <ol className="s2e4-thread" data-testid="s2e4-thread">
            {view.messages.length === 0 && <li className="s2e4-sub muted">Nêu điều cô muốn chỉnh, ví dụ "đổi cạnh BC thành 14".</li>}
            {view.messages.map((m) => (
              <Bubble key={m.id} m={m} />
            ))}
            <div ref={endRef} />
          </ol>
          <div className="s2e4-compose">
            <label className="s2e4-cap" htmlFor="s2e4-chat-input">
              Yêu cầu của cô
            </label>
            <textarea
              id="s2e4-chat-input"
              rows={2}
              value={text}
              disabled={!view.canSend}
              placeholder='Nhập yêu cầu, ví dụ "đổi cạnh BC thành 14"'
              onChange={(e) => setText(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  send();
                }
              }}
              data-math
              data-testid="s2e4-chat-input"
            />
            <div className="s2e4-row">
              <span className="s2e4-sub muted" data-testid="s2e4-chat-turns">
                Lượt {view.turns}/{view.maxTurns} · {charCount(text.trim())}/{MAX_MESSAGE_CHARS} ký tự
              </span>
              <span className="s2e4-grow" />
              <button type="button" className="btn" disabled={!issue.sendable || !view.canSend || busy} onClick={send} data-testid="s2e4-chat-send">
                <Icon name="send" />
                Gửi
              </button>
            </div>
            {issue.message && (
              <p className="s2e4-sub bad" role="alert" data-testid="s2e4-chat-input-error">
                {issue.message}
              </p>
            )}
            {view.sendNote && (
              <p className="s2e4-sub bad" role="status" data-testid="s2e4-chat-limit">
                {view.sendNote}
              </p>
            )}
          </div>
        </section>

        <div className="s2e4-side">
          <section className="card s2e4-card" aria-label="Nháp hiện tại">
            <div className="s2e4-row">
              <h2 className="s2e4-h">Nháp hiện tại</h2>
              <span className="s2e4-grow" />
              <span className={`chip${view.machineLabel === 'Đã kiểm chứng' ? ' grn' : ' yel'}`} data-testid="s2e4-chat-machine">
                <Icon name={view.machineLabel === 'Đã kiểm chứng' ? 'check' : 'warn'} />
                {view.machineLabel}
              </span>
              <span className="chip" data-testid="s2e4-chat-human">
                <Icon name="spark" />
                {view.humanLabel}
              </span>
            </div>
            <p className="s2e4-statement" data-math data-testid="s2e4-chat-statement">
              <MathText text={view.draft.statement} />
            </p>
            {result?.sceneKind === 'plane' && result.scene ? <Scene2DView scene={result.scene as Scene2D} testId="s2e4-scene2d" /> : null}
            {result?.answers && (
              <p className="s2e4-sub" data-testid="s2e4-chat-answers" data-state={result.answers.state}>
                Đề ghi: <b data-math><MathText text={result.answers.stated ?? '—'} /></b> · AI giải: <b data-math><MathText text={result.answers.ai ?? '—'} /></b> · Máy tính lại:{' '}
                <b data-math><MathText text={result.answers.computed ?? '—'} /></b>
              </p>
            )}
            {result?.approveBlocked && result.blockedReason && (
              <p className="alert" role="status" data-testid="s2e4-chat-blocked">
                {result.blockedReason}
              </p>
            )}
            <div className="s2e4-row">
              <button type="button" className="btn ghost" disabled={!view.canUndo || busy} onClick={() => run(() => undoEdit(id))} data-testid="s2e4-undo">
                <Icon name="rotateLeft" />
                Hoàn tác
              </button>
              {view.draft.result?.docId && (
                <button type="button" className="btn ghost" disabled={busy} onClick={() => run(() => recheck(id))} data-testid="s2e4-recheck">
                  <Icon name="shield" />
                  Kiểm tra lại
                </button>
              )}
              <span className="s2e4-sub muted" data-testid="s2e4-undo-note">
                {view.undoNote ?? (view.canUndo ? `Hoàn tác được ${view.undoSteps} bước` : 'Bấm Hoàn tác được sau khi nhận đề xuất')}
              </span>
            </div>
            <p className="s2e4-sub muted">
              <Icon name="shield" inline />
              AI không tự lưu. Nháp chỉ vào giáo trình khi cô bấm Duyệt
              {view.draft.result?.docId ? (
                <>
                  {' '}
                  ở màn{' '}
                  <Link to={`/gv/soat-loi-giai/${view.draft.result.docId}`} data-testid="s2e4-review-link">
                    soát lời giải
                  </Link>
                </>
              ) : null}
              .
            </p>
          </section>

          {pending?.proposal ? (
            <ProposalPanel
              key={pending.id}
              proposal={pending.proposal}
              busy={busy}
              onAccept={(after) => run(() => acceptProposal(id, pending.id, after))}
              onReject={() => run(() => rejectProposal(id, pending.id))}
            />
          ) : (
            <section className="card s2e4-card" data-testid="s2e4-no-proposal">
              <h2 className="s2e4-h">Đề xuất của AI</h2>
              <p className="s2e4-sub muted">Chưa có đề xuất nào đang chờ.</p>
            </section>
          )}
        </div>
      </div>
    </div>
  );
}

function Bubble({ m }: { m: MessageView }) {
  const p = m.proposal;
  return (
    <li className={`s2e4-bubble ${m.role}`} data-testid={`s2e4-msg-${m.seq}`} data-role={m.role} data-kind={m.kind}>
      <span data-math><MathText text={m.text} /></span>
      {p && p.state !== 'pending' && (
        <span className={`chip${p.state === 'accepted' ? ' grn' : ''}`} data-testid={`s2e4-msg-note-${m.seq}`}>
          <Icon name={p.state === 'accepted' ? 'check' : 'x'} />
          <MathText text={p.note} />
        </span>
      )}
      {p && p.state === 'pending' && <span className="chip yel">đang chờ cô quyết định</span>}
    </li>
  );
}
