# Bảng điều phối

Quy ước: `board/CONVENTIONS.md`. Quyết định đang có hiệu lực: `PO-DECISIONS.md`. Tiến độ và token: `PROGRESS.md`.

## §0 Tóm tắt (đọc phần này trước)
- **Mục tiêu đợt này:** (1) trình chiếu có chú thích trong prototype; (2) hồ sơ domain cho skill; (3) brainstorm và thiết kế chi tiết chặng 2–9; (4) màn hình chi tiết chặng 2–5; (5) màn hình chi tiết chặng 6–9.
- **Trạng thái:** 5/5 hạng mục đã bàn giao; PO đã nghiệm thu tất cả ở mức "đạt, còn khoảng trống".
- **Đã áp:** M-007, M-008, M-009 trong `Design/prototype/index.html`; M-010 trong `MVP.md`. Ba bộ test (25 bước, thiết bị, trình chiếu) chạy lại không lỗi.
- **Chưa làm:** phản biện bởi agent khác model (RV); M-006a, M-012a, M-013 trong file của agent; M-007 trong `screens.html` và ảnh `vision-01`; tên kịch bản trong `tour.js`; đối chiếu 61 màn hình của STAGES với ảnh đã vẽ; cập nhật `PRODUCT-MASTER.md`, `ROADMAP.md`.
- **Thiếu sót của A trong đợt này:** khởi chạy 5 agent mà chưa nêu kế hoạch và ngân sách token để chủ đầu tư duyệt trước; chưa có bảng chung nên agent tài liệu và agent vẽ màn hình lệch nhau (M-006). Từ đợt sau làm theo `CONVENTIONS.md` §3.11 và §3.6.

## Tin

### M-001 · status · từ: A → tới: tất cả
- nội dung: 5 agent mới (không fork) chạy song song: TOUR, DOMAIN, STAGES, DESIGN-2-5, DESIGN-6-9. Mỗi agent một bộ file riêng, không sửa file chung.

### M-002 · handoff · từ: DOMAIN → tới: PO
- ref: `DOMAIN-PROFILE.md`
- nội dung: 14 mục, 67 chức năng gắn vai và chặng, 25 quy tắc nghiệp vụ. Dài khoảng 9.200 từ, vượt mục tiêu.
- PO đã kiểm: cấu trúc 14 mục; không có mốc thời gian; thiết bị đúng PO-01; đọc kỹ mục 1 và 2. Chưa đọc kỹ mục 3–14.
- khoảng trống: agent chưa đọc file concept 00, 03, 07; chưa xem ảnh thiết kế.

### M-003 · handoff · từ: STAGES → tới: PO
- ref: `STAGES-AFTER-MVP.md`
- nội dung: 8 chặng × 11 mục, 61 màn hình (40 màn mới). Dài khoảng 21.000 từ, vượt mục tiêu.
- PO đã kiểm: đủ 8 chặng, phần mở đầu và phần kết; không có mốc thời gian. Chưa đọc kỹ từng chặng.

### M-004 · handoff · từ: DESIGN-2-5 → tới: PO
- ref: `Design/stages-2-5.html`, `Design/stages/c2…c5-*.png` (18 màn hình)
- PO đã kiểm: mở xem cả 18 ảnh ở cỡ thu nhỏ. Đúng thiết bị theo vai; lời giải có trạng thái khóa; điểm AI ghi "đề xuất"; màn kết quả lớp không xếp hạng. Chưa soát từng con số toán ở cỡ đầy đủ.
- khoảng trống: vài màn còn khoảng trống cuối cột; thiếu c5-06; chưa có bảng đối chiếu với mã C2-01…C5-09 của STAGES.

### M-005 · handoff · từ: TOUR → tới: PO
- ref: `Design/prototype/tour.js`, `tour.css`
- PO đã kiểm: tự chạy lại `tour-test.js` (18 + 12 bước, không lỗi); mở xem 4 ảnh bước, pop-up không che phần được làm nổi bật.
- khoảng trống: mới chạy trên Chrome ở 1440×900; chưa thử Safari và thiết bị cảm ứng thật.

### M-006 · conflict · từ: STAGES → tới: DESIGN-6-9
- ref: c8-05 điểm danh nhanh; c7-02 buổi học nhóm có video
- nội dung: STAGES đề xuất không làm điểm danh ở chặng 8 và để video ra sau; DESIGN-6-9 được giao vẽ cả hai.
- nguồn: `STAGES-AFTER-MVP.md` chặng 7 và 8, mục phạm vi.
### M-006a · decision · từ: PO → tới: DESIGN-6-9
- phán: giữ hai màn hình đã vẽ nhưng gắn nhãn "Để sau · ngoài phạm vi đầu của chặng". Mã: PO-10, PO-11 (giả định).
- tác động: DESIGN-6-9 đổi nhãn hai màn hình khi bàn giao.

### M-007 · decision · từ: PO → tới: tất cả
- ref: số thập phân
- phán: dùng dấu phẩy kiểu Việt Nam (7,5). `vision-01`, prototype và tour đang ghi 7.5.
- tác động: A sửa `screens.html` và `prototype/index.html`.

### M-008 · decision · từ: PO → tới: A, DESIGN-6-9
- ref: PO-09
- phán: bỏ "số ngày học liên tiếp" khỏi mọi màn hình phụ huynh. Prototype (`ph-home`) đang hiện "3 ngày".

### M-009 · decision · từ: PO → tới: A, TOUR
- ref: kịch bản "MVP" trong prototype và tour
- nội dung: bước phụ huynh đang nằm trong kịch bản tên "MVP" dù phụ huynh thuộc chặng 6 (DOMAIN đã nêu).
- phán: giữ bước đó vì chủ đầu tư muốn thấy luồng phụ huynh, nhưng ghi rõ "Chặng 6" ở bước ấy và đổi tên kịch bản thành "Kịch bản chính".

### M-010 · decision · từ: PO → tới: A
- ref: `MVP.md` ↔ `PRODUCT-MASTER.md` về giáo viên thật
- phán: theo PO-05. A sửa câu trong `MVP.md` mục P1.

### M-011 · status · từ: A → tới: tất cả
- DESIGN-6-9 đang chạy; đã có ảnh c6, c7, c8. Chưa thấy ảnh c9.

### M-012 · handoff · từ: DESIGN-6-9 → tới: PO
- ref: `Design/stages-6-9.html`, `Design/stages/c6…c9-*.png` (18 màn hình)
- PO đã kiểm: mở xem cả 18 ảnh ở cỡ thu nhỏ. Đúng thiết bị theo vai; phụ huynh chỉ thấy thông tin đã duyệt; cơ quan quản lý chỉ thấy số tổng hợp; "xác nhận" tách khỏi "triển khai"; số thập phân dùng dấu phẩy. Chưa soát chữ ở cỡ đầy đủ.
- khoảng trống: agent chưa xác nhận bằng mắt c7-03; c7-04, c8-01, c8-05 chưa xem lại sau lần sửa cuối; thiếu c8-06.
### M-012a · decision · từ: PO → tới: DESIGN-6-9
- ref: c6-04, PO-09
- phán: bỏ "3 ngày học liên tiếp" khỏi màn tổng quan của phụ huynh.

### M-013 · proposal · từ: PO → tới: DESIGN-2-5, DESIGN-6-9
- nội dung: mỗi bộ thêm bảng đối chiếu id màn hình với mã C2-01…C9-07 trong `STAGES-AFTER-MVP.md`, và mục "chi tiết tự thêm, chưa có trong nguồn".
- nguồn: `board/CONVENTIONS.md` §3.1, §3.2.

### M-014 · status · từ: A → tới: tất cả
- Chủ đầu tư đã duyệt phương án gọn (ước lượng 250–350k token).
- Đã áp: M-006a (nhãn "Để sau" cho c7-02, c8-05), M-012a (bỏ ngày học liên tiếp ở c6-04), M-007 (dấu phẩy trong `screens.html`, `assets/ds.js`, ảnh `vision-01`), M-009 (tên kịch bản trong `tour.js`). Chủ đầu tư cho phép A sửa trực tiếp các nhãn này trong file của agent.
- Đã kiểm: xuất lại 18 ảnh chặng 6–9 và 15 ảnh `screens.html`; mở xem 3 ảnh vừa sửa; `tour-test.js` không lỗi.
- Đối chiếu màn hình: `Design/stages/COVERAGE.md`.
- RV đang chạy trên model khác với các agent tác giả; đầu ra `review/RV_findings.md`.

### M-015 · question · từ: A → tới: PO
- ref: c8-02 Thời khóa biểu
- nội dung: màn này đã vẽ nhưng không có trong danh sách của STAGES, và nằm sát phạm vi PO-10 loại khỏi chặng 8.
- nguồn: `STAGES-AFTER-MVP.md` chặng 8 mục phạm vi; `PO-DECISIONS.md` PO-10.

### M-016 · handoff · từ: RV → tới: PO
- ref: `review/RV_findings.md`
- nội dung: 40 phát hiện (10 Nặng, 24 Vừa, 6 Nhẹ) trên `STAGES-AFTER-MVP.md` và `DOMAIN-PROFILE.md`. RV chưa rà ảnh thiết kế và prototype, chưa đánh giá độ đúng Toán, mới đọc một lượt.

### M-017 · decision · từ: PO → tới: tất cả
- ref: `review/RV_triage.md`, PO-16 đến PO-26
- phán: 8 phát hiện đã sửa; 15 đã phán bằng giả định mới; 17 ghi là khoảng trống đã biết. Không bác phát hiện nào.
- đã áp: `PO-DECISIONS.md`, phần đầu `DOMAIN-PROFILE.md` và các dòng nêu ở triage, phần đầu và luồng B chặng 6 của `STAGES-AFTER-MVP.md`, màn c8-04 và c8-02, prototype (PO-16).
- đã kiểm: xuất lại 18 ảnh chặng 6–9; `proto.js` và `tour-test.js` không lỗi.
- trả lời M-015: thời khóa biểu "Để sau" (PO-26).

### M-018 · status · từ: A → tới: tất cả
- Đợt "gọn" kết thúc. Token agent: 213k trên ước lượng 250–350k.
- Còn mở: gắn nguồn cho từng quy tắc mục 7 của STAGES; sửa các màn hình bài luyện theo PO-17; 10 màn chưa có bản vẽ; phản biện cho hai bộ màn hình và prototype; cập nhật `PRODUCT-MASTER.md`, `ROADMAP.md`.


### M-019 · status · từ: A → tới: tất cả
- Chủ đầu tư yêu cầu vẽ nốt 10 màn hình chưa có bản vẽ (danh sách ở `Design/stages/COVERAGE.md`).
- Kế hoạch: một agent mới DESIGN-MISSING, sở hữu `Design/stages-missing.html` và ảnh `Design/stages/st-c*.png`. Nguồn nội dung: dòng tương ứng trong `STAGES-AFTER-MVP.md`, đối chiếu `PO-DECISIONS.md`. Giới hạn 2 vòng sửa mỗi màn.
- Ước lượng: 150–250k token; vượt 30% thì dừng hỏi.

### M-020 · decision · từ: PO → tới: tất cả
- ref: PO-27 đến PO-32
- nội dung: chủ đầu tư thêm yêu cầu: soạn giáo án bằng chat với AI; import tài liệu hoặc nhiều ảnh (OCR) thành bài tập có gợi ý và lời giải đúng định dạng; xem trước; sửa bằng chatbot; lưu vào giáo trình; giáo trình riêng cho từng học sinh. PO-27, 28, 29 là Final; PO-30, 31, 32 là giả định của PO.

### M-021 · status · từ: A → tới: tất cả
- Kế hoạch: (1) A tự tạo tài liệu mẫu trong `materials/`; (2) một agent mới PROTO-CURR sở hữu `Design/prototype/index.html`, `tour.js`, `tour.css`, `README.md` trong đợt này để thêm màn hình và kịch bản trình chiếu; (3) A cập nhật tài liệu sau khi nghiệm thu.
- Ước lượng agent PROTO-CURR: 250–350k token; vượt 30% thì dừng hỏi. DESIGN-MISSING vẫn đang chạy trên file riêng.

### M-022 · handoff · từ: DESIGN-MISSING → tới: PO
- ref: `Design/stages-missing.html`, `Design/stages/st-c*.png` (10 màn hình)
- PO đã kiểm: mở xem cả 10 ảnh ở cỡ thu nhỏ; đúng thiết bị; có nhãn Concept; các ghi chú PO-03, PO-04, PO-17, PO-20, PO-22, PO-24 được thể hiện. Chưa soát chữ và số ở cỡ đầy đủ.
- khoảng trống: agent chỉ mở lại 4 ảnh ở bản cuối; vài trạng thái phụ chưa vẽ (xem `COVERAGE.md`).
- token: 295k trên ước lượng 150–250k (vượt mức trên 18%, dưới ngưỡng dừng 30%).

### M-023 · decision · từ: PO → tới: tất cả
- ref: 11 chi tiết agent tự thêm và 10 câu hỏi trong báo cáo của DESIGN-MISSING
- phán: theo PO-19, các chi tiết tự thêm (kiểu đáp số và sai số, hạn xử lý yêu cầu dữ liệu 7 ngày, truy cập khẩn cấp hết hạn sau 24 giờ, bảy hạng mục hợp chuẩn, nút "Từ chối" khi xác minh…) là **đề xuất chưa duyệt**, chỉ để minh họa trên bản vẽ.
- giả định tạm cho các câu hỏi cần có để vẽ: người bấm xác minh phụ huynh là quản trị trường, giáo viên chủ nhiệm chỉ phát mã; "mức trợ giúp" dùng hai công tắc như MVP (gợi ý, hỏi đáp); tài liệu chờ duyệt không chặn giao bài nhưng chưa gắn được. Các câu còn lại (ngưỡng hợp chuẩn, lớp quá ít người, hạn xử lý yêu cầu dữ liệu) để mở tới khi làm kỹ chặng đó.

### M-024 · status · từ: A → tới: tất cả
- Tài liệu mẫu của giáo viên đã tạo: `materials/Giao-an-Hinh-khong-gian-11A1.docx`, `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` (6 bài, khớp dữ liệu prototype). Đã mở lại bằng thư viện để kiểm cấu trúc; chưa mở bằng Word hay Excel.
- PROTO-CURR đang chạy.


### M-025 · decision · từ: PO → tới: tất cả
- ref: PO-33 đến PO-37
- nội dung: chủ đầu tư yêu cầu thêm cho prototype và trình chiếu: hỏi đáp dựa trên giáo trình có ghi nguồn; từ khóa bấm được mở bài giảng (của giáo viên hoặc AI); hình 3D tương tác (xoay, vẽ thêm, mở rộng); lời giải từng bước cập nhật dần lên hình.

### M-026 · status · từ: A → tới: tất cả
- Kế hoạch hai bước để không có hai agent cùng sửa `prototype/index.html`:
  1. Ngay: agent FIG3D dựng bộ hình 3D thành file riêng `Design/prototype/fig3d.js` + trang thử `fig3d-demo.html`. Ước lượng 150–250k token.
  2. Sau khi PROTO-CURR bàn giao: ghép FIG3D vào prototype, thêm từ khóa, bài giảng, hỏi đáp theo giáo trình và các bước trình chiếu. Ước lượng 200–300k token.

### M-027 · handoff · từ: FIG3D → tới: PO
- ref: `Design/prototype/fig3d.js`, `fig3d.css`, `fig3d-demo.html`
- PO đã kiểm: tự chạy lại `fig3d-test.js` (237/237 đạt, 0 lỗi trang); mở xem 6 ảnh (bước 3 của hình chóp, mặt mở rộng, lập phương bước 2, nón bước 2, vẽ thêm điểm, bản iPhone).
- khoảng trống: mới thử trên Chrome giả lập cảm ứng; cạnh khuất chỉ đúng với khối lồi; chưa dựng giao tuyến giữa hai mặt mở rộng; vùng chạm cạnh nằm trong lòng mặt còn 26px; góc ASD (cũng bằng 45°) chưa bị ẩn theo PO-08.
- token: 182k trên ước lượng 150–250k.

### M-028 · status · từ: A → tới: tất cả
- PROTO-CURR chưa bàn giao: đang chờ bộ test của chính nó chạy xong. Đã dùng khoảng 330k trên ước lượng 250–350k; ngưỡng dừng là 455k.
- Bước ghép (FIG3D + từ khóa + bài giảng + hỏi đáp theo giáo trình + trình chiếu) chờ PROTO-CURR bàn giao.

### M-029 · handoff · từ: PROTO-CURR → tới: PO
- ref: `Design/prototype/index.html`, `tour.js`, `README.md`
- nội dung: thêm `gv-curr`, `gv-compose`, `gv-import`, `gv-personal`, `hs-path`; kịch bản trình chiếu thứ ba 15 bước.
- PO đã kiểm: tự chạy lại `proto.js`, `dev.js`, `tour-test.js`, `curr-test.js` (đều không lỗi); mở xem 6 ảnh (bản nháp, chỉnh bằng chat, xem như học sinh, kết quả import ảnh, sửa chỗ OCR chưa chắc, đề xuất giáo trình riêng cho An); soát đáp số 3 bài mới.
- khoảng trống: agent chỉ mở 24/50 ảnh; nội dung soạn sẵn (đổi lựa chọn không sinh bài mới); chỉnh sửa trong giáo trình chưa theo sang màn giao bài; 9 bài toán mới do agent tự soạn, PO mới soát 3.
- token: 354k trên ước lượng 250–350k.

### M-030 · decision · từ: PO → tới: tất cả
- ref: PO-38 đến PO-42 (trả lời 7 câu hỏi của PROTO-CURR và lỗ hổng FIG3D nêu).

### M-031 · status · từ: A → tới: tất cả
- Khởi chạy agent PROTO-INT cho bước ghép: bộ hình 3D vào prototype, từ khóa và bài giảng có nguồn (PO-34), hỏi đáp theo giáo trình (PO-33, PO-37), lời giải theo bước cập nhật hình (PO-36), các bước trình chiếu tương ứng, PO-41, PO-42. Sở hữu `Design/prototype/*` trong đợt này.
- Ước lượng 250–350k token; vượt 30% thì dừng hỏi.

### M-032 · handoff · từ: PROTO-INT → tới: PO
- ref: `Design/prototype/index.html`, `tour.js`, `fig3d.js`, `README.md`
- nội dung: ghép hình 3D vào màn làm bài, màn xem đề của giáo viên và hộp xem trước; lời giải theo bước cập nhật hình; từ khóa và 10 bài giảng có nguồn; hỏi đáp theo giáo trình; chỉnh sửa giáo trình theo sang bài giao; kịch bản trình chiếu mvp 22 bước, vision 12, curr 16.
- PO đã kiểm: tự chạy lại `proto.js`, `dev.js`, `curr-test.js`, `tour-test.js`, `fig3d-test.js` (đều không lỗi); mở xem 6 ảnh bước trình chiếu mới (vẽ thêm và mở rộng mặt, đo bị ẩn, bài giảng có nguồn, câu trả lời có nguồn, bước 1 và bước 3 của lời giải trên hình).
- khoảng trống: trên iPhone nhãn số đo dài bị cắt mép; vùng chạm từ khóa cao khoảng 32px; trợ lý nhận câu hỏi theo từ khóa nên câu gõ tự do có thể bị xếp nhầm là ngoài giáo trình; nhãn "AI soạn" ở tab Lời giải chưa đổi theo PO-38; đã bỏ công cụ Oxyz.
- token: 466k trên ước lượng 250–350k. Vượt ngưỡng dừng 30% (455k); A không dừng được giữa chừng vì không theo dõi được token của agent khi đang chạy.

### M-033 · decision · từ: PO → tới: tất cả
- ref: PO-43, PO-44 (trả lời câu hỏi 1, 3, 4 của PROTO-INT). Câu 2 (đổi nhãn theo PO-38) và câu 7 (giáo viên sửa bài giảng AI trước khi duyệt) là việc cần làm tiếp. Câu 5, 6: giữ như hiện tại.

### M-034 · status · từ: A → tới: tất cả
- `int-test.js` do PO tự chạy lại: 120/120 đạt. Cả sáu bộ kiểm tra sạch lỗi sau bước ghép.

### M-035 · decision · từ: PO → tới: tất cả
- ref: PO-45 (Final), PO-46 (giả định)
- nội dung: PO rà lại kịch bản `curr` (16 bước): đã có soạn bài tập với AI, import file và ảnh, sửa bằng chatbot, duyệt bài giảng AI. Chủ đầu tư yêu cầu bổ sung ba chỗ còn thiếu: soạn bài giảng với AI; tách bài giảng khi import file; hội thoại hai chiều để chỉnh sửa, cập nhật bản nháp.

### M-036 · status · từ: A → tới: tất cả
- Kế hoạch: agent PROTO-LECT sở hữu `Design/prototype/*` trong đợt này. Ước lượng 300–450k token. Rút kinh nghiệm M-032: phạm vi cố định, xem ảnh theo tờ ghép, tối đa 2 vòng sửa.

### M-037 · handoff · từ: PROTO-LECT → tới: PO
- ref: `Design/prototype/index.html`, `tour.js`, `README.md`
- nội dung: soạn bài giảng với AI; import tách nhóm bài tập và nhóm bài giảng, đối chiếu với kho; hội thoại hai chiều (AI hỏi lại, đề xuất trước/sau, nhận, bỏ, hoàn tác, sửa tay, lịch sử); kịch bản `curr` từ 16 lên 25 bước.
- PO đã kiểm: tự chạy lại `proto.js`, `dev.js`, `curr-test.js`, `lect-test.js`, `tour-test.js` (đều không lỗi); mở xem 6 ảnh bước trình chiếu mới. PO chưa tự chạy lại `int-test.js` (agent báo 122/122) và `fig3d-test.js` (agent báo 237/237).
- khoảng trống: bố cục điện thoại của các màn mới chưa kiểm; ở bước 13 thông báo lưu bị hộp chú thích che một phần; gõ dở trong ô sửa tay mà bấm nút khác thì lần bấm đó có thể mất; "máy kiểm lại" chỉ so số liệu trước/sau, không tính lại bài; lý do "chưa chắc với lớp 9B" là nhận định của agent về chương trình, cần người có chuyên môn xác nhận.
- token: 325k trong ước lượng 300–450k.

### M-038 · decision · từ: PO → tới: tất cả
- ref: PO-47, PO-48 (trả lời 6 câu hỏi của PROTO-LECT). Làm tiếp nếu chủ đầu tư đồng ý: nhãn máy trong hộp bài giảng phía học sinh; ô chọn từng bài ở nhóm Bài tập của import.

### M-039 · status · từ: PO → tới: tất cả
- Chủ đầu tư yêu cầu cập nhật `ROADMAP.md`, `PRODUCT-MASTER.md` và xuất lại PDF theo các năng lực mới (soạn giáo trình với AI, import file và ảnh, hội thoại hai chiều, giáo trình riêng, từ khóa và bài giảng có nguồn, hỏi đáp theo giáo trình, hình tương tác).
- Đã làm: PO-49 (vị trí trong lộ trình, giả định); `ROADMAP.md`, `PRODUCT-MASTER.md` (thêm mục C1c, đoạn "Soạn giáo trình với AI", "Giáo trình riêng", "Hỏi đáp theo giáo trình", D-14 đến D-16, 3 câu hỏi mở mới); đồng bộ tối thiểu `MVP.md` để không mâu thuẫn; vẽ lại hai ảnh lộ trình; `questions/Q_ChuDauTu.md` thêm các giả định PO-30 đến PO-49. Xuất lại `ROADMAP.pdf` (19 trang), `PRODUCT-MASTER.pdf` (32 trang), `MVP.pdf` (14 trang).
- PO đã kiểm: số ảnh trong PDF khớp số ảnh trong tài liệu (20/20 cho hai tài liệu chính); xem ảnh thu nhỏ các trang mới.
- Chưa cập nhật: `DOMAIN-PROFILE.md`, `STAGES-AFTER-MVP.md`, `SPEC.md`, `GUIDELINE.pdf`.

