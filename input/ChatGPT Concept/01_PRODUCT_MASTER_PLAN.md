# 01 — PRODUCT MASTER PLAN
# Unified AI Education Ecosystem

## 1. Executive Summary

Unified AI Education Ecosystem (UAIEE) là nền tảng giáo dục số tập trung, kết nối cơ quan quản lý giáo dục, trường học, giáo viên, học sinh và phụ huynh trong một hệ sinh thái thống nhất. Nền tảng sử dụng chuẩn dữ liệu và quy trình chung, đồng thời cho phép mỗi trường duy trì bản sắc, cấu hình và quyền tự chủ trong phạm vi được phân cấp.

UAIEE không chỉ là một AI tutor hay Learning Management System. Đây là một **Education Operating Ecosystem** gồm:
- Learning & Assessment: dạy, học, giao bài, nộp bài, chấm và phản hồi.
- Teacher Operating System: AI Copilot và công cụ giảm tải hành chính cho giáo viên.
- Student Intelligence: lịch sử học tập, câu hỏi, tiến bộ, điểm mạnh và khoảng trống kiến thức.
- Parent Engagement: minh bạch tiến trình học tập và phối hợp gia đình – nhà trường.
- School Operating System: tổ chức, lớp, lịch, chương trình, hoạt động và truyền thông trường.
- Collaboration Network: nhóm học tập, cộng tác giáo viên, kết nối liên trường.
- Education Governance: truyền thông chính sách, theo dõi triển khai, báo cáo và chuẩn hóa vận hành.

**Chiến lược triển khai:** thiết kế kiến trúc và mô hình dữ liệu cho toàn hệ sinh thái ngay từ đầu; triển khai từng phase để kiểm soát rủi ro, chứng minh giá trị và tránh xây dựng một hệ thống quá rộng trước khi có adoption.

## 2. Vision, Mission, Positioning

### Vision
Một mạng lưới giáo dục thống nhất, nơi mọi trường học có thể kết nối, chia sẻ, học hỏi và phát triển; mọi giáo viên có công cụ tốt hơn để dạy học; mọi học sinh có lộ trình học tập rõ ràng; và cơ quan quản lý có dữ liệu đáng tin cậy để điều phối, hỗ trợ và cải tiến giáo dục.

### Mission
Kết nối con người, tri thức, quy trình và dữ liệu giáo dục bằng một nền tảng chung, sử dụng AI có trách nhiệm để tăng chất lượng học tập, giảm tải công việc và hỗ trợ quyết định dựa trên bằng chứng.

### Positioning
**Unified AI Education Ecosystem — One Education Network, Every School's Identity.**

### Không định vị là
- Một chatbot giải bài tập.
- Một phần mềm giao bài/chấm điểm đơn lẻ.
- Một hệ thống ERP trường học thuần túy.
- Một mạng xã hội học sinh mở, không kiểm soát.
- Một hệ thống AI thay thế giáo viên hoặc cơ quan quản lý.

## 3. Strategic Product Decisions

### 3.1 Federated Governance

Một bộ chuẩn chung toàn hệ thống:
- Định danh, cấu trúc tổ chức, dữ liệu học tập và mã tham chiếu.
- Quy trình tối thiểu: lớp học, giao bài, đánh giá, thông báo, báo cáo.
- Chuẩn bảo mật, quyền riêng tư, audit và interoperability.
- Chuẩn API, event và metadata chia sẻ.

Quyền tự chủ của trường:
- Branding, màu sắc, logo, tên gọi và trang trường.
- Cấu hình quy trình trong biên độ cho phép.
- Lịch học, hoạt động, thông báo nội bộ.
- Tổ chức lớp, phân công và phương pháp giảng dạy.
- Tạo/import nội dung KB và quyết định phạm vi chia sẻ.
- Thiết lập policy trường trong giới hạn policy cấp trên.

Nguyên tắc giải quyết xung đột policy:
`Platform baseline → Authority policy → School policy → Class/Teacher configuration`.
Policy cấp dưới chỉ được siết chặt hoặc tùy chỉnh trong vùng được cấp; không được vô hiệu hóa yêu cầu bảo mật, quyền riêng tư hay quy tắc bắt buộc cấp trên.

### 3.2 Centralized Deployment, Multi-level Access

- Một nền tảng ứng dụng tập trung.
- Multi-tenant logic, phân quyền theo tổ chức và phạm vi.
- Cơ quan quản lý xem dữ liệu tổng hợp hoặc dữ liệu định danh theo thẩm quyền.
- Trường quản lý không gian trường và thành viên của mình.
- Giáo viên truy cập lớp được phân công.
- Học sinh truy cập dữ liệu cá nhân và lớp đang tham gia.
- Phụ huynh truy cập hồ sơ con đã xác minh và được cấp quyền.
- Chia sẻ liên trường phải có policy, grant, consent phù hợp và audit.

### 3.3 Design Whole Ecosystem, Deliver in Phases

Mô hình dữ liệu phải sẵn sàng cho school hierarchy, parent relationship, collaboration, governance và federation ngay từ nền móng. Tuy nhiên, UI và workflow được phát hành theo phase để tránh làm MVP quá tải.

## 4. Product Outcomes

### Học sinh
- Hiểu cách giải và nguyên lý, không chỉ nhận đáp án.
- Nhận phản hồi đúng thời điểm.
- Theo dõi tiến bộ, điểm mạnh và phần cần củng cố.
- Có không gian học nhóm an toàn, có điều phối.

### Giáo viên
- Giảm thời gian soạn tài liệu, chấm bài và việc hành chính lặp lại.
- Nhìn thấy lịch sử câu hỏi và cách học của từng học sinh.
- Có bằng chứng để nhận diện hiểu sai và can thiệp phù hợp.
- Dành nhiều thời gian hơn cho sáng tạo sư phạm và quan hệ thầy trò.

### Phụ huynh
- Theo dõi tiến độ, bài tập, lịch học, nội dung đã học và phản hồi.
- Hiểu điểm mạnh/khó khăn của con theo bằng chứng.
- Phối hợp với giáo viên mà không gây áp lực hoặc gắn nhãn trẻ.

### Nhà trường
- Quản trị dữ liệu, lớp, lịch, chương trình, hoạt động và truyền thông.
- Theo dõi tình hình học tập và vận hành.
- Giữ bản sắc riêng trên một nền tảng chung.
- Học hỏi và hợp tác với trường khác.

### Cơ quan quản lý giáo dục
- Truyền tải thông tin/chính sách thống nhất.
- Theo dõi tiến độ triển khai các chương trình và cải cách.
- Quan sát chỉ số tổng hợp trong phạm vi thẩm quyền.
- Chuẩn hóa dữ liệu và quy trình mà không xóa bỏ quyền tự chủ hợp lệ của trường.

## 5. Ecosystem Capability Map

| Capability domain | Năng lực chính | Nhóm người dùng |
|---|---|---|
| Identity & Federation | Tổ chức, định danh, membership, policy inheritance | Tất cả |
| Learning Platform | Bài học, bài tập, tài liệu, nộp bài, phản hồi | Teacher, Student |
| Assessment & AI Grading | Rubric, AI proposal, teacher approval, phúc khảo | Teacher, Student, School |
| Teacher Copilot | Soạn bài, tạo đề, chấm, báo cáo, nhắc việc, thông báo | Teacher |
| Knowledge Governance | Teacher KB, school library, curriculum mapping, answer firewall | Teacher, School |
| Student Intelligence | Question history, mastery, misconceptions, progress timeline | Student, Teacher, Parent |
| Parent Engagement | Guardian verification, progress, schedule, communication | Parent |
| Collaboration Network | Study groups, chat, video, whiteboard, cross-school activities | Student, Teacher |
| School OS | Quản lý trường, lớp, nhân sự, lịch, chương trình, hoạt động | School |
| Education Governance | News, policy, implementation, aggregate reporting | Authority |
| Interoperability | APIs, data exchange, external school systems | Platform, School, Authority |
| Trust & Safety | Privacy, safeguarding, moderation, audit, consent | Tất cả |

## 6. Personas & Jobs-to-be-Done

### 6.1 Teacher
- Tôi muốn tạo bài tập từ đề chữ, ảnh, PDF, tài liệu và giáo án có sẵn.
- Tôi muốn xác định rubric và đáp án tham chiếu.
- Tôi muốn AI chấm sơ bộ nhưng tôi có quyền xem, sửa và phê duyệt.
- Tôi muốn xem toàn bộ lịch sử câu hỏi của học sinh trong phạm vi lớp mình.
- Tôi muốn biết học sinh sai ở khái niệm nào, bằng chứng ở đâu và nên can thiệp thế nào.
- Tôi muốn AI Copilot hỗ trợ giáo án, bài giảng, thông báo, báo cáo phụ huynh và việc hành chính.

### 6.2 Student
- Tôi muốn biết cần làm gì, hạn nộp khi nào và tài liệu nào được phép dùng.
- Tôi muốn gửi bài bằng chữ hoặc ảnh chụp vở.
- Tôi muốn được gợi ý từng bước thay vì bị đưa đáp án ngay.
- Tôi muốn hỏi lại AI/giáo viên và xem lịch sử trao đổi.
- Tôi muốn học cùng nhóm và theo dõi tiến bộ.

### 6.3 Parent / Guardian
- Tôi muốn xem con đã học gì, làm bài ra sao, tiến bộ thế nào.
- Tôi muốn nhận cảnh báo có ngữ cảnh và gợi ý hỗ trợ cụ thể.
- Tôi muốn trao đổi với nhà trường qua kênh được kiểm soát.

### 6.4 School Leadership
- Tôi muốn quản lý thành viên, lớp, lịch, chương trình, hoạt động và truyền thông.
- Tôi muốn có dashboard tổng hợp nhưng không xâm phạm quyền riêng tư không cần thiết.
- Tôi muốn chia sẻ hoạt động/tài nguyên với trường khác.

### 6.5 Education Authority
- Tôi muốn ban hành thông báo/chính sách đến đúng cấp và đúng đối tượng.
- Tôi muốn theo dõi triển khai theo thời gian và phát hiện nơi cần hỗ trợ.
- Tôi muốn chuẩn dữ liệu nhưng không can thiệp vào mọi quyết định sư phạm của trường.

## 7. Product Principles

1. Teacher-led, AI-assisted.
2. Learning-first, not answer-first.
3. Approved knowledge before generated claims.
4. Human review for consequential assessment.
5. Evidence-based student intelligence, never fixed labels.
6. Least privilege and privacy by default.
7. Federated standards with school identity.
8. Central platform, explicit data boundaries.
9. Accessibility and age-appropriate design.
10. Auditability for every high-impact action.
11. Interoperability without uncontrolled data replication.
12. Build foundations once, activate capabilities progressively.

## 8. Scope & Release Strategy

### Foundation / MVP
- Teacher, Student, limited Tenant/School Admin.
- Math Grades 10–12 pilot.
- Classes, assignment authoring, KB, rubric, submission, OCR, AI grading proposal, teacher approval, Answer Firewall.
- Basic question history and class insight.
- Basic announcements and notifications.

### Phase 2 — Student Intelligence & Parent
- Full question history analysis.
- Learning profile, mastery, progress timeline, misconception patterns.
- Calendar, reminders, parent portal, guardian verification.
- Teacher intervention and parent communication.

### Phase 3 — Collaboration Network
- Study groups, group chat, moderated video/calls.
- Shared whiteboard/documents and collaborative tasks.
- Teacher communities and cross-school groups.
- School discovery and knowledge exchange.

### Phase 4 — School Operating System
- School organization, staff, classes, timetables, attendance, curriculum, events, communications and dashboards.
- School identity customization and local policy settings.

### Phase 5 — Education Governance
- Official news and policy distribution.
- Implementation tracking, school reports, aggregate analytics.
- Authority-level dashboards and interoperability.

### Phase 6 — Ecosystem Optimization
- Broader subjects and grade levels.
- Shared content library with controlled licensing.
- Advanced AI orchestration and curriculum intelligence.
- National/regional partner integrations, subject to governance.

## 9. Business / Operating Model (TBD)

Potential operating model to validate:
- Platform operator manages core platform and technical standards.
- Authority accounts receive delegated scope based on legal mandate.
- Schools are onboarded as tenants or school units under a parent organization.
- School subscription, public-sector procurement or centrally funded deployment are alternative commercial models.
- No monetization model is assumed until buyer, procurement and data-controller responsibilities are validated.

## 10. Strategic KPI

### North Star
**Verified learning cycles completed per active class per week**, where a cycle includes assigned learning, student attempt, AI-supported analysis, teacher review and student feedback.

### Strategic outcomes
- Learning: mastery progression, feedback usefulness, intervention effectiveness.
- Teacher: time saved, adoption, AI suggestion usefulness, administrative burden.
- Student: participation, completion, help-seeking quality, revision behavior.
- Parent: verified activation, meaningful progress views, constructive engagement.
- School: workflow digitization, data quality, communication reach, operational visibility.
- Network: cross-school collaboration, shared resource adoption, successful policy implementation.
- Trust: zero critical unauthorized access, answer leakage and unapproved disclosure.

## 11. Risks

| Risk | Response |
|---|---|
| Vision quá rộng làm MVP mất trọng tâm | Phase gates và explicit non-goals |
| Central platform bị hiểu là tập trung hóa quyền quyết định | Federated governance và policy inheritance |
| AI chấm sai | Teacher approval, evidence, confidence, shadow evaluation |
| AI tiết lộ đáp án | Server-side Answer Firewall và adversarial tests |
| Dữ liệu học sinh bị truy cập sai | Multi-level RBAC/ABAC, tenant isolation, audit |
| Trường mất bản sắc | Branding, local configuration và school-owned content |
| Phụ huynh hiểu sai insight AI | Evidence-based wording, teacher context, no deterministic labels |
| Mở chat/video gây rủi ro trẻ em | Safeguarding, moderation, reporting, supervised rooms |
| Khó tích hợp hệ thống trường hiện hữu | API contracts, mapping, phased integration |
| Model provider thay đổi | AI gateway, evaluation sets, model registry |

## 12. Definition of Product Success

Sản phẩm thành công không phải vì có nhiều tính năng AI, mà vì:
- Giáo viên thực sự giảm tải và tin tưởng vào quy trình.
- Học sinh học tốt hơn, hiểu lỗi và biết cách cải thiện.
- Phụ huynh và trường có thông tin đúng, có ngữ cảnh.
- Trường hợp tác được nhưng không mất quyền tự chủ.
- Cơ quan quản lý có thông tin triển khai đáng tin cậy và đúng thẩm quyền.
- Hệ sinh thái vận hành an toàn, minh bạch và có khả năng mở rộng.
