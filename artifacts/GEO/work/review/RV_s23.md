Phiên bản: v0.3 · 2026-11-26
# RV_s23 — Soát phản biện spec chặng 2–3 (S2-E1…E9, S3-E1…E6)

> Người soát: runner-T-261126-15 (QC, soát độc lập, không dùng kết luận của agent viết spec). Phạm vi: 15 epic ở `artifacts/GEO/work/epics/` (T-12, T-14, 26/11/2026). Đối chiếu: `PO-DECISIONS.md` PO-61…65, `Design/s23/DQ.md`, `work/s23/story-map-s23.md`. Mỗi mục có tệp:dòng (dòng tính SAU khi sửa), vì sao, đề xuất, trạng thái.
> Cách soát: đọc toàn bộ bảng BR của 15 epic, đọc ACs của C2-03 (S2-E5), C2-06 (S2-E8), C3-04 (S3-E3), S2-E1, S3-E1, S3-E5; đối chiếu chéo bằng lệnh `grep`; so tiêu đề TC với tiêu đề AC cho mọi TC chỉ trỏ một AC (0 lệch); chạy `check_ac_tc.py` mở rộng. Chưa đọc từng chữ của mọi AC của S2-E2, E4, E9, S3-E6 (chỉ đối chiếu BR và tiêu đề): phần này soát nhẹ.

## 0. Tổng hợp

| Mức | Tổng | Đã sửa trong spec | Đã chốt PO-66 | Còn mở |
|---|---|---|---|---|
| Blocker | 1 | 0 | 1 (B-1, G1) | 0 |
| Major | 10 | 6 | 4 (M-01 G2, M-08 G4, M-09 G3, M-10 G5) | 0 |
| Minor | 8 | 2 | 1 (m-02, G5) | 5 |

Trạng thái mở: blocker 0, major 0, minor 5 (m-04…m-08, không liên quan gate). Mọi mục từng chờ owner đã chốt ở PO-66 (26/11/2026, "OK hết" cho GATE-T16) và đã áp vào spec ở T-261126-37 (v0.3).
Số liệu sau khi áp PO-66: 58 story (chặng 2: 35, chặng 3: 23), 746 AC, 751 TC; `check_ac_tc.py` 0 lỗi (trước: 56 story, 723 AC, 728 TC).
Công cụ: `check_ac_tc.py` ban đầu bỏ sót mọi AC loại `BV` (biên); sau khi sửa regex, 723 AC còn hiệu lực của 15 epic đều có TC (S2: 442 AC / 447 TC; S3: 281 AC / 281 TC); sau PO-66: 746 AC, 751 TC.

## 1. Blocker

### B-1 · Hai mô hình giao bài song song, và chặng 2 giao bài cho "lớp" khi lớp chỉ được tạo ở chặng 3 · Đã chốt PO-66 (G1)
- Chỗ: `S2-E8/stories_ac.md:393` (BR-S2E8-19 "học sinh thuộc lớp được giao"), `:595` (BR-S2E8-29 Giao bài), `S3-E1/stories_ac.md:87` (BR-S3E1-01 chỉ S3 mới có "tạo lớp"), `S3-E2/stories_ac.md:87` (BR-S3E2-01 `AssignmentSet`), `S3-E3/stories_ac.md:89` (ô đọc từ "bài làm").
- Vì sao: S2-E8 định nghĩa bài luyện (câu, lượt, giờ mở, Giao, Đóng) và kết quả riêng (C2-07). S3-E2 định nghĩa bộ bài (`AssignmentSet/Item`, `release_at`, Giao, Đóng) và S3-E3 dựng bảng lớp × bài trên bộ bài. Không AC nào nói bài luyện có là bộ bài hay không, bảng Kết quả lớp S3-E3 đọc từ `Attempt` của S2-E8 hay từ bảng khác, và cùng một đề giao bằng hai đường thì lời giải mở theo `release_at` nào. Ở chặng 2 không có đường nào tạo lớp (chỉ có lớp MVP đã chuyển, BR-S2E1-13).
- Đề xuất (chọn một, owner/PO quyết): (a) bài luyện của S2-E8 là một `AssignmentSet` của S3-E2 (một mô hình; S2-E8 chỉ làm phần màn học sinh C2-06/07 và soạn C2-08, giao bài dùng S3-E2); trong lúc chờ S3, chặng 2 chạy trên lớp mẫu đã chuyển (BR-S3E1-16); (b) kéo "tạo lớp tối giản" vào S2-E8. Khuyến nghị (a): ít code trùng nhất. Sau khi chốt, BA sửa BR-S2E8-19/29, thêm một BR nối ở S3-E2 và S3-E3.
- Trạng thái: Đã chốt PO-66 (G1): một mô hình "bộ bài"; chặng 2 giao cho lớp mẫu 11A1 từ MVP. Đã sửa: BR-S2E8-32, US-S2E8-05-SC10/FC10, BR-S3E2-07, US-S3E2-01-SC7; QA-S2E8-08 đóng.

## 2. Major

### M-01 · Quyền xem lời giải Kho chung: BR-S2E6-14 lệch PO-63, và "Tự học" không có chỗ định nghĩa · Đã chốt PO-66 (G2)
- Chỗ: `S2-E6/stories_ac.md:228`; PO-63 (`PO-DECISIONS.md:90`); `S3-E1/stories_ac.md:184` (BR-S3E1-12 "Tự học"); `S3-E5/stories_ac.md:392`.
- Vì sao: PO-63 nói ai có tài khoản đều "xem lời giải Kho chung theo giờ mở như học sinh". BR-S2E6-14 cho tài khoản vai Giáo viên xem lời giải Kho chung ngay (không theo giờ mở) và cấm học sinh xem ngoài bài giao đã mở. Vai chỉ là lựa chọn lúc đăng ký (PO-61, không xác minh) nên bất kỳ ai chọn "Giáo viên" đọc mọi lời giải ngay: đây đúng là rủi ro "giả làm giáo viên" mà PO-63 đã ghi chấp nhận, nhưng PO-63 ghi "theo giờ mở", còn BR thì không có giờ mở. Thêm: "Tự học" (học sinh không lớp) dùng đề gì, xem lời giải khi nào, không epic chặng 2–3 nào định nghĩa; nếu học sinh không xem được lời giải Kho chung thì Tự học không có lời giải.
- Đề xuất: owner chọn (a) giữ BR-S2E6-14 và sửa PO-63 cho khớp; (b) sửa BR theo PO-63: Kho chung có `release_at` công khai (mặc định mở sau N giờ hoặc người soát đặt) cho mọi vai. Kèm câu hỏi: Tự học dùng Kho chung theo luật nào.
- Trạng thái: Đã chốt PO-66 (G2): giáo viên xem ngay; học sinh tự học (không lớp) khóa lời giải, chỉ gợi ý. Đã sửa: BR-S2E6-14, US-S2E6-03-FC9; QA-S2E6-06 đóng.

### M-02 · Lớp MVP đã chuyển không có đường thành lớp của chặng 3 · ĐÃ SỬA
- Chỗ: `S2-E1/stories_ac.md:310` (BR-S2E1-13 "lớp giữ nguyên") và S3-E1 không nói gì về lớp cũ.
- Vì sao: sau chuyển có lớp 11A1 nhưng không có mã lớp `HK-`, không `ClassMembership`, chủ lớp mơ hồ; S3-E1…E5 hỏng ở lớp duy nhất đang có dữ liệu.
- Sửa: `S3-E1/stories_ac.md:93` BR-S3E1-16, AC `US-S3E1-01-SC6`, `S3-E1/test_cases.md` TC-S3E1-54 (v0.2). Đã chốt PO-66 (G9).

### M-03 · Giới hạn tạo tài khoản "theo địa chỉ mạng" mâu thuẫn với "không lưu IP" · ĐÃ SỬA (đã chốt PO-66, G9)
- Chỗ: `S2-E1/stories_ac.md:69` (BR-S2E1-06) ↔ `S3-E5/stories_ac.md:291` (BR-S3E5-37), `:287` (BR-S3E5-33 cấm tên trường `ip`).
- Vì sao: đếm theo IP cần lưu IP đâu đó; bản dựng đỏ nếu có cột/khóa nào tên `ip`.
- Sửa: cả hai BR nêu bộ đếm tạm tối đa 1 giờ, ở bộ nhớ hoặc khóa đã băm, không thành bảng (v0.2). Owner xác nhận khi chốt QA-S2E1-03.

### M-04 · Danh sách trường được phép của S3-E5 thiếu mọi thực thể chặng 2, và chặn nhầm khóa ảnh đề · ĐÃ SỬA
- Chỗ: `S3-E5/stories_ac.md:284` (BR-S3E5-30), `:287` (BR-S3E5-33 cấm `image`, `anh`, `photo`), `S2-E3/stories_ac.md:132` (BR-S2E3-09, giáo viên tải ảnh đề).
- Vì sao: BR-S3E5-31 "bản dựng đỏ khi cột/khóa không có trong danh sách": đề, lời giải, `StepCheck`, bài luyện, lượt thử, yêu cầu soát, cờ người soát không có trong danh sách nên bản dựng đỏ ngay khi S2 xong; khóa tải ảnh đề bị mẫu cấm chặn.
- Sửa: BR-S3E5-30 thêm thực thể chặng 2; BR-S3E5-33 thêm ngoại lệ có tên `problem_image` (ảnh đề, không lưu sau xác nhận). v0.2.

### M-05 · Bài đã giao "không sửa được" nhưng hai nơi sửa nó · ĐÃ SỬA
- Chỗ: `S2-E8/stories_ac.md:595` (BR-S2E8-29) ↔ `S2-E5/stories_ac.md:415` (BR-S2E5-26 "Áp dụng cho lớp", đáp số đổi) và `S2-E8/stories_ac.md:597` (BR-S2E8-31 "Xác nhận đúng" thêm dạng chấp nhận).
- Sửa: BR-S2E8-29 nêu đúng hai ngoại lệ có chủ đích, ghi rõ kết quả đã chấm không tính lại (v0.2). Chưa có AC kiểm ranh giới hai ngoại lệ: xem m-06.

### M-06 · Luật "tên hiển thị không giống email/số điện thoại" nằm ở S3, nhưng đăng ký làm ở S2 · ĐÃ SỬA (con trỏ), AC do S3-E5 giữ
- Chỗ: `S3-E5/stories_ac.md:289` (BR-S3E5-35) ↔ `S2-E1/stories_ac.md:70` (BR-S2E1-16 mới).
- Vì sao: S2-E1 (BR-S2E1-02) chỉ có độ dài; đợt chặng 2 nhận tên "0912345678"; kiểm lại ở S3 thành thay đổi hành vi.
- Sửa: thêm BR-S2E1-16 trỏ BR-S3E5-35, nói rõ chạy cùng đợt. Đề xuất: BA thêm 2 AC biên (9 chữ số / "Minh 10A") ở S2-E1 khi chốt; hiện AC giữ ở US-S3E5-03.

### M-07 · "Khi khóa, mọi kênh không chứa lời giải: … trình chiếu" mâu thuẫn Trình chiếu của giáo viên · ĐÃ SỬA
- Chỗ: `S3-E2/stories_ac.md:195` (BR-S3E2-12) ↔ `S2-E7/stories_ac.md:219` (BR-S2E7-11), DQ-09.
- Sửa: BR-S3E2-12 nói rõ Trình chiếu là màn của giáo viên, chỉ xem sau hộp xác nhận, máy học sinh vẫn khóa (v0.2).

### M-08 · Mã lớp `HK-` + 4 ký tự dò được bằng nhiều tài khoản · Đã chốt PO-66 (G4)
- Chỗ: `S3-E1/stories_ac.md:89` (BR-S3E1-03), `:186` (BR-S3E1-14 đếm theo tài khoản học sinh), `S2-E1/stories_ac.md:69` (5 tài khoản mỗi giờ mỗi IP).
- Vì sao: bảng ký tự 32 (bỏ 0, O, 1, I) cho khoảng 1,0 triệu mã; mỗi tài khoản thử 10 lần mỗi 15 phút, mỗi IP tạo 5 tài khoản mỗi giờ: kẻ xấu có thể dò vào lớp của trẻ em (vào được ở trạng thái Chờ duyệt, nhưng giáo viên duyệt từng em có thể tắt, BR-S3E1-06).
- Đề xuất: thêm giới hạn theo IP/tổng (không lưu bảng, như M-03) hoặc mã 6 ký tự; hoặc ép "Giáo viên duyệt từng em" không tắt được. Quyết định an toàn: owner. Đã chốt PO-66 (G4): giới hạn tổng lượt thử theo tài khoản (30 lần/24 giờ, thêm BR-S3E1-17) và bắt buộc giáo viên duyệt từng em (bỏ công tắc tắt, BR-S3E1-06/-11; SC4 viết lại); thêm US-S3E1-02-FC8, BV1.

### M-09 · Học sinh không thuộc lớp (Tự học) hoặc Chờ duyệt quên mật khẩu: không có đường khôi phục · Đã chốt PO-66 (G3)
- Chỗ: `S2-E1/stories_ac.md:254` (BR-S2E1-12), `S3-E1/stories_ac.md:366` (BR-S3E1-36) và BR-S3E1-30; câu mở QA-S2E1-04.
- Vì sao: spec đã ghi là câu mở với mặc định "tạo tài khoản mới"; nhưng đó là hành vi mất bài làm của trẻ em, nên nhắc lại ở danh sách owner. Đã chốt PO-66 (G3): cấp mã khôi phục cho cả học sinh Tự học/Chờ duyệt (BR-S2E1-05/-10/-12; US-S2E1-03-SC4, FC2, FC4); QA-S2E1-04 đóng.

### M-10 · Hai đề xuất đổi story map làm lệch bản R2 đã khóa · Đã chốt PO-66 (G5)
- Chỗ: `S3-E1/stories_ac.md:439` (US-S3E1-05 "đề xuất thêm": S3 thành 21 story thay vì 20); `S2-E1/questions.md` QA-S2E1-07, QA-S2E1-09 (đề xuất đổi story map).
- Vì sao: story map khóa 26/11, đổi là CR. Spec đã viết AC và TC cho US-S3E1-05 (10 AC) nên coi như đã nhận; cần CR chính thức để số liệu R3 (35 + 20) khớp.
- Đề xuất: PO ghi CR vào `cr_register.md` hoặc yêu cầu bỏ story. Không sửa được trong spec: đổi story map là quyết định sản phẩm Đã chốt PO-66 (G5): nhận cả hai. Story map v0.2 thêm US-S3E1-05, US-S3E2-04, US-S3E5-05 (chặng 3 thành 23 story); tick ở S2-E1 trỏ văn bản US-S3E5-02, xóa tài khoản ở US-S3E5-05; QA-S2E1-07, QA-S2E1-09 đóng.

## 3. Minor

| Mã | Chỗ | Vấn đề | Đề xuất | Trạng thái |
|---|---|---|---|---|
| m-01 | mọi epic có mã mẫu (12 mã) | mã mẫu `HS-4P9T-31WD`… chứa 0, 1, O, I, trái BR-S2E1-04 (bảng ký tự bỏ 4 ký tự đó) | đổi sang ký tự hợp lệ (0→8, 1→7, O→Q, I→J) | ĐÃ SỬA (31 tệp, tăng phiên bản; ví dụ `HS-4P9T-31WD` thành `HS-4P9T-37WD`) |
| m-02 | `S3-E1/stories_ac.md:282` BR-S3E1-20 | có 4 trạng thái (thêm Bị từ chối) trong khi story map và Ch3 §8 chỉ có 3 | PO xác nhận thêm "Bị từ chối" (đã dùng ở S3-E3/E4) | Đã chốt PO-66 (G5) |
| m-03 | `S2-E8/stories_ac.md:160` BR-S2E8-08 | so sánh tương đối 1e-9 vô nghĩa khi đáp số mẫu = 0 | thêm sai lệch tuyệt đối khi bằng 0 | ĐÃ SỬA (BR); thiếu AC biên đáp số 0, CÒN MỞ phần AC |
| m-04 | `S2-E7/stories_ac.md:219` ↔ `S3-E6/stories_ac.md:70` | hộp hỏi "Lớp sẽ thấy lời giải trên màn chiếu" hai câu chữ khác nhau (MSG-S3-91 thêm câu thứ hai) cho cùng điều kiện | một câu chữ duy nhất, dẫn MSG chung | CÒN MỞ |
| m-05 | `S2-E5/stories_ac.md:160` BR-S2E5-08 | dung sai tuyệt đối 0,01 cho mọi giá trị bước; bước có số lớn (ví dụ diện tích 12 345,678 làm tròn) bị "Lệch" oan | dung sai theo cả tuyệt đối và tương đối (QA-S2E5-02 mở) | CÒN MỞ |
| m-06 | `S2-E8` US-S2E8-05, `S2-E5` US-S2E5-05 | chưa có AC cho hai ngoại lệ của snapshot (M-05): "Áp dụng cho lớp" đổi đáp số đã snapshot, "Xác nhận đúng" thêm dạng chấp nhận | thêm 2 AC + 2 TC | CÒN MỞ |
| m-07 | `S2-E8/stories_ac.md:490` (US-S2E8-03-SC10), `S2-E6/stories_ac.md:85` | "cùng dùng được", "danh sách đổi nhanh" không kiểm được bằng máy | thay bằng số: phím cao tối thiểu 44 điểm (BR-S2E8-04), thời gian đổi danh sách dưới N ms | CÒN MỞ |
| m-08 | DQ-13 ↔ BR-S2E8-21 | DQ-13 viết "x câu đúng / y câu cần xem lại", BR viết "x câu đúng · y câu cần xem lại" | thống nhất dấu "·" và cập nhật DQ | CÒN MỞ (Design ngoài phạm vi) |

Ghi chú: m-08 thuộc `Design/` nên chỉ ghi nhận, không chặn.

## 4. Đã đối chiếu và không thấy lệch
- Định dạng mã người dùng `GV-xxxx-xxxx` / `HS-xxxx-xxxx`, mã khôi phục `RC-xxxx-xxxx-xxxx`, mã lớp `HK-xxxx` giữa DQ-16/23, S2-E1, S3-E1 và dữ liệu mẫu (sau m-01).
- Khóa 10 lần sai / 15 phút theo tài khoản (PO-53): S2-E1 (đăng nhập, khôi phục) và S3-E1 (mã lớp) khớp nhau, đều theo tài khoản.
- Giáo viên không đặt lại mật khẩu của giáo viên (BR-S3E1-36) ↔ khôi phục bằng mã (S2-E1-03): khớp.
- Ngưỡng nhận định gộp 5 bài (PO-65 Q6, DQ-22) khớp BR-S3E3-30 và biên 4/5/6 ở US-S3E3-03-SC2…SC4; lớp dưới 5 em (SC10); một em nhiều lần (SC11).
- Biên C2-03 (S2-E5): 1/30/31 bước, 2000/2001 ký tự, dung sai 0,01/0,011, 10/11 giây đều có AC; C2-06 (S2-E8): 50/51 ký tự, 15/16 chữ số, sai số 0 và biên ±0,5, lượt 1/5/0/6, sai số 0/10/âm/10,01, hết lượt, bấm đôi: đủ. Còn thiếu (m-03, m-06).
- Không còn nhắc `TeacherInvite`, `TeacherVerification`, "chờ xác nhận" (PO-61/62/63): `grep` 0 kết quả ngoài các dòng giải thích bỏ.
- Nhãn hai trục (PO-02, PO-38): chỉ engine gắn "Đã kiểm chứng" nhất quán ở S2-E5 BR-12/15, S3-E3 BR-33, S2-E3 BR-18.

## 5. Đã chốt PO-66 (trước đây "cần owner")
Owner trả lời `OK hết` cho `.claude/GATE-T16.md` v0.1 ngày 26/11/2026; quyết định ghi ở PO-66 và DECISIONS "S23 R3 OK".
1. B-1: Đã chốt PO-66 (G1), một mô hình bộ bài, chặng 2 giao cho lớp mẫu 11A1.
2. M-01: Đã chốt PO-66 (G2), giáo viên xem ngay, học sinh tự học khóa lời giải, chỉ gợi ý.
3. M-08: Đã chốt PO-66 (G4), giới hạn tổng lượt thử theo tài khoản và bắt buộc giáo viên duyệt từng em.
4. M-09: Đã chốt PO-66 (G3), mã khôi phục cho cả học sinh Tự học/Chờ duyệt.
5. M-10, m-02: Đã chốt PO-66 (G5), nhận US-S3E1-05, US-S3E2-04, US-S3E5-05, trạng thái Bị từ chối, xóa tài khoản và chính sách lưu/xóa, vòng đời bộ Đã đóng/Lưu trữ.
6. Giả định thêm ở lượt soát: M-02 (BR-S3E1-16), M-03 (bộ đếm IP tạm), M-04 (khóa `problem_image`): Đã chốt PO-66 (G9).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 1 blocker, 10 major, 8 minor; sửa 6 major và 2 minor trong spec | runner-T-261126-15 (QC) |
| v0.2 | 2026-11-26 | M-10 chuyển "Còn mở" sang nhóm chờ owner (cần CR, không sửa được trong spec); cập nhật bảng tổng và mục 5 | runner-T-261126-15 (QC) |
| v0.3 | 2026-11-26 | Áp PO-66 (T-261126-37): mọi mục "cần owner" thành "Đã chốt PO-66 (G…)"; cập nhật bảng tổng, mục 5 và số liệu (58 story, 746 AC, 751 TC) | runner-T-261126-37 (BA/QC) |
