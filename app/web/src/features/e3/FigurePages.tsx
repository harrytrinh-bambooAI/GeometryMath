import { useEffect, useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { e3Api } from './api.ts';
import './e3.css';
import { MathText } from '../../shared/math/MathText.tsx';

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
        <p className="e3-statement"><MathText text={data.statement} /></p>
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
