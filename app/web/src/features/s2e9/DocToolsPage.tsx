import { useState } from 'react';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { Scene2DView } from '../../shared/scene2d/Scene2DView.tsx';
import type { Scene2D } from '../../shared/scene2d/geometry2d.ts';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { s2e9Api, shareUrl, type DocOption, type ShareLink, type VariantItem } from './api.ts';
import {
  LINK_DAYS,
  LINK_DEFAULT_DAYS,
  LINK_MAX_ACTIVE,
  projectScene,
  variantCountError,
  VARIANT_MAX,
  VARIANT_MIN,
  type ExportItem,
} from './exportModel.ts';
import { ExportPanel } from './ExportPanel.tsx';
import { Labels } from './Labels.tsx';
import { useFetch } from './useFetch.ts';
import './s2e9.css';

interface Loaded {
  option: DocOption;
  item: ExportItem;
  variants: VariantItem[];
  links: ShareLink[];
}

/** Hình của đề: đề phẳng dùng bộ vẽ 2D chung; đề không gian vẽ nét chiếu xiên từ cảnh 3D (không đáp số). */
function Figure({ item }: { item: ExportItem }) {
  if (item.sceneKind === 'plane' && item.scene) {
    return (
      <div className="s2e9-figure">
        <Scene2DView scene={item.scene as unknown as Scene2D} testId="s2e9-figure" />
      </div>
    );
  }
  if (item.sceneKind !== 'space' || !item.scene) return null;
  const { points, segments } = projectScene(item);
  const xs = [...points.values()].map((p) => p[0]);
  const ys = [...points.values()].map((p) => p[1]);
  const minX = Math.min(...xs);
  const minY = Math.min(...ys);
  const w = Math.max(1e-9, Math.max(...xs) - minX);
  const h = Math.max(1e-9, Math.max(...ys) - minY);
  const k = Math.min(300 / w, 220 / h);
  const at = (id: string) => {
    const p = points.get(id)!;
    return [30 + (p[0] - minX) * k, 250 - (p[1] - minY) * k] as const;
  };
  return (
    <div className="s2e9-figure">
      <svg className="s2e9-svg" viewBox="0 0 360 280" role="img" aria-label="Hình vẽ của đề" data-testid="s2e9-figure">
        {segments.map(([a, b]) =>
          a && b && points.has(a) && points.has(b) ? (
            <line key={`${a}-${b}`} x1={at(a)[0]} y1={at(a)[1]} x2={at(b)[0]} y2={at(b)[1]} />
          ) : null,
        )}
        {[...points.keys()].map((id) => (
          <text key={id} x={at(id)[0] + 5} y={at(id)[1] - 5}>
            {id}
          </text>
        ))}
      </svg>
    </div>
  );
}

function Variants({ docId, initial }: { docId: string; initial: VariantItem[] }) {
  const [items, setItems] = useState(initial);
  const [count, setCount] = useState('3');
  const [seed, setSeed] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const create = async () => {
    setError(null);
    setMessage(null);
    const n = Number(count);
    const bad = variantCountError(n);
    if (bad) {
      setError(bad);
      return;
    }
    const s = seed.trim() === '' ? null : Number(seed);
    if (s != null && !Number.isSafeInteger(s)) {
      setError('Hạt giống là một số nguyên');
      return;
    }
    setBusy(true);
    try {
      const batch = await s2e9Api.createVariants(docId, n, s);
      setMessage(batch.message ?? `Đã tạo ${batch.created} biến thể (hạt giống ${batch.seed})`);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Chưa tạo được biến thể. Thầy cô thử lại nhé.');
    } finally {
      // chạm giới hạn giữa chừng thì các biến thể trước vẫn được tạo: luôn tải lại danh sách
      try {
        setItems((await s2e9Api.variants(docId)).items);
      } catch {
        /* giữ danh sách cũ */
      }
      setBusy(false);
    }
  };

  return (
    <section className="card s2e9-panel" data-testid="s2e9-variants" data-count={items.length}>
      <h2 className="s2e9-h">Biến thể</h2>
      <p className="s2e9-help">Số liệu mới trong 1 đến 100, đáp số do engine tính lại. Biến thể vào "Của tôi" và chờ thầy cô duyệt.</p>
      <div className="s2e9-form">
        <label className="s2e9-field">
          Số lượng ({VARIANT_MIN} đến {VARIANT_MAX})
          <input type="number" min={VARIANT_MIN} max={VARIANT_MAX} value={count} onChange={(e) => setCount(e.target.value)} data-testid="s2e9-variant-count" />
        </label>
        <label className="s2e9-field">
          Hạt giống (để trống = ngẫu nhiên)
          <input inputMode="numeric" value={seed} onChange={(e) => setSeed(e.target.value)} data-testid="s2e9-variant-seed" />
        </label>
        <button type="button" className="btn" onClick={() => void create()} disabled={busy} data-testid="s2e9-variant-create">
          <Icon name="copy" />
          Tạo biến thể
        </button>
      </div>
      {error ? (
        <p className="alert" role="alert" data-testid="s2e9-variant-error">
          {error}
        </p>
      ) : null}
      {message ? (
        <p className="s2e9-done" role="status" data-testid="s2e9-variant-message">
          {message}
        </p>
      ) : null}
      <ul className="s2e9-list">
        {items.map((v, i) => (
          <li key={v.id} className="s2e9-row" data-testid={`s2e9-variant-${i + 1}`} data-doc={v.docId}>
            <div className="s2e9-grow">
              <Link className="s2e9-link" to={`/gv/xuat-de/de/${v.docId}`} data-testid={`s2e9-variant-open-${i + 1}`}>
                {v.title}
              </Link>
              <p className="s2e9-statement" data-math>
                {v.statement}
              </p>
              <span className="s2e9-help" data-math data-testid={`s2e9-variant-answer-${i + 1}`}>
                Đáp số (engine): {v.answer}
                {v.approx && v.answer.includes('√') ? ` ≈ ${v.approx}` : ''}
              </span>
            </div>
            <Labels machine={v.machineLabel} human={v.humanLabel} testPrefix={`s2e9-variant-${i + 1}`} />
          </li>
        ))}
      </ul>
    </section>
  );
}

function ShareLinks({ docId, initial }: { docId: string; initial: ShareLink[] }) {
  const [links, setLinks] = useState(initial);
  const [days, setDays] = useState<number>(LINK_DEFAULT_DAYS);
  const [created, setCreated] = useState<ShareLink | null>(null);
  const [copied, setCopied] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fail = (e: unknown) => setError(e instanceof ApiError ? e.message : 'Chưa làm được. Thầy cô thử lại nhé.');
  const create = async () => {
    setBusy(true);
    setError(null);
    setCopied(false);
    try {
      const link = await s2e9Api.createLink(docId, days);
      setCreated(link);
      setLinks((all) => [link, ...all]);
    } catch (e) {
      fail(e);
    } finally {
      setBusy(false);
    }
  };
  const revoke = async (id: string) => {
    setBusy(true);
    setError(null);
    try {
      await s2e9Api.revokeLink(id);
      setLinks((all) => all.filter((l) => l.id !== id));
      if (created?.id === id) setCreated(null);
    } catch (e) {
      fail(e);
    } finally {
      setBusy(false);
    }
  };
  const url = created?.token ? shareUrl(window.location.origin, created.token) : '';
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
    } catch {
      setError('Trình duyệt không cho sao chép. Thầy cô bôi đen địa chỉ rồi sao chép nhé.');
    }
  };

  return (
    <section className="card s2e9-panel" data-testid="s2e9-links" data-count={links.length}>
      <h2 className="s2e9-h">Liên kết chỉ xem</h2>
      <p className="s2e9-help">
        Người mở liên kết chỉ thấy chữ đề và hình, không đăng nhập, không lời giải. Tối đa {LINK_MAX_ACTIVE} liên kết đang dùng mỗi đề.
      </p>
      <div className="s2e9-form">
        <label className="s2e9-field">
          Hạn dùng
          <select value={days} onChange={(e) => setDays(Number(e.target.value))} data-testid="s2e9-link-days">
            {LINK_DAYS.map((d) => (
              <option key={d} value={d}>
                {d} ngày
              </option>
            ))}
          </select>
        </label>
        <button type="button" className="btn" onClick={() => void create()} disabled={busy} data-testid="s2e9-link-create">
          <Icon name="send" />
          Tạo liên kết chỉ xem
        </button>
      </div>
      {error ? (
        <p className="alert" role="alert" data-testid="s2e9-link-error">
          {error}
        </p>
      ) : null}
      {created?.token ? (
        <div className="s2e9-new" data-testid="s2e9-link-new" data-days={created.days}>
          <label className="s2e9-field">
            Địa chỉ (chỉ hiện lần này, hạn {created.daysLabel})
            <input readOnly value={url} onFocus={(e) => e.target.select()} data-testid="s2e9-link-url" />
          </label>
          <button type="button" className="btn ghost" onClick={() => void copy()} data-testid="s2e9-link-copy">
            <Icon name="copy" />
            Sao chép
          </button>
          {copied ? (
            <span className="s2e9-done" role="status" data-testid="s2e9-link-copied">
              Đã sao chép
            </span>
          ) : null}
        </div>
      ) : null}
      {links.length === 0 ? <p className="s2e9-help" data-testid="s2e9-link-empty">Chưa có liên kết nào đang dùng.</p> : null}
      <ul className="s2e9-list">
        {links.map((l, i) => (
          <li key={l.id} className="s2e9-row" data-testid={`s2e9-link-${i + 1}`} data-days={l.days}>
            <div className="s2e9-grow">
              <b data-testid={`s2e9-link-label-${i + 1}`}>Hạn {l.daysLabel}</b>
              <div className="s2e9-help">
                Mã kết thúc bằng …{l.hint} · hết hạn {formatVnDateTime(l.expiresAt)}
              </div>
            </div>
            <button type="button" className="btn ghost" onClick={() => void revoke(l.id)} disabled={busy} data-testid={`s2e9-link-revoke-${i + 1}`}>
              <Icon name="x" />
              Thu hồi
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}

/**
 * S2-E9 · công cụ của MỘT đề "Của tôi": hình + hai nhãn, "Tạo biến thể" (US-S2E9-01), "Xuất ảnh" / "Xuất PDF" và
 * "Tạo liên kết chỉ xem" (US-S2E9-02). Đề của người khác: máy chủ trả NOT_FOUND.
 */
export function DocToolsPage() {
  const { id = '' } = useParams();
  const [fetched, reload] = useFetch<Loaded | null>(`s2e9:doc:${id}`, async (signal) => {
    const [docs, view] = await Promise.all([s2e9Api.docs(signal), s2e9Api.exportDocs([id], false, false, signal)]);
    const option = docs.items.find((d) => d.id === id);
    const item = view.items[0];
    if (!option || !item) return null;
    const [variants, links] = await Promise.all([s2e9Api.variants(id, signal), s2e9Api.links(id, signal)]);
    return { option, item, variants: variants.items, links: links.items };
  });

  if (fetched.state === 'error') return <ErrorNotice error={fetched.error} onRetry={reload} testId="s2e9-error" />;
  if (fetched.state === 'loading') return <p className="muted" data-testid="s2e9-loading">Đang tải đề…</p>;
  if (!fetched.data) return <p className="empty" data-testid="s2e9-missing">Không tìm thấy đề này trong "Của tôi".</p>;
  const { option, item, variants, links } = fetched.data;

  return (
    <div className="s2e9-page" data-testid="s2e9-doc" data-doc={option.id} data-variant={option.variant}>
      <div className="s2e9-head">
        <Link className="btn ghost" to="/gv/xuat-de" data-testid="s2e9-back">
          <Icon name="prev" />
          Đề của tôi
        </Link>
        <h1 className="s2e9-title" data-testid="s2e9-doc-title">
          {item.title}
        </h1>
      </div>
      <div className="s2e9-cols">
        <section className="card s2e9-panel">
          <Labels machine={item.machineLabel} human={item.humanLabel} kind={item.kind} testPrefix="s2e9-doc" />
          {option.variant ? (
            <span className="chip" data-testid="s2e9-is-variant">
              <Icon name="copy" />
              Biến thể · đáp số do engine tính lại
            </span>
          ) : null}
          <p className="s2e9-statement" data-math data-testid="s2e9-doc-statement">
            {item.statement}
          </p>
          <Figure item={item} />
        </section>
        <div className="s2e9-side">
          <ExportPanel key={`x-${option.id}`} docs={[option]} png title={item.title} />
          <ShareLinks key={`l-${option.id}`} docId={option.id} initial={links} />
        </div>
      </div>
      <Variants key={option.id} docId={option.id} initial={variants} />
    </div>
  );
}
