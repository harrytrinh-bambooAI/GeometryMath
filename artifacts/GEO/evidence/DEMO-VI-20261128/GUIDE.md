# GUIDE — Bộ video hướng dẫn sử dụng "Hình Học Không Gian Cùng AI"

Phiên bản: v1.0 · 2026-11-28 · T-261128-77 · sinh bởi `app/e2e/demo-vi/make-guides.mjs index` (sửa lời ở `guides/voice/<id>/lines.tsv`, cấu trúc ở `app/e2e/demo-vi/guides/guides.json`).

Mỗi video một tác vụ, xem độc lập: slide mở "Hướng dẫn · <tác vụ>" → thao tác từng bước (nhãn "Bước N" + khoanh nút cần bấm) → slide tóm tắt + mẹo. Lời đọc và phụ đề tiếng Việt (phụ đề burn-in + tệp `.srt` riêng). Dữ liệu mẫu, AI giả lập. Giọng tạm macOS "Linh": thay bằng Google TTS bằng cách thả `<mã>.mp3` cùng tên vào `guides/voice/<id>/` rồi chạy `node demo-vi/make-guides.mjs <node_modules> build --only <id>`.

| # | Video | Vai | Thời lượng | Các bước | Tệp |
|---|---|---|---|---|---|
| 01 | Hướng dẫn · Tạo đề và dựng hình 3D | Giáo viên | 1:01 | 1. Mở trang Thêm đề<br>2. Gõ hoặc dán đề bài<br>3. Bấm Dựng hình<br>4. Soát hình, lời giải và nhãn kiểm chứng | `guides/01-gv-tao-de-dung-hinh.mp4` · `.srt` |
| 02 | Hướng dẫn · Giao bài cho lớp | Giáo viên | 0:53 | 1. Mở đề cần giao<br>2. Chọn lớp<br>3. Đặt gợi ý và giờ mở lời giải<br>4. Bấm Giao bài | `guides/02-gv-giao-bai.mp4` · `.srt` |
| 03 | Hướng dẫn · Theo dõi lớp khi học sinh làm bài | Giáo viên | 0:43 | 1. Mở trang Theo dõi<br>2. Đọc bốn thẻ tổng hợp<br>3. Xem từng học sinh | `guides/03-gv-theo-doi-lop.mp4` · `.srt` |
| 04 | Hướng dẫn · Soạn đề bằng AI từ tài liệu | Giáo viên | 0:51 | 1. Mở tài liệu nguồn<br>2. Bấm soạn đề nháp<br>3. Xem nhãn kiểm chứng<br>4. Chọn và duyệt | `guides/04-gv-soan-de-tu-tai-lieu.mp4` · `.srt` |
| 05 | Hướng dẫn · Ra đề theo ma trận, nhiều mã đề | Giáo viên | 0:50 | 1. Mở Ra đề<br>2. Chọn loại đề<br>3. Xem bộ đề và mã đề<br>4. Đổi một câu | `guides/05-gv-ra-de-ma-tran.mp4` · `.srt` |
| 06 | Hướng dẫn · Soạn giáo án theo PPCT | Giáo viên | 0:58 | 1. Mở Giáo án mới<br>2. Chọn chương, bài, tiết<br>3. Chọn đối tượng, bấm soạn<br>4. Đọc và sửa từng mục<br>5. Duyệt và tải về | `guides/06-gv-soan-giao-an.mp4` · `.srt` |
| 07 | Hướng dẫn · Quản lý kho tri thức của lớp | Giáo viên | 0:43 | 1. Mở Kho tri thức<br>2. Xem tài liệu và phiên bản<br>3. Duyệt hoặc lưu trữ | `guides/07-gv-kho-tri-thuc.mp4` · `.srt` |
| 08 | Hướng dẫn · Tạo rubric chấm điểm | Giáo viên | 0:44 | 1. Mở Rubric<br>2. Sửa điểm từng tiêu chí<br>3. Kiểm tổng điểm<br>4. Lưu rubric | `guides/08-gv-rubric.mp4` · `.srt` |
| 09 | Hướng dẫn · Chấm bài từ hàng chờ | Giáo viên | 0:45 | 1. Mở Hàng chờ<br>2. Mở bài làm<br>3. Nhập điểm từng tiêu chí<br>4. Nhận xét và duyệt | `guides/09-gv-cham-bai.mp4` · `.srt` |
| 10 | Hướng dẫn · Nhận điểm AI đề xuất | Giáo viên | 0:47 | 1. Mở bài có đề xuất của AI<br>2. Xem vùng AI dựa vào<br>3. Nhận, sửa hoặc bỏ<br>4. Duyệt điểm | `guides/10-gv-nhan-ai-de-xuat-diem.mp4` · `.srt` |
| 11 | Hướng dẫn · Xem bài viết tay và lỗi chung của lớp | Giáo viên | 0:44 | 1. Mở trang Lỗi của lớp<br>2. Mở bằng chứng<br>3. Gợi ý bộ luyện nhắc lại | `guides/11-gv-xem-bai-viet-tay.mp4` · `.srt` |
| 12 | Hướng dẫn · Trình chiếu lời giải trên lớp | Giáo viên | 0:37 | 1. Mở chế độ trình chiếu<br>2. Mở từng bước<br>3. Dùng phím mũi tên | `guides/12-gv-trinh-chieu.mp4` · `.srt` |
| 13 | Hướng dẫn · Vào lớp bằng mã lớp | Học sinh | 0:32 | 1. Mở trang Vào lớp<br>2. Nhập tên và mã lớp<br>3. Xem Bài của em | `guides/13-hs-vao-lop.mp4` · `.srt` |
| 14 | Hướng dẫn · Làm bài trên hình 3D: xoay, vẽ thêm, đo | Học sinh | 0:54 | 1. Mở bài<br>2. Xoay hình<br>3. Vẽ thêm đoạn<br>4. Đo độ dài<br>5. Đo góc | `guides/14-hs-lam-bai-3d.mp4` · `.srt` |
| 15 | Hướng dẫn · Xem gợi ý và lời giải | Học sinh | 0:42 | 1. Mở thẻ Gợi ý<br>2. Gợi ý một: hướng nghĩ<br>3. Gợi ý hai: bước kế tiếp<br>4. Lời giải theo giờ mở | `guides/15-hs-goi-y-loi-giai.mp4` · `.srt` |
| 16 | Hướng dẫn · Hỏi AI, đọc nguồn | Học sinh | 0:43 | 1. Mở Hỏi đáp<br>2. Gõ câu hỏi và gửi<br>3. Đọc câu trả lời<br>4. Bấm nguồn | `guides/16-hs-hoi-ai.mp4` · `.srt` |
| 17 | Hướng dẫn · Chụp và nộp bài viết tay | Học sinh | 0:43 | 1. Mở bài có chấm<br>2. Chụp trang một<br>3. Chụp các trang tiếp<br>4. Nộp bài | `guides/17-hs-nop-bai-viet-tay.mp4` · `.srt` |

## Chi tiết từng video (để đặt vào trang trợ giúp)

### 01 · Tạo đề và dựng hình 3D (Giáo viên)

Hướng dẫn giáo viên tạo một đề hình không gian mới: gõ đề, để hệ thống dựng hình 3D, giải từng bước và kiểm chứng đáp số.

1. **Mở trang Thêm đề** — Ở thanh bên trái, chọn Đề mới. Trang Thêm đề mở ra ở thẻ Dán chữ.
2. **Gõ hoặc dán đề bài** — Gõ hoặc dán đề vào ô Đề đã chép lại. Ký hiệu toán như vuông góc hay căn bậc hai được giữ nguyên, và bản nháp tự lưu.
3. **Bấm Dựng hình** — Bấm Dựng hình. Hệ thống chạy bốn bước: đọc đề, dựng hình, giải, và kiểm chứng. Thầy cô theo dõi tiến trình ngay trên màn hình.
4. **Soát hình, lời giải và nhãn kiểm chứng** — Soát hình, lời giải nháp và nhãn. Nhãn Đã kiểm chứng nghĩa là máy đã tính lại đáp số. Nhãn AI soạn nhắc rằng lời giải cần giáo viên duyệt.

**Mẹo:** Đề càng đủ dữ kiện (cạnh, góc, quan hệ vuông góc) thì hình càng chính xác. · Nếu máy không dựng được, chọn Bổ sung điều kiện hoặc Dựng tay. · Đề chỉ giao được khi đã duyệt lời giải.

### 02 · Giao bài cho lớp (Giáo viên)

Hướng dẫn giáo viên giao một bài trong kho đề cho lớp, kèm quyền gợi ý và giờ mở lời giải.

1. **Mở đề cần giao** — Từ kho đề, mở đề cần giao rồi bấm Giao bài. Trang giao bài hiện tên đề và hình thu nhỏ.
2. **Chọn lớp** — Chọn lớp nhận bài. Có thể chọn nhiều lớp cùng lúc.
3. **Đặt gợi ý và giờ mở lời giải** — Đặt quyền xem gợi ý, quyền hỏi AI, và giờ mở lời giải. Trước giờ này, học sinh không xem được lời giải, kể cả khi hỏi AI.
4. **Bấm Giao bài** — Bấm Giao bài. Hệ thống xác nhận đã giao, và học sinh thấy bài trong mục Bài của em.

**Mẹo:** Đặt giờ mở lời giải sau buổi học để học sinh tự nghĩ trước. · Dùng Xem trước để thấy đúng giao diện học sinh.

### 03 · Theo dõi lớp khi học sinh làm bài (Giáo viên)

Hướng dẫn giáo viên theo dõi cả lớp theo thời gian thực trong lúc học sinh làm bài.

1. **Mở trang Theo dõi** — Mở mục Theo dõi và chọn bài đã giao. Trang theo dõi của bài mở ra.
2. **Đọc bốn thẻ tổng hợp** — Đọc bốn thẻ tổng hợp: số em đã mở bài, đã dùng gợi ý, đã hỏi AI, và thời gian thao tác trên hình.
3. **Xem từng học sinh** — Kéo xuống bảng từng học sinh: trạng thái, thời gian, và bậc gợi ý đã dùng. Không có cột xếp hạng.

**Mẹo:** Em nào dùng đến gợi ý bậc hai mà chưa xong là dấu hiệu cần hỗ trợ. · Trang tự cập nhật, không cần tải lại.

### 04 · Soạn đề bằng AI từ tài liệu (Giáo viên)

Hướng dẫn giáo viên nhờ AI soạn đề nháp từ một tài liệu đang dùng của lớp, rồi duyệt từng câu.

1. **Mở tài liệu nguồn** — Từ kho tri thức, mở tài liệu đang dùng và chọn Soạn đề nháp. Trang hiện tài liệu nguồn và phiên bản.
2. **Bấm soạn đề nháp** — Bấm soạn đề nháp. AI đọc tài liệu và đề xuất các câu hỏi bám nội dung.
3. **Xem nhãn kiểm chứng** — Câu nào máy dựng được hình có đáp số đã tính lại. Câu chưa kiểm chứng xếp cuối và không được chọn sẵn.
4. **Chọn và duyệt** — Đánh dấu các câu muốn giữ rồi bấm Duyệt. Chỉ câu đã duyệt mới vào ngân hàng đề của thầy cô.

**Mẹo:** Tài liệu càng rõ ràng, câu nháp càng sát. · Có thể sửa từng câu trước khi duyệt.

### 05 · Ra đề theo ma trận, nhiều mã đề (Giáo viên)

Hướng dẫn giáo viên ra một đề kiểm tra theo ma trận chuẩn, sinh nhiều mã đề từ ngân hàng đã kiểm chứng.

1. **Mở Ra đề** — Chọn Ra đề ở thanh bên trái.
2. **Chọn loại đề** — Chọn loại đề, ví dụ kiểm tra mười lăm phút. Số câu trắc nghiệm, tự luận và tổng điểm được điền sẵn theo ma trận.
3. **Xem bộ đề và mã đề** — Mở bộ đề vừa sinh. Mỗi câu có hai nhãn: máy kiểm chứng và người duyệt. Bấm từng mã đề để xem.
4. **Đổi một câu** — Muốn đổi câu nào, bấm Thay. Câu mới giữ nguyên chủ đề, dạng, mức độ và điểm.

**Mẹo:** Nên sinh bốn mã đề cho lớp đông. · Xuất đề ra tệp để in sau khi duyệt.

### 06 · Soạn giáo án theo PPCT (Giáo viên)

Hướng dẫn giáo viên soạn giáo án một tiết theo phân phối chương trình, với bản nháp do AI soạn.

1. **Mở Giáo án mới** — Chọn Giáo án, rồi Soạn giáo án mới.
2. **Chọn chương, bài, tiết** — Chọn chương, bài và tiết theo phân phối chương trình. Số tiết và kiến thức trọng tâm tự điền.
3. **Chọn đối tượng, bấm soạn** — Chọn đối tượng học sinh và phong cách dạy, rồi bấm soạn. Bản nháp có nhãn AI soạn.
4. **Đọc và sửa từng mục** — Đọc mục tiêu, thiết bị, tiến trình và phiếu học tập. Bấm chọn để sửa ở mục nào cần chỉnh, hoặc dùng ô chat sửa giáo án.
5. **Duyệt và tải về** — Khi đã ưng, bấm Duyệt giáo án, rồi tải Word hoặc PDF.

**Mẹo:** Khóa các mục đã ưng để AI không sửa đè. · Hoàn tác được về phiên bản trước.

### 07 · Quản lý kho tri thức của lớp (Giáo viên)

Hướng dẫn giáo viên quản lý kho tri thức: nơi chứa tài liệu mà AI được phép dùng để trả lời học sinh.

1. **Mở Kho tri thức** — Chọn Kho tri thức ở thanh bên trái.
2. **Xem tài liệu và phiên bản** — Mỗi tài liệu có phiên bản và trạng thái: đang dùng, chờ duyệt, hoặc đã lưu trữ. AI chỉ dùng bản đang dùng.
3. **Duyệt hoặc lưu trữ** — Duyệt bản mới để thay bản cũ, hoặc lưu trữ tài liệu không còn dùng. Học sinh luôn thấy nguồn đúng phiên bản.

**Mẹo:** Mỗi lớp có thể có tài liệu riêng. · Câu trả lời của AI luôn ghi tài liệu và mục được trích.

### 08 · Tạo rubric chấm điểm (Giáo viên)

Hướng dẫn giáo viên tạo và chỉnh rubric chấm điểm cho bài tự luận.

1. **Mở Rubric** — Chọn Rubric và mở rubric cần chỉnh, hoặc tạo mới từ mẫu.
2. **Sửa điểm từng tiêu chí** — Sửa tên và điểm tối đa của từng tiêu chí. Tổng điểm hiển thị ngay.
3. **Kiểm tổng điểm** — Nếu tổng không bằng mười, hệ thống cảnh báo và chặn lưu. Chỉnh lại cho đủ mười.
4. **Lưu rubric** — Lưu rubric. Mỗi lần sửa tạo phiên bản mới, bài đã chấm giữ phiên bản cũ.

**Mẹo:** Mô tả tiêu chí rõ ràng giúp AI đề xuất điểm sát hơn. · Dùng mẫu Tính toán cho bài hình.

### 09 · Chấm bài từ hàng chờ (Giáo viên)

Hướng dẫn giáo viên chấm bài viết tay của học sinh từ hàng chờ, theo rubric.

1. **Mở Hàng chờ** — Mở Hàng chờ, chọn lớp và bài có chấm. Bài cần chú ý như ảnh mờ hay nộp muộn nằm trên cùng.
2. **Mở bài làm** — Mở một bài. Ảnh các trang ở bên trái, rubric ở bên phải.
3. **Nhập điểm từng tiêu chí** — Nhập điểm từng tiêu chí. Tổng điểm tự cộng.
4. **Nhận xét và duyệt** — Viết nhận xét ngắn rồi bấm Duyệt, hoặc nhấn phím A. Bài tiếp theo trong hàng chờ tự mở.

**Mẹo:** Phím tắt: A duyệt, Enter sang tiêu chí kế. · Học sinh chỉ thấy điểm khi thầy cô trả bài.

### 10 · Nhận điểm AI đề xuất (Giáo viên)

Hướng dẫn giáo viên xem điểm AI đề xuất cho bài viết tay, rồi nhận, sửa hoặc bỏ từng tiêu chí.

1. **Mở bài có đề xuất của AI** — Mở bài có nhãn AI đã đề xuất. Mỗi tiêu chí có điểm AI và độ tin cậy.
2. **Xem vùng AI dựa vào** — Bấm tên một tiêu chí để thấy vùng trên trang bài làm mà AI đã dựa vào.
3. **Nhận, sửa hoặc bỏ** — Với từng tiêu chí, chọn Nhận, Sửa hoặc Bỏ. Nút Duyệt chỉ mở khi mọi tiêu chí đã được xử lý.
4. **Duyệt điểm** — Bấm Lưu và duyệt. Điểm cuối cùng là điểm của giáo viên.

**Mẹo:** Tin cậy thấp thì xem kỹ vùng ảnh trước khi nhận. · AI chấm chỉ bật cho lớp khi thầy cô cho phép.

### 11 · Xem bài viết tay và lỗi chung của lớp (Giáo viên)

Hướng dẫn giáo viên xem bài viết tay đã nhận dạng, và trang tổng hợp lỗi sai chung của lớp.

1. **Mở trang Lỗi của lớp** — Từ bài có chấm, mở Lỗi của lớp. Mỗi thẻ là một lỗi chung, kèm số bài có lỗi đó.
2. **Mở bằng chứng** — Bấm vào bằng chứng để mở đúng bài viết tay của học sinh, với ảnh các trang và phần chữ đã đọc.
3. **Gợi ý bộ luyện nhắc lại** — Quay lại và bấm Nhắc lại. Hệ thống gợi ý tối đa ba câu luyện cùng chủ đề và mục tài liệu liên quan.

**Mẹo:** Không có xếp hạng hay phần trăm theo học sinh. · Dùng lỗi chung để chọn nội dung chữa bài đầu giờ.

### 12 · Trình chiếu lời giải trên lớp (Giáo viên)

Hướng dẫn giáo viên trình chiếu một bài trên lớp: hình lớn, lời giải mở từng bước.

1. **Mở chế độ trình chiếu** — Từ đề, chọn Trình chiếu. Màn hình hiện hình lớn, lời giải còn che.
2. **Mở từng bước** — Bấm Bước tiếp để mở bước một. Hình tô sáng đúng các yếu tố bước đó dùng.
3. **Dùng phím mũi tên** — Dùng phím mũi tên phải để mở bước kế, mũi tên trái để quay lại.

**Mẹo:** Hỏi lớp dự đoán trước khi mở bước kế. · Xoay hình trực tiếp trên màn chiếu.

### 13 · Vào lớp bằng mã lớp (Học sinh)

Hướng dẫn học sinh vào lớp lần đầu, chỉ cần tên và mã lớp.

1. **Mở trang Vào lớp** — Mở trang Vào lớp từ đường dẫn thầy cô gửi.
2. **Nhập tên và mã lớp** — Nhập tên hiển thị và mã lớp sáu ký tự, rồi bấm Vào lớp.
3. **Xem Bài của em** — Mục Bài của em hiện mọi bài thầy cô đã giao.

**Mẹo:** Không cần email hay số điện thoại. · Hỏi thầy cô nếu mã lớp báo không đúng.

### 14 · Làm bài trên hình 3D: xoay, vẽ thêm, đo (Học sinh)

Hướng dẫn học sinh làm bài trên hình 3D: xoay hình, vẽ thêm đoạn, và đo độ dài, góc.

1. **Mở bài** — Mở bài từ Bài của em. Đề ở bên trái, hình 3D ở giữa.
2. **Xoay hình** — Kéo chuột hoặc ngón tay để xoay hình, nhìn các quan hệ vuông góc từ nhiều phía.
3. **Vẽ thêm đoạn** — Chọn công cụ Đường, rồi chạm hai điểm để nối thêm một đoạn.
4. **Đo độ dài** — Chọn Đo dài, rồi chạm hai điểm. Kết quả hiện ngay trên hình.
5. **Đo góc** — Chọn Đo góc, rồi chạm ba điểm. Đại lượng là đáp số thì được khoá cho đến giờ mở lời giải.

**Mẹo:** Bấm Xoay tự do để hình tự quay. · Đặt lại để về góc nhìn ban đầu.

### 15 · Xem gợi ý và lời giải (Học sinh)

Hướng dẫn học sinh dùng gợi ý từng bậc và xem lời giải khi được mở.

1. **Mở thẻ Gợi ý** — Trong bài, mở thẻ Gợi ý.
2. **Gợi ý một: hướng nghĩ** — Bấm mở gợi ý một. Gợi ý này chỉ hướng nghĩ, không có đáp số.
3. **Gợi ý hai: bước kế tiếp** — Nếu vẫn vướng, mở gợi ý hai để biết bước kế tiếp.
4. **Lời giải theo giờ mở** — Thẻ Lời giải hiện giờ mở do thầy cô đặt. Khi đến giờ, em xem lời giải từng bước, mỗi bước có câu vì sao.

**Mẹo:** Thử tự làm ít nhất năm phút trước khi mở gợi ý. · Thầy cô thấy em đã dùng gợi ý bậc nào để hỗ trợ.

### 16 · Hỏi AI, đọc nguồn (Học sinh)

Hướng dẫn học sinh hỏi AI về khái niệm trong bài, và đọc nguồn mà AI trích.

1. **Mở Hỏi đáp** — Mở Hỏi đáp từ bài đang làm. Phạm vi hỏi theo mức hỗ trợ thầy cô cho phép.
2. **Gõ câu hỏi và gửi** — Gõ câu hỏi về khái niệm hoặc cách nghĩ, rồi bấm Gửi.
3. **Đọc câu trả lời** — AI giải thích mà không làm hộ bài, và ghi rõ nguồn.
4. **Bấm nguồn** — Bấm vào nguồn để mở đúng đoạn trong tài liệu của lớp và đọc lại.

**Mẹo:** Hỏi vì sao thay vì hỏi đáp số. · AI không nêu đáp số trước giờ mở lời giải.

### 17 · Chụp và nộp bài viết tay (Học sinh)

Hướng dẫn học sinh chụp ảnh bài làm trên giấy và nộp cho thầy cô chấm.

1. **Mở bài có chấm** — Mở bài có chấm trong mục Nộp bài. Hạn nộp hiện ngay đầu trang.
2. **Chụp trang một** — Bấm Bắt đầu, chụp trang một. Ảnh được kiểm tra độ nét; ảnh đạt thì bấm Dùng ảnh này.
3. **Chụp các trang tiếp** — Chụp lần lượt các trang tiếp theo. Có thể đổi thứ tự trang nếu cần.
4. **Nộp bài** — Bấm Nộp bài. Em thấy xác nhận kèm giờ của máy chủ.

**Mẹo:** Chụp nơi đủ sáng, đặt giấy phẳng, không để bóng tay. · Ảnh mờ sẽ bị báo để chụp lại.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu: 17 video (12 GV, 5 HS) | runner-T-261128-77 |
