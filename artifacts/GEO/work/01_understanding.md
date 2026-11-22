# Understanding — lời chủ đầu tư so với thiết kế, bảng xung đột (Gate 2b) — GEO

> **Input snapshot:** như `00_inputs.md`. Giọng chủ đầu tư (`brief.md` §5, `PO-DECISIONS.md` Final) thắng mọi ghi chú giả định hoặc đề xuất. Đã xem ảnh: `mvp-02`, `mvp-03`, `mvp-04`, `mvp-05`. Chưa xem ảnh `mvp-01`, `mvp-06`, `mvp-07`, `mvp-08`: nội dung lấy từ mô tả `MVP.md` §3 và `Design/prototype/README.md`; BA kiểm lại bằng ảnh khi viết story của epic liên quan.

## 1. Hiểu biết tóm tắt
1. Feature GEO là MVP web app hình học không gian: học sinh thao tác hình 3D đúng, gợi ý trước, lời giải khóa ở máy chủ tới giờ giáo viên đặt; Giáo viên mẫu giao bài và theo dõi lớp.
2. Chủ đầu tư nêu thêm ngày 22-11-2026 các ràng buộc kỹ thuật: nhiều học sinh đồng thời, đa trường, 1000 người dùng đồng thời, truy vấn tối ưu, Java (brief §5). Chúng ảnh hưởng mọi epic, không phải một epic riêng; epic E1 chứa phần nền, ARCH chứa thiết kế.
3. Bảy epic theo `DOMAIN-PROFILE.md` §14, 26 chức năng P0. Một yêu cầu Final (từ khóa, bài giảng có nguồn, hỏi đáp theo giáo trình: PO-33, 34, 49) không có chức năng P0 trong §14: đã xếp vào E4 (D-GEO-030).
4. AI giả lập ở MVP (D-GEO-047). Mọi chỉ số cần AI thật chưa đo được.

## 2. Bảng xung đột và cách PO quyết

| # | Bên A | Bên B | Ai thắng và vì sao | Quyết định |
|---|---|---|---|---|
| C-01 | `MVP.md` B8, `DOMAIN-PROFILE.md` §9: "`org_id` để trống", tenant ngoài MVP (`MVP.md` §4) | `brief.md` §5b, §7: đa trường ngay từ kiến trúc, mọi dữ liệu có định danh trường | brief (Final của chủ đầu tư, ngày muộn hơn) | `school_id` bắt buộc ở mọi bản ghi của học sinh, bài giao, sự kiện; không có màn quản trị trường. D-GEO-012, D-GEO-050 |
| C-02 | PO-25: MVP giao từng đề | `MVP.md` §5E, LRN-02: "một đề hoặc một bộ đề" | `PO-DECISIONS.md` đứng đầu thứ tự nguồn | MVP giao từng đề; giao bộ là OUT. D-GEO-035 |
| C-03 | PO-27, 28, 29, 45 (Final): soạn giáo án, import, OCR, giáo trình riêng | `MVP.md` §4, §5 P1: ngoài MVP; PO-32 xếp chặng 2, 5, 6 | Final nói "cần có", không nói "trong MVP"; vị trí chặng là giả định PO-32 | Giữ yêu cầu Final làm REQ, đặt OUT khỏi MVP, hỏi chủ đầu tư. D-GEO-010, D-GEO-011 |
| C-04 | PO-30, PO-40: chỉ nội dung giáo viên duyệt tới học sinh | PO-43: bài giảng AI viết chưa duyệt vẫn hiện (kèm nhãn) | PO-DECISIONS tự ghi cần chủ đầu tư xác nhận | dùng PO-43, đánh dấu PROVISIONAL. D-GEO-029 |
| C-05 | `DOMAIN-PROFILE.md` §14: GEO-11 và bài giảng không có trong bảng epic | PO-34, PO-49, `MVP.md` §5D: từ khóa, bài giảng, hỏi đáp có nguồn nằm trong MVP | PO-34 là Final | xếp vào E4. D-GEO-030 |
| C-06 | `MVP.md` §9: "toàn bộ P0 chạy trên máy tính và điện thoại" | PO-01: học sinh iPad, iPhone; giáo viên web và điện thoại | PO-01 Final | thiết bị theo PO-01 (`DOMAIN-PROFILE.md` §13 mục 6 cũng hiểu vậy). D-GEO-001 |
| C-07 | `materials/*.xlsx` cột "Trạng thái kiểm chứng" đã điền sẵn | D-13: nhãn phải do tính toán | D-13 (Final) | nhãn tính lại; không sao chép. D-GEO-004 |
| C-08 | `MVP.md` §3b "Học sinh – tự học: bất kỳ học sinh" | IAM-01: học sinh vào bằng tên hiển thị và mã lớp | không nguồn nào cho học sinh không có mã lớp | tự học cho học sinh đã vào bằng mã lớp. D-GEO-052 |
| C-09 | `MVP.md` §1: giáo viên mẫu "vào vai" (khung thiết kế có công tắc Giáo viên / Học sinh) | BR-03: chỉ giáo viên của lớp đặt giờ mở lời giải | BR-03 | công tắc vai trong khung là của bản mẫu; sản phẩm vào bằng mã giáo viên mẫu. D-GEO-073 |
| C-10 | `Design/mvp-05` ghi "Hình đã kiểm chứng" | `mvp-02` và PO-02 ghi "Đã kiểm chứng" | PO-02 | một chuỗi "Đã kiểm chứng". D-GEO-079 |
| C-11 | `MVP.md` §5C "Giữ toàn bộ công cụ hiện có" (bản artifact cũ) | thanh công cụ ở `mvp-05` và prototype: Chọn, Điểm, Đường/Đoạn, Mặt phẳng, Đo dài, Đo góc, Oxyz, Đặt lại; không thấy "giao tuyến", "tô màu" | chưa đọc `SPEC.md`: không biết danh sách công cụ đầy đủ | BA đọc `SPEC.md` và `SPEC-technical.md` khi viết E3; ghi vào `known_gaps.md` KG-01 |
| C-12 | `DOMAIN-PROFILE.md` §9: kho đề thuộc Giáo viên mẫu | brief §7: mọi dữ liệu thuộc một trường | chưa nguồn | kho đề là nội dung hệ thống dùng chung. D-GEO-076 |
| C-13 | khung `mvp-04` có thẻ "3 ngày liên tiếp", phần trăm theo dạng gồm "Thiết diện" | PO-09 cấm "số ngày học liên tiếp" với phụ huynh; thiết diện là chặng 2; không có chức năng nào cho học sinh | không có nguồn yêu cầu | không xây phía sau; D-GEO-078 |
| C-14 | khung `mvp-02` có "Trình chiếu", "Sửa (sắp có)", "Sau buổi học tới" | GEO-13, KNW-03 chặng 2; PO-26 | các chặng sau | OUT; D-GEO-078 |
| C-15 | `MVP.md` trạng thái "Đề xuất, chờ chủ sản phẩm duyệt" | `PO-DECISIONS.md` liệt các quyết định ở `MVP.md` §1 là Final | `PO-DECISIONS.md` | dùng §1 làm Final; các phần khác của `MVP.md` là đề xuất đã được chủ đầu tư dùng làm nền |

## 3. Lời chủ đầu tư và khung thiết kế (đối chiếu)

| Lời chủ đầu tư (nguồn) | Khung / hành vi | Khớp? | Ghi chú |
|---|---|---|---|
| Gợi ý trước, lời giải từ giờ giáo viên đặt (SRC-102) | `mvp-05`: tab Gợi ý, Lời giải (khóa "mở lúc … do giáo viên đặt"), Hỏi đáp; `mvp-02` bước 3 "Thời điểm mở lời giải" | khớp | khung hiện giờ cụ thể và đếm ngược ("còn 3 giờ 12 phút"): story BA định nghĩa đếm ngược theo giờ máy chủ |
| Giao bài ba bước: lớp, trợ giúp, thời điểm (`MVP.md` §3) | `mvp-02` đủ ba bước, hai công tắc | khớp | D-GEO-053 |
| Theo dõi lớp: ai mở, mức gợi ý, hỏi gì (SRC-086) | `mvp-03` có đủ, thêm thẻ tổng, thời gian, tình trạng, "Nên nhắc lại trên lớp" | thừa | thẻ "Nên nhắc lại" không có nguồn: D-GEO-078; số liệu: D-GEO-077 |
| Mở ngay, Đổi giờ (SRC-088) | `mvp-03` thẻ vàng "Lời giải đang khóa" | khớp | D-GEO-041, 042 |
| Hình tương tác, đo (PO-35) | `mvp-05` thanh công cụ; chip "SA = a", "AD = a" hiển thị ràng buộc đề cho | khớp | chip ràng buộc là yêu cầu hiển thị chưa nêu ở nguồn văn bản; BA ghi nhận làm AC khi viết E3 |
| Công cụ đo ẩn đại lượng đề hỏi (PO-08) | `mvp-05` có "Đo dài", "Đo góc" | khung không thể hiện ẩn | AC phải nêu; D-GEO-024 |
| Bài của em: giờ mở và tiến độ (SRC-084) | `mvp-04` thẻ bài có nhãn giờ, thanh ba đoạn, "Đã xem gợi ý 2" | khớp | D-GEO-075 |
| Tự học: dán đề hoặc chọn hình mẫu (SRC-073) | `mvp-04` khung "Tự khám phá"; `mvp-07` (chưa xem) | khớp theo mô tả | D-GEO-052 |
| Từ khóa mở bài giảng, hỏi đáp có nguồn (PO-33, 34) | không có ảnh `mvp-*`; có `Design/prototype/` | bản mẫu có | tham chiếu hành vi: prototype README |

## 4. Điều tôi (PO) không suy diễn
Không viết hành vi cho giáo viên thật tự soạn đề, đăng ký, phụ huynh, chấm bài, trường (quản trị), thông báo. Không đặt số bộ câu thử tối thiểu (QC đề xuất, PO phán quyết). Không chọn nhà cung cấp AI, hạ tầng đám mây, mô hình kinh doanh.
