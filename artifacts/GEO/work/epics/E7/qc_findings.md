# E7 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E7-01 | To BA | US-GEO-703-SC5; D-GEO-059 | Ranh giới phiên chỉ có ca 35 phút (hai phiên) và 10 phút (một phiên). Đúng 30 phút không có sự kiện thì một hay hai phiên chưa được nói ("kết thúc khi 30 phút không có sự kiện") | Không viết case biên đúng 30 phút (không tự đặt kỳ vọng). Đề xuất BA thêm cặp biên 29 phút 59 giây và 30 phút 0 giây với kỳ vọng do PO chọn | BA, PO · R3 của E7 |
| QF-E7-02 | Fixed | US-GEO-704-SC3 | Tiêu chí đọc 100 sự kiện theo trang 50 nhưng không có ca hai sự kiện trùng mốc thời gian ở ranh giới trang (ca bắt buộc theo brief §7) | TC-GEO-0739 viết tay, trỏ US-GEO-704-SC3 và NFR-GEO-046; dữ liệu `F-GEO-201` | QC · xong |
| QF-E7-03 | Fixed | US-GEO-701-SC1…SC4 | Số của bốn thẻ và năm nhãn tình trạng dựa trên công thức suy từ khung (D-GEO-077) và phán quyết D-GEO-102 | Case PROVISIONAL theo mã của tiêu chí; đã soát: 4/5, 3, 7 = 3 + 2 + 2, (18 + 11 + 9 + 21 + 0) / 5 = 11,8 → 12′ | GiaoVienCoVan (qua PO) · R3 của E7 |
| QF-E7-04 | Question (PO, BA đã hỏi) | US-GEO-701-SC6; QA-GEO-38 | Bản đồ story không có story cho danh sách bài đã giao của giáo viên, đường duy nhất vào Theo dõi lớp và thẻ vàng; BA viết tạm trong 701-SC6 | TC-GEO-0705 PROVISIONAL QA-GEO-38; nếu PO mở story mới thì thêm case trong dải 0740…0799 | PO · R3 của E7 |
| QF-E7-05 | Fixed | US-GEO-703-FC2, FC3; NFR-GEO-051, 052 | Mất sự kiện không bền khi máy chủ CSDL sập và đối soát bộ đếm là mức ARCH đề xuất, chưa được chủ đầu tư chấp nhận | Không viết case; `work/backlog_cases.md` dòng 2, 18 | QC · xong |

## Kết quả đối chiếu
- Chiều xuôi: 39 / 39 tiêu chí có case (100%). Chiều ngược: 40 / 40 case trỏ về tiêu chí của BA (100%); 1 case viết tay (TC-GEO-0739).
- Giá trị đã soát với `02_domain_model.md`: M-GEO-15, 21, 22, 23 trên `F-GEO-050…054`; M-GEO-08 = 4/4 = 100,0% và 4/5 = 80,0%; bốn nhóm câu hỏi 2 + 3 + 1 + 1 = 7 lượt khớp M-GEO-22. Không thấy mâu thuẫn.
- Ca bắt buộc: học sinh không vào được Theo dõi lớp (TC-GEO-0710); không lẫn lớp khác, trường khác (0719, 0731, và TC-GEO-0132 của E1); trang không mang lời giải (0711, DT-9); phân trang theo con trỏ không lặp không sót kể cả trùng mốc (0734, 0739); không quét tuần tự bảng lớn (0735); bộ kiểm truy vấn bắt OFFSET và thiếu cận thời gian (0736); giáo viên xem trên điện thoại (0706).

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E7-01 | Fixed (D-GEO-144) | US-GEO-703-SC5; RV-GEO-17 | Biên 30 phút | Thêm TC-GEO-0740 (29 phút 59 giây: một phiên, 40 phút), TC-GEO-0741 (30 phút 0 giây: hai phiên, 10 phút), kiểm cả hàm tính và truy vấn CSDL; BA còn thêm tiêu chí | BA · R3 của E7 |
| QF-E7-06 | Fixed | US-GEO-703-FC2; RV-GEO-18 | Gửi lại sự kiện bền (câu hỏi) có thể cộng đôi | Thêm TC-GEO-0742: thẻ "Câu hỏi" 7 → 8, cột Hỏi đáp của An 3 → 4 | ARCH (khóa lũy đẳng) · R3 của E7 |
| QF-E7-04 | Fixed (D-GEO-129) | US-GEO-701-SC6 | Đã phán giữ trong 701 | Chờ BA đổi PROVISIONAL và thêm tiêu chí "không có Tổng quan, Lớp học" | BA · R3 của E7 |
| QF-E7-07 | To BA | US-GEO-703 (D-GEO-162, 165) | Sự kiện `open` bền, sự kiện `scene_shown` chưa có tiêu chí | QC thêm case khi BA bàn giao | BA · R3 của E7 |

- Đối chiếu sau lượt sửa: 39 / 39 tiêu chí có case (100%); 43 / 43 case trỏ về tiêu chí của BA.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- TC-GEO-0740 → 703-SC8, 0741 → 703-SC9 (giữ id, theo dữ liệu của BA); mới: 0743 (703-SC7, `open` bền), 0744 (701-FC7). QF-E7-04, 07 đóng.
- Đối chiếu cuối: 43 / 43 tiêu chí có case (100%); 45 / 45 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E7-08 | Fixed | mọi case của E7 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E7_auto.txt`. Kết quả: E2E-UI 18 / E2E-API 16 / IT 11 / Manual 0 | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa". Không case nào bị bỏ.
- Đối chiếu: 43 / 43 tiêu chí có case (100%); 45 / 45 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.

## Dev (E7, Đợt 1, nhánh mvp-e7, 25/11/2026)

| Id | Trạng thái | Case / tiêu chí | Phát hiện | Cách xử lý ở mã | Ai |
|---|---|---|---|---|---|
| Dev-E7-01 | Open (BA) | US-GEO-702-SC4, TC-GEO-0716 | Then của SC4 liệt kê bốn nhóm theo thứ tự hỏi lần đầu, còn BR-GEO-275 ghi "nhóm có câu hỏi mới nhất ở trên" | Làm theo BR-GEO-275 (mới nhất ở trên); test kiểm đủ bốn nhóm, tên, số lượt và thứ tự theo BR | Dev |
| Dev-E7-02 | Open (QC) | TC-GEO-0723, 0743 ("máy chủ ứng dụng bị khởi động lại") | E2E không khởi động lại được máy chủ giữa ca | Sự kiện bền ghi đồng bộ trong cùng giao dịch CSDL (fn_get_hint, fn_record_durable); test giả lập khởi động lại bằng móc /test-hooks/e7/restart (bỏ đệm Theo dõi lớp), đọc lại chỉ từ CSDL, kiểm đúng 1 sự kiện | Dev |
| Dev-E7-03 | Fixed | TC-GEO-0729 (US-GEO-703-FC3) | Lô thao tác gửi lỗi khi mất mạng chỉ được gửi lại khi có thao tác mới hoặc rời trang, nên không đạt "tính trong vòng 10 giây sau khi có mạng" | E3 useToolEvents: gửi lại khi có sự kiện `online` và mỗi 5 giây khi còn lô chờ (cùng mã lô, không ghi trùng) | Dev |
| Dev-E7-04 | Open (QC) | TC-GEO-0727 | Oxyz, góc nhìn, Đặt lại, xoay, thu phóng không phát sự kiện ở bộ xem (đã kiểm bằng trình duyệt ở TC-GEO-0309) nên ở tầng API không có gì để gửi | Test API: gửi thẳng mã ngoài 1…7 thì VALIDATION_FAILED, số thao tác vẫn 0 | Dev |
| Dev-E7-05 | Open (BA) | US-GEO-701-SC5, BR-GEO-269 | Thẻ vàng dùng chung của E6 (ReleaseCard, US-GEO-605) chỉ ghi "Mở lúc …", không có "· còn {thời gian}" | Theo dõi lớp hiện "còn 1 giờ 0 phút" ngay dưới thẻ của E6; không sửa thẻ của E6 | Dev |
| Dev-E7-06 | Fixed (FIX4b M1) | US-GEO-701-SC5, BR-GEO-268, 269; soát E7 M1 | Bấm "Mở ngay"/"Đổi giờ" trên Theo dõi lớp: thẻ E6 tự đọc lại nhưng dữ liệu trang (thẻ khóa, "còn …", Tình trạng) giữ bản cũ; đệm 5 s phía máy chủ chỉ so trạng thái mở | ReleaseCard nhận `onChanged`, MonitorPage tải lại; LockSlot giữ một khung (không dựng lại thẻ E6); đệm so cả releaseAt. Test: e2e `e7/fix4b.ui.spec.ts` (2 ca bấm thật), IT `MonitorFix4bE7IT.devE706` | Dev |
| Dev-E7-07 | Fixed (FIX4b M2) | BR-GEO-278, US-GEO-703-SC5/SC9; soát E7 M2 | Một lô cộng nguyên khoảng min…max vào active_seconds (lô 2 sự kiện cách 23 giờ: +23 giờ) | V85: fn_record_batch tính theo từng cặp liên tiếp, chỉ cộng khoảng < 30 phút (`_progress_apply_batch`). Test: IT `BatchFix4bE7IT.devE707_*` (lô cách 23 giờ = 0; T0/+10′/+50′/+55′ = 15 phút) | Dev |
| Dev-E7-08 | Fixed (FIX4b M3) | BR-GEO-280, NFR-GEO-052; soát E7 M3 | Lũy đẳng lô chỉ ở Redis (pending 2 phút): khóa mất/hết hạn thì gửi lại bị cộng đôi | V85: batch_id vào durable_request (ON CONFLICT DO NOTHING) cùng giao dịch với event_log, trả DUPLICATE; Redis chỉ là đường nhanh "đã ghi", bỏ khóa pending (khóa chính tuần tự hóa gửi đồng thời). Test: IT `BatchFix4bE7IT.devE708_*` (xóa khóa Redis rồi gửi lại; 4 lần gửi đồng thời) | Dev |
| Dev-E7-09 | Fixed (FIX4b m1–m8) | soát E7 minor | m1 lô E3 kẹt/thử lại vô hạn khi 4xx; m2 E7Sandbox* thiếu @Profile; m3 cắt cụt im lặng (101 em, 2000 câu cũ nhất); m4 thẻ "câu hỏi" và khối "đang hỏi gì" khác nguồn; m5 đọc sự kiện thiếu test quyền, userId ngoài lớp trả 200; m6 báo cáo phiên cắt 200 000 im lặng; m7 luật EVENT_TIME_BOUND thỏa bởi `occurred_at >` ngoài WHERE; m8 gộp route đè index, overview đếm màn bị thay, không tự làm mới, from/to không trần | m1 `toolRetry.ts` (chỉ mạng/5xx/408/429, tối đa 12 lần; 4xx bỏ lô) + sự kiện mới không chờ lô cũ; m2 @Profile dev/test cho E7SandboxService/Repository và SandboxRepository (E4) + luật ArchUnit `testHooksAreProfileScoped`; m3 trần 100 em/2000 câu, giữ câu MỚI nhất, cờ `studentsTruncated`/`questionsTruncated` + dòng báo trên trang; m4 quy ước: chỉ câu hỏi của học sinh CÒN trong lớp (cùng nguồn với cột Hỏi đáp và thẻ); m5 404 khi userId không thuộc lớp + e2e `e7/fix4b.api.spec.ts` (học sinh 403, GV lớp khác/trường khác 404); m6 vượt trần thì báo lỗi, ghi "chưa phơi ra"; m7 luật chỉ nhận cận sau WHERE/AND/ON (+ca SqlLintE7Test); m8 `routeMerge.ts` (+test), tự làm mới 30 s khi trang hiện, khoảng from/to tối đa 366 ngày | Dev |
| Dev-E7-10 | Open (BA) — không sửa | BR-GEO-267 "xếp theo tên A → Z"; soát E7 m8 | SQL xếp theo `name_key` toàn họ tên; dữ liệu vàng toàn tên đơn nên chưa phân biệt "tên gọi" hay "họ tên" | Không sửa: cần BA xác nhận. Cũng không sửa: m5 bài đã lưu trữ vẫn xem được qua id (giữ dữ liệu cho giáo viên; danh sách chỉ bài đang dùng); m6 chỉ mục cho báo cáo phiên (chưa có điểm cuối); m7 móc "restart" chỉ xóa đệm `mon` (giới hạn đã ghi ở Dev-E7-02; lũy đẳng sau mất khóa Redis nay kiểm ở Dev-E7-08); m8 `+07:00` không mã hóa trong URL là lỗi phía gọi (Instant nhận offset khi `+` được mã hóa %2B) | Dev |
| Dev-E7-11 | Fixed (FIX4b, làm hộ E1) | TC-GEO-0110 (E1) | Ca "Phiên hết hạn" chập chờn (webkit, 1/3 lần): danh sách Bài của em (E6) tự hỏi lại mỗi 5 giây, lượt hỏi với thẻ hết hạn nhận 401 và chuyển trang trước cú bấm nên chờ phản hồi của cú bấm hết giờ | Test chấp nhận 401 đầu tiên của yêu cầu học sinh (bấm hoặc lượt hỏi lại), giữ nguyên kiểm UNAUTHENTICATED, chuyển về Vào lớp, MSG-02, vào lại; 80/80 qua 4 lần lặp × 2 trình duyệt | Dev |
| Dev-E7-12 | Fixed (FIX tích hợp chặng 3, 27/11/2026) | TC-GEO-0744, US-GEO-701-FC7, BR-GEO-307 (D-GEO-129) | Khi chạy cùng S3-E1 và S3-E4, ca trượt mọi project: thanh bên GV nay có "Lớp học" (S3-E1, màn Lớp học) và "Tổng quan" (S3-E4 US-S3E4-01). D-GEO-129 chỉ chốt "không hiện ở MVP"; chặng 3 cố ý thêm, không phải lỗi giao diện | Sửa test theo spec mới: vẫn có "Theo dõi"; "Tổng quan" đúng một mục và là của S3-E4 (`s3e4-nav-tong-quan`, không có mục khung `gv-nav-overview`); "Lớp học" đúng một mục của S3-E1 (`s3e1-nav-lop-hoc`). Đề nghị BA ghi chú FC7 "chỉ MVP". Cùng lượt: thanh dưới điện thoại (≤ 720 px) cuộn ngang bên trong thanh, mục không co (base.css) vì ~15 mục làm trang tràn ngang và che nút (TC-GEO-0112, TC-S3E4-14, release/monitor trên mobile-pixel) | Dev |
