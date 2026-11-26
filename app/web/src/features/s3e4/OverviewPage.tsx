import { Link } from 'react-router';
import { Icon } from '../../shared/ui/Icon.tsx';
import { dueLabel, useLoad } from '../s3e2/api.ts';
import { MSG, overviewApi, type Overview } from './api.ts';
import { badge, vnAt } from './format.ts';

/**
 * C3-05 Tổng quan giáo viên (US-S3E4-01): ba thẻ "Bài đang giao", "Lời giải đang khóa", "Em chờ duyệt". Web và điện
 * thoại dùng cùng dữ liệu; màn hẹp xếp một cột, liên kết Theo dõi nhanh ở đầu trang (BR-S3E4-04). Chỉ số, tên lớp,
 * tên bộ và giờ — không tên học sinh (BR-S3E4-02).
 */
export function OverviewPage() {
  const [load, retry] = useLoad<Overview>('s3e4-overview', (s) => overviewApi.overview(s));
  if (load.state === 'loading') return <p className="muted">Đang tải tổng quan…</p>;
  if (load.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được tổng quan.{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const o = load.data;
  const locked = o.assigned.filter((s) => s.releaseState !== 'open');
  const nothing = o.assigned.length === 0 && o.pending.total === 0;
  return (
    <div className="s3e4-page" data-testid="s3e4-overview">
      <div className="s3e4-head">
        <h1 className="s3e4-title">Tổng quan</h1>
        <span className="s3e4-grow" />
        {o.hasClasses ? (
          <Link className="btn" to="/gv/theo-doi-nhanh" data-testid="s3e4-quick-link">
            <Icon name="eye" />
            Theo dõi nhanh
          </Link>
        ) : null}
      </div>
      {!o.hasClasses ? (
        <div className="empty s3e4-empty" data-testid="s3e4-no-class">
          <p>{MSG.s66}</p>
          <Link className="btn" to="/gv/lop-hoc" data-testid="s3e4-create-class">
            <Icon name="plus" />
            Tạo lớp
          </Link>
        </div>
      ) : nothing ? (
        <p className="empty" data-testid="s3e4-nothing">
          {MSG.s65}
        </p>
      ) : (
        <div className="s3e4-cards">
          <section className="card s3e4-card" data-testid="s3e4-card-assigned" aria-label="Bài đang giao">
            <h2 className="s3e4-h2">
              <Icon name="assign" inline />
              Bài đang giao
            </h2>
            {o.assigned.length === 0 ? <p className="s3e4-help">Chưa có bộ nào đang giao.</p> : null}
            <ul className="s3e4-rows">
              {o.assigned.map((s) => (
                <li key={s.setId}>
                  <Link className="s3e4-row" to={`/gv/theo-doi-nhanh?set=${s.setId}`} data-testid="s3e4-assigned-row">
                    <span className="s3e4-grow">
                      <b>{s.title}</b> · {s.className}
                      {s.dueAt ? ` · hạn ${dueLabel(s.dueAt)}` : ''} · {badge(s.notOpenedCount)} em chưa mở
                    </span>
                    {s.overdue ? <span className="chip red">Quá hạn</span> : null}
                    {s.releaseState === 'open' ? <span className="chip grn">{MSG.s64}</span> : null}
                  </Link>
                </li>
              ))}
            </ul>
          </section>
          <section className="card s3e4-card" data-testid="s3e4-card-locked" aria-label="Lời giải đang khóa">
            <h2 className="s3e4-h2">
              <Icon name="lock" inline />
              Lời giải đang khóa
            </h2>
            {locked.length === 0 ? <p className="s3e4-help">Không có lời giải nào đang khóa.</p> : null}
            <ul className="s3e4-rows">
              {locked.map((s) => (
                <li key={s.setId}>
                  <Link className="s3e4-row" to={`/gv/theo-doi-nhanh?set=${s.setId}`} data-testid="s3e4-locked-row">
                    <span className="s3e4-grow">
                      <b>{s.title}</b> · {s.className} · {s.releaseAt ? `mở lúc ${vnAt(s.releaseAt)}` : 'Chưa đặt giờ'}
                    </span>
                    <span className="chip yel">{s.releaseAt ? 'Sắp mở' : 'Chưa đặt giờ'}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
          {o.pending.total > 0 ? (
            <section className="card s3e4-card" data-testid="s3e4-card-pending" aria-label="Em chờ duyệt">
              <h2 className="s3e4-h2">
                <Icon name="users" inline />
                Em chờ duyệt
              </h2>
              <p className="s3e4-big" data-testid="s3e4-pending-total">
                {badge(o.pending.total)}
              </p>
              <ul className="s3e4-rows">
                {o.pending.byClass.map((c) => (
                  <li key={c.classId}>
                    <Link className="s3e4-row" to={`/gv/lop-hoc/${c.classId}`} data-testid="s3e4-pending-row">
                      {c.className}: {badge(c.count)}
                    </Link>
                  </li>
                ))}
              </ul>
            </section>
          ) : null}
        </div>
      )}
    </div>
  );
}
