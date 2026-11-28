import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router';
import { api } from '../../shared/api/client.ts';
import type { Scene } from '../../shared/scene/types.ts';
import { tid } from '../../shared/testid.ts';
import { fromScene } from '../../shared/viewer/adapter.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { WorkspacePanel, type PanelTab } from '../e3/WorkspacePanel.tsx';
import '../e3/e3.css';
import { M, e6Api, errText } from './api.ts';
import type { PreviewPage as PreviewData } from './api.ts';
import { LockCard } from './LockCard.tsx';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MathText } from '../../shared/math/MathText.tsx';

interface TeacherProblem {
  statement: string;
  scene: Scene | null;
  hints?: string[];
}

/**
 * "Xem như học sinh" (US-GEO-607): màn Làm bài như học sinh thấy (đề, hình, thanh công cụ, ba tab) với dải MSG-GEO-51 và
 * nút "Thoát xem trước". Khóa theo bài giao của lớp đang chọn, hoặc giờ đang chọn ở bước 3, hoặc Chưa đặt giờ (D-GEO-139,
 * 146). Không gọi đường lời giải nào, không tải bước giải hay đáp số (kể cả khi đã mở: tab chỉ hiện MSG-GEO-29), không ghi
 * sự kiện hay tiến độ: gợi ý lấy từ dữ liệu đề của giáo viên, công cụ đo không gửi sự kiện, hỏi đáp qua đường xem trước.
 */
export function PreviewPage() {
  const { versionId = '' } = useParams();
  const [q] = useSearchParams();
  const nav = useNavigate();
  const [data, setData] = useState<PreviewData | null>(null);
  const [prob, setProb] = useState<TeacherProblem | null>(null);
  const [receivedAt, setReceivedAt] = useState(0);
  const [error, setError] = useState<unknown>(null);
  const [tab, setTab] = useState<PanelTab>('hint');
  const [open, setOpen] = useState(false);
  const [shown, setShown] = useState(0);
  const [question, setQuestion] = useState('');
  const [turns, setTurns] = useState<Array<{ q: string; a: string }>>([]);
  const [asking, setAsking] = useState(false);
  const [askErr, setAskErr] = useState<string | null>(null);
  const classId = q.get('classId') ?? undefined;
  const releaseAt = q.get('releaseAt') ?? undefined;

  useEffect(() => {
    let alive = true;
    Promise.all([
      e6Api.preview(versionId, { classId, releaseAt }),
      api.get<TeacherProblem>(`/api/v1/teacher/problems/${encodeURIComponent(versionId)}`),
    ])
      .then(([d, p]) => {
        if (!alive) return;
        setData(d);
        setProb(p);
        setReceivedAt(Date.now());
      })
      .catch((e: unknown) => alive && setError(e));
    return () => {
      alive = false;
    };
  }, [versionId, classId, releaseAt]);

  if (error) return <div className="alert" role="alert" data-testid={tid('e6', 'preview', 'error')}>{errText(error)}</div>;
  if (!data || !prob) return <p className="muted">Đang tải…</p>;

  const hints = prob.hints ?? [];
  const ask = async () => {
    const text = question.trim();
    if (!text) return;
    setAsking(true);
    setAskErr(null);
    try {
      const r = await e6Api.previewAsk(versionId, text, shown > 0 ? hints[shown - 1] : undefined);
      setTurns((t) => [...t, { q: r.question, a: r.reply }]);
      setQuestion('');
    } catch {
      setAskErr(M['14']);
    } finally {
      setAsking(false);
    }
  };

  return (
    <div className="e3-workspace e6-preview" data-testid={tid('e6', 'preview')}>
      <div className="e6-banner row" data-testid={tid('e6', 'preview', 'banner')}>
        <span>{M['51']}</span>
        <button type="button" className="btn sm" data-testid={tid('e6', 'preview', 'exit')} onClick={() => void nav(`/gv/de/${versionId}`)}><Icon name="x" />
          Thoát xem trước
        </button>
      </div>
      <header className="e3-head">
        <h2>
          {data.problem.code} <span className="muted small">· {data.problem.title}</span>
        </h2>
        <p className="e3-statement" data-testid={tid('e6', 'preview', 'statement')}><MathText text={data.problem.statement} /></p>
      </header>
      <div className="e3-main">
        {prob.scene ? (
          <Figure3DViewer solid={fromScene(prob.scene)} lock={data.lock.state === 'open' ? 'open' : 'locked'} className="e3-viewer" />
        ) : null}
      </div>
      <WorkspacePanel
        assignmentId=""
        lock={data.lock.state === 'open' ? 'open' : 'locked'}
        tab={tab}
        onTab={(t) => {
          setTab(t);
          setOpen(true);
        }}
        open={open}
        onToggle={() => setOpen((o) => !o)}
        hints={{ shown: [] }}
        onHints={() => undefined}
        slots={{
          hint: (
            <div className="e3-hints">
              {hints.slice(0, shown).map((h, i) => (
                <div key={i} className="card e3-hint" data-testid={tid('e6', 'preview', 'hint', i + 1)}>
                  <strong>Gợi ý {i + 1}</strong>
                  <p>{h}</p>
                </div>
              ))}
              {shown < Math.min(2, hints.length) ? (
                <button type="button" className="btn" data-testid={tid('e6', 'preview', 'hint', 'next')} onClick={() => setShown((n) => n + 1)}>
                  Xem gợi ý {shown + 1}
                </button>
              ) : null}
            </div>
          ),
          solution: <LockCard state={data.lock.state} releaseAt={data.lock.releaseAt} serverNow={data.lock.serverNow} receivedAt={receivedAt} />,
          ask: (
            <div className="e6-ask" data-testid={tid('e6', 'preview', 'ask')}>
              {turns.map((t, i) => (
                <div key={i} className="card">
                  <p className="small muted">{t.q}</p>
                  <p data-testid={tid('e6', 'preview', 'answer', i + 1)}>{t.a}</p>
                </div>
              ))}
              {askErr ? <p className="alert" role="alert">{askErr}</p> : null}
              <div className="row">
                <input value={question} maxLength={1000} placeholder="Hỏi trợ lý về bài này" data-testid={tid('e6', 'preview', 'question')}
                  onChange={(e) => setQuestion(e.target.value)} />
                <button type="button" className="btn primary" disabled={asking} data-testid={tid('e6', 'preview', 'send')} onClick={() => void ask()}>
                  Gửi
                </button>
              </div>
            </div>
          ),
        }}
      />
    </div>
  );
}
