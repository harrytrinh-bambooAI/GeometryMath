import type { ReactNode } from 'react';

/** Nhãn tiếng Việt của Kho đề (BR-GEO-202, D-GEO-070) và hình thu nhỏ của thẻ đề (BR-GEO-204). */

export const LEVELS = [
  ['', 'Tất cả'],
  ['thcs', 'THCS'],
  ['thpt', 'THPT'],
] as const;

/** Một giá trị cho mỗi loại hình (D-GEO-070: từng hình riêng, không gộp "Nón · Trụ · Cầu"). */
export const SOLIDS = [
  ['', 'Tất cả'],
  ['pyramid', 'Hình chóp'],
  ['prism', 'Lăng trụ'],
  ['box', 'Hình hộp'],
  ['cube', 'Lập phương'],
  ['cone', 'Nón'],
  ['cylinder', 'Trụ'],
  ['sphere', 'Cầu'],
] as const;

/** Không có "Thiết diện", "Độ dài" (BR-GEO-205). */
export const TOPICS = [
  ['', 'Tất cả'],
  ['angle', 'Góc'],
  ['distance', 'Khoảng cách'],
  ['volume', 'Thể tích'],
  ['area', 'Diện tích'],
  ['sphere', 'Mặt cầu'],
] as const;

export const levelLabel = (l: string) => (l === 'thpt' ? 'THPT' : l === 'thcs' ? 'THCS' : l.toUpperCase());

/** Dòng nhãn người: luôn "AI soạn" (BR-GEO-207) kèm số bước lời giải. */
export const authoredLine = (steps: number | null) =>
  steps == null ? 'AI soạn · chưa có lời giải' : `AI soạn · ${steps} bước`;

const STROKE = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.6, strokeLinejoin: 'round' as const };
const DASH = { ...STROKE, strokeDasharray: '3 3' };

/** Hình thu nhỏ theo loại hình (vẽ tay, không phải hình dựng của đề). */
export function SolidThumb({ kind, testId }: { kind: string; testId?: string }) {
  let body: ReactNode;
  switch (kind) {
    case 'pyramid':
      body = (
        <>
          <path d="M14 46 L46 46 L56 36 L24 36 Z" {...STROKE} />
          <path d="M35 8 L14 46 M35 8 L46 46 M35 8 L56 36" {...STROKE} />
          <path d="M35 8 L24 36" {...DASH} />
        </>
      );
      break;
    case 'cube':
    case 'box':
      body = (
        <>
          <path d="M12 22 L40 22 L40 50 L12 50 Z M12 22 L24 10 L52 10 L40 22 M52 10 L52 38 L40 50" {...STROKE} />
          <path d="M24 10 L24 38 L12 50 M24 38 L52 38" {...DASH} />
        </>
      );
      break;
    case 'prism':
      body = (
        <>
          <path d="M12 48 L42 48 L30 38 M12 48 L12 14 L42 14 L30 4 L12 14 M42 14 L42 48" {...STROKE} />
          <path d="M30 4 L30 38 L12 48" {...DASH} />
        </>
      );
      break;
    case 'cone':
      body = (
        <>
          <path d="M32 6 L12 46 M32 6 L52 46" {...STROKE} />
          <path d="M12 46 A20 6 0 0 0 52 46" {...STROKE} />
          <path d="M12 46 A20 6 0 0 1 52 46" {...DASH} />
        </>
      );
      break;
    case 'cylinder':
      body = (
        <>
          <ellipse cx="32" cy="12" rx="18" ry="5" {...STROKE} />
          <path d="M14 12 L14 46 M50 12 L50 46 M14 46 A18 5 0 0 0 50 46" {...STROKE} />
          <path d="M14 46 A18 5 0 0 1 50 46" {...DASH} />
        </>
      );
      break;
    default:
      body = (
        <>
          <circle cx="32" cy="28" r="20" {...STROKE} />
          <path d="M12 28 A20 6 0 0 0 52 28" {...STROKE} />
          <path d="M12 28 A20 6 0 0 1 52 28" {...DASH} />
        </>
      );
  }
  return (
    <svg viewBox="0 0 64 56" className="e5-thumb" role="img" aria-label={kind} data-kind={kind} data-testid={testId}>
      {body}
    </svg>
  );
}
