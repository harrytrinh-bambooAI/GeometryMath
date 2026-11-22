import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import type { Scene } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { errorText } from '../e1/messages.ts';
import { e2Api } from './api.ts';
import type { TeacherProblem } from './api.ts';
import { SceneFrame } from './SceneFrame.tsx';

/**
 * Trang chi tiết đề của giáo viên (US-GEO-203-SC6, FC4): đề, nhãn máy đúng chuỗi "Đã kiểm chứng" / "Cần xem lại", hình.
 * KHÔNG có điều khiển đổi nhãn máy (nhãn chỉ engine tính, BR-GEO-135).
 */
export function ProblemDetailPage() {
  const { versionId = '' } = useParams();
  const [p, setP] = useState<TeacherProblem | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let alive = true;
    e2Api
      .teacherProblem(versionId)
      .then((x) => alive && setP(x))
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [versionId]);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('e2', 'problem', 'error')}
        data-error-code={error instanceof ApiError ? error.code : 'UNKNOWN'}>
        {errorText(error)}
      </div>
    );
  }
  if (!p) return <p className="muted">Đang tải…</p>;
  return (
    <div data-testid={tid('e2', 'problem-detail')}>
      <h2>
        {p.code}{' '}
        <span className={`chip ${p.verifyStatus === 'verified' ? 'grn' : 'yel'}`} data-testid={tid('e2', 'problem', 'label')}
          data-status={p.verifyStatus}>
          {p.label}
        </span>
      </h2>
      <p className="e1-statement" data-testid={tid('e2', 'problem', 'statement')}>
        {p.statement}
      </p>
      {p.scene ? <SceneFrame scene={p.scene as Scene} /> : <p className="muted">Không có hình vẽ cho bài này.</p>}
      <p>
        <Link className="btn ghost" to="/gv/kho-de">
          Về Kho đề
        </Link>
      </p>
    </div>
  );
}

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
