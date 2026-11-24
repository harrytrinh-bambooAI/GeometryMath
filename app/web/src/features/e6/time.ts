/** Hàm thuần về giờ mở của E6 (chạy được bằng `node --test`, không cần trình duyệt). */

/** "Còn 1 giờ 0 phút" / "Còn 5 phút 3 giây" / "Còn 9 giây"; không bao giờ âm (US-GEO-612-SC2). */
export function remaining(ms: number): string {
  const s = Math.max(0, Math.ceil(ms / 1000));
  if (s >= 600) {
    // từ 10 phút trở lên: làm tròn lên theo phút ("Còn 1 giờ 0 phút" khi còn 59 phút 58 giây)
    const total = Math.ceil(s / 60);
    const h = Math.floor(total / 60);
    const m = total % 60;
    return h > 0 ? `Còn ${h} giờ ${m} phút` : `Còn ${m} phút`;
  }
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return m > 0 ? `Còn ${m} phút ${sec} giây` : `Còn ${sec} giây`;
}

/** Ô ngày (yyyy-MM-dd) + ô giờ (HH:mm) theo giờ Việt Nam → ISO UTC (BR-GEO-233: thiết bị ở múi giờ nào cũng vậy). */
export function vnToIso(date: string, time: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || !/^\d{2}:\d{2}$/.test(time)) return null;
  const d = new Date(`${date}T${time}:00+07:00`);
  return Number.isNaN(d.getTime()) ? null : d.toISOString();
}
