import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { e4Api, isNetworkError, M } from './api.ts';
import { KwText } from './Lecture.tsx';
import { highlightLine, type HelpModel } from './useHelp.ts';

/** Thông điệp khóa của tab "Lời giải" theo trạng thái máy chủ (MSG-GEO-30, 31, 58; bài lớp: giờ mở của giáo viên). */
function lockText(state: string, message: string | null | undefined, releaseAt: string | null | undefined): string {
  if (state === 'hints') return M['30'];
  if (state === 'inherited') return M['31'];
  if (state === 'no_signature') return M['58'];
  if (releaseAt) return `Lời giải mở lúc ${formatVnDateTime(releaseAt)}.`;
  return message ?? 'Giáo viên chưa đặt giờ mở lời giải.';
}

/**
 * Tab "Lời giải" (US-GEO-405, 406, 407, 408). Bài lớp đã mở: mở tab là xem (ghi sự kiện mở lời giải đúng một lần ở máy
 * chủ). Bài tự học: bấm "Xem lời giải" sau khi đủ Gợi ý 1 và 2. Còn khóa: chỉ thông điệp, không nút, không bước hay đáp số.
 * Bấm bước → bước nổi lên, hình tô đối tượng của bước, phần vẽ thêm cộng dồn tới bước đó, khối "Vì sao nghĩ ra bước này?".
 */
export function SolutionTab({ id, model }: { id: string; model: HelpModel }) {
  const { help, refresh, solution, setSolution, step, setStep } = model;
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const open = useCallback(async () => {
    setBusy(true);
    setErr(null);
    try {
      const s = await e4Api.openSolution(id);
      setSolution(s);
      setStep(0);
      await refresh();
    } catch (e) {
      if (e instanceof ApiError && e.code === 'FORBIDDEN') await refresh();
      else setErr(isNetworkError(e) ? M['08'] : M['14']);
    } finally {
      setBusy(false);
    }
  }, [id, refresh, setSolution, setStep]);

  // Mở tab: hỏi lại trạng thái (giờ mở có thể vừa tới, giáo viên vừa giao đề trùng); bài lớp đã mở thì xem luôn.
  const state = help?.solution.state;
  const autoOpen = !solution && !busy && state === 'open' && (help?.kind === 'class' || help?.k === 3);
  useEffect(() => {
    const t = window.setTimeout(() => void refresh(), 0);
    return () => window.clearTimeout(t);
  }, [refresh]);
  useEffect(() => {
    if (!autoOpen) return;
    const t = window.setTimeout(() => void open(), 0);
    return () => window.clearTimeout(t);
  }, [autoOpen, open]);
  // Bài đang khóa: hỏi lại mỗi 5 giây (giờ mở tới, giáo viên "Mở ngay") mà không cần tải lại trang.
  useEffect(() => {
    if (!help || state === 'open') return;
    const t = window.setInterval(() => void refresh(), 5000);
    return () => window.clearInterval(t);
  }, [help, state, refresh]);

  if (!help) return <p className="muted">Đang tải…</p>;
  if (state !== 'open') {
    return (
      <div data-testid={tid('e4', 'solution')} data-state={state}>
        <p className="e4-lock" data-testid={tid('e4', 'solution', 'locked')} data-state={state}>
          {lockText(state ?? 'release', help.solution.message, help.solution.releaseAt)}
        </p>
      </div>
    );
  }
  if (!solution) {
    return (
      <div data-testid={tid('e4', 'solution')} data-state="open">
        {help.kind === 'self' && (
          <button type="button" className="btn primary" disabled={busy} data-testid={tid('e4', 'solution', 'view')}
            onClick={() => void open()}>
            Xem lời giải
          </button>
        )}
        {busy && <p className="muted">Đang tải…</p>}
        {err && (
          <p role="alert" className="e4-err" data-testid={tid('e4', 'solution', 'error')}>
            {err}
          </p>
        )}
      </div>
    );
  }
  const cur = solution.steps[step];
  const line = highlightLine(cur?.highlight);
  const last = solution.steps.length - 1;
  return (
    <div className="e4-solution" data-testid={tid('e4', 'solution')} data-state="open" data-step={step + 1}>
      <div className="row e4-labels">
        <span className={`chip ${solution.machineLabel === 'Đã kiểm chứng' ? 'grn' : 'yel'}`} data-testid={tid('e4', 'solution', 'label', 'machine')}>
          {solution.machineLabel}
        </span>
        <span className="chip pur" data-testid={tid('e4', 'solution', 'label', 'human')}>
          {solution.humanLabel}
        </span>
      </div>
      <ol className="e4-steps" data-testid={tid('e4', 'steps')}>
        {solution.steps.map((s, i) => (
          <li key={i}>
            <button type="button" className={`e4-step${i === step ? ' on' : ''}`} aria-current={i === step ? 'step' : undefined}
              data-testid={tid('e4', 'step', i + 1)} onClick={() => setStep(i)}>
              Bước {i + 1}: {s.title}
            </button>
            {i === step && (
              <div className="card e4-step-card" data-testid={tid('e4', 'step', 'card')}>
                <p>
                  <KwText text={s.body} testId={tid('e4', 'step', 'body')} />
                </p>
                {s.why && (
                  <div className="e4-why" data-testid={tid('e4', 'step', 'why')}>
                    <strong>Vì sao nghĩ ra bước này?</strong>
                    <p>{s.why}</p>
                  </div>
                )}
              </div>
            )}
          </li>
        ))}
      </ol>
      {line && (
        <p className="small" data-testid={tid('e4', 'step', 'highlight')}>
          Đang tô trên hình: {line}
        </p>
      )}
      <div className="row">
        <button type="button" className="btn ghost" disabled={step === 0} data-testid={tid('e4', 'step', 'prev')}
          onClick={() => setStep(Math.max(0, step - 1))}>
          Bước trước
        </button>
        <button type="button" className="btn ghost" disabled={step === last} data-testid={tid('e4', 'step', 'next')}
          onClick={() => setStep(Math.min(last, step + 1))}>
          Bước sau
        </button>
      </div>
      <p data-testid={tid('e4', 'solution', 'answer')}>
        Đáp số: <strong>{solution.finalAnswer}</strong>
      </p>
    </div>
  );
}
