import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { formatVnDateTime as formatVn } from '../../shared/time/vnTime.ts';
import { ExamExportPanel } from './export/ExamExportPanel.tsx';
import { examApi, MACHINE_LABEL, TYPE_LABEL, vn, type ExamSetDto, type QuestionDto } from './examApi.ts';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Bộ đề nháp (US-S5E2-04): xem từng mã đề, "Thay câu" lấy câu khác cùng ô (máy chủ kiểm lại ràng buộc cả mã), nhãn
 * hai trục — người "Máy lắp" / "Giáo viên đã duyệt" cho cả bộ, máy "Đã kiểm chứng" theo từng câu (QS5-08) — và
 * "Duyệt bộ đề" đưa vào ngăn "Của tôi".
 */
export function ExamSetPage() {
  const { id = '' } = useParams();
  const [set, setSet] = useState<ExamSetDto | null>(null);
  const [code, setCode] = useState('101');
  const [error, setError] = useState<unknown>(null);
  const [busy, setBusy] = useState<number | 'approve' | null>(null);

  useEffect(() => {
    examApi
      .get(id)
      .then((s) => {
        setSet(s);
        setCode(s.variants?.[0]?.code ?? '101');
      })
      .catch(setError);
  }, [id]);

  async function act(kind: number | 'approve') {
    setBusy(kind);
    setError(null);
    try {
      setSet(kind === 'approve' ? await examApi.approve(id) : await examApi.replace(id, code, kind));
    } catch (e) {
      setError(e);
    } finally {
      setBusy(null);
    }
  }

  if (!set) return error ? <ErrorNotice error={error} testId={tid('s5e2', 'error')} /> : <p className="muted">Đang tải…</p>;
  const variant = set.variants?.find((v) => v.code === code);
  const draft = set.status === 'draft';
  return (
    <section className="card s5e2-page" data-testid={tid('s5e2', 'set')}>
      <p className="small">
        <Link to="/gv/ra-de">‹ Ra đề theo ma trận</Link>
      </p>
      <h1 className="s5e2-title">{set.name}</h1>
      <p className="s5e2-meta">
        <span className={draft ? 'chip yel' : 'chip grn'} data-testid={tid('s5e2', 'nhan-nguoi')}>
          {set.humanLabel}
        </span>{' '}
        <span className="small muted">
          seed {set.seed} · độ khó các mã lệch {vn((set.difficultySpread ?? 0) * 100)}%
          {set.approvedAt ? ` · duyệt lúc ${formatVn(set.approvedAt)}` : ''}
        </span>
      </p>
      <div className="s5e2-tabs" role="tablist">
        {set.variants?.map((v) => (
          <button key={v.code} type="button" role="tab" aria-selected={v.code === code} className={v.code === code ? 'btn' : 'btn ghost'}
            onClick={() => setCode(v.code)} data-testid={tid('s5e2', 'ma', v.code)}>
            Mã {v.code}
          </button>
        ))}
      </div>
      {error ? <ErrorNotice error={error} testId={tid('s5e2', 'error')} /> : null}
      <ol className="s5e2-questions" data-testid={tid('s5e2', 'cau-list')}>
        {variant?.cau.map((q) => (
          <Question key={`${q.so}-${q.id_cau}`} q={q} draft={draft} busy={busy === q.so} onReplace={() => act(q.so)} />
        ))}
      </ol>
      {draft && (
        <button type="button" className="btn" disabled={busy !== null} onClick={() => act('approve')} data-testid={tid('s5e2', 'duyet')}>
          <Icon name="check" /> Duyệt bộ đề
        </button>
      )}
      {/* US-S5E2-05: xuất DOCX/PDF (T-261128-54) */}
      <ExamExportPanel setId={set.id} codes={(set.variants ?? []).map((v) => v.code)} />
    </section>
  );
}

function Question({ q, draft, busy, onReplace }: { q: QuestionDto; draft: boolean; busy: boolean; onReplace: () => void }) {
  return (
    <li className="s5e2-q" data-testid={tid('s5e2', 'cau', q.so)} data-id-cau={q.id_cau} data-chu-de={q.chu_de} data-dang={q.dang_tx}
      data-muc-do={q.muc_do} data-diem={q.diem}>
      <div className="s5e2-q-head">
        <strong>Câu {q.so}.</strong>
        <span className="small muted">
          {TYPE_LABEL[q.loai_cau] ?? q.loai_cau} · {q.muc_do} · {vn(q.diem)} điểm · ô {q.o}
        </span>
        <span className="chip grn" data-testid={tid('s5e2', 'cau', q.so, 'nhan-may')}>
          {MACHINE_LABEL[q.nhan_may] ?? q.nhan_may}
        </span>
        {draft && (
          <button type="button" className="btn ghost sm" disabled={busy} onClick={onReplace} data-testid={tid('s5e2', 'cau', q.so, 'thay')}>
            <Icon name="redo" /> Thay câu
          </button>
        )}
      </div>
      <p className="s5e2-math" data-math>
        <MathText text={q.de_bai} />
      </p>
      {q.phuong_an && (
        <ul className="s5e2-options" data-math>
          {(['A', 'B', 'C', 'D'] as const).map((k) => (
            <li key={k}>
              <strong>{k}.</strong> <MathText text={q.phuong_an?.[k]} />
            </li>
          ))}
        </ul>
      )}
      {q.y && (
        <ul className="s5e2-options" data-math>
          {q.y.map((y) => (
            <li key={y.nhan}>
              {y.nhan}) <MathText text={y.menh_de} />
            </li>
          ))}
        </ul>
      )}
    </li>
  );
}
