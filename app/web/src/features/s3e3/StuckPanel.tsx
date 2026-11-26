import { useState } from 'react';
import { formatVnTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { resultsApi, useLoad, type Insight, type Stuck, type StuckItem } from './api.ts';
import { columnLabel, groupByStep, MSG, splitCards, stepLabel } from './format.ts';
import type { Picked } from './ResultsPage.tsx';

/**
 * Khung "Cả lớp vướng ở đâu" (US-S3E3-03): thẻ nhận định của các đề đủ ngưỡng bài làm (hai nhãn cố định "AI gộp",
 * "Cần thầy cô xác nhận"; "Xem n bài làm" mở đúng n bài làm căn cứ) và dữ liệu thô gom theo bước cho các đề chưa đủ
 * ngưỡng, lớp nhỏ hoặc khi bộ gộp lỗi / quá giờ (kèm "Thử lại"). Tải riêng: bảng lớp × bài vẫn dùng được trong lúc chờ.
 */
export function StuckPanel(p: { classId: string; setId: string; refresh: number; onPick: (x: Picked) => void }) {
  const [stuck, retry] = useLoad<Stuck>(`s3e3-stuck-${p.classId}-${p.setId}-${p.refresh}`, (s) =>
    resultsApi.stuck(p.classId, p.setId, s),
  );
  const [showRest, setShowRest] = useState(false);
  return (
    <section className="card s3e3-card s3e3-stuck" data-testid="s3e3-stuck">
      <div className="s3e3-head">
        <h2 className="s3e3-h2">Cả lớp vướng ở đâu</h2>
      </div>
      {stuck.state === 'loading' && (
        <p className="muted" data-testid="s3e3-stuck-loading" aria-busy="true">
          Đang gộp bài làm của lớp…
        </p>
      )}
      {stuck.state === 'error' && <Failed onRetry={retry} />}
      {stuck.state === 'ok' && <StuckBody s={stuck.data} showRest={showRest} onShowRest={() => setShowRest(true)} onRetry={retry} onPick={p.onPick} />}
    </section>
  );
}

function Failed({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="alert" role="alert" data-testid="s3e3-msg-failed">
      {MSG.failed}{' '}
      <button className="btn ghost sm" data-testid="s3e3-stuck-retry" onClick={onRetry}>
        Thử lại
      </button>
    </div>
  );
}

function StuckBody(p: { s: Stuck; showRest: boolean; onShowRest: () => void; onRetry: () => void; onPick: (x: Picked) => void }) {
  const { s } = p;
  const { shown, rest } = splitCards(s.insights);
  const others = s.items.filter((i) => i.mode !== 'insight');
  const nobody = s.items.every((i) => i.mode === 'none');
  return (
    <>
      {s.classTooSmall && !nobody && (
        <p className="s3e3-banner" data-testid="s3e3-msg-small">
          {MSG.small(s.minAggregate)}
        </p>
      )}
      {nobody && (
        <p className="empty" data-testid="s3e3-stuck-empty">
          Chưa có bài làm.
        </p>
      )}
      {shown.map((c, i) => (
        <Card key={`${c.position}-${c.step}-${i}`} c={c} updatedAt={s.updatedAt} onPick={p.onPick} />
      ))}
      {rest.length > 0 && !p.showRest && (
        <button className="btn ghost sm" data-testid="s3e3-insight-more" onClick={p.onShowRest}>
          Còn {rest.length} nhận định khác
        </button>
      )}
      {p.showRest && rest.map((c, i) => <Card key={`r${c.position}-${c.step}-${i}`} c={c} updatedAt={s.updatedAt} onPick={p.onPick} />)}
      {!nobody && others.map((it) => <RawBlock key={it.position} it={it} s={s} onRetry={p.onRetry} />)}
      <p className="muted s3e3-note" data-testid="s3e3-stuck-note">
        {MSG.note(s.minAggregate)}
      </p>
    </>
  );
}

function Card({ c, updatedAt, onPick }: { c: Insight; updatedAt: string; onPick: (x: Picked) => void }) {
  const [open, setOpen] = useState(false);
  return (
    <article className="s3e3-insight" data-testid="s3e3-insight" data-position={c.position}>
      <b data-testid="s3e3-insight-title">{columnLabel(c)}</b>
      <div className="s3e3-insight-text" data-testid="s3e3-insight-text">
        {c.text}
      </div>
      <div className="s3e3-head">
        {c.labels.map((l) => (
          <span key={l} className={`chip${l === 'AI gộp' ? ' s3e3-ai' : ' yel'}`} data-testid="s3e3-insight-label">
            {l === 'AI gộp' && <Icon name="spark" inline />}
            {l}
          </span>
        ))}
      </div>
      <div className="s3e3-head">
        <button className="btn ghost sm" data-testid="s3e3-insight-open" aria-expanded={open} onClick={() => setOpen((x) => !x)}>
          <Icon name="eye" />
          Xem {c.n} bài làm
        </button>
        <span className="s3e3-grow" />
        <span className="muted" data-testid="s3e3-insight-updated">
          Cập nhật lúc {formatVnTime(updatedAt)}
        </span>
      </div>
      {open && (
        <ul className="s3e3-people" data-testid="s3e3-insight-people">
          {c.evidence.map((e) => (
            <li key={e.userCode}>
              <button
                className="btn ghost sm s3e3-person"
                data-testid="s3e3-insight-person"
                onClick={() => onPick({ userCode: e.userCode, item: c.position })}
              >
                <b>{e.displayName}</b>
                <span className="mono muted">{e.userCode}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </article>
  );
}

/** Đề chưa gộp: banner lý do + gợi ý và câu hỏi thô gom theo bước; không nhận định, không nhãn gộp (BR-S3E3-34). */
function RawBlock({ it, s, onRetry }: { it: StuckItem; s: Stuck; onRetry: () => void }) {
  return (
    <article className="s3e3-raw" data-testid="s3e3-raw" data-position={it.position} data-mode={it.mode}>
      <b>{columnLabel(it)}</b>
      {it.mode === 'failed' && <Failed onRetry={onRetry} />}
      {it.mode !== 'failed' && !s.classTooSmall && (
        <p className="s3e3-banner" data-testid="s3e3-msg-below">
          {MSG.below(it.works, s.minAggregate)}
        </p>
      )}
      {groupByStep(it.raw).map((g) => (
        <div key={g.step} className="s3e3-step" data-testid="s3e3-raw-step" data-step={g.step}>
          <div className="s3e3-kicker">{stepLabel(g.step)}</div>
          {g.entries.map((r, i) => (
            <div className="s3e3-line" key={i} data-testid="s3e3-raw-entry" data-kind={r.kind}>
              <span className="mono">{formatVnTime(r.at)}</span> {r.kind === 'hint' ? 'Gợi ý' : 'Câu hỏi'}:{' '}
              <span className="s3e3-plain">{r.text}</span>
            </div>
          ))}
        </div>
      ))}
      {it.mode !== 'none' && it.raw.length === 0 && <p className="muted">Các em chưa xin gợi ý hay đặt câu hỏi ở đề này.</p>}
    </article>
  );
}
