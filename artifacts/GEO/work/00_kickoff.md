# Kickoff (Gate −1) — GEO

> **Input snapshot (Q2):** như `00_inputs.md`. Không có luồng thảo luận ngoài; chủ đầu tư trao yêu cầu trực tiếp (22-11-2026).
**Status:** Not reviewed (TBD) — goal run, ruled by PO 22-11-2026 (R0 do PO lấy, chủ đầu tư xem sau). Nguồn: `artifacts/GOALS.md`, `Input/GeometryMath/GEO/brief.md`.
**Output scope (chủ đầu tư, 22-11-2026):** tới "sẵn sàng R3" cho bảy epic: story, AC, test case; song song kiến trúc dữ liệu, phi chức năng và khung mã Java. Không viết mã chức năng, không tích hợp AI thật, không chặng 2 đến 9. Không tạo: `12_api_spec.md`, xuất Jira, bảng tính xuất (ghi ở `team_profile.md` §5). Giữ: nhật ký Q&A và CR.
**Agreed defaults (OQ-1…n):** năm câu mở của brief §6 dùng mặc định ghi trong brief (D-GEO-044…049); mọi mặc định khác ở `03_decisions.md`. docs: Không mốc thời gian.

## Feature profile and tailoring (method §9 — also the dimensions removed)
- **Tên, mã (Q9):** Hình Học Không Gian Cùng AI (MVP); mã feature `GEO`; app: web app riêng, máy chủ Java; loại feature: hệ thống nhiều vai có trạng thái và sự kiện, màn hình tương tác 3D, thành phần AI, ràng buộc phi chức năng nặng (đa trường, 1000 đồng thời). Mốc phát hành: không đặt (D-GEO-002).
- **Thay cho F1:** mô hình thực thể và trạng thái, bảng chỉ số, hàm trạng thái giờ mở (`02_domain_model.md`).
- **Nguồn theo loại đầu vào:** brief, sổ quyết định, hồ sơ domain, ảnh thiết kế, bản mẫu tương tác, tài liệu mẫu giáo viên. Tiền tố nguồn: `SRC-nnn`.
- **Chiều bị loại (kèm lý do):**
  | Chiều | Lý do loại |
  |---|---|
  | Bảng tính xuất (xlsx) và `12_api_spec.md` | nằm ngoài đầu ra của run; hợp đồng API thuộc ARCH |
  | Xuất Jira | đang tắt |
  | Luồng thảo luận ngoài, Figma | không có |
  | Màn hình và logic phụ huynh, trường, cơ quan, chấm bài, nộp bài, soạn và import (chặng 2 đến 9) | ngoài MVP (D-GEO-011, 078) |
  | Chuẩn xác nhận của khách hàng qua công cụ thảo luận | thay bằng `questions/Q_<Tên>.md`, chủ đầu tư chuyển |
  | Ước lượng theo ngày công | cấm mốc thời gian |
- **Quy tắc không áp dụng:** quy tắc "thread captured Final / Open" (không có thread); lint test plan đòi ngày cho known gap: dùng cột "Needed by" ghi cổng (R3 của epic), không ghi ngày.

## QC test needs (fixture, environments and devices, data to obtain, hard-to-test areas, roles, executor and tooling)
- **Fixture:** dữ liệu mẫu (D-GEO-025, 054) và bộ đề chuẩn từ `materials/` (HKG-01…06) mở rộng tới ≥ 40 đề; bộ câu thử moi đáp án; bộ dữ liệu thử tải (D-GEO-049). Nhãn kiểm chứng không lấy từ file mà từ engine.
- **Môi trường và thiết bị:** iPad, iPhone (học sinh); trình duyệt máy tính và điện thoại (giáo viên). Phiên bản tối thiểu: TBD — owner TechLead. PostgreSQL và Redis cục bộ (D-GEO-046).
- **Vùng khó kiểm:** đúng lúc tới giờ mở; đỉnh 1000 học sinh mở lời giải; moi đáp án bằng ngôn ngữ tự nhiên; AI giả lập; đồng hồ máy khách lệch; cách ly giữa trường; hình 3D trên màn nhỏ.
- **Vai cần thử:** học sinh trong lớp, học sinh lớp khác, học sinh trường khác, Giáo viên mẫu, người giả làm giáo viên.
- **Người chạy và công cụ:** TBD — owner ChuDauTu (ai chạy vòng thử); công cụ thử tải và thử API: TBD — owner TechLead. Kế hoạch kiểm: `review/test_plan.md` (QC), §14 do PO duyệt.
- **Ca bắt buộc theo `DOMAIN-PROFILE.md` §14:** khóa ở tầng dữ liệu máy chủ trả về; moi đáp án qua hỏi đáp; ca giờ mở (chưa đặt, sắp mở, đã mở, đổi giờ, mở ngay, đang xem đúng lúc tới giờ); ca nhãn (khớp, không khớp, đề lạ); ca thiết bị; ca phân quyền.

## Epics and order
Thứ tự theo `DOMAIN-PROFILE.md` §14, chủ đầu tư giữ nguyên (xong mốc trước rồi mới sang mốc sau; epic chờ câu trả lời không chặn epic khác):

| Thứ tự | Epic | Chức năng | Story sơ bộ |
|---|---|---|---|
| 1 | E1 Nền tảng và gọi AI phía máy chủ; bộ đề chuẩn | TRU-01, TRU-03, IAM-01 | 8 |
| 2 | E2 Hình đúng | GEO-02, 03, 04 | 5 |
| 3 | E3 Thao tác hình 3D | GEO-05, 06, 07 | 7 |
| 4 | E4 Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học | GEO-01, 08, 09, 10, 11, ANS-04 | 12 |
| 5 | E5 Kho đề của Giáo viên mẫu | KNW-01, 02 | 5 |
| 6 | E6 Giao bài và kiểm soát lời giải | IAM-02, LRN-01, 02, 04, ANS-01, 02, 03, TRU-02 | 12 |
| 7 | E7 Theo dõi lớp và ghi sự kiện | LRN-03, PRF-01 | 4 |

Song song: ARCH (`work/architecture/`), khung mã Java (`app/`). Xuyên suốt: US-GEO-104 và các ràng buộc phi chức năng.

## Risks and review plan (R0–R3)
| Rủi ro | Cách xử lý |
|---|---|
| Lộ đáp số qua hỏi đáp và kênh phụ | BR-GEO-01, 02, 18; ca thử; D-GEO-065 |
| Nhãn "Đã kiểm chứng" sai | nhãn chỉ do engine; M-GEO-05 phải bằng 0 |
| Đa trường mâu thuẫn tài liệu cũ | D-GEO-050 |
| Ngưỡng hiệu năng chưa chốt | giữ nhãn "đề xuất" |
| Khung thiết kế có điều khiển không có nguồn | D-GEO-078 |
| Các agent song song va chạm | `board/CONVENTIONS.md` §1, §2 |

| Điểm | Ai lấy | Khi nào | Ghi vào |
|---|---|---|---|
| R0 kickoff | PO | trước Gate 0 | `review_status.md` |
| R1 hiểu biết, công thức, quyết định | PO | sau `03_decisions.md` | `review_status.md` |
| R2 bản đồ story | PO | sau `04_story_map.md` | `review_status.md` |
| R3 mỗi epic | chủ đầu tư, sau run | sau khi epic "sẵn sàng R3" | `review_status.md` |
