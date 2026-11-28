/**
 * S5-E4 · nộp bài viết tay (US-S5E4-03, 09) — luật thuần phía máy (T-261128-59): độ rõ, xếp trang, trần trang/cỡ ảnh,
 * thông điệp MSG-S5-32/35/36.
 */

/** Điểm độ rõ tối thiểu: 0,50 đạt, 0,49 không đạt (BR-S5E4-07). */
export const MIN_CLARITY = 0.5;
/** Mỗi bài nộp 1–10 trang; mỗi trang ≤ 8 MB sau khi máy nén (QS5-19). */
export const MAX_PAGES = 10;
export const MAX_PAGE_BYTES = 8 * 1024 * 1024;
/** Cạnh dài tối đa khi nén ảnh trước khi tải. */
export const MAX_EDGE = 2400;
/** Phương sai Laplace ứng với điểm 1,0 (ảnh nét); ≤ K/2 là mờ. */
export const CLARITY_K = 200;

export const MSG = {
  s32: (n: number) => `Ảnh trang ${n} hơi mờ. Em chụp lại trang này nhé.`,
  s35: 'Mất kết nối. Bài của em đã lưu trên máy và sẽ gửi lại khi có mạng.',
  s36: 'Nhớ: không ghi họ tên, số điện thoại lên bài. Ảnh chỉ thầy cô lớp em xem, được xử lý bằng AI để đọc chữ và đề xuất điểm, và bị xoá cuối năm học.',
  full: 'Máy em hết chỗ lưu, hãy nộp các trang đã chụp hoặc xoá bớt ảnh',
  tooMany: 'Mỗi bài nộp tối đa 10 trang.',
  tooBig: 'Ảnh trang này lớn hơn 8 MB sau khi nén. Em chụp lại gần hơn nhé.',
} as const;

export const isClear = (score: number) => score >= MIN_CLARITY;

/**
 * Điểm độ rõ 0–1 từ ảnh xám: phương sai của toán tử Laplace 4 lân cận, chia CLARITY_K, chặn ở 1.
 * Ảnh trắng trơn hoặc mờ nhoè cho điểm thấp; nét chữ rõ cho điểm cao.
 */
export function clarityScore(gray: ArrayLike<number>, width: number, height: number): number {
  if (width < 3 || height < 3) return 0;
  let sum = 0;
  let sum2 = 0;
  let n = 0;
  for (let y = 1; y < height - 1; y++) {
    for (let x = 1; x < width - 1; x++) {
      const i = y * width + x;
      const v = 4 * gray[i]! - gray[i - 1]! - gray[i + 1]! - gray[i - width]! - gray[i + width]!;
      sum += v;
      sum2 += v * v;
      n++;
    }
  }
  const mean = sum / n;
  const variance = sum2 / n - mean * mean;
  return Math.max(0, Math.min(1, Math.round((variance / CLARITY_K) * 100) / 100));
}

/** Đổi chỗ trang (kéo lên/xuống): trả mảng mới, chỉ số ngoài khoảng thì giữ nguyên. */
export function move<T>(pages: readonly T[], from: number, to: number): T[] {
  if (from < 0 || from >= pages.length || to < 0 || to >= pages.length || from === to) return [...pages];
  const out = [...pages];
  const [p] = out.splice(from, 1);
  out.splice(to, 0, p!);
  return out;
}

export interface PageState {
  clarity: number;
  uploadId?: string;
  bytes: number;
}

/** Nút Nộp bật khi đã đọc thông báo, có 1–10 trang, mọi trang rõ và không quá 8 MB. */
export function canSubmit(acked: boolean, pages: readonly PageState[]): boolean {
  return acked && pages.length >= 1 && pages.length <= MAX_PAGES && pages.every((p) => isClear(p.clarity) && p.bytes <= MAX_PAGE_BYTES);
}

/**
 * Gỡ đoạn APP1…APP15 (EXIF, XMP, ICC…) và COM khỏi JPEG ngay trên máy, giống bộ gỡ của máy chủ (MetadataStripper):
 * tệp tải lên đã sạch nên băm trên máy chủ trùng băm tệp máy tải lên (US-S5E4-03-SC1). Không phải JPEG thì trả nguyên.
 */
export function stripJpeg(b: Uint8Array): Uint8Array {
  if (b.length < 4 || b[0] !== 0xff || b[1] !== 0xd8) return b;
  const out: Uint8Array[] = [b.subarray(0, 2)];
  let i = 2;
  while (i + 4 <= b.length) {
    if (b[i] !== 0xff) return b;
    const m = b[i + 1]!;
    if (m === 0xff) {
      i++;
      continue;
    }
    if (m === 0xd9) {
      out.push(b.subarray(i, i + 2));
      break;
    }
    if ((m >= 0xd0 && m <= 0xd7) || m === 0x01) {
      out.push(b.subarray(i, i + 2));
      i += 2;
      continue;
    }
    const len = (b[i + 2]! << 8) | b[i + 3]!;
    if (len < 2 || i + 2 + len > b.length) return b;
    if (m === 0xda) {
      out.push(b.subarray(i));
      break;
    }
    if (!((m >= 0xe1 && m <= 0xef) || m === 0xfe)) out.push(b.subarray(i, i + 2 + len));
    i += 2 + len;
  }
  const n = out.reduce((s, x) => s + x.length, 0);
  const r = new Uint8Array(n);
  let o = 0;
  for (const x of out) {
    r.set(x, o);
    o += x.length;
  }
  return r;
}

/** Khóa lũy đẳng sinh trên máy (BR-S5E4-09). */
export function newKey(): string {
  return `k-${globalThis.crypto.randomUUID()}`;
}

/** Lỗi hết chỗ lưu của trình duyệt (QuotaExceededError). */
export function isQuotaError(e: unknown): boolean {
  return (
    typeof e === 'object' &&
    e !== null &&
    'name' in e &&
    ((e as { name: string }).name === 'QuotaExceededError' || (e as { name: string }).name === 'NS_ERROR_DOM_QUOTA_REACHED')
  );
}
