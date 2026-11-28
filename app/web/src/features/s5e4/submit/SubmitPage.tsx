import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router';
import { ApiError } from '../../../shared/api/errors.ts';
import { ErrorNotice } from '../../../shared/ui/ErrorNotice.tsx';
import { formatVnDateTime } from '../../../shared/time/vnTime.ts';
import { ackPrivacy, mine, myGraded, privacy, submit, uploadPage, type Mine, type Privacy, type StudentGraded, type Submission } from './api.ts';
import { clearDraft, loadDraft, saveDraft, type DraftPage } from './draft.ts';
import { canSubmit, clarityScore, isClear, isQuotaError, MAX_EDGE, MAX_PAGE_BYTES, MAX_PAGES, MSG, move, newKey, stripJpeg } from './logic.ts';

/**
 * C5-03 "Nộp bài" của học sinh (US-S5E4-03, 09): thông báo riêng tư S5 trước lần nộp đầu; chụp từng trang (camera
 * iPhone qua input capture), máy nén ≤ 8 MB, chấm độ rõ (< 0,5 phải chụp lại), xếp trang lên/xuống; mỗi trang tải ngay
 * theo đoạn; nháp giữ trên máy (IndexedDB) và tự gửi lại cùng khóa lũy đẳng khi có mạng.
 */

interface Candidate extends DraftPage {
  url: string;
}

const toDraft = (p: Candidate): DraftPage => ({
  blob: p.blob,
  contentType: p.contentType,
  derived: p.derived,
  clarity: p.clarity,
  sha: p.sha,
  uploadId: p.uploadId,
  derivedUploadId: p.derivedUploadId,
});

const isHeic = (f: File) => /heic|heif/i.test(f.type) || /\.(heic|heif)$/i.test(f.name);

async function sha256(b: Blob): Promise<string> {
  const d = await crypto.subtle.digest('SHA-256', await b.arrayBuffer());
  return [...new Uint8Array(d)].map((x) => x.toString(16).padStart(2, '0')).join('');
}

function toBlob(c: HTMLCanvasElement, q: number): Promise<Blob> {
  return new Promise((resolve, reject) => c.toBlob((b) => (b ? resolve(b) : reject(new Error('jpeg'))), 'image/jpeg', q));
}

/** Nén ảnh về JPEG (cạnh dài ≤ MAX_EDGE) và chấm độ rõ trên bản thu nhỏ 400px. */
async function prepare(file: File): Promise<Candidate> {
  const bmp = await createImageBitmap(file);
  const scale = Math.min(1, MAX_EDGE / Math.max(bmp.width, bmp.height));
  const c = document.createElement('canvas');
  c.width = Math.round(bmp.width * scale);
  c.height = Math.round(bmp.height * scale);
  c.getContext('2d')!.drawImage(bmp, 0, 0, c.width, c.height);
  let jpeg = await toBlob(c, 0.85);
  if (jpeg.size > MAX_PAGE_BYTES) jpeg = await toBlob(c, 0.6);
  // gỡ metadata ngay trên máy: tệp tải lên = "ảnh gốc của hệ thống", băm máy chủ trùng băm trên máy
  jpeg = new Blob([stripJpeg(new Uint8Array(await jpeg.arrayBuffer())) as Uint8Array<ArrayBuffer>], { type: 'image/jpeg' });
  const s = document.createElement('canvas');
  const k = Math.min(1, 400 / Math.max(bmp.width, bmp.height));
  s.width = Math.max(3, Math.round(bmp.width * k));
  s.height = Math.max(3, Math.round(bmp.height * k));
  const ctx = s.getContext('2d', { willReadFrequently: true })!;
  ctx.drawImage(bmp, 0, 0, s.width, s.height);
  const px = ctx.getImageData(0, 0, s.width, s.height).data;
  const gray = new Float32Array(s.width * s.height);
  for (let i = 0; i < gray.length; i++) gray[i] = 0.299 * px[i * 4]! + 0.587 * px[i * 4 + 1]! + 0.114 * px[i * 4 + 2]!;
  const clarity = clarityScore(gray, s.width, s.height);
  bmp.close();
  if (isHeic(file)) {
    // HEIC: ảnh gốc giữ đúng tệp; bản JPEG dựng trên máy là bản phái sinh cho đọc chữ và hiển thị (BR-S5E4-08)
    return { blob: file, contentType: 'image/heic', derived: jpeg, clarity, sha: await sha256(file), url: URL.createObjectURL(jpeg) };
  }
  return { blob: jpeg, contentType: 'image/jpeg', clarity, sha: await sha256(jpeg), url: URL.createObjectURL(jpeg) };
}

export function SubmitPage() {
  const [params, setParams] = useSearchParams();
  const bc = params.get('bc') ?? '';
  const [list, setList] = useState<StudentGraded[]>([]);
  const [info, setInfo] = useState<Mine | null>(null);
  const [notice, setNotice] = useState<Privacy | null>(null);
  const [started, setStarted] = useState(false);
  const [pages, setPages] = useState<Candidate[]>([]);
  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [retake, setRetake] = useState<number | null>(null);
  const [key, setKey] = useState('');
  const [offline, setOffline] = useState(false);
  const [saved, setSaved] = useState(false);
  const [full, setFull] = useState(false);
  const [done, setDone] = useState<Submission | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const fileRef = useRef<HTMLInputElement>(null);
  const sending = useRef(false);

  useEffect(() => {
    myGraded().then((r) => setList(r.items)).catch(setError);
    privacy().then(setNotice).catch(setError);
  }, []);

  useEffect(() => {
    if (!bc) return;
    mine(bc)
      .then((r) => {
        setInfo(r);
        setDone(null);
      })
      .catch(setError);
    loadDraft(bc)
      .then((d) => {
        if (!d) {
          setKey(newKey());
          setPages([]);
          return;
        }
        setKey(d.key);
        setPages(d.pages.map((p) => ({ ...p, url: URL.createObjectURL(p.derived ?? p.blob) })));
        setStarted(true);
        if (d.pending) {
          setOffline(true);
          setSaved(true);
        }
      })
      .catch(() => setKey(newKey()));
  }, [bc]);

  const persist = useCallback(
    async (next: Candidate[], pending: boolean) => {
      try {
        await saveDraft({ assignmentId: bc, key, pending, pages: next.map(toDraft) });
        setSaved(true);
        setFull(false);
        return true;
      } catch (e) {
        if (isQuotaError(e)) setFull(true);
        return false;
      }
    },
    [bc, key],
  );

  /** Tải các trang chưa có trên máy chủ (ảnh gốc + bản phái sinh HEIC). */
  async function ensureUploaded(list0: Candidate[]): Promise<Candidate[]> {
    const out: Candidate[] = [];
    for (const p of list0) {
      let q = p;
      if (!q.uploadId) q = { ...q, uploadId: await uploadPage(q.blob, q.contentType) };
      if (q.derived && !q.derivedUploadId) q = { ...q, derivedUploadId: await uploadPage(q.derived, 'image/jpeg') };
      out.push(q);
    }
    return out;
  }

  const send = useCallback(
    async (list0: Candidate[]) => {
      if (sending.current) return;
      sending.current = true;
      setBusy(true);
      setError(null);
      try {
        const up = await ensureUploaded(list0);
        setPages(up);
        const r = await submit(
          bc,
          key,
          up.map((p) => ({ uploadId: p.uploadId!, derivedUploadId: p.derivedUploadId, clarity: p.clarity })),
        );
        setDone(r);
        setOffline(false);
        setPages([]);
        setStarted(false);
        await clearDraft(bc).catch(() => undefined);
        setKey(newKey());
      } catch (e) {
        if (e instanceof ApiError && e.code === 'NETWORK') {
          setOffline(true);
          await persist(list0, true);
        } else {
          setError(e);
        }
      } finally {
        sending.current = false;
        setBusy(false);
      }
    },
    [bc, key, persist],
  );

  // có mạng lại: tự gửi lại cùng khóa lũy đẳng (US-S5E4-09-FC1)
  useEffect(() => {
    if (!offline) return;
    const again = () => void send(pages);
    window.addEventListener('online', again);
    if (navigator.onLine && !sending.current) {
      const t = window.setTimeout(again, 1500);
      return () => {
        window.clearTimeout(t);
        window.removeEventListener('online', again);
      };
    }
    return () => window.removeEventListener('online', again);
  }, [offline, pages, send]);

  async function onFile(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0];
    e.target.value = '';
    if (!f) return;
    setError(null);
    try {
      setCandidate(await prepare(f));
    } catch {
      setError(new Error('Máy không đọc được ảnh này. Em chụp lại nhé.'));
    }
  }

  async function useCandidate() {
    if (!candidate) return;
    let c = candidate;
    setCandidate(null);
    setBusy(true);
    if (isClear(c.clarity) && c.blob.size <= MAX_PAGE_BYTES) {
      try {
        c = (await ensureUploaded([c]))[0]!;
      } catch (e) {
        if (!(e instanceof ApiError && e.code === 'NETWORK')) setError(e);
      }
    }
    const next = retake !== null ? pages.map((p, i) => (i === retake ? c : p)) : [...pages, c];
    setRetake(null);
    setPages(next);
    await persist(next, false);
    setBusy(false);
  }

  function capture(index: number | null) {
    setRetake(index);
    fileRef.current?.click();
  }

  async function reorder(from: number, to: number) {
    const next = move(pages, from, to);
    setPages(next);
    await persist(next, false);
  }

  async function remove(i: number) {
    const next = pages.filter((_, j) => j !== i);
    setPages(next);
    await persist(next, false);
  }

  async function ack() {
    if (!notice) return;
    try {
      setNotice(await ackPrivacy(notice.version));
    } catch (e) {
      setError(e);
    }
  }

  const acked = notice?.acked ?? false;
  const ready = canSubmit(acked, pages.map((p) => ({ clarity: p.clarity, bytes: p.blob.size })));

  return (
    <section className="s5e4-page" data-testid="s5e4-submit-page">
      <h1>Nộp bài viết tay</h1>
      {error !== null && <ErrorNotice error={error} testId="s5e4-submit-error" />}
      {!bc && (
        <ul className="s5e4-list" data-testid="s5e4-hs-list">
          {list.map((g) => (
            <li key={g.id} data-testid={`s5e4-hs-bc-${g.code}`}>
              <strong>{g.title}</strong> · hạn {formatVnDateTime(g.dueAt)}
              <button type="button" onClick={() => setParams({ bc: g.id })} data-testid={`s5e4-open-${g.code}`}>
                Mở
              </button>
            </li>
          ))}
          {list.length === 0 && <li>Chưa có bài cần nộp.</li>}
        </ul>
      )}
      {bc && info && (
        <div className="s5e4-card" data-testid="s5e4-hs-assignment">
          <h2>{info.title}</h2>
          <p>
            Hạn nộp {formatVnDateTime(info.dueAt)}
            {info.allowLate ? ' · nhận nộp muộn (có nhãn)' : ''}
          </p>
          {info.submission && !done && (
            <p data-testid="s5e4-previous">
              {info.submission.message} (lần {info.submission.version}){info.submission.lateLabel ? ` · ${info.submission.lateLabel}` : ''}
            </p>
          )}
          {done && (
            <div role="status" className="s5e4-ok" data-testid="s5e4-submitted">
              <span data-testid="s5e4-submitted-msg">{done.message}</span>
              {done.lateLabel && <span data-testid="s5e4-late"> · {done.lateLabel}</span>}
            </div>
          )}
          {!started && (
            <button type="button" className="s5e4-primary" onClick={() => setStarted(true)} data-testid="s5e4-start">
              {info.submission || done ? 'Nộp lại' : 'Nộp bài'}
            </button>
          )}
        </div>
      )}
      {bc && started && (
        <div className="s5e4-card" data-testid="s5e4-capture-area" data-busy={busy ? 'true' : 'false'}>
          {!acked && notice && (
            <div className="s5e4-privacy" role="note" data-testid="s5e4-privacy">
              <p data-testid="s5e4-privacy-text">{notice.text}</p>
              <button type="button" onClick={ack} data-testid="s5e4-ack">
                Em đã hiểu
              </button>
            </div>
          )}
          <input
            ref={fileRef}
            type="file"
            accept="image/*,.heic,.heif"
            capture="environment"
            hidden
            onChange={onFile}
            data-testid="s5e4-file"
          />
          <button
            type="button"
            disabled={!acked || pages.length >= MAX_PAGES || busy}
            onClick={() => capture(null)}
            data-testid="s5e4-capture"
          >
            Chụp trang {pages.length + 1}
          </button>
          {candidate && (
            <div className="s5e4-candidate" data-testid="s5e4-candidate" data-clarity={candidate.clarity}>
              <img src={candidate.url} alt="Ảnh vừa chụp" width={160} />
              <button type="button" onClick={useCandidate} data-testid="s5e4-use">
                Dùng ảnh này
              </button>
              <button type="button" onClick={() => setCandidate(null)} data-testid="s5e4-discard">
                Bỏ
              </button>
            </div>
          )}
          <ol className="s5e4-pages">
            {pages.map((p, i) => (
              <li
                key={p.sha + i}
                data-testid={`s5e4-page-${i + 1}`}
                data-sha={p.sha}
                data-clarity={p.clarity}
                data-uploaded={p.uploadId ? 'true' : 'false'}
              >
                <img src={p.url} alt={`Trang ${i + 1}`} width={96} />
                <span>Trang {i + 1}</span>
                {!isClear(p.clarity) && (
                  <span className="s5e4-warn" role="alert" data-testid={`s5e4-msg32-${i + 1}`}>
                    {MSG.s32(i + 1)}
                  </span>
                )}
                {p.blob.size > MAX_PAGE_BYTES && <span className="s5e4-warn">{MSG.tooBig}</span>}
                <button type="button" disabled={i === 0} onClick={() => reorder(i, i - 1)} data-testid={`s5e4-up-${i + 1}`} aria-label={`Đưa trang ${i + 1} lên`}>
                  ↑
                </button>
                <button
                  type="button"
                  disabled={i === pages.length - 1}
                  onClick={() => reorder(i, i + 1)}
                  data-testid={`s5e4-down-${i + 1}`}
                  aria-label={`Đưa trang ${i + 1} xuống`}
                >
                  ↓
                </button>
                <button type="button" onClick={() => capture(i)} data-testid={`s5e4-retake-${i + 1}`}>
                  Chụp lại
                </button>
                <button type="button" onClick={() => remove(i)} data-testid={`s5e4-remove-${i + 1}`}>
                  Xoá
                </button>
              </li>
            ))}
          </ol>
          {full && (
            <p className="s5e4-warn" role="alert" data-testid="s5e4-full">
              {MSG.full}
            </p>
          )}
          {offline && (
            <p className="s5e4-warn" role="status" data-testid="s5e4-offline">
              {MSG.s35}
            </p>
          )}
          {offline && saved && (
            <p data-testid="s5e4-draft-saved">Đã lưu trên máy.</p>
          )}
          <button type="button" className="s5e4-primary" disabled={!ready || busy} onClick={() => send(pages)} data-testid="s5e4-submit">
            Nộp
          </button>
        </div>
      )}
    </section>
  );
}
