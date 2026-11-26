Phiên bản: v0.2 · 2026-11-26
# S2-E7 — Trình chiếu trên lớp: toàn màn hình, hiện từng bước, mất mạng, hỏi lại khi lời giải khóa: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Ba story cho khung C2-04 (ảnh `c2-04-trinh-chieu.png`, `c2-04-che-loi-giai.png`; DQ-08…DQ-10).

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S2En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, kèm giá trị cụ thể. `SC` = kịch bản chính, `FC` = lỗi hoặc ngoại lệ, `BV` = giá trị biên. Mã chỉ cho biết **chỗ** một điều được ghi, câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC trong tệp này** (kế thừa MVP, đối chiếu `stories_ac.md` của E1):
- "T0" là giờ máy chủ lúc bắt đầu ca kiểm; mọi mốc giờ tương đối so với T0. Giờ hiển thị `HH:mm, dd/MM/yyyy` giờ Việt Nam; số thập phân hiển thị dấu phẩy.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định theo D-GEO-143 (VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, RATE_LIMITED kèm `Retry-After`).
- Phản hồi chứa lời giải, gợi ý hoặc trạng thái khóa mang `Cache-Control: no-store` (D-GEO-145). "Dữ liệu trả về" là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Thiết bị theo PO-61 và STAGES mục 0.2: giáo viên dùng web (điện thoại chỉ để liếc); học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA, PO-65; chốt PO-66).
- AI trong kiểm thử là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật. Giá trị trong Given / Then lấy từ bảng `FX-nn` hoặc ghi ngay trong tiêu chí.
- AC mang chữ "đã chốt PO-66" dựa trên giả định đã được owner chốt ở PO-66 (26/11/2026); mã `QA-S2En-nn` hoặc `DQ-nn` đứng cạnh cho biết câu hỏi gốc.

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-S2En-nn` · `…-SCn` / `-FCn` / `-BVn` | story của epic S2-En · tiêu chí thành công / lỗi / biên thứ n của story | tệp này |
| `BR-S2En-nn` | quy tắc nghiệp vụ BA làm rõ cho epic; đi kèm `BR-S2-nn` của BRD delta khi có | `artifacts/GEO/work/s23/brd-delta-s23.md` |
| `M-S2-nn` | chỉ số chặng 2 (công thức ở BRD delta mục 1) | `brd-delta-s23.md` |
| `PO-nn` | dòng của sổ quyết định gốc | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi design và mặc định đang dùng | `Design/s23/DQ.md` |
| `C2-nn` | mã khung màn hình chặng 2 (C2-01…C2-08) | `STAGES-AFTER-MVP.md` Chặng 2 mục 6 |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (mã lỗi ổn định D-GEO-143, mock AI D-GEO-047, tiêu đề no-store D-GEO-145) | `artifacts/GEO/work/03_decisions.md` (MVP) |
| `QA-S2En-nn` | câu hỏi mở của epic, luôn kèm mặc định (đã chốt PO-66) | `questions.md` cùng thư mục |
| `TC-S2En-nnn` | test case của epic | `test_cases.md` cùng thư mục |
| `FX-nn` | dữ liệu mẫu chặng 2 (bảng dưới) | tệp này |
| `BG-n` | bài giao mẫu của MVP (BG-1…BG-5) | `artifacts/GEO/work/fixture.md` (MVP) |
| `BR-GEO-nnn` | quy tắc nghiệp vụ của MVP còn hiệu lực | `artifacts/GEO/work/02_gate2_brd.md` (MVP) |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng của MVP (câu chữ nguyên văn) | `artifacts/GEO/work/05_dictionary.md` (MVP) |
| Đã chốt PO-66 | mặc định đã được chủ đầu tư chốt ở PO-66 (26/11/2026); ghi theo mẫu `quyết định · tin cậy · nguồn`; ngưỡng chỉnh sau L6 (G12) | PO-65, PO-66 |
| Nhãn hai trục | trục máy: "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) · trục người: "AI soạn" / "Giáo viên đã duyệt" | PO-02 |
| `release_at` | giờ mở lời giải; máy chủ so giờ máy chủ, không tin giờ thiết bị | MVP, M-GEO-13 |
| Trình chiếu | chế độ toàn màn hình của giáo viên để giảng; lời giải che sẵn, hiện từng bước | STAGES Ch2 mục 5 luồng B |
| che toàn cục | nút "Che lời giải" ẩn mọi bước đang hiện nhưng nhớ vị trí bước | DQ-08 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B7C-8R2T`, cùng lớp 11A1 với An |
| FX-04 | Người soát nội dung "Mai" | mã `GV-2N6P-4W8X`, vai soát nội dung do chủ sản phẩm gán (đã chốt PO-66, QA-S2E6-01) |
| FX-10 | Đề phẳng PL-01 | Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số 90° |
| FX-11 | Đề phẳng PL-02 | Tam giác ABC cân tại A, góc A = 40°; tính góc B. Đáp số 70° |
| FX-12 | Đề phẳng PL-03 | Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC. Đáp số 10 |
| FX-13 | Đề phẳng PL-04 | Tam giác đều cạnh 4; tính diện tích. Đáp số `4√3` (xấp xỉ 6,93) |
| FX-14 | Đề không gian SP-01 | Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc giữa SC và mặt đáy. Đáp số 45° (tan SCA = SA / AC = 1) |
| FX-20 | Bài luyện BL-1 | 5 câu; câu 2 là FX-14, kiểu đáp số "số đo góc", sai số ±0,5°, 3 lượt thử; giờ mở lời giải `release_at` = T0 + 2 giờ |

## US-S2E7-01 — Trình chiếu toàn màn hình, hiện từng bước, nút che lời giải

- **Story:** Là giáo viên, tôi muốn chiếu đề toàn màn hình và hiện lời giải từng bước, để giảng cho cả lớp mà không lộ trước · Loại UI · Mức P0 · Khung C2-04
- **Nguồn:** STAGES Ch2 mục 5 luồng B; DQ-08; BR-S2-07
- **Góc nhìn người vận hành:** giáo viên nối máy chiếu và đứng giữa lớp bấm phím; phải che nhanh khi học sinh hỏi bất ngờ; chữ phải đọc được từ cuối lớp.
- **Phụ thuộc:** S2-E5 (lời giải), S2-E1

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E7-01 | Vào Trình chiếu từ một đề: toàn màn hình, lời giải che sẵn (0 bước hiện) | STAGES Ch2 mục 5 luồng B |
| BR-S2E7-02 | "Hiện bước kế" hiện thêm đúng một bước và tô màu phần hình của bước đó; "Bước trước" ẩn một bước; phím mũi tên phải và Space là bước kế, mũi tên trái là bước trước (đã chốt PO-66, DQ-08) | DQ-08 |
| BR-S2E7-03 | "Che lời giải" ẩn mọi bước, nhớ vị trí; bấm lần nữa hiện lại tới bước đã hiện (đã chốt PO-66, DQ-08) | DQ-08 |
| BR-S2E7-04 | Giáo viên xoay hình, đo, thêm đường phụ và hình đổi ngay; chữ và nét đủ lớn: chữ lời giải không nhỏ hơn 28 điểm ở màn hình 1920 × 1080 (đã chốt PO-66, QA-S2E7-01) | STAGES Ch2 mục 5 luồng B |
| BR-S2E7-05 | Phím Esc thoát Trình chiếu về C2-03 và che lại lời giải; học sinh không vào được Trình chiếu | STAGES mục 0.2 nguyên tắc 6 |
| BR-S2E7-06 | Góc màn hình luôn có hai nhãn nhỏ (PO-02) | DQ-07 |

### Tiêu chí chấp nhận

**US-S2E7-01-SC1 — Vào Trình chiếu thấy hình lớn, đề và lời giải che sẵn** · Nguồn: BR-S2E7-01
- Given: Cô Trang mở đề FX-14 (lời giải 4 bước) ở C2-03 trên web
- When: Bấm "Trình chiếu"
- Then: Chuyển toàn màn hình; hiện hình lớn và chữ đề; không bước lời giải nào hiện; có thanh công cụ gọn với "Hiện bước kế", "Bước trước", "Che lời giải"

**US-S2E7-01-SC2 — Hiện bước kế hiện đúng một bước và tô màu hình** · Nguồn: BR-S2E7-02; STAGES Ch2 mục 5
- Given: Đang ở 0 bước hiện
- When: Bấm "Hiện bước kế"
- Then: Bước 1 hiện; phần hình của bước 1 (AC là hình chiếu, góc SCA) tô màu; các bước 2 đến 4 chưa hiện

**US-S2E7-01-SC3 — Phím mũi tên và Space điều khiển bước** · Nguồn: BR-S2E7-02
- Given: Đang ở 1 bước hiện
- When: Nhấn →; nhấn Space; nhấn ←
- Then: → : bước 2 hiện; Space: bước 3 hiện; ←: ẩn bước 3, còn 2 bước

**US-S2E7-01-SC4 — Che lời giải ẩn tất cả, bỏ che hiện lại đúng bước** · Nguồn: BR-S2E7-03
- Given: Đang hiện 3 bước
- When: Bấm "Che lời giải"; bấm lần nữa
- Then: Lần 1: không bước nào hiện (cả chữ và phần tô màu của hình); lần 2: hiện lại đúng 3 bước

**US-S2E7-01-SC5 — Xoay hình, đo, thêm đường phụ thì hình đổi ngay** · Nguồn: STAGES Ch2 mục 5 luồng B
- Given: Đang trình chiếu đề FX-14
- When: Kéo xoay hình; đo đoạn AC; thêm đường phụ nối A với trung điểm BC
- Then: Hình xoay theo tay kéo; đo hiện giá trị của AC; đường phụ vẽ ngay; các thao tác không đổi dữ liệu đề đã lưu

**US-S2E7-01-BV1 — Ở bước cuối nút Hiện bước kế khóa** · Nguồn: BR-S2E7-02
- Given: Lời giải 4 bước, đang hiện cả 4
- When: Bấm "Hiện bước kế" thêm một lần; nhấn →
- Then: Nút khóa kèm "Hết bước"; phím → không đổi gì; vẫn 4 bước hiện

**US-S2E7-01-BV2 — Ở bước 0 nút Bước trước khóa** · Nguồn: BR-S2E7-02
- Given: Mới vào Trình chiếu (0 bước hiện)
- When: Bấm "Bước trước"; nhấn ←
- Then: Nút khóa; không đổi gì

**US-S2E7-01-BV3 — Lời giải 30 bước chiếu được đủ 30 bước** · Nguồn: BR-S2E7-02
- Given: Đề có lời giải 30 bước (giới hạn của S2-E5)
- When: Bấm "Hiện bước kế" 30 lần
- Then: Cả 30 bước hiện lần lượt; ở lần thứ 31 nút khóa; hiệu năng không giật (chuyển bước dưới 200 ms, đã chốt PO-66)

**US-S2E7-01-FC1 — Esc thoát về C2-03 và che lại lời giải** · Nguồn: BR-S2E7-05
- Given: Đang chiếu, hiện 2 bước
- When: Nhấn Esc; bấm "Trình chiếu" lại
- Then: Về C2-03; vào lại thấy 0 bước hiện (che sẵn)

**US-S2E7-01-FC2 — Trình duyệt từ chối toàn màn hình vẫn chiếu được** · Nguồn: BR-S2E7-01
- Given: Trình duyệt chặn toàn màn hình
- When: Bấm "Trình chiếu"
- Then: Hiện bố cục cửa sổ lớn lấp đầy trang kèm dòng "Trình duyệt không cho toàn màn hình, nhấn F11"; mọi nút hoạt động

**US-S2E7-01-FC3 — Học sinh không vào được Trình chiếu** · Nguồn: BR-S2E7-05; STAGES mục 0.2 nguyên tắc 6
- Given: Học sinh An
- When: An gọi trang và dữ liệu của Trình chiếu
- Then: FORBIDDEN; không bước lời giải nào trả về

**US-S2E7-01-FC4 — Đề chưa có lời giải thì khóa nút hiện bước** · Nguồn: BR-S2E7-02
- Given: Đề FX-12 chưa có lời giải
- When: Bấm "Trình chiếu"
- Then: Hiện hình và đề; nút "Hiện bước kế" khóa kèm "Chưa có lời giải"

**US-S2E7-01-SC6 — Hai nhãn luôn nằm ở góc màn hình** · Nguồn: BR-S2E7-06; DQ-07
- Given: Lời giải nhãn "Cần xem lại" và "Giáo viên đã duyệt"
- When: Trình chiếu; hiện bước
- Then: Hai nhãn nhỏ hiện ở góc cả khi lời giải che và khi hiện

**US-S2E7-01-SC7 — Chữ lời giải đủ lớn để đọc từ cuối lớp** · Nguồn: BR-S2E7-04; QA-S2E7-01
- Given: Màn 1920 × 1080; bước 3 dài 120 ký tự
- When: Hiện bước 3
- Then: Chữ lời giải không nhỏ hơn 28 điểm; không bị cắt; xuống dòng gọn

- **Mở và quyết định đang dùng:** QA-S2E7-01
- **DoR:** ready for R3, mức P0.

## US-S2E7-02 — Chạy được khi mất mạng; hỏi đáp AI tạm tắt kèm thông báo

- **Story:** Là giáo viên, tôi muốn buổi chiếu không gián đoạn khi mạng trường chập chờn, để không phải dừng giữa giờ · Loại UI · Mức P0 · Khung C2-04
- **Nguồn:** STAGES Ch2 mục 5 luồng B ngoại lệ; DQ-10
- **Góc nhìn người vận hành:** mạng trường hay rớt giữa tiết. Đề, hình, lời giải phải tải sẵn lúc vào chế độ; hỏi đáp AI cần mạng nên tắt tạm có thông báo; dữ liệu tải sẵn không được ở lại máy sau khi thoát.
- **Phụ thuộc:** US-S2E7-01

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E7-07 | Khi vào Trình chiếu tải sẵn đề, hình, mọi bước lời giải; xong thì hiện chip "Đã tải sẵn · mất mạng vẫn chiếu" | DQ-10 |
| BR-S2E7-08 | Mất mạng giữa giờ: hiện bước, che, xoay, đo vẫn dùng được; nút hỏi đáp AI tạm tắt kèm "Hỏi đáp AI tạm tắt vì mất mạng"; có mạng lại thì tự bật trong 5 giây (đã chốt PO-66, QA-S2E7-02) | STAGES Ch2 mục 5 ngoại lệ |
| BR-S2E7-09 | Chưa tải sẵn xong mà mất mạng: không vào chế độ chiếu nửa vời, hiện "Cần mạng để mở lần đầu" và nút "Thử lại" | DQ-10 |
| BR-S2E7-10 | Thoát Trình chiếu thì xóa dữ liệu lời giải đã tải sẵn khỏi bộ nhớ trình duyệt | STAGES mục 0.2 nguyên tắc 1 (đã chốt PO-66, QA-S2E7-03) |

### Tiêu chí chấp nhận

**US-S2E7-02-SC1 — Vào Trình chiếu có mạng thì hiện chip Đã tải sẵn** · Nguồn: BR-S2E7-07; DQ-10
- Given: Có mạng; đề FX-14
- When: Bấm "Trình chiếu"
- Then: Sau khi tải xong hiện chip "Đã tải sẵn · mất mạng vẫn chiếu"

**US-S2E7-02-SC2 — Mất mạng giữa giờ vẫn hiện bước và xoay hình** · Nguồn: BR-S2E7-08
- Given: Đang chiếu, đã tải sẵn; ngắt mạng
- When: Bấm "Hiện bước kế" hai lần; kéo xoay hình; đo
- Then: Hai bước hiện; hình xoay; đo hoạt động; không thông báo lỗi mạng cho các thao tác này

**US-S2E7-02-FC1 — Mất mạng thì hỏi đáp AI tắt kèm thông báo** · Nguồn: BR-S2E7-08; STAGES Ch2 mục 5
- Given: Đang chiếu; ngắt mạng
- When: Bấm nút hỏi đáp AI
- Then: Nút ở trạng thái tắt; hiện "Hỏi đáp AI tạm tắt vì mất mạng"; không gửi yêu cầu

**US-S2E7-02-SC3 — Có mạng lại thì hỏi đáp AI bật lại** · Nguồn: BR-S2E7-08; QA-S2E7-02
- Given: Đang mất mạng, hỏi đáp AI tắt
- When: Bật lại mạng; chờ 5 giây
- Then: Trong 5 giây nút hỏi đáp bật lại; thông báo mất mạng biến mất

**US-S2E7-02-BV1 — Bật lại sau 5 giây là hạn, 6 giây là quá** · Nguồn: BR-S2E7-08; QA-S2E7-02
- Given: Mất mạng; đồng hồ giả
- When: Có mạng lại; kiểm nút ở 5 giây và ở 6 giây
- Then: Đến mốc 5 giây nút đã bật; không trễ tới 6 giây

**US-S2E7-02-FC2 — Vào Trình chiếu khi đã mất mạng và chưa tải sẵn** · Nguồn: BR-S2E7-09
- Given: Mất mạng; chưa mở đề này trước đó
- When: Bấm "Trình chiếu"
- Then: Hiện "Cần mạng để mở lần đầu" và nút "Thử lại"; không vào chế độ chiếu; không hiện hình nửa vời

**US-S2E7-02-FC3 — Mất mạng khi đang tải sẵn thì báo chưa đủ** · Nguồn: BR-S2E7-09
- Given: Đang tải sẵn 2 trong 4 bước; ngắt mạng
- When: Chờ tải
- Then: Hiện "Chưa tải sẵn đủ, cần mạng"; nút "Thử lại"; không cho vào chế độ chiếu

**US-S2E7-02-FC4 — Thoát Trình chiếu xóa dữ liệu tải sẵn** · Nguồn: BR-S2E7-10; QA-S2E7-03
- Given: Đã chiếu đề FX-14, tải sẵn 4 bước
- When: Nhấn Esc; kiểm bộ nhớ trình duyệt (cache, lưu trữ cục bộ)
- Then: Không còn nội dung lời giải của đề trong bộ nhớ trình duyệt

**US-S2E7-02-FC5 — Phiên hết hạn lúc mất mạng thì chiếu tiếp, về lại thì đăng nhập** · Nguồn: BR-S2E7-08
- Given: Đang chiếu; phiên hết hạn trong lúc mất mạng
- When: Tiếp tục hiện bước; bật lại mạng; gọi yêu cầu cần xác thực
- Then: Chiếu vẫn tiếp tục khi mất mạng; khi có mạng, yêu cầu có xác thực nhận UNAUTHENTICATED và hiện "Phiên đã hết. Vui lòng vào lại." sau khi giáo viên thoát chiếu, không ngắt buổi chiếu đột ngột

- **Mở và quyết định đang dùng:** QA-S2E7-02, QA-S2E7-03
- **DoR:** ready for R3, mức P0.

## US-S2E7-03 — Hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu" khi lời giải đang khóa

- **Story:** Là giáo viên, tôi muốn được hỏi lại khi chiếu đề đang giao mà lời giải còn khóa, để không vô tình lộ lời giải trước giờ mở · Loại Logic · Mức P0 · Khung C2-04
- **Nguồn:** STAGES Ch2 mục 5 luồng B ngoại lệ; DQ-09
- **Góc nhìn người vận hành:** chiếu lên màn lớp là cả lớp thấy, kể cả học sinh chưa tới giờ mở. Trình chiếu chỉ là màn của giáo viên, máy học sinh vẫn khóa (DQ-09). Giờ máy chủ quyết định đang khóa hay đã mở.
- **Phụ thuộc:** US-S2E7-01, S2-E5

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E7-11 | Khi giáo viên bấm Trình chiếu một đề đang giao cho ít nhất một lớp mà lời giải của bài giao đó còn khóa (chưa đặt giờ hoặc giờ máy chủ < `release_at`), hệ thống hỏi "Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?" nêu tên lớp còn khóa | STAGES Ch2 mục 5 |
| BR-S2E7-12 | Không hỏi khi đề chưa giao hoặc mọi bài giao đã mở lời giải (giờ máy chủ ≥ `release_at`) | STAGES Ch2 mục 5 |
| BR-S2E7-13 | Chọn "Tiếp tục" vào Trình chiếu với lời giải che sẵn; chọn "Hủy" ở lại C2-03; trong cả hai trường hợp trạng thái khóa phía học sinh không đổi, chip "Máy học sinh vẫn khóa lời giải" hiện trong Trình chiếu | DQ-09 |
| BR-S2E7-14 | Việc hỏi dựa trên giờ máy chủ, không dựa trên giờ máy giáo viên | MVP, M-GEO-13 |

- **Dữ liệu:** đề FX-14 giao BG-1 (11A1) với `release_at` = T0 + 2 giờ; BG-2 (11A2) với `release_at` = T0 - 1 giờ (đã mở)

### Tiêu chí chấp nhận

**US-S2E7-03-SC1 — Đề đang giao và lời giải khóa thì hỏi lại** · Nguồn: BR-S2E7-11
- Given: Đề FX-14 giao BG-1 (11A1), lời giải mở lúc T0 + 2 giờ
- When: Bấm "Trình chiếu" lúc T0
- Then: Hiện hộp "Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?" nêu "11A1" và hai nút "Tiếp tục", "Hủy"; chưa vào chế độ chiếu

**US-S2E7-03-SC2 — Tiếp tục thì vào chiếu, máy học sinh vẫn khóa** · Nguồn: BR-S2E7-13; DQ-09
- Given: Hộp hỏi đang hiện (SC1)
- When: Bấm "Tiếp tục"; hiện vài bước; An (học sinh 11A1) gọi lời giải bài giao
- Then: Vào Trình chiếu che sẵn với chip "Máy học sinh vẫn khóa lời giải"; dữ liệu trả về cho An không chứa bước giải hay đáp số

**US-S2E7-03-SC3 — Hủy thì ở lại và không thay đổi gì** · Nguồn: BR-S2E7-13
- Given: Hộp hỏi đang hiện
- When: Bấm "Hủy"
- Then: Ở lại C2-03; không toàn màn hình; không bản ghi chiếu

**US-S2E7-03-FC1 — Đề chưa giao thì không hỏi** · Nguồn: BR-S2E7-12
- Given: Đề FX-12 chưa giao lớp nào
- When: Bấm "Trình chiếu"
- Then: Vào thẳng Trình chiếu; không hộp hỏi

**US-S2E7-03-FC2 — Bài giao đã mở lời giải thì không hỏi** · Nguồn: BR-S2E7-12
- Given: Đề FX-14 chỉ giao BG-2 (11A2) với `release_at` = T0 - 1 giờ
- When: Bấm "Trình chiếu"
- Then: Vào thẳng; không hộp hỏi

**US-S2E7-03-BV1 — Giờ máy chủ bằng release_at thì không hỏi, trước 1 giây thì hỏi** · Nguồn: BR-S2E7-11, BR-S2E7-12; M-GEO-13
- Given: Bài giao có `release_at` = R; đồng hồ giả
- When: Bấm "Trình chiếu" ở R - 1 giây; bấm ở đúng R
- Then: R - 1 giây: hỏi; đúng R: không hỏi (đã mở)

**US-S2E7-03-SC4 — Chưa đặt giờ mở thì cũng hỏi** · Nguồn: BR-S2E7-11
- Given: Đề giao BG-3 chưa đặt `release_at`
- When: Bấm "Trình chiếu"
- Then: Hiện hộp hỏi nêu tên lớp của BG-3

**US-S2E7-03-SC5 — Nhiều lớp thì nêu tên các lớp còn khóa** · Nguồn: BR-S2E7-11
- Given: FX-14 giao BG-1 (11A1, khóa) và BG-2 (11A2, đã mở)
- When: Bấm "Trình chiếu"
- Then: Hộp hỏi chỉ nêu "11A1" (lớp còn khóa), không nêu 11A2

**US-S2E7-03-FC3 — Giờ máy giáo viên lệch không đổi kết quả hỏi** · Nguồn: BR-S2E7-14
- Given: BG-1 `release_at` = T0 + 2 giờ; đồng hồ máy giáo viên chạy nhanh 3 giờ
- When: Bấm "Trình chiếu"
- Then: Vẫn hiện hộp hỏi (theo giờ máy chủ)

**US-S2E7-03-FC4 — Học sinh không thể mở khóa bằng cách giáo viên bấm Tiếp tục** · Nguồn: BR-S2E7-13
- Given: Đã bấm "Tiếp tục"
- When: Học sinh An làm mới trang bài giao nhiều lần
- Then: Bài giao vẫn khóa: không bước giải, không đáp số ở mọi dạng viết; `release_at` không đổi

- **Mở và quyết định đang dùng:** không có
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 3; tiêu chí: 33. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 3 story, 33 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
