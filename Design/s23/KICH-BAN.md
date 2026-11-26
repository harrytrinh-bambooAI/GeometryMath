# KỊCH BẢN S23 · cách xem bản mẫu chặng 2–3
Phiên bản: v0.1 · 2026-11-26 · T-261126-38 (Designer Dom)

Nguồn: Design/prototype/s23*.{html,js,css} (màn đăng ký trong V), PO-DECISIONS.md PO-61…66 (26/11/2026), DQ.md v0.2.

## Cách mở
- Mở file Design/prototype/s23-screens-c.html (hoặc s23-screens.html, hai trang cùng danh sách màn) bằng trình duyệt (file://). Không gõ hash thì vào trang "Cách hệ thống vận hành".
- Thanh trên cùng một dòng 40px: tiêu đề, màn hiện tại, nút "Danh sách màn ▾" (menu theo nhóm + danh sách kịch bản), nút "▶ Kịch bản", nút ▴ thu/mở thanh. Trạng thái nhớ bằng localStorage.
- Chọn kịch bản: Trước / Tiếp, phím ← →, thanh tiến độ "Bước n/N"; hộp chú thích 3 ý (đang làm gì, vì sao, hệ thống làm gì phía sau); vùng liên quan được tô viền cam.
- Nút "⛶ Trình chiếu": toàn màn hình, ẩn thanh nav, tự chạy mỗi bước 6 giây, Space dừng/chạy, ← → chuyển, Esc thoát. Mở thẳng bằng `&fs=1`.
- Link gửi được: `s23-screens-c.html#kb=<mã>&b=<n>`, ví dụ `#kb=kb4&b=5`; trang mở đầu `#kb=intro`.
- Các nút "Trình chiếu" / "Chữa bài này" trong màn bấm được và mở c2-04-trinh-chieu; "Che lời giải" chuyển qua lại hai màn chiếu.

## Kịch bản và bước ↔ màn

### KB1 · Giáo viên mở lớp (`#kb=kb1&b=1`, 8 bước)
Đăng ký bằng tên, nhận mã người dùng, tạo lớp và mã lớp, duyệt từng em vào lớp.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | GV | Đăng ký chỉ bằng tên | `c3-01-dang-ky` |
| 2 | GV | Nhận mã người dùng và mã khôi phục | `c3-01-ma-nguoi-dung` |
| 3 | GV | Chưa lưu mã thì chưa được đi tiếp | `c3-01-ma-chua-tick` |
| 4 | GV | Tạo lớp | **chưa vẽ** · Form tạo lớp (tên lớp, năm học) chưa vẽ riêng; bước sau là kết quả sau khi tạo. |
| 5 | GV | Mã lớp và bật duyệt từng em | `c3-07-ma-lop` |
| 6 | HS | Học sinh nhập mã lớp | `c3-07-vao-lop` |
| 7 | GV | Giáo viên duyệt từng em | `c3-07-duyet-dat-lai-mk` |
| 8 | HS | Nếu cô từ chối | `c3-07-bi-tu-choi` |

### KB2 · Giáo viên thêm đề (`#kb=kb2&b=1`, 10 bước)
Dán chữ hoặc ảnh, AI dựng hình và lời giải, kiểm và sửa, lệch đáp số bị chặn duyệt, duyệt vào Kho.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | GV | Dán chữ hoặc ảnh đề | `c2-02-nhap` |
| 2 | AI | AI dựng hình và lời giải | `c2-02-dang-dung` |
| 3 | GV | Ngoại lệ: ảnh mờ | `c2-02-anh-mo` |
| 4 | GV | Ngoại lệ: máy không dựng được | `c2-02-engine-khong-dung` |
| 5 | GV | Ngoại lệ: lệch đáp số | `c2-02-lech-dap-so` |
| 6 | GV | Soát: chưa kiểm chứng | `c2-03-chua-kiem` |
| 7 | GV | Lệch đáp số thì chặn duyệt | `c2-03-lech-chan-duyet` |
| 8 | AI | Chat để chỉnh nháp | `c2-04a-chat-chinh-nhap` |
| 9 | GV | Duyệt và vào Kho | **chưa vẽ** · Màn xác nhận “đã vào Kho” chưa vẽ riêng. |
| 10 | GV | Sửa sau khi đã giao | `c2-03-da-giao` |

### KB3 · Giáo viên soạn bộ bài (`#kb=kb3&b=1`, 6 bước)
Soạn bộ bài, đặt lịch mở lời giải, giao lớp, học sinh nhận thông báo.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | GV | Soạn bộ bài | `c3-03-bo-bai` |
| 2 | GV | Cài đặt luyện và giờ mở lời giải | `c2-08-soan-bai-luyen` |
| 3 | GV | Câu cần biểu thức | `c2-08-ban-phim-nang-cao` |
| 4 | GV | Câu trắc nghiệm | `c2-09c-trac-nghiem-gv` |
| 5 | HS | Học sinh nhận thông báo (iPad) | `c3-04-thong-bao-ipad` ·  Danh sách “Bài của em” chưa vẽ riêng; thông báo mở thẳng bộ bài. |
| 6 | HS | Cùng thông báo trên iPhone | `c3-04-thong-bao-iphone` |

### KB4 · Học sinh làm bài (`#kb=kb4&b=1`, 9 bước)
Vào lớp bằng mã, Bài của em, làm bài trên iPad hoặc iPhone với bàn phím toán, sai nhận gợi ý, hết lượt, xem kết quả, mất mạng.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | HS | Vào lớp bằng mã | `c3-07-vao-lop` |
| 2 | HS | Bài của em | `c3-04-thong-bao-ipad` · Danh sách “Bài của em” chưa vẽ riêng; đang dùng màn thông báo thay. |
| 3 | HS | Đang tải câu hỏi | `c2-06-dang-tai` |
| 4 | HS | Làm bài trên iPad | `c2-06-luyen-ipad` |
| 5 | HS | Làm bài trên iPhone | `c2-06-luyen-iphone` |
| 6 | HS | Sai thì nhận gợi ý | `c2-06-sai-goi-y` |
| 7 | HS | Hết lượt | `c2-06-het-luot` |
| 8 | HS | Xem kết quả | `c2-07-ket-qua` |
| 9 | HS | Trường hợp mất mạng | `c2-06-mat-mang` |

### KB5 · Giáo viên chữa bài (`#kb=kb5&b=1`, 8 bước)
Bảng lớp × bài, “Cả lớp vướng ở đâu”, chữa bài sang Trình chiếu (lời giải vẫn khóa trên máy học sinh), xuất kết quả, lưu trữ lớp.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | GV | Bảng lớp × bài | `c3-04-ket-qua-lop` |
| 2 | AI | Cả lớp vướng ở đâu | `c3-04-ket-qua-lop` |
| 3 | GV | Xem bài làm căn cứ | `c3-04-ket-qua-lop` |
| 4 | GV | Chữa bài sang Trình chiếu | `c3-06a-chua-bai-trinh-chieu` |
| 5 | GV | Trình chiếu hiện từng bước | `c2-04-trinh-chieu` |
| 6 | GV | Che lời giải | `c2-04-che-loi-giai` |
| 7 | GV | Xuất kết quả | `c3-06b-xuat-ket-qua` |
| 8 | GV | Lưu trữ lớp cuối năm | `c3-06c-luu-tru-lop` |

### KB6 · Học sinh tự học (không lớp) (`#kb=kb6&b=1`, 6 bước)
Luyện một mình, chỉ có gợi ý, mã khôi phục khi quên mật khẩu.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | HS | Tự đăng ký bằng tên | `c3-01-dang-ky` |
| 2 | HS | Mã người dùng và mã khôi phục | `c3-01-ma-nguoi-dung` · Màn này hiện vẽ cho giáo viên; bản dành cho học sinh tự học chưa vẽ lại. |
| 3 | HS | Luyện một mình | `c2-06-luyen-ipad` |
| 4 | HS | Chỉ có gợi ý | `c2-06-sai-goi-y` |
| 5 | HS | Quên mật khẩu | `c3-01-dang-nhap` |
| 6 | HS | Đặt mật khẩu mới | **chưa vẽ** · Màn nhập mã khôi phục / đặt mật khẩu mới chưa vẽ. |

### KB7 · Trình chiếu trên lớp (`#kb=kb7&b=1`, 5 bước)
Giáo viên mở Trình chiếu, che lời giải, hiện từng bước, học sinh hỏi khi lời giải còn khóa, mất mạng khi đang chiếu.

| Bước | Vai | Nội dung | Màn (V) |
|---|---|---|---|
| 1 | GV | Mở Trình chiếu | `c3-06a-chua-bai-trinh-chieu` · Nút “Trình chiếu” mở từ màn Soát đề và Soạn bộ bài chưa vẽ; tạm mở từ kết quả lớp. |
| 2 | GV | Che lời giải | `c2-04-che-loi-giai` |
| 3 | GV | Hiện từng bước | `c2-04-trinh-chieu` |
| 4 | HS | Học sinh hỏi lại khi lời giải còn khóa | `c2-06-het-luot` |
| 5 | GV | Mất mạng khi đang chiếu | `c2-04-trinh-chieu` · Màn thông báo mất mạng riêng cho Trình chiếu chưa vẽ; dùng chip “Đã tải sẵn” trên màn chiếu. |

## Bước "chưa vẽ" (không vẽ màn mới, theo phạm vi)
- KB1 b4: form tạo lớp.
- KB2 b9: màn xác nhận "đã vào Kho".
- KB3 b5 và KB4 b2: danh sách "Bài của em" (đang dùng màn thông báo thay).
- KB6 b2: màn mã khôi phục cho HS tự học (PO-66 G3; màn hiện có vẽ cho GV và còn ghi "học sinh không có mã khôi phục", lệch PO-66 G3, cần Dom vẽ lại); KB6 b6: nhập mã khôi phục / đặt mật khẩu mới.
- KB7 b1: nút Trình chiếu từ màn Soát đề / Soạn bộ bài; b5: màn mất mạng riêng cho Trình chiếu (dùng chip "Đã tải sẵn").

## Kết quả kiểm (Playwright, Design/s23/kb-shoot.js, cả hai trang)
```
[c] mở đầu OK · cao thanh nav 40px
[c] kb1: 8 bước đi hết · tô sáng 6/8 · chưa vẽ 1
[c] kb2: 10 bước đi hết · tô sáng 7/10 · chưa vẽ 1
[c] kb3: 6 bước đi hết · tô sáng 2/6 · chưa vẽ 0
[c] kb4: 9 bước đi hết · tô sáng 7/9 · chưa vẽ 0
[c] kb5: 8 bước đi hết · tô sáng 8/8 · chưa vẽ 0
[c] kb6: 6 bước đi hết · tô sáng 6/6 · chưa vẽ 1
[c] kb7: 5 bước đi hết · tô sáng 5/5 · chưa vẽ 0
[c] nút Trình chiếu/Chữa bài gắn liên kết: 2 · sau bấm hash=#c2-04-trinh-chieu
[c] trình chiếu: vào, ←→, Space, Esc thoát OK
[a] mở đầu OK · cao thanh nav 40px
[a] kb1: 8 bước đi hết · tô sáng 6/8 · chưa vẽ 1
[a] kb2: 10 bước đi hết · tô sáng 7/10 · chưa vẽ 1
[a] kb3: 6 bước đi hết · tô sáng 2/6 · chưa vẽ 0
[a] kb4: 9 bước đi hết · tô sáng 7/9 · chưa vẽ 0
[a] kb5: 8 bước đi hết · tô sáng 8/8 · chưa vẽ 0
[a] kb6: 6 bước đi hết · tô sáng 6/6 · chưa vẽ 1
[a] kb7: 5 bước đi hết · tô sáng 5/5 · chưa vẽ 0
[a] nút Trình chiếu/Chữa bài gắn liên kết: 2 · sau bấm hash=#c2-04-trinh-chieu
[a] trình chiếu: vào, ←→, Space, Esc thoát OK
iPhone nav cao 40px
Không có pageerror/console error ở mọi trang đã mở
```
Ảnh: Design/s23/kb/*.png (01 nav gọn, 02 menu mở, 03 mở đầu, 04-kb1…kb7 bước 1, 05 trình chiếu, 06 KB4 iPhone).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: nav gọn, 7 kịch bản, trang mở đầu, chế độ trình chiếu | runner-T-261126-38 (Designer Dom) |
