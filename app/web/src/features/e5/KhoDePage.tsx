import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { getMe } from '../e1/e1Session.ts';
import { errorText } from '../e1/messages.ts';
import type { ClassItem } from '../e1/studentApi.ts';
import { e5Api } from './api.ts';
import type { CatalogFilter, CatalogItem, CatalogSummary } from './api.ts';
import { authoredLine, levelLabel, LEVELS, SolidThumb, SOLIDS, TOPICS } from './labels.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon } from '../../shared/ui/icons.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/** MSG-GEO-40 (05_dictionary.md). */
export const MSG_40 = 'Không có đề nào khớp bộ lọc. Hãy bỏ bớt điều kiện.';

const ALL: CatalogFilter = { level: '', solid: '', topic: '' };

/**
 * Màn "Kho đề" (US-GEO-501, 502, 505; thay bản tối thiểu của E1 và giữ các data-testid `e1-…` mà ca E1 dùng).
 * Dòng đầu trang lấy từ bộ đếm của kho, không đổi theo bộ lọc (BR-GEO-203); ba nhóm lọc chọn một giá trị (BR-GEO-202);
 * danh sách tải theo trang bằng con trỏ, đổi bộ lọc thì làm lại từ đầu (US-GEO-505-FC2). Không có tìm kiếm, "Thêm đề",
 * soạn hay import (BR-GEO-205).
 */
export function KhoDePage() {
  const me = getMe();
  const [filter, setFilter] = useState<CatalogFilter>(ALL);
  const [items, setItems] = useState<CatalogItem[]>([]);
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const [summary, setSummary] = useState<CatalogSummary | null>(null);
  const [classes, setClasses] = useState<ClassItem[] | null>(null);
  const seq = useRef(0); // chỉ nhận phản hồi của lần tải mới nhất (đổi bộ lọc giữa chừng)

  /** Tải một trang; mọi setState nằm trong callback của lời hứa. */
  const fetchPage = useCallback((f: CatalogFilter, after: string | null) => {
    const my = ++seq.current;
    e5Api
      .list(f, after)
      .then((p) => {
        if (my !== seq.current) return;
        setItems((prev) => (after ? [...prev, ...p.items] : p.items));
        setCursor(p.nextCursor);
        setHasMore(p.hasMore);
      })
      .catch((e: unknown) => {
        if (my !== seq.current) return;
        setError(e);
        if (e instanceof ApiError && e.code === 'INVALID_CURSOR') setHasMore(false);
      })
      .finally(() => my === seq.current && setLoading(false));
  }, []);

  useEffect(() => {
    let alive = true;
    fetchPage(ALL, null);
    e5Api
      .summary()
      .then((s) => alive && setSummary(s))
      .catch((e: unknown) => alive && setError(e));
    api
      .get<{ items: ClassItem[] }>('/api/v1/teacher/classes')
      .then((c) => alive && setClasses(c.items))
      .catch(() => alive && setClasses([]));
    return () => {
      alive = false;
    };
  }, [fetchPage]);

  /** Đổi bộ lọc: làm lại danh sách từ đầu (US-GEO-505-FC2). */
  const pick = (key: keyof CatalogFilter, value: string) => {
    const f = { ...filter, [key]: value };
    setFilter(f);
    setItems([]);
    setCursor(null);
    setHasMore(false);
    setError(null);
    setLoading(true);
    fetchPage(f, null);
  };
  const more = () => {
    setError(null);
    setLoading(true);
    fetchPage(filter, cursor);
  };
  const groups: Array<[keyof CatalogFilter, string, ReadonlyArray<readonly [string, string]>]> = [
    ['level', 'Cấp', LEVELS],
    ['solid', 'Hình', SOLIDS],
    ['topic', 'Dạng bài', TOPICS],
  ];

  return (
    <div className="e5-bank" data-testid={tid('e1', 'catalog')}>
      <div className="e1-head">
        <h2>Kho đề hình không gian</h2>
        <span className="chip" data-testid={tid('e1', 'teacher-name')}>
          {me?.displayName ?? ''}
        </span>
      </div>
      <p className="e5-summary muted" data-testid={tid('e5', 'summary')}>
        {summary ? `${summary.total} đề · ${summary.verified} đã kiểm chứng · tài liệu của ${summary.author}` : ' '}
      </p>
      <section className="card" data-testid={tid('e1', 'my-classes')}>
        <h3>Lớp của tôi</h3>
        <div className="e1-chips">
          {(classes ?? []).map((c) => (
            <span key={c.id} className="chip" data-testid={tid('e1', 'class', c.name)}>
              {c.name}
            </span>
          ))}
        </div>
      </section>
      <div className="e5-filters" data-testid={tid('e5', 'filters')}>
        {groups.map(([key, title, options]) => (
          <div key={key} className="e5-filter" role="group" aria-label={title} data-testid={tid('e5', 'filter', key)}>
            <span className="e5-filter-title">{title}</span>
            {options.map(([value, label]) => (
              <button
                key={value || 'all'}
                type="button"
                className={`e5-opt${filter[key] === value ? ' on' : ''}`}
                aria-pressed={filter[key] === value}
                data-testid={tid('e5', 'filter', key, value || 'all')}
                onClick={() => pick(key, value)}
              >
                {label}
              </button>
            ))}
          </div>
        ))}
      </div>
      {error ? (
        <div className="alert" role="alert" data-testid={tid('e5', 'error')}
          data-error-code={error instanceof ApiError ? error.code : 'UNKNOWN'}>
          {errorText(error)}
        </div>
      ) : null}
      <div className="e5-grid" data-testid={tid('e5', 'list')} data-count={items.length} data-loading={loading}>
        {items.map((p) => (
          <ProblemCard key={p.id} p={p} />
        ))}
      </div>
      {!loading && !error && items.length === 0 ? (
        <p className="e5-empty muted" data-testid={tid('e5', 'empty')}>
          {MSG_40}
        </p>
      ) : null}
      {hasMore ? (
        <p className="e5-more">
          <button type="button" className="btn ghost" disabled={loading} data-testid={tid('e5', 'load-more')}
            onClick={more}>
            Tải thêm
          </button>
        </p>
      ) : null}
    </div>
  );
}

function ProblemCard({ p }: { p: CatalogItem }) {
  const open = `/gv/de/${p.versionId}`;
  return (
    <article className="card e5-card" data-testid={tid('e1', 'problem', p.code)} data-code={p.code}
      data-level={p.level} data-solid={p.solidKind} data-topic={p.topicSlug}>
      <Link to={open} className="e5-thumb-link" aria-label={`Xem đề ${p.code}`}>
        <SolidThumb kind={p.solidKind} testId={tid('e5', 'thumb', p.code)} />
      </Link>
      <div className="e5-card-chips">
        <b>{p.code}</b>
        <span className="chip pri" data-testid={tid('e5', 'level', p.code)}>{levelLabel(p.level)}</span>
        <span className="chip" data-testid={tid('e5', 'topic', p.code)}>{p.topic}</span>
        <span className={`chip ${p.verifyStatus === 'verified' ? 'grn' : 'yel'} e5-label`} data-status={p.verifyStatus}
          data-testid={tid('e5', 'label', p.code)}><Icon name={verifyIcon(p.verifyStatus === 'verified')} />
          {p.label}
        </span>
      </div>
      <Link to={open} className="e5-statement" data-testid={tid('e5', 'open', p.code)}>
        <MathText text={p.statement} />
      </Link>
      <div className="e5-card-foot">
        <span className="tiny muted" data-testid={tid('e5', 'authored', p.code)}>{authoredLine(p.stepCount)}</span>
        <Link className="btn sm" to={`/gv/giao-bai/${p.versionId}`} data-testid={tid('e1', 'problem', p.code, 'assign')}><Icon name="assign" />
          Giao bài
        </Link>
      </div>
    </article>
  );
}
