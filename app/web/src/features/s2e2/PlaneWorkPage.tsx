import { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { measure, type Measure, type MeasureKind } from '../../shared/scene2d/geometry2d.ts';
import { Scene2DView } from '../../shared/scene2d/Scene2DView.tsx';
import { fetchPlane, type PlaneView } from './api.ts';
import './s2e2.css';

export const MSG_LOCKED = 'Đại lượng này là đáp số, chưa đo được';
export const MSG_OFFLINE = 'Không tải được bài. Kiểm tra mạng rồi bấm Thử lại.';
const VERIFIED = 'Đã kiểm chứng';

type Load = { state: 'loading' } | { state: 'error' } | { state: 'ok'; data: PlaneView };
type Result = { key: string; error: true } | { key: string; error: false; data: PlaneView };

/**
 * C2-05 · Làm bài hình phẳng trên iPad / iPhone (US-S2E2-03). Hình ở trên, công cụ đo và gợi ý ở dưới (iPhone một
 * cột, iPad/web hai cột). Nhãn hai trục ở đầu thẻ hình (BR-S2E2-10). Đo độ dài/góc trên tọa độ của cảnh; đại lượng
 * là đáp số bị khóa tới giờ mở lời giải (máy chủ gửi danh sách khóa, không gửi giá trị — BR-S2E2-09).
 * Trạng thái đo và gợi ý đã mở giữ khi xoay máy (state của trang, không phụ thuộc kích thước).
 */
export function PlaneWorkPage() {
  const { id = '' } = useParams();
  const [result, setResult] = useState<Result | null>(null);
  const [attempt, setAttempt] = useState(0);
  const key = `${id}#${attempt}`;
  const load: Load =
    result?.key !== key ? { state: 'loading' } : result.error ? { state: 'error' } : { state: 'ok', data: result.data };
  const [tool, setTool] = useState<MeasureKind>('length');
  const [picked, setPicked] = useState<string[]>([]);
  const [measures, setMeasures] = useState<Measure[]>([]);
  const [notice, setNotice] = useState<string | null>(null);
  const [hintsOpen, setHintsOpen] = useState(0);

  useEffect(() => {
    const ctl = new AbortController();
    const k = `${id}#${attempt}`;
    fetchPlane(id, ctl.signal)
      .then((data) => setResult({ key: k, error: false, data }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setResult({ key: k, error: true });
      });
    return () => ctl.abort();
  }, [id, attempt]);

  const chooseTool = (k: MeasureKind) => {
    setTool(k);
    setPicked([]);
    setNotice(null);
  };

  const tap = (pid: string) => {
      if (load.state !== 'ok' || !load.data.scene) return;
      const need = tool === 'length' ? 2 : 3;
      const next = [...picked, pid];
      if (next.length < need) {
        setPicked(next);
        return;
      }
      setPicked([]);
      const m = measure(load.data.scene, load.data.locked, tool, next);
      if (!m) {
        setNotice(MSG_LOCKED);
        return;
      }
      setNotice(null);
      setMeasures((all) => [...all.filter((x) => x.text !== m.text), m]);
  };

  if (load.state === 'loading') {
    return (
      <div className="s2e2-page" aria-busy="true" data-testid="s2e2-loading">
        <div className="card s2e2-skel s2e2-skel-figure" />
        <div className="card s2e2-skel s2e2-skel-tools" />
        <p className="muted">Đang tải bài…</p>
      </div>
    );
  }
  if (load.state === 'error') {
    return (
      <div className="s2e2-page">
        <div className="alert" role="alert" data-testid="s2e2-error">
          {MSG_OFFLINE}{' '}
          <button type="button" className="btn" onClick={() => setAttempt((n) => n + 1)} data-testid="s2e2-retry">
            Thử lại
          </button>
        </div>
      </div>
    );
  }
  const d = load.data;
  const need = tool === 'length' ? 2 : 3;
  return (
    <div className="s2e2-page" data-testid="s2e2-page" data-code={d.code}>
      <section className="card s2e2-figure-card">
        <header className="s2e2-labels" data-testid="s2e2-labels">
          <span className={`chip ${d.machineLabel === VERIFIED ? 'grn' : 'yel'}`} data-testid="s2e2-label-machine">
            {d.machineLabel}
          </span>
          <span className="chip" data-testid="s2e2-label-human">
            {d.humanLabel}
          </span>
        </header>
        <p className="s2e2-statement" data-math data-testid="s2e2-statement">
          {d.statement}
        </p>
        {d.scene ? (
          <Scene2DView scene={d.scene} selected={picked} onTapPoint={tap} testId="s2e2-figure" />
        ) : (
          <p className="muted" data-testid="s2e2-no-figure">
            Chưa dựng được hình: {d.reason}
          </p>
        )}
      </section>
      <div className="s2e2-side">
        <section className="card s2e2-tools" data-testid="s2e2-tools">
          <h2 className="s2e2-h">Công cụ đo</h2>
          <div className="s2e2-toolbar" role="group" aria-label="Công cụ đo">
            <button
              type="button"
              className={`btn${tool === 'length' ? '' : ' ghost'}`}
              aria-pressed={tool === 'length'}
              onClick={() => chooseTool('length')}
              data-testid="s2e2-tool-length"
            >
              Đo độ dài
            </button>
            <button
              type="button"
              className={`btn${tool === 'angle' ? '' : ' ghost'}`}
              aria-pressed={tool === 'angle'}
              onClick={() => chooseTool('angle')}
              data-testid="s2e2-tool-angle"
            >
              Đo góc
            </button>
          </div>
          <p className="small muted" data-testid="s2e2-tool-help">
            {tool === 'length' ? 'Chạm hai đỉnh' : 'Chạm ba đỉnh, đỉnh của góc ở giữa'} ({picked.length}/{need})
          </p>
          {notice ? (
            <p className="s2e2-notice" role="status" data-testid="s2e2-locked">
              {notice}
            </p>
          ) : null}
          <ul className="s2e2-measures" data-testid="s2e2-measures">
            {measures.map((m) => (
              <li key={m.text} data-math data-testid={`s2e2-measure-${m.kind}-${m.of.join('')}`}>
                {m.text}
              </li>
            ))}
          </ul>
        </section>
        <section className="card s2e2-hints" data-testid="s2e2-hints">
          <h2 className="s2e2-h">Gợi ý</h2>
          <ol>
            {d.hints.slice(0, hintsOpen).map((h) => (
              <li key={h.level} data-testid={`s2e2-hint-${h.level}`}>
                {h.text}
              </li>
            ))}
          </ol>
          {hintsOpen < d.hints.length ? (
            <button
              type="button"
              className="btn ghost"
              onClick={() => setHintsOpen((n) => n + 1)}
              data-testid="s2e2-hint-open"
            >
              Mở gợi ý {hintsOpen + 1}
            </button>
          ) : null}
        </section>
      </div>
    </div>
  );
}
