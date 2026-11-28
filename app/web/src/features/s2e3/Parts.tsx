import { Link } from 'react-router';
import { Scene2DView } from '../../shared/scene2d/Scene2DView.tsx';
import type { Scene2D } from '../../shared/scene2d/geometry2d.ts';
import type { Scene } from '../../shared/scene/types.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import type { IconName } from '../../shared/ui/icons.ts';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { AnswerTable } from '../s2e5/AnswerTable.tsx';
import type { JobView, ResultView } from './api.ts';
import { stageText } from './logic.ts';
import { MathText } from '../../shared/math/MathText.tsx';

const STAGE_ICON: Record<string, IconName> = { read: 'file', build: 'cube', solve: 'spark', verify: 'check' };

/** Màn chờ bốn bước Đọc đề → Dựng hình → Giải → Kiểm chứng (BR-S2E3-11). */
export function Stages({ job, busy, onCancel, onRetry }: { job: JobView; busy: boolean; onCancel: () => void; onRetry: () => void }) {
  return (
    <section className="card s2e3-stages" data-testid="s2e3-stages" data-state={job.state} aria-label="Tiến trình bốn bước">
      <ol className="s2e3-stage-list">
        {job.stages.map((s, i) => (
          <li key={s.key} className={`s2e3-stage ${s.state}`} data-testid={`s2e3-stage-${s.key}`} data-state={s.state}>
            <span className="s2e3-stage-num" aria-hidden="true">
              {s.state === 'running' ? <span className="s2e3-spin" /> : <Icon name={s.state === 'error' ? 'warn' : STAGE_ICON[s.key] ?? 'layers'} />}
            </span>
            <span className="s2e3-stage-body">
              <b>
                {i + 1} · {s.label}
              </b>
              <span className="s2e3-sub" data-testid={`s2e3-stage-state-${s.key}`}>
                {stageText(s.state)}
                {s.note ? ` · ${s.note}` : ''}
              </span>
            </span>
          </li>
        ))}
      </ol>
      <div className="s2e3-row">
        <span className="s2e3-sub muted" data-testid="s2e3-job-state">
          {job.state === 'running' ? 'Đang soạn nháp…' : job.state === 'cancelled' ? 'Đã hủy' : job.state === 'failed' ? 'Dừng ở bước lỗi' : 'Đã xong'}
        </span>
        <span className="s2e3-grow" />
        {job.state === 'running' && (
          <button type="button" className="btn ghost" disabled={busy} onClick={onCancel} data-testid="s2e3-cancel">
            <Icon name="x" />
            Hủy
          </button>
        )}
        {job.retryable && (
          <button type="button" className="btn" disabled={busy} onClick={onRetry} data-testid="s2e3-retry">
            <Icon name="redo" />
            Thử lại bước này
          </button>
        )}
      </div>
    </section>
  );
}

/** Nhãn hai trục của kết quả (PO-02): máy "Đã kiểm chứng" / "Cần xem lại"; người "AI soạn"… */
export function Labels({ machine, human, note }: { machine: string; human?: string; note?: string | null }) {
  const ok = machine === 'Đã kiểm chứng';
  return (
    <div className="s2e3-labels">
      <span className={`chip ${ok ? 'grn' : 'yel'}`} data-testid="s2e3-machine">
        <Icon name={ok ? 'shield' : 'eye'} />
        {machine}
      </span>
      {human && (
        <span className="chip" data-testid="s2e3-human">
          <Icon name="spark" />
          {human}
        </span>
      )}
      {note && <span className="s2e3-sub muted">{note}</span>}
    </div>
  );
}

/** Kết quả khi bốn bước xong: hình, lời giải nháp, nhãn hai trục, đối chiếu đáp số (US-S2E3-03-SC1, 04). */
export function ResultPanel({
  result,
  busy,
  onUseMachine,
  onSetStated,
}: {
  result: ResultView;
  busy: boolean;
  onUseMachine: () => void;
  onSetStated: (v: string) => void;
}) {
  const solid = result.sceneKind === 'space' && result.scene ? fromScene(result.scene as Scene) : null;
  return (
    <section className="card s2e3-result" data-testid="s2e3-result">
      <header className="s2e3-row">
        <h2 className="s2e3-h">Bản nháp do AI soạn</h2>
        <span className="s2e3-grow" />
        <Labels machine={result.machineLabel} human={result.humanLabel} note={result.machineNote} />
      </header>
      <div className="s2e3-result-grid">
        <div className="s2e3-figure" data-testid="s2e3-figure">
          {result.sceneKind === 'plane' && result.scene ? (
            <Scene2DView scene={result.scene as Scene2D} testId="s2e3-scene2d" />
          ) : solid ? (
            <Figure3DViewer solid={solid} lock="open" readOnly className="s2e3-viewer" testId="s2e3-viewer" />
          ) : (
            <p className="s2e3-sub muted">Chưa có hình</p>
          )}
        </div>
        <ol className="s2e3-steps" data-testid="s2e3-steps">
          {result.steps.map((s, i) => (
            <li key={`${i}-${s.length}`} data-math data-testid={`s2e3-step-${i + 1}`}>
              <MathText text={s} />
            </li>
          ))}
        </ol>
      </div>
      {result.answers && (result.answers.stated || result.answers.state === 'mismatch') && (
        <AnswerTable answers={result.answers} busy={busy} onUseMachine={onUseMachine} onSetStated={onSetStated} testPrefix="s2e3" />
      )}
      {result.approveBlocked && result.blockedReason && (
        <p className="s2e3-ban red" data-testid="s2e3-blocked">
          <Icon name="lock" inline />
          {result.blockedReason}
        </p>
      )}
      {result.docId && (
        <Link className="btn ghost" to={`/gv/soat-loi-giai/${result.docId}`} data-testid="s2e3-open-review">
          <Icon name="eye" />
          Mở soát lời giải
        </Link>
      )}
    </section>
  );
}
