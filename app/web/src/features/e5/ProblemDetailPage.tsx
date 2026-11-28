import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { errorText } from '../e1/messages.ts';
import { KwText, LectureProvider } from '../e4/Lecture.tsx';
import { e5Api } from './api.ts';
import type { ProblemDetail } from './api.ts';
import { levelLabel } from './labels.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon } from '../../shared/ui/icons.ts';

/** MSG-GEO-54 (05_dictionary.md). */
export const MSG_54 = 'Đề này chưa có lời giải.';

/**
 * Trang "Chi tiết đề & giao bài" của giáo viên (US-GEO-503; thay trang tạm của E2, giữ data-testid `e2-problem-…`
 * mà ca E2 dùng). Bốn phần: hình 3D xoay được, đáp số, lời giải từng bước, Gợi ý 1 và 2 (BR-GEO-208). Giáo viên thấy
 * lời giải đầy đủ bất kể giờ mở của bài giao nào. Không có "Trình chiếu", "Sửa (sắp có)" (BR-GEO-210), không có điều
 * khiển đổi nhãn máy (BR-GEO-135). Thứ tự DOM = thứ tự trên điện thoại: đề, hình, lời giải, gợi ý, giao bài.
 * Chỗ cắm E4: khối "Lời giải" ở đây là bản đơn giản (đọc dữ liệu đề); lời giải đồng bộ hình của E4 thay vào
 * <SolutionBlock> khi có (không sửa module của E4).
 */
export function ProblemDetailPage() {
  const { versionId = '' } = useParams();
  const [loaded, setLoaded] = useState<{ id: string; p?: ProblemDetail; error?: unknown } | null>(null);

  useEffect(() => {
    let alive = true;
    e5Api
      .detail(versionId)
      .then((p) => alive && setLoaded({ id: versionId, p }))
      .catch((e: unknown) => alive && setLoaded({ id: versionId, error: e }));
    return () => {
      alive = false;
    };
  }, [versionId]);

  const cur = loaded?.id === versionId ? loaded : null;
  const p = cur?.p ?? null;
  const error = cur?.error ?? null;
  const solid = useMemo(() => (p?.scene ? fromScene(p.scene) : null), [p]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e2', 'problem', 'error')}
        data-error-code={error instanceof ApiError ? error.code : 'UNKNOWN'}>
        {errorText(error)}
      </div>
    );
  }
  if (!p) return <p className="muted">Đang tải…</p>;
  return (
    <div className="e5-detail" data-testid={tid('e2', 'problem-detail')} data-code={p.code}>
      <header className="e5-detail-head">
        <h2>Chi tiết đề &amp; giao bài</h2>
        <p className="e5-card-chips">
          <b data-testid={tid('e5', 'detail', 'code')}>{p.code}</b>
          <span className="chip pri">{levelLabel(p.level)}</span>
          <span className="chip">{p.topic}</span>
          <span className={`chip ${p.verifyStatus === 'verified' ? 'grn' : 'yel'}`} data-testid={tid('e2', 'problem', 'label')}
            data-status={p.verifyStatus}><Icon name={verifyIcon(p.verifyStatus === 'verified')} />
            {p.label}
          </span>
        </p>
      </header>
      <section className="card e5-part e5-part-statement" data-testid={tid('e5', 'part', 'statement')}>
        <h3>Đề bài</h3>
        <p className="e1-statement" data-testid={tid('e2', 'problem', 'statement')}>
          {p.statement}
        </p>
      </section>
      <section className="card e5-part e5-part-figure" data-testid={tid('e5', 'part', 'figure')}>
        <h3>Hình</h3>
        {solid ? (
          <div className="e5-viewer">
            <Figure3DViewer solid={solid} lock="open" readOnly className="e5-viewer-inner" />
          </div>
        ) : (
          <p className="muted">Không có hình vẽ cho bài này.</p>
        )}
      </section>
      <SolutionBlock p={p} />
      {/* Gợi ý 1, 2; từ khóa bấm được mở hộp bài giảng của E4 (US-GEO-411, 412-SC4) */}
      <LectureProvider>
        <section className="card e5-part e5-part-hints" data-testid={tid('e5', 'part', 'hints')}>
          <h3>Gợi ý</h3>
          <div data-testid={tid('e4', 'teacher', 'hints')}>
            {p.hints.map((h, i) => (
              <div key={i} className="e5-hint" data-testid={tid('e5', 'hint', i + 1)}>
                <b>Gợi ý {i + 1}</b>
                <p>
                  <KwText text={h} testId={tid('e4', 'teacher', 'hint', i + 1)} />
                </p>
              </div>
            ))}
          </div>
        </section>
      </LectureProvider>
      <section className="card e5-part e5-part-assign" data-testid={tid('e5', 'part', 'assign')}>
        <Link className="btn" to={`/gv/giao-bai/${p.versionId}`} data-testid={tid('e5', 'assign')}><Icon name="assign" />
          Giao bài
        </Link>{' '}
        <Link className="btn ghost" to="/gv/kho-de"><Icon name="prev" />
          Về Kho đề
        </Link>
      </section>
    </div>
  );
}

/** Khối "Lời giải": đáp số, chip khớp tính toán (BR-GEO-209), các bước kèm "vì sao", dòng tác giả (BR-GEO-297). */
function SolutionBlock({ p }: { p: ProblemDetail }) {
  const s = p.solution;
  return (
    <section className="card e5-part e5-part-solution" data-testid={tid('e5', 'part', 'solution')}>
      <h3>Lời giải</h3>
      <p className="tiny muted" data-testid={tid('e5', 'solution', 'author')}>
        AI soạn · tài liệu của {s?.author ?? p.author}
      </p>
      {!s ? (
        <p className="muted" data-testid={tid('e5', 'solution', 'empty')}>
          {MSG_54}
        </p>
      ) : (
        <>
          <p className="e5-answer">
            Đáp số: <b data-testid={tid('e5', 'answer')}>{s.finalAnswer}</b>
          </p>
          {p.answerMatch ? (
            <p>
              <span className="chip grn" data-testid={tid('e5', 'answer-match')}><Icon name="check" />
                Đáp số khớp tính toán: {p.answerMatch}
              </span>
            </p>
          ) : null}
          <ol className="e5-steps">
            {s.steps.map((st, i) => (
              <li key={i} data-testid={tid('e5', 'step', i + 1)}>
                <b>{st.title}</b>
                <p className="e5-step-body">{st.body}</p>
                {st.why ? <p className="tiny muted e5-step-why">Vì sao: {st.why}</p> : null}
              </li>
            ))}
          </ol>
        </>
      )}
    </section>
  );
}
