import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { e4Api, M, type Keyword, type Lecture } from './api.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MathText } from '../../shared/math/MathText.tsx';
import { splitMath } from '../../shared/math/math.ts';

/**
 * Từ khóa bấm được và hộp bài giảng (US-GEO-411; BR-GEO-198, 199, 296). Cụm từ trùng một từ khóa của kho (dài trước,
 * không phân biệt hoa thường) được gạch chân chấm; bấm mở hộp bài giảng nằm ĐÈ lên trang (tab bên dưới giữ nguyên chỗ
 * đang đọc, điểm vẽ thêm và tiến độ không đổi, US-GEO-411-SC4).
 */
type Open = (req: { term?: string; slug?: string }) => void;
const LectureCtx = createContext<{ keywords: Keyword[]; open: Open } | null>(null);

let cache: Promise<Keyword[]> | null = null;
function loadKeywords(): Promise<Keyword[]> {
  cache ??= e4Api
    .keywords()
    .then((r) => r.items)
    .catch(() => {
      cache = null;
      return [];
    });
  return cache;
}

export function LectureProvider({ children }: { children: ReactNode }) {
  const [keywords, setKeywords] = useState<Keyword[]>([]);
  const [req, setReq] = useState<{ term?: string; slug?: string } | null>(null);
  useEffect(() => {
    let alive = true;
    void loadKeywords().then((k) => alive && setKeywords(k));
    return () => {
      alive = false;
    };
  }, []);
  const value = useMemo(() => ({ keywords, open: (r: { term?: string; slug?: string }) => setReq(r) }), [keywords]);
  return (
    <LectureCtx.Provider value={value}>
      {children}
      {req && <LectureModal req={req} onClose={() => setReq(null)} />}
    </LectureCtx.Provider>
  );
}

/** Chia văn bản thành đoạn thường và đoạn trùng từ khóa (dài trước). */
type KwPart = { text: string; kw?: Keyword };
export function splitKeywords(text: string, keywords: Keyword[]): KwPart[] {
  if (!keywords.length || !text) return [{ text }];
  const sorted = [...keywords].sort((a, b) => b.term.length - a.term.length);
  const esc = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const re = new RegExp(`(${sorted.map((k) => esc(k.term)).join('|')})`, 'giu');
  const out: Array<{ text: string; kw?: Keyword }> = [];
  let last = 0;
  for (const m of text.matchAll(re)) {
    const i = m.index ?? 0;
    if (i > last) out.push({ text: text.slice(last, i) });
    const kw = sorted.find((k) => k.term.toLowerCase() === m[0].toLowerCase());
    out.push({ text: m[0], kw });
    last = i + m[0].length;
  }
  if (last < text.length) out.push({ text: text.slice(last) });
  return out;
}

/** Văn bản có từ khóa gạch chân chấm, bấm được. */
export function KwText({ text, testId }: { text: string; testId?: string }) {
  const ctx = useContext(LectureCtx);
  // T-261128-74: tách toán trước, chỉ gạch chân từ khóa trong đoạn chữ (không cắt ngang biểu thức $...$).
  const parts = useMemo(
    () =>
      splitMath(text).flatMap((s): KwPart[] =>
        s.kind === 'math' ? [{ text: s.display ? `$$${s.value}$$` : `$${s.value}$` }] : splitKeywords(s.value, ctx?.keywords ?? []),
      ),
    [text, ctx?.keywords],
  );
  return (
    <span data-testid={testId} data-math>
      {parts.map((p, i) =>
        p.kw && ctx ? (
          <button
            key={i}
            type="button"
            className="e4-kw"
            data-testid={tid('e4', 'kw', p.kw.slug)}
            data-term={p.text}
            onClick={() => ctx.open({ term: p.text })}
          >
            <MathText text={p.text} />
          </button>
        ) : (
          <span key={i}><MathText text={p.text} /></span>
        ),
      )}
    </span>
  );
}

/** Mở bài giảng theo mã (bấm tên bài giảng ở dòng "Nguồn"). */
export function useOpenLecture(): Open {
  const ctx = useContext(LectureCtx);
  return ctx?.open ?? (() => undefined);
}

function LectureModal({ req, onClose }: { req: { term?: string; slug?: string }; onClose: () => void }) {
  const [lec, setLec] = useState<Lecture | null>(null);
  const [err, setErr] = useState<string | null>(null);
  useEffect(() => {
    let alive = true;
    const p = req.slug ? e4Api.lecture(req.slug) : e4Api.lectureByTerm(req.term ?? '');
    p.then((l) => alive && setLec(l)).catch(
      (e: unknown) => alive && setErr(e instanceof ApiError && e.code === 'NOT_FOUND' ? M['39'] : M['08']),
    );
    return () => {
      alive = false;
    };
  }, [req]);
  return (
    <div className="e4-modal-back" role="presentation" onClick={onClose}>
      <div
        className="card e4-modal"
        role="dialog"
        aria-modal="true"
        aria-label={lec?.title ?? 'Bài giảng'}
        data-testid={tid('e4', 'lecture')}
        onClick={(e) => e.stopPropagation()}
      >
        {err && (
          <p role="alert" data-testid={tid('e4', 'lecture', 'error')}>
            {err}
          </p>
        )}
        {!err && !lec && <p className="muted">Đang tải…</p>}
        {lec && (
          <>
            <h3 data-testid={tid('e4', 'lecture', 'title')}>{lec.title}</h3>
            {lec.aiLabel && (
              <span className="chip pur" data-testid={tid('e4', 'lecture', 'ai-label')}><Icon name="spark" />
                {lec.aiLabel}
              </span>
            )}
            <div className="e4-lecture-body" data-testid={tid('e4', 'lecture', 'body')}>
              {lec.body.split(/\n\n+/).map((p, i) => (
                <p key={i}>{p}</p>
              ))}
            </div>
            <p className="small" data-testid={tid('e4', 'lecture', 'source')}>
              Nguồn: {lec.sourceLabel}
              {lec.sourceRef ? <span className="muted"> ({lec.sourceRef})</span> : null}
            </p>
            {lec.reviewNote && (
              <p className="small muted" data-testid={tid('e4', 'lecture', 'review')}>
                <MathText text={lec.reviewNote} />
              </p>
            )}
          </>
        )}
        <button type="button" className="btn ghost" data-testid={tid('e4', 'lecture', 'close')} onClick={onClose}><Icon name="x" />
          Đóng
        </button>
      </div>
    </div>
  );
}
