import { useEffect, useState } from 'react';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { lockLine } from './format.ts';

/**
 * Thẻ vàng "Lời giải đang khóa" của Theo dõi lớp (BR-GEO-269): bài Chưa đặt giờ hoặc Sắp mở; bài Đã mở không có thẻ.
 * Hai nút "Mở ngay", "Đổi giờ" theo hành vi US-GEO-605 (E6).
 */
export function LockSlot({
  assignmentId,
  releaseState,
  releaseAt,
  onChanged,
}: {
  assignmentId: string;
  releaseState: string;
  releaseAt: string | null;
  onChanged: () => void;
}) {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const t = window.setInterval(() => setNow(Date.now()), 30_000);
    return () => window.clearInterval(t);
  }, []);
  if (releaseState === 'open') return null;
  return (
    <section className="card e7-lock" data-testid={tid('e7', 'lock')} data-state={releaseState}>
      <h3>Lời giải đang khóa</h3>
      <p className="small" data-testid={tid('e7', 'lock', 'line')}>
        {lockLine(releaseAt, now)}
      </p>
      <div className="row">
        <button
          type="button"
          className="btn primary"
          data-testid={tid('e7', 'open-now')}
          onClick={() =>
            void api.post(`/api/v1/teacher/assignments/${encodeURIComponent(assignmentId)}/release`, { openNow: true }).then(onChanged)
          }
        >
          Mở ngay
        </button>
        <button type="button" className="btn ghost" data-testid={tid('e7', 'change-time')}>
          Đổi giờ
        </button>
      </div>
    </section>
  );
}
