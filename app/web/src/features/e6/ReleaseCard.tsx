import { useCallback, useEffect, useState } from 'react';
import { tid } from '../../shared/testid.ts';
import { confirmOpen, e6Api, errText, msg46, msg47, vnDateTime, vnToIso } from './api.ts';
import type { ReleaseView } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/**
 * Thẻ vàng "Lời giải đang khóa" trên Theo dõi lớp (US-GEO-605; khung Design/mvp-03): bài Chưa đặt giờ hoặc Sắp mở có
 * hai nút "Mở ngay" (có hộp xác nhận) và "Đổi giờ" (hộp chọn ngày, giờ; không có lựa chọn bỏ giờ). Bài Đã mở không có
 * thẻ. Khóa lạc quan theo releaseRev đã thấy (hai cửa sổ đổi chồng nhau: MSG-GEO-49). Dùng được trên điện thoại.
 */
export function ReleaseCard({ assignmentId, onChanged }: { assignmentId: string; onChanged?: () => void }) {
  const [v, setV] = useState<ReleaseView | null>(null);
  const [dialog, setDialog] = useState<'none' | 'open' | 'change'>('none');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setV(await e6Api.releaseView(assignmentId));
    } catch (e) {
      setError(errText(e));
    }
  }, [assignmentId]);
  useEffect(() => {
    const t = window.setTimeout(() => void load(), 0);
    return () => window.clearTimeout(t);
  }, [load]);

  if (!v) return error ? <p className="alert" role="alert">{error}</p> : null;

  const act = async (body: { openNow?: boolean; releaseAt?: string | null }, ok: (at: string) => string) => {
    setBusy(true);
    setError(null);
    try {
      const r = await e6Api.setRelease(assignmentId, { ...body, expectedRev: v.releaseRev });
      setToast(ok(r.releaseAt));
      setDialog('none');
      setV(await e6Api.releaseView(assignmentId));
      onChanged?.(); // FIX4b M1: trang chứa thẻ (Theo dõi lớp) tải lại dữ liệu của mình
    } catch (e) {
      setError(errText(e));
    } finally {
      setBusy(false);
    }
  };
  const iso = vnToIso(date, time);

  return (
    <div data-testid={tid('e6', 'release-card-wrap')}>
      {toast ? (
        <p className="toast chip grn" role="status" data-testid={tid('e6', 'toast')}><Icon name="check" />
          {toast}
        </p>
      ) : null}
      {error && dialog === 'none' ? (
        <p className="alert" role="alert" data-testid={tid('e6', 'release', 'error')}>
          {error}
        </p>
      ) : null}
      {v.releaseState === 'open' ? (
        <p className="chip grn" data-testid={tid('e6', 'release', 'opened')}><Icon name="unlock" />
          Lời giải đã mở{v.releaseAt ? ` lúc ${vnDateTime(v.releaseAt)}` : ''}
        </p>
      ) : (
        <section className="card e6-yellow" data-testid={tid('e6', 'release-card')} data-state={v.releaseState}>
          <h3>Lời giải đang khóa</h3>
          <p className="small" data-testid={tid('e6', 'release-card', 'when')}>
            {v.releaseAt ? `Mở lúc ${vnDateTime(v.releaseAt)}` : 'Chưa đặt giờ mở'}
          </p>
          <div className="row">
            <button type="button" className="btn primary" data-testid={tid('e6', 'open-now')} onClick={() => setDialog('open')}><Icon name="unlock" />
              Mở ngay
            </button>
            <button type="button" className="btn ghost" data-testid={tid('e6', 'change-time')}
              onClick={() => {
                setError(null);
                setDate('');
                setTime('');
                setDialog('change');
              }}><Icon name="schedule" />
              Đổi giờ
            </button>
          </div>
        </section>
      )}
      {dialog === 'open' ? (
        <div className="e6-dialog" role="dialog" aria-modal="true" data-testid={tid('e6', 'confirm')}>
          <p data-testid={tid('e6', 'confirm', 'text')}>{confirmOpen(v.className)}</p>
          <div className="row">
            <button type="button" className="btn primary" disabled={busy} data-testid={tid('e6', 'confirm', 'ok')}
              onClick={() => void act({ openNow: true }, () => msg46(v.className))}>
              Mở ngay
            </button>
            <button type="button" className="btn ghost" data-testid={tid('e6', 'confirm', 'cancel')} onClick={() => setDialog('none')}>
              Hủy
            </button>
          </div>
        </div>
      ) : null}
      {dialog === 'change' ? (
        <div className="e6-dialog" role="dialog" aria-modal="true" data-testid={tid('e6', 'change')}>
          <h3>Đổi giờ mở lời giải</h3>
          <label>
            Ngày{' '}
            <input type="date" value={date} data-testid={tid('e6', 'change', 'date')} onChange={(e) => setDate(e.target.value)} />
          </label>
          <label>
            Giờ{' '}
            <input type="time" value={time} data-testid={tid('e6', 'change', 'clock')} onChange={(e) => setTime(e.target.value)} />
          </label>
          {error ? (
            <p className="alert" role="alert" data-testid={tid('e6', 'change', 'error')}>
              {error}
            </p>
          ) : null}
          <div className="row">
            <button type="button" className="btn primary" disabled={busy || !iso} data-testid={tid('e6', 'change', 'save')}
              onClick={() => iso && void act({ releaseAt: iso }, (at) => msg47(at))}>
              Lưu
            </button>
            <button type="button" className="btn ghost" data-testid={tid('e6', 'change', 'cancel')} onClick={() => setDialog('none')}>
              Hủy
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
