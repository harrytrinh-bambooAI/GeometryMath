/** S3-E2 — hàm thuần định dạng giờ và cảnh báo (kiểm bằng node:test ở format.test.ts). */

/** "yyyy-MM-ddTHH:mm" của ô datetime-local (giờ trình duyệt) ↔ ISO. */
export function toLocalInput(iso: string | null | undefined): string {
  if (!iso) return '';
  const d = new Date(iso);
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}
export function fromLocalInput(v: string): string | null {
  if (!v) return null;
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? null : d.toISOString();
}

/** Giờ mở sớm hơn hạn từ 1 phút trở lên (BR-S3E2-13, QA-S3E2-03). */
export function releaseBeforeDue(release: string | null, due: string | null): boolean {
  if (!release || !due) return false;
  return new Date(release).getTime() + 60_000 <= new Date(due).getTime();
}

const DAYS = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
/** "T6 04/12/2026 20:00" theo giờ Việt Nam. */
export function dueLabel(iso: string): string {
  const d = new Date(new Date(iso).getTime() + 7 * 3_600_000);
  const p = (n: number) => String(n).padStart(2, '0');
  return `${DAYS[d.getUTCDay()]} ${p(d.getUTCDate())}/${p(d.getUTCMonth() + 1)}/${d.getUTCFullYear()} ${p(d.getUTCHours())}:${p(d.getUTCMinutes())}`;
}
