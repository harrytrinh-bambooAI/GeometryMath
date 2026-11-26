import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { messageOf, s3e1Api, type ClassDetail, type Pending, type Student } from './api.ts';

/**
 * C3-07 một lớp: mã lớp + đặt lại mã + trang in (US-S3E1-01), công tắc "Cho vào lớp bằng mã" và "Trợ lý AI cho học
 * sinh" (US-S3E5-04), Chờ duyệt (US-S3E1-03), Học sinh trong lớp + Cho rời lớp + Đặt lại mật khẩu (US-S3E1-04).
 */
export function ClassDetailPage() {
  const { id = '' } = useParams();
  const [c, setC] = useState<ClassDetail | null>(null);
  const [pending, setPending] = useState<Pending[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [confirm, setConfirm] = useState<null | { kind: 'code' } | { kind: 'remove' | 'reset'; s: Student }>(null);
  const [temp, setTemp] = useState<{ name: string; password: string } | null>(null);

  const load = useCallback(async () => {
    try {
      const [d, p, s] = await Promise.all([s3e1Api.detail(id), s3e1Api.pending(id), s3e1Api.students(id)]);
      setC(d);
      setPending(p.items);
      setStudents(s.items);
      setError(null);
    } catch (e) {
      setError(messageOf(e));
    }
  }, [id]);
  useEffect(() => {
    const t = setTimeout(() => void load(), 0);
    return () => clearTimeout(t);
  }, [load]);

  const act = async (fn: () => Promise<unknown>) => {
    try {
      await fn();
    } catch (e) {
      setToast(messageOf(e));
    }
    await load();
  };

  if (error && !c)
    return (
      <div className="alert" role="alert" data-testid={tid('s3e1', 'class', 'error')}>
        {error}
      </div>
    );
  if (!c) return <p className="muted">Đang tải…</p>;
  const active = students.filter((s) => s.status === 'active');
  const left = students.filter((s) => s.status === 'left');

  return (
    <div className="s3e1-page" data-testid={tid('s3e1', 'class')}>
      <p className="small muted">
        <Link to="/gv/lop-hoc">Lớp học</Link> › {c.name}
      </p>
      <h1 className="s3e1-title" data-testid={tid('s3e1', 'class', 'name')}>{c.name}</h1>
      {toast ? (
        <div className="alert" role="alert" data-testid={tid('s3e1', 'toast')}>
          {toast}
        </div>
      ) : null}
      <section className="card s3e1-card">
        <h2 className="s3e1-h">Mã lớp</h2>
        <div className="s3e1-code s3e1-code-big" data-testid={tid('s3e1', 'class', 'code')}>{c.code}</div>
        <div className="s3e1-row">
          <button type="button" className="btn ghost" onClick={() => setConfirm({ kind: 'code' })} data-testid={tid('s3e1', 'code', 'reset')}>
            <Icon name="redo" /> Đặt lại mã lớp
          </button>
          <Link className="btn ghost" to={`/gv/lop-hoc/${encodeURIComponent(c.id)}/in`} data-testid={tid('s3e1', 'print')}>
            <Icon name="print" /> In trang hướng dẫn
          </Link>
        </div>
        <Toggle
          label="Cho vào lớp bằng mã"
          on={c.codeJoinEnabled}
          testId={tid('s3e1', 'switch', 'code-join')}
          onChange={(v) => {
            setC({ ...c, codeJoinEnabled: v });
            void act(() => s3e1Api.settings(c.id, { codeJoinEnabled: v }));
          }}
        />
        <Toggle
          label="Trợ lý AI cho học sinh"
          on={c.aiEnabled}
          testId={tid('s3e5', 'switch', 'ai')}
          onChange={(v) => {
            setC({ ...c, aiEnabled: v });
            void act(() => s3e1Api.settings(c.id, { aiEnabled: v }));
          }}
        />
        <div className="s3e1-lock small">
          <Icon name="lock" inline /> <b>Giáo viên duyệt từng em</b> <span className="chip">Luôn bật</span>
        </div>
      </section>

      <section className="card s3e1-card" data-testid={tid('s3e1', 'pending')}>
        <h2 className="s3e1-h">
          Chờ duyệt (<span data-testid={tid('s3e1', 'pending', 'count')}>{pending.length}</span>)
        </h2>
        {pending.length === 0 ? <p className="muted small">Không có em nào đang chờ.</p> : null}
        <ul className="s3e1-list">
          {pending.map((p) => (
            <li key={p.userId} className="s3e1-line" data-testid={tid('s3e1', 'pending', 'row')} data-code={p.userCode}>
              <span className="s3e1-who">
                <b>{p.displayName}</b> <span className="s3e1-mono">{p.userCode}</span>
                <span className="small muted"> · gửi {formatVnDateTime(p.requestedAt)}</span>
              </span>
              <button type="button" className="btn primary" onClick={() => void act(() => s3e1Api.decide(c.id, p.userId, 'approve'))} data-testid={tid('s3e1', 'approve')}>
                <Icon name="check" /> Duyệt
              </button>
              <button type="button" className="btn ghost" onClick={() => void act(() => s3e1Api.decide(c.id, p.userId, 'reject'))} data-testid={tid('s3e1', 'reject')}>
                <Icon name="x" /> Từ chối
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="card s3e1-card" data-testid={tid('s3e1', 'students')}>
        <h2 className="s3e1-h">
          Học sinh trong lớp (<span data-testid={tid('s3e1', 'students', 'count')}>{active.length}</span>)
        </h2>
        <ul className="s3e1-list">
          {active.map((s) => (
            <li key={s.userId} className="s3e1-line" data-testid={tid('s3e1', 'student', 'row')} data-code={s.userCode}>
              <span className="s3e1-who">
                <b>{s.displayName}</b> <span className="s3e1-mono">{s.userCode}</span>
              </span>
              <button type="button" className="btn ghost" onClick={() => setConfirm({ kind: 'reset', s })} data-testid={tid('s3e1', 'reset-password')}>
                <Icon name="unlock" /> Đặt lại mật khẩu
              </button>
              <button type="button" className="btn ghost" onClick={() => setConfirm({ kind: 'remove', s })} data-testid={tid('s3e1', 'remove')}>
                <Icon name="trash" /> Cho rời lớp
              </button>
            </li>
          ))}
          {left.map((s) => (
            <li key={s.userId} className="s3e1-line muted" data-testid={tid('s3e1', 'student', 'left')} data-code={s.userCode}>
              <span className="s3e1-who">
                {s.displayName} <span className="s3e1-mono">{s.userCode}</span> <span className="chip">đã rời</span>
              </span>
            </li>
          ))}
        </ul>
      </section>

      {confirm ? (
        <div className="s3e1-dialog" role="dialog" aria-modal="true" data-testid={tid('s3e1', 'dialog')}>
          <div className="card s3e1-card">
            <p>
              {confirm.kind === 'code'
                ? 'Đặt lại mã lớp? Mã cũ hết hiệu lực ngay; học sinh đang học không bị ảnh hưởng.'
                : confirm.kind === 'reset'
                  ? `Đặt lại mật khẩu cho ${confirm.s.displayName}?`
                  : `Cho ${confirm.s.displayName} rời lớp? Bài làm cũ vẫn ở Kết quả lớp.`}
            </p>
            <div className="s3e1-row">
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)} data-testid={tid('s3e1', 'dialog', 'cancel')}>
                Hủy
              </button>
              <button
                type="button"
                className="btn primary"
                data-testid={tid('s3e1', 'dialog', 'ok')}
                onClick={() => {
                  const cf = confirm;
                  setConfirm(null);
                  if (cf.kind === 'code') void act(() => s3e1Api.resetCode(c.id));
                  else if (cf.kind === 'remove') void act(() => s3e1Api.decide(c.id, cf.s.userId, 'remove'));
                  else
                    void act(async () => {
                      const r = await s3e1Api.resetPassword(c.id, cf.s.userId);
                      setTemp({ name: cf.s.displayName, password: r.tempPassword });
                    });
                }}
              >
                {confirm.kind === 'reset' ? 'Đặt lại và hiện mật khẩu tạm' : confirm.kind === 'code' ? 'Đặt lại mã' : 'Cho rời lớp'}
              </button>
            </div>
          </div>
        </div>
      ) : null}
      {temp ? (
        <div className="s3e1-dialog" role="dialog" aria-modal="true" data-testid={tid('s3e1', 'temp')}>
          <div className="card s3e1-card">
            <p>Mật khẩu tạm của {temp.name}:</p>
            <div className="s3e1-code s3e1-code-big" data-testid={tid('s3e1', 'temp', 'password')}>{temp.password}</div>
            <p className="small"><b>Chỉ hiện một lần.</b> Em đăng nhập bằng mật khẩu này rồi đặt mật khẩu mới; hết hạn sau 24 giờ.</p>
            <button type="button" className="btn primary" onClick={() => setTemp(null)} data-testid={tid('s3e1', 'temp', 'close')}>
              Đóng
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function Toggle({ label, on, testId, onChange }: { label: string; on: boolean; testId: string; onChange: (v: boolean) => void }) {
  return (
    <label className="s3e1-toggle">
      <input type="checkbox" role="switch" checked={on} onChange={(e) => onChange(e.target.checked)} data-testid={testId} />
      <span>{label}</span>
      <span className="small muted">{on ? 'Đang bật' : 'Đang tắt'}</span>
    </label>
  );
}
