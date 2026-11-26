import { useEffect } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router';
import type { NavItem } from '../features/index.ts';
import { UNAUTHENTICATED_EVENT } from '../shared/auth/session.ts';
import { tid } from '../shared/testid.ts';
import { syncAccountTheme } from '../shared/theme/theme.ts';
import { Icon } from '../shared/ui/Icon.tsx';

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

  // T-261126-50: theme theo tài khoản (một lần mỗi thẻ; chạy lại theo điều hướng để bắt kịp lúc vừa đăng nhập)
  const { pathname } = useLocation();
  useEffect(() => {
    void syncAccountTheme();
  }, [pathname]);

  return (
    <div className="shell" data-testid={tid(area, 'shell')}>
      <nav className="rail" aria-label={`Điều hướng ${title}`} data-testid={tid(area, 'rail')}>
        <NavLink to="/" className="logo" data-testid={tid('shell', 'home')} aria-label="Trang chủ">
          <Icon name="cube" />
        </NavLink>
        {/* BR-GEO-307 (D-GEO-129): thanh bên của giáo viên không có "Tổng quan", "Lớp học" ở MVP */}
        {area === 'hs' ? (
          <NavLink to={`/${area}`} end data-testid={tid(area, 'nav', 'overview')}>
            <Icon name="home" />
            Tổng quan
          </NavLink>
        ) : null}
        {nav.map((n) => (
          <NavLink key={n.to} to={`/${area}/${n.to}`} data-testid={n.testId}>
            <Icon name={n.icon ?? 'layers'} />
            {n.label}
          </NavLink>
        ))}
        <NavLink to={`/${area}/cai-dat`} className="rail-end" data-testid={tid(area, 'nav', 'settings')}>
          <Icon name="settings" />
          Cài đặt
        </NavLink>
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
