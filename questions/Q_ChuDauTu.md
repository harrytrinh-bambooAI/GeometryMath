# Câu hỏi gửi chủ đầu tư

Sản phẩm là "Hình Học Cùng AI": bắt đầu bằng môn Hình học (MVP là hình không gian), hướng tới hệ sinh thái giáo dục 9 chặng. Chủ đầu tư là người quyết định cuối về phạm vi, người trả tiền, pháp lý và đối tác. Tài liệu này để bạn trả lời một lần cho nhiều điểm.

## 1. Giả định đang dùng
Các tài liệu và bản thiết kế hiện dựng trên những giả định dưới đây. Im lặng không biến giả định thành quyết định.

| Mã | Giả định | Đang dựng trên nó | Nếu câu trả lời khác đi |
|---|---|---|---|
| PO-02 | Nhãn tin cậy hai trục: máy ("Đã kiểm chứng" / "Cần xem lại") và người ("AI soạn" / "Giáo viên đã duyệt") | Màn hình chặng 2–5, tài liệu các chặng | Đổi nhãn trên mọi màn hình kho đề, lời giải, chấm bài |
| PO-03 | Không bao giờ tự duyệt điểm AI | Màn chấm bài, hàng chờ chấm, prototype | Thêm chế độ tự duyệt, đổi quy tắc hiển thị cho học sinh và phụ huynh |
| PO-04 | Không có xếp hạng | Màn kết quả lớp, hồ sơ học tập, báo cáo trường | Thêm bảng xếp hạng; cần xem lại nguyên tắc "không gắn nhãn" |
| PO-05 | Giáo viên chặng 2 vào bằng lời mời; đăng ký ở chặng 3 | Tài liệu các chặng, màn đăng ký c3-01 | Dời màn đăng ký lên chặng 2 |
| PO-06 | Môn thứ hai là Hàm số và đồ thị | Màn hình chặng 4, prototype | Vẽ lại màn hình chặng 4 cho môn khác |
| PO-07 | Bài tự học: lời giải mở sau gợi ý 1 và 2 | Prototype, `MVP.md` | Đổi luồng tự học |
| PO-08 | Trước giờ mở, công cụ đo không hiện đại lượng đề hỏi | Prototype, màn hình hình phẳng | Bỏ giới hạn, hoặc tắt hẳn công cụ đo khi đang khóa |
| PO-09 | Phụ huynh chỉ thấy thông tin đã duyệt; không xem từng câu con hỏi; không thấy số ngày học liên tiếp | Màn hình phụ huynh | Mở thêm dữ liệu cho phụ huynh |
| PO-10 | Điểm danh, sổ điểm chính thức, tài chính không thuộc chặng 8 | Tài liệu chặng 8 | Thêm các phân hệ này, chặng 8 lớn hơn nhiều |
| PO-11 | Video nhóm để sau; chặng 7 đi từ chia sẻ tài nguyên của giáo viên | Tài liệu chặng 7 | Đưa video vào phạm vi đầu; cần duyệt thiết kế an toàn trẻ em sớm hơn |
| PO-12 | Chạy dạng web trên iPad và iPhone tới hết chặng 4 | Prototype, mọi màn hình học sinh và phụ huynh | Làm ứng dụng cài đặt sớm hơn; đổi kế hoạch kỹ thuật |
| PO-13 | Sau MVP làm kỹ chặng 2 và 3 như một khối | Thứ tự thiết kế | Đổi thứ tự ưu tiên |
| PO-14 | Bộ dữ liệu mẫu dùng chung | Mọi màn hình | Đổi tên lớp, học sinh mẫu |
| PO-16 | Đề tự nhập trùng bài đang khóa thì kế thừa khóa | Prototype, tài liệu | Tự học thành đường vòng qua giờ mở lời giải |
| PO-17 | Giới hạn công cụ đo áp dụng cho bài luyện, bài hàm số, phòng nhóm | Tài liệu các chặng | Công cụ đo cho ra đáp số ở các nơi này |
| PO-18 | Chỉ giáo viên được mời hoặc đã xác nhận mới thêm đề và xem lời giải AI | Tài liệu chặng 2 và 3 | Ai đăng ký vai giáo viên cũng lấy được lời giải |
| PO-19 | Quy tắc do agent viết trong tài liệu các chặng là đề xuất cho tới khi gắn nguồn | Cách dùng tài liệu | Các quy tắc đó thành yêu cầu ngay |
| PO-20 | Phụ huynh luôn phải được trường xác minh; mục "Ở nhà giúp con" qua giáo viên duyệt | Tài liệu và màn hình chặng 6 | Cho phép liên kết ngay bằng mã |
| PO-21 | Chặng 3 và 5 chờ câu trả lời về đồng thuận của cha mẹ | Thứ tự làm việc | Được viết story tài khoản học sinh chính thức ngay |
| PO-22 | Giờ mở lời giải áp dụng cho mọi kênh (trả bài, chia sẻ, xuất file, trình chiếu, phụ huynh) | Tài liệu các chặng | Mở ngoại lệ cho từng kênh |
| PO-23 | Không có công tắc cho phụ huynh xem câu con hỏi | Màn chính sách của trường | Trường tự bật được |
| PO-24 | So sánh AI và giáo viên chỉ ở mức tổ bộ môn; đóng đường xem dữ liệu định danh của cơ quan quản lý | Tài liệu chặng 5, 8, 9 | Thiết kế luồng duyệt cho dữ liệu định danh |
| PO-25 | Kéo mặt phẳng ở chặng 2 mức P1; giao theo bộ ở chặng 3; chấm chéo để sau | Ba tài liệu | Đổi chặng của các tính năng này |
| PO-26 | Thời khóa biểu không thuộc phạm vi đầu của chặng 8 | Màn c8-02 | Đưa thời khóa biểu vào chặng 8 |
| PO-30 | Nội dung AI soạn hoặc đọc từ tài liệu, ảnh chỉ vào giáo trình sau khi giáo viên duyệt | Màn soạn, import, giáo trình | Cho phép tự lưu |
| PO-31 | Giáo trình riêng do AI đề xuất kèm bằng chứng, giáo viên duyệt mới áp dụng; không nhãn, không xếp hạng | Màn giáo trình riêng, lộ trình của em | Áp dụng thẳng cho học sinh |
| PO-32 | Soạn với AI và import thuộc chặng 2 và 5; giáo trình riêng thuộc chặng 6 | Thứ tự làm việc | Kéo các màn này vào MVP |
| PO-37 | Hỏi đáp theo giáo trình vẫn tuân giờ mở lời giải; trước giờ mở chỉ dẫn tới khái niệm | Màn hỏi đáp | Cho nêu cả hướng giải |
| PO-38 | Bài từ kho đề vào giáo trình mang nhãn "Giáo viên đã duyệt"; nhãn máy độc lập | Màn giáo trình | Đổi cách gắn nhãn |
| PO-39 | Sau MVP giáo viên thêm được tối đa một mức gợi ý (tổng ba mức) | Giáo trình riêng | Giữ cố định hai mức |
| PO-40 | Bài thêm vào giáo trình riêng chỉ lấy từ nội dung đã duyệt; học sinh thấy "đã vững" và "đang luyện"; phụ huynh thấy phần đang luyện trong dòng tóm tắt đã duyệt | Lộ trình của em, màn phụ huynh | Ẩn nhãn với học sinh hoặc phụ huynh |
| PO-41 | Chỉnh sửa bài trong giáo trình theo sang bài giao và bài học sinh làm | Màn giáo trình, giao bài | Chỉ áp dụng cho bài giao mới |
| PO-42 | Công cụ đo ẩn cả đại lượng tương đương với đại lượng đề hỏi | Màn làm bài | Chỉ ẩn đúng đại lượng đề hỏi |
| PO-43 | Bài giảng AI viết chưa duyệt vẫn hiện cho học sinh với nhãn "AI soạn · chờ giáo viên duyệt" | Hộp bài giảng, hỏi đáp | Ẩn tới khi giáo viên duyệt |
| PO-44 | Bài giảng được nêu công thức tổng quát trước giờ mở, không nêu đáp số hay bước của bài đang làm | Bài giảng, hỏi đáp | Siết hoặc nới mức nêu công thức |
| PO-46 | AI hỏi lại khi chưa rõ, đề xuất trước/sau, giáo viên nhận, bỏ, sửa tay, hoàn tác; AI không tự lưu | Màn soạn và import | Đổi mức tự chủ của AI |
| PO-47 | Import đối chiếu với kho bài giảng, không tạo bản trùng | Màn import | Luôn tạo bản mới |
| PO-48 | Sửa tay đổi dữ kiện đề thì vẫn lưu nhưng nhãn máy thành "Cần xem lại"; hộp bài giảng cần nhãn máy; nhóm Bài tập có ô chọn từng bài | Màn soạn, import | Chặn lưu khi lệch |
| PO-49 | Từ khóa, bài giảng có nguồn, hỏi đáp theo giáo trình nằm trong MVP ở mức mẫu; giáo viên tự soạn bài giảng ở chặng 2; kho tri thức tự quản ở chặng 5 | Tài liệu, trình chiếu | Dời các phần này sang chặng khác |

## 2. Cách trả lời
Với mỗi mã, ghi "đồng ý" hoặc phương án bạn chọn. Câu nào chưa quyết được, ghi "để sau": giả định tiếp tục được dùng và vẫn mang nhãn tạm thời.

## 3. Câu hỏi PO không tự đặt giả định được

**Q1. Người trả tiền đầu tiên là ai?**
Phương án: phụ huynh và học sinh · giáo viên hoặc trung tâm · nhà trường. Ảnh hưởng: thứ tự và phạm vi chặng 5 và chặng 8, cách đóng gói sản phẩm.

**Q2. Dữ liệu trẻ em: pháp nhân vận hành, nơi lưu, thời gian giữ, cách xin đồng thuận?**
Cần có trước khi mở tài khoản chính thức ở chặng 3. Cần tư vấn pháp lý.
Câu hỏi thêm sau phản biện: ai đồng thuận thay học sinh chưa thành niên khi phụ huynh chỉ vào hệ thống từ chặng 6? Cho tới khi có câu trả lời, chặng 3 và chặng 5 chưa được viết story (PO-21).

**Q3. Trường pilot là ai và cam kết gì?**
Số lớp, có cho dùng bài làm thật để đo AI chấm không, có thử xác minh phụ huynh không. Là điều kiện mở chặng 5 và 6.

**Q4. Ai soát kho 40–60 đề trước khi học sinh thật dùng?**
Lời giải hiện do AI soạn và mới được kiểm chứng phần tính toán.

**Q5. Tên sản phẩm khi mở môn thứ hai?**
Giữ "Hình Học Cùng AI" hay đặt tên nền tảng.
