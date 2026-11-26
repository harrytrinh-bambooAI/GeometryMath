import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { homeOf, saveCodeLogin } from '../../shared/auth/codeSession.ts';
import { tid } from '../../shared/testid.ts';
import { CodeBox, SavedGate } from './CodeBox.tsx';
import { checkName, checkPassword, fieldErrorOf, messageOf, s2e1Api } from './api.ts';
import type { Notice, Registered, Role } from './api.ts';

type Errors = Partial<Record<'displayName' | 'role' | 'password' | 'privacyAck' | 'form', string>>;

/**
 * C3-01 "Tạo tài khoản" (US-S2E1-01): chỉ ba trường tên hiển thị, vai, mật khẩu + tick đã đọc thông báo riêng
 * tư (PO-61, PO-64). Không hỏi email, số điện thoại, ngày sinh, trường, ảnh. Đăng ký xong hiện màn mã.
 */
export function RegisterPage({ area }: { area: 'gv' | 'hs' }) {
  const [role, setRole] = useState<Role>(area === 'gv' ? 'teacher' : 'student');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [show, setShow] = useState(false);
  const [ack, setAck] = useState(false);
  const [notice, setNotice] = useState<Notice | null>(null);
  const [showNotice, setShowNotice] = useState(false);
  const [errors, setErrors] = useState<Errors>({});
  const [busy, setBusy] = useState(false);
  const inFlight = useRef(false); // bấm Đăng ký hai lần liên tiếp chỉ gửi một yêu cầu (US-S2E1-01-FC5)
  const [done, setDone] = useState<Registered | null>(null);

  useEffect(() => {
    let live = true;
    s2e1Api
      .notice()
      .then((n) => live && setNotice(n))
      .catch(() => live && setNotice(null));
    return () => {
      live = false;
    };
  }, []);

  const version = notice?.version ?? '1.0';

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (inFlight.current || !ack) return;
    const e: Errors = {};
    const n = checkName(name);
    if (n) e.displayName = n;
    const p = checkPassword(password);
    if (p) e.password = p;
    setErrors(e);
    if (Object.keys(e).length) return;
    inFlight.current = true;
    setBusy(true);
    try {
      const r = await s2e1Api.register({ displayName: name, role, password, privacyAck: ack, privacyVersion: version });
      setDone(r);
    } catch (err) {
      const fields: Errors = {
        displayName: fieldErrorOf(err, 'displayName') ?? undefined,
        role: fieldErrorOf(err, 'role') ?? undefined,
        password: fieldErrorOf(err, 'password') ?? undefined,
        privacyAck: fieldErrorOf(err, 'privacyAck') ?? undefined,
      };
      const anyField = Object.values(fields).some(Boolean);
      setErrors({ ...fields, form: anyField ? undefined : messageOf(err) });
      inFlight.current = false;
    } finally {
      setBusy(false);
    }
  };

  if (done) return <CodesScreen r={done} />;

  return (
    <div className="s2e1-auth" data-testid={tid('s2e1', 'register')}>
      <form className="card s2e1-card" onSubmit={submit} noValidate>
        <h2>Tạo tài khoản</h2>
        <label className="s2e1-field">
          <span>Tên hiển thị</span>
          <input
            name="displayName"
            autoComplete="off"
            value={name}
            aria-invalid={!!errors.displayName}
            onChange={(e) => setName(e.target.value)}
            data-testid={tid('s2e1', 'name')}
          />
          <span className="s2e1-help">Tên thầy cô và các em thấy. Có thể trùng với người khác, mã người dùng sẽ phân biệt.</span>
          {errors.displayName ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'name', 'error')}>
              {errors.displayName}
            </span>
          ) : null}
        </label>
        <div className="s2e1-field" role="radiogroup" aria-label="Bạn là">
          <span>Bạn là</span>
          <div className="s2e1-roles">
            {(
              [
                ['teacher', 'Giáo viên'],
                ['student', 'Học sinh'],
              ] as const
            ).map(([r, label]) => (
              <button
                key={r}
                type="button"
                role="radio"
                aria-checked={role === r}
                className={`s2e1-role${role === r ? ' on' : ''}`}
                onClick={() => setRole(r)}
                data-testid={tid('s2e1', 'role', r)}
              >
                {label}
              </button>
            ))}
          </div>
          {errors.role ? <span className="s2e1-err">{errors.role}</span> : null}
        </div>
        <label className="s2e1-field">
          <span>Mật khẩu (tự đặt, 8 đến 64 ký tự)</span>
          <input
            name="password"
            type={show ? 'text' : 'password'}
            autoComplete="new-password"
            value={password}
            aria-invalid={!!errors.password}
            onChange={(e) => setPassword(e.target.value)}
            data-testid={tid('s2e1', 'password')}
          />
          <button type="button" className="s2e1-link" onClick={() => setShow(!show)} data-testid={tid('s2e1', 'password', 'show')}>
            {show ? 'Ẩn' : 'Hiện'}
          </button>
          {errors.password ? (
            <span className="s2e1-err" role="alert" data-testid={tid('s2e1', 'password', 'error')}>
              {errors.password}
            </span>
          ) : null}
        </label>
        <label className="s2e1-check">
          <input type="checkbox" checked={ack} onChange={(e) => setAck(e.target.checked)} data-testid={tid('s2e1', 'privacy')} />
          <span>
            Tôi đã đọc{' '}
            <button type="button" className="s2e1-link" onClick={() => setShowNotice(!showNotice)} data-testid={tid('s2e1', 'privacy', 'open')}>
              thông báo riêng tư
            </button>{' '}
            (bản {version})
          </span>
        </label>
        {showNotice && notice ? (
          <div className="s2e1-notice" data-testid={tid('s2e1', 'privacy', 'text')}>
            {notice.body}
          </div>
        ) : null}
        {errors.privacyAck ? <span className="s2e1-err">{errors.privacyAck}</span> : null}
        {errors.form ? (
          <div className="alert" role="alert" data-testid={tid('s2e1', 'register', 'error')}>
            {errors.form}
          </div>
        ) : null}
        <button type="submit" className="btn s2e1-wide" disabled={!ack || busy} data-testid={tid('s2e1', 'register', 'submit')}>
          Đăng ký
        </button>
        <div className="s2e1-foot">
          Đã có tài khoản?{' '}
          <Link to={`/${area}/dang-nhap-ma`} className="s2e1-link" data-testid={tid('s2e1', 'to-login')}>
            Đăng nhập bằng mã
          </Link>
        </div>
        <div className="s2e1-ban pri">
          Chúng tôi chỉ hỏi tên hiển thị. Không hỏi email, số điện thoại, ngày sinh, trường, ảnh; không xác minh phụ huynh.
        </div>
      </form>
    </div>
  );
}

/** Màn mã sau đăng ký (DQ-16): mã người dùng + mã khôi phục hiện MỘT lần; tick "đã lưu" mới đi tiếp. */
function CodesScreen({ r }: { r: Registered }) {
  const navigate = useNavigate();
  const teacher = r.role === 'TEACHER';
  return (
    <div className="s2e1-auth" data-testid={tid('s2e1', 'codes')}>
      <div className="card s2e1-card">
        <h2>
          Tài khoản đã tạo, chào {teacher ? '' : 'em '}
          {r.displayName}
        </h2>
        <CodeBox
          label={teacher ? 'Mã người dùng · dùng để đăng nhập, đưa cho học sinh khi cần' : 'Mã người dùng · dùng để đăng nhập, em nhớ hoặc chép lại'}
          code={r.userCode}
          name="user-code"
        />
        <CodeBox label="Mã khôi phục · chỉ hiện MỘT lần, dùng khi quên mật khẩu" code={r.recoveryCode} name="recovery-code" />
        <div className="s2e1-ban yel" data-testid={tid('s2e1', 'warning')}>
          <b>Hãy chép hoặc in mã khôi phục ngay.</b>
          <span>
            Rời trang này là không xem lại được. Mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản.
          </span>
        </div>
        {teacher ? null : (
          <div className="s2e1-help" data-testid={tid('s2e1', 'student-note')}>
            Quên mật khẩu thì dùng mã khôi phục này; nếu đã vào lớp, thầy cô cũng đặt lại được
          </div>
        )}
        <div className="s2e1-noprint">
          <button type="button" className="btn ghost" onClick={() => window.print()} data-testid={tid('s2e1', 'print')}>
            In / lưu ảnh
          </button>
        </div>
        <SavedGate
          label={teacher ? 'Tôi đã lưu mã' : 'Em đã lưu mã'}
          action={teacher ? 'Vào trang giáo viên' : 'Vào Bài của em'}
          onContinue={() => {
            saveCodeLogin(r);
            navigate(homeOf(r.role), { replace: true });
          }}
        />
      </div>
    </div>
  );
}

