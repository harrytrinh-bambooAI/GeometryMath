/** Hàm thuần của E7 (chạy được bằng `node --test`). Giờ luôn hiện theo giờ Việt Nam (BR-GEO-233). */

/** "HH:mm, dd/MM/yyyy" giờ Việt Nam. */
export function vnDateTime(iso: string): string {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', {
      timeZone: 'Asia/Ho_Chi_Minh',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
    })
      .formatToParts(new Date(iso))
      .map((x) => [x.type, x.value]),
  );
  return `${p.hour}:${p.minute}, ${p.day}/${p.month}/${p.year}`;
}

/** "còn 1 giờ 0 phút" / "còn 12 phút" / "còn 1 phút" (làm tròn lên theo phút, không âm). */
export function remaining(ms: number): string {
  const total = Math.max(0, Math.ceil(ms / 60_000));
  const h = Math.floor(total / 60);
  const m = total % 60;
  return h > 0 ? `còn ${h} giờ ${m} phút` : `còn ${m} phút`;
}

/** "Mở lúc {giờ} · còn {thời gian}" hoặc "Chưa đặt giờ mở" (BR-GEO-269). */
export function lockLine(releaseAt: string | null, nowMs: number): string {
  if (!releaseAt) return 'Chưa đặt giờ mở';
  return `Mở lúc ${vnDateTime(releaseAt)} · ${remaining(new Date(releaseAt).getTime() - nowMs)}`;
}

export const RELEASE_STATE: Record<string, string> = {
  unset: 'Chưa đặt giờ',
  upcoming: 'Sắp mở',
  open: 'Đã mở',
};
