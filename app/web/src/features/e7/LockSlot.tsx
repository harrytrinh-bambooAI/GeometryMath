import { useEffect, useState } from 'react';
import { tid } from '../../shared/testid.ts';
import { ReleaseCard } from '../e6/ReleaseCard.tsx';
import { remaining } from './format.ts';

/**
 * Thẻ vàng "Lời giải đang khóa" của Theo dõi lớp (BR-GEO-269): bài Chưa đặt giờ hoặc Sắp mở; bài Đã mở không có thẻ.
 * Thẻ, dòng "Mở lúc …" và hai nút "Mở ngay", "Đổi giờ" là của E6 (ReleaseCard, US-GEO-605); E7 thêm "còn {thời gian}".
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
  onChanged?: () => void;
}) {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const t = window.setInterval(() => setNow(Date.now()), 30_000);
    return () => window.clearInterval(t);
  }, []);
  // FIX4b M1: cùng một khung cho mọi trạng thái để thẻ E6 không bị dựng lại (giữ thông báo "Đã mở…") khi trang tải lại
  // sau "Mở ngay"/"Đổi giờ"; bài Đã mở thì khung không còn là thẻ khóa (không có e7-lock, BR-GEO-269).
  const open = releaseState === 'open';
  return (
    <div
      className={open ? undefined : 'e7-lock'}
      data-testid={open ? tid('e7', 'release') : tid('e7', 'lock')}
      data-state={releaseState}
    >
      <ReleaseCard assignmentId={assignmentId} onChanged={onChanged} />
      {!open && releaseAt ? (
        <p className="small muted" data-testid={tid('e7', 'lock', 'left')}>
          {remaining(new Date(releaseAt).getTime() - now)}
        </p>
      ) : null}
    </div>
  );
}
