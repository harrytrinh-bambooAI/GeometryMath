# Bộ xem 3D dùng chung (`shared/viewer`)

Phiên bản: v1.0 · 23/11/2026 · chủ sở hữu E3 (US-GEO-301…307). E4 dùng lại cho lời giải đồng bộ hình.

## Dùng

```tsx
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import { fromScene } from '../../shared/viewer/adapter.ts';

const solid = useMemo(() => fromScene(scene), [scene]); // Scene: hợp đồng E2 v1 (app/docs/SCENE-CONTRACT.md)
<Figure3DViewer solid={solid} lock={lock} onToolEvent={send} />
```

| Prop | Kiểu | Ý nghĩa |
|---|---|---|
| `solid` | `ViewerSolid` (`types.ts`) | Điểm, cạnh, mặt (có thể lõm), đường tròn, đơn vị `a`, đại lượng ẩn, thẻ ràng buộc. Đổi đối tượng ⇒ dựng lại hình (mất phần vẽ thêm). Giữ tham chiếu ổn định bằng `useMemo`. |
| `lock` | `'locked' \| 'open'` | Mặc định `'locked'`. Khóa thì đo đại lượng trong `hiddenMeasures` hiện MSG-GEO-24, không có số (BR-GEO-156). Đổi sang `'open'` thì lần đo kế tiếp hiện số (US-GEO-305-SC6). |
| `onToolEvent` | `(e: {tool, code}) => void` | Gọi mỗi thao tác trên hình (chọn, điểm, đường, mặt phẳng, mở rộng, đo). Không gọi khi xoay, thu phóng, đặt lại, góc nhìn (BR-GEO-144). `code` = mã `event_log.arg` (D-GEO-058). Gom lô: `features/e3/useToolEvents.ts`. |
| `readOnly` | `boolean` | Ẩn thanh công cụ; vẫn xoay, thu phóng, góc nhìn. |
| `banner` | `ReactNode` | Thông điệp cố định trên khung (ví dụ MSG-GEO-16 của hình AI dựng). |
| `highlight` | `{points?, edges?, faces?}` | Tô màu đối tượng theo tên/id (E4: bước lời giải). Id cạnh = hai tên đỉnh nối liền theo thứ tự đề (`SD`), id mặt = tên đỉnh đi vòng (`SAD`, `ABCD`). |
| `highlight.angles` | `string[]` | E4: góc được tô, ba tên đỉnh với đỉnh góc ở giữa (`SDA`). |
| `overlays` | `ViewerOverlay[]` (`overlay.tsx`) | E4: phần vẽ thêm của lời giải: `segment {a,b}` đường phụ, `right {at,a,b}` ký hiệu góc vuông, `arc {at,a,b,label}` cung góc kèm số đo. Dựng 3D rồi chiếu. Móc kiểm thử thêm khóa `solution: {highlight, shapes}`. |
| `testId` | `string` | Tiền tố data-testid (mặc định `viewer`). Hai bộ xem trên cùng trang phải khác tiền tố. |
| `className` | `string` | Lớp CSS cho khung ngoài; khung cần có chiều cao (bộ xem cao 100 %). |

## Cử chỉ và bố cục
- Một ngón/chuột kéo: xoay; hai ngón chụm/dang hoặc lăn chuột: thu phóng quanh điểm giữa (giới hạn 0,4…4); trang không cuộn, không phóng.
- "Nhìn từ trước", "Nhìn từ trên", "Xoay tự do" ở góc phải trên; "Đặt lại" chỉ đưa góc nhìn về ban đầu, giữ phần vẽ thêm; "Xóa hết" xóa phần vẽ thêm, giữ góc nhìn.
- ≤ 600 px (điện thoại): sáu nút chính + "Thêm" (BR-GEO-161), nút ≥ 44 × 44. Chỉ đổi bằng CSS nên xoay máy không mất trạng thái.

## Móc kiểm thử `window.__geoScene` (PO-52)
Chỉ đọc, bật ở `vite` dev hoặc bản build có `VITE_GEO_TEST_HOOKS=1` (`make e2e`); production không có. Nội dung: `sceneHook.ts`
(`camera`, `viewport`, `points[].screen` theo px trang, `edges[].style` `solid|dashed`, `faces`, `planes`, `traces`, `measures`,
`selection`, `opCount`, `lock`). Tiện ích Playwright: `app/e2e/tests/e3/e3.ts`.

## Tệp
`Figure3DViewer.tsx` (component) · `model.ts` (trạng thái, công cụ) · `render.ts`, `camera.ts`, `visibility.ts` (chiếu, nét đứt) ·
`pick.ts` (chạm trúng) · `measures.ts`, `format.ts` (đo, dấu phẩy, `a√2`) · `adapter.ts` (hợp đồng E2 → `ViewerSolid`) · `viewer.test.ts`.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 23/11/2026 | Bản đầu: API component, cử chỉ, móc kiểm thử | agent E3b |
| v1.1 | 23/11/2026 | E4: `highlight.angles`, `overlays` (overlay.tsx), khóa `solution` của móc; `rankNames` xuất khẩu để E4 tính id cạnh | agent E4 |
