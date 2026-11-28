import { useCallback, useEffect, useRef } from 'react';
import type { ToolEvent } from '../../shared/viewer/Figure3DViewer.tsx';
import { sendToolEvents, type ToolEventRow } from './api.ts';
import { nextAttempt } from './toolRetry.ts';

/**
 * Gom sự kiện thao tác trên hình thành lô (mức A, ADR-GEO-11): gửi sau 600 ms yên lặng, hoặc khi rời trang
 * (fetch keepalive). Xoay, thu phóng, đặt lại không bao giờ vào đây (bộ xem không phát, BR-GEO-144).
 */
export function useToolEvents(assignmentId: string | null) {
  const queue = useRef<ToolEventRow[]>([]);
  const timer = useRef<number | undefined>(undefined);
  // lô gửi lỗi chờ gửi lại NGUYÊN mã lô (máy chủ bỏ qua lô đã ghi, không đếm trùng; soát E3 #6)
  const retry = useRef<Array<{ batchId: string; events: ToolEventRow[]; attempts: number }>>([]);

  const flush = useCallback(
    (keepalive = false) => {
      window.clearTimeout(timer.current);
      if (!assignmentId) return;
      const batches = retry.current.splice(0, keepalive ? retry.current.length : 1);
      // rời trang: gửi cả lô chờ gửi lại lẫn sự kiện mới; bình thường: một lô chờ gửi lại VÀ sự kiện mới (FIX4b m1:
      // sự kiện mới không bị kẹt trong hàng đợi khi còn lô chờ gửi lại)
      if (queue.current.length > 0) {
        batches.push({ batchId: crypto.randomUUID(), events: queue.current.splice(0, 200), attempts: 0 });
      }
      for (const batch of batches) {
        void sendToolEvents(assignmentId, batch.events, keepalive, batch.batchId).catch((e: unknown) => {
          // FIX4b m1: chỉ gửi lại lỗi mạng/5xx/408/429, có giới hạn, cùng mã lô; 4xx khác thì bỏ lô
          const attempts = nextAttempt(e, batch.attempts, navigator.onLine);
          if (attempts !== null) retry.current.push({ ...batch, attempts });
        });
      }
    },
    [assignmentId],
  );

  useEffect(() => {
    const onHide = () => flush(true);
    // E7 (US-GEO-703-FC3, BR-GEO-279): lô gửi lỗi khi mất mạng được gửi lại ngay khi có mạng, và ít nhất mỗi 5 giây,
    // nên thao tác được tính trong vòng 10 giây sau khi có mạng mà không cần thao tác mới.
    const retryNow = () => {
      if (retry.current.length > 0 || queue.current.length > 0) flush(false);
    };
    const t = window.setInterval(retryNow, 5000);
    window.addEventListener('pagehide', onHide);
    window.addEventListener('online', retryNow);
    return () => {
      window.clearInterval(t);
      window.removeEventListener('pagehide', onHide);
      window.removeEventListener('online', retryNow);
      flush(true);
    };
  }, [flush]);

  return useCallback(
    (e: ToolEvent) => {
      queue.current.push({ tool: e.code, at: new Date().toISOString() });
      window.clearTimeout(timer.current);
      timer.current = window.setTimeout(() => flush(false), 600);
    },
    [flush],
  );
}
