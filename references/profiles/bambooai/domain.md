# Domain — Hình Học Không Gian Cùng AI (dự án GeometryMath)

> *Sản phẩm là gì.* Sự kiện agent không được tự đoán. Nguồn: `DOMAIN-PROFILE.md`, `PO-DECISIONS.md`, `MVP.md`, `Input/GeometryMath/GEO/brief.md`. Chi tiết nghiệp vụ nằm ở `artifacts/GEO/work/`; file này chỉ là bản đồ. Chỗ chưa biết ghi `TBD — owner <người>`.

## 1. Đơn vị và lời hứa sản phẩm
- **Chủ sản phẩm / đội:** chủ đầu tư (người dùng của skill), một đội nhỏ gồm PO, BA, QC, kiến trúc sư (ARCH) và agent mã Java.
- **Lời hứa (một câu mọi yêu cầu phải phục vụ):** *Học sinh nhìn và thao tác trên hình không gian 3D đúng, được gợi ý trước, chỉ xem lời giải khi giáo viên cho phép; giáo viên quyết định cuối, AI chỉ đề xuất.* (nguồn: `DOMAIN-PROFILE.md` §1, §7.)
- **Bốn điểm khác biệt:** (1) hình và đáp số kiểm chứng bằng tính toán; (2) học trước, đáp án sau, lời giải khóa ở máy chủ kể cả qua hỏi đáp; (3) giáo viên quyết định cuối; (4) lời giải đi cùng hình.
- **Khách hàng trước mắt:** học sinh và giáo viên Toán THCS, THPT. Phụ huynh, nhà trường, cơ quan quản lý thuộc chặng sau, không có trong MVP.
- **Phạm vi feature GEO (MVP, chặng 1):** hình không gian; 26 chức năng P0 (`DOMAIN-PROFILE.md` §5) chia bảy epic E1…E7, cộng yêu cầu phi chức năng của chủ đầu tư (đa trường, 1000 người dùng đồng thời, truy vấn tối ưu, Java).

## 2. Sản phẩm và module (bản đồ module)
Web app riêng có máy chủ, một khối nhiều module (`DOMAIN-PROFILE.md` §10). Mã nguồn máy chủ dùng Java (brief §7); thư mục mã: `app/`.

| Module | Việc của module | Chức năng (mã) | Epic |
|---|---|---|---|
| `iam` | đăng nhập nhẹ (tên hiển thị + mã lớp), người dùng, vai, tài khoản và lớp mẫu | IAM-01, IAM-02 | E1, E6 |
| `tenancy` | trường (`school_id`), cách ly dữ liệu giữa các trường, bối cảnh trường của mọi truy vấn | (brief §5b, §7) | E1 |
| `content` | đề, hình (`SceneSpec`), lời giải, gợi ý, bước, version, nhãn; dữ liệu đề tự nhập | GEO-01, GEO-08, GEO-09 (dữ liệu) | E4, E5 |
| `geometry` | engine hình học thuần hàm (ràng buộc → tọa độ), hình mẫu, kiểm chứng, đo; bộ đề chuẩn | GEO-02…07, TRU-01 | E1, E2, E3 |
| `learning` | lớp, bài giao, bài của em, theo dõi lớp, xem trước như học sinh, tự học | LRN-01…04, GEO-01 | E6, E7 |
| `answers` | giờ mở lời giải, khóa lời giải ở tầng dữ liệu, kiểm soát đáp án, hỏi đáp không lộ, bộ câu thử | ANS-01…04, GEO-10, TRU-02 | E4, E6 |
| `knowledge` | kho đề của Giáo viên mẫu, kho bài giảng mẫu, từ khóa, nguồn cho hỏi đáp | KNW-01, KNW-02, GEO-11 | E4, E5 |
| `events` | ghi `Interaction` (mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải), phân vùng theo thời gian | PRF-01 | E7 |
| `ai-gateway` | lớp gọi AI phía máy chủ: giấu khóa, đổi model, ghi chi phí, version prompt; nhà cung cấp giả lập | TRU-03 | E1 |
| `ui` (trình duyệt) | màn học sinh (iPad, iPhone), màn giáo viên (web, điện thoại), hình 3D | mọi màn | mọi epic |

**Quy tắc xuyên module:** (a) `geometry` thuần hàm, không phụ thuộc giao diện hay cơ sở dữ liệu; (b) mọi dữ liệu mang `school_id` (không để trống, xem D-GEO-012, D-GEO-050) và `owner_id`; (c) `answers` là cửa duy nhất đưa lời giải ra ngoài; không module nào khác đọc bảng lời giải để trả cho học sinh; (d) mọi lần gọi AI đi qua `ai-gateway`.

## 3. Người dùng và vai trò
| Vai | Phạm vi | Thiết bị | Không được |
|---|---|---|---|
| Học sinh | bài của lớp mình, dữ liệu của mình, gợi ý và hỏi đáp trong phạm vi cho phép | iPad, iPhone | thấy lời giải hay đáp số trước giờ mở; thấy bài hay câu hỏi của bạn khác; thấy dữ liệu trường khác |
| Giáo viên mẫu | một tài khoản tạo sẵn, sở hữu kho đề, dạy lớp mẫu; giao bài, đặt giờ mở, theo dõi lớp, xem trước | web máy tính; điện thoại để kiểm tra nhanh | tự soạn đề, sửa lời giải, đăng ký (ngoài MVP) |
| Tác nhân không đăng nhập | cổng AI (nhà cung cấp mô hình), bộ kiểm chứng hình học, bộ ghi sự kiện | — | tự quyết điểm, tự gắn nhãn "Đã kiểm chứng" khi chưa tính |
| Ngoài MVP | phụ huynh, quản trị trường, cơ quan quản lý, quản trị nền tảng | — | không có story trong MVP |

Persona và người dùng cực đoan: `artifacts/GEO/work/actor_profiles.md`.

## 4. Bảng thuật ngữ
Bảng đầy đủ có cột "banned synonyms" nằm ở `artifacts/GEO/work/glossary.md` (nguồn duy nhất, M19). Dưới đây là các dòng định dạng và định danh, mỗi dòng có chủ sở hữu (M13).

| Định dạng / định danh | Quy tắc | Chủ sở hữu |
|---|---|---|
| Số thập phân hiển thị | dấu phẩy, ví dụ 3,5; trong dữ liệu và API dùng dấu chấm | PO (hiển thị), ARCH (dữ liệu) |
| Giờ mở lời giải `release_at` | lưu UTC; hiển thị theo giờ Việt Nam; đồng hồ máy chủ là đồng hồ duy nhất | PO (quy tắc), ARCH (lưu) |
| Mã bài kho đề | `HKG-nn` (theo `materials/`) | PO |
| Mã lớp | chuỗi do hệ thống cấp, duy nhất toàn hệ thống (giả định D-GEO-051) | TechLead |
| Nhãn máy | "Đã kiểm chứng" / "Cần xem lại" | PO |
| Nhãn người | "AI soạn" / "Giáo viên đã duyệt" (MVP chỉ dùng "AI soạn") | PO |
| Mã định danh tài liệu | sơ đồ Q9 với tiền tố GEO (xem `team_profile.md` §5) | PO |
| `schema_version` | số nguyên tăng dần trên mọi cấu trúc JSON | ARCH |

Thuật ngữ miền (tóm tắt): kho đề, bài giao, giờ mở lời giải, khóa lời giải, kiểm soát đáp án, thang gợi ý, kiểm chứng, ràng buộc, hình mẫu, bộ đề chuẩn, bộ câu thử moi đáp án, giáo viên mẫu, lớp mẫu, tự học, theo dõi lớp: nghĩa ở `DOMAIN-PROFILE.md` §12 và `glossary.md`.

## 5. Thành phần dùng chung
Bộ hình 3D và công cụ trên hình (xoay, thu phóng, chọn, điểm, đoạn, mặt phẳng, mở rộng, đo độ dài, đo góc, đặt lại); thẻ nhãn kiểm chứng; thẻ "AI soạn"; thang gợi ý (hai mức rồi lời giải); thẻ giờ mở lời giải (chưa đặt, sắp mở, đã mở); từ khóa bấm được và hộp bài giảng có nguồn; khung hỏi đáp; màn chờ khi AI xử lý; thông báo lỗi dễ hiểu. Tham chiếu hành vi: `Design/prototype/` (`fig3d.js`), `Design/mvp-*.png`. Mã sản phẩm viết lại; bản mẫu không phải mã nguồn để sao chép.

## 6. Thiết bị và vùng khó kiểm thử
- Học sinh: iPad và iPhone (trình duyệt), cảm ứng là chính: chụm ngón, chạm chọn, kéo xoay. Giáo viên: trình duyệt máy tính; điện thoại để xem lớp, mở hoặc đổi giờ lời giải. Phiên bản iOS và trình duyệt tối thiểu: `TBD — owner TechLead`.
- Vùng khó kiểm: đúng lúc tới giờ mở; 1000 học sinh cùng mở lời giải; moi đáp án qua hỏi đáp (ngôn ngữ tự nhiên); đồng hồ máy khách lệch; AI giả lập khác AI thật; hình 3D trên màn nhỏ; cạnh khuất; cách ly dữ liệu giữa trường; thời gian chờ AI.

## 7. Ràng buộc dữ liệu và pháp lý
- MVP chỉ thu tên hiển thị và mã lớp; không thu email, số điện thoại. Quy định pháp lý về dữ liệu trẻ em, nơi lưu, lưu giữ, xóa, đồng thuận: chưa xác định, `TBD — owner ChuDauTu` (PO-21). Không viết story thu thêm dữ liệu trẻ em.
- Giảm tối đa dữ liệu học sinh gửi cho nhà cung cấp mô hình (không gửi tên hiển thị; `DOMAIN-PROFILE.md` §10).
- Không có số liệu thị trường, pháp lý hay đo thực tế; mọi ngưỡng là đề xuất.
- Không dùng tên người hay tổ chức thật trong dữ liệu mẫu (PO-14).
