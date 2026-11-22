import { useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { errorText } from './messages.ts';
import type { ClassItem } from './studentApi.ts';

/**
 * Màn giao bài tối thiểu của E1 (bước "1 · Chọn lớp" chỉ liệt kê lớp mình dạy, US-GEO-104-SC2). Đặt giờ mở,
 * trợ giúp và xác nhận đầy đủ thuộc E5/E6.
 */
export function AssignPage() {
  const { versionId = '' } = useParams();
  const [classes, setClasses] = useState<ClassItem[] | null>(null);
  const [picked, setPicked] = useState<string | null>(null);
  const [done, setDone] = useState<string | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    api
      .get<{ items: ClassItem[] }>('/api/v1/teacher/classes')
      .then((c) => alive && setClasses(c.items))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, []);

  const submit = async () => {
    if (!picked) return;
    setError(null);
    try {
      const r = await api.post<{ id: string }>('/api/v1/teacher/assignments', { classId: picked, problemVersionId: versionId });
      setDone(r.id);
    } catch (e) {
      setError(e);
    }
  };

  return (
    <div data-testid={tid('e1', 'assign')}>
      <h2>Giao bài</h2>
      <section className="card" data-testid={tid('e1', 'assign', 'step-class')}>
        <h3>1 · Chọn lớp</h3>
        {(classes ?? []).map((c) => (
          <label key={c.id} className="e1-radio" data-testid={tid('e1', 'assign', 'class', c.name)}>
            <input type="radio" name="class" value={c.id} checked={picked === c.id} onChange={() => setPicked(c.id)} />
            {c.name}
          </label>
        ))}
      </section>
      {error ? (
        <div className="alert" role="alert" data-testid={tid('e1', 'assign', 'error')}>
          {errorText(error)}
        </div>
      ) : null}
      {done ? (
        <p className="chip grn" data-testid={tid('e1', 'assign', 'done')}>
          Đã giao bài.
        </p>
      ) : (
        <button type="button" className="btn" disabled={!picked} onClick={() => void submit()} data-testid={tid('e1', 'assign', 'submit')}>
          Giao bài
        </button>
      )}
    </div>
  );
}
