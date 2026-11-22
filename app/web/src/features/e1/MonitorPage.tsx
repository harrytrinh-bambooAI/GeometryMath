import { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { errorText } from './messages.ts';
import { RELEASE_LABEL } from './studentApi.ts';
import type { Monitor } from './studentApi.ts';

/** Theo dõi lớp tối thiểu của E1 (số học sinh, tiến độ, câu hỏi; bản đầy đủ thuộc E7). */
export function MonitorPage() {
  const { id = '' } = useParams();
  const [m, setM] = useState<Monitor | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    api
      .get<Monitor>(`/api/v1/teacher/assignments/${encodeURIComponent(id)}/monitor`)
      .then((x) => alive && setM(x))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [id]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e1', 'monitor', 'error')}>
        {errorText(error)}
      </div>
    );
  }
  if (!m) return <p className="muted">Đang tải…</p>;
  return (
    <div data-testid={tid('e1', 'monitor')}>
      <h2>
        Theo dõi lớp {m.className} · {m.problemCode}
      </h2>
      <p>
        <span className="chip" data-testid={tid('e1', 'monitor', 'count')} data-count={m.studentCount}>
          {m.studentCount} học sinh · đã mở bài {m.openedCount} / {m.studentCount}
        </span>{' '}
        <span className="chip">{RELEASE_LABEL[m.releaseState] ?? m.releaseState}</span>
      </p>
      <table className="e1-table">
        <thead>
          <tr>
            <th>Học sinh</th>
            <th>Tiến độ</th>
            <th>Câu hỏi</th>
          </tr>
        </thead>
        <tbody>
          {m.students.map((s) => (
            <tr key={s.userId} data-testid={tid('e1', 'monitor', 'student', s.displayName)}>
              <td>{s.displayName}</td>
              <td>
                {s.k}/3 · {s.label}
              </td>
              <td>{s.askCount > 0 ? s.askCount : '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <section className="card">
        <h3>Câu hỏi gần đây</h3>
        {m.questions.map((q) => (
          <p key={q.id} data-testid={tid('e1', 'monitor', 'question')}>
            <b>{q.askerName}:</b> {q.question}
          </p>
        ))}
      </section>
    </div>
  );
}
