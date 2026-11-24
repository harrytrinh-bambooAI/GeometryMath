# E5 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E5-01 | To BA | US-GEO-505-FC4 | Kỳ vọng "số đề bằng trần của danh sách Kho đề (không bao giờ quá 100), kèm con trỏ trang kế" không có số cụ thể và tự mâu thuẫn tùy trần: kho thử có 48 đề; nếu trần là 50 hoặc 100 thì phản hồi có 48 đề và **không** có con trỏ trang kế; chỉ khi trần là 20 mới có 20 đề kèm con trỏ | TC-GEO-0540 giữ kỳ vọng của tiêu chí, PROVISIONAL QA-GEO-04. Diễn biến: BA sửa thành "đúng 20 đề, trần 20" (M-212); sau đó ARCH báo Kho đề trang 20, **trần 50** (M-403). Hai con số chưa khớp: với trần 50, xin 10000 sẽ nhận 48 đề và không có con trỏ trang kế. Đã gửi lại BA (M-110); case giữ theo tiêu chí hiện tại | BA, ARCH · R3 của E5 |
| QF-E5-02 | Fixed | US-GEO-504-SC2, SC3; `F-GEO-036` | Xung đột dữ liệu đã biết về nhãn `HKG-06` (PO phán D-GEO-105: theo phép tính) | In ở Remark của TC-GEO-0526; case PROVISIONAL D-GEO-105 | QC · xong |
| QF-E5-03 | Question (PO, BA đã hỏi) | US-GEO-501-FC2; US-GEO-503-FC3; QA-GEO-27 | Học sinh gọi Kho đề bị `FORBIDDEN` là mặc định của BA theo đề xuất của QC (M-102 điểm 3); nguồn chỉ nêu Kho đề là màn của giáo viên | TC-GEO-0507, 0522 PROVISIONAL QA-GEO-27 | PO · R3 của E5 |
| QF-E5-04 | Fixed | US-GEO-504-SC4; D-GEO-055 | M-GEO-07 ≥ 40 được kiểm trên kho thử 48 đề nhân từ sáu đề (`F-GEO-080`), không phải nội dung kho thật; nguồn của 34 đến 54 đề còn lại chưa có (KG-05) | TC-GEO-0527 PROVISIONAL D-GEO-055; chạy lại trên kho thật trước khi kết luận tiêu chí xong MVP (REQ-GEO-49) | ChuDauTu · R3 của E5 |
| QF-E5-05 | Fixed | US-GEO-505 | Kế hoạch thực thi của truy vấn danh sách Kho đề (không quét tuần tự) không có tiêu chí riêng ở E5 | Đã có TC-GEO-0802 của E1 (mọi truy vấn nóng, NFR-GEO-042); US-GEO-505-SC5 kiểm không OFFSET và không truy vấn lặp theo dòng (TC-GEO-0536) | QC · xong |

## Kết quả đối chiếu
- Chiều xuôi: 41 / 41 tiêu chí có case (100%). Chiều ngược: 41 / 41 case trỏ về đúng một tiêu chí (100%). Không có case viết tay.
- Giá trị đã soát với `fixture.md`: 48 đề = 20 + 20 + 8; THPT 24 = 20 + 4; THCS + Mặt cầu = 0 đề (`F-GEO-081`); THPT + Góc = 8 đề có `HKG-01` (`F-GEO-082`); 44 đề "Đã kiểm chứng" ≥ 40 (M-GEO-07). Mâu thuẫn tiềm ẩn duy nhất: QF-E5-01.
- Ca bắt buộc: phân trang theo con trỏ không lặp không sót (TC-GEO-0532, 0533, 0534), không tổng số (0535), không OFFSET và không truy vấn lặp theo dòng (0536), con trỏ hỏng (0539); học sinh không lấy được lời giải qua đường của Kho đề (0507, 0522); không nhãn nào sao chép từ tệp nguồn (0525, 0526).

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E5-01 | Fixed (D-GEO-142) | US-GEO-505-FC4 | Trần Kho đề là 50 | TC-GEO-0540 kỳ vọng 48 đề, không con trỏ, không tổng số (qua `E5_override.txt`); BA còn phải đổi tiêu chí và `F-GEO-083` | BA · R3 của E5 |
| QF-E5-03 | Fixed (D-GEO-131) | US-GEO-501-FC2, 503-FC3 | Học sinh gọi Kho đề, chi tiết đề: `FORBIDDEN` | TC-GEO-0507, 0522 đổi dẫn nguồn sang D-GEO-131 | QC · xong |
| QF-E5-06 | Fixed (D-GEO-153) | US-GEO-503-SC2; RV-GEO-04 | Hàm lời giải của giáo viên trả lời giải đề tự nhập của trường khác | Thêm TC-GEO-0541 (giáo viên trường khác → `NOT_FOUND`), TC-GEO-0542 (giáo viên cùng trường với bài tự học → `NOT_FOUND`, đối chứng đề kho) | QC · xong |
| QF-E5-07 | Question (PO) | D-GEO-153; TC-GEO-0542 | Mã trả về khi giáo viên gọi lời giải của đề không thuộc kho trong trường mình: `NOT_FOUND` hay `FORBIDDEN` | Case giả định `NOT_FOUND` (đề không thuộc phạm vi xem của giáo viên coi như không có) | PO · R3 của E5 |
| QF-E5-08 | To BA | US-GEO-503-FC2 (D-GEO-164) | Tiêu chí còn kỳ vọng hai nút hiện, không bấm được | TC-GEO-0521 kỳ vọng không hiện hai nút (qua `E5_override.txt`) | BA · R3 của E5 |

- Đối chiếu sau lượt sửa: 41 / 41 tiêu chí có case (100%); 43 / 43 case trỏ về tiêu chí của BA.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- TC-GEO-0541 → 503-FC6, 0542 → 503-FC5 (giữ id; AC của BA chốt `NOT_FOUND` nên QF-E5-07 đóng nếu PO không phán khác ở M-216). Gỡ mọi chỉnh của `E5_override.txt` (AC đã theo D-GEO-131, 142, 164); QF-E5-08 đóng.
- Đối chiếu cuối: 43 / 43 tiêu chí có case (100%); 43 / 43 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).
- Sau M-306, M-220, M-221 (D-GEO-173…183): TC-GEO-0542 theo D-GEO-175. Đối chiếu: 43 / 43 tiêu chí, 43 / 43 case.

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E5-09 | Fixed | mọi case của E5 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E5_auto.txt`. Kết quả: E2E-UI 23 / E2E-API 11 / IT 9 / Manual 0 | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa". Không case nào bị bỏ.
- Đối chiếu: 43 / 43 tiêu chí có case (100%); 43 / 43 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.

## Dev (E5, Đợt 1; 23-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E5-D01 | Assumption | F-GEO-080; US-GEO-501, 505; TC-GEO-0125 (E1) | Kho thử 48 đề (F-GEO-080) và "Kho đề mọi trường đúng sáu đề" (TC-GEO-0125) không cùng đứng trên một kho nền tảng | V50: cột `problem.bank` + bảng `catalog_bank_access`; mọi trường thấy bộ `main` (kho thật), chỉ tổ chức thử "Nhóm thử E5" (seed `500_e5_catalog.sql`) thấy thêm bộ `kho_thu` (HKG-07…48 nhân từ HKG-01…06). Kho vẫn là nội dung nền tảng (`problem_scope_ck` giữ nguyên) | PO · xác nhận dùng bộ đề khi kho thật có 40–60 đề |
| QF-E5-D02 | Assumption | US-GEO-504-FC1 ↔ US-GEO-106-FC1 (E1) | 504-FC1: đề thiếu đáp số "không vào kho", ghi "HKG-05: thiếu đáp số"; 106-FC1: vẫn thuộc bộ đề chuẩn, ghi "thiếu đáp số tham chiếu" | Bộ nạp E1 giữ nguyên (bộ đề chuẩn, TC-0147); `CatalogLoader` (E5) đưa đề thiếu đáp số ra khỏi Kho đề (`archived_at`) và ghi dòng của E5; nạp lại có đáp số thì đưa vào lại | BA · ghi chú chéo hai AC |
| QF-E5-D03 | Gap | US-GEO-503-FC5 (TC-GEO-0542) Given | "An đã mở được lời giải của bài tự học": máy chủ nhánh này trả `FORBIDDEN "Lời giải chưa mở."` cho bài tự học F-GEO-060 (luồng mở lời giải tự học là E4) | Ca kiểm phía giáo viên (Then) không phụ thuộc bước này; khi gộp E4, thêm bước An mở lời giải vào ca | E4 · khi gộp |
| QF-E5-D04 | Assumption | US-GEO-505-SC2, 502-FC1 | "Một đề mới được nạp vào kho giữa chừng" cần cách nạp trong lúc chạy | Móc kiểm thử `POST /test-hooks/e5/problems` (chỉ profile dev/test) nạp đề "Cần xem lại" mã `HKM-nn` vào bộ `kho_moi`, chỉ "Nhóm thử E5 nạp thêm" thấy | — |
| QF-E5-D05 | Assumption | US-GEO-504-SC6 | Chưa có chỗ lưu phiên bản engine của báo cáo kiểm chứng | Ghi `checks.engine_version` (cấu hình `geo.engine.version`, mặc định `engine-1`); nhãn = báo cáo mới nhất qua `fn_record_verification` | ARCH · cân nhắc cột riêng |
| QF-E5-D06 | Assumption | BR-GEO-204, 207; US-GEO-503-FC1 | Thẻ của đề chưa có lời giải không có số bước k | Thẻ ghi "AI soạn · chưa có lời giải" (vẫn có "AI soạn") | BA · câu chữ |
| QF-E5-D07 | Assumption | F-GEO-080 | Fixture không nói đề nào trong 4 đề "Cần xem lại" | HKG-45…48 (HKG-48 không có lời giải, dùng cho 503-FC1; HKG-45 cho 502-SC2, 503-SC3) | QC · ghi vào fixture |
| QF-E5-D08 | Fixed | màn E1 `/gv/kho-de`, E2 `/gv/de/:versionId`; bộ nạp E1 | Bản tối thiểu của E1, E2; bộ nạp chỉ tạo version mới khi đề đổi chữ | E5 thay bằng `features/e5/KhoDePage.tsx`, `ProblemDetailPage.tsx`, giữ data-testid `e1-…`, `e2-problem-…` nên ca E1, E2 không đổi. Bộ nạp: lời giải đổi (kể cả một bước) thì tạo version mới, bài đã giao giữ version cũ (BR-GEO-11) | — |
| QF-E5-D09 | Gap | US-GEO-503 ↔ E4 | Lời giải đồng bộ hình của E4 chưa có trên nhánh | Khối "Lời giải" là bản đơn giản đọc dữ liệu đề (`SolutionBlock` trong `ProblemDetailPage.tsx`); chỗ cắm để E4 thay | E4 · khi gộp |
| QF-E5-D10 | Fixed (FIX3) | BR-GEO-11, US-GEO-504-SC5; TC-GEO-0528 | Sau gộp E4+E5, nạp lại cùng tệp tạo version mới của HKG-01: E4 (seed 401) thêm khóa trình bày `highlight`, `draw` vào bước, bộ nạp so nguyên jsonb bước | V59: `fn_content_solution_same` so phần chữ của bước (`fn_solution_text`: title, body, why) và đáp số — đúng những gì tệp nguồn mang; TC-0528 đạt, không nới assertion | — |
| QF-E5-D11 | Fixed (FIX3) | BR-GEO-112; US-GEO-501…503; soát E5 #1–5 | Quyền bộ đề chỉ áp ở danh sách/đếm: giáo viên ngoài bộ xem được chi tiết, lời giải, cảnh và giao được đề | V59: `fn_catalog_bank_visible` trong `fn_create_assignment`, `fn_get_solution_teacher`; điều kiện bộ trong `CatalogRepository.detail`, `SceneRepository.catalogVersion`. Guard Java dùng chung `content.api.BankAccess` gọi ở `CatalogService.detail/teacherSolution`, `SceneService.teacherProblem`, `AssignmentService.create` (bắt buộc, ghi ở `app/docs/CONFIG.md` §9). Ca **Dev-E5-01…04** (404 cho chi tiết, lời giải, cảnh, giao bài), **Dev-E5-05** (hàm SQL mất điều kiện → guard Java vẫn 404) trong `it/e5/BankAccessE5IT` | E6 · giữ điều kiện khi định nghĩa lại `fn_create_assignment` |
| QF-E5-D12 | Fixed (FIX3) | soát E5 #6, #7; TC-GEO-0533, BR-GEO-217 | Móc nạp đề cấp mã `count+1` có thể trùng khi chạy song song; TC-0533 chưa chạm ca đề mới chèn TRƯỚC con trỏ | Móc khóa theo bộ (`pg_advisory_xact_lock`) trước khi đếm; nhận `prefix` (HKA sắp trước HKG); TC-0533 nạp thêm một đề `HKA-…` sau trang đầu và kiểm không lặp, không lệch trang | — |
