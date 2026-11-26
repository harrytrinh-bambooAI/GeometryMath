Phiên bản: v0.2 · 2026-11-26
# S2-E1 — Đăng ký, đăng nhập bằng mã và khôi phục (định danh tối giản PO-61): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Bốn story: đăng ký, đăng nhập, giáo viên tự khôi phục, chuyển dữ liệu từ MVP. Khung LOGIN (ảnh `c3-01-*.png`, DQ-15…DQ-18).

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
| `RC-xxxx-xxxx-xxxx` | mã khôi phục của giáo viên, hiện một lần lúc đăng ký (TẠM) | PO-61, DQ-16 |
| `GV-xxxx-xxxx` · `HS-xxxx-xxxx` | mã người dùng sinh tự động, tám ký tự sau tiền tố vai (TẠM) | PO-61, DQ-16 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B7C-8R2T`, cùng lớp 11A1 với An |
| FX-04 | Người soát nội dung "Mai" | mã `GV-2N6P-4W8X`, vai soát nội dung do chủ sản phẩm gán (TẠM, QA-S2E6-01) |
| FX-10 | Đề phẳng PL-01 | Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số 90° |
| FX-11 | Đề phẳng PL-02 | Tam giác ABC cân tại A, góc A = 40°; tính góc B. Đáp số 70° |
| FX-12 | Đề phẳng PL-03 | Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC. Đáp số 10 |
| FX-13 | Đề phẳng PL-04 | Tam giác đều cạnh 4; tính diện tích. Đáp số `4√3` (xấp xỉ 6,93) |
| FX-14 | Đề không gian SP-01 | Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc giữa SC và mặt đáy. Đáp số 45° (tan SCA = SA / AC = 1) |
| FX-20 | Bài luyện BL-1 | 5 câu; câu 2 là FX-14, kiểu đáp số "số đo góc", sai số ±0,5°, 3 lượt thử; giờ mở lời giải `release_at` = T0 + 2 giờ |

## US-S2E1-01 — Đăng ký bằng tên hiển thị và vai, nhận mã người dùng và mã khôi phục

- **Story:** Là giáo viên hoặc học sinh, tôi muốn tự đăng ký chỉ bằng tên hiển thị và vai, để dùng ứng dụng mà không phải đưa email, số điện thoại hay ngày sinh · Loại UI · Mức P0 · Khung LOGIN
- **Nguồn:** PO-61, PO-62; DQ-15, DQ-16; BR-S2-01
- **Góc nhìn người vận hành:** học sinh ngồi trước iPad ở đầu buổi, giáo viên ngồi máy tính ở nhà. Ca đáng kể: quên chép mã sau khi đăng ký; hai bạn cùng tên "An"; gõ tên có khoảng trắng thừa; bấm Đăng ký liên tiếp. Mã khôi phục có cho cả giáo viên và học sinh, kể cả học sinh Tự học hoặc Chờ duyệt (PO-66 G3).
- **Phụ thuộc:** PO-63 (giới hạn tần suất tạo tài khoản); văn bản thông báo riêng tư bản 1.0 lấy từ US-S3E5-02 (S3-E5): tick "đã đọc" ở S2-E1 hiện đúng văn bản đó và lưu cờ (PO-66 G5, QA-S2E1-07); nút "Xóa tài khoản" và chính sách lưu/xóa ở US-S3E5-05 (QA-S2E1-09)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E1-01 | Đăng ký chỉ thu ba trường: tên hiển thị, vai (Giáo viên / Học sinh), mật khẩu; cộng cờ "đã đọc thông báo riêng tư" kèm số phiên bản văn bản. Không thu email, số điện thoại, ngày sinh, trường, ảnh | PO-61, PO-64, DQ-15 |
| BR-S2E1-02 | Tên hiển thị từ 1 đến 60 ký tự sau khi bỏ khoảng trắng đầu và cuối; trùng tên được phép, mã người dùng phân biệt | MVP (BR-GEO-102), DQ-25 |
| BR-S2E1-03 | Mật khẩu từ 8 đến 64 ký tự, người dùng tự đặt (TẠM, QA-S2E1-02) | PO-61 |
| BR-S2E1-04 | Mã người dùng duy nhất toàn hệ thống, dạng `GV-` hoặc `HS-` cộng 8 ký tự chữ hoa và số, bỏ ký tự dễ nhầm O, 0, I, 1 (TẠM, DQ-16) | PO-61, DQ-16 |
| BR-S2E1-05 | Mã khôi phục `RC-xxxx-xxxx-xxxx` sinh cho mọi tài khoản mới (giáo viên và học sinh), hiện đúng một lần, buộc tick "đã lưu" mới đi tiếp | PO-61, DQ-16, DQ-17; PO-66 (G3) |
| BR-S2E1-06 | Tối đa 5 lần đăng ký mỗi giờ mỗi địa chỉ mạng, quá thì RATE_LIMITED; bộ đếm theo địa chỉ mạng chỉ giữ tạm tối đa 1 giờ, không lưu thành bảng (BR-S3E5-37) (TẠM, QA-S2E1-03) | PO-63 |
| BR-S2E1-16 | Tên hiển thị không được trông như email hoặc số điện thoại; luật chi tiết ở BR-S3E5-35 (US-S3E5-03) và phải chạy cùng đợt với đăng ký của chặng 2 (TẠM, RV_s23 M-06) | PO-61; BR-S3E5-35 |

### Tiêu chí chấp nhận

**US-S2E1-01-SC1 — Giáo viên đăng ký và nhận hai mã** · Nguồn: PO-61; DQ-16
- Given: Màn "Đăng ký" ở web; chưa có tài khoản tên "Cô Trang" (FX-01)
- When: Chọn vai "Giáo viên"; gõ tên "Cô Trang", mật khẩu `Trang@2026x`; tick "đã đọc thông báo riêng tư (bản 1.0)"; bấm "Đăng ký"
- Then: Màn mã hiện mã người dùng dạng `GV-xxxx-xxxx` (8 ký tự hợp lệ); hiện mã khôi phục dạng `RC-xxxx-xxxx-xxxx`; có nút "Sao chép" và "In / lưu ảnh"; có dòng cảnh báo mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được

**US-S2E1-01-SC2 — Học sinh đăng ký nhận mã người dùng và mã khôi phục** · Nguồn: PO-61; PO-66 (G3)
- Given: Màn "Đăng ký" ở iPad; chưa có tài khoản tên "An"
- When: Chọn vai "Học sinh"; gõ tên "An", mật khẩu `AnHoc#2026`; tick đã đọc thông báo; bấm "Đăng ký"
- Then: Màn mã hiện mã dạng `HS-xxxx-xxxx` và mã khôi phục dạng `RC-xxxx-xxxx-xxxx`; có dòng "Quên mật khẩu thì dùng mã khôi phục này; nếu đã vào lớp, thầy cô cũng đặt lại được"

**US-S2E1-01-SC3 — Nút Tiếp tục chỉ mở khi đã tick "Tôi đã lưu mã"** · Nguồn: DQ-16
- Given: Màn mã đang hiện (giáo viên)
- When: Bấm "Tiếp tục" khi chưa tick; tick "Tôi đã lưu mã" rồi bấm "Tiếp tục"
- Then: Lần 1: nút bị khóa, không chuyển màn; lần 2: chuyển vào Kho đề đã đăng nhập

**US-S2E1-01-SC4 — Mã khôi phục không hiện lại lần hai** · Nguồn: DQ-16; PO-61
- Given: Giáo viên vừa qua màn mã
- When: Đăng xuất; đăng nhập lại; mở mọi màn tài khoản
- Then: Không màn nào hiện lại mã khôi phục; dữ liệu trả về của mọi yêu cầu sau đăng ký không chứa mã khôi phục (chỉ chứa dấu hiệu "đã có")

**US-S2E1-01-SC5 — Hai người cùng tên có hai mã khác nhau** · Nguồn: DQ-25; BR-S2E1-02
- Given: Đã có học sinh "An" mã `HS-3F8A-2L6P`
- When: Đăng ký thêm học sinh tên "An"
- Then: Tạo thành công, mã mới khác `HS-3F8A-2L6P`; hai tài khoản tồn tại độc lập

**US-S2E1-01-SC6 — Yêu cầu đăng ký chỉ chứa các trường cho phép** · Nguồn: PO-61; PO-64
- Given: Màn "Đăng ký" mở
- When: Đăng ký thành công, bắt yêu cầu gửi lên máy chủ
- Then: Thân yêu cầu chỉ có tên hiển thị, vai, mật khẩu, cờ đã đọc thông báo và số phiên bản văn bản; không có email, số điện thoại, ngày sinh, trường, ảnh

**US-S2E1-01-FC1 — Tên rỗng hoặc chỉ khoảng trắng bị chặn** · Nguồn: BR-S2E1-02
- Given: Màn "Đăng ký"
- When: Để trống tên (hoặc gõ 3 dấu cách); điền phần còn lại; bấm "Đăng ký"
- Then: Hiện lỗi "Hãy nhập tên hiển thị." cạnh ô tên; không tạo tài khoản; mã lỗi VALIDATION_FAILED nếu gọi thẳng

**US-S2E1-01-BV1 — Tên đúng 60 ký tự được nhận** · Nguồn: BR-S2E1-02
- Given: Màn "Đăng ký"
- When: Gõ tên gồm 60 chữ "A"; điền phần còn lại; bấm "Đăng ký"
- Then: Tạo tài khoản thành công; tên hiển thị lưu đủ 60 ký tự

**US-S2E1-01-BV2 — Tên 61 ký tự bị chặn** · Nguồn: BR-S2E1-02
- Given: Màn "Đăng ký"
- When: Gõ tên gồm 61 chữ "A"; bấm "Đăng ký"
- Then: Hiện lỗi "Tên hiển thị tối đa 60 ký tự."; không tạo tài khoản

**US-S2E1-01-BV3 — Tên 1 ký tự và khoảng trắng đầu cuối được cắt** · Nguồn: BR-S2E1-02
- Given: Màn "Đăng ký"
- When: Gõ tên "  B  " (một chữ B giữa hai dấu cách); bấm "Đăng ký"
- Then: Tạo thành công; tên lưu là "B"

**US-S2E1-01-BV4 — Mật khẩu 8 ký tự được nhận** · Nguồn: BR-S2E1-03; QA-S2E1-02
- Given: Màn "Đăng ký"
- When: Đặt mật khẩu 8 ký tự `Abcd1234`; bấm "Đăng ký"
- Then: Tạo tài khoản thành công

**US-S2E1-01-BV5 — Mật khẩu 7 ký tự bị chặn** · Nguồn: BR-S2E1-03; QA-S2E1-02
- Given: Màn "Đăng ký"
- When: Đặt mật khẩu 7 ký tự `Abcd123`; bấm "Đăng ký"
- Then: Hiện lỗi "Mật khẩu từ 8 đến 64 ký tự."; không tạo tài khoản

**US-S2E1-01-BV6 — Mật khẩu 64 nhận, 65 bị chặn** · Nguồn: BR-S2E1-03; QA-S2E1-02
- Given: Gọi đăng ký qua API hai lần
- When: Lần 1: mật khẩu dài 64 ký tự; lần 2: mật khẩu dài 65 ký tự
- Then: Lần 1 thành công; lần 2: VALIDATION_FAILED, không tạo tài khoản

**US-S2E1-01-FC2 — Chưa tick thông báo riêng tư thì không đăng ký được** · Nguồn: PO-64; DQ-15
- Given: Màn "Đăng ký", điền đủ tên, vai, mật khẩu
- When: Bấm "Đăng ký" khi chưa tick; gọi API với cờ đã đọc = sai
- Then: Nút khóa trên giao diện; API trả VALIDATION_FAILED; không tạo tài khoản

**US-S2E1-01-FC3 — Vai không hợp lệ bị từ chối** · Nguồn: PO-61; BR-S2-01
- Given: Gọi API đăng ký với vai "admin" (và vai rỗng)
- When: Gửi hai yêu cầu
- Then: Cả hai trả VALIDATION_FAILED; không có tài khoản nào mới

**US-S2E1-01-BV7 — Lần đăng ký thứ 5 trong giờ nhận, lần thứ 6 bị giới hạn** · Nguồn: PO-63; BR-S2E1-06; QA-S2E1-03
- Given: Một địa chỉ mạng chưa đăng ký lần nào trong giờ này
- When: Đăng ký liên tiếp 6 tài khoản khác tên; chờ theo `Retry-After`; đăng ký lần nữa
- Then: Năm lần đầu thành công; lần 6: RATE_LIMITED kèm `Retry-After`; sau thời gian đó đăng ký được

**US-S2E1-01-FC4 — Mã người dùng không bao giờ trùng** · Nguồn: BR-S2E1-04; DQ-16
- Given: Cơ sở dữ liệu thử rỗng
- When: Đăng ký 1000 tài khoản liên tiếp (vai trộn)
- Then: 1000 mã khác nhau; mọi mã khớp `GV-` hoặc `HS-` + 8 ký tự hợp lệ; không có O, 0, I, 1

**US-S2E1-01-FC5 — Bấm Đăng ký liên tiếp chỉ tạo một tài khoản** · Nguồn: DQ-15
- Given: Màn "Đăng ký" điền đủ hợp lệ
- When: Bấm "Đăng ký" hai lần trong 1 giây
- Then: Chỉ một tài khoản được tạo; màn mã hiện một lần

- **Mở và quyết định đang dùng:** QA-S2E1-02, QA-S2E1-03, QA-S2E1-07 (mặc định TẠM đang dùng)
- **DoR:** ready for R3, mức P0.

## US-S2E1-02 — Đăng nhập bằng mã và mật khẩu; khóa 10 lần sai trong 15 phút theo tài khoản

- **Story:** Là người dùng đã đăng ký, tôi muốn đăng nhập bằng mã người dùng và mật khẩu, để vào đúng dữ liệu của mình · Loại UI · Mức P0 · Khung LOGIN
- **Nguồn:** PO-61, PO-53; DQ-18
- **Góc nhìn người vận hành:** học sinh gõ mã bằng ngón tay trên iPad nên hay nhầm chữ hoa thường và khoảng trắng; kẻ xấu dò mật khẩu của một mã. Thông báo lỗi không được tiết lộ mã có tồn tại hay không.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E1-07 | Đăng nhập chỉ có hai ô: mã người dùng và mật khẩu; mã không phân biệt hoa thường và được cắt khoảng trắng đầu cuối, mật khẩu có phân biệt hoa thường (TẠM, QA-S2E1-08) | PO-61, DQ-18 |
| BR-S2E1-08 | Mọi lỗi đăng nhập hiện cùng một câu "Mã hoặc mật khẩu chưa đúng", không nói sai ô nào và không lộ mã có tồn tại | DQ-18 |
| BR-S2E1-09 | Khóa theo tài khoản: 10 lần sai liên tiếp thì khóa 15 phút tính từ lần sai thứ 10; đăng nhập đúng trước đó đưa bộ đếm về 0; khóa của tài khoản này không ảnh hưởng tài khoản khác cùng địa chỉ mạng | PO-53, BR-S2-01 |

### Tiêu chí chấp nhận

**US-S2E1-02-SC1 — Học sinh đăng nhập đúng** · Nguồn: PO-61
- Given: Học sinh An (FX-02) ở màn "Đăng nhập" trên iPad
- When: Gõ mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026`; bấm "Đăng nhập"
- Then: Mở màn "Bài của em" với dòng "Chào An"

**US-S2E1-02-SC2 — Giáo viên đăng nhập đúng** · Nguồn: PO-61
- Given: Cô Trang (FX-01) ở màn "Đăng nhập" trên web
- When: Gõ mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x`; bấm "Đăng nhập"
- Then: Mở Kho đề với ngăn "Của tôi"

**US-S2E1-02-SC3 — Mã gõ chữ thường và có dấu cách vẫn vào được** · Nguồn: BR-S2E1-07; QA-S2E1-08
- Given: Học sinh An (FX-02)
- When: Gõ mã " hs-3f8a-2l6p " (chữ thường, dấu cách hai đầu) và đúng mật khẩu; bấm "Đăng nhập"
- Then: Vào đúng tài khoản An

**US-S2E1-02-FC1 — Sai mật khẩu hiện câu chung** · Nguồn: DQ-18
- Given: Học sinh An (FX-02)
- When: Gõ mã đúng, mật khẩu sai; bấm "Đăng nhập"
- Then: Hiện "Mã hoặc mật khẩu chưa đúng"; không vào được; không chỉ ra ô nào sai

**US-S2E1-02-FC2 — Mã không tồn tại trả đúng cùng một phản hồi** · Nguồn: DQ-18; BR-S2E1-08
- Given: Mã `HS-ZZZZ-ZZZZ` chưa đăng ký; học sinh An có mật khẩu sai
- When: Đăng nhập với mã không tồn tại; đăng nhập với mã An và mật khẩu sai
- Then: Hai phản hồi cùng mã lỗi, cùng câu chữ, cùng cấu trúc; không phân biệt được mã nào có thật

**US-S2E1-02-BV1 — Sai 9 lần chưa bị khóa** · Nguồn: PO-53; BR-S2E1-09
- Given: Tài khoản An chưa sai lần nào
- When: Đăng nhập sai mật khẩu 9 lần liên tiếp; lần thứ 10 nhập mật khẩu đúng
- Then: Chín lần đầu hiện câu chung; lần thứ 10 (đúng) vào được; bộ đếm về 0

**US-S2E1-02-BV2 — Sai lần thứ 10 thì khóa** · Nguồn: PO-53; BR-S2E1-09
- Given: Tài khoản An chưa sai lần nào
- When: Đăng nhập sai 10 lần liên tiếp; lần 11 nhập mật khẩu đúng
- Then: Sau lần sai thứ 10 hiện "Tài khoản tạm khóa, thử lại sau 15 phút"; lần 11 dù đúng mật khẩu vẫn bị từ chối trong lúc khóa; mã lỗi RATE_LIMITED kèm `Retry-After`

**US-S2E1-02-BV3 — Hết khóa đúng ở mốc 15 phút** · Nguồn: PO-53; BR-S2E1-09
- Given: Tài khoản An vừa bị khóa lúc T0 (sau lần sai thứ 10)
- When: Ở T0 + 14:59 đăng nhập đúng mật khẩu; ở T0 + 15:00 đăng nhập đúng mật khẩu
- Then: T0 + 14:59: vẫn bị khóa; T0 + 15:00: vào được (giờ máy chủ, giả lập đồng hồ)

**US-S2E1-02-BV4 — Đăng nhập đúng ở lần thứ 9 làm bộ đếm về 0** · Nguồn: BR-S2E1-09
- Given: Tài khoản An đã sai 9 lần liên tiếp
- When: Đăng nhập đúng; sau đó sai thêm 9 lần liên tiếp
- Then: Đăng nhập đúng được; 9 lần sai sau đó chưa khóa (bộ đếm không cộng dồn qua lần đúng)

**US-S2E1-02-FC3 — Khóa tài khoản này không ảnh hưởng tài khoản khác cùng máy** · Nguồn: BR-S2E1-09; PO-53
- Given: An bị khóa; Bình (FX-03) dùng cùng địa chỉ mạng
- When: Bình đăng nhập đúng mật khẩu
- Then: Bình vào được bình thường

**US-S2E1-02-FC4 — Để trống một trong hai ô** · Nguồn: DQ-18
- Given: Màn "Đăng nhập"
- When: Bấm "Đăng nhập" khi ô mã trống; rồi khi ô mật khẩu trống
- Then: Hiện lỗi cạnh ô trống; không gửi yêu cầu lên máy chủ; không tính một lần sai

**US-S2E1-02-FC5 — Phiên hết hạn thì yêu cầu bị từ chối** · Nguồn: MVP, MSG-GEO-02
- Given: Học sinh An đã đăng nhập; phiên bị hết hạn ở máy chủ
- When: Bấm vào một bài trong "Bài của em"
- Then: Hiện "Phiên đã hết. Vui lòng vào lại."; chuyển về màn "Đăng nhập"; mã lỗi UNAUTHENTICATED

- **Mở và quyết định đang dùng:** QA-S2E1-08 (mã không phân biệt hoa thường)
- **DoR:** ready for R3, mức P0.

## US-S2E1-03 — Giáo viên và học sinh tự khôi phục mật khẩu bằng mã khôi phục

- **Story:** Là giáo viên hoặc học sinh quên mật khẩu, tôi muốn tự đặt lại bằng mã khôi phục đã lưu, để không phải nhờ ai (học sinh Tự học hoặc Chờ duyệt không có giáo viên nào để nhờ) · Loại UI · Mức P0 · Khung LOGIN
- **Nguồn:** PO-61; DQ-17; PO-66 (G3: mã khôi phục cho cả học sinh)
- **Góc nhìn người vận hành:** giáo viên quên mật khẩu sau kỳ nghỉ hè nhưng vẫn giữ tờ in mã khôi phục. Ca đáng kể: mất cả hai thì không có đường lấy lại, đã nói rõ lúc đăng ký (BRD delta mục 2 dòng 6); học sinh Tự học quên mật khẩu (trước đây chỉ có đường tạo tài khoản mới và mất bài làm).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E1-10 | Khôi phục cần mã người dùng (`GV-` hoặc `HS-`), mã khôi phục và mật khẩu mới (8 đến 64 ký tự); thành công thì mã khôi phục cũ hết hiệu lực và hiện mã khôi phục mới một lần (TẠM, QA-S2E1-01) | PO-61 |
| BR-S2E1-11 | Sai mã khôi phục tính vào một bộ đếm riêng: 10 lần sai trong 15 phút khóa đường khôi phục của tài khoản đó 15 phút | PO-53, BR-S2-01 |
| BR-S2E1-12 | Học sinh ở mọi trạng thái lớp (Tự học, Chờ duyệt, Đang học) khôi phục như giáo viên; mã khôi phục của học sinh chỉ dùng được với đúng mã `HS-` của em; đặt lại bởi giáo viên của lớp (US-S3E1-04) vẫn là đường thứ hai cho em đã vào lớp | PO-61, DQ-17; PO-66 (G3) |

### Tiêu chí chấp nhận

**US-S2E1-03-SC1 — Khôi phục đúng đặt được mật khẩu mới** · Nguồn: PO-61
- Given: Cô Trang (FX-01) đã quên mật khẩu, còn mã khôi phục hợp lệ
- When: Ở "Đăng nhập" chọn "Quên mật khẩu (giáo viên)"; nhập mã `GV-7K2M-9Q4D`, mã khôi phục, mật khẩu mới `Trang@2027y`; bấm "Đặt lại"
- Then: Đổi mật khẩu thành công; hiện mã khôi phục MỚI một lần kèm nút tick "đã lưu"; đăng nhập bằng `Trang@2027y` được

**US-S2E1-03-SC2 — Mật khẩu cũ và mã khôi phục cũ hết hiệu lực** · Nguồn: BR-S2E1-10; QA-S2E1-01
- Given: Cô Trang vừa khôi phục thành công
- When: Đăng nhập bằng mật khẩu cũ; dùng lại mã khôi phục cũ để khôi phục lần nữa
- Then: Mật khẩu cũ bị từ chối; mã khôi phục cũ bị từ chối với câu lỗi chung

**US-S2E1-03-FC1 — Sai mã khôi phục hiện câu chung** · Nguồn: PO-61
- Given: Cô Trang
- When: Nhập mã người dùng đúng, mã khôi phục sai; bấm "Đặt lại"
- Then: Hiện "Mã người dùng hoặc mã khôi phục chưa đúng"; mật khẩu không đổi

**US-S2E1-03-BV1 — Sai 9 lần chưa khóa, sai 10 lần khóa đường khôi phục** · Nguồn: BR-S2E1-11; PO-53
- Given: Cô Trang, bộ đếm khôi phục bằng 0
- When: Nhập sai mã khôi phục 9 lần; nhập đúng ở lần 10 (nhánh A); nhánh B: sai 10 lần rồi nhập mã đúng ở lần 11
- Then: Nhánh A: lần 10 thành công; nhánh B: sau lần sai thứ 10 bị khóa 15 phút, lần 11 đúng vẫn bị từ chối (RATE_LIMITED); đường đăng nhập bằng mật khẩu cũ vẫn dùng được

**US-S2E1-03-FC2 — Học sinh Tự học khôi phục được bằng mã khôi phục của mình** · Nguồn: BR-S2E1-12; PO-66 (G3)
- Given: Học sinh An (FX-02) chưa vào lớp nào (Tự học), đã lưu mã khôi phục `RC-4F7H-2K9M-6Q3V`
- When: Nhập mã `HS-3F8A-2L6P`, mã khôi phục đúng và mật khẩu mới `AnMoi#2027`; bấm "Đặt lại"; đăng nhập bằng mật khẩu mới
- Then: Đặt lại thành công, hiện mã khôi phục mới một lần; đăng nhập được và thấy lịch sử Tự học cũ nguyên vẹn (không mất bài làm)

**US-S2E1-03-SC4 — Học sinh Chờ duyệt khôi phục được mật khẩu** · Nguồn: BR-S2E1-12; PO-66 (G3)
- Given: Học sinh Bình (FX-03) đang Chờ duyệt vào lớp 11A1, giáo viên chưa duyệt, có mã khôi phục hợp lệ
- When: Bình khôi phục bằng mã `HS-5B7C-8R2T` và mã khôi phục đúng
- Then: Khôi phục thành công; trạng thái Chờ duyệt giữ nguyên; Bình không cần nhờ giáo viên và không phải tạo tài khoản mới

**US-S2E1-03-FC4 — Mã khôi phục của người khác không dùng được cho mã HS khác** · Nguồn: BR-S2E1-11; BR-S2E1-12; PO-66 (G3)
- Given: Học sinh An và Bình mỗi em có mã khôi phục riêng
- When: Nhập mã `HS-3F8A-2L6P` kèm mã khôi phục của Bình
- Then: Hiện "Mã người dùng hoặc mã khôi phục chưa đúng"; mật khẩu của An không đổi; lần sai tính vào bộ đếm khôi phục của An (BR-S2E1-11)

**US-S2E1-03-BV2 — Mật khẩu mới 7 ký tự bị chặn, 8 ký tự nhận** · Nguồn: BR-S2E1-10
- Given: Cô Trang, mã khôi phục hợp lệ
- When: Đặt lại với mật khẩu mới 7 ký tự; đặt lại với 8 ký tự
- Then: Lần 1: lỗi "Mật khẩu từ 8 đến 64 ký tự.", mã khôi phục vẫn còn hiệu lực; lần 2: thành công

**US-S2E1-03-SC3 — Màn đăng ký nói rõ không lấy lại được khi mất cả hai** · Nguồn: BRD delta mục 2 dòng 6; PO-61
- Given: Màn mã sau đăng ký của giáo viên
- When: Đọc dòng cảnh báo cạnh mã khôi phục
- Then: Có câu: mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản

**US-S2E1-03-FC3 — Đổi mật khẩu bằng khôi phục hủy phiên đang đăng nhập khác** · Nguồn: QA-S2E1-01
- Given: Cô Trang đang đăng nhập ở máy B
- When: Khôi phục mật khẩu ở máy A; ở máy B bấm sang một màn khác
- Then: Máy B nhận UNAUTHENTICATED và về màn "Đăng nhập"

- **Mở và quyết định đang dùng:** QA-S2E1-01, QA-S2E1-04
- **DoR:** ready for R3, mức P0.

## US-S2E1-04 — Dữ liệu: chuyển tài khoản mẫu MVP sang mô hình mã người dùng

- **Story:** Là chủ sản phẩm, tôi muốn chuyển dữ liệu mẫu của MVP (giáo viên mẫu, học sinh vào lớp bằng tên và mã lớp) sang mô hình mã người dùng, để không mất tiến độ đã có · Loại Data · Mức P0 · Khung (dữ liệu)
- **Nguồn:** PO-61
- **Góc nhìn người vận hành:** lệnh chạy trên môi trường dev và test, không chạy trên prod (PO-51); chạy hai lần do nhầm; dữ liệu cũ có dòng lỗi.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E1-13 | Mỗi học sinh của lớp MVP (cặp tên và lớp) thành một tài khoản có mã `HS-`; "Giáo viên mẫu" thành một tài khoản có mã `GV-`; lớp, tiến độ và bài làm giữ nguyên | PO-61 |
| BR-S2E1-14 | Lệnh chuyển chạy lại được: chạy lần hai không tạo thêm tài khoản (cùng cặp tên và lớp đã chuyển thì bỏ qua) | PO-61 |
| BR-S2E1-15 | Tài khoản chuyển mang mật khẩu tạm và cờ "phải đặt mật khẩu mới ở lần đăng nhập đầu" (TẠM, QA-S2E1-05) | PO-61 |

- **Dữ liệu:** dữ liệu mẫu MVP: lớp 11A1 có 5 học sinh trong đó An có tiến độ 2/3 trên BG-1; một học sinh có tên rỗng cố ý (dòng lỗi) trong bộ thử

### Tiêu chí chấp nhận

**US-S2E1-04-SC1 — Mỗi học sinh MVP thành một tài khoản có mã** · Nguồn: PO-61
- Given: Dữ liệu mẫu MVP với 5 học sinh lớp 11A1
- When: Chạy lệnh chuyển
- Then: Có 5 tài khoản `HS-` mới (mỗi mã khác nhau); tiến độ của An vẫn 2/3 trên BG-1; số bài làm trước và sau bằng nhau

**US-S2E1-04-SC2 — Giáo viên mẫu thành một tài khoản có mã** · Nguồn: PO-61
- Given: Dữ liệu mẫu MVP có "Giáo viên mẫu"
- When: Chạy lệnh chuyển
- Then: Có đúng một tài khoản `GV-` cho giáo viên mẫu, vẫn là chủ lớp 11A1; có mã khôi phục sinh một lần ghi vào báo cáo chuyển

**US-S2E1-04-SC3 — Chạy lại lệnh không tạo trùng** · Nguồn: BR-S2E1-14
- Given: Lệnh chuyển đã chạy một lần
- When: Chạy lệnh chuyển lần hai
- Then: Số tài khoản, số bài làm, số lớp không đổi; báo cáo ghi "đã chuyển trước đó" cho mọi dòng

**US-S2E1-04-FC1 — Dòng dữ liệu lỗi không làm dừng cả lô** · Nguồn: BR-S2E1-13
- Given: Bộ thử có một học sinh tên rỗng
- When: Chạy lệnh chuyển
- Then: Dòng lỗi vào báo cáo lỗi kèm lý do; 4 học sinh còn lại được chuyển; lệnh kết thúc với mã thoát khác 0

**US-S2E1-04-SC4 — Tài khoản chuyển phải đặt mật khẩu mới ở lần đăng nhập đầu** · Nguồn: BR-S2E1-15; QA-S2E1-05
- Given: Tài khoản An sau chuyển có mật khẩu tạm
- When: Đăng nhập bằng mã và mật khẩu tạm
- Then: Hiện màn "Đặt mật khẩu mới" trước khi vào "Bài của em"; sau khi đặt xong vào được

**US-S2E1-04-FC2 — Lệnh từ chối chạy trên prod** · Nguồn: PO-51
- Given: Môi trường được đặt là `prod`
- When: Chạy lệnh chuyển
- Then: Lệnh thoát ngay với thông báo từ chối; không đọc hay ghi dữ liệu

- **Mở và quyết định đang dùng:** QA-S2E1-05, QA-S2E1-06
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 4; tiêu chí: 46. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 4 story, 44 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | Sửa theo RV_s23: BR-S2E1-06 nêu cách giữ bộ đếm IP; thêm BR-S2E1-16 trỏ luật tên giống email hoặc số điện thoại; đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
