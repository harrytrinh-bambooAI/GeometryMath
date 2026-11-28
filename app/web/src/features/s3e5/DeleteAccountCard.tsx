import { useEffect, useRef, useState, type FormEvent } from 'react';
import { clearToken } from '../../shared/auth/session.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { CONFIRM_WORD, accountApi, canConfirm, currentUserId, fieldError, messageOf } from './account.ts';

/**
 * US-S3E5-05: mục "Xóa tài khoản" ở Cài đặt (mọi tài khoản). Nhập lại mật khẩu và gõ "XÓA" thì nút xác nhận mới mở
 * (tài khoản không có mật khẩu — em vào lớp bằng tên, PO-72 — chỉ gõ "XÓA", không có ô mật khẩu);
 * xóa xong thì thoát phiên và về trang đầu. Giáo viên còn lớp Đang dạy nhận câu của máy chủ (CONFLICT kèm tên lớp).
 */
export function DeleteAccountCard() {
  const [open, setOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<{ password?: string; confirm?: string; form?: string }>({});
  const [busy, setBusy] = useState(false);
  const requestId = useRef<string>(crypto.randomUUID());
  const [hasPassword, setHasPassword] = useState(true);
  useEffect(() => {
    if (!open) return;
    let live = true;
    accountApi
      .me()
      .then((m) => {
        if (live) setHasPassword(m.hasPassword !== false);
      })
      .catch(() => {
        /* giữ mặc định: hỏi mật khẩu */
      });
    return () => {
      live = false;
    };
  }, [open]);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    if (busy || !canConfirm(password, confirm, hasPassword)) return;
    const userId = currentUserId();
    if (!userId) {
      setErrors({ form: 'Hãy đăng nhập bằng mã người dùng để xóa tài khoản.' });
      return;
    }
    setBusy(true);
    setErrors({});
    try {
      await accountApi.deleteAccount(userId, hasPassword ? password : null, confirm.trim(), requestId.current);
      clearToken();
      try {
        sessionStorage.removeItem('geo.e1.me');
      } catch {
        /* bỏ qua */
      }
      window.location.assign('/?da-xoa-tai-khoan=1');
    } catch (e) {
      const p = fieldError(e, 'password');
      const c = fieldError(e, 'confirm');
      setErrors(p || c ? { password: p ?? undefined, confirm: c ?? undefined } : { form: messageOf(e) });
      requestId.current = crypto.randomUUID();
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="card s3e5-delete" id="xoa-tai-khoan" data-testid={tid('s3e5', 'delete')}>
      <h3>Xóa tài khoản</h3>
      <p className="muted small">
        Xóa tài khoản thì không đăng nhập hay khôi phục lại được; bài làm, lượt hỏi trợ lý và lớp của bạn bị xóa trong tối đa
        30 ngày. Giáo viên cần lưu trữ hết lớp Đang dạy trước.
      </p>
      {!open ? (
        <button type="button" className="btn s3e5-danger s3e5-delete-open" onClick={() => setOpen(true)} data-testid={tid('s3e5', 'delete', 'open')}>
          <Icon name="trash" /> Xóa tài khoản
        </button>
      ) : (
        <form className="s3e5-delete-form" onSubmit={(e) => void submit(e)} data-testid={tid('s3e5', 'delete', 'form')}>
          {hasPassword && (
            <label className="s3e5-field">
              <span>Mật khẩu hiện tại</span>
              <input
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                aria-invalid={!!errors.password}
                data-testid={tid('s3e5', 'delete', 'password')}
              />
              {errors.password ? (
                <span className="s3e5-err" role="alert" data-testid={tid('s3e5', 'delete', 'password', 'error')}>
                  {errors.password}
                </span>
              ) : null}
            </label>
          )}
          <label className="s3e5-field">
            <span>Gõ chữ {CONFIRM_WORD} để xác nhận</span>
            <input
              type="text"
              autoComplete="off"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              aria-invalid={!!errors.confirm}
              data-testid={tid('s3e5', 'delete', 'confirm')}
            />
            {errors.confirm ? <span className="s3e5-err" role="alert">{errors.confirm}</span> : null}
          </label>
          {errors.form ? (
            <div className="alert" role="alert" data-testid={tid('s3e5', 'delete', 'error')}>
              {errors.form}
            </div>
          ) : null}
          <div className="s3e5-delete-actions">
            <button
              type="submit"
              className="btn s3e5-danger"
              disabled={busy || !canConfirm(password, confirm, hasPassword)}
              data-testid={tid('s3e5', 'delete', 'submit')}
            >
              <Icon name="trash" /> Xóa vĩnh viễn
            </button>
            <button type="button" className="btn" onClick={() => setOpen(false)} data-testid={tid('s3e5', 'delete', 'cancel')}>
              Hủy
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
