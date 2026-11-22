# Nhận xét và đề xuất MVP "Môn Hình học"

> Nguồn: `SPEC.md` (bản giới thiệu), `SPEC-technical.md`, và 8 file trong `input/ChatGPT Concept/` (00–07).
> Phần brainstorm do model Fable thực hiện, sau đó được đối chiếu lại với tài liệu gốc.
> Các nhận định về thị trường, pháp lý và điều khoản dịch vụ là suy luận, **chưa tra cứu**. Mọi con số KPI và mốc thời gian là đề xuất để bàn, chưa có số đo thực tế nào.

> **Cập nhật:** mục 4 (MVP) và câu 4 (bộ concept) trong tài liệu này đã được thay bằng [`MVP.md`](MVP.md) và [`PRODUCT-MASTER.md`](PRODUCT-MASTER.md). MVP hiện chỉ làm hình học không gian, có giáo viên mẫu và kho đề soạn sẵn. Tài liệu này được giữ làm bản ghi phần nhận xét và so sánh.

## Quyết định của chủ sản phẩm (21/11/2026)

| # | Câu hỏi | Quyết định | Hệ quả với MVP |
|---|---|---|---|
| 1 | Khối lớp mũi nhọn | **Cả THCS và THPT** | Hình phẳng (2D) không còn là phần phụ: phải được engine dựng và kiểm chứng như 3D. |
| 2 | Lời giải | **Gợi ý trước; lời giải chỉ hiện từ thời điểm giáo viên thiết lập** | Vai trò giáo viên, lớp và bài giao (bản nhẹ) vào MVP ngay. Việc khóa lời giải phải thực hiện ở server. |
| 3 | Rời runtime artifact | **Chấp nhận** | M0 dựng web app riêng có backend gọi AI. |
| 4 | Bộ concept ChatGPT | **Chưa chốt** — xem so sánh ở mục 6, chủ sản phẩm review rồi quyết | Chưa sửa file concept nào. |

Các mục 4 và 5 bên dưới đã cập nhật theo ba quyết định đầu.

## Kết luận chính

Bộ concept ChatGPT là một tài liệu tầm nhìn tốt nhưng là kế hoạch thực thi không hợp cho giai đoạn này. Nó đặt MVP là Teacher + Student + AI Grading trên nền multi-tenant, cần khoảng 12 người và 44 tuần mới tới pilot, và trong đó tool hình học chỉ còn là một tính năng phụ (LMS-003).

Đề xuất: hạ bộ concept xuống thành "tầm nhìn dài hạn", và làm một MVP khoảng 16 tuần quanh môn Hình học (ước lượng ban đầu là 12 tuần; tăng lên sau khi thêm hình phẳng và vai trò giáo viên theo quyết định 1 và 2). Điểm khác biệt cốt lõi nên là **hình và đáp số được kiểm chứng bằng tính toán**, không tin thẳng vào tọa độ AI sinh ra.

---

## 1. Nhận xét bộ concept ChatGPT

### Nên giữ
- **Nguyên tắc sản phẩm** (01 §7): "Teacher-led, AI-assisted", "Learning-first, not answer-first". Áp dụng được ngay cho tool hình học.
- **Tách "AI đề xuất" và "người duyệt"**, cùng với version bất biến cho đề, rubric, lời giải.
- **Kỷ luật trung thực** (00 §3–4, 07 §8): phân biệt Existing và Proposed, ghi rõ ảnh chụp là dữ liệu mẫu.
- **07 §5–6** (mapping và migration): phần sát thực tế nhất, dùng lại gần như nguyên vẹn.
- **Modular monolith** thay vì microservices, và AI Gateway phía server có version prompt/model.

### Điểm yếu và rủi ro
1. **Lệch với định hướng "bắt đầu từ hình học".** D-04 (thiết kế cả hệ sinh thái từ đầu) và D-05 (MVP = Teacher + Student + AI Grading) không dành tuần nào trong roadmap để cải thiện hình học. Học sinh chỉ chạm sản phẩm ở pilot, tuần 37–44.
2. **Ưu tiên bị đảo.** Trong 07 §7, "tọa độ AI không được kiểm chứng" (GAP-09) là P1, còn multi-tenant, rubric, Teacher KB là P0. Với sản phẩm dạy hình học, hình sai là lỗi nặng nhất.
3. **Không khớp nguồn lực.** Roadmap 36 tháng, team khoảng 12 người (06 §8). Hiện trạng là một file HTML khoảng 2.000 dòng.
4. **Giả định chưa kiểm chứng:**
   - Giáo viên sẵn sàng soạn rubric, duyệt KB và duyệt từng tiêu chí AI chấm.
   - AI chấm được bài tự luận toán viết tay. Đây là bài toán khó nhất trong cả bộ.
   - Có người mua: 01 §9 tự ghi mô hình kinh doanh là TBD.
5. **Answer Firewall ngược với giá trị hiện có.** Tool hiện tại đưa lời giải đầy đủ, concept lại chặn đáp án. Chưa ai quyết sản phẩm là "trợ lý tự học" hay "công cụ kiểm tra".
6. **Lệch phân khúc.** Pilot chốt lớp 10–12, trong khi tool mặc định cấp 2 và phục vụ cả hai cấp.
7. **Trộn ba mô hình** (bán cho học sinh, cho trường, cho cơ quan quản lý) trong một sản phẩm.
8. **"Decision đã chốt" chưa có bằng chứng**, mới là kết quả một cuộc trao đổi, chưa qua khảo sát người dùng.

### Các decision nên mở lại

| ID | Hiện tại | Đề xuất |
|---|---|---|
| D-01, D-02, D-03, D-10 | Accepted | Hạ xuống "giả thuyết tầm nhìn dài hạn" |
| D-04 | Thiết kế cả hệ sinh thái từ đầu | "Thiết kế các điểm nối tối thiểu, xây theo từng môn" |
| D-05 | MVP = Teacher + Student + AI Grading | MVP = môn Hình học cho học sinh tự học; giáo viên trình chiếu là P1 |
| D-06 | Pilot Toán 10–12 | Thu hẹp thành hình học không gian, khối lớp do chủ sản phẩm chốt |
| D-07 | Modular monolith + multi-tenant | Giữ modular monolith; hoãn multi-tenant, chỉ để sẵn `owner_id`, `org_id` nullable |
| D-08 | Teacher KB + Answer Firewall | Hoãn; thay bằng "thang gợi ý" và lời giải đã kiểm chứng |
| D-09 | Giáo viên duyệt điểm | Giữ làm nguyên tắc, chưa xây |

### Đề xuất sửa từng file

| File | Mục | Sửa gì |
|---|---|---|
| 00 | Header, §6 | Đổi trạng thái thành "Tầm nhìn dài hạn, không phải baseline triển khai". Thêm D-11: "Bắt đầu bằng môn Hình học". Cập nhật D-01 đến D-08 như bảng trên. |
| 00 | §4 | Sửa tên nguồn: bản concept gọi là `SPEC.md` nay là `SPEC-technical.md`; `SPEC.md` là bản giới thiệu mới. |
| 01 | §2, §8 | Giữ Vision làm đích dài hạn, thêm "điểm khởi đầu: làm thật tốt một môn". Chèn Phase 0 "Môn Hình học" và Phase 1 "Môn thứ 2 + lớp học nhẹ" trước Foundation hiện tại. |
| 01 | §10, §11 | North Star giai đoạn đầu: số bài hình học được học sinh khám phá và hiểu mỗi tuần. Thêm rủi ro "AI sinh hình hoặc đáp số sai" và "chưa có người mua". |
| 02 | Toàn bộ | Thêm Domain 0 "Geometry Subject Module" mức P0 (dựng hình, kiểm chứng, tương tác, gợi ý, luyện tập). Hạ ORG, ASM, KB, AF xuống P2. Viết lại §5–6 cho học sinh tự học. |
| 03 | Kiến trúc | Đưa `geometry-engine` (thuần hàm, có test), `SceneSpec` và interface theo môn thành trọng tâm. Đánh dấu phần tenancy, policy là "Later". |
| 04 | Data model | Thêm lược đồ tối thiểu cho MVP: `Problem`, `SceneSpec`, `Solution`, `VerificationReport`, `Interaction`, `Attempt`, `ConceptTag`. Phần còn lại chuyển xuống phụ lục. |
| 05 | Đầu file | Thêm Workflow 0 "học sinh giải, khám phá, xin gợi ý, tự kiểm tra" và trạng thái lời giải: sinh ra → đã kiểm chứng / gắn cờ → hiển thị. |
| 06 | §2, §3, §8 | Thay kế hoạch 52 tuần bằng kế hoạch khoảng 16 tuần ở mục 4; roadmap 36 tháng xuống phụ lục. Team 2–4 người. KPI đổi sang KPI hình học. |
| 07 | §7 | Đảo ưu tiên: GAP-09, 14, 15, 16 lên P0; GAP-02 đến GAP-06 xuống P2. GAP-01 chỉ còn "định danh nhẹ theo người dùng". Thêm gap "phụ thuộc runtime Claude artifact". |

Lưu ý: các đề xuất cho file 03, 04, 05 dựa trên bản đọc của model Fable; người tổng hợp chưa tự đọc lại ba file này.

---

## 2. Nhận xét SPEC.md (bản giới thiệu)

### Nên sửa
- **Ví dụ demo Bước 5 sai về mặt toán.** Giao tuyến của (SBD) với mặt đáy (ABCD) chính là đường BD; ảnh và mô tả lại đặt tên nó là "MN". Giáo viên xem demo sẽ thấy ngay. Nên đổi ví dụ, chẳng hạn giao tuyến của (SAC) và (SBD) là SO, hoặc mặt phẳng qua trung điểm SA, SB, SC. Cần chụp lại ảnh 06 và 07.
- **Mục 2 (Vấn đề) quá hẹp**: chỉ nói hình không gian, trong khi sản phẩm nhận cả hình phẳng và nay định vị là "môn Hình học". Nên nói rõ 3D là mũi nhọn, 2D đi kèm.
- **Thiếu mục "Giới hạn hiện tại"**: hình và lời giải do AI sinh ra chưa được kiểm chứng; chưa có tài khoản, dữ liệu dùng chung.
- **Bước 3**: thêm ghi chú chưa có nét đứt cho cạnh khuất như sách giáo khoa.
- **Thiếu "dành cho ai, dùng khi nào"** (tự học ở nhà, giáo viên chiếu trên lớp) và "khác gì GeoGebra hay app giải bài". Điểm khác: từ đề bài ra hình tự động, kèm nguyên lý, kèm thao tác.
- **Mục 7**: thay danh sách tính năng rời bằng mục tiêu, mốc và ưu tiên như mục 4 bên dưới.

### Gợi ý câu Vision (chọn một)
- **A (khuyến nghị):** "Mọi học sinh Việt Nam đều *nhìn thấy* và *tự tay dựng* được hình học. Hình Học Cùng AI là môn Hình học được dạy bởi một gia sư kiên nhẫn, với hình vẽ luôn đúng."
- **B (thiên về tin cậy):** "Trợ lý hình học đáng tin nhất cho học sinh Việt Nam: mỗi hình, mỗi đáp số đều được máy kiểm chứng, mỗi bước đều giải thích vì sao."
- **C (thiên về nền tảng):** "Bắt đầu từ Hình học, xây một cách học mới: thấy, thử, hiểu. Mỗi môn là một không gian tương tác hoàn chỉnh, mở rộng dần thành hệ sinh thái học tập."

### Gợi ý nội dung Định hướng
Ba nguyên tắc: đúng trước, đẹp sau; gợi ý trước, đáp án sau; xong một môn rồi mới sang môn tiếp theo.

Lộ trình bốn chặng: Hình học tự học → giáo viên trình chiếu và giao bài nhẹ → môn thứ 2 → lớp học và chấm bài.

---

## 3. Brainstorm cho môn Hình học

| # | Ý tưởng | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|
| 1 | **AI khai báo ràng buộc, engine tính tọa độ.** AI xuất "chóp S.ABCD, đáy vuông cạnh a, SA ⊥ đáy, SA = a"; engine dựng tọa độ và kiểm tra lại. | Rất cao | Trung bình | Đề lạ không khớp mẫu, cần fallback có cảnh báo | P0 |
| 2 | **Kiểm chứng đáp số bằng số.** Thay a = 1, tính góc, khoảng cách, thể tích từ tọa độ rồi so với đáp số AI; lệch thì gắn cờ. | Rất cao | Thấp–trung bình | Chỉ cho bài tính toán, không cho bài chứng minh | P0 |
| 3 | **Thang gợi ý 3 mức**: hướng nghĩ → bước kế tiếp → lời giải đầy đủ | Cao | Thấp | Học sinh bấm thẳng mức 3; cần đo | P0 |
| 4 | **Lời giải đồng bộ với hình**: bấm bước nào, hình tự highlight và dựng đường phụ của bước đó | Cao | Trung bình | Cần AI tham chiếu đúng đối tượng; làm sau ý 1 | P0 |
| 5 | **Đo đạc trên hình**, thêm trung điểm, điểm chia tỉ lệ, nét khuất | Cao | Trung bình | Thấp | P0 |
| 6 | **Thư viện hình mẫu tham số** (chóp, lăng trụ, hộp, nón, trụ, cầu) dựng không cần AI | Trung bình–cao | Thấp | Thấp | P0 |
| 7 | **Bài luyện có chấm tự động** (điền đáp số, trắc nghiệm) | Cao | Trung bình | Cần ngân hàng đề tốt | P1 |
| 8 | **Chế độ giáo viên trình chiếu**, link chia sẻ, xuất ảnh/PDF | Cao | Thấp–trung bình | Thấp | P1 |
| 9 | Thiết diện động: kéo mặt phẳng, thiết diện cập nhật trực tiếp | Cao về trình diễn | Trung bình–cao | Thuật toán cắt đa diện | P1 |
| 10 | Dựng hình 2D có ràng buộc thay cho SVG do AI vẽ | Trung bình | Cao | Phình phạm vi | P1/P2 |
| 11 | "Chụp bài làm, AI chỉ chỗ sai" | Cao | Cao | Đọc chữ viết tay; chính là bài toán AI Grading | P2 |
| 12 | Bản đồ khái niệm theo chương sách giáo khoa | Trung bình | Trung bình | Cần chuyên gia nội dung | P2 |
| 13 | AR đặt hình lên bàn | Thấp–trung bình | Cao | Ít giá trị học tập | Bỏ |

---

## 4. Đề xuất MVP "Môn Hình học"

**Mục tiêu:** trong khoảng 16 tuần có một web app độc lập mà học sinh THCS và THPT dùng hằng tuần để học hình học (phẳng và không gian), với hình và đáp số được kiểm chứng, theo cách "gợi ý trước, lời giải mở theo lịch của giáo viên".

**Người dùng**
- Học sinh THCS: chủ yếu hình phẳng (tam giác, tứ giác, đường tròn), cộng hình khối cơ bản.
- Học sinh THPT: hình không gian và Oxyz.
- Giáo viên Toán: tạo lớp, giao bài, đặt thời điểm mở lời giải; dùng để trình chiếu.

**Cách mở lời giải**
- Bài do giáo viên giao: học sinh chỉ có gợi ý (mức 1: hướng nghĩ; mức 2: bước kế tiếp). Lời giải đầy đủ, đáp số và phần "nguyên lý" của từng bước bị khóa tới thời điểm giáo viên đặt. Giáo viên có thể mở sớm hoặc lùi lại.
- Khóa ở server: trước giờ mở, server không gửi lời giải xuống trình duyệt, và chat cũng không được tiết lộ đáp số.
- Bài học sinh tự nhập (không có giáo viên): **giả định tạm** là phải đi qua gợi ý mức 1 và 2 rồi mới mở được lời giải. Cần bạn xác nhận (câu hỏi mới số 1 ở mục 5).

**Phạm vi**

| Trong phạm vi | Ngoài phạm vi |
|---|---|
| Web app riêng, có backend gọi AI, không phụ thuộc runtime artifact | Multi-tenant, trường, phụ huynh, cơ quan quản lý |
| Tài khoản học sinh và giáo viên, dữ liệu riêng từng người | Rubric, AI chấm tự luận, Teacher KB, Answer Firewall đầy đủ |
| Lớp bản nhẹ: mã lớp, giao bài, thời điểm mở lời giải | Trường, tenant, phân quyền nhiều tầng, nộp bài viết tay |
| Geometry engine, mô tả hình có version, kiểm chứng | Chat nhóm, video, lịch, thông báo |
| Đa diện; nón, trụ, cầu mức cơ bản | Môn khác; chứng minh hình thức; AR |
| Hình phẳng do engine dựng từ ràng buộc và kiểm chứng (các dạng phổ biến: tam giác, tứ giác, đường tròn); dạng lạ dùng SVG của AI, gắn nhãn "chưa kiểm chứng" | Công cụ dựng hình 2D tự do kiểu thước và compa |

**Tính năng**

| Mức | Tính năng |
|---|---|
| P0 | Tách module `solver`, `geometry-engine`, `scene3d`, `store`, `ui`, kèm backend gọi AI |
| P0 | Mô tả hình (SceneSpec) v1 và thư viện hình mẫu; fallback tọa độ AI có cảnh báo |
| P0 | Kiểm chứng ràng buộc và đáp số; nhãn "Đã kiểm chứng" / "Cần xem lại" |
| P0 | Sửa hạn chế 1, 4, 5, 6, 7, 8 trong `SPEC-technical.md` §7 (nhãn lặp, cảnh báo đường chéo nhau, chia tam giác, nét khuất, pinch-zoom và đặt lại góc nhìn, nhật ký theo bài) |
| P0 | Thêm điểm (trung điểm, tỉ lệ, tự do trên cạnh); đo độ dài, góc, khoảng cách; bấm chọn trực tiếp trên hình |
| P0 | Hình phẳng: engine dựng và kiểm chứng các dạng phổ biến của THCS |
| P0 | Thang gợi ý; lời giải đồng bộ với hình |
| P0 | Giáo viên: tạo lớp bằng mã, giao bài, đặt và đổi thời điểm mở lời giải; xem ai đã mở bài, đã dùng gợi ý nào |
| P0 | Khóa lời giải ở server theo thời điểm mở, áp dụng cả cho chat; có bộ test thử moi đáp án |
| P0 | Bộ đề chuẩn 100–150 đề có đáp án; ghi nhận sự kiện sử dụng |
| P1 | Link chia sẻ, chế độ trình chiếu, xuất ảnh/PDF |
| P1 | Bài luyện có chấm tự động theo chủ đề |
| P1 | Mặt phẳng kéo được bằng chuột và thiết diện động |

**Mốc phát triển** (giả định team 2–4 người: 1 full-stack/3D, 1 backend/AI, 1 giáo viên Toán cố vấn bán thời gian, có thể thêm 1 thiết kế kiêm QA)

| Tuần | Mốc | Kết quả kiểm được |
|---|---|---|
| 1–2 | M0 Nền | App chạy ngoài artifact với AI thật; bộ 100 đề chuẩn; đo tỉ lệ hình và đáp số đúng lần đầu |
| 3–5 | M1 Tin cậy | Engine, SceneSpec, kiểm chứng; unit test cho engine; đo lại trên bộ đề chuẩn |
| 6–8 | M2 Tương tác | Thêm điểm, đo đạc, nét khuất, chọn trực tiếp, điện thoại |
| 9–10 | M3 Hình phẳng | Engine dựng và kiểm chứng hình 2D; đo lại trên phần đề THCS của bộ đề chuẩn |
| 11–12 | M4 Sư phạm | Thang gợi ý, lời giải đồng bộ hình, nhật ký theo bài |
| 13–14 | M5 Giáo viên | Tài khoản, mã lớp, giao bài, thời điểm mở lời giải, khóa ở server |
| 15–16 | M6 Pilot kín | 1–2 lớp THCS và 1–2 lớp THPT, 3–5 giáo viên, dùng 2 tuần; quyết định đi tiếp hay dừng |
| 17–20 | M7 (P1) | Chia sẻ và trình chiếu, bài luyện có chấm |

Mốc thời gian là ước lượng thô cho team 2–4 người, cần ước lượng lại sau M0. Bộ đề chuẩn phải có cả đề THCS và THPT.

**KPI đề xuất** (ngưỡng là giả thuyết, chỉnh sau khi đo ở M0)
- **Độ đúng (cổng chặn):** ≥ 95% hình đúng ràng buộc, ≥ 90% đáp số đúng trên bộ đề chuẩn; không bài sai nào được gắn "Đã kiểm chứng".
- **Tương tác:** ≥ 60% phiên có ít nhất 1 thao tác trên hình; ≥ 40% lượt mở gợi ý mức 1–2 trước khi xem lời giải.
- **Khóa lời giải (cổng chặn):** 0 trường hợp lời giải hoặc đáp số lộ trước giờ mở trong bộ test thử moi đáp án.
- **Giáo viên:** ≥ 3/5 giáo viên pilot giao ít nhất 2 bài và có đặt thời điểm mở lời giải.
- **Giữ chân:** ≥ 30% học sinh pilot quay lại trong tuần thứ 2.
- **Vận hành:** thời gian trung vị ra hình dưới 40 giây; ghi nhận chi phí AI mỗi bài.
- **Định tính:** ≥ 3/5 giáo viên nói sẽ dùng để trình chiếu.

**Xong MVP khi:** đạt cổng độ đúng, toàn bộ P0 chạy trên máy tính và điện thoại, pilot không có lỗi chặn, và có số đo cho mọi KPI.

**Nối lên hệ sinh thái**

| Bước | Nội dung | Điều kiện mở |
|---|---|---|
| 1 | Hình học THCS và THPT: tự học, lớp bản nhẹ, giáo viên giao bài và đặt giờ mở lời giải (MVP) | — |
| 2 | Trình chiếu, bài luyện có chấm tự động, kết quả tổng hợp theo lớp | Pilot đạt cổng độ đúng và cổng khóa lời giải |
| 3 | Môn thứ 2 (ví dụ Đại số/Giải tích có đồ thị, hoặc Vật lý cơ học) dùng lại khung theo môn | Hình học đạt KPI giữ chân |
| 4 | Lớp, trường, chấm bài, Answer Firewall (phần lõi của concept ChatGPT) | Có ít nhất 1 trường trả tiền hoặc cam kết pilot |

**Thiết kế sẵn từ đầu** (rẻ bây giờ, đắt nếu làm lại)
- `schema_version` trên mọi JSON; tách `Problem`, `SceneSpec`, `Solution`, `VerificationReport`; mỗi lời giải lưu `model`, `prompt_version`.
- `geometry-engine` thuần hàm, không phụ thuộc giao diện hay Three.js.
- Interface theo môn: `parse`, `solve`, `verify`, `render`, `hint`.
- Lớp gọi AI phía server: giấu key, ghi chi phí, đổi được model.
- Mọi bản ghi có `owner_id`; `org_id`, `class_id` để nullable.
- Nhật ký sự kiện cho câu hỏi, gợi ý, thao tác.
- Nhãn khái niệm theo chương trình (lớp, chương, dạng bài).
- Trường `release_level` trên lời giải, làm nền cho việc kiểm soát đáp án sau này.

Chưa làm: phân quyền nhiều tầng, kế thừa policy, liên trường.

---

## 5. Rủi ro và câu hỏi mở

**Rủi ro**
1. **Độ đúng của AI trên đề thật**, nhất là đề từ ảnh. Hiện chưa có số đo nào vì mọi ảnh demo dùng dữ liệu mẫu. M0 phải đo ngay.
2. **Kênh phân phối.** Bản artifact phụ thuộc tài khoản Claude của người xem. Cần kiểm tra điều khoản về độ tuổi người dùng (chưa xác minh); nếu học sinh không dùng trực tiếp được thì tách thành app riêng là bắt buộc.
3. **Dữ liệu trẻ vị thành niên.** Khi có tài khoản cần thu thập tối thiểu và có tư vấn pháp lý.
4. **Chi phí AI mỗi bài** so với khả năng chi trả; cache và thư viện hình mẫu giúp giảm.
5. **Cạnh tranh.** GeoGebra mạnh về dựng hình, app giải bài mạnh về đáp án. Chỗ trống là "từ đề ra hình đúng, kèm nguyên lý, kèm thao tác"; lợi thế mất nếu hình không đáng tin.
6. **Phình phạm vi** quay về hệ sinh thái trước khi môn đầu tiên chứng minh giá trị.

**Đã trả lời** (xem bảng quyết định ở đầu tài liệu): khối lớp, cách mở lời giải, rời runtime artifact. Câu về hình 2D coi như đã rõ theo quyết định 1: phải kiểm chứng.

**Câu hỏi còn mở**
1. Bài học sinh tự nhập, không có giáo viên: lời giải mở sau khi đi hết gợi ý (giả định tạm), mở sau một khoảng chờ, hay không bao giờ mở nếu học sinh thuộc một lớp?
2. Bộ concept ChatGPT: chọn phương án nào ở mục 6?
3. Người trả tiền đầu tiên: phụ huynh/học sinh, giáo viên/trung tâm, hay trường?
4. Team thực tế gồm ai; có giáo viên Toán THCS và THPT nào nhận làm bộ đề chuẩn không?
5. Tài khoản giáo viên được xác nhận thế nào (tự đăng ký, hay cần mã mời)?
6. Giữ tên "Hình Học Cùng AI", hay đặt tên nền tảng ngay để dễ thêm môn thứ 2?

---

## 6. So sánh hai hướng (để review và chốt câu 4)

### Đối chiếu

| Tiêu chí | Concept ChatGPT | MVP "Môn Hình học" (đã cập nhật) |
|---|---|---|
| Định vị | Hệ sinh thái giáo dục liên trường | Một môn học làm trọn vẹn, mở rộng dần |
| Phạm vi môn và lớp | Toán 10–12 | Hình học THCS và THPT |
| Người dùng đợt đầu | Giáo viên, học sinh, quản trị trường | Học sinh, giáo viên |
| Giá trị cốt lõi | Giảm tải chấm bài cho giáo viên | Học sinh nhìn thấy và thao tác trên hình đúng |
| Việc khó nhất phải làm được | AI chấm bài tự luận viết tay | Engine dựng hình và kiểm chứng đáp số |
| Lời giải với học sinh | Answer Firewall theo policy, KB được duyệt | Gợi ý trước, lời giải mở theo giờ giáo viên đặt |
| Tool hình học hiện có | Một tính năng phụ (LMS-003) | Là sản phẩm |
| Hạ tầng đợt đầu | Multi-tenant, phân cấp tổ chức, audit, policy | Tài khoản, lớp bằng mã, `org_id` để sẵn |
| Thời gian tới pilot | Tuần 37–44 | Khoảng tuần 15–16 (ước lượng thô) |
| Team | Khoảng 12 người | 2–4 người |
| Rủi ro lớn nhất | Xây rộng trước khi có người dùng; AI chấm sai | Engine không phủ đủ dạng đề; khó thêm trường về sau nếu thiết kế dữ liệu ẩu |
| Bằng chứng thu được sớm | Giáo viên có tin AI chấm không | Hình và đáp số đúng bao nhiêu phần trăm; học sinh có quay lại không |

### Chỗ hai hướng đã gần nhau sau quyết định 2
Việc giáo viên đặt giờ mở lời giải chính là phần lõi của "answer-release" trong concept (ASM-008, AF-002, AF-004). Vai trò giáo viên, lớp và bài giao cũng đã vào MVP. Khác biệt còn lại là mức độ: MVP không có rubric, không AI chấm tự luận, không Teacher KB, không tenant.

### Ba phương án cho bộ concept

| Phương án | Nội dung | Được | Mất |
|---|---|---|---|
| **A (khuyến nghị)** | Giữ nguyên 8 file làm "tầm nhìn dài hạn"; thêm một file mới mô tả MVP Hình học là kế hoạch triển khai; mở lại D-04 đến D-08 | Team có một kế hoạch vừa sức; tầm nhìn không mất; không phải sửa nhiều | Hai bộ tài liệu, cần ghi rõ bộ nào đang có hiệu lực |
| B | Sửa trực tiếp 8 file theo bảng ở mục 1 | Một bộ tài liệu duy nhất | Tốn công; dễ kéo phạm vi hệ sinh thái quay lại MVP |
| C | Giữ concept làm baseline, chỉ chèn "Phase 0 Hình học" vào trước | Ít thay đổi nhất | Ưu tiên P0 trong concept vẫn là tenant, rubric, AI chấm; mâu thuẫn với hướng đã chọn |

Lý do nghiêng về A: ba quyết định đầu của bạn đã đi theo hướng "một môn trước", và A là phương án duy nhất không buộc team phải xây tenant, rubric hay AI chấm trước khi môn Hình học có người dùng.
