import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { saveLogin } from './e1Session.ts';
import type { LoginResponse } from './e1Session.ts';
import { MSG, errorText, fieldError } from './messages.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/**
 * Màn "Giáo viên mẫu" (US-GEO-102): một ô "Mã giáo viên", nút "Vào". Vai giáo viên chỉ có được bằng mã
 * (BR-GEO-104). Mã sai/thu hồi/quá hạn cùng MSG-GEO-12; dò mã quá ngưỡng: MSG-GEO-59 (máy chủ quyết định).
 */
export function TeacherLoginPage() {
  const navigate = useNavigate();
  const expired = (useLocation().state as { expired?: boolean } | null)?.expired === true;
  const [code, setCode] = useState('');
  const [error, setError] = useState<{ text: string; code: string } | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const r = await api.post<LoginResponse>('/api/v1/auth/teacher', { code }, { anonymous: true });
      saveLogin(r);
      navigate('/gv/kho-de', { replace: true });
    } catch (e) {
      const apiCode = e instanceof ApiError ? e.code : 'UNKNOWN';
      const text =
        fieldError(e, 'code') ?? (apiCode === 'RATE_LIMITED' ? (e as ApiError).message || MSG['59'] : errorText(e));
      setError({ text, code: apiCode });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="e1-auth" data-testid={tid('e1', 'teacher-login')}>
      <form className="card e1-auth-card" onSubmit={submit} noValidate>
        <h2>Giáo viên mẫu</h2>
        {expired ? (
          <div className="alert" role="alert" data-testid={tid('e1', 'session-expired')}>
            {MSG['02']}
          </div>
        ) : null}
        <label className="e1-field">
          <span>Mã giáo viên</span>
          <input
            name="code"
            autoComplete="off"
            autoCapitalize="characters"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            data-testid={tid('e1', 'teacher-login', 'code')}
          />
          {error ? (
            <small className="e1-err" role="alert" data-testid={tid('e1', 'teacher-login', 'error')} data-error-code={error.code}>
              {error.text}
            </small>
          ) : null}
        </label>
        <button type="submit" className="btn e1-wide" disabled={busy} data-testid={tid('e1', 'teacher-login', 'submit')}><Icon name="login" />
          Vào
        </button>
        <Link to="/hs/vao-lop" className="e1-link" data-testid={tid('e1', 'teacher-login', 'student-link')}>
          Tôi là học sinh
        </Link>
      </form>
    </div>
  );
}
