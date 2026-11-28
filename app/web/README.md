# Giao diện web (React + Vite + TypeScript, PO-50)

Khung giao diện cho các epic E1…E7. Đợt 0 chỉ có khung: trang chủ, vùng Giáo viên `/gv`, vùng Học sinh `/hs`,
máy khách API, helper giờ Việt Nam, chỗ đặt hình 3D. Chưa có tính năng nghiệp vụ.

## Lệnh (chạy trong `app/web`, hoặc dùng `make web-*` ở `app/`)
```
npm ci                 # cài đúng phiên bản trong package-lock.json (make web-install)
npm run dev            # Vite ở GEO_WEB_PORT (mặc định 5173), proxy /api /health /dev -> 127.0.0.1:GEO_PORT (make web-dev)
npm run typecheck      # tsc -b
npm run lint           # ESLint, 0 cảnh báo
npm test               # node --test: kiểm thử hàm thuần (giờ Việt Nam)
npm run build          # tsc + vite build -> dist/ (make web-build)
npm run preview        # phục vụ dist/ ở GEO_WEB_PORT, cùng proxy (E2E dùng cách này)
```
Cổng lấy từ biến môi trường hoặc `app/.env.ports` (xem `app/README.md`). Node ≥ 22.18 (máy dự án: Node 26).
Mọi phiên bản thư viện ghim chính xác (`.npmrc` có `save-exact=true`); danh sách ở `app/docs/STACK.md`.

## Cấu trúc
| Đường dẫn | Vai trò |
|---|---|
| `src/main.tsx`, `src/app/router.tsx` | điểm vào, bản đồ đường dẫn `/`, `/gv/*`, `/hs/*` |
| `src/app/AreaLayout.tsx` | khung một vùng: rail trái (thanh dưới trên điện thoại) + nội dung |
| `src/features/<epic>/index.tsx` | **chỗ cắm của từng epic** (`e1`…`e7`): khai báo `FeatureModule` (route + mục rail) |
| `src/features/index.ts` | danh sách epic; đã liệt kê đủ e1…e7, epic không cần sửa |
| `src/shared/api/client.ts` | `api.get/post/...`: gắn `Authorization: Bearer`, ném `ApiError` theo mô hình lỗi máy chủ |
| `src/shared/api/errors.ts` | `ApiError` (`code`, `status`, `message` tiếng Việt, `requestId`, `details`, `retryAfterSeconds`) |
| `src/shared/auth/session.ts` | lưu thẻ JWT (sessionStorage); 401 -> xóa thẻ, phát `geo:unauthenticated`, khung về `/` |
| `src/shared/time/vnTime.ts` | `formatVnDateTime/Date/Time`: LUÔN giờ Asia/Ho_Chi_Minh (BR-GEO-233) |
| `src/shared/figure3d/Figure3D.tsx` | chỗ đặt hình 3D; `loadThree()` nạp Three.js động (chưa làm tính năng) |
| `src/shared/ui/ErrorNotice.tsx` | hiển thị lỗi + mã yêu cầu + nút Thử lại |
| `src/shared/ui/Icon.tsx`, `icons.ts` | icon dùng chung `<Icon name="check" />` (aria-hidden, màu `currentColor` theo token theme). MỘT bộ Material Symbols Rounded đặc cho cả 4 theme (DECISIONS 26/11). Mục rail khai báo `icon` trong `NavItem` |
| `src/styles/icons.css`, `src/assets/fonts/` | luật `.ms` + font icon TỰ LƯU (chỉ các glyph trong `icons.ts`, ~7 KB, Apache-2.0; không gọi Google lúc chạy). Thêm icon: sửa `icons.ts` rồi `node scripts/fetch-icon-font.mjs` |
| `src/styles/tokens.css` | token màu, bo góc, bóng, font chép từ `Design/assets/ds.css` (bộ CSS của `Design/prototype/index.html`) |

### Epic cắm màn như thế nào
1. Tạo màn trong `src/features/e2/...` (ví dụ `KhoDePage.tsx`).
2. Khai báo trong `src/features/e2/index.tsx`:
   ```ts
   teacherRoutes: [{ path: 'kho-de', element: <KhoDePage /> }],   // -> /gv/kho-de
   teacherNav: [{ to: 'kho-de', label: 'Kho đề', testId: 'e2-nav-kho-de' }],
   ```
3. Gọi API qua `api` (không tự `fetch`), hiện lỗi bằng `ErrorNotice`, hiện giờ bằng `formatVnDateTime`.
   ESLint chặn `toLocaleString/DateString/TimeString` ngoài `src/shared/time`.

## Quy ước `data-testid`
```
<vùng>-<thành phần>[-<chi tiết>]       chữ thường, không dấu, gạch ngang
```
- `vùng`: `shell` (khung chung), `home` (trang chủ), `gv` / `hs` (khung vùng), `e1`…`e7` (màn của epic).
- Phần tử lặp: thêm khóa nghiệp vụ ổn định ở cuối (`e6-assignment-row-BG-1`, `e7-student-An`), không dùng chỉ số dòng.
- Trạng thái đọc bằng thuộc tính phụ, không đọc từ chữ: `data-status`, `data-error-code`.
- Dùng `tid('e2', 'nav', 'kho-de')` trong `src/shared/testid.ts` (tự bỏ dấu, nối gạch) thay vì gõ tay.
- Test E2E chọn phần tử bằng `page.getByTestId(...)`; chỉ dùng chữ hiển thị khi TC kiểm câu chữ (MSG-GEO-nn).

Mã có sẵn: `home-title`, `home-clock`, `home-server-status`, `home-server-error`, `home-role-teacher`, `home-role-student`,
`gv-shell`, `gv-rail`, `gv-title`, `gv-nav-overview`, `gv-overview`, `gv-overview-e1`…, tương tự `hs-…`,
`shell-home`, `shell-not-found`, `shell-route-error`, `shell-error`, `figure3d`.

## Phục vụ
- Dev: Vite (HMR) + proxy sang máy chủ Java. Trình duyệt chỉ thấy một nguồn nên không cần CORS.
- Build: `dist/` là tệp tĩnh, phục vụ bởi máy chủ tĩnh / reverse proxy cùng nguồn với `/api` (lý do: `app/docs/STACK.md`).
  E2E dùng `vite preview` để phục vụ đúng bản build.
