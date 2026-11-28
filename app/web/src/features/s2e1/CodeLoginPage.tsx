import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router';
import { homeOf, saveCodeLogin } from '../../shared/auth/codeSession.ts';
import type { CodeSession } from '../../shared/auth/codeSession.ts';
import { tid } from '../../shared/testid.ts';
import { MSG, codeOf, messageOf, s2e1Api } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * C3-01 "Đăng nhập" bằng mã người dùng + mật khẩu (US-S2E1-02). Chỉ hai ô; mã không phân biệt hoa thường (máy chủ
 * chuẩn hóa). Mọi lỗi cùng một câu (BR-S2E1-08); khóa 10 lần sai/15 phút do máy chủ quyết định. Ô trống thì báo
 * cạnh ô, không gửi yêu cầu (FC4). Tài khoản chuyển từ MVP phải đặt mật khẩu mới trước (US-S2E1-04-SC4).
 */
export function CodeLoginPage({ area }: { area: 'gv' | 'hs' }) {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const state = useLocation().state as { expired?: boolean; userCode?: string } | null;
  const expired = params.get('het-phien') === '1' || state?.expired === true;
  const [code, setCode] = useState(state?.userCode ?? '');
  const [password, setPassword] = useState('');
  const [fieldErr, setFieldErr] = useState<{ code?: string; password?: string }>({});
  const [error, setError] = useState<{ text: string; code: string } | null>(null);
  const [busy, setBusy] = useState(false);
  const inFlight = useRef(false);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (inFlight.current) return;
    const f: { code?: string; password?: string } = {};
    if (!code.trim()) f.code = MSG.codeEmpty;
    if (!password) f.password = MSG.passwordEmpty;
    setFieldErr(f);
    setError(null);
    if (f.code || f.password) return;
    inFlight.current = true;
    setBusy(true);
    try {
      const r = await s2e1Api.login(code, password);
      if (r.mustChangePassword) {
        navigate(`/${r.role === 'TEACHER' ? 'gv' : 'hs'}/dat-mat-khau`, {
          state: { userCode: r.userCode, password },
        });
        return;
      }
      saveCodeLogin(r as CodeSession);
      navigate(homeOf(r.role), { replace: true });
    } catch (e) {
      setError({ text: messageOf(e), code: codeOf(e) });
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  return (
    <div className="s2e1-auth" data-testid={tid('s2e1', 'login')}>
      <form className="card s2e1-card" onSubmit={submit} noValidate>
        <h2>Đăng nhập</h2>
        {expired ? (
          <div className="alert" role="alert" data-testid={tid('s2e1', 'expired')}>
            {MSG.expired}
          </div>
        ) : null}
        <label className="s2e1-field">
          <span>Mã người dùng</span>
          <input
            name="userCode"
            autoComplete="username"
            autoCapitalize="characters"
            spellCheck={false}
            placeholder="HS-xxxx-xxxx hoặc GV-xxxx-xxxx"
            value={code}
            aria-invalid={!!fieldErr.code}
            onChange={(e) => setCode(e.target.value)}
            data-testid={tid('s2e1', 'login', 'code')}
          />
          {fieldErr.code ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'login', 'code', 'error')}>
              {fieldErr.code}
            </span>
          ) : null}
        </label>
        <label className="s2e1-field">
          <span>Mật khẩu</span>
          <input
            name="password"
            type="password"
            autoComplete="current-password"
            value={password}
            aria-invalid={!!fieldErr.password}
            onChange={(e) => setPassword(e.target.value)}
            data-testid={tid('s2e1', 'login', 'password')}
          />
          {fieldErr.password ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'login', 'password', 'error')}>
              {fieldErr.password}
            </span>
          ) : null}
        </label>
        {error ? (
          <div className="alert" role="alert" data-testid={tid('s2e1', 'login', 'error')} data-error-code={error.code}>
            <MathText text={error.text} />
          </div>
        ) : null}
        <button type="submit" className="btn s2e1-wide" disabled={busy} data-testid={tid('s2e1', 'login', 'submit')}><Icon name="login" />
          Đăng nhập
        </button>
        <div className="s2e1-help">
          <b>Quên mật khẩu?</b>{' '}
          <Link to={`/${area}/khoi-phuc`} className="s2e1-link" data-testid={tid('s2e1', 'to-recover')}>
            Dùng mã khôi phục
          </Link>
          , hoặc nhờ giáo viên của lớp đặt lại (nếu em đã vào lớp).
        </div>
        <div className="s2e1-foot">
          Chưa có tài khoản?{' '}
          <Link to={`/${area}/dang-ky`} className="s2e1-link" data-testid={tid('s2e1', 'to-register')}>
            Tạo tài khoản bằng tên
          </Link>
        </div>
      </form>
    </div>
  );
}
