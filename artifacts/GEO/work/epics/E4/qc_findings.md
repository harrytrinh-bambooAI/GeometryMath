# E4 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E4-01 | Fixed (BA đổi US-GEO-404-FC1 sang `FORBIDDEN`, M-210) | US-GEO-404-FC1 so với US-GEO-405-FC2, 404-FC2 | Cùng một loại hành vi "chưa tới lượt được xem" nhưng hai `code` khác nhau: xin Gợi ý 2 khi chưa xem Gợi ý 1 → `VALIDATION_FAILED`; xin lời giải khi chưa đủ gợi ý → `FORBIDDEN`. Theo M-402 (b), `FORBIDDEN` là "thấy được tài nguyên nhưng không được làm hành động" | Case theo đúng tiêu chí đang viết (TC-GEO-0424: `VALIDATION_FAILED`). Đề xuất BA chọn một `code` cho cả hai, hoặc ghi lý do khác nhau | BA · R3 của E4 |
| QF-E4-02 | Fixed (BA bỏ vế "45°" ở US-GEO-411-FC2, M-210) | US-GEO-411-FC2; US-GEO-412-FC1 | "Không có đáp số 45° của bài": bài giảng "Góc giữa đường thẳng và mặt phẳng" có thể dùng 45° làm ví dụ chung; kiểm bằng chuỗi sẽ báo sai, kiểm bằng "đáp số của bài" thì không tự động được | TC-GEO-0474, 0479 kiểm theo chuỗi của tiêu chí. Đề xuất BA ghi ở `fixture.md` (`F-GEO-090`, `F-GEO-091`) rằng nội dung 10 bài giảng mẫu không chứa "45°", hoặc đổi kỳ vọng thành "không nhắc đề và các bước `F-GEO-037…039`" | BA · R3 của E4 |
| QF-E4-03 | Fixed | US-GEO-409-SC1, SC2; US-GEO-402-SC1; US-GEO-405-FC4 | Kỳ vọng về nội dung câu trả lời, chữ đọc từ ảnh phụ thuộc AI; ở MVP là nội dung soạn sẵn của nhà cung cấp giả lập (D-GEO-047) | Remark của TC-GEO-0409, 0433, 0454 ghi rõ chỉ kiểm hành vi quanh AI (dòng nguồn, lớp Kiểm soát đáp án), không chấm chất lượng | QC · xong |
| QF-E4-04 | Fixed (BA thêm US-GEO-406-SC6, M-210; TC-GEO-0481 nay trỏ SC6) | US-GEO-406 (thiếu một nhánh) | Tiêu chí kế thừa khóa chỉ nêu bài giao Sắp mở (SC1) và Đã mở (SC2, SC3). Thiếu nhánh bài giao Chưa đặt giờ (BG-2), suy ra được từ D-GEO-026 và D-GEO-043 | TC-GEO-0481 viết tay (dòng DT-8, cột Chưa đặt giờ), trỏ US-GEO-406-SC1, FC1 và D-GEO-043. Đề xuất BA thêm một tiêu chí hoặc mở rộng Given của SC1 | BA · R3 của E4 |
| QF-E4-05 | Question (ARCH, BA đã hỏi) | US-GEO-406-SC5; QA-GEO-10 | Quy tắc "gần trùng" chưa có; tiêu chí dùng đề đổi tên điểm `F-GEO-063` | TC-GEO-0438 PROVISIONAL QA-GEO-10; kỳ vọng có thể đổi theo câu trả lời của ARCH và phán quyết của PO | ARCH, PO · R3 của E4 |
| QF-E4-06 | Fixed | US-GEO-410 | Phân trang nhật ký hỏi đáp không lặp không sót: học sinh hiếm khi có nhiều hơn một trang câu hỏi; kích thước trang chưa có (QA-GEO-04) | Không viết case; ghi ở `work/backlog_cases.md` dòng 11. Con trỏ hỏng đã có TC-GEO-0468 | QC · xong |
| QF-E4-07 | Question (PO, BA đã hỏi) | US-GEO-403-SC3; QA-GEO-32 | Màn khám phá hình mẫu không có tab "Gợi ý", "Lời giải" là mặc định của BA | TC-GEO-0417 PROVISIONAL QA-GEO-32 | PO · R3 của E4 |

## Kết quả đối chiếu
- Chiều xuôi: 82 / 82 tiêu chí có case (100%; BA thêm US-GEO-406-SC6 sau M-210). Chiều ngược: 82 / 82 case trỏ về tiêu chí của BA (100%); 1 case viết tay (TC-GEO-0481).
- Giá trị đã soát: đáp số `F-GEO-060` 60/√769 ≈ 2,1637 (1/d² = 1/9 + 1/16 + 1/25 = 769/3600); trung vị của 10, 20, 60 là 20,0 (M-GEO-09); mốc T0 + 30, + 60, + 120 giây của US-GEO-406-FC2 khớp ví dụ 4 của `02_domain_model.md` §2. Không thấy mâu thuẫn với công thức.
- Ca bắt buộc: tự học không là đường vòng (TC-GEO-0434, 0438, 0439, 0440, 0481); lời giải tự học chặn ở máy chủ (0431, 0432); hỏi đáp không mở trước lời giải (0433); gợi ý và bài giảng không mang đáp số (0426, 0474, 0479).

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E4-08 | Fixed (D-GEO-150) | US-GEO-406-SC5; RV-GEO-01 | Đề gần trùng chưa có quy tắc nên cổng 0 lần lộ không đạt được | TC-GEO-0438 là ca chặn của cổng phát hành; thêm biến thể đổi a thành 2a (0482), viết lại câu chữ (0483) và cặp đảo khác đại lượng hỏi (0484) với dữ liệu F-GEO-233…235 | QC · xong |
| QF-E4-09 | Fixed (D-GEO-151) | US-GEO-406; RV-GEO-03 | Chưa có ca dán đề trước, giáo viên giao sau | Thêm TC-GEO-0485, 0486 (kiểm ở mỗi lần xin); 0486 giả định lời giải tự học đã xem cũng bị khóa lại, đã hỏi PO | QC · xong; PO · R3 của E4 |
| QF-E4-10 | Fixed (D-GEO-155, 145) | US-GEO-406-SC1, 404-SC1; RV-GEO-06, 25 | Gợi ý của bài tự học kế thừa khóa; tiêu đề không lưu đệm của phản hồi gợi ý | Thêm TC-GEO-0487 (gợi ý đề kho nguyên văn, không gọi AI sinh gợi ý), TC-GEO-0488 (`Cache-Control: no-store`) | QC · xong |
| QF-E4-11 | Fixed (D-GEO-169) | US-GEO-406-FC2; RV-GEO-24 | Bước "giao dịch thử" không tự động hóa được | TC-GEO-0440 chuyển sang tích hợp API, chờ đồng hồ thật, T0 lấy từ tiêu đề `Date`, kiểm tại R′ + 2 giây | QC · xong |
| QF-E4-12 | To BA | US-GEO-406 (D-GEO-150, 151, 155), 409, 410 (D-GEO-159), 403-SC3 (D-GEO-136), 412 (D-GEO-137) | Tiêu chí còn dẫn QA-GEO-10, 32; chưa có tiêu chí "dán trước, giao sau", gợi ý đề kho, đề không dựng được hình | QC đã có case theo D-GEO; nối mã tiêu chí khi BA bàn giao | BA · R3 của E4 |
| QF-E4-05 | Fixed (D-GEO-150) | US-GEO-406-SC5 | Đã có quy tắc gần trùng | Xem QF-E4-08 | — |

- Đối chiếu sau lượt sửa: 82 / 82 tiêu chí có case (100%); 89 / 89 case trỏ về tiêu chí của BA.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- TC-GEO-0482 → 406-SC7, 0485 → 406-SC9, 0487 → 406-SC10, 0488 → 404-FC5 (giữ id, chuyển thành case bám tiêu chí); mới: 0489 (406-SC8), 0490 (406-FC3), 0491 (406-FC4), 0492 (406-FC5), 0493 (410-SC5). Case viết tay còn lại: 0481, 0483 (viết lại câu chữ), 0484 (khác đại lượng hỏi), 0486 (đã xem rồi giáo viên giao, trỏ 406-SC9). QF-E4-12 đóng.
- Đối chiếu cuối: 91 / 91 tiêu chí có case (100%); 94 / 94 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).
- Sau M-306, M-220, M-221 (D-GEO-173…183): mới TC-GEO-0494 (412-SC4, D-GEO-181); TC-GEO-0486 nối vào US-GEO-406-FC6 (M-222). Đối chiếu: 93 / 93 tiêu chí, 95 / 95 case.

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E4-13 | Fixed | mọi case của E4 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E4_auto.txt`. Kết quả: E2E-UI 59 / E2E-API 29 / IT 6 / Manual 1 (Manual: TC-GEO-0411, lý do ở từng case) | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa". Không case nào bị bỏ.
- Đối chiếu: 93 / 93 tiêu chí có case (100%); 95 / 95 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.

## Dev (E4, Đợt 1; 23-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E4-D01 | Question | US-GEO-406-FC6, TC-GEO-0486 | Giờ mở BG-7 "T0 + 30 phút" không chờ được trong E2E | Test thu về T0 + 20 giây (cùng chuỗi Sắp mở → Đã mở), ghi trong test | QC · R3 |
| QF-E4-D02 | Fixed (Dev) | US-GEO-405-FC4, 410-SC5; BR-GEO-02 | Kiểm soát đáp án đầy đủ thuộc E6 (US-GEO-609) chưa có | E4 cài bộ kiểm tối thiểu (AnswerGuardApi, guard_version g1-e4): đáp số mọi dạng E4 nhận ra, kết luận "vế trái = vế phải" của bước, nguyên văn bước; không bắt đáp số một ký tự (F-GEO-116). E6 thay cài đặt; chưa ghi answer_guard_log | E6 |
| QF-E4-D03 | Question | US-GEO-410-SC1 | Tên bài "Hình chóp S.ABCD: tính góc giữa SD và mặt đáy" không có trong dữ liệu (problem.title = mã) | Seed 401_e4 đặt tên ngắn cho sáu đề kho theo mẫu Design/prototype | BA (fixture) · R3 |
| QF-E4-D04 | Question | MSG-GEO-35 | Chưa xem gợi ý nào thì "{gợi ý cao nhất em đã xem}" rỗng | Dùng MSG-GEO-34 "Em xem Gợi ý 1 trước nhé." làm phần đuôi | BA · R3 |
| QF-E4-D05 | Fixed (Dev) | TC-GEO-0346 (E3) | Tab Gợi ý của E4 hiện sẵn gợi ý đã xem (US-GEO-404-SC4); An dữ liệu vàng đã xem Gợi ý 2 trên BG-1 nên không còn nút | Test E3 chỉ bấm khi còn nút; mức kiểm giữ nguyên (Gợi ý 1 còn sau khi xoay) | — |
| QF-E4-D06 | Question | US-GEO-401-SC1 | "Màn Làm bài của bài tự học" | Sau "Dựng hình" vẫn mở trang kết quả tự học của E2 (hình, đề, nhãn máy), có nút "Làm bài trên hình" sang /hs/lam-bai/:id (tab Gợi ý/Lời giải/Hỏi) | PO · R3 |
| QF-E4-D07 | Question | US-GEO-402; D-GEO-110 | Ảnh F-GEO-066…068 không có tệp; AI giả lập không đọc chữ; ảnh gửi base64 nên ảnh gốc > ~750 KB đã vượt trần 1 MB | E2E dùng PNG giả mang dấu "GEO-FIXTURE:F-GEO-066"; thu nhỏ ảnh ở thiết bị còn mở | PO, ARCH |
