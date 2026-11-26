Phiên bản: v0.2 · 2026-11-26
# S3-E6 — Chữa bài từ kết quả sang Trình chiếu, xuất kết quả lớp, lưu trữ lớp, nhắc việc trong ứng dụng (P1): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA. Nguồn: `work/s23/story-map-s23.md` (R2 khóa, 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Ch3 (22/11/2026), `Design/s23/DQ.md` và ảnh `c3-*.png` (26/11/2026), `PO-DECISIONS.md` PO-53, PO-61…65. Khi nguồn mâu thuẫn thì PO-61 thắng STAGES.
> **Trạng thái: ready for R3** (chờ owner ở gate 16, T-261126-16). Mức epic: **P1**. Câu hỏi mở và đề xuất đổi story map ở `questions.md` cùng thư mục.

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


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E6-09)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-91 | Hộp xác nhận | "Lớp sẽ thấy lời giải trên màn chiếu. Máy học sinh vẫn khóa lời giải. Tiếp tục?" | — |
| MSG-S3-92 | Toast | "Đề đã được sửa. Mở Trình chiếu từ đầu đề." | — |
| MSG-S3-93 | Hộp xác nhận | "Lưu trữ lớp {tên lớp}? Lớp chỉ còn xem, mã lớp ngừng dùng." | — |
| MSG-S3-94 | Inline | "Đã xuất quá nhiều lần. Vui lòng thử lại sau ít phút." | RATE_LIMITED |
| MSG-S3-95 | Thông báo trong ứng dụng | "Bộ {tên bộ} sắp hết hạn: {HH:mm, dd/MM/yyyy}" | — |
| MSG-S3-96 | Page state | "Lớp này đã lưu trữ. Em chỉ xem được." | — |

## US-S3E6-01 — Chữa bài từ kết quả sang Trình chiếu
- **Story:** Là giáo viên, tôi muốn bấm từ thẻ "Cả lớp vướng ở đâu" sang Trình chiếu đúng đề và đúng bước, để chữa bài liền mạch trong tiết. · Loại UI · **P1** · Độ khó 3
- **Nguồn:** story map US-S3E6-01; STAGES Ch3 §4 P1 "chữa bài từ kết quả"; Ch2 §5 luồng B (Trình chiếu); M-S3-04; US-S2E7-01, US-S2E7-03
- **Góc nhìn người vận hành (M18):** cô Lan thấy nhiều em dừng ở bước giao tuyến, bấm "Chữa bài này" và màn chiếu mở đúng đề, đúng bước đó. Ca đáng kể: lời giải của bộ còn khóa ở máy học sinh, đề vừa được sửa phiên bản, bấm hai lần, chiếu cho cả lớp thấy tên em.
- **Phụ thuộc:** US-S3E3-03; S2-E7 (Trình chiếu); S2-E5 (phiên bản lời giải). Nút này chưa có trong bản mẫu `c3-04-ket-qua-lop.png` (QA-S3E6-02).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E6-01 | Chỉ thẻ nhận định đủ ngưỡng có nút "Chữa bài này"; dữ liệu thô không có nút. | US-S3E3-03 |
| BR-S3E6-02 | Bấm nút mở Trình chiếu của đúng đề, bắt đầu ở bước em dừng nhiều nhất của thẻ. | Ch3 §4 P1 |
| BR-S3E6-03 | Nếu lời giải đang khóa: hộp xác nhận MSG-S3-91 như S2E7-03; máy học sinh vẫn khóa theo giờ mở. | US-S2E7-03; DQ-09 |
| BR-S3E6-04 | Màn chiếu không hiện tên hiển thị, mã người dùng hay số liệu theo em; chỉ có nội dung đề và lời giải. | PO-61; chiếu cho cả lớp |
| BR-S3E6-05 | Mỗi lần bấm hợp lệ ghi một sự kiện đo cho M-S3-04 gồm mã giáo viên, mã đề, giờ; gộp các lần bấm trong 5 giây thành một. | M-S3-04; QA-S3E6-03 |
| BR-S3E6-06 | Đề đã có phiên bản mới mà bước của thẻ không còn: mở từ đầu đề kèm MSG-S3-92. | QA-S3E6-01; S2-E5 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E6-01, QA-S3E6-03.

### Tiêu chí chấp nhận

**US-S3E6-01-SC1 — Bấm "Chữa bài này" mở Trình chiếu đúng đề và bước** · Nguồn: BR-S3E6-02
- Given: thẻ Đề 3 · hai mặt phẳng với bước dừng nhiều nhất là bước 2 "xác định giao tuyến của (SAB) và (SCD)"; lời giải Đề 3 đã mở (F-S3-051)
- When: ở Kết quả lớp bấm "Chữa bài này" trên thẻ Đề 3
- Then: Trình chiếu toàn màn hình mở với Đề 3 ở bước 2; nút "Hiện bước kế" và "Bước trước" dùng được; nút thoát đưa về Kết quả lớp đúng bộ và đúng thẻ


**US-S3E6-01-SC2 — Lời giải còn khóa thì hỏi lại trước khi chiếu** · Nguồn: BR-S3E6-03; DQ-09
- Given: bộ tuần 15 Sắp mở (giờ mở 06/12/2026 08:00); giờ máy chủ 05/12/2026 (F-S3-050)
- When: bấm "Chữa bài này" trên thẻ Đề 3, rồi chọn "Hủy", rồi bấm lại, rồi chọn "Tiếp tục"
- Then: hộp MSG-S3-91 hiện ở mỗi lần bấm; "Hủy" không mở Trình chiếu; "Tiếp tục" mở Trình chiếu kèm chip "Máy học sinh vẫn khóa lời giải"; Bình An vẫn không lấy được lời giải (trạng thái Sắp mở)


**US-S3E6-01-SC3 — Màn chiếu không có tên hay mã học sinh** · Nguồn: BR-S3E6-04
- Given: Trình chiếu vừa mở từ thẻ Đề 3 (F-S3-051)
- When: soát giao diện, phần tử ẩn và thân phản hồi của yêu cầu tải Trình chiếu
- Then: không có tên hiển thị, mã người dùng `HS-…`, số bài làm hay số em của lớp; chỉ có đề, hình và các bước lời giải


**US-S3E6-01-SC4 — Mỗi lần chữa bài ghi một sự kiện đo** · Nguồn: BR-S3E6-05; M-S3-04
- Given: bảng sự kiện đo của dữ liệu thử đang trống
- When: cô Lan bấm "Chữa bài này" ở Đề 3, rồi thoát, rồi 10 giây sau bấm lại ở Đề 3
- Then: có đúng 2 sự kiện đo, mỗi sự kiện chỉ có mã giáo viên, mã đề, giờ máy chủ; không có tên hay mã học sinh


**US-S3E6-01-FC1 — Thẻ dữ liệu thô không có nút** · Nguồn: BR-S3E6-01
- Given: Đề 4 chỉ có 4 bài làm nên hiện dữ liệu thô với MSG-S3-43 (F-S3-051)
- When: xem khung của Đề 4
- Then: không có nút "Chữa bài này"; chỉ có danh sách thô


**US-S3E6-01-FC2 — Đề đã sửa phiên bản, bước không còn** · Nguồn: BR-S3E6-06 · PROVISIONAL QA-S3E6-01
- Given: thẻ Đề 3 trỏ bước 5 nhưng Đề 3 vừa có phiên bản mới chỉ còn 4 bước
- When: bấm "Chữa bài này"
- Then: Trình chiếu mở ở bước 1 của phiên bản mới kèm MSG-S3-92; không lỗi, không màn trắng


**US-S3E6-01-FC3 — Bấm hai lần trong 1 giây chỉ mở một phiên** · Nguồn: BR-S3E6-05 · PROVISIONAL QA-S3E6-03
- Given: thẻ Đề 3 (F-S3-051)
- When: gửi hai yêu cầu mở Trình chiếu từ thẻ cách nhau 1 giây
- Then: chỉ một phiên Trình chiếu; chỉ một sự kiện đo


**US-S3E6-01-FC4 — Giáo viên khác không mở được** · Nguồn: BR-S3E6-02; BR-S3E1-50
- Given: thầy Khôi đã đăng nhập; thẻ Đề 3 của lớp 11A1 (F-S3-002)
- When: gọi thẳng yêu cầu chữa bài từ thẻ của 11A1
- Then: máy chủ trả NOT_FOUND; không có phiên Trình chiếu; không có sự kiện đo


**US-S3E6-01-FC5 — Học sinh không gọi được** · Nguồn: BR-S3E6-02
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi thẳng yêu cầu chữa bài từ thẻ
- Then: máy chủ trả FORBIDDEN; không có phiên, không có sự kiện đo


**US-S3E6-01-FC6 — Mất mạng khi bấm** · Nguồn: BR-S3E6-02
- Given: mạng bị cắt trước khi bấm "Chữa bài này"
- When: bấm "Chữa bài này"
- Then: hiện thông báo chưa mở được và gợi ý kiểm tra mạng; không màn trắng; không sự kiện đo giả


## US-S3E6-02 — Xuất kết quả lớp; lưu trữ lớp cuối năm
- **Story:** Là giáo viên, tôi muốn xuất bảng kết quả lớp ra tệp và lưu trữ lớp khi hết năm học, để giữ hồ sơ và dọn màn Lớp học. · Loại UI · **P1** · Độ khó 3
- **Nguồn:** story map US-S3E6-02; STAGES Ch3 §4 P1 ("xuất kết quả lớp; lưu trữ lớp khi hết năm học"), §8 `Class.status` (Đang dạy rồi Đã lưu trữ); DQ-23
- **Góc nhìn người vận hành (M18):** cuối năm cô Lan xuất bảng của từng bộ để lưu máy, rồi lưu trữ lớp 11A2. Ca đáng kể: tệp bị mở bằng bảng tính (tên em bắt đầu bằng dấu `=`), xuất liên tục, lớp còn em chờ duyệt, học sinh muốn xem lại bài cũ.
- **Phụ thuộc:** US-S3E3-01; US-S3E1-01, 03; quy tắc "không tổng, không xếp hạng" của BR-S3E3-03.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E6-10 | Xuất ra tệp CSV mã hóa UTF-8 có dấu thứ tự byte, đúng bảng lớp × bài đang chọn: cột tên hiển thị, mã người dùng, "đã rời", rồi một cột trạng thái cho mỗi đề; không cột tổng, điểm, xếp hạng; không nội dung câu hỏi. | BR-S3E3-03; QA-S3E6-04 |
| BR-S3E6-11 | Tên tệp `ket-qua-<tên lớp không dấu>-<tên bộ không dấu>-<yyyyMMdd>.csv`; không chứa tên hay mã học sinh. | PO-61 |
| BR-S3E6-12 | Ô văn bản bắt đầu bằng `=`, `+`, `-` hoặc `@` được thêm dấu nháy đơn ở đầu để bảng tính không chạy như công thức. | an toàn tệp |
| BR-S3E6-13 | Mỗi giáo viên xuất tối đa 10 lần trong 1 giờ; chỉ chủ lớp xuất. | QA-S3E6-05 |
| BR-S3E6-14 | Lưu trữ lớp: trạng thái Đã lưu trữ; mã lớp ngừng dùng; không nhận em mới; không giao bộ mới; em Chờ duyệt tự thành Bị từ chối; không đặt lại mật khẩu; Kết quả lớp và xuất vẫn dùng được; lịch mở lời giải vẫn chạy. | Ch3 §8; QA-S3E6-06 |
| BR-S3E6-15 | Học sinh Đang học thấy lớp đã lưu trữ ở mục "Lớp đã lưu trữ", chỉ xem; không còn bộ mới. | QA-S3E6-06 |
| BR-S3E6-16 | Chủ lớp bỏ lưu trữ được: lớp về Đang dạy và nhận mã lớp mới. | QA-S3E6-07 |
| BR-S3E6-17 | Lớp đã lưu trữ không tính vào giới hạn 20 lớp Đang dạy. | BR-S3E1-04 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E1-05, QA-S3E6-05, QA-S3E6-06, QA-S3E6-07.

### Tiêu chí chấp nhận

**US-S3E6-02-SC1 — Xuất bảng tuần 15 đúng hàng và cột** · Nguồn: BR-S3E6-10
- Given: Kết quả lớp "Bộ bài tuần 15" của 11A1 như F-S3-051 đang mở (F-S3-050)
- When: bấm "Xuất kết quả"
- Then: tải về một tệp CSV có dòng tiêu đề "Tên hiển thị, Mã người dùng, Đã rời, Đề 1, Đề 2, Đề 3, Đề 4" và 8 dòng học sinh theo thứ tự tên; mỗi ô trạng thái khớp F-S3-051 (ví dụ dòng Khánh Linh: bốn ô "Chưa làm"); không cột hay dòng tổng, phần trăm, điểm hay hạng


**US-S3E6-02-SC2 — Tên tệp không chứa tên học sinh** · Nguồn: BR-S3E6-11
- Given: giờ máy chủ 07/12/2026; lớp "11A1"; bộ "Bộ bài tuần 15"
- When: xuất kết quả
- Then: tên tệp là `ket-qua-11A1-bo-bai-tuan-15-20261207.csv`; không chứa tên hay mã người dùng


**US-S3E6-02-SC3 — Ô bắt đầu bằng dấu công thức được vô hiệu** · Nguồn: BR-S3E6-12
- Given: một học sinh Đang học 11A1 có tên hiển thị "=HYPERLINK(""http://x"",""a"")" và một học sinh tên "+84 Lan" (tên hợp lệ, không phải 9 chữ số liền)
- When: xuất kết quả, rồi mở tệp trong bảng tính
- Then: trong tệp hai ô tên bắt đầu bằng dấu nháy đơn trước `=` và `+`; bảng tính hiển thị đúng chuỗi như chữ, không chạy công thức, không mở liên kết
- Dữ liệu nhập: `=HYPERLINK("http://x","a")` ; "+84 Lan"


**US-S3E6-02-SC4 — Tệp xuất chỉ có trường được phép** · Nguồn: BR-S3E6-10; BR-S3E5-30
- Given: tệp xuất của bộ tuần 15
- When: đọc mọi cột và mọi ô của tệp
- Then: chỉ có tên hiển thị, mã người dùng, cờ đã rời và trạng thái ô; không có câu hỏi của học sinh, đáp số em nhập, địa chỉ IP, mã lớp, tên giáo viên


**US-S3E6-02-SC5 — Lưu trữ lớp: trạng thái, mã và hiển thị của học sinh** · Nguồn: BR-S3E6-14, 15
- Given: lớp 11A2 Đang dạy, mã `HK-8M3C`, 3 em Đang học (F-S3-011, F-S3-041)
- When: bấm "Lưu trữ lớp", rồi xác nhận ở hộp MSG-S3-93, rồi Quốc Bảo mở Bài của em
- Then: 11A2 chuyển sang Đã lưu trữ và rời danh sách lớp Đang dạy; Quốc Bảo thấy 11A2 trong mục "Lớp đã lưu trữ" với MSG-S3-96, bài đã làm vẫn xem được, không có bộ mới


**US-S3E6-02-SC6 — Lưu trữ lớp có em Chờ duyệt thì các em đó bị từ chối tự động** · Nguồn: BR-S3E6-14 · PROVISIONAL QA-S3E6-06
- Given: 11A1 có 3 em Chờ duyệt (F-S3-030…032)
- When: lưu trữ lớp 11A1, rồi Hoàng Nam `HS-7Q8Z-48DF` mở màn Vào lớp
- Then: 3 em chuyển Bị từ chối; Hoàng Nam thấy trạng thái chưa vào được lớp và gợi ý hỏi giáo viên; danh sách Chờ duyệt của 11A1 trống


**US-S3E6-02-SC7 — Bỏ lưu trữ thì lớp về Đang dạy với mã lớp mới** · Nguồn: BR-S3E6-16 · PROVISIONAL QA-S3E6-07
- Given: lớp 11A2 đã lưu trữ, mã cũ `HK-8M3C` đã ngừng dùng (F-S3-011)
- When: bấm "Bỏ lưu trữ", rồi xác nhận
- Then: 11A2 Đang dạy; có mã lớp mới khác `HK-8M3C`; 3 em Đang học thấy lại bài của lớp


**US-S3E6-02-FC1 — Học sinh nhập mã của lớp đã lưu trữ** · Nguồn: BR-S3E6-14
- Given: lớp 11A2 đã lưu trữ (F-S3-011)
- When: học sinh chưa thuộc lớp nào nhập mã `HK-8M3C`
- Then: hiện MSG-S3-01 như mã sai; không tạo bản ghi vào lớp


**US-S3E6-02-FC2 — Giao bộ mới cho lớp đã lưu trữ** · Nguồn: BR-S3E6-14
- Given: lớp 11A2 đã lưu trữ (F-S3-011)
- When: gọi thẳng yêu cầu giao một bộ mới cho 11A2
- Then: máy chủ trả CONFLICT; không tạo bộ, không Notification


**US-S3E6-02-FC3 — Xuất quá 10 lần trong 1 giờ** · Nguồn: BR-S3E6-13 · PROVISIONAL QA-S3E6-05
- Given: cô Lan đã xuất 10 lần trong 1 giờ
- When: xuất lần thứ 11 trong cùng giờ
- Then: RATE_LIMITED kèm `Retry-After` và MSG-S3-94; không có tệp; lần xuất sau khi hết giờ giới hạn thành công


**US-S3E6-02-FC4 — Giáo viên khác không xuất được lớp 11A1** · Nguồn: BR-S3E6-13
- Given: thầy Khôi đã đăng nhập (F-S3-002)
- When: gọi thẳng yêu cầu xuất kết quả của 11A1, bộ tuần 15
- Then: máy chủ trả NOT_FOUND; không có tệp, không có tên em trong thân lỗi


**US-S3E6-02-FC5 — Học sinh không xuất được** · Nguồn: BR-S3E6-13
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi thẳng yêu cầu xuất kết quả của bộ tuần 15
- Then: máy chủ trả FORBIDDEN; không có tệp


**US-S3E6-02-FC6 — Lưu trữ không tính vào giới hạn 20 lớp** · Nguồn: BR-S3E6-17; BR-S3E1-04 · PROVISIONAL QA-S3E1-05
- Given: một giáo viên có 20 lớp Đang dạy
- When: lưu trữ một lớp, rồi tạo lớp mới
- Then: lớp mới được tạo (19 lớp Đang dạy rồi 20); giáo viên có tổng 21 lớp gồm 1 lớp Đã lưu trữ


**US-S3E6-02-FC7 — Lớp đã lưu trữ không đặt lại mật khẩu** · Nguồn: BR-S3E6-14 · PROVISIONAL QA-S3E6-06
- Given: lớp 11A2 đã lưu trữ; Thảo Vy `HS-8F2E-33BB` thuộc 11A2 (F-S3-041)
- When: cô Lan gọi thẳng yêu cầu đặt lại mật khẩu cho Thảo Vy
- Then: máy chủ trả NOT_FOUND; mật khẩu không đổi; không có AuditEvent thành công


**US-S3E6-02-FC8 — Lịch mở lời giải của lớp đã lưu trữ vẫn chạy** · Nguồn: BR-S3E6-14 · PROVISIONAL QA-S3E6-06
- Given: bộ tuần 15 của 11A2 Sắp mở lúc 06/12/2026 08:00; lớp 11A2 đã lưu trữ ngày 05/12/2026 (F-S3-053)
- When: Quốc Bảo lấy chi tiết lời giải Đề 3 ở giờ máy chủ 06/12/2026 08:00:00
- Then: lời giải trả đầy đủ chỉ đọc; trước 08:00:00 vẫn khóa


**US-S3E6-02-FC9 — Hủy ở hộp lưu trữ không đổi gì** · Nguồn: BR-S3E6-14
- Given: hộp MSG-S3-93 đang mở cho lớp 11A1 (F-S3-010)
- When: bấm "Hủy"
- Then: 11A1 vẫn Đang dạy; mã `HK-4P7Q` vẫn dùng được


## US-S3E6-03 — Nhắc việc trong ứng dụng: bài mới, sắp hết hạn, lời giải đã mở
- **Story:** Là học sinh, tôi muốn nhận nhắc trong ứng dụng khi có bài mới, khi bài sắp hết hạn và khi lời giải đã mở, để làm kịp và biết lúc xem lời giải. · Loại Logic · **P1** · Độ khó 3
- **Nguồn:** story map US-S3E6-03; STAGES Ch3 §3 ý 8, §4 P1; PO-65 (Q9: chỉ thông báo trong ứng dụng); dùng lại bảng `Notification` của US-S3E4-03
- **Góc nhìn người vận hành (M18):** Bình An thấy "Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026" vào buổi tối hôm trước. Ca đáng kể: nhắc rơi vào ban đêm, tác vụ chạy lại hai lần, giáo viên đổi hạn, em đã làm xong.
- **Phụ thuộc:** US-S3E4-03, US-S3E2-02; tác vụ chạy nền theo lịch.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E6-20 | Nhắc "sắp hết hạn" gửi 24 giờ trước `due_at`, một lần cho mỗi bộ và mỗi học sinh, chỉ cho học sinh Đang học còn ít nhất một đề Chưa làm hoặc Đang làm trong bộ. | QA-S3E6-08 |
| BR-S3E6-21 | Giờ yên lặng 21:00 đến 06:00 giờ Việt Nam: nhắc rơi trong khung được hoãn tới 06:00; đúng 21:00:00 là đã yên lặng, đúng 06:00:00 là được gửi. | QA-S3E6-08 |
| BR-S3E6-22 | Bộ không có `due_at` thì không có nhắc "sắp hết hạn"; bộ giao khi còn ít hơn 24 giờ đến hạn không có nhắc này (đã có thông báo bài mới). | QA-S3E6-08 |
| BR-S3E6-23 | Giáo viên đổi `due_at`: hủy nhắc cũ chưa gửi và lên lịch lại theo hạn mới. | QA-S3E6-08 |
| BR-S3E6-24 | Khi tới giờ mở lời giải theo lịch, mỗi học sinh Đang học nhận một thông báo "lời giải đã mở" (Mở ngay vẫn do US-S3E4-02). | US-S3E4-03 |
| BR-S3E6-25 | Tác vụ nhắc idempotent; không gửi cho Chờ duyệt, Đã rời, Bị từ chối; không gửi cho bộ Đã đóng hoặc Lưu trữ; chỉ trong ứng dụng; nội dung chỉ tên bộ và giờ. | BR-S3E4-21, 25, 26 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E6-08.

### Tiêu chí chấp nhận

**US-S3E6-03-SC1 — Nhắc sắp hết hạn tới đúng bốn em chưa xong** · Nguồn: BR-S3E6-20 · PROVISIONAL QA-S3E6-08
- Given: bộ tuần 15 có hạn 04/12/2026 20:00; giờ máy chủ 03/12/2026 20:00:00; trạng thái ô như F-S3-051 (F-S3-050)
- When: tác vụ nhắc chạy
- Then: bốn em còn đề Chưa làm hoặc Đang làm là Chi Mai, Gia Bảo, Khánh Linh, Ngọc Anh mỗi em nhận đúng một thông báo MSG-S3-95 "Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026"; các em khác không nhận


**US-S3E6-03-SC2 — Em đã xong bộ không nhận nhắc** · Nguồn: BR-S3E6-20
- Given: như AC SC1; Bình An, Đức Huy, Hà My, Minh Quân đã xong mọi đề (F-S3-051)
- When: tác vụ nhắc chạy ở 03/12/2026 20:00:00
- Then: bốn em này không có thông báo MSG-S3-95


**US-S3E6-03-SC3 — Biên giờ yên lặng 21:00 và 06:00** · Nguồn: BR-S3E6-21 · PROVISIONAL QA-S3E6-08
- Given: ba bộ có giờ nhắc lần lượt rơi vào 20:59:59, 21:00:00 và 06:00:00 giờ Việt Nam (dữ liệu thử)
- When: tác vụ nhắc chạy ở từng giờ đó
- Then: nhắc 20:59:59 được gửi ngay; nhắc 21:00:00 hoãn tới 06:00 hôm sau; nhắc 06:00:00 được gửi ngay


**US-S3E6-03-SC4 — Nhắc rơi giữa đêm được hoãn tới 06:00** · Nguồn: BR-S3E6-21 · PROVISIONAL QA-S3E6-08
- Given: bộ có hạn 05/12/2026 02:00 nên giờ nhắc là 04/12/2026 02:00; em Gia Bảo chưa xong (F-S3-024)
- When: tác vụ nhắc chạy lúc 02:00, rồi chạy lại lúc 06:00
- Then: lúc 02:00 không có thông báo; lúc 06:00 Gia Bảo nhận một thông báo MSG-S3-95


**US-S3E6-03-SC5 — Tác vụ chạy lặp không gửi hai lần** · Nguồn: BR-S3E6-25
- Given: tác vụ nhắc của AC SC1 đã chạy một lần
- When: chạy lại tác vụ ba lần nữa
- Then: mỗi em trong bốn em vẫn đúng một thông báo MSG-S3-95; ràng buộc duy nhất từ chối bản ghi trùng


**US-S3E6-03-SC6 — Tới giờ mở lời giải thì thông báo cho em Đang học** · Nguồn: BR-S3E6-24
- Given: bộ tuần 15 mở lời giải lúc 06/12/2026 08:00 theo lịch; 8 em Đang học, 3 em Chờ duyệt (F-S3-050, F-S3-010)
- When: tác vụ chạy ở 06/12/2026 08:00:00
- Then: 8 em Đang học mỗi em nhận "Lời giải Bộ bài tuần 15 đã mở"; 3 em Chờ duyệt không nhận; chạy lại vẫn mỗi em một thông báo


**US-S3E6-03-SC7 — Giáo viên đổi hạn thì nhắc theo hạn mới** · Nguồn: BR-S3E6-23 · PROVISIONAL QA-S3E6-08
- Given: bộ tuần 15 hạn 04/12/2026 20:00 có nhắc lên lịch 03/12/2026 20:00; cô Lan đổi hạn sang 05/12/2026 20:00
- When: tác vụ chạy ở 03/12/2026 20:00:00, rồi chạy ở 04/12/2026 20:00:00
- Then: lúc 03/12 không có nhắc; lúc 04/12 20:00:00 bốn em chưa xong nhận nhắc với hạn "20:00, 05/12/2026"


**US-S3E6-03-FC1 — Bộ giao khi còn dưới 24 giờ đến hạn** · Nguồn: BR-S3E6-22 · PROVISIONAL QA-S3E6-08
- Given: bộ giao lúc 03/12/2026 21:00 với hạn 04/12/2026 20:00 (còn 23 giờ)
- When: tác vụ nhắc chạy sau khi giao
- Then: học sinh chỉ có thông báo "Có bài mới"; không có thông báo MSG-S3-95


**US-S3E6-03-FC2 — Bộ không có hạn** · Nguồn: BR-S3E6-22
- Given: "Bộ bài tuần 16" Đã giao, không có hạn làm (F-S3-052)
- When: tác vụ nhắc chạy mỗi giờ trong 48 giờ giả lập
- Then: không có thông báo MSG-S3-95 nào cho bộ này


**US-S3E6-03-FC3 — Em Chờ duyệt, Đã rời, Bị từ chối không nhận nhắc** · Nguồn: BR-S3E6-25
- Given: Hoàng Nam Chờ duyệt, Văn Tú Đã rời, một em Bị từ chối ở 11A1; bộ tuần 15 sắp hết hạn (F-S3-030, F-S3-028)
- When: tác vụ nhắc chạy
- Then: ba em này không có thông báo nào của bộ tuần 15


**US-S3E6-03-FC4 — Bộ Đã đóng thì không nhắc** · Nguồn: BR-S3E6-25 · PROVISIONAL QA-S3E6-08
- Given: bộ tuần 15 chuyển Đã đóng lúc 03/12/2026 10:00, trước giờ nhắc 20:00
- When: tác vụ nhắc chạy ở 03/12/2026 20:00:00
- Then: không có thông báo MSG-S3-95


**US-S3E6-03-FC5 — Nội dung nhắc chỉ có tên bộ và giờ** · Nguồn: BR-S3E6-25; BR-S3E4-25
- Given: Gia Bảo vừa nhận thông báo MSG-S3-95 (F-S3-051)
- When: gọi API danh sách thông báo của Gia Bảo
- Then: thông báo chỉ có tên bộ, giờ hạn và loại; không có đáp số, tiến độ, tên hay trạng thái của bạn; không có từ ngữ so sánh với lớp


**US-S3E6-03-FC6 — Chỉ nhắc trong ứng dụng** · Nguồn: BR-S3E6-25; PO-65 (Q9)
- Given: nhà cung cấp gửi tin ngoài (push, email, SMS) được giả lập để đếm lượt gọi
- When: chạy trọn bộ tác vụ nhắc của AC SC1, SC3 và SC6
- Then: số lượt gọi nhà cung cấp gửi tin ngoài bằng 0


**US-S3E6-03-FC7 — Giờ gửi nhắc đúng 24 giờ trước hạn, sai số không quá 1 phút** · Nguồn: BR-S3E6-20 · PROVISIONAL QA-S3E6-08
- Given: bộ có hạn 04/12/2026 20:00; tác vụ chạy mỗi phút
- When: giả lập đồng hồ từ 03/12/2026 19:58 đến 03/12/2026 20:02
- Then: thông báo được tạo trong khoảng 20:00:00 đến 20:00:59 ngày 03/12/2026, không sớm hơn 20:00:00 và không muộn hơn 20:01:00

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E6, ready for R3 | runner-T-261126-14 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
