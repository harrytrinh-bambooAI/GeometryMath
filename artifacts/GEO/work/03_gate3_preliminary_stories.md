# Gate 3 — Preliminary user stories (cấp PO) — GEO

> **Input snapshot:** như `00_inputs.md`. Chỉ story sơ bộ, chưa phải Dev-Ready; **không có tiêu chí chấp nhận** (BA viết Given/When/Then và dữ liệu cụ thể). Đánh giá cỡ dùng S, M, L theo mức công tương đối; cố ý không quy ra ngày hay tuần (D-GEO-002). Độ khó 1 đến 5. Actor và persona: `actor_profiles.md`. Phụ thuộc: story trong cùng epic theo thứ tự id; mọi story UI phụ thuộc US-GEO-101 hoặc 102 và US-GEO-104; story E3 đến E7 phụ thuộc E2. Mọi giá trị dùng giả định mang nhãn PROVISIONAL cùng mã D-.

Id story: `US-GEO-nnn`, hàng trăm là epic. PO dùng nn từ 01; BA thêm story (sau quyết định của PO) từ nn = 20 (xem `board/CONVENTIONS.md` §2).

## Epic E1 — Nền tảng và gọi AI phía máy chủ; bộ đề chuẩn
Chức năng: TRU-01, TRU-03, IAM-01 (+ ràng buộc chủ đầu tư).

### [US-GEO-101] Học sinh vào lớp bằng tên hiển thị và mã lớp
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-048, 051, 067 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-23, REQ-GEO-44, REQ-GEO-28
- **Story:** Là học sinh, tôi muốn vào lớp bằng tên hiển thị và mã lớp, để thấy bài của lớp mình mà không phải đăng ký.
- **Success outline:** Nhập tên và mã lớp hợp lệ, thấy Bài của em; dữ liệu của tôi riêng.
- **Fail / edge outline:** Mã lớp sai; tên trống; hai học sinh trùng tên trong một lớp; không thu email hay số điện thoại.
- **→ Refine in BA:** Quy tắc trùng tên, độ dài tên, khóa phiên (D-GEO-048, 051).

### [US-GEO-102] Giáo viên mẫu vào hệ thống
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-073, 034 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-24, REQ-GEO-28
- **Story:** Là Giáo viên mẫu, tôi muốn vào bằng mã riêng, để chỉ tôi (không phải học sinh) đặt được giờ mở lời giải.
- **Success outline:** Mã đúng vào vai giáo viên, thấy Kho đề và lớp mẫu.
- **Fail / edge outline:** Mã sai; học sinh thử vào vai giáo viên; khung thiết kế có công tắc vai (bản mẫu).
- **→ Refine in BA:** Cơ chế mã và giới hạn thử sai (D-GEO-073, PROVISIONAL).

### [US-GEO-103] Máy chủ gọi AI qua một cổng
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Tech
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-047, 072, 067 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-27, REQ-GEO-47, REQ-GEO-44
- **Story:** Là nhóm sản phẩm, tôi muốn mọi lượt gọi AI đi qua một cổng phía máy chủ, để giấu khóa, đổi model và ghi chi phí từng lượt.
- **Success outline:** Một lượt gọi ghi model, phiên bản prompt, chi phí; đổi model không đổi mã gọi; nhà cung cấp giả lập chạy được.
- **Fail / edge outline:** Nhà cung cấp lỗi hoặc quá thời gian; khóa không bao giờ xuống trình duyệt; không gửi tên hiển thị cho nhà cung cấp.
- **→ Refine in BA:** Chính sách thử lại, thời gian chờ tối đa (D-GEO-072, 047).

### [US-GEO-104] Cách ly dữ liệu theo trường và phân quyền ở máy chủ (xuyên suốt)
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Xuyên suốt
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-012, 050, 054, 076 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-28, REQ-GEO-37, REQ-GEO-30, REQ-GEO-34
- **Story:** Là chủ đầu tư, tôi muốn dữ liệu mỗi trường tách biệt ngay từ máy chủ, để học sinh trường này không bao giờ thấy dữ liệu trường khác và học sinh không thấy dữ liệu bạn.
- **Success outline:** Mọi truy vấn có bối cảnh trường; học sinh chỉ thấy bài lớp mình và dữ liệu của mình; giáo viên chỉ lớp mình.
- **Fail / edge outline:** Đoán mã bài hay mã lớp của trường khác; đổi mã trên địa chỉ; nội dung hệ thống dùng chung phải đọc được mà không lộ dữ liệu trường.
- **→ Refine in BA:** Ma trận quyền theo vai và thực thể; áp dụng cho mọi epic (V14).

### [US-GEO-105] Chạy bộ đề chuẩn và đo tỉ lệ đúng
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Tech
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-040, 047 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-25, REQ-GEO-47, REQ-GEO-49
- **Story:** Là nhóm sản phẩm, tôi muốn chạy bộ đề chuẩn và thấy tỉ lệ hình thỏa ràng buộc, đáp số đúng, số bài sai mang nhãn kiểm chứng, để biết MVP đạt chưa và chạy lại khi đổi model.
- **Success outline:** Báo cáo M-GEO-01, 04, 05, 07 cho mỗi lần chạy, kèm model và prompt_version.
- **Fail / edge outline:** Bộ đề trống; đáp số tham chiếu thiếu; AI giả lập cho kết quả không có ý nghĩa đo thực (D-GEO-047).
- **→ Refine in BA:** Định dạng báo cáo; ngưỡng ≥ 95%, ≥ 90% là đề xuất.

### [US-GEO-106] Dữ liệu: đề chuẩn và đáp số tham chiếu
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-004, 055 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-25, REQ-GEO-20
- **Story:** Là nhóm sản phẩm, tôi muốn bộ đề có đáp số tham chiếu dùng chung với kho đề, để đo và để giáo viên xem cùng một nguồn.
- **Success outline:** Mỗi đề chuẩn có đáp số tham chiếu và tham số kiểm.
- **Fail / edge outline:** Đáp số nhiều giá trị (HKG-04); đáp số theo tham số (HKG-02).
- **→ Refine in BA:** Danh sách đề và đáp số tham chiếu do BA và QC lập từ `materials/`.

### [US-GEO-107] Thử tải 1000 người dùng đồng thời
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Tech
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-013, 044, 045, 049 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-38, REQ-GEO-39, REQ-GEO-40
- **Story:** Là chủ đầu tư, tôi muốn thấy hệ thống chịu 1000 phiên đang hoạt động và đỉnh 1000 học sinh cùng mở lời giải, để tin vào kiến trúc.
- **Success outline:** Báo cáo p95 đọc, ghi, tỉ lệ lỗi theo M-GEO-20 trên dữ liệu M-GEO-19.
- **Fail / edge outline:** Vượt ngưỡng đề xuất; lộ lời giải trước giờ mở khi tải cao.
- **→ Refine in BA:** Kịch bản tải, công cụ thử (ARCH, QC).

### [US-GEO-108] Dữ liệu: bộ dữ liệu thử tải
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-049 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-50, REQ-GEO-38
- **Story:** Là nhóm sản phẩm, tôi muốn dữ liệu 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện, để thử tải thực tế.
- **Success outline:** Bộ dữ liệu sinh ra đúng M-GEO-19, tách khỏi dữ liệu mẫu.
- **Fail / edge outline:** Dữ liệu thử lẫn vào dữ liệu mẫu hoặc dữ liệu thật; tên không dùng người thật.
- **→ Refine in BA:** Cách sinh và nơi chạy (TechLead).

## Epic E2 — Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn
Chức năng: GEO-02, GEO-03, GEO-04.

### [US-GEO-201] Engine dựng hình từ ràng buộc
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-040, 061 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-02, REQ-GEO-47
- **Story:** Là học sinh, tôi muốn hình do engine tính từ ràng buộc của đề, để hình đúng thay vì tin tọa độ AI.
- **Success outline:** Đề khớp hình mẫu hoặc ràng buộc dựng được hình thỏa mọi ràng buộc theo M-GEO-02.
- **Fail / edge outline:** Ràng buộc mâu thuẫn; ràng buộc không dựng được; hình suy biến.
- **→ Refine in BA:** Danh sách loại ràng buộc hỗ trợ (từ `materials/` và hình mẫu).

### [US-GEO-202] Dựng hình mẫu có tham số không cần AI
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-056 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-03
- **Story:** Là học sinh, tôi muốn chọn một hình mẫu và đổi tham số, để khám phá mà không cần gõ đề.
- **Success outline:** Chọn chóp, lăng trụ, hộp, nón, trụ, cầu và thấy hình ngay.
- **Fail / edge outline:** Tham số không hợp lệ (âm, bằng 0).
- **→ Refine in BA:** Phạm vi và đơn vị tham số mỗi hình.

### [US-GEO-203] Kiểm chứng hình và đáp số, gắn nhãn máy
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-004, 040, 061, 079 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-04, REQ-GEO-34, REQ-GEO-47
- **Story:** Là giáo viên và học sinh, tôi muốn mỗi bài có nhãn do máy tính, để biết hình và đáp số đã được tính lại hay cần xem lại.
- **Success outline:** Đề khớp: "Đã kiểm chứng"; không khớp: "Cần xem lại"; nhãn tính theo M-GEO-02, 03.
- **Fail / edge outline:** Đáp số nhiều giá trị chỉ một giá trị đúng; sai số sát ngưỡng; AI cố tự gắn nhãn; nhãn có sẵn trong file không được dùng.
- **→ Refine in BA:** Dung sai D-GEO-040; chuỗi nhãn D-GEO-079; hiển thị nhãn trên mvp-01, 02, 05.

### [US-GEO-204] Đề lạ dùng tọa độ AI kèm cảnh báo chưa kiểm chứng
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-061 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-01, REQ-GEO-04
- **Story:** Là học sinh, tôi muốn đề không khớp hình mẫu vẫn có hình nhưng được cảnh báo, để không tin nhầm hình chưa kiểm.
- **Success outline:** Hình hiện kèm cảnh báo và nhãn "Cần xem lại".
- **Fail / edge outline:** Tọa độ AI suy biến; đề hình phẳng (OUT).
- **→ Refine in BA:** Câu chữ cảnh báo (MSG-GEO).

### [US-GEO-205] Dữ liệu: danh mục hình mẫu và tham số
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-056 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-03
- **Story:** Là nhóm sản phẩm, tôi muốn danh mục hình mẫu có tên, tham số và ràng buộc, để dùng chung cho dựng hình, tự học và kiểm.
- **Success outline:** Danh mục đủ chóp, lăng trụ, hộp, lập phương, nón, trụ, cầu, Oxyz.
- **Fail / edge outline:** Thiếu tham số mặc định.
- **→ Refine in BA:** Giá trị mặc định cho tự học.

## Epic E3 — Thao tác hình 3D trên thiết bị cảm ứng
Chức năng: GEO-05, GEO-06, GEO-07.

### [US-GEO-301] Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-008 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-05, REQ-GEO-07
- **Story:** Là học sinh, tôi muốn xoay và thu phóng hình, thấy cạnh khuất bằng nét đứt, để hình dung hình không gian.
- **Success outline:** Kéo để xoay, chụm hoặc lăn để thu phóng, Nhìn từ trước, Nhìn từ trên, Xoay tự do, Đặt lại.
- **Fail / edge outline:** Xoay lật ngược; thu phóng quá giới hạn.
- **→ Refine in BA:** Giới hạn thu phóng; Oxyz bật tắt.

### [US-GEO-302] Chạm chọn đối tượng; tô màu cạnh và mặt
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-001 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-05, REQ-GEO-07
- **Story:** Là học sinh, tôi muốn chạm chọn điểm, cạnh, mặt và thấy được tô màu, để chỉ đúng đối tượng.
- **Success outline:** Chạm chọn trên hình, đối tượng được tô màu.
- **Fail / edge outline:** Chạm nhầm khi hai đối tượng gần nhau; chạm ngoài hình.
- **→ Refine in BA:** Quy tắc ưu tiên khi chồng lên nhau.

### [US-GEO-303] Vẽ thêm đoạn, mặt phẳng; giao điểm, giao tuyến; mở rộng mặt, kéo dài cạnh
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-008 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-05
- **Story:** Là học sinh, tôi muốn vẽ thêm đoạn, mặt phẳng, tìm giao điểm, giao tuyến, mở rộng mặt và kéo dài cạnh, để dựng đường phụ.
- **Success outline:** Công cụ Đường, Mặt phẳng, Mở rộng; kết quả hiện trên hình.
- **Fail / edge outline:** Hai đường chéo nhau (cảnh báo); mặt phẳng suy biến.
- **→ Refine in BA:** Danh sách công cụ đầy đủ (KG-01).

### [US-GEO-304] Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-008 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-06
- **Story:** Là học sinh, tôi muốn thêm trung điểm, điểm chia tỉ lệ và điểm tự do trên cạnh, để dựng đường phụ.
- **Success outline:** Chọn cạnh, thêm điểm, điểm có nhãn.
- **Fail / edge outline:** Tỉ lệ không hợp lệ; điểm trùng điểm có sẵn.
- **→ Refine in BA:** Quy tắc đặt tên điểm mới.

### [US-GEO-305] Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-024 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-06, REQ-GEO-31
- **Story:** Là học sinh, tôi muốn đo độ dài, góc, khoảng cách trên hình, để kiểm yếu tố trung gian mà không đọc được đáp số.
- **Success outline:** Đo yếu tố trung gian; đại lượng đề hỏi và đại lượng cùng vai trò bị ẩn khi lời giải khóa; hiện sau giờ mở.
- **Fail / edge outline:** Đo gián tiếp để tính ra đáp số; đại lượng cùng giá trị khác tam giác.
- **→ Refine in BA:** Xác định tập đại lượng ẩn cho mỗi đề (PO-42, D-GEO-024).

### [US-GEO-306] Thao tác trên iPhone: chụm ngón, nút đặt lại, bố cục điện thoại
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-001, 033 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-07, REQ-GEO-42
- **Story:** Là học sinh dùng iPhone, tôi muốn hình ở trên và bảng gợi ý trượt lên từ dưới, chụm ngón để thu phóng, để làm bài trên màn nhỏ.
- **Success outline:** Chụm ngón, nút Đặt lại, bảng gợi ý trượt lên.
- **Fail / edge outline:** Màn xoay ngang; chạm nhầm trên màn nhỏ.
- **→ Refine in BA:** Kích thước chạm tối thiểu, hướng màn.

### [US-GEO-307] Sửa lỗi hình đã biết
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-001 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-36
- **Story:** Là học sinh, tôi muốn nhãn không lặp "Mặt Mặt …", có cảnh báo khi hai đường chéo nhau và mặt nhiều đỉnh được chia tam giác đúng, để hình không gây hiểu sai.
- **Success outline:** Ba lỗi đã biết không còn tái hiện.
- **Fail / edge outline:** Mặt nhiều đỉnh lõm.
- **→ Refine in BA:** Ca tái hiện từ bản cũ (đọc `SPEC.md`).

## Epic E4 — Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học
Chức năng: GEO-01, GEO-08, GEO-09, GEO-10, GEO-11, ANS-04.

### [US-GEO-401] Nhập đề tự học bằng chữ
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-052, 072 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-01, REQ-GEO-41, REQ-GEO-48
- **Story:** Là học sinh, tôi muốn dán đề bằng chữ và nhận hình, để tự học với đề của mình.
- **Success outline:** Dán đề, thấy màn chờ, nhận hình (hoặc đề lạ kèm cảnh báo).
- **Fail / edge outline:** Đề rỗng, quá dài, không phải hình học không gian; AI lỗi; đề hình phẳng (OUT).
- **→ Refine in BA:** Giới hạn độ dài; thông báo lỗi (D-GEO-072).

### [US-GEO-402] Nhập đề bằng ảnh
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Should
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-052 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-01, REQ-GEO-41, REQ-GEO-48
- **Story:** Là học sinh, tôi muốn chụp hoặc chọn ảnh đề, để khỏi gõ lại.
- **Success outline:** Ảnh được đọc thành chữ đề, học sinh xem lại rồi tiếp tục.
- **Fail / edge outline:** Ảnh mờ, chữ viết tay; ảnh không phải đề.
- **→ Refine in BA:** Kích thước ảnh; học sinh sửa chữ đọc được (BA hỏi: nguồn nêu "chữ hoặc ảnh" nhưng không nói bước xem lại).

### [US-GEO-403] Chọn hình mẫu để khám phá
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-052 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-01, REQ-GEO-03
- **Story:** Là học sinh, tôi muốn chọn một hình mẫu và xoay thử, để khám phá khi chưa có đề.
- **Success outline:** Chọn hình mẫu từ Tự khám phá và vào màn thao tác.
- **Fail / edge outline:** Chưa có mã lớp (D-GEO-052).
- **→ Refine in BA:** —

### [US-GEO-404] Xin Gợi ý 1 và Gợi ý 2
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-003, 053, 075 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-08
- **Story:** Là học sinh, tôi muốn xin gợi ý từng mức trước khi xem lời giải, để tự nghĩ trước.
- **Success outline:** Gợi ý 1 rồi Gợi ý 2; thanh tiến độ k/3; mỗi lần xem ghi sự kiện.
- **Fail / edge outline:** Xin Gợi ý 2 khi chưa xem Gợi ý 1; giáo viên tắt gợi ý (D-GEO-053).
- **→ Refine in BA:** Thứ tự bắt buộc hay tự do.

### [US-GEO-405] Mở lời giải của bài tự học sau Gợi ý 1 và 2
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-023 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-19
- **Story:** Là học sinh tự học, tôi muốn xem lời giải sau khi đã xem hai gợi ý, để vẫn học trước, đáp án sau.
- **Success outline:** Sau Gợi ý 1 và 2, nút Lời giải mở.
- **Fail / edge outline:** Thử mở khi chưa xem gợi ý; mở qua hỏi đáp.
- **→ Refine in BA:** Chặn ở máy chủ, không chỉ ở giao diện.

### [US-GEO-406] Tự học kế thừa khóa của bài đang khóa
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-026 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-32
- **Story:** Là giáo viên, tôi muốn đề học sinh dán lại trùng bài đang khóa của lớp thì vẫn khóa theo giờ tôi đặt, để tự học không là đường vòng.
- **Success outline:** Đề trùng hoặc gần trùng thì lời giải theo giờ mở của bài giao.
- **Fail / edge outline:** Gần trùng mơ hồ; đổi chữ, đổi tên điểm.
- **→ Refine in BA:** Định nghĩa "gần trùng" (ARCH đề xuất, PO duyệt).

### [US-GEO-407] Xem lời giải đồng bộ hình: bấm bước thì hình tô màu
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-009 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-09
- **Story:** Là học sinh, tôi muốn bấm từng bước lời giải và thấy hình tô màu đối tượng của bước đó, để hiểu bước nào làm gì.
- **Success outline:** Bấm bước, hình tô màu đúng đối tượng.
- **Fail / edge outline:** Bước không có đối tượng; chuyển bước nhanh.
- **→ Refine in BA:** Dữ liệu đối tượng của mỗi bước.

### [US-GEO-408] Đường vẽ thêm của từng bước hiện dần; "Vì sao nghĩ ra bước này?"
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-009 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-09
- **Story:** Là học sinh, tôi muốn đường vẽ thêm của từng bước lần lượt hiện lên hình và đọc được vì sao nghĩ ra bước đó, để học cách nghĩ.
- **Success outline:** Qua từng bước, đường phụ thêm dần; có "Vì sao nghĩ ra bước này?".
- **Fail / edge outline:** Quay lại bước trước (đường phụ bớt đi).
- **→ Refine in BA:** Hành vi khi nhảy bước.

### [US-GEO-409] Hỏi đáp trong ngữ cảnh bài, có nguồn
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-006, 030, 072 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-10, REQ-GEO-48
- **Story:** Là học sinh, tôi muốn hỏi trợ lý trong bài và nhận câu trả lời dựa trên giáo trình, có nguồn, để hiểu thêm.
- **Success outline:** Câu trả lời ghi nguồn; ngoài giáo trình thì nói chưa có và mời hỏi giáo viên.
- **Fail / edge outline:** AI lỗi; câu hỏi không liên quan; trợ lý bịa nguồn.
- **→ Refine in BA:** Cách chọn nguồn từ kho bài giảng mẫu.

### [US-GEO-410] Nhật ký hỏi đáp lọc theo bài
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Should
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-10, REQ-GEO-36
- **Story:** Là học sinh, tôi muốn xem lại các câu hỏi đã hỏi, lọc theo bài, để ôn lại.
- **Success outline:** Lọc theo bài, chỉ thấy câu của mình.
- **Fail / edge outline:** Bài không có câu hỏi.
- **→ Refine in BA:** Mục "Lịch sử" của thanh bên có giữ không (D-GEO-078).

### [US-GEO-411] Từ khóa bấm được mở bài giảng có nguồn và nhãn
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-007, 029, 030 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-11
- **Story:** Là học sinh, tôi muốn bấm từ khóa trong gợi ý, lời giải, câu trả lời để đọc bài giảng ngắn có ghi nguồn, để nhớ lại kiến thức.
- **Success outline:** Từ khóa mở hộp bài giảng ghi nguồn và nhãn "AI soạn · chờ giáo viên duyệt" khi áp dụng.
- **Fail / edge outline:** Từ khóa không có bài giảng; bài giảng làm lộ đáp số trước giờ mở (D-GEO-027).
- **→ Refine in BA:** Danh sách từ khóa và cách nhận diện.

### [US-GEO-412] Dữ liệu: kho bài giảng mẫu
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-029, 030, 076 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-11, REQ-GEO-10
- **Story:** Là nhóm sản phẩm, tôi muốn kho bài giảng mẫu 10 khái niệm có nguồn và nhãn, để từ khóa và hỏi đáp có chỗ dựa.
- **Success outline:** Kho có khái niệm, nội dung, nguồn, nhãn.
- **Fail / edge outline:** Bài giảng nêu đáp số của bài trong kho.
- **→ Refine in BA:** Nội dung từ giáo án mẫu hay AI viết (docx chưa đọc).

## Epic E5 — Kho đề của Giáo viên mẫu
Chức năng: KNW-01, KNW-02.

### [US-GEO-501] Xem Kho đề và lọc theo cấp, loại hình, dạng bài
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-070 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-20, REQ-GEO-39
- **Story:** Là Giáo viên mẫu, tôi muốn xem Kho đề và lọc theo cấp, loại hình, dạng bài, để chọn đề giao.
- **Success outline:** Lọc cho ra đúng tập đề; thẻ có hình, đề, nhãn.
- **Fail / edge outline:** Lọc không có kết quả; nhiều bộ lọc cùng lúc.
- **→ Refine in BA:** Giá trị bộ lọc (D-GEO-070).

### [US-GEO-502] Thẻ đề hiện nhãn máy và "AI soạn"
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-028, 079 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-20, REQ-GEO-29, REQ-GEO-04
- **Story:** Là Giáo viên mẫu, tôi muốn mỗi đề ghi "AI soạn" và nhãn máy, để biết mức tin cậy.
- **Success outline:** Mỗi thẻ có "AI soạn" và "Đã kiểm chứng" hoặc "Cần xem lại".
- **Fail / edge outline:** Đề chưa tính nhãn.
- **→ Refine in BA:** Chuỗi nhãn (D-GEO-079).

### [US-GEO-503] Xem một đề: hình, đáp số, lời giải từng bước, gợi ý
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-21
- **Story:** Là Giáo viên mẫu, tôi muốn xem hình, đáp số, lời giải từng bước và gợi ý của một đề, để quyết định giao.
- **Success outline:** Trang chi tiết đủ bốn phần; giáo viên thấy lời giải đầy đủ.
- **Fail / edge outline:** Đề không có lời giải; nút "Sửa (sắp có)" không hoạt động.
- **→ Refine in BA:** Sắp xếp màn.

### [US-GEO-504] Dữ liệu: nạp 40 đến 60 đề của Giáo viên mẫu và tính lại nhãn
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-004, 055, 062, 076 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-20, REQ-GEO-49, REQ-GEO-30
- **Story:** Là nhóm sản phẩm, tôi muốn kho có 40 đến 60 đề, ít nhất 40 đã kiểm chứng, để đủ tiêu chí hoàn thành.
- **Success outline:** Kho nạp từ `materials/` và bổ sung; nhãn tính lại bằng engine.
- **Fail / edge outline:** Đề thiếu đáp số; nhãn trong file khác nhãn tính.
- **→ Refine in BA:** Nguồn 34 đến 54 đề còn lại và người soát (D-GEO-055).

### [US-GEO-505] Kho đề tải theo trang theo khóa
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: Tech
- **Priority (MoSCoW):** Should
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-014 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-39
- **Story:** Là Giáo viên mẫu, tôi muốn danh sách đề tải thêm mượt khi cuộn, để dùng nhanh dù kho lớn.
- **Success outline:** Tải trang kế theo khóa; không lặp, không sót khi dữ liệu đổi.
- **Fail / edge outline:** Cuối danh sách; lọc đổi giữa chừng.
- **→ Refine in BA:** Kích thước trang (ARCH).

## Epic E6 — Giao bài và kiểm soát lời giải
Chức năng: IAM-02, LRN-01, LRN-02, LRN-04, ANS-01, ANS-02, ANS-03, TRU-02.

### [US-GEO-601] Dữ liệu: tài khoản Giáo viên mẫu, lớp mẫu, học sinh, bài giao mẫu
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-025, 054 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-24
- **Story:** Là nhóm sản phẩm, tôi muốn dữ liệu mẫu một Giáo viên mẫu, hai lớp, năm học sinh mỗi lớp và vài bài giao với giờ mở khác nhau (đã mở, sắp mở, chưa đặt).
- **Success outline:** Dữ liệu mẫu nạp được và lặp lại được.
- **Fail / edge outline:** Dữ liệu mẫu lẫn dữ liệu thử tải.
- **→ Refine in BA:** Tên theo D-GEO-025, 054.

### [US-GEO-602] Giao một đề cho lớp: chọn lớp
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-035, 078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-13
- **Story:** Là Giáo viên mẫu, tôi muốn chọn lớp và giao một đề, để học sinh thấy trong Bài của em.
- **Success outline:** Chọn lớp, giao đề; bài xuất hiện cho học sinh của lớp đó.
- **Fail / edge outline:** Giao hai lần một đề cho một lớp; chọn lớp không phải của mình.
- **→ Refine in BA:** Có cho giao lại không.

### [US-GEO-603] Chọn trợ giúp học sinh được dùng
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** S   |   **Difficulty:** 2
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-053 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-13
- **Story:** Là Giáo viên mẫu, tôi muốn bật hoặc tắt Gợi ý mức 1 và 2 và Hỏi đáp, để điều chỉnh trợ giúp theo lớp.
- **Success outline:** Hai công tắc ảnh hưởng đúng màn học sinh.
- **Fail / edge outline:** Tắt cả hai; đổi sau khi giao.
- **→ Refine in BA:** Hành vi khi đổi sau khi giao.

### [US-GEO-604] Đặt giờ mở lời giải khi giao bài
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-041, 043, 078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-13, REQ-GEO-16, REQ-GEO-35
- **Story:** Là Giáo viên mẫu, tôi muốn đặt giờ mở lời giải hoặc để chưa đặt, để học sinh chỉ thấy lời giải đúng lúc tôi muốn.
- **Success outline:** Đặt giờ tương lai thì Sắp mở; để trống thì Chưa đặt giờ và khóa.
- **Fail / edge outline:** Giờ ở quá khứ; giờ hiển thị khác múi giờ.
- **→ Refine in BA:** Lối tắt "Ngay bây giờ"; "Sau buổi học tới" là OUT (D-GEO-078).

### [US-GEO-605] Đổi giờ hoặc mở ngay lời giải
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-041, 042 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-16
- **Story:** Là Giáo viên mẫu, tôi muốn đổi giờ hoặc mở ngay lời giải của bài đã giao, để linh hoạt theo lớp.
- **Success outline:** Đổi giờ khi Sắp mở; Mở ngay đặt giờ bằng giờ máy chủ.
- **Fail / edge outline:** Đổi giờ khi đã Đã mở bị từ chối; hai thao tác cùng lúc; học sinh đang xem.
- **→ Refine in BA:** Chữ thông báo khi từ chối (D-GEO-042).

### [US-GEO-606] Bài của em: giờ mở và tiến độ
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-075, 078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-12
- **Story:** Là học sinh, tôi muốn thấy bài được giao, lời giải mở lúc nào và mình đã đi tới đâu, để biết làm gì tiếp.
- **Success outline:** Mỗi bài có trạng thái giờ mở và tiến độ k/3; nút Làm tiếp, Bắt đầu, Xem lại.
- **Fail / edge outline:** Chưa có bài; bài đã mở lời giải.
- **→ Refine in BA:** Các thẻ không nguồn bị bỏ (D-GEO-078).

### [US-GEO-607] Xem trước như học sinh
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-027 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-15, REQ-GEO-17
- **Story:** Là Giáo viên mẫu, tôi muốn xem bài đúng như học sinh thấy, để kiểm trước khi giao.
- **Success outline:** Màn xem trước tuân khóa lời giải như học sinh.
- **Fail / edge outline:** Xem trước làm lộ lời giải qua dữ liệu trả về.
- **→ Refine in BA:** Xem trước dùng cùng đường dữ liệu của học sinh (BR-GEO-18).

### [US-GEO-608] Máy chủ khóa lời giải và đáp số trước giờ mở
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-003, 041, 043 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-17, REQ-GEO-35, REQ-GEO-40
- **Story:** Là giáo viên, tôi muốn máy chủ không trả lời giải và đáp số cho học sinh trước giờ mở ở bất kỳ kênh nào, để học trước đáp án sau.
- **Success outline:** Trước giờ mở, dữ liệu trả về không chứa lời giải hay đáp số.
- **Fail / edge outline:** Yêu cầu trực tiếp tới địa chỉ lời giải; phản hồi đã lưu đệm; đồng hồ máy khách lệch.
- **→ Refine in BA:** Danh sách mọi kênh trả dữ liệu bài.

### [US-GEO-609] Hỏi đáp không lộ đáp số khi lời giải khóa
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 5
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-027, 065 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-18, REQ-GEO-40
- **Story:** Là giáo viên, tôi muốn trợ lý chỉ gợi ý hoặc giải thích khái niệm khi lời giải khóa, để không thành đường tắt lấy đáp số.
- **Success outline:** Câu trả lời qua kiểm tra; khi từ chối thì nhắc lại gợi ý, không tính thêm lượt.
- **Fail / edge outline:** Hỏi bằng nhiều cách; yêu cầu dịch, mã hóa, từng bước; hỏi sau giờ mở.
- **→ Refine in BA:** Định nghĩa lộ đáp số (D-GEO-065).

### [US-GEO-610] Bộ câu thử moi đáp án
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Tech
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-026, 065 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-26, REQ-GEO-32
- **Story:** Là nhóm sản phẩm, tôi muốn chạy bộ câu thử cố ép trợ lý lộ đáp số, để chứng minh 0 lần lộ.
- **Success outline:** Báo cáo M-GEO-06 cho mỗi lần chạy; gồm ca dán lại đề đang khóa vào tự học.
- **Fail / edge outline:** Câu thử mới phát hiện lộ; kết quả thay đổi theo model.
- **→ Refine in BA:** Số câu và nhóm kỹ thuật (QC đề xuất).

### [US-GEO-611] Dữ liệu: bộ câu thử moi đáp án
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-065 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-26
- **Story:** Là nhóm sản phẩm, tôi muốn bộ câu thử có phân nhóm kỹ thuật và đáp số tham chiếu, để chạy lặp lại.
- **Success outline:** Bộ câu thử lưu cùng đáp số tham chiếu của bài.
- **Fail / edge outline:** Câu thử lặp.
- **→ Refine in BA:** Nhóm kỹ thuật do QC lập.

### [US-GEO-612] Lời giải tự hiện khi tới giờ mở trong lúc học sinh đang xem
- **Actor / persona:** Học sinh (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-041, 074 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-16, REQ-GEO-17
- **Story:** Là học sinh đang xem bài, tôi muốn thấy lời giải mở ngay khi tới giờ mà không phải tải lại, để không bỏ lỡ.
- **Success outline:** Trong 10 giây sau giờ mở, trang đang mở hiện lời giải.
- **Fail / edge outline:** Mạng chập chờn; thiết bị ngủ.
- **→ Refine in BA:** Cơ chế làm mới (D-GEO-074).

## Epic E7 — Theo dõi lớp và ghi sự kiện
Chức năng: LRN-03, PRF-01.

### [US-GEO-701] Theo dõi lớp: ai đã mở bài, tình trạng
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-022, 077, 078 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-14, REQ-GEO-33
- **Story:** Là Giáo viên mẫu, tôi muốn thấy ai đã mở bài, thời gian và tình trạng của cả lớp, để biết lớp làm tới đâu, mà không có xếp hạng.
- **Success outline:** Thẻ tổng và bảng học sinh theo M-GEO-15, 21…24; thẻ vàng khi lời giải khóa.
- **Fail / edge outline:** Lớp không có sự kiện; học sinh đang làm mà chưa có sự kiện.
- **→ Refine in BA:** Định nghĩa thẻ và tình trạng (D-GEO-077); không có "Nên nhắc lại" (D-GEO-078).

### [US-GEO-702] Theo dõi lớp: mức gợi ý và câu hỏi của từng học sinh
- **Actor / persona:** Giáo viên mẫu (`actor_profiles.md`) · loại: UI
- **Priority (MoSCoW):** Must
- **Size:** M   |   **Difficulty:** 3
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-022, 077 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-14
- **Story:** Là Giáo viên mẫu, tôi muốn thấy mỗi học sinh đã dùng gợi ý mức nào và hỏi gì, để giúp đúng chỗ.
- **Success outline:** Cột Gợi ý 1·2, Hỏi đáp; danh sách câu hỏi nhóm theo nội dung.
- **Fail / edge outline:** Câu hỏi nhạy cảm; nhiều câu giống nhau.
- **→ Refine in BA:** Cách nhóm câu hỏi (không dùng AI tự nhận định).

### [US-GEO-703] Ghi sự kiện tương tác
- **Actor / persona:** Hệ thống (`actor_profiles.md`) · loại: Logic
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-058, 059 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-22, REQ-GEO-49
- **Story:** Là nhóm sản phẩm, tôi muốn ghi mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải, để theo dõi lớp và đo lường.
- **Success outline:** Mỗi hành động tạo một sự kiện có trường, bài, thời điểm; đếm theo M-GEO-08.
- **Fail / edge outline:** Gửi lặp; mất mạng; hàng triệu sự kiện.
- **→ Refine in BA:** Mức chi tiết, gộp lô (ARCH, D-GEO-058).

### [US-GEO-704] Dữ liệu: bảng sự kiện chia theo thời gian và phân trang theo khóa
- **Actor / persona:** Nhóm sản phẩm (`actor_profiles.md`) · loại: Data
- **Priority (MoSCoW):** Must
- **Size:** L   |   **Difficulty:** 4
- **Risk / Solution:** Có giả định chưa xác nhận: D-GEO-014 → câu hỏi ở `questions/`
- **Mapped REQ_IDs:** REQ-GEO-22, REQ-GEO-39, REQ-GEO-38
- **Story:** Là nhóm sản phẩm, tôi muốn bảng sự kiện chia theo thời gian và đọc theo khóa, để tải lớn vẫn nhanh.
- **Success outline:** Truy vấn nóng có chỉ mục và kế hoạch thực thi được kiểm.
- **Fail / edge outline:** Truy vấn lặp theo dòng; OFFSET.
- **→ Refine in BA:** Thiết kế của ARCH (`architecture/`).
