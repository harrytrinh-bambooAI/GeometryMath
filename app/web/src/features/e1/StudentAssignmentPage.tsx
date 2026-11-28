import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useParams } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { AssignmentScene } from '../e2/ProblemDetailPage.tsx';
import { MSG, errorText } from './messages.ts';
import { RELEASE_LABEL, VERIFY_LABEL } from './studentApi.ts';
import type { Asked, StudentAssignment } from './studentApi.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon, releaseIcon } from '../../shared/ui/icons.ts';

/**
 * Một bài giao của học sinh (tối thiểu cho E1): đề, trạng thái giờ mở, tiến độ, nhãn máy và khung Hỏi đáp
 * qua cổng AI (US-GEO-103). Cổng lỗi/quá thời gian: MSG-GEO-14 + "Thử lại" gửi lại đúng câu cũ, không hiện nội
 * dung dở dang (BR-GEO-109). Bài không thuộc lớp em: MSG-GEO-04 (US-GEO-104-FC1).
 */
export function StudentAssignmentPage() {
  const { id = '' } = useParams();
  const [a, setA] = useState<StudentAssignment | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    api
      .get<StudentAssignment>(`/api/v1/student/assignments/${encodeURIComponent(id)}`)
      .then((x) => alive && setA(x))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [id]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e1', 'assignment-page', 'error')}
        data-error-code={error instanceof ApiError ? error.code : 'UNKNOWN'}>
        {errorText(error)}
      </div>
    );
  }
  if (!a) return <p className="muted">Đang tải…</p>;
  return (
    <div data-testid={tid('e1', 'assignment-page')}>
      <h2>
        {a.problemCode} <span className="muted small">· Lớp {a.className}</span>
      </h2>
      <p className="e1-statement" data-testid={tid('e1', 'assignment-page', 'statement')}>
        {a.statement}
      </p>
      <p>
        <span className="chip" data-testid={tid('e1', 'assignment-page', 'release')} data-status={a.releaseState}><Icon name={releaseIcon(a.releaseState)} />
          Lời giải: {RELEASE_LABEL[a.releaseState]}
          {a.releaseState === 'upcoming' && a.releaseAt ? ` · mở lúc ${formatVnDateTime(a.releaseAt)}` : ''}
        </span>{' '}
        <span className="chip" data-testid={tid('e1', 'assignment-page', 'label')} data-status={a.verifyStatus}><Icon name={verifyIcon(a.verifyStatus === 'verified')} />
          {VERIFY_LABEL[a.verifyStatus]}
        </span>{' '}
        <span className="chip" data-testid={tid('e1', 'assignment-page', 'progress')}>
          {a.progress.k}/3 · {a.progress.label}
        </span>
      </p>
      <p>
        {/* E3: không gian làm bài trên hình 3D */}
        <Link className="btn primary" to={`/hs/lam-bai/${encodeURIComponent(a.id)}`} data-testid={tid('e1', 'assignment-page', 'workspace')}><Icon name="pencil" />
          Làm bài trên hình
        </Link>
      </p>
      {/* E2: hình của đề (thẻ ràng buộc, nhãn máy; US-GEO-201-SC3) */}
      <AssignmentScene assignmentId={a.id} />

      {a.allowQa ? <QaPanel assignmentId={a.id} /> : <p className="muted">Bài này không bật Hỏi đáp.</p>}
    </div>
  );
}

function QaPanel({ assignmentId }: { assignmentId: string }) {
  const [text, setText] = useState('');
  const [turns, setTurns] = useState<Asked[]>([]);
  const [pending, setPending] = useState<string | null>(null);
  const [failed, setFailed] = useState<{ question: string; error: unknown } | null>(null);

  const send = async (question: string) => {
    setFailed(null);
    setPending(question);
    try {
      const r = await api.post<Asked>(`/api/v1/student/assignments/${encodeURIComponent(assignmentId)}/questions`, {
        question,
        requestId: crypto.randomUUID(),
      });
      setTurns((t) => [...t, r]);
      setText('');
    } catch (e) {
      setFailed({ question, error: e });
    } finally {
      setPending(null);
    }
  };

  const submit = (ev: FormEvent) => {
    ev.preventDefault();
    if (text.trim()) void send(text.trim());
  };

  const busyOrServer = (e: unknown) =>
    !(e instanceof ApiError) || ['TIMEOUT', 'DEPENDENCY_UNAVAILABLE', 'INTERNAL', 'NETWORK', 'BAD_RESPONSE'].includes(e.code);

  return (
    <section className="card" data-testid={tid('e1', 'qa')}>
      <h3>Hỏi đáp</h3>
      <div className="e1-qa-log">
        {turns.map((t) => (
          <div key={t.id} className="e1-qa-turn" data-testid={tid('e1', 'qa', 'turn')}>
            <p className="e1-q">{t.question}</p>
            <p className="e1-a" data-testid={tid('e1', 'qa', 'answer')}>
              {t.reply}
            </p>
          </div>
        ))}
        {pending ? (
          <p className="muted" data-testid={tid('e1', 'qa', 'pending')}>
            Trợ lý đang trả lời…
          </p>
        ) : null}
        {failed ? (
          <div className="alert" role="alert" data-testid={tid('e1', 'qa', 'error')}
            data-error-code={failed.error instanceof ApiError ? failed.error.code : 'UNKNOWN'}>
            {busyOrServer(failed.error) ? MSG['14'] : errorText(failed.error)}{' '}
            <button type="button" className="btn ghost" onClick={() => void send(failed.question)} data-testid={tid('e1', 'qa', 'retry')}><Icon name="redo" />
              Thử lại
            </button>
          </div>
        ) : null}
      </div>
      <form className="e1-qa-form" onSubmit={submit}>
        <input
          value={text}
          placeholder="Em muốn hỏi gì?"
          onChange={(e) => setText(e.target.value)}
          disabled={pending !== null}
          data-testid={tid('e1', 'qa', 'input')}
        />
        <button type="submit" className="btn" disabled={pending !== null} data-testid={tid('e1', 'qa', 'send')}><Icon name="send" />
          Gửi
        </button>
      </form>
    </section>
  );
}
