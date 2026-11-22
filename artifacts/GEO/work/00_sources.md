# Source register (Q1) — GEO

> **Input snapshot:** như `00_inputs.md` (22-11-2026). Mỗi trích dẫn chép nguyên văn từ tệp nguồn; dấu "[…]" nghĩa là lược phần giữa. Thứ tự ưu tiên khi mâu thuẫn: `PO-DECISIONS.md` > `brief.md` §5, §7 > `DOMAIN-PROFILE.md` > `MVP.md`, `PRODUCT-MASTER.md` Phần B > tham chiếu hành vi (`Design/`, `materials/`). Ý kiến của agent không phải nguồn.
> Cột "Used by" dẫn REQ (ở `00_gate0_intake_voc.md`), BR (ở `02_gate2_brd.md`) và D- (ở `03_decisions.md`).

| ID | Source (file § / mục) | Verbatim quote | Used by (REQ / BR / D-) |
|---|---|---|---|
| SRC-001 | `brief.md` §1 | "Hệ thống phải phục vụ **nhiều học sinh cùng lúc và nhiều trường** ngay từ kiến trúc đầu tiên." | REQ-GEO-37, REQ-GEO-38; D-GEO-012, D-GEO-013 |
| SRC-002 | `brief.md` §5, ràng buộc kỹ thuật | "(a) phục vụ nhiều học sinh dùng đồng thời; (b) kiến trúc cho nhiều trường (liên trường); (c) đáp ứng 1000 người dùng đồng thời; (d) truy vấn dữ liệu không được "smell code": tối ưu từ cách viết truy vấn, phân trang, bố cục thiết kế bảng và dữ liệu; (e) máy chủ có thể dùng Java." | REQ-GEO-37…39, REQ-GEO-45; D-GEO-012…015 |
| SRC-003 | `brief.md` §5, thứ tự làm việc | "Epic, user story, tiêu chí chấp nhận, test case và khung mã nguồn xong trước; sau đó mới chia việc cho các agent viết mã." | D-GEO-016 |
| SRC-004 | `brief.md` §7, Đa trường | "mọi dữ liệu thuộc một trường có định danh trường; truy vấn luôn lọc theo trường; học sinh trường này không bao giờ thấy dữ liệu trường khác; chia sẻ liên trường (chặng 7) phải đi qua cấp phép tường minh, nên mô hình dữ liệu phải chừa chỗ ngay từ đầu." | REQ-GEO-28, REQ-GEO-37; D-GEO-012, D-GEO-050 |
| SRC-005 | `brief.md` §7, Truy vấn | "không phân trang kiểu bỏ qua n dòng (OFFSET); dùng phân trang theo khóa; không truy vấn lặp theo từng dòng (N+1); chỉ lấy cột cần; mọi truy vấn nóng có chỉ mục phục vụ và có kiểm bằng kế hoạch thực thi; bảng sự kiện lớn được chia theo thời gian; dữ liệu nhạy cảm (lời giải) tách bảng riêng để không lộ qua truy vấn thông thường." | REQ-GEO-39, REQ-GEO-40; D-GEO-014 |
| SRC-006 | `brief.md` §7, Khóa lời giải | "ở tầng dữ liệu máy chủ trả về, không chỉ ở giao diện; ca thử moi đáp án qua hỏi đáp" | REQ-GEO-17, REQ-GEO-40; BR-GEO-01 |
| SRC-007 | `brief.md` §6 câu 1, mặc định | "Giả định PO: 1000 phiên đang hoạt động, mỗi phiên khoảng một yêu cầu mỗi 3–5 giây; đỉnh 1000 học sinh cùng mở lời giải khi giờ mở tới; p95 đọc ≤ 300 ms, ghi ≤ 500 ms; tỉ lệ lỗi < 0,5%. Đây là đề xuất, chưa phải ngưỡng đã chốt." | REQ-GEO-38; D-GEO-013, D-GEO-044, D-GEO-045 |
| SRC-008 | `brief.md` §6 câu 2, mặc định | "Giả định PO: PostgreSQL và Redis riêng của dự án (đã dựng cục bộ, cổng 54329 và 6389); chưa chọn nhà cung cấp đám mây." | D-GEO-046 |
| SRC-009 | `brief.md` §6 câu 3, mặc định | "Giả định PO: cổng AI có nhà cung cấp giả lập (nội dung soạn sẵn), tích hợp nhà cung cấp thật là bước sau; MVP vẫn có đủ tầng cổng, ghi chi phí và đổi model." | REQ-GEO-27; D-GEO-047 |
| SRC-010 | `brief.md` §6 câu 4, mặc định | "Giả định PO: đăng nhập nhẹ (tên hiển thị + mã lớp), không thu email, số điện thoại; dữ liệu tối thiểu." | REQ-GEO-23, REQ-GEO-44; D-GEO-048 |
| SRC-011 | `brief.md` §6 câu 5, mặc định | "Giả định PO: 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện." | REQ-GEO-50; D-GEO-049 |
| SRC-012 | `brief.md` §7, Thiết bị | "iPad, iPhone cho học sinh; máy tính và điện thoại cho giáo viên; tiếng Việt có dấu; số thập phân dùng dấu phẩy." | REQ-GEO-42, REQ-GEO-43 |
| SRC-013 | `brief.md` §7, Công nghệ | "máy chủ dùng Java (chủ đầu tư cho phép); Java cài sẵn là bản 26." | REQ-GEO-45; D-GEO-015, D-GEO-068 |
| SRC-014 | `brief.md` §9 | "Theo `MVP.md` §9: hình thỏa ràng buộc ≥ 95% và đáp số đúng ≥ 90% trên bộ đề chuẩn; không bài sai nào mang nhãn "Đã kiểm chứng"; không lần nào lời giải lộ trước giờ mở trong bộ câu thử; theo câu hỏi 1 cho hiệu năng. Các ngưỡng là đề xuất, chưa có số đo thực tế." | REQ-GEO-49 |
| SRC-015 | `brief.md` §7, Riêng tư | "dữ liệu trẻ em tối thiểu; chưa mở tài khoản chính thức (PO-21)." | REQ-GEO-44; D-GEO-067 |
| SRC-016 | `brief.md` §7, Hiệu năng | "theo câu hỏi 1; đo bằng bộ thử tải trên dữ liệu mẫu theo câu hỏi 5." | REQ-GEO-38, REQ-GEO-50 |
| SRC-017 | `PO-DECISIONS.md` PO-01 | "Thiết bị theo vai: học sinh iPad hoặc iPhone; phụ huynh iPhone (hoặc iPad); giáo viên web trên máy tính, kiểm tra nhanh bằng điện thoại; quản trị trường và cơ quan quản lý dùng web." | REQ-GEO-42; D-GEO-001 |
| SRC-018 | `PO-DECISIONS.md` PO-15 | "Mọi màn hình ngoài MVP có nhãn "Concept · Chặng N". Tài liệu và lộ trình không ghi mốc thời gian, chỉ ghi thứ tự." | REQ-GEO-55; D-GEO-002 |
| SRC-019 | `PO-DECISIONS.md` PO-02 | "Nhãn tin cậy có hai trục, dùng thống nhất ở mọi chặng. Máy: "Đã kiểm chứng" / "Cần xem lại". Người: "AI soạn" / "Giáo viên đã duyệt". Giáo viên không tự gắn được nhãn của máy." | REQ-GEO-04, REQ-GEO-29; BR-GEO-14; D-GEO-020 |
| SRC-020 | `PO-DECISIONS.md` PO-03 | "Điểm AI luôn là đề xuất. Chỉ giáo viên duyệt mới thành điểm chính thức. Không có tự duyệt, kể cả khi AI "chắc chắn" hay khi trường yêu cầu." | REQ-GEO-34; D-GEO-021 |
| SRC-021 | `PO-DECISIONS.md` PO-04 | "Không có bất kỳ dạng xếp hạng nào: học sinh, lớp, giáo viên, trường. Màn kết quả lớp trả lời "cả lớp vướng ở đâu"." | REQ-GEO-33; BR-GEO-15; D-GEO-022 |
| SRC-022 | `PO-DECISIONS.md` PO-07 | "Bài tự học (không có giáo viên giao): lời giải mở sau khi học sinh đã xem gợi ý 1 và 2." | REQ-GEO-19; BR-GEO-05; D-GEO-023 |
| SRC-023 | `PO-DECISIONS.md` PO-08 | "Trước giờ mở lời giải, công cụ đo không hiện giá trị của chính đại lượng mà đề bài hỏi. Học sinh vẫn đo được các yếu tố trung gian." | REQ-GEO-31; BR-GEO-16; D-GEO-024 |
| SRC-024 | `PO-DECISIONS.md` PO-42 | "Công cụ đo ẩn mọi đại lượng có cùng giá trị và cùng vai trò với đại lượng đề hỏi trong cùng một tam giác lời giải (ví dụ ở bài hình chóp, góc ASD cũng ẩn như góc SDA)." | REQ-GEO-31; BR-GEO-16; D-GEO-024 |
| SRC-025 | `PO-DECISIONS.md` PO-14 | "Dữ liệu mẫu dùng chung: lớp 11A1 và 9B; học sinh An, Bình, Chi, Dũng, Hà (thêm Lan, Khoa, Vy cho nhóm liên trường); "Giáo viên Toán · 11A1"; trường "THPT Mẫu A", "THPT Mẫu B", "THCS Mẫu C". Không dùng tên người hay tổ chức thật." | REQ-GEO-24; D-GEO-025, D-GEO-054 |
| SRC-026 | `PO-DECISIONS.md` PO-16 | "Đề học sinh tự nhập mà trùng hoặc gần trùng với một bài đang khóa của lớp mình thì kế thừa khóa của bài đó: lời giải mở theo giờ giáo viên đặt, không theo quy tắc tự học. Bộ câu thử có thêm ca "dán lại đề đang khóa vào tự học"." | REQ-GEO-32; BR-GEO-17; D-GEO-026 |
| SRC-027 | `PO-DECISIONS.md` PO-17 | "PO-08 áp dụng cho mọi nơi có ô đáp số hoặc lời giải đang khóa: bài luyện có chấm, bài hàm số (thanh trượt không tự hiện đại lượng đề hỏi), phòng học nhóm." | REQ-GEO-31; D-GEO-024 |
| SRC-028 | `PO-DECISIONS.md` PO-22 | "Giờ mở lời giải áp dụng cho mọi kênh: trả bài từng em không kèm lời giải trước giờ mở chung; link chia sẻ, bản PDF hay ảnh xuất ra, màn trình chiếu cho lớp khác và tài khoản phụ huynh đều tuân theo." | REQ-GEO-17; BR-GEO-18; D-GEO-027 |
| SRC-029 | `PO-DECISIONS.md` PO-25 | "Kéo mặt phẳng và thiết diện động thuộc chặng 2, mức P1. MVP giao từng đề; giao theo bộ thuộc chặng 3. Chỉ giáo viên dạy lớp đó duyệt điểm; chấm chéo để sau." | REQ-GEO-13; REQ-GEO-54; D-GEO-035 |
| SRC-030 | `PO-DECISIONS.md` PO-27 | "Giáo viên có bước soạn giáo án và đưa tài liệu lên hệ thống. Ba cách đưa vào: chat với AI để soạn; import tài liệu (docx, xlsx, pdf); chụp hoặc chọn nhiều ảnh để hệ thống đọc chữ (OCR). Kết quả là bài tập có gợi ý và lời giải theo đúng định dạng giáo viên yêu cầu, có bước xem trước để giáo viên kiểm tra, và có chatbot AI để sửa tiếp sau khi import." | REQ-GEO-52; D-GEO-010, D-GEO-011 |
| SRC-031 | `PO-DECISIONS.md` PO-28 | "Khi soạn với AI, giáo viên chỉ cần định hướng: hướng lời giải, loại bài tập, mức chi tiết. AI soạn bài tập, gợi ý, lời giải; giáo viên lưu vào giáo trình." | REQ-GEO-52; D-GEO-010 |
| SRC-032 | `PO-DECISIONS.md` PO-29 | "Từ giáo trình chung của lớp, tạo được giáo trình riêng cho từng học sinh dựa trên điểm mạnh và phần còn yếu của em đó." | REQ-GEO-53; D-GEO-010, D-GEO-011 |
| SRC-033 | `PO-DECISIONS.md` PO-32 | "Vị trí trong lộ trình: soạn với AI và import thuộc chặng 2 (thêm đề) và chặng 5 (trợ lý giáo viên, kho tri thức); giáo trình riêng thuộc chặng 6 (hồ sơ học tập). Trong prototype các màn này mang nhãn Concept. Tài liệu mẫu của giáo viên nằm ở thư mục `materials/`." | REQ-GEO-52, REQ-GEO-53; D-GEO-011 |
| SRC-034 | `PO-DECISIONS.md` PO-33 | "Khi học sinh hỏi AI, câu trả lời dựa trên giáo trình của giáo viên (kho tri thức) và ghi rõ lấy từ đâu." | REQ-GEO-10; D-GEO-006 |
| SRC-035 | `PO-DECISIONS.md` PO-34 | "Trong gợi ý, lời giải và câu trả lời có các từ khóa bấm được; bấm vào mở bài giảng để đọc lại. Bài giảng lấy từ bài soạn của giáo viên, hoặc do AI viết khi giáo viên chưa có; mỗi bài giảng ghi rõ nguồn." | REQ-GEO-11; D-GEO-007 |
| SRC-036 | `PO-DECISIONS.md` PO-35 | "Hình không gian tương tác được: xoay, vẽ thêm (điểm, đoạn, mặt phẳng), mở rộng mặt và kéo dài cạnh, để học sinh dễ hình dung." | REQ-GEO-05; D-GEO-008 |
| SRC-037 | `PO-DECISIONS.md` PO-36 | "Khi học sinh xem lời giải, phần vẽ thêm của từng bước lần lượt được thêm vào hình." | REQ-GEO-09; D-GEO-009 |
| SRC-038 | `PO-DECISIONS.md` PO-37 | "Hỏi đáp theo giáo trình vẫn tuân giờ mở lời giải (PO-22): trước giờ mở, câu trả lời chỉ dẫn tới khái niệm và bài giảng, không nêu đáp số." | REQ-GEO-18; BR-GEO-02; D-GEO-027 |
| SRC-039 | `PO-DECISIONS.md` PO-43 | "Bài giảng khái niệm do AI viết mà giáo viên chưa duyệt vẫn hiện cho học sinh và được trợ lý dùng làm nguồn, với nhãn rõ "AI soạn · chờ giáo viên duyệt"." | REQ-GEO-11; D-GEO-029 |
| SRC-040 | `PO-DECISIONS.md` PO-44 | "Bài giảng được nêu công thức và phương pháp tổng quát trước giờ mở lời giải, nhưng không nêu đáp số hay các bước của bài đang làm. Khi từ chối nêu đáp số, trợ lý nhắc lại gợi ý ở mức học sinh đang có và không tính thêm lượt gợi ý." | REQ-GEO-18; D-GEO-027, D-GEO-065 |
| SRC-041 | `PO-DECISIONS.md` PO-45 | "Giáo viên soạn được cả bài tập lẫn bài giảng bằng cách trao đổi với AI, hoặc import từ file. Giáo viên chat với AI để chỉnh sửa, cập nhật bản nháp của AI. Phần trình chiếu phải thể hiện rõ ba việc này." | REQ-GEO-52; D-GEO-010, D-GEO-011 |
| SRC-042 | `PO-DECISIONS.md` PO-49 | "từ khóa bấm được, bài giảng có ghi nguồn và hỏi đáp dựa trên giáo trình nằm trong MVP ở mức bài giảng và giáo trình mẫu do hệ thống cung cấp (nguồn là giáo án mẫu của Giáo viên mẫu hoặc AI viết, ghi rõ nhãn). Giáo viên tự soạn và duyệt bài giảng thuộc chặng 2." | REQ-GEO-10, REQ-GEO-11; D-GEO-030 |
| SRC-043 | `PO-DECISIONS.md` PO-12 | "Sản phẩm chạy dạng web trên trình duyệt của iPad và iPhone cho tới hết chặng 4." | REQ-GEO-42; D-GEO-033 |
| SRC-044 | `PO-DECISIONS.md` PO-05 | "Giáo viên thật ở chặng 2 vào bằng lời mời. Đăng ký và xác nhận tài khoản thuộc chặng 3." | REQ-GEO-54; D-GEO-034 |
| SRC-045 | `PO-DECISIONS.md` PO-19 | "Thứ tự nguồn: tài liệu này → `PRODUCT-MASTER.md` và `MVP.md` → `STAGES-AFTER-MVP.md`." | D-GEO-031 |
| SRC-046 | `PO-DECISIONS.md` đầu tệp | "Không ai được tự thêm tính năng, quy tắc hay số liệu ngoài ba nguồn trên. Chi tiết nào chưa có trong nguồn phải được nêu ra để PO quyết, không tự đưa vào như yêu cầu đã có." | toàn bộ |
| SRC-047 | `PO-DECISIONS.md` Việc PO không tự quyết | "Ai soát kho đề trước khi học sinh thật dùng." | D-GEO-055 |
| SRC-048 | `DOMAIN-PROFILE.md` §8 BR-01 | "Bài được giao: trước giờ mở, máy chủ không gửi lời giải và đáp số xuống thiết bị. Ẩn ở giao diện là không đủ." | REQ-GEO-17; BR-GEO-01 |
| SRC-049 | `DOMAIN-PROFILE.md` §8 BR-02 | "Khi lời giải còn khóa, hỏi đáp chỉ được gợi ý hoặc giải thích khái niệm, không nêu đáp số; câu trả lời phải qua kiểm tra trước khi trả về." | REQ-GEO-18; BR-GEO-02 |
| SRC-050 | `DOMAIN-PROFILE.md` §8 BR-03 | "Chỉ giáo viên của lớp được đặt, đổi, hoặc mở ngay giờ mở lời giải. Bài giao có thể chưa đặt giờ mở; khi đó lời giải vẫn khóa *(suy diễn từ dữ liệu mẫu "chưa đặt")*." | REQ-GEO-16, REQ-GEO-35; BR-GEO-03; D-GEO-043 |
| SRC-051 | `DOMAIN-PROFILE.md` §8 BR-04 | "Gợi ý đi trước lời giải: mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải đầy đủ." | REQ-GEO-08; BR-GEO-04 |
| SRC-052 | `DOMAIN-PROFILE.md` §8 BR-05 | "Bài tự học: mức 3 mở sau khi học sinh đã xem mức 1 và 2 *(giả định, chờ chủ sản phẩm xác nhận)*." | REQ-GEO-19; BR-GEO-05; D-GEO-023 |
| SRC-053 | `DOMAIN-PROFILE.md` §8 BR-06 | "Hình dựng từ ràng buộc do engine tính tọa độ; không tin thẳng tọa độ AI sinh ra." | REQ-GEO-02; BR-GEO-06 |
| SRC-054 | `DOMAIN-PROFILE.md` §8 BR-07 | "Nhãn "Đã kiểm chứng" chỉ gắn khi đáp số khớp kết quả tính toán; không bài sai nào được mang nhãn này. Trường hợp còn lại mang nhãn "Cần xem lại"." | REQ-GEO-04; BR-GEO-07; D-GEO-061 |
| SRC-055 | `DOMAIN-PROFILE.md` §8 BR-08 | "Đề lạ không khớp hình mẫu thì dùng tọa độ AI và phải hiện cảnh báo chưa kiểm chứng." | REQ-GEO-01, REQ-GEO-04; BR-GEO-08; D-GEO-061 |
| SRC-056 | `DOMAIN-PROFILE.md` §8 BR-09 | "Engine chỉ kiểm chứng phần tính toán, không kiểm chứng lập luận chứng minh. Lời giải do AI soạn luôn ghi rõ "AI soạn"." | REQ-GEO-04, REQ-GEO-29; BR-GEO-09 |
| SRC-057 | `DOMAIN-PROFILE.md` §8 BR-10 | "Kho đề MVP thuộc tài khoản Giáo viên mẫu; lời giải do AI soạn và đứng tên giáo viên này." | REQ-GEO-20, REQ-GEO-29; BR-GEO-10 |
| SRC-058 | `DOMAIN-PROFILE.md` §8 BR-11 | "Lời giải đã giao không sửa tại chỗ; sửa thì tạo version mới." | REQ-GEO-30; BR-GEO-11; D-GEO-062 |
| SRC-059 | `DOMAIN-PROFILE.md` §8 BR-12 | "Học sinh chỉ thấy bài của lớp mình và dữ liệu của chính mình." | REQ-GEO-28; BR-GEO-12 |
| SRC-060 | `DOMAIN-PROFILE.md` §8 BR-13 | "Giáo viên là người quyết định cuối." | REQ-GEO-34; BR-GEO-13 |
| SRC-061 | `DOMAIN-PROFILE.md` §9 | "Mọi bản ghi có chủ sở hữu (`owner_id`) và có sẵn trường tổ chức (`org_id`) để trống; mọi cấu trúc dữ liệu có số phiên bản lược đồ." | REQ-GEO-30; D-GEO-050 (xung đột, xem `01_understanding.md` C-01) |
| SRC-062 | `DOMAIN-PROFILE.md` §10 Hiệu năng | "Thời gian trung vị từ lúc gửi đề tới lúc có hình: ngưỡng đề xuất dưới 40 giây. Chi phí AI mỗi bài: cần đo, chưa đặt ngưỡng. Phải có màn chờ rõ ràng khi AI đang xử lý." | REQ-GEO-41, REQ-GEO-48; D-GEO-060 |
| SRC-063 | `DOMAIN-PROFILE.md` §10 Độ tin cậy của AI | "Hình và đáp số qua kiểm chứng; nhãn trạng thái rõ ràng; độ không chắc chắn phải được hiện ra; version hóa prompt, model và bộ đề đánh giá; bộ đề chuẩn chạy lại khi đổi model." | REQ-GEO-47; REQ-GEO-25 |
| SRC-064 | `DOMAIN-PROFILE.md` §10 Kiến trúc | "Rời runtime Claude artifact; web app riêng có backend; một khối nhiều module; engine hình học thuần hàm, không phụ thuộc giao diện; mỗi môn học là một module." | REQ-GEO-46 |
| SRC-065 | `DOMAIN-PROFILE.md` §10 Riêng tư | "MVP chỉ thu tên hiển thị và mã lớp. Dữ liệu học sinh riêng tư mặc định; giảm tối đa dữ liệu học sinh gửi cho nhà cung cấp model." | REQ-GEO-44; D-GEO-067 |
| SRC-066 | `DOMAIN-PROFILE.md` §10 Ngôn ngữ | "Tiếng Việt có dấu là mặc định; thông báo lỗi dễ hiểu, phù hợp lứa tuổi; ký hiệu toán đọc được." | REQ-GEO-43 |
| SRC-067 | `DOMAIN-PROFILE.md` §11 | "MVP hoàn thành khi: hình thỏa ràng buộc và đáp số đúng đạt ngưỡng đề xuất trên bộ đề chuẩn; không bài sai nào mang nhãn "Đã kiểm chứng"; không lần nào lời giải lộ trước giờ mở trong bộ câu thử; toàn bộ P0 chạy được trên các thiết bị ở mục 10; kho đề có ít nhất 40 bài đã kiểm chứng." | REQ-GEO-49 |
| SRC-068 | `DOMAIN-PROFILE.md` §14, điều phải đưa vào AC | "Kiểm tra khóa lời giải ở tầng dữ liệu máy chủ trả về, không chỉ ở giao diện; có ca thử moi đáp án qua hỏi đáp." | REQ-GEO-40; REQ-GEO-26 |
| SRC-069 | `DOMAIN-PROFILE.md` §14 | "Ca giờ mở: chưa đặt, sắp mở, đã mở, đổi giờ, mở ngay; học sinh đang mở bài đúng lúc tới giờ." | REQ-GEO-16; D-GEO-041, D-GEO-042 |
| SRC-070 | `DOMAIN-PROFILE.md` §14 | "Ca nhãn: bài khớp kiểm chứng, bài không khớp, đề lạ dùng tọa độ AI kèm cảnh báo." | REQ-GEO-04 |
| SRC-071 | `DOMAIN-PROFILE.md` §14 | "Ca phân quyền: học sinh lớp này không thấy bài lớp khác và dữ liệu của bạn khác." | REQ-GEO-28 |
| SRC-072 | `DOMAIN-PROFILE.md` §14, không được suy diễn | "Không viết story cho giáo viên thật tự soạn đề, đăng ký tài khoản, phụ huynh, chấm bài, trường hay thông báo trong MVP." | REQ-GEO-52, REQ-GEO-54 |
| SRC-073 | `DOMAIN-PROFILE.md` §5 | "GEO-01 | Nhập đề tự học | Học sinh dán đề bằng chữ hoặc ảnh, hoặc chọn hình mẫu để khám phá." | REQ-GEO-01 |
| SRC-074 | `DOMAIN-PROFILE.md` §5 | "GEO-02 | Dựng hình từ ràng buộc | AI mô tả hình bằng ràng buộc; engine tính tọa độ và kiểm tra lại." | REQ-GEO-02 |
| SRC-075 | `DOMAIN-PROFILE.md` §5 | "GEO-03 | Thư viện hình mẫu | Chóp, lăng trụ, hộp, nón, trụ, cầu có tham số, dựng không cần AI." | REQ-GEO-03 |
| SRC-076 | `DOMAIN-PROFILE.md` §5 | "GEO-04 | Kiểm chứng và nhãn tin cậy | So đáp số AI với kết quả tính từ tọa độ; gắn nhãn "Đã kiểm chứng" hoặc "Cần xem lại"." | REQ-GEO-04 |
| SRC-077 | `DOMAIN-PROFILE.md` §5 | "GEO-05 | Thao tác hình 3D | Xoay, phóng to, tô màu cạnh và mặt, vẽ đường, dựng mặt phẳng, giao điểm, giao tuyến, trục Oxyz." | REQ-GEO-05 |
| SRC-078 | `DOMAIN-PROFILE.md` §5 | "GEO-06 | Thêm điểm và đo đạc | Trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh; đo độ dài, góc, khoảng cách ngay trên hình." | REQ-GEO-06 |
| SRC-079 | `DOMAIN-PROFILE.md` §5 | "GEO-07 | Hiển thị và cảm ứng | Nét đứt cho cạnh khuất, chạm chọn đối tượng trên hình, chụm ngón để phóng to, đặt lại góc nhìn." | REQ-GEO-07 |
| SRC-080 | `DOMAIN-PROFILE.md` §5 | "GEO-08 | Thang gợi ý | Mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải đầy đủ kèm nguyên lý." | REQ-GEO-08 |
| SRC-081 | `DOMAIN-PROFILE.md` §5 | "GEO-09 | Lời giải đồng bộ hình | Bấm bước nào, hình tô màu đối tượng và đường phụ của bước đó; có "Vì sao nghĩ ra bước này?"." | REQ-GEO-09 |
| SRC-082 | `DOMAIN-PROFILE.md` §5 | "GEO-10 | Hỏi đáp theo bài | Hỏi trợ lý trong ngữ cảnh bài; nhật ký hỏi đáp lọc theo bài." | REQ-GEO-10 |
| SRC-083 | `DOMAIN-PROFILE.md` §5 | "GEO-11 | Thuật ngữ và bài giảng ngắn | Bấm thuật ngữ để đọc bài giảng ngắn. Có ở bản hiện tại; `MVP.md` chưa nêu rõ." | REQ-GEO-11; D-GEO-030, D-GEO-056 |
| SRC-084 | `DOMAIN-PROFILE.md` §5 | "LRN-01 | Bài của em | Danh sách bài được giao, hiện giờ mở lời giải và tiến độ của mình." | REQ-GEO-12 |
| SRC-085 | `DOMAIN-PROFILE.md` §5 | "LRN-02 | Giao bài | Chọn lớp, chọn trợ giúp học sinh được dùng, đặt giờ mở lời giải; giao một đề hoặc một bộ đề." | REQ-GEO-13; D-GEO-035, D-GEO-053 |
| SRC-086 | `DOMAIN-PROFILE.md` §5 | "LRN-03 | Theo dõi lớp | Ai đã mở bài, dùng gợi ý mức nào, hỏi gì." | REQ-GEO-14 |
| SRC-087 | `DOMAIN-PROFILE.md` §5 | "LRN-04 | Xem trước như học sinh | Giáo viên xem bài đúng như học sinh sẽ thấy." | REQ-GEO-15 |
| SRC-088 | `DOMAIN-PROFILE.md` §5 | "ANS-01 | Giờ mở lời giải | Đặt, đổi, hoặc mở ngay thời điểm hiện lời giải của một bài đã giao." | REQ-GEO-16 |
| SRC-089 | `DOMAIN-PROFILE.md` §5 | "ANS-02 | Khóa lời giải ở máy chủ | Trước giờ mở, máy chủ không gửi lời giải và đáp số xuống thiết bị." | REQ-GEO-17 |
| SRC-090 | `DOMAIN-PROFILE.md` §5 | "ANS-03 | Hỏi đáp không lộ đáp án | Khi lời giải còn khóa, trợ lý chỉ gợi ý; câu trả lời được kiểm tra trước khi trả về." | REQ-GEO-18 |
| SRC-091 | `DOMAIN-PROFILE.md` §5 | "ANS-04 | Mở lời giải bài tự học | Mức 3 mở sau khi đã xem mức 1 và 2 *(giả định)*." | REQ-GEO-19 |
| SRC-092 | `DOMAIN-PROFILE.md` §5 | "KNW-01 | Kho đề soạn sẵn | 40–60 đề hình không gian, lọc theo cấp, loại hình, dạng bài; mỗi đề ghi "AI soạn" và có nhãn kiểm chứng." | REQ-GEO-20; D-GEO-055 |
| SRC-093 | `DOMAIN-PROFILE.md` §5 | "KNW-02 | Xem đề và lời giải | Xem hình, đáp số, lời giải từng bước, gợi ý của một đề." | REQ-GEO-21 |
| SRC-094 | `DOMAIN-PROFILE.md` §5 | "PRF-01 | Nhật ký tương tác | Ghi mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải." | REQ-GEO-22 |
| SRC-095 | `DOMAIN-PROFILE.md` §5 | "IAM-01 | Đăng nhập nhẹ | Học sinh vào bằng tên hiển thị và mã lớp; dữ liệu riêng từng người." | REQ-GEO-23 |
| SRC-096 | `DOMAIN-PROFILE.md` §5 | "IAM-02 | Tài khoản và lớp mẫu | Một Giáo viên mẫu, hai lớp mẫu (một THCS, một THPT), vài học sinh và bài giao mẫu." | REQ-GEO-24 |
| SRC-097 | `DOMAIN-PROFILE.md` §5 | "TRU-01 | Bộ đề chuẩn | Đo tỉ lệ hình thỏa ràng buộc và đáp số đúng." | REQ-GEO-25 |
| SRC-098 | `DOMAIN-PROFILE.md` §5 | "TRU-02 | Bộ câu thử moi đáp án | Kiểm tra lời giải không lộ trước giờ mở, kể cả qua hỏi đáp." | REQ-GEO-26 |
| SRC-099 | `DOMAIN-PROFILE.md` §5 | "TRU-03 | Lớp gọi AI phía máy chủ | Giấu khóa truy cập, đổi được model, ghi chi phí từng lượt gọi." | REQ-GEO-27 |
| SRC-100 | `DOMAIN-PROFILE.md` §14, thứ tự epic | "Mỗi epic nên nằm trọn trong một sub-domain và một chặng, và khép được một luồng giá trị cho một vai." | `04_story_map.md` |
| SRC-101 | `MVP.md` §1 | "MVP **chỉ làm hình học không gian**. Hình phẳng để sau." | REQ-GEO-51; D-GEO-005 |
| SRC-102 | `MVP.md` §1 | "**Gợi ý trước.** Lời giải chỉ hiện từ thời điểm giáo viên thiết lập." | REQ-GEO-08, REQ-GEO-16; D-GEO-003 |
| SRC-103 | `MVP.md` §1 | "Có **kho đề soạn sẵn** làm tài liệu của giáo viên; lời giải do AI sinh, đứng tên một giáo viên." | REQ-GEO-20, REQ-GEO-29; D-GEO-005 |
| SRC-104 | `MVP.md` §1 | "Rời runtime Claude artifact, làm web app riêng có backend." | REQ-GEO-46; D-GEO-005 |
| SRC-105 | `MVP.md` §5 C, sửa lỗi đã biết | "Sửa lỗi đã biết: nhãn "Mặt Mặt …", cảnh báo khi hai đường chéo nhau, chia tam giác cho mặt nhiều đỉnh, nhật ký hỏi đáp lọc theo bài." | REQ-GEO-36 |
| SRC-106 | `MVP.md` §5 D | "Hỏi AI: câu trả lời dựa trên giáo trình và ghi nguồn; trước giờ mở không nêu đáp số; ngoài giáo trình thì nói rõ là chưa có (PO-37, PO-49)." | REQ-GEO-10, REQ-GEO-18 |
| SRC-107 | `MVP.md` §5 E | "Giao một đề hoặc một bộ đề cho lớp mẫu; đặt, đổi, mở sớm thời điểm hiện lời giải." | REQ-GEO-13, REQ-GEO-16; D-GEO-035 |
| SRC-108 | `MVP.md` §9 | "Phiên có ít nhất 1 thao tác trên hình | ≥ 60%" | REQ-GEO-49; D-GEO-058, D-GEO-059 |
| SRC-109 | `MVP.md` §10 rủi ro 1 | "Lời giải AI đứng tên giáo viên nhưng chưa có người duyệt. Engine chỉ kiểm chứng được phần tính toán, không kiểm chứng lập luận chứng minh." | REQ-GEO-29; D-GEO-028 |
| SRC-110 | `PRODUCT-MASTER.md` B8 | "`schema_version` trên mọi JSON; tách `Problem`, `SceneSpec`, `Solution`, `VerificationReport`, `Assignment`, `Interaction`." | REQ-GEO-30 |
| SRC-111 | `PRODUCT-MASTER.md` B8 | "Lời giải lưu `model`, `prompt_version`, `author_id`; đã giao thì sửa bằng version mới." | REQ-GEO-30, REQ-GEO-47; BR-GEO-11 |
| SRC-112 | `PRODUCT-MASTER.md` B8 | "`release_at` trên bài giao: mầm của kiểm soát đáp án." | REQ-GEO-16 |
| SRC-113 | `PRODUCT-MASTER.md` D-13 | "hình và đáp số phải qua kiểm chứng bằng tính toán trước khi gắn nhãn tin cậy" | REQ-GEO-04; D-GEO-004 |
| SRC-114 | `PRODUCT-MASTER.md` B4 | "Chóp, lăng trụ, hộp; nón, trụ, cầu cơ bản; Oxyz" | REQ-GEO-03; D-GEO-056 |
| SRC-115 | `Design/prototype/README.md` | "Trước giờ mở: không nêu đáp số, không đi vào từng bước. Câu hỏi ngoài giáo trình: "Giáo trình của lớp chưa có phần này", mời hỏi giáo viên." | REQ-GEO-10, REQ-GEO-18 |
| SRC-116 | `Design/prototype/README.md` | "Kho bài giảng mẫu có 10 khái niệm: 7 bài lấy từ tài liệu mẫu `Giao-an-Hinh-khong-gian-11A1.docx`, 3 bài do AI viết (Tỉ số lượng giác trong tam giác vuông, Hai đường thẳng chéo nhau, Mặt cầu ngoại tiếp)." | REQ-GEO-11; D-GEO-030 |
| SRC-117 | `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx`, sheet Dinh huong | "Tính góc; tính khoảng cách; tính thể tích và diện tích." | REQ-GEO-20; D-GEO-070 |
| SRC-118 | `materials/…xlsx`, sheet Dinh huong | "Gợi ý 1 → gợi ý 2 → lời giải. Lời giải mở theo thời điểm giáo viên đặt khi giao bài." | REQ-GEO-08 |
| SRC-119 | `Design/prototype/README.md`, công cụ hình | "thanh công cụ: Chọn, Điểm (trung điểm, chia tỉ lệ, giao điểm), Đoạn, Mặt phẳng, Mở rộng (chạm cạnh để kéo dài, chạm mặt để mở rộng), Đo dài, Đo góc, Đặt lại" | REQ-GEO-05, REQ-GEO-06; D-GEO-058 |
| SRC-120 | `DOMAIN-PROFILE.md` §13, câu hỏi 8 | "Mục "chọn trợ giúp học sinh được dùng" khi giao bài gồm những lựa chọn nào (tắt hỏi đáp, giới hạn mức gợi ý)?" | REQ-GEO-13; D-GEO-053 |
