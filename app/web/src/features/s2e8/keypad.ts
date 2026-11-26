/**
 * Bàn phím toán của bài luyện (US-S2E8-01, DQ-11): bảng phím, chèn theo vị trí con trỏ, xóa lùi, lọc chuỗi dán.
 * Hàm thuần để kiểm bằng test đơn vị; giao diện ở MathKeypad.tsx.
 */

/** 4 hàng × 6 cột, cùng thứ tự trên iPad và iPhone (BR-S2E8-01). */
export const KEY_ROWS: readonly (readonly string[])[] = [
  ['7', '8', '9', '(', ')', '⌫'],
  ['4', '5', '6', '+', '-', '√'],
  ['1', '2', '3', '×', '÷', 'π'],
  ['0', ',', '°', '/', '^', 'a'],
];

export const BACKSPACE = '⌫';
export const MAX_LENGTH = 50;
export const MSG_MAX = 'Tối đa 50 ký tự';

/** Ký tự ô đáp số nhận (BR-S2E8-02, 05): đúng bảng phím. */
export const ALLOWED = new Set([...'0123456789()+-×÷√π,°/^ax']);

/** Tên đọc của phím cho data-testid và aria-label. */
export const KEY_NAMES: Record<string, string> = {
  '(': 'mo-ngoac',
  ')': 'dong-ngoac',
  '⌫': 'xoa-lui',
  '+': 'cong',
  '-': 'tru',
  '√': 'can',
  '×': 'nhan',
  '÷': 'chia',
  π: 'pi',
  ',': 'phay',
  '°': 'do',
  '/': 'phan-so',
  '^': 'mu',
};

export const keyName = (k: string): string => KEY_NAMES[k] ?? k;

export interface Field {
  value: string;
  /** Vị trí con trỏ: 0 … value.length. */
  caret: number;
  /** Lần chèn vừa rồi bị chặn vì đủ 50 ký tự. */
  full: boolean;
}

export const emptyField: Field = { value: '', caret: 0, full: false };

const chars = (s: string): string[] => [...s];

/** Chèn một ký tự của bảng phím tại con trỏ; đủ 50 ký tự thì không chèn và bật cờ full (BR-S2E8-03). */
export function insert(f: Field, ch: string): Field {
  if (!ALLOWED.has(ch)) return { ...f, full: false };
  const cs = chars(f.value);
  if (cs.length >= MAX_LENGTH) return { ...f, full: true };
  const at = Math.max(0, Math.min(f.caret, cs.length));
  cs.splice(at, 0, ch);
  return { value: cs.join(''), caret: at + 1, full: false };
}

/** Xóa ký tự ngay trước con trỏ. */
export function backspace(f: Field): Field {
  const cs = chars(f.value);
  const at = Math.max(0, Math.min(f.caret, cs.length));
  if (at === 0) return { ...f, full: false };
  cs.splice(at - 1, 1);
  return { value: cs.join(''), caret: at - 1, full: false };
}

/** Chỉ giữ ký tự thuộc bảng phím (dấu trừ dài, dấu chấm, dấu sao đổi về ký hiệu của bảng phím). */
export function filterText(raw: string): string {
  return chars(raw)
    .map((c) => (c === '−' || c === '–' ? '-' : c === '*' ? '×' : c))
    .filter((c) => ALLOWED.has(c))
    .join('');
}

/** Dán: lọc theo bảng phím rồi chèn tại con trỏ, cắt ở 50 ký tự (BR-S2E8-05). */
export function paste(f: Field, raw: string): Field {
  let out: Field = { ...f, full: false };
  for (const c of chars(filterText(raw))) {
    out = insert(out, c);
    if (out.full) break;
  }
  return out;
}

export function setCaret(f: Field, caret: number): Field {
  return { ...f, caret: Math.max(0, Math.min(caret, chars(f.value).length)), full: false };
}

/** Mã chống lặp cho một lần bấm Kiểm tra (BR-S2E8-14): gửi lại sau mất mạng dùng CÙNG mã. */
export function newRequestKey(): string {
  const c = globalThis.crypto;
  if (c && typeof c.randomUUID === 'function') return c.randomUUID();
  return `k-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`;
}
