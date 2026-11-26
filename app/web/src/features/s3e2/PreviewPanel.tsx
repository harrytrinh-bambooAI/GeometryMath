import { useState } from 'react';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { errorText, MSG, RELEASE_LABEL, setsApi, useLoad, type Preview, type PreviewCheck } from './api.ts';

const OUTCOME: Record<PreviewCheck['outcome'], string> = { correct: 'Đúng', wrong: 'Chưa đúng', syntax: 'Chưa đọc được đáp số' };

/**
 * "Xem trước như học sinh" (BR-S3E2-05): dựng đúng nội dung màn Bài của em của bộ; chấm thử không ghi Attempt,
 * không ghi tiến độ, không tạo thông báo; lời giải vẫn khóa theo lịch (chỉ hiện trạng thái khóa).
 */
export function PreviewPanel({ setId }: { setId: string }) {
  const [load, retry] = useLoad<Preview>(`s3e2-preview-${setId}`, (s) => setsApi.preview(setId, s));
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [results, setResults] = useState<Record<number, string>>({});
  const [hints, setHints] = useState<Record<number, boolean>>({});
  if (load.state === 'loading') return <p className="muted">Đang dựng màn xem trước…</p>;
  if (load.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa dựng được màn xem trước.{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const pv = load.data;
  const check = async (position: number) => {
    try {
      const r = await setsApi.previewCheck(setId, position, answers[position] ?? '');
      setResults({ ...results, [position]: OUTCOME[r.outcome] });
    } catch (e) {
      setResults({ ...results, [position]: errorText(e, 'Chưa chấm thử được.') });
    }
  };
  return (
    <section className="card s3e2-card s3e2-preview" aria-label="Xem trước như học sinh" data-testid="s3e2-preview">
      <h2 className="s3e2-h2">Xem trước như học sinh · {pv.title}</h2>
      <p className="s3e2-help">
        {pv.dueAt ? `Hạn: ${formatVnDateTime(pv.dueAt)}` : 'Chưa đặt hạn'}
        {pv.overdue ? ` · ${MSG.s28}` : ''} · Không ghi tiến độ, không gửi thông báo.
      </p>
      <ol className="s3e2-items">
        {pv.items.map((i) => (
          <li key={i.position} className="s3e2-item s3e2-col" data-testid={`s3e2-pv-item-${i.position}`}>
            <div className="s3e2-row">
              <span className="s3e2-num">{i.position}</span>
              <span className="s3e2-grow" data-math>
                {i.statement}
              </span>
            </div>
            <div className="s3e2-row">
              <input
                aria-label={`Đáp số đề ${i.position}`}
                value={answers[i.position] ?? ''}
                data-testid={`s3e2-pv-answer-${i.position}`}
                onChange={(e) => setAnswers({ ...answers, [i.position]: e.target.value })}
              />
              <button className="btn ghost sm" data-testid={`s3e2-pv-check-${i.position}`} onClick={() => void check(i.position)}>
                Kiểm tra
              </button>
              <button className="btn ghost sm" data-testid={`s3e2-pv-hint-${i.position}`} onClick={() => setHints({ ...hints, [i.position]: true })}>
                <Icon name="hint" />
                Gợi ý
              </button>
              {results[i.position] ? <b data-testid={`s3e2-pv-result-${i.position}`}>{results[i.position]}</b> : null}
            </div>
            {hints[i.position] ? (
              <p className="s3e2-help" data-testid={`s3e2-pv-hint-text-${i.position}`}>
                {i.hint ?? 'Đề này chưa có gợi ý.'}
              </p>
            ) : null}
            <p className="s3e2-help" data-testid={`s3e2-pv-lock-${i.position}`}>
              <Icon name="lock" />
              Lời giải: {RELEASE_LABEL[i.releaseState]}
              {i.releaseAt ? ` · mở lúc ${formatVnDateTime(i.releaseAt)}` : ''}
            </p>
          </li>
        ))}
      </ol>
    </section>
  );
}
