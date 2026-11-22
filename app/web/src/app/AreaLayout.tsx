import { useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router';
import type { NavItem } from '../features/index.ts';
import { UNAUTHENTICATED_EVENT } from '../shared/auth/session.ts';
import { tid } from '../shared/testid.ts';

/**
 * Khung một vùng (Giáo viên `gv` / Học sinh `hs`): rail bên trái + vùng nội dung.
 * Mục rail đến từ các epic (FeatureModule.teacherNav / studentNav).
 */
export function AreaLayout({ area, title, nav }: { area: 'gv' | 'hs'; title: string; nav: NavItem[] }) {
  const navigate = useNavigate();
  useEffect(() => {
    // E1: phiên hết hạn -> về màn đăng nhập của vùng, hiện MSG-GEO-02 (US-GEO-101-FC4)
    const onUnauth = () =>
      navigate(area === 'hs' ? '/hs/vao-lop' : '/gv/dang-nhap', { replace: true, state: { expired: true } });
    window.addEventListener(UNAUTHENTICATED_EVENT, onUnauth);
    return () => window.removeEventListener(UNAUTHENTICATED_EVENT, onUnauth);
  }, [navigate, area]);

  return (
    <div className="shell" data-testid={tid(area, 'shell')}>
      <nav className="rail" aria-label={`Điều hướng ${title}`} data-testid={tid(area, 'rail')}>
        <NavLink to="/" className="logo" data-testid={tid('shell', 'home')} aria-label="Trang chủ">
          H
        </NavLink>
        <NavLink to={`/${area}`} end data-testid={tid(area, 'nav', 'overview')}>
          Tổng quan
        </NavLink>
        {nav.map((n) => (
          <NavLink key={n.to} to={`/${area}/${n.to}`} data-testid={n.testId}>
            {n.label}
          </NavLink>
        ))}
      </nav>
      <div className="main">
        <header className="top">
          <h2 data-testid={tid(area, 'title')}>{title}</h2>
        </header>
        <main className="body">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

/** Trang tổng quan khi epic chưa cắm màn nào. */
export function AreaOverview({ area, items }: { area: 'gv' | 'hs'; items: Array<{ id: string; title: string; screens: number }> }) {
  return (
    <div className="card" data-testid={tid(area, 'overview')}>
      <h3>Các phần sẽ có</h3>
      <ul>
        {items.map((it) => (
          <li key={it.id} data-testid={tid(area, 'overview', it.id)}>
            <b>{it.id.toUpperCase()}</b> — {it.title}{' '}
            <span className="chip">{it.screens > 0 ? `${it.screens} màn` : 'chưa có màn'}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
