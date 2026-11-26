import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router';
import type { Scene, SolidTemplate } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { e2Api } from '../e2/api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/**
 * Khối "Tự khám phá" ở "Bài của em" (US-GEO-403-SC1, SC2; khung mvp-04): hình mẫu nhỏ (bấm mở màn khám phá) và nút
 * "Mở chế độ tự học".
 */
export function ExploreBlock() {
  const [templates, setTemplates] = useState<SolidTemplate[]>([]);
  useEffect(() => {
    let alive = true;
    e2Api
      .templates()
      .then((r) => alive && setTemplates(r.items.filter((t) => t.card)))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, []);
  return (
    <section className="card e4-explore-block" data-testid={tid('e4', 'explore', 'block')}>
      <h3>Tự khám phá</h3>
      <div className="row e4-explore-cards">
        {templates.map((t) => (
          <Link key={t.code} className="e4-explore-card" to={`/hs/kham-pha/${t.code}`} data-testid={tid('e4', 'explore', t.code)}>
            {t.name}
          </Link>
        ))}
      </div>
      <Link className="btn" to="/hs/tu-hoc" data-testid={tid('e4', 'open', 'self-study')}><Icon name="cube" />
        Mở chế độ tự học
      </Link>
    </section>
  );
}

/**
 * Màn khám phá hình mẫu `/hs/kham-pha/:code` (US-GEO-403; BR-GEO-173, D-GEO-136): chỉ có hình, ô tham số và thanh
 * công cụ; không có gợi ý, lời giải, hỏi đáp (không có đề). Engine dựng ngay, không gọi AI.
 */
export function ExplorePage() {
  const { code = 'pyramid' } = useParams();
  const [tpl, setTpl] = useState<SolidTemplate | null>(null);
  const [values, setValues] = useState<Record<string, string>>({});
  const [scene, setScene] = useState<Scene | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    e2Api
      .templates()
      .then((r) => {
        if (!alive) return;
        const t = r.items.find((x) => x.code === code) ?? null;
        setTpl(t);
        setValues(Object.fromEntries((t?.params ?? []).map((p) => [p.key, String(p.default).replace('.', ',')])));
      })
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [code]);

  const params = useMemo(() => {
    const out: Record<string, number> = {};
    for (const [k, v] of Object.entries(values)) {
      const n = Number(v.replace(',', '.'));
      if (Number.isFinite(n) && n > 0) out[k] = n;
    }
    return out;
  }, [values]);
  const key = JSON.stringify(params);

  useEffect(() => {
    if (!tpl) return;
    let alive = true;
    e2Api
      .templateScene(code, JSON.parse(key) as Record<string, number>)
      .then((s) => alive && setScene(s))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [tpl, code, key]);

  const solid = useMemo(() => (scene ? fromScene(scene) : null), [scene]);
  if (error) return <ErrorNotice error={error} testId={tid('e4', 'explore', 'error')} />;
  return (
    <div className="e3-solo e4-explore" data-testid={tid('e4', 'explore')} data-code={code}>
      <header className="e3-head">
        <h2>Khám phá: {tpl?.name ?? ''}</h2>
        <div className="row e4-params">
          {(tpl?.params ?? []).map((p) => (
            <label key={p.key} className="row">
              <span>{p.label}</span>
              <input
                inputMode="decimal"
                value={values[p.key] ?? ''}
                onChange={(e) => setValues((v) => ({ ...v, [p.key]: e.target.value }))}
                data-testid={tid('e4', 'param', p.key)}
              />
            </label>
          ))}
        </div>
      </header>
      <div className="e3-main">{solid ? <Figure3DViewer solid={solid} lock="open" className="e3-viewer" /> : <p className="muted">Đang tải…</p>}</div>
    </div>
  );
}
