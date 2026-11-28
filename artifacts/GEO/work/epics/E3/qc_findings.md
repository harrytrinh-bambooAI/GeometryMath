# E3 — QC findings (đối chiếu AC ↔ case, E3 của guide)

> Chủ sở hữu: QC. Bảng đối chiếu hai chiều đầy đủ nằm ở `test_cases.md` §3 (sinh tự động). Loại: Fixed · To BA · Question · Rejected. Cột "Cần trước" là một cổng review, không ghi ngày. Giải thích mã: `test_cases.md` §0.

| ID | Type (Fixed / To BA / Question / Rejected) | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E3-01 | Fixed (BA nhận một phần, M-210: thêm tập đại lượng ẩn của HKG-02, HKG-06; vẫn PROVISIONAL D-GEO-024) | US-GEO-305-FC3; `work/fixture.md` mục 2 | Tập đại lượng ẩn chỉ được liệt kê cho `HKG-01` (góc SDA, góc ASD). Với `HKG-02` (BG-2) đề hỏi d(AC, B′D′) = a: khoảng cách giữa hai mặt đáy và độ dài cạnh bên AA′ có cùng giá trị a; chưa rõ cái nào thuộc "cùng giá trị, cùng vai trò" (D-GEO-024) | TC-GEO-0338 chỉ kiểm đúng đại lượng đề hỏi. Đề xuất BA thêm tập đại lượng ẩn của `HKG-02…HKG-05` vào `fixture.md`, hoặc ghi rõ chỉ `HKG-01` có đại lượng cùng vai trò | BA · R3 của E3 |
| QF-E3-02 | Fixed (BA thêm US-GEO-305-SC6, M-210; TC-GEO-0352) | US-GEO-305-SC4; D-GEO-074 | Tiêu chí kiểm đo được sau "Mở ngay" nhưng không nói học sinh đang mở sẵn công cụ đo thì giá trị hiện khi nào (cần tải lại không). D-GEO-074 nói trang tự cập nhật trạng thái khóa trong 10 giây | TC-GEO-0334 đo sau khi mở lại bài. Đề xuất thêm một vế vào US-GEO-612 hoặc 305-SC4: sau khi trạng thái khóa cập nhật, lần đo kế tiếp hiện số | BA · R3 của E3 |
| QF-E3-03 | Fixed | US-GEO-305-SC1, SC2 | Yếu tố trung gian đo được trước giờ mở (SD = a√2, góc SAD = 90°, SA = a, AD = a) đủ để học sinh tự tính ra đáp số; đây là điều D-GEO-024 cho phép ("vẫn đo được yếu tố trung gian") | Không viết case cấm đo gián tiếp; ghi ở `test_design.md` phần khó kiểm | QC · xong |
| QF-E3-04 | Fixed | mọi case loại thiết bị | Phiên bản iOS và trình duyệt tối thiểu chưa có (KG-09) | 13 case thiết bị mang PROVISIONAL KG-09; chạy trên iPad, iPhone bản mới nhất | TechLead · R3 của E3 |
| QF-E3-05 | Question (PO, BA đã hỏi) | US-GEO-306-SC5; QA-GEO-31 | Khung `mvp-08` vẽ sáu nút công cụ, tiêu chí đòi chín công cụ tới được trên iPhone | TC-GEO-0345 theo tiêu chí (chín công cụ), PROVISIONAL QA-GEO-31; khung khác tiêu chí thì kiểm theo tiêu chí | PO · R3 của E3 |

## Kết quả đối chiếu
- Chiều xuôi: 53 / 53 tiêu chí có case (100%; BA thêm US-GEO-306-FC2 sau M-301 → TC-GEO-0351, và US-GEO-305-SC6 sau M-210 → TC-GEO-0352). Chiều ngược: 53 / 53 case trỏ về đúng một tiêu chí (100%). Không có case viết tay.
- Giá trị đã soát: SD = a√2, góc SAD = 90°, góc SDA = 45° (`fixture.md` mục 2); M(0; 0,5; 0,5) và N(0; 1/3; 2/3) (`F-GEO-076`); đường sinh nón √(2,5² + 4²) = √22,25 ≈ 4,717 (US-GEO-305-SC5). Không thấy mâu thuẫn.
- Ca bắt buộc: công cụ đo không thành đường tắt được kiểm ở cả màn (TC-GEO-0336…0339) và dữ liệu trả về (TC-GEO-0340); thiết bị iPad (0300, 0301, 0310…0312) và iPhone (0341…0346).
- 50 / 53 case cần chạy bằng tay trên thiết bị cảm ứng thật (36 case chỉ có tầng thủ công); phần tính hình học (giao điểm, giao tuyến, trung điểm, cạnh khuất) có thêm tầng đơn vị.

## Lượt sửa gom sau phản biện (M-304, RV-GEO-01…37, D-GEO-125…170)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E3-06 | Fixed (D-GEO-154) | US-GEO-305-FC5; RV-GEO-05 | Ca dữ liệu trả về chỉ tìm chuỗi "45°" trong khi tọa độ đỉnh đủ để tự tính đáp số | PO phán tọa độ là dữ kiện của đề, "lộ" là khi hệ thống hiển thị hoặc trả giá trị đáp số; ghi vào Remark của TC-GEO-0340 | QC · xong |
| QF-E3-07 | To BA | US-GEO-301, 302 (D-GEO-134), 306-SC5 (D-GEO-135) | Tiêu chí còn PROVISIONAL QA-GEO-30, 31; chưa có tiêu chí lệnh "Xóa hết" | QC thêm case khi BA bàn giao tiêu chí mới | BA · R3 của E3 |

- Đối chiếu sau lượt sửa: 53 / 53 tiêu chí có case (100%); 53 / 53 case trỏ về tiêu chí của BA.

### Đồng bộ với bàn giao của BA (M-217, M-218)
- mới: TC-GEO-0353 (301-SC9, "Xóa hết"). QF-E3-07 đóng phần "Xóa hết".
- Đối chiếu cuối: 54 / 54 tiêu chí có case (100%); 54 / 54 case trỏ về tiêu chí có thật của BA hoặc NFR-GEO; 0 case trỏ tiêu chí không tồn tại (bộ sinh và phép đối chiếu độc lập).

## Sinh lại theo AC hiện hành (PO-50, Đợt 0; 22-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E3-08 | Question (TechLead) | Bộ E2E (D-GEO-170, PO-50); 46 case E2E-UI của E3, các case tô hình ở E4 (TC-GEO-0441…0453, 0446) | Kỳ vọng nằm trên khung hình 3D (cạnh nét đứt, đối tượng được tô, góc nhìn, điểm mới nằm trên cạnh): Playwright không đọc được nội dung WebGL nếu ứng dụng không lộ trạng thái cảnh | Đề nghị profile test có móc chỉ đọc trạng thái cảnh (vd. `window.__geoScene`: camera, mức thu phóng, danh sách điểm / đoạn / mặt với tên, kiểu nét, đối tượng đang chọn, tọa độ màn hình của đỉnh) và `data-testid` cho thanh công cụ; phần không đọc được bằng móc thì so ảnh chụp với ảnh mẫu có ngưỡng. Thiếu móc thì các case này chỉ chạy được bằng so ảnh | TechLead · trước khi viết E2E của E3 |
| QF-E3-09 | Fixed | TC-GEO-0300, 0302, 0310…0312, 0315, 0341, 0345, 0346, 0351 | Mặc định "thiết bị → Manual" quá chặt: kéo chuột, lăn chuột, chạm đơn, bố cục và vùng chạm trên iPhone chạy được bằng giả lập của Playwright | Chuyển thành E2E-UI ở `work/src/qc/E3_auto.txt`, ghi rõ phần còn kiểm trên máy thật. Còn Manual: chụm ngón (0301, 0342), chỗ chạm chồng nhau (0314), nút đặt lại và bảng trượt trên iPhone (0343, 0344) | QC · xong |
| QF-E3-10 | Fixed | mọi case của E3 | Case chưa có trường cho biết tự động hóa bằng cách nào; cột "Tầng tự động hóa" cũ ghi E2E trình duyệt = 0 | Thêm cột "Tự động hóa" do bộ sinh suy từ tầng và loại; QC chỉnh từng case ở `work/src/qc/E3_auto.txt`. Kết quả: E2E-UI 46 / E2E-API 3 / IT 0 / Manual 5 (Manual: TC-GEO-0301, 0314, 0342, 0343, 0344, lý do ở từng case) | QC · xong |

- Sinh lại: bộ sinh chạy trên `stories_ac.md` hiện hành cho đúng nội dung tệp cũ (0 case lệch AC); thay đổi là cột "Tự động hóa". Không case nào bị bỏ.
- Đối chiếu: 54 / 54 tiêu chí có case (100%); 54 / 54 case trỏ về tiêu chí của BA hoặc NFR-GEO / D-GEO; kiểm bằng `work/tools/check_ac_tc.py`.

## Dev (E3, Đợt 1; 23-11-2026)

| ID | Type | Where | Finding | Action taken or proposal | Owner · cần trước |
|---|---|---|---|---|---|
| QF-E3-D01 | Note | TC-GEO-0334, 0352 ("Giáo viên mẫu bấm Mở ngay") | Màn giáo viên của E1 chưa có nút "Mở ngay"; bấm trên BG-1 thật sẽ đổi trạng thái của các ca khác chạy song song | Gọi `POST /api/v1/teacher/assignments/{id}/release {openNow}` (điểm cuối E1) trên bài HKG-01 Sắp mở của làn riêng E3 (seed `300_e3_core.sql`, mỗi ca × dự án một lớp) | E5–E7 · nút giao diện |
| QF-E3-D02 | Note | TC-GEO-0159 (E1) × seed E3 | TC-GEO-0159 đếm cứng số lớp (2), học sinh (10) của THPT Mẫu A và số trường (2) | Dữ liệu E3 nằm trong tổ chức riêng kiểu `group` ("Nhóm thử E3"); không đổi seed và test của E1 | QC · ghi nhận |
| QF-E3-D03 | Note | TC-GEO-0305, 0308 (chụm/dang ngón trên iPad) | Playwright không có cử chỉ hai ngón | Phát hai con trỏ cảm ứng (PointerEvent `touch`) vào khung hình; cảm ứng thật vẫn ở TC-GEO-0301, 0342 (Manual) | QC · ghi nhận |
| QF-E3-D04 | Note | TC-GEO-0309, 0313 (E2E-API) | Xoay/thu phóng/đặt lại là thao tác trên trình duyệt | Ca API kiểm phía máy chủ (sự kiện mở bài, `toolCount` qua Theo dõi lớp, lô gửi lại không tính hai lần); phía trình duyệt kiểm `opCount = 0` khi xoay ở TC-GEO-0300 | QC · ghi nhận |
| QF-E3-D05 | Note | TC-GEO-0350 (lăng trụ đáy chữ L) | Kho đề không có hình này | Trang thử `/hs/thu-hinh/lang-tru-l` chỉ có khi bật móc kiểm thử (dev, `VITE_GEO_TEST_HOOKS=1`); vùng tô kiểm bằng `isPointInFill` của phần tử SVG tại điểm mẫu trong/ngoài, kèm ảnh chụp | QC · ghi nhận |
| QF-E3-D06 | Fixed | US-GEO-305-SC6; US-GEO-307-SC3 | (1) Thông báo MSG-GEO-24 che đỉnh S nên lần đo ngay sau đó không nhận chạm; (2) chọn mặt lõm theo trọng tâm đỉnh rơi vào đỉnh lõm | (1) thông báo không nhận chạm (`pointer-events: none`); (2) bộ chọn dùng trọng tâm diện tích các tam giác tô | E3 · xong |
| QF-E3-D07 | Note | TC-GEO-0335 ("đo đường sinh") | Hình mẫu nón của engine E2 có S, O và M trên đường tròn đáy | Đo SM trên `/hs/hinh-mau/cone?r=2,5&h=4` | QC · ghi nhận |
| QF-E3-D08 | Note | US-GEO-306-SC4; tab Lời giải, Hỏi | Nội dung tab thuộc E4 | E3 dựng khung tab + bảng trượt điện thoại và chỗ cắm `slots` (`features/e3/WorkspacePanel.tsx`); tab Gợi ý tối thiểu gọi `/hints/{level}` | E4 |
| QF-E3-D09 | Fixed (FIX2, soát E3 #1; PO-56) | US-GEO-305; BR-GEO-155, 156 | Đo gián tiếp lộ đại lượng ẩn (điểm–mặt thay mặt–mặt, điểm–đường thay đường–đường, đoạn bằng nhau) | Khi khóa, ẩn mọi số đo cùng loại (góc / độ dài) có giá trị trùng đại lượng ẩn trong dung sai AnswerMatch 1e-4·max(1,\|q\|)+1e-12; giá trị khác (SM = SD/2) vẫn hiện. Ca Dev-E3-01, 02 (`shared/viewer/viewer.fix2.test.ts`) | FIX2 · xong |
| QF-E3-D10 | Fixed (FIX2, soát E3 #8) | US-GEO-305-SC6; US-GEO-303 | Khóa lại không ẩn lại số đo đã hiện; thiết diện lõm vẽ bằng bao lồi | Khóa lại: ẩn ngay và xóa giá trị; thiết diện nối giao tuyến từng mặt thành vòng kín (không được thì bao lồi như cũ). Ca Dev-E3-03, 05, 06 | FIX2 · xong |
| QF-E3-D11 | Fixed (FIX2, soát E3 #7) | BR-GEO-142 | Nét khuất lấy mẫu 32 khúc: điểm chuyển lệch tới 1/32, bỏ sót khúc ngắn | Điểm chuyển chính xác (giao hình chiếu đoạn với cạnh mặt che, giao với mặt phẳng che). Giới hạn: mặt cong (nón, trụ, cầu) chỉ vẽ đường tròn, không che khuất. Ca Dev-E3-04 | FIX2 · xong |
| QF-E3-D12 | Fixed (FIX2, soát E3 #2, #3; PO-52) | PO-52 | Móc `__geoScene` và trang thử hình còn trong bản production; bản e2e ghi đè `web/dist` | Điều kiện viết thẳng `import.meta.env` + import động (`sceneHook.ts`, `SandboxPage.tsx`); `npm run build` chạy `scripts/check-dist.mjs` (thoát 1 nếu `dist` còn `__geoScene`); bản e2e `npm run build:e2e` → `web/dist-e2e` | FIX2 · xong |
| QF-E3-D13 | Fixed (FIX2, soát E3 #4, #5, #6) | TC-GEO-0309, 0313, 0334, 0352; US-GEO-302 | TC-0309 bản API không xoay gì (luôn đúng); 0309/0313 tuần tự và `expectedRev: 1` vỡ khi chạy lại; khóa Redis của lô đặt trước khi ghi CSDL | TC-0309 thành E2E-UI xoay/thu phóng/"Đặt lại" thật trên làn E3 riêng (k = 5/6) thay Hà ở BG-1, có đối chứng một lần "Chọn" = 1; TC-0313 đếm theo chênh lệch; seed 300_e3 có 12 làn, chạy lại dùng làn +6; `openNowE3` đọc `releaseRev`; khóa lô "pending" rồi "done" sau khi ghi, lỗi thì gỡ; trình duyệt giữ batchId khi gửi lại. Ca Dev-E3-07 (`it/e3/FigureBatchE3IT`) | QC · ghi nhận đổi tầng TC-0309 (E2E-API → UI) |
| QF-E3-D14 | Fixed (FIX2) | API học sinh E3 | Thiếu test an ninh cho figure-events, open, hints, lock | `tests/e3/security.api.spec.ts`: Dev-E3-08 vai giáo viên → 403; Dev-E3-09 học sinh cùng trường khác lớp → 404; Dev-E3-10 học sinh trường khác, bài không tồn tại → 404 | FIX2 · xong |
