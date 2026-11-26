import { useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { practiceApi, type PlayView, type ResultItem, type ResultView, type SetLine, type SolutionView } from './api.ts';
import { STATUS_LABEL, useLoad } from './load.ts';
import { LoadError } from './LoadError.tsx';
import { PracticeRunner, type RunnerDriver } from './PracticeRunner.tsx';

/** Danh sách "Bài luyện" của học sinh. */
export function StudentListPage() {
  const [load, retry] = useLoad<{ items: SetLine[] }>('list', (s) => practiceApi.mySets(s));
  if (load.state === 'loading') return <p className="muted" data-testid="s2e8-loading">Đang tải bài luyện…</p>;
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="danh sách bài luyện" />;
  return (
    <div className="s2e8-page" data-testid="s2e8-student-list">
      <h1 className="s2e8-title">Bài luyện</h1>
      {load.data.items.length === 0 ? <p className="empty">Thầy cô chưa giao bài luyện nào.</p> : null}
      <ul className="s2e8-list">
        {load.data.items.map((s) => (
          <li key={s.id} className="card s2e8-row" data-testid={`s2e8-set-${s.id}`}>
            <div className="s2e8-grow">
              <b>{s.title}</b>
              <div className="s2e8-help">
                Lớp {s.className} · {s.itemCount} câu
              </div>
            </div>
            <span className={`chip ${s.status === 'closed' ? '' : 'grn'}`}>{STATUS_LABEL[s.status] ?? s.status}</span>
            <Link className="btn" to={s.status === 'closed' ? `${s.id}/ket-qua` : s.id}>
              {s.status === 'closed' ? 'Xem kết quả' : 'Làm bài'}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

const realDriver: RunnerDriver = {
  check: (item, answer, key) => practiceApi.attempt(item.assignmentId, answer, key),
  report: (item, note) => practiceApi.report(item.assignmentId, note),
  figures: true,
};

/** C2-06 · màn làm bài luyện (US-S2E8-03). Bài đã đóng thì chuyển tới màn kết quả (BR-S2E8-19). */
export function PlayPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [load, retry] = useLoad<PlayView>(`play:${id}`, (s) => practiceApi.open(id, s));
  if (load.state === 'loading') {
    return (
      <div className="s2e8-play" aria-busy="true" data-testid="s2e8-loading">
        <section className="card s2e8-stage">
          <div className="s2e8-skel s2e8-skel-line" />
          <div className="s2e8-skel s2e8-skel-figure" />
          <p className="muted">Đang tải câu hỏi và hình…</p>
        </section>
        <section className="card s2e8-side">
          <div className="s2e8-answer off" aria-disabled="true" data-testid="s2e8-answer-placeholder">
            <span className="s2e8-help">Chờ câu hỏi tải xong</span>
          </div>
          <div className="s2e8-skel s2e8-skel-kbd" data-testid="s2e8-keypad-placeholder" />
        </section>
      </div>
    );
  }
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="bài luyện" />;
  if (load.data.status === 'closed') return <Navigate to="ket-qua" replace />;
  return <PracticeRunner view={load.data} driver={realDriver} onFinish={() => void navigate('ket-qua')} />;
}

const VERIFIED = 'Đã kiểm chứng';

/** Lời giải của một câu đã tới giờ mở, kèm nhãn hai trục (US-S2E8-04-SC5). Chỉ tải khi học sinh bấm xem. */
function SolutionPanel({ item }: { item: ResultItem }) {
  const [state, setState] = useState<
    { s: 'idle' } | { s: 'loading' } | { s: 'error'; message: string } | { s: 'ok'; solution: SolutionView; machine: string; human: string }
  >({ s: 'idle' });
  const open = async () => {
    setState({ s: 'loading' });
    try {
      const [solution, labels] = await Promise.all([
        practiceApi.solution(item.assignmentId),
        practiceApi.figure(item.assignmentId).catch(() => null),
      ]);
      setState({ s: 'ok', solution, machine: labels?.machineLabel ?? 'Cần xem lại', human: labels?.humanLabel ?? 'Giáo viên đã duyệt' });
    } catch (e) {
      setState({ s: 'error', message: e instanceof ApiError ? e.message : 'Không tải được lời giải.' });
    }
  };
  if (state.s === 'idle' || state.s === 'loading') {
    return (
      <button type="button" className="btn ghost" onClick={() => void open()} disabled={state.s === 'loading'} data-testid={`s2e8-solution-open-${item.position}`}>
        Xem lời giải
      </button>
    );
  }
  if (state.s === 'error') {
    return (
      <span className="s2e8-err" role="alert">
        {state.message}
      </span>
    );
  }
  return (
    <div className="s2e8-solution" data-testid={`s2e8-solution-${item.position}`}>
      <div className="s2e8-head">
        <span className={`chip ${state.machine === VERIFIED ? 'grn' : 'yel'}`} data-testid="s2e8-label-machine">
          {state.machine}
        </span>
        <span className="chip" data-testid="s2e8-label-human">
          {state.human}
        </span>
      </div>
      <ol>
        {state.solution.steps.map((st, i) => (
          <li key={i} data-math data-testid="s2e8-solution-step">
            <b>{st.title}</b> {st.body}
          </li>
        ))}
      </ol>
      <p data-math>
        <b>Đáp số:</b> {state.solution.finalAnswer}
      </p>
    </div>
  );
}

/**
 * C2-07 · Kết quả bài luyện (US-S2E8-04): chỉ "x câu đúng · y câu cần xem lại"; không có số tổng, phần trăm hay xếp
 * hạng (BR-S2E8-21). Lời giải khóa tới giờ mở kể cả khi hết lượt; trước giờ mở trang không tải và không lưu lời giải.
 */
export function ResultPage() {
  const { id = '' } = useParams();
  const [load, retry] = useLoad<ResultView>(`result:${id}`, (s) => practiceApi.result(id, s));
  if (load.state === 'loading') return <p className="muted" data-testid="s2e8-loading">Đang tải kết quả…</p>;
  if (load.state === 'error') return <LoadError notFound={load.notFound} onRetry={retry} what="kết quả bài luyện" />;
  const r = load.data;
  return (
    <div className="s2e8-page" data-testid="s2e8-result">
      <h1 className="s2e8-title" data-testid="s2e8-result-title">
        Kết quả: {r.title}
      </h1>
      <section className="card s2e8-summary">
        <p className="s2e8-big" data-testid="s2e8-result-summary">
          {r.summary}
        </p>
        {r.pending ? (
          <p className="chip yel" data-testid="s2e8-result-pending">
            {r.pending}
          </p>
        ) : null}
        {r.note ? (
          <p className="s2e8-help" data-testid="s2e8-result-note">
            {r.note}
          </p>
        ) : null}
      </section>
      <section className="card s2e8-rows">
        <h2 className="s2e8-h">Từng câu</h2>
        <ul className="s2e8-list">
          {r.items.map((it) => (
            <li key={it.position} className="s2e8-row" data-testid={`s2e8-result-row-${it.position}`} data-state={it.state}>
              <span className="s2e8-num">Câu {it.position}</span>
              <span className={`chip ${it.state === 'correct' ? 'grn' : it.state === 'review' ? 'yel' : ''}`} data-testid="s2e8-result-state">
                {it.label}
              </span>
              <span className="chip" data-testid="s2e8-result-solution" data-solution={it.solutionState}>
                {it.solutionLabel}
              </span>
              {it.solutionState === 'open' ? <SolutionPanel item={it} /> : null}
            </li>
          ))}
        </ul>
      </section>
      {r.status === 'closed' ? (
        <p className="s2e8-help" data-testid="s2e8-result-closed">
          Bài đã đóng nên em chỉ xem được kết quả.
        </p>
      ) : r.remaining > 0 ? (
        <Link className="btn" to=".." relative="path" data-testid="s2e8-result-continue">
          Làm tiếp
        </Link>
      ) : null}
    </div>
  );
}
