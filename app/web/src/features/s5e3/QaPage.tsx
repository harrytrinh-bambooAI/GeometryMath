import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { ackPrivacy, askQa, getPrivacy, qaSource, type Privacy, type QaReply, type QaSourceView } from './qaApi.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * "Hỏi đáp" của học sinh trong bài (C5 "Hỏi đáp theo giáo trình", US-S5E3-03…05): /hs/hoi-dap?bai=<id bài>. Lần đầu phải
 * bấm "Em đã hiểu" ở MSG-S5-26 (nút Gửi mờ tới lúc đó); câu trả lời luôn kèm nguồn "Theo: …" bấm mở đúng mục được dẫn;
 * câu bị chặn hiện câu an toàn của máy chủ, lỗi bộ kiểm hiện MSG-S5-24 và Thử lại. data-testid bắt đầu bằng 's5e3-qa-'.
 */
export function QaPage() {
  const [params] = useSearchParams();
  const assignmentId = params.get('bai') ?? '';
  const [privacy, setPrivacy] = useState<Privacy | null>(null);
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState<QaReply | null>(null);
  const [reader, setReader] = useState<QaSourceView | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getPrivacy().then(setPrivacy).catch(setError);
  }, []);

  const send = async () => {
    setBusy(true);
    setError(null);
    setReader(null);
    try {
      setAnswer(await askQa(assignmentId, question.trim()));
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };

  const open = async (a: QaReply) => {
    try {
      setReader(await qaSource(a.interactionId));
    } catch (e) {
      setError(e);
    }
  };

  const acked = privacy?.acked === true;
  return (
    <section className="s5e3-page" data-testid="s5e3-qa">
      <h1 className="s5e3-title">Hỏi đáp</h1>
      {privacy && !acked && (
        <div className="card s5e3-card" role="note" data-testid="s5e3-qa-privacy">
          <p data-testid="s5e3-qa-msg26"><MathText text={privacy.text} /></p>
          <div className="s5e3-row">
            <button
              type="button"
              className="btn primary"
              onClick={() => void ackPrivacy(privacy.version).then(setPrivacy).catch(setError)}
              data-testid="s5e3-qa-ack"
            >
              <Icon name="check" /> Em đã hiểu
            </button>
          </div>
        </div>
      )}
      <div className="card s5e3-card">
        <label className="s5e3-grow">
          Câu hỏi của em
          <textarea
            className="s5e3-body"
            rows={3}
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            data-testid="s5e3-qa-question"
          />
        </label>
        <div className="s5e3-row">
          <button
            type="button"
            className="btn primary"
            disabled={!acked || busy || !question.trim() || !assignmentId}
            onClick={() => void send()}
            data-testid="s5e3-qa-send"
          >
            <Icon name="send" /> Gửi
          </button>
        </div>
      </div>
      {answer && (
        <div className="card s5e3-card" data-testid="s5e3-qa-answer" data-kind={answer.kind}>
          <p data-testid="s5e3-qa-reply"><MathText text={answer.reply} /></p>
          {answer.source && (
            <button type="button" className="btn s5e3-source" onClick={() => void open(answer)} data-testid="s5e3-qa-source">
              <Icon name="file" /> {answer.source.label}
            </button>
          )}
          {answer.source && <span className="chip">{answer.source.typeLabel}</span>}
          {answer.retry && (
            <button type="button" className="btn" disabled={busy} onClick={() => void send()} data-testid="s5e3-qa-retry">
              <Icon name="redo" /> Thử lại
            </button>
          )}
        </div>
      )}
      {reader && (
        <article className="card s5e3-card" data-testid="s5e3-qa-reader">
          <p className="s5e3-muted">
            {reader.docTitle} · v{reader.version}
          </p>
          <h2 className="s5e3-h" data-testid="s5e3-qa-reader-title">
            {reader.sectionTitle}
          </h2>
          <p className="s5e3-section-body" data-testid="s5e3-qa-reader-body">
            <MathText text={reader.body} />
          </p>
        </article>
      )}
      {error !== null && <ErrorNotice error={error} />}
    </section>
  );
}
