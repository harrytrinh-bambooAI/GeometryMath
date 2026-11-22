# Gate 1 — Vision alignment (vấn đề, đối tượng, IN / OUT) — GEO

> **Input snapshot:** như `00_inputs.md`. Mọi mục dẫn nguồn ở `00_sources.md`.

## 1. Vấn đề
Học sinh khó hình dung hình học không gian vì hình trong sách là ảnh phẳng; lời giải có sẵn cho đáp số mà ít giải thích vì sao; công cụ giải bài đưa đáp án ngay nên học sinh không học cách nghĩ. Giáo viên tốn công soạn đề, vẽ hình, và lo học sinh chép đáp số từ AI (`DOMAIN-PROFILE.md` §2; `brief.md` §1). Chưa có khảo sát người dùng và chưa có số đo nào.

## 2. Đối tượng và tác nhân
Học sinh THCS, THPT (iPad, iPhone); Giáo viên mẫu (web máy tính, điện thoại). Tác nhân không đăng nhập: cổng AI, bộ kiểm chứng hình học, bộ ghi sự kiện. Persona và ca cực đoan: `actor_profiles.md`.

## 3. IN / OUT và hệ quả người dùng thấy (M4)

| Mục | IN / OUT | Hệ quả người dùng thấy khi OUT |
|---|---|---|
| Hình không gian THCS, THPT (chóp, lăng trụ, hộp, nón, trụ, cầu mức cơ bản, Oxyz) | IN | — |
| Kho đề 40 đến 60 đề của Giáo viên mẫu, nhãn "AI soạn" và nhãn kiểm chứng | IN | — |
| Gợi ý hai mức rồi lời giải; khóa ở máy chủ kể cả hỏi đáp | IN | — |
| Từ khóa mở bài giảng có nguồn; hỏi đáp theo giáo trình mẫu | IN (D-GEO-030) | — |
| Tự học với đề tự nhập hoặc hình mẫu | IN (D-GEO-052) | — |
| Giao một đề cho lớp; giờ mở; theo dõi lớp; xem trước | IN | — |
| Đăng nhập nhẹ; lớp mẫu, Giáo viên mẫu | IN | — |
| Đa trường (cách ly dữ liệu), 1000 đồng thời, truy vấn tối ưu, Java | IN (ràng buộc, D-GEO-012…015) | — |
| Hình học phẳng (REQ-GEO-51) | OUT | không có công cụ hình phẳng; đề hình phẳng nhận thông báo "chưa hỗ trợ" |
| Giáo viên tự soạn đề và bài giảng, import docx xlsx pdf, ảnh OCR, chat sửa (REQ-GEO-52) | OUT | giáo viên không thấy nút soạn hoặc import; màn mẫu mang nhãn "Concept · Chặng 2/5" |
| Giáo trình riêng cho từng học sinh (REQ-GEO-53) | OUT | học sinh không thấy lộ trình riêng; nhãn "Concept · Chặng 6" |
| Giao theo bộ đề (D-GEO-035) | OUT | giao bài từng đề một |
| Trình chiếu, Sửa lời giải, "Sau buổi học tới", "Nên nhắc lại trên lớp", chuỗi ngày, phần trăm theo dạng, "Thiết diện" (D-GEO-078) | OUT | không có điều khiển hoạt động; ẩn hoặc nhãn "Concept" |
| Đăng ký giáo viên thật, quản trị trường, tổ chức nhiều cấp, phụ huynh, chấm bài, nộp bài, nhóm học, thông báo, chia sẻ liên trường (REQ-GEO-54) | OUT | chỉ có trong bản mẫu với nhãn Concept; MVP không có màn |
| Xếp hạng ở mọi cấp; tự duyệt điểm AI | không bao giờ làm | — |
| Số học sinh không có mã lớp tự học (D-GEO-052) | OUT | học sinh cần mã lớp để vào |

## 4. Thành phần dùng lại (M2)
| Thành phần | Nguồn | Kế thừa hay lệch |
|---|---|---|
| Hành vi màn hình, bộ hình 3D `fig3d.js`, kịch bản dữ liệu mẫu | `Design/prototype/` | kế thừa hành vi tham chiếu; lệch: mã sản phẩm viết lại trên máy chủ thật, không sao chép bản mẫu |
| Sáu bài tập mẫu `HKG-01…06`, định hướng giáo viên | `materials/*.xlsx` | kế thừa nội dung làm hạt giống kho đề; lệch: nhãn kiểm chứng tính lại (D-GEO-004) |
| Kho bài giảng mẫu 10 khái niệm | prototype README | kế thừa làm dữ liệu E4; lệch: nhãn người và nguồn theo D-GEO-029 |
| Bộ công cụ hiện có của bản artifact cũ | `SPEC.md` | chưa đọc; KG-01 |
| Cổng AI nhà cung cấp giả lập | brief §6 câu 3 | mới; D-GEO-047 |

## 5. Giả định và câu hỏi mở
Danh sách đầy đủ: `03_decisions.md` (các dòng "Assumption (PO)") và `questions_for_stakeholders.md`. Điểm rủi ro cao nhất: D-GEO-050 (đa trường vs org_id), D-GEO-073 (cách vào của Giáo viên mẫu), D-GEO-045 (ngưỡng hiệu năng là đề xuất), D-GEO-011 (soạn và import ngoài MVP trong khi là yêu cầu Final).
