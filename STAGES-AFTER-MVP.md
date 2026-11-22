# Các chặng sau MVP — Brainstorm và thiết kế chi tiết

- **Trạng thái:** Đề xuất, chờ chủ sản phẩm duyệt
- **Thuộc về:** [`PRODUCT-MASTER.md`](PRODUCT-MASTER.md), Phần C và D1. Thứ tự nguồn khi mâu thuẫn: `PO-DECISIONS.md` → `PRODUCT-MASTER.md` và `MVP.md` → tài liệu này.
- **Hiệu lực (PO-19):** chỉ nội dung dẫn được về hai nguồn trên mới là yêu cầu. Các quy tắc ở mục 7 của từng chặng chưa gắn nguồn là **đề xuất chưa duyệt**.
- **Phán quyết sau phản biện, áp lên mọi chặng** (chi tiết trong `PO-DECISIONS.md`): PO-16 tự học kế thừa khóa · PO-17 giới hạn công cụ đo ở bài luyện, bài hàm số, phòng nhóm · PO-18 quyền thêm đề · PO-20 xác minh phụ huynh và duyệt mục "Ở nhà giúp con" · PO-21 chặng 3 và 5 chờ câu trả lời pháp lý · PO-22 giờ mở áp dụng mọi kênh · PO-23 phụ huynh không xem câu con hỏi · PO-24 dữ liệu định danh và so sánh AI–giáo viên · PO-25 chặng của kéo mặt phẳng, giao theo bộ, chấm chéo · PO-12 web tới hết chặng 4. Danh sách phát hiện và cách xử lý: `review/RV_triage.md`.
- **Phạm vi:** chặng 2 đến chặng 9. Chặng 1 (MVP hình không gian) đang làm và chỉ được nhắc như điểm xuất phát.
- **Mức độ:** thiết kế sản phẩm và trải nghiệm, không phải thiết kế kỹ thuật.

---

## 0. Mở đầu

### 0.1 Cách đọc

Mỗi chặng có đúng 11 mục theo cùng một khuôn: (1) mục tiêu và điều kiện mở, (2) người dùng và thiết bị, (3) brainstorm, (4) phạm vi, (5) luồng chính, (6) màn hình, (7) quy tắc và quyền, (8) dữ liệu mới, (9) phụ thuộc, (10) rủi ro và chỉ số, (11) câu hỏi mở.

- Tài liệu chỉ ghi **thứ tự** và **điều kiện mở chặng**, không đặt thời hạn.
- Mã màn hình có dạng `C<chặng>-<số>`. Cột "Concept" cho biết màn hình đã có bản phác trong `Design/` hay chưa.
- Luồng viết theo dạng "vai · thiết bị: thao tác → phản hồi của hệ thống".
- Xếp loại ý tưởng: **Làm** (làm ngay trong chặng), **Sau** (để sau), **Bỏ**.
- Chỗ nào chưa có căn cứ thì ghi "cần xác minh". Tài liệu không dùng số liệu thị trường hay pháp lý nào.

### 0.2 Nguyên tắc áp dụng cho mọi chặng

| # | Nguyên tắc | Hệ quả khi thiết kế |
|---|---|---|
| 1 | Gợi ý trước, lời giải sau | Lời giải, đáp số và điểm chỉ hiện từ thời điểm giáo viên đặt. Khóa ở máy chủ, áp dụng cả cho hỏi đáp. |
| 2 | Kết quả AI phải qua kiểm chứng | Mọi sản phẩm AI mang nhãn trạng thái. Không có nhãn "đã kiểm chứng" nếu chưa có phép kiểm thật. |
| 3 | Giáo viên quyết định cuối | Điểm AI chỉ là đề xuất. Không có đường tắt từ đề xuất sang chính thức. |
| 4 | Nhận định dựa trên bằng chứng | Mỗi nhận định về học sinh bấm vào được bài làm hoặc câu hỏi căn cứ. Không dùng từ gắn nhãn con người. |
| 5 | Riêng tư dữ liệu trẻ em | Thu thập tối thiểu, quyền tối thiểu, mặc định là không chia sẻ. |
| 6 | Đúng thiết bị của từng vai | Xem bảng dưới. |

| Vai | Thiết bị chính | Thiết bị phụ | Ý nghĩa thiết kế |
|---|---|---|---|
| Học sinh | iPad | iPhone | iPad để thao tác hình và làm bài; iPhone để xem bài, chụp bài, xem gợi ý. Không thiết kế bản máy tính riêng cho học sinh. |
| Phụ huynh | iPhone | iPad | Một cột, đọc nhanh, ít thao tác. |
| Giáo viên | Web trên máy tính | Điện thoại | Máy tính để soạn, chấm, duyệt. Điện thoại chỉ để "liếc và quyết nhanh": xem ai chưa làm, mở lời giải, trả lời tin nhắn. |
| Quản trị trường | Web | — | Bảng, bộ lọc, nhập danh sách. |
| Cơ quan quản lý | Web | — | Bảng tổng hợp, văn bản. |

### 0.3 Bảy ý tưởng xuyên chặng đáng giá nhất

1. **Khung môn học.** Mỗi môn là một module có `parse`, `solve`, `verify`, `render`, `hint`. Đề xuất thêm `grade` (chấm đáp số) và `concepts` (danh mục khái niệm của môn) từ chặng 2, để bài luyện và hồ sơ học tập về sau không phải viết riêng cho từng môn.
2. **Nhãn tin cậy hai trục.** Trục máy: "Đã kiểm chứng" hoặc "Cần xem lại". Trục người: "AI soạn" hoặc "Giáo viên đã duyệt". Hai trục độc lập và luôn hiện cùng nhau. Cùng một cách ghi nhãn dùng cho lời giải, điểm đề xuất, nhận định hồ sơ và bản nháp của trợ lý.
3. **Một cổng công bố duy nhất.** `release_at` của MVP lớn dần thành lịch công bố chung cho lời giải, đáp số, điểm, nhận xét và nhận định gửi phụ huynh. Một cơ chế, một nơi kiểm tra ở máy chủ, một bộ câu thử moi đáp án.
4. **Một hộp duyệt cho giáo viên.** Lời giải AI, điểm đề xuất, bản nháp của trợ lý, nhận định hồ sơ, bản tin phụ huynh đều vào một hàng chờ với ba hành động giống nhau: nhận, sửa, bỏ. Giáo viên học một lần, dùng ở mọi chặng.
5. **Quyền theo quan hệ, không theo vai.** Giáo viên thấy học sinh vì đang dạy lớp đó. Phụ huynh thấy con vì quan hệ đã được trường xác minh. Cơ quan quản lý thấy trường vì có ủy quyền còn hiệu lực. Hết quan hệ thì hết quyền, không cần ai nhớ thu hồi.
6. **Bằng chứng đi kèm mọi con số.** Bất kỳ phần trăm, mức nắm hay cảnh báo nào cũng mở ra được danh sách bài làm và câu hỏi tạo nên nó. Con số không truy được nguồn thì không hiển thị.
7. **Công tắc an toàn theo năng lực.** Tắt được AI phía học sinh, AI chấm, trò chuyện nhóm một cách độc lập, theo trường, môn hoặc lớp. Khi tắt, luồng học cơ bản vẫn chạy.

### 0.4 Bản đồ chín chặng

| Chặng | Tên | Điều kiện mở (theo `PRODUCT-MASTER.md` D1) |
|---|---|---|
| 1 | MVP hình không gian | Đang làm, ngoài phạm vi tài liệu này |
| 2 | Trọn môn Hình học | MVP đạt tiêu chí B7 |
| 3 | Lớp học thật | Có giáo viên thật dùng lặp lại ở chặng 2 |
| 4 | Môn học thứ hai | Hình học giữ chân được học sinh |
| 5 | Đánh giá và trợ lý giáo viên | Có ít nhất một trường cam kết pilot |
| 6 | Hồ sơ học tập và phụ huynh | Chặng 5 ổn định; xác minh phụ huynh đã thử |
| 7 | Cộng tác và liên trường | Thiết kế bảo vệ trẻ em được duyệt |
| 8 | Vận hành nhà trường | Có trường yêu cầu và trả tiền |
| 9 | Quản trị giáo dục | Có ủy quyền pháp lý và đối tác quản lý |

---

## Chặng 2 — Trọn môn Hình học

### 1. Mục tiêu, giá trị và điều kiện mở
Hoàn tất môn Hình học: thêm hình phẳng với cùng mức tin cậy như hình không gian, và chuyển kho đề từ "Giáo viên mẫu" sang giáo viên thật. Giáo viên tự thêm đề, sửa và duyệt lời giải AI, trình chiếu trên lớp; học sinh có bài luyện được chấm ngay. Đây là chặng đầu tiên trả lời câu hỏi "giáo viên thật có dùng không".

**Điều kiện mở:** MVP đạt tiêu chí B7 (độ đúng hình và đáp số trên bộ đề chuẩn, không bài sai mang nhãn "Đã kiểm chứng", không lộ lời giải trước giờ mở, đủ kho đề đã kiểm chứng).

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Giáo viên Toán (nhóm nhỏ, vào bằng lời mời) | Web trên máy tính; máy tính nối máy chiếu khi trình chiếu | Thêm đề, sửa và duyệt lời giải, trình chiếu, soạn bài luyện |
| Học sinh THCS và THPT | iPad là chính, iPhone | Làm bài hình phẳng, làm bài luyện |
| Người soát nội dung (nội bộ) | Web | Soát đề của kho chung |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Hình phẳng dựng từ ràng buộc | Tam giác, tứ giác, đường tròn do engine dựng và kiểm chứng như hình không gian | Rất cao | Cao | Dạng đề phẳng đa dạng hơn hình khối | Làm |
| 2 | Thêm đề có AI soạn nháp | Dán chữ hoặc ảnh, AI dựng hình và lời giải, engine kiểm chứng, giáo viên duyệt | Rất cao | Trung bình | Đề lạ rơi vào "Cần xem lại" | Làm |
| 3 | Sửa lời giải theo từng bước | Sửa chữ, đổi thứ tự, thêm bước; mỗi lần lưu tạo phiên bản mới | Cao | Trung bình | Bước sửa tay làm mất liên kết với hình | Làm |
| 4 | Kiểm tra số cho từng bước | Mỗi khẳng định trung gian (vuông góc, bằng nhau, độ dài) được engine thử bằng số | Cao | Trung bình | Dễ bị hiểu nhầm là đã kiểm chứng lập luận | Làm |
| 5 | Trình chiếu | Toàn màn hình, chữ lớn, hiện từng bước, có nút che lời giải | Cao | Thấp | Thấp | Làm |
| 6 | Bài luyện điền đáp số | Engine chấm, nhận các dạng viết tương đương của cùng một đáp số | Cao | Trung bình | Cách nhập căn, phân số trên cảm ứng | Làm |
| 7 | Biến thể đề | Engine đổi số liệu của đề gốc, tự tính lại đáp số; mỗi học sinh nhận số liệu khác nhau | Cao | Trung bình | Số liệu sinh ra xấu hoặc hình suy biến | Làm (P1) |
| 8 | Thiết diện động | Kéo mặt phẳng, thiết diện cập nhật trực tiếp | Cao khi trình diễn | Cao | Thuật toán cắt đa diện | Sau |
| 9 | Dựng hình tự do kiểu thước và compa | Bảng vẽ trống cho học sinh tự dựng | Trung bình | Rất cao | Đối đầu trực diện công cụ dựng hình sẵn có | Bỏ |
| 10 | Kiểm chứng chứng minh hình thức | Máy xác nhận toàn bộ lập luận | Rất cao nếu làm được | Rất cao | Chưa khả thi ở mức sản phẩm | Bỏ |

**Phản biện.** Ý 9 hấp dẫn vì "trọn môn" nghe như phải có bảng vẽ tự do, nhưng lợi thế của sản phẩm là đi từ đề bài ra hình đúng, không phải công cụ vẽ. Ý 10 là thứ giáo viên muốn nhất; thay vào đó làm ý 4 và nói thật trên giao diện: máy chỉ kiểm tra phần tính toán, lập luận do giáo viên duyệt. Ý 7 là ý khác biệt nhất: chỉ sản phẩm có engine ràng buộc mới sinh được bài tương tự mà đáp số chắc chắn đúng.

### 4. Phạm vi đề xuất
- **P0:** hình phẳng (dạng phổ biến của THCS và THPT) có kiểm chứng; thêm đề; sửa và duyệt lời giải có phiên bản; nhãn hai trục; trình chiếu; bài luyện điền đáp số chấm tự động; kho đề có ngăn "Của tôi" và "Kho chung"; tài khoản giáo viên bằng lời mời.
- **P1:** biến thể đề; xuất ảnh và PDF; liên kết chia sẻ chỉ xem; câu trắc nghiệm trong bài luyện.
- **Ngoài phạm vi:** đăng ký giáo viên mở, nhiều lớp và giao theo bộ (chặng 3); nộp bài viết tay và rubric (chặng 5); dựng hình tự do; kiểm chứng chứng minh; bảng xếp hạng.

### 5. Luồng chính
**Luồng A — Giáo viên thêm đề và duyệt lời giải**
1. Giáo viên · web: mở Kho đề, bấm Thêm đề, dán chữ hoặc tải ảnh đề → hệ thống hiện đề đã chép lại để giáo viên sửa.
2. Giáo viên · web: bấm Dựng hình → màn chờ từng bước (đọc đề, dựng hình, giải, kiểm chứng), rồi hiện hình, lời giải nháp và nhãn hai trục.
3. Giáo viên · web: sửa từng bước, bấm Kiểm tra lại → engine chạy lại, bước nào sai số thì tô đỏ kèm lý do.
4. Giáo viên · web: bấm Duyệt và đưa vào kho → đề vào ngăn "Của tôi" với nhãn "Giáo viên đã duyệt".
- *Ngoại lệ:* ảnh đọc không rõ → yêu cầu giáo viên gõ lại phần thiếu, không tự đoán. Engine không dựng được → cho lưu với nhãn "Cần xem lại · hình do AI dựng"; muốn giao bài phải bấm xác nhận riêng. Đáp số AI lệch với engine → chặn nút Duyệt cho tới khi giáo viên chọn giữ đáp số nào.

**Luồng B — Trình chiếu trên lớp**
1. Giáo viên · web (nối máy chiếu): mở đề, bấm Trình chiếu → toàn màn hình, lời giải che sẵn.
2. Giáo viên · web: xoay hình, đo, thêm đường phụ → hình đổi ngay, chữ và nét đủ lớn để nhìn từ cuối lớp.
3. Giáo viên · web: bấm Hiện bước kế → hiện từng bước, hình tô màu theo bước.
- *Ngoại lệ:* mất mạng giữa giờ → hình và các bước đã tải vẫn dùng được, hỏi đáp AI tạm tắt kèm thông báo. Đề đang giao cho lớp và lời giải còn khóa → hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?".

**Luồng C — Học sinh làm bài luyện**
1. Học sinh · iPad: mở bài luyện → hình ở giữa, ô đáp số ở dưới, bàn phím toán (căn, phân số, π).
2. Học sinh · iPad: nhập đáp số, bấm Kiểm tra → đúng thì xác nhận và sang câu kế; sai thì gợi ý mức 1, không hiện đáp số.
3. Học sinh · iPad: hết số lần thử do giáo viên đặt → câu được đánh dấu "xem lại cùng thầy cô"; lời giải mở theo giờ giáo viên đặt.
- *Ngoại lệ:* đáp số đúng giá trị nhưng khác dạng viết → vẫn tính đúng, kèm dạng chuẩn để học sinh biết. Nhập sai cú pháp → báo chỗ sai, không trừ lượt thử.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C2-01 | Kho đề (mở rộng) | Giáo viên | Web | Tìm và quản lý đề | Ngăn Của tôi / Kho chung, lọc theo loại hình, nhãn hai trục | Trống (chưa có đề riêng), đang tải, không có kết quả lọc | Có: `mvp-01`, cần mở rộng |
| C2-02 | Thêm đề mới | Giáo viên | Web | Nhập đề và cho AI soạn nháp | Ô dán chữ, tải ảnh, đề đã chép lại, tiến trình bốn bước | Đang dựng, ảnh không đọc được, engine không dựng được | Có: bản mẫu (Kho đề → Thêm đề) |
| C2-03 | Soát và duyệt lời giải | Giáo viên | Web | Sửa, kiểm tra lại, duyệt | Hình, danh sách bước sửa được, kết quả kiểm tra từng bước, lịch sử phiên bản | Chưa kiểm chứng, lệch đáp số (chặn duyệt), đã duyệt, đã giao (chỉ tạo phiên bản mới) | Chưa |
| C2-04 | Trình chiếu | Giáo viên | Web, toàn màn hình | Giảng trên lớp | Hình lớn, thanh công cụ gọn, nút hiện bước kế, nút che | Lời giải che, mất mạng, đề đang khóa lời giải | Chưa |
| C2-05 | Làm bài hình phẳng | Học sinh | iPad, iPhone | Thao tác hình 2D, xin gợi ý | Hình 2D, công cụ đo, bảng gợi ý, khóa lời giải | Lời giải khóa, hình chưa kiểm chứng, đang tải | Có: `shots/16` |
| C2-06 | Bài luyện | Học sinh | iPad, iPhone | Trả lời và được chấm ngay | Hình, ô đáp số, bàn phím toán, số lượt còn lại, tiến độ | Đúng, sai có gợi ý, hết lượt, sai cú pháp, mất mạng khi gửi | Chưa |
| C2-07 | Kết quả bài luyện | Học sinh | iPad, iPhone | Xem lại sau khi làm | Câu đúng, câu cần xem lại, lời giải khóa hay đã mở | Chưa làm xong, lời giải khóa | Chưa |
| C2-08 | Soạn bài luyện | Giáo viên | Web | Chọn đề, đặt lượt thử và giờ mở | Danh sách đề, kiểu đáp số, sai số cho phép, xem trước như học sinh | Đề chưa có đáp số máy chấm được | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Lời giải đã giao thì bất biến; sửa là tạo phiên bản mới và giáo viên chọn có áp cho bài đang giao hay không.
- Chỉ đề có lời giải "Giáo viên đã duyệt" hoặc thuộc Kho chung đã soát mới giao được không cần xác nhận thêm.
- Nhãn "Đã kiểm chứng" chỉ do engine gắn. Giáo viên không tự tay gắn được, kể cả khi đã duyệt.
- Bài luyện chỉ chấm đáp số, không phải điểm chính thức và không được gọi là "điểm" trên giao diện.
- Đề trong ngăn "Của tôi" chỉ chủ sở hữu thấy. Đưa vào Kho chung cần người soát nội dung duyệt.
- Học sinh không thấy đáp số của bài luyện trước giờ mở, kể cả sau khi hết lượt thử.

### 8. Dữ liệu mới
| Thực thể hoặc trường | Nội dung | Trạng thái |
|---|---|---|
| `Problem.kind` | Không gian hoặc phẳng | — |
| `SceneSpec` cho hình phẳng | Ràng buộc và tọa độ 2D | — |
| `Solution.review_status`, `reviewed_by` | Trục người của nhãn | AI soạn → đang sửa → giáo viên đã duyệt → bị thay thế |
| `StepCheck` | Kết quả kiểm tra số của từng bước | Đạt, không đạt, không kiểm được |
| `PracticeSet`, `PracticeItem` | Bài luyện, kiểu đáp số, sai số, số lượt thử | Nháp → đã giao → đã đóng |
| `Attempt` | Đáp số học sinh nhập, lượt, kết quả | Đúng, sai, sai cú pháp |
| `ProblemVariant` (P1) | Bộ số liệu sinh từ đề gốc và đáp số tính lại | — |
| `TeacherInvite` | Lời mời vào dùng thử | Đã gửi → đã nhận → hết hạn |

### 9. Phụ thuộc
- Từ chặng 1: `geometry-engine`, `SceneSpec`, `VerificationReport`, thang gợi ý, `release_at`, khóa ở máy chủ, bộ đề chuẩn và bộ câu thử moi đáp án.
- Nền móng cần thêm: bộ đề chuẩn cho hình phẳng; bàn phím toán trên iPad và iPhone; người có chuyên môn Toán soát Kho chung.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Hình phẳng đa dạng, engine phủ không đủ | Chốt danh sách dạng hỗ trợ theo bộ đề chuẩn; dạng ngoài danh sách đi nhánh "Cần xem lại" |
| Giáo viên bấm duyệt mà không đọc | Kiểm tra số từng bước tô đỏ chỗ nghi ngờ; chặn duyệt khi lệch đáp số |
| "Đã kiểm chứng" bị hiểu là đúng cả lập luận | Dòng chú thích cố định cạnh nhãn: "Máy đã kiểm tra phần tính toán" |
| Chấm đáp số sai do dạng viết | Bộ ca thử các dạng tương đương; nút "Em nghĩ đáp số này đúng" gửi về giáo viên |

**Chỉ số (cách đo):** tỉ lệ hình phẳng thỏa ràng buộc và đáp số đúng trên bộ đề chuẩn; số bài sai mang nhãn "Đã kiểm chứng"; số đề giáo viên tự thêm và tỉ lệ được duyệt; tỉ lệ bước lời giải bị giáo viên sửa; số phiên trình chiếu trên mỗi giáo viên; tỉ lệ bài luyện được làm hết; số lượt báo "chấm sai" được xác nhận là đúng.

### 11. Câu hỏi mở
1. Giáo viên chặng 2 vào bằng lời mời, chưa cần xác nhận danh tính: có chấp nhận không, hay kéo đăng ký chính thức của chặng 3 lên trước?
2. Ai soát đề trước khi vào Kho chung, và có trả công không?
3. Đề có nhãn "Cần xem lại" mà giáo viên đã duyệt: có cho giao không? Đề xuất: cho giao, hiện rõ nhãn với học sinh.
4. Bài luyện có hiển thị con số tổng kết cho học sinh không, hay chỉ "đúng / cần xem lại"?

---

## Chặng 3 — Lớp học thật

### 1. Mục tiêu, giá trị và điều kiện mở
Thay tài khoản mẫu bằng tài khoản chính thức: giáo viên đăng ký, mở nhiều lớp, giao bài theo bộ và xem kết quả tổng hợp theo lớp. Giá trị nằm ở chỗ giáo viên dùng sản phẩm cho lớp của chính mình qua nhiều lần giao bài, không phải một lần thử.

**Điều kiện mở:** có giáo viên thật dùng lặp lại ở chặng 2 (tự thêm đề, trình chiếu hoặc giao bài luyện nhiều lần).

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Giáo viên | Web trên máy tính; điện thoại để kiểm tra nhanh | Đăng ký, tạo lớp, giao bộ bài, xem kết quả lớp |
| Học sinh | iPad, iPhone | Vào lớp, làm bài theo bộ, nhận nhắc việc |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Tài khoản giáo viên có xác nhận | Đăng ký, sau đó được xác nhận bằng mã của trường hoặc người giới thiệu | Cao | Trung bình | Người ngoài giả làm giáo viên để xem lời giải | Làm |
| 2 | Tài khoản học sinh không cần email | Tên đăng nhập và mật khẩu; giáo viên cấp lại được | Cao | Thấp | Học sinh quên mật khẩu thường xuyên | Làm |
| 3 | Vào lớp bằng mã, giáo viên duyệt | Học sinh nhập mã, chờ giáo viên chấp nhận | Cao | Thấp | Mã lộ ra ngoài lớp | Làm |
| 4 | Bộ bài | Nhiều đề trong một lần giao, có hạn làm và lịch mở lời giải chung hoặc riêng từng bài | Cao | Trung bình | Thiết lập rối | Làm |
| 5 | Bảng lớp × bài | Mỗi ô là trạng thái có bằng chứng: chưa mở, đang làm, xong, số gợi ý đã dùng | Rất cao | Trung bình | Bị đọc thành bảng xếp hạng | Làm |
| 6 | "Cả lớp vướng ở đâu" | Gom gợi ý và câu hỏi theo từng bước của lời giải, chỉ ra bước nhiều em dừng nhất | Rất cao | Trung bình | Gom sai khi ít dữ liệu | Làm |
| 7 | Giáo viên trên điện thoại | Xem ai chưa làm, mở lời giải ngay, đổi giờ | Cao | Thấp | Nhồi quá nhiều việc vào màn nhỏ | Làm |
| 8 | Nhắc việc cho học sinh | Bài mới, sắp hết hạn, lời giải đã mở | Trung bình | Trung bình | Cách gửi thông báo trên iPad và iPhone cần xác minh | Làm (P1) |
| 9 | Đăng nhập học sinh bằng mạng xã hội | Vào nhanh bằng tài khoản có sẵn | Trung bình | Thấp | Kéo dữ liệu trẻ em sang bên thứ ba | Bỏ |
| 10 | Bảng xếp hạng lớp | Thứ hạng theo kết quả | Thấp | Thấp | Trái nguyên tắc không gắn nhãn | Bỏ |

**Phản biện.** Ý 9 giảm ma sát rõ rệt nhưng đổi lại là dữ liệu trẻ em; để dành cho lúc kết nối định danh của trường (chặng 8). Ý 6 là thứ nên dồn sức: nó biến nhật ký tương tác thành câu trả lời cho câu hỏi giáo viên thật sự có trước mỗi tiết học: "hôm nay chữa phần nào".

### 4. Phạm vi đề xuất
- **P0:** đăng ký và xác nhận giáo viên; tài khoản học sinh; nhiều lớp; vào lớp bằng mã có duyệt; bộ bài với hạn làm và lịch mở lời giải; bảng lớp × bài; "cả lớp vướng ở đâu"; tổng quan giáo viên trên web và điện thoại; đồng thuận và thông báo riêng tư tối thiểu.
- **P1:** chữa bài từ kết quả; nhắc việc; xuất kết quả lớp; lưu trữ lớp khi hết năm học.
- **Ngoài phạm vi:** trường và phân công (chặng 8); phụ huynh (chặng 6); điểm chính thức và rubric (chặng 5); nhiều giáo viên một lớp.

### 5. Luồng chính
**Luồng A — Giáo viên đăng ký, mở lớp, học sinh vào lớp**
1. Giáo viên · web: đăng ký, nhập mã xác nhận → tài khoản ở trạng thái "đã xác nhận"; chưa có mã thì "chờ xác nhận" và chỉ dùng được kho đề, chưa giao được bài.
2. Giáo viên · web: tạo lớp (tên, cấp, năm học) → hệ thống sinh mã lớp và trang hướng dẫn in được.
3. Học sinh · iPad hoặc iPhone: tạo tài khoản, nhập mã lớp → trạng thái "chờ thầy cô duyệt".
4. Giáo viên · web hoặc điện thoại: duyệt danh sách chờ → học sinh thấy bài của lớp.
- *Ngoại lệ:* mã sai hoặc đã khóa → báo rõ và gợi ý hỏi lại giáo viên. Trùng tên trong lớp → hệ thống yêu cầu thêm ký hiệu phân biệt. Học sinh quên mật khẩu → giáo viên cấp mã đặt lại dùng một lần.

**Luồng B — Giao bộ bài**
1. Giáo viên · web: chọn nhiều đề từ kho, bấm Tạo bộ bài → màn soạn với thứ tự kéo thả.
2. Giáo viên · web: chọn lớp, hạn làm, mức trợ giúp, lịch mở lời giải (cả bộ hoặc từng bài) → xem trước như học sinh.
3. Giáo viên · web: bấm Giao → học sinh thấy bộ bài mới; tổng quan giáo viên thêm một dòng.
- *Ngoại lệ:* giờ mở lời giải sớm hơn hạn làm → cảnh báo, vẫn cho tiếp tục nếu giáo viên xác nhận. Bộ có đề "Cần xem lại" → liệt kê các đề đó trước khi giao.

**Luồng C — Xem kết quả và quyết định chữa bài**
1. Giáo viên · điện thoại: mở Tổng quan → thấy số em chưa mở bài và trạng thái khóa lời giải.
2. Giáo viên · điện thoại: bấm Mở ngay hoặc Đổi giờ → thẻ đổi trạng thái, học sinh nhận thông báo trong ứng dụng.
3. Giáo viên · web: mở Kết quả lớp → bảng lớp × bài; bấm một ô ra gợi ý và câu hỏi của em đó ở bài đó.
4. Giáo viên · web: mở "Cả lớp vướng ở đâu" → danh sách bước theo số em dừng lại, kèm câu hỏi tiêu biểu.
- *Ngoại lệ:* dưới một số lượng bài làm tối thiểu → không hiện nhận định gộp, chỉ hiện dữ liệu thô. Học sinh rời lớp → dữ liệu cũ vẫn ở kết quả lớp, đánh dấu "đã rời".

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C3-01 | Đăng ký và xác nhận giáo viên | Giáo viên | Web | Tạo tài khoản chính thức | Thông tin tối thiểu, mã xác nhận, điều khoản | Chờ xác nhận, mã sai, bị từ chối | Chưa |
| C3-02 | Lớp học | Giáo viên | Web | Quản lý lớp và học sinh | Thẻ lớp, mã lớp, danh sách chờ duyệt, cấp lại mật khẩu | Chưa có lớp, có em chờ duyệt, lớp đã lưu trữ | Có: `shots/23` |
| C3-03 | Soạn bộ bài | Giáo viên | Web | Giao nhiều đề một lần | Danh sách đề, hạn làm, mức trợ giúp, lịch mở lời giải, xem trước | Chưa chọn lớp, xung đột giờ, có đề cần xem lại | Chưa (mở rộng `mvp-02`) |
| C3-04 | Kết quả lớp | Giáo viên | Web | Nhìn cả lớp trên cả bộ bài | Bảng lớp × bài, bấm ô xem bằng chứng, thẻ "Cả lớp vướng ở đâu" | Chưa ai làm, chưa đủ dữ liệu để gộp, đang tải | Chưa (mở rộng `mvp-03`) |
| C3-05 | Tổng quan giáo viên | Giáo viên | Web, điện thoại | Việc cần làm hôm nay | Bài đang giao, lời giải đang khóa, em chờ duyệt | Chưa có lớp, không có việc | Có: `shots/22`, `dev-iphone-gv-tong-quan` |
| C3-06 | Theo dõi nhanh | Giáo viên | Điện thoại | Liếc và quyết nhanh | Danh sách em chưa làm, nút Mở ngay, Đổi giờ | Đã mở, mất mạng | Có: `dev-iphone-gv-theo-doi` |
| C3-07 | Vào lớp | Học sinh | iPad, iPhone | Tạo tài khoản và nhập mã lớp | Ô mã, trạng thái chờ duyệt | Mã sai, chờ duyệt, bị từ chối | Chưa |
| C3-08 | Bài của em (theo bộ) | Học sinh | iPad, iPhone | Thấy việc cần làm theo hạn | Bộ bài, hạn, tiến độ, giờ mở lời giải | Chưa có bài, quá hạn, lời giải đã mở | Có: `mvp-04`, cần mở rộng |

### 7. Quy tắc nghiệp vụ và quyền
- Giáo viên chỉ thấy học sinh của lớp mình tạo. Học sinh chỉ thấy bài của lớp mình đang học và không thấy kết quả của bạn.
- Giáo viên chưa xác nhận không giao được bài và không xem được lời giải của Kho chung.
- Tài khoản học sinh chỉ thu tên hiển thị, tên đăng nhập, lớp. Không thu số điện thoại, ảnh hay ngày sinh ở chặng này.
- Hạn làm không khóa bài; quá hạn chỉ là nhãn. Việc khóa hay mở thuộc về lịch mở lời giải.
- Mọi lần mở sớm, đổi giờ, cấp lại mật khẩu đều được ghi lại kèm người thực hiện.
- Nhận định gộp chỉ hiện khi đủ số bài làm tối thiểu (ngưỡng cần chốt), để tránh suy diễn từ vài em.

### 8. Dữ liệu mới
| Thực thể hoặc trường | Nội dung | Trạng thái |
|---|---|---|
| `AuthIdentity` | Thông tin đăng nhập tách khỏi `User` | — |
| `TeacherVerification` | Cách xác nhận và người xác nhận | Chờ → đã xác nhận → từ chối |
| `Class.academic_year`, `Class.status` | Năm học, vòng đời lớp | Đang dạy → đã lưu trữ |
| `ClassMembership` | Quan hệ học sinh–lớp | Chờ duyệt → đang học → đã rời |
| `AssignmentSet`, `AssignmentItem` | Bộ bài và từng đề trong bộ; `due_at`, `release_at` theo bộ hoặc theo bài | Nháp → đã giao → đã đóng → lưu trữ |
| `Notification` | Nhắc việc trong ứng dụng | Chưa đọc → đã đọc |
| `ConsentRecord` | Bằng chứng đã đọc thông báo riêng tư | — |
| `AuditEvent` | Hành động nhạy cảm | — |

### 9. Phụ thuộc
- Từ chặng 2: kho đề có đề do giáo viên duyệt, bài luyện, trình chiếu.
- Nền móng cần có: tư vấn pháp lý về tài khoản trẻ em (cần xác minh quy định áp dụng); văn bản thông báo riêng tư; quy trình hỗ trợ khi mất tài khoản; `owner_id` và `org_id` đã đặt sẵn từ MVP.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Người ngoài giả làm giáo viên để lấy lời giải | Xác nhận trước khi mở lời giải Kho chung và quyền giao bài |
| Bảng lớp × bài bị dùng để xếp hạng | Không có cột tổng, không sắp theo "tốt nhất"; mỗi ô là trạng thái |
| Học sinh quên mật khẩu, bỏ dùng | Giáo viên cấp lại ngay trong màn Lớp học |
| Thu dữ liệu trẻ em vượt mức cần | Danh sách trường dữ liệu được phép thu, duyệt trước khi làm |

**Chỉ số (cách đo):** số giáo viên giao từ hai bộ bài trở lên cho cùng một lớp; tỉ lệ học sinh trong lớp mở bài; tỉ lệ giáo viên mở màn Kết quả lớp sau khi giao; số lần bấm từ "Cả lớp vướng ở đâu" sang Trình chiếu; số yêu cầu cấp lại mật khẩu trên mỗi lớp; số sự cố truy cập sai dữ liệu.

### 11. Câu hỏi mở
1. Giáo viên được xác nhận bằng cách nào: mã của trường, người giới thiệu, hay duyệt tay?
2. Học sinh có được tự đăng ký không có lớp (tự học) với tài khoản chính thức không?
3. Thông báo cho học sinh đi qua ứng dụng cài trên máy hay web? Lựa chọn này ảnh hưởng cách phân phối; cần xác minh giới hạn thông báo của web trên iPad và iPhone.
4. Ngưỡng số bài làm tối thiểu để hiện nhận định gộp là bao nhiêu?

---

## Chặng 4 — Môn học thứ hai

### 1. Mục tiêu, giá trị và điều kiện mở
Chứng minh khung môn học dùng lại được: môn mới có cùng cách học "hình đúng, gợi ý trước, lời giải theo giờ giáo viên" mà không phải viết lại lớp học, giao bài hay theo dõi. Giá trị kinh doanh là sản phẩm thôi là "công cụ hình học" và trở thành nền tảng nhiều môn.

**Điều kiện mở:** Hình học giữ chân được học sinh (học sinh quay lại làm bài qua nhiều lần giao, đo ở chặng 3).

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Giáo viên Toán đang dùng Hình học | Web | Thêm đề môn mới, giao bài cho lớp sẵn có |
| Học sinh đang dùng Hình học | iPad, iPhone | Học môn mới trong cùng ứng dụng |
| Nhóm sản phẩm (nội bộ) | Web | Kiểm tra môn mới đạt chuẩn trước khi mở |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Môn hai là Hàm số và đồ thị | Cùng giáo viên, cùng học sinh, kiểm chứng được bằng số | Rất cao | Trung bình | Bị so với công cụ vẽ đồ thị sẵn có | Làm |
| 2 | Bộ hợp chuẩn môn học | Môn mới phải qua bộ đề chuẩn, bộ thử moi đáp án và thang gợi ý mới được mở | Rất cao | Trung bình | Bị bỏ qua khi vội | Làm |
| 3 | "Dự đoán trước khi kéo" | Học sinh chọn dự đoán, rồi kéo thanh trượt để tự kiểm | Cao | Thấp | Cần soạn câu dự đoán tốt | Làm |
| 4 | Lời giải gắn với đồ thị | Bấm bước nào, đồ thị đánh dấu điểm, khoảng, tiệm cận của bước đó | Cao | Trung bình | AI tham chiếu sai đối tượng | Làm |
| 5 | Kiểm chứng biểu thức bằng thử số | So đáp số dạng biểu thức bằng cách thay nhiều giá trị | Cao | Trung bình | Sót trường hợp biên | Làm |
| 6 | Một lớp nhiều môn | Giáo viên bật môn cho lớp sẵn có, không tạo lớp mới | Cao | Thấp | Thấp | Làm |
| 7 | Liên kết chéo môn | Bài Oxyz dẫn sang phương trình, khái niệm dùng chung | Trung bình | Trung bình | Cần danh mục khái niệm chín hơn | Sau |
| 8 | Vật lý cơ học làm môn hai | Mô phỏng chuyển động | Cao | Cao | Khác giáo viên, khác cách kiểm chứng | Sau |
| 9 | Máy tính đồ thị tự do | Nhập hàm tùy ý, vẽ tự do | Trung bình | Trung bình | Lặp lại thứ đã có sẵn miễn phí | Bỏ |
| 10 | Mở khung cho bên ngoài viết môn | Đối tác tự làm module | Cao về lâu dài | Rất cao | Chưa đủ chín, khó giữ chất lượng | Bỏ |

**Phản biện.** Vật lý (ý 8) trình diễn ấn tượng hơn, nhưng nó đổi cùng lúc cả môn, cả giáo viên lẫn cách kiểm chứng; nếu thất bại sẽ không biết lỗi do khung hay do môn. Hàm số và đồ thị chỉ đổi một biến. Ý 9 mắc cùng bẫy với "dựng hình tự do" ở chặng 2.

### 4. Phạm vi đề xuất
- **P0:** môn Hàm số và đồ thị (khảo sát, đọc đồ thị, tương giao, tham số) có đủ dựng, kiểm chứng, gợi ý, lời giải gắn đồ thị, bài luyện; chọn môn; kho đề theo môn; bật môn cho lớp sẵn có; bộ hợp chuẩn môn học.
- **P1:** "dự đoán trước khi kéo" cho cả Hình học; kết quả lớp lọc theo môn; đổi tên nền tảng nếu chủ sản phẩm chốt.
- **Ngoài phạm vi:** môn ngoài Toán; máy tính đồ thị tự do; cho bên ngoài viết môn; liên kết chéo môn.

### 5. Luồng chính
**Luồng A — Học sinh làm bài hàm số**
1. Học sinh · iPad: mở bài được giao → đồ thị ở giữa, thanh trượt tham số, bảng gợi ý bên phải.
2. Học sinh · iPad: chọn một dự đoán → hệ thống ghi nhận, chưa nói đúng sai.
3. Học sinh · iPad: kéo thanh trượt → đồ thị đổi ngay; hệ thống đối chiếu với dự đoán và hỏi "em thấy gì khác?".
4. Học sinh · iPad: xin gợi ý, nhập đáp số → chấm như bài luyện; lời giải khóa tới giờ mở.
- *Ngoại lệ:* trên iPhone thanh trượt nằm dưới đồ thị, bảng gợi ý trượt lên từ dưới. Hàm ngoài phạm vi hỗ trợ → nhãn "Cần xem lại", không có thanh trượt.

**Luồng B — Giáo viên bật môn và giao bài**
1. Giáo viên · web: mở Lớp học, bật môn Hàm số cho lớp → lớp có thêm một ngăn môn.
2. Giáo viên · web: mở Kho đề, chọn môn → cùng bộ lọc và nhãn hai trục như Hình học.
3. Giáo viên · web: thêm đề mới hoặc chọn đề, giao theo bộ → luồng giống chặng 3.
- *Ngoại lệ:* trộn đề hai môn trong một bộ → cho phép, hiện biểu tượng môn trên từng đề.

**Luồng C — Mở một môn mới (nội bộ)**
1. Nhóm sản phẩm · web: chạy bộ hợp chuẩn cho môn → bảng kết quả theo từng hạng mục.
2. Nhóm sản phẩm · web: hạng mục nào chưa đạt thì môn ở trạng thái "thử nghiệm", chỉ lớp được mời mới thấy.
3. Nhóm sản phẩm · web: đủ hạng mục → mở cho mọi lớp.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C4-01 | Chọn môn | Học sinh | iPad, iPhone | Chuyển giữa các môn | Thẻ môn, việc cần làm theo môn | Chỉ có một môn (ẩn màn), môn đang thử nghiệm | Một phần: mục "Môn khác" trong bản mẫu |
| C4-02 | Làm bài hàm số | Học sinh | iPad, iPhone | Khảo sát và trả lời | Đồ thị, thanh trượt, dự đoán, gợi ý, ô đáp số | Lời giải khóa, chưa kiểm chứng, hàm ngoài phạm vi | Có: `shots/17` |
| C4-03 | Kho đề theo môn | Giáo viên | Web | Tìm đề theo môn | Bộ chọn môn, bộ lọc theo dạng bài của môn | Môn chưa có đề riêng | Chưa (mở rộng C2-01) |
| C4-04 | Thêm đề hàm số | Giáo viên | Web | Nhập đề và duyệt lời giải | Ô đề, đồ thị xem trước, miền xác định, kiểm tra từng bước | Hàm ngoài phạm vi, lệch đáp số | Chưa (mở rộng C2-02, C2-03) |
| C4-05 | Thiết lập môn của lớp | Giáo viên | Web | Bật hoặc tắt môn cho lớp | Danh sách môn, trạng thái | Môn đang thử nghiệm | Chưa |
| C4-06 | Bảng hợp chuẩn môn học | Nhóm sản phẩm | Web | Quyết định mở môn | Hạng mục, kết quả, lịch sử lần chạy | Chưa chạy, chưa đạt, đạt | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Môn mới không được có luật riêng về lời giải: lịch mở, khóa ở máy chủ và kiểm tra hỏi đáp dùng chung cổng công bố.
- Môn chưa qua bộ hợp chuẩn không được mang nhãn "Đã kiểm chứng" trên bất kỳ đề nào.
- Quyền không đổi theo môn: giáo viên thấy lớp mình, học sinh thấy bài mình.
- Kết quả của hai môn không cộng gộp thành một con số chung.

### 8. Dữ liệu mới
| Thực thể hoặc trường | Nội dung | Trạng thái |
|---|---|---|
| `Subject` | Môn và phiên bản module | Thử nghiệm → đang mở → tạm tắt |
| `Problem.subject_id` | Đề thuộc môn nào | — |
| `SceneSpec` cho đồ thị | Hàm, miền, tham số, điểm đặc biệt | — |
| `ClassSubject` | Môn đang bật cho lớp | Bật, tắt |
| `Concept` | Danh mục khái niệm theo môn | — |
| `ConformanceRun` | Kết quả một lần chạy bộ hợp chuẩn | Đạt, chưa đạt |
| `Prediction` | Dự đoán của học sinh trước khi thao tác | — |

### 9. Phụ thuộc
- Từ chặng 2 và 3: giao diện module đủ `parse`, `solve`, `verify`, `render`, `hint`, `grade`; bài luyện; bộ bài; kết quả lớp.
- Nền móng cần có: bộ đề chuẩn và bộ câu thử moi đáp án cho môn mới; người có chuyên môn soát đề; rà lại những chỗ Hình học đã "đi tắt" khỏi khung.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Khung môn học hóa ra chỉ vừa với Hình học | Liệt kê trước mọi chỗ viết riêng cho hình; coi số chỗ phải sửa là một kết quả đo |
| Môn hai làm loãng chất lượng Hình học | Điều kiện mở dựa trên giữ chân; bộ hợp chuẩn chạy lại cho cả hai môn |
| Học sinh coi đây là công cụ vẽ đồ thị | Mọi bài bắt đầu từ đề và câu hỏi, không có bảng vẽ trống |

**Chỉ số (cách đo):** tỉ lệ thành phần dùng lại nguyên vẹn so với phải sửa; kết quả bộ đề chuẩn của môn mới; tỉ lệ lớp Hình học bật môn hai; tỉ lệ học sinh dùng cả hai môn; so sánh mức quay lại giữa hai môn.

### 11. Câu hỏi mở
1. Chốt môn hai là Hàm số và đồ thị, hay chủ sản phẩm có ưu tiên khác?
2. Đổi tên nền tảng ở chặng này hay giữ "Hình Học Cùng AI" kèm tên môn?
3. "Giữ chân được học sinh" đo bằng chỉ số nào và ngưỡng nào?
4. Môn hai phục vụ cả THCS và THPT ngay từ đầu, hay bắt đầu ở một cấp?

---

## Chặng 5 — Đánh giá và trợ lý giáo viên

### 1. Mục tiêu, giá trị và điều kiện mở
Khép vòng dạy học: học sinh nộp bài viết tay, AI đề xuất điểm theo rubric kèm bằng chứng, giáo viên duyệt rồi mới thành điểm. Cùng lúc, giáo viên có kho tri thức riêng để AI phía học sinh chỉ trả lời từ tài liệu đã duyệt, và có trợ lý soạn bộ đề, giáo án, thông báo. Đây là chặng có giá trị lớn nhất với giáo viên và cũng rủi ro nhất về độ đúng.

**Điều kiện mở:** có ít nhất một trường cam kết pilot. Trước khi bật AI chấm cho lớp nào, AI phải chạy song song với giáo viên trên bài thật của lớp đó.

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Giáo viên | Web trên máy tính; điện thoại để xem số bài chờ | Soạn rubric, chấm và duyệt, quản lý kho tri thức, dùng trợ lý |
| Học sinh | iPhone để chụp bài, iPad để làm và xem kết quả | Nộp bài viết tay, xem điểm và nhận xét đã duyệt |
| Quản trị trường (mức tối thiểu) | Web | Bật hoặc tắt AI chấm và AI phía học sinh cho trường |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Nộp bài bằng ảnh viết tay | Chụp nhiều trang, giữ ảnh gốc, bản đọc chữ là bản phái sinh sửa được | Rất cao | Trung bình | Ảnh mờ, thiếu trang | Làm |
| 2 | Rubric có phiên bản và mẫu sẵn | Tiêu chí, mức điểm, hướng dẫn điểm thành phần; mẫu cho các dạng bài hình | Rất cao | Trung bình | Giáo viên ngại soạn rubric | Làm |
| 3 | AI đề xuất điểm theo tiêu chí | Kèm lý do, vùng ảnh làm căn cứ, độ tin cậy | Rất cao | Rất cao | Đọc sai chữ viết tay, chấm sai | Làm |
| 4 | Chạy song song và quyền từ chối chấm | AI chấm ngầm để so với giáo viên trước khi bật; khi ảnh kém hoặc độ tin cậy thấp thì AI nói rõ lý do và chuyển chấm tay | Rất cao | Trung bình | Tốn công giáo viên lúc đầu; từ chối quá nhiều làm mất giá trị | Làm |
| 5 | Engine làm tín hiệu thứ hai | Với bài có đáp số, engine kiểm tra đáp số cuối trong bài làm, độc lập với AI | Cao | Trung bình | Chỉ áp dụng cho bài tính toán | Làm |
| 6 | Kho tri thức và kiểm soát đáp án đầy đủ | Tài liệu có phiên bản gắn cho lớp hoặc bài, AI học sinh trả lời kèm nguồn; mức trợ giúp theo bài, kiểm tra đầu ra, nhật ký từng quyết định | Rất cao | Cao | Tài liệu ít thì AI từ chối nhiều; chặn nhầm câu hỏi chính đáng | Làm |
| 7 | Trợ lý soạn bộ đề, thông báo, giáo án | Soạn nháp từ tài liệu của giáo viên; đề qua engine và có nhãn; chỉ dùng khi giáo viên duyệt | Cao | Trung bình | Đề nháp không đều, giáo án chung chung | Làm bộ đề và thông báo; giáo án P1 |
| 8 | Duyệt theo cụm lỗi | Gom bài cùng một lỗi, duyệt nhận xét một lần cho cả cụm | Cao | Trung bình | Duyệt ẩu hàng loạt | Sau |
| 9 | Tự duyệt khi AI tin cậy cao | Bỏ bước giáo viên với bài "chắc chắn" | Cao về tốc độ | Thấp | Phá nguyên tắc giáo viên quyết định cuối | Bỏ |
| 10 | Học sinh xem "AI chấm thử" trước khi nộp | Tự kiểm tra bài mình | Cao | Thấp | Lộ rubric, biến AI thành người chấm | Bỏ |

**Phản biện.** Ý 9 sẽ được đề nghị nhiều lần vì tiết kiệm công rõ nhất; phải từ chối dứt khoát, và thay bằng cách làm cho việc duyệt nhanh hơn (phím tắt, nhận cả bài khi không có cờ). Ý 8 chỉ nên làm cho **nhận xét**, không bao giờ cho điểm. Ý 5 là ý khác biệt: không sản phẩm chấm bài nào có sẵn một máy tính hình học để đối chiếu với AI.

### 4. Phạm vi đề xuất
- **P0:** tổ chức cấp trường tối thiểu và cách ly dữ liệu giữa các trường; bài nộp viết tay; rubric có phiên bản; AI đề xuất điểm có bằng chứng; chế độ chạy song song; AI từ chối chấm; giáo viên nhận, sửa, bỏ từng tiêu chí rồi duyệt; lịch công bố điểm và lời giải; kho tri thức; kiểm soát đáp án đầy đủ; trợ lý soạn bộ đề và thông báo; công tắc tắt AI theo trường, môn.
- **P1:** engine làm tín hiệu thứ hai; phúc khảo; trợ lý giáo án; tổng hợp lỗi phổ biến của lớp theo tiêu chí; duyệt nhận xét theo cụm.
- **Ngoài phạm vi:** tự duyệt điểm; thi có giám sát; sổ điểm chính thức của trường; phụ huynh xem điểm (chặng 6).

### 5. Luồng chính
**Luồng A — Học sinh nộp bài viết tay**
1. Học sinh · iPhone: mở bài, bấm Nộp bài, chụp từng trang → hệ thống kiểm tra độ rõ, tự cắt mép, đánh số trang.
2. Học sinh · iPhone: xếp lại thứ tự, bấm Nộp → trạng thái "Đã nộp", có giờ nộp; ảnh gốc được giữ nguyên.
3. Học sinh · iPad hoặc iPhone: xem trạng thái → "Đang chờ thầy cô chấm". Không hiện gì về đề xuất của AI.
- *Ngoại lệ:* ảnh mờ → yêu cầu chụp lại ngay lúc đó. Mất mạng khi nộp → giữ bản nháp trên máy, gửi lại không tạo bài trùng. Quá hạn → vẫn nộp được, có nhãn "nộp muộn", tùy thiết lập của giáo viên.

**Luồng B — AI đề xuất, giáo viên duyệt**
1. Hệ thống: đọc chữ, chấm theo từng tiêu chí, kiểm tra điểm bằng quy tắc cứng (đúng thang, đúng tổng) → lưu đề xuất, hoặc ghi "AI không chấm" kèm lý do.
2. Giáo viên · web: mở Hàng chờ chấm → danh sách bài, bài có cờ (độ tin cậy thấp, AI và engine lệch nhau) xếp trước.
3. Giáo viên · web: mở một bài → ảnh gốc bên trái, tiêu chí và đề xuất bên phải; bấm một tiêu chí thì vùng ảnh liên quan được khoanh.
4. Giáo viên · web: nhận, sửa hoặc bỏ từng tiêu chí, sửa nhận xét, bấm Duyệt → thành điểm chính thức, ghi lại giá trị trước và sau.
5. Giáo viên · web: bấm Trả bài hoặc để theo lịch công bố → học sinh thấy điểm, nhận xét và lời giải.
- *Ngoại lệ:* chế độ chạy song song → giáo viên chấm tay trước, sau đó mới thấy đề xuất của AI. Bản đọc chữ sai → giáo viên sửa bản đọc và yêu cầu chấm lại; ảnh gốc không đổi. Rubric bị sửa sau khi giao → tạo phiên bản mới, bài đã chấm giữ phiên bản cũ.

**Luồng C — Học sinh hỏi AI trong lúc làm bài**
1. Học sinh · iPad: hỏi trong tab Hỏi đáp → máy chủ kiểm tra lớp, bài, mức trợ giúp và lịch công bố.
2. Hệ thống: lấy tài liệu đã duyệt gắn với bài, soạn câu trả lời, kiểm tra lộ đáp án → trả gợi ý kèm nguồn.
3. Giáo viên · web: xem lịch sử câu hỏi của bài → thấy câu hỏi, câu trả lời, quyết định cho phép hay chặn.
- *Ngoại lệ:* không có tài liệu phù hợp → trả lời "Thầy cô chưa cung cấp tài liệu cho phần này", gợi ý hỏi giáo viên. Câu trả lời bị nghi lộ đáp án → thay bằng gợi ý an toàn, ghi lại cho giáo viên. Trường tắt AI phía học sinh → tab Hỏi đáp báo đang tắt, phần còn lại vẫn chạy.

**Luồng D — Trợ lý soạn bộ đề**
1. Giáo viên · web: chọn việc "Soạn bộ đề", chọn tài liệu nguồn và lớp → bản nháp các đề, mỗi đề có nhãn hai trục và nguồn.
2. Giáo viên · web: sửa, bỏ đề không đạt, bấm Duyệt vào kho → đề vào ngăn "Của tôi".
- *Ngoại lệ:* đề nháp không qua kiểm chứng → vẫn hiện nhưng nằm cuối, không được chọn sẵn.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C5-01 | Soạn bài có chấm | Giáo viên | Web | Gắn rubric, mức trợ giúp, lịch công bố | Đề, rubric, tài liệu được phép, lịch điểm và lời giải | Rubric chưa hợp lệ (chặn giao), tài liệu chưa duyệt | Chưa |
| C5-02 | Soạn rubric | Giáo viên | Web | Tạo và sửa tiêu chí | Tiêu chí, mức điểm, hướng dẫn, mẫu sẵn, phiên bản | Đã dùng (chỉ tạo phiên bản mới), tổng điểm sai | Chưa |
| C5-03 | Nộp bài viết tay | Học sinh | iPhone, iPad | Chụp và nộp | Máy ảnh, kiểm tra độ rõ, xếp trang, xác nhận nộp | Ảnh mờ, mất mạng, đã nộp, nộp muộn | Chưa |
| C5-04 | Hàng chờ chấm | Giáo viên | Web; điện thoại chỉ xem số lượng | Biết bài nào cần xem trước | Danh sách, cờ, lọc theo lớp và bài | Trống, AI đang xử lý, AI không chấm, AI đang tắt | Chưa |
| C5-05 | Chấm bài | Giáo viên | Web | Xem bằng chứng và duyệt | Ảnh gốc, bản đọc chữ, tiêu chí, đề xuất, độ tin cậy, nút nhận, sửa, bỏ, duyệt | Chạy song song (ẩn đề xuất), lệch với engine, đã duyệt, đã trả | Có: `vision-01`, `shots/14` |
| C5-06 | Bài đã trả | Học sinh | iPad, iPhone | Xem điểm và nhận xét | Điểm theo tiêu chí, nhận xét, lời giải, ảnh bài làm | Chờ chấm, chờ công bố, đã trả | Chưa |
| C5-07 | Kho tri thức | Giáo viên | Web | Quản lý tài liệu cho AI | Danh sách, trạng thái, phiên bản, gắn cho lớp hoặc bài | Trống, chờ duyệt, hết hiệu lực | Chưa |
| C5-08 | Trợ lý giáo viên | Giáo viên | Web | Soạn nháp và duyệt | Chọn việc, chọn nguồn, bản nháp có nhãn và nguồn, nút duyệt | Đang soạn, không đủ nguồn, nháp chưa kiểm chứng | Có: `vision-03`, `shots/15` |
| C5-09 | Điều khiển AI của trường | Quản trị trường | Web | Bật, tắt và xem kết quả chạy song song | Công tắc theo môn, bảng so sánh AI và giáo viên | Chưa đủ dữ liệu, đang tắt khẩn cấp | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Không có chuyển trạng thái nào từ đề xuất của AI sang điểm chính thức mà thiếu hành động duyệt của giáo viên dạy lớp đó.
- Đề xuất của AI chỉ giáo viên của lớp thấy. Học sinh, phụ huynh, quản trị trường và cơ quan quản lý đều không thấy.
- Rubric, đề và tài liệu đã dùng cho bài đã giao là bất biến; mỗi đề xuất ghi rõ phiên bản đề, rubric, bài nộp, model và prompt.
- Ảnh gốc không bao giờ bị sửa hay thay thế. Bản đọc chữ sửa được và có lịch sử.
- AI phía học sinh chỉ dùng tài liệu "đang dùng" được gắn cho lớp hoặc bài đó; mỗi câu trả lời lưu nguồn và quyết định.
- Trợ lý không gửi, không công bố, không đổi điểm; mọi bản nháp mang nhãn "AI soạn" cho tới khi duyệt.
- Dữ liệu của trường này không truy cập được từ trường khác dù đổi mã định danh. Quản trị nền tảng không mặc nhiên xem được bài làm.

### 8. Dữ liệu mới
| Thực thể | Nội dung | Trạng thái |
|---|---|---|
| `Organization`, `Membership` | Trường và quan hệ người dùng–trường; kích hoạt `org_id` | — |
| `Rubric`, `RubricVersion`, `RubricCriterion` | Tiêu chí chấm, bất biến khi đã dùng | Nháp → đang dùng → lưu trữ |
| `Submission`, `SubmissionVersion`, `SubmissionFile` | Bài nộp, ảnh gốc | Nháp → đã nộp → đang xử lý → chờ giáo viên → đã duyệt → đã trả |
| `OCRArtifact` | Bản đọc chữ, độ tin cậy | — |
| `GradingProposal`, `CriterionAssessment` | Đề xuất theo tiêu chí, bằng chứng, độ tin cậy | Chờ → đã đề xuất → đã xem; ngoại lệ: AI không chấm, lỗi |
| `ApprovedGrade` | Điểm chính thức, người duyệt, phần đã sửa | Đã duyệt → đã trả |
| `ReleasePolicy` | Lịch công bố điểm, đáp số, lời giải | — |
| `KnowledgeItem`, `KnowledgeVersion`, `KnowledgeGrant` | Tài liệu, phiên bản, phạm vi dùng | Nháp → chờ duyệt → đang dùng → lưu trữ |
| `AIInteraction`, `AIPolicyDecision` | Câu hỏi, trả lời, nguồn, quyết định cho phép hay chặn | — |
| `CopilotDraft` | Bản nháp của trợ lý, nguồn, người duyệt | Nháp → đã duyệt → đã bỏ |
| `FeatureSwitch` | Công tắc AI theo trường, môn | — |

### 9. Phụ thuộc
- Từ chặng 3: tài khoản, lớp, bộ bài, hộp việc của giáo viên. Từ chặng 2: nhãn hai trục, engine, bài luyện.
- Nền móng cần có: thỏa thuận với trường pilot về dữ liệu bài làm; bài mẫu đã được giáo viên chấm để đánh giá AI; chốt nhà cung cấp model và nơi lưu ảnh (cần xác minh yêu cầu pháp lý); quy trình xử lý sự cố lộ đáp án và chấm sai.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| AI đọc sai chữ viết tay toán | Kiểm tra độ rõ lúc chụp; AI được từ chối; giáo viên luôn thấy ảnh gốc |
| Giáo viên duyệt theo quán tính | Bài có cờ xếp trước; đo tỉ lệ sửa; chạy song song định kỳ trên mẫu ngẫu nhiên |
| Giáo viên không chịu soạn rubric | Mẫu rubric theo dạng bài; trợ lý soạn nháp rubric từ lời giải |
| Kiểm soát đáp án chặn nhầm | Giáo viên xem được các lần chặn và báo "chặn sai" |
| Phạm vi quá lớn cho một chặng | Tách ba đợt bật: nộp bài và chấm tay → chạy song song → AI đề xuất |

**Chỉ số (cách đo):** mức khớp giữa AI và giáo viên theo từng tiêu chí và chủ đề; tỉ lệ tiêu chí bị giáo viên sửa; tỉ lệ bài AI từ chối chấm; thời gian duyệt một bài so với chấm tay trên cùng loại bài; số lần lộ đáp án trong bộ câu thử; tỉ lệ câu trả lời của AI học sinh có nguồn đã duyệt; tỉ lệ bản nháp của trợ lý được duyệt sau khi sửa.

### 11. Câu hỏi mở
1. Trường pilot là ai và họ cam kết gì (số lớp, cho dùng bài làm thật để đánh giá AI)?
2. Ngưỡng khớp giữa AI và giáo viên bao nhiêu thì được bật đề xuất cho một lớp? Ai quyết?
3. Ảnh bài làm của học sinh được lưu ở đâu, giữ bao lâu, ai là chủ thể quản lý dữ liệu? Cần xác minh pháp lý.
4. Kho tri thức: giáo viên tự duyệt tài liệu của mình, hay cần tổ bộ môn duyệt?
5. Điểm trong sản phẩm có vai trò gì so với sổ điểm chính thức của trường?

---

## Chặng 6 — Hồ sơ học tập và phụ huynh

### 1. Mục tiêu, giá trị và điều kiện mở
Biến dữ liệu đã có (bài làm, gợi ý, câu hỏi, điểm đã duyệt) thành hồ sơ học tập theo khái niệm, luôn kèm bằng chứng, và mở cho phụ huynh phần đã được giáo viên duyệt. Giáo viên biết nên giúp em nào việc gì; học sinh thấy mình tiến bộ ở đâu; phụ huynh biết con đang học gì mà không tạo thêm áp lực.

**Điều kiện mở:** chặng 5 ổn định (điểm đã duyệt đủ tin cậy làm bằng chứng) và quy trình xác minh phụ huynh đã được thử với trường pilot.

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Giáo viên | Web; điện thoại để trả lời tin nhắn | Xem hồ sơ, duyệt nhận định, ghi nhận can thiệp, duyệt bản tin |
| Học sinh | iPad, iPhone | Xem hồ sơ của mình |
| Phụ huynh | iPhone (hoặc iPad) | Liên kết với con, xem tổng quan, trao đổi với giáo viên |
| Quản trị trường hoặc giáo viên chủ nhiệm | Web | Phát mã và xác minh quan hệ phụ huynh |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Mức nắm theo khái niệm có bằng chứng | Mỗi khái niệm có ước lượng, số bài căn cứ và độ chắc | Rất cao | Cao | Ước lượng bị đọc như điểm số | Làm |
| 2 | Giáo viên duyệt nhận định và ghi nhận can thiệp | "Chỗ em hay vướng" do AI đề xuất, giáo viên nhận, sửa hoặc bỏ; ghi việc đã làm và hẹn xem lại kèm bằng chứng mới | Rất cao | Trung bình | Thêm việc cho giáo viên | Làm |
| 3 | Hồ sơ của em | Học sinh thấy "đang vững" và "đang luyện", tự chọn mục tiêu | Cao | Trung bình | Ngôn ngữ dễ thành nhãn | Làm |
| 4 | Xác minh phụ huynh bằng mã của trường | Mã dùng một lần gắn với một học sinh, trường phát | Rất cao | Trung bình | Mã đến nhầm người | Làm |
| 5 | Bản tin phụ huynh do giáo viên duyệt | Tóm tắt theo kỳ, trợ lý soạn nháp, giáo viên duyệt rồi mới gửi | Cao | Trung bình | Bản tin chung chung | Làm (P1) |
| 6 | "Ở nhà giúp con thế nào" | Gợi ý hành động cụ thể, không đòi phụ huynh biết Toán | Cao | Thấp | Lời khuyên sáo rỗng | Làm |
| 7 | Trao đổi phụ huynh–giáo viên | Tin nhắn một–một, có giờ yên tĩnh | Cao | Trung bình | Giáo viên bị làm phiền | Làm |
| 8 | Gợi ý luyện tập cá nhân | Hệ thống đề xuất bài luyện theo phần đang vướng | Cao | Trung bình | Sai khi bằng chứng mỏng | Sau (P1) |
| 9 | Phụ huynh xem từng câu con hỏi, nhận báo tức thời | Toàn bộ lịch sử hỏi đáp và thông báo theo từng hành động của con | Trung bình | Thấp | Trẻ ngại hỏi; giám sát quá mức; trái mặc định riêng tư | Bỏ |
| 10 | So với trung bình lớp, xếp hạng | Vị trí của con trong lớp | Trung bình với phụ huynh | Thấp | Gắn nhãn, lộ dữ liệu bạn khác | Bỏ |

**Phản biện.** Ý 9 và 10 là những thứ phụ huynh sẽ xin đầu tiên. Cả hai đều biến công cụ học thành công cụ giám sát, và làm học sinh thôi hỏi thật. `PRODUCT-MASTER.md` đã đặt lịch sử câu hỏi là "không mặc định" với phụ huynh; đề xuất giữ nguyên. Ý 5 và 6 trả lời đúng nhu cầu thật của phụ huynh ("con tôi thế nào, tôi nên làm gì") bằng thông tin đã qua tay giáo viên.

### 4. Phạm vi đề xuất
- **P0:** hồ sơ theo khái niệm có bằng chứng; hàng duyệt nhận định; ghi nhận can thiệp; hồ sơ của em; liên kết và xác minh phụ huynh; tổng quan của con (chỉ nội dung đã duyệt); "ở nhà giúp con thế nào"; trao đổi một–một có giờ yên tĩnh; thu hồi liên kết.
- **P1:** bản tin phụ huynh theo kỳ; gợi ý luyện tập cá nhân; nhiều phụ huynh cho một học sinh với quyền khác nhau; lịch sắp tới.
- **Ngoài phạm vi:** phụ huynh xem lịch sử hỏi đáp; thông báo theo từng hành động; xếp hạng và so sánh; dự đoán điểm; nhóm phụ huynh của lớp.

### 5. Luồng chính
**Luồng A — Giáo viên xem hồ sơ và ghi nhận can thiệp**
1. Giáo viên · web: mở Lớp học, bấm Hồ sơ của một học sinh → mức nắm theo khái niệm, mỗi dòng có số bài căn cứ.
2. Giáo viên · web: bấm một khái niệm → danh sách bài làm, gợi ý đã dùng, câu hỏi liên quan.
3. Giáo viên · web: xem "Chỗ em hay vướng" do AI đề xuất, bấm nhận, sửa hoặc bỏ → nhận định đã nhận mới hiện cho học sinh và phụ huynh.
4. Giáo viên · web: thêm ghi nhận (ví dụ giao thêm bài, hẹn ngày xem lại) → đến hẹn, hệ thống nhắc và hiện bằng chứng mới từ sau ngày ghi nhận.
- *Ngoại lệ:* ít bằng chứng → dòng khái niệm ghi "chưa đủ dữ liệu", không hiện phần trăm. Giáo viên gõ từ gắn nhãn con người → hệ thống gợi ý diễn đạt theo hành vi quan sát được.

**Luồng B — Phụ huynh liên kết với con**
1. Quản trị trường hoặc giáo viên chủ nhiệm · web: tạo mã liên kết cho từng học sinh, phát qua kênh của trường.
2. Phụ huynh · iPhone: tạo tài khoản, nhập mã, đọc phần "bạn sẽ thấy gì và không thấy gì", đồng ý → trạng thái "chờ xác minh". Chỉ sau khi trường xác minh mới thành "đã liên kết" (PO-20).
3. Quản trị trường · web: xác nhận quan hệ → phụ huynh thấy tổng quan của con.
- *Ngoại lệ:* mã hết hạn hoặc đã dùng → báo rõ, hướng dẫn xin mã mới. Quan hệ chấm dứt hoặc trường thu hồi → quyền mất ngay, phụ huynh được báo. Một phụ huynh có nhiều con → chuyển giữa các con ở đầu màn hình.

**Luồng C — Phụ huynh xem tổng quan và trao đổi**
1. Phụ huynh · iPhone: mở ứng dụng → bài của con, phần đang làm tốt, phần nên luyện thêm, bài đang chờ giáo viên duyệt (không có điểm AI).
2. Phụ huynh · iPhone: bấm "Ở nhà giúp con thế nào" → hai hoặc ba việc cụ thể.
3. Phụ huynh · iPhone: gửi tin nhắn cho giáo viên → nếu trong giờ yên tĩnh, báo "thầy cô sẽ nhận vào giờ làm việc".
4. Giáo viên · điện thoại hoặc web: trả lời → phụ huynh nhận thông báo.
- *Ngoại lệ:* chưa có nội dung nào được duyệt → màn hình nói rõ "thầy cô chưa gửi nhận xét", vẫn hiện bài được giao. Tin nhắn có dấu hiệu khẩn cấp về an toàn → hướng sang kênh liên hệ của trường.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C6-01 | Hồ sơ học tập | Giáo viên | Web | Hiểu một học sinh qua bằng chứng | Mức nắm theo khái niệm, chỗ hay vướng, ghi nhận, dòng thời gian | Chưa đủ dữ liệu, nhận định chờ duyệt | Có: `vision-02`, `shots/24` |
| C6-02 | Bằng chứng của khái niệm | Giáo viên | Web | Xem căn cứ của một con số | Bài làm, gợi ý, câu hỏi, điểm đã duyệt | Không có bằng chứng, đang tải | Chưa |
| C6-03 | Duyệt nhận định | Giáo viên | Web | Nhận, sửa, bỏ đề xuất của AI | Hàng chờ theo lớp, căn cứ, ô sửa lời | Trống, gợi ý đổi cách diễn đạt | Chưa |
| C6-04 | Hồ sơ của em | Học sinh | iPad, iPhone | Thấy tiến bộ của mình | Đang vững, đang luyện, mục tiêu tự chọn, bài nên làm tiếp | Mới bắt đầu, chưa có nhận xét của thầy cô | Chưa |
| C6-05 | Liên kết với con | Phụ huynh | iPhone, iPad | Nhập mã và đồng ý | Ô mã, phần "thấy gì, không thấy gì", đồng ý | Mã sai, chờ xác minh, bị thu hồi | Chưa |
| C6-06 | Tổng quan của con | Phụ huynh | iPhone, iPad | Biết con đang học gì | Bài của con, đang làm tốt, nên luyện thêm, bài chờ duyệt, gợi ý ở nhà | Chưa có nội dung duyệt, nhiều con | Có: `vision-04`, `shots/13`, `shots/25`, `dev-iphone-ph-tong-quan` |
| C6-07 | Trao đổi | Phụ huynh; giáo viên | iPhone; web và điện thoại | Nhắn tin một–một | Luồng tin, trạng thái giờ yên tĩnh | Ngoài giờ, chưa có tin, bị tắt bởi trường | Có một phần: `shots/13` |
| C6-08 | Quản lý liên kết phụ huynh | Quản trị trường, giáo viên chủ nhiệm | Web | Phát mã, xác minh, thu hồi | Danh sách học sinh, trạng thái liên kết, nhật ký | Chưa phát mã, chờ xác minh, đã thu hồi | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Phụ huynh chỉ thấy: bài được giao, điểm đã duyệt và đã trả, nhận định giáo viên đã nhận. Không thấy: đề xuất của AI, lịch sử hỏi đáp, ghi chú riêng của giáo viên, bất kỳ thông tin nào về học sinh khác.
- Quan hệ phụ huynh–học sinh do trường xác minh, không do phụ huynh tự khai. Thu hồi có hiệu lực ngay.
- Mọi nhận định hiển thị phải có ít nhất một bằng chứng mở được; không có thì ghi "ước lượng, chưa đủ dữ liệu" hoặc không hiện.
- Không dùng từ mô tả con người (yếu, kém, lười). Chỉ mô tả hành vi quan sát được trên bài làm.
- Học sinh thấy toàn bộ hồ sơ của chính mình, trừ ghi chú riêng của giáo viên.
- Giáo viên chỉ thấy hồ sơ của học sinh mình đang dạy, trong môn mình dạy.
- Trao đổi tuân theo giờ yên tĩnh do trường đặt; nội dung được lưu theo chính sách của trường.

### 8. Dữ liệu mới
| Thực thể | Nội dung | Trạng thái |
|---|---|---|
| `LearningSignal` | Tín hiệu quan sát được gắn với khái niệm, kèm bằng chứng | — |
| `MasterySnapshot` | Ước lượng mức nắm, độ chắc, số bài căn cứ | — |
| `Insight` | Nhận định do AI đề xuất | Đề xuất → đã nhận, đã sửa hoặc đã bỏ |
| `Intervention` | Ghi nhận của giáo viên, ngày hẹn, kết quả | Mở → đã xem lại |
| `GuardianRelationship` | Quan hệ phụ huynh–học sinh, phạm vi được xem | Đã yêu cầu → chờ xác minh → đang hiệu lực → tạm dừng, thu hồi hoặc hết hạn |
| `GuardianInvite` | Mã liên kết dùng một lần | Đã phát → đã dùng → hết hạn |
| `Conversation`, `Message` | Trao đổi một–một | — |
| `ParentDigest` (P1) | Bản tin theo kỳ | Nháp → đã duyệt → đã gửi |

### 9. Phụ thuộc
- Từ chặng 5: điểm đã duyệt, lịch sử hỏi đáp, tổ chức cấp trường. Từ chặng 4: danh mục khái niệm theo môn.
- Nền móng cần có: ứng dụng cho phụ huynh trên iPhone; quy trình xác minh đã thử; văn bản đồng thuận; chính sách lưu và xóa dữ liệu (cần xác minh pháp lý).

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Ước lượng mức nắm sai nhưng trông chắc chắn | Luôn hiện số bài căn cứ và độ chắc; dưới ngưỡng thì không hiện con số |
| Phụ huynh dùng để gây áp lực | Không thông báo theo hành động; ngôn ngữ hướng tới việc nên làm |
| Liên kết nhầm phụ huynh | Mã dùng một lần, trường xác minh, nhật ký truy cập, thu hồi tức thì |
| Giáo viên quá tải vì tin nhắn | Giờ yên tĩnh; trợ lý soạn nháp câu trả lời; trường tắt được kênh |
| Hàng duyệt nhận định bị bỏ mặc | Gộp theo lớp; chỉ đề xuất nhận định có đủ bằng chứng |

**Chỉ số (cách đo):** tỉ lệ nhận định được nhận, sửa, bỏ; tỉ lệ nhận định có bằng chứng mở được (phải là toàn bộ); số ghi nhận can thiệp có xem lại; tỉ lệ phụ huynh hoàn tất liên kết; số ngoại lệ xác minh; mức phụ huynh quay lại xem; thời gian giáo viên phản hồi tin nhắn; số phản ánh của học sinh về việc bị giám sát.

### 11. Câu hỏi mở
1. Ai xác minh phụ huynh: văn phòng trường hay giáo viên chủ nhiệm? Có trường pilot nào sẵn sàng thử quy trình không?
2. Phụ huynh có được xem ảnh bài làm của con không, hay chỉ điểm và nhận xét?
3. Có giữ số liệu kiểu "số ngày học liên tiếp" trên màn phụ huynh (bản phác hiện có) không? Đề xuất: bỏ, vì dễ thành áp lực.
4. Học sinh có được biết phụ huynh thấy những gì không? Đề xuất: có, hiện rõ trong Hồ sơ của em.
5. Học sinh THPT có quyền gì với việc phụ huynh xem hồ sơ? Cần xác minh pháp lý.

---

## Chặng 7 — Cộng tác và liên trường

### 1. Mục tiêu, giá trị và điều kiện mở
Mở việc học cùng nhau một cách an toàn: nhóm học do giáo viên lập cùng thao tác trên một hình, và giáo viên các trường chia sẻ tài nguyên đã duyệt. Giá trị với học sinh là giải thích cho nhau; với giáo viên là không phải soạn lại thứ đồng nghiệp đã soạn tốt.

**Điều kiện mở:** thiết kế bảo vệ trẻ em được duyệt (quy tắc trò chuyện, kiểm duyệt, quy trình xử lý báo cáo, người chịu trách nhiệm ở từng trường).

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Học sinh | iPad để thao tác hình chung; iPhone để theo dõi và nhắn | Tham gia nhóm học |
| Giáo viên | Web; điện thoại để nhận cảnh báo | Lập và điều phối nhóm; chia sẻ và dùng tài nguyên chung |
| Cán bộ phụ trách an toàn của trường | Web | Xử lý báo cáo |
| Quản trị trường | Web | Duyệt hợp tác và cấp phép chia sẻ liên trường |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Thư viện chung của giáo viên | Chia sẻ đề và rubric đã duyệt, ghi công tác giả, "mượn" về kho riêng | Rất cao | Trung bình | Bản quyền nội dung | Làm trước tiên |
| 2 | Nhóm trong lớp cùng thao tác một hình | Giáo viên lập nhóm nhỏ có thời hạn; cả nhóm cùng xoay, đo, thêm đường trên một hình | Cao | Cao | Đồng bộ nhiều thiết bị | Làm |
| 3 | Trò chuyện nhóm bằng chữ có kiểm duyệt | Giáo viên xem được toàn bộ, có lọc nội dung và nút báo cáo | Cao | Trung bình | Bắt nạt, nội dung xấu | Làm |
| 4 | Trợ lý nhóm chỉ gợi ý | Dùng chung cổng kiểm soát đáp án | Trung bình | Thấp | Một em moi đáp án cho cả nhóm | Làm |
| 5 | Vai luân phiên trong nhóm | Người dựng hình, người phản biện, người ghi lời giải | Cao | Thấp | Cứng nhắc nếu ép buộc | Làm (P1) |
| 6 | Trao đổi sản phẩm giữa hai lớp | Hai lớp, hai trường gửi lời giải cho nhau nhận xét, không nhắn tin trực tiếp | Cao | Trung bình | Thấp hơn nhiều so với nhóm liên trường | Làm (P1) |
| 7 | Chia sẻ liên trường qua cấp phép | Trường cấp quyền theo phạm vi, mục đích, thời hạn | Cao | Cao | Phân quyền phức tạp | Làm |
| 8 | Nhóm học liên trường trực tiếp | Học sinh hai trường cùng phòng | Trung bình | Cao | An toàn, hai trường cùng chịu trách nhiệm | Sau |
| 9 | Video trong nhóm | Gọi hình có giáo viên điều phối | Trung bình | Rất cao | Rủi ro an toàn cao nhất, chi phí lớn | Sau |
| 10 | Nhắn riêng giữa học sinh, bảng tin công khai | Tin nhắn một–một; theo dõi nhau kiểu mạng xã hội | Thấp | Trung bình | Không điều phối được; trái định vị sản phẩm | Bỏ |

**Phản biện.** Bản phác `vision-07` vẽ ngay cảnh hấp dẫn nhất: nhóm liên trường cùng thao tác hình. Đó là điểm đến, không phải điểm bắt đầu. Thứ tự đề xuất: giáo viên chia sẻ tài nguyên (không có trẻ em trong luồng) → nhóm trong lớp → trao đổi sản phẩm giữa hai lớp → nhóm liên trường. Video (ý 9) tốn nhiều nhất mà trường thường đã có công cụ họp; chỉ làm khi có bằng chứng nhóm chữ và hình chung chưa đủ.

### 4. Phạm vi đề xuất
- **P0:** thư viện chung của giáo viên theo môn; nhóm học trong lớp; phòng nhóm có hình chung, trò chuyện chữ, trợ lý nhóm; màn điều phối của giáo viên; báo cáo và xử lý an toàn; cấp phép chia sẻ tài nguyên liên trường; công tắc tắt trò chuyện theo trường, lớp.
- **P1:** vai luân phiên; trao đổi sản phẩm giữa hai lớp; nhóm liên trường do hai trường cùng duyệt.
- **Ngoài phạm vi:** nhắn riêng học sinh–học sinh; bảng tin công khai; video (chờ quyết định riêng); nhóm do học sinh tự lập không có giáo viên.

### 5. Luồng chính
**Luồng A — Giáo viên chia sẻ và mượn tài nguyên**
1. Giáo viên · web: mở một đề đã duyệt, bấm Chia sẻ → chọn phạm vi (trong trường, các trường được cấp phép), điều kiện sử dụng.
2. Giáo viên khác · web: tìm trong Thư viện chung, xem đề với nhãn hai trục và tên tác giả → bấm Mượn về kho.
3. Hệ thống: tạo bản sao có ghi nguồn → tác giả thấy số lần được mượn.
- *Ngoại lệ:* tác giả rút chia sẻ → bản đã mượn vẫn dùng được, không mượn mới. Đề bị báo sai → gắn cờ ở mọi bản mượn, báo cho người đã mượn.

**Luồng B — Nhóm học trong lớp**
1. Giáo viên · web: chọn bài, bấm Lập nhóm, chia học sinh, đặt thời hạn hoạt động → học sinh thấy nhóm trong "Bài của em".
2. Học sinh · iPad: vào phòng nhóm → hình chung ở giữa, trò chuyện bên phải, tên các bạn đang có mặt.
3. Học sinh · iPad: thêm một đường phụ → mọi máy thấy ngay, có tên người làm.
4. Học sinh · iPad: hỏi trợ lý nhóm → chỉ nhận gợi ý; lời giải khóa theo lịch của bài.
5. Giáo viên · web: mở Điều phối → thấy mọi phòng, đọc được trò chuyện, tạm khóa phòng khi cần.
- *Ngoại lệ:* hai em sửa cùng một đối tượng → giữ thao tác đến trước, báo cho em còn lại. Bộ lọc bắt nội dung không phù hợp → tin bị giữ lại, giáo viên được báo. Hết thời hạn → phòng chuyển chỉ đọc.

**Luồng C — Báo cáo và xử lý**
1. Học sinh · iPad hoặc iPhone: bấm Báo cáo trên một tin nhắn, chọn lý do → nhận xác nhận, không lộ danh tính với nhóm.
2. Cán bộ an toàn · web: mở hồ sơ báo cáo → nội dung, ngữ cảnh, lịch sử; chọn cách xử lý.
3. Hệ thống: ghi kết quả, báo cho người báo cáo rằng đã xử lý.
- *Ngoại lệ:* báo cáo nghiêm trọng → khóa phòng ngay, chuyển theo quy trình của trường. Nhóm liên trường → cả hai trường cùng nhận báo cáo.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C7-01 | Thư viện chung | Giáo viên | Web | Tìm, mượn, chia sẻ tài nguyên | Tìm theo môn và dạng bài, nhãn hai trục, tác giả, nút mượn | Trống, đã rút chia sẻ, bị gắn cờ | Chưa |
| C7-02 | Lập nhóm | Giáo viên | Web | Chia nhóm cho một bài | Danh sách học sinh, nhóm, thời hạn, quyền trò chuyện | Trò chuyện bị trường tắt, lớp quá ít người | Chưa |
| C7-03 | Phòng nhóm | Học sinh | iPad; iPhone ở chế độ xem và nhắn | Cùng làm một bài | Hình chung, trò chuyện, trợ lý nhóm, người có mặt, nút báo cáo | Một mình trong phòng, bị tạm khóa, chỉ đọc, mất kết nối | Có: `vision-07`, `shots/18` |
| C7-04 | Điều phối nhóm | Giáo viên | Web; điện thoại nhận cảnh báo | Theo dõi và can thiệp | Danh sách phòng, hoạt động, tin bị giữ, nút tạm khóa | Không có phòng đang mở, có cảnh báo | Chưa |
| C7-05 | Báo cáo nội dung | Học sinh | iPad, iPhone | Báo cáo an toàn | Lý do, ghi chú, xác nhận | Đã gửi, đã xử lý | Một phần: nút báo cáo trong `vision-07` |
| C7-06 | Xử lý báo cáo | Cán bộ an toàn | Web | Xem và quyết định | Hàng chờ, ngữ cảnh, hành động, nhật ký | Trống, khẩn cấp, liên trường | Chưa |
| C7-07 | Hợp tác và cấp phép liên trường | Quản trị trường | Web | Duyệt chia sẻ với trường khác | Yêu cầu, phạm vi, mục đích, thời hạn, thu hồi | Chờ duyệt, đang hiệu lực, hết hạn | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Mọi không gian có học sinh trò chuyện đều có ít nhất một giáo viên điều phối, người này đọc được toàn bộ nội dung. Học sinh được báo rõ điều này.
- Không có tin nhắn riêng giữa học sinh. Không có nhóm không gắn với lớp hoặc bài.
- Trợ lý nhóm tuân theo cùng cổng kiểm soát đáp án như trợ lý cá nhân.
- Chia sẻ liên trường chỉ tồn tại qua cấp phép tường minh có phạm vi, mục đích, thời hạn; hết hạn hoặc thu hồi thì mất quyền, nhật ký vẫn giữ.
- Tài nguyên chia sẻ vẫn thuộc tác giả; bản mượn ghi nguồn và không chia sẻ lại nếu tác giả không cho.
- Thư viện chung chỉ nhận nội dung "Giáo viên đã duyệt". Dữ liệu học sinh không bao giờ đi theo tài nguyên.
- Phụ huynh không thấy trò chuyện nhóm. Ghi nhận đóng góp trong nhóm không xếp hạng.

### 8. Dữ liệu mới
| Thực thể | Nội dung | Trạng thái |
|---|---|---|
| `StudyGroup`, `GroupMembership` | Nhóm, thành viên, vai, thời hạn | Đang mở → tạm khóa → chỉ đọc → lưu trữ |
| `SharedScene` | Trạng thái hình chung và lịch sử thao tác theo người | — |
| `Conversation`, `Message.moderation_state` | Trò chuyện nhóm | Đã gửi, bị giữ, đã gỡ |
| `SafetyReport`, `ModerationCase` | Báo cáo và xử lý | Mới → đang xử lý → đã đóng |
| `SharedResource` | Tài nguyên trong thư viện chung, điều kiện sử dụng, nguồn gốc | Đang chia sẻ → đã rút → bị gắn cờ |
| `SchoolPartnership`, `SharingGrant` | Quan hệ và cấp phép giữa hai trường | Đã yêu cầu → đã duyệt → đang hiệu lực → hết hạn hoặc thu hồi |

### 9. Phụ thuộc
- Từ chặng 5: tổ chức cấp trường, kiểm soát đáp án, tài nguyên có phiên bản. Từ chặng 6: nền tin nhắn và giờ yên tĩnh. Từ chặng 2: thao tác hình có ghi lại.
- Nền móng cần có: thiết kế bảo vệ trẻ em được duyệt; mỗi trường có người phụ trách an toàn; quy tắc về bản quyền tài liệu chia sẻ (cần xác minh).

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Bắt nạt hoặc nội dung xấu trong nhóm | Giáo viên luôn có mặt; lọc trước khi hiện; báo cáo ẩn danh; công tắc tắt |
| Một em làm, cả nhóm chép | Ghi thao tác theo người; vai luân phiên; lời giải vẫn khóa theo lịch |
| Chia sẻ tài liệu có bản quyền của bên thứ ba | Người chia sẻ xác nhận quyền; có nút báo vi phạm |
| Hình chung không đồng bộ được trên mạng yếu | Thử trên mạng trường học thật trước khi mở; chế độ "một người thao tác, cả nhóm xem" |

**Chỉ số (cách đo):** số tài nguyên được mượn và được giao lại cho lớp; tỉ lệ nhóm có hơn một thành viên thao tác trên hình; thời gian xử lý báo cáo; số sự cố an toàn theo mức độ; số cấp phép liên trường đang hiệu lực và được dùng thật.

### 11. Câu hỏi mở
1. Ai duyệt "thiết kế bảo vệ trẻ em": trường pilot, chuyên gia bên ngoài, hay cả hai?
2. Có làm video không, hay liên kết ra công cụ họp trường đang dùng?
3. Tài nguyên giáo viên chia sẻ thuộc về giáo viên hay nhà trường? Cần xác minh.
4. Nhóm liên trường: bắt đầu bằng trao đổi sản phẩm (đề xuất) hay làm trực tiếp như bản phác?
5. Nội dung trò chuyện của học sinh lưu bao lâu? Cần xác minh pháp lý.

---

## Chặng 8 — Vận hành nhà trường

### 1. Mục tiêu, giá trị và điều kiện mở
Cho nhà trường một nơi quản trị phần mà trường thực sự cần để dùng sản phẩm ở quy mô cả trường: người dùng, năm học, phân công, thông báo, chính sách và báo cáo tổng hợp, cùng nhận diện riêng. Mục tiêu không phải thay phần mềm quản lý trường học mà là bỏ việc nhập tay và cho ban giám hiệu nhìn thấy việc dạy học trên nền tảng.

**Điều kiện mở:** có trường yêu cầu và trả tiền. Phạm vi P0 do chính yêu cầu của trường đó quyết định.

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Quản trị trường | Web | Thiết lập trường, người dùng, phân công, chính sách |
| Ban giám hiệu, tổ trưởng bộ môn | Web | Xem báo cáo tổng hợp, gửi thông báo |
| Giáo viên | Web, điện thoại | Nhận phân công và thông báo |
| Học sinh, phụ huynh | iPad, iPhone | Nhận thông báo của trường |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Hồ sơ và nhận diện trường | Tên, logo, màu, cách gọi riêng trong khung chung | Trung bình | Thấp | Tùy biến quá đà phá trải nghiệm chung | Làm |
| 2 | Nhập danh sách và phân công | Nhập giáo viên, học sinh, lớp; phân công giáo viên–lớp–môn | Rất cao | Trung bình | Dữ liệu nguồn bẩn | Làm |
| 3 | Thông báo của trường có xác nhận | Gửi theo đối tượng, theo dõi đã đọc | Cao | Trung bình | Thành thêm một kênh gây nhiễu | Làm |
| 4 | Báo cáo theo tổ bộ môn | Tổng hợp mức dùng và kết quả theo tổ, khối, không theo cá nhân | Cao | Trung bình | Bị dùng để đánh giá giáo viên | Làm |
| 5 | Chính sách của trường | Giờ yên tĩnh, bật tắt AI, thời gian lưu, ai xác minh phụ huynh | Cao | Trung bình | Xung đột với thiết lập của giáo viên | Làm |
| 6 | Sổ truy cập minh bạch | Trường xem ai đã xem dữ liệu gì; xử lý yêu cầu xuất và xóa | Cao | Trung bình | Thấp | Làm |
| 7 | Kết nối hệ thống trường đang dùng | Đồng bộ danh sách và đăng nhập thay vì nhập tay | Rất cao | Cao | Mỗi trường một hệ thống; cần xác minh | Nhập file: Làm; đồng bộ: Sau |
| 8 | Thời khóa biểu, điểm danh | Hiển thị lịch dạy; ghi có mặt theo tiết | Trung bình | Trung bình | Trùng hệ thống sẵn có; ràng buộc quy định | Sau (thời khóa biểu chỉ hiển thị, P1) |
| 9 | Sổ điểm chính thức, học phí, lương | Thay sổ điểm và phần mềm tài chính của trường | Cao | Rất cao | Ràng buộc pháp lý cần xác minh; trái định vị "không phải ERP" | Bỏ |
| 10 | Xếp hạng giáo viên theo mức dùng | Bảng thứ hạng cá nhân | Thấp | Thấp | Phá lòng tin của giáo viên | Bỏ |

**Phản biện.** Tên chặng dễ kéo sản phẩm thành phần mềm quản lý trường học đầy đủ, điều `PRODUCT-MASTER.md` đã loại khỏi định vị. Ý 8 và 9 đều là thứ trường đang có ở nơi khác; làm lại là cạnh tranh ở chỗ không có lợi thế. Thứ trường chưa có là cái nhìn về việc học thật (ý 4) và quyền kiểm soát AI cùng dữ liệu (ý 5, 6). Ý 10 có thể do chính ban giám hiệu yêu cầu; nên từ chối vì nó làm giáo viên ngừng dùng thật.

### 4. Phạm vi đề xuất
- **P0:** hồ sơ và nhận diện trường; năm học, khối, tổ bộ môn; nhập danh sách; phân công; chuyển năm học; thông báo của trường có xác nhận; chính sách của trường; báo cáo tổng hợp theo tổ và khối; sổ truy cập và yêu cầu dữ liệu.
- **P1:** hiển thị thời khóa biểu và lịch; thư viện tài nguyên cấp trường; đồng bộ với hệ thống của trường; đăng nhập bằng định danh của trường.
- **Ngoài phạm vi:** sổ điểm và học bạ chính thức; điểm danh; xếp thời khóa biểu; tài chính, nhân sự; xếp hạng giáo viên.

### 5. Luồng chính
**Luồng A — Thiết lập trường đầu năm**
1. Quản trị trường · web: điền hồ sơ trường, tải logo, tạo năm học và tổ bộ môn → trang xem trước nhận diện.
2. Quản trị trường · web: tải file danh sách giáo viên, học sinh, lớp → hệ thống đối chiếu, hiện dòng lỗi và dòng trùng để sửa trước khi nhập.
3. Quản trị trường · web: phân công giáo viên–lớp–môn → giáo viên thấy lớp trong tài khoản; lớp giáo viên tự tạo trước đó được gợi ý ghép.
- *Ngoại lệ:* trùng học sinh đã có tài khoản → đề nghị ghép, không tạo mới. Giáo viên rời trường → lớp chuyển cho người khác, quyền cũ mất ngay, tài nguyên riêng xử lý theo chính sách.

**Luồng B — Gửi thông báo của trường**
1. Ban giám hiệu · web: soạn thông báo, chọn đối tượng (khối, lớp, giáo viên, phụ huynh), yêu cầu xác nhận hay không → xem trước theo từng vai.
2. Ban giám hiệu · web: bấm Gửi → người nhận thấy thông báo có dấu "Thông báo của trường".
3. Phụ huynh · iPhone: mở thông báo, bấm Đã đọc → bảng theo dõi cập nhật.
4. Ban giám hiệu · web: xem ai chưa xác nhận, gửi nhắc.
- *Ngoại lệ:* gửi trong giờ yên tĩnh → hẹn giờ gửi, trừ loại khẩn. Gửi nhầm → thu hồi, người đã đọc thấy ghi chú thu hồi.

**Luồng C — Đặt chính sách và xem báo cáo**
1. Quản trị trường · web: đặt chính sách (giờ yên tĩnh, AI chấm theo môn, trò chuyện nhóm) → hệ thống cho thấy thiết lập nào của giáo viên bị ảnh hưởng.
2. Tổ trưởng · web: mở báo cáo tổ → số bài giao, tỉ lệ học sinh làm, khái niệm cả khối hay vướng; bấm vào ra lớp, không ra từng học sinh.
- *Ngoại lệ:* nhóm quá nhỏ → gộp hoặc ẩn. Chính sách cấp trên không cho tắt → mục đó khóa kèm lý do.

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C8-01 | Tổng quan trường | Ban giám hiệu, quản trị trường | Web | Nhìn việc dạy học toàn trường | Số liệu theo tổ, thông báo gần đây, việc cần xử lý | Trường mới, chưa đủ dữ liệu, nhóm nhỏ bị ẩn | Có: `vision-05`, `shots/19` |
| C8-02 | Thiết lập trường | Quản trị trường | Web | Hồ sơ, nhận diện, năm học, tổ | Biểu mẫu, xem trước nhận diện | Chưa hoàn tất, đang chuyển năm học | Chưa |
| C8-03 | Người dùng và phân công | Quản trị trường | Web | Nhập danh sách, gán vai, phân công | Bảng, nhập file, đối chiếu lỗi, ghép trùng | Đang nhập, có lỗi, có trùng | Chưa |
| C8-04 | Thông báo của trường | Ban giám hiệu | Web | Soạn, gửi, theo dõi | Trình soạn, đối tượng, hẹn giờ, bảng xác nhận | Nháp, hẹn gửi, đã thu hồi | Một phần: khối thông báo trong `vision-05` |
| C8-05 | Hộp thông báo | Học sinh, phụ huynh, giáo viên | iPad, iPhone; web | Nhận và xác nhận | Danh sách, dấu của trường, nút Đã đọc | Trống, cần xác nhận, đã thu hồi | Chưa |
| C8-06 | Chính sách của trường | Quản trị trường | Web | Đặt quy tắc chung | Giờ yên tĩnh, công tắc AI, lưu trữ, xác minh phụ huynh | Bị khóa bởi cấp trên, xung đột | Chưa (kế thừa C5-09) |
| C8-07 | Báo cáo tổ bộ môn | Tổ trưởng, ban giám hiệu | Web | Hiểu việc dạy học theo tổ, khối | Biểu đồ, bảng, bấm xuống mức lớp | Chưa đủ dữ liệu, nhóm nhỏ | Chưa |
| C8-08 | Sổ truy cập và yêu cầu dữ liệu | Quản trị trường | Web | Minh bạch và xử lý xuất, xóa | Nhật ký, bộ lọc, hàng chờ yêu cầu | Trống, yêu cầu quá hạn | Chưa |

### 7. Quy tắc nghiệp vụ và quyền
- Quản trị trường quản lý tài khoản và phân công nhưng không mặc nhiên đọc được bài làm, lịch sử hỏi đáp hay đề xuất của AI.
- Báo cáo cho ban giám hiệu dừng ở mức lớp. Xem tới học sinh cần quan hệ dạy học hoặc mục đích được ghi lại.
- Chính sách của trường không tắt được các yêu cầu bắt buộc của nền tảng (giáo viên duyệt điểm, khóa lời giải ở máy chủ, kiểm duyệt trò chuyện).
- Nhận diện riêng chỉ đổi hình thức, không đổi luồng và quy tắc an toàn.
- Thông báo của trường luôn mang dấu phân biệt với tin của giáo viên.
- Người rời trường mất quyền ngay; dữ liệu học tập của học sinh xử lý theo chính sách lưu giữ.
- Truy cập khẩn cấp của nền tảng phải có mục đích, thời hạn và hiện trong sổ truy cập của trường.

### 8. Dữ liệu mới
| Thực thể | Nội dung | Trạng thái |
|---|---|---|
| `SchoolProfile` | Nhận diện và thông tin trường | — |
| `AcademicYear`, `Department` | Năm học, tổ bộ môn | Sắp tới → đang diễn ra → đã đóng |
| `Membership.role`, `scope` | Vai theo phạm vi | Đang hiệu lực → đã kết thúc |
| `TeachingAssignment` | Phân công giáo viên–lớp–môn | — |
| `ImportJob` | Lần nhập danh sách và kết quả đối chiếu | Đang xử lý → cần sửa → hoàn tất |
| `Announcement`, `Acknowledgment` | Thông báo và xác nhận | Nháp → hẹn gửi → đã gửi → thu hồi |
| `Policy` | Quy tắc của trường, phiên bản, hiệu lực | — |
| `DataRequest` | Yêu cầu xuất, sửa, xóa | Mới → đang xử lý → hoàn tất |
| `ExternalIdMapping` (P1) | Ánh xạ mã từ hệ thống của trường | — |

### 9. Phụ thuộc
- Từ chặng 5: tổ chức cấp trường và cách ly dữ liệu. Từ chặng 6: quan hệ phụ huynh, tin nhắn. Từ chặng 3: lớp và kết quả lớp.
- Nền móng cần có: hợp đồng và mô hình giá; thỏa thuận xử lý dữ liệu với trường; khảo sát hệ thống trường đang dùng (cần xác minh); kênh hỗ trợ cho quản trị trường.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Phình thành phần mềm quản lý trường học | Giữ danh sách "ngoài phạm vi"; mỗi yêu cầu mới phải gắn với việc dạy học |
| Báo cáo bị dùng để chấm giáo viên | Không có bảng cá nhân; tổng hợp theo tổ, khối |
| Nhập danh sách sai làm lộ dữ liệu | Bước đối chiếu bắt buộc; nhập thử trước khi áp dụng |
| Xây theo yêu cầu riêng của một trường | Tách phần cấu hình khỏi phần lõi; yêu cầu riêng phải dùng được cho trường thứ hai |

**Chỉ số (cách đo):** tỉ lệ lớp được tạo từ phân công so với tự tạo; số lỗi nhập trên mỗi lần nhập; tỉ lệ thông báo được xác nhận; tỉ lệ giáo viên trong trường giao bài; số yêu cầu dữ liệu xử lý đúng hạn; số trường gia hạn.

### 11. Câu hỏi mở
1. Trường trả tiền cho điều gì cụ thể: số học sinh, số giáo viên, hay gói cả trường?
2. Trường đầu tiên đang dùng hệ thống quản lý nào và cần kết nối tới mức nào? Cần xác minh.
3. Ban giám hiệu được xem tới mức nào: lớp (đề xuất) hay từng giáo viên?
4. Điểm đã duyệt có cần xuất sang sổ điểm của trường không?
5. Mức tùy biến nhận diện: logo và màu (đề xuất), hay cả tên miền riêng?

---

## Chặng 9 — Quản trị giáo dục

### 1. Mục tiêu, giá trị và điều kiện mở
Cho cơ quan quản lý một kênh phát hành văn bản tới đúng trường, theo dõi xác nhận và triển khai, và xem số liệu tổng hợp trong phạm vi được ủy quyền. Giá trị là thay việc gửi văn bản rồi gom báo cáo thủ công bằng một luồng truy được từng bước, mà không biến nền tảng thành công cụ soi từng học sinh.

**Điều kiện mở:** có ủy quyền pháp lý và đối tác quản lý cụ thể. Không thiết kế chi tiết trước khi có đối tác, vì quy trình văn bản phải theo đúng cách đối tác làm việc.

### 2. Người dùng và thiết bị
| Vai | Thiết bị | Việc chính |
|---|---|---|
| Cán bộ cơ quan quản lý | Web | Soạn, phát hành văn bản; theo dõi; xem tổng hợp |
| Người ký duyệt của cơ quan | Web | Duyệt trước khi phát hành |
| Quản trị trường, ban giám hiệu | Web | Nhận, xác nhận, nộp báo cáo triển khai |
| Giáo viên | Web, điện thoại | Nhận văn bản liên quan |

### 3. Brainstorm
| # | Ý tưởng | Mô tả | Giá trị | Công sức | Rủi ro | Xếp |
|---|---|---|---|---|---|---|
| 1 | Phát hành văn bản theo phạm vi | Văn bản có người ban hành, đối tượng, ngày hiệu lực, phiên bản | Rất cao | Trung bình | Sai phạm vi nhận | Làm |
| 2 | Tách xác nhận khỏi triển khai | "Đã đọc" và "đã làm" là hai trạng thái, hai bằng chứng khác nhau | Rất cao | Thấp | Trường thấy thêm việc | Làm |
| 3 | Mẫu báo cáo và thu báo cáo | Mẫu có hạn nộp và minh chứng; trường điền một lần | Cao | Trung bình | Thành báo cáo hình thức | Làm |
| 4 | Bảng tổng hợp có ngưỡng gộp | Chỉ số liệu gộp; nhóm nhỏ bị ẩn | Cao | Trung bình | Lộ danh tính gián tiếp | Làm |
| 5 | Ủy quyền có phạm vi và thời hạn | Ghi rõ được xem trường nào, loại dữ liệu nào, đến khi nào | Rất cao | Cao | Phức tạp khi cấu hình | Làm |
| 6 | Sổ minh bạch cho trường | Trường thấy cơ quan đã xem gì của mình | Cao | Thấp | Cơ quan có thể không muốn | Làm |
| 7 | Điền sẵn báo cáo từ dữ liệu nền tảng | Số liệu đã có được điền sẵn, trường duyệt trước khi nộp | Cao | Trung bình | Trường mất quyền kiểm soát số liệu nếu bỏ bước duyệt | Làm (P1) |
| 8 | Kết nối cơ sở dữ liệu ngành | Trao đổi dữ liệu với hệ thống của ngành | Cao | Rất cao | Chuẩn và pháp lý cần xác minh | Sau |
| 9 | Xếp hạng trường, AI gắn cờ "trường có vấn đề" | Bảng thứ hạng theo chỉ số; cảnh báo tự động | Hấp dẫn với quản lý | Trung bình | Trường chạy theo chỉ số; gắn nhãn tổ chức từ dữ liệu mỏng | Bỏ |
| 10 | Xem tới từng học sinh | Truy xuống hồ sơ cá nhân | Thấp | Thấp | Trái nguyên tắc riêng tư | Bỏ mặc định |

**Phản biện.** Ý 9 là thứ làm bảng điều khiển trông mạnh nhất, và cũng là thứ khiến trường ngừng dùng thật để giữ số đẹp. Nguyên tắc "không gắn nhãn" áp dụng cho trường như cho học sinh. Ý 6 là ý khác biệt và là điều kiện để trường tin: quyền xem của cấp trên phải nhìn thấy được từ phía dưới.

### 4. Phạm vi đề xuất
- **P0:** đăng ký cơ quan và ủy quyền theo phạm vi, thời hạn; soạn, duyệt, phát hành văn bản có phiên bản; hộp văn bản của trường và xác nhận; mẫu báo cáo, nộp báo cáo kèm minh chứng; theo dõi theo trường (xác nhận, triển khai, ngoại lệ); bảng số liệu tổng hợp có ngưỡng gộp; sổ minh bạch.
- **P1:** điền sẵn báo cáo; nhắc hạn và chuyển cấp khi quá hạn; chia sẻ thực hành tốt; xuất báo cáo.
- **Ngoài phạm vi:** xếp hạng trường; xem dữ liệu cá nhân học sinh; cảnh báo tự động; kết nối cơ sở dữ liệu ngành (chờ xác minh); thanh tra trực tuyến.

### 5. Luồng chính
**Luồng A — Phát hành văn bản**
1. Cán bộ · web: soạn văn bản, đính kèm bản gốc, chọn đối tượng nhận, ngày hiệu lực, có yêu cầu báo cáo hay không → hệ thống liệt kê các trường sẽ nhận theo ủy quyền.
2. Người ký duyệt · web: xem, duyệt → văn bản ở trạng thái "đã duyệt", hẹn giờ phát hành.
3. Hệ thống: phát hành → trường nhận trong hộp văn bản, có dấu "Văn bản chính thức".
- *Ngoại lệ:* trường nằm ngoài phạm vi ủy quyền → không chọn được, kèm lý do. Sửa sau khi phát hành → tạo phiên bản mới, trường xác nhận lại, bản cũ vẫn xem được.

**Luồng B — Trường xác nhận và báo cáo triển khai**
1. Quản trị trường · web: mở văn bản, bấm Xác nhận đã nhận → trạng thái của trường thành "đã xác nhận".
2. Quản trị trường · web: chuyển văn bản tới giáo viên liên quan qua thông báo của trường (chặng 8).
3. Quản trị trường · web: mở mẫu báo cáo, điền, đính minh chứng, nộp → trạng thái "đã báo cáo".
- *Ngoại lệ:* không thực hiện được đúng hạn → gửi "báo cáo ngoại lệ" kèm lý do thay vì bỏ trống. Báo cáo bị trả lại → sửa và nộp lại, giữ lịch sử.

**Luồng C — Theo dõi và xem tổng hợp**
1. Cán bộ · web: mở Theo dõi → bảng theo trường với hai cột riêng: xác nhận và triển khai.
2. Cán bộ · web: lọc trường chưa xác nhận, gửi nhắc → trường nhận nhắc trong hộp văn bản.
3. Cán bộ · web: mở Số liệu tổng hợp → chỉ số gộp theo khu vực; không có đường bấm xuống cá nhân.
- *Ngoại lệ:* ủy quyền hết hạn → dữ liệu trường đó biến khỏi bảng, kèm thông báo gia hạn. Nhóm dưới ngưỡng gộp → hiện "không đủ lớn để hiển thị".

### 6. Danh sách màn hình
| Mã | Tên | Vai | Thiết bị | Mục đích | Thành phần chính | Trạng thái cần thiết kế | Concept |
|---|---|---|---|---|---|---|---|
| C9-01 | Tổng quan triển khai | Cán bộ quản lý | Web | Nhìn nhanh văn bản đang chạy | Văn bản gần đây, tỉ lệ xác nhận và triển khai, ngoại lệ | Chưa có văn bản, ủy quyền sắp hết hạn | Có: `vision-06`, `shots/20` |
| C9-02 | Soạn và phát hành văn bản | Cán bộ, người ký duyệt | Web | Soạn, duyệt, phát hành | Nội dung, bản gốc đính kèm, đối tượng, hiệu lực, yêu cầu báo cáo | Nháp, chờ duyệt, hẹn phát hành, có phiên bản mới | Chưa |
| C9-03 | Theo dõi theo trường | Cán bộ | Web | Biết trường nào ở bước nào | Bảng trường, cột xác nhận, cột triển khai, ngoại lệ, nút nhắc | Chưa ai xác nhận, quá hạn | Một phần: bảng trong `vision-06` |
| C9-04 | Mẫu và báo cáo đã nộp | Cán bộ | Web | Tạo mẫu, đọc báo cáo | Trình tạo mẫu, danh sách báo cáo, minh chứng, trả lại | Chưa có báo cáo, bị trả lại | Chưa |
| C9-05 | Hộp văn bản của trường | Quản trị trường, ban giám hiệu | Web | Nhận, xác nhận, nộp báo cáo | Danh sách văn bản, nút xác nhận, mẫu báo cáo, báo cáo ngoại lệ | Trống, cần xác nhận, quá hạn, có phiên bản mới | Chưa |
| C9-06 | Ủy quyền và sổ minh bạch | Cán bộ; quản trị trường | Web | Xem phạm vi được phép và lịch sử truy cập | Danh sách ủy quyền, thời hạn, nhật ký xem | Hết hạn, bị thu hồi | Chưa |
| C9-07 | Số liệu tổng hợp | Cán bộ | Web | Xem chỉ số gộp | Biểu đồ theo khu vực, bộ lọc, chú thích cách tính | Nhóm nhỏ bị ẩn, chưa đủ dữ liệu | Một phần: khối số liệu trong `vision-06` |

### 7. Quy tắc nghiệp vụ và quyền
- Cơ quan quản lý chỉ thấy trường nằm trong ủy quyền còn hiệu lực, và chỉ loại dữ liệu ghi trong ủy quyền.
- Mặc định chỉ có số liệu gộp. Dữ liệu cá nhân học sinh không có đường truy cập từ giao diện; trường hợp có cơ sở pháp lý phải qua yêu cầu riêng, có mục đích, thời hạn và ghi vào sổ minh bạch.
- Xác nhận đã nhận và báo cáo triển khai là hai trạng thái tách biệt, không suy ra cái này từ cái kia.
- Văn bản đã phát hành là bất biến; sửa là phiên bản mới. Văn bản chính thức luôn có dấu phân biệt với tin của trường và của người dùng.
- Trường là bên duyệt số liệu trước khi nộp, kể cả khi hệ thống điền sẵn.
- Không có bảng xếp hạng trường. So sánh chỉ ở mức phân bố, không nêu tên.
- Chính sách cấp trên có thể đặt yêu cầu bắt buộc xuống trường, nhưng không nới được yêu cầu an toàn của nền tảng.

### 8. Dữ liệu mới
| Thực thể | Nội dung | Trạng thái |
|---|---|---|
| `Organization` loại cơ quan quản lý | Nút cấp trên trong cây tổ chức | — |
| `AuthorityDelegation` | Phạm vi trường, loại dữ liệu, hành động, thời hạn | Đề nghị → đang hiệu lực → hết hạn hoặc thu hồi |
| `OfficialNotice`, `NoticeVersion` | Văn bản, phiên bản, đối tượng, hiệu lực | Nháp → chờ duyệt → đã duyệt → hẹn phát hành → đã phát hành → đã đóng |
| `NoticeReceipt` | Trạng thái của từng trường với từng văn bản | Đã nhận → đã xác nhận → đang triển khai → đã báo cáo; ngoại lệ |
| `ReportTemplate`, `ImplementationReport` | Mẫu và báo cáo kèm minh chứng | Nháp → đã nộp → bị trả lại → đã chấp nhận |
| `AggregateMetric` | Chỉ số gộp, cách tính, ngưỡng gộp | — |
| `AccessLedger` | Ai ở cấp trên đã xem gì của trường nào | — |

### 9. Phụ thuộc
- Từ chặng 8: trường đã có tổ chức, thông báo, chính sách, sổ truy cập. Từ chặng 5: cách ly dữ liệu giữa các trường.
- Nền móng cần có: văn bản ủy quyền pháp lý; đối tác quản lý cụ thể và quy trình văn bản của họ; định nghĩa chỉ số gộp được các trường đồng ý; đủ số trường trong phạm vi để số liệu gộp có nghĩa.

### 10. Rủi ro và chỉ số
| Rủi ro | Cách giảm |
|---|---|
| Thiếu cơ sở pháp lý cho việc xem dữ liệu | Không mở chặng khi chưa có ủy quyền; mặc định chỉ số liệu gộp |
| Trường coi nền tảng là công cụ giám sát và rời bỏ | Sổ minh bạch; không xếp hạng; trường duyệt số liệu |
| Báo cáo hình thức | Điền sẵn từ dữ liệu thật; cho phép báo cáo ngoại lệ |
| Số liệu gộp vẫn lộ danh tính | Ngưỡng gộp; ẩn nhóm nhỏ; rà chéo giữa các bộ lọc |
| Xây riêng cho một đối tác | Tách quy trình văn bản thành cấu hình |

**Chỉ số (cách đo):** tỉ lệ trường trong phạm vi nhận được văn bản; tỉ lệ xác nhận và thời gian tới khi xác nhận; tỉ lệ báo cáo nộp đủ; thời gian phát hiện và xử lý ngoại lệ; số lần truy cập ngoài phạm vi (phải bằng không); số phản ánh của trường về quyền xem.

### 11. Câu hỏi mở
1. Đối tác quản lý đầu tiên ở cấp nào, và họ có thẩm quyền với những trường nào? Cần xác minh.
2. Cơ sở pháp lý nào cho phép cơ quan xem số liệu gộp từ nền tảng? Cần xác minh.
3. Ai trả tiền cho chặng này: cơ quan quản lý, hay đây là phần đi kèm để trường dùng?
4. Ngưỡng gộp tối thiểu là bao nhiêu, và ai quyết?
5. Văn bản có cần chữ ký số hay chỉ là kênh phân phối bản đã ký? Cần xác minh.

---

## Phần kết

### K1. Tổng hợp màn hình theo vai và thiết bị

| Vai | Thiết bị | Chặng 2 | Chặng 3 | Chặng 4 | Chặng 5 | Chặng 6 | Chặng 7 | Chặng 8 | Chặng 9 |
|---|---|---|---|---|---|---|---|---|---|
| Học sinh | iPad, iPhone | C2-05, C2-06, C2-07 | C3-07, C3-08 | C4-01, C4-02 | C5-03, C5-06 | C6-04 | C7-03, C7-05 | C8-05 | — |
| Phụ huynh | iPhone, iPad | — | — | — | — | C6-05, C6-06, C6-07 | — | C8-05 | — |
| Giáo viên | Web | C2-01, C2-02, C2-03, C2-04, C2-08 | C3-01, C3-02, C3-03, C3-04, C3-05 | C4-03, C4-04, C4-05 | C5-01, C5-02, C5-04, C5-05, C5-07, C5-08 | C6-01, C6-02, C6-03, C6-07 | C7-01, C7-02, C7-04 | C8-05 | — |
| Giáo viên | Điện thoại | — | C3-05, C3-06 | — | C5-04 (xem số lượng) | C6-07 | C7-04 (cảnh báo) | C8-05 | — |
| Quản trị trường, ban giám hiệu | Web | — | — | — | C5-09 | C6-08 | C7-06, C7-07 | C8-01 đến C8-04, C8-06 đến C8-08 | C9-05, C9-06 |
| Cơ quan quản lý | Web | — | — | — | — | — | — | — | C9-01 đến C9-04, C9-06, C9-07 |
| Nhóm sản phẩm (nội bộ) | Web | — | — | C4-06 | — | — | — | — | — |

Tổng cộng 61 màn hình. Trong đó 15 màn đã có bản phác đầy đủ trong `Design/`, 6 màn có bản phác một phần, còn lại là màn mới.

**Nhận xét về độ phủ concept.** Các bản phác hiện có tập trung vào màn "trình diễn" (chấm bài, hồ sơ, phòng nhóm, bảng của trường). Chỗ còn trống chủ yếu là màn thiết lập và ngoại lệ: thêm tài khoản, nộp bài, duyệt nhận định, xác minh phụ huynh, xử lý báo cáo. Đây lại là những màn quyết định sản phẩm có dùng được thật hay không.

### K2. Quyết định cần chủ sản phẩm chốt, xếp theo mức ảnh hưởng

| # | Quyết định | Ảnh hưởng tới | Đề xuất |
|---|---|---|---|
| 1 | Người trả tiền đầu tiên là ai | Thứ tự và phạm vi chặng 5 và 8; toàn bộ mô hình | Chốt trước khi mở chặng 5 |
| 2 | Chủ thể quản lý dữ liệu, nơi lưu, thời gian giữ, đồng thuận cho trẻ em | Chặng 3 trở đi | Lấy tư vấn pháp lý trước khi mở tài khoản chính thức |
| 3 | Học sinh dùng ứng dụng cài trên iPad, iPhone hay web | Thông báo, máy ảnh nộp bài, phân phối; chặng 3, 5, 6 | Chốt ở đầu chặng 3; cần xác minh giới hạn kỹ thuật |
| 4 | Giữ nguyên tắc "không tự duyệt điểm AI" kể cả khi trường yêu cầu | Chặng 5 | Giữ, không có ngoại lệ |
| 5 | Phụ huynh thấy gì và không thấy gì | Chặng 6 | Chỉ nội dung giáo viên đã duyệt; không lịch sử hỏi đáp |
| 6 | Trường pilot và điều họ cam kết | Điều kiện mở chặng 5, 6 | Tìm song song với chặng 3 |
| 7 | Cách xác nhận giáo viên | Chặng 2, 3 | Lời mời ở chặng 2, xác nhận ở chặng 3 |
| 8 | Môn học thứ hai | Chặng 4 | Hàm số và đồ thị |
| 9 | Tên nền tảng | Chặng 4 trở đi | Chốt trước khi mở môn hai |
| 10 | Có làm video không | Chặng 7 | Không làm trong P0; quyết lại khi có số đo |
| 11 | Giới hạn của "vận hành nhà trường" | Chặng 8 | Không làm sổ điểm chính thức, điểm danh, tài chính |
| 12 | Mức xem của ban giám hiệu và cơ quan quản lý | Chặng 8, 9 | Dừng ở mức lớp và số liệu gộp |

### K3. Khuyến nghị: làm kỹ chặng nào trước

**1. Thiết kế chi tiết ngay chặng 2 và chặng 3, coi như một khối.**
Hai chặng này có điều kiện mở gần nhất và phụ thuộc trực tiếp vào thứ MVP đang xây. Chúng cũng trả lời câu hỏi mà MVP chưa trả lời được: giáo viên thật có dùng lặp lại không. Trong khối này, ba màn đáng dồn công thiết kế nhất là C2-03 (soát và duyệt lời giải), C3-04 (kết quả lớp và "cả lớp vướng ở đâu") và C2-06 (bài luyện), vì chúng tạo ra thói quen dùng hằng ngày.

**2. Bắt đầu sớm phần khám phá của chặng 5, dù chưa mở chặng.**
Chặng 5 có rủi ro lớn nhất (đọc chữ viết tay toán, độ khớp giữa AI và giáo viên) và điều kiện mở phụ thuộc người ngoài (trường pilot). Hai việc có thể làm song song với chặng 2 và 3 mà không phải xây gì: thu bài làm mẫu đã được giáo viên chấm để đo AI, và thử cho vài giáo viên soạn rubric bằng mẫu. Nếu kết quả đo kém, phạm vi chặng 5 phải đổi sớm.

**3. Thử quy trình xác minh phụ huynh ngay khi có trường pilot.**
Đây là điều kiện mở chặng 6 và là việc về quy trình của trường, không phải việc xây phần mềm. Thử bằng giấy và mã in sẵn là đủ để biết trường làm được hay không.

**4. Chặng 4 chỉ cần giữ kỷ luật, chưa cần thiết kế thêm.**
Việc quan trọng nhất cho chặng 4 nằm ở chặng 2: đừng để Hình học đi tắt khỏi khung môn học. Mỗi chỗ viết riêng cho hình là một khoản nợ trả ở chặng 4.

**5. Chặng 7, 8, 9 dừng ở mức phác trong tài liệu này.**
Cả ba đều có điều kiện mở do bên ngoài quyết định (thiết kế bảo vệ trẻ em, trường trả tiền, ủy quyền pháp lý). Thiết kế chi tiết trước khi có đối tác cụ thể sẽ phải làm lại. Ngoại lệ duy nhất là thư viện chung của giáo viên ở chặng 7: nó không có trẻ em trong luồng và có thể kéo lên sớm nếu giáo viên ở chặng 3 có nhu cầu chia sẻ đề.

**Tóm lại:** làm kỹ chặng 2 và 3, đo sớm cho chặng 5, thử quy trình cho chặng 6, và để chặng 7 đến 9 chờ đối tác.
