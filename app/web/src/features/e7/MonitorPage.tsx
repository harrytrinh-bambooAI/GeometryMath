import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { errorText } from '../e1/messages.ts';
import { e7Api } from './api.ts';
import type { Monitor, StudentRow } from './api.ts';
import { vnDateTime } from './format.ts';
import { LockSlot } from './LockSlot.tsx';

const MSG_52 = 'Chưa có học sinh nào mở bài.';
const MSG_53 = 'Chưa có câu hỏi nào.';

const STATUS_CLASS: Record<string, string> = {
  'Đang chờ lời giải': 'yel',
  'Tự làm, không cần gợi ý': 'grn',
  'Đã xem lời giải': 'grn',
  'Đang làm': 'pri',
  'Chưa bắt đầu': '',
};

/**
 * Theo dõi lớp đầy đủ (E7, US-GEO-701, 702; khung Design/mvp-03): đầu trang, bốn thẻ, bảng "Học sinh" A → Z (bấm tiêu
 * đề cột không sắp lại, không xếp hạng), thẻ vàng khi lời giải khóa, khối "Học sinh đang hỏi gì" (gộp câu trùng
 * nguyên văn, không AI). Không có "Nên nhắc lại trên lớp" (D-GEO-078). Dữ liệu không chứa lời giải, đáp số.
 */
export function MonitorPage() {
  const { id = '' } = useParams();
  const [m, setM] = useState<Monitor | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [open, setOpen] = useState<string | null>(null);

  const load = useCallback(() => {
    e7Api
      .monitor(id)
      .then((x) => {
        setM(x);
        setError(null);
      })
      .catch((e: unknown) => setError(e));
  }, [id]);
  useEffect(() => {
    const t = window.setTimeout(load, 0);
    return () => window.clearTimeout(t);
  }, [load]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e7', 'monitor', 'error')}>
        {errorText(error)}
      </div>
    );
  }
  if (!m) return <p className="muted">Đang tải…</p>;
  const cards: Array<[string, string, string]> = [
    ['opened', `${m.openedCount}/${m.studentCount}`, 'đã mở bài'],
    ['hints', String(m.hintUsers), 'dùng gợi ý'],
    ['questions', String(m.questionCount), 'câu hỏi'],
    ['figure', `${m.figureMinutes}′`, 'thao tác hình'],
  ];
  const mine = (s: StudentRow) => m.askedQuestions.filter((q) => q.userId === s.userId);
  return (
    <div className="e7-monitor" data-testid={tid('e7', 'monitor')}>
      <header className="e7-head">
        <h1 data-testid={tid('e7', 'title')}>Theo dõi lớp {m.className}</h1>
        <p className="muted" data-testid={tid('e7', 'subtitle')}>
          Bài: {m.problemTitle} · giao lúc {vnDateTime(m.assignedAt)}
        </p>
      </header>
      <div className="e7-layout">
        <div className="e7-left">
          <div className="e7-cards">
            {cards.map(([k, v, label]) => (
              <div key={k} className="card e7-stat" data-testid={tid('e7', 'card', k)}>
                <b>{v}</b> <span>{label}</span>
              </div>
            ))}
          </div>
          <section className="card">
            <h3>Học sinh</h3>
            {m.openedCount === 0 ? (
              <p className="empty" data-testid={tid('e7', 'empty-opened')}>
                {MSG_52}
              </p>
            ) : null}
            <div className="e7-table-wrap">
              <table className="e7-table" data-testid={tid('e7', 'students')}>
                <thead>
                  <tr>
                    {['Học sinh', 'Mở bài', 'Gợi ý 1 · 2', 'Hỏi đáp', 'Thời gian', 'Tình trạng'].map((h) => (
                      <th key={h} data-testid={tid('e7', 'col', h)}>
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {m.students.map((s) => (
                    <tr key={s.userId} data-testid={tid('e7', 'student', s.displayName)}>
                      <td data-testid={tid('e7', 'name', s.displayName)}>{s.displayName}</td>
                      <td data-testid={tid('e7', 'opened', s.displayName)}>
                        <span className={`chip ${s.opened ? 'grn' : ''}`}>{s.opened ? 'Đã mở bài' : 'Chưa mở'}</span>
                      </td>
                      <td data-testid={tid('e7', 'hints', s.displayName)} data-level={s.hintLevel}>
                        {[1, 2].map((n) => (
                          <span key={n} className={`e7-dot ${s.hintLevel >= n ? 'on' : ''}`} data-on={s.hintLevel >= n} aria-hidden="true" />
                        ))}
                        <span className="sr-only">
                          {s.k}/3 · {s.label}
                        </span>
                      </td>
                      <td data-testid={tid('e7', 'asks', s.displayName)}>
                        {s.askCount > 0 ? (
                          <button
                            type="button"
                            className="chip pri"
                            data-testid={tid('e7', 'asks-btn', s.displayName)}
                            onClick={() => setOpen(open === s.userId ? null : s.userId)}
                          >
                            {s.askCount} câu
                          </button>
                        ) : (
                          '—'
                        )}
                        {open === s.userId ? (
                          <ul className="e7-popover" data-testid={tid('e7', 'asks-list', s.displayName)}>
                            {mine(s).map((q) => (
                              <li key={q.id} data-testid={tid('e7', 'asks-item')}>
                                {q.question} <span className="small muted">{vnDateTime(q.askedAt)}</span>
                              </li>
                            ))}
                          </ul>
                        ) : null}
                      </td>
                      <td data-testid={tid('e7', 'time', s.displayName)}>{s.opened ? `${s.activeMinutes} phút` : '—'}</td>
                      <td data-testid={tid('e7', 'status', s.displayName)}>
                        <span className={`chip ${STATUS_CLASS[s.status] ?? ''}`}>{s.status}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>
        <aside className="e7-right" data-testid={tid('e7', 'right')}>
          <LockSlot assignmentId={id} releaseState={m.releaseState} releaseAt={m.releaseAt} onChanged={load} />
          <section className="card" data-testid={tid('e7', 'asking')}>
            <h3>Học sinh đang hỏi gì</h3>
            {m.questionGroups.length === 0 ? (
              <p className="empty" data-testid={tid('e7', 'asking', 'empty')}>
                {MSG_53}
              </p>
            ) : (
              m.questionGroups.map((g) => (
                <div key={g.question} className="e7-group" data-testid={tid('e7', 'group')}>
                  <b data-testid={tid('e7', 'group', 'q')}>"{g.question}"</b>
                  <p className="small muted" data-testid={tid('e7', 'group', 'meta')}>
                    {g.names.join(', ')} · {g.count} lượt
                  </p>
                </div>
              ))
            )}
          </section>
        </aside>
      </div>
    </div>
  );
}
