Phiên bản: v0.1 · 2026-11-26
# S3-E3 — thiết kế kiểm thử (Kết quả lớp (bảng lớp × bài) và "Cả lớp vướng ở đâu" — màn ưu tiên C3-04)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục và `work/s23/story-map-s23.md`; phần chung dưới đây dùng cho cả S3-E1…E6. **Trạng thái: ready for R3**. Mỗi dòng thiết kế dẫn tới ít nhất một case ở `test_cases.md`; quyền theo quan hệ, chặn trường dữ liệu cá nhân (PO-61, PO-64) và AuditEvent là bắt buộc của chặng 3.

## Danh mục nhóm kiểm tra (CL) dùng chung cho S3-E1…E6
| Nhóm | Nội dung |
|---|---|
| CL-FN-01 | Luồng chính |
| CL-FN-02 | Dữ liệu nhập (biên, định dạng, độ dài) |
| CL-FN-03 | Lỗi, trạng thái rỗng và thông báo |
| CL-FN-04 | Chuyển trạng thái (lớp, thành viên, bộ, giờ mở) |
| CL-FN-05 | Phân quyền theo vai (sai vai thì FORBIDDEN) |
| CL-FN-06 | Cách ly theo quan hệ (ngoài quan hệ thì NOT_FOUND, không lộ dữ liệu) |
| CL-FN-07 | Dữ liệu máy chủ trả về (soát cả thân phản hồi, tiêu đề no-store) |
| CL-FN-08 | Phân trang và con trỏ |
| CL-FN-09 | Thời gian (biên giờ, giờ máy chủ, múi giờ, giờ yên lặng) |
| CL-FN-10 | Đồng thời, gửi lặp, trùng tên, hai thiết bị |
| CL-FN-11 | Nhãn máy và nhãn người (không ai ngoài engine gắn "Đã kiểm chứng") |
| CL-FN-12 | Định dạng và quy tắc hiển thị (không tổng, không hạng, thứ tự tên) |
| CL-FN-13 | Sự kiện, AuditEvent, Notification |
| CL-FN-14 | Cổng AI (giả lập, quá giờ, căn cứ, ẩn danh) |
| CL-UI-01 | Thiết bị và khổ màn (web, iPad, iPhone) |
| CL-UI-02 | Chuỗi hiển thị và khung chờ |
| CL-UI-04 | Gián đoạn (mất mạng) |
| CL-X-01 | Tải và hiệu năng |
| CL-X-02 | Kế hoạch truy vấn, chỉ mục |
| CL-X-03 | An toàn (IDOR, nội dung độc, ràng buộc cơ sở dữ liệu) |
| CL-X-04 | Dữ liệu cá nhân và trẻ em (PO-61, PO-64) |

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 | US-S3E3-01…03 | Y |  |
| CL-FN-02 | US-S3E3-01 (bộ 1 và 20 đề), 03 (ngưỡng cấu hình) | Y |  |
| CL-FN-03 | US-S3E3-01, 02, 03 (rỗng, chưa đủ dữ liệu, lỗi tải) | Y |  |
| CL-FN-04 | US-S3E3-01, 02 (Đã rời, Chờ duyệt, bộ Nháp) | Y |  |
| CL-FN-05 | US-S3E3-01…03 | Y |  |
| CL-FN-06 | US-S3E3-01…03 (lớp khác, giáo viên khác, em hai lớp) | Y |  |
| CL-FN-07 | US-S3E3-01, 02, 03 (không tổng, không hạng, no-store) | Y |  |
| CL-FN-08 | US-S3E3-01 (101 học sinh), 03 (5 thẻ) | Y |  |
| CL-FN-09 | US-S3E3-02, 03 (cập nhật lúc, làm mới) | Y |  |
| CL-FN-10 | US-S3E3-01, 03 (hòa tên, một em nhiều lần, hai tab) | Y |  |
| CL-FN-11 | US-S3E3-03 (không nhãn "Đã kiểm chứng") | Y |  |
| CL-FN-12 | US-S3E3-01 (sắp theo tên tiếng Việt, ô có chữ), 03 | Y |  |
| CL-FN-13 | — | N | sự kiện đo chữa bài ở S3-E6 |
| CL-FN-14 | US-S3E3-03 (AI gộp giả lập, quá giờ, căn cứ, ẩn danh) | Y |  |
| CL-UI-01 | US-S3E3-01 (thang xám, màn nhỏ) | Y |  |
| CL-UI-02 | US-S3E3-01 (khung chờ, lỗi tải) | Y |  |
| CL-UI-04 | — | N | đã có lỗi tải ở CL-UI-02 |
| CL-X-01 | US-S3E3-01 (lớp 100 em) | Y |  |
| CL-X-02 | US-S3E3-01 (không truy vấn lặp theo hàng) | Y |  |
| CL-X-03 | US-S3E3-02 (nội dung HTML trong câu hỏi) | Y |  |
| CL-X-04 | US-S3E3-02, 03 (trường được phép, ẩn danh khi gửi AI) | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 01 | bảng 8 × 4 theo F-S3-051 | từng ô khớp; dòng phụ đề đúng | EP | E2E trình duyệt |
| CL-FN-12 | 01 | soát cột cuối, dòng cuối, tooltip, bản in, thân phản hồi | không tổng, phần trăm, điểm, hạng ở bất kỳ đâu | soát, đoán lỗi | E2E trình duyệt, tích hợp API |
| CL-FN-12 | 01 | sắp theo tên: Dũng, Đạt, Đức Huy; hòa tên; đổi kết quả | thứ tự chữ cái tiếng Việt, hòa theo mã, không đổi theo kết quả | EP, BVA | E2E trình duyệt |
| CL-UI-01 | 01 | thang xám; trình đọc màn hình | mọi ô có chữ và biểu tượng, tên truy cập đủ | soát | E2E trình duyệt, thủ công |
| CL-FN-02 | 01 | bộ 1 đề, 20 đề | 1 và 20 cột, cột học sinh cố định | BVA | E2E trình duyệt |
| CL-X-01 | 01 | lớp 100 em, bộ 20 đề, 30 lần gọi | một trang, số truy vấn không đổi theo số hàng, p95 không quá 2 giây (đề xuất) | tải nhẹ | tích hợp CSDL, tải |
| CL-FN-08 | 01 | lớp 101 em | trang 100 + 1, con trỏ sửa thì INVALID_CURSOR | BVA | tích hợp API |
| CL-FN-03 | 01 | chưa ai làm; lớp 0 em Đang học; bộ Nháp; chậm rồi lỗi | MSG-S3-41, 42; NOT_FOUND; khung chờ rồi Thử lại | bảng trạng thái | E2E trình duyệt |
| CL-FN-06 | 01 | giáo viên khác; học sinh; ghép lớp A với bộ lớp B; em hai lớp | NOT_FOUND hoặc FORBIDDEN; hàng em chỉ có bốn ô của lớp này | bảng quyết định | tích hợp API |
| CL-FN-04 | 01 | Chờ duyệt không có hàng; Đã rời có hàng và dấu | đúng 8 và 9 hàng | EP | E2E trình duyệt |
| CL-FN-01 | 02 | bấm ô Xem lại, Chưa làm, Đang làm, Đã rời, Mở bài làm | khung bằng chứng đúng từng loại; Mở bài làm chỉ đọc | EP | E2E trình duyệt |
| CL-X-03 | 02 | câu hỏi chứa `<script>`, 1000 ký tự, số điện thoại | hiển thị như chữ thuần, gập 300 ký tự, nhật ký không ghi nội dung | đoán lỗi | E2E trình duyệt |
| CL-X-04 | 02 | liệt kê mọi khóa thân bằng chứng | chỉ trường được phép của US-S3E5-03 | soát | tích hợp API |
| CL-FN-01 | 03 | đề 7 bài làm, đúng 5, 4, 6, 3 bài làm (bảng biên bên dưới) | gộp khi n lớn hơn hoặc bằng 5; dưới 5 thì dữ liệu thô và MSG-S3-43 | BVA, bảng quyết định | E2E trình duyệt |
| CL-FN-10 | 03 | một em tương tác nhiều lần; em Đã rời | tính theo số em khác nhau; em Đã rời không tính | đoán lỗi | tích hợp API |
| CL-FN-14 | 03 | AI giả lập: nhận định không căn cứ, nhãn Đã kiểm chứng, phần trăm, quá 10 giây, quá 5 thẻ | loại; bỏ nhãn; loại phần trăm; lùi về dữ liệu thô; gập "Còn k" | đoán lỗi | tích hợp API, E2E trình duyệt |
| CL-X-04 | 03 | yêu cầu gửi nhà cung cấp AI giả lập | không tên, không mã người dùng, không mã lớp; email và số điện thoại thay `[đã ẩn]` | soát | tích hợp CSDL, bộ giả lập |
| CL-FN-02 | 03 | ngưỡng đặt 3 rồi 5 | áp theo cấu hình, chữ "5" trong banner lấy từ cấu hình | BVA | đơn vị |
| CL-X-01 | 03 | làm mới hai lần không đổi dữ liệu | nhà cung cấp giả lập chỉ bị gọi một lần | soát | tích hợp CSDL |

## Bảng biên ngưỡng gộp (BV-n) cho US-S3E3-03
`MIN_AGGREGATE` = 5 (TẠM, PO-65 Q6). n là số bài làm của một đề: học sinh Đang học khác nhau có ít nhất một tương tác (gửi đáp số, xin gợi ý, hỏi). Mỗi đề xét riêng.

| BV | Tình huống (bộ tuần 15, 11A1) | n của đề | Kết quả mong đợi |
|---|---|---|---|
| BV-1 | Bộ tuần 16 vừa giao, chưa em nào mở | 0 | không thẻ; MSG-S3-43 với m = 0 hoặc câu chung "Chưa có bài làm" |
| BV-2 | Lớp 11A2 chỉ 3 em, cả 3 đã làm Đề 3 | 3 | luôn dữ liệu thô; MSG-S3-44 |
| BV-3 | Đề 4 khi Ngọc Anh chưa mở | 4 | không thẻ; MSG-S3-43 với m = 4; AI giả lập không bị gọi |
| BV-4 | Đề 4 theo F-S3-051 (Ngọc Anh Đang làm tính) | 5 | gộp |
| BV-5 | Đề 4 khi Khánh Linh xin thêm một gợi ý | 6 | gộp |
| BV-6 | Đề 3 theo F-S3-051 | 7 | gộp, thẻ "Xem 6 bài làm" theo ví dụ AI giả lập |
| BV-7 | Quân gửi 3 lần và xin 2 gợi ý ở Đề 4, chỉ 4 em khác nhau | 4 | không gộp (tính theo em, không theo lượt) |
| BV-8 | Đề 4 có 4 em Đang học và Văn Tú Đã rời | 4 | không gộp (Đã rời không tính) |
| BV-9 | Cấu hình `MIN_AGGREGATE` = 3, Đề 4 có 3 bài làm | 3 | gộp |
| BV-10 | Lớp 101 em | — | 100 hàng + 1 hàng, con trỏ |

## Định nghĩa trạng thái ô (BR-S3E3-02) và dữ liệu mẫu F-S3-051
Chưa làm: không có tương tác nào trên đề. Đang làm: có tương tác nhưng chưa gửi đáp số cuối. Đúng: đáp số cuối khớp đáp số máy đã duyệt. Xem lại: đã gửi đáp số cuối nhưng chưa khớp, hoặc hết lượt thử.
Đếm n theo F-S3-051: Đề 1 = 7, Đề 2 = 7, Đề 3 = 7, Đề 4 = 5 (Linh, Mai, Bảo chưa làm Đề 4).

## Danh sách khóa cấm trong thân phản hồi Kết quả lớp và "Cả lớp vướng ở đâu"
total, sum, score, point, grade, percent, pct, rank, ranking, order_by_result, average, mean, best, worst, top, bottom (không phân biệt hoa thường, gạch dưới hoặc gạch ngang). Chữ cấm trong giao diện: "Tổng", "Điểm", "Hạng", "Xếp hạng", "Tốt nhất", "Kém nhất", dấu `%`.

## Rủi ro đang ưu tiên (thang cao, vừa, thấp)
| Rủi ro | Mức | Case chặn |
|---|---|---|
| Bảng bị đọc thành xếp hạng (có số tổng lọt vào thân phản hồi) | cao | SC2, SC3 của US-S3E3-01; FC10 của US-S3E3-03 |
| Gộp nhận định từ ít bài làm | cao | SC3, SC11, FC1 của US-S3E3-03 |
| Lộ dữ liệu giữa lớp hay giáo viên | cao | FC6…FC9 của US-S3E3-01; FC3, FC4 của US-S3E3-02 |
| Dữ liệu em gửi ra ngoài (nhà cung cấp AI) có danh tính | cao | SC12, FC8 của US-S3E3-03 |
| Nội dung độc trong câu hỏi | vừa | FC1 của US-S3E3-02 |

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E3 | runner-T-261126-14 (QC) |
