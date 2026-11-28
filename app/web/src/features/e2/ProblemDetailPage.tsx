/** Trang chi tiết đề của giáo viên đã chuyển sang E5 (features/e5/ProblemDetailPage.tsx); tệp này giữ hình bài giao. */
import { useEffect, useState } from 'react';
import type { Scene } from '../../shared/scene/types.ts';
import { e2Api } from './api.ts';
import { SceneFrame } from './SceneFrame.tsx';

/** Hình của một bài giao trên màn Làm bài (E1 nhúng): thẻ ràng buộc ở góc dưới trái (US-GEO-201-SC3). */
export function AssignmentScene({ assignmentId }: { assignmentId: string }) {
  // hình gắn với mã bài đã tải: đổi bài thì không hiện hình của bài cũ trong lúc chờ (soát E2 #8)
  const [loaded, setLoaded] = useState<{ id: string; scene: Scene | null } | null>(null);
  useEffect(() => {
    let alive = true;
    e2Api
      .assignmentScene(assignmentId)
      .then((s) => alive && setLoaded({ id: assignmentId, scene: s }))
      .catch(() => alive && setLoaded({ id: assignmentId, scene: null }));
    return () => {
      alive = false;
    };
  }, [assignmentId]);
  const scene = loaded?.id === assignmentId ? loaded.scene : null;
  return scene ? <SceneFrame scene={scene} /> : null;
}
