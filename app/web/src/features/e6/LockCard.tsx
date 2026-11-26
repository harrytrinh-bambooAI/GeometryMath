import { useEffect, useState } from 'react';
import { tid } from '../../shared/testid.ts';
import { M, msg27, remaining } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/**
 * Thẻ khóa của tab "Lời giải" (khung Design/mvp-05): Sắp mở → MSG-GEO-27 "Lời giải mở lúc {HH:mm, dd/MM/yyyy}" và dòng
 * phụ "Do giáo viên của em đặt. Còn …" (đếm ngược theo GIỜ MÁY CHỦ: offset = serverNow − đồng hồ thiết bị lúc nhận, nên
 * đồng hồ thiết bị sai không làm sai; không bao giờ âm); Chưa đặt giờ → MSG-GEO-28; Đã mở → MSG-GEO-29 (màn xem trước).
 */
export function LockCard(props: { state: string; releaseAt?: string | null; serverNow?: string | null; receivedAt?: number }) {
  const { state, releaseAt, serverNow, receivedAt } = props;
  const [tick, setTick] = useState(() => Date.now());
  useEffect(() => {
    if (state !== 'upcoming') return;
    const t = window.setInterval(() => setTick(Date.now()), 1000);
    return () => window.clearInterval(t);
  }, [state]);

  if (state === 'open') {
    return (
      <div className="card e6-lock" data-testid={tid('e6', 'lock')} data-state="open">
        <strong data-testid={tid('e6', 'lock', 'title')}><Icon name="unlock" inline />{M['29']}</strong>
      </div>
    );
  }
  if (state === 'upcoming' && releaseAt) {
    const offset = serverNow && receivedAt ? new Date(serverNow).getTime() - receivedAt : 0;
    const left = new Date(releaseAt).getTime() - (tick + offset);
    return (
      <div className="card e6-lock" data-testid={tid('e6', 'lock')} data-state="upcoming">
        <strong data-testid={tid('e6', 'lock', 'title')}><Icon name="schedule" inline />{msg27(releaseAt)}</strong>
        <p className="small" data-testid={tid('e6', 'lock', 'sub')}>
          {M['27sub']} <span data-testid={tid('e6', 'lock', 'left')}>{remaining(left)}.</span>
        </p>
      </div>
    );
  }
  return (
    <div className="card e6-lock" data-testid={tid('e6', 'lock')} data-state="unset">
      <strong data-testid={tid('e6', 'lock', 'title')}><Icon name="lock" inline />{M['28']}</strong>
      <p className="small" data-testid={tid('e6', 'lock', 'sub')}>
        {M['28sub']}
      </p>
    </div>
  );
}
