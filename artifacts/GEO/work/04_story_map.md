# Story map — GEO

> R2 status: **locked (goal run, ruled by PO 22-11-2026; Not reviewed (TBD) bởi chủ đầu tư)**. Sau khi khóa, thay đổi là CR (`cr_register.md`). Quy tắc cắt: một khung = một story (phần khung của nhiều story được ghi rõ); story dữ liệu tách riêng (loại `Data`); một story xuyên suốt (US-GEO-104, loại `Xuyên suốt`); story kỹ thuật (loại `Tech`) phục vụ yêu cầu phi chức năng. Tên khung: `mvp-01` Kho đề, `mvp-02` Xem đề và giao bài, `mvp-03` Theo dõi lớp, `mvp-04` Bài của em, `mvp-05` Làm bài, `mvp-06` Lời giải đã mở, `mvp-07` Tự học, `mvp-08` Điện thoại · làm bài (`Design/mvp-*.png`).

| Epic | Story | Loại | Frames | Metrics (F1) | Decisions (F2) | Statement(s) covered (REQ) |
|---|---|---|---|---|---|---|
| E1 | US-GEO-101 Học sinh vào lớp bằng tên hiển thị và mã lớp | UI | IAM-01 (không có khung) | — | D-GEO-048, 051, 067 | REQ-GEO-23, 44, 28 |
| E1 | US-GEO-102 Giáo viên mẫu vào hệ thống | UI | (đầu vào của mvp-01…03) | — | D-GEO-073, 034 | REQ-GEO-24, 28 |
| E1 | US-GEO-103 Máy chủ gọi AI qua một cổng | Tech | (không có khung) | M-GEO-17 | D-GEO-047, 072, 067 | REQ-GEO-27, 47, 44 |
| E1 | US-GEO-104 Cách ly dữ liệu theo trường và phân quyền ở máy chủ (xuyên suốt) | Xuyên suốt | (không có khung) | — | D-GEO-012, 050, 054, 076 | REQ-GEO-28, 37, 30, 34 |
| E1 | US-GEO-105 Chạy bộ đề chuẩn và đo tỉ lệ đúng | Tech | (báo cáo, không có khung) | M-GEO-01, 04, 05, 07 | D-GEO-040, 047 | REQ-GEO-25, 47, 49 |
| E1 | US-GEO-106 Dữ liệu: đề chuẩn và đáp số tham chiếu | Data | (dữ liệu) | M-GEO-01, 04 | D-GEO-004, 055 | REQ-GEO-25, 20 |
| E1 | US-GEO-107 Thử tải 1000 người dùng đồng thời | Tech | (không có khung) | M-GEO-10, 11, 12, 20 | D-GEO-013, 044, 045, 049 | REQ-GEO-38, 39, 40 |
| E1 | US-GEO-108 Dữ liệu: bộ dữ liệu thử tải | Data | (dữ liệu) | M-GEO-19 | D-GEO-049 | REQ-GEO-50, 38 |
| E2 | US-GEO-201 Engine dựng hình từ ràng buộc | Logic | mvp-05 (hình), mvp-07 | M-GEO-02 | D-GEO-040, 061 | REQ-GEO-02, 47 |
| E2 | US-GEO-202 Dựng hình mẫu có tham số không cần AI | UI | mvp-04 (Tự khám phá), mvp-07 | — | D-GEO-056 | REQ-GEO-03 |
| E2 | US-GEO-203 Kiểm chứng hình và đáp số, gắn nhãn máy | Logic | mvp-01, 02, 05 (nhãn) | M-GEO-02, 03, 05 | D-GEO-004, 040, 061, 079 | REQ-GEO-04, 34, 47 |
| E2 | US-GEO-204 Đề lạ dùng tọa độ AI kèm cảnh báo chưa kiểm chứng | UI | mvp-07 | M-GEO-18 | D-GEO-061 | REQ-GEO-01, 04 |
| E2 | US-GEO-205 Dữ liệu: danh mục hình mẫu và tham số | Data | (dữ liệu) | — | D-GEO-056 | REQ-GEO-03 |
| E3 | US-GEO-301 Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz | UI | mvp-05 | — | D-GEO-008 | REQ-GEO-05, 07 |
| E3 | US-GEO-302 Chạm chọn đối tượng; tô màu cạnh và mặt | UI | mvp-05, mvp-08 | — | D-GEO-001 | REQ-GEO-05, 07 |
| E3 | US-GEO-303 Vẽ thêm đoạn, mặt phẳng; giao điểm, giao tuyến; mở rộng mặt, kéo dài cạnh | Logic | mvp-05 | — | D-GEO-008 | REQ-GEO-05 |
| E3 | US-GEO-304 Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh | Logic | mvp-05 (công cụ Điểm) | — | D-GEO-008 | REQ-GEO-06 |
| E3 | US-GEO-305 Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi | Logic | mvp-05, mvp-06 | — | D-GEO-024 | REQ-GEO-06, 31 |
| E3 | US-GEO-306 Thao tác trên iPhone: chụm ngón, nút đặt lại, bố cục điện thoại | UI | mvp-08 | — | D-GEO-001, 033 | REQ-GEO-07, 42 |
| E3 | US-GEO-307 Sửa lỗi hình đã biết | Logic | mvp-05 | — | D-GEO-001 | REQ-GEO-36 |
| E4 | US-GEO-401 Nhập đề tự học bằng chữ | Logic | mvp-07 | M-GEO-09, 17, 18 | D-GEO-052, 072 | REQ-GEO-01, 41, 48 |
| E4 | US-GEO-402 Nhập đề bằng ảnh | Logic | mvp-07 | — | D-GEO-052 | REQ-GEO-01, 41, 48 |
| E4 | US-GEO-403 Chọn hình mẫu để khám phá | UI | mvp-04, mvp-07 | — | D-GEO-052 | REQ-GEO-01, 03 |
| E4 | US-GEO-404 Xin Gợi ý 1 và Gợi ý 2 | UI | mvp-05 | M-GEO-14, 16 | D-GEO-003, 053, 075 | REQ-GEO-08 |
| E4 | US-GEO-405 Mở lời giải của bài tự học sau Gợi ý 1 và 2 | Logic | mvp-07 | M-GEO-16 | D-GEO-023 | REQ-GEO-19 |
| E4 | US-GEO-406 Tự học kế thừa khóa của bài đang khóa | Logic | mvp-07 | — | D-GEO-026 | REQ-GEO-32 |
| E4 | US-GEO-407 Xem lời giải đồng bộ hình: bấm bước thì hình tô màu | UI | mvp-06 | — | D-GEO-009 | REQ-GEO-09 |
| E4 | US-GEO-408 Đường vẽ thêm của từng bước hiện dần; "Vì sao nghĩ ra bước này?" | UI | mvp-06 | — | D-GEO-009 | REQ-GEO-09 |
| E4 | US-GEO-409 Hỏi đáp trong ngữ cảnh bài, có nguồn | Logic | mvp-05 (tab Hỏi đáp) | — | D-GEO-006, 030, 072 | REQ-GEO-10, 48 |
| E4 | US-GEO-410 Nhật ký hỏi đáp lọc theo bài | UI | (thanh bên Lịch sử) | — | D-GEO-078 | REQ-GEO-10, 36 |
| E4 | US-GEO-411 Từ khóa bấm được mở bài giảng có nguồn và nhãn | UI | (prototype) | — | D-GEO-007, 029, 030 | REQ-GEO-11 |
| E4 | US-GEO-412 Dữ liệu: kho bài giảng mẫu | Data | (dữ liệu) | — | D-GEO-029, 030, 076 | REQ-GEO-11, 10 |
| E5 | US-GEO-501 Xem Kho đề và lọc theo cấp, loại hình, dạng bài | UI | mvp-01 | — | D-GEO-070 | REQ-GEO-20, 39 |
| E5 | US-GEO-502 Thẻ đề hiện nhãn máy và "AI soạn" | UI | mvp-01 | — | D-GEO-028, 079 | REQ-GEO-20, 29, 04 |
| E5 | US-GEO-503 Xem một đề: hình, đáp số, lời giải từng bước, gợi ý | UI | mvp-02 (bên trái) | — | D-GEO-078 | REQ-GEO-21 |
| E5 | US-GEO-504 Dữ liệu: nạp 40 đến 60 đề của Giáo viên mẫu và tính lại nhãn | Data | (dữ liệu) | M-GEO-07 | D-GEO-004, 055, 062, 076 | REQ-GEO-20, 49, 30 |
| E5 | US-GEO-505 Kho đề tải theo trang theo khóa | Tech | mvp-01 | — | D-GEO-014 | REQ-GEO-39 |
| E6 | US-GEO-601 Dữ liệu: tài khoản Giáo viên mẫu, lớp mẫu, học sinh, bài giao mẫu | Data | (dữ liệu) | — | D-GEO-025, 054 | REQ-GEO-24 |
| E6 | US-GEO-602 Giao một đề cho lớp: chọn lớp | UI | mvp-02 (bước 1) | — | D-GEO-035, 078 | REQ-GEO-13 |
| E6 | US-GEO-603 Chọn trợ giúp học sinh được dùng | UI | mvp-02 (bước 2) | — | D-GEO-053 | REQ-GEO-13 |
| E6 | US-GEO-604 Đặt giờ mở lời giải khi giao bài | Logic | mvp-02 (bước 3) | M-GEO-13 | D-GEO-041, 043, 078 | REQ-GEO-13, 16, 35 |
| E6 | US-GEO-605 Đổi giờ hoặc mở ngay lời giải | Logic | mvp-03 (thẻ vàng) | M-GEO-13 | D-GEO-041, 042 | REQ-GEO-16 |
| E6 | US-GEO-606 Bài của em: giờ mở và tiến độ | UI | mvp-04 | M-GEO-13, 14 | D-GEO-075, 078 | REQ-GEO-12 |
| E6 | US-GEO-607 Xem trước như học sinh | UI | mvp-02 (nút Xem như học sinh) | — | D-GEO-027 | REQ-GEO-15, 17 |
| E6 | US-GEO-608 Máy chủ khóa lời giải và đáp số trước giờ mở | Logic | mvp-05 (thẻ khóa) | M-GEO-13 | D-GEO-003, 041, 043 | REQ-GEO-17, 35, 40 |
| E6 | US-GEO-609 Hỏi đáp không lộ đáp số khi lời giải khóa | Logic | mvp-05 | M-GEO-06 | D-GEO-027, 065 | REQ-GEO-18, 40 |
| E6 | US-GEO-610 Bộ câu thử moi đáp án | Tech | (báo cáo) | M-GEO-06 | D-GEO-026, 065 | REQ-GEO-26, 32 |
| E6 | US-GEO-611 Dữ liệu: bộ câu thử moi đáp án | Data | (dữ liệu) | M-GEO-06 | D-GEO-065 | REQ-GEO-26 |
| E6 | US-GEO-612 Lời giải tự hiện khi tới giờ mở trong lúc học sinh đang xem | Logic | mvp-05 | M-GEO-13 | D-GEO-041, 074 | REQ-GEO-16, 17 |
| E7 | US-GEO-701 Theo dõi lớp: ai đã mở bài, tình trạng | UI | mvp-03 | M-GEO-15, 21, 22, 23, 24 | D-GEO-022, 077, 078 | REQ-GEO-14, 33 |
| E7 | US-GEO-702 Theo dõi lớp: mức gợi ý và câu hỏi của từng học sinh | UI | mvp-03 | M-GEO-15, 22 | D-GEO-022, 077 | REQ-GEO-14 |
| E7 | US-GEO-703 Ghi sự kiện tương tác | Logic | (không có khung) | M-GEO-08 | D-GEO-058, 059 | REQ-GEO-22, 49 |
| E7 | US-GEO-704 Dữ liệu: bảng sự kiện chia theo thời gian và phân trang theo khóa | Data | (dữ liệu) | M-GEO-19 | D-GEO-014 | REQ-GEO-22, 39, 38 |

## Coverage check: every frame → a story; every customer statement → a story or OUT

### Khung thiết kế → story
| Khung | Story |
|---|---|
| mvp-01 Kho đề | US-GEO-501, 502, 505 |
| mvp-02 Xem đề và giao bài | US-GEO-503 (bên trái), 602, 603, 604 (bên phải); nút Trình chiếu, Sửa (sắp có), "Sau buổi học tới": OUT (D-GEO-078); nút Xem như học sinh: US-GEO-607 |
| mvp-03 Theo dõi lớp | US-GEO-701, 702; thẻ vàng Mở ngay, Đổi giờ: US-GEO-605; thẻ "Nên nhắc lại trên lớp": OUT |
| mvp-04 Bài của em | US-GEO-606; khung Tự khám phá: US-GEO-403; thẻ chuỗi ngày và "Em đang học tới đâu": OUT |
| mvp-05 Làm bài | US-GEO-301…305, 404, 409, 608, 612; chip ràng buộc (SA = a): ghi ở US-GEO-201 và BA |
| mvp-06 Lời giải đã mở | US-GEO-407, 408; hộp bài giảng: US-GEO-411 |
| mvp-07 Tự học | US-GEO-401, 402, 403, 405, 406, 204 |
| mvp-08 Điện thoại · làm bài | US-GEO-306 |

### Yêu cầu thô → story hoặc OUT
| REQ | Story / xử lý |
|---|---|
| REQ-GEO-01 | US-GEO-204, US-GEO-401, US-GEO-402, US-GEO-403 |
| REQ-GEO-02 | US-GEO-201 |
| REQ-GEO-03 | US-GEO-202, US-GEO-205, US-GEO-403 |
| REQ-GEO-04 | US-GEO-203, US-GEO-204, US-GEO-502 |
| REQ-GEO-05 | US-GEO-301, US-GEO-302, US-GEO-303 |
| REQ-GEO-06 | US-GEO-304, US-GEO-305 |
| REQ-GEO-07 | US-GEO-301, US-GEO-302, US-GEO-306 |
| REQ-GEO-08 | US-GEO-404 |
| REQ-GEO-09 | US-GEO-407, US-GEO-408 |
| REQ-GEO-10 | US-GEO-409, US-GEO-410, US-GEO-412 |
| REQ-GEO-11 | US-GEO-411, US-GEO-412 |
| REQ-GEO-12 | US-GEO-606 |
| REQ-GEO-13 | US-GEO-602, US-GEO-603, US-GEO-604 |
| REQ-GEO-14 | US-GEO-701, US-GEO-702 |
| REQ-GEO-15 | US-GEO-607 |
| REQ-GEO-16 | US-GEO-604, US-GEO-605, US-GEO-612 |
| REQ-GEO-17 | US-GEO-607, US-GEO-608, US-GEO-612 |
| REQ-GEO-18 | US-GEO-609 |
| REQ-GEO-19 | US-GEO-405 |
| REQ-GEO-20 | US-GEO-106, US-GEO-501, US-GEO-502, US-GEO-504 |
| REQ-GEO-21 | US-GEO-503 |
| REQ-GEO-22 | US-GEO-703, US-GEO-704 |
| REQ-GEO-23 | US-GEO-101 |
| REQ-GEO-24 | US-GEO-102, US-GEO-601 |
| REQ-GEO-25 | US-GEO-105, US-GEO-106 |
| REQ-GEO-26 | US-GEO-610, US-GEO-611 |
| REQ-GEO-27 | US-GEO-103 |
| REQ-GEO-28 | US-GEO-101, US-GEO-102, US-GEO-104 |
| REQ-GEO-29 | US-GEO-502 |
| REQ-GEO-30 | US-GEO-104, US-GEO-504 |
| REQ-GEO-31 | US-GEO-305 |
| REQ-GEO-32 | US-GEO-406, US-GEO-610 |
| REQ-GEO-33 | US-GEO-701 |
| REQ-GEO-34 | US-GEO-104, US-GEO-203 |
| REQ-GEO-35 | US-GEO-604, US-GEO-608 |
| REQ-GEO-36 | US-GEO-307, US-GEO-410 |
| REQ-GEO-37 | US-GEO-104 |
| REQ-GEO-38 | US-GEO-107, US-GEO-108, US-GEO-704 |
| REQ-GEO-39 | US-GEO-107, US-GEO-501, US-GEO-505, US-GEO-704 |
| REQ-GEO-40 | US-GEO-107, US-GEO-608, US-GEO-609 |
| REQ-GEO-41 | US-GEO-401, US-GEO-402 |
| REQ-GEO-42 | US-GEO-306 |
| REQ-GEO-43 | điều kiện chung của mọi story có giao diện (tiếng Việt có dấu, dấu phẩy thập phân, lỗi dễ hiểu); BA đưa vào từng story và từ điển thông báo |
| REQ-GEO-44 | US-GEO-101, US-GEO-103 |
| REQ-GEO-45 | khung mã Java (agent riêng, `app/`); ARCH ghi NFR |
| REQ-GEO-46 | khung mã (module); ARCH ghi NFR |
| REQ-GEO-47 | US-GEO-103, US-GEO-105, US-GEO-201, US-GEO-203 |
| REQ-GEO-48 | US-GEO-401, US-GEO-402, US-GEO-409 |
| REQ-GEO-49 | US-GEO-105, US-GEO-504, US-GEO-703 |
| REQ-GEO-50 | US-GEO-108 |
| REQ-GEO-51 | OUT (hình học phẳng) |
| REQ-GEO-52 | OUT (soạn, import) |
| REQ-GEO-53 | OUT (giáo trình riêng) |
| REQ-GEO-54 | OUT (chặng 2 đến 9) |
| REQ-GEO-55 | quy tắc tài liệu và nhãn Concept (mọi tài liệu) |

Kết quả: 55 / 55 REQ có story hoặc xử lý rõ (47 có story, 8 xử lý khác: 43, 45, 46, 51, 52, 53, 54, 55). 8 / 8 khung có story. V14 chạy ở mục sau.

## V14 cross-epic findings (XF-nn)
Chạy trên bản đồ sau R2 (PO). Tám loại xung đột của method §10.5 đã dò trên danh sách story và `02_domain_model.md`.

| XF | Loại | Hai phía | Xử lý | Trạng thái |
|---|---|---|---|---|
| XF-01 | 1 thực thể định nghĩa khác | `Problem.school_id` (đề tự nhập) và kho đề nội dung hệ thống (E5) | D-GEO-076: nội dung hệ thống dùng chung, đề tự nhập có trường | Fixed ở `02_domain_model.md` |
| XF-02 | 2 trạng thái cùng tên | "Đã mở": trạng thái giờ mở (M-GEO-13) và "Đã mở bài" của học sinh (M-GEO-15) | glossary: "Đã mở" chỉ trạng thái giờ mở; học sinh dùng "Đã mở bài" | Fixed ở `glossary.md` |
| XF-03 | 4 thông báo | nhãn "Hình đã kiểm chứng" (mvp-05) và "Đã kiểm chứng" (mvp-02) | D-GEO-079 một chuỗi | Fixed |
| XF-04 | 5 quy tắc mâu thuẫn | E4 (US-GEO-405 mở lời giải tự học sau gợi ý) và E6 (US-GEO-608 khóa theo giờ); đề kế thừa khóa (US-GEO-406) | D-GEO-026: kế thừa khóa thắng quy tắc tự học | Fixed; BA phải viết ca biên |
| XF-05 | 5 quy tắc mâu thuẫn | E3 US-GEO-305 (công cụ đo ẩn đại lượng) và E4 US-GEO-407 (lời giải hiện đo ngay trên hình) | sau giờ mở công cụ đo hiện đủ; trước giờ mở ẩn (BR-GEO-16) | Fixed; BA viết AC cả hai phía |
| XF-06 | 6 vòng phụ thuộc | không phát hiện vòng; thứ tự E1 → E2 → E3 → E4 → E5 → E6 → E7 | — | OK |
| XF-07 | 7 quyết định áp dụng khác nhau | D-GEO-053 (trợ giúp) áp dụng ở E4 (gợi ý, hỏi đáp) và E6 (giao bài) | E6 ghi cấu hình, E4 đọc cấu hình | Fixed; BA nối hai story |
| XF-08 | 8 cùng một chỉ số khác giá trị | tiến độ k/3 (mvp-04, mvp-05) dùng ở E4 và E6 | M-GEO-14 một công thức | Fixed |
| XF-09 | 3 cùng hành động khác vai | "Mở ngay" ở E6 (US-GEO-605) và thẻ vàng ở E7 (US-GEO-701) | hành động thuộc E6, E7 chỉ hiện nút | Fixed |