import { ICONS } from './icons.ts';
import type { IconName } from './icons.ts';

/**
 * Icon dùng chung (Material Symbols Rounded đặc, font tự lưu; luật `.ms` ở src/styles/icons.css).
 * - Trang trí: luôn `aria-hidden`; nghĩa do chữ bên cạnh (hoặc aria-label của nút) mang.
 * - Tên glyph nằm ở thuộc tính `data-icon`, CSS vẽ bằng `::before { content: attr(data-icon) }`, nên icon KHÔNG thêm
 *   chữ vào textContent của nút/chip (sao chép và test theo chữ không đổi).
 * - Màu: currentColor (theo token của theme đang chọn qua màu chữ của khối chứa). Cỡ: token --ic / --ic-chip.
 * - `inline`: icon nằm trong dòng chữ thường (khối chứa không phải flex): thêm khoảng cách phải.
 */
export function Icon({ name, inline, className }: { name: IconName; inline?: boolean; className?: string }) {
  const cls = `ms${inline ? ' ms-in' : ''}${className ? ` ${className}` : ''}`;
  return <span className={cls} aria-hidden="true" data-icon={ICONS[name]} />;
}
