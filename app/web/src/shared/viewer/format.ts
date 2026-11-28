/**
 * Định dạng số đo (BR-GEO-158, D-GEO-115): dấu phẩy thập phân, góc bằng độ, hình có tham số thì độ dài theo
 * tham số dạng căn ("a√2", "a√3/2", "2a"). Hình không có tham số: số thập phân (tối đa 2 chữ số).
 */
export function formatDecimal(x: number, digits = 2): string {
  const r = Number(x.toFixed(digits));
  const s = (Object.is(r, -0) ? 0 : r).toString();
  return s.replace('.', ',');
}

/** Hệ số k√m/q (k, q nguyên dương, m không chính phương) của một số dương, hoặc null. */
export function surd(x: number): { k: number; m: number; q: number } | null {
  if (!(x > 0)) return null;
  for (let q = 1; q <= 12; q++) {
    const sq = (x * q) ** 2;
    const n = Math.round(sq);
    if (n < 1 || n > 100_000 || Math.abs(sq - n) > 1e-6 * Math.max(1, n)) continue;
    let k = 1;
    let m = n;
    for (let f = 2; f * f <= m; f++) {
      while (m % (f * f) === 0) {
        m /= f * f;
        k *= f;
      }
    }
    const g = gcd(k, q);
    return { k: k / g, m, q: q / g };
  }
  return null;
}
const gcd = (a: number, b: number): number => (b ? gcd(b, a % b) : a);

export interface LengthUnit {
  /** Tên tham số độ dài của đề, ví dụ "a". */
  symbol: string;
  /** Giá trị số của tham số trong tọa độ (đề dựng với a = 1 thì 1). */
  value: number;
}

export function formatLength(value: number, unitParam?: LengthUnit | null): string {
  if (Math.abs(value) < 1e-9) return '0';
  if (unitParam) {
    const s = surd(value / unitParam.value);
    if (s) {
      const coef = s.k === 1 ? '' : String(s.k);
      const root = s.m === 1 ? '' : `√${s.m}`;
      const body = `${coef}${unitParam.symbol}${root}`;
      return s.q === 1 ? body : `${body}/${s.q}`;
    }
    return `${formatDecimal(value / unitParam.value)}${unitParam.symbol}`;
  }
  return formatDecimal(value);
}

export function formatAngle(deg: number): string {
  const r = Math.round(deg);
  return `${Math.abs(deg - r) < 1e-6 ? r : formatDecimal(deg, 1)}°`;
}
