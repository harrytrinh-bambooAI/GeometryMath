import { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fetchPinnedLabels, type PinnedLabels } from './api.ts';
import { TrustLabels } from './TrustLabels.tsx';
import './s2e5.css';

/**
 * Học sinh: nhãn hai trục của phiên bản lời giải ĐÃ GIAO cho bài (US-S2E5-03-SC5, SC6). Chỉ có nhãn, không có bước
 * giải, đáp số hay StepCheck; phiên bản mới hơn chưa áp dụng không hiện ở đây.
 */
export function PinnedLabelsPage() {
  const { id = '' } = useParams();
  const [data, setData] = useState<PinnedLabels | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const ctl = new AbortController();
    fetchPinnedLabels(id, ctl.signal)
      .then((d) => {
        setData(d);
        setError(null);
      })
      .catch((e: unknown) => {
        if (!(e instanceof DOMException && e.name === 'AbortError')) setError(e);
      });
    return () => ctl.abort();
  }, [id, attempt]);

  if (error) return <ErrorNotice error={error} onRetry={() => setAttempt((n) => n + 1)} testId="s2e5-student-error" />;
  if (!data) {
    return (
      <p className="muted" aria-busy="true" data-testid="s2e5-student-loading">
        Đang tải bài…
      </p>
    );
  }
  return (
    <section className="card s2e5-student" data-testid="s2e5-student" data-version={data.version}>
      <header className="s2e5-figure-head">
        <h1 className="s2e5-h">Lời giải của bài</h1>
        <TrustLabels machineLabel={data.machineLabel} machineNote={data.machineNote} humanLabel={data.humanLabel} where="student" />
      </header>
      <p className="s2e5-sub muted" data-testid="s2e5-student-note">
        Nhãn của bản lời giải thầy cô đã giao cho lớp. Lời giải mở theo giờ thầy cô đặt.
      </p>
    </section>
  );
}
