# Backlog of deferred cases and checks — GEO

> Chủ sở hữu: QC. Rule M18: ca hiếm, gượng ép hoặc không có nghĩa ở màn đó không được viết thành case; ghi ở đây kèm lý do và xem lại sau vòng thực thi 1. Không có gì bị bỏ lặng lẽ. Giải thích mã: `review/QC_test_design.md` §0.

| # | Type (AC / TC / CL row) | Story / screen | What was deferred (one line) | Why (likelihood · impact · user cannot do it · no meaning here) | Decided by | Revisit at | Status (Deferred / Promoted → id / Dropped) |
|---|---|---|---|---|---|---|---|
| 1 | CL row (CL-UI-04) | US-GEO-101 màn vào lớp | mất mạng đúng lúc bấm vào lớp | hiếm; không mất dữ liệu, bấm lại là được | QC | sau vòng thực thi 1 | Deferred |
| 2 | TC | US-GEO-703 | mất sự kiện `open`, `tool` khi máy chủ CSDL sập đột ngột (NFR-GEO-051) | rất hiếm; ARCH đã nêu mức chấp nhận, chờ chủ đầu tư; không ảnh hưởng quyền xem lời giải | QC | khi NFR-GEO-051 được chấp nhận | Deferred |
| 3 | TC | US-GEO-612 | thiết bị hết pin đúng lúc tới giờ mở | hiếm; mở lại trang là thấy trạng thái đúng | QC | sau vòng thực thi 1 | Deferred |
| 4 | TC | US-GEO-301 | xoay hình liên tục 5 phút để tìm rò bộ nhớ | không phải việc thường ngày; cần công cụ đo riêng | QC | sau vòng thực thi 1 | Deferred |
| 5 | TC | US-GEO-101 | tên hiển thị chứa emoji, ký tự điều khiển, chữ Hán | học sinh nhập tên tiếng Việt; có một case tên có dấu; phần còn lại hiếm | QC | sau vòng thực thi 1 | Deferred |
| 6 | TC | US-GEO-107 | tắt một bản sao máy chủ giữa lúc tải (NFR-GEO-058), co giãn ngang (NFR-GEO-012) | máy phát triển chỉ có một nút; chưa đo được | QC, theo ghi chú của ARCH | khi có môi trường nhiều nút | Deferred |
| 7 | TC | US-GEO-505 | đổi bộ lọc trong lúc trang kế đang tải | kho 48 đề, ba trang; hậu quả chỉ là tải lại danh sách | QC | sau vòng thực thi 1 | Deferred |
| 8 | TC | US-GEO-306 | iPad chia đôi màn hình, bàn phím rời | không có nguồn yêu cầu; hiếm ở lớp học | QC | sau vòng thực thi 1 | Deferred |
| 9 | TC | US-GEO-609 | moi đáp án bằng ngôn ngữ thứ ba, bằng hình ảnh | AI giả lập không xử lý; có ý nghĩa khi nối AI thật | QC | khi nối AI thật | Deferred |
| 10 | TC | US-GEO-104 | thử mọi tổ hợp vai × thực thể × hành động ở tầng giao diện | đã kiểm ở tầng API bằng bộ kiểm cách ly tự động (NFR-GEO-030); lặp ở giao diện không thêm giá trị | QC | sau vòng thực thi 1 | Deferred |
| 11 | TC | US-GEO-410 | phân trang nhật ký hỏi đáp không lặp không sót khi có bản ghi trùng mốc | một học sinh hiếm khi có hơn một trang câu hỏi; kích thước trang chưa có (QA-GEO-04); quy tắc phân trang đã kiểm ở Kho đề và bảng sự kiện | QC | sau vòng thực thi 1 | Deferred |
| 12 | AC / TC | US-GEO-101 | giả mạo tên bạn cùng lớp khi vào lớp | không có cơ chế trong nguồn; rủi ro đã nêu cho chủ đầu tư (Q-CDT-03) | BA đề nghị (M-215), QC nhận | khi chủ đầu tư trả lời Q-CDT-03 | Deferred |
| 13 | AC / TC | US-GEO-303, 304 | xóa hoặc hoàn tác đối tượng đã vẽ; giữ hình vẽ thêm sau khi tải lại; nhồi rất nhiều điểm trên một cạnh | nguồn không nêu; ca nhồi điểm là gượng ép | BA đề nghị (M-215), QC nhận | sau vòng thực thi 1 | Deferred |
| 14 | TC | US-GEO-305 | đo gián tiếp yếu tố trung gian rồi tự tính đáp số | được phép theo D-GEO-024, không phải lỗi | BA đề nghị (M-215), QC nhận | không xem lại trừ khi D-GEO-024 đổi | Dropped |
| 15 | AC / TC | US-GEO-402, 408, 411 | ảnh chữ viết tay; đề trộn hai ngôn ngữ; đường học sinh tự vẽ khi chuyển bước; "Hỏi thêm về khái niệm này", từ khóa liên quan, hình nhỏ trong bài giảng | AI giả lập không xử lý; các phần của prototype không có trong REQ-GEO-11 | BA đề nghị (M-215), QC nhận | khi nối AI thật | Deferred |
| 16 | AC / TC | US-GEO-602, 605 | chọn nhiều lớp trong một lần giao; đổi giờ khi học sinh đang xem thì thẻ khóa cập nhật giờ mới trong bao lâu | nguồn không nêu; liên quan mâu thuẫn QA-GEO-37 (M-108) | BA đề nghị (M-215), QC nhận | khi PO phán M-108 | Deferred |
| 17 | AC / TC | US-GEO-702 | câu hỏi có nội dung nhạy cảm hiển thị thế nào ở Theo dõi lớp | nguồn không nêu cách xử lý | BA đề nghị (M-215), QC nhận | sau vòng thực thi 1 | Deferred |
| 18 | TC | US-GEO-703 | đối soát bộ đếm tiến độ với nhật ký sự kiện (NFR-GEO-053) | việc vận hành, ARCH đã có truy vấn đối soát ở `architecture/07_verification.md` | BA đề nghị (M-215), QC nhận | sau lần thử tải đầu | Deferred |
