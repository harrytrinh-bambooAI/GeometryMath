import { useEffect, useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import type { ViewerSolid } from '../../shared/viewer/types.ts';
import { e3Api } from './api.ts';
import './e3.css';

function useLoad<T>(load: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<unknown>(null);
  useEffect(() => {
    let alive = true;
    load()
      .then((d) => alive && setData(d))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, deps); // eslint-disable-line react-hooks/exhaustive-deps
  return { data, error };
}

/**
 * Hình của một đề cho giáo viên `/gv/de/:versionId/hinh` (máy tính, F-GEO-102): đủ công cụ, lời giải không khóa với
 * giáo viên; không ghi sự kiện thao tác (chỉ học sinh mới có tiến độ).
 */
export function TeacherFigurePage() {
  const { versionId = '' } = useParams();
  const { data, error } = useLoad(() => e3Api.teacherProblem(versionId), [versionId]);
  const solid = useMemo(() => (data ? fromScene(data.scene) : null), [data]);
  if (error) return <ErrorNotice error={error} testId={tid('e3', 'teacher', 'error')} />;
  if (!data || !solid) return <p className="muted">Đang tải…</p>;
  return (
    <div className="e3-solo" data-testid={tid('e3', 'teacher-figure')}>
      <header className="e3-head">
        <h2>
          {data.code} <span className="chip">{data.label}</span>
        </h2>
        <p className="e3-statement">{data.statement}</p>
      </header>
      <div className="e3-main">
        <Figure3DViewer solid={solid} lock="open" className="e3-viewer" />
      </div>
    </div>
  );
}

/** Tham số mặc định của trang hình mẫu (TC-GEO-0335: nón r = 2,5, h = 4). Đổi bằng query, ví dụ `?r=3&h=5`. */
const TEMPLATE_DEFAULTS: Record<string, Record<string, number>> = {
  cone: { r: 2.5, h: 4 },
};

/** Hình mẫu `/hs/hinh-mau/:code` (US-GEO-202/205 của E2; E3 dùng để đo trên hình không có tham số chữ). */
export function TemplateFigurePage() {
  const { code = 'cone' } = useParams();
  const [q] = useSearchParams();
  const params = useMemo(() => {
    const p: Record<string, number> = { ...(TEMPLATE_DEFAULTS[code] ?? {}) };
    for (const [k, v] of q.entries()) {
      const n = Number(v.replace(',', '.'));
      if (Number.isFinite(n)) p[k] = n;
    }
    return p;
  }, [code, q]);
  const key = JSON.stringify(params);
  const { data, error } = useLoad(() => e3Api.templateScene(code, params), [code, key]);
  const solid = useMemo(() => (data ? fromScene(data) : null), [data]);
  if (error) return <ErrorNotice error={error} testId={tid('e3', 'template', 'error')} />;
  if (!solid) return <p className="muted">Đang tải…</p>;
  return (
    <div className="e3-solo" data-testid={tid('e3', 'template-figure')}>
      <header className="e3-head">
        <h2>Hình mẫu</h2>
      </header>
      <div className="e3-main">
        <Figure3DViewer solid={solid} lock="open" className="e3-viewer" />
      </div>
    </div>
  );
}

/**
 * Lăng trụ đứng đáy chữ L (TC-GEO-0350, mặt lõm): sáu đỉnh đáy (0,0), (2,0), (2,1), (1,1), (1,2), (0,2), cao 1.
 * Không có đề nào trong kho dùng hình này nên trang chỉ có khi bật móc kiểm thử (dev / VITE_GEO_TEST_HOOKS=1).
 */
export const L_PRISM: ViewerSolid = (() => {
  const base: Array<[string, number, number]> = [
    ['A', 0, 0],
    ['B', 2, 0],
    ['C', 2, 1],
    ['D', 1, 1],
    ['E', 1, 2],
    ['F', 0, 2],
  ];
  const top = base.map(([n, x, y]) => [`${n}′`, x, y] as [string, number, number]);
  const points = [...base.map(([n, x, y]) => ({ name: n, pos: [x, y, 0] as [number, number, number] })),
    ...top.map(([n, x, y]) => ({ name: n, pos: [x, y, 1] as [number, number, number] }))];
  const names = base.map((b) => b[0]);
  const edges: Array<[string, string]> = [];
  const faces: string[][] = [names, names.map((n) => `${n}′`)];
  names.forEach((n, i) => {
    const m = names[(i + 1) % names.length]!;
    edges.push([n, m], [`${n}′`, `${m}′`], [n, `${n}′`]);
    faces.push([n, m, `${m}′`, `${n}′`]);
  });
  return { points, edges, faces };
})();

export function LPrismSandboxPage() {
  return (
    <div className="e3-solo" data-testid={tid('e3', 'sandbox')}>
      <header className="e3-head">
        <h2>Thử hình: lăng trụ đáy chữ L</h2>
      </header>
      <div className="e3-main">
        <Figure3DViewer solid={L_PRISM} lock="open" className="e3-viewer" />
      </div>
    </div>
  );
}
