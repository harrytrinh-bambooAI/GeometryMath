import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { createPlan, fetchCatalog, fetchPlan, type CatalogChapter, type LessonFormBody } from './api.ts';
import {
  AUDIENCES,
  EMPTY,
  LEVELS,
  NOTE_MAX,
  STYLES,
  choosePeriod,
  findLesson,
  findPeriod,
  intOrNull,
  ratioSum,
  validate,
  type FormState,
} from './logic.ts';

/**
 * C5-08 "Soạn giáo án mới" (US-S5E1-01, giao-dien.md §1): trường B bắt buộc (Tiết PPCT, Đối tượng, Phong cách chọn
 * sẵn Truyền thống, Kiến thức trọng tâm tích sẵn theo tiết), trường C thu gọn. ?from=<giáo án>&focus=<trường>: điền
 * lại form của giáo án cũ và đặt tiêu điểm vào trường AI hỏi lại (nút "Mở trường …" ở màn kết quả).
 */
export function NewLessonPage() {
  const [chapters, setChapters] = useState<CatalogChapter[] | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [s, setS] = useState<FormState>(EMPTY);
  const [touched, setTouched] = useState(false);
  const [serverErr, setServerErr] = useState<Record<string, string>>({});
  const [sending, setSending] = useState(false);
  const [showOptions, setShowOptions] = useState(false);
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const soTietRef = useRef<HTMLInputElement>(null);

  const load = useCallback(() => {
    fetchCatalog()
      .then((r) => {
        setError(null);
        setChapters(r.chapters);
      })
      .catch(setError);
  }, []);
  useEffect(load, [load]);

  const from = params.get('from');
  const focus = params.get('focus');
  useEffect(() => {
    if (!from || !chapters) return;
    fetchPlan(from)
      .then((p) => {
        const r = p.request;
        const f = findPeriod(chapters, r.tietPpct);
        setS({
          ...EMPTY,
          chapter: f?.chapter ?? null,
          lesson: f?.lesson.no ?? null,
          period: r.tietPpct,
          soTiet: r.soTiet,
          doiTuong: r.doiTuong,
          phongCach: r.phongCach,
          kienThuc: r.kienThuc,
          phieuHocTap: r.phieuHocTap,
          soBaiTap: r.soBaiTap,
          tiLe: r.tiLe ?? {},
          ghiChu: r.ghiChu ?? '',
        });
        if (focus === 'soTiet') setTimeout(() => soTietRef.current?.focus(), 0);
      })
      .catch(() => undefined);
  }, [from, focus, chapters]);

  if (error) return <ErrorNotice error={error} onRetry={load} testId="s5e1-form-error" />;
  if (!chapters) return <p className="muted" aria-busy="true">Đang tải…</p>;

  const errs = { ...validate(s), ...serverErr };
  const shown = touched ? errs : {};
  const lesson = findLesson(chapters, s.lesson);
  const chapter = chapters.find((c) => c.no === s.chapter);
  const set = (p: Partial<FormState>) => {
    setServerErr({});
    setS((x) => ({ ...x, ...p }));
  };

  const submit = async () => {
    setTouched(true);
    if (Object.keys(validate(s)).length > 0 || !s.period || !s.doiTuong) return;
    const body: LessonFormBody = {
      tietPpct: s.period,
      doiTuong: s.doiTuong,
      phongCach: s.phongCach,
      kienThuc: s.kienThuc,
      soTiet: s.soTiet,
      phieuHocTap: s.phieuHocTap,
      soBaiTap: s.soBaiTap,
      tiLe: Object.keys(s.tiLe).length ? s.tiLe : null,
      phanHoa: null,
      phanMem: null,
      thietBi: [],
      ghiChu: s.ghiChu.trim() ? s.ghiChu : null,
    };
    setSending(true);
    try {
      const r = await createPlan(body);
      navigate(`/gv/giao-an/${r.draftId}`);
    } catch (e) {
      if (e instanceof ApiError && e.details?.length) {
        setServerErr(Object.fromEntries(e.details.map((d) => [d.field, e.message])));
      } else {
        setError(e);
      }
    } finally {
      setSending(false);
    }
  };

  const blocked = Object.keys(validate(s)).length > 0;
  return (
    <div className="s5e1-page" data-testid="s5e1-form">
      <h1 className="s5e1-title">Soạn giáo án mới</h1>
      <section className="card s5e1-card">
        <div className="s5e1-field">
          <span className="s5e1-label">Tiết PPCT *</span>
          <div className="s5e1-row">
            <select
              aria-label="Chương"
              data-testid="s5e1-chapter"
              value={s.chapter ?? ''}
              onChange={(e) => set({ chapter: intOrNull(e.target.value), lesson: null, period: null, soTiet: null, kienThuc: [] })}
            >
              <option value="">Chương…</option>
              {chapters.map((c) => (
                <option key={c.no} value={c.no}>
                  {c.label}. {c.title}
                </option>
              ))}
            </select>
            <select
              aria-label="Bài"
              data-testid="s5e1-lesson"
              value={s.lesson ?? ''}
              onChange={(e) => set({ lesson: intOrNull(e.target.value), period: null, soTiet: null, kienThuc: [] })}
            >
              <option value="">Bài…</option>
              {(chapter?.lessons ?? []).map((l) => (
                <option key={l.no} value={l.no}>
                  {l.title}
                </option>
              ))}
            </select>
            <select
              aria-label="Tiết"
              data-testid="s5e1-period"
              value={s.period ?? ''}
              onChange={(e) => setS((x) => choosePeriod(x, chapters, e.target.value || null))}
            >
              <option value="">Tiết…</option>
              {(lesson?.periods ?? []).map((p) => (
                <option key={p.id} value={p.id}>
                  {p.label}
                </option>
              ))}
            </select>
          </div>
          {shown.tietPpct && <p className="s5e1-err" data-testid="s5e1-err-tietPpct">{shown.tietPpct}</p>}
        </div>

        <label className="s5e1-field">
          <span className="s5e1-label">Số tiết</span>
          <span className="s5e1-row">
            <input
              ref={soTietRef}
              type="number"
              min={1}
              max={5}
              className="s5e1-num"
              data-testid="s5e1-sotiet"
              value={s.soTiet ?? ''}
              onChange={(e) => set({ soTiet: intOrNull(e.target.value) })}
            />
            <span className="small muted">tự điền theo PPCT, sửa được</span>
          </span>
          {shown.soTiet && <span className="s5e1-err">{shown.soTiet}</span>}
        </label>

        <div className="s5e1-field" role="radiogroup" aria-label="Đối tượng">
          <span className="s5e1-label">Đối tượng *</span>
          <div className="s5e1-row">
            {AUDIENCES.map((a) => (
              <button
                key={a.code}
                type="button"
                role="radio"
                aria-checked={s.doiTuong === a.code}
                className={`chip s5e1-pick${s.doiTuong === a.code ? ' on' : ''}`}
                data-testid={`s5e1-doituong-${a.code}`}
                onClick={() => set({ doiTuong: a.code })}
              >
                {a.label}
              </button>
            ))}
          </div>
          {shown.doiTuong && <p className="s5e1-err" data-testid="s5e1-err-doiTuong">{shown.doiTuong}</p>}
        </div>

        <div className="s5e1-field" role="radiogroup" aria-label="Phong cách">
          <span className="s5e1-label">Phong cách *</span>
          <div className="s5e1-row">
            {STYLES.map((p) => (
              <button
                key={p.code}
                type="button"
                role="radio"
                aria-checked={s.phongCach === p.code}
                className={`chip s5e1-pick${s.phongCach === p.code ? ' on' : ''}`}
                data-testid={`s5e1-style-${p.code}`}
                onClick={() => set({ phongCach: p.code })}
              >
                {p.label}
              </button>
            ))}
          </div>
        </div>

        <fieldset className="s5e1-field s5e1-kt" data-testid="s5e1-knowledge">
          <legend className="s5e1-label">Kiến thức trọng tâm * (tích sẵn theo tiết, bấm để bỏ/thêm)</legend>
          {!lesson && <p className="small muted">Chọn Tiết PPCT để thấy kiến thức của bài.</p>}
          {lesson?.knowledge.map((k) => (
            <label key={k.code} className="s5e1-check">
              <input
                type="checkbox"
                data-testid={`s5e1-kt-${k.code}`}
                checked={s.kienThuc.includes(k.code)}
                onChange={(e) =>
                  set({ kienThuc: e.target.checked ? [...s.kienThuc, k.code] : s.kienThuc.filter((c) => c !== k.code) })
                }
              />
              <span>
                <span className="s5e1-mono">{k.code}</span> <span data-math>{k.statement}</span>
              </span>
            </label>
          ))}
          {shown.kienThuc && <p className="s5e1-err" data-testid="s5e1-err-kienThuc">{shown.kienThuc}</p>}
        </fieldset>

        <button type="button" className="btn ghost" data-testid="s5e1-options" aria-expanded={showOptions} onClick={() => setShowOptions((v) => !v)}>
          <Icon name={showOptions ? 'up' : 'down'} />
          Tuỳ chọn
        </button>
        {showOptions && (
          <div className="s5e1-options">
            <label className="s5e1-check">
              <input type="checkbox" data-testid="s5e1-pht" checked={s.phieuHocTap} onChange={(e) => set({ phieuHocTap: e.target.checked })} />
              Phiếu học tập
            </label>
            <div className="s5e1-row">
              <label>
                Số bài tập{' '}
                <input
                  type="number"
                  min={0}
                  max={20}
                  className="s5e1-num"
                  data-testid="s5e1-sobaitap"
                  value={s.soBaiTap ?? ''}
                  onChange={(e) => set({ soBaiTap: intOrNull(e.target.value) })}
                />
              </label>
              {LEVELS.map((l) => (
                <label key={l}>
                  {l}{' '}
                  <input
                    type="number"
                    min={0}
                    className="s5e1-num"
                    data-testid={`s5e1-tile-${l}`}
                    value={s.tiLe[l] ?? ''}
                    onChange={(e) => {
                      const n = intOrNull(e.target.value);
                      const t = { ...s.tiLe };
                      if (n === null) delete t[l];
                      else t[l] = n;
                      set({ tiLe: t });
                    }}
                  />
                </label>
              ))}
              {s.soBaiTap !== null && !errs.tiLe && Object.keys(s.tiLe).length > 0 && (
                <span className="small">✓ tổng = {ratioSum(s.tiLe)}</span>
              )}
            </div>
            {errs.tiLe && (
              <p className="s5e1-err" data-testid="s5e1-err-tiLe">
                {errs.tiLe}
              </p>
            )}
            <label className="s5e1-field">
              <span className="s5e1-label">Ghi chú</span>
              <textarea data-testid="s5e1-ghichu" rows={3} value={s.ghiChu} onChange={(e) => set({ ghiChu: e.target.value })} />
              <span className="small muted">
                {[...s.ghiChu].length}/{NOTE_MAX} · AI coi ghi chú là thông tin tham khảo, không đổi được luật soạn
              </span>
              {errs.ghiChu && <span className="s5e1-err" data-testid="s5e1-err-ghiChu">{errs.ghiChu}</span>}
            </label>
          </div>
        )}

        <div className="s5e1-row">
          <span className="s5e1-grow" />
          <button type="button" className="btn primary" data-testid="s5e1-submit" disabled={sending || (touched && blocked)} onClick={() => void submit()}>
            <Icon name="spark" />
            Soạn giáo án
          </button>
        </div>
      </section>
    </div>
  );
}
