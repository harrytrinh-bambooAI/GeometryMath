/**
 * T-261128-74 · Tách chuỗi có toán LaTeX thành đoạn chữ / đoạn toán (không phụ thuộc trình duyệt, chạy được bằng
 * node --test). Dùng chung cho MỌI màn qua <MathText> (shared/math/MathText.tsx).
 * - Dấu toán: `$...$`, `$$...$$` (khối), `\(...\)`, `\[...\]` (khối). `\$` là dấu đô la thường.
 * - Chuỗi KHÔNG có dấu toán nhưng là biểu thức trần (phương án "\dfrac{a^{3}}{3}", "\approx 50{,}77^\circ") thì cả
 *   chuỗi là toán (looksLikeBareLatex).
 * - Đoạn chữ còn sót lệnh LaTeX (dữ liệu viết thiếu dấu $) được đổi sang ký hiệu Unicode (latexToPlain), để màn hình
 *   không bao giờ hiện `\perp`, `\dfrac`... thô.
 */
export type MathSegment = { kind: 'text'; value: string } | { kind: 'math'; value: string; display: boolean };

const MARKER = /\\[a-zA-Z]+|\^|_\{|\{,\}/;

/** Chuỗi có dấu hiệu toán LaTeX (có dấu $ hoặc lệnh / mũ / chỉ số). */
export function hasMath(text: string): boolean {
  return text.includes('$') || text.includes('\\(') || text.includes('\\[') || MARKER.test(text);
}

/** Biểu thức trần: có dấu hiệu LaTeX và không có từ chữ thường dài ≥ 3 (sau khi bỏ lệnh) — tức không phải câu văn. */
export function looksLikeBareLatex(text: string): boolean {
  const t = text.trim();
  if (!t || t.length > 200 || !MARKER.test(t)) return false;
  const rest = t.replace(/\\[a-zA-Z]+/g, ' ');
  return !/\p{Ll}{3,}/u.test(rest);
}

export function splitMath(input: string | null | undefined): MathSegment[] {
  const text = input ?? '';
  if (!text) return [];
  const out: MathSegment[] = [];
  let buf = '';
  let i = 0;
  const pushText = () => {
    if (buf) out.push({ kind: 'text', value: buf });
    buf = '';
  };
  const close = (from: number, end: string): number => {
    let j = from;
    while (j < text.length) {
      if (text[j] === '\\' && end !== '\\)' && end !== '\\]' && text[j + 1] === '$') {
        j += 2;
        continue;
      }
      if (text.startsWith(end, j)) return j;
      j++;
    }
    return -1;
  };
  while (i < text.length) {
    const c = text[i];
    if (c === '\\' && text[i + 1] === '$') {
      buf += '$';
      i += 2;
      continue;
    }
    let open = '';
    let end = '';
    let display = false;
    if (text.startsWith('$$', i)) [open, end, display] = ['$$', '$$', true];
    else if (c === '$') [open, end] = ['$', '$'];
    else if (text.startsWith('\\(', i)) [open, end] = ['\\(', '\\)'];
    else if (text.startsWith('\\[', i)) [open, end, display] = ['\\[', '\\]', true];
    if (open) {
      const j = close(i + open.length, end);
      const body = j < 0 ? '' : text.slice(i + open.length, j);
      if (j >= 0 && body.trim()) {
        pushText();
        out.push({ kind: 'math', value: body.trim(), display });
        i = j + end.length;
        continue;
      }
    }
    buf += c;
    i++;
  }
  pushText();
  const only = out.length === 1 ? out[0] : undefined;
  if (only && only.kind === 'text' && looksLikeBareLatex(only.value)) {
    return [{ kind: 'math', value: only.value.trim(), display: false }];
  }
  return out;
}

const SYMBOLS: Record<string, string> = {
  perp: '⊥', parallel: '∥', approx: '≈', circ: '°', cdot: '·', times: '×', div: '÷', le: '≤', leq: '≤', ge: '≥',
  geq: '≥', ne: '≠', neq: '≠', pm: '±', infty: '∞', in: '∈', notin: '∉', subset: '⊂', subseteq: '⊆', supset: '⊃',
  cap: '∩', cup: '∪', angle: '∠', widehat: '', hat: '', triangle: '△', Delta: 'Δ', alpha: 'α', beta: 'β',
  gamma: 'γ', varphi: 'φ', phi: 'φ', pi: 'π', theta: 'θ', Rightarrow: '⇒', Leftrightarrow: '⇔', rightarrow: '→',
  to: '→', equiv: '≡', sim: '∼', cong: '≅', quad: ' ', qquad: '  ', left: '', right: '', displaystyle: '',
  vec: '', overrightarrow: '', overline: '', mathrm: '', text: '', textbf: '', mathbb: '', ldots: '…', dots: '…',
};

/** Đổi LaTeX sang chữ Unicode (dùng cho <option>, title, aria-label và đoạn chữ còn sót lệnh). */
export function latexToPlain(input: string | null | undefined): string {
  let s = input ?? '';
  if (!hasMath(s)) return s;
  s = s.replace(/\\\$/g, '\uE000').replace(/\$\$?|\\\(|\\\)|\\\[|\\\]/g, '').replace(/\uE000/g, '$');
  for (let k = 0; k < 4; k++) {
    s = s
      .replace(/\\[dt]?frac\s*\{([^{}]*)\}\s*\{([^{}]*)\}/g, (_m, a: string, b: string) => `${wrap(a)}/${wrap(b)}`)
      .replace(/\\sqrt\s*\[([^\]]*)\]\s*\{([^{}]*)\}/g, (_m, n: string, a: string) => `${n}√${wrap(a)}`)
      .replace(/\\sqrt\s*\{([^{}]*)\}/g, (_m, a: string) => `√${wrap(a)}`);
  }
  s = s
    .replace(/\{,\}/g, ',')
    .replace(/\^\s*\{?\\circ\}?/g, '°')
    .replace(/\^\s*\{([^{}]*)\}/g, (_m, a: string) => sup(a))
    .replace(/\^(\w)/g, (_m, a: string) => sup(a))
    .replace(/_\{([^{}]*)\}/g, '$1')
    .replace(/\\([a-zA-Z]+)\s?/g, (m, name: string) => SYMBOLS[name] ?? m.replace('\\', ''))
    .replace(/\\([,;:! ])/g, ' ')
    .replace(/[{}]/g, '');
  return s;
}

function wrap(a: string): string {
  const t = a.trim();
  return /^[\w√]+$/u.test(t) ? t : `(${t})`;
}

const SUP: Record<string, string> = {
  '0': '⁰', '1': '¹', '2': '²', '3': '³', '4': '⁴', '5': '⁵', '6': '⁶', '7': '⁷', '8': '⁸', '9': '⁹', '+': '⁺',
  '-': '⁻', n: 'ⁿ',
};
function sup(a: string): string {
  const t = a.trim();
  return [...t].every((ch) => ch in SUP) ? [...t].map((ch) => SUP[ch]).join('') : `^(${t})`;
}
