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
 * Màn "Vào lớp" (US-GEO-101): đúng hai ô "Tên hiển thị", "Mã lớp", nút "Vào lớp" và liên kết "Tôi là giáo viên"
 * (BR-GEO-100). Không thu email, số điện thoại, ngày sinh; thân yêu cầu chỉ có hai trường.
 */
export function JoinClassPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const expired = (location.state as { expired?: boolean } | null)?.expired === true;
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [nameError, setNameError] = useState<string | null>(null);
  const [codeError, setCodeError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    setNameError(null);
    setCodeError(null);
    setFormError(null);
    if (name.trim() === '') {
      setNameError(MSG['09']);
      return;
    }
    setBusy(true);
    try {
      const r = await api.post<LoginResponse>('/api/v1/auth/student', { displayName: name, joinCode: code }, { anonymous: true });
      saveLogin(r);
      navigate('/hs/bai-cua-em', { replace: true });
    } catch (e) {
      const n = fieldError(e, 'displayName');
      const c = fieldError(e, 'joinCode');
      if (n) setNameError(n);
      else if (c) setCodeError(c);
      else setFormError(e instanceof ApiError ? errorText(e) : 'Không kết nối được máy chủ.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="e1-auth" data-testid={tid('e1', 'join')}>
      <form className="card e1-auth-card" onSubmit={submit} noValidate>
        <h2>Vào lớp</h2>
        {expired ? (
          <div className="alert" role="alert" data-testid={tid('e1', 'session-expired')}>
            {MSG['02']}
          </div>
        ) : null}
        <label className="e1-field">
          <span>Tên hiển thị</span>
          <input
            name="displayName"
            autoComplete="off"
            value={name}
            onChange={(e) => setName(e.target.value)}
            data-testid={tid('e1', 'join', 'name')}
          />
          {nameError ? (
            <small className="e1-err" role="alert" data-testid={tid('e1', 'join', 'name', 'error')}>
              {nameError}
            </small>
          ) : null}
        </label>
        <label className="e1-field">
          <span>Mã lớp</span>
          <input
            name="joinCode"
            autoComplete="off"
            autoCapitalize="characters"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            data-testid={tid('e1', 'join', 'code')}
          />
          {codeError ? (
            <small className="e1-err" role="alert" data-testid={tid('e1', 'join', 'code', 'error')}>
              {codeError}
            </small>
          ) : null}
        </label>
        {formError ? (
          <div className="alert" role="alert" data-testid={tid('e1', 'join', 'error')}>
            {formError}
          </div>
        ) : null}
        <button type="submit" className="btn e1-wide" disabled={busy} data-testid={tid('e1', 'join', 'submit')}><Icon name="login" />
          Vào lớp
        </button>
        <Link to="/gv/dang-nhap" className="e1-link" data-testid={tid('e1', 'join', 'teacher-link')}>
          Tôi là giáo viên
        </Link>
      </form>
    </div>
  );
}
