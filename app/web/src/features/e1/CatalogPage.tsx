import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { getMe } from './e1Session.ts';
import { errorText } from './messages.ts';
import { VERIFY_LABEL } from './studentApi.ts';
import type { CatalogProblem, ClassItem, Page } from './studentApi.ts';

/**
 * Màn "Kho đề" tối thiểu của E1 (US-GEO-102-SC1, SC2, SC3; US-GEO-104-SC3): tên giáo viên ở góc màn, mục
 * "Lớp của tôi" (chỉ lớp mình dạy), danh sách đề kho với nút "Giao bài". Lọc/phân trang đầy đủ thuộc E5.
 */
export function CatalogPage() {
  const me = getMe();
  const [problems, setProblems] = useState<CatalogProblem[] | null>(null);
  const [classes, setClasses] = useState<ClassItem[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    Promise.all([
      api.get<Page<CatalogProblem>>('/api/v1/catalog/problems', { query: { limit: 50 } }),
      api.get<{ items: ClassItem[] }>('/api/v1/teacher/classes'),
    ])
      .then(([p, c]) => {
        if (!alive) return;
        setProblems(p.items);
        setClasses(c.items);
      })
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <div data-testid={tid('e1', 'catalog')}>
      <div className="e1-head">
        <h2>Kho đề</h2>
        <span className="chip" data-testid={tid('e1', 'teacher-name')}>
          {me?.displayName ?? ''}
        </span>
      </div>
      {error ? (
        <div className="alert" role="alert" data-testid={tid('e1', 'catalog', 'error')}>
          {errorText(error)}
        </div>
      ) : null}
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
      <div className="e1-list">
        {(problems ?? []).map((p) => (
          <div key={p.id} className="card e1-row" data-testid={tid('e1', 'problem', p.code)}>
            <div className="e1-row-head">
              <b>{p.code}</b>
              <span className="chip">{p.level.toUpperCase()}</span>
              {p.topic ? <span className="chip">{p.topic}</span> : null}
              <span className="chip" data-status={p.verifyStatus}>
                {VERIFY_LABEL[p.verifyStatus]}
              </span>
            </div>
            <p className="e1-statement">{p.statement}</p>
            <Link className="btn" to={`/gv/giao-bai/${p.versionId}`} data-testid={tid('e1', 'problem', p.code, 'assign')}>
              Giao bài
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}
