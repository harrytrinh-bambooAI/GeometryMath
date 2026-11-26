/**
 * Bảng icon của app: tên dùng trong mã -> tên Material Symbols (bộ Rounded, đặc: FILL 1, wght 500).
 * Nguồn: Design/styles/lib.js v0.2 (26/11/2026, bảng L.I) + các icon app cần thêm. DECISIONS 26/11 dòng "Icon":
 * cả 4 theme A/B/C/D dùng MỘT bộ này; màu theo token của theme đang chọn (currentColor).
 *
 * Font tự lưu ở src/assets/fonts/material-symbols-rounded-subset.woff2 và CHỈ chứa các glyph liệt kê ở đây.
 * Thêm icon: thêm dòng vào ICONS rồi chạy `node scripts/fetch-icon-font.mjs` (trong app/web) để tải lại tập glyph.
 * Tệp này thuần (không chạm DOM) để chạy được bằng `node --test`.
 */
export const ICONS = {
  // 23 icon của thiết kế (Design/styles/lib.js)
  home: 'home',
  cube: 'deployed_code',
  users: 'groups',
  layers: 'layers',
  clock: 'history',
  check: 'check',
  lock: 'lock',
  eye: 'visibility',
  redo: 'replay',
  back: 'backspace',
  file: 'description',
  chart: 'bar_chart',
  pencil: 'edit',
  spark: 'auto_awesome',
  shield: 'verified_user',
  next: 'arrow_forward',
  prev: 'arrow_back',
  x: 'close',
  plus: 'add',
  warn: 'warning',
  info: 'info',
  bolt: 'bolt',
  search: 'search',
  // app thêm
  settings: 'settings',
  unlock: 'lock_open',
  error: 'error',
  assign: 'assignment',
  send: 'send',
  camera: 'photo_camera',
  copy: 'content_copy',
  print: 'print',
  trash: 'delete',
  up: 'arrow_upward',
  down: 'arrow_downward',
  rotateLeft: 'rotate_left',
  rotateRight: 'rotate_right',
  login: 'login',
  save: 'save',
  schedule: 'schedule',
  hint: 'lightbulb',
} as const;

export type IconName = keyof typeof ICONS;

/** Tên glyph (đã sắp, không trùng) mà tệp font phải chứa. */
export function iconGlyphNames(): string[] {
  return [...new Set<string>(Object.values(ICONS))].sort();
}

/** Icon của nhãn máy: "Đã kiểm chứng" = dấu tích; còn lại ("Cần xem lại") = con mắt. */
export const verifyIcon = (verified: boolean): IconName => (verified ? 'check' : 'eye');

/** Icon trạng thái mở lời giải: đã mở = khóa mở; hẹn giờ = đồng hồ; còn lại = khóa. */
export const releaseIcon = (state: string): IconName =>
  state === 'open' ? 'unlock' : state === 'upcoming' ? 'schedule' : 'lock';
