import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { homeOf, saveCodeLogin } from '../../shared/auth/codeSession.ts';
import type { CodeSession } from '../../shared/auth/codeSession.ts';
import { tid } from '../../shared/testid.ts';
import { CodeBox, SavedGate } from './CodeBox.tsx';
import { MSG, checkPassword, codeOf, fieldErrorOf, messageOf, s2e1Api } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * C3-01 "Lấy lại tài khoản bằng mã khôi phục" (US-S2E1-03): mã người dùng + mã khôi phục + mật khẩu mới. Xong thì
 * mã cũ hết hiệu lực, hiện mã khôi phục MỚI một lần; tick "đã lưu" rồi vào luôn bằng mật khẩu mới.
 */
export function RecoverPage({ area }: { area: 'gv' | 'hs' }) {
  const navigate = useNavigate();
  const [userCode, setUserCode] = useState('');
  const [recovery, setRecovery] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErr, setFieldErr] = useState<{ userCode?: string; recovery?: string; password?: string }>({});
  const [error, setError] = useState<{ text: string; code: string } | null>(null);
  const [busy, setBusy] = useState(false);
  const inFlight = useRef(false);
  const [done, setDone] = useState<{ userCode: string; recoveryCode: string; role: 'TEACHER' | 'STUDENT' } | null>(null);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (inFlight.current) return;
    const f: typeof fieldErr = {};
    if (!userCode.trim()) f.userCode = MSG.codeEmpty;
    if (!recovery.trim()) f.recovery = MSG.recoveryEmpty;
    const p = checkPassword(password);
    if (p) f.password = p;
    setFieldErr(f);
    setError(null);
    if (Object.keys(f).length) return;
    inFlight.current = true;
    setBusy(true);
    try {
      setDone(await s2e1Api.recover(userCode, recovery, password));
    } catch (e) {
      const pw = fieldErrorOf(e, 'newPassword');
      if (pw) setFieldErr({ password: pw });
      else setError({ text: messageOf(e), code: codeOf(e) });
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  const enter = async () => {
    if (!done || inFlight.current) return;
    inFlight.current = true;
    setBusy(true);
    try {
      const r = await s2e1Api.login(done.userCode, password);
      saveCodeLogin(r as CodeSession);
      navigate(homeOf(r.role), { replace: true });
    } catch (e) {
      setError({ text: messageOf(e), code: codeOf(e) });
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  if (done) {
    const teacher = done.role === 'TEACHER';
    return (
      <div className="s2e1-auth" data-testid={tid('s2e1', 'recover', 'done')}>
        <div className="card s2e1-card">
          <h2>Đã đặt mật khẩu mới</h2>
          <div className="s2e1-ban grn">Mật khẩu mới đã dùng được. Mã khôi phục cũ hết hiệu lực.</div>
          <CodeBox label="Mã khôi phục MỚI · chỉ hiện một lần" code={done.recoveryCode} name="new-recovery-code" />
          {error ? (
            <div className="alert" role="alert" data-testid={tid('s2e1', 'recover', 'error')}>
              <MathText text={error.text} />
            </div>
          ) : null}
          <SavedGate
            label={teacher ? 'Tôi đã lưu mã khôi phục mới' : 'Em đã lưu mã khôi phục mới'}
            action={teacher ? 'Vào trang giáo viên' : 'Vào Bài của em'}
            onContinue={enter}
            busy={busy}
          />
        </div>
      </div>
    );
  }

  const locked = error?.code === 'RATE_LIMITED';
  return (
    <div className="s2e1-auth" data-testid={tid('s2e1', 'recover')}>
      <form className="card s2e1-card" onSubmit={submit} noValidate>
        <h2>Lấy lại tài khoản bằng mã khôi phục</h2>
        <label className="s2e1-field">
          <span>Mã người dùng</span>
          <input
            name="userCode"
            autoComplete="username"
            autoCapitalize="characters"
            spellCheck={false}
            value={userCode}
            onChange={(e) => setUserCode(e.target.value)}
            data-testid={tid('s2e1', 'recover', 'user-code')}
          />
          {fieldErr.userCode ? <span className="s2e1-err">{fieldErr.userCode}</span> : null}
        </label>
        <label className="s2e1-field">
          <span>Mã khôi phục</span>
          <input
            name="recoveryCode"
            autoComplete="off"
            autoCapitalize="characters"
            spellCheck={false}
            placeholder="RC-xxxx-xxxx-xxxx"
            value={recovery}
            aria-invalid={!!error && !locked}
            onChange={(e) => setRecovery(e.target.value)}
            data-testid={tid('s2e1', 'recover', 'code')}
          />
          {fieldErr.recovery ? <span className="s2e1-err">{fieldErr.recovery}</span> : null}
        </label>
        <label className="s2e1-field">
          <span>Mật khẩu mới (8 đến 64 ký tự)</span>
          <input
            name="newPassword"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            data-testid={tid('s2e1', 'recover', 'password')}
          />
          {fieldErr.password ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'recover', 'password', 'error')}>
              {fieldErr.password}
            </span>
          ) : null}
        </label>
        {error ? (
          <div className={locked ? 's2e1-ban red' : 'alert'} role="alert" data-testid={tid('s2e1', 'recover', 'error')} data-error-code={error.code}><Icon name={locked ? 'lock' : 'warn'} inline />
            <MathText text={error.text} />
          </div>
        ) : null}
        <button type="submit" className="btn s2e1-wide" disabled={busy} data-testid={tid('s2e1', 'recover', 'submit')}>
          Đặt lại
        </button>
        <div className="s2e1-foot">
          Chưa nhớ mã? Em đã vào lớp thì nhờ giáo viên của lớp đặt lại.{' '}
          <Link to={`/${area}/dang-nhap-ma`} className="s2e1-link" data-testid={tid('s2e1', 'recover', 'to-login')}>
            Về đăng nhập
          </Link>
        </div>
      </form>
    </div>
  );
}
