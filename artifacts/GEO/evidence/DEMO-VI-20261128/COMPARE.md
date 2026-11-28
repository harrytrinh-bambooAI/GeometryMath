# COMPARE — Tính năng đã có so với các video cũ → video giới thiệu tiếng Việt mới (T-261128-75)

Phiên bản: v1.0 · 2026-11-28 · runner-T-261128-75

## Nguồn đã dùng

| Nguồn | Ngày | Ghi chú |
|---|---|---|
| `MVP.md`, `PRODUCT-MASTER.md` (checkout chính; B5 Tính năng P0, C4 Bản đồ năng lực, D1 Lộ trình) | 22/11/2026 | Danh mục tính năng chặng 1, 2, 3, 5 |
| `Video/GeometryMath.mp4` (7:48) + `Video/slides/*.png` (s1 giới thiệu, s2 tính năng & AI, s3 ý nghĩa, c1 giao bài, c2 soạn giáo án, c3 tầm nhìn, o1 tổng kết, o2 kết) | 22/11/2026 | Style BambooAI owner chỉ định (28/11). Nhiều tính năng AI gắn nhãn **CONCEPT** |
| `Video/01-giao-bai.mp4` (2:23), `Video/02-soan-giao-an.mp4` (3:11), `Video/03-tam-nhin-lo-trinh.mp4` (1:11) | 22/11/2026 | Ba chương tách lẻ của video tổng |
| Video T-73 `DEMO-20261128/GEO-MVP-demo-full-1080p.mp4` (9:46) + `SCRIPT.md` | 28/11/2026 | **Mới nhất** về tính năng: 19 clip chặng 1, 2, 3, 5, lời đọc tiếng Anh, giao diện chủ đạo xanh dương (không theo style BambooAI) |

Ký hiệu cột "Đã nêu ở": **GM** = GeometryMath.mp4 (+01/02/03), **T73** = video T-73. "CONCEPT" = video cũ chỉ nêu ở mức bản mẫu.

## Bảng đối chiếu

| # | Tính năng (chặng) | Đã nêu ở | Thiếu / chưa cụ thể | Đưa vào video mới ở cảnh |
|---|---|---|---|---|
| 1 | Kho đề, lọc theo cấp/hình/chủ đề, nhãn "Đã kiểm chứng" (1) | GM (01), T73 clip 01 | Chưa dùng bài thật của tổ Toán; T73 tiếng Anh | **04-kho-de-that** (HKG-01, HKG-03 thật) |
| 2 | Dựng hình 3D **từ đề gõ vào**, 4 bước Đọc đề → Dựng hình → Giải → Kiểm chứng (2, S2-E3) | GM s2 (nêu một dòng), T73: **chưa có** | Chưa từng quay luồng gõ đề → hình → kiểm chứng | **05-dung-hinh-tu-de** (đề biến thể HKG-01, đáp số 45° kiểm chứng) |
| 3 | Xoay hình 3D, vẽ thêm điểm/đoạn, đo độ dài/góc trên hình (1, 2) | GM s2 + clip 01; T73 clip 01–02 (chỉ xoay) | Đo trên hình và khoá đại lượng đáp số chưa quay | **06-xoay-ve-do** |
| 4 | Lời giải từng bước, câu "vì sao", trình chiếu mở từng bước, hình tô sáng (1, S2-E7) | GM s2; T73 clip 05 | Câu "vì sao" theo định hướng GV chưa nhấn | **07-loi-giai-tung-buoc** |
| 5 | Gợi ý từng bậc, lời giải mở theo giờ GV đặt; HS vào lớp bằng tên + mã (1) | GM c1 (01); T73 clip 02 | Chưa dùng gợi ý thật của HKG-01 | **09-goi-y-hoi-ai** |
| 6 | Kho tri thức có phiên bản, duyệt (S5-E3) | GM s2 ("Hỏi AI theo giáo trình"), T73 clip 11 | GM chỉ nói chung chung | **10-kho-tri-thuc** |
| 7 | Hỏi đáp có nguồn, không làm hộ bài, bấm nguồn mở đúng đoạn (S5-E3) | GM s2; T73 clip 11 | Câu hỏi mẫu không thuộc bài đang học | **11-hoi-dap-nguon** (câu hỏi về HKG-01, câu trả lời bám giáo án 11A1 — fixture demo) |
| 8 | Soạn đề bằng yêu cầu / từ tài liệu: AI soạn nháp, engine kiểm chứng, GV duyệt (S2-E3, S5-E2) | GM c2 (CONCEPT: "chat chỉnh bản nháp"); T73 clip 10 | GM ở mức concept; chưa nói rõ "câu chưa kiểm chứng xếp cuối, không chọn sẵn" | **05-dung-hinh-tu-de** (gõ đề) + **13-soan-de-prompt** (từ tài liệu). *Chưa có ô "prompt tự do" đúng nghĩa — xem Giả định* |
| 9 | Ra đề theo ma trận, nhiều mã đề, đổi câu giữ ma trận (S5-E2) | T73 clip 09 | GM chưa có | **14-ra-de-ma-tran** |
| 10 | Soạn giáo án theo PPCT, bản nháp có nhãn AI soạn (S5-E1) | GM c2 (CONCEPT "soạn bài giảng"); T73 clip 08 | GM concept; T73 chọn bài "Khoảng cách", không khớp giáo án thật | **15-soan-giao-an** (PPCT bài 24 = đúng chủ đề giáo án 11A1) |
| 11 | Rubric, tổng điểm sống, chặn lưu khi ≠ 10 (S5-E4) | T73 clip 12 | GM chưa có | **17-rubric** |
| 12 | HS chụp bài viết tay, kiểm độ nét, nộp có giờ máy chủ (S5-E4) | GM s2 (CONCEPT "đọc chữ từ ảnh"); T73 clip 12 (ảnh mẫu nét gạch) | Chưa có ảnh bài làm trông thật | **18-nop-bai-viet-tay** (ảnh viết tay minh hoạ) |
| 13 | Nhận dạng bài làm: đọc chữ viết tay, đối chiếu rubric, đánh dấu chỗ chưa chắc (S5-E4/E6) | GM s2 CONCEPT; T73 chỉ thấy "vùng ảnh AI dựa vào" | Chưa có cảnh giải thích luồng nhận dạng | **19-nhan-dang-bai-lam** (slide minh hoạ) |
| 14 | AI đề xuất điểm từng tiêu chí, khoanh vùng, GV nhận/sửa/bỏ, duyệt (S5-E6) | T73 clip 16 | GM chưa có | **20-ai-de-xuat-diem** |
| 15 | Chạy song song + báo cáo khớp AI–GV, cổng KPI (S5-E6) | T73 clip 17 | Quá kỹ thuật cho video giới thiệu | Bỏ khỏi video mới (nhắc gián tiếp ở 20: "điểm cuối do GV quyết định") |
| 16 | Tổng hợp lỗi sai của lớp + bộ luyện nhắc lại (S5-E5) | T73 clip 15 | GM chưa có | **21-loi-sai-lop** |
| 17 | Theo dõi lớp thời gian thực, không xếp hạng (1) | GM c1; T73 clip 03 | — | **22-theo-doi-lop** |
| 18 | Bài luyện chấm tự động, bàn phím toán (S2-E8) | T73 clip 04 | GM chưa có | Không quay riêng (thời lượng); nêu trong slide 02/25 |
| 19 | Lớp học thật, duyệt HS, bảng kết quả lớp × bài, tổng quan GV (chặng 3) | T73 clip 06–07 | GM chưa có | Không quay riêng; gộp ý vào 22 + slide 24 |
| 20 | Thông báo do trợ lý soạn, công tắc AI theo lớp, hạn mức (S5-E5) | T73 clip 14 | GM chưa có | Nêu ở slide 24 ("Bật/tắt AI theo từng lớp") |
| 21 | Phụ huynh xem tiến độ, giáo trình riêng từng HS (chặng 6) | GM c1, s2 (CONCEPT) | Chưa có trong sản phẩm | **Không đưa** (ngoài chặng 1, 2, 3, 5) |
| 22 | Lợi ích HS / GV, nguyên tắc "AI đề xuất, GV quyết định" | GM s3, o1 | T73 chỉ một câu | **23-loi-ich-hs**, **24-loi-ich-gv**, **25-tong-ket**, **26-ket** |

## Kết luận cho video mới

- Video mới tiếng Việt, style BambooAI, ~8 phút, 4 chương: **01 Hỗ trợ hình học · 02 AI đồng hành cùng học sinh · 03 Soạn đề và giáo án · 04 Chấm điểm & nhận dạng bài làm**, cộng mở đầu, lợi ích, tổng kết, kết.
- Những gì video cũ gắn **CONCEPT** nay quay trên giao diện thật: soạn giáo án (15), soạn đề nháp từ tài liệu (13), nộp bài viết tay (18), AI đề xuất điểm (20).
- Lần đầu quay: dựng hình từ đề gõ vào (05), đo trên hình (06), nhận dạng bài làm (19, minh hoạ).
- Dữ liệu thật thay dữ liệu mẫu: HKG-01..06 + gợi ý + lời giải (xlsx), chủ đề giáo án 11A1 (docx).

## Giả định (owner xem)

1. **Soạn đề "bằng prompting"**: giao diện hiện tại chưa có ô yêu cầu tự do kiểu chat để ra đề; video dùng (a) gõ đề → AI dựng + giải + kiểm chứng (`/gv/them-de`) và (b) AI soạn đề nháp từ tài liệu (`/gv/ra-de/tu-tai-lieu`). Lời đọc nói "giáo viên yêu cầu AI soạn đề nháp" — đúng với (b).
2. **Đề ở cảnh 05** là biến thể của HKG-01 (SA = a√2, hỏi góc của SC) vì AI giả lập chỉ có quy tắc dựng hình 3D cho đề này; đáp số 45° vẫn được engine kiểm chứng. Với AI thật có thể gõ nguyên văn HKG-01.
3. **Giáo án (15)**: nội dung bản nháp do AI giả lập soạn từ đơn vị kiến thức của PPCT bài 24, không chép nguyên văn file docx (không ghi đè hiển thị để tránh "giả" giao diện).
4. **Rubric (17), bài nộp (20), lỗi lớp (21)** dùng dữ liệu seed của e2e chặng 5 (tiêu chí có thể khác lời đọc "vẽ hình, hình chiếu, góc, tính toán, kết luận" — lời đọc khớp slide 19).
5. Cờ chặng 5 (`geo.s5.grading`, `geo.s5.ai-grading`) chỉ bật trong phiên dev local khi quay.
6. Lỗi hiển thị còn trong bản nháp (để T-76 quay lại sau khi gộp T-74): cảnh 07 trình chiếu, hình 3D nhỏ ở góc trái trên; LaTeX thô (`$S.ABCD$`) ở phiếu học tập cảnh 15.
7. Cảnh 11: chữ trả lời lấy từ fixture demo (bám HKG-01), nhãn nguồn vẫn là tài liệu seed "Khoảng cách 11A1 v2". Cảnh 18/20: tên bài giao trong seed là "Kiểm tra 15 phút – Khoảng cách", ảnh nộp là bài HKG-01 (góc).
8. Video bản nháp 7:26 (giọng Linh). Brief ghi ~8–10 phút: giọng Google TTS thường chậm hơn, nên bản dựng lại có thể dài hơn; muốn dài hơn nữa thì tăng `TAIL`/`EXTRA` trong `make-video-vi.mjs`.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu | runner-T-261128-75 |
