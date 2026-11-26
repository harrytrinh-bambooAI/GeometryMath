import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MSG, messageOf, s3e1Api, type MyClass } from './api.ts';

/** C3-07 học sinh: lớp của em + nhập mã lớp, chờ duyệt / bị từ chối (US-S3E1-02, US-S3E1-03-SC3). */
export function JoinPage() {
  const [params] = useSearchParams();
  const [code, setCode] = useState(params.get('ma') ?? '');
  const [classes, setClasses] = useState<MyClass[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [waiting, setWaiting] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setClasses((await s3e1Api.myClasses()).items);
    } catch {
      setClasses([]);
    }
  }, []);
  useEffect(() => {
    const t = setTimeout(() => void load(), 0);
    return () => clearTimeout(t);
  }, [load]);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    setBusy(true);
    setErr(null);
    try {
      const r = await s3e1Api.join(code);
      setWaiting(r.className);
      await load();
    } catch (e) {
      if (e instanceof ApiError && e.code === 'RATE_LIMITED') setErr(MSG.s08);
      else if (e instanceof ApiError && e.code === 'CONFLICT') setErr(MSG.s03);
      else if (e instanceof ApiError && e.code === 'VALIDATION_FAILED') setErr(MSG.s01);
      else setErr(messageOf(e));
    } finally {
      setBusy(false);
    }
  };

  const pending = classes.filter((c) => c.status === 'pending');
  const rejected = classes.filter((c) => c.status === 'rejected');
  const active = classes.filter((c) => c.status === 'active');

  return (
    <div className="s3e1-page s3e1-join" data-testid={tid('s3e1', 'join')}>
      <h1 className="s3e1-title">Lớp của em</h1>
      {active.length > 0 ? (
        <section className="card s3e1-card" data-testid={tid('s3e1', 'join', 'active')}>
          <h2 className="s3e1-h">Đang học</h2>
          <ul className="s3e1-list">
            {active.map((c) => (
              <li key={c.className} className="s3e1-line">{c.className}</li>
            ))}
          </ul>
          <Link className="btn ghost" to="/hs/bai-luyen"><Icon name="assign" /> Bài của lớp</Link>
        </section>
      ) : null}
      {waiting || pending.length > 0 ? (
        <section className="card s3e1-card" role="status" data-testid={tid('s3e1', 'join', 'waiting')}>
          <p><Icon name="clock" inline /> {MSG.s02}</p>
          <p className="small muted">Lớp chờ duyệt: {[...new Set([...(waiting ? [waiting] : []), ...pending.map((p) => p.className)])].join(', ')}</p>
          <Link className="btn ghost" to="/hs/tu-hoc"><Icon name="cube" /> Vào Tự học</Link>
        </section>
      ) : null}
      {rejected.map((c) => (
        <section key={c.className} className="card s3e1-card" role="status" data-testid={tid('s3e1', 'join', 'rejected')}>
          <p><Icon name="info" inline /> {MSG.rejected} (Lớp {c.className})</p>
        </section>
      ))}
      <form className="card s3e1-card s3e1-form" onSubmit={(e) => void submit(e)} noValidate>
        <h2 className="s3e1-h">Vào lớp bằng mã</h2>
        <label className="s3e1-field">
          <span>Mã lớp thầy cô đọc (ví dụ HK-4P7Q)</span>
          <input value={code} onChange={(e) => setCode(e.target.value)} autoCapitalize="characters" className="s3e1-mono" data-testid={tid('s3e1', 'join', 'code')} />
          {err ? <span className="s3e1-err" data-testid={tid('s3e1', 'join', 'error')}>{err}</span> : null}
        </label>
        <button type="submit" className="btn primary" disabled={busy} data-testid={tid('s3e1', 'join', 'submit')}>
          <Icon name="login" /> Vào lớp
        </button>
      </form>
    </div>
  );
}
