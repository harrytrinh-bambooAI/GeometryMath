# E2 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E2-01 | Fixed (BA ghi `FORBIDDEN`, M-208) | US-GEO-203-FC4 | "Yêu cầu bị từ chối" không ghi `code`; theo M-402 (b) có hai khả năng (`FORBIDDEN` nếu có điểm cuối mà giáo viên không có quyền, `NOT_FOUND` nếu không có điểm cuối đổi nhãn) | TC-GEO-0222 chỉ kiểm nhãn vẫn "Cần xem lại" và trang không có điều khiển. Đề xuất BA ghi một `code` | BA · R3 của E2 |
| QF-E2-02 | Question (PO, qua BA) | US-GEO-203-SC3; D-GEO-040 | D-GEO-040 đòi "ít nhất 3 bộ giá trị tham số (a = 1 và hai giá trị khác)". Chưa có tiêu chí cho: (a) đề không có tham số (`HKG-03`, số đo cụ thể) thì "3 bộ" áp thế nào; (b) chỉ kiểm được dưới 3 bộ thì nhãn là gì | Không viết case (không tự đặt kỳ vọng). Thiết kế `test_design.md` có dòng này; chờ tiêu chí | PO · R3 của E2 |
| QF-E2-03 | Fixed | US-GEO-203-SC5; `F-GEO-036` | Xung đột dữ liệu đã biết về nhãn `HKG-06` (QA-GEO-03) | In ở Remark của TC-GEO-0217; case PROVISIONAL | QC · xong |
| QF-E2-04 | Fixed (BA thêm F-GEO-110…112, M-208) | US-GEO-204-SC1, FC2; US-GEO-203-FC3 | Kết quả của nhà cung cấp giả lập ("tọa độ sáu đỉnh", "sáu đỉnh trùng một điểm", "tự ghi kèm trạng thái") là dữ liệu vào của case nhưng chưa có hàng `F-GEO` ghi giá trị cụ thể | Case dùng mô tả của Given. Đề xuất BA thêm hàng dữ liệu cho ba kịch bản trả lời của AI giả lập (tọa độ bát diện đều cạnh 1; sáu điểm trùng; trạng thái tự khai) | BA · R3 của E2 |
| QF-E2-05 | Question (ARCH) | US-GEO-205 | Lược đồ chưa có bảng danh mục hình mẫu (BA đã hỏi, QA-GEO-29); TC-GEO-0230, 0233 là tích hợp CSDL nên phụ thuộc câu trả lời | Giữ case; đổi tầng nếu danh mục nằm ở mã chứ không ở CSDL | ARCH · R3 của E2 |

## Kết quả đối chiếu
- Chiều xuôi: 34 / 34 tiêu chí có case (100%). Chiều ngược: 34 / 34 case trỏ về đúng một tiêu chí (100%). Không có case viết tay: bốn dòng bảng quyết định nhãn và hai cặp biên (1e-6 / 2e-6, 45,0045 / 45,0046) đều đã có tiêu chí của BA.
- Giá trị đã soát với `02_domain_model.md`: ngưỡng 1e-4 · 45 = 0,0045 (M-GEO-03); a = 1, 2, 3 → 1, 2, 3 (`HKG-02`); 1 / 2 = 50,0% (M-GEO-18); chuỗi "Đã kiểm chứng" (D-GEO-079). Không thấy mâu thuẫn.
- Ca bắt buộc về nhãn: khớp (TC-GEO-0213), không khớp (0214, 0219, 0220), đề lạ kèm cảnh báo (0224, 0225, 0227); không bài sai mang "Đã kiểm chứng" (0221, 0227 và TC-GEO-0142 của E1).

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E2-06 | Fixed (D-GEO-163) | US-GEO-203-SC4; `F-GEO-071`; RV-GEO-23 | Biên 45,0045 nằm đúng ngưỡng dấu phẩy động nên bản cài đúng vẫn có thể trả "không khớp" | TC-GEO-0216 đổi thành 45,0044 qua `E2_override.txt`; ghi so sánh có ε ở TC-GEO-0203, 0205, 0216, 0219 | QC · xong |
| QF-E2-07 | To BA | US-GEO-203-SC4, 201-SC4; `F-GEO-071`, `F-GEO-073` | Fixture và tiêu chí còn ghi 45,0045; biên mới của F-GEO-073 chưa có | Đề nghị BA đổi theo D-GEO-163; QC gỡ chỉnh khi BA bàn giao | BA · R3 của E2 |
| QF-E2-02 | Fixed một phần (D-GEO-140) | US-GEO-203 | PO đã phán dung sai tuyệt đối cho đề không tham số | Chờ BA thêm tiêu chí và hàng fixture (ví dụ HKG-03) rồi QC thêm case | BA, QC · R3 của E2 |

- Đối chiếu sau lượt sửa: 34 / 34 tiêu chí có case (100%); 34 / 34 case trỏ về tiêu chí của BA.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- mới: TC-GEO-0234 (203-SC7), 0235 (203-FC6) cho đề không tham số (D-GEO-140). Gỡ chỉnh TC-GEO-0216 (AC đã ghi 45,0044). QF-E2-07 và QF-E2-02 đóng.
- Đối chiếu cuối: 36 / 36 tiêu chí có case (100%); 36 / 36 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).
- Sau M-306, M-220, M-221 (D-GEO-173…183): mới TC-GEO-0236 (203-SC8, D-GEO-178); TC-GEO-0235 theo dữ liệu mới "41,56 cm³". Đối chiếu: 37 / 37 tiêu chí, 37 / 37 case.

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E2-08 | Fixed | mọi case của E2 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E2_auto.txt`. Kết quả: E2E-UI 12 / E2E-API 5 / IT 20 / Manual 0 | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa". Không case nào bị bỏ.
- Đối chiếu: 37 / 37 tiêu chí có case (100%); 37 / 37 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.

## Dev (E2, Đợt 1; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E2-D01 | Fixed (chốt QF-E1-D07) | BR-GEO-134; D-GEO-040, 163, 178; F-GEO-071, F-GEO-079 | E1 dùng max(1e-4·\|q\|, nửa đơn vị chữ số cuối) + 1e-12 cho mọi đề; AC của E2 tách hai trường hợp | Một công thức DUY NHẤT `AnswerMatch.tolerance` (geometry.api) đúng BR-GEO-134: đề/đại lượng có tham số a → 1e-4·max(1, \|q\|) + 1e-12 trên a = 1, 2, 3; không tham số → max(1e-6, nửa đơn vị chữ số thập phân cuối) + 1e-12, tính một lần. Lý do: BR-GEO-134 là quy tắc của chủ sở hữu kiểm chứng (E2) và có ví dụ số cho cả hai nhánh (TC-0216/0219, 0234…0236); một công thức thì E1 và E2 không thể cho hai nhãn khác nhau cho cùng đáp số. E1 `AnswerExpr.matches` gọi hàm này (đại lượng có a ⇒ nhánh tham số); test E1 vẫn đạt | E2 · xong |
| QF-E2-D02 | Note | US-GEO-201-FC2 (F-GEO-074) | Fixture chỉ ghi "đáy tam giác ABC có AB = 1, BC = 1, CA = 3", không có đề đầy đủ | Test dùng đề "Cho lăng trụ đứng ABC.A′B′C′ có đáy là tam giác ABC với AB = 1, BC = 1, CA = 3, AA′ = 2. Tính thể tích."; AI giả lập trả kèm tọa độ để kiểm "không lặng lẽ thay bằng tọa độ AI" | QC · ghi nhận |
| QF-E2-D03 | Note | US-GEO-203-FC4; TC-GEO-0222 | Cần "một đề mang Cần xem lại" mà Kho đề phải giữ đúng sáu đề (TC-GEO-0125) | Seed `200_e2_core.sql`: version 99 (không hiện hành) của HKG-06, chưa có báo cáo ⇒ "Cần xem lại"; trang chi tiết đề `/gv/de/:versionId`. Điểm cuối đổi nhãn `PUT /api/v1/problems/{id}/verify-status` luôn `FORBIDDEN` (khớp QF-E2-01) | QC · ghi nhận |
| QF-E2-D04 | Note | US-GEO-204; D-GEO-047 | Nhận ra "hình phẳng"/"không khớp hình mẫu" thuộc cổng AI | AI giả lập tác vụ "scene" theo mẫu chữ (`server/src/main/resources/mock-ai/scene.json`); kịch bản F-GEO-111, F-GEO-112 đặt qua móc `/test-hooks/ai {scenario}`. Đề lạ lưu `solid_kind` theo gợi ý của AI (enum chưa có bát diện) | ARCH · khi nối AI thật |
| QF-E2-D05 | Note | US-GEO-202-SC2, 201-SC2 ("đo bằng công cụ Đo dài") | Công cụ Đo dài đầy đủ thuộc E3 | Khung hình tạm của E2 có Đo dài tối giản (chọn hai điểm); E3 thay bằng bộ xem thật | E3 |
| QF-E2-D06 | Note | D-GEO-109 | Giá trị mặc định của tham số hình mẫu chưa có nguồn | V20 đặt: chóp a=2, h=3; lăng trụ a=2, h=3; hộp 3×2×2; lập phương 2; nón r=2, h=3; trụ r=2, h=3; cầu r=2 | PO · khi có nguồn |
| QF-E2-D07 | Note | US-GEO-202-SC4, 203-SC6 (iPhone) | Playwright chưa có dự án iPhone | Test mở ngữ cảnh iPhone 13 (khung nhìn, cảm ứng) trên trình duyệt của dự án đang chạy | QC · ghi nhận |
| QF-E2-D08 | Fixed (QF-E1-D02) | TC-GEO-0118 | Phần "một lượt dựng hình tự học" chờ E2 | Đã thêm vào test TC-GEO-0118 (dựng F-GEO-060 ở màn Tự học, kiểm cả phản hồi đó không chứa khóa) | E2 · xong |
