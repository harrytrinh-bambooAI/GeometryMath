import { useCallback, useEffect, useRef } from 'react';
import type { ToolEvent } from '../../shared/viewer/Figure3DViewer.tsx';
import { sendToolEvents, type ToolEventRow } from './api.ts';

/**
 * Gom sự kiện thao tác trên hình thành lô (mức A, ADR-GEO-11): gửi sau 600 ms yên lặng, hoặc khi rời trang
 * (fetch keepalive). Xoay, thu phóng, đặt lại không bao giờ vào đây (bộ xem không phát, BR-GEO-144).
 */
export function useToolEvents(assignmentId: string | null) {
  const queue = useRef<ToolEventRow[]>([]);
  const timer = useRef<number | undefined>(undefined);
  // lô gửi lỗi chờ gửi lại NGUYÊN mã lô (máy chủ bỏ qua lô đã ghi, không đếm trùng; soát E3 #6)
  const retry = useRef<Array<{ batchId: string; events: ToolEventRow[] }>>([]);

  const flush = useCallback(
    (keepalive = false) => {
      window.clearTimeout(timer.current);
      if (!assignmentId) return;
      const batches = retry.current.splice(0, keepalive ? retry.current.length : 1);
      // rời trang: gửi cả lô chờ gửi lại lẫn sự kiện mới; bình thường: mỗi lần một lô
      if (queue.current.length > 0 && (keepalive || batches.length === 0)) {
        batches.push({ batchId: crypto.randomUUID(), events: queue.current.splice(0, 200) });
      }
      for (const batch of batches) {
        void sendToolEvents(assignmentId, batch.events, keepalive, batch.batchId).catch(() => {
          retry.current.push(batch); // thử lại ở lần gửi sau, cùng mã lô
        });
      }
    },
    [assignmentId],
  );

  useEffect(() => {
    const onHide = () => flush(true);
    window.addEventListener('pagehide', onHide);
    return () => {
      window.removeEventListener('pagehide', onHide);
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
