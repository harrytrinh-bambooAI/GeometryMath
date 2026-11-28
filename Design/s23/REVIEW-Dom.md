# REVIEW-Dom · Nhận xét cải thiện thiết kế chặng 2–3 (S23)
Phiên bản: v0.1 · 2026-11-26 · Designer Dom (agent runner-design-dom, T-261126-36)

Nguồn rà: `Design/s23/*.png` (24 ảnh gốc, 26/11), `Design/prototype/s23*.js|css`, `Design/s23/DQ.md` v0.1, `Design/Q_Dom.md` v0.2, spec `S2-E4/E8/E9`, `S3-E1/E4/E6` (chỉ đọc), `PO-DECISIONS.md` PO-61.
Phạm vi mắt xem: xem trực tiếp 10 ảnh (c2-02-lech-dap-so, c2-03-da-giao, c2-06-luyen-iphone, c3-01-ma-nguoi-dung, c3-04-ket-qua-lop, c2-04…); các ảnh còn lại rà qua mã nguồn bản mẫu. Chưa đo tương phản bằng công cụ (ghi rõ ở R-14).
Thang: cao / vừa / thấp. "Đã sửa" = đã sửa trong `Design/prototype/` và chụp lại ảnh.

| # | Màn / ảnh | Vấn đề | Mức | Đề xuất / trạng thái |
|---|---|---|---|---|
| R-01 | C3-04 `c3-04-ket-qua-lop` | Ô "Đang làm" tô cam-đỏ, cùng họ màu với lỗi/cảnh báo; giáo viên dễ đọc là "em làm sai". Lệch nguyên tắc "không xếp hạng, không phán xét" | cao | Đổi sang xanh chàm nhạt (cùng họ "đang diễn ra"), giữ biểu tượng bút. **Đã sửa** (`s23.css` `.cell.r`) |
| R-02 | Mọi màn học sinh (iPad, iPhone) | Chữ phụ 11,5 – 12,5 px, đề trên iPhone 12,8 px; trái Q_Dom A5 (thân ≥ 16 px) | cao | iPad/iPhone: `.small` 14, `.tiny` 13, chip 13, đề 16 (iPad) / 14–15 (iPhone). **Đã sửa** (`s23.css`) |
| R-03 | C2-06 bàn phím iPhone | Phím cao 40 px < vùng chạm 44 px (A5) | cao | Nâng `min-height` 44 px. **Đã sửa** |
| R-04 | C2-06 `c2-06-het-luot` | Hết lượt nhưng không có lối đi tiếp: thiếu "Xem lại cùng thầy cô" và "Em nghĩ đáp số này đúng" (S2-E8-03, S2-E8-05) | cao | Thêm hai nút vào trạng thái hết lượt. **Đã sửa** |
| R-05 | C3-01 `c3-01-ma-nguoi-dung` | Chỉ có ảnh ô "đã lưu" đã tick, nút đã sáng; DQ-16 nói "bắt tick mới đi tiếp" nhưng chưa có ảnh trạng thái khóa | cao | Thêm ảnh `c3-01-ma-chua-tick` (nút xám, ghi lý do khóa). **Đã sửa** |
| R-06 | C2-02/C2-03 `…lech-dap-so`, `…lech-chan-duyet` | Hàng "Máy tính lại 45°" tô đỏ trong khi máy đúng, đề ghi sai: màu đỏ gán nhầm bên; dễ lái giáo viên tin cột "Đề ghi" | vừa | Tô cảnh báo ở ô lệch (cả hai ô), không tô riêng hàng máy; hoặc dùng xanh chàm cho máy. Chờ owner (DQ-04) |
| R-07 | C2-03 `c2-03-da-giao` | Hành động "Áp dụng cho lớp" chỉ nằm trong chữ banner, không có nút; hai cột trái và giữa trống nửa màn; không có so sánh v2 / v3 trước khi áp dụng | vừa | Nút "Áp dụng cho lớp" trong hộp duyệt + màn so sánh v2↔v3 (DQ-30 mới) |
| R-08 | C2-06 iPhone | Thanh điều hướng dưới vẫn hiện khi đang làm bài: chạm nhầm thoát bài, mất không gian | vừa | Chế độ tập trung khi làm bài (ẩn thanh dưới, có nút Thoát có xác nhận) |
| R-09 | Mọi màn có nhãn "Concept · chưa có trong MVP" | Trên iPhone nhãn thu thành biểu tượng ngôi sao vô nghĩa; trên web chiếm chỗ ở góc phải | vừa | Giữ cho bản duyệt, ẩn khi chụp bản giao Dev; quy ước ghi ở `DQ.md` |
| R-10 | C2-07 `c2-07-ket-qua` | Mỗi dòng 2–3 chip + nút; chip "Lời giải khóa" lặp 5 lần: nhiễu, đặc biệt ở iPad | vừa | Gộp thành một dòng "Lời giải mở 18:00 thứ Sáu" ở đầu trang |
| R-11 | C3-04, C2-07, nhiều ban | Nhãn nội bộ (TẠM, Q6, DQ) hiện ngay trong giao diện người dùng cuối | vừa | Tách thành chú thích handoff ngoài khung màn; bản app không có (DQ-37 mới). Các màn mới của Dom vẫn để ở khung "flow" như màn cũ để owner thấy giả định |
| R-12 | C3-04 `c3-04-ket-qua-lop` | Bảng 8 em ổn; lớp 40 em phải cuộn, tiêu đề cột không cố định; chưa có lọc "chỉ em cần xem lại" | vừa | Cố định hàng tiêu đề + cột tên; chip lọc "Cần xem lại / Chưa làm" (không sắp hạng) |
| R-13 | C3-01 trang thành công | Biểu tượng "Sao chép" dùng hình tệp; nửa trái trang trống trên iPad | thấp | Dùng biểu tượng sao chép; thu panel trái ở iPad |
| R-14 | Chip nhỏ vàng / xám trên nền nhạt | Chữ 11,5 px màu `#9A6B00`, `#5F6B85` trên nền nhạt: chưa đo, nghi sát ngưỡng AA 4,5:1 ở giáo viên web | thấp | Đo bằng công cụ; nếu < 4,5:1 thì đậm màu chữ thêm một bậc. Status: Not run |
| R-15 | Trình chiếu `c2-04-*` | Màn tối không có nhãn hai trục (AI soạn / Đã kiểm chứng), lệch DQ-07 "luôn hiện cùng nhau" | thấp | Thêm chip nhỏ góc trên: "Đã kiểm chứng · Giáo viên đã duyệt" |
| R-16 | Toàn bộ | Chưa có dark mode; chữ tiếng Việt, dấu phẩy thập phân (0,5°), ký hiệu góc đúng; nhãn không chỉ dựa vào màu (có biểu tượng + chữ) | thấp | Giữ nguyên (Pass). Dark mode: không làm ở chặng 2–3 (xem Q_Dom A2) |

## Tóm tắt
- Tổng 16 nhận xét: **cao 5** (đều đã sửa), **vừa 7**, **thấp 4**.
- Đã sửa trong prototype: R-01, R-02, R-03, R-04, R-05. Ảnh chụp lại toàn bộ 24 ảnh gốc và thêm ảnh mới.
- Nhận xét chờ owner / vòng sau: R-06 đến R-16.
- Nguyên tắc sản phẩm (gợi ý trước, lời giải sau; nhãn hai trục; không xếp hạng; PO-61 không thu dữ liệu cá nhân): bản mẫu gốc tuân thủ tốt; chỗ lệch nhẹ chỉ là R-01 (đã sửa) và R-06.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 16 nhận xét, 5 mục cao đã sửa | runner-design-dom |
