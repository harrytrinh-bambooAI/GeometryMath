Phiên bản: v0.1 · 2026-11-28
# S5-E2 — test case (Ra đề theo ma trận và trợ lý soạn bộ đề (chọn loại đề và phạm vi, lắp đề, nhiều mã đề, xuất))

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E2-01 — Nạp ma trận chuẩn, mẫu câu và ngân hàng câu vào máy chủ, kiểm schema và đáp số

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-01 | Nạp đủ dữ liệu đề v1.0 | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Người vận hành (vai hệ thống) | • CSDL thử chưa có dữ liệu đề | • Hàng dữ liệu theo Pre-condition | 1. chạy lệnh nạp với `de-thi v1.0`<br/>2. gọi `GET /api/exam/catalog` | • báo cáo nạp: 4 ma trận, 110 mẫu, 1 774 câu, 0 lỗi<br/>• danh mục trả 4 loại đề KT15-HHKG11, GK2-L11, CK2-L11, TN-THPT-764 | US-S5E2-01-SC1 |
| TC-S5E2-02 | Chạy lại cùng phiên bản là lũy đẳng | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Người vận hành (vai hệ thống) | • đã nạp `de-thi v1.0` | • Hàng dữ liệu theo Pre-condition | 1. chạy lại lệnh nạp | • số bản ghi không đổi (4 / 110 / 1 774)<br/>• báo cáo "0 thêm, 0 đổi" | US-S5E2-01-SC2 |
| TC-S5E2-03 | Câu lệch đáp số engine bị loại, phần còn lại vẫn nạp | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Người vận hành (vai hệ thống) | • bản sao ngân hàng có 1 câu HHKG sửa đáp số thành 0,8200 (engine 0,8165) | • Hàng dữ liệu theo Pre-condition | 1. chạy lệnh nạp | • báo cáo 1 lỗi nêu id câu và hai giá trị<br/>• 1 773 câu được nạp<br/>• câu lỗi không có trong ngân hàng máy chủ | US-S5E2-01-FC1 |
| TC-S5E2-04 | Ma trận tổng điểm khác 10 bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Người vận hành (vai hệ thống) | • bản sao `KT15-HHKG11` có tổng điểm 9 | • Hàng dữ liệu theo Pre-condition | 1. chạy lệnh nạp | • ma trận đó không được nạp, báo cáo nêu "tổng điểm 9 ≠ 10"<br/>• ba ma trận còn lại được nạp | US-S5E2-01-FC2 |

### US-S5E2-02 — Form ra đề: loại đề, lớp, chương, thời gian, số câu và điểm, tỉ lệ mức độ, số mã đề

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-05 | Chọn đề 15 phút, form điền theo ma trận chuẩn | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • cô Lan mở "Ra đề theo ma trận" | • Hàng dữ liệu theo Pre-condition | 1. chọn loại đề "Kiểm tra 15 phút – Quan hệ vuông góc" (F-S5-030) | • form điền thời gian 15, phần TN4 6 câu × 1,0 và trả lời ngắn 2 câu × 2,0, tổng 10<br/>• không có ô sửa cấu trúc cố định<br/>• số mã đề mặc định 4 | US-S5E2-02-SC1 |
| TC-S5E2-06 | Bỏ một chương thì ô chuyển sang chương còn lại | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ma trận `GK2-L11` (mũ-lôgarit và quan hệ vuông góc) | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/exams/preview` bỏ chương mũ-lôgarit | • bảng ô trả về chỉ có chủ đề quan hệ vuông góc<br/>• số câu mỗi loại không đổi<br/>• tổng điểm vẫn 10 | US-S5E2-02-SC2 |
| TC-S5E2-07 | Tổng điểm khác 10 bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ma trận F-S5-030 | • Hàng dữ liệu theo Pre-condition | 1. gửi preview với điểm câu trả lời ngắn 1,5 | • VALIDATION_FAILED, MSG-S5-10 | US-S5E2-02-FC1 |
| TC-S5E2-08 | Thời gian dự kiến vượt thời gian làm bài bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ma trận F-S5-030 | • Hàng dữ liệu theo Pre-condition | 1. gửi preview với thời gian 8 phút | • VALIDATION_FAILED, MSG-S5-11 | US-S5E2-02-FC2 |
| TC-S5E2-09 | Số mã đề 9 bị từ chối | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ma trận F-S5-030 | • Hàng dữ liệu theo Pre-condition | 1. gửi với số mã đề 9 | • VALIDATION_FAILED, MSG-S5-13 | US-S5E2-02-FC3 |

### US-S5E2-03 — Lắp nhiều mã đề tương đương theo ràng buộc của `lap_de.py`, tái lập theo seed

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-10 | Lắp 4 mã đề 15 phút đạt mọi ràng buộc | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • dữ liệu F-S5-032 đã nạp | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/exams` ma trận F-S5-030, 4 mã, seed 2026 | • trả 4 mã đề, mỗi mã 8 câu, tổng điểm 10<br/>• bộ kiểm đối chứng (`check.py` Kiểm 6) đạt cho cả 4 mã<br/>• `AiCallLog` không có dòng mới | US-S5E2-03-SC1 |
| TC-S5E2-11 | Cùng seed cho ra cùng bộ đề | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • đã lắp bộ đề ở SC1 | • Hàng dữ liệu theo Pre-condition | 1. gửi lại cùng yêu cầu với seed 2026 | • danh sách id câu và thứ tự phương án của 4 mã trùng khớp lần trước | US-S5E2-03-SC2 |
| TC-S5E2-12 | Đề TN THPT đúng cấu trúc QĐ 764 | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • dữ liệu F-S5-032 | • Hàng dữ liệu theo Pre-condition | 1. lắp ma trận F-S5-031, 2 mã, seed 2026 | • mỗi mã có 12 TN4, 4 đúng/sai, 6 trả lời ngắn (đáp án ≤ 4 ký tự)<br/>• A/B/C/D lệch ≤ 1<br/>• độ khó hai mã lệch ≤ 5 % | US-S5E2-03-SC3 |
| TC-S5E2-13 | Ngân hàng không đủ câu thì báo ô thiếu, không trả đề thiếu | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ma trận CK2-L11 chỉ giữ chương lượng giác (1 mẫu) | • Hàng dữ liệu theo Pre-condition | 1. lắp 8 mã | • CONFLICT, MSG-S5-12 nêu ô thiếu<br/>• không có mã đề nào được lưu | US-S5E2-03-FC1 |
| TC-S5E2-14 | Giáo viên không lắp đề cho lớp không phải của mình | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 10B của thầy Khôi | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gửi `POST /api/exams` với `classId` của 10B | • NOT_FOUND | US-S5E2-03-FC2 |

### US-S5E2-04 — Xem và sửa bộ đề nháp: thay một câu cùng ô, nhãn hai trục, duyệt vào kho "Của tôi"

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-15 | Thay một câu cùng ô | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • bộ đề 4 mã từ US-S5E2-03 SC1 | • Hàng dữ liệu theo Pre-condition | 1. mở mã 101<br/>2. bấm "Thay câu" ở câu 3 | • câu 3 mới cùng chủ đề, dạng, mức độ và điểm<br/>• các câu khác không đổi<br/>• nhãn "Đã kiểm chứng" của câu mới hiện cạnh câu | US-S5E2-04-SC1 |
| TC-S5E2-16 | Duyệt bộ đề vào "Của tôi" | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • bộ đề nháp của cô Lan | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/exams/{id}/approve` | • nhãn người "Giáo viên đã duyệt", người duyệt, giờ<br/>• bộ đề có trong `GET /api/exams?shelf=mine` | US-S5E2-04-SC2 |
| TC-S5E2-17 | Hết lượt bốc thì giữ câu cũ và báo | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ô của câu 3 chỉ có 1 câu trong ngân hàng | • Hàng dữ liệu theo Pre-condition | 1. gửi thay câu 3 | • CONFLICT, MSG-S5-12 nêu ô<br/>• câu 3 giữ nguyên | US-S5E2-04-FC1 |
| TC-S5E2-18 | Bộ đề chưa duyệt không giao được | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • bộ đề nháp chưa duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi tạo bài có chấm (US-S5E4-02) với bộ đề này | • CONFLICT<br/>• không có bài nào được tạo | US-S5E2-04-FC2 |

### US-S5E2-05 — Xuất mã đề: đề, đáp án, hướng dẫn chấm, bảng đặc tả (DOCX, PDF)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-19 | Xuất 4 phần của mã 101 | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • bộ đề đã duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi `GET /api/exams/{id}/codes/101/export?format=docx&parts=all` | • tệp nén có 4 tệp: đề, đáp án, hướng dẫn chấm, bảng đặc tả<br/>• tệp đề không chứa chuỗi đáp án nào của mã đó<br/>• điểm trong hướng dẫn chấm cộng đủ 10 | US-S5E2-05-SC1 |
| TC-S5E2-20 | Xuất PDF chỉ phần đề cho cả 4 mã | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • bộ đề đã duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi export `format=pdf&parts=de` cho cả 4 mã | • 4 tệp PDF, mỗi tệp ghi mã đề ở đầu trang<br/>• không có trang đáp án | US-S5E2-05-SC2 |
| TC-S5E2-21 | Học sinh không tải được đáp án | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An (học sinh 11A1) | • bộ đề đã duyệt, đã giao 11A1 | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi export `parts=dap-an` | • FORBIDDEN<br/>• thân phản hồi không có nội dung đề hay đáp án | US-S5E2-05-FC1 |
| TC-S5E2-22 | Bộ đề nháp xuất được nhưng có chân trang nháp | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • bộ đề chưa duyệt | • Hàng dữ liệu theo Pre-condition | 1. xuất `parts=de` | • mọi trang có chân trang "Bản nháp – chưa duyệt" | US-S5E2-05-FC2 |

### US-S5E2-06 — Trợ lý soạn đề nháp từ tài liệu Đang dùng của giáo viên (luồng D)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E2-23 | Soạn 5 đề nháp từ TL-01, đề đạt kiểm chứng chọn sẵn | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • kho F-S5-040; giả lập `exam_draft` trả 5 đề, 4 đề engine dựng được | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/copilot/exam-drafts` với `TL-01` v2, lớp 11A1, 5 đề | • 5 đề nháp nhãn "AI soạn", mỗi đề ghi nguồn "TL-01 v2"<br/>• 4 đề "Đã kiểm chứng" chọn sẵn<br/>• 1 đề "Cần xem lại" ở cuối, không chọn sẵn, MSG-S5-14 | US-S5E2-06-SC1 |
| TC-S5E2-24 | Duyệt từng đề đưa vào ngân hàng "Của tôi" | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 5 đề nháp ở SC1 | • Hàng dữ liệu theo Pre-condition | 1. duyệt 2 đề "Đã kiểm chứng" | • 2 đề vào ngân hàng của cô Lan, nhãn "Giáo viên đã duyệt"<br/>• 3 đề còn lại vẫn là nháp<br/>• đáp số 2 đề bằng giá trị engine | US-S5E2-06-SC2 |
| TC-S5E2-25 | Tài liệu chưa Đang dùng không làm nguồn được | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • `TL-02` Chờ duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi soạn đề với `TL-02` | • CONFLICT, MSG-S5-15<br/>• không gọi AI | US-S5E2-06-FC1 |
| TC-S5E2-26 | Tài liệu của giáo viên khác không làm nguồn được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • `TL-09` của thầy Khôi (F-S5-041) | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gửi soạn đề với `TL-09` | • NOT_FOUND<br/>• không gọi AI | US-S5E2-06-FC2 |
| TC-S5E2-27 | AI ghi đáp số khác engine thì đáp số engine thắng, đề "Cần xem lại" | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập trả 1 đề ghi đáp số "a√3/3" trong khi engine tính `a√6/3` | • Hàng dữ liệu theo Pre-condition | 1. gửi soạn đề | • đề đó nhãn "Cần xem lại", ghi rõ "đáp số AI khác máy tính"<br/>• ô đáp số hiện giá trị engine | US-S5E2-06-FC3 |

## Truy vết story → tiêu chí → case

Tổng: 27 case · E2E-API 25 · E2E-UI 2.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E2-01 | US-S5E2-01-SC1 | Success | Nạp đủ dữ liệu đề v1.0 | TC-S5E2-01 | E2E-API |
| US-S5E2-01 | US-S5E2-01-SC2 | Success | Chạy lại cùng phiên bản là lũy đẳng | TC-S5E2-02 | E2E-API |
| US-S5E2-01 | US-S5E2-01-FC1 | Fail / biên | Câu lệch đáp số engine bị loại, phần còn lại vẫn nạp | TC-S5E2-03 | E2E-API |
| US-S5E2-01 | US-S5E2-01-FC2 | Fail / biên | Ma trận tổng điểm khác 10 bị từ chối | TC-S5E2-04 | E2E-API |
| US-S5E2-02 | US-S5E2-02-SC1 | Success | Chọn đề 15 phút, form điền theo ma trận chuẩn | TC-S5E2-05 | E2E-UI |
| US-S5E2-02 | US-S5E2-02-SC2 | Success | Bỏ một chương thì ô chuyển sang chương còn lại | TC-S5E2-06 | E2E-API |
| US-S5E2-02 | US-S5E2-02-FC1 | Fail / biên | Tổng điểm khác 10 bị từ chối | TC-S5E2-07 | E2E-API |
| US-S5E2-02 | US-S5E2-02-FC2 | Fail / biên | Thời gian dự kiến vượt thời gian làm bài bị từ chối | TC-S5E2-08 | E2E-API |
| US-S5E2-02 | US-S5E2-02-FC3 | Fail / biên | Số mã đề 9 bị từ chối | TC-S5E2-09 | E2E-API |
| US-S5E2-03 | US-S5E2-03-SC1 | Success | Lắp 4 mã đề 15 phút đạt mọi ràng buộc | TC-S5E2-10 | E2E-API |
| US-S5E2-03 | US-S5E2-03-SC2 | Success | Cùng seed cho ra cùng bộ đề | TC-S5E2-11 | E2E-API |
| US-S5E2-03 | US-S5E2-03-SC3 | Success | Đề TN THPT đúng cấu trúc QĐ 764 | TC-S5E2-12 | E2E-API |
| US-S5E2-03 | US-S5E2-03-FC1 | Fail / biên | Ngân hàng không đủ câu thì báo ô thiếu, không trả đề thiếu | TC-S5E2-13 | E2E-API |
| US-S5E2-03 | US-S5E2-03-FC2 | Fail / biên | Giáo viên không lắp đề cho lớp không phải của mình | TC-S5E2-14 | E2E-API |
| US-S5E2-04 | US-S5E2-04-SC1 | Success | Thay một câu cùng ô | TC-S5E2-15 | E2E-UI |
| US-S5E2-04 | US-S5E2-04-SC2 | Success | Duyệt bộ đề vào "Của tôi" | TC-S5E2-16 | E2E-API |
| US-S5E2-04 | US-S5E2-04-FC1 | Fail / biên | Hết lượt bốc thì giữ câu cũ và báo | TC-S5E2-17 | E2E-API |
| US-S5E2-04 | US-S5E2-04-FC2 | Fail / biên | Bộ đề chưa duyệt không giao được | TC-S5E2-18 | E2E-API |
| US-S5E2-05 | US-S5E2-05-SC1 | Success | Xuất 4 phần của mã 101 | TC-S5E2-19 | E2E-API |
| US-S5E2-05 | US-S5E2-05-SC2 | Success | Xuất PDF chỉ phần đề cho cả 4 mã | TC-S5E2-20 | E2E-API |
| US-S5E2-05 | US-S5E2-05-FC1 | Fail / biên | Học sinh không tải được đáp án | TC-S5E2-21 | E2E-API |
| US-S5E2-05 | US-S5E2-05-FC2 | Fail / biên | Bộ đề nháp xuất được nhưng có chân trang nháp | TC-S5E2-22 | E2E-API |
| US-S5E2-06 | US-S5E2-06-SC1 | Success | Soạn 5 đề nháp từ TL-01, đề đạt kiểm chứng chọn sẵn | TC-S5E2-23 | E2E-API |
| US-S5E2-06 | US-S5E2-06-SC2 | Success | Duyệt từng đề đưa vào ngân hàng "Của tôi" | TC-S5E2-24 | E2E-API |
| US-S5E2-06 | US-S5E2-06-FC1 | Fail / biên | Tài liệu chưa Đang dùng không làm nguồn được | TC-S5E2-25 | E2E-API |
| US-S5E2-06 | US-S5E2-06-FC2 | Fail / biên | Tài liệu của giáo viên khác không làm nguồn được | TC-S5E2-26 | E2E-API |
| US-S5E2-06 | US-S5E2-06-FC3 | Fail / biên | AI ghi đáp số khác engine thì đáp số engine thắng, đề "Cần xem lại" | TC-S5E2-27 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
