Phiên bản: v0.2 · 2026-11-26
# S3-E1 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (TẠM) để việc không dừng; chủ sản phẩm chỉ cần sửa chỗ không đồng ý ở gate 16 (T-261126-16). Sau khi chốt, ghi vào `PO-DECISIONS.md` (ngoài phạm vi của task này) và cập nhật AC theo mã QA.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E1-01 | Mã người dùng và mã lớp: bản mẫu hiện hai độ dài (thẻ `HS-4P9T`, hộp thoại `HS-4P9T-37WD`). Dùng độ dài nào? | `GV-XXXX-XXXX` và `HS-XXXX-XXXX` (4+4 ký tự), mã lớp `HK-XXXX`; sửa bản mẫu cho khớp. TẠM · vừa · DQ-16, ảnh `c3-07-duyet-dat-lai-mk.png` | Mọi AC có mã; F-S3-020… | Designer, PO |
| QA-S3E1-02 | `ClassMembership` cần trạng thái "Bị từ chối" (bản mẫu có nút Từ chối và màn "bị từ chối"; story map chỉ có chờ, đang học, đã rời)? | Thêm trạng thái Bị từ chối; em đã rời hoặc bị từ chối xin lại thì tạo Chờ duyệt mới. TẠM · vừa · DQ-27, STAGES Ch3 §6 C3-07 | US-S3E1-02 SC5; US-S3E1-03 SC3 | PO |
| QA-S3E1-03 | Định dạng năm học và mã lớp: ràng buộc `YYYY–YYYY` năm sau bằng năm trước cộng 1; mã lớp bỏ ký tự 0, O, 1, I? | Có, như BR-S3E1-02, 03. TẠM · vừa · DQ-23 | US-S3E1-01 FC3 | BA, ARCH |
| QA-S3E1-04 | Đối tượng ngoài quan hệ trả NOT_FOUND hay FORBIDDEN? | NOT_FOUND để không lộ đối tượng tồn tại; sai vai FORBIDDEN. TẠM · cao · an toàn dữ liệu trẻ em | Mọi AC phân quyền của S3 | PO, ARCH |
| QA-S3E1-05 | Giới hạn số lớp Đang dạy mỗi giáo viên? | 20 lớp; lớp lưu trữ không tính. TẠM · thấp · chống lạm dụng, PO-63 | US-S3E1-01 FC5; US-S3E6-02 FC6 | PO |
| QA-S3E1-06 | Câu chữ MSG-S3-01…08 do ai duyệt? | BA đề xuất, Designer duyệt ở vòng sau. TẠM · cao | Mọi AC dẫn MSG-S3- | Designer |
| QA-S3E1-07 | Ngưỡng dò mã lớp: 10 lần sai/15 phút theo tài khoản học sinh (theo mẫu PO-53)? | Giữ 10/15; đếm theo tài khoản. TẠM · vừa · PO-53, D-GEO-185 | US-S3E1-02 FC4, FC5 | PO, ARCH |
| QA-S3E1-08 | Khi tắt "Giáo viên duyệt từng em", học sinh vào thẳng Đang học (DQ-23)? | Có. TẠM · vừa · DQ-23 | US-S3E1-02 SC4 | PO |
| QA-S3E1-09 | Duyệt, từ chối, cho rời có cần AuditEvent không (STAGES chỉ nêu mở sớm, đổi giờ, đặt lại mật khẩu)? | Không ở chặng 3; đề xuất owner cân nhắc cho "cho rời lớp". TẠM · vừa · Ch3 §7 | US-S3E1-03 | PO |
| QA-S3E1-10 | Định dạng mật khẩu tạm? | 8 ký tự chữ thường và số, bỏ ký tự dễ nhầm, hiện nhóm 3-3-2. TẠM · vừa · DQ-26 | US-S3E1-04 SC1 | Designer, ARCH |
| QA-S3E1-11 | Mật khẩu tạm hết hạn sau bao lâu? | 24 giờ. TẠM · thấp · chưa có nguồn | US-S3E1-04 FC6 | PO |
| QA-S3E1-12 | Giới hạn đặt lại mật khẩu mỗi giáo viên? | 10 lần/giờ. TẠM · thấp · chống lạm dụng | US-S3E1-04 FC7; US-S3E5-01 FC8 | PO |
| QA-S3E1-13 | Em thuộc hai lớp của hai giáo viên: cả hai đặt lại được mật khẩu? | Có (mỗi giáo viên có quan hệ); rủi ro ảnh hưởng em ở lớp kia, ghi cho owner. TẠM · thấp · Ch3 §7 | US-S3E1-04 SC4 | PO |
| QA-S3E1-14 | Story map chưa có story riêng cho quyền theo quan hệ ở S3 (quy tắc cắt: một story xuyên suốt cho quyền). Thêm US-S3E1-05? | Thêm US-S3E1-05 vào S3-E1 (xuyên suốt, P0). TẠM · cao · story map, brd-delta §4 | Cả epic E1 | PO |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Thêm story `US-S3E1-05` Quyền theo quan hệ (xuyên suốt, P0) vào S3-E1; story map hiện 4 story | QA-S3E1-14 |
| Thêm trạng thái `Bị từ chối` cho `ClassMembership` | QA-S3E1-02 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 14 câu hỏi và 2 đề xuất của S3-E1 | runner-T-261126-14 |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
