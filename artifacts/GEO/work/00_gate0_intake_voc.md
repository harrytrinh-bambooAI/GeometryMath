# Gate 0 — Intake và VoC (REQ id) — GEO

> **Input snapshot:** như `00_inputs.md`. Mỗi yêu cầu thô có id ổn định, chưa phân tích. Cột "Xử lý": IN + epic, hoặc OUT + hệ quả người dùng thấy. Độ phủ ở cuối tệp. Trích dẫn nguyên văn nằm ở `00_sources.md` (cột "Nguồn").

## 1. Yêu cầu chức năng (26 chức năng P0 + một yêu cầu Final thiếu chức năng)

| REQ_ID | Nguồn | Yêu cầu thô (sát nguyên văn) | Xử lý |
|---|---|---|---|
| REQ-GEO-01 | SRC-073, SRC-055 | GEO-01: học sinh dán đề bằng chữ hoặc ảnh, hoặc chọn hình mẫu để khám phá; đề lạ dùng tọa độ AI kèm cảnh báo | IN E4 |
| REQ-GEO-02 | SRC-074, SRC-053 | GEO-02: AI mô tả hình bằng ràng buộc; engine tính tọa độ và kiểm tra lại | IN E2 |
| REQ-GEO-03 | SRC-075, SRC-114 | GEO-03: thư viện hình mẫu có tham số, dựng không cần AI | IN E2 |
| REQ-GEO-04 | SRC-076, SRC-054, SRC-070, SRC-113 | GEO-04: so đáp số AI với kết quả tính; gắn "Đã kiểm chứng" hoặc "Cần xem lại" | IN E2 |
| REQ-GEO-05 | SRC-077, SRC-036 | GEO-05: xoay, phóng to, tô màu cạnh và mặt, vẽ đường, dựng mặt phẳng, giao điểm, giao tuyến, trục Oxyz; mở rộng mặt, kéo dài cạnh | IN E3 |
| REQ-GEO-06 | SRC-078 | GEO-06: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh; đo độ dài, góc, khoảng cách | IN E3 |
| REQ-GEO-07 | SRC-079 | GEO-07: nét đứt cạnh khuất, chạm chọn, chụm ngón phóng to, đặt lại góc nhìn | IN E3 |
| REQ-GEO-08 | SRC-080, SRC-051, SRC-102, SRC-118 | GEO-08: thang gợi ý; mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải | IN E4 |
| REQ-GEO-09 | SRC-081, SRC-037 | GEO-09: bấm bước, hình tô màu đối tượng và đường phụ; vẽ thêm lần lượt; "Vì sao nghĩ ra bước này?" | IN E4 |
| REQ-GEO-10 | SRC-082, SRC-034, SRC-106, SRC-115, SRC-042 | GEO-10: hỏi đáp theo bài, dựa trên giáo trình, ghi nguồn, ngoài giáo trình nói rõ là chưa có; nhật ký lọc theo bài | IN E4 |
| REQ-GEO-11 | SRC-035, SRC-083, SRC-039, SRC-116, SRC-042 | Từ khóa bấm được mở bài giảng có nguồn và nhãn (GEO-11 và PO-34, mức MVP theo PO-49) | IN E4 |
| REQ-GEO-12 | SRC-084 | LRN-01: danh sách bài được giao, hiện giờ mở lời giải và tiến độ | IN E6 |
| REQ-GEO-13 | SRC-085, SRC-107, SRC-120, SRC-029 | LRN-02: chọn lớp, chọn trợ giúp, đặt giờ mở; giao một đề (bộ đề là OUT, D-GEO-035) | IN E6 |
| REQ-GEO-14 | SRC-086 | LRN-03: ai đã mở bài, dùng gợi ý mức nào, hỏi gì | IN E7 |
| REQ-GEO-15 | SRC-087 | LRN-04: giáo viên xem bài đúng như học sinh thấy | IN E6 |
| REQ-GEO-16 | SRC-088, SRC-050, SRC-069, SRC-112 | ANS-01: đặt, đổi, mở ngay giờ mở lời giải; chưa đặt thì khóa | IN E6 |
| REQ-GEO-17 | SRC-089, SRC-048, SRC-006, SRC-028 | ANS-02: trước giờ mở máy chủ không gửi lời giải và đáp số xuống thiết bị; mọi kênh | IN E6 |
| REQ-GEO-18 | SRC-090, SRC-049, SRC-038, SRC-040 | ANS-03: lời giải khóa thì trợ lý chỉ gợi ý; câu trả lời kiểm tra trước khi trả về | IN E6 |
| REQ-GEO-19 | SRC-091, SRC-022, SRC-052 | ANS-04: mức 3 của bài tự học mở sau khi xem mức 1 và 2 (giả định) | IN E4 |
| REQ-GEO-20 | SRC-092, SRC-057, SRC-103, SRC-117 | KNW-01: kho 40 đến 60 đề, lọc theo cấp, loại hình, dạng bài; ghi "AI soạn"; nhãn kiểm chứng | IN E5 |
| REQ-GEO-21 | SRC-093 | KNW-02: xem hình, đáp số, lời giải từng bước, gợi ý của một đề | IN E5 |
| REQ-GEO-22 | SRC-094 | PRF-01: ghi mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải | IN E7 |
| REQ-GEO-23 | SRC-095, SRC-010 | IAM-01: học sinh vào bằng tên hiển thị và mã lớp; dữ liệu riêng từng người | IN E1 |
| REQ-GEO-24 | SRC-096, SRC-025 | IAM-02: một Giáo viên mẫu, hai lớp mẫu, vài học sinh, bài giao mẫu | IN E6 |
| REQ-GEO-25 | SRC-097, SRC-063 | TRU-01: bộ đề chuẩn đo tỉ lệ hình thỏa ràng buộc và đáp số đúng; chạy lại khi đổi model | IN E1 |
| REQ-GEO-26 | SRC-098, SRC-068, SRC-026 | TRU-02: bộ câu thử moi đáp án, kể cả qua hỏi đáp | IN E6 |
| REQ-GEO-27 | SRC-099, SRC-009 | TRU-03: lớp gọi AI phía máy chủ: giấu khóa, đổi model, ghi chi phí mỗi lượt | IN E1 |

## 2. Quy tắc nghiệp vụ và ràng buộc chức năng

| REQ_ID | Nguồn | Yêu cầu thô | Xử lý |
|---|---|---|---|
| REQ-GEO-28 | SRC-059, SRC-071, SRC-004 | Học sinh chỉ thấy bài của lớp mình và dữ liệu của chính mình; học sinh trường này không thấy dữ liệu trường khác | IN E1 (xuyên suốt) |
| REQ-GEO-29 | SRC-019, SRC-056, SRC-057, SRC-103, SRC-109 | Lời giải AI soạn luôn ghi "AI soạn"; kho đề thuộc Giáo viên mẫu, đứng tên giáo viên này; nhãn hai trục | IN E5 |
| REQ-GEO-30 | SRC-058, SRC-061, SRC-110, SRC-111 | `schema_version`, `owner_id`, `model`, `prompt_version`, `author_id`; lời giải đã giao sửa bằng version mới | IN E1 (dữ liệu), E5 |
| REQ-GEO-31 | SRC-023, SRC-024, SRC-027 | Trước giờ mở, công cụ đo không hiện giá trị đại lượng đề hỏi và đại lượng cùng vai trò | IN E3 |
| REQ-GEO-32 | SRC-026 | Đề tự nhập trùng hoặc gần trùng bài đang khóa của lớp thì kế thừa khóa | IN E4 |
| REQ-GEO-33 | SRC-021 | Không có xếp hạng ở mọi cấp | IN E7 |
| REQ-GEO-34 | SRC-020, SRC-060 | Giáo viên quyết định cuối; AI không tự quyết, không tự gắn nhãn máy | IN (ràng buộc mọi epic) |
| REQ-GEO-35 | SRC-050 | Bài giao chưa đặt giờ thì lời giải vẫn khóa | IN E6 |
| REQ-GEO-36 | SRC-105 | Sửa lỗi đã biết: nhãn "Mặt Mặt …", cảnh báo hai đường chéo nhau, chia tam giác cho mặt nhiều đỉnh, nhật ký hỏi đáp lọc theo bài | IN E3 (ba lỗi hình), E4 (nhật ký) |

## 3. Yêu cầu phi chức năng và ràng buộc của chủ đầu tư

| REQ_ID | Nguồn | Yêu cầu thô | Xử lý |
|---|---|---|---|
| REQ-GEO-37 | SRC-001, SRC-002, SRC-004 | Đa trường: kiến trúc nhiều trường từ đầu; mọi dữ liệu có định danh trường; truy vấn luôn lọc theo trường; chừa chỗ cho cấp phép liên trường | IN E1; ARCH |
| REQ-GEO-38 | SRC-001, SRC-002, SRC-007, SRC-016 | Nhiều học sinh đồng thời; 1000 người dùng đồng thời; hiệu năng đo bằng bộ thử tải (ngưỡng là đề xuất) | IN E1; ARCH |
| REQ-GEO-39 | SRC-002, SRC-005 | Truy vấn tối ưu: không OFFSET, phân trang theo khóa, không N+1, chỉ lấy cột cần, chỉ mục và kế hoạch thực thi, bảng sự kiện chia theo thời gian | IN E1, E5, E7; ARCH |
| REQ-GEO-40 | SRC-005, SRC-006, SRC-068 | Khóa lời giải ở tầng dữ liệu máy chủ trả về; lời giải ở bảng riêng; ca thử moi đáp án qua hỏi đáp | IN E6; ARCH |
| REQ-GEO-41 | SRC-062 | Thời gian trung vị từ lúc gửi đề tới có hình dưới 40 giây (đề xuất) | IN E4, đo ở E1 |
| REQ-GEO-42 | SRC-012, SRC-017, SRC-043 | Thiết bị: iPad, iPhone cho học sinh; máy tính và điện thoại cho giáo viên; web trên trình duyệt | IN mọi epic có giao diện |
| REQ-GEO-43 | SRC-012, SRC-066 | Tiếng Việt có dấu; số thập phân dùng dấu phẩy; lỗi dễ hiểu | IN mọi epic |
| REQ-GEO-44 | SRC-010, SRC-015, SRC-065 | Dữ liệu trẻ em tối thiểu: chỉ tên hiển thị và mã lớp | IN E1 |
| REQ-GEO-45 | SRC-002, SRC-013 | Máy chủ dùng Java (cho phép); Java cài sẵn bản 26 | IN khung mã |
| REQ-GEO-46 | SRC-064, SRC-104 | Rời runtime artifact; web app riêng có backend; một khối nhiều module; engine thuần hàm | IN E1 |
| REQ-GEO-47 | SRC-063, SRC-111 | Version hóa prompt, model, bộ đề đánh giá; độ không chắc chắn hiện ra | IN E1, E2 |
| REQ-GEO-48 | SRC-062 | Màn chờ rõ ràng khi AI đang xử lý | IN E4 |
| REQ-GEO-49 | SRC-014, SRC-067, SRC-108 | Tiêu chí xong MVP: hình ≥ 95%, đáp số ≥ 90%, 0 bài sai mang nhãn kiểm chứng, 0 lần lộ, thao tác ≥ 60%, kho ≥ 40 đề đã kiểm chứng | IN E1 (đo), E5, E6, E7 |
| REQ-GEO-50 | SRC-011, SRC-016 | Dữ liệu thử tải: 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện | IN E1 (dữ liệu) |

## 4. Yêu cầu ngoài MVP (ghi nhận, không story)

| REQ_ID | Nguồn | Yêu cầu thô | Xử lý và hệ quả người dùng thấy |
|---|---|---|---|
| REQ-GEO-51 | SRC-101 | Hình học phẳng để sau (GEO-12) | OUT. Người dùng không thấy hình phẳng; nếu đề tự nhập là hình phẳng, thông báo "chưa hỗ trợ" (BA đề xuất câu chữ) |
| REQ-GEO-52 | SRC-030, SRC-031, SRC-041, SRC-072 | Giáo viên soạn bài tập và bài giảng với AI, import docx, xlsx, pdf, ảnh (OCR), chat sửa bản nháp, xem trước, duyệt rồi lưu | OUT (D-GEO-011, hỏi ChuDauTu). Giáo viên không có nút soạn hay import; bản mẫu giữ nhãn "Concept · Chặng 2/5" |
| REQ-GEO-53 | SRC-032 | Giáo trình riêng cho từng học sinh | OUT (D-GEO-011). Học sinh không thấy lộ trình riêng; nhãn "Concept · Chặng 6" |
| REQ-GEO-54 | SRC-044, SRC-072, SRC-029 | Đăng ký giáo viên thật, trường (quản trị), phụ huynh, chấm bài, nộp bài, giao theo bộ, kéo mặt phẳng và thiết diện động, trình chiếu, nhóm học, thông báo (chặng 2 đến 9) | OUT. Chỉ hiện trong bản mẫu với nhãn Concept |
| REQ-GEO-55 | SRC-018 | Mọi màn ngoài MVP có nhãn "Concept · Chặng N"; tài liệu không ghi mốc thời gian | IN như quy tắc tài liệu và nhãn |

## 5. Độ phủ (VoC coverage)
- Tổng REQ: **55** (REQ-GEO-01…55).
- Đối chiếu nguồn: 26 chức năng P0 của `DOMAIN-PROFILE.md` §5 → REQ-GEO-01…10, 12…27 (26 chức năng) cộng REQ-GEO-11 (yêu cầu Final PO-34) = 27 yêu cầu chức năng. Các quy tắc BR-01…13 → REQ-GEO-17, 18, 16/35, 08, 19, 02, 04, 04, 29, 29, 30, 28, 34. Ràng buộc kỹ thuật brief §5 (a…e) → REQ-GEO-37…39, 45; brief §7 (đa trường, hiệu năng, truy vấn, khóa lời giải, thiết bị, riêng tư, công nghệ) → REQ-GEO-37…45. Final PO-27…29, 33…36, 45 → REQ-GEO-52, 53, 10, 11, 05, 09, 52.
- Đã ánh xạ vào epic hoặc phi chức năng (IN, gồm REQ-GEO-55 là quy tắc tài liệu): **51** REQ; OUT có lý do và hệ quả: **4** (REQ-GEO-51…54). Tổng đã xử lý: 51 + 4 = 55 / 55.
- **Độ phủ: 100%** (≥ 90% yêu cầu).
