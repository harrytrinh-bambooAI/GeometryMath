import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import type { Scene, SelfStudy, SolidTemplate } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { errorText, fieldError } from '../e1/messages.ts';
import { e2Api } from './api.ts';
import { SceneFrame } from './SceneFrame.tsx';
import { MSG_19, parseParam } from './sceneMath.ts';

/**
 * Màn "Tự học" (US-GEO-202, 204, 205): ô nhập đề + "Dựng hình" (cổng AI nhận ra hình; engine dựng hoặc tọa độ AI kèm
 * cảnh báo), mục "Hoặc chọn một hình mẫu" (sáu thẻ, engine dựng ngay, KHÔNG gọi AI, không màn chờ AI) với ô tham số.
 */
export function SelfStudyPage() {
  const navigate = useNavigate();
  const [statement, setStatement] = useState('');
  const [busy, setBusy] = useState(false);
  const [buildError, setBuildError] = useState<string | null>(null);
  const [templates, setTemplates] = useState<SolidTemplate[] | null>(null);
  const [picked, setPicked] = useState<SolidTemplate | null>(null);

  useEffect(() => {
    let alive = true;
    e2Api
      .templates()
      .then((r) => alive && setTemplates(r.items.filter((t) => t.card)))
      .catch(() => alive && setTemplates([]));
    return () => {
      alive = false;
    };
  }, []);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (!statement.trim()) return;
    setBusy(true);
    setBuildError(null);
    try {
      const r = await e2Api.build(statement);
      navigate(`/hs/tu-hoc/${r.assignmentId}`);
    } catch (e) {
      // đề giữ nguyên trong ô nhập (US-GEO-201-FC2, US-GEO-204-FC2)
      setBuildError(fieldError(e, 'statement') ?? errorText(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div data-testid={tid('e2', 'self-study')}>
      <h2>Tự học</h2>
      <form className="card e2-input" onSubmit={submit}>
        <label htmlFor="e2-statement">Đề bài</label>
        <textarea
          id="e2-statement"
          rows={4}
          value={statement}
          placeholder="Dán hoặc gõ đề hình học không gian…"
          onChange={(e) => setStatement(e.target.value)}
          data-testid={tid('e2', 'statement')}
        />
        {buildError ? (
          <p className="e2-field-error" role="alert" data-testid={tid('e2', 'statement', 'error')}>
            {buildError}
          </p>
        ) : null}
        <button type="submit" className="btn" disabled={busy} data-testid={tid('e2', 'build')}>
          Dựng hình
        </button>
        {busy ? (
          <span className="muted small" data-testid={tid('e2', 'ai-wait')}>
            {' '}
            Đang dựng hình…
          </span>
        ) : null}
      </form>

      <section className="card">
        <h3>Hoặc chọn một hình mẫu</h3>
        <div className="e2-cards" data-testid={tid('e2', 'templates')}>
          {(templates ?? []).map((t) => (
            <button
              key={t.code}
              type="button"
              className={`e2-card ${picked?.code === t.code ? 'on' : ''}`}
              onClick={() => setPicked(t)}
              data-testid={tid('e2', 'template', t.code)}
            >
              {t.name}
            </button>
          ))}
        </div>
        {picked ? <TemplateExplorer key={picked.code} template={picked} /> : null}
      </section>
    </div>
  );
}

/** Hình mẫu + ô tham số: số > 0, nhận dấu phẩy; sai thì MSG-GEO-19 dưới ô và hình giữ nguyên. */
function TemplateExplorer({ template }: { template: SolidTemplate }) {
  const [text, setText] = useState<Record<string, string>>(() =>
    Object.fromEntries(template.params.map((p) => [p.key, String(p.default).replace('.', ',')])),
  );
  const values = useRef<Record<string, number>>(Object.fromEntries(template.params.map((p) => [p.key, p.default])));
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [scene, setScene] = useState<Scene | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  const redraw = (params: Record<string, number>) =>
    e2Api
      .templateScene(template.code, params)
      .then((s) => {
        setScene(s);
        setLoadError(null);
      })
      .catch((e: unknown) => setLoadError(errorText(e)));

  useEffect(() => {
    void redraw(values.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [template.code]);

  const change = (key: string, raw: string) => {
    setText((t) => ({ ...t, [key]: raw }));
    const v = parseParam(raw);
    if (v === null) {
      setErrors((e) => ({ ...e, [key]: MSG_19 }));
      return; // hình giữ nguyên
    }
    setErrors((e) => {
      const n = { ...e };
      delete n[key];
      return n;
    });
    values.current = { ...values.current, [key]: v };
    void redraw(values.current);
  };

  return (
    <div className="e2-explorer" data-testid={tid('e2', 'explorer', template.code)}>
      <div className="e2-params">
        {template.params.map((p) => (
          <label key={p.key} className="e2-param">
            <span>{p.label}</span>
            <input
              inputMode="decimal"
              value={text[p.key] ?? ''}
              onChange={(e) => change(p.key, e.target.value)}
              aria-invalid={!!errors[p.key]}
              data-testid={tid('e2', 'param', p.key)}
            />
            {errors[p.key] ? (
              <span className="e2-field-error" data-testid={tid('e2', 'param', p.key, 'error')}>
                {errors[p.key]}
              </span>
            ) : null}
          </label>
        ))}
      </div>
      {loadError ? <p className="alert">{loadError}</p> : null}
      {scene ? (
        <div data-testid={tid('e2', 'scene', 'params')} data-params={JSON.stringify(scene.params)}>
          <SceneFrame scene={scene} />
        </div>
      ) : null}
    </div>
  );
}

/** Một bài tự học đã dựng: đề, hình (nhãn, cảnh báo cố định), tab Hình / Hỏi đáp. Tải lại trang vẫn như cũ. */
export function SelfStudyResultPage() {
  const { id = '' } = useParams();
  const [data, setData] = useState<SelfStudy | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [tab, setTab] = useState<'hinh' | 'hoi-dap'>('hinh');

  useEffect(() => {
    let alive = true;
    e2Api
      .selfStudy(id)
      .then((x) => alive && setData(x))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [id]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e2', 'self-study', 'error')}
        data-error-code={error instanceof ApiError ? error.code : 'UNKNOWN'}>
        {errorText(error)}
      </div>
    );
  }
  if (!data) return <p className="muted">Đang tải…</p>;
  const v = data.scene.verification;
  return (
    <div data-testid={tid('e2', 'self-study', 'result')}>
      <h2>Tự học</h2>
      <p className="e1-statement" data-testid={tid('e2', 'self-study', 'statement')}>
        {data.statement}{' '}
        <span className={`chip ${v.status === 'verified' ? 'grn' : 'yel'}`} data-testid={tid('e2', 'problem', 'label')}
          data-status={v.status}>
          {v.label}
        </span>
      </p>
      <div className="e2-tabs" role="tablist">
        <button type="button" role="tab" aria-selected={tab === 'hinh'} className={`btn ghost ${tab === 'hinh' ? 'on' : ''}`}
          onClick={() => setTab('hinh')} data-testid={tid('e2', 'tab', 'hinh')}>
          Hình
        </button>
        <button type="button" role="tab" aria-selected={tab === 'hoi-dap'} className={`btn ghost ${tab === 'hoi-dap' ? 'on' : ''}`}
          onClick={() => setTab('hoi-dap')} data-testid={tid('e2', 'tab', 'hoi-dap')}>
          Hỏi đáp
        </button>
      </div>
      {tab === 'hinh' ? (
        <SceneFrame scene={data.scene} />
      ) : (
        <section className="card" data-testid={tid('e2', 'qa')}>
          <p className="muted">Hỏi đáp cho bài tự học sẽ có ở bản đầy đủ (E4).</p>
        </section>
      )}
    </div>
  );
}
