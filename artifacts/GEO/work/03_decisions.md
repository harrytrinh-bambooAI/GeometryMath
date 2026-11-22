# Decision log (F2) — GEO

> Chủ sở hữu: PO. Id không bao giờ dùng lại. Trạng thái: **Final (chủ đầu tư)** (PO không đổi được; đổi là CR do chủ đầu tư) · **Assumption (PO) — confirm with <người>** (mặc định đang dùng, mọi thứ dựng trên đó là PROVISIONAL, không bao giờ coi là đã chốt) · **Nguồn tài liệu** (suy trực tiếp từ nguồn, không phải quyết định mới). Mỗi giả định có đúng một câu hỏi ở `questions_for_stakeholders.md`. Dải id: PO `D-GEO-001…099`; quyết định phát sinh từ kiến trúc `D-GEO-200…299` (ARCH đề xuất qua bảng chung, PO ghi). Ngưỡng hiệu năng là đề xuất, giữ nguyên nhãn đó.
> Ghi chú về "Affects": mã story sơ bộ ở `04_story_map.md`; BA và QC dựng AC và case, đánh dấu PROVISIONAL cùng mã D- khi dùng giả định.

## A. Final của chủ đầu tư (giữ nguyên trạng thái Final)

| ID | Topic | Question | Options | Recommended default | Status | Source | Affects | Asked to |
|---|---|---|---|---|---|---|---|---|
| D-GEO-001 | Thiết bị theo vai | Học sinh, giáo viên dùng thiết bị nào? | — | Học sinh iPad hoặc iPhone; giáo viên web máy tính, kiểm tra nhanh bằng điện thoại | Final (chủ đầu tư) | SRC-017 (PO-01) | US-GEO-306, mọi story có giao diện | — |
| D-GEO-002 | Nhãn Concept, không mốc thời gian | Màn ngoài MVP và lộ trình ghi gì? | — | Màn ngoài MVP mang nhãn "Concept · Chặng N"; tài liệu chỉ ghi thứ tự | Final (chủ đầu tư) | SRC-018 (PO-15) | mọi tài liệu | — |
| D-GEO-003 | Gợi ý trước, lời giải từ giờ giáo viên đặt | Khi nào học sinh thấy lời giải? | — | Từ giờ giáo viên đặt (bài giao); gợi ý đi trước | Final (chủ đầu tư) | SRC-102 (MVP §1 #3); `PRODUCT-MASTER.md` D-12 | US-GEO-404, 604, 608 | — |
| D-GEO-004 | Kiểm chứng bằng tính toán | Ai quyết nhãn tin cậy? | — | Hình và đáp số phải qua kiểm chứng bằng tính toán trước khi gắn nhãn; AI không tự gắn; nhãn trong `materials/*.xlsx` không được sao chép | Final (chủ đầu tư) | SRC-113 (D-13); brief §5 | US-GEO-203, 504 | — |
| D-GEO-005 | Phạm vi MVP | MVP làm gì? | — | Chỉ hình không gian, THCS và THPT; có vai Giáo viên mẫu, kho đề soạn sẵn, lời giải AI đứng tên Giáo viên mẫu; web app riêng có backend | Final (chủ đầu tư) | SRC-101, SRC-103, SRC-104 (MVP §1) | toàn feature | — |
| D-GEO-006 | Hỏi đáp dựa trên giáo trình | Hỏi AI trả lời theo nguồn nào? | — | Dựa trên giáo trình của giáo viên (kho tri thức) và ghi rõ nguồn; mức MVP theo D-GEO-030 | Final (chủ đầu tư) | SRC-034 (PO-33) | US-GEO-409, 411 | — |
| D-GEO-007 | Từ khóa mở bài giảng | Từ khóa bấm được làm gì? | — | Bấm mở bài giảng; bài giảng từ bài soạn của giáo viên hoặc AI viết; ghi rõ nguồn | Final (chủ đầu tư) | SRC-035 (PO-34) | US-GEO-411, 412 | — |
| D-GEO-008 | Hình tương tác | Học sinh làm gì với hình? | — | Xoay, vẽ thêm điểm, đoạn, mặt phẳng, mở rộng mặt, kéo dài cạnh | Final (chủ đầu tư) | SRC-036 (PO-35) | US-GEO-301…305 | — |
| D-GEO-009 | Lời giải cập nhật hình | Xem lời giải thì hình thế nào? | — | Phần vẽ thêm của từng bước lần lượt được thêm vào hình | Final (chủ đầu tư) | SRC-037 (PO-36) | US-GEO-407, 408 | — |
| D-GEO-010 | Yêu cầu soạn giáo án, import, OCR, giáo trình riêng | Giáo viên soạn bài tập và bài giảng với AI, import file và ảnh, chat sửa; giáo trình riêng cho học sinh | — | Yêu cầu là Final; vị trí chặng xem D-GEO-011 | Final (chủ đầu tư) | SRC-030, SRC-031, SRC-032, SRC-041 (PO-27, 28, 29, 45) | `00_gate0_intake_voc.md` REQ-GEO-52, 53 | — |
| D-GEO-012 | Đa trường | Kiến trúc nhiều trường từ đầu | — | Mọi dữ liệu thuộc một trường có định danh trường; truy vấn luôn lọc theo trường; chỗ cho cấp phép liên trường chừa sẵn | Final (chủ đầu tư) | SRC-001, SRC-002, SRC-004 (brief §1, §5, §7) | US-GEO-104; mọi epic; `architecture/` | — |
| D-GEO-013 | Nhiều học sinh đồng thời; 1000 người dùng đồng thời | Hệ thống phục vụ đồng thời bao nhiêu? | — | Yêu cầu "1000 người dùng đồng thời" là Final; nghĩa đo được và ngưỡng là đề xuất (D-GEO-044, D-GEO-045) | Final (chủ đầu tư) | SRC-002 (c) | US-GEO-107; `architecture/` | — |
| D-GEO-014 | Truy vấn tối ưu | Cách viết truy vấn, phân trang, thiết kế bảng | — | Phân trang theo khóa, không OFFSET, không N+1, chỉ lấy cột cần, chỉ mục cho truy vấn nóng và kiểm kế hoạch thực thi, bảng sự kiện chia theo thời gian, lời giải ở bảng riêng | Final (chủ đầu tư) | SRC-002 (d), SRC-005 | US-GEO-505, 704; `architecture/` | — |
| D-GEO-015 | Java | Công nghệ máy chủ | — | Máy chủ có thể dùng Java (chủ đầu tư cho phép); phiên bản và khung: D-GEO-068 | Final (chủ đầu tư) | SRC-002 (e), SRC-013 | `app/` | — |
| D-GEO-016 | Thứ tự làm việc | Làm gì trước? | — | Epic, story, AC, test case và khung mã xong trước; sau đó mới chia việc cho agent viết mã | Final (chủ đầu tư) | SRC-003 | `artifacts/GOALS.md` | — |
| D-GEO-031 | Thứ tự và phạm vi nguồn | Nguồn nào thắng; có dùng chặng sau không? | — | `PO-DECISIONS.md` > `brief.md` §5, §7 > `DOMAIN-PROFILE.md` > `MVP.md`, `PRODUCT-MASTER.md` Phần B > `Design/`, `materials/`. `STAGES-AFTER-MVP.md` không phải yêu cầu | Final (chủ đầu tư) | lệnh goal run 22-11-2026; SRC-045, SRC-046 | toàn feature | — |

## B. Giả định chuyển từ `PO-DECISIONS.md` và brief (giữ "Assumption (PO)")

| ID | Topic | Question | Options | Recommended default | Status | Source | Affects | Asked to |
|---|---|---|---|---|---|---|---|---|
| D-GEO-011 | Vị trí chặng của soạn giáo án, import, OCR, giáo trình riêng | Có đưa vào MVP không? | A: ngoài MVP (chặng 2, 5, 6) · B: kéo một phần vào MVP | A. MVP chỉ có kho đề và bài giảng mẫu do hệ thống cung cấp; hiển thị trong bản mẫu với nhãn "Concept". Hậu quả OUT: giáo viên không thấy nút soạn hoặc import trong MVP | Assumption (PO) — confirm with ChuDauTu | SRC-033 (PO-32), SRC-072, `MVP.md` §4 | REQ-GEO-52, 53 (OUT); US-GEO-504, 412 | ChuDauTu |
| D-GEO-020 | Nhãn hai trục | Dùng nhãn nào, ở đâu? | — | Máy: "Đã kiểm chứng" / "Cần xem lại". Người: "AI soạn" / "Giáo viên đã duyệt". Giáo viên không tự gắn nhãn của máy | Assumption (PO) — confirm with ChuDauTu | SRC-019 (PO-02) | US-GEO-203, 502, 411 | ChuDauTu |
| D-GEO-021 | Điểm AI là đề xuất | Tự duyệt điểm? | — | Không tự duyệt; MVP không có chấm điểm; nguyên tắc giữ cho mọi chặng | Assumption (PO) — confirm with ChuDauTu | SRC-020 (PO-03) | REQ-GEO-34 | ChuDauTu |
| D-GEO-022 | Không xếp hạng | Có thứ tự học sinh, lớp trên Theo dõi lớp không? | — | Không có xếp hạng ở mọi cấp; Theo dõi lớp liệt kê, không sắp theo điểm hay mức nắm | Assumption (PO) — confirm with ChuDauTu | SRC-021 (PO-04) | US-GEO-701, 702 | ChuDauTu |
| D-GEO-023 | Bài tự học mở lời giải | Khi nào mở Lời giải ở bài tự học? | A: sau khi xem Gợi ý 1 và 2 · B: sau khoảng chờ · C: không mở nếu thuộc lớp | A | Assumption (PO) — confirm with ChuDauTu | SRC-022, SRC-052 (PO-07, BR-05); `MVP.md` §10 câu 1 | US-GEO-405 | ChuDauTu |
| D-GEO-024 | Công cụ đo không thành đường tắt | Ẩn gì trước giờ mở? | — | Ẩn giá trị của đại lượng đề hỏi và mọi đại lượng cùng giá trị, cùng vai trò trong cùng tam giác lời giải; vẫn đo được yếu tố trung gian; áp dụng cả bài tự học đang khóa | Assumption (PO) — confirm with GiaoVienCoVan | SRC-023, SRC-024, SRC-027 (PO-08, 17, 42) | US-GEO-305 | GiaoVienCoVan |
| D-GEO-025 | Dữ liệu mẫu dùng chung | Tên lớp, học sinh, trường mẫu | — | Lớp 11A1 (THPT) và 9B (THCS); học sinh An, Bình, Chi, Dũng, Hà; "Giáo viên Toán · 11A1"; không tên thật | Assumption (PO) — confirm with ChuDauTu | SRC-025 (PO-14) | US-GEO-601 | ChuDauTu |
| D-GEO-026 | Tự học kế thừa khóa | Đề tự nhập trùng bài đang khóa của lớp? | — | Kế thừa khóa của bài đó; lời giải mở theo giờ giáo viên; bộ câu thử có ca "dán lại đề đang khóa vào tự học". "Gần trùng" đo bằng quy tắc ARCH đề xuất và PO duyệt | Assumption (PO) — confirm with ChuDauTu | SRC-026 (PO-16) | US-GEO-406, 610 | ChuDauTu |
| D-GEO-027 | Giờ mở áp dụng mọi kênh; hỏi đáp tuân giờ mở | Hỏi đáp trước giờ mở nói gì? | — | Trước giờ mở: dẫn tới khái niệm và bài giảng, nêu công thức và phương pháp tổng quát, không nêu đáp số hay các bước của bài đang làm; khi từ chối nhắc lại gợi ý đang có, không tính thêm lượt gợi ý; mọi kênh (xem trước, theo dõi lớp, hỏi đáp) tuân giờ mở | Assumption (PO) — confirm with GiaoVienCoVan | SRC-028, SRC-038, SRC-040 (PO-22, 37, 44) | US-GEO-608, 609, 607 | GiaoVienCoVan |
| D-GEO-028 | Không bài nào "Giáo viên đã duyệt" trong MVP | Nhãn người của kho đề MVP? | — | Mọi lời giải trong kho đề chỉ mang "AI soạn"; nhãn "Giáo viên đã duyệt" chưa dùng vì chưa có giáo viên thật duyệt | Assumption (PO) — confirm with ChuDauTu | SRC-109 (`MVP.md` §10 rủi ro 1); `MVP.md` §1 | US-GEO-502 | ChuDauTu |
| D-GEO-029 | Bài giảng AI chưa duyệt hiện cho học sinh | Hai nguyên tắc chạm nhau | A: hiện kèm nhãn · B: không hiện tới khi giáo viên duyệt | A: nhãn "AI soạn · chờ giáo viên duyệt"; PO-DECISIONS ghi cần chủ đầu tư xác nhận | Assumption (PO) — confirm with ChuDauTu | SRC-039 (PO-43) | US-GEO-411, 412 | ChuDauTu |
| D-GEO-030 | Mức MVP của bài giảng, từ khóa, hỏi đáp | Gói nào, epic nào? | — | Bộ bài giảng và giáo trình mẫu do hệ thống cung cấp (10 khái niệm của prototype làm tham chiếu hành vi); GEO-11 chưa có trong bảng epic của hồ sơ domain §14 nên PO xếp vào E4 | Assumption (PO) — confirm with ChuDauTu | SRC-042 (PO-49), SRC-083, SRC-116 | US-GEO-409, 411, 412 | ChuDauTu |
| D-GEO-033 | Chạy web trên trình duyệt iPad, iPhone | Có ứng dụng cài đặt không? | — | Chỉ web tới hết chặng 4 | Assumption (PO) — confirm with ChuDauTu | SRC-043 (PO-12) | US-GEO-306 | ChuDauTu |
| D-GEO-034 | Giáo viên thật | Khi nào? | — | MVP chỉ Giáo viên mẫu; giáo viên thật vào bằng lời mời ở chặng 2 | Assumption (PO) — confirm with ChuDauTu | SRC-044 (PO-05) | US-GEO-102 | ChuDauTu |
| D-GEO-035 | Giao từng đề hay giao bộ đề | MVP giao bộ đề được không? | A: giao từng đề · B: giao cả bộ | A. `MVP.md` §5E và LRN-02 nói "một đề hoặc một bộ đề"; PO-25 (cao hơn) nói MVP giao từng đề. Giao bộ là OUT (chặng 3) | Assumption (PO) — confirm with ChuDauTu | SRC-029 (PO-25), SRC-085, SRC-107 | US-GEO-602 | ChuDauTu |
| D-GEO-044 | Phiên đang hoạt động; cửa sổ đỉnh | "1000 đồng thời" đo bằng gì? | — | Phiên đang hoạt động = có ít nhất một yêu cầu trong 60 giây gần nhất; cửa sổ đỉnh mở lời giải = 10 giây; công thức ở `02_domain_model.md` M-GEO-10…12 | Assumption (PO) — confirm with TechLead | SRC-007 (brief §6 câu 1); 60 và 10 là PO đặt | US-GEO-107; `architecture/` | TechLead |
| D-GEO-045 | Ngưỡng hiệu năng | p95 đọc, p95 ghi, tỉ lệ lỗi chấp nhận | — | Đề xuất: p95 đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5%; đỉnh 1000 học sinh cùng mở lời giải. Đề xuất, chưa phải ngưỡng chốt | Assumption (PO) — confirm with ChuDauTu | SRC-007 | US-GEO-107; NFR-GEO-nnn | ChuDauTu |
| D-GEO-046 | Hạ tầng chạy thử | Cơ sở dữ liệu, bộ nhớ đệm, nơi đặt | — | PostgreSQL cổng 54329 và Redis cổng 6389 cục bộ; chưa chọn đám mây | Assumption (PO) — confirm with TechLead | SRC-008 | `app/`, `architecture/` | TechLead |
| D-GEO-047 | Nhà cung cấp AI | AI thật khi nào? | — | Cổng AI có nhà cung cấp giả lập (nội dung soạn sẵn); thật là bước sau; MVP vẫn có đủ tầng cổng, ghi chi phí, đổi model. Hệ quả: chỉ số thời gian gửi đề tới hình và độ đúng thật chưa đo được trên AI giả lập | Assumption (PO) — confirm with ChuDauTu | SRC-009 | US-GEO-103, 105 | ChuDauTu |
| D-GEO-048 | Định danh học sinh | Tên và mã lớp có đủ không? | — | Đủ cho MVP: tên hiển thị + mã lớp, không thu email, số điện thoại | Assumption (PO) — confirm with ChuDauTu | SRC-010 | US-GEO-101 | ChuDauTu |
| D-GEO-049 | Dữ liệu thử tải | Quy mô | — | 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện | Assumption (PO) — confirm with TechLead | SRC-011 | US-GEO-107, 108 | TechLead |

## C. Giả định PO đặt thêm (chưa có nguồn; ghi rõ là suy luận của PO)

| ID | Topic | Question | Options | Recommended default | Status | Source | Affects | Asked to |
|---|---|---|---|---|---|---|---|---|
| D-GEO-040 | Dung sai kiểm chứng | Khớp là khớp trong sai số nào? | — | Ràng buộc hình (vuông góc, song song, bằng nhau): sai số tuyệt đối ≤ 1e-6 trên hình có cạnh lớn nhất quy về 1. Đáp số: sai số tương đối ≤ 1e-4 so với giá trị tính từ tọa độ; kiểm với ít nhất 3 bộ giá trị tham số cụ thể (a = 1 và hai giá trị khác). Công thức ở `02_domain_model.md` M-GEO-02, 03 | Assumption (PO) — confirm with GiaoVienCoVan | không có nguồn số (`MVP.md` §5B chỉ nói "thay giá trị cụ thể") | US-GEO-203, 105 | GiaoVienCoVan |
| D-GEO-041 | Ranh giới giờ mở | Đúng giờ `release_at` thì khóa hay mở? | A: mở khi giờ máy chủ ≥ `release_at` · B: mở khi > | A; đồng hồ máy chủ là đồng hồ duy nhất; giờ máy khách không được tin | Assumption (PO) — confirm with GiaoVienCoVan | SRC-069 (ca giờ mở) | US-GEO-604, 608, 612 | GiaoVienCoVan |
| D-GEO-042 | Đổi giờ và mở ngay | Đổi giờ sau khi đã mở? | A: không khóa lại lời giải đã mở; đổi giờ chỉ khi chưa mở · B: cho khóa lại | A (lời giải đã lộ không thu hồi được). Mở ngay đặt `release_at` bằng giờ máy chủ lúc bấm | Assumption (PO) — confirm with GiaoVienCoVan | SRC-088 (ANS-01); SRC-069 | US-GEO-605 | GiaoVienCoVan |
| D-GEO-043 | Bài chưa đặt giờ | Lời giải thế nào? | — | Vẫn khóa tới khi giáo viên đặt giờ hoặc mở ngay | Assumption (PO) — confirm with GiaoVienCoVan | SRC-050 (BR-03, ghi suy diễn) | US-GEO-604, 608 | GiaoVienCoVan |
| D-GEO-050 | Đa trường trong MVP nghĩa là gì | Mâu thuẫn `org_id` để trống và đa trường | A: cách ly dữ liệu theo `school_id` bắt buộc, không có màn quản trị trường · B: để trống như `MVP.md` | A. Hậu quả OUT: quản trị trường, tổ chức nhiều cấp (IAM-04), chia sẻ liên trường vẫn ngoài MVP | Assumption (PO) — confirm with ChuDauTu | SRC-004 và SRC-061; xem C-01 ở `01_understanding.md` | US-GEO-104; `architecture/` | ChuDauTu |
| D-GEO-051 | Mã lớp | Duy nhất phạm vi nào? | A: toàn hệ thống · B: trong một trường | A (từ mã lớp suy ra trường khi đăng nhập nhẹ) | Assumption (PO) — confirm with TechLead | SRC-010, SRC-004 | US-GEO-101 | TechLead |
| D-GEO-052 | Tự học và mã lớp | Học sinh chưa có mã lớp tự học thế nào? | A: tự học chỉ cho học sinh đã đăng nhập bằng mã lớp · B: lớp tự học chung | A; học sinh chưa có mã lớp ngoài MVP (`DOMAIN-PROFILE.md` §13 câu 1 còn mở) | Assumption (PO) — confirm with ChuDauTu | `DOMAIN-PROFILE.md` §13 câu hỏi 1; SRC-095 | US-GEO-401…403 | ChuDauTu |
| D-GEO-053 | Trợ giúp học sinh được dùng | Giao bài chọn gì? | — | hai công tắc như khung `Design/mvp-02`: "Gợi ý mức 1 và 2" bật hoặc tắt; "Hỏi đáp (không nêu đáp số)" bật hoặc tắt; giờ mở lời giải độc lập với hai công tắc; tắt Hỏi đáp thì tab Hỏi đáp ẩn hoặc báo giáo viên đã tắt (câu chữ do BA đề xuất) | Assumption (PO) — confirm with GiaoVienCoVan | SRC-120 (câu hỏi mở), SRC-085 | US-GEO-603 | GiaoVienCoVan |
| D-GEO-054 | Dữ liệu mẫu và trường | Mẫu có bao nhiêu trường? | — | Giáo viên mẫu và hai lớp mẫu thuộc một trường mẫu "THPT Mẫu A" (cấp là thuộc tính của lớp); để kiểm cách ly, bộ dữ liệu thử có thêm trường "THCS Mẫu C" với lớp và học sinh riêng | Assumption (PO) — confirm with TechLead | SRC-025, SRC-096 | US-GEO-601, 104 | TechLead |
| D-GEO-055 | Kho đề: số lượng và người soát | Bao nhiêu đề, ai soát? | — | 40 đến 60 đề, ít nhất 40 đề đã kiểm chứng (tính lại bằng engine); người soát nội dung Toán: chưa xác định | Assumption (PO) — confirm with ChuDauTu | SRC-092, SRC-067, SRC-047 | US-GEO-504 | ChuDauTu |
| D-GEO-056 | Danh sách hình mẫu | Hình nào dựng không cần AI? | — | Chóp, lăng trụ, hộp (gồm lập phương), nón, trụ, cầu mức cơ bản; hệ trục Oxyz | Nguồn tài liệu | SRC-075, SRC-114 | US-GEO-202, 205 | — |
| D-GEO-058 | "Thao tác trên hình" | Tính những hành động nào? | — | Dùng công cụ Chọn, Điểm, Đoạn, Mặt phẳng, Mở rộng, Đo dài, Đo góc; xoay, thu phóng, đặt lại không tính | Assumption (PO) — confirm with ChuDauTu | SRC-108, SRC-119 | US-GEO-703, M-GEO-08 | ChuDauTu |
| D-GEO-059 | "Phiên làm bài" | Một phiên là gì? | — | Một lần học sinh mở một bài, kết thúc khi 30 phút không có sự kiện | Assumption (PO) — confirm with ChuDauTu | SRC-108 | M-GEO-08 | ChuDauTu |
| D-GEO-060 | Thời gian gửi đề tới có hình | Đo thế nào? | — | Trung vị (p50) từ lúc gửi đề tự học tới lúc hình hiện; ngưỡng đề xuất dưới 40 giây; chỉ đo trên AI thật (D-GEO-047) | Assumption (PO) — confirm with ChuDauTu | SRC-062 | NFR-GEO-nnn | ChuDauTu |
| D-GEO-061 | Điều kiện nhãn "Đã kiểm chứng" | Khi nào gắn? | — | Chỉ khi cả ba: hình do engine dựng thỏa ràng buộc, đáp số khớp theo D-GEO-040, nguồn tọa độ là engine (không phải tọa độ AI); còn lại là "Cần xem lại" | Nguồn tài liệu | SRC-054, SRC-055, SRC-053 | US-GEO-203, 204 | — |
| D-GEO-062 | Version lời giải | Sửa lời giải đã giao? | — | MVP không có màn sửa; ở tầng dữ liệu lời giải đã giao gắn một version cố định, sửa về sau tạo version mới | Nguồn tài liệu | SRC-058, SRC-111 | US-GEO-104, 504; `architecture/` | — |
| D-GEO-065 | Định nghĩa "lộ đáp số" | Câu trả lời nào bị coi là lộ? | — | Chứa đáp số hoặc giá trị quyết định ở mọi dạng viết tương đương (thập phân, phân số, căn thức, kèm hay không kèm đơn vị), hoặc chứa các bước của lời giải đang khóa; công thức và phương pháp tổng quát không tính (PO-44) | Assumption (PO) — confirm with GiaoVienCoVan | SRC-040, SRC-049 | US-GEO-609, 610, 611 | GiaoVienCoVan |
| D-GEO-067 | Dữ liệu trẻ em | Lưu giữ, xóa, đồng thuận, nơi lưu | — | MVP chỉ thu tên hiển thị và mã lớp; không gửi tên hiển thị cho nhà cung cấp mô hình; chính sách lưu giữ, xóa, xuất, đồng thuận: chưa xác định; AC không viết hành vi xóa hay xuất dữ liệu | Assumption (PO) — confirm with ChuDauTu | SRC-015, SRC-065 | US-GEO-101, 103 | ChuDauTu |
| D-GEO-068 | Phiên bản Java và khung | Chọn gì? | — | Java 26 cài sẵn; khung, công cụ build, thư viện: TBD — owner TechLead; không ảnh hưởng AC | Assumption (PO) — confirm with TechLead | SRC-013 | `app/` | TechLead |
| D-GEO-070 | Bộ lọc kho đề | Lọc theo gì? | — | Cấp {THCS, THPT}; chủ đề {Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu} theo `materials/`; loại hình {chóp, lăng trụ, hộp, lập phương, nón, trụ, cầu}; "dạng bài" = chủ đề | Assumption (PO) — confirm with GiaoVienCoVan | SRC-092, SRC-117 | US-GEO-501 | GiaoVienCoVan |
| D-GEO-072 | Cổng AI lỗi hoặc quá thời gian | Học sinh thấy gì? | — | Thông báo dễ hiểu, cho thử lại, không hiện nội dung dở dang, không đổi nhãn; thời gian chờ tối đa: TBD — owner TechLead | Assumption (PO) — confirm with TechLead | SRC-062, SRC-066 | US-GEO-103, 401, 402, 409 | TechLead |
| D-GEO-073 | Cách vào của Giáo viên mẫu | Giáo viên vào bằng gì? | A: mã giáo viên mẫu riêng · B: chọn vai không mật khẩu | A: một mã truy cập riêng do chủ đầu tư cấp, học sinh không biết; B làm hỏng BR-GEO-03 vì mọi học sinh thành giáo viên. Nguồn chỉ nói "vào vai giáo viên bằng tài khoản mẫu" | Assumption (PO) — confirm with ChuDauTu | `MVP.md` §1 (cách hiểu); SRC-050 | US-GEO-102 | ChuDauTu |
| D-GEO-074 | Độ trễ khi tới giờ mở | Học sinh đang xem bài đúng lúc tới giờ thấy lời giải khi nào? | — | Trang đang mở tự cập nhật trạng thái khóa không muộn hơn 10 giây sau `release_at`, không cần tải lại hay đăng nhập lại; cơ chế (làm mới định kỳ hay máy chủ đẩy) do ARCH đề xuất | Assumption (PO) — confirm with TechLead | SRC-069 (ca "học sinh đang mở bài đúng lúc tới giờ"); 10 giây là PO đặt | US-GEO-612 | TechLead |
| D-GEO-075 | Tiến độ trong "Bài của em" | "Em đã đi tới bước nào" nghĩa là gì? | — | Tiến độ = k/3 với k là số mốc đã đạt trong Gợi ý 1, Gợi ý 2, Lời giải (khung `Design/mvp-04`, `mvp-05` vẽ thanh ba đoạn). Công thức M-GEO-14 | Assumption (PO) — confirm with GiaoVienCoVan | `MVP.md` §3 (màn "Bài của em"), SRC-084, `Design/mvp-04` | US-GEO-606 | GiaoVienCoVan |
| D-GEO-076 | Nội dung dùng chung và đa trường | Kho đề, bài giảng mẫu thuộc trường nào? | A: nội dung hệ thống dùng chung, chỉ đọc từ mọi trường · B: bản sao theo từng trường | A. Kho đề và kho bài giảng mẫu do Giáo viên mẫu sở hữu (BR-GEO-10) là nội dung hệ thống, mọi trường đọc được, chỉ hệ thống ghi. Bài giao, sự kiện, đề tự nhập, câu hỏi của học sinh luôn thuộc đúng một trường | Assumption (PO) — confirm with TechLead | SRC-057 (BR-10), SRC-004 | US-GEO-104, 504, 412 | TechLead |
| D-GEO-077 | Số liệu trên khung Theo dõi lớp | Thẻ "12′", cột Thời gian, cột Tình trạng tính thế nào? | — | Công thức M-GEO-21…24; khung thiết kế không ghi định nghĩa | Assumption (PO) — confirm with GiaoVienCoVan | `Design/mvp-03-theo-doi-lop.png` | US-GEO-701, 702 | GiaoVienCoVan |
| D-GEO-078 | Phần khung thiết kế không có nguồn yêu cầu | Làm gì với: nút "Trình chiếu" và "Sửa (sắp có)" (mvp-02), lựa chọn "Sau buổi học tới" (mvp-02), thẻ "Nên nhắc lại trên lớp" (mvp-03), thẻ "3 ngày liên tiếp", "Em đang học tới đâu" với phần trăm và dạng "Thiết diện" (mvp-04), mục "Lịch sử" ở thanh bên | A: không xây phía sau, hiện nhãn "Concept · Chặng N" hoặc ẩn · B: xây | A (M4: câu hỏi mở trên điều khiển đã vẽ là "không xây gì phía sau"). Lý do: Trình chiếu là GEO-13 chặng 2; Sửa lời giải là KNW-03 chặng 2; "Sau buổi học tới" cần thời khóa biểu (PO-26); "Nên nhắc lại" là tổng hợp lỗi sai COP-02 chặng 5; thẻ chuỗi ngày và phần trăm theo dạng không có chức năng nào trong MVP (PO-04, PO-09 nói không "số ngày học liên tiếp" với phụ huynh; chưa có nguồn cho học sinh). "Lịch sử": chỉ nhật ký hỏi đáp lọc theo bài có nguồn (SRC-105) | Assumption (PO) — confirm with ChuDauTu | `Design/mvp-02`, `mvp-03`, `mvp-04`; SRC-018, SRC-021, SRC-072 | US-GEO-503, 602, 604, 606, 701, 410 | ChuDauTu |
| D-GEO-079 | Chuỗi nhãn kiểm chứng | Khung học sinh ghi "Hình đã kiểm chứng", khung giáo viên ghi "Đã kiểm chứng" | — | Dùng đúng chuỗi "Đã kiểm chứng" (PO-02); "Hình đã kiểm chứng" là biến thể của thiết kế, không dùng | Assumption (PO) — confirm with ChuDauTu | SRC-019; `Design/mvp-05` so với `mvp-02` | US-GEO-203, 502 | ChuDauTu |

## Ghi chú
- Đã xem và không tạo D- riêng vì thuộc chặng sau hoặc OUT: PO-06, 09, 10, 11, 13, 18, 20, 21, 23, 24, 26, 30, 31, 32, 38, 39, 40, 41, 46, 47, 48 (xem `01_gate1_vision_alignment.md` mục OUT).
- Dải `D-GEO-200…299` dành cho quyết định phát sinh từ kiến trúc; chưa dùng.

## Phán quyết bổ sung của PO trong goal run (22-11-2026)
Tất cả là `Assumption (PO) — confirm with ChuDauTu` trừ khi ghi khác. Trả lời M-103 (QC) và M-203 (BA).

| ID | Điểm | Phán | Nguồn / lý do |
|---|---|---|---|
| D-GEO-101 | Dòng tiến độ khi k = 0 nhưng đã mở bài (M-GEO-14) | Hiện "Đã mở bài"; "Chưa bắt đầu" chỉ khi chưa mở | M-103 (1); D-GEO-075 |
| D-GEO-102 | Nhãn tình trạng (M-GEO-24) | Thêm nhãn thứ năm "Đã xem lời giải" khi k = 3; đã xem Gợi ý 1 và 2, bài đã mở, chưa xem lời giải là "Đang làm" | M-103 (2); D-GEO-077 |
| D-GEO-103 | Bộ câu thử moi đáp án (KG-04) | Nhận đề xuất của QC: 20 câu, 10 nhóm kỹ thuật, gồm bộ tối thiểu của BA; cổng chặn là 0 lần lộ | M-103 (3); `MVP.md` §9 |
| D-GEO-104 | Tên học sinh lớp 9B (QA-GEO-02) | Nhận: Khánh, Linh, Minh, Ngân, Phúc (dữ liệu mẫu, hư cấu) | PO-14 |
| D-GEO-105 | Nhãn máy của HKG-06 (QA-GEO-03) | Kỳ vọng theo phép tính của engine, không theo cột nhãn trong tệp `materials/`; nhãn trong tệp chỉ là dữ liệu minh họa | D-13 của `PRODUCT-MASTER.md`; G5b câu 2 |
| D-GEO-106 | Hai lần vào cùng tên trong một lớp (QA-GEO-06) | Cùng một học sinh | Lược đồ ARCH; confirm with TechLead |
| D-GEO-107 | Thử sai mã giáo viên (QA-GEO-07) | Không khóa riêng; chỉ giới hạn tốc độ chung theo NFR của ARCH | confirm with TechLead |
| D-GEO-108 | Thứ tự gợi ý (QA-GEO-08) | Phải xem Gợi ý 1 trước Gợi ý 2 | BR-GEO-04 |
| D-GEO-109 | Tham số hình mẫu (QA-GEO-09) | Số > 0, không đơn vị, mặc định theo dữ liệu US-GEO-205 | confirm with GiaoVienCoVan |
| D-GEO-110 | Nhập ảnh ở tự học (QA-GEO-11) | Có bước học sinh xem và sửa chữ đọc được; ảnh vượt 1 MB bị từ chối | `mvp-07`; M-004 |
| D-GEO-111 | Nhãn người của bài giảng nguồn giáo án mẫu (QA-GEO-12) | Chỉ hiện "Nguồn: Giáo án mẫu", không nhãn người | D-GEO-028 |
| D-GEO-112 | Điều khiển trên khung không có REQ (QA-GEO-13) | Không xây, không hiện; bổ sung vào danh sách D-GEO-078 | D-GEO-078 |
| D-GEO-113 | Giao lại cùng đề cho cùng lớp (QA-GEO-14) | Chặn, báo đề đã giao | confirm with GiaoVienCoVan |
| D-GEO-114 | Đổi công tắc trợ giúp sau khi giao (QA-GEO-15) | MVP không có | D-GEO-053 |
| D-GEO-115 | Số đo trên hình có tham số (QA-GEO-16) | Hiện theo tham số, ví dụ "a√2" | bản mẫu `fig3d.js` |
| D-GEO-116 | Nhóm câu hỏi ở Theo dõi lớp (QA-GEO-17) | Gộp câu trùng nguyên văn sau khi chuẩn hóa hoa thường và khoảng trắng; không dùng AI | D-GEO-047 |
| D-GEO-117 | Thu phóng, kích thước chạm, xoay ngang iPhone (QA-GEO-18) | Vùng chạm tối thiểu 44 điểm; dùng được ở màn dọc; không đặt số cho thu phóng | PO-01; confirm with TechLead |
| D-GEO-118 | Tên điểm mới (QA-GEO-19) | Chữ in hoa kế tiếp chưa dùng, bắt đầu từ M | bản mẫu |
| D-GEO-119 | Hộp xác nhận trước "Mở ngay" (QA-GEO-22) | CÓ hộp xác nhận, vì đã mở không khóa lại được (D-GEO-042) | Hành động không hoàn tác cần xác nhận; confirm with ChuDauTu |
| D-GEO-120 | Xem trước như học sinh (QA-GEO-23) | Không ghi sự kiện, không tính tiến độ | — |
| D-GEO-121 | Thứ tự học sinh ở Theo dõi lớp (QA-GEO-24) | Theo tên A → Z (không theo kết quả, PO-04) | PO-04 |
| D-GEO-122 | Dòng "{n} đề · {m} đã kiểm chứng" (QA-GEO-25) | Lấy từ bộ đếm M-GEO-07, không phụ thuộc bộ lọc; API danh sách vẫn không trả tổng | M-004 |
| D-GEO-123 | Tên cột trường | `org_id` (theo ARCH M-401); "school_id" trong tài liệu PO hiểu là `org_id` | M-401 |
| D-GEO-124 | Con trỏ phân trang | Chấp nhận cursor là `id` UUIDv7 với chỉ mục `(org_id, <khóa lọc>, id DESC)`; khung mã sẽ chỉnh ví dụ và bộ quét khi tách migration | M-401; confirm with TechLead |

### Phán quyết đợt 2 (22-11-2026): trả lời M-214 (BA) và M-404 (ARCH)
Tất cả là `Assumption (PO)`; người xác nhận ghi ở cột cuối.

| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-125 | NFR do ARCH tự đề xuất (005, 006, 007, 009–012, 027, 035, 043, 044, 048, 053, 055–058) | Nhận làm yêu cầu kỹ thuật của MVP với ngưỡng giữ nhãn "đề xuất"; là tiêu chí cho đội viết mã và thử tải, không phải cam kết với người dùng | TechLead |
| D-GEO-126 | Mất tối đa khoảng 10,6 giây sự kiện thao tác hình khi máy chủ sập đột ngột | Chấp nhận cho sự kiện thao tác hình; sự kiện "đã xem gợi ý", "đã xem lời giải", "đã hỏi" phải bền | ChuDauTu |
| D-GEO-127 | Row-Level Security | Bật cho các bảng dữ liệu học sinh như lớp phòng thủ thứ hai, sau khi đo chi phí trên khung mã thật | TechLead |
| D-GEO-128 | Lưu giữ sự kiện | Chưa quyết thời hạn; MVP giữ nguyên, có sẵn cơ chế dọn theo tháng | ChuDauTu (gắn Q pháp lý) |
| D-GEO-129 | Danh sách bài đã giao của giáo viên (QA-GEO-38) | Giữ trong US-GEO-701 (SC6), không mở story mới; mục "Tổng quan" và "Lớp học" của thanh bên giáo viên không hiện ở MVP | ChuDauTu |
| D-GEO-130 | Độ trễ cập nhật trạng thái khóa (QA-GEO-37) | Giữ 10 giây (D-GEO-074, NFR-GEO-008); ARCH chỉnh ADR-GEO-10 để chu kỳ hỏi lại không vượt 10 giây | TechLead |
| D-GEO-131 | Học sinh gọi Kho đề, chi tiết đề (QA-GEO-27) | `FORBIDDEN` | — |
| D-GEO-132 | Thiết bị ngủ hoặc mất mạng qua giờ mở (QA-GEO-28) | Khi trở lại, trang hỏi lại trạng thái và mở lời giải nếu đã tới giờ; không cần thao tác của học sinh | GiaoVienCoVan |
| D-GEO-133 | Lược đồ thiếu chỗ lưu danh mục hình mẫu, đại lượng ẩn, bộ câu thử (QA-GEO-29) | ARCH bổ sung vào lược đồ trước khi tách migration | TechLead |
| D-GEO-134 | Ưu tiên chạm và "Đặt lại" (QA-GEO-30) | Ưu tiên điểm > cạnh > mặt; "Đặt lại" chỉ đặt lại góc nhìn, có lệnh "Xóa hết" riêng cho hình vẽ thêm | GiaoVienCoVan |
| D-GEO-135 | `mvp-08` vẽ sáu nút, yêu cầu chín công cụ (QA-GEO-31) | iPhone hiện sáu nút chính và một nút "Thêm" mở phần còn lại | ChuDauTu |
| D-GEO-136 | Màn khám phá hình mẫu (QA-GEO-32) | Chỉ có hình và công cụ; không gợi ý, lời giải, hỏi đáp | — |
| D-GEO-137 | Nội dung bảy bài giảng "Giáo án mẫu" (QA-GEO-33, KG-02) | MVP dùng nội dung trong bản mẫu tương tác làm dữ liệu mẫu, đánh dấu "chưa có người soát" | GiaoVienCoVan |
| D-GEO-138 | Dòng "hôm nay em có n bài cần làm" (QA-GEO-34) | n = số bài được giao chưa xem lời giải | — |
| D-GEO-139 | Xem trước đề đã giao (QA-GEO-35) | Theo trạng thái giờ mở của chính bài giao đó | — |
| D-GEO-140 | Dung sai với đề không tham số (từ M-105) | Dùng dung sai tuyệt đối của D-GEO-040 trên giá trị số của đề | GiaoVienCoVan |
| D-GEO-141 | Kho đề mới có sáu đề, tiêu chí cần ít nhất 40 đề đã kiểm chứng | Giữ tiêu chí; việc soạn thêm 34 đề trở lên là việc nội dung cần người soát, KHÔNG giao cho agent viết mã | ChuDauTu |

### Phán quyết đợt 3 (22-11-2026): trả lời M-108, M-109, M-110 (QC)
| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-142 | Trần trang Kho đề (QF-E5-01) | Trang mặc định 20, trần 50 theo ARCH (M-403); BA sửa US-GEO-505-FC4, QC sửa kỳ vọng | TechLead |
| D-GEO-143 | Mã lỗi cho xung đột trạng thái (QF-E6-05) và quá tải (QF-E1-04) | Thêm `CONFLICT` (đổi giờ khi đã mở, giao lại cùng đề), `RATE_LIMITED`, `SERVICE_OVERLOADED` vào danh sách mã lỗi của M-004 | TechLead |
| D-GEO-144 | Biên phiên làm bài 30 phút (QF-E7-01) | Khoảng nghỉ ≥ 30 phút 0 giây mở phiên mới; 29 phút 59 giây vẫn cùng phiên | GiaoVienCoVan |
| D-GEO-145 | Chỉ dẫn không lưu đệm (QF-E6-02) | Phản hồi chứa lời giải, gợi ý, trạng thái khóa trả `Cache-Control: no-store` | TechLead |
| D-GEO-146 | Xem trước khi giáo viên đã chọn giờ ở bước 3 (QF-E6-07) | Hiện thẻ khóa kèm giờ đang chọn | GiaoVienCoVan |
| D-GEO-147 | Giáo viên dùng điện thoại để đổi giờ và "Mở ngay" (QF-E6-08) | Thuộc phạm vi (PO-01: kiểm tra nhanh bằng điện thoại); BA thêm AC thiết bị cho US-GEO-605 | — (PO-01 Final) |
| D-GEO-148 | Giới hạn tốc độ TC-GEO-0861 | Giữ case ở mức Low, PROVISIONAL theo D-GEO-125 | TechLead |
| D-GEO-149 | Giả mạo tên bạn cùng lớp khi đăng nhập nhẹ (D-01 của `06_nfr.md`) | Rủi ro ĐÃ BIẾT của MVP, chấp nhận tạm vì chưa thu định danh trẻ em (PO-21); ghi vào known gaps, không viết AC | ChuDauTu |

### Phán quyết đợt 4 (22-11-2026): trả lời phản biện RV (M-502, `review/RV_findings.md`)
Mọi dòng dưới đây là **Assumption (PO)** trừ khi cột "Xác nhận với" ghi Final. PO chấp nhận cả 37 phát hiện RV-GEO-01…37; các phát hiện không có dòng riêng ở đây được sửa đúng như cột "Đề xuất sửa" của RV.

| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-150 | Tự học gần trùng (RV-GEO-01, PO-16, D-GEO-026) | Bài tự học **kế thừa khóa** khi đề tự nhập khớp đề kho của một bài giao đang khóa theo **chữ ký chuẩn**: (hình mẫu, bộ tham số đã quy về a = 1, đại lượng đề hỏi), KHÔNG phụ thuộc tên điểm, cách viết, chữ thêm. Chữ ký tính từ bản dựng hình (scene_build) sau khi đề đã được dựng; băm văn bản chuẩn hóa giữ làm phép khớp phụ. Đề không dựng được hình thì không tính chữ ký và không có lời giải tự học (chỉ gợi ý). F-GEO-226 là ca chặn của cổng phát hành | GiaoVienCoVan |
| D-GEO-151 | Thời điểm kiểm kế thừa khóa (RV-GEO-03) | Kiểm ở **mỗi lần** xin Gợi ý 2 hoặc lời giải của bài tự học, đối chiếu các bài giao đang khóa của mọi lớp học sinh đang thuộc; không chỉ lúc tạo bài. `lock_source_assignment_id` chỉ còn là dấu vết | — |
| D-GEO-152 | Cách vào của Giáo viên mẫu (RV-GEO-02, PO-18) | Mã cấp **theo từng tài khoản** giáo viên, ngẫu nhiên ≥ 12 ký tự base32 (≈ 60 bit), lưu **băm** (không lưu mã rõ), thu hồi được, có hạn dùng. Đảo D-GEO-107 cho riêng đăng nhập giáo viên: sau 10 lần sai trong 15 phút theo IP hoặc theo tài khoản thì trả `RATE_LIMITED`. Đăng nhập nhẹ của học sinh giữ D-GEO-107. QC thêm ca "giả làm giáo viên" vào bộ câu thử | ChuDauTu |
| D-GEO-153 | Phạm vi `fn_get_solution_teacher` (RV-GEO-04, 12, 27) | MVP: giáo viên chỉ xem lời giải của **đề kho nền tảng** (`source = 'catalog'`); không bao giờ xem lời giải của trường khác hay của bài tự học. Màn xem trước không gọi hàm lời giải | — (brief §7 Final) |
| D-GEO-154 | Tọa độ đỉnh gửi xuống trang Làm bài (RV-GEO-05) | Tọa độ hình là **dữ kiện của đề**, không phải đáp số theo BR-01. PO-08 (ẩn số đo) là biện pháp chống đường tắt ở giao diện. M-GEO-06 và NFR-GEO-020 đếm "lộ" là khi hệ thống **hiển thị hoặc trả về giá trị đáp số** (kể cả công cụ đo hiện giá trị đề hỏi), không tính việc học sinh tự tính từ tọa độ | ChuDauTu |
| D-GEO-155 | Gợi ý của bài tự học (RV-GEO-06) | Bài tự học kế thừa khóa dùng **gợi ý của đề kho** (do Giáo viên mẫu soạn), không dùng gợi ý AI sinh. Bài tự học không kế thừa khóa dùng gợi ý AI như cũ | — |
| D-GEO-156 | Kiểm soát đáp án (RV-GEO-07, BR-02) | ARCH viết **ADR-GEO-14**: đầu vào (câu trả lời AI, đáp số, các dạng tương đương do bộ kiểm chứng tính), quy tắc (khớp biểu thức chuẩn hóa, khớp giá trị số theo dung sai D-GEO-040/140, đáp số ngắn như "a" chỉ chặn khi đứng làm kết luận, câu chỉ xác nhận/phủ nhận đáp số bị chặn), ghi lần chặn, **đóng khi lỗi** (bộ kiểm lỗi → không trả câu trả lời). PO duyệt ở R3. QC nối mỗi quy tắc vào F-GEO-210…229 | — |
| D-GEO-157 | Đệm và chu kỳ hỏi lại trạng thái khóa (RV-GEO-08, 09, D-GEO-130) | Trang đang chờ khóa hỏi lại **≤ 8 giây** (cộng đệm không quá 2 giây); ARCH tính lại tải Q04b thường trực. Quyền xem lời giải **luôn** đi qua cổng CSDL với (org, user, assignment); chỉ được đệm `release_at` của bài giao cho điểm cuối trạng thái, không bao giờ đệm quyết định "được xem" | TechLead |
| D-GEO-158 | Quyền ghi của `geo_app` (RV-GEO-10) | Siết theo đề xuất RV: vai nạp nội dung riêng cho kho nền tảng; tạo người dùng qua hàm chỉ tạo `student/light`; nhãn máy chỉ đổi qua hàm có tham chiếu `verification_report`; tiến độ chỉ đổi qua hàm ghi sự kiện; tạo bài giao có giờ mở qua hàm (CHECK giờ mở > giờ tạo, ghi `audit_log`). ARCH thêm ca vào bộ kiểm cách ly | — |
| D-GEO-159 | Lưu câu trả lời hỏi đáp (RV-GEO-11) | **Có lưu** câu trả lời đã qua Kiểm soát đáp án, để US-GEO-409, 410 mở lại không gọi AI; lưu giữ theo D-GEO-128; chỉ chủ câu hỏi đọc được | ChuDauTu |
| D-GEO-160 | Mô hình "Giáo viên mẫu" và chủ sở hữu kho đề (RV-GEO-14) | Kho đề đứng tên **một tác giả nền tảng** (tài khoản hệ thống của tổ chức nền tảng); mỗi trường có tài khoản Giáo viên mẫu riêng để giao bài. `schema_version` bắt buộc ở cấu trúc JSON; bảng quan hệ dùng phiên bản migration (hiểu lại DOMAIN-PROFILE §9) | ChuDauTu |
| D-GEO-161 | "Cùng đề" khi giao trùng (RV-GEO-16) | Cùng `problem` (bất kể version) cho cùng lớp và bài giao chưa lưu trữ → `CONFLICT`; ràng buộc duy nhất một phần ở CSDL | GiaoVienCoVan |
| D-GEO-162 | Mức bền của sự kiện `open` (RV-GEO-22) | `open` là sự kiện **bền** (mức B, một sự kiện mỗi lần mở); D-GEO-126 vẫn chỉ áp cho sự kiện thao tác hình | — |
| D-GEO-163 | So sánh dung sai và biên F-GEO-071, 073 (RV-GEO-23) | So sánh `|x − y| ≤ tol·max(1,|y|) + 1e-12`; fixture đổi biên thành 45,0044 (khớp) và 45,0046 (không khớp), tương tự cho F-GEO-073 | — |
| D-GEO-164 | Nút "Trình chiếu", "Sửa (sắp có)" ở chi tiết đề (RV-GEO-32) | Áp D-GEO-112: **không hiện** | — |
| D-GEO-165 | Đo thời điểm "hình hiện" (RV-GEO-33, M-GEO-09) | Thiết bị gửi sự kiện `scene_shown` (mức A, không bền) kèm thời lượng; chỉ dùng làm số đo | — |
| D-GEO-166 | Ô tìm bài giảng Q11b, Q11c (RV-GEO-34) | **Không thuộc MVP**; bỏ khỏi catalogue và mô hình tải; giữ `lecture_search_idx` cho Q12 | — |
| D-GEO-167 | Thứ tự tên trong danh sách lớp (RV-GEO-36, D-GEO-121) | Phân trang theo `(name_key, user_id)`; không giới hạn cỡ lớp ở MVP | — |
| D-GEO-168 | Bảng cấu hình chung khung mã ↔ ARCH (RV-GEO-20, 37) | Theo ARCH: pool 20 mỗi nút, chờ kết nối 2 giây, `statement_timeout` 2 giây, Redis 100 ms. Con trỏ: **mờ, AES-GCM, gắn trường + loại danh sách + phiên bản**, bên trong là khóa sắp xếp với UUIDv7 `id` (giữ D-GEO-124). Trần trang chung 100, Kho đề 50 (D-GEO-142). Máy chủ trả ISO-8601 UTC; giao diện **luôn hiển thị giờ Việt Nam** (BR-GEO-233). Một bảng duy nhất ở `app/docs/CONFIG.md`, ARCH dẫn tới | TechLead |
| D-GEO-169 | Kiểm thử ranh giới đúng R (RV-GEO-24) | Profile test có hàm `fn_test_set_release(p_now)` và vai kiểm thử; kiểm qua API dùng R = giờ máy chủ + 2 giây rồi chờ. Không có ở profile chạy thật | TechLead |
| D-GEO-170 | Công cụ kiểm thử (RV-GEO-35) | E2E: Playwright; đếm câu lệnh JDBC: datasource-proxy; thử tải: Gatling (Java). Thêm vào khung mã trong lượt sửa này (chỉ phụ thuộc và ví dụ chạy được) | TechLead |

### Phán quyết đợt 5 (22-11-2026): trả lời M-503 (CODE)
| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-171 | `org_id = :platformOrgId` có tính là bộ lọc trường trong SqlLint không | **Không.** Mọi câu đọc nội dung nền tảng phải mang `-- sqllint:allow=tenant` kèm lý do, để mỗi đường đọc ngoài trường đều có người duyệt (brief §7). Assumption (PO) | TechLead |
| D-GEO-172 | Gói Java cho module ADR-GEO-01 (`assignment`, `tracking`, `platform`) | Tạo gói khi tách migration V10, tức sau R3, cùng lúc chia việc viết mã. Assumption (PO) | TechLead |

### Phán quyết đợt 6 (22-11-2026): trả lời M-113 (QC) và M-216 (BA)
Mọi dòng là Assumption (PO).
| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-173 | Đếm thử sai đăng nhập giáo viên (M-113.1, M-216.1–2) | Đếm **theo IP** (màn chỉ có ô mã nên chưa biết tài khoản khi mã sai). Trong cửa sổ bị chặn, **mã đúng cũng bị `RATE_LIMITED`** (lần thứ 11 trở đi). Làm rõ D-GEO-152 | TechLead |
| D-GEO-174 | Đã xem lời giải tự học, sau đó giáo viên giao đúng đề (M-113.2) | Xin lại thì **bị khóa** theo bài giao; mở lại khi bài giao Đã mở. Hệ quả của D-GEO-151 | GiaoVienCoVan |
| D-GEO-175 | Giáo viên gọi lời giải đề tự nhập trong trường mình (M-113.3) | `NOT_FOUND` (ngoài phạm vi D-GEO-153) | — |
| D-GEO-176 | Bộ câu thử moi đáp án (M-113.4) | **22 câu, 11 nhóm** (thêm nhóm "giả làm giáo viên", F-GEO-230, 231); sửa D-GEO-103. BA đổi US-GEO-610-SC1, 611-SC1 | — |
| D-GEO-177 | Đề lạ dựng bằng tọa độ AI, không theo hình mẫu (M-216.3) | Coi là **không tính được chữ ký** → không có lời giải tự học (chỉ gợi ý), để giữ "0 lần lộ" của D-GEO-150 | ChuDauTu |
| D-GEO-178 | Dung sai của đề không tham số (M-216.4) | Sửa D-GEO-140: dung sai tuyệt đối = **max(1e-6, nửa đơn vị của chữ số thập phân cuối mà đáp số ghi)**; "41,57 cm³" của HKG-03 vì vậy **khớp** (dung sai 0,005) | GiaoVienCoVan |
| D-GEO-179 | Tên hiển thị tác giả nền tảng (M-216.5) | "Giáo viên mẫu" | — |
| D-GEO-180 | Xem trước bài giao đã mở (M-216.6) | Chỉ hiện nhãn "Lời giải đã mở" (MSG-GEO-29), không tải lời giải | — |
| D-GEO-181 | Cờ "chưa có người soát" của bài giảng (M-216.7) | **Hiện cho cả học sinh và giáo viên** (minh bạch: AI/bản mẫu đề xuất, giáo viên quyết định — PO-03); câu chữ: "Nội dung đang chờ giáo viên soát" | GiaoVienCoVan |
| D-GEO-182 | Câu chữ khi n = 0 (M-216.8) | "Hôm nay em không có bài nào cần làm." | — |
| D-GEO-183 | Bộ kiểm soát đáp án lỗi (M-216.9) | Học sinh thấy MSG-GEO-14; không trả câu trả lời (đóng khi lỗi, D-GEO-156) | — |

### Phán quyết đợt 7 (22-11-2026): trả lời M-406 (ARCH)
| ID | Điểm | Phán | Xác nhận với |
|---|---|---|---|
| D-GEO-184 | KS-2: số đã làm tròn (ví dụ "≈ 0,43" cho √3/4) | **Chặn** cả giá trị làm tròn tới ít nhất 2 chữ số có nghĩa của đáp số; lệch về phía chặn (BR-02 ưu tiên không lộ). Assumption (PO) | GiaoVienCoVan |
| D-GEO-185 | Đếm thử sai theo tài khoản bên cạnh theo IP | Chấp nhận thêm, vì 4 ký tự đầu của mã định vị được tài khoản; quy tắc theo IP của D-GEO-173 và AC giữ nguyên. Assumption (PO) | TechLead |
