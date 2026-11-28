import { useState } from 'react';
import { Link } from 'react-router';
import { formatVnDateTime } from '../../shared/time/vnTime.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { errorText, MSG, newKey, setsApi, STATUS_LABEL, useLoad, type SetLine } from './api.ts';

type Tab = 'assigned' | 'draft' | 'closed' | 'archived';
const TABS: Array<[Tab, string]> = [
  ['assigned', 'Bài đang giao'],
  ['draft', 'Nháp'],
  ['closed', 'Đã đóng'],
  ['archived', 'Bộ đã lưu trữ'],
];

/** Danh sách bộ bài của giáo viên (C3-03): Bài đang giao, Nháp, Đã đóng, Bộ đã lưu trữ; đóng và lưu trữ (US-S3E2-04). */
export function TeacherSetsPage() {
  const [tab, setTab] = useState<Tab>('assigned');
  const [n, setN] = useState(0);
  const [active, retryA] = useLoad<{ items: SetLine[] }>(`s3e2-active-${n}`, (s) => setsApi.list('active', s));
  const [archived, retryB] = useLoad<{ items: SetLine[] }>(`s3e2-archived-${n}`, (s) => setsApi.list('archived', s));
  const [ask, setAsk] = useState<{ set: SetLine; action: 'close' | 'archive'; key: string } | null>(null);
  const [error, setError] = useState<string | null>(null);
  if (active.state === 'loading' || archived.state === 'loading') return <p className="muted">Đang tải bộ bài…</p>;
  if (active.state === 'error' || archived.state === 'error')
    return (
      <div className="alert" role="alert">
        Chưa tải được danh sách bộ bài.{' '}
        <button
          className="btn ghost sm"
          onClick={() => {
            retryA();
            retryB();
          }}
        >
          Thử lại
        </button>
      </div>
    );
  const all = [...active.data.items, ...archived.data.items];
  const shown = all.filter((s) => s.status === tab);
  const count = (t: Tab) => all.filter((s) => s.status === t).length;
  const run = async () => {
    if (!ask) return;
    setError(null);
    try {
      if (ask.action === 'close') await setsApi.close(ask.set.id, ask.key);
      else await setsApi.archive(ask.set.id, ask.key);
      setAsk(null);
      setN((x) => x + 1);
    } catch (e) {
      setAsk(null);
      setError(errorText(e, 'Chưa thực hiện được. Thầy cô thử lại nhé.'));
    }
  };
  return (
    <div className="s3e2-page" data-testid="s3e2-teacher-sets">
      <div className="s3e2-head">
        <h1 className="s3e2-title">Bộ bài</h1>
        <span className="s3e2-grow" />
        <Link className="btn" to="moi" data-testid="s3e2-new">
          <Icon name="plus" />
          Tạo bộ bài
        </Link>
      </div>
      <div className="s3e2-tabs" role="tablist">
        {TABS.map(([t, label]) => (
          <button key={t} role="tab" aria-selected={tab === t} className={`btn ghost sm${tab === t ? ' on' : ''}`} data-testid={`s3e2-tab-${t}`} onClick={() => setTab(t)}>
            {label} · {count(t)}
          </button>
        ))}
      </div>
      {error ? (
        <div className="alert" role="alert" data-testid="s3e2-error">
          {error}
        </div>
      ) : null}
      {shown.length === 0 ? <p className="empty">Chưa có bộ nào ở mục này.</p> : null}
      <ul className="s3e2-items" data-testid={`s3e2-list-${tab}`}>
        {shown.map((s) => (
          <li key={s.id} className="card s3e2-item" data-testid={`s3e2-set-${s.id}`}>
            <div className="s3e2-grow">
              <b>{s.title}</b>
              <div className="s3e2-help">
                {s.itemCount} đề{s.className ? ` · lớp ${s.className}` : ''}
                {s.dueAt ? ` · hạn ${formatVnDateTime(s.dueAt)}` : ''}
                {s.releaseAt ? ` · lời giải mở ${formatVnDateTime(s.releaseAt)}` : ''}
              </div>
            </div>
            {s.overdue && s.status === 'assigned' ? <span className="chip red">{MSG.s28}</span> : null}
            <span className={`chip ${s.status === 'assigned' ? 'grn' : s.status === 'draft' ? 'yel' : ''}`}>{STATUS_LABEL[s.status]}</span>
            <Link className="btn ghost sm" to={s.id} data-testid={`s3e2-open-${s.id}`}>
              {s.status === 'draft' ? 'Sửa' : 'Xem'}
            </Link>
            {s.status === 'assigned' ? (
              <button className="btn ghost sm" data-testid={`s3e2-close-${s.id}`} onClick={() => setAsk({ set: s, action: 'close', key: newKey() })}>
                <Icon name="lock" />
                Đóng bộ
              </button>
            ) : null}
            {s.status === 'closed' ? (
              <button className="btn ghost sm" data-testid={`s3e2-archive-${s.id}`} onClick={() => setAsk({ set: s, action: 'archive', key: newKey() })}>
                <Icon name="layers" />
                Lưu trữ bộ
              </button>
            ) : null}
          </li>
        ))}
      </ul>
      {tab === 'draft' ? <p className="s3e2-help">Bộ Nháp không đóng được. Bộ Đã giao mới đóng được.</p> : null}
      {tab === 'closed' ? <p className="s3e2-help">Bộ Đã giao chưa đóng thì chưa lưu trữ được.</p> : null}
      {ask ? (
        <div className="s3e2-banner yel" role="dialog" aria-label="Xác nhận" data-testid="s3e2-ask">
          {ask.action === 'close' ? (
            <>
              <b>Đóng {ask.set.title}? Đóng là một chiều, không mở lại được.</b>
              <p className="s3e2-help">
                Em không gửi thêm đáp số và không xin thêm gợi ý; em vẫn xem kết quả của mình; lịch mở lời giải vẫn chạy; bảng lớp
                vẫn đọc được số liệu.
              </p>
            </>
          ) : (
            <>
              <b>Lưu trữ {ask.set.title}?</b>
              <p className="s3e2-help">Bộ rời “Bài đang giao” và danh sách chính của học sinh, nằm ở mục “Bộ đã lưu trữ”. Bảng lớp vẫn đọc được số liệu.</p>
            </>
          )}
          <div className="s3e2-actions">
            <button className="btn ghost" onClick={() => setAsk(null)}>
              Để sau
            </button>
            <button className="btn" data-testid="s3e2-ask-ok" onClick={() => void run()}>
              {ask.action === 'close' ? 'Đóng bộ' : 'Lưu trữ bộ'}
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
