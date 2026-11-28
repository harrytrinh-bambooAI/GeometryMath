import { useEffect, useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { Scene2DView } from '../../shared/scene2d/Scene2DView.tsx';
import type { Scene2D } from '../../shared/scene2d/geometry2d.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { practiceApi, type AttemptView, type PlayItem, type PlayView } from './api.ts';
import type { ChoiceAttemptView, RunnerChoice } from './choice.ts';
import { emptyField, newRequestKey, type Field } from './keypad.ts';
import { AnswerBox, MathKeypad } from './MathKeypad.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

export const MSG_NET = 'Chưa gửi được, đáp số của em vẫn còn';
export const MSG_SENT = 'Đã gửi cho thầy cô';
export const MSG_REVIEW = 'Xem lại cùng thầy cô';

/** Cách chấm: thật (ghi lượt ở máy chủ) hoặc xem trước của giáo viên (không ghi gì, BR-S2E8-28). */
export interface RunnerDriver {
  check: (item: PlayItem, answer: string, requestKey: string) => Promise<AttemptView>;
  report?: (item: PlayItem, note: string) => Promise<{ message: string }>;
  /** Chấm câu trắc nghiệm (S2-E9): gửi mã lựa chọn, không gửi chữ. */
  checkChoice?: (item: PlayItem, choiceId: string, requestKey: string) => Promise<ChoiceAttemptView>;
  /** Có vẽ hình của câu không (xem trước bài nháp chưa có hàng bài giao nên không có hình). */
  figures: boolean;
}

type Feedback =
  | { kind: 'correct' | 'wrong' | 'syntax'; message: string; note: string | null }
  | { kind: 'net'; message: string; note: null };

/** Hình của câu (đề phẳng): khung giữ chỗ khi đang tải, bỏ trống khi đề không có hình. */
function ItemFigure({ assignmentId }: { assignmentId: string }) {
  const [state, setState] = useState<{ id: string; scene: Scene2D | null } | null>(null);
  useEffect(() => {
    const ctl = new AbortController();
    practiceApi
      .figure(assignmentId, ctl.signal)
      .then((f) => setState({ id: assignmentId, scene: f.scene }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setState({ id: assignmentId, scene: null });
      });
    return () => ctl.abort();
  }, [assignmentId]);
  if (state?.id !== assignmentId) return <div className="s2e8-skel s2e8-skel-figure" data-testid="s2e8-figure-loading" />;
  if (!state.scene) return null;
  return (
    <div className="s2e8-figure">
      <Scene2DView scene={state.scene} testId="s2e8-figure" />
    </div>
  );
}

function segmentState(item: PlayItem, current: boolean): string {
  if (item.state === 'correct') return 'correct';
  if (item.state === 'review') return 'review';
  return current ? 'current' : 'todo';
}

/**
 * C2-06 · Làm bài luyện (US-S2E8-03): "Câu k/n", chip lượt, thanh tiến độ n đoạn, hình, ô đáp số, bàn phím toán.
 * Đúng: xác nhận + "Câu kế". Sai: "Chưa đúng. Còn m lượt." + gợi ý mức 1. Hết lượt: "xem lại cùng thầy cô", khóa nhập,
 * vẫn xem gợi ý. Mất mạng: giữ đáp số, "Thử lại" gửi CÙNG mã chống lặp nên không trừ lượt hai lần (BR-S2E8-14, 20).
 * Câu trắc nghiệm (S2-E9, `choices` theo assignmentId): chọn một lựa chọn rồi "Kiểm tra"; bàn phím toán ẩn; lựa chọn sai
 * bị làm mờ, không chọn lại; không có nút "Em nghĩ đáp số này đúng" (BR-S2E9-12…14).
 */
export function PracticeRunner({
  view,
  driver,
  onFinish,
  finishLabel = 'Xem kết quả',
  preview = false,
  choices,
}: {
  view: PlayView;
  driver: RunnerDriver;
  choices?: Record<string, RunnerChoice>;
  onFinish: () => void;
  finishLabel?: string;
  preview?: boolean;
}) {
  const total = view.items.length;
  const [items, setItems] = useState<PlayItem[]>(view.items);
  const [index, setIndex] = useState(Math.max(1, Math.min(view.current, total)));
  const item = items[index - 1]!;
  return (
    <div className="s2e8-run" data-testid="s2e8-run" data-preview={preview}>
      <QuestionCard
        key={item.assignmentId + '#' + index}
        title={view.title}
        items={items}
        index={index}
        driver={driver}
        preview={preview}
        choice={choices?.[item.assignmentId]}
        onUpdate={(next) => setItems((all) => all.map((x) => (x.position === next.position ? next : x)))}
        onPrev={index > 1 ? () => setIndex(index - 1) : undefined}
        onNext={index < total ? () => setIndex(index + 1) : undefined}
        onFinish={onFinish}
        finishLabel={finishLabel}
      />
    </div>
  );
}

function QuestionCard({
  title,
  items,
  index,
  driver,
  preview,
  choice,
  onUpdate,
  onPrev,
  onNext,
  onFinish,
  finishLabel,
}: {
  title: string;
  items: PlayItem[];
  index: number;
  driver: RunnerDriver;
  preview: boolean;
  choice?: RunnerChoice;
  onUpdate: (item: PlayItem) => void;
  onPrev?: () => void;
  onNext?: () => void;
  onFinish: () => void;
  finishLabel: string;
}) {
  const item = items[index - 1]!;
  const total = items.length;
  const [field, setField] = useState<Field>(emptyField);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [busy, setBusy] = useState(false);
  const [pending, setPending] = useState<{ answer: string; key: string } | null>(null);
  const [hintOpen, setHintOpen] = useState(Boolean(item.hint) && item.state !== 'review');
  const [reportOpen, setReportOpen] = useState(false);
  const [note, setNote] = useState('');
  const [reportMsg, setReportMsg] = useState<string | null>(null);
  const [reportErr, setReportErr] = useState<string | null>(null);
  const [picked, setPicked] = useState<string | null>(choice?.chosen ?? null);
  const [dimmed, setDimmed] = useState<string[]>(choice?.dimmed ?? []);

  const done = item.state === 'correct' || item.state === 'review';

  const send = async (answer: string, key: string) => {
    setBusy(true);
    try {
      const r = await driver.check(item, answer, key);
      setPending(null);
      setFeedback({ kind: r.outcome, message: r.message, note: r.note });
      if (r.outcome === 'wrong' && r.hint) setHintOpen(r.attemptsLeft > 0);
      onUpdate({
        ...item,
        attemptsLeft: r.attemptsLeft,
        state: r.state,
        hint: r.hint ?? item.hint,
        canonical: r.canonical,
        lastAnswer: answer,
        canReport: r.canReport && !item.reported,
      });
    } catch (e) {
      if (e instanceof ApiError && e.code === 'NETWORK') {
        setPending({ answer, key }); // giữ mã: "Thử lại" không trừ lượt hai lần
        setFeedback({ kind: 'net', message: MSG_NET, note: null });
      } else {
        setPending(null);
        setFeedback({ kind: 'syntax', message: e instanceof ApiError ? e.message : 'Chưa gửi được. Em thử lại nhé.', note: null });
      }
    } finally {
      setBusy(false);
    }
  };
  const sendChoice = async (choiceId: string, key: string) => {
    if (!driver.checkChoice) return;
    setBusy(true);
    try {
      const r = await driver.checkChoice(item, choiceId, key);
      setPending(null);
      setFeedback({ kind: r.outcome, message: r.message, note: null });
      setDimmed(r.dimmed);
      if (r.outcome === 'wrong') setPicked(null);
      onUpdate({ ...item, attemptsLeft: r.attemptsLeft, state: r.state, canReport: false });
    } catch (e) {
      if (e instanceof ApiError && e.code === 'NETWORK') {
        setPending({ answer: choiceId, key }); // giữ mã: "Thử lại" không trừ lượt hai lần
        setFeedback({ kind: 'net', message: 'Chưa gửi được, lựa chọn của em vẫn còn', note: null });
      } else {
        setPending(null);
        setFeedback({ kind: 'syntax', message: e instanceof ApiError ? e.message : 'Chưa gửi được. Em thử lại nhé.', note: null });
      }
    } finally {
      setBusy(false);
    }
  };
  const checkChoice = () => {
    if (done || busy || !picked) return;
    void sendChoice(picked, pending && pending.answer === picked ? pending.key : newRequestKey());
  };
  const check = () => {
    if (done || busy || field.value.length === 0) return;
    void send(field.value, pending && pending.answer === field.value ? pending.key : newRequestKey());
  };
  const sendReport = async () => {
    if (!driver.report) return;
    setReportErr(null);
    try {
      const r = await driver.report(item, note);
      setReportMsg(r.message);
      setReportOpen(false);
      onUpdate({ ...item, canReport: false, reported: true });
    } catch (e) {
      setReportErr(e instanceof ApiError ? e.message : 'Chưa gửi được. Em thử lại nhé.');
    }
  };

  const tone = feedback?.kind === 'correct' ? 'ok' : feedback?.kind === 'wrong' || feedback?.kind === 'net' ? 'bad' : 'warn';
  return (
    <div className="s2e8-play" data-testid="s2e8-question" data-position={index} data-state={item.state}>
      <section className="card s2e8-stage">
        <h1 className="s2e8-title" data-testid="s2e8-title">
          {title}
        </h1>
        <p className="s2e8-statement" data-math data-testid="s2e8-statement">
          <MathText text={item.statement} />
        </p>
        {driver.figures ? <ItemFigure assignmentId={item.assignmentId} /> : null}
      </section>
      <section className="card s2e8-side">
        <div className="s2e8-head">
          <span className="chip" data-testid="s2e8-counter">
            Câu {index}/{total}
          </span>
          <span className={`chip ${item.attemptsLeft === 0 ? 'red' : 'yel'}`} data-testid="s2e8-attempts">
            Còn {item.attemptsLeft}/{item.maxAttempts} lượt
          </span>
          <div className="s2e8-progress" data-testid="s2e8-progress" aria-label={`Tiến độ: câu ${index} trên ${total}`}>
            {items.map((x) => (
              <i key={x.position} data-state={segmentState(x, x.position === index)} data-testid={`s2e8-seg-${x.position}`} />
            ))}
          </div>
        </div>
        {choice ? (
          <>
            <div className="s2e8-label">CHỌN MỘT ĐÁP ÁN</div>
            <div className="s2e9-options" role="radiogroup" aria-label="Các lựa chọn" data-testid="s2e9-options">
              {choice.options.map((o, k) => {
                const off = dimmed.includes(o.id);
                return (
                  <button
                    key={o.id}
                    type="button"
                    role="radio"
                    aria-checked={picked === o.id}
                    className={`s2e9-option${picked === o.id ? ' on' : ''}${off ? ' dim' : ''}`}
                    disabled={done || busy || off}
                    onClick={() => setPicked(o.id)}
                    data-math
                    data-dimmed={off}
                    data-picked={picked === o.id}
                    data-testid={`s2e9-option-${k + 1}`}
                  >
                    <MathText text={o.text} />
                  </button>
                );
              })}
            </div>
            {done ? null : (
              <button type="button" className="btn s2e9-check" onClick={checkChoice} disabled={busy || !picked} data-testid="s2e9-choice-check">
                <Icon name="check" />
                {busy ? 'Đang gửi…' : 'Kiểm tra'}
              </button>
            )}
          </>
        ) : (
          <>
            <div className="s2e8-label">ĐÁP SỐ CỦA EM</div>
            <AnswerBox
              field={field}
              onChange={(f) => {
                setField(f);
                if (feedback?.kind === 'net' && f.value !== pending?.answer) setFeedback(null);
              }}
              onSubmit={check}
              disabled={done}
              invalid={feedback?.kind === 'wrong'}
            />
          </>
        )}
        {item.state === 'correct' && !feedback ? (
          <p className="s2e8-fb ok" role="status" data-testid="s2e8-feedback">
            Câu này em đã làm đúng.{item.canonical ? ` Dạng chuẩn: ${item.canonical}` : ''}
          </p>
        ) : null}
        {item.state === 'review' && !feedback ? (
          <p className="s2e8-fb bad" role="status" data-testid="s2e8-feedback">
            Hết lượt cho câu này. Em xem lại cùng thầy cô nhé.
          </p>
        ) : null}
        {feedback ? (
          <div className={`s2e8-fb ${tone}`} role="status" data-testid="s2e8-feedback" data-kind={feedback.kind}>
            <span data-testid="s2e8-feedback-text">{feedback.message}</span>
            {feedback.note ? (
              <span className="s2e8-fb-note" data-testid="s2e8-feedback-note">
                <MathText text={feedback.note} />
              </span>
            ) : null}
            {feedback.kind === 'net' && pending ? (
              <button type="button" className="btn" onClick={() => void (choice ? sendChoice(pending.answer, pending.key) : send(pending.answer, pending.key))} disabled={busy} data-testid="s2e8-retry"><Icon name="redo" />
                Thử lại
              </button>
            ) : null}
          </div>
        ) : null}
        {item.state === 'review' ? (
          <p className="chip red s2e8-review" data-testid="s2e8-review"><Icon name="warn" />
            {MSG_REVIEW}
          </p>
        ) : null}
        {item.hint && hintOpen ? (
          <div className="s2e8-hint" data-testid="s2e8-hint">
            <b>Gợi ý:</b> <MathText text={item.hint} />
          </div>
        ) : null}
        {item.hint && !hintOpen ? (
          <button type="button" className="btn ghost" onClick={() => setHintOpen(true)} data-testid="s2e8-hint-open"><Icon name="hint" />
            Xem gợi ý
          </button>
        ) : null}
        {!preview && !choice && item.canReport && !reportOpen ? (
          <button type="button" className="btn ghost" onClick={() => setReportOpen(true)} data-testid="s2e8-report-open">
            Em nghĩ đáp số này đúng
          </button>
        ) : null}
        {reportOpen ? (
          <div className="s2e8-report" data-testid="s2e8-report-form">
            <label className="s2e8-field">
              Ghi chú cho thầy cô (không bắt buộc)
              <textarea value={note} maxLength={200} rows={2} onChange={(e) => setNote(e.target.value)} data-testid="s2e8-report-note" />
            </label>
            <span className="s2e8-help">{note.length}/200 ký tự</span>
            {reportErr ? (
              <span className="s2e8-err" role="alert" data-testid="s2e8-report-error">
                {reportErr}
              </span>
            ) : null}
            <div className="s2e8-actions">
              <button type="button" className="btn" onClick={() => void sendReport()} data-testid="s2e8-report-send"><Icon name="send" />
                Gửi cho thầy cô
              </button>
              <button type="button" className="btn ghost" onClick={() => setReportOpen(false)}>
                Thôi
              </button>
            </div>
          </div>
        ) : null}
        {!choice && (reportMsg || item.reported) ? (
          <p className="s2e8-fb ok" role="status" data-testid="s2e8-report-sent">
            {reportMsg ?? MSG_SENT}
          </p>
        ) : null}
        <div className="s2e8-actions s2e8-nav">
          {onPrev ? (
            <button type="button" className="btn ghost" onClick={onPrev} data-testid="s2e8-prev"><Icon name="prev" />
              Câu trước
            </button>
          ) : null}
          {onNext ? (
            <button type="button" className={`btn${done ? '' : ' ghost'}`} onClick={onNext} data-testid="s2e8-next">
              Câu kế
            </button>
          ) : (
            <button type="button" className={`btn${done ? '' : ' ghost'}`} onClick={onFinish} data-testid="s2e8-finish">
              {finishLabel}
            </button>
          )}
        </div>
        {done || choice ? null : <MathKeypad field={field} onChange={setField} onSubmit={check} busy={busy} />}
      </section>
    </div>
  );
}
