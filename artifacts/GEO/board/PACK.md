# GEO — knowledge pack for fresh agents

> Owner A (PO soạn nội dung lần đầu), ≈ 5k token. Đọc tệp này, `CONVENTIONS.md`, BOARD §0, phần kỹ năng vai bạn cần cho bước hiện tại, và **chỉ** tệp của epic bạn làm. Appetite: **lean** — G5b = 8 câu ở §6, đạt ≥ 6, mọi câu ★ phải đúng; viết đáp án ở đầu báo cáo CP-1. Khi tệp này và gói tài liệu mâu thuẫn, tệp này thắng; nguồn gốc vẫn là `work/03_decisions.md` và `PO-DECISIONS.md`.

## 1. Feature, scope, state
- **GEO** (Hình Học Không Gian Cùng AI, MVP chặng 1): web app riêng, máy chủ Java, một khối nhiều module (`iam`, `tenancy`, `content`, `geometry`, `learning`, `answers`, `knowledge`, `events`, `ai-gateway`). Lời hứa: học sinh thao tác hình 3D đúng, gợi ý trước, lời giải khóa ở máy chủ tới giờ giáo viên đặt; giáo viên quyết định cuối, AI chỉ đề xuất.
- **Bảy epic**, thứ tự cố định: E1 nền và cổng AI, bộ đề chuẩn · E2 hình đúng · E3 thao tác 3D · E4 gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học · E5 kho đề · E6 giao bài và khóa lời giải · E7 theo dõi lớp, ghi sự kiện. **53 story sơ bộ** (`work/04_story_map.md`), id `US-GEO-nnn`, hàng trăm = epic. **55 REQ** (`work/00_gate0_intake_voc.md`), độ phủ 100%.
- **Ràng buộc của chủ đầu tư (Final):** đa trường, 1000 người dùng đồng thời, truy vấn tối ưu (không OFFSET, không N+1, phân trang theo khóa, chỉ mục + kế hoạch thực thi, bảng sự kiện chia theo thời gian, lời giải ở bảng riêng), Java được phép. **Ngưỡng hiệu năng là đề xuất.**
- **Trạng thái:** PO xong S0 đến S3 (R0, R1, R2 do PO lấy, "Not reviewed (TBD)"). BA (story, AC), QC (case) và ARCH (`work/architecture/`) làm tiếp, song song. Không viết mã chức năng ở lượt này; khung mã `app/` do agent khác.
- **OUT (không có story):** hình phẳng; soạn đề, import, OCR, chat sửa, giáo trình riêng; giao theo bộ; đăng ký giáo viên thật, trường (quản trị), phụ huynh, chấm bài, nộp bài, thông báo, nhóm học, liên trường. Khung thiết kế có điều khiển không nguồn (Trình chiếu, Sửa, Sau buổi học tới, Nên nhắc lại, chuỗi ngày, phần trăm theo dạng) không xây phía sau (D-GEO-078).

## 2. Glossary and banned synonyms (rút gọn; đầy đủ ở `work/glossary.md`)
| Dùng | Không dùng |
|---|---|
| Kho đề, Bài giao, Bài của em | ngân hàng đề, assignment, danh sách bài |
| Giờ mở lời giải (`release_at`); Chưa đặt giờ, Sắp mở, Đã mở, Mở ngay | thời điểm công bố, hạn, deadline, mở sớm |
| Khóa lời giải; Kiểm soát đáp án | ẩn lời giải; bộ lọc |
| Gợi ý 1, Gợi ý 2, Lời giải (thang gợi ý) | gợi ý cơ bản, cấp gợi ý, đáp án |
| Đã kiểm chứng / Cần xem lại (nhãn máy); AI soạn / Giáo viên đã duyệt (nhãn người) | đã duyệt (cho nhãn máy), đúng, verified; "Hình đã kiểm chứng" |
| Đề lạ, Tự học, Theo dõi lớp, Xem trước như học sinh | đề ngoài, free mode, dashboard lớp, preview |
| Giáo viên mẫu, Lớp mẫu, Mã lớp, Tên hiển thị, Trường | giáo viên demo, mật khẩu, họ tên, tenant (chỉ trong tài liệu kỹ thuật) |
| Lộ đáp số, Phiên đang hoạt động, Phiên làm bài, Thao tác trên hình | rò rỉ, user online, session học, tương tác hình |

## 3. Data rules and formats (with owners)
- **Mọi bản ghi** có `owner_id`, `schema_version`; bản ghi của học sinh, bài giao, sự kiện, đề tự nhập có `school_id` bắt buộc; kho đề và bài giảng mẫu là nội dung hệ thống dùng chung (D-GEO-076). Chủ: ARCH (lược đồ), PO (nghĩa).
- **Giờ mở:** hàm của (`release_at`, giờ máy chủ). Rỗng → Chưa đặt giờ (khóa); t < R → Sắp mở (khóa); t ≥ R → Đã mở. Mở ngay: R := giờ máy chủ lúc bấm. Đã mở không khóa lại, không đổi giờ nữa. Chỉ giờ máy chủ có giá trị. Chủ: PO.
- **Nhãn máy:** "Đã kiểm chứng" chỉ khi hình do engine dựng thỏa ràng buộc (sai số ≤ 1e-6), đáp số khớp (sai số tương đối ≤ 1e-4, ≥ 3 bộ giá trị tham số) và tọa độ là của engine; còn lại "Cần xem lại"; đề lạ kèm cảnh báo. Dung sai là giả định D-GEO-040. Chủ: PO.
- **Số:** hiển thị dấu phẩy (3,5); dữ liệu dấu chấm; phần trăm một chữ số thập phân; giờ `HH:mm, dd/MM/yyyy` giờ Việt Nam, lưu UTC. Mã bài kho đề `HKG-nn`. Chủ: PO, ARCH.
- **Công thức chỉ số** M-GEO-01…24: một công thức mỗi con số ở `work/02_domain_model.md`; không tự đặt công thức khác. Tải: 1000 phiên, 200 đến 333,3 yêu cầu/giây đều, đỉnh 100 yêu cầu/giây mở lời giải (cửa sổ 10 giây).
- **Dữ liệu mẫu:** lớp 11A1 (THPT), 9B (THCS); An, Bình, Chi, Dũng, Hà; một trường mẫu "THPT Mẫu A"; thử cách ly thêm "THCS Mẫu C". Không tên người hay tổ chức thật.

## 4. Agreed facts and Human decisions (M-nnn → D-GEO)
| Điều đã chốt | Mã | Trạng thái |
|---|---|---|
| Thiết bị: học sinh iPad, iPhone; giáo viên web, điện thoại kiểm tra nhanh | D-GEO-001 | Final |
| Gợi ý trước; lời giải từ giờ giáo viên đặt | D-GEO-003 | Final |
| Hình và đáp số kiểm chứng bằng tính toán; không sao chép nhãn trong `materials/*.xlsx` | D-GEO-004 | Final |
| MVP: chỉ hình không gian, Giáo viên mẫu, kho đề AI soạn, web app riêng | D-GEO-005 | Final |
| Hỏi đáp theo giáo trình có nguồn; từ khóa mở bài giảng; hình tương tác; lời giải cập nhật hình | D-GEO-006…009 | Final |
| Đa trường, 1000 đồng thời, truy vấn tối ưu, Java | D-GEO-012…015 | Final |
| Soạn với AI, import, giáo trình riêng: yêu cầu Final nhưng OUT khỏi MVP | D-GEO-010, 011 | Final / Assumption |
| Mọi thứ khác (giờ mở, dung sai, lộ đáp số, trợ giúp, đa trường nghĩa là gì, cách vào của Giáo viên mẫu…) | D-GEO-020…079 | Assumption (PO) — PROVISIONAL |
Tin nhắn M-nnn của chủ đầu tư trong run: chưa có.

## 5. Pitfalls (từ phân tích nguồn; RV bổ sung sau)
1. Ẩn lời giải ở giao diện không đủ: kiểm dữ liệu máy chủ trả về, kể cả xem trước, theo dõi lớp, hỏi đáp, phản hồi lưu đệm.
2. Nhãn kiểm chứng trong `materials/*.xlsx` là nhãn có sẵn, không phải kết quả tính.
3. Hồ sơ cũ ghi `org_id` để trống; brief mới yêu cầu đa trường: brief thắng.
4. `MVP.md` ghi "một bộ đề"; PO-25 ghi MVP giao từng đề: PO-25 thắng.
5. Khung `mvp-05` ghi "Hình đã kiểm chứng"; chuỗi đúng là "Đã kiểm chứng".
6. Công cụ đo không được thành đường tắt: ẩn cả đại lượng cùng vai trò (góc ASD như góc SDA).
7. Tự học không là đường vòng: đề trùng bài đang khóa kế thừa khóa.
8. Đo lường "đang hoạt động", "thao tác", "phiên" theo định nghĩa trong `glossary.md`; không đo xoay hay thu phóng là thao tác.
9. Đừng đưa chặng 2 đến 9 vào AC; đừng tự đặt ngưỡng hiệu năng thành "đã chốt".
10. AI giả lập: chỉ số thời gian và độ đúng thật chưa đo được.

## 6. G5b control questions (★ = critical)
1. ★ Bài giao chưa đặt giờ mở: lời giải ở trạng thái nào và vì sao?
2. ★ Ai quyết nhãn "Đã kiểm chứng" và có dùng cột "Trạng thái kiểm chứng" sẵn trong `materials/*.xlsx` không?
3. ★ "Đa trường" trong MVP nghĩa là gì, và hồ sơ cũ ghi gì khác?
4. Dải id case của E4 và dải id story BA được thêm cho E4?
5. ★ Ngưỡng p95 đọc, ghi và tỉ lệ lỗi trong brief có phải đã chốt không?
6. Đúng giờ `release_at` thì mở hay khóa, và đã mở rồi có khóa lại được không?
7. ★ Soạn đề với AI, import file, giáo trình riêng có trong MVP không?
8. Bài tự học xem được lời giải khi nào, và ngoại lệ nào?
