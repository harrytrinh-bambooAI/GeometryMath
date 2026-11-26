import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router';
import { homeOf, saveCodeLogin } from '../../shared/auth/codeSession.ts';
import type { CodeSession } from '../../shared/auth/codeSession.ts';
import { tid } from '../../shared/testid.ts';
import { checkPassword, fieldErrorOf, messageOf, s2e1Api } from './api.ts';

/**
 * "Đặt mật khẩu mới" cho tài khoản chuyển từ MVP (US-S2E1-04-SC4, BR-S2E1-15): đăng nhập bằng mật khẩu tạm xong
 * phải đặt mật khẩu mới trước khi vào "Bài của em". Mật khẩu tạm chỉ giữ trong bộ nhớ của màn (state điều hướng).
 */
export function FirstPasswordPage({ area }: { area: 'gv' | 'hs' }) {
  const navigate = useNavigate();
  const state = useLocation().state as { userCode?: string; password?: string } | null;
  const [pw, setPw] = useState('');
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const inFlight = useRef(false);

  if (!state?.userCode || !state.password) return <Navigate to={`/${area}/dang-nhap-ma`} replace />;
  const { userCode, password } = state;

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (inFlight.current) return;
    const p = checkPassword(pw);
    setErr(p);
    if (p) return;
    inFlight.current = true;
    setBusy(true);
    try {
      const r = await s2e1Api.firstPassword(userCode, password, pw);
      saveCodeLogin(r as CodeSession);
      navigate(homeOf(r.role), { replace: true });
    } catch (e) {
      setErr(fieldErrorOf(e, 'newPassword') ?? messageOf(e));
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  return (
    <div className="s2e1-auth" data-testid={tid('s2e1', 'first-password')}>
      <form className="card s2e1-card" onSubmit={submit} noValidate>
        <h2>Đặt mật khẩu mới</h2>
        <div className="s2e1-ban pri">
          Tài khoản <b>{userCode}</b> đang dùng mật khẩu tạm. Hãy tự đặt mật khẩu mới trước khi vào học.
        </div>
        <label className="s2e1-field">
          <span>Mật khẩu mới (8 đến 64 ký tự)</span>
          <input
            name="newPassword"
            type="password"
            autoComplete="new-password"
            value={pw}
            onChange={(e) => setPw(e.target.value)}
            data-testid={tid('s2e1', 'first-password', 'input')}
          />
          {err ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'first-password', 'error')}>
              {err}
            </span>
          ) : null}
        </label>
        <button type="submit" className="btn s2e1-wide" disabled={busy} data-testid={tid('s2e1', 'first-password', 'submit')}>
          Lưu mật khẩu và vào học
        </button>
      </form>
    </div>
  );
}
