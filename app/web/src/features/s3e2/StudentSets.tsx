import { useState } from 'react';
import { Link, useParams } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { dueLabel, errorText, ITEM_LABEL, MSG, RELEASE_LABEL, setsApi, useLoad, type SolutionView, type StudentHome, type StudentSet, type StudentSetDetail } from './api.ts';
import { MathText } from '../../shared/math/MathText.tsx';

const vnDate = (iso: string) => dueLabel(iso).slice(3);

function SetChips({ s }: { s: StudentSet }) {
  return (
    <div className="s3e2-chips">
      <span className="chip" data-testid={`s3e2-due-${s.id}`}>
        <Icon name="clock" />
        {s.dueAt ? `Hạn ${dueLabel(s.dueAt)}` : 'Chưa đặt hạn'}
      </span>
      {s.overdue ? (
        <span className="chip red" data-testid={`s3e2-overdue-${s.id}`}>
          {MSG.s28}
        </span>
      ) : null}
      <span className="chip" data-testid={`s3e2-progress-${s.id}`}>
        {s.done} / {s.total}
      </span>
      <span className={`chip ${s.releaseState === 'open' ? 'grn' : 'yel'}`} data-testid={`s3e2-release-${s.id}`}>
        <Icon name={s.releaseState === 'open' ? 'unlock' : 'lock'} />
        {s.releaseAt ? `Lời giải ${vnDate(s.releaseAt)} · ` : ''}
        {RELEASE_LABEL[s.releaseState]}
      </span>
    </div>
  );
}

/** C3-08 · Bài của em theo lớp rồi theo bộ (BR-S3E2-20, 21): hạn, tiến độ của chính em, giờ mở lời giải. */
export function StudentHomePage() {
  const [load, retry] = useLoad<StudentHome>('s3e2-home', (s) => setsApi.home(s));
  if (load.state === 'loading') return <p className="muted">Đang tải bài của em…</p>;
  if (load.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được bài của em.{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const { groups } = load.data;
  return (
    <div className="s3e2-page" data-testid="s3e2-home">
      <h1 className="s3e2-title">Bài của em</h1>
      {groups.length === 0 ? (
        <p className="empty" data-testid="s3e2-empty">
          {MSG.s27}
        </p>
      ) : null}
      {groups.map((g) => (
        <section key={g.classId} className="s3e2-group" aria-label={`Lớp ${g.className}`} data-testid={`s3e2-group-${g.className}`}>
          <h2 className="s3e2-h2">{g.className}</h2>
          <ul className="s3e2-items">
            {g.sets.map((s) => (
              <li key={s.id} className="card s3e2-item s3e2-col" data-testid={`s3e2-hs-set-${s.id}`}>
                <div className="s3e2-row">
                  <b className="s3e2-grow">{s.title}</b>
                  {s.status === 'closed' ? <span className="chip">Đã đóng</span> : null}
                  <Link className="btn ghost sm" to={s.id} data-testid={`s3e2-hs-open-${s.id}`}>
                    Mở bộ
                  </Link>
                </div>
                <SetChips s={s} />
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}

/** Một bộ của em: các đề theo thứ tự của giáo viên, trạng thái của em, giờ mở lời giải từng đề. */
export function StudentSetPage() {
  const { id = '' } = useParams();
  const [load, retry] = useLoad<StudentSetDetail>(`s3e2-mine-${id}`, (s) => setsApi.mine(id, s));
  const [sol, setSol] = useState<Record<number, SolutionView | string>>({});
  if (load.state === 'loading') return <p className="muted">Đang tải bộ bài…</p>;
  if (load.state === 'error')
    return (
      <div className="alert" role="alert" data-testid="s3e2-hs-error">
        {load.notFound ? 'Không tìm thấy bộ bài này.' : 'Chưa tải được bộ bài.'}{' '}
        <button className="btn ghost sm" onClick={retry}>
          Thử lại
        </button>
      </div>
    );
  const s = load.data;
  const open = async (position: number) => {
    try {
      setSol({ ...sol, [position]: await setsApi.solution(s.id, position) });
    } catch (e) {
      setSol({ ...sol, [position]: errorText(e, 'Chưa tải được lời giải.') });
    }
  };
  return (
    <div className="s3e2-page" data-testid="s3e2-hs-set">
      <div className="s3e2-head">
        <h1 className="s3e2-title">{s.title}</h1>
        <span className="s3e2-grow" />
        <Link className="btn ghost" to=".." relative="path">
          <Icon name="back" />
          Bài của em
        </Link>
      </div>
      <p className="s3e2-help">Lớp {s.className}</p>
      <SetChips s={s} />
      <div className="s3e2-actions">
        {s.status === 'assigned' ? (
          <Link className="btn" to={`/hs/bai-luyen/${s.id}`} data-testid="s3e2-hs-play">
            <Icon name="pencil" />
            Làm bài
          </Link>
        ) : (
          <span className="s3e2-help" data-testid="s3e2-hs-closed">
            Bộ đã đóng: em xem được kết quả, không gửi thêm đáp số.
          </span>
        )}
      </div>
      <ol className="s3e2-items" data-testid="s3e2-hs-items">
        {s.items.map((i) => {
          const v = sol[i.position];
          return (
            <li key={i.position} className="card s3e2-item s3e2-col" data-testid={`s3e2-hs-item-${i.position}`}>
              <div className="s3e2-row">
                <span className="s3e2-num">{i.position}</span>
                <span className="s3e2-grow" data-math>
                  <MathText text={i.statement} />
                </span>
                <span className={`chip ${i.state === 'correct' ? 'grn' : i.state === 'review' ? 'yel' : ''}`} data-testid={`s3e2-hs-state-${i.position}`}>
                  {ITEM_LABEL[i.state]}
                </span>
              </div>
              <div className="s3e2-row">
                <span className="s3e2-help s3e2-grow" data-testid={`s3e2-hs-release-${i.position}`}>
                  Lời giải: {RELEASE_LABEL[i.releaseState]}
                  {i.releaseAt ? ` · mở lúc ${formatVnDateTime(i.releaseAt)}` : ' · Chưa có giờ mở lời giải'}
                </span>
                {i.releaseState === 'open' ? (
                  <button className="btn ghost sm" data-testid={`s3e2-hs-solution-${i.position}`} onClick={() => void open(i.position)}>
                    Xem lời giải
                  </button>
                ) : null}
              </div>
              {typeof v === 'string' ? <p className="alert">{v}</p> : null}
              {v && typeof v !== 'string' && v.finalAnswer ? (
                <div className="s3e2-solution" data-testid={`s3e2-hs-solution-body-${i.position}`}>
                  {(v.steps ?? []).map((st, k) => (
                    <p key={k} data-math>
                      <b>{st.title}</b> <MathText text={st.body} />
                    </p>
                  ))}
                  <p data-math>Đáp số: <MathText text={v.finalAnswer} /></p>
                </div>
              ) : null}
            </li>
          );
        })}
      </ol>
    </div>
  );
}
