import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate, useSearchParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { ResultsPage } from '../s3e3/ResultsPage.tsx';
import { resultsApi } from '../s3e3/api.ts';
import { MSG, exportCsv, s3e6Api } from './api.ts';

/**
 * S3-E6 trên màn Kết quả lớp (C3-04, màn gốc của S3-E3 giữ nguyên bên trong):
 * - "Xuất kết quả" của bộ đang chọn (US-S3E6-02, CSV do máy chủ dựng);
 * - nút "Chữa bài này" gắn vào mỗi thẻ nhận định đủ ngưỡng của "Cả lớp vướng ở đâu" (US-S3E6-01, BR-S3E6-01); thẻ dữ
 *   liệu thô không có nút. Lời giải còn khóa thì hỏi lại MSG-S3-91 trước khi chiếu.
 */
export function ResultsWithActions() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [classId, setClassId] = useState<string | null>(params.get('lop'));
  const [setId, setSetId] = useState<string | null>(params.get('bo'));
  const [steps, setSteps] = useState<Record<number, number>>({});
  const [cards, setCards] = useState<HTMLElement[]>([]);
  const [msg, setMsg] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [ask, setAsk] = useState<{ position: number } | null>(null);

  // lớp / bộ đang chọn: như màn gốc (tham số URL, mặc định phần tử đầu)
  useEffect(() => {
    let live = true;
    const ctl = new AbortController();
    (async () => {
      const lop = params.get('lop') ?? (await resultsApi.classes(ctl.signal)).items[0]?.id ?? null;
      if (!lop) return;
      const sets = await resultsApi.sets(lop, ctl.signal);
      const bo = sets.items.find((s) => s.id === params.get('bo'))?.id ?? sets.items[0]?.id ?? null;
      if (!live) return;
      setClassId(lop);
      setSetId(bo);
      if (bo) {
        const stuck = await resultsApi.stuck(lop, bo, ctl.signal);
        if (live) setSteps(Object.fromEntries(stuck.insights.map((i) => [i.position, i.step])));
      }
    })().catch(() => undefined);
    return () => {
      live = false;
      ctl.abort();
    };
  }, [params]);

  // thẻ nhận định do màn gốc vẽ: gắn nút vào từng thẻ khi chúng xuất hiện
  useEffect(() => {
    const scan = () => {
      const found = Array.from(document.querySelectorAll<HTMLElement>('[data-testid="s3e3-insight"]'));
      setCards((old) => (old.length === found.length && old.every((x, i) => x === found[i]) ? old : found));
    };
    scan();
    const obs = new MutationObserver(scan);
    obs.observe(document.body, { childList: true, subtree: true });
    return () => obs.disconnect();
  }, []);

  // về từ Trình chiếu (?the=): cuộn tới đúng thẻ
  const back = params.get('the');
  useEffect(() => {
    if (!back) return;
    const el = cards.find((c) => c.dataset.position === back);
    if (el) {
      el.scrollIntoView({ block: 'center' });
      el.setAttribute('data-focus', 'true');
    }
  }, [back, cards]);

  const open = (position: number) => {
    if (!classId || !setId) return;
    const step = steps[position];
    const q = new URLSearchParams({ lop: classId, bo: setId, de: String(position) });
    if (step) q.set('buoc', String(step));
    navigate(`/gv/chua-bai?${q.toString()}`);
  };

  const fix = async (position: number) => {
    if (!classId || !setId || busy) return;
    setBusy(true);
    setMsg(null);
    try {
      const p = await s3e6Api.preflight(classId, setId, position);
      if (p.locked) setAsk({ position });
      else open(position);
    } catch {
      setMsg(MSG.offline);
    } finally {
      setBusy(false);
    }
  };

  const doExport = async () => {
    if (!classId || !setId || busy) return;
    setBusy(true);
    setMsg(null);
    const r = await exportCsv(classId, setId);
    setBusy(false);
    if (!r.ok) {
      setMsg(r.message);
      return;
    }
    const url = URL.createObjectURL(r.blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = r.fileName;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  };

  return (
    <>
      <div className="s3e6-bar" data-testid={tid('s3e6', 'results-bar')}>
        <button type="button" className="btn" disabled={!setId || busy} onClick={() => void doExport()} data-testid={tid('s3e6', 'export')}>
          <Icon name="save" /> Xuất kết quả
        </button>
        {msg ? (
          <span className="s3e6-err" role="alert" data-testid={tid('s3e6', 'results', 'error')}>
            {msg}
          </span>
        ) : null}
      </div>
      <ResultsPage />
      {cards.map((el) => {
        const position = Number(el.dataset.position);
        return createPortal(
          <button
            type="button"
            className="btn sm s3e6-fix"
            disabled={busy}
            onClick={() => void fix(position)}
            data-testid={tid('s3e6', 'fix')}
            data-position={position}
          >
            <Icon name="next" /> Chữa bài này
          </button>,
          el,
          `fix-${position}`,
        );
      })}
      {ask ? (
        <div className="s3e6-modal" role="dialog" aria-modal="true" aria-labelledby="s3e6-ask-title" data-testid={tid('s3e6', 'ask')}>
          <div className="card s3e6-dialog">
            <h2 id="s3e6-ask-title" className="s3e6-dialog-title">
              <Icon name="warn" /> {MSG.s91}
            </h2>
            <div className="s3e6-row">
              <button type="button" className="btn" data-testid={tid('s3e6', 'ask', 'continue')} onClick={() => open(ask.position)}>
                Tiếp tục
              </button>
              <button type="button" className="btn ghost" data-testid={tid('s3e6', 'ask', 'cancel')} onClick={() => setAsk(null)}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
