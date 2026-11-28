import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { Icon } from '../../shared/ui/Icon.tsx';
import { resultsApi, useLoad, type Board, type CellStatus, type ClassOption, type Row, type Sets } from './api.ts';
import { EvidencePanel } from './EvidencePanel.tsx';
import { cellLabel, columnLabel, MSG, STATUS_ICON, STATUS_LABEL } from './format.ts';
import { StuckPanel } from './StuckPanel.tsx';

export interface Picked {
  userId: string;
  item: number;
}

/**
 * C3-04 Kết quả lớp (US-S3E3-01…03): bảng lớp × bài (ô là trạng thái có chữ + biểu tượng; không cột tổng, không xếp
 * hạng), khung bằng chứng của một ô và "Cả lớp vướng ở đâu". Lớp và bộ đang chọn nằm ở ?lop=&bo= để quay lại đúng chỗ.
 */
export function ResultsPage() {
  const [params, setParams] = useSearchParams();
  const [classes, retryClasses] = useLoad<{ items: ClassOption[] }>('s3e3-classes', (s) => resultsApi.classes(s));
  const classId = params.get('lop') ?? (classes.state === 'ok' ? (classes.data.items[0]?.id ?? null) : null);
  const [sets, retrySets] = useLoad<Sets>(classId ? `s3e3-sets-${classId}` : null, (s) => resultsApi.sets(classId ?? '', s));
  const wanted = params.get('bo');
  const setId =
    sets.state === 'ok' ? (sets.data.items.find((x) => x.id === wanted)?.id ?? sets.data.items[0]?.id ?? null) : null;
  const [refresh, setRefresh] = useState(0);
  const [picked, setPicked] = useState<Picked | null>(null);
  const select = (next: Record<string, string>) => {
    setPicked(null);
    setParams(next, { replace: true });
  };

  if (classes.state === 'loading') return <BoardSkeleton />;
  if (classes.state === 'error') return <LoadError onRetry={retryClasses} />;
  if (classes.data.items.length === 0)
    return (
      <div className="s3e3-page" data-testid="s3e3-page">
        <h1 className="s3e3-title">Kết quả lớp</h1>
        <p className="empty">Thầy cô chưa có lớp nào. Tạo lớp ở mục Lớp học để bắt đầu.</p>
      </div>
    );
  return (
    <div className="s3e3-page" data-testid="s3e3-page">
      <div className="s3e3-head">
        <h1 className="s3e3-title">Kết quả lớp</h1>
        <label className="s3e3-pick">
          <span className="muted">Lớp</span>
          <select data-testid="s3e3-class-select" value={classId ?? ''} onChange={(e) => select({ lop: e.target.value })}>
            {classes.data.items.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        {sets.state === 'ok' && sets.data.items.length > 0 && classId && (
          <label className="s3e3-pick">
            <span className="muted">Bộ bài</span>
            <select data-testid="s3e3-set-select" value={setId ?? ''} onChange={(e) => select({ lop: classId, bo: e.target.value })}>
              {sets.data.items.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.title}
                </option>
              ))}
            </select>
          </label>
        )}
        <span className="s3e3-grow" />
        {setId && (
          <button className="btn ghost sm" data-testid="s3e3-refresh" onClick={() => setRefresh((n) => n + 1)}>
            <Icon name="redo" />
            Làm mới
          </button>
        )}
      </div>
      {sets.state === 'loading' && <BoardSkeleton bare />}
      {sets.state === 'error' && <LoadError onRetry={retrySets} bare />}
      {sets.state === 'ok' && classId && !sets.data.hasActiveStudents && (
        <p className="empty" data-testid="s3e3-msg-noactive">
          {MSG.noActive}{' '}
          <Link to={`/gv/lop-hoc/${classId}`} data-testid="s3e3-to-pending">
            Mở danh sách Chờ duyệt
          </Link>
        </p>
      )}
      {sets.state === 'ok' && sets.data.hasActiveStudents && !setId && (
        <p className="empty" data-testid="s3e3-msg-nosets">
          Lớp chưa có bộ bài Đã giao.
        </p>
      )}
      {sets.state === 'ok' && sets.data.hasActiveStudents && classId && setId && (
        <div className="s3e3-layout">
          <BoardCard key={`${classId}|${setId}`} classId={classId} setId={setId} refresh={refresh} picked={picked} onPick={setPicked} />
          <div className="s3e3-side">
            <StuckPanel key={`${classId}|${setId}`} classId={classId} setId={setId} refresh={refresh} onPick={setPicked} />
            {picked && (
              <EvidencePanel
                key={`${picked.userId}|${picked.item}`}
                classId={classId}
                setId={setId}
                picked={picked}
                refresh={refresh}
                onRefreshed={() => setRefresh((n) => n + 1)}
              />
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function BoardCard(p: { classId: string; setId: string; refresh: number; picked: Picked | null; onPick: (x: Picked) => void }) {
  const [board, retry] = useLoad<Board>(`s3e3-board-${p.classId}-${p.setId}-${p.refresh}`, (s) =>
    resultsApi.board(p.classId, p.setId, null, s),
  );
  const [extra, setMore] = useState<{ refresh: number; rows: Row[]; next: string | null } | null>(null);
  const more = extra && extra.refresh === p.refresh ? extra : null;
  if (board.state === 'loading') return <BoardSkeleton bare />;
  if (board.state === 'error') return <LoadError onRetry={retry} bare />;
  const b = board.data;
  const rows = [...b.rows, ...(more?.rows ?? [])];
  const next = more ? more.next : (b.nextCursor ?? null);
  const active = rows.filter((r) => !r.left).length;
  const untouched = rows.every((r) => r.cells.every((c) => c === 'todo'));
  const loadMore = async () => {
    if (!next) return;
    const page = await resultsApi.board(p.classId, p.setId, next);
    setMore({ refresh: p.refresh, rows: [...(more?.rows ?? []), ...page.rows], next: page.nextCursor ?? null });
  };
  return (
    <section className="card s3e3-card" data-testid="s3e3-board-card">
      <div className="s3e3-head">
        <h2 className="s3e3-h2">Bảng lớp × bài</h2>
        <span className="chip" data-testid="s3e3-active">
          <Icon name="users" inline />
          {next ? `hơn ${active}` : active} em Đang học
        </span>
        <span className="s3e3-grow" />
        <span className="muted s3e3-sub" data-testid="s3e3-sub">
          {MSG.subtitle}
        </span>
      </div>
      {untouched && (
        <p className="empty" data-testid="s3e3-msg-none">
          {MSG.none}
        </p>
      )}
      <div className="s3e3-scroll">
        <table className="s3e3-table" data-testid="s3e3-board">
          <thead>
            <tr>
              <th scope="col" className="s3e3-stick">
                Học sinh
              </th>
              {b.columns.map((c) => (
                <th scope="col" key={c.position} data-testid="s3e3-col">
                  {columnLabel(c)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((r, idx) => (
              <tr key={`${r.userId}|${idx}`} data-testid="s3e3-row" data-code={r.userCode} data-user={r.userId}>
                <th scope="row" className="s3e3-stick s3e3-name">
                  <b>{r.displayName}</b>
                  {r.left && (
                    <span className="chip" data-testid="s3e3-left-chip">
                      đã rời
                    </span>
                  )}
                  <span className="mono muted s3e3-code">{r.userCode}</span>
                </th>
                {r.cells.map((s: CellStatus, i) => {
                  const col = b.columns[i];
                  if (!col) return null;
                  const on = p.picked?.userId === r.userId && p.picked.item === col.position;
                  return (
                    <td key={col.position}>
                      <button
                        type="button"
                        className={`s3e3-cell ${s}${on ? ' on' : ''}`}
                        data-testid="s3e3-cell"
                        data-status={s}
                        data-item={col.position}
                        aria-label={cellLabel(r.displayName, col, s)}
                        aria-pressed={on}
                        onClick={() => p.onPick({ userId: r.userId, item: col.position })}
                      >
                        <Icon name={STATUS_ICON[s]} inline />
                        {STATUS_LABEL[s]}
                      </button>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {next && (
        <button className="btn ghost sm" data-testid="s3e3-more-rows" onClick={() => void loadMore()}>
          Xem thêm học sinh
        </button>
      )}
      <div className="s3e3-legend" data-testid="s3e3-legend">
        {(['correct', 'review', 'doing', 'todo'] as const).map((s) => (
          <span key={s} className={`s3e3-cell ${s} s3e3-key`}>
            <Icon name={STATUS_ICON[s]} inline />
            {STATUS_LABEL[s]}
          </span>
        ))}
        <span className="s3e3-grow" />
        <span className="muted">Bấm ô để xem bài làm làm bằng chứng</span>
      </div>
    </section>
  );
}

function BoardSkeleton({ bare }: { bare?: boolean }) {
  const table = (
    <div className="card s3e3-card s3e3-skeleton" data-testid="s3e3-skeleton" aria-busy="true" aria-label="Đang tải kết quả lớp">
      {Array.from({ length: 6 }, (_, r) => (
        <div className="s3e3-skel-row" key={r}>
          {Array.from({ length: 5 }, (_, c) => (
            <span className="s3e3-skel-cell" key={c} />
          ))}
        </div>
      ))}
    </div>
  );
  return bare ? table : <div className="s3e3-page">{table}</div>;
}

function LoadError({ onRetry, bare }: { onRetry: () => void; bare?: boolean }) {
  const box = (
    <div className="alert" role="alert" data-testid="s3e3-error">
      {MSG.loadFailed}{' '}
      <button className="btn ghost sm" data-testid="s3e3-retry" onClick={onRetry}>
        Thử lại
      </button>
    </div>
  );
  return bare ? box : <div className="s3e3-page">{box}</div>;
}
