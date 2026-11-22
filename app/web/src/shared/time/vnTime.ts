/**
 * Hiển thị giờ LUÔN theo Asia/Ho_Chi_Minh (BR-GEO-233, app/docs/CONFIG.md `geo.time.display-zone`),
 * bất kể múi giờ của máy người dùng. Máy chủ trả ISO-8601 UTC ("...Z"); chỉ định dạng ở đây.
 * Không import gì để chạy được bằng `node --test` (không cần trình duyệt).
 */
export const VN_TIME_ZONE = 'Asia/Ho_Chi_Minh';

type Input = string | number | Date;

function toDate(value: Input): Date {
  const d = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(d.getTime())) {
    throw new RangeError(`Giá trị thời gian không hợp lệ: ${String(value)}`);
  }
  return d;
}

const partsFormatter = new Intl.DateTimeFormat('en-GB', {
  timeZone: VN_TIME_ZONE,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hourCycle: 'h23',
});

/** Các thành phần lịch theo giờ Việt Nam. */
export function vnParts(value: Input): {
  year: string;
  month: string;
  day: string;
  hour: string;
  minute: string;
  second: string;
} {
  const out: Record<string, string> = {};
  for (const p of partsFormatter.formatToParts(toDate(value))) {
    out[p.type] = p.value;
  }
  return {
    year: out.year ?? '',
    month: out.month ?? '',
    day: out.day ?? '',
    hour: out.hour ?? '',
    minute: out.minute ?? '',
    second: out.second ?? '',
  };
}

/** "dd/MM/yyyy" theo giờ Việt Nam. */
export function formatVnDate(value: Input): string {
  const p = vnParts(value);
  return `${p.day}/${p.month}/${p.year}`;
}

/** "HH:mm" theo giờ Việt Nam (24 giờ). */
export function formatVnTime(value: Input): string {
  const p = vnParts(value);
  return `${p.hour}:${p.minute}`;
}

/** "HH:mm dd/MM/yyyy" theo giờ Việt Nam: định dạng mặc định cho mọi mốc giờ trên giao diện. */
export function formatVnDateTime(value: Input): string {
  return `${formatVnTime(value)} ${formatVnDate(value)}`;
}
