import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { getToken, signalUnauthenticated } from '../../shared/auth/session.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { docPath, presentApi, type Presentation } from './api.ts';
import { exitPresentFullscreen, fullscreenResult, requestPresentFullscreen } from './fullscreen.ts';
import { highlightOf, MSG, next, pollMs, prev, START, toggleCover, visible, type StepState } from './logic.ts';
import './s2e7.css';
import { MathText } from '../../shared/math/MathText.tsx';

type Phase =
  | { k: 'checking' }
  | { k: 'ask'; classes: string[] }
  | { k: 'loading' }
  | { k: 'offline-first' }
  | { k: 'partial' }
  | { k: 'error'; error: unknown }
  | { k: 'ready'; data: Presentation }
  | { k: 'expired' };

const isNetwork = (e: unknown) => e instanceof ApiError && e.code === 'NETWORK';

/**
 * Trình chiếu (C2-04, US-S2E7-01…03). Vào: hỏi lại nếu đề đang giao mà lời giải còn khóa (giờ máy chủ) → tải sẵn MỘT
 * lần mọi dữ liệu (đề, hình, mọi bước) vào bộ nhớ trang → chiếu được khi mất mạng. Không ghi gì vào bộ nhớ trình
 * duyệt; thoát (Esc) thì bỏ dữ liệu, về màn đề (BR-S2E7-10).
 */
export function PresentPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [phase, setPhase] = useState<Phase>({ k: 'checking' });

  const load = useCallback(async () => {
    setPhase({ k: 'loading' });
    try {
      const data = await presentApi.load(id);
      setPhase({ k: 'ready', data });
    } catch (e) {
      setPhase(isNetwork(e) ? { k: 'partial' } : { k: 'error', error: e });
    }
  }, [id]);

  const start = useCallback(async () => {
    try {
      const p = await presentApi.preflight(id);
      if (p.ask) setPhase({ k: 'ask', classes: p.lockedClasses });
      else await load();
    } catch (e) {
      setPhase(isNetwork(e) ? { k: 'offline-first' } : { k: 'error', error: e });
    }
  }, [id, load]);

  useEffect(() => {
    let live = true;
    presentApi
      .preflight(id)
      .then((p) => {
        if (!live) return;
        if (p.ask) setPhase({ k: 'ask', classes: p.lockedClasses });
        else void load();
      })
      .catch((e: unknown) => {
        if (live) setPhase(isNetwork(e) ? { k: 'offline-first' } : { k: 'error', error: e });
      });
    return () => {
      live = false;
    };
  }, [id, load]);

  const leave = useCallback(async () => {
    exitPresentFullscreen();
    setPhase({ k: 'checking' }); // bỏ dữ liệu chiếu khỏi bộ nhớ trang
    try {
      // Phiên hết hạn trong lúc mất mạng: báo SAU khi thoát chiếu, không ngắt buổi chiếu (US-S2E7-02-FC5).
      const r = await fetch(`/api/v1/teacher/present/${encodeURIComponent(id)}/preflight`, {
        headers: { Authorization: `Bearer ${getToken() ?? ''}`, Accept: 'application/json' },
        cache: 'no-store',
      });
      if (r.status === 401) {
        setPhase({ k: 'expired' });
        return;
      }
    } catch {
      // mất mạng: vẫn về màn đề
    }
    navigate(docPath(id));
  }, [id, navigate]);

  switch (phase.k) {
    case 'checking':
    case 'loading':
      return (
        <div className="s2e7-gate" data-testid="s2e7-loading">
          <p className="muted">{phase.k === 'loading' ? 'Đang tải sẵn đề, hình và lời giải…' : 'Đang kiểm tra…'}</p>
        </div>
      );
    case 'ask':
      return (
        <div className="s2e7-gate" role="dialog" aria-modal="true" aria-labelledby="s2e7-ask-title" data-testid="s2e7-ask">
          <div className="card s2e7-dialog">
            <h2 id="s2e7-ask-title" className="s2e7-ask-title">
              <Icon name="warn" />
              {MSG.ask}
            </h2>
            <p>
              Lời giải còn khóa với lớp: <b data-testid="s2e7-ask-classes">{phase.classes.join(', ')}</b>. Máy học sinh vẫn khóa.
            </p>
            <div className="s2e7-row">
              <button
                type="button"
                className="btn"
                data-testid="s2e7-ask-continue"
                onClick={() => {
                  requestPresentFullscreen();
                  void load();
                }}
              >
                <Icon name="next" />
                Tiếp tục
              </button>
              <button
                type="button"
                className="btn ghost"
                data-testid="s2e7-ask-cancel"
                onClick={() => {
                  exitPresentFullscreen();
                  navigate(docPath(id));
                }}
              >
                <Icon name="x" />
                Hủy
              </button>
            </div>
          </div>
        </div>
      );
    case 'offline-first':
    case 'partial':
      return (
        <div className="s2e7-gate" data-testid="s2e7-need-network">
          <div className="card s2e7-dialog">
            <p className="s2e7-ask-title" data-testid="s2e7-network-msg">
              <Icon name="error" />
              {phase.k === 'partial' ? MSG.partial : MSG.needNetwork}
            </p>
            <div className="s2e7-row">
              <button type="button" className="btn" data-testid="s2e7-retry" onClick={() => {
                  setPhase({ k: 'checking' });
                  void start();
                }}>
                <Icon name="redo" />
                Thử lại
              </button>
              <button type="button" className="btn ghost" onClick={() => navigate(docPath(id))}>
                <Icon name="prev" />
                Về màn đề
              </button>
            </div>
          </div>
        </div>
      );
    case 'error':
      return <ErrorNotice error={phase.error} onRetry={() => void start()} testId="s2e7-error" />;
    case 'expired':
      return (
        <div className="s2e7-gate" data-testid="s2e7-expired">
          <div className="card s2e7-dialog">
            <p className="s2e7-ask-title">
              <Icon name="login" />
              {MSG.expired}
            </p>
            <button type="button" className="btn" data-testid="s2e7-relogin" onClick={() => signalUnauthenticated()}>
              <Icon name="login" />
              Vào lại
            </button>
          </div>
        </div>
      );
    case 'ready':
      return <Stage data={phase.data} onExit={() => void leave()} />;
  }
}

function useOnline(reconnectSeconds: number): boolean {
  const [online, setOnline] = useState(() => navigator.onLine);
  useEffect(() => {
    const up = () => setOnline(true);
    const down = () => setOnline(false);
    window.addEventListener('online', up);
    window.addEventListener('offline', down);
    // Dự phòng khi trình duyệt không phát sự kiện: kiểm lại dày hơn hạn bật lại (QA-S2E7-02).
    const t = window.setInterval(() => setOnline(navigator.onLine), pollMs(reconnectSeconds));
    return () => {
      window.removeEventListener('online', up);
      window.removeEventListener('offline', down);
      window.clearInterval(t);
    };
  }, [reconnectSeconds]);
  return online;
}

function Stage({ data, onExit }: { data: Presentation; onExit: () => void }) {
  const [steps, setSteps] = useState<StepState>(START);
  const [full, setFull] = useState<'pending' | 'on' | 'fallback'>('pending');
  const [aiOpen, setAiOpen] = useState(false);
  const online = useOnline(data.reconnectSeconds);
  const total = data.steps.length;
  const shown = visible(steps);
  const solid = useMemo(() => (data.scene ? fromScene(data.scene) : null), [data.scene]);
  const hl = useMemo(() => highlightOf(shown > 0 ? data.steps[shown - 1] : undefined), [shown, data.steps]);
  const wasFull = useRef(false);
  const exitRef = useRef(onExit);
  useEffect(() => {
    exitRef.current = onExit;
  }, [onExit]);

  useEffect(() => {
    let live = true;
    void fullscreenResult().then((ok) => {
      if (!live) return;
      wasFull.current = ok;
      setFull(ok ? 'on' : 'fallback');
    });
    const onChange = () => {
      if (wasFull.current && !document.fullscreenElement) exitRef.current(); // Esc của trình duyệt thoát toàn màn hình
    };
    document.addEventListener('fullscreenchange', onChange);
    return () => {
      live = false;
      document.removeEventListener('fullscreenchange', onChange);
    };
  }, []);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement | null;
      if (target && (target.tagName === 'TEXTAREA' || target.tagName === 'INPUT')) return;
      if (e.key === 'ArrowRight' || e.key === ' ') {
        e.preventDefault();
        setSteps((s) => next(s, total));
      } else if (e.key === 'ArrowLeft') {
        e.preventDefault();
        setSteps((s) => prev(s));
      } else if (e.key === 'Escape') {
        e.preventDefault();
        wasFull.current = false;
        exitRef.current();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [total]);

  const atEnd = steps.shown >= total;
  const highlight = shown > 0 ? { edges: hl.edges, angles: hl.angles, faces: hl.faces } : undefined;

  return (
    <div
      className="s2e7-stage"
      data-testid="s2e7-stage"
      data-fullscreen={full}
      data-shown={shown}
      data-covered={steps.covered ? 'true' : 'false'}
      data-online={online ? 'true' : 'false'}
      data-highlight={shown > 0 ? [...hl.edges, ...hl.angles, ...hl.faces].join(',') : ''}
    >
      <header className="s2e7-top">
        <h1 className="s2e7-title">{data.title}</h1>
        <div className="s2e7-chips">
          <span className="chip grn" data-testid="s2e7-preloaded">
            <Icon name="check" />
            {MSG.preloaded}
          </span>
          {data.lockedClasses.length > 0 ? (
            <span className="chip yel" data-testid="s2e7-student-locked">
              <Icon name="lock" />
              {MSG.studentLocked}
            </span>
          ) : null}
        </div>
        <div className="s2e7-labels" data-testid="s2e7-labels">
          <span className={`chip ${data.machineLabel === 'Đã kiểm chứng' ? 'grn' : 'yel'}`} data-testid="s2e7-machine">
            <Icon name={data.machineLabel === 'Đã kiểm chứng' ? 'check' : 'warn'} />
            {data.machineLabel}
          </span>
          <span className={`chip ${data.humanLabel === 'Giáo viên đã duyệt' ? 'grn' : ''}`} data-testid="s2e7-human">
            <Icon name={data.humanLabel === 'Giáo viên đã duyệt' ? 'shield' : 'pencil'} />
            {data.humanLabel}
          </span>
        </div>
      </header>
      {full === 'fallback' ? (
        <p className="s2e7-note" data-testid="s2e7-no-fullscreen">
          <Icon name="info" />
          {MSG.noFullscreen}
        </p>
      ) : null}
      <div className="s2e7-body">
        <section className="s2e7-figure" data-testid="s2e7-figure">
          {solid ? (
            <Figure3DViewer solid={solid} lock="open" highlight={highlight} className="s2e7-viewer" />
          ) : (
            <div className="s2e7-figure-plain" data-testid="s2e7-figure-plain">
              <Icon name="file" />
              Hình phẳng của đề
            </div>
          )}
        </section>
        <section className="s2e7-text">
          <p className="s2e7-statement" data-math data-testid="s2e7-statement">
            <MathText text={data.statement} />
          </p>
          <ol className="s2e7-steps" data-testid="s2e7-steps">
            {data.steps.slice(0, shown).map((s, i) => (
              <li key={i} className="s2e7-step" data-math data-testid={`s2e7-step-${i + 1}`}>
                <MathText text={s} />
              </li>
            ))}
          </ol>
          {steps.covered ? (
            <p className="muted" data-testid="s2e7-covered">
              Lời giải đang che
            </p>
          ) : null}
        </section>
      </div>
      {aiOpen && online ? (
        <div className="card s2e7-ai" data-testid="s2e7-ai-panel">
          <b>Hỏi đáp AI</b>
          <p className="muted">Hỏi AI về đề này (bản thử dùng AI giả lập).</p>
        </div>
      ) : null}
      {!online ? (
        <p className="s2e7-note" data-testid="s2e7-ai-offline">
          <Icon name="warn" />
          {MSG.aiOffline}
        </p>
      ) : null}
      <nav className="s2e7-bar" aria-label="Điều khiển trình chiếu">
        <button type="button" className="btn ghost" data-testid="s2e7-prev" disabled={steps.shown <= 0} onClick={() => setSteps((s) => prev(s))}>
          <Icon name="prev" />
          Bước trước
        </button>
        <button
          type="button"
          className="btn"
          data-testid="s2e7-next"
          disabled={!data.hasSolution || atEnd}
          title={!data.hasSolution ? MSG.noSolution : atEnd ? MSG.lastStep : undefined}
          onClick={() => setSteps((s) => next(s, total))}
        >
          <Icon name="next" />
          {!data.hasSolution ? MSG.noSolution : atEnd ? MSG.lastStep : 'Hiện bước kế'}
        </button>
        <button type="button" className="btn ghost" data-testid="s2e7-cover" aria-pressed={steps.covered} onClick={() => setSteps((s) => toggleCover(s))}>
          <Icon name={steps.covered ? 'eye' : 'lock'} />
          {steps.covered ? 'Bỏ che' : 'Che lời giải'}
        </button>
        <button type="button" className="btn ghost" data-testid="s2e7-ai" disabled={!online} onClick={() => setAiOpen((o) => !o)}>
          <Icon name="spark" />
          Hỏi đáp AI
        </button>
        <button type="button" className="btn ghost" data-testid="s2e7-exit" onClick={() => { wasFull.current = false; onExit(); }}>
          <Icon name="x" />
          Thoát (Esc)
        </button>
      </nav>
    </div>
  );
}
