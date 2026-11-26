Phiên bản: v0.3 · 2026-11-26
# S3-E3 — Kết quả lớp (bảng lớp × bài) và "Cả lớp vướng ở đâu" — màn ưu tiên C3-04: story Dev-Ready và tiêu chí chấp nhận

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
| `MIN_AGGREGATE` | hằng cấu hình ngưỡng bài làm tối thiểu để gộp nhận định; mặc định 5 (đã chốt PO-66, PO-65 Q6, QA-S3E3-02) | tệp này |

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


### Thông điệp dùng trong tệp này (đề xuất của BA, QA-S3E3-07)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S3-41 | Page state | "Chưa em nào làm bài của bộ này." | — |
| MSG-S3-42 | Page state | "Lớp chưa có học sinh Đang học. Duyệt các em đang chờ để bắt đầu." | — |
| MSG-S3-43 | Banner | "Mới có {m} trên 5 bài làm cho đề này, chưa đủ để gộp. Dưới đây là dữ liệu thô." | — |
| MSG-S3-44 | Banner | "Lớp có ít hơn 5 học sinh, chưa gộp được. Dưới đây là dữ liệu thô." | — |
| MSG-S3-45 | Inline + nút "Thử lại" | "Chưa gộp được. Thử lại." | — |
| MSG-S3-46 | Page state | "Em chưa mở bài này." | — |
| MSG-S3-47 | Page state + nút "Thử lại" | "Chưa tải được kết quả lớp." | — |
| MSG-S3-48 | Chip | "AI gộp" · "Cần thầy cô xác nhận" (hai nhãn cố định trên mọi thẻ nhận định) | — |

## US-S3E3-01 — Bảng lớp × bài, ô là trạng thái, không cột tổng, không sắp "tốt nhất"
- **Story:** Là giáo viên, tôi muốn nhìn cả lớp trên cả bộ bài trong một bảng mà mỗi ô là một trạng thái có bằng chứng, để biết ai đã làm, ai cần xem lại mà không bị bảng đọc thành xếp hạng. · Loại UI · P0 · Độ khó 4
- **Nguồn:** story map US-S3E3-01; STAGES Ch3 §3 ý 5, §10 (rủi ro "đọc thành bảng xếp hạng"); PRODUCT-MASTER K3 (ưu tiên dồn công); DQ-21; ảnh `Design/s23/c3-04-ket-qua-lop.png`; M-S3-03
- **Góc nhìn người vận hành (M18):** cô Lan mở màn này tối T6 sau hạn, cần thấy trong vài giây em nào chưa làm để nhắn, đề nào nhiều em "Xem lại". Ca đáng kể: lớp 32 em, em đã rời, chưa ai làm, ô nào cũng Đúng, bảng bị chụp màn hình gửi phụ huynh (không được lộ xếp hạng).
- **Phụ thuộc:** US-S3E2-01 (bộ Đã giao); bài làm của MVP và S2-E8; US-S3E1-05 (quyền theo quan hệ).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E3-01 | Hàng là học sinh Đang học của lớp, cộng học sinh Đã rời đã có bài làm trong bộ (đánh dấu "đã rời"); Chờ duyệt và Bị từ chối không có hàng. Cột là các đề của bộ theo thứ tự bộ. | Ch3 §5 ngoại lệ luồng C |
| BR-S3E3-02 | Mỗi ô có đúng một trạng thái: Chưa làm (chưa có tương tác nào), Đang làm (có tương tác, chưa gửi đáp số cuối), Đúng (đáp số cuối khớp đáp số máy đã duyệt), Xem lại (đã gửi đáp số cuối mà chưa khớp, hoặc hết lượt thử). | DQ-21; QA-S3E3-01 |
| BR-S3E3-03 | Không có cột tổng, hàng tổng, phần trăm, điểm, số hạng, "tốt nhất" hay "kém nhất" ở giao diện, thân phản hồi, tệp xuất và nhật ký. | Ch3 §3 ý 5, §10; DQ-21 |
| BR-S3E3-04 | Hàng xếp theo tên hiển thị theo thứ tự chữ cái tiếng Việt (Đ sau D), hòa thì theo mã người dùng; thứ tự không đổi theo kết quả. | DQ-21; QA-S3E3-08 |
| BR-S3E3-05 | Màu không là phương tiện duy nhất: mỗi ô có chữ trạng thái và biểu tượng; chú giải màu ở chân bảng. | WCAG; DQ-21 |
| BR-S3E3-06 | Mỗi ô bấm được để xem bằng chứng (US-S3E3-02); ô không mang số liệu nào khác ngoài trạng thái. | 0.3 ý 6 |
| BR-S3E3-07 | Chỉ chủ lớp xem; thân phản hồi chỉ có tên hiển thị, mã người dùng, trạng thái ô; phản hồi `Cache-Control: no-store`. | PO-61; D-GEO-145 |
| BR-S3E3-08 | Bộ mặc định là bộ Đã giao mới nhất của lớp; chọn bộ khác đổi cột; bộ Nháp không có bảng. | Ch3 §5 luồng C |
| BR-S3E3-09 | Bảng hiển thị tối đa 100 hàng mỗi trang, phân trang theo con trỏ; không trả tổng số bản ghi. | D-GEO-142; QA-S3E3-09 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E3-09.

### Tiêu chí chấp nhận

**US-S3E3-01-SC1 — Bảng 8 học sinh × 4 đề đúng từng ô** · Nguồn: BR-S3E3-01, 02; DQ-21
- Given: cô Lan đã đăng nhập; bộ tuần 15 của 11A1 có trạng thái từng ô theo bảng F-S3-051 (F-S3-050)
- When: mở Theo dõi, chọn lớp 11A1, mở Kết quả lớp của "Bộ bài tuần 15"
- Then: bảng có đúng 8 hàng và 4 cột Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích; mỗi ô khớp F-S3-051, ví dụ Bình An Đề 1 Đúng, Chi Mai Đề 4 Chưa làm, Gia Bảo Đề 3 Đang làm, Khánh Linh cả bốn ô Chưa làm; dòng phụ đề hiện "8 em Đang học"


**US-S3E3-01-SC2 — Giao diện không có cột tổng, dòng tổng, phần trăm hay xếp hạng** · Nguồn: BR-S3E3-03
- Given: bảng của AC SC1 đang mở (F-S3-051)
- When: soát cột cuối, dòng cuối, đầu cột, chú giải, tooltip khi rê chuột và bản in của trang
- Then: không có cột "Tổng", không có dòng "Tổng", không phần trăm, không điểm, không số hạng, không chữ "tốt nhất" hay "kém nhất"; dòng phụ đề ghi "Xếp theo tên. Không có cột tổng, không xếp hạng."


**US-S3E3-01-SC3 — Thân phản hồi không chứa tổng, phần trăm, điểm hay hạng** · Nguồn: BR-S3E3-03; BR-S3E3-07
- Given: bảng của AC SC1 (F-S3-051)
- When: gọi API Kết quả lớp của bộ tuần 15 và duyệt toàn bộ khóa của thân phản hồi
- Then: mỗi hàng chỉ có tên hiển thị, mã người dùng, cờ "đã rời" và danh sách trạng thái ô theo thứ tự đề; không khóa nào tên là total, sum, score, percent, rank, average, best, worst hay tương đương; có `Cache-Control: no-store`; không có tổng số bản ghi


**US-S3E3-01-SC4 — Xếp theo tên theo chữ cái tiếng Việt** · Nguồn: BR-S3E3-04
- Given: lớp 11A1 như F-S3-051, thêm hai em Đang học là Dũng `HS-2D4F-78AA` và Đạt `HS-5G7H-28BB` đều làm Đề 1
- When: mở Kết quả lớp của bộ tuần 15
- Then: thứ tự hàng là Bình An, Chi Mai, Dũng, Đạt, Đức Huy, Gia Bảo, Hà My, Khánh Linh, Minh Quân, Ngọc Anh; Dũng đứng trước Đạt và Đạt đứng trước Đức Huy


**US-S3E3-01-SC5 — Hòa tên thì xếp theo mã người dùng** · Nguồn: BR-S3E3-04
- Given: 11A1 có hai em cùng tên "Hoàng Nam" Đang học mã `HS-7Q8Z-48DF` và `HS-9B2L-85VG` (F-S3-030, F-S3-032 sau khi được duyệt)
- When: mở Kết quả lớp
- Then: hai hàng "Hoàng Nam" liền nhau, `HS-7Q8Z-48DF` trước `HS-9B2L-85VG`; mỗi hàng hiện cả mã người dùng


**US-S3E3-01-SC6 — Đổi kết quả không đổi thứ tự hàng** · Nguồn: BR-S3E3-04
- Given: bảng của AC SC1; Gia Bảo đứng hàng thứ 4 với Đề 3 Đang làm (F-S3-051)
- When: Gia Bảo gửi đáp số đúng cho Đề 3, rồi cô Lan bấm "Làm mới"
- Then: ô Gia Bảo Đề 3 chuyển thành Đúng; Gia Bảo vẫn ở hàng thứ 4; thứ tự cả 8 hàng giữ nguyên


**US-S3E3-01-SC7 — Mỗi ô có chữ và biểu tượng, không dựa riêng vào màu** · Nguồn: BR-S3E3-05
- Given: bảng của AC SC1; trình duyệt chuyển sang thang xám
- When: nhìn từng loại ô
- Then: ô Đúng có chữ "Đúng" và dấu tick, ô Xem lại có chữ "Xem lại" và biểu tượng mắt, ô Đang làm có chữ "Đang làm" và biểu tượng bút, ô Chưa làm có chữ "Chưa làm" và biểu tượng đồng hồ; tên truy cập của ô (cho trình đọc màn hình) nêu cả tên học sinh, tên đề và trạng thái


**US-S3E3-01-SC8 — Chọn bộ khác thì cột đổi theo** · Nguồn: BR-S3E3-08
- Given: 11A1 có "Bộ bài tuần 15" Đã giao và "Bộ bài tuần 16" Đã giao với 2 đề (F-S3-050, F-S3-052 sau khi giao)
- When: mở Kết quả lớp (không chọn bộ), rồi đổi bộ sang "Bộ bài tuần 15"
- Then: lúc mở, bộ mặc định là "Bộ bài tuần 16" (mới giao nhất) với 2 cột; sau khi đổi, bảng có 4 cột của tuần 15 và các ô theo F-S3-051


**US-S3E3-01-SC9 — Bộ chỉ một đề và bộ 20 đề** · Nguồn: BR-S3E3-01
- Given: một bộ Đã giao có đúng 1 đề và một bộ Đã giao có đúng 20 đề của 11A1
- When: mở Kết quả lớp của từng bộ
- Then: bộ 1 đề có 1 cột; bộ 20 đề có 20 cột, cột "Học sinh" cố định bên trái và các cột đề cuộn ngang được; mọi ô vẫn đọc được trạng thái


**US-S3E3-01-SC10 — Lớp 100 học sinh vẫn nằm trong một trang và trong ngưỡng thời gian** · Nguồn: BR-S3E3-09 · PO-66 (QA-S3E3-09)
- Given: dữ liệu thử có một lớp 100 học sinh Đang học và một bộ 20 đề với ô ngẫu nhiên (dữ liệu thử tải, không phải dữ liệu thật)
- When: gọi API Kết quả lớp 30 lần liên tiếp và đếm câu lệnh truy vấn của mỗi lần
- Then: mỗi lần trả đủ 100 hàng trong một trang; số câu truy vấn không đổi theo số hàng (không truy vấn lặp theo từng hàng); thời gian xử lý p95 không quá 2 giây (đề xuất)


**US-S3E3-01-FC1 — Chưa ai làm bài của bộ** · Nguồn: BR-S3E3-08
- Given: bộ "Bộ bài tuần 16" vừa giao, 8 em Đang học chưa ai mở (F-S3-052)
- When: mở Kết quả lớp của bộ đó
- Then: bảng hiện 8 hàng với mọi ô Chưa làm; hiện MSG-S3-41; không có thẻ nhận định nào; không có lỗi


**US-S3E3-01-FC2 — Lớp không có học sinh Đang học** · Nguồn: BR-S3E3-01
- Given: lớp mới "11B3" vừa tạo, 0 học sinh, đã có một bộ Đã giao
- When: mở Kết quả lớp
- Then: hiện MSG-S3-42 kèm liên kết tới danh sách Chờ duyệt; không có bảng rỗng không giải thích


**US-S3E3-01-FC3 — Học sinh Chờ duyệt không có hàng** · Nguồn: BR-S3E3-01
- Given: 11A1 có 3 em Chờ duyệt (F-S3-030…032) và 8 em Đang học
- When: mở Kết quả lớp của bộ tuần 15
- Then: bảng có đúng 8 hàng; không có hàng của Hoàng Nam, Thu Hà; thân phản hồi không chứa mã `HS-7Q8Z-48DF`


**US-S3E3-01-FC4 — Bộ Nháp không có bảng** · Nguồn: BR-S3E3-08
- Given: "Bộ bài tuần 16" đang Nháp (F-S3-052)
- When: chọn bộ đó ở Kết quả lớp
- Then: bộ Nháp không có trong danh sách chọn bộ; gọi thẳng API Kết quả lớp cho bộ Nháp trả NOT_FOUND


**US-S3E3-01-FC5 — Đang tải hiện khung chờ, lỗi tải hiện Thử lại** · Nguồn: BR-S3E3-06; C3-04
- Given: API Kết quả lớp được giả lập chậm 3 giây rồi lỗi
- When: mở Kết quả lớp, rồi chờ, rồi bấm "Thử lại"
- Then: trong lúc chờ hiện khung chờ có hình dạng bảng, không hiện dữ liệu của lớp hay bộ khác; khi lỗi hiện MSG-S3-47 với nút "Thử lại"; "Thử lại" gọi lại yêu cầu và hiện bảng khi thành công


**US-S3E3-01-FC6 — Giáo viên khác không xem được kết quả lớp 11A1** · Nguồn: BR-S3E3-07; PO-61
- Given: thầy Khôi đã đăng nhập (F-S3-002)
- When: gọi API Kết quả lớp của 11A1, bộ tuần 15
- Then: máy chủ trả NOT_FOUND; thân lỗi không chứa tên lớp, tên học sinh hay số học sinh


**US-S3E3-01-FC7 — Học sinh không xem được kết quả lớp** · Nguồn: BR-S3E3-07
- Given: Bình An Đang học 11A1 (F-S3-020)
- When: gọi API Kết quả lớp của bộ tuần 15 và API bằng chứng một ô của bạn
- Then: cả hai trả FORBIDDEN; không có dữ liệu của bạn nào trong thân lỗi


**US-S3E3-01-FC8 — Ghép lớp này với bộ của lớp khác** · Nguồn: BR-S3E3-07
- Given: cô Lan chủ cả 11A1 và 11A2; bộ tuần 15 của 11A2 (F-S3-053)
- When: gọi API Kết quả lớp với lớp 11A1 nhưng mã bộ là bộ tuần 15 của 11A2
- Then: máy chủ trả NOT_FOUND; không có hàng của em 11A2 trong thân phản hồi


**US-S3E3-01-FC9 — Kết quả của em ở lớp khác không lọt vào bảng** · Nguồn: BR-S3E3-01; BR-S3E1-24
- Given: Chi Mai Đang học 11A1 và 10B, đã có bài làm ở bộ của 10B (F-S3-040, F-S3-021)
- When: cô Lan gọi API Kết quả lớp của 11A1, bộ tuần 15
- Then: hàng Chi Mai chỉ có bốn ô của 11A1; không có tên đề, trạng thái hay tương tác nào của bộ 10B trong thân phản hồi


**US-S3E3-01-FC10 — Phân trang theo con trỏ ở lớp trên 100 em** · Nguồn: BR-S3E3-09 · PO-66 (QA-S3E3-09)
- Given: một lớp có 101 học sinh Đang học
- When: gọi trang đầu, rồi gọi trang kế bằng con trỏ nhận được, rồi gọi trang kế với con trỏ đã bị sửa một ký tự
- Then: trang đầu 100 hàng, trang hai 1 hàng, không trùng hàng; con trỏ bị sửa trả INVALID_CURSOR; không có tổng số bản ghi


**US-S3E3-01-FC11 — Học sinh Đã rời có hàng, dấu "đã rời" và nằm đúng chỗ** · Nguồn: BR-S3E3-01
- Given: Văn Tú Đã rời 11A1, đã có bài làm ở bộ tuần 15 (F-S3-028)
- When: mở Kết quả lớp của bộ tuần 15
- Then: bảng có 9 hàng gồm 8 em Đang học và Văn Tú; hàng Văn Tú có chip "đã rời" và các ô cũ; thứ tự vẫn theo tên


## US-S3E3-02 — Bấm một ô xem gợi ý, câu hỏi và bằng chứng; học sinh đã rời đánh dấu
- **Story:** Là giáo viên, tôi muốn bấm một ô để xem em đó đã làm gì ở đề đó (gợi ý, câu hỏi, đáp số), để mọi trạng thái trên bảng đều có bằng chứng và tôi biết nên hỏi em điều gì. · Loại UI · P0 · Độ khó 3
- **Nguồn:** story map US-S3E3-02; STAGES Ch3 §5 luồng C bước 3, ngoại lệ "học sinh rời lớp"; 0.3 ý 6; ảnh `c3-04-ket-qua-lop.png` (khung "Bằng chứng")
- **Góc nhìn người vận hành (M18):** cô Lan thấy ô "Xem lại" của Chi Mai ở Đề 3, bấm vào để đọc câu em hỏi trợ lý và đáp số em gửi rồi quyết định có nhắc em hay chữa chung. Ca đáng kể: em gõ mã HTML hay số điện thoại vào câu hỏi, em đã rời lớp, dữ liệu đổi trong lúc đang xem.
- **Phụ thuộc:** US-S3E3-01; nhật ký tương tác của MVP.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E3-20 | Khung bằng chứng của một ô gồm: tên em và mã người dùng, tên đề, trạng thái ô, các lần gửi đáp số (giờ, giá trị em nhập, khớp hay không), số và mức gợi ý đã dùng, các câu hỏi em đã hỏi (nguyên văn, kèm giờ), bước em dừng. | 0.3 ý 6 |
| BR-S3E3-21 | Mọi chữ do học sinh gõ hiển thị như chữ thuần: không diễn giải HTML, không chạy mã, không tự biến thành liên kết. | an toàn nội dung |
| BR-S3E3-22 | Ô Chưa làm hiện MSG-S3-46 và không dựng dữ liệu giả; ô Đang làm hiện các tương tác đã có, không có đáp số cuối. | DQ-21 |
| BR-S3E3-23 | Học sinh Đã rời: khung chỉ đọc kèm chip "đã rời"; dữ liệu giữ nguyên. | Ch3 §5 ngoại lệ luồng C |
| BR-S3E3-24 | "Mở bài làm" mở bài của em ở chế độ chỉ đọc, không ghi tương tác. Nút "Nhắn em" của bản mẫu không làm ở chặng 3. | QA-S3E3-03 |
| BR-S3E3-25 | Câu hỏi tự do của em có thể chứa dữ liệu cá nhân em tự gõ: không lọc ở giao diện giáo viên, không tách thành trường, không ghi nội dung vào nhật ký hệ thống. | PO-61; QA-S3E3-04 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E3-04.

### Tiêu chí chấp nhận

**US-S3E3-02-SC1 — Bấm ô Xem lại hiện đủ bằng chứng** · Nguồn: BR-S3E3-20
- Given: Chi Mai `HS-8L2K-77AB` ở Đề 3 có: mở đề 15:08, xin gợi ý mức 1 lúc 15:10, hỏi "Giao tuyến của hai mặt phẳng là đường nào ạ?" lúc 15:12, gửi đáp số "SB" lúc 15:14 không khớp (F-S3-051)
- When: ở Kết quả lớp, bấm ô Chi Mai × Đề 3
- Then: khung "BẰNG CHỨNG · Chi Mai · Đề 3" hiện mã `HS-8L2K-77AB`, trạng thái Xem lại, một lần gửi đáp số "SB" lúc 15:14 ghi không khớp, một gợi ý mức 1 lúc 15:10, đúng một câu hỏi nguyên văn lúc 15:12; có nút "Mở bài làm"


**US-S3E3-02-SC2 — Ô Chưa làm** · Nguồn: BR-S3E3-22
- Given: Khánh Linh chưa làm Đề 3 (F-S3-051)
- When: bấm ô Khánh Linh × Đề 3
- Then: khung hiện MSG-S3-46 "Em chưa mở bài này."; không có đáp số, gợi ý hay câu hỏi nào; không có nút "Mở bài làm"


**US-S3E3-02-SC3 — Ô Đang làm chỉ có tương tác đã xảy ra** · Nguồn: BR-S3E3-22
- Given: Gia Bảo Đang làm Đề 3 với một gợi ý mức 1 và chưa gửi đáp số (F-S3-051)
- When: bấm ô Gia Bảo × Đề 3
- Then: khung hiện một gợi ý mức 1 với giờ; không có dòng "đáp số cuối"; trạng thái ghi Đang làm


**US-S3E3-02-SC4 — Học sinh Đã rời: khung chỉ đọc có chip** · Nguồn: BR-S3E3-23
- Given: Văn Tú Đã rời 11A1, có Đề 1 trạng thái Đúng (F-S3-028)
- When: bấm ô Văn Tú × Đề 1
- Then: khung có chip "đã rời" và đủ dữ liệu cũ; không có nút nào sửa dữ liệu; "Mở bài làm" mở chế độ chỉ đọc


**US-S3E3-02-SC5 — "Mở bài làm" chỉ đọc, không sinh tương tác** · Nguồn: BR-S3E3-24
- Given: khung bằng chứng của Chi Mai × Đề 3 đang mở (F-S3-051)
- When: bấm "Mở bài làm", rồi xem đủ các bước, rồi quay lại Kết quả lớp
- Then: bài mở ở chế độ chỉ đọc không có ô nhập đáp số; số gợi ý, số câu hỏi, trạng thái ô của Chi Mai không đổi; không Notification nào gửi cho em


**US-S3E3-02-FC1 — Câu hỏi chứa mã HTML hiển thị như chữ thuần** · Nguồn: BR-S3E3-21
- Given: Chi Mai hỏi ở Đề 3 đúng câu `<script>alert(1)</script><b>đậm</b>` (F-S3-021)
- When: bấm ô Chi Mai × Đề 3
- Then: khung hiển thị đúng chuỗi ký tự trên như chữ; không có hộp thoại nào bật; không có chữ in đậm; thẻ `<script>` không có trong DOM dưới dạng phần tử
- Dữ liệu nhập: `<script>alert(1)</script><b>đậm</b>`


**US-S3E3-02-FC2 — Câu hỏi rất dài gập gọn nhưng đủ nội dung** · Nguồn: BR-S3E3-20 · PO-66 (QA-S3E3-04)
- Given: Chi Mai hỏi một câu 1000 ký tự ở Đề 3
- When: bấm ô Chi Mai × Đề 3, rồi bấm "Xem thêm"
- Then: ban đầu hiện 300 ký tự đầu kèm "Xem thêm"; sau khi bấm hiện đủ 1000 ký tự không bị cắt; khung không vỡ bố cục
- Dữ liệu nhập: câu hỏi 1000 ký tự


**US-S3E3-02-FC3 — Bằng chứng của ô thuộc em ngoài lớp** · Nguồn: BR-S3E1-50
- Given: cô Lan đã đăng nhập; Mi Na `HS-2A5F-77BC` chỉ thuộc 10B (F-S3-040)
- When: gọi API bằng chứng với mã Mi Na và mã một đề của bộ 10B
- Then: máy chủ trả NOT_FOUND; không có dữ liệu của Mi Na


**US-S3E3-02-FC4 — Giáo viên khác không đọc bằng chứng của lớp 11A1** · Nguồn: BR-S3E1-50
- Given: thầy Khôi đã đăng nhập (F-S3-002)
- When: gọi API bằng chứng của Chi Mai × Đề 3 ở lớp 11A1
- Then: máy chủ trả NOT_FOUND; thân lỗi không có tên Chi Mai hay nội dung câu hỏi


**US-S3E3-02-FC5 — Thân phản hồi bằng chứng chỉ có trường được phép** · Nguồn: BR-S3E5-03
- Given: khung bằng chứng của Chi Mai × Đề 3 (F-S3-021)
- When: gọi API bằng chứng và liệt kê mọi khóa của thân phản hồi
- Then: mọi khóa thuộc danh sách được phép của US-S3E5-03 (tên hiển thị, mã người dùng, trạng thái, tương tác, giờ máy chủ); không có email, điện thoại, ngày sinh, trường, ảnh, địa chỉ IP hay mã thiết bị


**US-S3E3-02-FC6 — Số điện thoại do em tự gõ trong câu hỏi** · Nguồn: BR-S3E3-25 · PO-66 (QA-S3E3-04)
- Given: Chi Mai hỏi "Em là Mai, số em 0912345678, cô giải giúp em" ở Đề 3 (F-S3-021)
- When: bấm ô Chi Mai × Đề 3, rồi kiểm tra nhật ký hệ thống và dữ liệu cơ sở dữ liệu
- Then: khung hiện đúng nguyên văn câu hỏi; không có trường nào của cơ sở dữ liệu tách số điện thoại ra riêng; nhật ký hệ thống không ghi nội dung câu hỏi
- Dữ liệu nhập: "Em là Mai, số em 0912345678, cô giải giúp em"


**US-S3E3-02-FC7 — Dữ liệu đổi trong lúc đang xem** · Nguồn: BR-S3E3-20
- Given: khung bằng chứng Gia Bảo × Đề 3 đang mở lúc 15:20, Gia Bảo Đang làm (F-S3-051)
- When: Gia Bảo gửi đáp số đúng lúc 15:21, rồi cô Lan bấm "Làm mới" ở khung
- Then: trước khi làm mới khung ghi "Cập nhật lúc 15:20" và trạng thái cũ; sau khi làm mới khung ghi "Cập nhật lúc 15:21", hiện thêm lần gửi đáp số, trạng thái Đúng; ô tương ứng trên bảng cũng đổi


## US-S3E3-03 — "Cả lớp vướng ở đâu": bước nhiều em dừng nhất; dưới ngưỡng 5 bài chỉ hiện dữ liệu thô
- **Story:** Là giáo viên, tôi muốn thấy bước nào của mỗi đề nhiều em dừng nhất kèm bài làm căn cứ, để biết hôm nay chữa phần nào; khi chưa đủ bài làm thì chỉ cho tôi dữ liệu thô, đừng suy diễn từ vài em. · Loại Logic · P0 · Độ khó 5
- **Nguồn:** story map US-S3E3-03; STAGES Ch3 §3 ý 6, §5 luồng C bước 4 + ngoại lệ, §7 ý cuối, §11 câu 4; PO-65 (Q6: ngưỡng 5 bài); DQ-22; ảnh `c3-04-ket-qua-lop.png` (khung "Cả lớp vướng ở đâu"); M-S3-03, M-S3-04
- **Góc nhìn người vận hành (M18):** trước tiết, cô Lan cần một câu trả lời "hôm nay chữa phần nào". Máy chỉ được nói khi đủ dữ liệu và luôn nói "thầy cô xác nhận lại". Ca đáng kể: đúng 4, đúng 5, đúng 6 bài làm; một em làm nhiều lần; em vừa rời lớp; AI hết giờ hoặc bịa căn cứ; lớp chỉ 3 em.
- **Phụ thuộc:** US-S3E3-01, US-S3E3-02; nhật ký gợi ý và câu hỏi của MVP; bộ gộp qua cổng AI giả lập (D-GEO-047).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S3E3-30 | Mỗi đề của bộ được xét riêng: đủ `MIN_AGGREGATE` (mặc định 5) bài làm thì gộp nhận định; chưa đủ thì chỉ dữ liệu thô. "Bài làm" tính theo số học sinh Đang học khác nhau có ít nhất một tương tác trên đề (kể cả Đang làm); một em tương tác nhiều lần chỉ tính một. | PO-65 (Q6); QA-S3E3-02 |
| BR-S3E3-31 | Mỗi thẻ nhận định gồm: tên đề, mô tả bước em dừng, số bài làm căn cứ n, hai nhãn "AI gộp" và "Cần thầy cô xác nhận" (MSG-S3-48), nút "Xem n bài làm", dòng "Cập nhật lúc HH:mm". | DQ-22 |
| BR-S3E3-32 | Thẻ sắp theo n giảm dần; hòa thì theo thứ tự đề rồi số bước; tối đa 5 thẻ mỗi bộ, phần còn lại gập vào "Còn k nhận định khác". | DQ-22; QA-S3E3-10 |
| BR-S3E3-33 | Mỗi thẻ có ít nhất một bài làm căn cứ và n bằng đúng số bài làm khớp; nhận định không có căn cứ bị loại; không thẻ nào mang nhãn "Đã kiểm chứng" (chỉ engine gắn). | 0.3 ý 6; M-S2-03 |
| BR-S3E3-34 | Dữ liệu thô: danh sách gợi ý và câu hỏi gom theo bước, không nhận định, không "n em"; kèm banner MSG-S3-43 (nêu m) hoặc MSG-S3-44 (lớp dưới 5 học sinh Đang học). | Ch3 §5 ngoại lệ |
| BR-S3E3-35 | Học sinh Đã rời không tính vào ngưỡng và không vào n; bài làm của họ vẫn xem được ở bảng. | QA-S3E3-06 |
| BR-S3E3-36 | Dữ liệu gửi cho bộ gộp là ẩn danh: không tên hiển thị, không mã người dùng, không mã lớp hay giáo viên; chỉ mã đề, số bước và nội dung câu hỏi hoặc gợi ý đã che chuỗi giống email hoặc số điện thoại thành `[đã ẩn]`. | PO-61, PO-64; QA-S3E3-05 |
| BR-S3E3-37 | Bộ gộp lỗi hoặc quá 10 giây thì hiện dữ liệu thô kèm MSG-S3-45 "Thử lại"; không hiện thẻ cũ của lần khác. | QA-S3E3-11 |
| BR-S3E3-38 | Gộp tính lại khi mở màn hoặc bấm "Làm mới"; nếu dữ liệu đầu vào không đổi thì dùng kết quả đã có, không gọi lại bộ gộp. | chi phí AI |
| BR-S3E3-39 | Chỉ chủ lớp xem; học sinh không có API này. | PO-61 |

- **DoR:** Ready for R3; giả định đã chốt PO-66: QA-S3E3-02, QA-S3E3-05, QA-S3E3-06, QA-S3E3-10, QA-S3E3-11.

### Tiêu chí chấp nhận

**US-S3E3-03-SC1 — Đề đủ bài làm hiện thẻ gộp đúng cấu trúc** · Nguồn: BR-S3E3-30, 31; DQ-22
- Given: Đề 3 của bộ tuần 15 có 7 bài làm (An, Mai, Huy, Bảo, My, Quân, Ngọc Anh); bộ gộp giả lập trả nhận định "Nhiều em xác định sai giao tuyến của (SAB) và (SCD)" với 6 bài làm căn cứ (F-S3-051)
- When: mở Kết quả lớp của bộ tuần 15, nhìn khung "Cả lớp vướng ở đâu"
- Then: có thẻ "Đề 3 · hai mặt phẳng" với đúng câu nhận định trên; nhãn "AI gộp" và "Cần thầy cô xác nhận"; nút "Xem 6 bài làm"; dòng "Cập nhật lúc" có giờ; dòng chú "Chỉ gộp khi đủ 5 bài làm cho một đề"


**US-S3E3-03-SC2 — Biên 5: đúng 5 bài làm thì gộp** · Nguồn: BR-S3E3-30; PO-65 (Q6) · PO-66 (QA-S3E3-02)
- Given: Đề 4 có đúng 5 bài làm: An Xem lại, Huy Đúng, My Đúng, Quân Xem lại, Ngọc Anh Đang làm; bộ gộp giả lập trả một nhận định "Nhiều em quên điều kiện đáy là hình vuông" với 3 bài làm căn cứ (F-S3-051)
- When: mở Kết quả lớp, rồi xem khung của Đề 4
- Then: Đề 4 có thẻ nhận định với đúng câu trên và nút "Xem 3 bài làm"; không hiện MSG-S3-43; bộ gộp giả lập được gọi đúng một lần cho Đề 4


**US-S3E3-03-SC3 — Biên 4: 4 bài làm thì chỉ dữ liệu thô** · Nguồn: BR-S3E3-30, 34; PO-65 (Q6) · PO-66 (QA-S3E3-02)
- Given: như AC SC2 nhưng Ngọc Anh chưa mở Đề 4 (chuyển Chưa làm), nên Đề 4 có đúng 4 bài làm (F-S3-051)
- When: mở Kết quả lớp, rồi xem khung của Đề 4
- Then: không có thẻ nhận định nào cho Đề 4; hiện MSG-S3-43 với m = 4 ("Mới có 4 trên 5 bài làm…"); hiện danh sách gợi ý và câu hỏi thô gom theo bước, không nhãn "AI gộp"; bộ gộp giả lập không bị gọi cho Đề 4


**US-S3E3-03-SC4 — Biên 6: 6 bài làm thì gộp** · Nguồn: BR-S3E3-30 · PO-66 (QA-S3E3-02)
- Given: như AC SC2 nhưng Khánh Linh cũng đã xin một gợi ý ở Đề 4, nên Đề 4 có 6 bài làm (F-S3-051)
- When: mở Kết quả lớp, rồi xem khung của Đề 4
- Then: Đề 4 được gộp; không hiện MSG-S3-43; số bài làm là 6


**US-S3E3-03-SC5 — Mỗi đề xét riêng ngưỡng** · Nguồn: BR-S3E3-30
- Given: Đề 3 có 7 bài làm, Đề 4 có 4 bài làm (Ngọc Anh chưa mở Đề 4) trong cùng bộ tuần 15 (F-S3-051)
- When: mở Kết quả lớp
- Then: chỉ Đề 3 có thẻ nhận định; Đề 4 hiện dữ liệu thô với MSG-S3-43; hai trạng thái cùng hiện trên một màn mà không lẫn nhau


**US-S3E3-03-SC6 — Thẻ sắp theo số bài làm căn cứ giảm dần** · Nguồn: BR-S3E3-32
- Given: bộ gộp giả lập trả hai nhận định: Đề 2 (d(A,SBC)) với 4 căn cứ và Đề 3 với 6 căn cứ (F-S3-051)
- When: mở Kết quả lớp
- Then: thẻ Đề 3 (n = 6) đứng trên thẻ Đề 2 (n = 4); nếu hai thẻ cùng n thì thẻ của đề đứng trước trong bộ đứng trên


**US-S3E3-03-SC7 — "Xem n bài làm" mở đúng n bài làm căn cứ** · Nguồn: BR-S3E3-33; 0.3 ý 6
- Given: thẻ Đề 3 với n = 6 (F-S3-051)
- When: bấm "Xem 6 bài làm", rồi bấm một dòng trong danh sách
- Then: danh sách có đúng 6 dòng, mỗi dòng ghi tên em và mã người dùng; bấm một dòng mở khung bằng chứng của ô tương ứng (US-S3E3-02); không có em nào ngoài 6 căn cứ


**US-S3E3-03-SC8 — Không thẻ nào mang nhãn "Đã kiểm chứng"** · Nguồn: BR-S3E3-33; M-S2-03
- Given: bộ gộp giả lập trả ba nhận định, một nhận định tự gắn nhãn "Đã kiểm chứng" trong dữ liệu trả về (F-S3-051)
- When: gọi API "Cả lớp vướng ở đâu" và mở màn
- Then: không thẻ nào ở giao diện hay thân phản hồi mang nhãn "Đã kiểm chứng"; nhãn của mọi thẻ là "AI gộp" và "Cần thầy cô xác nhận"; nhãn giả bị bỏ


**US-S3E3-03-SC9 — Dưới ngưỡng: dữ liệu thô đủ để dùng** · Nguồn: BR-S3E3-34
- Given: Đề 4 có 4 bài làm; trong đó có 2 câu hỏi và 3 gợi ý ở hai bước khác nhau (F-S3-051)
- When: mở khung dữ liệu thô của Đề 4
- Then: gợi ý và câu hỏi được gom theo bước, mỗi mục ghi bước, loại (gợi ý hay câu hỏi), nội dung nguyên văn và giờ; không có nhận định, không có chữ "n em", không có nhãn "AI gộp"


**US-S3E3-03-SC10 — Lớp dưới 5 học sinh luôn dữ liệu thô** · Nguồn: BR-S3E3-34; BR-S3E3-35
- Given: 11A2 chỉ có 3 em Đang học và cả 3 đã làm Đề 3 của bộ tuần 15 (F-S3-041, F-S3-053)
- When: mở Kết quả lớp của 11A2, bộ tuần 15
- Then: không có thẻ nhận định; hiện MSG-S3-44 "Lớp có ít hơn 5 học sinh…"; hiện dữ liệu thô của Đề 3


**US-S3E3-03-SC11 — Một em làm nhiều lần chỉ tính một bài làm** · Nguồn: BR-S3E3-30
- Given: Đề 4 chỉ có 4 em khác nhau có tương tác; riêng Quân đã gửi 3 lần đáp số và xin 2 gợi ý (tổng 5 tương tác của Quân) (F-S3-051)
- When: mở Kết quả lớp, rồi xem khung của Đề 4
- Then: số bài làm của Đề 4 vẫn là 4 chứ không phải 8; hiện MSG-S3-43 với m = 4; không có nhận định


**US-S3E3-03-SC12 — Dữ liệu gửi bộ gộp là ẩn danh** · Nguồn: BR-S3E3-36; PO-61
- Given: nhà cung cấp AI giả lập ghi lại mọi yêu cầu nhận được; Đề 3 đủ bài làm, một câu hỏi của Chi Mai ghi "Em là Mai, số em 0912345678" (F-S3-051, F-S3-021)
- When: mở Kết quả lớp để kích hoạt gộp Đề 3
- Then: yêu cầu gửi nhà cung cấp không chứa tên hiển thị nào của lớp, không chứa mã `HS-…`, không chứa `HK-4P7Q`, không chứa tên giáo viên; số điện thoại trong câu hỏi thay bằng `[đã ẩn]`; chỉ có mã đề, số bước và nội dung đã che


**US-S3E3-03-SC13 — Làm mới sau bài làm thứ 5 thì xuất hiện nhận định** · Nguồn: BR-S3E3-38
- Given: Đề 4 đang có 4 bài làm với MSG-S3-43; lúc 15:30 Ngọc Anh bắt đầu làm Đề 4 (bài làm thứ 5) (F-S3-051)
- When: cô Lan bấm "Làm mới" lúc 15:31
- Then: MSG-S3-43 biến mất; Đề 4 hiện nhận định với "Cập nhật lúc 15:31"; ô Ngọc Anh × Đề 4 chuyển Đang làm trên bảng


**US-S3E3-03-FC1 — Học sinh Đã rời không tính vào ngưỡng** · Nguồn: BR-S3E3-35 · PO-66 (QA-S3E3-06)
- Given: Đề 4 có 5 bài làm gồm 4 em Đang học và Văn Tú Đã rời (F-S3-028)
- When: mở Kết quả lớp, rồi xem khung của Đề 4
- Then: Đề 4 tính 4 bài làm: hiện MSG-S3-43 và dữ liệu thô; bài làm của Văn Tú vẫn có mặt trong bảng với chip "đã rời" nhưng không có trong n của bất kỳ thẻ nào


**US-S3E3-03-FC2 — Bộ gộp quá thời gian thì lùi về dữ liệu thô** · Nguồn: BR-S3E3-37 · PO-66 (QA-S3E3-11)
- Given: nhà cung cấp AI giả lập không trả lời trong 10 giây; Đề 3 đủ bài làm (F-S3-051)
- When: mở Kết quả lớp, rồi chờ, rồi bấm "Thử lại" khi nhà cung cấp trả lời bình thường
- Then: sau 10 giây khung Đề 3 hiện dữ liệu thô kèm MSG-S3-45 "Chưa gộp được. Thử lại."; bảng lớp × bài vẫn dùng được trong lúc chờ; "Thử lại" hiện thẻ nhận định và xóa MSG-S3-45


**US-S3E3-03-FC3 — Nhận định không có căn cứ bị loại** · Nguồn: BR-S3E3-33
- Given: bộ gộp giả lập trả hai nhận định: một có 3 bài làm căn cứ hợp lệ, một dẫn tới bài làm không tồn tại (F-S3-051)
- When: mở Kết quả lớp
- Then: chỉ nhận định có căn cứ hiện thành thẻ; nhận định dẫn bài làm không tồn tại bị loại, không có thẻ rỗng; n của thẻ giữ đúng 3


**US-S3E3-03-FC4 — Quá 5 thẻ thì gập phần còn lại** · Nguồn: BR-S3E3-32 · PO-66 (QA-S3E3-10)
- Given: bộ gộp giả lập trả 7 nhận định hợp lệ cho một bộ 20 đề
- When: mở Kết quả lớp, rồi bấm "Còn 2 nhận định khác"
- Then: lúc đầu hiện đúng 5 thẻ có n lớn nhất; dòng "Còn 2 nhận định khác" mở thêm 2 thẻ còn lại, vẫn theo thứ tự n giảm dần


**US-S3E3-03-FC5 — Giáo viên khác không gọi được "Cả lớp vướng ở đâu"** · Nguồn: BR-S3E3-39
- Given: thầy Khôi đã đăng nhập (F-S3-002)
- When: gọi API "Cả lớp vướng ở đâu" của 11A1, bộ tuần 15
- Then: máy chủ trả NOT_FOUND; bộ gộp giả lập không bị gọi; thân lỗi không có nhận định hay tên em


**US-S3E3-03-FC6 — Học sinh không gọi được "Cả lớp vướng ở đâu"** · Nguồn: BR-S3E3-39
- Given: Bình An Đang học 11A1 (F-S3-020)
- When: gọi API "Cả lớp vướng ở đâu" của bộ tuần 15
- Then: máy chủ trả FORBIDDEN; bộ gộp giả lập không bị gọi


**US-S3E3-03-FC7 — Ngưỡng là cấu hình, không viết cứng** · Nguồn: BR-S3E3-30; PO-65 (Q6) · PO-66 (QA-S3E3-02)
- Given: `MIN_AGGREGATE` đặt bằng 3 trong môi trường thử; Đề 4 có 3 bài làm
- When: mở Kết quả lớp, rồi đặt lại `MIN_AGGREGATE` bằng 5, rồi mở lại
- Then: với ngưỡng 3, Đề 4 được gộp; với ngưỡng 5, Đề 4 chỉ dữ liệu thô với MSG-S3-43 ghi "5"; chữ "5" trong câu chú và banner lấy từ cấu hình


**US-S3E3-03-FC8 — Chuỗi giống email trong câu hỏi bị che** · Nguồn: BR-S3E3-36 · PO-66 (QA-S3E3-05)
- Given: câu hỏi của một em ghi "gửi lời giải vào mai@example.org nhé"; nhà cung cấp AI giả lập ghi lại yêu cầu (F-S3-051)
- When: kích hoạt gộp Đề 3
- Then: yêu cầu gửi nhà cung cấp chứa `[đã ẩn]` thay cho địa chỉ email; không chứa `mai@example.org`


**US-S3E3-03-FC9 — Dữ liệu đầu vào không đổi thì không gọi lại bộ gộp** · Nguồn: BR-S3E3-38
- Given: nhà cung cấp AI giả lập đếm lượt gọi; Đề 3 đã được gộp một lần
- When: cô Lan bấm "Làm mới" hai lần khi chưa có tương tác mới
- Then: số lượt gọi nhà cung cấp vẫn là 1; nhận định giữ nguyên; "Cập nhật lúc" được cập nhật theo lần làm mới


**US-S3E3-03-FC10 — Thẻ không có phần trăm hay xếp hạng toàn lớp** · Nguồn: BR-S3E3-03; BR-S3E3-31
- Given: bộ gộp giả lập trả nhận định có chứa chữ "75% lớp sai bước này" và "đề khó nhất lớp" (F-S3-051)
- When: gọi API "Cả lớp vướng ở đâu"
- Then: thân phản hồi không chứa phần trăm của lớp hay xếp hạng; câu nhận định vi phạm bị loại hoặc cắt phần đó; số duy nhất trong thẻ là số bài làm căn cứ n


**US-S3E3-03-FC11 — Chưa ai làm thì không có thẻ nhận định** · Nguồn: BR-S3E3-30
- Given: "Bộ bài tuần 16" vừa giao, chưa em nào mở (F-S3-052)
- When: mở Kết quả lớp của bộ đó
- Then: khung "Cả lớp vướng ở đâu" hiện MSG-S3-43 với m = 0 cho mỗi đề hoặc một câu chung "Chưa có bài làm"; không thẻ nhận định, không dữ liệu thô rỗng không giải thích

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: story và AC của S3-E3, ready for R3 | runner-T-261126-14 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | Áp PO-66 (gate R3, T-261126-37): bỏ nhãn giả định chưa chốt, ghi "đã chốt PO-66" | runner-T-261126-37 (BA/QC) |
