Phiên bản: v0.2 · 2026-11-26 · Designer Dom

# 4 hướng style cho GEO (app học hình học không gian lớp 11)

Mục đích: owner chọn hướng. Dom không chọn thay. Xem `index.html` (so sánh, ảnh phóng to) và `png/<hướng>-<màn>.png`.
Nguồn: `Design/s23/*.png`, `DQ.md`, `REVIEW-Dom.md` (26/11), `Design/prototype/s23-base.css` (token hiện tại), `Design/Q_Dom.md` v0.3 (A5: chữ ≥ 14 px, đã áp dụng cho cả 4 hướng).
Màn dựng lại: (1) HS làm bài (A: iPhone 390×844; B, C, D: iPad 1024×768), (2) GV “Cả lớp vướng ở đâu” 1440×900, (3) Thêm đề – soát và duyệt lời giải 1440×900, (4) bảng component.
Sinh lại: `node Design/styles/build.js` rồi `PW_CORE=<playwright-core> node Design/styles/shoot.js`. Kiểm tương phản: `node Design/styles/contrast.js`.

## A · Lớp học sáng
- Chuẩn: **Material Design 3** (tonal, pill). Bo: nút/chip/ô nhập 999 px (pill), thẻ 24 px, phím 16 px.
- Theme sáng: chính #00796B, nhấn vàng #FFC83D, san hô #E8503F, nền #F4FAF7. Font Nunito. Icon Material Symbols Rounded đặc.
- Bố cục: tab đáy (iPhone), rail trái 88 px (web), thẻ bento. Mật độ thấp.
- Cao chạm: nút 52, phím 52, tab 44. Nút chính chữ trắng 5,3:1.
- Hợp: HS (thân thiện, nút to). Hạn chế: ít dòng trên màn cho GV; icon là font Google, phải tự lưu khi chạy offline.

## B · Bảng đen tối
- Chuẩn: **IBM Carbon**, theme g100. Bo 0 px (chip 24 px). Nút khối vuông, ô nhập nền đặc có gạch chân.
- Theme tối #161616, lớp #262626, nhấn xanh #0F62FE, nét phấn vàng #FFD966. Font IBM Plex Sans/Mono, nhãn hình Patrick Hand. Icon: dùng chung bộ của A (Material Symbols Rounded đặc), màu theo token B.
- Bố cục: thanh trên (UI shell 48 px), bảng dữ liệu, bước dạng thanh. Hình vẽ nét phấn có nhiễu.
- Cao chạm: nút 48, phím 52, hàng bảng 38–48. Chữ/nền 16,5:1.
- Hợp: trình chiếu trên lớp, GV đọc bảng. Hạn chế: tối trong lớp sáng dễ mỏi, vuông ít thân thiện với HS.

## C · Giấy kẻ ô
- Chuẩn: **Apple HIG**. Bo: nút/ô nhập 10 px, thẻ 14 px, phím 8 px, segmented 9 px.
- Theme giấy #FBF8EE, ô li xanh nhạt, lề đỏ. Xanh hệ thống đậm #0A62C9. Font Be Vietnam Pro, ghi chú Caveat. Icon: dùng chung bộ của A (Material Symbols Rounded đặc), màu theo token C.
- Bố cục: thanh tab nổi phía trên, “tờ giấy” là thẻ trắng. Biểu đồ cột kẻ tay, khoanh đỏ “câu 3”. Hình nét chì rung nhẹ, mặt gạch chéo.
- Cao chạm: nút 44, phím 50, tab 40–44. Nút chính 5,8:1.
- Hợp: cảm giác vở toán cho HS. Hạn chế: nền ô li nhiễu khi bảng dày; Caveat dấu tiếng Việt cần kiểm trên iPad thật.

## D · Bảng điều khiển gọn
- Chuẩn: **shadcn/Radix** (token) + **Fluent 2** (mật độ, vòng tiêu điểm). Bo 6 px (chip 4 px).
- Theme sáng slate, nhấn #1D4ED8. Font Inter, số JetBrains Mono. Icon: dùng chung bộ của A (Material Symbols Rounded đặc), màu theo token D.
- Bố cục: sidebar trái 224 px có nhãn, thanh lọc, KPI, bảng có xu hướng, khung chi tiết phải. Mật độ cao nhất.
- Cao chạm: nút 36 (web), 44–48 (iPad), phím 48. Nút chính 6,7:1.
- Hợp: GV quét 36 em nhanh. Hạn chế: ít cảm xúc cho HS; 36 px phải nâng lên 44 khi cảm ứng.

## Chuẩn component chung (mọi hướng đều có)
Nút (chính, viền/phụ, ghost, nguy hiểm; mặc định, di chuột, nhấn, tiêu điểm, vô hiệu), ô nhập (mặc định, tiêu điểm, lỗi, vô hiệu; ô đáp số 24–26 px), chip/badge (đúng, cần xem, sai, thông tin, trung tính), thẻ, tab, thanh tiến độ (phân đoạn theo câu và thanh phần trăm), toast (thành công, lỗi), bàn phím toán 6 cột × 4 hàng + hàng Kiểm tra, bộ icon 23 biểu tượng.
Quy tắc chung: chữ ≥ 14 px; vùng chạm của nút/phím ≥ 44 px (D web 36 px là ngoại lệ chỉ cho chuột, cảm ứng 44); chữ thường ≥ 4,5:1 (đã đo bằng `contrast.js`, 22/22 cặp đạt); trạng thái không chỉ dựa vào màu (chip có chữ và icon).

## Khuyến nghị của Dom (owner quyết)
1. Hai tầng, chung token: HS dùng **A**, GV dùng **D**; hai bên chia nhau màu chính và quy tắc chạm.
2. **B** làm chế độ “Trình chiếu” (bản mẫu đã có màn trình chiếu tối).
3. **C** giữ làm phương án bản sắc “vở toán”; nếu chọn thì dùng cho hình và màn tự học, không toàn app.
4. Cần owner trả lời: giữ một phong cách hay hai tầng (HS/GV)? Có tự lưu font icon cho offline (PWA)? Có cần theme tối cho HS không?
Rủi ro chung: ảnh chụp mô phỏng tĩnh, chưa thử trên iPad/iPhone thật; từ 26/11 (lệnh owner) cả 4 theme dùng một bộ icon Material Symbols Rounded đặc, màu theo token từng theme; là font Google nên app phải tự lưu (self-host) để chạy offline.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Bản đầu: 4 hướng, 16 ảnh, index so sánh, kiểm tương phản | Designer Dom (runner-T-261126-39) |
| v0.2 | 2026-11-26 | Cả 4 hướng dùng bộ icon của A (Material Symbols Rounded đặc), màu theo token từng theme; dựng lại 16 HTML + 16 ảnh | phiên điều phối (lệnh owner) |
