# Sổ quyết định của PO

Tài liệu này ghi các quyết định đang có hiệu lực để mọi người và mọi agent làm theo cùng một nguồn. Theo skill po-ba-qc, có hai loại:

- **Final (chủ đầu tư):** chủ đầu tư đã nói rõ. PO không được đổi.
- **Giả định (PO):** nguồn chưa trả lời, PO đặt mặc định để công việc không dừng. Mọi thứ dựng trên đó là tạm thời cho tới khi chủ đầu tư xác nhận. Giả định không bao giờ được coi là đã chốt. Danh sách chờ xác nhận nằm ở `questions/Q_ChuDauTu.md`.

## Nguồn yêu cầu có hiệu lực
1. `PRODUCT-MASTER.md` và `MVP.md`
2. `STAGES-AFTER-MVP.md` (các chặng 2 đến 9)
3. Tài liệu này

Khi các nguồn mâu thuẫn, tài liệu này thắng, rồi tới `PRODUCT-MASTER.md` và `MVP.md`, rồi mới tới `STAGES-AFTER-MVP.md` (xem PO-19). Không ai được tự thêm tính năng, quy tắc hay số liệu ngoài ba nguồn trên. Chi tiết nào chưa có trong nguồn phải được nêu ra để PO quyết, không tự đưa vào như yêu cầu đã có.

## Final từ chủ đầu tư (đã ghi ở các tài liệu khác, nhắc lại để đủ)
- Bắt đầu bằng một môn: Hình học; MVP chỉ làm hình học không gian, cho cả THCS và THPT.
- Gợi ý trước; lời giải chỉ hiện từ thời điểm giáo viên thiết lập.
- MVP có vai giáo viên dùng dữ liệu mẫu, kho đề soạn sẵn, lời giải AI đứng tên một giáo viên mẫu.
- Rời runtime artifact, làm web app riêng.
- Tám file concept là tầm nhìn dài hạn; lộ trình không ghi thời gian.

## Quyết định

| Mã | Trạng thái | Quyết định | Lý do ngắn |
|---|---|---|---|
| PO-01 | Final (chủ đầu tư) | Thiết bị theo vai: học sinh iPad hoặc iPhone; phụ huynh iPhone (hoặc iPad); giáo viên web trên máy tính, kiểm tra nhanh bằng điện thoại; quản trị trường và cơ quan quản lý dùng web. | Chủ đầu tư đã nêu. |
| PO-02 | Giả định (PO) | Nhãn tin cậy có hai trục, dùng thống nhất ở mọi chặng. Máy: "Đã kiểm chứng" / "Cần xem lại". Người: "AI soạn" / "Giáo viên đã duyệt". Giáo viên không tự gắn được nhãn của máy. | Người dùng phân biệt được "máy đã tính lại" với "có người chịu trách nhiệm". |
| PO-03 | Giả định (PO) | Điểm AI luôn là đề xuất. Chỉ giáo viên duyệt mới thành điểm chính thức. Không có tự duyệt, kể cả khi AI "chắc chắn" hay khi trường yêu cầu. | Nguyên tắc giáo viên quyết định cuối. |
| PO-04 | Giả định (PO) | Không có bất kỳ dạng xếp hạng nào: học sinh, lớp, giáo viên, trường. Màn kết quả lớp trả lời "cả lớp vướng ở đâu". | Tránh gắn nhãn và áp lực; đúng nguyên tắc dựa trên bằng chứng. |
| PO-05 | Giả định (PO) | Giáo viên thật ở chặng 2 vào bằng lời mời. Đăng ký và xác nhận tài khoản thuộc chặng 3. Chỗ `MVP.md` ghi "đăng ký giáo viên" ngay sau MVP được hiểu theo quyết định này. | Gỡ mâu thuẫn giữa `MVP.md` và `PRODUCT-MASTER.md`. |
| PO-06 | Giả định (PO) | Môn học thứ hai là "Hàm số và đồ thị". | Chỉ đổi một biến so với Hình học: cùng giáo viên Toán, cùng cách kiểm chứng. |
| PO-07 | Giả định (PO) | Bài tự học (không có giáo viên giao): lời giải mở sau khi học sinh đã xem gợi ý 1 và 2. | Giữ đúng "gợi ý trước" khi không có giáo viên đặt giờ. |
| PO-08 | Giả định (PO) | Trước giờ mở lời giải, công cụ đo không hiện giá trị của chính đại lượng mà đề bài hỏi. Học sinh vẫn đo được các yếu tố trung gian. | Công cụ đo không được thành đường tắt để lấy đáp số. |
| PO-09 | Giả định (PO) | Phụ huynh chỉ thấy thông tin đã được giáo viên duyệt. Không xem nội dung từng câu con hỏi, không nhận báo tức thời, không thấy "số ngày học liên tiếp". | Hỗ trợ con mà không tạo áp lực hay giám sát. |
| PO-10 | Giả định (PO) | Điểm danh, sổ điểm chính thức và tài chính không thuộc chặng 8. Màn hình "điểm danh nhanh" nếu đã vẽ thì gắn nhãn "Để sau", không tính vào phạm vi. | Trường đã có hệ thống cho các việc này; không cạnh tranh ở đó. |
| PO-11 | Giả định (PO) | Video nhóm không thuộc phạm vi đầu của chặng 7. Thứ tự chặng 7: giáo viên chia sẻ tài nguyên → nhóm trong lớp → trao đổi sản phẩm giữa hai lớp → nhóm liên trường. Màn hình video nếu đã vẽ thì gắn nhãn "Để sau". | Rủi ro an toàn trẻ em cao nhất nằm ở video và liên trường. |
| PO-12 | Giả định (PO) | Sản phẩm chạy dạng web trên trình duyệt của iPad và iPhone cho tới hết chặng 4. Xét ứng dụng cài đặt khi tới chặng 5 và 6 (chụp bài, thông báo). Cần xác minh giới hạn kỹ thuật trước khi vào chặng 5. | Ra sản phẩm sớm, không phụ thuộc duyệt kho ứng dụng. |
| PO-13 | Giả định (PO) | Sau MVP, làm kỹ chặng 2 và 3 như một khối. Ba màn hình ưu tiên: soát và duyệt lời giải, kết quả lớp, bài luyện. | Hai chặng này trả lời câu MVP chưa trả lời được: giáo viên thật có dùng lặp lại không. |
| PO-14 | Giả định (PO) | Dữ liệu mẫu dùng chung: lớp 11A1 và 9B; học sinh An, Bình, Chi, Dũng, Hà (thêm Lan, Khoa, Vy cho nhóm liên trường); "Giáo viên Toán · 11A1"; trường "THPT Mẫu A", "THPT Mẫu B", "THCS Mẫu C". Không dùng tên người hay tổ chức thật. | Các bộ màn hình khớp nhau. |
| PO-15 | Final (chủ đầu tư) | Mọi màn hình ngoài MVP có nhãn "Concept · Chặng N". Tài liệu và lộ trình không ghi mốc thời gian, chỉ ghi thứ tự. | Chủ đầu tư đã yêu cầu. |

| PO-16 | Giả định (PO) | Đề học sinh tự nhập mà trùng hoặc gần trùng với một bài đang khóa của lớp mình thì kế thừa khóa của bài đó: lời giải mở theo giờ giáo viên đặt, không theo quy tắc tự học. Bộ câu thử có thêm ca "dán lại đề đang khóa vào tự học". | RV-01: tự học không được là đường vòng qua giờ mở lời giải. |
| PO-17 | Giả định (PO) | PO-08 áp dụng cho mọi nơi có ô đáp số hoặc lời giải đang khóa: bài luyện có chấm, bài hàm số (thanh trượt không tự hiện đại lượng đề hỏi), phòng học nhóm. | RV-02: công cụ đo không được thành đường tắt ở các chặng sau. |
| PO-18 | Giả định (PO) | Chỉ giáo viên được mời (chặng 2) hoặc đã được xác nhận (chặng 3) mới dùng được "Thêm đề" và xem lời giải AI. Tài khoản đang chờ xác nhận chỉ xem danh mục đề, không xem lời giải. Bộ câu thử có ca "giả làm giáo viên". | RV-07: luồng thêm đề không được thành máy giải bài cho người ngoài. |
| PO-19 | Giả định (PO) | Thứ tự nguồn: tài liệu này → `PRODUCT-MASTER.md` và `MVP.md` → `STAGES-AFTER-MVP.md`. Trong `STAGES-AFTER-MVP.md`, chỉ nội dung dẫn được về hai nguồn trên mới là yêu cầu; các quy tắc ở mục 7 của từng chặng chưa gắn nguồn là "đề xuất chưa duyệt". | RV-08, RV-16: quy tắc do agent viết không tự thành yêu cầu. |
| PO-20 | Giả định (PO) | Phụ huynh luôn phải được trường xác minh trước khi thấy dữ liệu của con; không có ngoại lệ theo chính sách trường. Mục "Ở nhà giúp con" do AI soạn nháp, giáo viên duyệt mới hiện. | RV-05; `PRODUCT-MASTER.md` C5 và PO-09. |
| PO-21 | Giả định (PO) | Tài khoản học sinh chính thức (chặng 3) và việc thu ảnh bài làm (chặng 5) chưa được viết story hay thiết kế chi tiết cho tới khi chủ đầu tư trả lời về cơ chế đồng thuận của cha mẹ (Q2). | RV-06: không thu dữ liệu trẻ em trước khi có cơ chế đồng thuận. |
| PO-22 | Giả định (PO) | Giờ mở lời giải áp dụng cho mọi kênh: trả bài từng em không kèm lời giải trước giờ mở chung; link chia sẻ, bản PDF hay ảnh xuất ra, màn trình chiếu cho lớp khác và tài khoản phụ huynh đều tuân theo. | RV-11, RV-12, RV-29: chặn các kênh phụ làm lộ lời giải. |
| PO-23 | Giả định (PO) | PO-09 là tuyệt đối: không có công tắc nào, kể cả ở chính sách của trường, cho phụ huynh xem nội dung câu con hỏi. | RV-21: "mặc định tắt" vẫn là có thể bật. |
| PO-24 | Giả định (PO) | Quản trị trường chỉ thấy so sánh AI và giáo viên ở mức gộp theo tổ bộ môn, không theo từng giáo viên. Đường cho cơ quan quản lý xem dữ liệu định danh của học sinh tạm đóng: không thiết kế, không viết story cho tới khi có câu trả lời pháp lý. | RV-13, RV-27, RV-28: tránh xếp hạng giáo viên trá hình và lối vào dữ liệu cá nhân chưa có người duyệt. |
| PO-25 | Giả định (PO) | Kéo mặt phẳng và thiết diện động thuộc chặng 2, mức P1. MVP giao từng đề; giao theo bộ thuộc chặng 3. Chỉ giáo viên dạy lớp đó duyệt điểm; chấm chéo để sau. | RV-15, RV-20, RV-33: gỡ ba chỗ các tài liệu gắn chặng khác nhau. |
| PO-26 | Giả định (PO) | Thời khóa biểu không thuộc phạm vi đầu của chặng 8; màn hình đã vẽ gắn nhãn "Để sau". | Cùng lý do với PO-10: trường đã có hệ thống cho việc này. |
| PO-27 | Final (chủ đầu tư) | Giáo viên có bước soạn giáo án và đưa tài liệu lên hệ thống. Ba cách đưa vào: chat với AI để soạn; import tài liệu (docx, xlsx, pdf); chụp hoặc chọn nhiều ảnh để hệ thống đọc chữ (OCR). Kết quả là bài tập có gợi ý và lời giải theo đúng định dạng giáo viên yêu cầu, có bước xem trước để giáo viên kiểm tra, và có chatbot AI để sửa tiếp sau khi import. | Chủ đầu tư yêu cầu ngày 22/11/2026. |
| PO-28 | Final (chủ đầu tư) | Khi soạn với AI, giáo viên chỉ cần định hướng: hướng lời giải, loại bài tập, mức chi tiết. AI soạn bài tập, gợi ý, lời giải; giáo viên lưu vào giáo trình. | Chủ đầu tư yêu cầu: giảm công soạn bài cho giáo viên. |
| PO-29 | Final (chủ đầu tư) | Từ giáo trình chung của lớp, tạo được giáo trình riêng cho từng học sinh dựa trên điểm mạnh và phần còn yếu của em đó. | Chủ đầu tư yêu cầu. |
| PO-30 | Giả định (PO) | Mọi nội dung AI soạn hoặc đọc từ tài liệu, ảnh mang nhãn hai trục của PO-02 và chỉ vào giáo trình sau khi giáo viên bấm duyệt. Chỗ OCR chưa chắc được đánh dấu để giáo viên sửa. Chỉ giáo viên được mời hoặc đã xác nhận dùng được các bước này (PO-18). | Giữ nguyên tắc giáo viên quyết định cuối và kết quả AI phải qua kiểm chứng. |
| PO-31 | Giả định (PO) | Giáo trình riêng do AI đề xuất kèm bằng chứng (bài nào, câu hỏi nào); giáo viên duyệt mới áp dụng cho học sinh. Cách diễn đạt là "đã vững" và "đang luyện", không gắn nhãn, không xếp hạng (PO-04). Học sinh thấy lộ trình của mình; phụ huynh chỉ thấy phần giáo viên đã duyệt (PO-09). | Khớp nguyên tắc dựa trên bằng chứng và không gắn nhãn. |
| PO-32 | Giả định (PO) | Vị trí trong lộ trình: soạn với AI và import thuộc chặng 2 (thêm đề) và chặng 5 (trợ lý giáo viên, kho tri thức); giáo trình riêng thuộc chặng 6 (hồ sơ học tập). Trong prototype các màn này mang nhãn Concept. Tài liệu mẫu của giáo viên nằm ở thư mục `materials/`. | Không đổi phạm vi MVP đã chốt; chủ đầu tư có thể kéo sớm. |
| PO-33 | Final (chủ đầu tư) | Khi học sinh hỏi AI, câu trả lời dựa trên giáo trình của giáo viên (kho tri thức) và ghi rõ lấy từ đâu. | Chủ đầu tư yêu cầu ngày 22/11/2026. |
| PO-34 | Final (chủ đầu tư) | Trong gợi ý, lời giải và câu trả lời có các từ khóa bấm được; bấm vào mở bài giảng để đọc lại. Bài giảng lấy từ bài soạn của giáo viên, hoặc do AI viết khi giáo viên chưa có; mỗi bài giảng ghi rõ nguồn. | Chủ đầu tư yêu cầu. |
| PO-35 | Final (chủ đầu tư) | Hình không gian tương tác được: xoay, vẽ thêm (điểm, đoạn, mặt phẳng), mở rộng mặt và kéo dài cạnh, để học sinh dễ hình dung. | Chủ đầu tư yêu cầu. |
| PO-36 | Final (chủ đầu tư) | Khi học sinh xem lời giải, phần vẽ thêm của từng bước lần lượt được thêm vào hình. | Chủ đầu tư yêu cầu. |
| PO-37 | Giả định (PO) | Hỏi đáp theo giáo trình vẫn tuân giờ mở lời giải (PO-22): trước giờ mở, câu trả lời chỉ dẫn tới khái niệm và bài giảng, không nêu đáp số. Bài giảng do AI viết mang nhãn "AI soạn" cho tới khi giáo viên duyệt (PO-02). Công cụ trên hình vẫn theo PO-08. | Giữ các nguyên tắc đã có khi thêm tính năng mới. |
| PO-38 | Giả định (PO) | Bài từ kho đề khi được giáo viên đưa vào giáo trình thì mang nhãn "Giáo viên đã duyệt". Nhãn máy "Cần xem lại" chỉ đổi khi máy kiểm lại đạt, không đổi vì giáo viên xác nhận chữ OCR; còn ô OCR chưa xác nhận thì vẫn lưu được. | Trả lời câu hỏi của agent PROTO-CURR; giữ hai trục nhãn độc lập (PO-02). |
| PO-39 | Giả định (PO) | Sau MVP, giáo viên được thêm tối đa một mức gợi ý nữa cho một bài (tổng ba mức gợi ý trước lời giải). "Gợi ý kỹ hơn" trong giáo trình riêng nghĩa là chia gợi ý 2 thành các bước nhỏ hơn. | MVP giữ hai mức; các chặng sau cho giáo viên linh hoạt. |
| PO-40 | Giả định (PO) | Bài thêm vào giáo trình riêng chỉ lấy từ giáo trình chung hoặc kho đề đã được giáo viên duyệt. Học sinh thấy "đã vững" và "đang luyện" trên lộ trình của mình; phụ huynh thấy tên phần đang luyện trong dòng tóm tắt đã duyệt. | Không có nội dung chưa duyệt tới học sinh; khớp PO-09 và PO-31. |
| PO-41 | Giả định (PO) | Chỉnh sửa bài trong giáo trình (gợi ý, hướng lời giải) phải theo sang màn giao bài và bài học sinh làm. Mục "Giáo trình" đứng cạnh "Kho đề" trên thanh điều hướng của giáo viên. | Giáo trình là nguồn của bài được giao. |
| PO-42 | Giả định (PO) | Công cụ đo ẩn mọi đại lượng có cùng giá trị và cùng vai trò với đại lượng đề hỏi trong cùng một tam giác lời giải (ví dụ ở bài hình chóp, góc ASD cũng ẩn như góc SDA). | Bịt đường tắt mà agent FIG3D chỉ ra (PO-08). |
| PO-43 | Giả định (PO) | Bài giảng khái niệm do AI viết mà giáo viên chưa duyệt vẫn hiện cho học sinh và được trợ lý dùng làm nguồn, với nhãn rõ "AI soạn · chờ giáo viên duyệt". Quy tắc "chỉ nội dung đã duyệt mới tới học sinh" (PO-30, PO-40) áp dụng cho bài tập, lời giải và giáo trình riêng, không áp dụng cho bài giảng khái niệm. Giáo viên sửa được bài giảng trước khi duyệt. | Chủ đầu tư muốn có bài giảng của AI khi giáo viên chưa soạn (PO-34); nhãn giúp học sinh biết mức tin cậy. Cần chủ đầu tư xác nhận vì đây là chỗ hai nguyên tắc chạm nhau. |
| PO-44 | Giả định (PO) | Bài giảng được nêu công thức và phương pháp tổng quát trước giờ mở lời giải, nhưng không nêu đáp số hay các bước của bài đang làm. Khi từ chối nêu đáp số, trợ lý nhắc lại gợi ý ở mức học sinh đang có và không tính thêm lượt gợi ý. | Kiến thức chung không phải là lộ đáp án. |
| PO-45 | Final (chủ đầu tư) | Giáo viên soạn được cả bài tập lẫn bài giảng bằng cách trao đổi với AI, hoặc import từ file. Giáo viên chat với AI để chỉnh sửa, cập nhật bản nháp của AI. Phần trình chiếu phải thể hiện rõ ba việc này. | Chủ đầu tư yêu cầu ngày 22/11/2026. |
| PO-46 | Giả định (PO) | Cách thể hiện sự qua lại: giáo viên nêu định hướng; AI hỏi lại khi định hướng chưa rõ thay vì tự đoán; AI nói rõ đã đổi gì (trước và sau), dựa trên tài liệu nào và chỗ nào nó chưa chắc; giáo viên chấp nhận, sửa tay hoặc bác từng đề xuất, và hoàn tác được; AI không tự lưu, chỉ giáo viên bấm duyệt mới vào giáo trình. Khi import một file giáo án, hệ thống tách cả bài tập lẫn bài giảng (phần lý thuyết); giáo viên xem từng mục trước khi lưu. | Giữ nguyên tắc giáo viên dẫn dắt, AI hỗ trợ (PO-03, PO-30). |
| PO-47 | Giả định (PO) | Import file đối chiếu với kho bài giảng hiện có: mục trùng nội dung ghi "đã có trong kho", mục giáo viên đã sửa thì "cập nhật khi lưu"; không tạo bản trùng. Giáo viên soạn bài giảng với AI chỉ cho khái niệm giáo án chưa có; bài lưu thay bản AI tự viết đang chờ duyệt. | Trả lời câu hỏi 1, 2, 6 của PROTO-LECT; tránh trùng bài giảng. |
| PO-48 | Giả định (PO) | Giáo viên sửa tay làm đổi dữ kiện đề thì vẫn lưu được nhưng nhãn máy chuyển thành "Cần xem lại" cho tới khi máy kiểm lại đạt. Hộp bài giảng phía học sinh cần có cả nhãn máy khi bài giảng có dữ kiện số (PO-02). Nhóm Bài tập trong import có ô chọn từng bài như nhóm Bài giảng. | Trả lời câu hỏi 3, 4, 5 của PROTO-LECT. Chưa làm trong prototype. |

| PO-49 | Giả định (PO) | Vị trí trong lộ trình của các yêu cầu Final PO-33 và PO-34: từ khóa bấm được, bài giảng có ghi nguồn và hỏi đáp dựa trên giáo trình nằm trong MVP ở mức bài giảng và giáo trình mẫu do hệ thống cung cấp (nguồn là giáo án mẫu của Giáo viên mẫu hoặc AI viết, ghi rõ nhãn). Giáo viên tự soạn và duyệt bài giảng thuộc chặng 2. Kho tri thức do giáo viên tự quản lý, kiểm soát đầy đủ thuộc chặng 5. PO-35 và PO-36 (hình tương tác, lời giải cập nhật hình) thuộc MVP. | Yêu cầu Final chưa nêu chặng; bản hiện tại đã có thuật ngữ bấm được và chat nên đặt vào MVP là bước nhỏ nhất. |

Các mã PO-16 đến PO-26 được đặt sau đợt phản biện (`review/RV_findings.md`); cách xử lý từng phát hiện nằm ở `review/RV_triage.md`.

## Việc PO không tự quyết được, cần chủ đầu tư
1. Người trả tiền đầu tiên là ai (phụ huynh, giáo viên hay trung tâm, nhà trường).
2. Pháp nhân vận hành, nơi lưu dữ liệu, chính sách lưu giữ và đồng thuận cho dữ liệu trẻ em.
3. Trường pilot và mức cam kết của họ.
4. Tên sản phẩm khi mở môn thứ hai.
5. Ai soát kho đề trước khi học sinh thật dùng.
