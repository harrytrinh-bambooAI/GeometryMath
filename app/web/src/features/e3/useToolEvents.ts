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

  const flush = useCallback(
    (keepalive = false) => {
      window.clearTimeout(timer.current);
      if (!assignmentId || queue.current.length === 0) return;
      const batch = queue.current.splice(0, 200);
      void sendToolEvents(assignmentId, batch, keepalive).catch(() => {
        queue.current.unshift(...batch); // thử lại ở lần gửi sau
      });
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
