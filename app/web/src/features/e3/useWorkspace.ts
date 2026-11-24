import { useEffect, useMemo, useState } from 'react';
import type { Scene } from '../../shared/scene/types.ts';
import { fromScene } from '../../shared/viewer/adapter.ts';
import type { LockState, ViewerSolid } from '../../shared/viewer/types.ts';
import { e3Api } from './api.ts';

/** Chu kỳ hỏi lại trạng thái khóa (US-GEO-305-SC6: trang nhận Đã mở không muộn hơn 10 giây, không tải lại). */
export const LOCK_POLL_MS = 5000;

export interface WorkspaceData {
  kind: 'class' | 'self';
  title: string;
  subtitle: string;
  statement: string;
  scene: Scene;
  solid: ViewerSolid;
}

/**
 * Nạp dữ liệu trang Làm bài: bài giao của lớp (`/student/assignments/{id}`) hoặc bài tự học
 * (`/student/self-study/{id}`, khi id không phải bài lớp). Cảnh theo hợp đồng E2 v1; ghi sự kiện mở bài một lần.
 */
export function useWorkspace(id: string) {
  const [data, setData] = useState<WorkspaceData | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      let info: Omit<WorkspaceData, 'solid' | 'scene'> & { scene?: Scene };
      try {
        const a = await e3Api.assignment(id);
        info = { kind: 'class', title: a.problemCode, subtitle: `Bài được giao · lớp ${a.className}`, statement: a.statement }; // E6 US-GEO-606-SC5
      } catch (e) {
        try {
          const s = await e3Api.selfStudy(id);
          info = { kind: 'self', title: 'Tự học', subtitle: 'Bài tự học', statement: s.statement, scene: s.scene };
        } catch {
          throw e;
        }
      }
      const scene = info.scene ?? (await e3Api.scene(id));
      if (!alive) return;
      setData({ ...info, scene, solid: fromScene(scene) });
      void e3Api.open(id, crypto.randomUUID()).catch(() => undefined);
    })().catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [id]);

  return { data, error };
}

/** Trạng thái khóa lời giải, hỏi lại mỗi 5 giây; mặc định 'locked' tới khi máy chủ trả lời (an toàn, BR-GEO-156). */
export function useLockState(id: string, enabled: boolean): LockState {
  const [locked, setLocked] = useState(true);
  useEffect(() => {
    if (!enabled) return;
    let alive = true;
    const tick = () =>
      e3Api
        .lock(id)
        .then((l) => alive && setLocked(l.locked))
        .catch(() => undefined);
    void tick();
    const t = window.setInterval(tick, LOCK_POLL_MS);
    return () => {
      alive = false;
      window.clearInterval(t);
    };
  }, [id, enabled]);
  return useMemo(() => (locked ? 'locked' : 'open'), [locked]);
}
