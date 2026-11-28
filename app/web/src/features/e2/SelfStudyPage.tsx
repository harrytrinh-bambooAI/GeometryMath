import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import type { Scene, SelfStudy, SolidTemplate } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { errorText, fieldError } from '../e1/messages.ts';
import { e4Api, isAssistantError, M } from '../e4/api.ts';
import { AskTab } from '../e4/AskTab.tsx';
import { LectureProvider } from '../e4/Lecture.tsx';
import { useHelp } from '../e4/useHelp.ts';
import { e2Api } from './api.ts';
import { SceneFrame } from './SceneFrame.tsx';
import { MSG_19, parseParam } from './sceneMath.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon } from '../../shared/ui/icons.ts';
import { MathText } from '../../shared/math/MathText.tsx';

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
  // E4 (US-GEO-401, 402): cấp, đọc ảnh đề, màn chờ MSG-GEO-13, lỗi trợ lý MSG-GEO-14 + "Thử lại"
  const [level, setLevel] = useState<'thcs' | 'thpt'>('thpt');
  const [ocrBusy, setOcrBusy] = useState(false);
  const [retry, setRetry] = useState<null | 'build' | 'ocr'>(null);
  const lastImage = useRef<string | null>(null);
  const fileRef = useRef<HTMLInputElement | null>(null);

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

  const build = async () => {
    // E4: đề rỗng / quá 8000 ký tự báo ngay, không gửi (US-GEO-401-FC1, FC2)
    if (!statement.trim()) {
      setBuildError(M['20']);
      return;
    }
    if (statement.trim().length > 8000) {
      setBuildError(M['21']);
      return;
    }
    setBusy(true);
    setBuildError(null);
    setRetry(null);
    const t0 = performance.now();
    try {
      const r = await e2Api.build(statement, level);
      sessionStorage.setItem(`geo.e4.sentAt.${r.assignmentId}`, String(Date.now() - (performance.now() - t0)));
      navigate(`/hs/tu-hoc/${r.assignmentId}`);
    } catch (e) {
      // đề giữ nguyên trong ô nhập (US-GEO-201-FC2, US-GEO-204-FC2)
      if (isAssistantError(e)) {
        setBuildError(M['14']);
        setRetry('build');
      } else setBuildError(fieldError(e, 'statement') ?? errorText(e));
    } finally {
      setBusy(false);
    }
  };
  const submit = (ev: FormEvent) => {
    ev.preventDefault();
    void build();
  };
  // E4 (US-GEO-402): ảnh được đọc thành chữ, điền vào ô đề để em xem và sửa; chưa dựng hình (BR-GEO-170)
  const readImage = async (dataUrl: string) => {
    setOcrBusy(true);
    setBuildError(null);
    setRetry(null);
    try {
      const r = await e4Api.ocr(dataUrl);
      setStatement(r.statement);
    } catch (e) {
      if (isAssistantError(e)) {
        setBuildError(M['14']);
        setRetry('ocr');
      } else if (e instanceof ApiError && e.code === 'PAYLOAD_TOO_LARGE') setBuildError(M['06']);
      else if (e instanceof ApiError && e.code === 'NETWORK') setBuildError(M['08']);
      else setBuildError(fieldError(e, 'image') ?? M['22']);
    } finally {
      setOcrBusy(false);
    }
  };
  const onFile = (f: File | undefined) => {
    if (!f) return;
    const fr = new FileReader();
    fr.onload = () => {
      lastImage.current = String(fr.result);
      void readImage(lastImage.current);
    };
    fr.readAsDataURL(f);
  };

  return (
    <div data-testid={tid('e2', 'self-study')}>
      <h2>Tự học</h2>
      <form className="card e2-input" onSubmit={submit}>
        <label htmlFor="e2-statement">Đề bài của em</label>
        <div className="row">
          <select value={level} onChange={(e) => setLevel(e.target.value as 'thcs' | 'thpt')} data-testid={tid('e4', 'level')}
            aria-label="Cấp học">
            <option value="thcs">THCS</option>
            <option value="thpt">THPT</option>
          </select>
          <button type="button" className="btn ghost" disabled={busy || ocrBusy} data-testid={tid('e4', 'photo')}
            onClick={() => fileRef.current?.click()}><Icon name="camera" />
            Chụp hoặc dán ảnh đề
          </button>
          <input ref={fileRef} type="file" accept="image/*" hidden data-testid={tid('e4', 'photo', 'input')}
            onChange={(e) => {
              onFile(e.target.files?.[0]);
              e.target.value = '';
            }} />
          <span className="muted small">Ảnh sẽ được đọc thành chữ để em sửa lại</span>
        </div>
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
        <button type="submit" className="btn" disabled={busy || ocrBusy} data-testid={tid('e2', 'build')}><Icon name="cube" />
          Dựng hình
        </button>
        {busy || ocrBusy ? (
          <span className="muted small" data-testid={tid('e2', 'ai-wait')}>
            {' '}
            {M['13']}
          </span>
        ) : null}
        {retry ? (
          <button type="button" className="btn ghost" data-testid={tid('e4', 'retry')}
            onClick={() => (retry === 'ocr' && lastImage.current ? void readImage(lastImage.current) : void build())}><Icon name="redo" />
            Thử lại
          </button>
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

  // Gõ nhanh gửi nhiều yêu cầu dựng lại; phản hồi có thể về sai thứ tự (máy chủ bận). Chỉ phản hồi của yêu cầu MỚI
  // NHẤT được vẽ, phản hồi cũ về sau bị bỏ (trước đây hình có thể giữ tham số cũ, TC-GEO-0209 chập chờn trên WebKit).
  const seq = useRef(0);
  const redraw = (params: Record<string, number>) => {
    const mine = ++seq.current;
    return e2Api
      .templateScene(template.code, params)
      .then((s) => {
        if (mine !== seq.current) return;
        setScene(s);
        setLoadError(null);
      })
      .catch((e: unknown) => {
        if (mine === seq.current) setLoadError(errorText(e));
      });
  };

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
  const help = useHelp(id);

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
  void reportSceneShown(id);
  return (
    <div data-testid={tid('e2', 'self-study', 'result')}>
      <h2>Tự học</h2>
      <p className="e1-statement" data-testid={tid('e2', 'self-study', 'statement')}>
        <MathText text={data.statement} />{' '}
        <span className={`chip ${v.status === 'verified' ? 'grn' : 'yel'}`} data-testid={tid('e2', 'problem', 'label')}
          data-status={v.status}><Icon name={verifyIcon(v.status === 'verified')} />
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
      <p>
        {/* E4: gợi ý, lời giải, hỏi đáp của bài tự học trên trang Làm bài (E3) */}
        <Link className="btn primary" to={`/hs/lam-bai/${encodeURIComponent(id)}`} data-testid={tid('e4', 'open', 'workspace')}><Icon name="pencil" />
          Làm bài trên hình
        </Link>
      </p>
      {tab === 'hinh' ? (
        <SceneFrame scene={data.scene} />
      ) : (
        <section className="card" data-testid={tid('e2', 'qa')}>
          <LectureProvider>
            <AskTab id={id} model={help} />
          </LectureProvider>
        </section>
      )}
    </div>
  );
}

/**
 * E4 (BR-GEO-169, M-GEO-09): khi hình của bài tự học vừa dựng hiện lần đầu, gửi sự kiện không bền 'scene_shown' kèm thời
 * lượng từ lúc gửi đề tới lúc hình hiện (mốc gửi lưu ở sessionStorage lúc bấm "Dựng hình"; mở lại bài không gửi nữa).
 */
async function reportSceneShown(id: string): Promise<void> {
  const k = `geo.e4.sentAt.${id}`;
  const sent = Number(sessionStorage.getItem(k));
  if (!sent) return;
  sessionStorage.removeItem(k);
  await e4Api.sceneShown(id, Math.max(0, Date.now() - sent)).catch(() => undefined);
}
