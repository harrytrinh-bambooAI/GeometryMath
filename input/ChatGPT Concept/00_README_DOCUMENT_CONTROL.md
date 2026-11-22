# UNIFIED AI EDUCATION ECOSYSTEM
## Master Documentation Pack

- **Phiên bản:** 1.0
- **Ngày:** 21/11/2026
- **Trạng thái:** Product baseline / Proposed
- **Định vị:** Unified AI Education Ecosystem
- **Governance:** Federated — chuẩn chung, trường tự chủ trong phạm vi được phép
- **Deployment:** Một nền tảng tập trung, phân quyền đa cấp, đa trường
- **Scope strategy:** Thiết kế toàn hệ sinh thái ngay từ đầu, triển khai theo giai đoạn
- **MVP đã chốt trước đó:** Teacher + Student + AI Grading
- **Pilot đã chốt trước đó:** Toán lớp 10–12
- **Kiến trúc ứng dụng đã chốt trước đó:** Modular monolith + multi-tenant
- **Knowledge Governance đã chốt trước đó:** Teacher-controlled KB + Answer Firewall

---

## 1. Mục đích bộ tài liệu

Bộ tài liệu này là baseline thống nhất để Product, UX/UI, Engineering, QA, AI/ML, Security, Operations và các bên giáo dục cùng làm việc. Tài liệu kết hợp:

1. Năng lực hiện có của GeometryMathSupport theo `SPEC.md` và `GUIDELINE.pdf`.
2. Các quyết định sản phẩm đã chốt: MVP, pilot, kiến trúc, governance, deployment.
3. Tầm nhìn hệ sinh thái giáo dục liên trường: học tập, vận hành nhà trường, cộng tác, phụ huynh, quản trị giáo dục và cơ quan quản lý.
4. Các yêu cầu mới về hồ sơ học tập, lịch sử câu hỏi, AI Copilot cho giáo viên, nhóm học tập, chat/video, School Discovery và Education Governance.

## 2. Danh mục tài liệu

| File | Nội dung |
|---|---|
| `00_README_DOCUMENT_CONTROL.md` | Document control, thuật ngữ, cách đọc và nguyên tắc sử dụng |
| `01_PRODUCT_MASTER_PLAN.md` | Vision, chiến lược, mô hình hệ sinh thái, phân kỳ sản phẩm, KPI chiến lược |
| `02_MASTER_PRD.md` | Master requirements, module breakdown, use cases, business rules, acceptance criteria |
| `03_SYSTEM_ARCHITECTURE.md` | Kiến trúc logical/technical, modular monolith, multi-tenant, AI architecture, integration |
| `04_DATA_MODEL_AND_PERMISSION_MATRIX.md` | Domain model, entities, ownership, data classification, permission matrix |
| `05_WORKFLOWS_AND_STATE_MACHINES.md` | Workflow end-to-end và state machine chính |
| `06_ROADMAP_KPI_AND_RELEASE_GATES.md` | Roadmap theo phase, KPI, pilot plan, release gates |
| `07_SOURCE_BASELINE_AND_GAP_REGISTER.md` | Kế thừa SPEC/GUIDELINE, hiện trạng, gap, migration và backlog |

## 3. Nguyên tắc đọc

- **Current / Existing:** được mô tả trong SPEC/GUIDELINE là năng lực của artifact hiện tại. Không đồng nghĩa đã production-ready.
- **Target / Proposed:** yêu cầu hoặc kiến trúc được đề xuất cho hệ sinh thái mới.
- **MVP:** phần cần triển khai đầu tiên để kiểm chứng vòng lặp giáo dục.
- **Later phase:** năng lực thuộc tầm nhìn dài hạn, không được mặc định là đã nằm trong MVP.
- **TBD:** cần discovery, quyết định nghiệp vụ, pháp lý hoặc xác nhận với trường trước khi triển khai.

## 4. Nguồn và tính trung thực của tài liệu

### Nguồn sản phẩm hiện tại
- `SPEC.md — Hình Học Cùng AI (GeometryMathSupport)`.
- `GUIDELINE.pdf`, gồm mô tả luồng và hình minh họa giao diện của ứng dụng hiện tại.

### Nguồn định hướng
- Các quyết định và yêu cầu sản phẩm đã được người dùng xác nhận trong quá trình trao đổi.

### Phân biệt sự thật và đề xuất
Các chức năng Teacher/Student, AI Grading, multi-school governance, parent portal, school operations, video groups và authority governance là phạm vi mục tiêu/đề xuất phát triển; không được hiểu là đã có trong artifact hiện tại. KPI là mục tiêu giả thuyết cần đo trong pilot, không phải kết quả đạt được.

## 5. Thuật ngữ

| Thuật ngữ | Định nghĩa |
|---|---|
| Platform Operator | Đơn vị vận hành nền tảng toàn hệ thống |
| Education Authority | Cơ quan quản lý giáo dục theo cấp/phạm vi được ủy quyền |
| School | Trường học, có identity và cấu hình riêng |
| Tenant | Ranh giới dữ liệu và cấu hình logic; có thể đại diện một trường hoặc một tổ chức quản lý |
| Federated Governance | Chuẩn chung toàn hệ thống, trường tự chủ trong policy/setting được phép |
| Teacher KB | Kho tri thức do giáo viên tạo/import, kiểm duyệt và cấp quyền sử dụng |
| Answer Firewall | Lớp kiểm soát server-side trước và sau khi AI tương tác với học sinh |
| AI Grading Proposal | Điểm/nhận xét do AI đề xuất, chưa phải kết quả cuối |
| Approved Grade | Điểm cuối do giáo viên có thẩm quyền phê duyệt |
| Student Intelligence Profile | Hồ sơ tiến bộ học tập dựa trên bằng chứng, không phải nhãn cố định về năng lực |
| Teacher Copilot | Trợ lý AI hỗ trợ giáo viên soạn bài, chấm bài, quản lý và giao tiếp |
| School Discovery | Khả năng khám phá hoạt động, tài nguyên và cơ hội hợp tác giữa các trường |

## 6. Product decisions đã chốt

| ID | Quyết định | Trạng thái |
|---|---|---|
| D-01 | Unified AI Education Ecosystem | Accepted |
| D-02 | Federated governance | Accepted |
| D-03 | Một nền tảng tập trung, phân quyền đa cấp/đa trường | Accepted |
| D-04 | Thiết kế toàn hệ sinh thái từ đầu, triển khai theo phase | Accepted |
| D-05 | MVP: Teacher + Student + AI Grading | Accepted |
| D-06 | Pilot: Toán lớp 10–12 | Accepted |
| D-07 | Modular monolith + multi-tenant | Accepted |
| D-08 | Teacher-controlled KB + Answer Firewall | Accepted |
| D-09 | Giáo viên phê duyệt điểm cuối cùng | Product guardrail |
| D-10 | Mỗi trường giữ bản sắc riêng trên nền tảng chung | Accepted |

## 7. Các quyết định cần xác nhận trước production

- Mô hình pháp nhân/đơn vị ký hợp đồng và chủ thể quản lý dữ liệu.
- Phân cấp cơ quan quản lý và phạm vi quyền của từng cấp.
- Quy định pháp lý áp dụng với dữ liệu trẻ em, hồ sơ học tập, lưu trữ và chia sẻ dữ liệu.
- Vùng lưu trữ dữ liệu, nhà cung cấp cloud và model provider.
- Chính sách retention, deletion, export và consent.
- Chuẩn danh mục trường, lớp, môn, chương trình và mã định danh tích hợp.
- Quy trình cấp tài khoản học sinh và xác minh quan hệ phụ huynh.
- Cơ chế xử lý khiếu nại, sửa điểm, phúc khảo và tranh chấp dữ liệu.
