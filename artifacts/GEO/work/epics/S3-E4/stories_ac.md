Phiên bản: v0.1 · 2026-11-26
# S3-E4 — Tổng quan giáo viên, theo dõi nhanh trên điện thoại, thông báo trong ứng dụng: story Dev-Ready và tiêu chí chấp nhận

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
| `F-S3-020…027` | Học sinh Đang học của 11A1: Bình An `HS-4P9T-31WD`, Chi Mai `HS-8L2K-77AB`, Đức Huy `HS-3M7Q-05XN`, Gia Bảo `HS-9D4W-61CE`, Hà My `HS-2X6R-48PL`, Khánh Linh `HS-7T1B-90QZ`, Minh Quân `HS-5N8C-24TR`, Ngọc Anh `HS-6V3F-18HM` |
| `F-S3-028` | Văn Tú `HS-3H6J-72KP` · Đã rời 11A1 (còn dữ liệu bài làm cũ) |
| `F-S3-030…032` | Chờ duyệt ở 11A1: Hoàng Nam `HS-1Q8Z-40DF`, Thu Hà `HS-7C5J-63LS`, Hoàng Nam thứ hai `HS-9B2L-85VG` (trùng tên với em đầu) |
| `F-S3-040` | Lớp 10B: Mi Na `HS-2A5F-11BC`, Bình An thứ hai `HS-6Z1N-52YT` (trùng tên với Bình An của 11A1), hai em khác; Chi Mai `HS-8L2K-77AB` cũng Đang học 10B (em thuộc hai lớp của hai giáo viên) |
| `F-S3-041` | Lớp 11A2: Quốc Bảo `HS-3C9D-77AA`, Thảo Vy `HS-8F2E-33BB`, Tuấn Kiệt `HS-1G4H-99CC` |
| `F-S3-050` | Bộ "Bộ bài tuần 15" của 11A1 · Đã giao · hạn làm T6 04/12/2026 20:00 · giờ mở lời giải 06/12/2026 08:00 · bốn đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích |
| `F-S3-051` | Bảng trạng thái 11A1 × bộ tuần 15 (Đề 1, 2, 3, 4): An Đúng, Xem lại, Đúng, Xem lại · Mai Đúng, Xem lại, Xem lại, Chưa làm · Huy Đúng, Đúng, Xem lại, Đúng · Bảo Xem lại, Xem lại, Đang làm, Chưa làm · My Đúng, Đúng, Đúng, Đúng · Linh Chưa làm bốn đề · Quân Đúng, Xem lại, Xem lại, Xem lại · Ngọc Anh Đúng, Đúng, Xem lại, Đang làm |
| `F-S3-052` | Bộ "Bộ bài tuần 16" của 11A1 · Nháp · chưa đặt hạn, chưa đặt giờ mở lời giải |
| `F-S3-053` | Bộ tuần 15 của 11A2 · Đã giao · Đề 3 có ba bài làm (Quốc Bảo, Thảo Vy, Tuấn Kiệt) |

---


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E4-08)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-61 | Hộp xác nhận | "Lớp sẽ thấy lời giải ngay. Mở ngay?" | — |
| MSG-S3-62 | Toast | "Chưa gửi được. Kiểm tra mạng rồi thử lại." | — |
| MSG-S3-63 | Inline | "Giờ mới phải sau thời điểm hiện tại. Muốn mở ngay, dùng Mở ngay." | VALIDATION_FAILED |
| MSG-S3-64 | Chip | "Đã mở" | — |
| MSG-S3-65 | Page state | "Chưa có việc cần làm hôm nay." | — |
| MSG-S3-66 | Page state | "Bạn chưa có lớp. Tạo lớp đầu tiên." | — |
| MSG-S3-67 | Inline | "Lời giải đã mở, không đổi giờ được." | CONFLICT |
| MSG-S3-68 | Thông báo trong ứng dụng | "Có bài mới: {tên bộ}" · "Lời giải {tên bộ} đã mở" · "Giờ mở lời giải {tên bộ} đổi sang {HH:mm, dd/MM/yyyy}" | — |

## US-S3E4-01 — Tổng quan giáo viên web + điện thoại: bài đang giao, lời giải đang khóa, em chờ duyệt
- **Story:** Là giáo viên, tôi muốn mở một màn là biết hôm nay có bài nào đang giao, lời giải nào đang khóa và bao nhiêu em chờ duyệt, để quyết việc cần làm trước tiết. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E4-01; STAGES Ch3 §5 luồng C bước 1, §6 C3-05; ảnh concept `shots/22`, `dev-iphone-gv-tong-quan` (có sẵn từ MVP, không vẽ lại trong T-10, DQ.md mục "Chưa vẽ")
- **Góc nhìn người vận hành (M18):** cô Lan mở điện thoại trên đường đến lớp, liếc thấy "1 em chưa mở bài" và "3 em chờ duyệt". Ca đáng kể: chưa có lớp, không có việc gì, số chờ duyệt lớn, màn hình nhỏ.
- **Phụ thuộc:** US-S3E1-01…03 (lớp, chờ duyệt); US-S3E2-01, 02 (bộ, giờ mở).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E4-01 | Ba thẻ: "Bài đang giao" (bộ Đã giao chưa Đã đóng, mỗi dòng tên bộ, lớp, hạn, số em chưa mở bài), "Lời giải đang khóa" (bộ hoặc đề ở Chưa đặt giờ hoặc Sắp mở, kèm giờ mở), "Em chờ duyệt" (tổng và theo lớp, liên kết tới danh sách). | Ch3 §6 C3-05 |
| BR-S3E4-02 | Chỉ tính lớp của giáo viên đang đăng nhập; dữ liệu tổng quan chỉ gồm số và tên lớp, tên bộ, không tên học sinh. | PO-61; BR-S3E1-50 |
| BR-S3E4-03 | Số lớn hiển thị "99+" khi từ 100 trở lên; 0 thì thẻ "Em chờ duyệt" ẩn. | QA-S3E4-03 |
| BR-S3E4-04 | Web và điện thoại dùng cùng dữ liệu; điện thoại xếp thẻ một cột và đặt hành động nhanh (liên kết tới Theo dõi nhanh) lên đầu. | C3-05, C3-06 |
| BR-S3E4-05 | Chưa có lớp thì hiện lời mời tạo lớp; có lớp mà không có việc thì hiện MSG-S3-65. | Ch3 §6 C3-05 |
| BR-S3E4-06 | "Chưa mở bài" nghĩa là học sinh Đang học chưa có tương tác nào trên mọi đề của bộ. | QA-S3E4-01 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E4-03.

### Tiêu chí chấp nhận

**US-S3E4-01-SC1 — Ba thẻ đúng số liệu trên web** · Nguồn: BR-S3E4-01
- Given: cô Lan có 11A1 và 11A2; bộ tuần 15 của 11A1 Đã giao, Sắp mở lúc 06/12/2026 08:00, 1 em chưa mở (Khánh Linh); bộ tuần 15 của 11A2 Đã giao, Sắp mở, 0 em chưa mở; 3 em Chờ duyệt ở 11A1 (F-S3-050, F-S3-053, F-S3-030…032)
- When: mở Tổng quan trên web
- Then: thẻ "Bài đang giao" có hai dòng: "Bộ bài tuần 15 · 11A1 · hạn T6 04/12/2026 20:00 · 1 em chưa mở" và dòng của 11A2 "0 em chưa mở"; thẻ "Lời giải đang khóa" có hai dòng cùng giờ mở "08:00, 06/12/2026"; thẻ "Em chờ duyệt" ghi 3, tách "11A1: 3"


**US-S3E4-01-SC2 — Cùng dữ liệu trên điện thoại, xếp một cột** · Nguồn: BR-S3E4-04
- Given: dữ liệu như AC SC1; điện thoại màn dọc 390×844
- When: mở Tổng quan trên điện thoại
- Then: ba thẻ xếp một cột với cùng số liệu như web; liên kết tới Theo dõi nhanh nằm ở đầu trang; không có thanh cuộn ngang; nút và dòng bấm được cao ít nhất 44 điểm ảnh


**US-S3E4-01-SC3 — Chưa có lớp** · Nguồn: BR-S3E4-05
- Given: một giáo viên mới đăng ký, chưa có lớp nào
- When: mở Tổng quan
- Then: hiện MSG-S3-66 kèm nút "Tạo lớp" dẫn tới màn tạo lớp; không có ba thẻ rỗng


**US-S3E4-01-SC4 — Có lớp nhưng không có việc** · Nguồn: BR-S3E4-05
- Given: giáo viên có một lớp không có bộ Đã giao nào và không có em Chờ duyệt
- When: mở Tổng quan
- Then: hiện MSG-S3-65; không có thẻ nào mang số 0 gây rối ngoài lời chú


**US-S3E4-01-SC5 — Sau khi giao một bộ mới, tổng quan thêm một dòng** · Nguồn: Ch3 §5 luồng B bước 3
- Given: tổng quan đang có 2 dòng "Bài đang giao"; bộ "Bộ bài tuần 16" Nháp (F-S3-052)
- When: giao "Bộ bài tuần 16", rồi mở lại Tổng quan
- Then: thẻ "Bài đang giao" có 3 dòng, dòng mới ghi "Bộ bài tuần 16 · 11A1" với 8 em chưa mở


**US-S3E4-01-SC6 — Quá hạn vẫn còn trong "Bài đang giao" cho tới khi đóng** · Nguồn: BR-S3E4-01; BR-S3E2-03
- Given: bộ tuần 15 đã quá hạn làm 04/12/2026 20:00 và chưa Đã đóng; giờ máy chủ 05/12/2026 (F-S3-050)
- When: mở Tổng quan
- Then: dòng bộ tuần 15 vẫn có trong "Bài đang giao" kèm nhãn "Quá hạn"; không biến mất vì quá hạn


**US-S3E4-01-SC7 — Lời giải đã mở rời khỏi thẻ "đang khóa"** · Nguồn: BR-S3E4-01
- Given: bộ tuần 15 mở lời giải lúc 06/12/2026 08:00; giờ máy chủ 06/12/2026 08:01 (F-S3-050)
- When: mở Tổng quan
- Then: dòng bộ tuần 15 không còn trong "Lời giải đang khóa" và có chip MSG-S3-64 "Đã mở" ở thẻ "Bài đang giao"


**US-S3E4-01-FC1 — Biên số em chờ duyệt: 99 và 100** · Nguồn: BR-S3E4-03 · PROVISIONAL QA-S3E4-03
- Given: lần lượt 99 rồi 100 học sinh Chờ duyệt ở một lớp (dữ liệu thử)
- When: mở Tổng quan ở mỗi trường hợp
- Then: với 99 thẻ ghi "99"; với 100 thẻ ghi "99+"; thân phản hồi vẫn chứa số đúng là 100 cho lần gọi API


**US-S3E4-01-FC2 — Thẻ "Em chờ duyệt" ẩn khi 0** · Nguồn: BR-S3E4-03
- Given: không em nào Chờ duyệt ở các lớp của cô Lan
- When: mở Tổng quan
- Then: thẻ "Em chờ duyệt" không hiện; hai thẻ còn lại hiện bình thường


**US-S3E4-01-FC3 — Giáo viên khác chỉ thấy lớp của mình** · Nguồn: BR-S3E4-02
- Given: thầy Khôi đã đăng nhập; cô Lan có em Chờ duyệt và bộ tuần 15 (F-S3-002, F-S3-050)
- When: thầy Khôi gọi API Tổng quan
- Then: thân phản hồi chỉ có dữ liệu của 10B; không có tên lớp 11A1, 11A2, tên bộ tuần 15 hay số em Chờ duyệt của cô Lan


**US-S3E4-01-FC4 — Tổng quan không chứa tên học sinh** · Nguồn: BR-S3E4-02
- Given: dữ liệu như AC SC1
- When: gọi API Tổng quan và liệt kê mọi giá trị chuỗi
- Then: không có tên hiển thị hay mã người dùng `HS-…` của học sinh nào; chỉ có số và tên lớp, tên bộ, giờ; có `Cache-Control: no-store`


**US-S3E4-01-FC5 — Học sinh không gọi được Tổng quan** · Nguồn: BR-S3E4-02
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi API Tổng quan của giáo viên
- Then: máy chủ trả FORBIDDEN; không có dữ liệu nào


**US-S3E4-01-FC6 — Bộ Nháp không vào "Bài đang giao"** · Nguồn: BR-S3E4-01
- Given: "Bộ bài tuần 16" Nháp (F-S3-052)
- When: mở Tổng quan
- Then: không có dòng "Bộ bài tuần 16" ở bất kỳ thẻ nào


## US-S3E4-02 — Theo dõi nhanh trên điện thoại: em chưa làm, Mở ngay, Đổi giờ
- **Story:** Là giáo viên, tôi muốn trên điện thoại thấy em nào chưa làm và có thể mở lời giải ngay hoặc đổi giờ mở, để quyết nhanh khi đang ở lớp. · Loại UI · P0 · Độ khó 4
- **Nguồn:** story map US-S3E4-02; STAGES Ch3 §5 luồng C bước 1–2, §6 C3-06, §7 ý "mọi lần mở sớm, đổi giờ đều được ghi lại"; ảnh concept `dev-iphone-gv-theo-doi`; M-S3-05
- **Góc nhìn người vận hành (M18):** cô Lan đứng trước lớp, thấy cả lớp đã làm xong nên bấm "Mở ngay" để chữa liền; hoặc thấy còn nhiều em chưa làm nên đổi giờ mở sang tối. Ca đáng kể: bấm hai lần, mất mạng giữa chừng, đồng hồ điện thoại sai, web và điện thoại cùng đổi.
- **Phụ thuộc:** US-S3E2-02 (lịch mở), US-S3E4-03 (thông báo), US-S3E5-01 (AuditEvent).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E4-10 | Màn hiện danh sách học sinh Đang học chưa mở bài của bộ đã chọn (tên và mã người dùng), kèm nút "Mở ngay" và "Đổi giờ" cho bộ. | Ch3 §6 C3-06 |
| BR-S3E4-11 | "Mở ngay" đặt `release_at` bằng giờ máy chủ lúc xử lý (không dùng giờ điện thoại), sau hộp xác nhận MSG-S3-61. | Ch3 §5 luồng C; D-GEO-054 |
| BR-S3E4-12 | "Đổi giờ" chỉ nhận giờ mới lớn hơn giờ máy chủ ít nhất 1 phút; nhỏ hơn thì VALIDATION_FAILED MSG-S3-63. | QA-S3E4-04 |
| BR-S3E4-13 | Lời giải đã mở thì "Mở ngay" và "Đổi giờ" là CONFLICT MSG-S3-67; hai thao tác đồng thời: thao tác đến sau là CONFLICT. | BR-S3E2-14 |
| BR-S3E4-14 | Mỗi lần "Mở ngay" hoặc "Đổi giờ" thành công ghi một AuditEvent (EARLY_RELEASE hoặc RELEASE_TIME_CHANGE) và gửi một Notification cho học sinh Đang học của lớp. | Ch3 §7; US-S3E4-03 |
| BR-S3E4-15 | Thao tác nhạy cảm không xếp hàng khi mất mạng: mất mạng thì nút tắt kèm MSG-S3-62, không tự gửi lại khi có mạng. | C3-06 trạng thái "mất mạng"; QA-S3E4-05 |
| BR-S3E4-16 | Mỗi yêu cầu mang khóa chống lặp; gửi lặp cùng khóa chỉ có một tác dụng, một AuditEvent, một đợt Notification. | CL-FN-10 |
| BR-S3E4-17 | Trên điện thoại "Mở ngay" và "Đổi giờ" tác động lên cả bộ; từng đề chỉ ở web. | QA-S3E4-06 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E4-01, QA-S3E4-04, QA-S3E4-05.

### Tiêu chí chấp nhận

**US-S3E4-02-SC1 — Danh sách em chưa làm** · Nguồn: BR-S3E4-10; BR-S3E4-06 · PROVISIONAL QA-S3E4-01
- Given: bộ tuần 15 của 11A1; chỉ Khánh Linh chưa có tương tác nào ở bộ (F-S3-051, F-S3-025)
- When: mở Theo dõi nhanh trên điện thoại, chọn lớp 11A1 và "Bộ bài tuần 15"
- Then: danh sách có đúng một dòng "Khánh Linh · `HS-7T1B-90QZ`"; không có em nào đã làm một phần (Bảo, Mai, Ngọc Anh); không có điểm hay xếp hạng


**US-S3E4-02-SC2 — Mở ngay: lời giải mở, thông báo, AuditEvent** · Nguồn: BR-S3E4-11, 14; Ch3 §5
- Given: bộ tuần 15 Sắp mở (giờ mở 06/12/2026 08:00), giờ máy chủ 05/12/2026 21:00 (F-S3-050)
- When: bấm "Mở ngay", rồi bấm "Mở ngay" ở hộp MSG-S3-61
- Then: giờ mở của bộ thành giờ máy chủ lúc xử lý (05/12/2026 21:00); thẻ đổi sang chip MSG-S3-64 "Đã mở"; Bình An lấy được lời giải đầy đủ ngay yêu cầu kế tiếp; có một AuditEvent EARLY_RELEASE ghi giờ cũ 06/12/2026 08:00 và giờ mới 05/12/2026 21:00; 8 em Đang học nhận mỗi em một Notification "Lời giải Bộ bài tuần 15 đã mở"


**US-S3E4-02-SC3 — Đổi giờ sang giờ tương lai hợp lệ** · Nguồn: BR-S3E4-12, 14
- Given: bộ tuần 15 Sắp mở, giờ mở 06/12/2026 08:00; giờ máy chủ 05/12/2026 21:00 (F-S3-050)
- When: bấm "Đổi giờ", rồi chọn "06/12/2026 19:00", rồi xác nhận
- Then: giờ mở của bộ là 06/12/2026 19:00; thẻ vẫn Sắp mở; có một AuditEvent RELEASE_TIME_CHANGE ghi giờ cũ 08:00 và giờ mới 19:00 ngày 06/12/2026; 8 em Đang học nhận Notification "Giờ mở lời giải Bộ bài tuần 15 đổi sang 19:00, 06/12/2026"
- Dữ liệu nhập: "06/12/2026 19:00"


**US-S3E4-02-SC4 — Biên giờ mới: sau giờ hiện tại đúng 1 phút** · Nguồn: BR-S3E4-12 · PROVISIONAL QA-S3E4-04
- Given: bộ tuần 15 Sắp mở; giờ máy chủ 05/12/2026 21:00:00 (F-S3-050)
- When: gửi đổi giờ sang 21:01:00 cùng ngày, rồi gửi đổi giờ sang 21:00:59 cùng ngày
- Then: giờ 21:01:00 được nhận; giờ 21:00:59 bị VALIDATION_FAILED kèm MSG-S3-63 và giờ mở giữ nguyên


**US-S3E4-02-SC5 — Mở ngay khi bộ chưa đặt giờ** · Nguồn: BR-S3E4-11
- Given: bộ "Bộ bài tuần 16" đã giao, Chưa đặt giờ (F-S3-052)
- When: bấm "Mở ngay", rồi xác nhận
- Then: lời giải mở ngay; AuditEvent EARLY_RELEASE ghi giờ cũ là "chưa đặt" và giờ mới là giờ máy chủ


**US-S3E4-02-FC1 — Đã mở thì Mở ngay và Đổi giờ bị chặn** · Nguồn: BR-S3E4-13
- Given: bộ tuần 15 đã mở lời giải từ 06/12/2026 08:00 (F-S3-050)
- When: gửi "Mở ngay", rồi gửi "Đổi giờ" sang 07/12/2026 08:00
- Then: cả hai trả CONFLICT kèm MSG-S3-67; giờ mở không đổi; không có AuditEvent hay Notification mới


**US-S3E4-02-FC2 — Bấm Mở ngay hai lần chỉ một tác dụng** · Nguồn: BR-S3E4-16
- Given: bộ tuần 15 Sắp mở (F-S3-050)
- When: gửi hai yêu cầu "Mở ngay" cùng khóa chống lặp, cách nhau 1 giây
- Then: chỉ một AuditEvent EARLY_RELEASE; mỗi học sinh Đang học nhận đúng một Notification; hai phản hồi cùng kết quả thành công


**US-S3E4-02-FC3 — Mất mạng thì nút tắt, không tự gửi lại** · Nguồn: BR-S3E4-15 · PROVISIONAL QA-S3E4-05
- Given: cô Lan đang ở hộp MSG-S3-61 trên điện thoại; mạng bị cắt
- When: bấm "Mở ngay", rồi bật lại mạng và chờ 60 giây
- Then: hiện MSG-S3-62; nút "Mở ngay" không báo đã thành công; sau khi có mạng không có yêu cầu nào tự gửi đi; lời giải vẫn khóa; không AuditEvent


**US-S3E4-02-FC4 — Web và điện thoại đổi chồng nhau** · Nguồn: BR-S3E4-13
- Given: bộ tuần 15 Sắp mở; cô Lan mở hai thiết bị cùng đọc lịch ở phiên bản số 3 (F-S3-050)
- When: web đổi giờ sang 06/12/2026 10:00, rồi điện thoại bấm "Mở ngay" với phiên bản số 3
- Then: yêu cầu của điện thoại bị CONFLICT; giờ mở là 06/12/2026 10:00; điện thoại tải lại thấy giờ mới; chỉ một AuditEvent


**US-S3E4-02-FC5 — Giáo viên khác không Mở ngay được** · Nguồn: BR-S3E4-10
- Given: bộ tuần 15 của 11A1 (F-S3-050)
- When: thầy Khôi gọi thẳng "Mở ngay" và "Đổi giờ" của bộ đó
- Then: cả hai trả NOT_FOUND; giờ mở không đổi; không có Notification; có một sự kiện từ chối quan hệ ghi cho mỗi yêu cầu (QA-S3E5-02)


**US-S3E4-02-FC6 — Học sinh không Mở ngay được** · Nguồn: BR-S3E4-10
- Given: Bình An Đang học 11A1 (F-S3-020)
- When: gọi thẳng "Mở ngay" của bộ tuần 15
- Then: máy chủ trả FORBIDDEN; lời giải vẫn khóa; không có AuditEvent EARLY_RELEASE


**US-S3E4-02-FC7 — Giờ điện thoại sai không ảnh hưởng** · Nguồn: BR-S3E4-11
- Given: đồng hồ điện thoại của cô Lan chạy nhanh 3 giờ so với máy chủ; bộ tuần 15 Sắp mở (F-S3-050)
- When: bấm "Mở ngay", rồi gửi kèm giờ điện thoại trong thân yêu cầu
- Then: giờ mở của bộ bằng giờ máy chủ; giờ điện thoại bị bỏ qua và không xuất hiện trong AuditEvent


**US-S3E4-02-FC8 — Hủy ở hộp xác nhận không đổi gì** · Nguồn: BR-S3E4-11
- Given: hộp MSG-S3-61 đang mở (F-S3-050)
- When: bấm "Hủy"
- Then: lời giải vẫn khóa; không AuditEvent; không Notification


**US-S3E4-02-FC9 — Hiển thị trên điện thoại màn dọc** · Nguồn: C3-06
- Given: điện thoại màn dọc 390×844; bộ tuần 15 Sắp mở (F-S3-050)
- When: mở Theo dõi nhanh
- Then: danh sách em chưa làm và hai nút "Mở ngay", "Đổi giờ" nằm trong một màn không cuộn ngang; nút cao ít nhất 44 điểm ảnh; chữ không bị cắt


## US-S3E4-03 — Notification trong ứng dụng cho học sinh (chưa đọc, đã đọc)
- **Story:** Là học sinh, tôi muốn thấy trong ứng dụng khi có bài mới, khi lời giải mở và khi giờ mở đổi, để không bỏ sót việc. · Loại Logic · P0 · Độ khó 3
- **Nguồn:** story map US-S3E4-03; STAGES Ch3 §5 luồng C bước 2, §8 `Notification`; PO-65 (Q9: chỉ thông báo trong ứng dụng, không đẩy ngoài); ảnh concept `mvp-04` (Bài của em)
- **Góc nhìn người vận hành (M18):** Bình An mở ứng dụng thấy chấm đỏ "2", bấm vào đọc "Có bài mới" rồi vào làm. Ca đáng kể: em vào lớp sau khi đã giao, sự kiện bị xử lý lặp, thông báo vô tình chứa đáp số.
- **Phụ thuộc:** US-S3E2-01, US-S3E4-02; S3-E6 (nhắc việc dùng lại bảng này).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E4-20 | Ba loại ở chặng 3 P0: bài mới (khi Giao), lời giải đã mở (khi tới giờ mở hoặc Mở ngay), giờ mở đổi (khi Đổi giờ). Nhắc việc thuộc S3-E6. | Ch3 §5; §8 |
| BR-S3E4-21 | Người nhận là học sinh Đang học của lớp tại lúc xảy ra sự kiện; Chờ duyệt, Đã rời, Bị từ chối không nhận; em vào lớp sau không nhận thông báo cũ. | BR-S3E1-53; QA-S3E4-07 |
| BR-S3E4-22 | Mỗi sự kiện tạo đúng một thông báo cho mỗi người nhận (khóa chống lặp theo sự kiện và người nhận). | CL-FN-10 |
| BR-S3E4-23 | Trạng thái Chưa đọc rồi Đã đọc; mở thông báo đánh dấu Đã đọc; có "Đánh dấu tất cả đã đọc"; chấm số chưa đọc hiện đến 99 rồi "99+". | Ch3 §8 |
| BR-S3E4-24 | Chỉ người nhận đọc được thông báo của mình; danh sách phân trang theo con trỏ, 20 dòng mỗi trang. | BR-S3E1-52; D-GEO-142 |
| BR-S3E4-25 | Nội dung thông báo không chứa đáp số, bước giải, tên hay kết quả của bạn; chỉ tên bộ và giờ. | BR-S3E2-12 |
| BR-S3E4-26 | Chỉ thông báo trong ứng dụng: không đẩy ra ngoài (push, email, SMS). | PO-65 (Q9) |
| BR-S3E4-27 | Giữ thông báo 90 ngày rồi xóa. | QA-S3E4-09 |

- **DoR:** Ready for R3; dựng trên giả định TẠM (PROVISIONAL): QA-S3E4-07, QA-S3E4-09.

### Tiêu chí chấp nhận

**US-S3E4-03-SC1 — Giao bộ thì 8 em Đang học nhận thông báo "bài mới"** · Nguồn: BR-S3E4-20, 21
- Given: "Bộ bài tuần 16" vừa được giao cho 11A1; 8 em Đang học, 3 em Chờ duyệt (F-S3-052, F-S3-010)
- When: Bình An mở ứng dụng
- Then: chấm thông báo ghi 1; danh sách có thông báo "Có bài mới: Bộ bài tuần 16" ở trạng thái Chưa đọc; 3 em Chờ duyệt (F-S3-030…032) không có thông báo nào


**US-S3E4-03-SC2 — Mở ngay thì có thông báo "lời giải đã mở"** · Nguồn: BR-S3E4-20
- Given: cô Lan vừa "Mở ngay" bộ tuần 15 (F-S3-050)
- When: Bình An mở danh sách thông báo
- Then: có "Lời giải Bộ bài tuần 15 đã mở" Chưa đọc; bấm vào đưa tới bộ tuần 15 với lời giải đã mở


**US-S3E4-03-SC3 — Mở thông báo thì đánh dấu đã đọc** · Nguồn: BR-S3E4-23
- Given: Bình An có 2 thông báo Chưa đọc
- When: bấm vào thông báo thứ nhất
- Then: thông báo đó thành Đã đọc; chấm thông báo giảm từ 2 xuống 1; thông báo thứ hai vẫn Chưa đọc


**US-S3E4-03-SC4 — Đánh dấu tất cả đã đọc** · Nguồn: BR-S3E4-23
- Given: Bình An có 5 thông báo Chưa đọc
- When: bấm "Đánh dấu tất cả đã đọc"
- Then: cả 5 thành Đã đọc; chấm thông báo biến mất; thông báo của các bạn không đổi


**US-S3E4-03-SC5 — Đổi giờ thì thông báo ghi giờ mới, không lộ lời giải** · Nguồn: BR-S3E4-20, 25
- Given: cô Lan vừa đổi giờ mở bộ tuần 15 sang 19:00, 06/12/2026 (F-S3-050)
- When: Bình An gọi API danh sách thông báo
- Then: có "Giờ mở lời giải Bộ bài tuần 15 đổi sang 19:00, 06/12/2026"; thân thông báo không chứa đáp số, bước giải hay tên bạn nào


**US-S3E4-03-FC1 — Em vào lớp sau khi giao không nhận thông báo cũ** · Nguồn: BR-S3E4-21 · PROVISIONAL QA-S3E4-07
- Given: bộ tuần 15 giao trước khi Thu Hà được duyệt vào 11A1 (F-S3-031)
- When: Thu Hà được duyệt, rồi mở danh sách thông báo
- Then: không có thông báo "Có bài mới" của bộ tuần 15; bộ tuần 15 vẫn có trong Bài của em


**US-S3E4-03-FC2 — Biên chấm số: 99 và 100 thông báo chưa đọc** · Nguồn: BR-S3E4-23
- Given: lần lượt 99 rồi 100 thông báo Chưa đọc của một học sinh thử
- When: mở ứng dụng ở mỗi trường hợp
- Then: với 99 chấm ghi "99"; với 100 chấm ghi "99+"; thân phản hồi chứa số đúng


**US-S3E4-03-FC3 — Học sinh không đọc được thông báo của bạn** · Nguồn: BR-S3E4-24
- Given: Chi Mai có một thông báo mang mã nội bộ đã biết (F-S3-021)
- When: Bình An gọi chi tiết thông báo đó và gọi danh sách thông báo
- Then: chi tiết trả NOT_FOUND; danh sách chỉ có thông báo của Bình An


**US-S3E4-03-FC4 — Chỉ thông báo trong ứng dụng, không gửi ra ngoài** · Nguồn: BR-S3E4-26; PO-65 (Q9)
- Given: nhà cung cấp gửi tin ngoài (push, email, SMS) được giả lập để đếm lượt gọi
- When: giao bộ, Mở ngay và Đổi giờ ở 11A1
- Then: số lượt gọi nhà cung cấp gửi tin ngoài bằng 0; bảng cấu hình không có địa chỉ email, số điện thoại hay mã đăng ký đẩy của học sinh nào


**US-S3E4-03-FC5 — Sự kiện xử lý lặp không tạo hai thông báo** · Nguồn: BR-S3E4-22
- Given: sự kiện "bài mới" của bộ tuần 16 bị xử lý hai lần do thử lại
- When: chạy lại bộ xử lý sự kiện
- Then: mỗi học sinh Đang học vẫn đúng một thông báo cho sự kiện đó; ràng buộc duy nhất theo sự kiện và người nhận từ chối bản ghi trùng


**US-S3E4-03-FC6 — Danh sách thông báo phân trang 20 dòng** · Nguồn: BR-S3E4-24
- Given: một học sinh có 45 thông báo
- When: gọi trang đầu, rồi gọi trang kế bằng con trỏ, rồi gọi trang cuối, rồi gọi với con trỏ đã bị sửa
- Then: ba trang có 20, 20, 5 dòng, mới nhất trước, không trùng; con trỏ bị sửa trả INVALID_CURSOR; không có tổng số bản ghi


**US-S3E4-03-FC7 — Học sinh Đã rời không nhận thông báo mới** · Nguồn: BR-S3E4-21
- Given: Văn Tú Đã rời 11A1 (F-S3-028)
- When: cô Lan giao "Bộ bài tuần 16", rồi Văn Tú mở danh sách thông báo
- Then: Văn Tú không có thông báo "Có bài mới: Bộ bài tuần 16"


**US-S3E4-03-FC8 — Thông báo quá 90 ngày bị xóa** · Nguồn: BR-S3E4-27 · PROVISIONAL QA-S3E4-09
- Given: hai thông báo, một tạo cách đây 89 ngày và một cách đây 91 ngày (dữ liệu thử)
- When: chạy tác vụ dọn dẹp
- Then: thông báo 91 ngày bị xóa; thông báo 89 ngày còn nguyên


**US-S3E4-03-FC9 — Học sinh không tạo được thông báo** · Nguồn: BR-S3E4-24
- Given: Bình An đã đăng nhập vai Học sinh (F-S3-020)
- When: gọi thẳng yêu cầu tạo thông báo cho Chi Mai
- Then: máy chủ trả FORBIDDEN; Chi Mai không có thông báo mới

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E4, ready for R3 | runner-T-261126-14 (BA) |
