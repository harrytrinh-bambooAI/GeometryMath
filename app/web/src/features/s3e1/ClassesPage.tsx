import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { GRADES, MSG, fieldError, messageOf, s3e1Api, type ClassLine } from './api.ts';

/** C3-02 Lớp học: thẻ lớp của giáo viên + tạo lớp (US-S3E1-01). Bước "Giáo viên duyệt từng em" luôn bật (PO-66 G4). */
export function ClassesPage() {
  const [items, setItems] = useState<ClassLine[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState('');
  const [grade, setGrade] = useState(11);
  const [year, setYear] = useState('2026–2027');
  const [errs, setErrs] = useState<Record<string, string | undefined>>({});
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setItems((await s3e1Api.list()).items);
    } catch (e) {
      setError(messageOf(e));
    }
  }, []);
  useEffect(() => {
    const t = setTimeout(() => void load(), 0);
    return () => clearTimeout(t);
  }, [load]);

  const submit = async (ev: FormEvent) => {
    ev.preventDefault();
    const n = name.trim();
    if (n.length === 0 || [...n].length > 60) {
      setErrs({ name: MSG.s04 });
      return;
    }
    setBusy(true);
    setErrs({});
    try {
      await s3e1Api.create({ name: n, grade, academicYear: year });
      setName('');
      await load();
    } catch (e) {
      setErrs({
        name: fieldError(e, 'name') ?? undefined,
        academicYear: fieldError(e, 'academicYear') ?? undefined,
        form: fieldError(e, 'limit') ?? (fieldError(e, 'name') || fieldError(e, 'academicYear') ? undefined : messageOf(e)),
      });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="s3e1-page" data-testid={tid('s3e1', 'classes')}>
      <h1 className="s3e1-title">Lớp học</h1>
      {error ? <div className="alert" role="alert">{error}</div> : null}
      <section className="card s3e1-card" aria-label="Lớp của tôi">
        {items === null ? <p className="muted">Đang tải…</p> : null}
        {items?.length === 0 ? (
          <p className="muted" data-testid={tid('s3e1', 'classes', 'empty')}>Chưa có lớp nào. Tạo lớp đầu tiên ở dưới.</p>
        ) : null}
        <ul className="s3e1-grid">
          {items?.map((c) => (
            <li key={c.id} className="s3e1-class" data-testid={tid('s3e1', 'class-card')} data-name={c.name}>
              <Link to={`/gv/lop-hoc/${encodeURIComponent(c.id)}`} className="s3e1-class-link">
                <span className="s3e1-class-name" data-testid={tid('s3e1', 'class-card', 'name')}>{c.name}</span>
                <span className="s3e1-code" data-testid={tid('s3e1', 'class-card', 'code')}>{c.code}</span>
                <span className="small muted">
                  Lớp {c.grade} · {c.academicYear ?? '—'} · <span data-testid={tid('s3e1', 'class-card', 'status')}>{c.status === 'active' ? 'Đang dạy' : 'Lưu trữ'}</span>
                </span>
                <span className="small">
                  <Icon name="users" inline /> <span data-testid={tid('s3e1', 'class-card', 'count')}>{c.activeCount}</span> học sinh
                  {c.pendingCount > 0 ? <> · {c.pendingCount} chờ duyệt</> : null}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </section>
      <form className="card s3e1-card s3e1-form" onSubmit={(e) => void submit(e)} data-testid={tid('s3e1', 'create')} noValidate>
        <h2 className="s3e1-h">Tạo lớp mới</h2>
        <label className="s3e1-field">
          <span>Tên lớp</span>
          <input value={name} onChange={(e) => setName(e.target.value)} data-testid={tid('s3e1', 'create', 'name')} />
          {errs.name ? <span className="s3e1-err" data-testid={tid('s3e1', 'create', 'name-error')}>{errs.name}</span> : null}
        </label>
        <label className="s3e1-field">
          <span>Cấp</span>
          <select value={grade} onChange={(e) => setGrade(Number(e.target.value))} data-testid={tid('s3e1', 'create', 'grade')}>
            {GRADES.map((g) => (
              <option key={g} value={g}>Lớp {g}</option>
            ))}
          </select>
        </label>
        <label className="s3e1-field">
          <span>Năm học</span>
          <input value={year} onChange={(e) => setYear(e.target.value)} data-testid={tid('s3e1', 'create', 'year')} />
          {errs.academicYear ? <span className="s3e1-err" data-testid={tid('s3e1', 'create', 'year-error')}>{errs.academicYear}</span> : null}
        </label>
        <div className="s3e1-lock small">
          <Icon name="lock" inline /> <b>Giáo viên duyệt từng em vào lớp</b> <span className="chip">Luôn bật</span>
        </div>
        {errs.form ? <div className="alert" role="alert" data-testid={tid('s3e1', 'create', 'error')}>{errs.form}</div> : null}
        <button type="submit" className="btn primary" disabled={busy} data-testid={tid('s3e1', 'create', 'submit')}>
          <Icon name="plus" /> Tạo lớp
        </button>
      </form>
    </div>
  );
}
