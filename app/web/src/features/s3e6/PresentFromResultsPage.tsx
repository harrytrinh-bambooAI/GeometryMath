import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MSG, s3e6Api, type Presentation } from './api.ts';
import { MathText } from '../../shared/math/MathText.tsx';

type Phase = { k: 'loading' } | { k: 'ready'; data: Presentation } | { k: 'error'; message: string };

/**
 * US-S3E6-01: Trình chiếu mở từ thẻ "Cả lớp vướng ở đâu" — đúng đề, bắt đầu ở bước em dừng nhiều nhất (BR-S3E6-02).
 * Chỉ có đề và các bước lời giải, không tên, mã hay số liệu theo em (BR-S3E6-04). Thoát về Kết quả lớp đúng bộ, đúng
 * thẻ. Mở (POST) ghi một sự kiện đo M-S3-04 ở máy chủ (gộp các lần trong 5 giây).
 */
export function PresentFromResultsPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const lop = params.get('lop') ?? '';
  const bo = params.get('bo') ?? '';
  const de = Number(params.get('de') ?? '0');
  const buoc = params.get('buoc');
  const [phase, setPhase] = useState<Phase>({ k: 'loading' });
  const [shown, setShown] = useState(0);

  useEffect(() => {
    let live = true;
    s3e6Api
      .present(lop, bo, de, buoc ? Number(buoc) : null)
      .then((data) => {
        if (!live) return;
        setShown(data.startStep);
        setPhase({ k: 'ready', data });
      })
      .catch((e: unknown) => {
        if (!live) return;
        const offline = e instanceof ApiError && e.code === 'NETWORK';
        setPhase({ k: 'error', message: offline ? MSG.offline : e instanceof ApiError ? e.message : MSG.offline });
      });
    return () => {
      live = false;
    };
  }, [lop, bo, de, buoc]);

  const exit = () => navigate(`/gv/ket-qua-lop?${new URLSearchParams({ lop, bo, the: String(de) }).toString()}`);

  if (phase.k === 'loading') {
    return (
      <div className="s3e6-present" data-testid={tid('s3e6', 'present', 'loading')}>
        <p className="muted">Đang mở Trình chiếu…</p>
      </div>
    );
  }
  if (phase.k === 'error') {
    return (
      <div className="s3e6-present" data-testid={tid('s3e6', 'present', 'error')}>
        <div className="alert" role="alert">
          {phase.message}
        </div>
        <button type="button" className="btn" onClick={exit} data-testid={tid('s3e6', 'present', 'back')}>
          <Icon name="back" /> Về Kết quả lớp
        </button>
      </div>
    );
  }
  const d = phase.data;
  const total = d.steps.length;
  return (
    <div className="s3e6-present" data-testid={tid('s3e6', 'present')} data-step={shown}>
      <header className="s3e6-present-head">
        <h1 className="s3e6-present-title" data-testid={tid('s3e6', 'present', 'title')}>
          {d.title}
        </h1>
        {d.locked ? (
          <span className="chip yel" data-testid={tid('s3e6', 'present', 'locked')}>
            <Icon name="lock" /> {MSG.chip}
          </span>
        ) : null}
        <button type="button" className="btn ghost" onClick={exit} data-testid={tid('s3e6', 'present', 'exit')}>
          <Icon name="x" /> Thoát
        </button>
      </header>
      {d.notice ? (
        <div className="s3e6-toast" role="status" data-testid={tid('s3e6', 'present', 'notice')}>
          {d.notice}
        </div>
      ) : null}
      <p className="s3e6-statement" data-math data-testid={tid('s3e6', 'present', 'statement')}>
        <MathText text={d.statement} />
      </p>
      <ol className="s3e6-steps" data-testid={tid('s3e6', 'present', 'steps')}>
        {d.steps.slice(0, shown).map((s, i) => (
          <li key={i} className="s3e6-step" data-math data-testid={`s3e6-present-step-${i + 1}`}>
            <MathText text={s} />
          </li>
        ))}
      </ol>
      <footer className="s3e6-row">
        <button
          type="button"
          className="btn ghost"
          disabled={shown <= 1}
          onClick={() => setShown((n) => Math.max(1, n - 1))}
          data-testid={tid('s3e6', 'present', 'prev')}
        >
          <Icon name="prev" /> Bước trước
        </button>
        <span className="muted" data-testid={tid('s3e6', 'present', 'counter')}>
          Bước {shown}/{total}
        </span>
        <button
          type="button"
          className="btn"
          disabled={shown >= total}
          onClick={() => setShown((n) => Math.min(total, n + 1))}
          data-testid={tid('s3e6', 'present', 'next')}
        >
          Hiện bước kế <Icon name="next" />
        </button>
      </footer>
    </div>
  );
}
