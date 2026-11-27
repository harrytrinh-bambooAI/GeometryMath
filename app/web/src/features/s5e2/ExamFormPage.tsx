import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { examApi, fieldError, TYPE_LABEL, vn, type ExamSetDto, type Levels, type MatrixDto, type PreviewDto } from './examApi.ts';

const LEVELS = ['NB', 'TH', 'VD', 'VDC'] as const;

/**
 * C5-08 "Ra đề theo ma trận" (US-S5E2-02): chọn loại đề → form điền theo ma trận chuẩn (thời gian, số câu và điểm mỗi
 * phần, chương, tỉ lệ mức độ, số mã đề 1–8, seed tùy chọn). Trường cố định (cấu trúc QĐ 764, tổng điểm 10, đảo câu
 * và phương án) chỉ hiện chữ, không có ô sửa. "Lắp đề" gọi POST /api/exams rồi mở bộ đề nháp.
 */
export function ExamFormPage() {
  const nav = useNavigate();
  const [items, setItems] = useState<MatrixDto[] | null>(null);
  const [code, setCode] = useState('');
  const [minutes, setMinutes] = useState(0);
  const [parts, setParts] = useState<Record<string, { count: number; points: number }>>({});
  const [chapters, setChapters] = useState<string[]>([]);
  const [levels, setLevels] = useState<Levels>({ NB: 0, TH: 0, VD: 0, VDC: 0 });
  const [variants, setVariants] = useState(4);
  const [seed, setSeed] = useState('');
  const [preview, setPreview] = useState<PreviewDto | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);
  const [sets, setSets] = useState<ExamSetDto[]>([]);

  useEffect(() => {
    examApi.catalog().then((r) => setItems(r.items)).catch(setError);
    examApi.list().then((r) => setSets(r.items)).catch(() => undefined);
  }, []);

  const mt = useMemo(() => items?.find((m) => m.code === code) ?? null, [items, code]);

  function choose(c: string) {
    setCode(c);
    setPreview(null);
    setError(null);
    const m = items?.find((x) => x.code === c);
    if (!m) return;
    setMinutes(m.minutes);
    setParts(Object.fromEntries(m.parts.map((p) => [p.id, { count: p.count, points: p.points }])));
    setChapters(m.chapters.filter((ch) => ch.selected).map((ch) => ch.id));
    setLevels(m.levels);
    setVariants(m.defaultVariantCount);
  }

  const total = mt ? mt.parts.reduce((s, p) => s + (parts[p.id]?.count ?? p.count) * (parts[p.id]?.points ?? p.points), 0) : 0;
  const fixedStructure = mt?.kind === 'thi-thu-tn-thpt';

  function body() {
    return {
      matrixCode: code,
      minutes,
      chapters,
      parts: mt?.parts.map((p) => ({ id: p.id, count: parts[p.id]?.count, points: parts[p.id]?.points })),
      levels,
      variantCount: variants,
      seed: seed.trim() ? Number(seed) : null,
    };
  }

  async function run(kind: 'preview' | 'create') {
    setBusy(true);
    setError(null);
    try {
      if (kind === 'preview') setPreview(await examApi.preview(body()));
      else {
        const s = await examApi.create(body());
        nav(`/gv/ra-de/${s.id}`);
      }
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }

  if (!items) return error ? <ErrorNotice error={error} testId={tid('s5e2', 'error')} /> : <p className="muted">Đang tải…</p>;
  return (
    <section className="card s5e2-page" data-testid={tid('s5e2', 'form')}>
      <h1 className="s5e2-title">
        <Icon name="file" /> Ra đề theo ma trận
      </h1>
      <label className="s5e2-field">
        Loại đề
        <select value={code} onChange={(e) => choose(e.target.value)} data-testid={tid('s5e2', 'loai-de')}>
          <option value="">— Chọn loại đề —</option>
          {items.map((m) => (
            <option key={m.code} value={m.code}>
              {m.name}
            </option>
          ))}
        </select>
      </label>
      {mt && (
        <>
          <div className="s5e2-grid">
            <label className="s5e2-field">
              Thời gian (phút)
              <input type="number" min={1} value={minutes} onChange={(e) => setMinutes(Number(e.target.value))} data-testid={tid('s5e2', 'thoi-gian')} />
              <FieldMsg e={error} f="minutes" />
            </label>
            <label className="s5e2-field">
              Số mã đề (1–8)
              <input type="number" min={1} max={8} value={variants} onChange={(e) => setVariants(Number(e.target.value))} data-testid={tid('s5e2', 'so-ma')} />
              <FieldMsg e={error} f="variantCount" />
            </label>
            <label className="s5e2-field">
              Seed (tùy chọn, giữ để in lại)
              <input value={seed} inputMode="numeric" onChange={(e) => setSeed(e.target.value)} data-testid={tid('s5e2', 'seed')} />
            </label>
          </div>
          <table className="s5e2-table" data-testid={tid('s5e2', 'phan')}>
            <thead>
              <tr>
                <th>Phần</th>
                <th>Loại câu</th>
                <th>Số câu</th>
                <th>Điểm/câu</th>
              </tr>
            </thead>
            <tbody>
              {mt.parts.map((p) => (
                <tr key={p.id} data-testid={tid('s5e2', 'phan', p.id)}>
                  <td>{p.name}</td>
                  <td>{TYPE_LABEL[p.type] ?? p.type}</td>
                  <td>
                    {fixedStructure ? (
                      <span data-testid={tid('s5e2', 'phan', p.id, 'so-cau')}>{p.count}</span>
                    ) : (
                      <input type="number" min={1} value={parts[p.id]?.count ?? p.count} data-testid={tid('s5e2', 'phan', p.id, 'so-cau')}
                        onChange={(e) => setParts({ ...parts, [p.id]: { count: Number(e.target.value), points: parts[p.id]?.points ?? p.points } })} />
                    )}
                  </td>
                  <td>
                    <input type="number" min={0} step={0.05} value={parts[p.id]?.points ?? p.points} data-testid={tid('s5e2', 'phan', p.id, 'diem')}
                      onChange={(e) => setParts({ ...parts, [p.id]: { count: parts[p.id]?.count ?? p.count, points: Number(e.target.value) } })} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <p data-testid={tid('s5e2', 'tong-diem')}>Tổng điểm: <strong>{vn(total)}</strong> / 10</p>
          <FieldMsg e={error} f="parts" />
          <fieldset className="s5e2-fieldset" data-testid={tid('s5e2', 'chuong')}>
            <legend>Chương</legend>
            {mt.chapters.map((ch) => (
              <label key={ch.id} className="s5e2-check">
                <input type="checkbox" checked={chapters.includes(ch.id)} data-testid={tid('s5e2', 'chuong', ch.id)}
                  onChange={(e) => setChapters(e.target.checked ? [...chapters, ch.id] : chapters.filter((x) => x !== ch.id))} />
                {ch.name}
              </label>
            ))}
          </fieldset>
          <fieldset className="s5e2-fieldset" data-testid={tid('s5e2', 'muc-do')}>
            <legend>Tỉ lệ mức độ</legend>
            {LEVELS.map((l) => (
              <label key={l} className="s5e2-slider">
                {l} {Math.round(levels[l] * 100)}%
                <input type="range" min={0} max={1} step={0.05} value={levels[l]} data-testid={tid('s5e2', 'muc-do', l)}
                  onChange={(e) => setLevels({ ...levels, [l]: Number(e.target.value) })} />
              </label>
            ))}
          </fieldset>
          <ul className="s5e2-fixed small muted" data-testid={tid('s5e2', 'co-dinh')}>
            <li>Tổng điểm 10 (cố định)</li>
            <li>{fixedStructure ? 'Cấu trúc phần theo QĐ 764 (cố định)' : 'Cấu trúc phần theo ma trận chuẩn'}</li>
            <li>Đảo thứ tự câu trong phần và đảo phương án: luôn bật</li>
          </ul>
          {error && !fieldError(error, 'minutes') && !fieldError(error, 'parts') && !fieldError(error, 'variantCount') ? (
            <ErrorNotice error={error} testId={tid('s5e2', 'error')} />
          ) : null}
          <div className="s5e2-actions">
            <button type="button" className="btn ghost" disabled={busy} onClick={() => run('preview')} data-testid={tid('s5e2', 'xem-truoc')}>
              <Icon name="eye" /> Xem trước
            </button>
            <button type="button" className="btn" disabled={busy} onClick={() => run('create')} data-testid={tid('s5e2', 'lap-de')}>
              <Icon name="layers" /> Lắp đề
            </button>
          </div>
          {preview && (
            <p className="small" data-testid={tid('s5e2', 'preview')}>
              {preview.cells.length} ô · tổng điểm {vn(preview.totalPoints)} · thời gian dự kiến tối thiểu {vn(preview.estimatedMinutes)} / {vn(preview.minutes)} phút
            </p>
          )}
        </>
      )}
      {sets.length > 0 && (
        <div data-testid={tid('s5e2', 'bo-de')}>
          <h2 className="s5e2-sub">Bộ đề của tôi</h2>
          <ul>
            {sets.map((s) => (
              <li key={s.id}>
                <Link to={`/gv/ra-de/${s.id}`}>{s.name}</Link> <span className={s.status === 'approved' ? 'chip grn' : 'chip yel'}>{s.humanLabel}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}

function FieldMsg({ e, f }: { e: unknown; f: string }) {
  const m = fieldError(e, f);
  return m ? (
    <span className="s5e2-err" role="alert" data-testid={tid('s5e2', 'loi', f)}>
      {m}
    </span>
  ) : null;
}
