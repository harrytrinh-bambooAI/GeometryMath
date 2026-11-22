# Team profile — dự án GeometryMath (feature GEO)

> *Đội làm việc thế nào.* Chạy theo chế độ goal run (`07_GUIDE_goal_run.md`). Hồ sơ này do PO tạo từ `artifacts/GOALS.md` và `brief.md`; chỗ chưa biết ghi `TBD — owner <người>`.

## 1. Người và quyền hạn
Chỉ ba người dưới đây có tệp câu hỏi `questions/Q_<Tên>.md`. Kỹ năng không bao giờ gửi câu hỏi; chủ đầu tư là người chuyển.

| Người (mã tệp) | Vai | Quyền hạn | Được hỏi về |
|---|---|---|---|
| ChuDauTu | chủ đầu tư, đồng thời là người dùng skill | cao nhất; quyết định phạm vi, ưu tiên, nhà cung cấp AI, pháp lý, cách hiểu chỉ số; Final của chủ đầu tư PO không được đổi | nghiệp vụ, phạm vi, ngân sách, nhà cung cấp AI, định danh học sinh, chỉ số thành công, ai soát kho đề, pháp lý trẻ em |
| GiaoVienCoVan | giáo viên Toán cố vấn nội dung | quyết định chuyên môn Toán và sư phạm trong phạm vi ChuDauTu giao | dung sai kiểm chứng, đáp số tương đương, định nghĩa "lộ đáp số", công cụ đo ẩn, giờ mở và đổi giờ theo thực tế lớp học, tùy chọn trợ giúp khi giao bài, chủ đề kho đề |
| TechLead | trưởng kỹ thuật | quyết định "cách làm" kỹ thuật; không đổi phạm vi | hạ tầng, cơ sở dữ liệu, bộ nhớ đệm, dữ liệu thử tải, ngưỡng tải, phiên bản Java và thiết bị tối thiểu, dữ liệu mẫu ở tầng dữ liệu |

- Vai nội bộ của run: **PO** (quyền cao nhất trong run, không đổi được Final), **BA**, **QC**, **ARCH** (kiến trúc sư dữ liệu và phi chức năng; vai ngoài skill, đăng ký trong profile này là TechLead trong vai agent: viết `artifacts/GEO/work/architecture/` và các `NFR-GEO-nnn`), **RV** (soát đối kháng, mô hình khác), **A** (điều phối cơ học). Agent mã Java làm việc trong `app/`, không sửa `artifacts/`.
- Điểm chưa có người: người soát kho đề, người xác nhận giáo viên thật, pháp nhân: `TBD — owner ChuDauTu`.
- Điều chưa có chủ rõ ràng đi vào tệp của ChuDauTu.

## 2. Công cụ và vòng khách hàng
- Không có công cụ thảo luận ngoài. Vòng khách hàng: PO soạn `questions/Q_<Tên>.md` kèm "Assumptions in use"; chủ đầu tư chuyển và dán câu trả lời lại; thay đổi đi qua vòng thay đổi (process §5).
- Thiết kế: `Design/mvp-*.png`, `Design/prototype/` (tham chiếu hành vi). Không có Figma.
- Bảng chung giữa agent: `artifacts/GEO/board/BOARD.md`. Không có tracker lỗi; bảng lỗi `TBD — owner ChuDauTu`.

## 3. Tệp tri thức
`DOMAIN-PROFILE.md`, `PRODUCT-MASTER.md`, `MVP.md`, `PO-DECISIONS.md`, `materials/` (giáo án và ngân hàng bài tập mẫu), `Design/prototype/README.md`. Chưa có tệp tri thức chưng cất; gói cho agent mới là `artifacts/GEO/board/PACK.md`. Nhật ký cải tiến: `improvement_log.md` của skill (chỉ đọc).

## 4. Tệp Input sống (skill được cập nhật)
Chỉ `Input/GeometryMath/GEO/brief.md` qua dòng "Revision log" (không viết lại). Không sửa `PO-DECISIONS.md`, `DOMAIN-PROFILE.md`, `MVP.md`, `PRODUCT-MASTER.md`; phát hiện sai thì ghi vào báo cáo và `01_understanding.md`.

## 5. Quy ước
- **Ngôn ngữ:** làm việc và tài liệu bằng tiếng Việt có dấu; tên định danh, mã, tên tệp, tên cột dữ liệu bằng ASCII.
- **Không mốc thời gian:** không ngày giao, số tuần, số tháng, hạn chót. Chỉ ghi thứ tự và cổng (R0…R3). Ngày chỉ dùng làm dấu vết sửa tệp (nhật ký thay đổi).
- **Sơ đồ mã định danh (Q9), tiền tố feature `GEO`:**

| Loại | Dạng | Ghi chú |
|---|---|---|
| Yêu cầu thô | `REQ-GEO-nn` | PO |
| Quy tắc nghiệp vụ | `BR-GEO-nn` | `BR-GEO-01…13` trùng `BR-01…13` của `DOMAIN-PROFILE.md` §8 |
| Yêu cầu phi chức năng | `NFR-GEO-nnn` | ARCH sở hữu |
| Story | `US-GEO-nnn` | hàng trăm = epic (E1 → 1xx) |
| Tiêu chí | `US-GEO-nnn-SCn` / `-FCn` | BA |
| Test case | `TC-GEO-nnnn` | QC |
| Quyết định | `D-GEO-nnn` | PO |
| Câu hỏi và trả lời | `QA-GEO-nn`; câu hỏi cho người: `Q-CDT-nn`, `Q-GV-nn`, `Q-TL-nn` | PO |
| Thay đổi | `CR-GEO-nn` | A |
| Hàng dữ liệu mẫu | `F-GEO-nnn` | BA, QC |
| Thông báo | `MSG-GEO-nn` | đề xuất cho A |
| Chỉ số | `M-GEO-nn` | PO |
| Phát hiện chéo epic | `XF-nn` | BA, QC |
| Quyết định kiến trúc | `ADR-GEO-nn` | ARCH |

- **Bố cục thư mục:** `Input/GeometryMath/GEO/brief.md`; `artifacts/GEO/{work,board,questions,review}`; `artifacts/GEO/work/epics/E1…E7`; `artifacts/GEO/work/architecture/` (ARCH); `app/` (mã Java); `references/profiles/bambooai/`.
- **Phiên bản đầu ra:** tệp v(n+1) riêng, không sửa tệp đã giao (process §7). Mô hình và bậc agent: PO, BA, QC dùng mô hình chính; RV dùng mô hình khác.
- **Đầu ra bị loại:** `12_api_spec.md` (hợp đồng API thuộc ARCH và TechLead), xuất Jira (đang tắt), bảng tính xuất sau khi QC xong.

## 6. Môi trường
Cục bộ: PostgreSQL cổng 54329, Redis cổng 6389 (brief §6 câu 2, giả định D-GEO-046). Java cài sẵn bản 26 (brief §7). Môi trường chạy thử và vận hành thật: `TBD — owner TechLead`. Thiết bị thử: iPad, iPhone, máy tính, điện thoại: `TBD — owner TechLead`.

## 7. Tracker
Chưa có. Bảng lỗi và nơi đặt post-mortem: `TBD — owner ChuDauTu`. Tạm thời lỗi ghi ở `epics/<EPIC>/qc_findings.md`.

## 8. Cấu hình lint
Lint của skill (`tools/lint/lint.config.json`) nằm trong thư mục skill, chỉ đọc; dùng giá trị mặc định. Đề xuất cho khi dựng bản riêng của dự án (chưa áp dụng): `persona_actors` = ["Học sinh", "Giáo viên mẫu", "Người dùng cực đoan"]; `max_steps` = 8; chạy `check_feature.py artifacts/GEO` trước R3 mỗi epic và trước khi bàn giao. Các phát hiện về thiếu story, test spec, test plan trước khi BA và QC làm là do chưa tới lượt, không phải lỗi.
