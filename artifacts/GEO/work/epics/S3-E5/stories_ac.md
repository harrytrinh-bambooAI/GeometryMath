Phiên bản: v0.4 · 2026-11-27
# S3-E5 — Nhật ký thao tác nhạy cảm (AuditEvent), thông báo riêng tư, danh sách trường dữ liệu được phép, công tắc AI theo lớp: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA. Nguồn: `work/s23/story-map-s23.md` (R2 khóa, 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Ch3 (22/11/2026), `Design/s23/DQ.md` và ảnh `c3-*.png` (26/11/2026), `PO-DECISIONS.md` PO-53, PO-61…65. Khi nguồn mâu thuẫn thì PO-61 thắng STAGES.
> **Trạng thái: ready for R3** (owner đã duyệt R3 theo PO-66, 26/11/2026; ngưỡng số liệu giữ, chỉnh sau L6 (G12)). Mức epic: **P0**. Câu hỏi mở và đề xuất đổi story map ở `questions.md` cùng thư mục.

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S3En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, với giá trị cụ thể của dữ liệu mẫu bên dưới. `SC` = kịch bản chính, `FC` = lỗi, biên hoặc ngoại lệ. Mã chỉ cho biết **chỗ** một điều được ghi; câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC chặng 3** (kế thừa MVP, nguồn: `epics/E1/stories_ac.md` mục 0; PO-61…65):
- Mọi mốc giờ lấy từ giờ máy chủ (T0); giờ của thiết bị không được tin. API dùng ISO-8601 UTC; màn hình hiện `HH:mm, dd/MM/yyyy` giờ Việt Nam.
- Lỗi của máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định: VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN (sai vai), NOT_FOUND (đối tượng không thuộc quan hệ của người gọi; không tiết lộ đối tượng có tồn tại), CONFLICT (xung đột trạng thái), RATE_LIMITED (kèm `Retry-After`), INVALID_CURSOR. Chọn NOT_FOUND thay cho FORBIDDEN với đối tượng ngoài quan hệ là mặc định đã chốt PO-66, QA-S3E1-04.
- Quyền theo quan hệ, không theo vai đơn thuần: giáo viên chỉ thấy và tác động lên lớp mình tạo cùng học sinh Đang học của lớp đó; học sinh chỉ thấy bài và dữ liệu của chính mình ở lớp mình Đang học, không thấy kết quả của bạn; hết quan hệ (Đã rời, lớp rời) thì hết quyền. Mã lớp và mã người dùng lấy từ thẻ xác thực hoặc đường dẫn nhưng luôn kiểm quan hệ ở máy chủ (BR-S3E1-50).
- Chỉ thu và trả về các trường trong danh sách được phép (US-S3E5-03): tên hiển thị, mã người dùng, vai, lớp, bài làm, cờ đã đọc thông báo riêng tư cùng phiên bản văn bản. Không email, điện thoại, ngày sinh, trường, ảnh (PO-61, PO-64). "Dữ liệu trả về" nghĩa là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Phản hồi chứa dữ liệu học sinh, lời giải hoặc trạng thái khóa mang tiêu đề `Cache-Control: no-store` (kế thừa D-GEO-145).
- Thiết bị: giáo viên dùng web trên máy tính, điện thoại chỉ để liếc và quyết nhanh; học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA), PO-65.
- AI giữ giả lập trong CI (D-GEO-047): không có kỳ vọng nào về chất lượng câu chữ AI thật; không gọi AI thật.
- AC mang chữ PO-66 (QA-S3En-nn) dựa trên giả định đã được chủ đầu tư chốt ở PO-66 (26/11/2026); mã `QA-S3En-nn` cho biết câu hỏi gốc, câu trả lời ở `questions.md`. Ngưỡng và định dạng mã giữ, chỉnh sau L6 (G12).

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-S3En-nn` · `-SCn` / `-FCn` | story (En = epic S3-En, nn = số story) · tiêu chí thành công / thất bại thứ n | tệp này; khóa bởi `work/s23/story-map-s23.md` |
| `BR-S3En-nn` | quy tắc nghiệp vụ của story (nguồn dẫn kèm) | tệp này |
| `TC-S3En-nn` | test case của epic S3-En | cột No của `test_cases.md` |
| `F-S3-nnn` | hàng dữ liệu mẫu chung của chặng 3 | mục "Dữ liệu mẫu" của tệp này |
| `QA-S3En-nn` | câu hỏi mở của epic, kèm mặc định đang dùng (đã chốt PO-66) | `questions.md` cùng thư mục |
| `PO-nn` | dòng của sổ quyết định gốc: PO-53, PO-61…65 | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi và mặc định của thiết kế chặng 2–3 | `Design/s23/DQ.md` |
| `M-S3-nn` | chỉ số chặng 3 (M-S3-01…06) | `work/s23/brd-delta-s23.md` §1 |
| `C3-nn` | mã màn hình chặng 3 | `STAGES-AFTER-MVP.md` Ch3 §6; ảnh ở `Design/s23/c3-*.png` |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (ví dụ D-GEO-047 AI giả lập, D-GEO-185 khóa theo tài khoản) | `work/03_decisions.md` |
| `GV-…` · `HS-…` · `HK-…` · `RC-…` | mã người dùng giáo viên · học sinh · mã lớp · mã khôi phục (định dạng đã chốt PO-66, DQ-16, DQ-23) | `Design/s23/DQ.md` |
| `release_at` · `due_at` | giờ mở lời giải · hạn làm (chỉ là nhãn, không khóa bài) | `STAGES-AFTER-MVP.md` Ch3 §7, §8 |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có `release_at` (khóa) · giờ máy chủ < `release_at` (khóa) · giờ máy chủ ≥ `release_at` (được xem) | MVP, M-GEO-13 |
| Chờ duyệt · Đang học · Đã rời · Bị từ chối | trạng thái `ClassMembership` (Bị từ chối là đề xuất của BA, QA-S3E1-02) | tệp này |
| Đúng · Xem lại · Đang làm · Chưa làm | bốn trạng thái ô của bảng lớp × bài (nghĩa ở US-S3E3-01) | `Design/s23/c3-04-ket-qua-lop.png`, DQ-21 |
| Bài làm | một học sinh Đang học có ít nhất một tương tác (gửi đáp số, xin gợi ý hoặc hỏi) trên một đề của bộ; tính theo số học sinh khác nhau | US-S3E3-03, QA-S3E3-02 |
| T0 · t | giờ máy chủ lúc bắt đầu ca kiểm · giờ máy chủ tại thời điểm xét | mọi mốc giờ là tương đối |
| `CL-FN-nn` · `CL-UI-nn` · `CL-X-nn` | nhóm kiểm tra chức năng · giao diện và thiết bị · xuyên suốt (ma trận quyền, trường dữ liệu, tải) | `test_design.md` cùng thư mục |
| PO-66 · ENG · X | kỳ vọng theo giả định đã chốt PO-66 (kèm mã QA-) · case cần công cụ kỹ thuật · case xuyên suốt | cột Remark |
| `MSG-S3-nn` | thông điệp hiện cho người dùng của chặng 3, câu chữ ở bảng "Thông điệp" của tệp này | tệp này |
| `AuditEvent` · `EARLY_RELEASE` · `RELEASE_TIME_CHANGE` · `PASSWORD_RESET` | bản ghi nhật ký một thao tác nhạy cảm (chỉ thêm, không sửa) · ba loại sự kiện của chặng 3 | tệp này; STAGES Ch3 §7, §8 |

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


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E5-09)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-81 | Tiêu đề màn | "Thông báo riêng tư (bản 1.0)" | — |
| MSG-S3-82 | Inline | "Tên hiển thị không nên là email hay số điện thoại." | VALIDATION_FAILED |
| MSG-S3-83 | Inline | "Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn." | FORBIDDEN (details: AI_OFF_FOR_CLASS) |
| MSG-S3-84 | Inline | "Em cần đọc thông báo riêng tư để tiếp tục." | VALIDATION_FAILED |
| MSG-S3-85 | Toast | "Bạn không có quyền thực hiện thao tác này." | FORBIDDEN |

## US-S3E5-01 — AuditEvent cho mở sớm, đổi giờ, đặt lại mật khẩu, kèm người thực hiện
- **Story:** Là chủ sản phẩm, tôi muốn mỗi lần mở lời giải sớm, đổi giờ mở và đặt lại mật khẩu đều được ghi lại kèm người thực hiện, để truy được ai đã làm gì và đo chỉ số M-S3-05. · Loại Logic · P0 · Độ khó 3
- **Nguồn:** story map US-S3E5-01; STAGES Ch3 §7 ý 5, §8 `AuditEvent`; PO-61; M-S3-05, M-S3-06
- **Góc nhìn người vận hành (M18):** khi một phụ huynh hỏi "ai đã đặt lại mật khẩu của con tôi", chủ sản phẩm tra được người thực hiện và giờ. Ca đáng kể: ghi lỗi giữa chừng, thao tác bị từ chối, mật khẩu tạm lọt vào nhật ký, sửa nhật ký.
- **Phụ thuộc:** US-S3E1-04, US-S3E4-02, US-S3E2-02.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E5-01 | Ba loại sự kiện: EARLY_RELEASE (Mở ngay), RELEASE_TIME_CHANGE (Đổi giờ sau khi bộ đã giao), PASSWORD_RESET (giáo viên đặt lại mật khẩu học sinh). | Ch3 §7 |
| BR-S3E5-02 | Mỗi bản ghi có: mã sự kiện, loại, mã người dùng người thực hiện và vai, loại và mã đối tượng (mã người dùng hoặc mã bộ hoặc mã đề), mã lớp nội bộ, giá trị trước và sau (chỉ với hai loại giờ), kết quả (Thành công hoặc Từ chối), giờ máy chủ UTC. | Ch3 §7; QA-S3E5-01 |
| BR-S3E5-03 | Không ghi mật khẩu, mật khẩu tạm, giá trị băm, nội dung câu hỏi của học sinh hay trường nào ngoài danh sách được phép. | US-S3E5-03; BR-S3E1-32 |
| BR-S3E5-04 | Thao tác nhạy cảm và AuditEvent thành công cùng một giao dịch: ghi nhật ký lỗi thì thao tác không xảy ra. | an toàn |
| BR-S3E5-05 | Nhật ký chỉ thêm: không có API hay lệnh ứng dụng sửa hoặc xóa; cơ sở dữ liệu từ chối UPDATE và DELETE của tài khoản ứng dụng. | an toàn |
| BR-S3E5-06 | Đặt lịch lần đầu cho bộ Nháp không phải sự kiện; chỉ đổi giờ sau khi bộ Đã giao mới ghi. | QA-S3E5-03 |
| BR-S3E5-07 | Lần bị từ chối vì quyền hoặc quan hệ ghi một sự kiện bảo mật (Từ chối) chỉ có loại, người gọi, loại đối tượng, giờ, để đếm M-S3-06. Để nhật ký không phình khi bị gọi dồn: ghi tối đa 1 hàng Từ chối trong mỗi giờ cho mỗi cặp (người gọi, loại đối tượng); các lần từ chối thêm trong cùng giờ vẫn trả NOT_FOUND/FORBIDDEN nhưng không thêm hàng. | M-S3-06; QA-S3E5-02; PO-76 |
| BR-S3E5-08 | Chủ lớp đọc được nhật ký của lớp mình (chỉ đọc); học sinh không đọc được; người khác không thấy lớp. | QA-S3E5-01 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E1-12, QA-S3E5-01, QA-S3E5-02, QA-S3E5-03.

### Tiêu chí chấp nhận

**US-S3E5-01-SC1 — Mở ngay ghi EARLY_RELEASE** · Nguồn: BR-S3E5-01, 02
- Given: bộ tuần 15 Sắp mở, giờ mở 06/12/2026 08:00; giờ máy chủ 05/12/2026 21:00 (F-S3-050)
- When: cô Lan "Mở ngay" bộ tuần 15, rồi đọc nhật ký của lớp 11A1
- Then: có đúng một AuditEvent loại EARLY_RELEASE với người thực hiện `GV-7K2M-9QPA` vai Giáo viên, đối tượng là bộ tuần 15, giá trị trước 06/12/2026 08:00, giá trị sau 05/12/2026 21:00, kết quả Thành công, giờ máy chủ UTC


**US-S3E5-01-SC2 — Đổi giờ ghi RELEASE_TIME_CHANGE** · Nguồn: BR-S3E5-01, 02
- Given: bộ tuần 15 đã giao, Sắp mở, giờ mở 06/12/2026 08:00 (F-S3-050)
- When: cô Lan đổi giờ mở sang 06/12/2026 19:00
- Then: có một AuditEvent RELEASE_TIME_CHANGE với giá trị trước 06/12/2026 08:00 và giá trị sau 06/12/2026 19:00, người thực hiện `GV-7K2M-9QPA`


**US-S3E5-01-SC3 — Đặt lại mật khẩu ghi PASSWORD_RESET, không có mật khẩu** · Nguồn: BR-S3E5-01, 03
- Given: Chi Mai `HS-8L2K-77AB` Đang học 11A1 (F-S3-021)
- When: cô Lan đặt lại mật khẩu cho Chi Mai, rồi đọc nhật ký của lớp 11A1
- Then: có một AuditEvent PASSWORD_RESET, người thực hiện `GV-7K2M-9QPA`, đối tượng `HS-8L2K-77AB`; bản ghi không chứa mật khẩu tạm, giá trị băm hay bất kỳ chuỗi nào của mật khẩu


**US-S3E5-01-SC4 — Bản ghi chỉ có trường được phép** · Nguồn: BR-S3E5-02, 03
- Given: ba AuditEvent của ba loại đã có ở 11A1
- When: gọi API nhật ký và liệt kê mọi khóa của mỗi bản ghi
- Then: khóa chỉ gồm mã sự kiện, loại, mã người thực hiện, vai, loại đối tượng, mã đối tượng, mã lớp, giá trị trước, giá trị sau, kết quả, giờ; không có tên hiển thị của học sinh, không có địa chỉ IP, không có thông tin thiết bị


**US-S3E5-01-SC5 — Giờ ghi là giờ máy chủ, hiển thị giờ Việt Nam** · Nguồn: BR-S3E5-02
- Given: đồng hồ điện thoại của cô Lan lệch 3 giờ; bộ tuần 15 Sắp mở (F-S3-050)
- When: cô Lan bấm "Mở ngay", rồi đọc nhật ký
- Then: giờ của sự kiện là giờ máy chủ UTC dạng ISO-8601; không có giờ của điện thoại trong bản ghi; màn nhật ký hiện giờ theo định dạng `HH:mm, dd/MM/yyyy` giờ Việt Nam


**US-S3E5-01-SC6 — Chủ lớp đọc nhật ký lớp mình** · Nguồn: BR-S3E5-08 · PO-66 (QA-S3E5-01)
- Given: 11A1 có 3 AuditEvent; 11A2 có 1 AuditEvent; cô Lan chủ cả hai (F-S3-010, F-S3-011)
- When: cô Lan gọi nhật ký của 11A1 rồi nhật ký của 11A2
- Then: 11A1 trả đúng 3 bản ghi, 11A2 trả đúng 1; không bản ghi nào của lớp khác lẫn vào


**US-S3E5-01-FC1 — Ghi nhật ký lỗi thì thao tác không xảy ra** · Nguồn: BR-S3E5-04
- Given: việc ghi AuditEvent được giả lập báo lỗi; mật khẩu của Chi Mai đang là giá trị cũ đã biết (F-S3-021)
- When: cô Lan đặt lại mật khẩu cho Chi Mai
- Then: yêu cầu trả lỗi máy chủ; mật khẩu của Chi Mai không đổi; không có mật khẩu tạm; không có AuditEvent; phiên của Chi Mai không bị hủy


**US-S3E5-01-FC2 — Đổi giờ trên bản Nháp không ghi AuditEvent** · Nguồn: BR-S3E5-06 · PO-66 (QA-S3E5-03)
- Given: "Bộ bài tuần 16" Nháp (F-S3-052)
- When: cô Lan đặt giờ mở rồi đổi giờ mở hai lần khi bộ còn Nháp, rồi giao bộ, rồi đổi giờ một lần
- Then: nhật ký chỉ có một AuditEvent RELEASE_TIME_CHANGE (lần đổi sau khi giao); ba lần đặt khi còn Nháp không có bản ghi


**US-S3E5-01-FC3 — Thao tác bị từ chối ghi sự kiện bảo mật** · Nguồn: BR-S3E5-07 · PO-66 (QA-S3E5-02)
- Given: thầy Khôi đã đăng nhập; bộ đếm sự kiện bảo mật của dữ liệu thử bằng 0
- When: thầy Khôi gọi "Mở ngay" của bộ tuần 15 lớp 11A1
- Then: máy chủ trả NOT_FOUND; có một bản ghi kết quả Từ chối với người gọi `GV-4D8R-2WNX` và loại đối tượng là bộ; không có AuditEvent EARLY_RELEASE thành công; bản ghi Từ chối không chứa tên lớp hay nội dung yêu cầu; gọi thêm lần nữa trong cùng giờ vẫn trả NOT_FOUND nhưng số hàng Từ chối của cặp (người gọi, bộ) vẫn là 1 (BR-S3E5-07, PO-76)


**US-S3E5-01-FC4 — Nhật ký chỉ thêm, không sửa không xóa** · Nguồn: BR-S3E5-05
- Given: một AuditEvent có sẵn
- When: gọi thẳng yêu cầu sửa và yêu cầu xóa bản ghi qua API, rồi chạy UPDATE và DELETE bằng tài khoản cơ sở dữ liệu của ứng dụng
- Then: hai yêu cầu API trả FORBIDDEN hoặc không có đường dẫn; hai lệnh cơ sở dữ liệu bị từ chối; bản ghi nguyên vẹn


**US-S3E5-01-FC5 — Học sinh không đọc được nhật ký** · Nguồn: BR-S3E5-08
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi API nhật ký của 11A1
- Then: máy chủ trả FORBIDDEN; không có bản ghi nào trong thân lỗi


**US-S3E5-01-FC6 — Giáo viên khác không đọc được nhật ký lớp 11A1** · Nguồn: BR-S3E5-08
- Given: 11A1 có AuditEvent; thầy Khôi đã đăng nhập (F-S3-002)
- When: thầy Khôi gọi API nhật ký của 11A1
- Then: máy chủ trả NOT_FOUND; không có bản ghi hay mã người dùng nào của lớp 11A1


**US-S3E5-01-FC7 — Gửi lặp cùng khóa chỉ một bản ghi** · Nguồn: BR-S3E5-04
- Given: bộ tuần 15 Sắp mở (F-S3-050)
- When: gửi "Mở ngay" hai lần cùng khóa chống lặp
- Then: nhật ký có đúng một AuditEvent EARLY_RELEASE


**US-S3E5-01-FC8 — Đặt lại mật khẩu bị giới hạn tần suất không có bản ghi thành công** · Nguồn: BR-S3E5-01; BR-S3E1-35 · PO-66 (QA-S3E1-12)
- Given: cô Lan đã đặt lại thành công 10 lần trong 1 giờ (F-S3-010)
- When: đặt lại lần thứ 11 trong cùng giờ
- Then: RATE_LIMITED; nhật ký có đúng 10 AuditEvent PASSWORD_RESET thành công; lần thứ 11 chỉ có bản ghi Từ chối


**US-S3E5-01-FC9 — Đếm sự kiện theo lớp phục vụ M-S3-05** · Nguồn: BR-S3E5-02; M-S3-05
- Given: 11A1 có 4 AuditEvent PASSWORD_RESET thành công, 11A2 có 1, 10B có 0
- When: chạy truy vấn đếm số PASSWORD_RESET thành công theo lớp
- Then: kết quả 4, 1, 0 tương ứng; không tính bản ghi Từ chối; truy vấn dùng chỉ mục, không quét toàn bảng (kế hoạch truy vấn có chỉ mục)


## US-S3E5-02 — Thông báo riêng tư, cờ "đã đọc" cùng phiên bản văn bản
- **Story:** Là người dùng, tôi muốn được đọc thông báo riêng tư ngắn gọn lần đầu vào ứng dụng và xác nhận đã đọc, để biết sản phẩm thu gì và không thu gì. · Loại UI · P0 · Độ khó 2
- **Nguồn:** story map US-S3E5-02; PO-64 (cờ "đã đọc" + phiên bản văn bản, không danh tính; thay `ConsentRecord`); brd-delta §2 mục 4; DQ-15; ảnh `c3-01-dang-ky.png`
- **Góc nhìn người vận hành (M18):** học sinh đăng ký trên iPad thấy hộp tick "Em đã đọc thông báo riêng tư (bản 1.0)" cùng liên kết đọc. Ca đáng kể: tài khoản MVP chuyển sang chưa có cờ, văn bản đổi lên bản 1.1, thiết bị gửi sai phiên bản.
- **Phụ thuộc:** S2-E1 (đăng ký); US-S3E5-03 (danh sách trường).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E5-20 | Đăng ký bắt buộc tick "đã đọc thông báo riêng tư (bản X)"; không tick thì không đăng ký được. | DQ-15 |
| BR-S3E5-21 | Cờ đã đọc lưu đúng ba giá trị: mã người dùng, phiên bản văn bản máy chủ đang áp dụng, giờ máy chủ; không lưu địa chỉ IP, thiết bị, danh tính. | PO-64; PO-61 |
| BR-S3E5-22 | Phiên bản do máy chủ quyết, không tin giá trị từ thiết bị. | an toàn |
| BR-S3E5-23 | Tài khoản chưa có cờ (ví dụ tài khoản MVP chuyển sang) thấy thông báo ở lần vào ứng dụng đầu tiên; khi phiên bản hiện hành tăng, hiện lại một lần cho người đã đọc bản cũ. | PO-64 |
| BR-S3E5-24 | Chưa bấm "Đã đọc" thì chưa dùng được phần còn lại của ứng dụng. | QA-S3E5-04 |
| BR-S3E5-25 | Văn bản nêu đủ: chỉ thu tên hiển thị, mã người dùng, vai, lớp, bài làm; không thu email, điện thoại, ngày sinh, trường, ảnh; học sinh quên mật khẩu thì dùng mã khôi phục của mình hoặc nhờ giáo viên của lớp đặt lại (PO-66 G3); giáo viên mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được. | brd-delta §2 mục 6; PO-61 |
| BR-S3E5-26 | Cờ chỉ người đó đọc được; giáo viên và học sinh khác không thấy cờ của nhau. | BR-S3E1-52 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E5-04.

### Tiêu chí chấp nhận

**US-S3E5-02-SC1 — Đăng ký có tick thì lưu cờ và phiên bản** · Nguồn: BR-S3E5-20, 21; DQ-15
- Given: người dùng mới ở màn đăng ký; văn bản hiện hành là bản 1.0
- When: nhập tên hiển thị "Thu Hà", chọn vai Học sinh, đặt mật khẩu, rồi tick "Em đã đọc thông báo riêng tư (bản 1.0)", rồi bấm "Đăng ký"
- Then: tài khoản được tạo; cờ đã đọc có phiên bản "1.0" cùng giờ máy chủ; có mã người dùng dạng `HS-` được hiện
- Dữ liệu nhập: "Thu Hà" ; vai Học sinh


**US-S3E5-02-SC2 — Bản ghi cờ chỉ có ba giá trị** · Nguồn: BR-S3E5-21
- Given: tài khoản vừa đăng ký ở AC SC1
- When: đọc bản ghi cờ trong cơ sở dữ liệu thử và API trả về của chính người dùng
- Then: bản ghi chỉ gồm mã người dùng, phiên bản, giờ; không có địa chỉ IP, chuỗi trình duyệt, mã thiết bị hay tên


**US-S3E5-02-SC3 — Tài khoản MVP chuyển sang thấy thông báo ở lần vào đầu tiên** · Nguồn: BR-S3E5-23
- Given: Bình An là tài khoản chuyển từ MVP, chưa có cờ (F-S3-020)
- When: đăng nhập lần đầu, rồi bấm "Đã đọc"
- Then: trước khi bấm hiện màn MSG-S3-81 và chưa vào được Bài của em; sau khi bấm cờ có phiên bản hiện hành và vào được Bài của em; lần đăng nhập sau không hiện lại


**US-S3E5-02-SC4 — Phiên bản văn bản tăng thì hiện lại một lần** · Nguồn: BR-S3E5-23
- Given: Bình An đã đọc bản 1.0; văn bản hiện hành đổi sang bản 1.1 (F-S3-020)
- When: đăng nhập, rồi bấm "Đã đọc", rồi đăng nhập lần nữa
- Then: lần đầu hiện màn MSG-S3-81 ghi bản 1.1; sau khi bấm cờ có phiên bản "1.1"; lần sau không hiện


**US-S3E5-02-SC5 — Văn bản nêu đủ các ý bắt buộc** · Nguồn: BR-S3E5-25
- Given: màn thông báo riêng tư bản 1.0 đang mở
- When: đọc nội dung
- Then: có câu nói chỉ thu tên hiển thị, mã người dùng, vai, lớp, bài làm; có câu nói không thu email, điện thoại, ngày sinh, trường, ảnh; có câu nói học sinh quên mật khẩu thì dùng mã khôi phục của mình hoặc nhờ giáo viên của lớp đặt lại (PO-66 G3); có câu nói giáo viên mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được


**US-S3E5-02-FC1 — Đã đọc bản hiện hành thì không hiện lại** · Nguồn: BR-S3E5-23
- Given: Bình An đã đọc bản hiện hành 1.0 (F-S3-020)
- When: đăng nhập từ thiết bị khác
- Then: không hiện màn thông báo riêng tư; vào thẳng Bài của em


**US-S3E5-02-FC2 — Đăng ký không tick bị từ chối** · Nguồn: BR-S3E5-20
- Given: người dùng mới điền tên hiển thị, vai, mật khẩu hợp lệ, không tick
- When: bấm "Đăng ký", rồi gọi thẳng yêu cầu đăng ký không có cờ
- Then: giao diện hiện MSG-S3-84 dưới hộp tick; yêu cầu gọi thẳng trả VALIDATION_FAILED; không tạo tài khoản, không sinh mã người dùng


**US-S3E5-02-FC3 — Chưa bấm "Đã đọc" thì không dùng được API khác** · Nguồn: BR-S3E5-24 · PO-66 (QA-S3E5-04)
- Given: học sinh có cờ chưa đọc bản hiện hành
- When: gọi API Bài của em và API Vào lớp
- Then: cả hai trả FORBIDDEN với `details` nêu cần đọc thông báo riêng tư; không có dữ liệu bài


**US-S3E5-02-FC4 — Thiết bị gửi sai phiên bản** · Nguồn: BR-S3E5-22
- Given: văn bản hiện hành là bản 1.1; học sinh chưa đọc
- When: gửi xác nhận đã đọc kèm phiên bản "9.9", rồi gửi xác nhận kèm phiên bản "1.0"
- Then: cả hai bị VALIDATION_FAILED; cờ không đổi; chỉ xác nhận không kèm hoặc kèm đúng "1.1" mới được nhận và máy chủ ghi "1.1"
- Dữ liệu nhập: "9.9" ; "1.0"


**US-S3E5-02-FC5 — Người khác không thấy cờ của mình** · Nguồn: BR-S3E5-26
- Given: Bình An có cờ phiên bản 1.0 (F-S3-020)
- When: cô Lan gọi chi tiết học sinh Bình An và danh sách học sinh 11A1, rồi Chi Mai gọi chi tiết Bình An
- Then: thân phản hồi cho cô Lan không có cờ hay phiên bản thông báo riêng tư của em nào; Chi Mai nhận FORBIDDEN


**US-S3E5-02-FC6 — Màn thông báo đọc được trên iPad và iPhone** · Nguồn: BR-S3E5-25
- Given: iPad màn ngang 1180×820 và iPhone màn dọc 390×844, trình duyệt web
- When: mở màn thông báo riêng tư ở từng thiết bị
- Then: văn bản đọc được không cắt chữ, hộp tick và nút "Đã đọc" cao ít nhất 44 điểm ảnh, không có thanh cuộn ngang


## US-S3E5-03 — Danh sách trường dữ liệu được phép thu; test chặn trường mới
- **Story:** Là chủ sản phẩm, tôi muốn có một danh sách trường dữ liệu được phép thu và một bộ test làm hỏng bản dựng khi ai đó thêm trường ngoài danh sách, để cam kết "không thu dữ liệu cá nhân" không bị phá lặng lẽ. · Loại Xuyên suốt · P0 · Độ khó 4
- **Nguồn:** story map US-S3E5-03; STAGES Ch3 §10 ("danh sách trường dữ liệu được phép thu, duyệt trước khi làm"); PO-61, PO-64; brd-delta §4 "Riêng tư"; DQ-15; M-S3-06
- **Góc nhìn người vận hành (M18):** một nhà phát triển thêm cột `email` để "tiện gửi nhắc việc"; bản dựng phải đỏ. Ca đáng kể: tên hiển thị là địa chỉ email hay số điện thoại, trường đặt tên biến thể (`dob`, `phoneNumber`), cột trong bảng sao lưu hay phân tích, địa chỉ IP trong nhật ký.
- **Phụ thuộc:** mọi epic S2 và S3 có thêm cột hay trường API; kế hoạch kiến trúc (PLAN task 17).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E5-30 | Danh sách được phép nằm trong một tệp máy đọc được của mã nguồn (`allowed_fields`), mỗi dòng: thực thể, trường, lý do, mã quyết định. Nội dung ban đầu: người dùng (tên hiển thị, mã người dùng, vai, băm mật khẩu, băm mã khôi phục của giáo viên, cờ và phiên bản thông báo riêng tư, giờ tạo), thành viên lớp (lớp, người dùng, trạng thái, giờ), lớp (tên, cấp, năm học, mã lớp, trạng thái, công tắc), bộ bài và đề trong bộ, bài làm và tương tác, thông báo, AuditEvent; và thực thể chặng 2 (S2-E1…E9): đề và lời giải cùng phiên bản lời giải, `StepCheck`, nhãn hai trục, bài luyện và lượt thử, báo "Em nghĩ đáp số này đúng" (ghi chú), yêu cầu soát Kho chung (kèm lý do từ chối), cờ vai người soát, cờ "phải đặt mật khẩu mới", bộ đếm khóa đăng nhập và đếm tần suất. Thiếu một thực thể của chặng 2 trong danh sách thì bản dựng đỏ ngay ở lần đầu (RV_s23 M-04). | Ch3 §10; PO-61; PO-65 |
| BR-S3E5-31 | Bản dựng đỏ khi một cột cơ sở dữ liệu, một khóa phản hồi API, một khóa thân yêu cầu hoặc một khóa sự kiện đo không có trong danh sách. | Ch3 §10 |
| BR-S3E5-32 | Bản dựng đỏ khi một dòng của danh sách thiếu mã quyết định (PO-nn) hoặc thiếu lý do. | duyệt trước khi làm |
| BR-S3E5-33 | Tên trường cấm được chặn theo mẫu không phân biệt hoa thường và dấu gạch: email, mail, phone, sdt, mobile, dob, birth, ngay_sinh, address, diachi, school, truong, avatar, photo, image, anh, cmnd, cccd, ip, device. Ngoại lệ có tên và có dòng riêng trong `allowed_fields` (mã quyết định PO-65 Q4): khóa tải ảnh đề của giáo viên ở US-S2E3-02 (`problem_image`); đó là ảnh đề bài, không phải ảnh người dùng, và không lưu sau khi chữ được xác nhận (BR-S2E3-09). | PO-61; PO-65 |
| BR-S3E5-34 | Thân yêu cầu có trường ngoài danh sách bị từ chối VALIDATION_FAILED, không lưu phần nào. | PO-61 |
| BR-S3E5-35 | Tên hiển thị không được trông như email (có `@` và dấu chấm sau) hoặc số điện thoại (từ 9 chữ số trở lên, bỏ khoảng trắng và dấu chấm hay gạch); "Minh 10A" và tên có số ngắn vẫn được nhận. | PO-61; QA-S3E5-05 |
| BR-S3E5-36 | Mã trường kỹ thuật của MVP (`org_id`) chỉ là định danh nội bộ không do người dùng nhập, không hiển thị và không có trong phản hồi API. | QA-S3E5-07 |
| BR-S3E5-37 | Nhật ký truy cập của máy chủ web không lưu địa chỉ IP quá 7 ngày; không bảng ứng dụng nào lưu IP hay thiết bị của người dùng. Ngoại lệ duy nhất: bộ đếm tần suất theo địa chỉ mạng của BR-S2E1-06 giữ ở bộ nhớ tạm hoặc ở hàng có khóa đã băm bằng HMAC (khóa bí mật của máy chủ) có hạn lưu tối đa 1 giờ rồi bị công việc dọn xóa (PO-71), không phải cột bền vững gắn vào tài khoản, không có trong `allowed_fields` (Đã chốt PO-66 · vừa · RV_s23 M-03). | QA-S3E5-06; PO-63 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E5-05, QA-S3E5-06, QA-S3E5-07.

### Tiêu chí chấp nhận

**US-S3E5-03-SC1 — Mọi cột, khóa phản hồi và khóa yêu cầu đều trong danh sách** · Nguồn: BR-S3E5-30, 31
- Given: cơ sở dữ liệu thử đã chạy hết các migration và dữ liệu mẫu chung của chặng 3 đã nạp
- When: chạy test quét: liệt kê mọi cột của mọi bảng ứng dụng, gọi mọi API chặng 3 bằng dữ liệu mẫu và liệt kê mọi khóa phản hồi, mọi khóa thân yêu cầu hợp lệ của API ghi
- Then: mọi cột, mọi khóa phản hồi và mọi khóa yêu cầu có trong `allowed_fields`; test báo số mục đã quét và 0 mục ngoài danh sách


**US-S3E5-03-SC2 — Thêm trường mới ngoài danh sách thì bản dựng đỏ** · Nguồn: BR-S3E5-31
- Given: một migration giả thêm cột `contact_note` vào bảng người dùng; một DTO giả thêm khóa `parentName` vào phản hồi
- When: chạy test quét ở từng thay đổi
- Then: test thất bại và nêu đúng tên cột `contact_note` rồi khóa `parentName` là ngoài danh sách; sau khi bỏ hai thay đổi giả, test xanh


**US-S3E5-03-SC3 — Đăng ký có trường thừa bị từ chối** · Nguồn: BR-S3E5-34
- Given: yêu cầu đăng ký đủ trường hợp lệ (tên hiển thị, vai, mật khẩu, cờ thông báo riêng tư)
- When: gửi yêu cầu kèm thêm `email` = "mai@example.org", rồi gửi yêu cầu kèm thêm `phone` = "0912345678"
- Then: cả hai bị VALIDATION_FAILED với `details` chỉ ra trường thừa; không tạo tài khoản; không có giá trị `mai@example.org` hay `0912345678` trong cơ sở dữ liệu hay nhật ký
- Dữ liệu nhập: `email` = "mai@example.org" ; `phone` = "0912345678"


**US-S3E5-03-SC4 — Mỗi dòng danh sách phải có lý do và mã quyết định** · Nguồn: BR-S3E5-32
- Given: tệp `allowed_fields` có hai dòng giả: một thiếu mã quyết định, một thiếu lý do
- When: chạy test kiểm tệp danh sách
- Then: test thất bại và nêu hai dòng đó; mọi dòng ban đầu có lý do và mã quyết định dạng PO-nn


**US-S3E5-03-SC5 — Tên trường cấm bị chặn mọi dạng viết** · Nguồn: BR-S3E5-33
- Given: danh sách thử gồm các tên `Email`, `e-mail`, `phoneNumber`, `so_dien_thoai`, `dob`, `ngay_sinh`, `birthday`, `truong`, `school_name`, `anh_dai_dien`, `avatarUrl`, `ipAddress`, `deviceId`
- When: thêm từng tên làm cột hoặc khóa phản hồi giả rồi chạy test quét
- Then: test thất bại với từng tên và nêu rằng tên khớp mẫu cấm; không tên nào lọt qua


**US-S3E5-03-SC6 — Nhật ký ứng dụng không chứa email hay số điện thoại** · Nguồn: BR-S3E5-33
- Given: chạy toàn bộ bộ test E2E chặng 3 với nhật ký ứng dụng bật
- When: quét nhật ký bằng mẫu email và số điện thoại
- Then: 0 khớp (các giá trị cố ý đưa vào ở AC SC3 và AC FC6 của US-S3E3-02 không xuất hiện); nội dung câu hỏi của học sinh không có trong nhật ký


**US-S3E5-03-FC1 — Tên hiển thị là email** · Nguồn: BR-S3E5-35 · PO-66 (QA-S3E5-05)
- Given: màn đăng ký đang mở
- When: nhập tên hiển thị "mai@example.org", rồi bấm "Đăng ký"
- Then: dưới ô tên hiện MSG-S3-82; VALIDATION_FAILED nếu gọi thẳng; không tạo tài khoản
- Dữ liệu nhập: "mai@example.org"


**US-S3E5-03-FC2 — Biên số điện thoại: 9 chữ số bị chặn, 8 chữ số được nhận** · Nguồn: BR-S3E5-35 · PO-66 (QA-S3E5-05)
- Given: màn đăng ký đang mở
- When: đăng ký tên hiển thị "0912 345 678", rồi đăng ký tên "Lớp 12345678", rồi đăng ký tên "Minh 10A"
- Then: "0912 345 678" (9 chữ số sau khi bỏ khoảng trắng) bị VALIDATION_FAILED với MSG-S3-82; "Lớp 12345678" (8 chữ số) và "Minh 10A" được nhận
- Dữ liệu nhập: "0912 345 678" ; "Lớp 12345678" ; "Minh 10A"


**US-S3E5-03-FC3 — Tên có dấu tiếng Việt và ký tự thường gặp vẫn được nhận** · Nguồn: BR-S3E5-35
- Given: màn đăng ký đang mở
- When: đăng ký tên "Nguyễn Thị Ánh Tuyết", rồi đăng ký tên "Đức-Huy (11A1)"
- Then: cả hai được nhận; tên hiển thị lưu và hiện đúng dấu
- Dữ liệu nhập: "Nguyễn Thị Ánh Tuyết" ; "Đức-Huy (11A1)"


**US-S3E5-03-FC4 — Thân yêu cầu có khóa biến thể tên cấm** · Nguồn: BR-S3E5-33, 34
- Given: cô Lan gọi API tạo lớp
- When: gửi thân yêu cầu kèm `schoolName` = "THPT Mẫu A", rồi gửi thân kèm `Truong` = "THPT Mẫu A"
- Then: cả hai bị VALIDATION_FAILED; không có lớp mới; không lưu "THPT Mẫu A"
- Dữ liệu nhập: `schoolName` = "THPT Mẫu A" ; `Truong` = "THPT Mẫu A"


**US-S3E5-03-FC5 — Không bảng ứng dụng nào lưu IP hay thiết bị** · Nguồn: BR-S3E5-37 · PO-66 (QA-S3E5-06)
- Given: cơ sở dữ liệu thử sau một ca E2E chặng 3
- When: quét tên cột và giá trị của mọi bảng ứng dụng theo mẫu địa chỉ IP, chuỗi trình duyệt và mã thiết bị
- Then: 0 khớp; cấu hình máy chủ web ghi rõ nhật ký truy cập giữ không quá 7 ngày


**US-S3E5-03-FC6 — Bảng sao lưu và sự kiện đo cũng bị quét** · Nguồn: BR-S3E5-31
- Given: bảng sự kiện đo (chỉ số M-S3-01…06) và bảng xuất dữ liệu cho báo cáo đã có dữ liệu thử
- When: chạy test quét cho các bảng này
- Then: mọi cột và khóa nằm trong danh sách; sự kiện đo chỉ có mã người dùng của giáo viên hoặc số đếm, không tên hiển thị, không nội dung câu hỏi


**US-S3E5-03-FC7 — Mã trường kỹ thuật không lộ ra ngoài** · Nguồn: BR-S3E5-36 · PO-66 (QA-S3E5-07)
- Given: cô Lan đã đăng nhập; dữ liệu mẫu có mã trường kỹ thuật nội bộ
- When: gọi mọi API chặng 3 dành cho giáo viên và học sinh, rồi gửi yêu cầu tạo lớp kèm `org_id`
- Then: không phản hồi nào chứa `org_id`, tên trường hay mã trường; yêu cầu có `org_id` bị VALIDATION_FAILED; mã trường nội bộ chỉ lấy từ thẻ xác thực của máy chủ


## US-S3E5-04 — Công tắc AI phía học sinh theo lớp; tắt thì luồng học cơ bản vẫn chạy
- **Story:** Là giáo viên, tôi muốn tắt trợ lý AI cho học sinh của một lớp mà các em vẫn học bình thường, để quyết định mức dùng AI theo lớp mình. · Loại Logic · P0 · Độ khó 3
- **Nguồn:** story map US-S3E5-04; STAGES 0.3 ý 7; Ch3 §6 C3-02 (cài đặt lớp); `Design/s23/c3-07-ma-lop.png` (khu cài đặt lớp); D-GEO-047
- **Góc nhìn người vận hành (M18):** cô Lan tắt trợ lý ở lớp 11A1 trong tuần kiểm tra, học sinh vẫn làm bài, xin gợi ý có sẵn và xem lời giải khi mở. Ca đáng kể: tắt giữa lúc em đang hỏi, em sửa giao diện để hiện lại nút, em ở hai lớp một bật một tắt.
- **Phụ thuộc:** US-S3E1-01 (lớp); cổng AI của MVP; US-S3E2-03 (bộ của lớp).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E5-40 | Lớp có `ai_enabled`, mặc định bật; chỉ chủ lớp đổi. | QA-S3E5-08 |
| BR-S3E5-41 | Công tắc áp lên mọi lượt hỏi đáp AI của học sinh khi làm bài thuộc lớp đó; máy chủ kiểm ở mỗi lượt gọi, không dựa vào giao diện. | an toàn |
| BR-S3E5-42 | Khi tắt: ẩn khung trợ lý ở màn học sinh kèm MSG-S3-83; lượt gọi trợ lý trả FORBIDDEN (details AI_OFF_FOR_CLASS); không có lượt gọi nào tới nhà cung cấp AI, không phát sinh chi phí. | Ch3 0.3 ý 7 |
| BR-S3E5-43 | Luồng học cơ bản không phụ thuộc AI: vào lớp, Bài của em, làm bài và gửi đáp số, gợi ý có sẵn từ lời giải đã duyệt, xem lời giải khi mở, thông báo. | 0.3 ý 7 |
| BR-S3E5-44 | Công tắc theo lớp của bộ đang làm: học sinh ở hai lớp thấy AI bật ở bài lớp này, tắt ở bài lớp kia; Tự học không lớp theo mặc định bật. | PO-65 (Q9) |
| BR-S3E5-45 | Đổi công tắc có hiệu lực từ lượt gọi kế tiếp; lượt đang xử lý hoàn tất. | QA-S3E5-08 |
| BR-S3E5-46 | Công tắc này không tắt "AI gộp" ở Kết quả lớp (phía giáo viên). | QA-S3E5-08; US-S3E3-03 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E5-08.

### Tiêu chí chấp nhận

**US-S3E5-04-SC1 — Tắt công tắc thì khung trợ lý ẩn ở học sinh** · Nguồn: BR-S3E5-42
- Given: lớp 11A1 đang bật trợ lý AI (F-S3-010)
- When: cô Lan tắt công tắc "Trợ lý AI cho học sinh" ở cài đặt lớp, rồi Bình An mở một đề của bộ tuần 15 trên iPad
- Then: khung trợ lý không hiện; có dòng MSG-S3-83; các phần còn lại của màn làm bài giữ nguyên


**US-S3E5-04-SC2 — Máy chủ chặn lượt gọi trợ lý và không gọi nhà cung cấp** · Nguồn: BR-S3E5-41, 42
- Given: 11A1 tắt trợ lý; nhà cung cấp AI giả lập đếm lượt gọi (F-S3-010)
- When: Bình An gọi thẳng API hỏi trợ lý cho đề của bộ tuần 15
- Then: máy chủ trả FORBIDDEN với `details` AI_OFF_FOR_CLASS; số lượt gọi nhà cung cấp bằng 0; không có bản ghi lượt gọi AI mới


**US-S3E5-04-SC3 — Luồng học cơ bản vẫn chạy khi tắt** · Nguồn: BR-S3E5-43
- Given: 11A1 tắt trợ lý; Bình An chưa làm Đề 1 của bộ tuần 15 (F-S3-050)
- When: mở Bài của em, rồi mở Đề 1, rồi xin gợi ý mức 1 có sẵn, rồi gửi đáp số, rồi mở thông báo
- Then: mọi bước thực hiện được không lỗi; gợi ý mức 1 hiện từ lời giải đã duyệt; đáp số được chấm; ô Bình An × Đề 1 trên Kết quả lớp cập nhật; số lượt gọi nhà cung cấp AI bằng 0


**US-S3E5-04-SC4 — Bật lại thì có hiệu lực từ lượt kế tiếp** · Nguồn: BR-S3E5-45
- Given: 11A1 đang tắt trợ lý
- When: cô Lan bật lại công tắc, rồi Bình An gọi API hỏi trợ lý ngay sau đó
- Then: lượt gọi của Bình An được nhận (trả lời của nhà cung cấp giả lập); khung trợ lý hiện lại ở lần mở màn kế tiếp


**US-S3E5-04-SC5 — Em ở hai lớp: bộ lớp nào theo công tắc lớp đó** · Nguồn: BR-S3E5-44
- Given: Chi Mai Đang học 11A1 (tắt trợ lý) và 10B (bật trợ lý) (F-S3-040, F-S3-021)
- When: gọi API hỏi trợ lý cho đề của bộ 11A1, rồi gọi API hỏi trợ lý cho đề của bộ 10B
- Then: lượt cho bộ 11A1 trả FORBIDDEN (AI_OFF_FOR_CLASS); lượt cho bộ 10B được nhận


**US-S3E5-04-SC6 — Lớp mới mặc định bật** · Nguồn: BR-S3E5-40 · PO-66 (QA-S3E5-08)
- Given: cô Lan vừa tạo lớp "11B3"
- When: mở cài đặt lớp "11B3"
- Then: công tắc "Trợ lý AI cho học sinh" đang bật


**US-S3E5-04-FC1 — Học sinh không đổi được công tắc** · Nguồn: BR-S3E5-40
- Given: 11A1 đang tắt trợ lý; Bình An đã đăng nhập (F-S3-020)
- When: Bình An gọi thẳng yêu cầu bật công tắc của 11A1
- Then: máy chủ trả FORBIDDEN; công tắc vẫn tắt


**US-S3E5-04-FC2 — Giáo viên khác không đổi được công tắc lớp 11A1** · Nguồn: BR-S3E5-40
- Given: thầy Khôi đã đăng nhập (F-S3-002)
- When: gọi thẳng yêu cầu tắt công tắc của 11A1
- Then: máy chủ trả NOT_FOUND; công tắc của 11A1 không đổi


**US-S3E5-04-FC3 — Tắt giữa lúc em đang hỏi: lượt đang chạy hoàn tất, lượt sau bị chặn** · Nguồn: BR-S3E5-45 · PO-66 (QA-S3E5-08)
- Given: Bình An vừa gửi một câu hỏi tới trợ lý, nhà cung cấp giả lập trả lời sau 2 giây (F-S3-020)
- When: ngay sau khi gửi, cô Lan tắt công tắc, rồi chờ trả lời, rồi Bình An gửi câu hỏi thứ hai
- Then: câu hỏi thứ nhất vẫn nhận trả lời và được lưu; câu hỏi thứ hai bị FORBIDDEN (AI_OFF_FOR_CLASS); bài làm của Bình An không mất dữ liệu


**US-S3E5-04-FC4 — Sửa giao diện để hiện lại nút vẫn không gọi được** · Nguồn: BR-S3E5-41
- Given: 11A1 tắt trợ lý; Bình An dùng công cụ nhà phát triển của trình duyệt để hiện lại khung trợ lý
- When: gửi câu hỏi từ khung vừa hiện lại
- Then: máy chủ trả FORBIDDEN; hiện MSG-S3-83; số lượt gọi nhà cung cấp bằng 0


**US-S3E5-04-FC5 — Tự học không lớp vẫn có trợ lý** · Nguồn: BR-S3E5-44 · PO-66 (QA-S3E5-08)
- Given: Văn Tú Đã rời 11A1 (tắt trợ lý), đang ở Tự học không thuộc lớp nào (F-S3-028)
- When: hỏi trợ lý ở một đề Tự học
- Then: lượt hỏi được nhận; công tắc của 11A1 không ảnh hưởng


**US-S3E5-04-FC6 — "AI gộp" của giáo viên không bị tắt theo** · Nguồn: BR-S3E5-46 · PO-66 (QA-S3E5-08)
- Given: 11A1 tắt trợ lý AI cho học sinh; Đề 3 đủ 5 bài làm (F-S3-051)
- When: mở Kết quả lớp
- Then: "Cả lớp vướng ở đâu" vẫn gộp Đề 3 như bình thường; bộ gộp giả lập được gọi đúng một lần


**US-S3E5-04-FC7 — Giá trị công tắc không phải đúng/sai** · Nguồn: BR-S3E5-40
- Given: cô Lan gọi API đổi công tắc của 11A1
- When: gửi giá trị "có", rồi gửi giá trị rỗng, rồi gửi giá trị 2
- Then: cả ba bị VALIDATION_FAILED; công tắc không đổi
- Dữ liệu nhập: "có" ; rỗng ; 2

## US-S3E5-05 — Xóa tài khoản và chính sách lưu/xóa dữ liệu bài làm
- **Story:** Là học sinh hoặc giáo viên, tôi muốn tự xóa tài khoản và biết dữ liệu bài làm được giữ bao lâu, để dữ liệu của mình (hoặc của con) không nằm lại ngoài ý muốn. · Loại UI · P0 · Độ khó 3
- **Nguồn:** PO-66 (G5; QA-S3E5-10, QA-S2E1-09); PO-64 (Q8: chính sách lưu và xóa làm trước L7 chặng 3); PO-61 (không thu dữ liệu cá nhân)
- **Góc nhìn người vận hành (M18):** phụ huynh nhắn thầy cô muốn xóa tài khoản của con; học sinh tự vào Cài đặt, bấm "Xóa tài khoản". Ca đáng kể: giáo viên còn lớp Đang dạy, gõ sai mật khẩu, bấm xóa hai lần, em đã xóa thử đăng nhập lại hoặc khôi phục, người khác thử xóa tài khoản của em.
- **Phụ thuộc:** US-S3E5-02 (văn bản thông báo riêng tư nêu chính sách này), US-S3E5-03 (nhật ký IP 7 ngày), US-S3E1-05 (quyền theo quan hệ), S2-E1 (đăng nhập, khôi phục). Tick "đã đọc" ở S2-E1 trỏ tới văn bản của US-S3E5-02.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E5-50 | Mọi tài khoản (giáo viên, học sinh, mọi trạng thái lớp) có nút "Xóa tài khoản" ở Cài đặt; xóa cần nhập lại mật khẩu và gõ chữ "XÓA" xác nhận. Ngoại lệ: tài khoản học sinh vào lớp bằng tên, không có mật khẩu (PO-61), xóa chỉ cần gõ "XÓA" xác nhận, không có bước mật khẩu và không có bộ đếm sai. | PO-64; PO-66 (G5); PO-72 |
| BR-S3E5-51 | Xóa có hiệu lực ngay phía người dùng: tài khoản không đăng nhập, không khôi phục được, mã người dùng và mã khôi phục hết hiệu lực và không cấp lại; dữ liệu của tài khoản (tên hiển thị, cờ đã đọc, bài làm, lượt hỏi AI, thành viên lớp) bị xóa vật lý trong tối đa 30 ngày. | PO-64; PO-66 (G5, G12: ngưỡng 30 ngày chỉnh sau L6) |
| BR-S3E5-52 | Giáo viên còn lớp Đang dạy không xóa được cho tới khi lưu trữ hết lớp (US-S3E6-02); học sinh xóa được ở mọi lúc. | PO-66 (G5) |
| BR-S3E5-53 | Sau khi xóa học sinh, bài làm của em biến mất khỏi bảng lớp và mọi số liệu; AuditEvent cũ giữ loại, giờ, đối tượng nhưng mã người dùng thay bằng "đã xóa" (nhật ký không chứa dữ liệu cá nhân, BR-S3E5-03). | PO-66 (G5); BR-S3E5-05 |
| BR-S3E5-54 | Chính sách lưu và xóa hiện trong văn bản thông báo riêng tư: bài làm giữ tới khi tài khoản bị xóa; nhật ký IP của máy chủ web không quá 7 ngày; xóa tài khoản thì xóa trong tối đa 30 ngày. Văn bản hiện hành là bản 1.0; trước cổng phát hành L7 mọi chỉnh nội dung sửa tại chỗ giữ số 1.0 (chưa có người dùng thật); từ khi phát hành, mỗi lần đổi nội dung phát hành bản mới (1.1, 1.2…) và người đã đọc phải đọc lại (US-S3E5-02-SC4); tập tài khoản "phải đọc" nạp lúc khởi động, nên sau lệnh chuyển tài khoản MVP phải khởi động lại máy chủ. | PO-64; BR-S3E5-37; PO-67 |
| BR-S3E5-55 | Xóa tài khoản idempotent theo khóa yêu cầu; chỉ chủ tài khoản xóa được tài khoản của mình, người khác trả NOT_FOUND; sai mật khẩu khi xác nhận tính vào bộ đếm khóa 10 lần/15 phút (BR-S2E1-09); tài khoản không có mật khẩu (vào lớp bằng tên) bỏ qua bước này và không tăng bộ đếm nào (BR-S3E5-50). | BR-S3E2-06; QA-S3E1-04; PO-72 |

### Tiêu chí chấp nhận

**US-S3E5-05-SC1 — Học sinh xóa tài khoản của mình** · Nguồn: BR-S3E5-50, BR-S3E5-51, BR-S3E5-53
- Given: Bình An `HS-4P9T-37WD` Đang học 11A1, đã có bài làm ở bộ tuần 15 (F-S3-020, F-S3-050)
- When: Bình An vào Cài đặt, bấm "Xóa tài khoản", nhập đúng mật khẩu, gõ "XÓA", xác nhận
- Then: tài khoản bị xóa; đăng nhập lại bị từ chối với câu chung; bảng lớp 11A1 không còn dòng Bình An và số liệu bộ tuần 15 tính lại không có em

**US-S3E5-05-SC2 — Giáo viên không còn lớp Đang dạy xóa được tài khoản** · Nguồn: BR-S3E5-50, BR-S3E5-52
- Given: thầy Khôi (F-S3-002) đã lưu trữ lớp 10B, không còn lớp Đang dạy
- When: xóa tài khoản đúng quy trình
- Then: tài khoản bị xóa; mã `GV-4D8R-2WNX` không đăng nhập, không khôi phục, không cấp lại cho người khác

**US-S3E5-05-SC3 — Văn bản thông báo riêng tư nêu chính sách lưu và xóa** · Nguồn: BR-S3E5-54
- Given: màn thông báo riêng tư bản hiện hành (MSG-S3-81)
- When: mở màn
- Then: có ba ý: bài làm giữ tới khi xóa tài khoản; nhật ký IP của máy chủ web không quá 7 ngày; xóa tài khoản thì xóa trong tối đa 30 ngày; có liên kết tới Cài đặt "Xóa tài khoản"

**US-S3E5-05-SC4 — Dữ liệu của tài khoản đã xóa bị xóa vật lý trong 30 ngày** · Nguồn: BR-S3E5-51; BR-S3E5-53
- Given: tài khoản Bình An đã xóa ở ngày T
- When: chạy công việc dọn dữ liệu ở ngày T + 30
- Then: không còn bản ghi nào trong cơ sở dữ liệu mang mã người dùng, tên hiển thị, bài làm, lượt hỏi AI của em; AuditEvent cũ còn nhưng mã người dùng là "đã xóa"

**US-S3E5-05-FC1 — Giáo viên còn lớp Đang dạy bị chặn xóa** · Nguồn: BR-S3E5-52
- Given: cô Lan còn lớp 11A1 và 11A2 Đang dạy (F-S3-001)
- When: cô Lan xóa tài khoản đúng quy trình
- Then: CONFLICT kèm danh sách lớp còn Đang dạy; tài khoản không đổi

**US-S3E5-05-FC2 — Sai mật khẩu không xóa được** · Nguồn: BR-S3E5-55
- Given: Bình An ở hộp xác nhận xóa
- When: nhập sai mật khẩu, gõ "XÓA"
- Then: từ chối, tài khoản không đổi; lần sai tính vào bộ đếm khóa đăng nhập của em

**US-S3E5-05-FC3 — Chưa gõ XÓA thì nút xác nhận bị khóa** · Nguồn: BR-S3E5-50
- Given: Bình An ở hộp xác nhận xóa, đã nhập đúng mật khẩu
- When: để trống ô xác nhận hoặc gõ "xoa"; rồi gọi API xóa thẳng không có chữ xác nhận
- Then: nút xác nhận bị khóa; API trả VALIDATION_FAILED; tài khoản không đổi

**US-S3E5-05-FC4 — Người khác không xóa được tài khoản của em** · Nguồn: BR-S3E5-55; QA-S3E1-04
- Given: cô Lan là chủ lớp 11A1 của Bình An
- When: cô Lan gọi xóa tài khoản của Bình An
- Then: NOT_FOUND; tài khoản Bình An không đổi

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E5, ready for R3 | runner-T-261126-14 (BA) |
| v0.2 | 2026-11-26 | Sửa theo RV_s23: BR-S3E5-30 thêm thực thể chặng 2, BR-S3E5-33 ngoại lệ ảnh đề, BR-S3E5-37 ngoại lệ bộ đếm tần suất; đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | G5: story mới US-S3E5-05 (xóa tài khoản, chính sách lưu/xóa) gồm BR-S3E5-50…55 và 8 AC/TC; G3: văn bản nêu mã khôi phục của học sinh; bỏ nhãn giả định chưa chốt (PO-66) | runner-T-261126-37 (BA/QC) |
| v0.4 | 2026-11-27 | Chốt PO-67/71/72/76: văn bản riêng tư 1.0 sửa tại chỗ tới L7 rồi mới tăng phiên bản (BR-S3E5-54); BR-S3E5-07 ≤1 hàng Từ chối/giờ/(người, loại); BR-S3E5-37 hàng HMAC ≤1 giờ; BR-S3E5-50/55 học sinh vào lớp bằng tên xóa chỉ gõ XÓA | runner-po-s3 (PO/BA) |
