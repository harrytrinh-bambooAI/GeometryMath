/**
 * Toàn màn hình cho Trình chiếu (BR-S2E7-01). Trình duyệt chỉ cho xin toàn màn hình trong thao tác của người dùng,
 * nên lời xin được gửi NGAY trong lúc bấm "Trình chiếu" / "Tiếp tục"; màn chiếu đọc kết quả sau. Bị từ chối thì màn
 * chiếu dùng bố cục cửa sổ lớn lấp đầy trang (US-S2E7-01-FC2).
 */
let pending: Promise<boolean> | null = null;

export function requestPresentFullscreen(): void {
  const el = document.documentElement;
  try {
    pending = typeof el.requestFullscreen === 'function' ? el.requestFullscreen().then(() => true, () => false) : Promise.resolve(false);
  } catch {
    pending = Promise.resolve(false);
  }
}

export function fullscreenResult(): Promise<boolean> {
  return pending ?? Promise.resolve(Boolean(document.fullscreenElement));
}

export function exitPresentFullscreen(): void {
  pending = null;
  if (document.fullscreenElement && typeof document.exitFullscreen === 'function') document.exitFullscreen().catch(() => undefined);
}
