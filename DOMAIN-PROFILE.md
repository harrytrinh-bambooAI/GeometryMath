# Hồ sơ domain — Hình Học Cùng AI và hệ sinh thái giáo dục

- **Mục đích:** làm đầu vào cho skill PO/BA/QC sinh BRD, story map, user story, acceptance criteria và test case theo từng epic.
- **Nguồn có hiệu lực, theo thứ tự ưu tiên:** `PO-DECISIONS.md` → `PRODUCT-MASTER.md` và `MVP.md` → `STAGES-AFTER-MVP.md` (chỉ phần đã dẫn nguồn, xem PO-19).
- **Phạm vi dùng được:** hồ sơ này đủ chi tiết để viết story và tiêu chí chấp nhận cho **MVP**. Với chặng 2 đến 9, nó chỉ cho bức tranh chung; chi tiết lấy ở `STAGES-AFTER-MVP.md` và phải đối chiếu `PO-DECISIONS.md`.
- **Quy tắc bắt buộc bổ sung sau phản biện** (đọc trong `PO-DECISIONS.md`): PO-04 không xếp hạng ở mọi cấp · PO-08 và PO-17 giới hạn công cụ đo · PO-09 và PO-23 giới hạn tuyệt đối với phụ huynh · PO-16 tự học kế thừa khóa · PO-18 quyền thêm đề · PO-20 xác minh phụ huynh · PO-21 chặn bởi câu hỏi pháp lý · PO-22 giờ mở áp dụng mọi kênh · PO-24 dữ liệu định danh · PO-14 dữ liệu mẫu · PO-15 nhãn "Concept · Chặng N".
- **Nguồn ban đầu khi soạn:** `PRODUCT-MASTER.md` và `MVP.md`. Tám file trong `input/ChatGPT Concept/` chỉ là nguồn chi tiết cho tầm nhìn; khi mâu thuẫn thì `PRODUCT-MASTER.md` và `MVP.md` đúng.
- **Quy ước đánh dấu:** *(suy diễn)* là chỗ người soạn tự suy ra, chưa có trong nguồn; *(giả định)* là điều nguồn đang tạm coi là đúng; "chưa xác định" là chỗ chưa ai quyết.
- **Về thời gian:** tài liệu chỉ ghi thứ tự chặng, không ghi thời hạn. Mọi ngưỡng chỉ số là đề xuất để bàn; chưa có số đo thực tế nào.

---

## 1. Tóm tắt

**Sản phẩm là gì.** Một nền tảng học tập bắt đầu bằng một môn: Hình học, mở đầu là hình học không gian. Học sinh nhìn và thao tác trên hình 3D đúng, được gợi ý từng mức, và chỉ xem lời giải từ thời điểm giáo viên thiết lập. Giáo viên có kho đề soạn sẵn, giao bài, đặt giờ mở lời giải và theo dõi lớp. Hướng dài hạn là một hệ sinh thái giáo dục mở rộng qua chín chặng nối tiếp.

**Cho ai.** Trước mắt là học sinh và giáo viên Toán THCS và THPT. Về sau thêm phụ huynh, nhà trường và cơ quan quản lý giáo dục.

**Khác biệt.**
1. Hình và đáp số được kiểm chứng bằng tính toán, không tin thẳng kết quả AI sinh ra.
2. Học trước, đáp án sau: gợi ý trước, lời giải bị khóa ở máy chủ tới giờ giáo viên đặt, kể cả trong hỏi đáp.
3. Giáo viên là người quyết định cuối; mọi kết quả AI (lời giải, điểm, nhận định) chỉ là đề xuất cho tới khi có người duyệt.
4. Lời giải đi cùng hình: bấm bước nào, hình tô màu đối tượng của bước đó, kèm "Vì sao nghĩ ra bước này?".

**Trạng thái hiện tại.** Đang có một trang web một file chạy trong Claude artifact: học sinh nhập đề bằng chữ hoặc ảnh, AI chép đề, vẽ hình 2D hoặc 3D, giải từng bước, có công cụ thao tác hình, thuật ngữ bấm được, hỏi đáp và lịch sử. Bản này chưa kiểm chứng hình và đáp số, không có tài khoản, không có vai giáo viên, lời giải hiện ngay. MVP (chặng 1) ở trạng thái đề xuất chờ chủ sản phẩm duyệt; đã có bản thiết kế giao diện và bản mẫu tương tác chạy bằng dữ liệu mẫu. Chưa có người dùng thật và chưa có số đo nào.

## 2. Bối cảnh domain giáo dục

**Chủ thể và quan hệ.**
- Giáo viên dạy một hay nhiều lớp; lớp thuộc một trường và một năm học; học sinh là thành viên của lớp.
- Giáo viên giao bài cho lớp; học sinh làm bài; giáo viên phản hồi và (về sau) chấm điểm.
- Phụ huynh gắn với học sinh qua quan hệ được trường xác minh, và trao đổi với giáo viên.
- Nhà trường quản lý giáo viên, lớp, lịch và truyền thông; các trường có thể hợp tác với nhau.
- Cơ quan quản lý giáo dục ban hành văn bản cho các trường trong phạm vi thẩm quyền và theo dõi triển khai.
- AI là tác nhân của hệ thống, không phải một vai: nó soạn nháp, dựng hình, gợi ý, đề xuất; không tự quyết.

**Vấn đề hiện tại của từng nhóm.** Các ý dưới đây lấy từ tài liệu tầm nhìn và chưa được kiểm chứng bằng khảo sát người dùng.

| Nhóm | Vấn đề |
|---|---|
| Học sinh | Khó hình dung hình không gian từ đề chữ; công cụ giải bài đưa đáp án ngay nên không học được cách nghĩ; không biết mình vướng ở đâu. |
| Giáo viên | Tốn thời gian soạn đề, vẽ hình, chấm bài và việc hành chính; không thấy học sinh hỏi gì, vướng bước nào; lo học sinh dùng AI để chép đáp án. |
| Phụ huynh | Không rõ con học gì, tiến bộ ra sao; thông tin tới muộn hoặc thiếu ngữ cảnh; không muốn tạo thêm áp lực cho con. |
| Nhà trường | Công cụ rời rạc; khó tổng hợp theo tổ bộ môn; muốn giữ bản sắc riêng khi dùng nền tảng chung. |
| Cơ quan quản lý | Khó biết văn bản đã tới đúng nơi và được triển khai tới đâu; thiếu số liệu tổng hợp đáng tin. |

## 3. Bản đồ sub-domain

| Mã | Sub-domain | Mục đích | Năng lực chính | Chặng | Trong MVP |
|---|---|---|---|---|---|
| GEO | Môn học tương tác | Học sinh thấy, thử và hiểu trên hình đúng | Dựng hình từ ràng buộc, kiểm chứng, thao tác 3D, gợi ý, lời giải đồng bộ hình | 1 (hình không gian), 2 (hình phẳng), 4 (môn thứ hai) | Có |
| LRN | Dạy và học, lớp học | Khép kín vòng giáo viên giao, học sinh làm | Lớp, bài giao, theo dõi lớp; về sau nộp bài, phản hồi | 1 (lớp mẫu), 3 (lớp thật), 5 (nộp bài) | Một phần |
| ANS | Kiểm soát đáp án | Bảo đảm học trước, đáp án sau | Giờ mở lời giải, khóa ở máy chủ, chặn lộ đáp án qua AI | 1 (giờ mở, khóa), 5 (đầy đủ) | Một phần |
| KNW | Kho tri thức giáo viên | Nội dung đã duyệt đứng trước nội dung AI tự sinh | Kho đề và tài liệu có chủ, version, trạng thái duyệt, phạm vi | 1 (kho đề mẫu), 2 (giáo viên thêm đề), 5 (đầy đủ) | Mầm |
| ASM | Đánh giá và chấm bài | Giảm thời gian chấm mà giáo viên vẫn quyết | Rubric, AI đề xuất điểm, giáo viên duyệt, công bố, phúc khảo | 2 (bài luyện chấm tự động), 5 | Chưa |
| PRF | Hồ sơ học tập | Nhận định về học sinh dựa trên bằng chứng | Lịch sử câu hỏi, mức nắm khái niệm, can thiệp của giáo viên | 1 (nhật ký tương tác), 6 | Mầm |
| COP | Trợ lý giáo viên | Soạn nháp thay giáo viên, giáo viên duyệt | Nháp bộ đề, giáo án, rubric, thông báo, tổng hợp lỗi sai | 5 | Chưa |
| PAR | Phụ huynh | Phụ huynh theo dõi và phối hợp đúng mức | Xác minh quan hệ, tiến độ, thông tin đã duyệt, trao đổi | 6 | Chưa |
| COL | Giao tiếp và cộng tác | Học nhóm an toàn, chia sẻ giữa các trường | Nhóm học, chat, video có điều phối, cộng đồng giáo viên, cấp phép chia sẻ | 7 | Chưa |
| SCH | Vận hành trường | Trường quản trị trên nền tảng chung | Nhân sự, lớp, thời khóa biểu, điểm danh, thông báo, báo cáo, nhận diện riêng | 8 | Chưa |
| GOV | Quản trị giáo dục | Truyền tải và theo dõi chính sách | Phát hành văn bản, theo dõi xác nhận và triển khai, số liệu tổng hợp | 9 | Chưa |
| IAM | Định danh, tổ chức, liên thông | Đúng người, đúng phạm vi | Tài khoản, vai theo phạm vi, tổ chức nhiều cấp, tích hợp hệ thống ngoài | Xuyên suốt; 1 (đăng nhập nhẹ), 3 (tài khoản chính thức), 5 (nhiều trường) | Tối thiểu |
| TRU | An toàn và tin cậy | AI đáng tin, dữ liệu trẻ em được bảo vệ | Đo độ đúng, thử moi đáp án, riêng tư, audit, kiểm duyệt, công tắc an toàn | Xuyên suốt | Tối thiểu |

Chín chặng theo thứ tự: (1) MVP hình không gian; (2) Trọn môn Hình học; (3) Lớp học thật; (4) Môn học thứ hai; (5) Đánh giá và trợ lý giáo viên; (6) Hồ sơ học tập và phụ huynh; (7) Cộng tác và liên trường; (8) Vận hành nhà trường; (9) Quản trị giáo dục. Mỗi chặng chỉ mở khi đạt điều kiện mở (mục 11).

## 4. Vai trò

### 4.1 Học sinh (THCS, THPT)
- **Mục tiêu:** hiểu cách giải và nguyên lý, không chỉ có đáp số.
- **Việc cần làm:** xem bài được giao và biết lời giải mở lúc nào; thao tác, đo đạc trên hình; xin gợi ý từng mức; hỏi lại khi vướng; xem lời giải từng bước khi được mở; tự học với đề của mình; về sau nộp bài, xem phản hồi và tiến bộ, học nhóm.
- **Nỗi đau:** khó hình dung hình không gian; bị đưa đáp án ngay hoặc không được trợ giúp gì; hình sai làm hiểu sai.
- **Thiết bị:** iPad hoặc iPhone. Giao diện phải ưu tiên cảm ứng.
- **Quyền chính:** bài của lớp mình, dữ liệu của mình, gợi ý và hỏi đáp trong phạm vi được phép.
- **Giới hạn:** không thấy lời giải hay đáp số trước giờ mở; không thấy bài và câu hỏi của bạn khác; không thấy đề xuất chấm của AI hay ghi chú riêng của giáo viên.

### 4.2 Giáo viên
- **Mục tiêu:** dạy tốt hơn với ít thời gian soạn và chấm hơn, vẫn giữ quyền quyết định.
- **Việc cần làm:** chọn đề từ kho, xem hình và lời giải, giao bài, đặt và đổi giờ mở lời giải, theo dõi ai làm tới đâu và hỏi gì; về sau tự thêm đề, duyệt lời giải AI, soạn rubric, duyệt điểm, ghi nhận can thiệp, trao đổi với phụ huynh.
- **Nỗi đau:** soạn đề và vẽ hình tốn công; không thấy quá trình nghĩ của học sinh; lo AI làm lộ đáp án hoặc chấm sai.
- **Thiết bị:** web trên máy tính là chính; điện thoại để kiểm tra nhanh (xem lớp, mở hoặc đổi giờ lời giải).
- **Quyền chính:** lớp mình dạy; kho đề của mình; bài nộp, đề xuất chấm, lịch sử câu hỏi của học sinh mình dạy.
- **Giới hạn:** không thấy lớp của giáo viên khác. Trong MVP chỉ có một tài khoản "Giáo viên mẫu" với lớp mẫu; chưa đăng ký giáo viên thật, chưa tự soạn đề hay sửa lời giải.

### 4.3 Phụ huynh (từ chặng 6)
- **Mục tiêu:** biết con học gì, tiến bộ thế nào, và phối hợp với giáo viên mà không tạo áp lực.
- **Việc cần làm:** xem tiến độ, bài tập, điểm đã duyệt, lịch sắp tới; đọc điểm mạnh và phần cần luyện đã được giáo viên duyệt; nhắn giáo viên qua kênh được trường cho phép.
- **Nỗi đau:** thông tin rời rạc, tới muộn, thiếu ngữ cảnh.
- **Thiết bị:** iPhone là chính, có thể dùng iPad.
- **Quyền chính:** chỉ thông tin của con mình, sau khi quan hệ được trường xác minh.
- **Giới hạn:** chỉ thấy bản đã duyệt; mặc định không thấy lịch sử câu hỏi của con, ghi chú riêng của giáo viên, đề xuất chấm của AI, hay trao đổi riêng giữa các học sinh. Quyền bị thu hồi khi quan hệ hoặc ủy quyền chấm dứt.

### 4.4 Quản trị trường và ban giám hiệu (từ chặng 8)
- **Mục tiêu:** vận hành trường trên một nền tảng, có số liệu tổng hợp, giữ bản sắc riêng.
- **Việc cần làm:** quản lý nhân sự, lớp, năm học, thời khóa biểu, thông báo; xem báo cáo theo tổ bộ môn; xác minh quan hệ phụ huynh; duyệt hợp tác liên trường.
- **Thiết bị:** web trên máy tính (Final, PO-01).
- **Quyền chính:** dữ liệu trong phạm vi trường mình.
- **Giới hạn:** không thấy dữ liệu trường khác; mặc định không thấy bản nháp của giáo viên hay đề xuất chấm của AI; xem bài nộp và lịch sử câu hỏi theo policy.

### 4.5 Cơ quan quản lý giáo dục (từ chặng 9)
- **Mục tiêu:** truyền tải chính sách đúng đối tượng và biết việc triển khai tới đâu.
- **Việc cần làm:** phát hành văn bản, theo dõi xác nhận và triển khai theo từng trường, xem số liệu tổng hợp.
- **Thiết bị:** web trên máy tính (Final, PO-01).
- **Giới hạn:** chỉ trong phạm vi được ủy quyền; chỉ số liệu tổng hợp; không duyệt hồ sơ từng học sinh trừ khi có ủy quyền pháp lý tường minh; không can thiệp quyết định sư phạm của trường.

### 4.6 Quản trị nền tảng
- **Mục tiêu:** vận hành, bảo mật, hỗ trợ.
- **Quyền chính:** cấu hình nền tảng, công tắc an toàn (tắt AI chấm, tắt AI phía học sinh).
- **Giới hạn:** không mặc nhiên xem nội dung học tập; truy cập khẩn cấp phải có mục đích, thời hạn hiệu lực và dấu vết kiểm tra.

### 4.7 Vai phụ
| Vai | Ghi chú |
|---|---|
| Người soát đề (chuyên môn Toán) | Soát kho đề và lời giải AI trước khi học sinh thật dùng. Trong MVP là người ngoài hệ thống, chưa có tài khoản riêng; ai đảm nhận thì chưa xác định. |
| Tổ trưởng hoặc điều phối học thuật | Xem tổng hợp theo tổ bộ môn, chấm chéo (tầm nhìn, chặng 8 trở đi). |
| Người điều phối nhóm, cán bộ an toàn | Điều phối nhóm học, xử lý báo cáo (chặng 7). |
| Hệ thống tích hợp ngoài | Danh tính dịch vụ để kết nối hệ thống sẵn có của trường; chặng chưa xác định. |

## 5. Chức năng theo vai và theo chặng

**Mức ưu tiên:** P0 là bắt buộc trong MVP; P1 là ngay sau MVP (theo `MVP.md`); "Sau" là thuộc chặng sau, thứ tự ưu tiên chi tiết chưa xác định. Vai "Hệ thống" nghĩa là hành vi tự động mà người dùng nhìn thấy kết quả.

| Mã | Tên | Mô tả | Vai | Sub-domain | Chặng | Ưu tiên |
|---|---|---|---|---|---|---|
| GEO-01 | Nhập đề tự học | Học sinh dán đề bằng chữ hoặc ảnh, hoặc chọn hình mẫu để khám phá. | Học sinh | GEO | 1 | P0 |
| GEO-02 | Dựng hình từ ràng buộc | AI mô tả hình bằng ràng buộc; engine tính tọa độ và kiểm tra lại. | Hệ thống | GEO | 1 | P0 |
| GEO-03 | Thư viện hình mẫu | Chóp, lăng trụ, hộp, nón, trụ, cầu có tham số, dựng không cần AI. | Học sinh | GEO | 1 | P0 |
| GEO-04 | Kiểm chứng và nhãn tin cậy | So đáp số AI với kết quả tính từ tọa độ; gắn nhãn "Đã kiểm chứng" hoặc "Cần xem lại". | Hệ thống; học sinh và giáo viên thấy nhãn | GEO, TRU | 1 | P0 |
| GEO-05 | Thao tác hình 3D | Xoay, phóng to, tô màu cạnh và mặt, vẽ đường, dựng mặt phẳng, giao điểm, giao tuyến, trục Oxyz. | Học sinh, giáo viên | GEO | 1 | P0 |
| GEO-06 | Thêm điểm và đo đạc | Trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh; đo độ dài, góc, khoảng cách ngay trên hình. | Học sinh | GEO | 1 | P0 |
| GEO-07 | Hiển thị và cảm ứng | Nét đứt cho cạnh khuất, chạm chọn đối tượng trên hình, chụm ngón để phóng to, đặt lại góc nhìn. | Học sinh | GEO | 1 | P0 |
| GEO-08 | Thang gợi ý | Mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải đầy đủ kèm nguyên lý. | Học sinh | GEO | 1 | P0 |
| GEO-09 | Lời giải đồng bộ hình | Bấm bước nào, hình tô màu đối tượng và đường phụ của bước đó; có "Vì sao nghĩ ra bước này?". | Học sinh, giáo viên | GEO | 1 | P0 |
| GEO-10 | Hỏi đáp theo bài | Hỏi trợ lý trong ngữ cảnh bài; nhật ký hỏi đáp lọc theo bài. | Học sinh | GEO | 1 | P0 |
| GEO-11 | Thuật ngữ và bài giảng ngắn | Bấm thuật ngữ để đọc bài giảng ngắn. Có ở bản hiện tại; `MVP.md` chưa nêu rõ. | Học sinh | GEO | 1 *(suy diễn)* | Chưa xác định |
| GEO-12 | Hình học phẳng | Dựng và kiểm chứng hình phẳng như hình không gian. | Học sinh, giáo viên | GEO | 2 | Sau |
| GEO-13 | Trình chiếu và chia sẻ | Chế độ trình chiếu trên lớp, link chia sẻ, xuất ảnh hoặc PDF. | Giáo viên | GEO | 2 | P1 |
| GEO-14 | Thiết diện trực tiếp | Kéo mặt phẳng, thiết diện cập nhật ngay. | Học sinh | GEO | 2 | P1 |
| GEO-15 | Môn học thứ hai | Môn mới dùng lại khung đọc đề, giải, kiểm chứng, hiển thị, gợi ý. Môn thứ hai là Hàm số và đồ thị (giả định PO-06). | Học sinh, giáo viên | GEO | 4 | Sau |
| LRN-01 | Bài của em | Danh sách bài được giao, hiện giờ mở lời giải và tiến độ của mình. | Học sinh | LRN | 1 | P0 |
| LRN-02 | Giao bài | Chọn lớp, chọn trợ giúp học sinh được dùng, đặt giờ mở lời giải; giao một đề hoặc một bộ đề. | Giáo viên | LRN | 1 | P0 |
| LRN-03 | Theo dõi lớp | Ai đã mở bài, dùng gợi ý mức nào, hỏi gì. | Giáo viên | LRN | 1 | P0 |
| LRN-04 | Xem trước như học sinh | Giáo viên xem bài đúng như học sinh sẽ thấy. | Giáo viên | LRN | 1 | P0 |
| LRN-05 | Lớp thật | Tạo nhiều lớp, ghi danh học sinh, giao bài theo bộ. | Giáo viên | LRN | 3 | Sau |
| LRN-06 | Kết quả theo lớp | Cho thấy cả lớp vướng ở đâu theo bài và dạng bài. Không xếp hạng, không so thứ tự học sinh (PO-04). | Giáo viên | LRN | 3 | Sau |
| LRN-07 | Nộp bài | Nộp bài bằng chữ hoặc nhiều ảnh viết tay; giữ ảnh gốc, bản đọc chữ sửa được. | Học sinh | LRN | 5 | Sau |
| LRN-08 | Thông báo lớp, nhắc hạn | Thông báo bài mới, hạn nộp, bài đã trả. | Giáo viên, học sinh | LRN | Chưa xác định | Sau |
| ANS-01 | Giờ mở lời giải | Đặt, đổi, hoặc mở ngay thời điểm hiện lời giải của một bài đã giao. | Giáo viên | ANS | 1 | P0 |
| ANS-02 | Khóa lời giải ở máy chủ | Trước giờ mở, máy chủ không gửi lời giải và đáp số xuống thiết bị. | Hệ thống | ANS | 1 | P0 |
| ANS-03 | Hỏi đáp không lộ đáp án | Khi lời giải còn khóa, trợ lý chỉ gợi ý; câu trả lời được kiểm tra trước khi trả về. | Hệ thống | ANS | 1 | P0 |
| ANS-04 | Mở lời giải bài tự học | Mức 3 mở sau khi đã xem mức 1 và 2 *(giả định)*. | Học sinh | ANS | 1 | P0 |
| ANS-05 | Kiểm soát đáp án đầy đủ | Mọi yêu cầu AI của học sinh được kiểm tra danh tính, lớp, bài, trạng thái, lịch công bố; chỉ dùng tài liệu đã duyệt; ghi lại quyết định. | Hệ thống | ANS | 5 | Sau |
| KNW-01 | Kho đề soạn sẵn | 40–60 đề hình không gian, lọc theo cấp, loại hình, dạng bài; mỗi đề ghi "AI soạn" và có nhãn kiểm chứng. | Giáo viên | KNW | 1 | P0 |
| KNW-02 | Xem đề và lời giải | Xem hình, đáp số, lời giải từng bước, gợi ý của một đề. | Giáo viên | KNW | 1 | P0 |
| KNW-03 | Giáo viên thêm và duyệt đề | Tự thêm đề, sửa và duyệt lời giải AI trước khi vào kho. | Giáo viên | KNW | 2 | P1 |
| KNW-04 | Kho tri thức có kiểm soát | Tài liệu có chủ, version, trạng thái duyệt, cấp cho lớp hoặc bài; AI phía học sinh chỉ dùng bản đã duyệt và truy được nguồn. | Giáo viên | KNW | 5 | Sau |
| ASM-01 | Bài luyện chấm tự động | Học sinh điền đáp số, hệ thống chấm. | Học sinh | ASM | 2 | P1 |
| ASM-02 | Rubric | Tiêu chí, mức điểm, hướng dẫn cho điểm từng phần; version bất biến khi đã dùng. | Giáo viên | ASM | 5 | Sau |
| ASM-03 | AI đề xuất điểm | AI đọc bài viết tay, đề xuất điểm từng tiêu chí kèm lý do, bằng chứng, độ tin cậy. | Hệ thống | ASM | 5 | Sau |
| ASM-04 | Giáo viên duyệt điểm | Nhận, sửa, từ chối từng tiêu chí; duyệt mới thành điểm chính thức. | Giáo viên | ASM | 5 | Sau |
| ASM-05 | Công bố và phúc khảo | Giáo viên chọn thời điểm công bố điểm, đáp án, phản hồi; học sinh xin xem lại. | Giáo viên, học sinh | ASM | 5 | Sau |
| PRF-01 | Nhật ký tương tác | Ghi mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải. | Hệ thống | PRF, TRU | 1 | P0 |
| PRF-02 | Lịch sử câu hỏi | Giáo viên xem và lọc toàn bộ câu hỏi của học sinh mình dạy. | Giáo viên | PRF | 6 | Sau |
| PRF-03 | Mức nắm khái niệm | Mức nắm từng khái niệm và chỗ hay vướng, kèm bài làm làm căn cứ. | Giáo viên, học sinh | PRF | 6 | Sau |
| PRF-04 | Ghi nhận can thiệp | Giáo viên xác nhận hoặc bác nhận định, ghi việc đã làm và kết quả. | Giáo viên | PRF | 6 | Sau |
| COP-01 | Soạn nháp nội dung dạy | Nháp bộ đề, giáo án, rubric từ tài liệu của giáo viên; có nhãn kiểm chứng; vào kho khi được duyệt. | Giáo viên | COP | 5 | Sau |
| COP-02 | Tổng hợp lỗi sai | Tóm tắt lỗi phổ biến của lớp. | Giáo viên | COP | 5 | Sau |
| COP-03 | Soạn nháp thông báo | Nháp thông báo, nhắc việc, báo cáo cho phụ huynh; chỉ gửi khi giáo viên duyệt. | Giáo viên | COP | 5 | Sau |
| PAR-01 | Xác minh phụ huynh | Trường xác minh quan hệ phụ huynh–học sinh; thu hồi khi hết hiệu lực. | Quản trị trường, phụ huynh | PAR, IAM | 6 | Sau |
| PAR-02 | Tiến độ của con | Bài tập, điểm đã duyệt, điểm mạnh và phần cần luyện đã duyệt, lịch sắp tới. | Phụ huynh | PAR | 6 | Sau |
| PAR-03 | Trao đổi với giáo viên | Nhắn tin qua kênh được trường cho phép. | Phụ huynh, giáo viên | PAR | 6 | Sau |
| COL-01 | Nhóm học có điều phối | Chat, cùng thao tác một hình, trợ lý nhóm chỉ gợi ý; có nút báo cáo. | Học sinh, giáo viên | COL | 7 | Sau |
| COL-02 | Video có điều phối | Phòng học trực tuyến có quyền điều phối và báo cáo an toàn. Ngoài phạm vi đầu của chặng 7, để sau (PO-11). | Học sinh, giáo viên | COL | 7 | Sau |
| COL-03 | Cộng đồng giáo viên | Cộng đồng theo môn, tự nguyện tham gia. | Giáo viên | COL | 7 | Sau |
| COL-04 | Chia sẻ liên trường | Chia sẻ tài nguyên và nhóm liên trường qua cấp phép của các trường tham gia. | Quản trị trường, giáo viên | COL | 7 | Sau |
| SCH-01 | Quản lý trường | Nhân sự, lớp, năm học, phân công, sự kiện, thông báo trường. Điểm danh, sổ điểm chính thức, tài chính và thời khóa biểu không thuộc chặng 8 (PO-10, PO-26). | Quản trị trường | SCH | 8 | Sau |
| SCH-02 | Báo cáo trường | Số liệu tổng hợp theo tổ bộ môn. | Quản trị trường, ban giám hiệu | SCH | 8 | Sau |
| SCH-03 | Nhận diện và policy riêng | Logo, tên, giao diện, quy định riêng trong giới hạn cấp trên cho phép. | Quản trị trường | SCH | 8 | Sau |
| GOV-01 | Phát hành văn bản | Gửi văn bản tới đúng phạm vi, có version và ngày hiệu lực. | Cơ quan quản lý | GOV | 9 | Sau |
| GOV-02 | Theo dõi triển khai | Theo dõi xác nhận và triển khai theo từng trường, tách riêng hai trạng thái. | Cơ quan quản lý, quản trị trường | GOV | 9 | Sau |
| GOV-03 | Số liệu tổng hợp | Chỉ số tổng hợp trong phạm vi được ủy quyền. | Cơ quan quản lý | GOV | 9 | Sau |
| IAM-01 | Đăng nhập nhẹ | Học sinh vào bằng tên hiển thị và mã lớp; dữ liệu riêng từng người. | Học sinh | IAM | 1 | P0 |
| IAM-02 | Tài khoản và lớp mẫu | Một Giáo viên mẫu, hai lớp mẫu (một THCS, một THPT), vài học sinh và bài giao mẫu. | Giáo viên | IAM | 1 | P0 |
| IAM-03 | Tài khoản chính thức | Đăng ký giáo viên thật, tài khoản học sinh chính thức. Cách xác nhận giáo viên chưa xác định. | Giáo viên, học sinh | IAM | 3 | Sau |
| IAM-04 | Tổ chức nhiều cấp | Nền tảng, cơ quan quản lý, nhóm trường, trường, năm học, lớp; một người giữ nhiều vai theo phạm vi; cách ly dữ liệu giữa các trường. | Quản trị nền tảng, quản trị trường | IAM | 5 | Sau |
| IAM-05 | Kết nối hệ thống của trường | Tích hợp hệ thống sẵn có, có mục đích, phạm vi và dấu vết. | Quản trị nền tảng, quản trị trường | IAM | Chưa xác định | Sau |
| TRU-01 | Bộ đề chuẩn | Đo tỉ lệ hình thỏa ràng buộc và đáp số đúng. | Nhóm sản phẩm | TRU | 1 | P0 |
| TRU-02 | Bộ câu thử moi đáp án | Kiểm tra lời giải không lộ trước giờ mở, kể cả qua hỏi đáp. | Nhóm sản phẩm | TRU | 1 | P0 |
| TRU-03 | Lớp gọi AI phía máy chủ | Giấu khóa truy cập, đổi được model, ghi chi phí từng lượt gọi. | Hệ thống | TRU | 1 | P0 |
| TRU-04 | Audit và truy cập khẩn cấp | Ghi dấu vết hành động nhạy cảm; truy cập khẩn cấp có mục đích và thời hạn hiệu lực. | Quản trị nền tảng | TRU | 5 *(suy diễn)* | Sau |
| TRU-05 | Công tắc an toàn | Tắt AI chấm theo trường, môn, chủ đề; tắt AI phía học sinh mà luồng học vẫn chạy. | Quản trị nền tảng | TRU | 5 *(suy diễn)* | Sau |

## 6. Lợi ích theo vai và cách nhận biết

Chưa có số đo nào. Cột "Cách nhận biết" là thứ cần đo, không phải kết quả đã đạt.

| Vai | Lợi ích | Cách nhận biết |
|---|---|---|
| Học sinh | Hình dung được hình không gian; học được cách nghĩ thay vì chép đáp án | Tỉ lệ phiên có ít nhất một thao tác trên hình (ngưỡng đề xuất ≥ 60%); số lượt xem gợi ý trước khi xem lời giải; số bài đi hết gợi ý; phản hồi định tính của học sinh |
| Học sinh | Tin được hình và đáp số | Tỉ lệ hình thỏa ràng buộc (đề xuất ≥ 95%) và đáp số đúng (đề xuất ≥ 90%) trên bộ đề chuẩn; không bài sai nào mang nhãn "Đã kiểm chứng" |
| Giáo viên | Có sẵn đề kèm hình và lời giải; giao bài nhanh | Số bài được giao; giáo viên có dùng lặp lại hay không (chỉ đo được khi có giáo viên thật, từ chặng 2) |
| Giáo viên | Kiểm soát được lời giải | Không lần nào lời giải hoặc đáp số lộ trước giờ mở trong bộ câu thử |
| Giáo viên | Thấy học sinh làm tới đâu, vướng ở đâu | Giáo viên mở trang theo dõi lớp; phản hồi định tính |
| Giáo viên (chặng 5) | Giảm thời gian chấm và soạn | Thời gian duyệt so với chấm tay; tỉ lệ giáo viên sửa đề xuất của AI |
| Phụ huynh (chặng 6) | Biết tiến độ của con, phối hợp được với giáo viên | Tỉ lệ phụ huynh đã xác minh có dùng; thời gian giáo viên phản hồi; tỉ lệ tắt thông báo |
| Nhà trường (chặng 8) | Quản trị và tổng hợp trên một nơi, giữ bản sắc | Mức số hóa quy trình; độ phủ thông báo |
| Cơ quan quản lý (chặng 9) | Biết văn bản tới đâu, triển khai tới đâu | Độ phủ phát hành, tỉ lệ xác nhận, độ đầy đủ báo cáo triển khai |
| Mọi vai | An toàn | Số sự cố truy cập sai dữ liệu và số sự cố lộ đáp án (mục tiêu là không có) |

**Chỉ số dẫn đường.** Giai đoạn MVP: số bài hình không gian mà học sinh thao tác trên hình và đi hết gợi ý trong mỗi kỳ đo. Dài hạn: số vòng học đã xác nhận của mỗi lớp trong mỗi kỳ đo (bài được giao, học sinh làm, AI phân tích, giáo viên duyệt, học sinh nhận phản hồi).

## 7. Vision, định vị, nguyên tắc sản phẩm

**Vision.** Một mạng lưới giáo dục thống nhất, nơi mọi trường có thể kết nối, chia sẻ và phát triển; mọi giáo viên có công cụ tốt hơn để dạy; mọi học sinh có lộ trình học rõ ràng; cơ quan quản lý có dữ liệu đáng tin để điều phối và hỗ trợ.

**Mission.** Kết nối con người, tri thức, quy trình và dữ liệu giáo dục trên một nền tảng chung, dùng AI có trách nhiệm để nâng chất lượng học, giảm tải công việc và hỗ trợ quyết định dựa trên bằng chứng.

**Định vị.** Dài hạn là hệ sinh thái giáo dục AI thống nhất: một mạng lưới chung, mỗi trường giữ bản sắc riêng. Điểm khởi đầu là làm thật tốt một môn. Tầm nhìn này là định hướng, chưa cam kết và chưa kiểm chứng.

**Không định vị là:** chatbot giải bài tập; phần mềm giao bài chấm điểm đơn lẻ; phần mềm quản trị trường thuần túy; mạng xã hội học sinh không kiểm soát; AI thay giáo viên.

**Nguyên tắc.**
1. Giáo viên dẫn dắt, AI hỗ trợ.
2. Học trước, đáp án sau.
3. Tri thức đã duyệt đứng trước nội dung AI tự sinh.
4. Đánh giá có hệ quả phải có người duyệt.
5. Nhận định về học sinh dựa trên bằng chứng, không gắn nhãn cố định.
6. Quyền tối thiểu, riêng tư mặc định.
7. Chuẩn chung, trường giữ bản sắc.
8. Nền tảng tập trung, ranh giới dữ liệu rõ ràng.
9. Dễ tiếp cận, phù hợp lứa tuổi.
10. Mọi hành động tác động lớn đều có dấu vết kiểm tra.
11. Kết nối hệ thống khác mà không nhân bản dữ liệu tràn lan.
12. Xây nền một lần, bật năng lực dần.
13. Kết quả AI không được tin mặc định: hình và đáp số phải qua kiểm chứng.

## 8. Quy tắc nghiệp vụ cốt lõi và bất biến

**Áp dụng từ MVP.**

| Mã | Quy tắc |
|---|---|
| BR-01 | Bài được giao: trước giờ mở, máy chủ không gửi lời giải và đáp số xuống thiết bị. Ẩn ở giao diện là không đủ. |
| BR-02 | Khi lời giải còn khóa, hỏi đáp chỉ được gợi ý hoặc giải thích khái niệm, không nêu đáp số; câu trả lời phải qua kiểm tra trước khi trả về. |
| BR-03 | Chỉ giáo viên của lớp được đặt, đổi, hoặc mở ngay giờ mở lời giải. Bài giao có thể chưa đặt giờ mở; khi đó lời giải vẫn khóa *(suy diễn từ dữ liệu mẫu "chưa đặt")*. |
| BR-04 | Gợi ý đi trước lời giải: mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải đầy đủ. |
| BR-05 | Bài tự học: mức 3 mở sau khi học sinh đã xem mức 1 và 2 *(giả định, chờ chủ sản phẩm xác nhận)*. |
| BR-06 | Hình dựng từ ràng buộc do engine tính tọa độ; không tin thẳng tọa độ AI sinh ra. |
| BR-07 | Nhãn "Đã kiểm chứng" chỉ gắn khi đáp số khớp kết quả tính toán; không bài sai nào được mang nhãn này. Trường hợp còn lại mang nhãn "Cần xem lại". |
| BR-08 | Đề lạ không khớp hình mẫu thì dùng tọa độ AI và phải hiện cảnh báo chưa kiểm chứng. |
| BR-09 | Engine chỉ kiểm chứng phần tính toán, không kiểm chứng lập luận chứng minh. Lời giải do AI soạn luôn ghi rõ "AI soạn". |
| BR-10 | Kho đề MVP thuộc tài khoản Giáo viên mẫu; lời giải do AI soạn và đứng tên giáo viên này. |
| BR-11 | Lời giải đã giao không sửa tại chỗ; sửa thì tạo version mới. |
| BR-12 | Học sinh chỉ thấy bài của lớp mình và dữ liệu của chính mình. |
| BR-13 | Giáo viên là người quyết định cuối. |

**Áp dụng từ các chặng sau (tầm nhìn).**

| Mã | Quy tắc | Chặng |
|---|---|---|
| BR-20 | Điểm AI là đề xuất; không có đường chuyển thẳng từ đề xuất của AI thành điểm chính thức nếu thiếu thao tác duyệt của giáo viên có quyền. | 5 |
| BR-21 | Học sinh và phụ huynh không bao giờ thấy đề xuất chấm của AI; chỉ thấy điểm và phản hồi đã duyệt, từ thời điểm giáo viên công bố. | 5, 6 |
| BR-22 | Rubric, bài giao, tài liệu đã công bố là bất biến; sửa thì tạo version mới. Mỗi lần giáo viên sửa điểm ghi lại giá trị trước, sau và người sửa. | 5 |
| BR-23 | AI phía học sinh chỉ dùng tài liệu đã duyệt và đang hiệu lực; không có nguồn đã duyệt thì nói rõ và hướng học sinh hỏi giáo viên. | 5 |
| BR-24 | Ảnh bài nộp gốc được giữ nguyên; bản đọc chữ là bản phái sinh sửa được. | 5 |
| BR-25 | Trợ lý giáo viên không tự gửi tin, công bố nội dung hay đổi điểm; bản nháp ghi rõ là do AI soạn. | 5 |
| BR-26 | Nhận định về học sinh phải gắn bằng chứng hoặc ghi rõ là ước lượng; không mô tả học sinh là yếu, lười hay kém. | 6 |
| BR-27 | Phụ huynh chỉ xem thông tin của con mình, sau khi trường xác minh quan hệ, và chỉ thông tin đã duyệt; mặc định không xem lịch sử câu hỏi. | 6 |
| BR-28 | Nhóm học liên trường phải được các trường tham gia duyệt; chia sẻ liên trường cần cấp phép có mục đích và thời hạn hiệu lực. | 7 |
| BR-29 | Chính sách cấp dưới không tắt được yêu cầu bắt buộc của cấp trên. | 8 |
| BR-30 | Cơ quan quản lý chỉ xem số liệu tổng hợp trong phạm vi ủy quyền; "đã xác nhận" và "đã triển khai" là hai trạng thái tách biệt. | 9 |
| BR-31 | Quản trị nền tảng không mặc nhiên xem nội dung học tập. Khi không rõ quyền thì từ chối. | 5 trở đi |

## 9. Thực thể dữ liệu chính và vòng đời

**Thực thể của MVP.**

| Thực thể | Ý nghĩa | Thuộc tính chính |
|---|---|---|
| Người dùng (`User`) | Giáo viên hoặc học sinh | tên, vai trò |
| Lớp (`Class`) | Lớp mẫu | tên, cấp, mã lớp, giáo viên |
| Đề bài (`Problem`) | Một bài toán | đề, cấp, chủ đề, nguồn (kho hoặc tự nhập), chủ sở hữu |
| Mô tả hình (`SceneSpec`) | Hình do engine dựng | version, ràng buộc, tọa độ đã tính |
| Lời giải (`Solution`) | Các bước, gợi ý, đáp số | version, model, phiên bản prompt, tác giả, cờ "AI soạn" |
| Báo cáo kiểm chứng (`VerificationReport`) | Kết quả từng phép kiểm tra | trạng thái, quyết định nhãn |
| Bài giao (`Assignment`) | Đề giao cho lớp | lớp, đề, giờ mở lời giải (`release_at`), trạng thái |
| Tương tác (`Interaction`) | Sự kiện của học sinh | loại (thao tác, gợi ý, hỏi đáp, mở lời giải), thời điểm |

Mọi bản ghi có chủ sở hữu (`owner_id`) và có sẵn trường tổ chức (`org_id`) để trống; mọi cấu trúc dữ liệu có số phiên bản lược đồ.

**Thực thể thêm ở các chặng sau (rút gọn).** Tổ chức, trường, tư cách thành viên theo phạm vi, policy; năm học, môn, chuẩn chương trình; rubric, bài nộp, đề xuất chấm, điểm đã duyệt, phúc khảo; tài liệu tri thức và quyền cấp; tín hiệu học tập, mức nắm khái niệm, can thiệp; quan hệ phụ huynh–học sinh, bản ghi đồng thuận; thông báo, hội thoại, nhóm học, báo cáo an toàn; cấp phép chia sẻ, văn bản chính sách, báo cáo triển khai; nhật ký kiểm tra.

**Vòng đời quan trọng.**

| Đối tượng | Trạng thái | Chặng |
|---|---|---|
| Lời giải | sinh ra → đã kiểm chứng hoặc cần xem lại → vào kho → đã giao (khóa) → đã mở | 1 |
| Giờ mở của bài giao | chưa đặt, sắp mở, đã mở; giáo viên đổi giờ hoặc mở ngay | 1 |
| Bài giao | nháp → công bố → đóng → lưu trữ | 5 |
| Bài nộp | nháp → đã nộp → xử lý → chờ giáo viên → đã duyệt → đã trả | 5 |
| Đề xuất chấm | chờ → phân tích → đã đề xuất → giáo viên xem → nhận hoặc sửa → duyệt; ngoại lệ: AI từ chối chấm, lỗi, bị bác | 5 |
| Tài liệu tri thức | nháp → chờ duyệt → đang dùng → lưu trữ | 5 |
| Quan hệ phụ huynh | yêu cầu → chờ xác minh → hiệu lực → tạm ngưng, thu hồi hoặc hết hạn | 6 |
| Cấp phép chia sẻ | yêu cầu → duyệt → hiệu lực → hết hạn hoặc thu hồi | 7 |
| Văn bản chính sách | nháp → duyệt → phát hành → đã xác nhận hoặc đang triển khai → hoàn tất | 9 |

## 10. Ràng buộc và yêu cầu phi chức năng

| Nhóm | Yêu cầu |
|---|---|
| Thiết bị | Học sinh: iPad hoặc iPhone, cảm ứng là chính. Phụ huynh: iPhone, có thể iPad. Giáo viên: web trên máy tính; kiểm tra nhanh trên điện thoại. Chạy dạng web trên trình duyệt tới hết chặng 4; xét ứng dụng cài đặt ở chặng 5 và 6 (giả định PO-12). |
| Ngôn ngữ | Tiếng Việt có dấu là mặc định; thông báo lỗi dễ hiểu, phù hợp lứa tuổi; ký hiệu toán đọc được. |
| Độ tin cậy của AI | Hình và đáp số qua kiểm chứng; nhãn trạng thái rõ ràng; độ không chắc chắn phải được hiện ra; version hóa prompt, model và bộ đề đánh giá; bộ đề chuẩn chạy lại khi đổi model. |
| Kiểm soát đáp án | Khóa ở máy chủ; bộ câu thử moi đáp án phải đạt trước khi phát hành. |
| Riêng tư dữ liệu trẻ em | MVP chỉ thu tên hiển thị và mã lớp. Dữ liệu học sinh riêng tư mặc định; giảm tối đa dữ liệu học sinh gửi cho nhà cung cấp model. Quy định pháp lý áp dụng, chính sách lưu giữ, xóa, xuất và đồng thuận đều chưa xác định và phải có tư vấn pháp lý trước khi mở rộng. |
| Bảo mật | Phân quyền kiểm tra ở máy chủ; dữ liệu riêng từng người; về sau cách ly dữ liệu giữa các trường, mã hóa, ghi dấu vết hành động nhạy cảm. |
| Hiệu năng | Thời gian trung vị từ lúc gửi đề tới lúc có hình: ngưỡng đề xuất dưới 40 giây. Chi phí AI mỗi bài: cần đo, chưa đặt ngưỡng. Phải có màn chờ rõ ràng khi AI đang xử lý. |
| Vận hành AI | Gọi AI qua một lớp phía máy chủ, đổi được nhà cung cấp, ghi chi phí; về sau tác vụ AI chạy nền và chạy lại an toàn. |
| Kiến trúc | Rời runtime Claude artifact; web app riêng có backend; một khối nhiều module; engine hình học thuần hàm, không phụ thuộc giao diện; mỗi môn học là một module. |
| An toàn trẻ em | Chat và video chỉ mở sau khi thiết kế bảo vệ trẻ em được duyệt; có kiểm duyệt và nút báo cáo. |

## 11. Phạm vi

**Trong MVP (chặng 1).** Hình không gian cho THCS và THPT: chóp, lăng trụ, hộp, lập phương; nón, trụ, cầu mức cơ bản; hệ trục Oxyz. Kho 40–60 đề soạn sẵn của Giáo viên mẫu. Lớp mẫu, giao bài, giờ mở lời giải. Gợi ý hai mức và lời giải, khóa ở máy chủ kể cả hỏi đáp. Chế độ tự học với đề tự nhập. Đăng nhập nhẹ. Trang theo dõi lớp. Bộ đề chuẩn, bộ câu thử, ghi sự kiện.

**Không bao giờ làm.** Mọi dạng xếp hạng học sinh, lớp, giáo viên, trường (PO-04); tự duyệt điểm AI (PO-03).

**Ngoài MVP.** Hình phẳng; giáo viên tự soạn đề, sửa lời giải, tải tài liệu riêng; đăng ký giáo viên thật; trường, nhiều tổ chức, phụ huynh; rubric, nộp bài viết tay, AI chấm bài; bài luyện có chấm điểm; chat nhóm, video, thông báo, lịch.

**MVP hoàn thành khi:** hình thỏa ràng buộc và đáp số đúng đạt ngưỡng đề xuất trên bộ đề chuẩn; không bài sai nào mang nhãn "Đã kiểm chứng"; không lần nào lời giải lộ trước giờ mở trong bộ câu thử; toàn bộ P0 chạy được trên các thiết bị ở mục 10; kho đề có ít nhất 40 bài đã kiểm chứng.

**Theo chặng.**

| Chặng | Nội dung thêm | Điều kiện mở |
|---|---|---|
| 1 | MVP hình không gian | Đang làm |
| 2 | Hình phẳng; giáo viên thật tự thêm đề và duyệt lời giải; trình chiếu; bài luyện chấm tự động | MVP đạt tiêu chí hoàn thành |
| 3 | Đăng ký giáo viên, nhiều lớp, giao bài theo bộ, kết quả theo lớp | Có giáo viên thật dùng lặp lại ở chặng 2 |
| 4 | Môn học thứ hai dùng lại khung môn học | Hình học giữ chân được học sinh |
| 5 | Nộp bài viết tay, rubric, AI đề xuất điểm, giáo viên duyệt; kho tri thức và kiểm soát đáp án đầy đủ; trợ lý giáo viên; nhiều trường | Có ít nhất một trường cam kết thí điểm |
| 6 | Hồ sơ học tập và phụ huynh | Chặng 5 ổn định; xác minh phụ huynh đã thử |
| 7 | Chia sẻ tài nguyên của giáo viên, nhóm học trong lớp, trao đổi giữa hai lớp, nhóm liên trường; video để sau (PO-11) | Thiết kế bảo vệ trẻ em được duyệt |
| 8 | Vận hành nhà trường | Có trường yêu cầu và trả tiền |
| 9 | Quản trị giáo dục | Có ủy quyền pháp lý và đối tác quản lý |

## 12. Thuật ngữ

| Tiếng Việt | Tiếng Anh | Nghĩa |
|---|---|---|
| Kho đề | Problem bank | Tập đề soạn sẵn kèm hình, lời giải, gợi ý |
| Bài giao | Assignment | Đề được giáo viên giao cho một lớp |
| Giờ mở lời giải | Solution release time | Thời điểm giáo viên đặt để học sinh được xem lời giải |
| Khóa lời giải | Solution lock | Lời giải được giữ ở máy chủ tới giờ mở |
| Kiểm soát đáp án | Answer Firewall | Lớp kiểm tra ở máy chủ chặn lộ đáp án qua AI |
| Thang gợi ý | Hint ladder | Ba mức: hướng nghĩ, bước kế tiếp, lời giải đầy đủ |
| Kiểm chứng | Verification | So kết quả AI với kết quả tính từ tọa độ |
| Ràng buộc | Constraint | Điều kiện hình học của đề, ví dụ "SA vuông góc với đáy" |
| Hình mẫu | Parametric template | Hình dựng sẵn có tham số |
| Bộ đề chuẩn | Benchmark set | Đề có đáp án tham chiếu để đo độ đúng |
| Bộ câu thử moi đáp án | Adversarial test set | Câu hỏi cố ép AI lộ đáp án |
| Giáo viên mẫu, lớp mẫu | Sample teacher, sample class | Tài khoản và dữ liệu tạo sẵn cho MVP |
| Tự học | Self-study | Học sinh làm đề tự nhập, không có giáo viên giao |
| Theo dõi lớp | Class monitoring | Trang giáo viên xem tiến độ và câu hỏi của lớp |
| Kho tri thức | Knowledge base | Tài liệu giáo viên đã duyệt cho AI dùng |
| Đề xuất chấm | Grading proposal | Điểm AI đề xuất, chưa phải điểm chính thức |
| Điểm đã duyệt | Approved grade | Điểm chính thức sau khi giáo viên duyệt |
| Hồ sơ học tập | Learning profile | Lịch sử và mức nắm khái niệm dựa trên bằng chứng |
| Trợ lý giáo viên | Teacher copilot | AI soạn nháp cho giáo viên duyệt |
| Phúc khảo | Regrade request | Học sinh xin xem lại điểm |
| Dấu vết kiểm tra | Audit trail | Ghi lại ai làm gì, khi nào |

## 13. Giả định và câu hỏi mở

**Giả định đang dùng.**
1. Bài tự học mở lời giải sau khi xem gợi ý mức 1 và 2.
2. "Lời giải AI đứng tên giáo viên" nghĩa là một tài khoản Giáo viên mẫu sở hữu kho đề và lời giải AI.
3. Người dùng thử vào vai giáo viên bằng tài khoản mẫu; chưa có đăng ký.
4. *(suy diễn)* Bài giao chưa đặt giờ mở thì lời giải vẫn khóa.
5. *(suy diễn)* Quản trị trường, cơ quan quản lý và quản trị nền tảng dùng web trên máy tính.
6. *(suy diễn)* Tiêu chí "chạy trên máy tính và điện thoại" trong `MVP.md` được hiểu lại theo thiết bị đã chốt: học sinh trên iPad và iPhone, giáo viên trên web máy tính và điện thoại.

**Câu hỏi mở cho chủ sản phẩm.**
1. Bài tự học: giữ giả định 1, hay mở sau một khoảng chờ, hay không mở nếu học sinh thuộc một lớp? Học sinh tự học chưa có mã lớp thì đăng nhập thế nào?
2. Ai soát kho đề trước khi học sinh thật dùng, và có nguồn đề ưu tiên không?
3. Dùng thử MVP với ai: nội bộ hay một lớp thật?
4. Người trả tiền đầu tiên là ai: phụ huynh và học sinh, giáo viên và trung tâm, hay trường?
5. Giữ tên "Hình Học Cùng AI" hay đặt tên nền tảng?
6. Học sinh và phụ huynh dùng web trên iPad và iPhone hay cần ứng dụng cài đặt? Bản thiết kế hiện làm cho màn hình rộng; màn học sinh có cần thiết kế lại cho iPad trước không?
7. Thuật ngữ bấm được và lịch sử bài tự học của bản hiện tại có giữ trong MVP không?
8. Mục "chọn trợ giúp học sinh được dùng" khi giao bài gồm những lựa chọn nào (tắt hỏi đáp, giới hạn mức gợi ý)?
9. Tài khoản giáo viên thật được xác nhận thế nào, và thuộc chặng 2 hay chặng 3?
10. Môn học thứ hai là môn nào?
11. Pháp nhân vận hành, quy định áp dụng cho dữ liệu trẻ em, nơi lưu dữ liệu, chính sách lưu giữ, xóa và đồng thuận.
12. Cách cấp tài khoản học sinh chính thức và cách xác minh phụ huynh.

## 14. Hướng dẫn cho người hoặc skill dùng tài liệu này

**Cách tách epic.** Mỗi epic nên nằm trọn trong một sub-domain và một chặng, và khép được một luồng giá trị cho một vai. Dùng mã chức năng ở mục 5 để truy vết từ epic tới user story.

**Thứ tự viết epic cho MVP.** Thứ tự dưới đây theo các mốc phát triển của MVP; xong mốc trước rồi mới sang mốc sau.

| Thứ tự | Epic đề xuất | Chức năng |
|---|---|---|
| 1 | Nền tảng và gọi AI phía máy chủ; bộ đề chuẩn | TRU-01, TRU-03, IAM-01 |
| 2 | Hình đúng: dựng hình từ ràng buộc, hình mẫu, kiểm chứng và nhãn | GEO-02, GEO-03, GEO-04 |
| 3 | Thao tác hình 3D trên thiết bị cảm ứng | GEO-05, GEO-06, GEO-07 |
| 4 | Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học | GEO-01, GEO-08, GEO-09, GEO-10, ANS-04 |
| 5 | Kho đề của Giáo viên mẫu | KNW-01, KNW-02 |
| 6 | Giao bài và kiểm soát lời giải | IAM-02, LRN-01, LRN-02, LRN-04, ANS-01, ANS-02, ANS-03, TRU-02 |
| 7 | Theo dõi lớp và ghi sự kiện | LRN-03, PRF-01 |

Sau MVP, viết theo đúng thứ tự chặng, và chỉ khi chặng trước đạt điều kiện mở.

**Điều phải đưa vào acceptance criteria và test case của MVP.**
- Kiểm tra khóa lời giải ở tầng dữ liệu máy chủ trả về, không chỉ ở giao diện; có ca thử moi đáp án qua hỏi đáp.
- Ca giờ mở: chưa đặt, sắp mở, đã mở, đổi giờ, mở ngay; học sinh đang mở bài đúng lúc tới giờ.
- Ca nhãn: bài khớp kiểm chứng, bài không khớp, đề lạ dùng tọa độ AI kèm cảnh báo.
- Ca thiết bị: iPad và iPhone cho học sinh; máy tính và điện thoại cho giáo viên.
- Ca phân quyền: học sinh lớp này không thấy bài lớp khác và dữ liệu của bạn khác.

**Những điều không được suy diễn.**
1. Không đặt mốc thời gian, thời lượng hay hạn chót cho bất kỳ chặng hoặc epic nào.
2. Không coi tầm nhìn (chặng 2 đến 9) là phạm vi đã cam kết; không kéo tính năng chặng sau vào MVP.
3. Không viết story cho giáo viên thật tự soạn đề, đăng ký tài khoản, phụ huynh, chấm bài, trường hay thông báo trong MVP. Bản mẫu tương tác có màn hình phụ huynh và các màn hình "Concept" chỉ để minh họa.
4. Không để AI tự quyết: không tự công bố điểm, tự gửi tin, tự gắn nhãn "Đã kiểm chứng" khi chưa qua tính toán.
5. Không bịa số liệu thị trường, pháp lý hay kết quả đo; các ngưỡng trong tài liệu là đề xuất, chưa phải kết quả.
6. Không coi nhãn "Đã kiểm chứng" là đã có giáo viên duyệt lập luận.
7. Không tự chọn mô hình kinh doanh, môn học thứ hai, hay quy định pháp lý; ghi thành giả định và câu hỏi cho chủ sản phẩm.
8. Chỗ nào ghi *(suy diễn)*, *(giả định)* hoặc "chưa xác định" trong tài liệu này phải giữ nguyên tính chất đó trong BRD và user story.
