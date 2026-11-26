import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { libraryApi, type Card, type KindFilter, type Shelf } from './api.ts';
import { CardLabels } from './CardLabels.tsx';
import { KIND_FILTERS, MSG } from './logic.ts';
import './s2e6.css';

type View = { state: 'loading' } | { state: 'error'; error: unknown } | { state: 'ok'; items: Card[]; next: string | null; reviewer: boolean };

/**
 * Kho đề của giáo viên (C2-01, US-S2E6-01): hai ngăn "Của tôi" (mặc định) / "Kho chung", lọc Tất cả / Không gian /
 * Phẳng; đổi ngăn đưa bộ lọc về Tất cả (QA-S2E6-02). Trang 20 đề, "Xem thêm" theo con trỏ (BR-S2E6-03).
 * Ba trạng thái: đang tải (khung giữ chỗ), trống, không có kết quả lọc (BR-S2E6-04).
 */
export function LibraryPage() {
  const [shelf, setShelf] = useState<Shelf>('mine');
  const [kind, setKind] = useState<KindFilter>('all');
  const [attempt, setAttempt] = useState(0);
  const key = `${shelf}|${kind}|${attempt}`;
  const [result, setResult] = useState<{ key: string; view: View } | null>(null);
  const [more, setMore] = useState<{ key: string; busy: boolean; error: unknown } | null>(null);
  const [adding, setAdding] = useState(false);
  const view: View = result?.key === key ? result.view : { state: 'loading' };
  const moreState = more?.key === key ? more : { busy: false, error: null };
  const setView = (v: View) => setResult({ key, view: v });
  const load = () => setAttempt((n) => n + 1);

  useEffect(() => {
    const ctl = new AbortController();
    libraryApi
      .list(shelf, kind, null, ctl.signal)
      .then((p) => setResult({ key, view: { state: 'ok', items: p.items, next: p.nextCursor ?? null, reviewer: p.reviewer } }))
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === 'AbortError') return;
        setResult({ key, view: { state: 'error', error: e } });
      });
    return () => ctl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- khóa key gồm shelf, kind, lần thử
  }, [key]);

  const pickShelf = (s: Shelf) => {
    if (s === shelf) return;
    setKind('all'); // BR-S2E6-02: đổi ngăn thì bộ lọc về "Tất cả"
    setShelf(s);
  };

  const loadMore = async () => {
    if (view.state !== 'ok' || !view.next) return;
    setMore({ key, busy: true, error: null });
    try {
      const p = await libraryApi.list(shelf, kind, view.next);
      const seen = new Set(view.items.map((c) => c.id));
      setView({ ...view, items: [...view.items, ...p.items.filter((c) => !seen.has(c.id))], next: p.nextCursor ?? null });
      setMore({ key, busy: false, error: null });
    } catch (e) {
      setMore({ key, busy: false, error: e });
    }
  };

  return (
    <div className="s2e6-page" data-testid="s2e6-page" data-shelf={shelf} data-kind={kind}>
      <div className="s2e6-head">
        <h1 className="s2e6-title">Kho đề</h1>
        {view.state === 'ok' && view.reviewer ? (
          <Link className="btn ghost" to="hang-doi-soat" data-testid="s2e6-queue-link">
            <Icon name="shield" />
            Hàng đợi soát
          </Link>
        ) : null}
        <button type="button" className="btn" data-testid="s2e6-add" onClick={() => setAdding((a) => !a)}>
          <Icon name="plus" />
          Thêm đề
        </button>
      </div>
      {adding ? <AddProblem onDone={() => { setAdding(false); load(); }} /> : null}
      <div className="s2e6-tabs" role="tablist" aria-label="Ngăn đề">
        {(['mine', 'library'] as const).map((s) => (
          <button
            key={s}
            type="button"
            role="tab"
            aria-selected={shelf === s}
            className={`s2e6-tab${shelf === s ? ' on' : ''}`}
            data-testid={`s2e6-tab-${s}`}
            onClick={() => pickShelf(s)}
          >
            <Icon name={s === 'mine' ? 'file' : 'layers'} />
            {s === 'mine' ? 'Của tôi' : 'Kho chung'}
          </button>
        ))}
      </div>
      <div className="s2e6-filters" role="radiogroup" aria-label="Loại hình" data-testid="s2e6-filter">
        {KIND_FILTERS.map((f) => (
          <button
            key={f.value}
            type="button"
            role="radio"
            aria-checked={kind === f.value}
            className={`chip s2e6-filter${kind === f.value ? ' on' : ''}`}
            data-testid={`s2e6-filter-${f.value}`}
            onClick={() => setKind(f.value)}
          >
            {f.label}
          </button>
        ))}
      </div>
      <Body view={view} shelf={shelf} kind={kind} onClear={() => setKind('all')} onReload={load} onAdd={() => setAdding(true)} />
      {view.state === 'ok' && view.next ? (
        <button type="button" className="btn ghost s2e6-more" data-testid="s2e6-more" disabled={moreState.busy} onClick={loadMore}>
          {moreState.busy ? 'Đang tải…' : 'Xem thêm'}
        </button>
      ) : null}
      {moreState.error ? <ListError error={moreState.error} onReload={load} /> : null}
    </div>
  );
}

function Body(props: { view: View; shelf: Shelf; kind: KindFilter; onClear: () => void; onReload: () => void; onAdd: () => void }) {
  const { view, shelf, kind } = props;
  if (view.state === 'loading') {
    return (
      <ul className="s2e6-list" data-testid="s2e6-skeleton" aria-busy="true">
        {[0, 1, 2].map((i) => (
          <li key={i} className="card s2e6-card s2e6-ghost" />
        ))}
      </ul>
    );
  }
  if (view.state === 'error') return <ListError error={view.error} onReload={props.onReload} />;
  if (view.items.length === 0) {
    if (kind !== 'all') {
      return (
        <div className="empty s2e6-empty" data-testid="s2e6-no-match">
          <p>{MSG.noMatch}</p>
          <button type="button" className="btn ghost" data-testid="s2e6-clear-filter" onClick={props.onClear}>
            <Icon name="x" />
            Xóa bộ lọc
          </button>
        </div>
      );
    }
    return shelf === 'mine' ? (
      <div className="empty s2e6-empty" data-testid="s2e6-empty">
        <p>{MSG.emptyMine}</p>
        <button type="button" className="btn" data-testid="s2e6-empty-add" onClick={props.onAdd}>
          <Icon name="plus" />
          Thêm đề
        </button>
      </div>
    ) : (
      <p className="empty s2e6-empty" data-testid="s2e6-empty-library">{MSG.emptyLibrary}</p>
    );
  }
  return (
    <ul className="s2e6-list" data-testid="s2e6-list">
      {view.items.map((c) => (
        <li key={c.id} className="card s2e6-card" data-testid={`s2e6-card-${c.id}`} data-kind={c.kind}>
          <Link className="s2e6-card-link" to={c.shelf === 'mine' ? `de/${c.id}` : `kho-chung/${c.id}`} data-testid="s2e6-card-title">
            {c.title}
          </Link>
          <CardLabels card={c} />
        </li>
      ))}
    </ul>
  );
}

function ListError({ error, onReload }: { error: unknown; onReload: () => void }) {
  if (error instanceof ApiError && error.code === 'INVALID_CURSOR') {
    return (
      <div className="empty s2e6-empty" data-testid="s2e6-invalid-cursor">
        <p>{MSG.invalidCursor}</p>
        <button type="button" className="btn ghost" onClick={onReload}>
          <Icon name="redo" />
          Tải lại
        </button>
      </div>
    );
  }
  return <ErrorNotice error={error} onRetry={onReload} testId="s2e6-error" />;
}

/** Thêm đề bằng dán chữ (đường đầy đủ ảnh / import thuộc S2-E3, S2-E4); bộ đếm 20 đề / 60 phút (BR-S2E6-13). */
function AddProblem({ onDone }: { onDone: () => void }) {
  const [statement, setStatement] = useState('');
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);
  const save = async () => {
    setBusy(true);
    setError(null);
    try {
      await api.post('/api/v1/teacher/library/problems', { statement });
      setStatement('');
      onDone();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="card s2e6-add" data-testid="s2e6-add-form">
      <label className="s2e6-help" htmlFor="s2e6-add-text">Dán đề bài</label>
      <textarea id="s2e6-add-text" data-math rows={3} value={statement} onChange={(e) => setStatement(e.target.value)} data-testid="s2e6-add-text" />
      <div className="s2e6-row">
        <button type="button" className="btn" disabled={busy || !statement.trim()} onClick={save} data-testid="s2e6-add-save">
          <Icon name="save" />
          Lưu vào Của tôi
        </button>
      </div>
      {error ? <ErrorNotice error={error} testId="s2e6-add-error" /> : null}
    </div>
  );
}
