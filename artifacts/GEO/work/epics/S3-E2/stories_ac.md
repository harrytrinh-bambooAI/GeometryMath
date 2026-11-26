Phiên bản: v0.2 · 2026-11-26
# S3-E2 — Bộ bài, lịch mở lời giải, Bài của em theo bộ: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA. Nguồn: `work/s23/story-map-s23.md` (R2 khóa, 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Ch3 (22/11/2026), `Design/s23/DQ.md` và ảnh `c3-*.png` (26/11/2026), `PO-DECISIONS.md` PO-53, PO-61…65. Khi nguồn mâu thuẫn thì PO-61 thắng STAGES.
> **Trạng thái: ready for R3** (chờ owner ở gate 16, T-261126-16). Mức epic: **P0**. Câu hỏi mở và đề xuất đổi story map ở `questions.md` cùng thư mục.

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S3En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, với giá trị cụ thể của dữ liệu mẫu bên dưới. `SC` = kịch bản chính, `FC` = lỗi, biên hoặc ngoại lệ. Mã chỉ cho biết **chỗ** một điều được ghi; câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC chặng 3** (kế thừa MVP, nguồn: `epics/E1/stories_ac.md` mục 0; PO-61…65):
- Mọi mốc giờ lấy từ giờ máy chủ (T0); giờ của thiết bị không được tin. API dùng ISO-8601 UTC; màn hình hiện `HH:mm, dd/MM/yyyy` giờ Việt Nam.
- Lỗi của máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định: VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN (sai vai), NOT_FOUND (đối tượng không thuộc quan hệ của người gọi; không tiết lộ đối tượng có tồn tại), CONFLICT (xung đột trạng thái), RATE_LIMITED (kèm `Retry-After`), INVALID_CURSOR. Chọn NOT_FOUND thay cho FORBIDDEN với đối tượng ngoài quan hệ là TẠM, QA-S3E1-04.
- Quyền theo quan hệ, không theo vai đơn thuần: giáo viên chỉ thấy và tác động lên lớp mình tạo cùng học sinh Đang học của lớp đó; học sinh chỉ thấy bài và dữ liệu của chính mình ở lớp mình Đang học, không thấy kết quả của bạn; hết quan hệ (Đã rời, lớp rời) thì hết quyền. Mã lớp và mã người dùng lấy từ thẻ xác thực hoặc đường dẫn nhưng luôn kiểm quan hệ ở máy chủ (BR-S3E1-50).
- Chỉ thu và trả về các trường trong danh sách được phép (US-S3E5-03): tên hiển thị, mã người dùng, vai, lớp, bài làm, cờ đã đọc thông báo riêng tư cùng phiên bản văn bản. Không email, điện thoại, ngày sinh, trường, ảnh (PO-61, PO-64). "Dữ liệu trả về" nghĩa là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Phản hồi chứa dữ liệu học sinh, lời giải hoặc trạng thái khóa mang tiêu đề `Cache-Control: no-store` (kế thừa D-GEO-145).
- Thiết bị: giáo viên dùng web trên máy tính, điện thoại chỉ để liếc và quyết nhanh; học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA), PO-65.
- AI giữ giả lập trong CI (D-GEO-047): không có kỳ vọng nào về chất lượng câu chữ AI thật; không gọi AI thật.
- AC mang chữ PROVISIONAL dựng trên một giả định chưa được chủ đầu tư xác nhận; mã `QA-S3En-nn` cạnh đó cho biết giả định nào, câu trả lời mặc định ở `questions.md`. Ngưỡng và định dạng mã là đề xuất.

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-S3En-nn` · `-SCn` / `-FCn` | story (En = epic S3-En, nn = số story) · tiêu chí thành công / thất bại thứ n | tệp này; khóa bởi `work/s23/story-map-s23.md` |
| `BR-S3En-nn` | quy tắc nghiệp vụ của story (nguồn dẫn kèm) | tệp này |
| `TC-S3En-nn` | test case của epic S3-En | cột No của `test_cases.md` |
| `F-S3-nnn` | hàng dữ liệu mẫu chung của chặng 3 | mục "Dữ liệu mẫu" của tệp này |
| `QA-S3En-nn` | câu hỏi mở của epic, kèm mặc định đang dùng (TẠM) | `questions.md` cùng thư mục |
| `PO-nn` | dòng của sổ quyết định gốc: PO-53, PO-61…65 | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi và mặc định của thiết kế chặng 2–3 | `Design/s23/DQ.md` |
| `M-S3-nn` | chỉ số chặng 3 (M-S3-01…06) | `work/s23/brd-delta-s23.md` §1 |
| `C3-nn` | mã màn hình chặng 3 | `STAGES-AFTER-MVP.md` Ch3 §6; ảnh ở `Design/s23/c3-*.png` |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (ví dụ D-GEO-047 AI giả lập, D-GEO-185 khóa theo tài khoản) | `work/03_decisions.md` |
| `GV-…` · `HS-…` · `HK-…` · `RC-…` | mã người dùng giáo viên · học sinh · mã lớp · mã khôi phục (định dạng TẠM, DQ-16, DQ-23) | `Design/s23/DQ.md` |
| `release_at` · `due_at` | giờ mở lời giải · hạn làm (chỉ là nhãn, không khóa bài) | `STAGES-AFTER-MVP.md` Ch3 §7, §8 |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có `release_at` (khóa) · giờ máy chủ < `release_at` (khóa) · giờ máy chủ ≥ `release_at` (được xem) | MVP, M-GEO-13 |
| Chờ duyệt · Đang học · Đã rời · Bị từ chối | trạng thái `ClassMembership` (Bị từ chối là đề xuất của BA, QA-S3E1-02) | tệp này |
| Đúng · Xem lại · Đang làm · Chưa làm | bốn trạng thái ô của bảng lớp × bài (nghĩa ở US-S3E3-01) | `Design/s23/c3-04-ket-qua-lop.png`, DQ-21 |
| Bài làm | một học sinh Đang học có ít nhất một tương tác (gửi đáp số, xin gợi ý hoặc hỏi) trên một đề của bộ; tính theo số học sinh khác nhau | US-S3E3-03, QA-S3E3-02 |
| T0 · t | giờ máy chủ lúc bắt đầu ca kiểm · giờ máy chủ tại thời điểm xét | mọi mốc giờ là tương đối |
| `CL-FN-nn` · `CL-UI-nn` · `CL-X-nn` | nhóm kiểm tra chức năng · giao diện và thiết bị · xuyên suốt (ma trận quyền, trường dữ liệu, tải) | `test_design.md` cùng thư mục |
| PROVISIONAL · ENG · X | kỳ vọng theo giả định chưa xác nhận (kèm mã QA-) · case cần công cụ kỹ thuật · case xuyên suốt | cột Remark |
| `MSG-S3-nn` | thông điệp hiện cho người dùng của chặng 3, câu chữ ở bảng "Thông điệp" của tệp này | tệp này |

### Dữ liệu mẫu chung của chặng 3

| Mã | Nội dung (dữ liệu mẫu chung của chặng 3; giờ Việt Nam) |
|---|---|
| `F-S3-001` | Giáo viên Lan · mã người dùng `GV-7K2M-9QPA` · vai Giáo viên · chủ lớp 11A1 và 11A2 |
| `F-S3-002` | Giáo viên Khôi · `GV-4D8R-2WNX` · chủ lớp 10B |
| `F-S3-010` | Lớp 11A1 · Lớp 11 · năm học 2026–2027 · mã lớp `HK-4P7Q` · Đang dạy · 8 em Đang học, 3 em Chờ duyệt, 1 em Đã rời |
| `F-S3-011` | Lớp 11A2 của cô Lan · mã lớp `HK-8M3C` · 3 em Đang học |
| `F-S3-012` | Lớp 10B của thầy Khôi · mã lớp `HK-5T9X` · 4 em Đang học |
| `F-S3-020…027` | Học sinh Đang học của 11A1: Bình An `HS-4P9T-37WD`, Chi Mai `HS-8L2K-77AB`, Đức Huy `HS-3M7Q-85XN`, Gia Bảo `HS-9D4W-67CE`, Hà My `HS-2X6R-48PL`, Khánh Linh `HS-7T7B-98QZ`, Minh Quân `HS-5N8C-24TR`, Ngọc Anh `HS-6V3F-78HM` |
| `F-S3-028` | Văn Tú `HS-3H6J-72KP` · Đã rời 11A1 (còn dữ liệu bài làm cũ) |
| `F-S3-030…032` | Chờ duyệt ở 11A1: Hoàng Nam `HS-7Q8Z-48DF`, Thu Hà `HS-7C5J-63LS`, Hoàng Nam thứ hai `HS-9B2L-85VG` (trùng tên với em đầu) |
| `F-S3-040` | Lớp 10B: Mi Na `HS-2A5F-77BC`, Bình An thứ hai `HS-6Z7N-52YT` (trùng tên với Bình An của 11A1), hai em khác; Chi Mai `HS-8L2K-77AB` cũng Đang học 10B (em thuộc hai lớp của hai giáo viên) |
| `F-S3-041` | Lớp 11A2: Quốc Bảo `HS-3C9D-77AA`, Thảo Vy `HS-8F2E-33BB`, Tuấn Kiệt `HS-7G4H-99CC` |
| `F-S3-050` | Bộ "Bộ bài tuần 15" của 11A1 · Đã giao · hạn làm T6 04/12/2026 20:00 · giờ mở lời giải 06/12/2026 08:00 · bốn đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích |
| `F-S3-051` | Bảng trạng thái 11A1 × bộ tuần 15 (Đề 1, 2, 3, 4): An Đúng, Xem lại, Đúng, Xem lại · Mai Đúng, Xem lại, Xem lại, Chưa làm · Huy Đúng, Đúng, Xem lại, Đúng · Bảo Xem lại, Xem lại, Đang làm, Chưa làm · My Đúng, Đúng, Đúng, Đúng · Linh Chưa làm bốn đề · Quân Đúng, Xem lại, Xem lại, Xem lại · Ngọc Anh Đúng, Đúng, Xem lại, Đang làm |
| `F-S3-052` | Bộ "Bộ bài tuần 16" của 11A1 · Nháp · chưa đặt hạn, chưa đặt giờ mở lời giải |
| `F-S3-053` | Bộ tuần 15 của 11A2 · Đã giao · Đề 3 có ba bài làm (Quốc Bảo, Thảo Vy, Tuấn Kiệt) |

---


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E2-08)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-21 | Banner đỏ | "Giờ mở lời giải sớm hơn hạn làm. Em sẽ thấy lời giải trước khi hết hạn." | — |
| MSG-S3-22 | Banner vàng | "Bộ có đề đang ở trạng thái Cần xem lại. Thầy cô xem lại trước khi giao." | — |
| MSG-S3-23 | Inline | "Bộ bài cần từ 1 đến 20 đề." | VALIDATION_FAILED |
| MSG-S3-24 | Inline | "Đề này đã có trong bộ." | VALIDATION_FAILED |
| MSG-S3-25 | Inline | "Chưa chọn lớp để giao." | VALIDATION_FAILED |
| MSG-S3-26 | Inline | "Lời giải đã mở, không đổi giờ được." | CONFLICT |
| MSG-S3-27 | Page state | "Chưa có bài nào. Khi thầy cô giao bài, bài sẽ hiện ở đây." | — |
| MSG-S3-28 | Chip | "Quá hạn" (chỉ là nhãn, em vẫn làm được) | — |

## US-S3E2-01 — Soạn bộ bài: chọn đề, thứ tự, hạn làm (chỉ là nhãn), xem trước như học sinh
- **Story:** Là giáo viên, tôi muốn gom nhiều đề thành một bộ bài, sắp thứ tự, đặt hạn làm và giao cho một lớp, để giao cả tuần một lần. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E2-01; STAGES Ch3 §5 luồng B, §7 ý 4; DQ-19, DQ-20; ảnh `c3-03-bo-bai.png`
- **Góc nhìn người vận hành (M18):** cô Lan chọn 4 đề từ Kho đề, kéo thả sắp thứ tự, đặt hạn T6 20:00, xem trước như học sinh rồi bấm Giao. Ca đáng kể: bấm Giao hai lần, quên chọn lớp, chọn đề Của tôi của người khác, hạn nằm trong quá khứ.
- **Phụ thuộc:** US-S3E1-01 (lớp), Kho đề MVP và S2-E6; US-S3E2-02 (lịch mở).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E2-01 | Bộ bài (`AssignmentSet`) gồm 1 đến 20 đề (`AssignmentItem`) có thứ tự, thuộc đúng một lớp của giáo viên tạo; giao cho lớp khác là tạo bộ mới. | Ch3 §8; QA-S3E2-01 |
| BR-S3E2-07 | **Một mô hình "bộ bài":** bài luyện của S2-E8 (BR-S2E8-32) chính là một `AssignmentSet`; chặng 2 giao cho lớp mẫu 11A1 chuyển từ MVP (BR-S3E1-16). Chặng 3 thêm lớp thật, lịch mở, bảng lớp trên cùng mô hình, giữ nguyên bộ và kết quả đã có từ chặng 2 | PO-66 (G1); RV_s23 B-1 |
| BR-S3E2-02 | Vòng đời bộ: Nháp, Đã giao, Đã đóng, Lưu trữ (Đã đóng và Lưu trữ ở US-S3E2-04, PO-66 G5). Học sinh chỉ thấy bộ Đã giao. | Ch3 §8 |
| BR-S3E2-03 | Hạn làm `due_at` chỉ là nhãn; quá hạn vẫn làm được; không có chức năng khóa theo hạn. | Ch3 §7 ý 4 |
| BR-S3E2-04 | Một đề chỉ xuất hiện một lần trong một bộ; giáo viên chỉ chọn đề Kho chung hoặc đề Của tôi của chính mình. | PO-63; S2-E6 |
| BR-S3E2-05 | Xem trước dựng đúng màn học sinh nhưng không ghi tiến độ, không tạo Notification và không tính "bài làm". | Ch3 §5 luồng B bước 2 |
| BR-S3E2-06 | Giao là idempotent theo khóa yêu cầu: bấm lặp chỉ tạo một lần giao và một đợt Notification. | CL-FN-10 |

- **DoR:** Ready for R3; giả định QA-S3E2-01, QA-S3E2-02 đã chốt PO-66 (G12).

### Tiêu chí chấp nhận

**US-S3E2-01-SC1 — Tạo bộ nháp từ bốn đề và lưu** · Nguồn: Ch3 §5 luồng B bước 1
- Given: cô Lan ở Kho đề, đã tick 4 đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích (F-S3-050)
- When: bấm "Tạo bộ bài", rồi đặt tên "Bộ bài tuần 16", chọn lớp 11A1, hạn làm "11/12/2026 20:00", rồi bấm "Lưu nháp"
- Then: bộ "Bộ bài tuần 16" ở trạng thái Nháp với 4 đề theo thứ tự đã tick; học sinh 11A1 chưa thấy bộ này; ở tổng quan giáo viên chưa có dòng mới
- Dữ liệu nhập: "Bộ bài tuần 16" ; `HK-4P7Q` lớp 11A1 ; "11/12/2026 20:00"


**US-S3E2-01-SC2 — Kéo thả đổi thứ tự đề, thứ tự lưu lại** · Nguồn: Ch3 §5 luồng B bước 1
- Given: bộ nháp "Bộ bài tuần 16" có 4 đề theo thứ tự 1, 2, 3, 4 (F-S3-052)
- When: kéo Đề 4 lên đầu, rồi bấm "Lưu nháp", rồi tải lại trang
- Then: thứ tự sau khi tải lại là Đề 4, Đề 1, Đề 2, Đề 3; khi giao, học sinh thấy đúng thứ tự này


**US-S3E2-01-SC3 — Xem trước như học sinh không ghi gì** · Nguồn: BR-S3E2-05
- Given: bộ nháp "Bộ bài tuần 16" (F-S3-052); 11A1 đang có 8 em Đang học
- When: bấm "Xem trước như học sinh", rồi làm thử Đề 1 đến hết (gõ đáp số, xin một gợi ý)
- Then: màn xem trước giống màn Bài của em; ở Kết quả lớp không có ô, không có bài làm nào mới; không Notification nào được tạo; lời giải vẫn khóa theo lịch


**US-S3E2-01-SC4 — Giao bộ: học sinh Đang học thấy, em Chờ duyệt không thấy** · Nguồn: BR-S3E2-02
- Given: bộ nháp "Bộ bài tuần 16" đã chọn lớp 11A1 và hạn làm (F-S3-052); 8 em Đang học, 3 em Chờ duyệt (F-S3-010)
- When: bấm "Giao", rồi xác nhận
- Then: bộ chuyển Đã giao; 8 em Đang học thấy "Bộ bài tuần 16" ở Bài của em; 3 em Chờ duyệt (F-S3-030…032) không thấy; tổng quan giáo viên thêm một dòng bài đang giao


**US-S3E2-01-SC5 — Quá hạn chỉ là nhãn, em vẫn làm được** · Nguồn: BR-S3E2-03
- Given: bộ tuần 15 có hạn làm 04/12/2026 20:00 và giờ máy chủ là 04/12/2026 20:01 (F-S3-050)
- When: Bình An mở Đề 1 và gửi đáp số
- Then: bài hiện chip MSG-S3-28 "Quá hạn"; đáp số được nhận và tính vào tiến độ như bình thường; không có chức năng nào khóa bài vì quá hạn


**US-S3E2-01-SC6 — Bộ đúng 20 đề được nhận** · Nguồn: BR-S3E2-01
- Given: cô Lan có 20 đề khác nhau để chọn
- When: tạo bộ gồm đúng 20 đề, rồi bấm "Giao"
- Then: bộ được giao; học sinh thấy 20 đề đúng thứ tự
- Dữ liệu nhập: 20 đề


**US-S3E2-01-SC7 — Bài luyện giao ở chặng 2 hiện như một bộ bài ở chặng 3** · Nguồn: BR-S3E2-07; BR-S2E8-32; PO-66 (G1)
- Given: Bài luyện BL-1 (5 câu) đã giao cho lớp mẫu 11A1 ở chặng 2, An đã làm câu 1 đúng
- When: Giáo viên chủ lớp 11A1 mở "Bài đang giao" ở chặng 3
- Then: Bộ BL-1 hiện như một bộ Đã giao của 11A1 với 5 đề; kết quả và số lượt đã làm của An còn nguyên; bảng lớp (US-S3E3-01) đọc được ô của An; không có bản sao hay bảng bài luyện thứ hai

**US-S3E2-01-FC1 — Bộ không có đề không giao được** · Nguồn: BR-S3E2-01
- Given: bộ nháp chưa có đề nào, đã chọn lớp 11A1
- When: bấm "Giao"
- Then: hiện MSG-S3-23; máy chủ trả VALIDATION_FAILED nếu gọi thẳng; bộ vẫn Nháp


**US-S3E2-01-FC2 — Bộ 21 đề bị chặn** · Nguồn: BR-S3E2-01 · PROVISIONAL QA-S3E2-01
- Given: cô Lan có 21 đề khác nhau để chọn
- When: thêm đề thứ 21 vào bộ đã có 20 đề
- Then: hiện MSG-S3-23; VALIDATION_FAILED nếu gọi thẳng; bộ vẫn 20 đề
- Dữ liệu nhập: đề thứ 21


**US-S3E2-01-FC3 — Thêm trùng một đề vào bộ** · Nguồn: BR-S3E2-04
- Given: bộ nháp "Bộ bài tuần 16" đã có Đề 1 SC-đáy (F-S3-052)
- When: thêm lại Đề 1 SC-đáy
- Then: hiện MSG-S3-24; bộ vẫn một lần Đề 1


**US-S3E2-01-FC4 — Chưa chọn lớp thì nút Giao chờ chọn** · Nguồn: DQ-19; C3-03
- Given: bộ nháp có 4 đề, chưa chọn lớp
- When: nhìn nút Giao, rồi gọi thẳng yêu cầu giao
- Then: nút "Giao" không bấm được và có chú thích MSG-S3-25; yêu cầu gọi thẳng trả VALIDATION_FAILED; bộ vẫn Nháp


**US-S3E2-01-FC5 — Chọn lớp của giáo viên khác** · Nguồn: BR-S3E2-01; PO-61
- Given: cô Lan tạo bộ nháp có 4 đề
- When: gọi thẳng yêu cầu tạo bộ với lớp 10B của thầy Khôi (F-S3-012)
- Then: máy chủ trả NOT_FOUND; không tạo bộ; thân lỗi không chứa tên lớp 10B


**US-S3E2-01-FC6 — Chọn đề Của tôi của giáo viên khác** · Nguồn: BR-S3E2-04; PO-63
- Given: thầy Khôi có một đề "Của tôi" riêng chưa vào Kho chung (F-S3-002)
- When: cô Lan gọi thẳng yêu cầu thêm đề đó vào bộ của cô
- Then: máy chủ trả NOT_FOUND; bộ không đổi


**US-S3E2-01-FC7 — Hạn làm trong quá khứ khi giao** · Nguồn: BR-S3E2-03 · PROVISIONAL QA-S3E2-02
- Given: bộ nháp có hạn làm đặt ở quá khứ so với giờ máy chủ
- When: bấm "Giao"
- Then: hiện cảnh báo "hạn làm đã qua" và cho tiếp tục nếu giáo viên xác nhận; sau khi giao học sinh thấy chip "Quá hạn" ngay


**US-S3E2-01-FC8 — Bấm Giao hai lần chỉ giao một lần** · Nguồn: BR-S3E2-06
- Given: bộ nháp "Bộ bài tuần 16" sẵn sàng giao (F-S3-052)
- When: gửi hai yêu cầu Giao liên tiếp cùng khóa yêu cầu
- Then: chỉ có một bộ Đã giao; mỗi học sinh Đang học nhận đúng một Notification "bài mới"; tổng quan giáo viên có một dòng


## US-S3E2-02 — Lịch mở lời giải theo bộ hoặc từng bài; cảnh báo giờ mở sớm hơn hạn
- **Story:** Là giáo viên, tôi muốn đặt giờ mở lời giải cho cả bộ hoặc từng bài và được cảnh báo khi giờ mở sớm hơn hạn làm, để học sinh không thấy lời giải trước khi làm xong. · Loại Logic · P0 · Độ khó 4
- **Nguồn:** story map US-S3E2-02; STAGES Ch3 §5 luồng B ngoại lệ, §7; PO-25; DQ-19; brd-delta §4 "Một cổng công bố"; ảnh `c3-03-bo-bai.png`
- **Góc nhìn người vận hành (M18):** cô Lan đặt giờ mở lời giải là 08:00 Chủ nhật cho cả bộ, riêng Đề 3 mở muộn hơn. Ca đáng kể: giờ mở trùng đúng hạn, sớm hơn hạn một phút, học sinh đổi đồng hồ máy, lời giải lọt qua kênh phụ (gợi ý, thông báo, trình chiếu, bộ nhớ đệm).
- **Phụ thuộc:** US-S3E2-01; kế thừa cổng `release_at` của MVP (M-GEO-13); US-S3E4-02 (Mở ngay, Đổi giờ), US-S3E5-01 (AuditEvent).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E2-10 | Mỗi bộ có một `release_at` chung; mỗi đề có thể có `release_at` riêng ghi đè; rỗng nghĩa là Chưa đặt giờ và lời giải khóa. | Ch3 §8 |
| BR-S3E2-11 | Lời giải mở khi giờ máy chủ t ≥ `release_at` (bằng đúng giờ mở là đã mở); t < `release_at` là Sắp mở và khóa; chỉ giờ máy chủ có giá trị. | M-GEO-13 |
| BR-S3E2-12 | Khi khóa, mọi kênh không chứa đáp số, bước giải hay gợi ý giải đang khóa: danh sách, chi tiết, gợi ý, trình chiếu, thông báo, tệp xuất, bộ nhớ đệm, phản hồi lỗi. Với người gọi là học sinh áp dụng cho mọi kênh; riêng Trình chiếu là màn của giáo viên (học sinh không vào được, BR-S2E7-05): giáo viên chỉ xem lời giải đang khóa sau khi xác nhận hộp hỏi ở US-S2E7-03 và máy học sinh vẫn khóa. | brd-delta §4; D-GEO-145; BR-S2E7-11 |
| BR-S3E2-13 | Giờ mở sớm hơn `due_at` (chênh từ 1 phút trở lên) thì hiện cảnh báo MSG-S3-21 ngay trong màn soạn kèm hai cách sửa; giáo viên xác nhận mới giao được. | DQ-19; Ch3 §5 |
| BR-S3E2-14 | Đổi giờ khi lời giải đã mở là CONFLICT; hai lần đổi chồng nhau là CONFLICT cho lần đến sau. | MVP E1 glossary |
| BR-S3E2-15 | Chỉ chủ lớp đặt hoặc đổi lịch; học sinh không bao giờ đặt được. | PO-61 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E2-03.

### Tiêu chí chấp nhận

**US-S3E2-02-SC1 — Đặt giờ mở cho cả bộ** · Nguồn: BR-S3E2-10
- Given: bộ nháp "Bộ bài tuần 16" có 4 đề, hạn làm 11/12/2026 20:00 (F-S3-052)
- When: ở "Lịch mở lời giải" chọn "Cả bộ", rồi đặt "13/12/2026 08:00", rồi bấm "Lưu nháp"
- Then: cả 4 đề cùng có giờ mở lời giải 13/12/2026 08:00; không hiện cảnh báo vì giờ mở sau hạn làm


**US-S3E2-02-SC2 — Giờ mở riêng của một đề ghi đè giờ của bộ** · Nguồn: BR-S3E2-10
- Given: bộ nháp có giờ mở chung 13/12/2026 08:00 (F-S3-052)
- When: chọn "Từng bài", rồi đặt Đề 3 mở "14/12/2026 08:00", rồi lưu
- Then: Đề 1, 2, 4 mở lúc 13/12/2026 08:00; Đề 3 mở lúc 14/12/2026 08:00; sau khi giao, ở Bài của em mỗi đề hiện đúng giờ mở của mình


**US-S3E2-02-SC3 — Biên giờ mở: đúng giờ mở là đã mở** · Nguồn: BR-S3E2-11
- Given: bộ tuần 15 có `release_at` 06/12/2026 08:00 giờ Việt Nam (F-S3-050); Bình An Đang học
- When: lấy chi tiết lời giải Đề 1 ở giờ máy chủ 07:59:59, rồi ở đúng 08:00:00, rồi ở 08:00:01
- Then: lúc 07:59:59 trả trạng thái Sắp mở và thân phản hồi không có lời giải; lúc 08:00:00 và 08:00:01 trả lời giải đầy đủ; mọi thân trả lời có `Cache-Control: no-store`


**US-S3E2-02-SC4 — Bộ chưa đặt giờ thì lời giải khóa** · Nguồn: BR-S3E2-10
- Given: bộ "Bộ bài tuần 16" đã giao nhưng chưa đặt giờ mở (F-S3-052)
- When: Bình An lấy chi tiết lời giải của từng đề
- Then: trạng thái Chưa đặt giờ; không thân phản hồi nào chứa đáp số, bước giải hay gợi ý giải; màn hiện "Chưa có giờ mở lời giải"


**US-S3E2-02-SC5 — Bộ câu thử moi đáp số không lấy được lời giải đang khóa** · Nguồn: BR-S3E2-12; brd-delta §4
- Given: bộ tuần 15 ở trạng thái Sắp mở (giờ máy chủ trước 06/12/2026 08:00) với đáp số mẫu của Đề 1 là giá trị đã biết (F-S3-050)
- When: chạy bộ câu thử moi đáp số của học sinh qua mọi kênh: danh sách bộ, chi tiết đề, gợi ý mức 1, tìm kiếm, thông báo, bộ nhớ đệm trình duyệt, phản hồi lỗi, đổi tiêu đề ngôn ngữ, gọi lại với `If-None-Match`
- Then: không phản hồi nào chứa đáp số mẫu hay bước giải ở bất kỳ dạng viết tương đương nào; không có phản hồi nào bị lưu bộ nhớ đệm dùng chung


**US-S3E2-02-FC1 — Giờ mở sớm hơn hạn làm thì cảnh báo** · Nguồn: BR-S3E2-13; DQ-19
- Given: bộ nháp có hạn làm 11/12/2026 20:00 (F-S3-052)
- When: đặt giờ mở "10/12/2026 08:00" cho cả bộ
- Then: cột cài đặt hiện banner đỏ MSG-S3-21 kèm hai cách sửa "Dời giờ mở sau hạn" và "Giữ giờ mở sớm"; nút "Giao" chờ giáo viên chọn một trong hai; không tự chặn giao


**US-S3E2-02-FC2 — Biên cảnh báo: giờ mở bằng đúng hạn thì không cảnh báo** · Nguồn: BR-S3E2-13 · PROVISIONAL QA-S3E2-03
- Given: bộ nháp có hạn làm 11/12/2026 20:00 (F-S3-052)
- When: đặt giờ mở "11/12/2026 20:00" rồi đổi thành "11/12/2026 19:59"
- Then: với 20:00 không có cảnh báo; với 19:59 hiện MSG-S3-21


**US-S3E2-02-FC3 — Giáo viên chọn "Giữ giờ mở sớm" thì giao được và có ghi nhận** · Nguồn: DQ-19
- Given: bộ nháp đang có banner MSG-S3-21 (F-S3-052)
- When: bấm "Giữ giờ mở sớm", rồi bấm "Giao"
- Then: bộ được giao với giờ mở đã đặt; màn Bài của em hiện đúng giờ mở sớm đó; không có AuditEvent vì đây là lần đặt đầu của bản nháp (QA-S3E5-03)


**US-S3E2-02-FC4 — Học sinh đổi đồng hồ máy không mở được lời giải** · Nguồn: BR-S3E2-11
- Given: bộ tuần 15 ở trạng thái Sắp mở; đồng hồ thiết bị của Bình An chỉnh sang 10/12/2026 (F-S3-050)
- When: Bình An lấy chi tiết lời giải kèm tiêu đề giờ thiết bị
- Then: máy chủ vẫn trả Sắp mở và không có lời giải; giờ thiết bị bị bỏ qua


**US-S3E2-02-FC5 — Đổi giờ khi lời giải đã mở** · Nguồn: BR-S3E2-14
- Given: bộ tuần 15 đã mở lời giải từ 06/12/2026 08:00; giờ máy chủ là 06/12/2026 09:00 (F-S3-050)
- When: cô Lan gửi yêu cầu đổi giờ mở sang 07/12/2026 08:00
- Then: máy chủ trả CONFLICT kèm MSG-S3-26; giờ mở không đổi; không AuditEvent đổi giờ thành công


**US-S3E2-02-FC6 — Hai lần đổi giờ chồng nhau** · Nguồn: BR-S3E2-14
- Given: bộ tuần 15 ở trạng thái Sắp mở; cô Lan mở hai tab cùng đọc phiên bản lịch số 3 (F-S3-050)
- When: tab một lưu giờ "06/12/2026 10:00", rồi tab hai lưu giờ "06/12/2026 12:00" với phiên bản số 3
- Then: lần của tab hai bị CONFLICT; giờ mở là 06/12/2026 10:00; tab hai thấy giờ hiện hành sau khi tải lại


**US-S3E2-02-FC7 — Giáo viên khác không đặt được lịch của lớp cô Lan** · Nguồn: BR-S3E2-15
- Given: bộ tuần 15 của 11A1 (F-S3-050)
- When: thầy Khôi gọi thẳng yêu cầu đổi giờ mở của bộ đó
- Then: máy chủ trả NOT_FOUND; giờ mở không đổi


**US-S3E2-02-FC8 — Học sinh không đặt được lịch** · Nguồn: BR-S3E2-15
- Given: Bình An Đang học 11A1 (F-S3-020)
- When: Bình An gọi thẳng yêu cầu đặt giờ mở của bộ tuần 15
- Then: máy chủ trả FORBIDDEN; giờ mở không đổi


**US-S3E2-02-FC9 — Giờ mở sai định dạng** · Nguồn: BR-S3E2-10
- Given: bộ nháp "Bộ bài tuần 16" (F-S3-052)
- When: gửi giờ mở "31/02/2026 08:00" rồi giờ mở rỗng kèm cờ "đặt giờ"
- Then: cả hai bị VALIDATION_FAILED với `details` chỉ ra trường giờ mở; giờ mở của bộ không đổi
- Dữ liệu nhập: "31/02/2026 08:00"


## US-S3E2-03 — Bài của em theo bộ; liệt kê đề "Cần xem lại" trước khi giao
- **Story:** Là học sinh, tôi muốn thấy bài của lớp theo từng bộ cùng hạn, tiến độ và giờ mở lời giải; là giáo viên, tôi muốn được liệt kê các đề "Cần xem lại" trước khi giao. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E2-03; STAGES Ch3 §5 luồng B ngoại lệ, §6 C3-08; DQ-20; ảnh `c3-03-bo-bai.png`; `Design/mvp-04` (Bài của em, mở rộng)
- **Góc nhìn người vận hành (M18):** Bình An mở iPad ở đầu buổi tự học, cần thấy ngay bộ nào còn bao nhiêu đề và đề nào hết hạn. Cô Lan trước khi giao muốn biết đề nào máy còn ghi "Cần xem lại". Ca đáng kể: học sinh thuộc hai lớp, bộ quá hạn, chưa có bài.
- **Phụ thuộc:** US-S3E2-01, US-S3E2-02; nhãn máy của S2-E5.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E2-20 | Bài của em nhóm theo lớp rồi theo bộ Đã giao; mỗi bộ hiện tên, hạn (nhãn), tiến độ n/N đề, giờ mở lời giải. | C3-08 |
| BR-S3E2-21 | Học sinh chỉ thấy tiến độ của chính mình, không thấy tiến độ bạn. | Ch3 §7; BR-S3E1-52 |
| BR-S3E2-22 | Khi giao bộ có đề mang nhãn máy "Cần xem lại", màn Giao liệt kê các đề đó trước; giáo viên quyết định giao hay bỏ đề; quyết định cuối thuộc giáo viên. | DQ-20; STAGES §0.2 nguyên tắc 3 |
| BR-S3E2-23 | Học sinh không thấy nhãn nội bộ "Cần xem lại" của đề. | QA-S3E2-04 |
| BR-S3E2-24 | Học sinh Chờ duyệt, Đã rời, Bị từ chối không thấy bộ mới của lớp đó. | BR-S3E1-53 |

- **DoR:** Ready for R3.

### Tiêu chí chấp nhận

**US-S3E2-03-SC1 — Bài của em hiện theo bộ với hạn, tiến độ, giờ mở** · Nguồn: BR-S3E2-20; C3-08
- Given: Bình An Đang học 11A1, đã làm 2 trên 4 đề của bộ tuần 15 (F-S3-020, F-S3-050)
- When: mở Bài của em trên iPad
- Then: có một nhóm "11A1" chứa "Bộ bài tuần 15" với hạn "T6 04/12/2026 20:00", tiến độ "2 / 4", giờ mở lời giải "06/12/2026 08:00" và trạng thái Sắp mở; bốn đề theo đúng thứ tự của giáo viên


**US-S3E2-03-SC2 — Học sinh ở hai lớp thấy hai nhóm** · Nguồn: BR-S3E2-20
- Given: Chi Mai Đang học 11A1 và 10B; mỗi lớp có một bộ Đã giao (F-S3-021, F-S3-040)
- When: mở Bài của em
- Then: có hai nhóm "11A1" và "10B", mỗi nhóm chỉ chứa bộ của lớp đó; không nhóm nào lộ tên giáo viên của lớp kia


**US-S3E2-03-SC3 — Chưa có bài** · Nguồn: C3-08
- Given: học sinh mới vào lớp 11A2, lớp chưa giao bộ nào (F-S3-041)
- When: mở Bài của em
- Then: hiện MSG-S3-27; không có nhóm nào


**US-S3E2-03-SC4 — Màn Giao liệt kê đề "Cần xem lại"** · Nguồn: BR-S3E2-22; DQ-20
- Given: bộ nháp "Bộ bài tuần 16" có Đề 3 mang nhãn máy "Cần xem lại" (F-S3-052)
- When: bấm "Giao"
- Then: trước khi giao hiện banner MSG-S3-22 và danh sách đúng một đề "Đề 3 · hai mặt phẳng" viền vàng; giáo viên chọn "Vẫn giao" hoặc "Bỏ đề khỏi bộ"; chưa chọn thì bộ chưa được giao


**US-S3E2-03-SC5 — Giáo viên chọn "Vẫn giao" thì bộ giao và đề giữ nhãn** · Nguồn: BR-S3E2-22
- Given: banner MSG-S3-22 đang hiện với Đề 3 (F-S3-052)
- When: bấm "Vẫn giao"
- Then: bộ chuyển Đã giao với đủ 4 đề; nhãn máy của Đề 3 vẫn là "Cần xem lại" ở Kho đề của giáo viên; học sinh không thấy nhãn đó (BR-S3E2-23)


**US-S3E2-03-FC1 — Học sinh Chờ duyệt không thấy bộ** · Nguồn: BR-S3E2-24
- Given: Hoàng Nam Chờ duyệt ở 11A1; bộ tuần 15 đã giao (F-S3-030, F-S3-050)
- When: Hoàng Nam mở Bài của em và gọi thẳng chi tiết bộ tuần 15
- Then: Bài của em hiện MSG-S3-27 hoặc chỉ Tự học; chi tiết bộ trả NOT_FOUND


**US-S3E2-03-FC2 — Bộ Nháp không hiện cho học sinh** · Nguồn: BR-S3E2-02
- Given: bộ "Bộ bài tuần 16" đang Nháp (F-S3-052)
- When: Bình An mở Bài của em và gọi thẳng chi tiết bộ đó
- Then: Bài của em không có bộ tuần 16; chi tiết trả NOT_FOUND


**US-S3E2-03-FC3 — Tiến độ của bạn không lộ** · Nguồn: BR-S3E2-21
- Given: Bình An Đang học 11A1; các bạn đã làm nhiều đề của bộ tuần 15 (F-S3-051)
- When: Bình An gọi Bài của em và chi tiết bộ tuần 15, soát toàn bộ thân phản hồi
- Then: thân phản hồi chỉ có tiến độ của Bình An; không có tên, mã người dùng, số lượng hay trạng thái của bạn nào; không có tổng hay xếp hạng của lớp


**US-S3E2-03-FC4 — Bộ bài hiện đúng trên iPhone màn dọc** · Nguồn: BR-S3E2-20
- Given: Bình An dùng iPhone trình duyệt web màn dọc 390×844 (F-S3-020)
- When: mở Bài của em
- Then: nhóm lớp và thẻ bộ xếp một cột; chip hạn, tiến độ, giờ mở đều đọc được không cắt chữ; không có thanh cuộn ngang

## US-S3E2-04 — Đóng bộ và lưu trữ bộ (vòng đời Đã đóng, Lưu trữ)
- **Story:** Là giáo viên, tôi muốn đóng bộ khi hết đợt làm và lưu trữ bộ cũ, để học sinh chỉ xem kết quả của bộ đã đóng và màn "Bài đang giao" không dài mãi. · Loại UI · P0 · Độ khó 2
- **Nguồn:** PO-66 (G5; QA-S3E2-07); STAGES Ch3 §8 (`AssignmentSet.status`); BR-S3E2-02
- **Góc nhìn người vận hành (M18):** cuối tuần 15 cô Lan đóng bộ tuần 15 để các em không gửi thêm đáp số, rồi lưu trữ bộ tuần 12 để dọn màn. Ca đáng kể: đóng bộ Nháp, bấm Đóng hai lần, giáo viên khác thử đóng, bộ Đã đóng mà giờ mở lời giải mới tới.
- **Phụ thuộc:** US-S3E2-01, US-S3E2-02; US-S3E4-01 ("Bài đang giao" chỉ liệt kê bộ chưa Đã đóng).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E2-30 | Chỉ chủ lớp đóng được bộ Đã giao; Đóng là một chiều, không mở lại (muốn làm tiếp thì tạo bộ mới). Bộ Nháp không đóng được. | PO-66 (G5); QA-S3E2-07 |
| BR-S3E2-31 | Bộ Đã đóng: học sinh vẫn xem kết quả của mình; không gửi, không xin gợi ý thêm (CONFLICT); lịch mở lời giải vẫn chạy; bảng lớp vẫn đọc được. | PO-66 (G5); BR-S2E8-19 |
| BR-S3E2-32 | Chỉ bộ Đã đóng mới lưu trữ được; bộ Lưu trữ rời "Bài đang giao" và danh sách chính của học sinh, nằm ở mục "Bộ đã lưu trữ" của giáo viên, vẫn xem được kết quả. | PO-66 (G5) |
| BR-S3E2-33 | Đóng và lưu trữ idempotent theo khóa yêu cầu; sai chủ lớp hoặc ngoài quan hệ trả NOT_FOUND (QA-S3E1-04). | BR-S3E2-06; PO-66 |

### Tiêu chí chấp nhận

**US-S3E2-04-SC1 — Giáo viên đóng bộ Đã giao, học sinh chỉ còn xem kết quả** · Nguồn: BR-S3E2-30, BR-S3E2-31
- Given: bộ tuần 15 của 11A1 Đã giao (F-S3-050); Bình An đã làm Đề 1
- When: cô Lan bấm "Đóng bộ", xác nhận; Bình An mở bộ rồi thử gửi đáp số Đề 2
- Then: bộ chuyển Đã đóng; Bình An xem được kết quả Đề 1; gửi đáp số Đề 2 trả CONFLICT, không ghi `Attempt`

**US-S3E2-04-SC2 — Bộ Đã đóng vẫn mở lời giải khi tới giờ mở** · Nguồn: BR-S3E2-31
- Given: bộ tuần 15 Đã đóng, giờ mở lời giải 06/12/2026 08:00 (F-S3-050)
- When: giờ máy chủ tới 06/12/2026 08:00; Bình An mở Đề 2
- Then: lời giải hiện đúng luật giờ mở; không có nút nhập đáp số

**US-S3E2-04-SC3 — Lưu trữ bộ Đã đóng** · Nguồn: BR-S3E2-32
- Given: bộ tuần 15 Đã đóng
- When: cô Lan bấm "Lưu trữ bộ"
- Then: bộ ra khỏi "Bài đang giao" của cô Lan và danh sách chính của học sinh; hiện ở "Bộ đã lưu trữ" của cô Lan; bảng lớp vẫn đọc được số liệu của bộ

**US-S3E2-04-FC1 — Bộ Nháp không đóng được** · Nguồn: BR-S3E2-30
- Given: bộ tuần 16 Nháp (F-S3-052)
- When: cô Lan gọi đóng bộ
- Then: CONFLICT; bộ vẫn Nháp

**US-S3E2-04-FC2 — Bộ Đã giao chưa đóng thì không lưu trữ được** · Nguồn: BR-S3E2-32
- Given: bộ tuần 15 Đã giao
- When: cô Lan gọi lưu trữ bộ
- Then: CONFLICT; bộ vẫn Đã giao

**US-S3E2-04-FC3 — Giáo viên khác không đóng được bộ của lớp mình** · Nguồn: BR-S3E2-33; QA-S3E1-04
- Given: bộ tuần 15 của 11A1; thầy Khôi (F-S3-002) không có quan hệ với 11A1
- When: thầy Khôi gọi đóng bộ
- Then: NOT_FOUND; bộ không đổi

**US-S3E2-04-FC4 — Bấm Đóng lặp chỉ đóng một lần** · Nguồn: BR-S3E2-33
- Given: bộ tuần 15 Đã giao; cùng khóa yêu cầu
- When: cô Lan gửi lệnh đóng hai lần liên tiếp
- Then: bộ Đã đóng một lần; lần hai trả cùng kết quả, không sinh lỗi

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E2, ready for R3 | runner-T-261126-14 (BA) |
| v0.2 | 2026-11-26 | Sửa theo RV_s23: BR-S3E2-12 làm rõ kênh Trình chiếu của giáo viên (mâu thuẫn với US-S2E7-03); đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
