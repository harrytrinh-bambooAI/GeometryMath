import { useState } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { e4Api, isNetworkError, M } from './api.ts';
import { KwText } from './Lecture.tsx';
import type { HelpModel } from './useHelp.ts';

/**
 * Tab "Gợi ý" (US-GEO-404): "Tiến độ của em k/3", gợi ý đã xem hiện sẵn (không cần bấm lại), nút xin mức kế tiếp —
 * không có nút "Xem gợi ý 2" khi chưa xem Gợi ý 1. Giáo viên tắt gợi ý: MSG-GEO-32, không có nút. Mất mạng: MSG-GEO-08,
 * không gợi ý nào hiện, tiến độ giữ nguyên (sự kiện chỉ ghi khi máy chủ trả gợi ý).
 */
export function HintTab({ id, model }: { id: string; model: HelpModel }) {
  const { help, helpError, refresh } = model;
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  if (!help) {
    return helpError ? (
      <p role="alert" data-testid={tid('e4', 'help', 'error')}>
        {isNetworkError(helpError) ? M['08'] : M['04']}
      </p>
    ) : (
      <p className="muted">Đang tải…</p>
    );
  }
  const next = help.hints.length + 1;
  const ask = async () => {
    setBusy(true);
    setErr(null);
    try {
      await e4Api.hint(id, next);
      await refresh();
    } catch (e) {
      if (isNetworkError(e)) setErr(M['08']);
      else if (e instanceof ApiError && e.code === 'FORBIDDEN') setErr(e.message || M['34']);
      else setErr(M['14']);
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="e4-hints" data-testid={tid('e4', 'hints')}>
      <p className="e4-progress" data-testid={tid('e4', 'progress')} data-k={help.k}>
        Tiến độ của em {help.k}/3
      </p>
      {!help.allowHint ? (
        <p className="muted" data-testid={tid('e4', 'hint', 'disabled')}>
          {help.hintMessage ?? M['32']}
        </p>
      ) : (
        <>
          {help.hints.map((h) => (
            <div key={h.level} className="card e4-hint" data-testid={tid('e4', 'hint', h.level)}>
              <strong data-testid={tid('e4', 'hint', h.level, 'title')}>{h.title}</strong>
              <p>
                <KwText text={h.body} testId={tid('e4', 'hint', h.level, 'body')} />
              </p>
            </div>
          ))}
          {next <= 2 && (
            <button type="button" className="btn" disabled={busy} data-testid={tid('e4', 'hint', 'next')} onClick={() => void ask()}>
              Xem gợi ý {next}
            </button>
          )}
        </>
      )}
      {err && (
        <p className="e4-err" role="alert" data-testid={tid('e4', 'hint', 'error')}>
          {err}
        </p>
      )}
    </div>
  );
}
