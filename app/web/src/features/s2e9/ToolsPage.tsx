import { useState } from 'react';
import { Link } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { s2e9Api, type DocOption } from './api.ts';
import { EXPORT_MAX } from './exportModel.ts';
import { ExportPanel } from './ExportPanel.tsx';
import { Labels } from './Labels.tsx';
import { useFetch } from './useFetch.ts';
import './s2e9.css';

/**
 * S2-E9 · "Xuất đề" (US-S2E9-02; chưa có khung thiết kế, QA-S2E9-01: dựng bằng component sẵn có). Danh sách đề
 * "Của tôi": tick để chọn 1…20 đề (giữ THỨ TỰ đã chọn) rồi xuất PDF; bấm tên đề để mở công cụ của đề đó
 * (biến thể, xuất ảnh, liên kết chỉ xem).
 */
export function ToolsPage() {
  const [fetched, reload] = useFetch<{ items: DocOption[] }>('s2e9:docs', (signal) => s2e9Api.docs(signal));
  const [picked, setPicked] = useState<string[]>([]);

  if (fetched.state === 'error') return <ErrorNotice error={fetched.error} onRetry={reload} testId="s2e9-error" />;
  if (fetched.state === 'loading') return <p className="muted" data-testid="s2e9-loading">Đang tải đề…</p>;
  const docs = fetched.data.items;
  const byId = new Map(docs.map((d) => [d.id, d]));
  const selected = picked.map((id) => byId.get(id)).filter((d): d is DocOption => d != null);
  const toggle = (id: string) => setPicked((all) => (all.includes(id) ? all.filter((x) => x !== id) : [...all, id]));

  return (
    <div className="s2e9-page" data-testid="s2e9-page">
      <div className="s2e9-head">
        <h1 className="s2e9-title">Xuất đề và biến thể</h1>
        <span className="chip" data-testid="s2e9-picked">
          Đã chọn {selected.length}/{EXPORT_MAX}
        </span>
      </div>
      <div className="s2e9-cols">
        <section className="card s2e9-panel">
          <h2 className="s2e9-h">Đề của tôi ({docs.length})</h2>
          {docs.length === 0 ? <p className="empty" data-testid="s2e9-empty">Chưa có đề riêng</p> : null}
          <ul className="s2e9-list">
            {docs.map((d, i) => {
              const order = picked.indexOf(d.id);
              return (
                <li key={d.id} className="s2e9-row" data-testid={`s2e9-doc-${i + 1}`} data-doc={d.id} data-picked={order >= 0}>
                  <label className="s2e9-check">
                    <input
                      type="checkbox"
                      checked={order >= 0}
                      onChange={() => toggle(d.id)}
                      aria-label={`Chọn đề ${d.title}`}
                      data-testid={`s2e9-pick-${i + 1}`}
                    />
                    {order >= 0 ? <span className="chip" data-testid={`s2e9-order-${i + 1}`}>{order + 1}</span> : null}
                  </label>
                  <div className="s2e9-grow">
                    <Link className="s2e9-link" to={`de/${d.id}`} data-testid={`s2e9-open-${i + 1}`}>
                      {d.title}
                    </Link>
                    <Labels machine={d.machineLabel} human={d.humanLabel} kind={d.kind} />
                  </div>
                  {d.variant ? (
                    <span className="chip" data-testid={`s2e9-variant-chip-${i + 1}`}>
                      <Icon name="copy" />
                      Biến thể
                    </span>
                  ) : null}
                  {d.lockedAssigned ? (
                    <span className="chip yel">
                      <Icon name="lock" />
                      Đang giao, lời giải khóa
                    </span>
                  ) : null}
                </li>
              );
            })}
          </ul>
        </section>
        <ExportPanel docs={selected} title="Phiếu đề" />
      </div>
    </div>
  );
}
