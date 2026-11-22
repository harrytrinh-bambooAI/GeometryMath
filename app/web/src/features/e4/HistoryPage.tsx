import { useEffect, useState } from 'react';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import type { Page, StudentAssignment } from '../e1/studentApi.ts';
import { e4Api, isNetworkError, M, type LogItem, type SavedAnswer } from './api.ts';
import { AnswerView } from './AskTab.tsx';
import { LectureProvider } from './Lecture.tsx';

/**
 * "Lịch sử" (US-GEO-410): nhật ký hỏi đáp của chính em, mới nhất trước, lọc theo bài, "Tải thêm" theo con trỏ, không
 * tổng số (BR-GEO-195, 197). Mở một dòng hiện câu trả lời đã lưu (câu đã qua Kiểm soát đáp án) kèm dòng "Nguồn", không
 * gọi AI lại (BR-GEO-196).
 */
export function HistoryPage() {
  return (
    <LectureProvider>
      <History />
    </LectureProvider>
  );
}

function History() {
  const [filter, setFilter] = useState<string>('');
  const [options, setOptions] = useState<StudentAssignment[]>([]);
  const [items, setItems] = useState<LogItem[] | null>(null);
  const [next, setNext] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [open, setOpen] = useState<Record<number, SavedAnswer | 'loading' | 'error'>>({});

  useEffect(() => {
    let alive = true;
    api
      .get<Page<StudentAssignment>>('/api/v1/student/assignments', { query: { limit: 50 } })
      .then((p) => alive && setOptions(p.items))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, []);

  const errText = (e: unknown) =>
    e instanceof ApiError && e.code === 'INVALID_CURSOR' ? M['05'] : isNetworkError(e) ? M['08'] : M['04'];
  useEffect(() => {
    let alive = true;
    e4Api
      .log(filter || null, null)
      .then((p) => {
        if (!alive) return;
        setItems(p.items);
        setNext(p.nextCursor ?? null);
        setErr(null);
      })
      .catch((e: unknown) => alive && setErr(errText(e)));
    return () => {
      alive = false;
    };
  }, [filter]);
  const more = async (cursor: string) => {
    try {
      const p = await e4Api.log(filter || null, cursor);
      setItems((xs) => [...(xs ?? []), ...p.items]);
      setNext(p.nextCursor ?? null);
    } catch (e) {
      setErr(errText(e));
    }
  };
  const changeFilter = (v: string) => {
    setItems(null);
    setOpen({});
    setFilter(v);
  };

  const toggle = async (row: LogItem) => {
    if (open[row.id]) {
      setOpen((o) => Object.fromEntries(Object.entries(o).filter(([k]) => k !== String(row.id))));
      return;
    }
    setOpen((o) => ({ ...o, [row.id]: 'loading' }));
    try {
      const a = await e4Api.answer(row.id);
      setOpen((o) => ({ ...o, [row.id]: a }));
    } catch {
      setOpen((o) => ({ ...o, [row.id]: 'error' }));
    }
  };

  return (
    <div data-testid={tid('e4', 'history')}>
      <h2>Lịch sử hỏi đáp</h2>
      <label className="row">
        <span>Lọc theo bài</span>
        <select value={filter} onChange={(e) => changeFilter(e.target.value)} data-testid={tid('e4', 'log', 'filter')}>
          <option value="">Tất cả bài</option>
          {options.map((a) => (
            <option key={a.id} value={a.id}>
              {a.problemCode} · Lớp {a.className}
            </option>
          ))}
        </select>
      </label>
      {err && (
        <p role="alert" className="e4-err" data-testid={tid('e4', 'log', 'error')}>
          {err}
        </p>
      )}
      {items === null && !err && <p className="muted">Đang tải…</p>}
      {items && items.length === 0 && (
        <p className="empty" data-testid={tid('e4', 'log', 'empty')}>
          {M['38']}
        </p>
      )}
      <ul className="e4-log" data-testid={tid('e4', 'log', 'list')}>
        {(items ?? []).map((r) => {
          const o = open[r.id];
          return (
            <li key={r.id} className="card" data-testid={tid('e4', 'log', 'row', r.id)} data-assignment-id={r.assignmentId}>
              <button type="button" className="e4-log-head" onClick={() => void toggle(r)} data-testid={tid('e4', 'log', 'open', r.id)}>
                <span className="e4-question" data-testid={tid('e4', 'log', 'question')}>
                  {r.question}
                </span>
                <span className="small muted" data-testid={tid('e4', 'log', 'title')}>
                  {r.problemTitle}
                </span>
                <span className="small muted" data-testid={tid('e4', 'log', 'time')}>
                  {formatVnDateTime(r.askedAt)}
                </span>
              </button>
              {o === 'loading' && <p className="muted">Đang tải…</p>}
              {o === 'error' && <p className="e4-err">{M['04']}</p>}
              {o && typeof o === 'object' && <AnswerView a={o} testId={tid('e4', 'log', 'answer', r.id)} />}
            </li>
          );
        })}
      </ul>
      {next && (
        <button type="button" className="btn ghost" data-testid={tid('e4', 'log', 'more')} onClick={() => void more(next)}>
          Tải thêm
        </button>
      )}
    </div>
  );
}
