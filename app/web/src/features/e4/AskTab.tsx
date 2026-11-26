import { useState } from 'react';
import type { FormEvent } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { e4Api, isNetworkError, M, type Asked } from './api.ts';
import { KwText, useOpenLecture } from './Lecture.tsx';
import type { AskTurn, HelpModel } from './useHelp.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/** Một câu trả lời của trợ lý: nội dung (từ khóa bấm được) + dòng "Nguồn: …" kèm tên bài giảng bấm được (BR-GEO-189). */
export function AnswerView({ a, testId }: { a: Asked; testId: string }) {
  const openLecture = useOpenLecture();
  return (
    <div className="e4-answer" data-testid={testId} data-kind={a.replyKind}>
      <p>
        <KwText text={a.reply} testId={tid(testId, 'text')} />
      </p>
      {a.source && (
        <p className="small" data-testid={tid(testId, 'source')}>
          Nguồn: {a.source.label} ·{' '}
          <button type="button" className="e4-link" data-testid={tid(testId, 'source', 'link')}
            onClick={() => openLecture({ slug: a.source!.slug })}>
            {a.source.title}
          </button>
        </p>
      )}
    </div>
  );
}

/**
 * Tab "Hỏi đáp" (US-GEO-409): câu hỏi 1…1000 ký tự (MSG-GEO-37), trong lúc chờ hiện MSG-GEO-15 và không gửi được câu
 * thứ hai, câu trả lời hiện nguyên vẹn một lần. Trợ lý lỗi: MSG-GEO-14 + "Thử lại" (gửi lại đúng câu với cùng requestId,
 * máy chủ lũy đẳng). Giáo viên tắt hỏi đáp: MSG-GEO-33, không có ô nhập.
 */
export function AskTab({ id, model }: { id: string; model: HelpModel }) {
  const { help, asks, setAsks, refresh } = model;
  const [text, setText] = useState('');
  const [invalid, setInvalid] = useState(false);
  if (!help) return <p className="muted">Đang tải…</p>;
  if (!help.allowQa) {
    return (
      <p className="muted" data-testid={tid('e4', 'qa', 'disabled')}>
        {help.qaMessage ?? M['33']}
      </p>
    );
  }
  const pending = asks.some((t) => t.pending);

  const send = async (turn: AskTurn) => {
    setAsks((xs) => xs.map((t) => (t.key === turn.key ? { ...t, pending: true, error: undefined } : t)));
    try {
      const answer = await e4Api.ask(id, turn.question, turn.requestId);
      setAsks((xs) => xs.map((t) => (t.key === turn.key ? { ...t, pending: false, answer } : t)));
      void refresh();
    } catch (error) {
      setAsks((xs) => xs.map((t) => (t.key === turn.key ? { ...t, pending: false, error } : t)));
    }
  };

  const submit = (ev: FormEvent) => {
    ev.preventDefault();
    if (pending) return;
    const q = text.trim();
    if (q.length < 1 || [...q].length > 1000) {
      setInvalid(true);
      return;
    }
    setInvalid(false);
    const turn: AskTurn = { key: crypto.randomUUID(), question: q, requestId: crypto.randomUUID(), pending: true };
    setAsks((xs) => [...xs, turn]);
    setText('');
    void send(turn);
  };

  return (
    <div className="e4-qa" data-testid={tid('e4', 'qa')}>
      <ol className="e4-qa-list">
        {asks.map((t, i) => (
          <li key={t.key} data-testid={tid('e4', 'qa', 'item', i + 1)}>
            <p className="e4-question" data-testid={tid('e4', 'qa', 'question', i + 1)}>
              {t.question}
            </p>
            {t.pending && (
              <p className="muted" data-testid={tid('e4', 'qa', 'wait')}>
                {M['15']}
              </p>
            )}
            {t.answer && <AnswerView a={t.answer} testId={tid('e4', 'qa', 'answer', i + 1)} />}
            {t.error !== undefined && !t.pending && (
              <p role="alert" className="e4-err" data-testid={tid('e4', 'qa', 'error')}>
                {errorOf(t.error)}{' '}
                {!(t.error instanceof ApiError && t.error.code === 'FORBIDDEN') && (
                  <button type="button" className="btn ghost" data-testid={tid('e4', 'qa', 'retry')} onClick={() => void send(t)}><Icon name="redo" />
                    Thử lại
                  </button>
                )}
              </p>
            )}
          </li>
        ))}
      </ol>
      <form className="row e4-qa-form" onSubmit={submit}>
        <input
          className="grow"
          value={text}
          placeholder="Hỏi về bài này…"
          disabled={pending}
          onChange={(e) => setText(e.target.value)}
          data-testid={tid('e4', 'qa', 'input')}
          aria-label="Câu hỏi"
        />
        <button type="submit" className="btn" disabled={pending} data-testid={tid('e4', 'qa', 'send')}><Icon name="send" />
          Gửi
        </button>
      </form>
      {invalid && (
        <p role="alert" className="e4-err" data-testid={tid('e4', 'qa', 'invalid')}>
          {M['37']}
        </p>
      )}
    </div>
  );
}

function errorOf(e: unknown): string {
  if (isNetworkError(e)) return M['08'];
  if (e instanceof ApiError && e.code === 'FORBIDDEN') return e.message || M['33'];
  if (e instanceof ApiError && e.code === 'VALIDATION_FAILED') return M['37'];
  return M['14'];
}
