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
