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
