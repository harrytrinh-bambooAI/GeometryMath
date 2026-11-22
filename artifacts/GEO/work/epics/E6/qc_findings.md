# E6 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Conflict · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E6-01 | Conflict (BA ↔ ARCH) | US-GEO-612-SC3 (PROVISIONAL QA-GEO-37) so với ADR-GEO-10 mục 3 | Tiêu chí: giáo viên "Mở ngay" tại T0 + 10 giây thì trang học sinh đang mở hiện lời giải không muộn hơn T0 + 20 giây. Cơ chế ARCH đề xuất: thiết bị chỉ hẹn giờ theo `release_in_seconds` đã biết, còn thăm dò dự phòng 30 giây ± 10 giây. Với "Mở ngay" (giờ mở không biết trước) và với đổi giờ sớm hơn, thiết bị chỉ biết ở lần thăm dò kế, tức tới 40 giây, lớn hơn 10 giây. D-GEO-074 chỉ nói "sau `release_at`", không tách hai trường hợp | TC-GEO-0689 giữ kỳ vọng 10 giây của tiêu chí, PROVISIONAL QA-GEO-37. Cần ARCH đổi cơ chế (thăm dò ≤ 10 giây hoặc đẩy) hoặc PO nới tiêu chí cho trường hợp "Mở ngay". Đã gửi M-108 | ARCH, PO · R3 của E6 |
| QF-E6-02 | To BA | US-GEO-608-FC5 | "Phản hồi chứa lời giải mang chỉ dẫn không cho lưu đệm dùng chung" chưa có giá trị cụ thể (tên và giá trị của tiêu đề phản hồi) | TC-GEO-0664 kiểm phần quan sát được: Khánh nhận `FORBIDDEN` và không có lời giải. Đề xuất BA ghi giá trị chỉ dẫn lưu đệm (ARCH cho giá trị) | BA, ARCH · R3 của E6 |
| QF-E6-03 | Fixed | US-GEO-608-SC2, FC2; 605-SC7; 406-FC2 | Ca biên t = R, R − 1 giây không lặp lại được nếu chờ đồng hồ thật | Theo M-402 (a): tầng tích hợp CSDL, đặt `release_at` theo `now()` trong một giao dịch thử (TC-GEO-0657, 0661, 0633); thêm TC-GEO-0695 cho R + 1 giây (BV-4, NFR-GEO-023) | QC · xong |
| QF-E6-04 | Fixed (QC tự sửa) | `review/QC_test_design.md` §6; US-GEO-610-SC1, 611-SC1; D-GEO-103 | Bảng bộ câu thử bản đầu của QC ghi "20 câu, 10 nhóm" nhưng thực có 13 nhãn nhóm, một câu là ca đối chứng, và ba câu chưa trùng nguyên văn bộ tối thiểu `F-GEO-095`; không khớp tiêu chí 611-SC1 | Đã sửa bảng: đúng 20 câu, 10 nhóm × 2 câu, năm câu của `F-GEO-095` nguyên văn; ca đối chứng là TC-GEO-0670 | QC · xong |
| QF-E6-05 | To BA | US-GEO-605-FC1, FC3 | Đổi giờ khi Đã mở và hai lần đổi chồng nhau đều trả `VALIDATION_FAILED` (MSG-GEO-48, 49). Đây là xung đột trạng thái chứ không phải dữ liệu nhập sai; máy khách chỉ phân biệt được qua câu chữ | Case theo tiêu chí. Đề xuất hỏi ARCH có `code` riêng cho xung đột trạng thái không (cùng câu hỏi M-104 về `code` quá tải) | BA, ARCH · R3 của E6 |
| QF-E6-06 | Fixed | US-GEO-609, 610 | Với AI giả lập, case chỉ chứng minh lớp Kiểm soát đáp án chặn câu trả lời soạn sẵn có đáp số (`F-GEO-113…115`); không chứng minh mô hình thật không lộ | Remark của TC-GEO-0671, 0676; test plan §16 yêu cầu chạy lại khi nối AI thật | QC · xong |
| QF-E6-07 | Question (BA) | US-GEO-607-SC2 | Màn xem trước hiện thẻ khóa MSG-GEO-28 (câu của bài Chưa đặt giờ); nếu giáo viên xem trước khi đã chọn giờ ở bước 3 thì thẻ nào hiện (PROVISIONAL QA-GEO-35) | TC-GEO-0650 theo tiêu chí | BA, PO · R3 của E6 |
| QF-E6-08 | To BA | US-GEO-607, 605 | Chưa có tiêu chí thiết bị cho giáo viên dùng điện thoại ở bước giao bài, đổi giờ, "Mở ngay" (D-GEO-001: giáo viên kiểm tra nhanh bằng điện thoại) | Không viết case (không có tiêu chí); nếu E7 có tiêu chí điện thoại cho Theo dõi lớp thì thẻ vàng được phủ ở đó | BA · R3 của E6 |

## Kết quả đối chiếu
- Chiều xuôi: 95 / 95 tiêu chí có case (100%). Chiều ngược: 98 / 98 case trỏ về tiêu chí của BA hoặc NFR-GEO (100%); 96 / 98 trỏ về tiêu chí của BA; 2 case loại X (TC-GEO-0860 nhật ký chỉ thêm, NFR-GEO-027; TC-GEO-0861 giới hạn tốc độ, NFR-GEO-056).
- Bảng quyết định khóa lời giải: DT-1 (TC-GEO-0656, 0660, 0662), DT-2 (0665), DT-3 (TC-GEO-0129, 0170 của E1), DT-4 (0650, 0653), DT-5 (TC-GEO-0516 của E5), DT-6 (0667, 0670), DT-7 (TC-GEO-0336…0340 của E3; 0655), DT-8 (TC-GEO-0434, 0435, 0481 của E4; 0677), DT-9 (E7).
- Biên giờ mở: BV-1 (0619, 0660), BV-2 (0661), BV-3 (0657), BV-4 (0695), BV-5 và BV-6 (0633), BV-7 (0630), BV-8 (0635), BV-9 (0663, 0694), BV-10 (0687). Chuyển trạng thái cấm: 0635 (từ Đã mở), 0638 (xóa giờ).
- Giá trị đã soát với `02_domain_model.md` §2: ví dụ 1…8 đều có tiêu chí và case; "Còn 1 giờ 0 phút" khớp R = T0 + 60 phút. Lệch duy nhất giữa các tài liệu: QF-E6-01.

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E6-01 | Fixed (D-GEO-130, 157) | US-GEO-612-SC3 | Đã phán: hỏi lại ≤ 8 giây, đệm ≤ 2 giây | TC-GEO-0689 dẫn D-GEO-157; thêm TC-GEO-0697 đo chu kỳ bằng Playwright; ARCH còn sửa ADR-GEO-10 | ARCH · R3 của E6 |
| QF-E6-02 | Fixed (D-GEO-145) | US-GEO-608-FC5 | Giá trị tiêu đề: `Cache-Control: no-store` | TC-GEO-0664 chỉnh kỳ vọng; thêm TC-GEO-0698 cho lời giải, gợi ý, trạng thái khóa | BA ghi vào 404, 608, 612 · R3 của E6 |
| QF-E6-03 | Fixed (D-GEO-169) | US-GEO-608-SC2, FC2; 605-SC7; RV-GEO-24 | Cách chạy bằng giao dịch thử của M-402 (a) không tự động hóa được | TC-GEO-0657, 0661, 0695: `fn_test_set_release` của profile test, cổng gọi trong cùng giao dịch; tầng API R = giờ máy chủ + 2 giây rồi chờ. TC-GEO-0633 chờ đồng hồ thật | ARCH (hàm) · R3 của E6 |
| QF-E6-05 | Fixed (D-GEO-143) | US-GEO-602-FC1, 605-FC1, FC3 | Mã `CONFLICT` | TC-GEO-0609, 0635, 0637 chỉnh qua `E6_override.txt`; thêm TC-GEO-0696 (giao đồng thời, D-GEO-161) | BA đổi mã trong tiêu chí và từ điển · R3 của E6 |
| QF-E6-07 | Fixed (D-GEO-146) | US-GEO-607-SC2 | Đã phán: thẻ khóa kèm giờ đang chọn | Chờ BA thêm tiêu chí rồi QC thêm case | BA · R3 của E6 |
| QF-E6-08 | Fixed (D-GEO-147) | US-GEO-605 | Đổi giờ và "Mở ngay" trên điện thoại thuộc phạm vi | Chờ BA thêm tiêu chí thiết bị rồi QC thêm case | BA · R3 của E6 |
| QF-E6-09 | Fixed | US-GEO-605-FC1; RV-GEO-28 | Giờ mở trôi qua khi hộp "Đổi giờ" đang mở | Thêm TC-GEO-0699 (`CONFLICT`, MSG-GEO-48 PROVISIONAL); ca R′ = giờ máy chủ + 1 giây chờ tiêu chí của BA | BA · R3 của E6 |
| QF-E6-10 | Fixed (D-GEO-152, 156) | US-GEO-610, 611; RV-GEO-02, 07 | Bộ câu thử thiếu ca giả làm giáo viên; chưa nối quy tắc Kiểm soát đáp án | Thêm nhóm 11 F-GEO-230, 231 và TC-GEO-0862; bảng KS-1…KS-5 ở `review/QC_test_design.md` §6; đề nghị BA đổi 610-SC1, 611-SC1 thành 22 câu, 11 nhóm | BA, ARCH (ADR-GEO-14) · R3 của E6 |
| QF-E6-11 | Fixed (D-GEO-157, 158) | US-GEO-608-FC6, FC7; RV-GEO-09, 10 | Đệm quyết định theo bài giao; quyền ghi rộng của vai CSDL ứng dụng | TC-GEO-0664, 0665 giữ High, 0665 thêm tiền điều kiện đệm đang có; thêm TC-GEO-0863 (năm lệnh ghi thẳng bị từ chối) | QC · xong |
| QF-E6-12 | Fixed (D-GEO-148) | TC-GEO-0861 | Mức Low, PROVISIONAL D-GEO-125 | Đã ghi | QC · xong |

- Đối chiếu sau lượt sửa: 95 / 95 tiêu chí có case (100%); 104 / 104 case trỏ về tiêu chí của BA hoặc NFR-GEO; 102 / 104 trỏ về tiêu chí của BA, 2 case X (0860, 0861).
- Biên giờ mở thêm BV-11 (TC-GEO-0699); bảng quyết định thêm DT-11 (0664, 0665), DT-10 và DT-12 ở E5, E4.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- TC-GEO-0696 → 602-FC4, 0699 → 605-FC5, 0698 → 608-FC8 (giữ id); 0862 trỏ 610-SC6; mới (dải 0860…0879 của E6 vì 0600…0699 đã hết): 0864 (612-FC5), 0865 (602-FC5), 0866 (605-SC9), 0867 (605-SC10), 0868 (605-FC6), 0869 (606-SC7), 0870 (607-SC5), 0871 (607-SC6), 0872 (607-FC4), 0873 (608-SC5), 0874 (609-SC5, KS-3), 0875 (609-FC6, KS-5), 0876 (610-SC5, ca chặn F-GEO-226). Gỡ chỉnh của TC-GEO-0609, 0635, 0637, 0664, 0689 (AC đã đổi). QF-E6-07, 08, 09, 10 đóng.
- Đối chiếu cuối: 112 / 112 tiêu chí có case (100%); 117 / 117 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).
- Sau M-306, M-220, M-221 (D-GEO-173…183): mới TC-GEO-0877 (606-SC8, D-GEO-182); TC-GEO-0862 dẫn D-GEO-176, bỏ PROVISIONAL; TC-GEO-0676 chạy 22 câu. Đối chiếu: 113 / 113 tiêu chí, 118 / 118 case (116 trỏ tiêu chí của BA, 2 case X).

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E6-13 | To BA | US-GEO-609-FC1; D-GEO-184 (M-307); KS-2 | PO chốt KS-2 chặn cả số đã làm tròn tới ≥ 2 chữ số có nghĩa của đáp số; AC của 609 chưa có tiêu chí hay ví dụ số làm tròn | Thêm TC-GEO-0878 ("0,79" và "0,785" cho π/4 trên BG-1 → MSG-GEO-35, dữ liệu trả về không chứa hai số đó), PROVISIONAL D-GEO-184; dữ liệu `F-GEO-241` ở `review/QC_test_design.md` §6. Đề xuất BA thêm một tiêu chí (hoặc một dòng Then) cho số làm tròn; chưa viết case cho số 1 chữ số có nghĩa ("0,8") vì D-GEO-184 không nói | BA · R3 của E6 |
| QF-E6-14 | Fixed | mọi case của E6 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E6_auto.txt`. Kết quả: E2E-UI 61 / E2E-API 38 / IT 18 / Manual 2 (Manual: TC-GEO-0866, 0691, lý do ở từng case) | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa" và case mới nêu trên. Không case nào bị bỏ.
- Đối chiếu: 113 / 113 tiêu chí có case (100%); 119 / 119 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.
