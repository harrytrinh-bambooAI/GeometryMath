import { useEffect, useRef, useState } from 'react';
import { e6Api } from './api.ts';
import type { LockInfo } from './api.ts';

/** Chu kỳ hỏi lại trạng thái khóa khi đang chờ (D-GEO-157: ≤ 8 giây, cộng đệm ≤ 2 giây). */
export const RELEASE_POLL_MS = 5000;

/**
 * Theo dõi trạng thái khóa của một bài lớp đang mở trên màn (US-GEO-612): hỏi lại máy chủ mỗi 5 giây khi còn khóa (kể
 * cả khi chưa biết giờ mở mới: "Mở ngay", đổi giờ sớm hơn, Chưa đặt giờ), và hỏi ngay khi trang hiện lại hoặc mạng có
 * lại (D-GEO-132). Không bao giờ tự suy "đã mở" từ đồng hồ thiết bị: chỉ máy chủ quyết. justOpened = trang đã thấy khóa
 * rồi thấy mở (hiện MSG-GEO-55).
 */
export function useReleaseWatch(id: string, enabled: boolean) {
  const [lock, setLock] = useState<LockInfo | null>(null);
  const [receivedAt, setReceivedAt] = useState(0);
  const [justOpened, setJustOpened] = useState(false);
  const sawLocked = useRef(false);

  const open = lock !== null && !lock.locked;
  useEffect(() => {
    if (!enabled || open) return;
    let alive = true;
    const tick = () =>
      e6Api
        .lock(id)
        .then((l) => {
          if (!alive) return;
          setLock(l);
          setReceivedAt(Date.now());
          if (l.locked) sawLocked.current = true;
          else if (sawLocked.current) setJustOpened(true);
        })
        .catch(() => undefined); // mất mạng: giữ thẻ khóa, thử lại ở lần kế
    void tick();
    const t = window.setInterval(tick, RELEASE_POLL_MS);
    const wake = () => {
      if (document.visibilityState === 'visible') void tick();
    };
    document.addEventListener('visibilitychange', wake);
    window.addEventListener('online', wake);
    window.addEventListener('focus', wake);
    return () => {
      alive = false;
      window.clearInterval(t);
      document.removeEventListener('visibilitychange', wake);
      window.removeEventListener('online', wake);
      window.removeEventListener('focus', wake);
    };
  }, [id, enabled, open]);

  return { lock, receivedAt, justOpened };
}
