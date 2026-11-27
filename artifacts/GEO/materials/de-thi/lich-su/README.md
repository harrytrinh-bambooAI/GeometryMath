Phiên bản: v1.0 · 27/11/2026

# Hồ sơ đề lịch sử môn Toán (bước 1 của bộ lắp đề)

Mục tiêu: có dữ liệu từng câu của các đề Toán lịch sử để rút ma trận đề chuẩn (phần cố định, phần xoay vòng, xu hướng). Kiểm máy: `python3 check.py` (exit 0).

## Nội dung thư mục
- `de/<ky-thi>-<nam>.json`: 23 đề (mỗi đề một tệp, đủ trường theo yêu cầu, thêm `ma_de_dung`, `cau_truc_loai`, `do_tin_cay`, `ghi_chu`).
- `taxonomy.json`: 24 chủ đề + dạng chuẩn hóa (mạch CT2018 + chủ đề CT2006), mức độ, năng lực.
- `tan-suat.csv`, `tan-suat.md`: bảng chủ đề x năm (số câu, điểm), phân loại cố định/xoay vòng/hiếm, xu hướng, mức độ theo năm, mục hình học không gian riêng. Sinh bởi `build_data.py`, không sửa tay.
- `check.py` (kiểm), `build_data.py` + `taxonomy_def.py` + `exam_data.py` (nguồn dữ liệu gắn nhãn; sửa nhãn ở `exam_data.py` rồi chạy lại `build_data.py`).

## Phạm vi đã có
| Nhóm | Đề | Ghi chú |
|---|---|---|
| TN THPT cấu trúc cũ (50 câu x 0,2 đ) | 2017, 2018, 2019, 2020 (đợt 1), 2021 (đợt 1), 2022, 2023, 2024 | mỗi năm một mã đề (101) |
| TN THPT cấu trúc mới (12 TN4 + 4 đúng/sai + 6 trả lời ngắn) | 2025 chính thức (mã 0101); 2026 đợt 1 (mã 0101, NGOÀI phạm vi yêu cầu, thêm vì có sẵn) | |
| Minh họa | MH 2025 (công bố 29/12/2023) | |
| ĐH tự luận | khối A (đề chung A1 từ 2012) 2009-2014: 6 đề; khối B 2009-2014: 6 đề | chỉ phần riêng chương trình Chuẩn |
Tổng 23 đề, 588 câu/ý (24 chủ đề, 139 dạng).

## Cấu trúc đề: nguồn chính thức đã xác minh (ngày tra 27/11/2026)
- Quyết định 764/QĐ-BGDĐT ngày 08/03/2024 (Thứ trưởng Phạm Ngọc Thưởng ký) về cấu trúc định dạng đề thi tốt nghiệp THPT từ 2025. Toán: 90 phút, 22 câu; Phần I 12 câu TN4 x 0,25 = 3 đ; Phần II 4 câu đúng/sai (đúng 1/2/3/4 ý được 0,1/0,25/0,5/1,0 đ) = 4 đ; Phần III 6 câu trả lời ngắn x 0,5 = 3 đ. Đọc trực tiếp tại https://luatvietnam.vn/giao-duc/quyet-dinh-764-qd-bgddt-2024-cau-truc-de-thi-ky-thi-tot-nghiep-thpt-tu-2025-302643-d1.html (WebFetch đọc được; `curl` bị chặn 403). Đối chiếu với đề 2025: đúng 12/4/6.
- Cấu trúc 50 câu trắc nghiệm (2017-2024): đếm trực tiếp từ đề (50 câu); thang 0,2 đ/câu là quy ước chung, không trích được văn bản riêng. Cấu trúc ĐH tự luận 180 phút, thang 10: đọc từ đề (PDF chính thức 2014).

## Văn bản của Bộ về ma trận / bản đặc tả đề kiểm tra (tra, không suy đoán)
| Văn bản | Ngày | Trạng thái | Link |
|---|---|---|---|
| Công văn 7991/BGDĐT-GDTrH về thực hiện kiểm tra, đánh giá cấp THCS, THPT (có phụ lục ma trận đề kiểm tra định kỳ) | 17/12/2024 | nội dung chỉ thấy qua kết quả tìm kiếm; trang hoatieu trả 403, CHƯA đọc toàn văn | https://hoatieu.vn/phap-luat/cong-van-7991-bgddt-gdtrh-2024-kiem-tra-danh-gia-cap-thcs-thpt-227764 |
| Công văn 5555/BGDĐT-GDTrH về sinh hoạt chuyên môn, đổi mới PPDH và kiểm tra đánh giá (căn cứ nhiều trường dùng cho ma trận/đặc tả theo 4 mức độ) | 08/10/2014 | tồn tại (luatvietnam yêu cầu đăng nhập), CHƯA đọc toàn văn | https://luatvietnam.vn/giao-duc/cong-van-5555-bgddt-gdtrh-2014-ve-doi-moi-phuong-phap-day-hoc-va-kiem-tra-191259-d6.html |
| QĐ 764/QĐ-BGDĐT (cấu trúc định dạng đề thi TN THPT từ 2025) | 08/03/2024 | đã đọc (xem trên) | link ở mục trên |
| QĐ 4068/QĐ-BGDĐT phê duyệt phương án thi từ 2025 | 28/11/2023 | chỉ thấy nhắc làm căn cứ của QĐ 764 | chưa có link đã mở |
Lưu ý: "Công văn 7791/BGDĐT-GDTrH" như nêu trong yêu cầu KHÔNG tìm thấy; số đúng gần nhất là 7991 (17/12/2024). CHƯA tìm thấy bản ma trận/đặc tả riêng của đề thi tốt nghiệp môn Toán do Bộ công bố (Bộ chỉ công bố cấu trúc định dạng và đề minh họa). Số hiệu 5555 cũng trùng với một công văn khác năm 2026 (5555/BGDĐT-GDPT ngày 18/8/2026, về cơ sở giáo dục sau sắp xếp); chỉ dùng bản 2014.

## Nguồn nội dung đề (URL + ngày tra trong từng `de/*.json`, trường `nguon`)
- Bộ đề tổng hợp 2005-2026 (424 trang, toanmath.com, kèm đáp án): https://toanmath.com/toanmath-pdf/tong-hop-88-de-thi-thpt-quoc-gia-mon-toan-cua-bo-gddt-2005-2026.pdf. Mọi câu hỏi đọc từ text trích của PDF này (không chép lời giải). Lưu ý đây là tài liệu của bên thứ ba (trung tâm luyện thi) tổng hợp lại đề của Bộ.
- PDF riêng: 2017-2020, 2023, 2024, 2025, 2026 trên toanmath.com; đề chính thức ĐH 2014 khối A/A1 và B trên hocmai.vn (bản PDF của Bộ, đã đọc nguyên văn); đáp án chính thức 2013 khối A trên HNUE.
- Ảnh đề 2025 (phongvu.vn, mã 0102-0105) đã xem để xác nhận cấu trúc 12/4/6.
- Link UIT/Báo Đà Nẵng/SGGP của đề ĐH 2009-2012 và 2013B đã chết (404/HTML); nội dung các đề này đọc từ bộ tổng hợp trên.

## Giới hạn và nhãn không chắc (đọc trước khi dùng)
1. **Nhãn chủ đề/dạng là do agent gắn** từ text đề, không phải bảng đặc tả của Bộ. Một số câu TN bị mất text do cắt trang (ví dụ 2017 câu 41, 49) nên gắn theo phần text còn lại; với câu hình/biểu đồ chỉ gắn được theo lời dẫn. Độ tin cậy: ĐH 2014 (đọc PDF chính thức) và đề 2025 cao; ĐH 2009-2013 và TN 2017-2024 trung bình. Cần người rà soát mẫu (đề xuất 10 % câu) trước khi dùng làm chuẩn.
2. **`muc_do` và `nang_luc` là suy luận**, không phải số liệu của Bộ. TN cũ: câu 1-20 NB, 21-35 TH, 36-45 VD, 46-50 VDC (xấp xỉ theo phân bố điển hình). TN mới: Phần I câu 1-9 NB, 10-12 TH; Phần II câu 1 TH còn lại VD; Phần III 3 câu đầu VD, 3 câu cuối VDC. ĐH: theo độ khó điển hình từng dạng. Không dùng bảng mức độ để kết luận chính xác.
3. **Chỉ một mã đề mỗi đề TN** (24 mã/đề); các mã cùng bộ câu hỏi nhưng khác thứ tự/số liệu, không ảnh hưởng tần suất chủ đề.
4. **Năm 2021**: tài liệu tổng hợp ghi nhãn "Năm 2022 - Lần 1/2" cho hai đợt thi 2021 (toanmath xác nhận 2021 có hai đợt, mỗi đợt 4 mã). Đã gán `nam` = 2021, chọn "Lần 1" làm đợt 1 (giả định, chưa đối chiếu mã đề với trang Bộ).
5. **ĐH chỉ ghi phần riêng chương trình Chuẩn** (2009-2013); phần Nâng cao (3 điểm, thay thế, có elip, số phức, hệ log) bị bỏ nên "số phức" ở ĐH 2012-2013 (chuẩn không có, nâng cao có) và "elip" hiển thị thấp hơn thực tế. Đề ĐH 2014 không chia phần.
6. **Cấu trúc mới chỉ có 2-3 đề** (2025, 2026, minh họa 2025): phân loại cố định/xoay vòng cho nhóm này vô nghĩa thống kê; chỉ dùng để thấy "chủ đề nào đã xuất hiện". Đề minh họa còn lại (2023, 2024...), đề tham khảo các năm khác, đề TN 2021 đợt 2, 2020 đợt 2 chưa đưa vào.
7. **Thiếu**: đề chính thức ĐH khối A1 riêng 2009-2011 (không có, A1 chỉ từ 2012 và dùng chung đề với A), khối D, ĐH 2005-2008 (nguồn có sẵn trong bộ tổng hợp, ngoài phạm vi), đề dự bị.
8. `tom_tat` của TN cũ lấy mô tả dạng (không phải tóm tắt từng câu); ĐH và đề mới có tóm tắt riêng từng câu.
9. Nguồn `luatvietnam.vn` ghi `http` 403 với `curl` (chặn máy khách); mọi đề vẫn có ít nhất một nguồn 200.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 27/11/2026 | Tạo hồ sơ: 23 đề, taxonomy, tần suất, check.py (exit 0) | agent runner-de-lich-su |
