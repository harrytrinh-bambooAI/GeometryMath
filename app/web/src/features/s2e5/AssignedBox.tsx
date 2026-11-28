import { useState } from 'react';
import { Icon } from '../../shared/ui/Icon.tsx';
import { ApiError } from '../../shared/api/errors.ts';
import { applyRevision, fetchApplyPreview, type ApplyPreview, type AssignedView, type DocView } from './api.ts';

interface Props {
  doc: DocView;
  /** Phiên bản mới nhất đã "Giáo viên đã duyệt" chưa (nút "Áp dụng cho lớp" chỉ mở khi đã duyệt, BR-S2E5-24). */
  latestApproved: boolean;
  busy: boolean;
  onApplied: (d: DocView) => void;
}

/**
 * US-S2E5-05: hộp "Đã giao" của C2-03. Bản đã giao bất biến; khi có phiên bản mới hơn bản đang giao, mỗi bài giao có
 * hai lựa chọn "Áp dụng cho lớp" / "Giữ vN" (mặc định giữ). Áp dụng khi đã có học sinh xem lời giải cũ hoặc đáp số đổi
 * thì hỏi xác nhận (BR-S2E5-25, 26).
 */
export function AssignedBox({ doc, latestApproved, busy, onApplied }: Props) {
  const [kept, setKept] = useState<Record<string, boolean>>({});
  const [confirm, setConfirm] = useState<ApplyPreview | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [working, setWorking] = useState(false);
  const latest = doc.latestVersion;

  const apply = (assignmentId: string, ok: boolean) => {
    setWorking(true);
    setError(null);
    applyRevision(doc.id, assignmentId, latest, ok)
      .then((d) => {
        setConfirm(null);
        onApplied(d);
      })
      .catch((e: unknown) => setError(e instanceof ApiError ? e.message : 'Có lỗi, vui lòng thử lại.'))
      .finally(() => setWorking(false));
  };

  const start = (assignmentId: string) => {
    setWorking(true);
    setError(null);
    fetchApplyPreview(doc.id, assignmentId, latest)
      .then((p) => {
        if (p.warnings.length > 0) {
          setConfirm(p);
          setWorking(false);
        } else {
          apply(assignmentId, false);
        }
      })
      .catch((e: unknown) => {
        setError(e instanceof ApiError ? e.message : 'Có lỗi, vui lòng thử lại.');
        setWorking(false);
      });
  };

  return (
    <section className="s2e5-sec" data-testid="s2e5-assigned">
      <h3 className="s2e5-cap">Đã giao</h3>
      {(doc.assignedPins ?? (doc.assignments as AssignedView[])).map((x) => {
        const key = x.assignmentId ?? `${x.className}-${x.version}`;
        const behind = x.assignmentId && x.version < latest;
        return (
          <div key={key} className="s2e5-assigned-item" data-testid={`s2e5-assigned-${x.className}`}>
            <p className="s2e5-sub" data-testid="s2e5-assigned-line">
              Đã giao cho {x.className} (v{x.version})
            </p>
            {behind && !kept[key] && (
              <div className="s2e5-row">
                <button
                  type="button"
                  className="btn sm"
                  disabled={!latestApproved || busy || working}
                  onClick={() => start(x.assignmentId!)}
                  data-testid={`s2e5-apply-${x.className}`}
                >
                  <Icon name="send" />
                  Áp dụng cho lớp
                </button>
                <button
                  type="button"
                  className="btn ghost sm"
                  disabled={busy || working}
                  onClick={() => setKept({ ...kept, [key]: true })}
                  data-testid={`s2e5-keep-${x.className}`}
                >
                  Giữ v{x.version}
                </button>
              </div>
            )}
            {behind && !kept[key] && !latestApproved && (
              <p className="s2e5-sub muted" data-testid={`s2e5-apply-hint-${x.className}`}>
                Duyệt v{latest} trước
              </p>
            )}
            {behind && kept[key] && (
              <p className="s2e5-sub muted" data-testid={`s2e5-kept-${x.className}`}>
                Giữ v{x.version} cho {x.className}; v{latest} chỉ nằm ở "Của tôi".
              </p>
            )}
          </div>
        );
      })}
      <p className="s2e5-sub muted" data-testid="s2e5-assigned-note">
        Bản đã giao không sửa được, sửa sẽ tạo phiên bản mới
      </p>
      {error && (
        <p className="alert" role="alert" data-testid="s2e5-apply-error">
          {error}
        </p>
      )}
      {confirm && (
        <div className="s2e5-overlay">
          <div className="card s2e5-dialog" role="alertdialog" aria-modal="true" aria-labelledby="s2e5-apply-title" data-testid="s2e5-apply-confirm">
            <p id="s2e5-apply-title" className="s2e5-dialog-title">
              Áp dụng v{confirm.toVersion} cho {confirm.className}?
            </p>
            {confirm.warnings.map((w) => (
              <p key={w} className="s2e5-sub" data-testid="s2e5-apply-warning">
                {w}
              </p>
            ))}
            <div className="s2e5-row">
              <button type="button" className="btn" disabled={working} onClick={() => apply(confirm.assignmentId, true)} data-testid="s2e5-apply-yes">
                Vẫn áp dụng
              </button>
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)} data-testid="s2e5-apply-no">
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
