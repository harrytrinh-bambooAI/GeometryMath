Phiên bản: v0.1 · 2026-11-26
# S3-E3 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (đã chốt PO-66) để việc không dừng; chủ sản phẩm đã chốt toàn bộ ở gate 16 (PO-66, 26/11/2026, "OK hết"); AC đã cập nhật theo mã QA ở T-261126-37.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (quyết định · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E3-01 | Định nghĩa bốn trạng thái ô: "Xem lại" là gì? (STAGES nêu chưa mở, đang làm, xong, số gợi ý đã dùng; bản mẫu dùng Đúng, Xem lại, Chưa làm, Đang làm). | Chưa làm: chưa tương tác; Đang làm: có tương tác, chưa gửi đáp số cuối; Đúng: đáp số cuối khớp máy; Xem lại: đáp số cuối chưa khớp hoặc hết lượt. Số gợi ý chỉ ở khung bằng chứng. Đã chốt PO-66 · vừa · DQ-21 | US-S3E3-01 (cả epic) | PO, giáo viên cố vấn |
| QA-S3E3-02 | "Bài làm" để tính ngưỡng 5 là gì: có tính em Đang làm? | Tính (có ít nhất một tương tác, kể cả Đang làm), theo số em khác nhau. Quyết định này làm Đề 4 của dữ liệu mẫu đúng bằng 5. Đã chốt PO-66 · vừa · PO-65 Q6 | US-S3E3-03 SC2…SC5, SC11, FC7 | PO |
| QA-S3E3-03 | Nút "Nhắn em" trong bản mẫu: làm không? (không có trong story map; nhắn tin cho học sinh là giao tiếp mới) | Không làm ở chặng 3; ẩn khỏi bản dựng, giữ "Mở bài làm". Đề xuất owner chốt. Đã chốt PO-66 · vừa · ảnh `c3-04-ket-qua-lop.png`, PO-65 | US-S3E3-02 BR-S3E3-24 | PO, Designer |
| QA-S3E3-04 | Câu hỏi tự do của học sinh có thể chứa dữ liệu cá nhân em tự gõ. Có lọc không? Độ dài tối đa? | Không lọc ở màn giáo viên; gập sau 300 ký tự; độ dài tối đa 1000 ký tự; không ghi nội dung vào nhật ký; thông báo riêng tư nói rõ. Đã chốt PO-66 · thấp · PO-61, PO-64 | US-S3E3-02 FC2, FC6 | PO, ARCH |
| QA-S3E3-05 | Che chuỗi giống email hoặc số điện thoại khỏi dữ liệu gửi nhà cung cấp AI gộp? | Có, thay bằng `[đã ẩn]`; không gửi tên hiển thị, mã, mã lớp. Đã chốt PO-66 · vừa · PO-61, PO-64 | US-S3E3-03 SC12, FC8 | PO, ARCH |
| QA-S3E3-06 | Học sinh Đã rời có tính vào ngưỡng gộp và vào n không? | Không (kết quả lớp hiện tại); bài làm của em vẫn xem ở bảng. Đã chốt PO-66 · vừa · Ch3 §5 | US-S3E3-03 FC1 | PO |
| QA-S3E3-07 | Câu chữ MSG-S3-41…48 do ai duyệt? | BA đề xuất, Designer duyệt. Đã chốt PO-66 · cao | Mọi AC dẫn MSG-S3-4 | Designer |
| QA-S3E3-08 | Thứ tự sắp theo tên: tên hiển thị tiếng Việt, Đ sau D, hòa theo mã người dùng; tên có hai từ sắp theo từ đầu hay từ cuối (cách người Việt thường sắp là theo tên gọi)? | Theo toàn bộ chuỗi tên hiển thị từ trái sang phải (khớp bản mẫu). Đã chốt PO-66 · vừa · DQ-21 | US-S3E3-01 SC4, SC5 | Designer, giáo viên cố vấn |
| QA-S3E3-09 | Lớp tối đa bao nhiêu học sinh; bảng một trang bao nhiêu hàng? | 100 hàng một trang (trần chung); lớp lớn hơn phân trang bằng con trỏ; ngưỡng p95 2 giây là đề xuất. Đã chốt PO-66 · thấp · D-GEO-142 | US-S3E3-01 SC10, FC10 | ARCH |
| QA-S3E3-10 | Tối đa bao nhiêu thẻ "Cả lớp vướng ở đâu" mỗi bộ? | 5 thẻ, phần còn lại gập "Còn k nhận định khác". Đã chốt PO-66 · thấp · DQ-22 | US-S3E3-03 FC4 | Designer |
| QA-S3E3-11 | Bộ gộp quá thời gian: bao lâu và lùi thế nào? | 10 giây rồi dữ liệu thô kèm "Thử lại". Đã chốt PO-66 · vừa · D-GEO-047 | US-S3E3-03 FC2 | ARCH |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Làm rõ AC cho story `US-S3E3-03`: ngưỡng tính theo đề, không theo bộ, và tính theo số em khác nhau (đề xuất cập nhật dòng story map) | QA-S3E3-02 |
| Bản mẫu `c3-04` có nút "Nhắn em" ngoài story map; đề xuất ẩn ở chặng 3 | QA-S3E3-03 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 11 câu hỏi và 2 đề xuất của S3-E3 | runner-T-261126-14 |
