Phiên bản: v0.3 · 2026-11-26
# S3-E1 — Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu, quyền theo quan hệ: story Dev-Ready và tiêu chí chấp nhận

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
| Chờ duyệt · Đang học · Đã rời · Bị từ chối | trạng thái `ClassMembership` (Bị từ chối được nhận vào mô hình theo PO-66 G5, đã đóng QA-S3E1-02) | tệp này |
| Đúng · Xem lại · Đang làm · Chưa làm | bốn trạng thái ô của bảng lớp × bài (nghĩa ở US-S3E3-01) | `Design/s23/c3-04-ket-qua-lop.png`, DQ-21 |
| Bài làm | một học sinh Đang học có ít nhất một tương tác (gửi đáp số, xin gợi ý hoặc hỏi) trên một đề của bộ; tính theo số học sinh khác nhau | US-S3E3-03, QA-S3E3-02 |
| T0 · t | giờ máy chủ lúc bắt đầu ca kiểm · giờ máy chủ tại thời điểm xét | mọi mốc giờ là tương đối |
| `CL-FN-nn` · `CL-UI-nn` · `CL-X-nn` | nhóm kiểm tra chức năng · giao diện và thiết bị · xuyên suốt (ma trận quyền, trường dữ liệu, tải) | `test_design.md` cùng thư mục |
| PO-66 · ENG · X | kỳ vọng theo giả định đã chốt PO-66 (kèm mã QA-) · case cần công cụ kỹ thuật · case xuyên suốt | cột Remark |
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


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E1-06)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-01 | Inline | "Mã lớp chưa đúng hoặc lớp đã khóa. Em hỏi lại thầy cô nhé." | VALIDATION_FAILED |
| MSG-S3-02 | Page state | "Em đang chờ thầy cô duyệt. Trong lúc chờ, em vẫn dùng Tự học." | — |
| MSG-S3-03 | Inline | "Em đã ở trong lớp này rồi." | CONFLICT |
| MSG-S3-04 | Inline | "Tên lớp chưa có hoặc dài quá 60 ký tự." | VALIDATION_FAILED |
| MSG-S3-05 | Inline | "Mật khẩu tạm đã hết hạn. Nhờ thầy cô đặt lại." | VALIDATION_FAILED |
| MSG-S3-06 | Toast | "Bạn không có quyền thực hiện thao tác này." | FORBIDDEN |
| MSG-S3-07 | Page state | "Không tìm thấy nội dung này." | NOT_FOUND |
| MSG-S3-08 | Inline | "Đã thử quá nhiều lần. Vui lòng thử lại sau ít phút." | RATE_LIMITED |

## US-S3E1-01 — Giáo viên tạo lớp (tên, cấp, năm học), sinh mã lớp và trang in
- **Story:** Là giáo viên, tôi muốn tạo lớp của mình và nhận mã lớp cùng trang in, để học sinh vào lớp mà tôi không phải nhập từng em. · Loại UI · P0 · Độ khó 2
- **Nguồn:** story map US-S3E1-01; STAGES Ch3 §5 luồng A bước 2, §8 (`Class.academic_year`, `Class.status`); DQ-23, DQ-24; ảnh `c3-07-ma-lop.png`
- **Góc nhìn người vận hành (M18):** cô Lan đầu năm học tạo ba lớp, in một trang A4 cho mỗi lớp rồi dán lên bảng. Ca đáng kể: mã lớp bị chụp ảnh gửi ra ngoài nhóm Zalo (đặt lại mã), tạo nhầm tên lớp, một giáo viên có nhiều lớp.
- **Phụ thuộc:** S2-E1 (đăng ký, đăng nhập mã + mật khẩu); US-S3E1-05 (quyền theo quan hệ).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E1-01 | Chỉ tài khoản có vai Giáo viên tạo được lớp; người tạo là chủ lớp duy nhất (nhiều giáo viên một lớp ngoài phạm vi). | Ch3 §4 ngoài phạm vi; PO-61 |
| BR-S3E1-02 | Tên lớp 1 đến 60 ký tự sau khi bỏ khoảng trắng đầu và cuối; cấp học chọn từ danh sách (Lớp 6 … Lớp 12); năm học dạng `YYYY–YYYY` với năm sau bằng năm trước cộng 1. | Ch3 §8; QA-S3E1-03 |
| BR-S3E1-03 | Mã lớp dạng `HK-` cộng 4 ký tự chữ hoa hoặc số, bỏ ký tự dễ nhầm (0, O, 1, I); duy nhất toàn hệ thống; sinh ở máy chủ. | DQ-23; QA-S3E1-03 |
| BR-S3E1-04 | Một giáo viên có tối đa 20 lớp Đang dạy; lớp đã lưu trữ không tính. | QA-S3E1-05 |
| BR-S3E1-05 | Đặt lại mã lớp làm mã cũ hết hiệu lực ngay; thành viên hiện có không bị ảnh hưởng. | DQ-23 |
| BR-S3E1-06 | Công tắc "Cho vào lớp bằng mã" tắt thì mã bị từ chối như mã đã khóa; bước "Giáo viên duyệt từng em" là bắt buộc, không có công tắc tắt (chống dò mã lớp). | DQ-23; PO-66 (G4) |
| BR-S3E1-16 | Lớp MVP đã chuyển ở US-S2E1-04 (lớp 11A1 và học sinh có mã `HS-`) thành lớp theo mô hình chặng 3: chủ lớp là tài khoản `GV-` của "Giáo viên mẫu", sinh mã lớp `HK-`, mỗi học sinh một `ClassMembership` Đang học; lệnh chạy lại được và không chạy trên prod (đã chốt PO-66, RV_s23 M-02) | PO-61; BR-S2E1-13 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E1-03, QA-S3E1-05.

### Tiêu chí chấp nhận

**US-S3E1-01-SC1 — Tạo lớp hợp lệ, nhận mã lớp và trang in** · Nguồn: REQ Ch3 §5 luồng A; DQ-24
- Given: cô Lan đã đăng nhập, chưa có lớp tên "11B3" (F-S3-001); đang ở màn Lớp học
- When: nhập Tên lớp "11B3", Cấp "Lớp 11", Năm học "2026–2027", rồi bấm "Tạo lớp"
- Then: lớp "11B3" xuất hiện ở trạng thái Đang dạy với 0 học sinh; có mã lớp dạng `HK-` cộng 4 ký tự hợp lệ; nút "In trang hướng dẫn" mở một trang A4 có tên lớp, mã QR, mã lớp cỡ lớn và bốn bước vào lớp; thân phản hồi không có trường nào ngoài tên lớp, cấp, năm học, mã lớp, trạng thái
- Dữ liệu nhập: "11B3" ; "Lớp 11" ; "2026–2027"


**US-S3E1-01-SC2 — Một giáo viên có nhiều lớp, mỗi lớp một mã khác nhau** · Nguồn: Ch3 §3 ý 3; Ch3 §8
- Given: cô Lan có lớp 11A1 và 11A2 (F-S3-010, F-S3-011)
- When: mở Lớp học
- Then: thấy đúng hai thẻ lớp của cô là 11A1 mã `HK-4P7Q` và 11A2 mã `HK-8M3C`; không thấy lớp 10B của thầy Khôi (F-S3-012)


**US-S3E1-01-SC3 — Tên lớp đúng 60 ký tự được nhận** · Nguồn: BR-S3E1-02
- Given: cô Lan đang tạo lớp
- When: nhập Tên lớp gồm 60 ký tự "A", Cấp "Lớp 10", Năm học "2026–2027", rồi bấm "Tạo lớp"
- Then: lớp được tạo; tên hiển thị đủ 60 ký tự, không bị cắt
- Dữ liệu nhập: 60 ký tự "A"


**US-S3E1-01-SC4 — Đặt lại mã lớp: mã cũ hết hiệu lực, thành viên giữ nguyên** · Nguồn: DQ-23; BR-S3E1-05
- Given: lớp 11A1 có mã `HK-4P7Q` và 8 em Đang học (F-S3-010)
- When: bấm "Đặt lại mã lớp", rồi xác nhận
- Then: lớp 11A1 có mã mới khác `HK-4P7Q` (dạng `HK-` cộng 4 ký tự); mã `HK-4P7Q` không còn dùng được ở màn Vào lớp; 8 em Đang học vẫn thấy bài của lớp; trang in hiện mã mới


**US-S3E1-01-SC5 — Tắt "Cho vào lớp bằng mã" thì mã bị khóa, bật lại thì dùng được** · Nguồn: DQ-23; BR-S3E1-06
- Given: lớp 11A2 đang bật "Cho vào lớp bằng mã" (F-S3-011)
- When: tắt công tắc, rồi học sinh mới nhập `HK-8M3C`, rồi bật lại công tắc, rồi học sinh đó nhập lại `HK-8M3C`
- Then: lần nhập đầu bị từ chối với MSG-S3-01 và không tạo bản ghi; lần nhập sau tạo yêu cầu vào lớp ở trạng thái Chờ duyệt


**US-S3E1-01-FC1 — Tên lớp rỗng** · Nguồn: BR-S3E1-02
- Given: cô Lan đang tạo lớp
- When: để trống Tên lớp, chọn Cấp "Lớp 11" và Năm học "2026–2027", rồi bấm "Tạo lớp"
- Then: dưới ô Tên lớp hiện đúng MSG-S3-04 "Tên lớp chưa có hoặc dài quá 60 ký tự."; máy chủ trả VALIDATION_FAILED nếu gọi thẳng; không tạo lớp mới


**US-S3E1-01-FC2 — Tên lớp 61 ký tự** · Nguồn: BR-S3E1-02
- Given: cô Lan đang tạo lớp
- When: nhập Tên lớp gồm 61 ký tự "A", rồi bấm "Tạo lớp"
- Then: hiện MSG-S3-04; VALIDATION_FAILED nếu gọi thẳng; không tạo lớp
- Dữ liệu nhập: 61 ký tự "A"


**US-S3E1-01-FC3 — Năm học sai quy tắc** · Nguồn: BR-S3E1-02; QA-S3E1-03 · PO-66 (QA-S3E1-03)
- Given: cô Lan đang tạo lớp
- When: nhập Tên lớp "11B4", Năm học "2026–2028", rồi bấm "Tạo lớp"
- Then: máy chủ trả VALIDATION_FAILED với `details` chỉ ra trường năm học; không tạo lớp
- Dữ liệu nhập: "2026–2028"


**US-S3E1-01-FC4 — Học sinh không tạo được lớp** · Nguồn: BR-S3E1-01; PO-61
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi thẳng yêu cầu tạo lớp với tên "Lớp lậu"
- Then: máy chủ trả FORBIDDEN; không có lớp nào được tạo; trên giao diện học sinh không có nút tạo lớp


**US-S3E1-01-FC5 — Chặn quá 20 lớp Đang dạy** · Nguồn: BR-S3E1-04; QA-S3E1-05 · PO-66 (QA-S3E1-05)
- Given: một giáo viên đã có đúng 20 lớp Đang dạy
- When: tạo lớp thứ 21
- Then: máy chủ trả VALIDATION_FAILED với `details` nêu giới hạn 20 lớp; sau khi lưu trữ một lớp, tạo lớp thứ 21 thành công


**US-S3E1-01-FC6 — Mã lớp sinh không trùng mã đang có** · Nguồn: BR-S3E1-03
- Given: bộ sinh mã được giả lập trả về `HK-4P7Q` ở lần đầu rồi `HK-9R2D` ở lần sau
- When: tạo lớp mới khi `HK-4P7Q` đang thuộc lớp 11A1
- Then: lớp mới nhận `HK-9R2D`; không có hai lớp cùng mã; ràng buộc duy nhất của cơ sở dữ liệu từ chối bản ghi trùng nếu chèn thẳng


**US-S3E1-01-SC6 — Lớp MVP đã chuyển thành lớp thật có chủ lớp và mã lớp** · Nguồn: BR-S3E1-16; US-S2E1-04
- Given: lệnh chuyển dữ liệu MVP của US-S2E1-04 đã chạy: lớp 11A1 của "Giáo viên mẫu" có 5 học sinh mang mã `HS-`
- When: chạy lệnh chuyển hồ sơ lớp; mở màn Lớp học bằng tài khoản "Giáo viên mẫu"; chạy lệnh lần thứ hai
- Then: lớp 11A1 có mã lớp dạng `HK-xxxx`, chủ lớp là "Giáo viên mẫu"; 5 học sinh có `ClassMembership` Đang học; tiến độ và bài làm cũ giữ nguyên; lần hai không tạo thêm lớp, mã lớp hay thành viên

## US-S3E1-02 — Học sinh nhập mã lớp, trạng thái chờ duyệt; mã sai hoặc đã khóa
- **Story:** Là học sinh, tôi muốn nhập mã lớp thầy cô đọc và chờ thầy cô duyệt, để vào đúng lớp mà không phải khai báo gì thêm. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E1-02; STAGES Ch3 §5 luồng A bước 3; DQ-23, DQ-27; PO-53 (khóa theo tài khoản); ảnh `c3-07-vao-lop.png`
- **Góc nhìn người vận hành (M18):** học sinh ngồi ở lớp, gõ mã trên iPad theo bảng. Ca đáng kể: gõ chữ thường, gõ thừa dấu cách, gõ sai nhiều lần, bấm hai lần, vào lại lớp sau khi đã rời.
- **Phụ thuộc:** US-S3E1-01 (mã lớp), US-S3E1-03 (duyệt).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E1-10 | Chỉ tài khoản vai Học sinh nhập mã lớp; mã chuẩn hóa: bỏ khoảng trắng, chữ hoa; máy chủ suy ra lớp từ mã. | DQ-27 |
| BR-S3E1-11 | Mã đúng và lớp nhận người mới: tạo `ClassMembership` Chờ duyệt; không có đường vào thẳng Đang học, mọi em phải được chủ lớp duyệt. | DQ-23; PO-66 (G4) |
| BR-S3E1-12 | Trong lúc Chờ duyệt, học sinh không thấy bài của lớp nhưng vẫn dùng Tự học. | DQ-27; PO-65 (Q9) |
| BR-S3E1-13 | Mã sai, mã cũ sau khi đặt lại và mã bị khóa cùng một thông điệp, không nói lớp có tồn tại hay không. | PO-61; chống dò mã |
| BR-S3E1-14 | Nhập sai 10 lần trong 15 phút theo tài khoản học sinh thì RATE_LIMITED; đếm theo tài khoản, không theo mã lớp. | PO-53; D-GEO-185; QA-S3E1-07 |
| BR-S3E1-15 | Một học sinh có tối đa một bản ghi Chờ duyệt hoặc Đang học cho mỗi lớp; gửi lặp không tạo thêm. | CL-FN-10 |
| BR-S3E1-17 | Ngoài giới hạn 10 lần sai/15 phút (BR-S3E1-14), mỗi tài khoản học sinh có giới hạn tổng lượt nhập mã lớp sai: 30 lần trong 24 giờ; chạm ngưỡng thì RATE_LIMITED tới khi lần sai cũ nhất rời cửa sổ 24 giờ. Hai giới hạn cùng hiệu lực, cùng thông điệp MSG-S3-08; đếm theo tài khoản (ngưỡng chỉnh sau L6, PO-66 G12). | PO-66 (G4); RV_s23 M-08 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E1-02, QA-S3E1-07, QA-S3E1-08.

### Tiêu chí chấp nhận

**US-S3E1-02-SC1 — Nhập mã đúng, vào trạng thái Chờ duyệt** · Nguồn: Ch3 §5 luồng A; DQ-27
- Given: Hoàng Nam đã đăng nhập vai Học sinh và chưa thuộc lớp nào (F-S3-030); lớp 11A1 bật "Cho vào lớp bằng mã" (F-S3-010)
- When: mở Vào lớp, rồi gõ mã `HK-4P7Q`, rồi bấm "Vào lớp"
- Then: hiện màn chờ với MSG-S3-02; lớp 11A1 hiện 3 em Chờ duyệt (F-S3-030…032) ở màn của cô Lan; Hoàng Nam không thấy bộ bài nào của 11A1 và vẫn mở được Tự học
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-SC2 — Mã gõ chữ thường và có khoảng trắng vẫn được nhận** · Nguồn: BR-S3E1-10
- Given: học sinh Thu Hà chưa thuộc lớp nào (F-S3-031)
- When: gõ mã "  hk-4p7q " (khoảng trắng đầu cuối, chữ thường), rồi bấm "Vào lớp"
- Then: mã được chuẩn hóa thành `HK-4P7Q`; tạo yêu cầu Chờ duyệt ở 11A1
- Dữ liệu nhập: "  hk-4p7q "


**US-S3E1-02-SC3 — Bấm gửi hai lần không tạo hai yêu cầu** · Nguồn: BR-S3E1-15
- Given: Hoàng Nam đã gửi mã `HK-4P7Q` và đang Chờ duyệt (F-S3-030)
- When: gửi lại cùng yêu cầu vào lớp hai lần liên tiếp
- Then: ở màn của cô Lan vẫn đúng một dòng chờ duyệt của Hoàng Nam `HS-7Q8Z-48DF`; học sinh thấy lại MSG-S3-02, không báo lỗi


**US-S3E1-02-SC4 — Mọi lớp bắt buộc giáo viên duyệt: mã đúng luôn vào Chờ duyệt** · Nguồn: DQ-23; BR-S3E1-11; PO-66 (G4)
- Given: lớp 11A2 (F-S3-011) có 3 em Đang học; Thu Hà chưa thuộc lớp nào (F-S3-031)
- When: gõ mã `HK-8M3C`, rồi bấm "Vào lớp"; rồi gọi API cập nhật lớp 11A2 với tham số tắt bước duyệt
- Then: Thu Hà ở trạng thái Chờ duyệt, chưa thấy bài của 11A2; lớp 11A2 vẫn 3 em Đang học và có một dòng chờ duyệt; API không nhận tham số tắt duyệt (VALIDATION_FAILED)
- Dữ liệu nhập: `HK-8M3C`


**US-S3E1-02-SC5 — Em đã rời xin vào lại lớp** · Nguồn: Ch3 §8 `ClassMembership` · PO-66 (QA-S3E1-02)
- Given: Văn Tú Đã rời 11A1 (F-S3-028)
- When: gõ mã `HK-4P7Q`, rồi bấm "Vào lớp"
- Then: Văn Tú ở trạng thái Chờ duyệt mới; dữ liệu bài làm cũ vẫn nằm ở Kết quả lớp với dấu "đã rời" cho tới khi cô duyệt
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-FC1 — Mã sai** · Nguồn: BR-S3E1-13
- Given: học sinh Thu Hà chưa thuộc lớp nào (F-S3-031)
- When: gõ mã `HK-ZZZZ`, rồi bấm "Vào lớp"
- Then: dưới ô mã hiện đúng MSG-S3-01; máy chủ trả VALIDATION_FAILED; không tạo bản ghi vào lớp; thông điệp không nêu tên lớp hay giáo viên nào
- Dữ liệu nhập: `HK-ZZZZ`


**US-S3E1-02-FC2 — Mã đã khóa (công tắc tắt)** · Nguồn: BR-S3E1-06; BR-S3E1-13
- Given: lớp 11A2 tắt "Cho vào lớp bằng mã" (F-S3-011)
- When: gõ mã `HK-8M3C`, rồi bấm "Vào lớp"
- Then: hiện đúng MSG-S3-01, cùng câu chữ và cùng mã lỗi với trường hợp mã sai; không tạo bản ghi


**US-S3E1-02-FC3 — Mã cũ sau khi cô đặt lại mã** · Nguồn: BR-S3E1-05; BR-S3E1-13
- Given: cô Lan đã đặt lại mã lớp 11A1 từ `HK-4P7Q` sang mã mới (F-S3-010)
- When: học sinh gõ mã cũ `HK-4P7Q`, rồi bấm "Vào lớp"
- Then: hiện MSG-S3-01; mã mới vẫn vào được
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-FC4 — Dò mã: quá 10 lần sai trong 15 phút theo tài khoản** · Nguồn: BR-S3E1-14; PO-53; PO-66
- Given: Thu Hà đã nhập sai mã 10 lần trong 15 phút (F-S3-031); đồng hồ máy chủ ở T0
- When: nhập lần thứ 11 với mã đúng `HK-4P7Q` ở T0 cộng 14 phút, rồi nhập lại ở T0 cộng 16 phút
- Then: lần 11 bị RATE_LIMITED kèm `Retry-After` và MSG-S3-08, không tạo bản ghi dù mã đúng; lần sau 16 phút vào được bình thường; đếm theo tài khoản, đổi mã lớp đích không reset bộ đếm
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-FC5 — Chín lần sai vẫn chưa bị khóa (biên dưới)** · Nguồn: BR-S3E1-14 · PO-66 (QA-S3E1-07)
- Given: Thu Hà đã nhập sai mã đúng 9 lần trong 15 phút (F-S3-031)
- When: nhập lần thứ 10 với mã đúng `HK-4P7Q`
- Then: yêu cầu được xử lý bình thường, tạo Chờ duyệt, không RATE_LIMITED
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-FC8 — Dò mã chậm: đủ 30 lần sai trong 24 giờ thì khóa theo tổng lượt** · Nguồn: BR-S3E1-17; PO-66 (G4)
- Given: Thu Hà đã nhập sai 30 lần trong 24 giờ, mỗi 15 phút dưới 10 lần nên chưa chạm giới hạn ngắn (F-S3-031)
- When: nhập lần thứ 31 với mã đúng `HK-4P7Q`; nhập lại sau khi lần sai cũ nhất đã quá 24 giờ
- Then: Lần 31 bị RATE_LIMITED với MSG-S3-08, không tạo bản ghi dù mã đúng; lần sau vào được bình thường; đổi mã lớp đích không reset bộ đếm

**US-S3E1-02-BV1 — 29 lần sai trong 24 giờ chưa bị khóa theo tổng lượt (biên dưới)** · Nguồn: BR-S3E1-17; PO-66 (G4)
- Given: Thu Hà đã nhập sai 29 lần trong 24 giờ, không lúc nào quá 9 lần trong 15 phút (F-S3-031)
- When: nhập lần thứ 30 với mã đúng `HK-4P7Q`
- Then: Yêu cầu được xử lý bình thường, tạo Chờ duyệt, không RATE_LIMITED

**US-S3E1-02-FC6 — Em đã Đang học lớp này nhập lại mã** · Nguồn: BR-S3E1-15
- Given: Bình An đang Đang học 11A1 (F-S3-020)
- When: gõ mã `HK-4P7Q`, rồi bấm "Vào lớp"
- Then: hiện MSG-S3-03; máy chủ trả CONFLICT; lớp 11A1 vẫn đúng 8 em Đang học
- Dữ liệu nhập: `HK-4P7Q`


**US-S3E1-02-FC7 — Tài khoản giáo viên không nhập được mã lớp** · Nguồn: BR-S3E1-10
- Given: cô Lan đã đăng nhập vai Giáo viên (F-S3-001)
- When: gọi thẳng yêu cầu vào lớp với mã `HK-5T9X` của lớp 10B
- Then: máy chủ trả FORBIDDEN; không có bản ghi thành viên nào cho cô Lan


## US-S3E1-03 — Giáo viên duyệt danh sách chờ; nhiều lớp; ClassMembership (chờ, đang học, đã rời)
- **Story:** Là giáo viên, tôi muốn duyệt hoặc từ chối từng em xin vào lớp và cho em rời lớp, để lớp chỉ có đúng học sinh của tôi. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E1-03; STAGES Ch3 §5 luồng A bước 4, §8; DQ-25; ảnh `c3-07-duyet-dat-lai-mk.png`
- **Góc nhìn người vận hành (M18):** cô Lan mở danh sách chờ đầu tiết, thấy hai em cùng tên Hoàng Nam, phân biệt bằng mã người dùng và giờ gửi. Ca đáng kể: hai tab cùng duyệt một em, em xóa tài khoản lúc đang chờ, em thuộc hai lớp của hai giáo viên.
- **Phụ thuộc:** US-S3E1-02; US-S3E5-01 (không có AuditEvent cho duyệt ở chặng 3, QA-S3E1-09).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E1-20 | `ClassMembership` có bốn trạng thái: Chờ duyệt, Đang học, Đã rời, Bị từ chối; chỉ chủ lớp đổi trạng thái. | Ch3 §8; QA-S3E1-02 |
| BR-S3E1-21 | Dòng chờ duyệt và bảng lớp hiện tên hiển thị kèm mã người dùng; không hỏi ký hiệu phân biệt. | DQ-25; brd-delta §2 mục 5 |
| BR-S3E1-22 | Duyệt hoặc từ chối là idempotent: lặp lại cùng quyết định không đổi gì; quyết định khác với trạng thái hiện có là CONFLICT. | CL-FN-10 |
| BR-S3E1-23 | Cho em rời lớp: trạng thái Đã rời; bài làm cũ giữ nguyên cho giáo viên (dấu "đã rời"); em hết quyền xem bài mới của lớp. | Ch3 §5 ngoại lệ luồng C |
| BR-S3E1-24 | Một học sinh có thể thuộc nhiều lớp của nhiều giáo viên; mỗi giáo viên chỉ thấy phần thuộc lớp mình. | Ch3 §7 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E1-02.

### Tiêu chí chấp nhận

**US-S3E1-03-SC1 — Duyệt một em, em thấy bài của lớp** · Nguồn: Ch3 §5 luồng A bước 4
- Given: Thu Hà Chờ duyệt ở 11A1 (F-S3-031); 11A1 có bộ "Bộ bài tuần 15" Đã giao (F-S3-050)
- When: ở Chờ duyệt (3), bấm "Duyệt" cạnh Thu Hà
- Then: Thu Hà chuyển sang Đang học và xuất hiện trong "Học sinh trong lớp" (9 em); Chờ duyệt còn 2; khi Thu Hà mở Bài của em thấy "Bộ bài tuần 15"


**US-S3E1-03-SC2 — Hai em trùng tên được phân biệt bằng mã người dùng** · Nguồn: DQ-25; BR-S3E1-21
- Given: hai em tên "Hoàng Nam" Chờ duyệt ở 11A1: `HS-7Q8Z-48DF` gửi lúc 15:10 và `HS-9B2L-85VG` gửi lúc 16:10 (F-S3-030, F-S3-032)
- When: mở danh sách Chờ duyệt, rồi duyệt dòng có mã `HS-9B2L-85VG`
- Then: mỗi dòng hiện tên và mã người dùng của em; chỉ em `HS-9B2L-85VG` thành Đang học, em `HS-7Q8Z-48DF` vẫn Chờ duyệt; không có hộp hỏi ký hiệu phân biệt


**US-S3E1-03-SC3 — Từ chối một em** · Nguồn: Ch3 §5; DQ-27 · PO-66 (QA-S3E1-02)
- Given: Hoàng Nam `HS-7Q8Z-48DF` Chờ duyệt ở 11A1 (F-S3-030)
- When: bấm "Từ chối" cạnh em đó
- Then: dòng biến khỏi Chờ duyệt, trạng thái của em ở 11A1 là Bị từ chối; em không thấy bài của lớp và màn của em hiện câu cho biết chưa vào được lớp, gợi ý hỏi lại giáo viên


**US-S3E1-03-SC4 — Cho em rời lớp, dữ liệu cũ vẫn ở Kết quả lớp** · Nguồn: BR-S3E1-23
- Given: Gia Bảo Đang học 11A1 với bài làm ở "Bộ bài tuần 15" (F-S3-023, F-S3-051)
- When: ở "Học sinh trong lớp" bấm "Cho rời lớp" cạnh Gia Bảo, rồi xác nhận
- Then: Gia Bảo chuyển Đã rời; Gia Bảo không còn thấy bài mới của 11A1; ở Kết quả lớp hàng Gia Bảo vẫn còn các ô cũ và có dấu "đã rời"


**US-S3E1-03-SC5 — Em thuộc hai lớp của hai giáo viên, mỗi giáo viên chỉ thấy lớp mình** · Nguồn: BR-S3E1-24
- Given: Chi Mai Đang học cả 11A1 của cô Lan và 10B của thầy Khôi (F-S3-041, F-S3-040)
- When: cô Lan mở danh sách học sinh 11A1, rồi thầy Khôi mở danh sách học sinh 10B
- Then: cô Lan thấy Chi Mai với trạng thái và bài làm của 11A1 và không thấy bài làm hay tiến độ của Chi Mai ở 10B; thầy Khôi thấy Chi Mai chỉ ở 10B


**US-S3E1-03-FC1 — Giáo viên khác không duyệt được lớp của cô Lan** · Nguồn: BR-S3E1-20; PO-61
- Given: thầy Khôi đã đăng nhập (F-S3-002); Thu Hà Chờ duyệt ở 11A1 (F-S3-031)
- When: thầy Khôi gọi thẳng yêu cầu duyệt Thu Hà ở lớp 11A1
- Then: máy chủ trả NOT_FOUND, không tiết lộ lớp 11A1 tồn tại; Thu Hà vẫn Chờ duyệt; thân phản hồi không chứa tên hiển thị hay mã người dùng của em nào


**US-S3E1-03-FC2 — Hai tab cùng duyệt một em** · Nguồn: BR-S3E1-22
- Given: cô Lan mở danh sách chờ ở hai tab, cùng thấy Thu Hà Chờ duyệt (F-S3-031)
- When: tab một bấm "Duyệt", rồi tab hai bấm "Từ chối" cho cùng em
- Then: yêu cầu của tab hai bị CONFLICT; Thu Hà vẫn Đang học; tab hai tải lại danh sách và không còn dòng của Thu Hà


**US-S3E1-03-FC3 — Duyệt lặp cùng quyết định** · Nguồn: BR-S3E1-22
- Given: Thu Hà vừa được duyệt ở 11A1 (F-S3-031)
- When: gửi lại đúng yêu cầu duyệt Thu Hà hai lần nữa
- Then: cả hai lần thành công như lần đầu; lớp vẫn đúng một bản ghi Đang học của Thu Hà


**US-S3E1-03-FC4 — Học sinh không duyệt được** · Nguồn: BR-S3E1-20
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020); Thu Hà Chờ duyệt (F-S3-031)
- When: Bình An gọi thẳng yêu cầu duyệt Thu Hà ở 11A1
- Then: máy chủ trả FORBIDDEN; Thu Hà vẫn Chờ duyệt


**US-S3E1-03-FC5 — Duyệt em đã xóa tài khoản** · Nguồn: CL-FN-10
- Given: Hoàng Nam `HS-7Q8Z-48DF` đã xóa tài khoản trong lúc đang Chờ duyệt (F-S3-030)
- When: cô Lan bấm "Duyệt" ở dòng cũ chưa tải lại
- Then: máy chủ trả NOT_FOUND; danh sách tải lại không còn dòng đó; không có bản ghi mồ côi


## US-S3E1-04 — Giáo viên đặt lại mật khẩu cho học sinh lớp mình
- **Story:** Là giáo viên, tôi muốn đặt lại mật khẩu cho học sinh trong lớp mình khi em quên, để em không phải bỏ dùng. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E1-04; PO-61 (chốt 26/11: giáo viên đặt lại cho học sinh lớp mình); STAGES Ch3 §7, §10; DQ-17, DQ-26; ảnh `c3-07-duyet-dat-lai-mk.png`
- **Góc nhìn người vận hành (M18):** học sinh quên mật khẩu trước giờ kiểm tra, cô Lan đặt lại trong màn Lớp học và đọc mật khẩu tạm cho em. Ca đáng kể: cô đặt nhầm em, mật khẩu tạm bị gửi qua nhóm chat chung, em đã rời lớp vẫn xin đặt lại, em thuộc hai lớp.
- **Phụ thuộc:** US-S3E1-05 (quyền theo quan hệ); US-S3E5-01 (AuditEvent PASSWORD_RESET); S2-E1 (đăng nhập).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E1-30 | Chỉ chủ lớp đặt lại mật khẩu của học sinh Đang học trong lớp đó; hết quan hệ (Đã rời, Chờ duyệt, Bị từ chối) thì hết quyền. | PO-61; Ch3 §7 |
| BR-S3E1-31 | Đặt lại sinh mật khẩu tạm 8 ký tự chữ thường và số, hiện một lần dạng nhóm 3-3-2; mật khẩu cũ và mọi phiên đang đăng nhập của em bị hủy ngay. | DQ-26; QA-S3E1-10 |
| BR-S3E1-32 | Mật khẩu tạm chỉ lưu dạng băm, không trả lại lần thứ hai ở bất kỳ API nào, không vào nhật ký hay AuditEvent. | DQ-26 |
| BR-S3E1-33 | Lần đăng nhập đầu bằng mật khẩu tạm bắt buộc đặt mật khẩu mới trước khi vào ứng dụng. | DQ-26 |
| BR-S3E1-34 | Mật khẩu tạm hết hạn sau 24 giờ kể từ lúc sinh. | QA-S3E1-11 |
| BR-S3E1-35 | Mỗi giáo viên đặt lại tối đa 10 lần trong 1 giờ. | QA-S3E1-12 |
| BR-S3E1-36 | Không đặt lại được mật khẩu của tài khoản giáo viên; giáo viên tự khôi phục bằng mã khôi phục. | DQ-17; PO-61 |
| BR-S3E1-37 | Mỗi lần đặt lại thành công ghi một AuditEvent PASSWORD_RESET kèm người thực hiện (chi tiết ở US-S3E5-01). | Ch3 §7 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E1-11, QA-S3E1-12, QA-S3E1-13.

### Tiêu chí chấp nhận

**US-S3E1-04-SC1 — Đặt lại mật khẩu, mật khẩu tạm hiện một lần** · Nguồn: DQ-26; PO-61
- Given: Chi Mai `HS-8L2K-77AB` Đang học 11A1 và đang đăng nhập trên một thiết bị (F-S3-021)
- When: ở "Học sinh trong lớp" bấm "Đặt lại mật khẩu" cạnh Chi Mai, rồi bấm "Đặt lại và hiện mật khẩu tạm" ở hộp xác nhận
- Then: hộp hiện mật khẩu tạm 8 ký tự dạng nhóm 3-3-2 kèm dòng "Chỉ hiện một lần"; mật khẩu cũ của Chi Mai không còn đăng nhập được; phiên đang mở của Chi Mai bị đăng xuất ở yêu cầu kế tiếp; có một AuditEvent PASSWORD_RESET


**US-S3E1-04-SC2 — Em đăng nhập bằng mật khẩu tạm và phải đặt mật khẩu mới** · Nguồn: BR-S3E1-33
- Given: cô Lan vừa đặt lại mật khẩu cho Chi Mai và đọc mật khẩu tạm (F-S3-021)
- When: Chi Mai nhập mã `HS-8L2K-77AB` và mật khẩu tạm, rồi đặt mật khẩu mới, rồi đăng nhập lại bằng mật khẩu mới
- Then: sau lần đăng nhập đầu hiện màn bắt buộc đặt mật khẩu mới và chưa vào được ứng dụng; sau khi đặt xong vào được Bài của em; mật khẩu tạm không dùng lại được


**US-S3E1-04-SC3 — Mật khẩu tạm không lấy lại được lần thứ hai** · Nguồn: BR-S3E1-32
- Given: cô Lan vừa đặt lại mật khẩu cho Chi Mai và đã đóng hộp (F-S3-021)
- When: mở lại màn Lớp học, rồi gọi lại mọi API danh sách học sinh và chi tiết học sinh của 11A1
- Then: không thân phản hồi nào chứa mật khẩu tạm hay giá trị băm của mật khẩu; hộp không thể mở lại; muốn có mật khẩu mới phải đặt lại lần nữa (mật khẩu tạm cũ hết hiệu lực)


**US-S3E1-04-SC4 — Em thuộc hai lớp: cả hai giáo viên đều đặt lại được** · Nguồn: Ch3 §7 · PO-66 (QA-S3E1-13)
- Given: Chi Mai Đang học cả 11A1 và 10B (F-S3-041, F-S3-040)
- When: thầy Khôi đặt lại mật khẩu của Chi Mai ở lớp 10B
- Then: thành công vì có quan hệ; AuditEvent ghi thầy Khôi là người thực hiện; ở 11A1 cô Lan thấy Chi Mai vẫn Đang học và không có thông tin nào về mật khẩu tạm của thầy Khôi


**US-S3E1-04-FC1 — Giáo viên không đặt lại được cho em ngoài lớp mình** · Nguồn: BR-S3E1-30; PO-61
- Given: Bình An `HS-4P9T-37WD` Đang học 11A1 của cô Lan, không thuộc lớp nào của thầy Khôi (F-S3-020)
- When: thầy Khôi gọi thẳng yêu cầu đặt lại mật khẩu cho Bình An
- Then: máy chủ trả NOT_FOUND; mật khẩu của Bình An không đổi; không sinh mật khẩu tạm; AuditEvent ghi một lần bị từ chối (QA-S3E5-02), không ghi PASSWORD_RESET thành công


**US-S3E1-04-FC2 — Em đã rời lớp thì hết quyền đặt lại** · Nguồn: BR-S3E1-30
- Given: Văn Tú Đã rời 11A1 (F-S3-028)
- When: cô Lan gọi thẳng yêu cầu đặt lại mật khẩu cho Văn Tú
- Then: máy chủ trả NOT_FOUND; mật khẩu không đổi; ở giao diện hàng của Văn Tú (nếu hiện) không có nút "Đặt lại mật khẩu"


**US-S3E1-04-FC3 — Em còn Chờ duyệt thì chưa đặt lại được** · Nguồn: BR-S3E1-30
- Given: Thu Hà Chờ duyệt ở 11A1 (F-S3-031)
- When: cô Lan gọi thẳng yêu cầu đặt lại mật khẩu cho Thu Hà
- Then: máy chủ trả NOT_FOUND; mật khẩu không đổi


**US-S3E1-04-FC4 — Học sinh không đặt lại được mật khẩu của bạn** · Nguồn: BR-S3E1-30
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi thẳng yêu cầu đặt lại mật khẩu cho Chi Mai
- Then: máy chủ trả FORBIDDEN; mật khẩu của Chi Mai không đổi


**US-S3E1-04-FC5 — Không đặt lại mật khẩu tài khoản giáo viên** · Nguồn: BR-S3E1-36; DQ-17
- Given: hai giáo viên Lan và Khôi (F-S3-001, F-S3-002)
- When: thầy Khôi gọi thẳng yêu cầu đặt lại mật khẩu cho mã `GV-7K2M-9QPA`
- Then: máy chủ trả NOT_FOUND; mật khẩu của cô Lan không đổi


**US-S3E1-04-FC6 — Mật khẩu tạm hết hạn đúng 24 giờ** · Nguồn: BR-S3E1-34 · PO-66 (QA-S3E1-11)
- Given: mật khẩu tạm của Chi Mai sinh lúc T0 (F-S3-021)
- When: đăng nhập với mật khẩu tạm ở T0 cộng 23 giờ 59 phút, rồi đăng nhập lại với một mật khẩu tạm khác sinh lúc T0 ở T0 cộng 24 giờ 01 phút
- Then: lần đăng nhập đầu được nhận (rồi buộc đặt mật khẩu mới); lần sau bị từ chối với MSG-S3-05 và không có phiên


**US-S3E1-04-FC7 — Đặt lại quá 10 lần trong 1 giờ** · Nguồn: BR-S3E1-35 · PO-66 (QA-S3E1-12)
- Given: cô Lan đã đặt lại thành công 10 lần trong 1 giờ cho các em của 11A1 (F-S3-010)
- When: đặt lại lần thứ 11 trong cùng giờ
- Then: máy chủ trả RATE_LIMITED kèm `Retry-After` và MSG-S3-08; không sinh mật khẩu tạm; không có AuditEvent thành công thứ 11


**US-S3E1-04-FC8 — Hủy ở hộp xác nhận thì không đổi gì** · Nguồn: DQ-26
- Given: hộp "Đặt lại mật khẩu cho Chi Mai?" đang mở (F-S3-021)
- When: bấm "Hủy"
- Then: hộp đóng; mật khẩu của Chi Mai không đổi; không có AuditEvent mới; phiên của Chi Mai không bị đăng xuất


## US-S3E1-05 — Quyền theo quan hệ giáo viên, lớp, học sinh (xuyên suốt; nhận vào chặng 3 theo PO-66 G5)
- **Story:** Là chủ sản phẩm, tôi muốn mọi API và màn của chặng 3 chỉ cho người dùng thấy dữ liệu thuộc quan hệ của họ, để giáo viên không thấy học sinh lớp khác và học sinh không thấy bài hay kết quả của bạn. · Loại Xuyên suốt · P0 · Độ khó 4
- **Nguồn:** story map (story thêm theo quy tắc cắt "một story xuyên suốt cho quyền", chủ đầu tư nhận vào chặng 3 ở PO-66 G5, đã đóng QA-S3E1-14); STAGES Ch3 §7 ý 1; brd-delta §4 "Quyền theo quan hệ"; PO-61; chỉ số M-S3-06 (ngưỡng 0)
- **Góc nhìn người vận hành (M18):** rủi ro lớn nhất của chặng 3 là lộ dữ liệu trẻ em giữa các lớp. Ca đáng kể: đổi mã lớp hoặc mã học sinh trên đường dẫn, dùng con trỏ phân trang của lớp khác, học sinh thử gọi API của giáo viên, em thuộc hai lớp.
- **Phụ thuộc:** US-S3E1-01…04; áp lên mọi story S3-E2…E6.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E1-50 | Mọi API chặng 3 kiểm quan hệ ở máy chủ trên từng đối tượng được nêu trong đường dẫn hoặc thân yêu cầu; không tin mã lớp hay mã người dùng từ thiết bị. | brd-delta §4 |
| BR-S3E1-51 | Đối tượng ngoài quan hệ của người gọi trả NOT_FOUND; sai vai trả FORBIDDEN; thân lỗi không chứa dữ liệu của đối tượng. | QA-S3E1-04 |
| BR-S3E1-52 | Học sinh chỉ đọc dữ liệu của chính mình; không có API trả bài làm, tiến độ hay kết quả của học sinh khác cho học sinh. | Ch3 §7 |
| BR-S3E1-53 | Hết quan hệ thì hết quyền ngay ở yêu cầu kế tiếp (Đã rời, Bị từ chối, lớp bị bỏ). | brd-delta §4 |
| BR-S3E1-54 | Mỗi lần bị từ chối vì quan hệ ghi một sự kiện bảo mật để đếm M-S3-06 (không ghi nội dung). | M-S3-06; QA-S3E5-02 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E5-02.

### Tiêu chí chấp nhận

**US-S3E1-05-SC1 — Giáo viên chỉ thấy lớp của mình** · Nguồn: BR-S3E1-50
- Given: cô Lan có 11A1 và 11A2, thầy Khôi có 10B (F-S3-010, F-S3-011, F-S3-012)
- When: cô Lan gọi danh sách lớp, rồi danh sách học sinh của từng lớp mình
- Then: danh sách lớp chỉ có 11A1 và 11A2; học sinh chỉ gồm em của hai lớp đó; không có em nào chỉ thuộc 10B (Mi Na `HS-2A5F-77BC`)


**US-S3E1-05-SC2 — Học sinh Đang học chỉ thấy bài của lớp mình** · Nguồn: BR-S3E1-52
- Given: Bình An Đang học 11A1 (F-S3-020); 11A1 có bộ tuần 15, 11A2 có bộ tuần 15 riêng (F-S3-050, F-S3-053)
- When: Bình An mở Bài của em
- Then: chỉ thấy bộ của 11A1; thân phản hồi không có bộ của 11A2, không có tên hay mã người dùng của bạn


**US-S3E1-05-FC1 — Đổi mã lớp trên đường dẫn** · Nguồn: BR-S3E1-50; BR-S3E1-51
- Given: cô Lan đã đăng nhập; mã nội bộ của lớp 10B lấy từ dữ liệu thử (F-S3-012)
- When: gọi từng API chặng 3 có tham số lớp (danh sách học sinh, danh sách chờ duyệt, kết quả lớp, đặt lại mật khẩu, bộ bài của lớp, công tắc AI) với mã nội bộ của lớp 10B
- Then: mọi API trả NOT_FOUND; thân lỗi không chứa tên lớp, tên giáo viên hay số học sinh; không API nào đổi dữ liệu


**US-S3E1-05-FC2 — Đổi mã học sinh trên đường dẫn** · Nguồn: BR-S3E1-50
- Given: cô Lan đã đăng nhập; Mi Na `HS-2A5F-77BC` chỉ thuộc 10B (F-S3-040)
- When: gọi chi tiết học sinh, bằng chứng một ô và đặt lại mật khẩu với mã của Mi Na
- Then: mọi API trả NOT_FOUND; không có dữ liệu của Mi Na trong thân phản hồi


**US-S3E1-05-FC3 — Học sinh gọi API của giáo viên** · Nguồn: BR-S3E1-51
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi từng API dành cho giáo viên của chặng 3 (kết quả lớp, danh sách chờ duyệt, tạo bộ bài, đổi giờ, Mở ngay, công tắc AI, đặt lại mật khẩu)
- Then: mọi API trả FORBIDDEN và không đổi dữ liệu


**US-S3E1-05-FC4 — Học sinh Chờ duyệt, Đã rời hoặc Bị từ chối không thấy bài lớp** · Nguồn: BR-S3E1-53
- Given: Hoàng Nam Chờ duyệt, Văn Tú Đã rời (bài mới giao sau khi rời), một em Bị từ chối ở 11A1 (F-S3-030, F-S3-028, F-S3-050)
- When: từng em mở Bài của em và gọi chi tiết bộ tuần 15 bằng mã nội bộ
- Then: Bài của em không có nhóm 11A1; chi tiết bộ trả NOT_FOUND; Văn Tú vẫn xem được lịch sử bài của chính mình ở Tự học


**US-S3E1-05-FC5 — Con trỏ phân trang của lớp khác** · Nguồn: BR-S3E1-50
- Given: cô Lan lấy được con trỏ trang kế của danh sách học sinh lớp 10B từ phiên của thầy Khôi (F-S3-012)
- When: cô Lan gọi danh sách học sinh 11A1 với con trỏ đó
- Then: máy chủ trả INVALID_CURSOR; thân phản hồi không chứa dữ liệu của 10B


**US-S3E1-05-FC6 — Mỗi lần từ chối vì quan hệ được ghi để đếm** · Nguồn: BR-S3E1-54; M-S3-06 · PO-66 (QA-S3E5-02)
- Given: bộ đếm sự kiện bảo mật của dữ liệu thử đang bằng 0
- When: chạy 5 yêu cầu ngoài quan hệ (thầy Khôi gọi 11A1, Bình An gọi API giáo viên, hai yêu cầu đổi mã lớp, một con trỏ lạ)
- Then: bộ đếm tăng đúng 5; mỗi bản ghi chỉ có loại, mã người gọi, loại đối tượng và giờ máy chủ, không có nội dung yêu cầu

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E1, ready for R3 | runner-T-261126-14 (BA) |
| v0.2 | 2026-11-26 | thêm BR-S3E1-16 và US-S3E1-01-SC6 (lớp MVP đã chuyển), RV_s23 M-02; đổi mã mẫu sai bảng ký tự (RV_s23 m-01) | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | G4: BR-S3E1-06/-11 duyệt bắt buộc, BR-S3E1-17 giới hạn tổng lượt (30 lần/24 giờ), thêm FC8, BV1; G5: US-S3E1-05 và Bị từ chối được nhận; bỏ nhãn giả định chưa chốt (PO-66) | runner-T-261126-37 (BA/QC) |
