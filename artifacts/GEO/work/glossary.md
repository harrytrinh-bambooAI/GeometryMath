# Glossary (Q3) — GEO

> Nguồn gốc cho mọi tệp khác (M19): story, test specification và bảng chung chép phần "Reader guide" ở cuối tệp này làm §0. Đổi một dòng định dạng là một CR chạy lan (M13). Chủ sở hữu của tệp: PO. Hiển thị dùng đúng "Canonical term"; không dùng "Banned synonyms" trong story, AC, case và thông báo.

| Canonical term | Meaning | Banned synonyms | Format / owner (M13) |
|---|---|---|---|
| Kho đề | tập đề soạn sẵn của Giáo viên mẫu, mỗi đề có hình, gợi ý, đáp số, lời giải từng bước (KNW-01) | ngân hàng đề, thư viện đề, kho bài tập | tên màn "Kho đề" · PO |
| Bài giao | một đề giao cho một lớp, có giờ mở lời giải (`Assignment`) | bài tập giao, assignment, nhiệm vụ | "Bài giao" · PO |
| Bài của em | màn học sinh liệt kê bài giao của lớp mình (LRN-01) | bài tập của tôi, danh sách bài | tên màn · PO |
| Giờ mở lời giải | thời điểm `release_at` mà từ đó học sinh được xem lời giải | thời điểm công bố, hạn, deadline, giờ công bố | lưu UTC; hiển thị "HH:mm, dd/MM/yyyy" giờ Việt Nam · PO (quy tắc), ARCH (lưu) |
| Chưa đặt giờ | bài giao chưa có `release_at`; lời giải vẫn khóa (BR-GEO-03) | chưa có hạn, để trống | trạng thái giờ mở · PO |
| Sắp mở | `release_at` còn ở tương lai so với giờ máy chủ | đang chờ, sắp công bố | trạng thái giờ mở · PO |
| Đã mở | giờ máy chủ ≥ `release_at`; lời giải được phép xem | đã công bố, hết khóa | trạng thái giờ mở · PO |
| Mở ngay | giáo viên đặt `release_at` bằng giờ máy chủ lúc bấm | mở sớm, công bố ngay | thao tác · PO |
| Khóa lời giải | máy chủ không trả lời giải và đáp số cho học sinh trước giờ mở (ANS-02) | ẩn lời giải, che lời giải | "Lời giải đang khóa" · PO |
| Kiểm soát đáp án | lớp kiểm tra ở máy chủ chặn lộ đáp số qua hỏi đáp (ANS-03) | bộ lọc, answer firewall (chỉ dùng trong tài liệu kỹ thuật) | tên module `answers` · PO |
| Thang gợi ý | mức 1 hướng nghĩ, mức 2 bước kế tiếp, mức 3 lời giải đầy đủ | cấp gợi ý, hint level | "Gợi ý 1", "Gợi ý 2", "Lời giải" · PO |
| Gợi ý 1 / Gợi ý 2 | hai mức gợi ý trước lời giải; mức 3 là lời giải | gợi ý cơ bản, gợi ý nâng cao | nhãn nút · PO |
| Lời giải | các bước, "Vì sao nghĩ ra bước này?", đáp số, nguyên lý | đáp án, solution | "Lời giải" · PO |
| Đáp số | giá trị kết quả của đại lượng mà đề hỏi | kết quả cuối, answer | số có đơn vị; thập phân dấu phẩy · PO |
| Đại lượng đề hỏi | đại lượng mà đề bài yêu cầu tính; công cụ đo ẩn nó trước giờ mở (BR-GEO-16) | đại lượng cần tìm, ẩn số | thuật ngữ · PO |
| Nhãn máy | "Đã kiểm chứng" hoặc "Cần xem lại" (PO-02) | đã duyệt, đúng, verified | hai chuỗi cố định · PO |
| Nhãn người | "AI soạn" hoặc "Giáo viên đã duyệt"; MVP chỉ có "AI soạn" | do AI viết, AI tạo | hai chuỗi cố định · PO |
| Đã kiểm chứng | hình thỏa ràng buộc và đáp số khớp kết quả tính, theo dung sai D-GEO-040 | đúng 100%, chuẩn, đã duyệt | nhãn máy · PO |
| Cần xem lại | mọi trường hợp còn lại, gồm đề lạ dùng tọa độ AI | sai, lỗi, chưa chắc | nhãn máy · PO |
| Kiểm chứng | so đáp số và ràng buộc của AI với kết quả tính từ tọa độ do engine dựng | xác minh, kiểm tra đúng sai | quy trình · PO |
| Ràng buộc | điều kiện hình học của đề, ví dụ "SA vuông góc với đáy" | điều kiện, giả thiết (chỉ dùng trong đề gốc) | trường `constraints` · ARCH |
| Hình mẫu | hình dựng sẵn có tham số, không cần AI (GEO-03) | template hình, hình có sẵn | danh sách D-GEO-056 · PO |
| Đề lạ | đề tự nhập không khớp hình mẫu; dùng tọa độ AI kèm cảnh báo chưa kiểm chứng | đề ngoài, đề lạ lẫm | thuật ngữ · PO |
| Tự học | học sinh làm đề tự nhập hoặc hình mẫu, không có giáo viên giao | luyện tự do, free mode | tên chế độ · PO |
| Theo dõi lớp | trang giáo viên xem ai đã mở bài, dùng gợi ý mức nào, hỏi gì (LRN-03) | báo cáo lớp, dashboard lớp | tên màn · PO |
| Xem trước như học sinh | giáo viên xem bài đúng như học sinh thấy (LRN-04) | preview, chế độ thử | tên thao tác · PO |
| Giáo viên mẫu | tài khoản giáo viên tạo sẵn, sở hữu kho đề và lớp mẫu | giáo viên thật, giáo viên demo | tên hiển thị "Giáo viên mẫu" · PO |
| Lớp mẫu | lớp tạo sẵn: 11A1 (THPT) và 9B (THCS), theo PO-14 | lớp thử, lớp demo | tên "11A1", "9B" · PO |
| Mã lớp | chuỗi học sinh nhập cùng tên hiển thị để vào lớp | mật khẩu, mã tham gia, PIN | duy nhất toàn hệ thống, D-GEO-051 · TechLead |
| Tên hiển thị | tên học sinh nhập khi đăng nhập nhẹ; không phải tên thật bắt buộc | họ tên, username | tối đa độ dài TBD · TechLead |
| Trường | đơn vị cách ly dữ liệu (`school_id`); không có màn quản trị trường trong MVP | tenant (chỉ dùng trong tài liệu kỹ thuật), org | `school_id` · ARCH |
| Từ khóa | cụm từ bấm được trong gợi ý, lời giải, câu trả lời, mở bài giảng (REQ-GEO-11) | thuật ngữ bấm được, link khái niệm | "Từ khóa" · PO |
| Bài giảng | đoạn lý thuyết ngắn mở từ từ khóa, ghi nguồn và nhãn người | bài học, tài liệu lý thuyết | "Bài giảng" · PO |
| Nguồn | nơi lấy bài giảng hoặc câu trả lời: "Giáo án mẫu" hoặc "AI soạn" | tham khảo, source | dòng "Nguồn: …" · PO |
| Hỏi đáp | khung trò chuyện với trợ lý trong ngữ cảnh bài (GEO-10) | chat, chatbot (chỉ trong tài liệu kỹ thuật) | tên tab "Hỏi đáp" · PO |
| Bộ đề chuẩn | đề có đáp số tham chiếu để đo tỉ lệ hình thỏa ràng buộc và đáp số đúng (TRU-01) | benchmark, test set | dùng chung với kho đề · PO |
| Bộ câu thử moi đáp án | câu hỏi cố ép trợ lý lộ đáp số trước giờ mở (TRU-02) | adversarial set, câu hỏi bẫy | tên bộ · PO |
| Lộ đáp số | câu trả lời hoặc dữ liệu trả về chứa đáp số, giá trị trung gian quyết định hoặc bước giải của lời giải đang khóa (D-GEO-065) | rò rỉ, leak | định nghĩa kiểm thử · PO |
| Phiên đang hoạt động | phiên có ít nhất một yêu cầu tới máy chủ trong 60 giây gần nhất (D-GEO-044) | người dùng online, user đồng thời | số đếm · PO |
| Phiên làm bài | một lần học sinh mở một bài cho tới khi 30 phút không có sự kiện (D-GEO-059) | session học | số đếm · PO |
| Thao tác trên hình | một lần dùng công cụ Chọn, Điểm, Đoạn, Mặt phẳng, Mở rộng, Đo dài, Đo góc; xoay và thu phóng không tính (D-GEO-058) | tương tác hình | số đếm · PO |
| Sự kiện tương tác | bản ghi `Interaction`: mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải | log, tracking | schema ARCH · ARCH |
| Concept · Chặng N | nhãn của màn hình ngoài MVP (PO-15) | bản nháp, demo | chuỗi cố định · PO |

## Reader guide and id glossary (M19 — copied verbatim as §0 of every delivered story and test specification; exported as the Glossary sheet)

**How to read a story file / a test specification:** Mỗi story nói một vai làm được gì và vì sao; tiêu chí chấp nhận (SC thành công, FC thất bại) nói một hành vi quan sát được với giá trị cụ thể. Mỗi test case trỏ về tiêu chí và dùng dữ liệu của hàng `F-GEO-nnn`. Người viết mã đọc story và tiêu chí; người kiểm thử đọc test specification; không cần mở tài liệu khác để hiểu một dòng.

Giá trị mặc định đã dùng khi nguồn chưa trả lời mang nhãn PROVISIONAL kèm mã `D-GEO-nnn` và nằm trong `questions/Q_<Tên>.md`. Mọi ngưỡng hiệu năng là đề xuất, chưa phải số đã chốt.

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-GEO-nnn` · `US-GEO-nnn-SCn` / `-FCn` · `BR-GEO-nn` · `F-GEO-nnn` | story (hàng trăm = epic) · tiêu chí thành công / thất bại · quy tắc nghiệp vụ · hàng dữ liệu mẫu | story file, `02_gate2_brd.md` |
| `TC-GEO-nnnn` · `ENG` · `X` · Pending · PROVISIONAL | case · case kỹ thuật · case xuyên epic · chưa chạy tới khi có phán quyết · chạy theo mặc định chưa được xác nhận | test specification |
| `REQ-GEO-nn` · `NFR-GEO-nnn` · `M-GEO-nn` | yêu cầu thô · yêu cầu phi chức năng · chỉ số | `00_gate0_intake_voc.md`, `architecture/` |
| `D-GEO-nnn` · `QA-GEO-nn` · `CR-GEO-nn` · `Q-CDT-nn` · `Q-GV-nn` · `Q-TL-nn` · `ADR-GEO-nn` | quyết định · hỏi đáp · thay đổi · câu hỏi cho ChuDauTu · GiaoVienCoVan · TechLead · quyết định kiến trúc | registers, `questions/` |
| `MSG-GEO-nn` (+ exact text) · `XF-nn` | thông báo · phát hiện chéo epic | dictionary, `04_story_map.md` |
| `GEO-01…11`, `LRN-`, `ANS-`, `KNW-`, `PRF-`, `IAM-`, `TRU-` + số | mã chức năng của hồ sơ domain | `DOMAIN-PROFILE.md` §5 |
| `PO-nn` | quyết định trong sổ quyết định (Final hoặc giả định) | `PO-DECISIONS.md` |
| `E1…E7` | bảy epic MVP | `epic_board.md` |
| `release_at` · `school_id` · `owner_id` · `schema_version` | giờ mở lời giải · định danh trường · chủ sở hữu bản ghi · số phiên bản lược đồ | `02_domain_model.md` |
| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy · nhãn máy · nhãn người | glossary |
