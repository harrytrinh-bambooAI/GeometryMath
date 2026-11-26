Phiên bản: v0.1 · 2026-11-26
# S2-E6 — Kho đề: ngăn Của tôi / Kho chung, người soát nội dung, riêng tư và giới hạn tần suất: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Ba story: duyệt kho (C2-01), soát nội dung vào Kho chung, riêng tư của đề Của tôi cộng giới hạn tần suất thêm đề. Khung C2-01 (concept `mvp-01` cần mở rộng, chưa có ảnh riêng).

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S2En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, kèm giá trị cụ thể. `SC` = kịch bản chính, `FC` = lỗi hoặc ngoại lệ, `BV` = giá trị biên. Mã chỉ cho biết **chỗ** một điều được ghi, câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC trong tệp này** (kế thừa MVP, đối chiếu `stories_ac.md` của E1):
- "T0" là giờ máy chủ lúc bắt đầu ca kiểm; mọi mốc giờ tương đối so với T0. Giờ hiển thị `HH:mm, dd/MM/yyyy` giờ Việt Nam; số thập phân hiển thị dấu phẩy.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định theo D-GEO-143 (VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, RATE_LIMITED kèm `Retry-After`).
- Phản hồi chứa lời giải, gợi ý hoặc trạng thái khóa mang `Cache-Control: no-store` (D-GEO-145). "Dữ liệu trả về" là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Thiết bị theo PO-61 và STAGES mục 0.2: giáo viên dùng web (điện thoại chỉ để liếc); học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA, TẠM, PO-65).
- AI trong kiểm thử là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật. Giá trị trong Given / Then lấy từ bảng `FX-nn` hoặc ghi ngay trong tiêu chí.
- AC mang chữ TẠM dựng trên giả định chưa được owner xác nhận; mã `QA-S2En-nn` hoặc `DQ-nn` đứng cạnh cho biết giả định nào.

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-S2En-nn` · `…-SCn` / `-FCn` / `-BVn` | story của epic S2-En · tiêu chí thành công / lỗi / biên thứ n của story | tệp này |
| `BR-S2En-nn` | quy tắc nghiệp vụ BA làm rõ cho epic; đi kèm `BR-S2-nn` của BRD delta khi có | `artifacts/GEO/work/s23/brd-delta-s23.md` |
| `M-S2-nn` | chỉ số chặng 2 (công thức ở BRD delta mục 1) | `brd-delta-s23.md` |
| `PO-nn` | dòng của sổ quyết định gốc | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi design và mặc định đang dùng | `Design/s23/DQ.md` |
| `C2-nn` | mã khung màn hình chặng 2 (C2-01…C2-08) | `STAGES-AFTER-MVP.md` Chặng 2 mục 6 |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (mã lỗi ổn định D-GEO-143, mock AI D-GEO-047, tiêu đề no-store D-GEO-145) | `artifacts/GEO/work/03_decisions.md` (MVP) |
| `QA-S2En-nn` | câu hỏi mở của epic, luôn kèm mặc định TẠM | `questions.md` cùng thư mục |
| `TC-S2En-nnn` | test case của epic | `test_cases.md` cùng thư mục |
| `FX-nn` | dữ liệu mẫu chặng 2 (bảng dưới) | tệp này |
| `BG-n` | bài giao mẫu của MVP (BG-1…BG-5) | `artifacts/GEO/work/fixture.md` (MVP) |
| `BR-GEO-nnn` | quy tắc nghiệp vụ của MVP còn hiệu lực | `artifacts/GEO/work/02_gate2_brd.md` (MVP) |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng của MVP (câu chữ nguyên văn) | `artifacts/GEO/work/05_dictionary.md` (MVP) |
| TẠM | giả định đang dùng, chờ owner xác nhận; ghi theo mẫu `TẠM · tin cậy · nguồn` | PO-65 |
| Nhãn hai trục | trục máy: "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) · trục người: "AI soạn" / "Giáo viên đã duyệt" | PO-02 |
| `release_at` | giờ mở lời giải; máy chủ so giờ máy chủ, không tin giờ thiết bị | MVP, M-GEO-13 |
| Của tôi | ngăn chứa đề do chính giáo viên tạo hoặc sao chép; chỉ chủ sở hữu thấy | STAGES Ch2 mục 7 |
| Kho chung | ngăn chứa đề đã được người soát nội dung duyệt; mọi tài khoản đọc được | STAGES Ch2 mục 7, PO-65 Q9 |
| người soát nội dung | tài khoản có vai soát, do chủ sản phẩm gán; chặng 2 là owner tạm soát | PO-65 Q9 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B1C-8R2T`, cùng lớp 11A1 với An |
| FX-04 | Người soát nội dung "Mai" | mã `GV-2N6P-4W8X`, vai soát nội dung do chủ sản phẩm gán (TẠM, QA-S2E6-01) |
| FX-10 | Đề phẳng PL-01 | Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số 90° |
| FX-11 | Đề phẳng PL-02 | Tam giác ABC cân tại A, góc A = 40°; tính góc B. Đáp số 70° |
| FX-12 | Đề phẳng PL-03 | Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC. Đáp số 10 |
| FX-13 | Đề phẳng PL-04 | Tam giác đều cạnh 4; tính diện tích. Đáp số `4√3` (xấp xỉ 6,93) |
| FX-14 | Đề không gian SP-01 | Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc giữa SC và mặt đáy. Đáp số 45° (tan SCA = SA / AC = 1) |
| FX-20 | Bài luyện BL-1 | 5 câu; câu 2 là FX-14, kiểu đáp số "số đo góc", sai số ±0,5°, 3 lượt thử; giờ mở lời giải `release_at` = T0 + 2 giờ |

## US-S2E6-01 — Kho đề có ngăn Của tôi / Kho chung và lọc loại hình

- **Story:** Là giáo viên, tôi muốn thấy đề của mình và Kho chung ở hai ngăn riêng và lọc theo loại hình, để tìm đề nhanh · Loại UI · Mức P0 · Khung C2-01
- **Nguồn:** STAGES Ch2 mục 6 (C2-01); BR-S2-06
- **Góc nhìn người vận hành:** giáo viên mở Kho đề trên máy tính; có khi chưa có đề riêng nào; lọc ra rỗng; danh sách dài cần phân trang. Học sinh không dùng màn này.
- **Phụ thuộc:** S2-E1, S2-E2 (Problem.kind), S2-E5 (nhãn)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E6-01 | Kho đề có hai ngăn: "Của tôi" (mặc định) và "Kho chung"; mỗi thẻ đề hiện tên, loại hình (Không gian / Phẳng), hai nhãn (PO-02) | STAGES Ch2 mục 6 |
| BR-S2E6-02 | Lọc theo loại hình: Tất cả, Không gian, Phẳng (dựa trên `Problem.kind`); đổi ngăn thì bộ lọc về "Tất cả" (TẠM, QA-S2E6-02) | STAGES Ch2 mục 6 |
| BR-S2E6-03 | Sắp xếp mới nhất lên đầu; phân trang theo con trỏ: trang 20 đề, tối đa 50 đề một yêu cầu, không trả tổng số bản ghi (kế thừa MVP) | MVP, E1 quy ước phân trang |
| BR-S2E6-04 | Ba trạng thái: trống (chưa có đề riêng), đang tải, không có kết quả lọc | STAGES Ch2 mục 6 |

### Tiêu chí chấp nhận

**US-S2E6-01-SC1 — Ngăn Của tôi mặc định hiện đề của chủ, mới nhất ở trên** · Nguồn: BR-S2E6-01
- Given: Cô Trang có 3 đề: FX-10 (thêm lúc 09:00), FX-14 (10:00), FX-13 (11:00)
- When: Mở Kho đề
- Then: Ngăn "Của tôi" đang chọn; thứ tự FX-13, FX-14, FX-10; mỗi thẻ có tên, loại hình và hai nhãn

**US-S2E6-01-SC2 — Ngăn Kho chung chỉ có đề đã soát** · Nguồn: BR-S2E6-01
- Given: Kho chung có 2 đề đã soát và có 1 đề chờ soát
- When: Bấm ngăn "Kho chung"
- Then: Hiện đúng 2 đề đã soát; đề chờ soát không có

**US-S2E6-01-SC3 — Lọc Hình phẳng chỉ giữ đề phẳng** · Nguồn: BR-S2E6-02
- Given: Của tôi có FX-10, FX-13 (phẳng) và FX-14 (không gian)
- When: Chọn lọc "Phẳng"
- Then: Chỉ FX-13 và FX-10; FX-14 ẩn; không gọi lại toàn trang (danh sách đổi nhanh)

**US-S2E6-01-SC4 — Đổi ngăn đưa bộ lọc về Tất cả** · Nguồn: BR-S2E6-02; QA-S2E6-02
- Given: Đang lọc "Phẳng" ở "Của tôi"
- When: Bấm sang "Kho chung"
- Then: Bộ lọc hiện "Tất cả"; danh sách Kho chung đủ loại

**US-S2E6-01-FC1 — Chưa có đề riêng hiện trạng thái trống** · Nguồn: BR-S2E6-04
- Given: Giáo viên mới không có đề
- When: Mở "Của tôi"
- Then: Hiện "Chưa có đề riêng" kèm nút "Thêm đề"; không hiện bảng rỗng trơ

**US-S2E6-01-FC2 — Lọc không có kết quả** · Nguồn: BR-S2E6-04
- Given: Của tôi chỉ có đề phẳng
- When: Chọn lọc "Không gian"
- Then: Hiện "Không có đề phù hợp" kèm nút "Xóa bộ lọc"; bấm nút trở về danh sách đủ

**US-S2E6-01-FC3 — Trạng thái đang tải** · Nguồn: BR-S2E6-04
- Given: Mạng chậm
- When: Mở Kho đề
- Then: Hiện khung giữ chỗ (không hiện "trống" khi đang tải); sau khi có dữ liệu thay bằng danh sách

**US-S2E6-01-BV1 — Đúng 20 đề không có trang hai** · Nguồn: BR-S2E6-03
- Given: Của tôi có đúng 20 đề
- When: Mở Kho đề; cuộn hết
- Then: Hiện 20 đề; không có nút "Xem thêm"; không có con trỏ trang kế

**US-S2E6-01-BV2 — 21 đề có trang hai với 1 đề** · Nguồn: BR-S2E6-03
- Given: Của tôi có 21 đề
- When: Mở Kho đề; bấm "Xem thêm"
- Then: Trang 1 có 20 đề, có nút "Xem thêm"; trang 2 có đúng 1 đề; không trùng đề giữa hai trang

**US-S2E6-01-BV3 — Yêu cầu 50 đề nhận, 51 bị từ chối** · Nguồn: BR-S2E6-03
- Given: Của tôi có 60 đề
- When: Gọi danh sách với `limit` 50, rồi `limit` 51
- Then: Lần 1: trả 50 đề; lần 2: VALIDATION_FAILED

**US-S2E6-01-FC4 — Con trỏ của ngăn khác không dùng được** · Nguồn: MVP, MSG-GEO-05
- Given: Con trỏ lấy từ ngăn "Của tôi"
- When: Dùng con trỏ đó để xin trang của ngăn "Kho chung"
- Then: INVALID_CURSOR; hiện "Danh sách đã thay đổi. Vui lòng tải lại từ đầu."

**US-S2E6-01-FC5 — Học sinh không vào được Kho đề** · Nguồn: PO-61; STAGES mục 0.2 nguyên tắc 6
- Given: Học sinh An (FX-02)
- When: Gọi danh sách "Của tôi" và "Kho chung" của Kho đề giáo viên
- Then: FORBIDDEN; không dữ liệu đề nào trả về

- **Mở và quyết định đang dùng:** QA-S2E6-02
- **DoR:** ready for R3, mức P0.

## US-S2E6-02 — Người soát nội dung duyệt đề vào Kho chung

- **Story:** Là người soát nội dung, tôi muốn xem hàng đợi đề chờ soát và duyệt hoặc từ chối, để Kho chung chỉ có đề đáng tin · Loại UI · Mức P0 · Khung C2-01
- **Nguồn:** PO-65 (Q9, TẠM · vừa); STAGES Ch2 mục 7
- **Góc nhìn người vận hành:** chặng 2 owner tạm soát (PO-65 Q9) nên số đề ít. Ca đáng kể: gửi đề chưa duyệt; gửi hai lần; người soát tự duyệt đề của mình; từ chối không có lý do.
- **Phụ thuộc:** US-S2E6-01, S2-E5

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E6-05 | Giáo viên bấm "Đề xuất vào Kho chung" cho đề có trục người "Giáo viên đã duyệt"; đề chưa duyệt không gửi được | STAGES Ch2 mục 7 |
| BR-S2E6-06 | Trạng thái đề kho: Của tôi, Chờ soát, Đã vào Kho chung, Bị từ chối (kèm lý do); gửi lại khi đang Chờ soát chỉ giữ một yêu cầu | STAGES Ch2 mục 7 |
| BR-S2E6-07 | Chỉ tài khoản có vai người soát thấy hàng đợi và duyệt hoặc từ chối; vai này do chủ sản phẩm gán (TẠM, QA-S2E6-01); người soát không soát đề của chính mình | PO-65 Q9 |
| BR-S2E6-08 | Từ chối cần lý do từ 5 đến 500 ký tự (TẠM, QA-S2E6-03) | BR-S2-06 |
| BR-S2E6-09 | Mỗi giáo viên có tối đa 10 đề đang Chờ soát (TẠM, QA-S2E6-04) | PO-63 |
| BR-S2E6-10 | Sau khi vào Kho chung, đề trong Kho chung là bản độc lập; giáo viên sửa bản Của tôi không đổi bản Kho chung (TẠM, QA-S2E6-05) | STAGES Ch2 mục 7 |

### Tiêu chí chấp nhận

**US-S2E6-02-SC1 — Giáo viên gửi đề đã duyệt vào hàng đợi** · Nguồn: BR-S2E6-05
- Given: Đề FX-14 của Cô Trang có trục người "Giáo viên đã duyệt"
- When: Bấm "Đề xuất vào Kho chung"
- Then: Đề chuyển "Chờ soát"; xuất hiện trong hàng đợi của người soát; vẫn ở "Của tôi" và chưa có trong Kho chung

**US-S2E6-02-SC2 — Người soát duyệt thì đề vào Kho chung** · Nguồn: BR-S2E6-07
- Given: Đề FX-14 Chờ soát; Mai đăng nhập
- When: Mở hàng đợi; bấm "Duyệt vào Kho chung"
- Then: Đề thành "Đã vào Kho chung"; xuất hiện trong ngăn "Kho chung" với nhãn của nó; Cô Trang thấy trạng thái đã vào

**US-S2E6-02-SC3 — Người soát từ chối có lý do** · Nguồn: BR-S2E6-08
- Given: Đề Chờ soát
- When: Bấm "Từ chối"; gõ lý do "Đáp số chưa khớp phần b"; xác nhận
- Then: Đề thành "Bị từ chối" kèm lý do; Cô Trang thấy lý do; đề không vào Kho chung

**US-S2E6-02-FC1 — Đề chưa duyệt không gửi được** · Nguồn: BR-S2E6-05
- Given: Đề có trục người "AI soạn"
- When: Bấm "Đề xuất vào Kho chung"; gọi API gửi
- Then: Nút khóa kèm "Hãy duyệt lời giải trước"; API: CONFLICT; không vào hàng đợi

**US-S2E6-02-FC2 — Gửi hai lần chỉ giữ một yêu cầu** · Nguồn: BR-S2E6-06
- Given: Đề đã gửi, đang Chờ soát
- When: Bấm "Đề xuất vào Kho chung" lần hai
- Then: Không tạo yêu cầu thứ hai; hàng đợi vẫn một dòng cho đề này

**US-S2E6-02-FC3 — Người không phải người soát không thấy hàng đợi** · Nguồn: BR-S2E6-07
- Given: Cô Trang (giáo viên thường)
- When: Gọi hàng đợi chờ soát; gọi duyệt
- Then: FORBIDDEN; không dữ liệu hàng đợi

**US-S2E6-02-FC4 — Người soát không soát đề của chính mình** · Nguồn: BR-S2E6-07
- Given: Mai là chủ đề FX-13 và đã gửi soát
- When: Mai thử duyệt đề FX-13
- Then: FORBIDDEN với thông báo "Bạn không thể soát đề của chính mình"; đề vẫn Chờ soát

**US-S2E6-02-BV1 — Lý do từ chối 4 ký tự bị chặn, 5 ký tự nhận** · Nguồn: BR-S2E6-08; QA-S2E6-03
- Given: Đề Chờ soát
- When: Từ chối với lý do 4 ký tự; từ chối với lý do 5 ký tự
- Then: 4 ký tự: lỗi "Lý do từ 5 đến 500 ký tự", đề vẫn Chờ soát; 5 ký tự: từ chối thành công

**US-S2E6-02-BV2 — Lý do 500 ký tự nhận, 501 bị chặn** · Nguồn: BR-S2E6-08; QA-S2E6-03
- Given: Hai đề Chờ soát
- When: Từ chối đề 1 với lý do 500 ký tự; đề 2 với lý do 501 ký tự
- Then: Đề 1: từ chối thành công; đề 2: VALIDATION_FAILED

**US-S2E6-02-BV3 — Đề thứ 10 Chờ soát nhận, đề thứ 11 bị chặn** · Nguồn: BR-S2E6-09; QA-S2E6-04
- Given: Cô Trang có 9 đề đã duyệt đang Chờ soát
- When: Gửi đề thứ 10; gửi đề thứ 11
- Then: Đề 10: vào hàng đợi; đề 11: lỗi "Tối đa 10 đề chờ soát cùng lúc"; CONFLICT; không vào hàng đợi

**US-S2E6-02-SC4 — Sửa bản Của tôi không đổi bản Kho chung** · Nguồn: BR-S2E6-10; QA-S2E6-05
- Given: Đề FX-14 đã vào Kho chung
- When: Cô Trang sửa lời giải bản Của tôi và lưu
- Then: Bản trong Kho chung giữ nguyên nội dung và nhãn; người khác đọc thấy bản cũ

**US-S2E6-02-FC5 — Đề bị từ chối gửi lại được sau khi sửa** · Nguồn: BR-S2E6-06
- Given: Đề "Bị từ chối"
- When: Sửa đề rồi bấm "Đề xuất vào Kho chung"
- Then: Đề thành "Chờ soát" lại; lịch sử giữ dòng từ chối cũ

- **Mở và quyết định đang dùng:** QA-S2E6-01, QA-S2E6-03, QA-S2E6-04, QA-S2E6-05
- **DoR:** ready for R3, mức P0.

## US-S2E6-03 — Đề Của tôi chỉ chủ sở hữu thấy; giới hạn tần suất thêm đề

- **Story:** Là giáo viên, tôi muốn đề Của tôi chỉ mình thấy và hệ thống chặn việc thêm đề dồn dập, để đề riêng an toàn và Kho không bị nhồi · Loại Xuyên suốt · Mức P0 · Khung (không có khung)
- **Nguồn:** STAGES Ch2 mục 7; PO-63; BR-S2-06
- **Góc nhìn người vận hành:** không còn xác minh giáo viên nên ai cũng đăng ký vai giáo viên (PO-63): hai tuyến phòng thủ là riêng tư của Của tôi và giới hạn tần suất. Ca đáng kể: đoán mã đề của người khác; thêm đề bằng nhiều đường để lách bộ đếm.
- **Phụ thuộc:** S2-E3, S2-E4, US-S2E6-01

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E6-11 | Đề Của tôi chỉ chủ sở hữu thấy trên mọi đường: danh sách, chi tiết, tìm, đối chiếu trùng, xuất, hộp duyệt; người soát chỉ thấy đề đang Chờ soát | STAGES Ch2 mục 7 |
| BR-S2E6-12 | Học sinh của giáo viên chỉ thấy đề qua bài giao, không thấy ngăn Của tôi của giáo viên | STAGES mục 0.2 nguyên tắc 5 |
| BR-S2E6-13 | Giới hạn thêm đề: tối đa 20 đề trong 60 phút trượt mỗi tài khoản, tính chung mọi đường tạo đề (dán chữ, ảnh, import, sao chép từ Kho chung) (TẠM, QA-S2E3-02) | PO-63 |
| BR-S2E6-14 | Lời giải đề Kho chung: tài khoản vai Giáo viên xem được; tài khoản vai Học sinh không xem lời giải đề Kho chung ngoài bài giao đã mở (TẠM, QA-S2E6-06) | PO-63, STAGES mục 0.2 nguyên tắc 1 |

### Tiêu chí chấp nhận

**US-S2E6-03-SC1 — Chủ sở hữu thấy đề Của tôi** · Nguồn: BR-S2E6-11
- Given: Đề FX-10 trong Của tôi của Cô Trang
- When: Cô Trang gọi chi tiết đề
- Then: Trả đủ nội dung đề

**US-S2E6-03-FC1 — Giáo viên khác không lấy được chi tiết đề Của tôi** · Nguồn: BR-S2E6-11
- Given: Đề FX-10 của Cô Trang; Cô Lan đăng nhập
- When: Cô Lan gọi chi tiết bằng mã đề của Cô Trang
- Then: NOT_FOUND; không phân biệt "không có" và "của người khác"; không dữ liệu đề

**US-S2E6-03-FC2 — Đề Của tôi không lộ qua danh sách, tìm và đối chiếu** · Nguồn: BR-S2E6-11
- Given: Cô Trang có đề riêng FX-10; Cô Lan đăng nhập
- When: Cô Lan xem danh sách Kho đề, tìm "5, 12, 13", import đề giống FX-10
- Then: Không đường nào hiện đề của Cô Trang

**US-S2E6-03-FC3 — Học sinh không thấy ngăn Của tôi của giáo viên** · Nguồn: BR-S2E6-12
- Given: An thuộc lớp 11A1 của Cô Trang
- When: An gọi danh sách đề Của tôi của Cô Trang
- Then: FORBIDDEN; An chỉ thấy đề qua bài giao của lớp

**US-S2E6-03-FC4 — Người soát chỉ thấy đề đang Chờ soát** · Nguồn: BR-S2E6-11
- Given: Mai là người soát; Cô Trang có 1 đề Chờ soát và 2 đề Của tôi bình thường
- When: Mai gọi chi tiết cho từng đề
- Then: Đề Chờ soát: thấy; hai đề còn lại: NOT_FOUND

**US-S2E6-03-BV1 — Đề thứ 20 trong giờ nhận, đề thứ 21 bị chặn** · Nguồn: BR-S2E6-13; PO-63
- Given: Cô Trang chưa thêm đề nào trong 60 phút qua
- When: Thêm lần lượt 21 đề (nhập chữ hợp lệ)
- Then: 20 đề đầu tạo được; đề 21: RATE_LIMITED kèm `Retry-After`; không đề nào được tạo thêm

**US-S2E6-03-BV2 — Cửa sổ trượt: đề đầu hết hạn sau 60 phút thì thêm được** · Nguồn: BR-S2E6-13
- Given: Cô Trang đã thêm đủ 20 đề, đề đầu lúc T0; đồng hồ giả
- When: Thêm đề lúc T0 + 59:59; thêm đề lúc T0 + 60:00
- Then: T0 + 59:59: RATE_LIMITED; T0 + 60:00: tạo được

**US-S2E6-03-FC5 — Bộ đếm tính chung mọi đường tạo đề** · Nguồn: BR-S2E6-13
- Given: Cô Trang đã thêm 10 đề bằng dán chữ, 5 bằng ảnh, 5 bằng import
- When: Thêm đề thứ 21 bằng sao chép từ Kho chung
- Then: RATE_LIMITED; không đường nào lách được bộ đếm

**US-S2E6-03-FC6 — Bộ đếm theo tài khoản, không theo địa chỉ mạng** · Nguồn: BR-S2E6-13
- Given: Cô Trang đủ 20 đề; Cô Lan cùng địa chỉ mạng
- When: Cô Lan thêm một đề
- Then: Cô Lan thêm được; bộ đếm của Cô Trang không bị ảnh hưởng

**US-S2E6-03-SC2 — Giáo viên xem được lời giải đề Kho chung** · Nguồn: BR-S2E6-14; PO-63
- Given: Đề Kho chung có lời giải đã duyệt; Cô Lan vai Giáo viên
- When: Cô Lan mở lời giải
- Then: Xem được kèm nhãn hai trục; không cần xác minh giáo viên

**US-S2E6-03-FC7 — Học sinh không xem lời giải đề Kho chung ngoài bài giao** · Nguồn: BR-S2E6-14; QA-S2E6-06
- Given: Học sinh An; đề Kho chung không nằm trong bài giao nào của An
- When: An gọi lời giải đề đó
- Then: Khóa: không bước giải, không đáp số (kiểm dữ liệu trả về); thông báo "Lời giải mở theo bài giao của thầy cô"

**US-S2E6-03-FC8 — Bộ câu thử "giả làm giáo viên" không lấy được gì ngoài quyền giáo viên** · Nguồn: PO-63
- Given: Tài khoản vừa đăng ký vai Giáo viên (không lớp, không xác minh)
- When: Thử xem kết quả học sinh của lớp khác, đề Của tôi của người khác, đặt giờ mở bài giao của lớp khác
- Then: Mọi thử đều FORBIDDEN hoặc NOT_FOUND; chỉ có quyền đọc Kho chung và tạo đề của chính mình

- **Mở và quyết định đang dùng:** QA-S2E6-06, QA-S2E3-02
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 3; tiêu chí: 36. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 3 story, 36 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
