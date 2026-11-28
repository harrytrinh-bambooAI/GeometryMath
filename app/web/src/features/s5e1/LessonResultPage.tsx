import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { approvePlan, createPlan, downloadExport, fetchPlan, lockSection, retryPlan, undoPlan, type LessonBody, type PlanView } from './api.ts';
import { MSG_FAILED, MSG_REFUSED, sectionLabel, versionLine } from './logic.ts';
import { PatchPanel } from './PatchPanel.tsx';

const FIELD_LABEL: Record<string, string> = {
  soTiet: 'Số tiết',
  doiTuong: 'Đối tượng',
  kienThuc: 'Kiến thức trọng tâm',
  tietPpct: 'Tiết PPCT',
  phongCach: 'Phong cách',
};

/**
 * Màn kết quả (US-S5E1-03, giao-dien.md §2): chờ "Đang soạn" (hỏi lại máy chủ mỗi giây), rồi dựng theo kind:
 * lesson = 4 mục khung 5512 + thanh thời lượng Σ phút, nhãn "AI chưa chắc", khung "⚠ AI đề xuất (n)";
 * question = đúng một câu hỏi, ô trả lời có tiêu điểm, nút mở trường form; Lỗi / Từ chối = thông điệp chung.
 */
export function LessonResultPage() {
  const { id = '' } = useParams();
  const [plan, setPlan] = useState<PlanView | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [tick, setTick] = useState(0);
  // T-261128-54: mục đang chọn để chat vá, thông báo của thanh duyệt / xuất
  const [selected, setSelected] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const load = useCallback(() => {
    fetchPlan(id)
      .then((p) => {
        setError(null);
        setPlan(p);
      })
      .catch(setError);
  }, [id]);
  useEffect(load, [load, tick]);
  // "Đang soạn": hỏi lại máy chủ mỗi giây tới khi có kết quả
  useEffect(() => {
    if (plan?.status !== 'drafting') return;
    const t = setTimeout(() => setTick((n) => n + 1), 1000);
    return () => clearTimeout(t);
  }, [plan]);

  if (error) return <ErrorNotice error={error} onRetry={load} testId="s5e1-result-error" />;
  if (!plan) return <p className="muted" aria-busy="true">Đang tải…</p>;

  const retry = () => {
    retryPlan(plan.draftId)
      .then(() => setPlan({ ...plan, status: 'drafting', statusLabel: 'Đang soạn', kind: undefined, message: undefined }))
      .catch(setError);
  };
  const act = (p: Promise<unknown>) => {
    setNotice(null);
    p.then(load).catch((e: unknown) => setNotice(e instanceof Error ? e.message : 'Chưa thực hiện được, thầy cô thử lại.'));
  };
  const exportAs = (format: 'docx' | 'pdf') => {
    setNotice(null);
    void downloadExport(plan.draftId, format).then((m) => setNotice(m));
  };
  const toggleLock = (section: string) => act(lockSection(plan.draftId, section, !plan.lockedSections.includes(section)));

  return (
    <div className="s5e1-page" data-testid="s5e1-result" data-status={plan.status}>
      <header className="s5e1-row">
        <h1 className="s5e1-title" data-testid="s5e1-title">
          {plan.title}
        </h1>
        <span className="chip" data-testid="s5e1-human-label">
          {plan.humanLabel}
        </span>
        <span className="chip" data-testid="s5e1-status">
          {plan.statusLabel}
        </span>
        <span className="s5e1-grow" />
        <Link className="btn ghost" to="/gv/giao-an/moi" data-testid="s5e1-new">
          <Icon name="plus" />
          Soạn giáo án mới
        </Link>
      </header>
      {plan.status === 'drafting' && (
        <p className="card s5e1-card" aria-busy="true" data-testid="s5e1-wait">
          AI đang soạn và máy chủ đang kiểm giáo án…
        </p>
      )}
      {plan.status === 'error' && (
        <div className="alert" role="alert" data-testid="s5e1-failed">
          <p>{plan.message ?? MSG_FAILED}</p>
          <button type="button" className="btn primary" data-testid="s5e1-retry" onClick={retry}>
            <Icon name="redo" />
            Thử lại
          </button>
        </div>
      )}
      {plan.status === 'refused' && (
        <div className="alert" role="alert" data-testid="s5e1-refused">
          <p>{plan.message ?? MSG_REFUSED}</p>
        </div>
      )}
      {(plan.kind === 'question' || plan.kind === 'reply') && plan.reply && <ReplyBubble plan={plan} />}
      {plan.kind === 'lesson' && plan.lesson && (
        <div className="card s5e1-row s5e1-toolbar" data-testid="s5e1-toolbar">
          <span className="chip" data-testid="s5e1-version">
            {versionLine(plan.version, plan.approvedVersion)}
          </span>
          <button type="button" className="btn ghost" data-testid="s5e1-undo" disabled={!plan.canUndo} onClick={() => act(undoPlan(plan.draftId))}>
            <Icon name="redo" />
            Hoàn tác
          </button>
          <span className="s5e1-grow" />
          <button
            type="button"
            className="btn primary"
            data-testid="s5e1-approve"
            disabled={plan.approvedVersion === plan.version}
            onClick={() => act(approvePlan(plan.draftId))}
          >
            <Icon name="shield" />
            Duyệt giáo án
          </button>
          <button type="button" className="btn ghost" data-testid="s5e1-export-docx" onClick={() => exportAs('docx')}>
            <Icon name="file" />
            Tải Word
          </button>
          <button type="button" className="btn ghost" data-testid="s5e1-export-pdf" onClick={() => exportAs('pdf')}>
            <Icon name="print" />
            Tải PDF
          </button>
          {notice && (
            <p className="small s5e1-notice" role="alert" data-testid="s5e1-notice">
              {notice}
            </p>
          )}
        </div>
      )}
      {plan.kind === 'lesson' && plan.lesson && (
        <LessonView lesson={plan.lesson} locked={plan.lockedSections} selected={selected} onSelect={setSelected} onLock={toggleLock} />
      )}
      {plan.kind === 'lesson' && plan.lesson && (
        <PatchPanel planId={plan.draftId} selected={selected} locked={plan.lockedSections} onClear={() => setSelected(null)} onChanged={load} />
      )}
    </div>
  );
}

function ReplyBubble({ plan }: { plan: PlanView }) {
  const navigate = useNavigate();
  const [answer, setAnswer] = useState('');
  const [error, setError] = useState<unknown>(null);
  const reply = plan.reply!;
  const field = reply.field;
  const send = () => {
    const note = [plan.request.ghiChu, `Trả lời câu hỏi của AI: ${answer.trim()}`].filter(Boolean).join('\n');
    createPlan({ ...plan.request, ghiChu: note.slice(0, 500) })
      .then((r) => navigate(`/gv/giao-an/${r.draftId}`))
      .catch(setError);
  };
  return (
    <section className="card s5e1-card s5e1-chat" data-testid="s5e1-chat">
      <p className="s5e1-bubble" data-testid={plan.kind === 'question' ? 's5e1-question' : 's5e1-reply'}>
        <strong>AI ▸ </strong>
        {reply.text}
      </p>
      {plan.kind === 'question' && (
        <>
          <textarea
            // tiêu điểm đặt sẵn vào ô trả lời (giao-dien.md §2 kind question)
            autoFocus
            rows={2}
            aria-label="Trả lời AI"
            data-testid="s5e1-answer"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
          />
          <div className="s5e1-row">
            {field && FIELD_LABEL[field] && (
              <Link className="btn ghost" data-testid="s5e1-open-field" to={`/gv/giao-an/moi?from=${plan.draftId}&focus=${field}`}>
                <Icon name="pencil" />
                Mở trường {FIELD_LABEL[field]}
              </Link>
            )}
            <span className="s5e1-grow" />
            <button type="button" className="btn primary" data-testid="s5e1-answer-send" disabled={!answer.trim()} onClick={send}>
              <Icon name="send" />
              Gửi
            </button>
          </div>
          {error !== null && <ErrorNotice error={error} testId="s5e1-answer-error" />}
        </>
      )}
    </section>
  );
}

/** Nút "chọn để sửa" và khoá / mở khoá của một mục (US-S5E1-04). */
function SectionTools({
  section,
  locked,
  selected,
  onSelect,
  onLock,
}: {
  section: string;
  locked: string[];
  selected: string | null;
  onSelect: (s: string) => void;
  onLock: (s: string) => void;
}) {
  const isLocked = locked.includes(section);
  return (
    <span className="s5e1-tools">
      <button
        type="button"
        className={`btn ghost small${selected === section ? ' on' : ''}`}
        aria-pressed={selected === section}
        data-testid={`s5e1-pick-${section}`}
        onClick={() => onSelect(section)}
      >
        <Icon name="pencil" />
        {selected === section ? 'Đang chọn' : 'chọn để sửa'}
      </button>
      <button
        type="button"
        className="btn ghost small"
        aria-label={`${isLocked ? 'Mở khoá' : 'Khoá'} ${sectionLabel(section)}`}
        aria-pressed={isLocked}
        data-testid={`s5e1-lock-${section}`}
        onClick={() => onLock(section)}
      >
        <Icon name={isLocked ? 'lock' : 'unlock'} />
      </button>
    </span>
  );
}

function LessonView({
  lesson,
  locked,
  selected,
  onSelect,
  onLock,
}: {
  lesson: LessonBody;
  locked: string[];
  selected: string | null;
  onSelect: (s: string) => void;
  onLock: (s: string) => void;
}) {
  const tools = (section: string) => <SectionTools section={section} locked={locked} selected={selected} onSelect={onSelect} onLock={onLock} />;
  const [handled, setHandled] = useState<Record<number, 'accepted' | 'skipped'>>({});
  const total = lesson.tongPhut;
  const pending = lesson.deXuat.filter((_, i) => !handled[i]);
  return (
    <>
      <section className="card s5e1-card" data-testid="s5e1-sec-muc-tieu">
        <h2 className="s5e1-h">
          {locked.includes('muc_tieu') && <Icon name="lock" />} I. Mục tiêu
          <span className="s5e1-grow" />
          {tools('muc_tieu')}
        </h2>
        <ul>
          {lesson.mucTieu.kienThuc.map((k, i) => (
            <li key={i} data-math>
              {k.noi_dung} <span className="s5e1-mono small">{k.kt.join(', ')}</span>
            </li>
          ))}
          {lesson.mucTieu.nangLuc.map((k, i) => (
            <li key={`nl${i}`}>Năng lực: {k}</li>
          ))}
          {lesson.mucTieu.phamChat.map((k, i) => (
            <li key={`pc${i}`}>Phẩm chất: {k}</li>
          ))}
        </ul>
      </section>
      <section className="card s5e1-card" data-testid="s5e1-sec-thiet-bi">
        <h2 className="s5e1-h">
          {locked.includes('thiet_bi') && <Icon name="lock" />} II. Thiết bị
          <span className="s5e1-grow" />
          {tools('thiet_bi')}
        </h2>
        <p>Giáo viên: {lesson.thietBi.giaoVien.join(', ')}</p>
        <p>Học sinh: {lesson.thietBi.hocSinh.join(', ')}</p>
      </section>
      <section className="card s5e1-card" data-testid="s5e1-sec-tien-trinh">
        <h2 className="s5e1-h">III. Tiến trình</h2>
        <ol className="s5e1-acts">
          {lesson.hoatDong.map((a) => (
            <li key={a.ma} className="s5e1-act" data-testid={`s5e1-hd-${a.ma}`}>
              <div className="s5e1-row">
                <span className="s5e1-min" style={{ flexBasis: `${Math.max(8, (a.thoiLuongPhut / Math.max(total, 1)) * 100)}%` }}>
                  {a.oNha ? 'ở nhà' : `${a.thoiLuongPhut}'`}
                </span>
                <strong data-testid={`s5e1-hd-ten-${a.ma}`}>
                  {locked.includes(a.ma) && <Icon name="lock" inline />} {a.nhan} {a.loaiLabel}: {a.ten}
                </strong>
                <span className="s5e1-grow" />
                {tools(a.ma)}
                {a.unsure.length > 0 && (
                  <span className="chip s5e1-unsure" data-testid={`s5e1-unsure-${a.ma}`} title={a.unsure.join('\n')}>
                    AI chưa chắc
                  </span>
                )}
                {a.chuaSoat.map((c) => (
                  <span key={c} className="chip s5e1-unreviewed" data-testid={`s5e1-chua-soat-${a.ma}`}>
                    {c}
                  </span>
                ))}
              </div>
              <p className="small" data-testid={`s5e1-hd-noi-dung-${a.ma}`}>
                {a.noiDung}
              </p>
            </li>
          ))}
        </ol>
        <p className="s5e1-total" data-testid="s5e1-total">
          Σ {total}' {total % 45 === 0 ? '✓' : ''}
        </p>
      </section>
      {lesson.phieuHocTap.map((p) => (
        <section key={p.ma} className="card s5e1-card" data-testid={`s5e1-pht-${p.ma}`}>
          <h2 className="s5e1-h">
            IV. Phiếu học tập {p.ma} ({p.cau.length} câu)
            <span className="s5e1-grow" />
            {tools(p.ma)}
          </h2>
          <ol>
            {p.cau.map((q) => (
              <li key={q.so} data-testid={`s5e1-cau-${p.ma}-${q.so}`}>
                <span className="s5e1-mono small">[{q.mucDo}] {q.nguon}</span> <span data-math>{q.de}</span>
                <div className="small">
                  Đáp số: {q.dapSo ? <span data-math>{q.dapSo}</span> : '(đáp số chưa kiểm chứng)'} ·{' '}
                  <span className="chip">{q.nhanMay}</span>
                </div>
              </li>
            ))}
          </ol>
        </section>
      ))}
      {lesson.unsure.length > 0 && (
        <p className="small muted" data-testid="s5e1-unsure-general">
          AI chưa chắc: {lesson.unsure.join(' · ')}
        </p>
      )}
      {lesson.deXuat.length > 0 && (
        <section className="card s5e1-card s5e1-proposals" data-testid="s5e1-proposals">
          <h2 className="s5e1-h">⚠ AI đề xuất ({pending.length})</h2>
          {lesson.deXuat.map((d, i) =>
            handled[i] ? null : (
              <div key={i} className="s5e1-row" data-testid={`s5e1-proposal-${i}`}>
                <span className="s5e1-grow">
                  ▸ <strong>{d.viTri}</strong>: {d.vanDe} — {d.goiY} <span className="small muted">({d.lyDo})</span>
                </span>
                <button type="button" className="btn ghost" data-testid={`s5e1-proposal-accept-${i}`} onClick={() => setHandled({ ...handled, [i]: 'accepted' })}>
                  Nhận
                </button>
                <button type="button" className="btn ghost" data-testid={`s5e1-proposal-skip-${i}`} onClick={() => setHandled({ ...handled, [i]: 'skipped' })}>
                  Bỏ qua
                </button>
              </div>
            ),
          )}
          {Object.values(handled).includes('accepted') && (
            <p className="small muted">Đã ghi nhận đề xuất; giáo án chỉ đổi khi thầy cô sửa hoặc vá bằng chat.</p>
          )}
        </section>
      )}
    </>
  );
}
