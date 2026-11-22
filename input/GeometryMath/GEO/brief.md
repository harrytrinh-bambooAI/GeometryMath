# GEO — Hình Học Không Gian Cùng AI (MVP) — brief

| | |
|---|---|
| Version / date | 1.00 / 22-11-2026 |
| Owner (business) | Chủ đầu tư (người dùng) |
| Requested by | Chủ đầu tư, trực tiếp trong phiên làm việc, 22-11-2026 |
| Status | Draft — goal run 22-11-2026; các điểm chưa xác nhận nằm trong `questions/Q_ChuDauTu.md` và mục 6 |
| Target release | Không đặt mốc thời gian (PO-15); chỉ ghi thứ tự: tài liệu và khung mã trước, rồi mới viết mã |

## 1. Why (problem and outcome)
Học sinh khó hình dung hình học không gian vì hình trong sách và trên bảng là ảnh phẳng; lời giải có sẵn cho đáp số mà ít giải thích vì sao. Khi MVP chạy: học sinh nhìn và thao tác trên hình 3D đúng, được gợi ý trước, chỉ xem lời giải khi giáo viên cho phép; giáo viên có kho đề, giao bài, đặt giờ mở lời giải và theo dõi lớp. Hệ thống phải phục vụ **nhiều học sinh cùng lúc và nhiều trường** ngay từ kiến trúc đầu tiên. Phục vụ lời hứa sản phẩm: học trước, đáp án sau; AI đề xuất, giáo viên quyết định (`DOMAIN-PROFILE.md` §1, §7).

## 2. What (scope)
**Epics:** bảy epic MVP theo `DOMAIN-PROFILE.md` §14.
- E1 Nền tảng và gọi AI phía máy chủ; bộ đề chuẩn (TRU-01, TRU-03, IAM-01).
- E2 Hình đúng: dựng hình từ ràng buộc, hình mẫu, kiểm chứng và nhãn (GEO-02, GEO-03, GEO-04).
- E3 Thao tác hình 3D trên thiết bị cảm ứng (GEO-05, GEO-06, GEO-07).
- E4 Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học (GEO-01, GEO-08, GEO-09, GEO-10, ANS-04).
- E5 Kho đề của Giáo viên mẫu (KNW-01, KNW-02).
- E6 Giao bài và kiểm soát lời giải (IAM-02, LRN-01, LRN-02, LRN-04, ANS-01, ANS-02, ANS-03, TRU-02).
- E7 Theo dõi lớp và ghi sự kiện (LRN-03, PRF-01).

**Tái sử dụng / mở rộng:** bản mẫu tương tác `Design/prototype/` (logic màn hình, bộ hình 3D `fig3d.js`, kịch bản dữ liệu mẫu) là tham chiếu hành vi; mã sản phẩm viết lại trên máy chủ thật.
**In scope:** web app riêng có máy chủ; hình không gian THCS và THPT; gợi ý hai mức rồi lời giải; khóa lời giải ở máy chủ kể cả qua hỏi đáp; từ khóa mở bài giảng có nguồn; hỏi đáp theo giáo trình mẫu; giáo viên mẫu giao bài và theo dõi lớp; đa trường (cách ly dữ liệu theo trường).
**Out of scope — và người dùng thấy gì thay vào:** hình học phẳng, giáo viên tự soạn đề và bài giảng, import file và ảnh, giáo trình riêng, phụ huynh, chấm bài, nhóm học, vận hành trường, quản trị giáo dục (các chặng 2 đến 9) → không có trong sản phẩm MVP; chỉ còn ở bản mẫu với nhãn "Concept".

## 3. Who (users, roles, actors without a login)
Học sinh (iPad, iPhone), Giáo viên mẫu (web máy tính; kiểm tra nhanh bằng điện thoại). Tác nhân không đăng nhập: cổng AI phía máy chủ (nhà cung cấp mô hình), bộ kiểm chứng hình học, bộ ghi sự kiện. Phụ huynh, quản trị trường, cơ quan quản lý không có trong MVP (PO-01, PO-15).

## 4. Sources (the only inputs for analysis)
| Source | Link / file | Date / version | What it covers |
|---|---|---|---|
| hồ sơ domain | `DOMAIN-PROFILE.md` | 22-11-2026 | 13 sub-domain, vai trò, 67 chức năng, quy tắc BR-01…, thực thể, phi chức năng, hướng dẫn tách epic |
| tài liệu sản phẩm | `PRODUCT-MASTER.md`, `MVP.md` | 22-11-2026 | Phạm vi MVP, tiêu chí hoàn thành, nền móng B8 |
| sổ quyết định | `PO-DECISIONS.md` | 22-11-2026 | Final của chủ đầu tư (PO-01, 15, 27–29, 33–36, 45) và giả định PO-02…PO-49 |
| thiết kế | `Design/mvp-01…08*.png`, `Design/prototype/` | 22-11-2026 | Màn hình MVP và bản mẫu tương tác (hành vi tham chiếu) |
| tài liệu mẫu giáo viên | `materials/*.docx`, `*.xlsx` | 22-11-2026 | 6 bài tập mẫu có lời giải, gợi ý, đáp số |
| hệ thống hiện có | `SPEC-technical.md` | 22-11-2026 | Hạn chế của bản artifact cũ |
| các chặng sau | `STAGES-AFTER-MVP.md` | 22-11-2026 | CHỈ để biết điểm nối, KHÔNG phải phạm vi MVP |

## 5. Already decided (with source)
- Thiết bị theo vai: học sinh iPad/iPhone, giáo viên web, kiểm tra nhanh bằng điện thoại — chủ đầu tư — PO-01 (Final).
- Gợi ý trước; lời giải chỉ hiện từ giờ giáo viên đặt — chủ đầu tư — `PRODUCT-MASTER.md` D-12.
- Hình và đáp số phải qua kiểm chứng bằng tính toán trước khi gắn nhãn tin cậy — `PRODUCT-MASTER.md` D-13.
- Giáo viên quyết định cuối; điểm AI chỉ là đề xuất (không có trong MVP, vẫn là nguyên tắc) — PO-03.
- Không xếp hạng ở mọi cấp — PO-04.
- Hình tương tác, lời giải cập nhật hình, hỏi đáp có nguồn, từ khóa mở bài giảng — chủ đầu tư — PO-33…PO-36 (Final); mức MVP theo PO-49 (giả định).
- **Ràng buộc kỹ thuật chủ đầu tư nêu ngày 22-11-2026:** (a) phục vụ nhiều học sinh dùng đồng thời; (b) kiến trúc cho nhiều trường (liên trường); (c) đáp ứng 1000 người dùng đồng thời; (d) truy vấn dữ liệu không được "smell code": tối ưu từ cách viết truy vấn, phân trang, bố cục thiết kế bảng và dữ liệu; (e) máy chủ có thể dùng Java.
- Thứ tự làm việc (chủ đầu tư, 22-11-2026): Epic, user story, tiêu chí chấp nhận, test case và khung mã nguồn xong trước; sau đó mới chia việc cho các agent viết mã.

## 6. Open questions (with the default we will use)
| # | Question (≤ 2 sentences) | Default | Owner | Needed by |
|---|---|---|---|---|
| 1 | "1000 người dùng đồng thời" nghĩa là gì và ngưỡng chấp nhận là bao nhiêu? | Giả định PO: 1000 phiên đang hoạt động, mỗi phiên khoảng một yêu cầu mỗi 3–5 giây; đỉnh 1000 học sinh cùng mở lời giải khi giờ mở tới; p95 đọc ≤ 300 ms, ghi ≤ 500 ms; tỉ lệ lỗi < 0,5%. Đây là đề xuất, chưa phải ngưỡng đã chốt. | ChuDauTu | trước khi viết mã |
| 2 | Hạ tầng chạy thử và vận hành: máy chủ, cơ sở dữ liệu, bộ nhớ đệm, nơi đặt? | Giả định PO: PostgreSQL và Redis riêng của dự án (đã dựng cục bộ, cổng 54329 và 6389); chưa chọn nhà cung cấp đám mây. | TechLead | trước khi viết mã |
| 3 | Nhà cung cấp AI thật và khóa dùng khi nào? | Giả định PO: cổng AI có nhà cung cấp giả lập (nội dung soạn sẵn), tích hợp nhà cung cấp thật là bước sau; MVP vẫn có đủ tầng cổng, ghi chi phí và đổi model. | ChuDauTu | trước E1 |
| 4 | Định danh học sinh trong MVP: đăng nhập nhẹ bằng tên và mã lớp có đủ không, khi PO-21 còn chờ pháp lý về dữ liệu trẻ em? | Giả định PO: đăng nhập nhẹ (tên hiển thị + mã lớp), không thu email, số điện thoại; dữ liệu tối thiểu. | ChuDauTu | trước E6 |
| 5 | Số trường, lớp, học sinh mẫu để thử tải? | Giả định PO: 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện. | TechLead | trước khi viết mã |

## 7. Constraints and NFR
- **Đa trường:** mọi dữ liệu thuộc một trường có định danh trường; truy vấn luôn lọc theo trường; học sinh trường này không bao giờ thấy dữ liệu trường khác; chia sẻ liên trường (chặng 7) phải đi qua cấp phép tường minh, nên mô hình dữ liệu phải chừa chỗ ngay từ đầu.
- **Hiệu năng:** theo câu hỏi 1; đo bằng bộ thử tải trên dữ liệu mẫu theo câu hỏi 5.
- **Truy vấn:** không phân trang kiểu bỏ qua n dòng (OFFSET); dùng phân trang theo khóa; không truy vấn lặp theo từng dòng (N+1); chỉ lấy cột cần; mọi truy vấn nóng có chỉ mục phục vụ và có kiểm bằng kế hoạch thực thi; bảng sự kiện lớn được chia theo thời gian; dữ liệu nhạy cảm (lời giải) tách bảng riêng để không lộ qua truy vấn thông thường.
- **Khóa lời giải:** ở tầng dữ liệu máy chủ trả về, không chỉ ở giao diện; ca thử moi đáp án qua hỏi đáp (`DOMAIN-PROFILE.md` §14).
- **Thiết bị:** iPad, iPhone cho học sinh; máy tính và điện thoại cho giáo viên; tiếng Việt có dấu; số thập phân dùng dấu phẩy.
- **Riêng tư:** dữ liệu trẻ em tối thiểu; chưa mở tài khoản chính thức (PO-21).
- **Công nghệ:** máy chủ dùng Java (chủ đầu tư cho phép); Java cài sẵn là bản 26.

## 8. Timeline and team
Không đặt mốc thời gian. Thứ tự: (1) hồ sơ nền (PO), (2) user story + tiêu chí chấp nhận (BA), test case (QC), kiến trúc dữ liệu và phi chức năng (kiến trúc sư) chạy song song theo luật bảng chung; song song dựng khung mã nguồn Java; (3) chủ đầu tư duyệt (R3); (4) chia việc cho các agent viết mã.

## 9. Success measures
Theo `MVP.md` §9: hình thỏa ràng buộc ≥ 95% và đáp số đúng ≥ 90% trên bộ đề chuẩn; không bài sai nào mang nhãn "Đã kiểm chứng"; không lần nào lời giải lộ trước giờ mở trong bộ câu thử moi đáp án; theo câu hỏi 1 cho hiệu năng. Các ngưỡng là đề xuất, chưa có số đo thực tế.

## Revision log
| Version | Date | Change | By |
|---|---|---|---|
| 1.00 | 22-11-2026 | Bản nháp goal run từ `DOMAIN-PROFILE.md`, `MVP.md`, `PO-DECISIONS.md` và yêu cầu kỹ thuật của chủ đầu tư | PO |
