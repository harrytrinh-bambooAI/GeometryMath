# 05 — Mô hình tải và kế hoạch năng lực cho 1000 người dùng đồng thời

> Vai: ARCH. Trạng thái: **đề xuất**. Nguồn: `brief.md` §6 câu 1, 2, 5 và §7; `work/03_decisions.md` D-GEO-013, 044, 045, 046, 049, 125, 130, 132, 152, 157, 162, 166, 168; `work/02_domain_model.md` M-GEO-10…12, 20.
> Nhật ký sửa (dấu vết): lượt sửa sau RV: tải thăm dò Q04b tính **thường trực** (D-GEO-157, RV-08); bỏ Q11b/c (D-GEO-166); bỏ khóa đệm quyết định "đã mở" (RV-09); `open` mức B (D-GEO-162); giới hạn thử sai mã giáo viên (D-GEO-152, 173); số cấu hình dẫn về `app/docs/CONFIG.md` (D-GEO-168); đo lại đường ghi qua hàm (`07_verification.md` §4.6).
> **Mọi con số đo trong tài liệu này là số đo trên máy một nút cục bộ** (10 lõi, 24 GB RAM; PostgreSQL 17, Redis, bộ sinh tải `pgbench` và các việc khác chạy chung một máy; macOS không ép ghi đĩa vật lý ở `fsync`), **không phải kết quả sản xuất** và không thể ngoại suy tuyến tính. Chúng cho biết bậc độ lớn và hình dạng đường cong (điểm gối), không phải cam kết. Chưa có phép đo cho tầng Java (ARCH không viết mã Java) và cho Redis: ghi rõ ở mục "chưa đo". Các tỉ lệ yêu cầu là **giả định** của ARCH (không có nguồn), cần TechLead/ChuDauTu xác nhận.

## 1. Định nghĩa và công thức (từ nguồn, không đổi)
- **Phiên đang hoạt động** `S(t)` = số phiên có ≥ 1 yêu cầu trong 60 giây gần nhất (D-GEO-044). Mục tiêu `S = 1000`.
- **Tải đều** `λ_đều = S / T`, T ∈ [3, 5] giây là khoảng giữa hai yêu cầu của một phiên (brief §6 câu 1): `1000/5 = 200` đến `1000/3 ≈ 333,3` yêu cầu/giây. **Thiết kế theo cận trên 333,3.**
- **Đỉnh mở lời giải** `λ_đỉnh = S_mở / W = 1000 / 10 = 100` yêu cầu/giây trên đường xem lời giải (cửa sổ W = 10 giây, D-GEO-044), **chồng lên** tải đều: tối đa `333,3 + 100 = 433,3` yêu cầu/giây (M-GEO-12).
- **Thăm dò trạng thái khóa thường trực (D-GEO-130, 157):** trang đang chờ khóa hỏi lại mỗi `T_poll ∈ [6; 7,5)` giây (trung bình 6,75 giây, ADR-GEO-10). Ca xấu: cả `S_chờ = 1000` phiên đang mở trang chờ khóa: `λ_thăm_dò = 1000 / 6,75 ≈ 148,1` yêu cầu/giây HTTP, **cộng thêm** vào tải đều (một phiên vừa làm bài vừa có trang chờ khóa đang mở). Xuống CSDL: đệm `release_at` theo bài 2 giây ⇒ mỗi bài đang chờ tối đa 0,5 truy vấn/giây; với ≈ 30 học sinh mỗi bài, `1000 / 30 ≈ 34` bài ⇒ ≈ **17 truy vấn/giây** Q04b (Redis tắt: cả 148,1 xuống CSDL).
- **Tải thiết kế mới:** đều `333,3 + 148,1 = 481,4` yêu cầu/giây; đỉnh `481,4 + 100 = 581,4` yêu cầu/giây (bản trước: 433,3, vì coi thăm dò là thay thế). Vào CSDL: `333,3 + 17 + 100 ≈ 450` (Redis sẵn) hoặc `581,4` (Redis tắt).
- Dự phòng của ARCH (không có trong nguồn, **ARCH đề xuất**, NFR-GEO-006): `1,5 × 333,3 ≈ 500` yêu cầu/giây cho tải đều không kể thăm dò, tức ≈ 650 yêu cầu/giây kể cả thăm dò (đo ở mức này, §7); ca xấu nhất `1000` yêu cầu xem lời giải dồn trong 1 giây (không có cửa sổ 10 giây).

## 2. Tỉ lệ các loại yêu cầu (giả định) và yêu cầu/giây

Giả định của ARCH về hỗn hợp của một phiên học sinh điển hình trong giờ cao điểm (cộng 100%, cần xác nhận). Mỗi yêu cầu ánh xạ tới truy vấn trong `04_query_catalogue.md`:

| Loại yêu cầu | Truy vấn | Tỉ lệ | Yêu cầu/giây ở 333,3 | Ở đỉnh 433,3 | Ghi |
|---|---|---|---|---|---|
| Lô sự kiện mức A (thao tác hình, `scene_shown`) | `fn_record_batch` (Q07 + Q08) | 35% | 116,7 | 116,7 | ghi, commit không đồng bộ (ADR-GEO-11) |
| Hỏi đáp (tìm nguồn + gọi AI + Kiểm soát đáp án + ghi) | Q12 + `fn_record_qa` | 12% | 40,0 | 40,0 | gọi AI và Kiểm soát đáp án (ADR-GEO-14) ngoài giao dịch; một lần gọi hàm ghi câu hỏi + câu trả lời + `ask` + tiến độ |
| Bấm từ khóa (và hộp bài giảng Q11d) | Q11, Q11d | 12% | 40,0 | 40,0 | đệm Redis; ô tìm Q11b/c không thuộc MVP (D-GEO-166) |
| Mở bài | Q04 + `fn_record_durable('open')` | 10% | 33,3 | 33,3 | `open` mức B, commit đồng bộ (D-GEO-162) |
| Danh sách bài của em | Q02/Q02b | 8% | 26,7 | 26,7 | |
| Xin gợi ý | `fn_get_hint` (Q05/Q05b + ghi bền) | 8% | 26,7 | 26,7 | một lần gọi; bài tự học thêm Q16 |
| Xem lời giải | `fn_get_solution` + `fn_record_durable('unlock')` | 5% | 16,7 | 116,7 | **+100 ở đỉnh** (M-GEO-12) |
| Duyệt kho đề (giáo viên) | Q13 | 5% | 16,7 | 16,7 | |
| Theo dõi lớp, bài đã giao, xem trước (giáo viên) | Q09, Q09b, Q03, Q14, Q22, Q23 | 3% | 10,0 | 10,0 | 30 giáo viên mở làm mới mỗi 10 giây ≈ 3/giây; còn lại cho các thao tác giáo viên khác |
| Đăng nhập / làm mới thẻ | `fn_student_login` (Q01); giáo viên `fn_teacher_login` (Q24) | 2% | 6,7 | 6,7 | |
| **Tổng (không kể thăm dò)** | | **100%** | **333,3** | **433,3** | |
| Thăm dò trạng thái khóa (thường trực, ca xấu) | Q04b (đệm 2 giây theo bài) | + | +148,1 HTTP (≈ 17 xuống CSDL) | +148,1 | D-GEO-157; ADR-GEO-10 |
| **Tổng thiết kế** | | | **481,4** | **581,4** | |

Bản trước coi thăm dò Q04b là "thay thế" yêu cầu khác và chỉ dày trong 60 giây cuối; RV-08 chỉ ra cách đó trượt 10 giây với "Mở ngay". Nay thăm dò ≤ 7,5 giây suốt thời gian trang chờ khóa và được **cộng** vào tải (dòng trên). Q04b thực thi 0,006 ms, 7 trang (`07_verification.md` §3); pgbench riêng kịch bản thăm dò ≈ 56.000 giao dịch/giây ở 16 kết nối (§4.6), tức 148 yêu cầu/giây là ≈ 0,3% khả năng của CSDL ngay cả khi Redis tắt.

## 3. Công thức dòng dữ liệu và khối lượng ghi
- **Sự kiện/giây** = `λ_lô × s̄`, với `λ_lô = 116,7/giây` (35% × 333,3) và `s̄` = số sự kiện mỗi lô. Giả định `s̄ = 20` (thiết bị gom ≤ 10 giây hoặc ≤ 50 sự kiện; D-GEO-058 loại xoay/thu phóng): **≈ 2.330 sự kiện/giây**, tức ≈ 8,4 triệu sự kiện mỗi giờ nếu duy trì tải đỉnh.
- **Cảnh báo về quy mô dữ liệu:** "2 triệu sự kiện" (brief §6 câu 5) là **kích thước dữ liệu thử**, không phải khối lượng sản xuất. Ở tải duy trì 1000 phiên, 2 triệu sự kiện chỉ là ≈ 14 phút tải đỉnh. Nếu 1000 phiên hoạt động liên tục 2 giờ mỗi ngày (giả định), mỗi ngày ≈ 16,8 triệu sự kiện và mỗi tháng có thể vượt vài trăm triệu hàng. Khi đó partition theo tháng quá thô: chuyển sang partition theo tuần hoặc ngày chỉ là đổi tham số trong `geo.ensure_monthly_partitions` (hàm viết lại cho khoảng khác), không đổi bảng; và chính sách lưu giữ (PO-21) thành yếu tố quyết định dung lượng. Chi phí lưu: xem số đo `bytes/hàng` ở `07_verification.md` (≈ 237 byte mỗi sự kiện trong bộ dữ liệu thử sạch, gồm chỉ mục; thí nghiệm chèn tuần tự cho ≈ 208) ⇒ ≈ 4 GB/ngày ở kịch bản trên **(giả định)**. Đây là rủi ro cần ChuDauTu/TechLead nhìn: đề xuất bắt đầu đo khối lượng thật rồi chỉnh.
- Mỗi lô là **một** câu `INSERT` nhiều hàng + một `UPSERT` tiến độ; đo thật: một lô 50 sự kiện + cập nhật tiến độ mất ≈ 1,8 ms ở 16 kết nối chạy đồng thời (≈ 9.000 lô/giây ≈ 450.000 sự kiện/giây, gấp ≈ 190 lần nhu cầu 2.330 sự kiện/giây).

## 4. Nhóm kết nối, số tiến trình/nút

### 4.1. Đo thật: thông lượng theo số kết nối (hỗn hợp theo bảng §2)
`pgbench` hỗn hợp 10 kịch bản đúng tỉ lệ §2, giao thức prepared, vai `geo_app`, 20 giây mỗi mức, cùng máy với CSDL:

| Số kết nối | tps (giao dịch/giây) | Trung bình (ms) |
|---|---|---|
| 2 | 6.985 | 0,29 |
| 4 | 7.569 | 0,53 |
| 8 | 9.894 | 0,81 |
| **16** | **11.873** (đỉnh) | 1,35 |
| 32 | 10.432 | 3,07 |
| 64 | 9.952 | 6,43 |
| 128 | 8.632 | 14,83 |
| 256 | 4.043 | 63,32 |

(Số liệu lần chạy cuối, `07_verification.md` §4.2; lần chạy trước cho đỉnh ở 32 kết nối với 9.054 tps: điểm gối nằm trong khoảng 16 đến 32 và thông lượng dao động ± 20% giữa các lần do máy dùng chung.)

Đọc đồ thị: thông lượng tăng tới ≈ 16 đến 32 kết nối (10 lõi, nhưng `pgbench` chiếm một phần lõi) rồi **giảm**: thêm kết nối quá điểm gối chỉ làm tăng độ trễ và giảm thông lượng. Vì vậy nhóm kết nối **không được nâng theo số người dùng**.

### 4.2. Kích thước nhóm kết nối (quy tắc, ADR-GEO-04)
- **Định luật Little:** kết nối bận trung bình `L = λ × W`. Với `λ = 581,4`/giây (tải mới) thì `L` vẫn < 1 kết nối; với `λ = 433,3`/giây và thời gian giữ kết nối trung bình `W ≈ 0,3 ms` (hỗn hợp ở 2 kết nối, gần như không tải) → `L ≈ 0,17` kết nối. Dù có hệ số an toàn 20 cho bùng nổ và độ trễ đuôi, vẫn chỉ cần ≈ 4 kết nối bận.
- **Đề xuất (đã vào `app/docs/CONFIG.md`, D-GEO-168):** 20 kết nối mỗi nút ứng dụng, 2 nút ⇒ 40 kết nối tới PostgreSQL, quanh điểm gối đo được; chờ lấy kết nối 2 giây, thất bại nhanh bằng 503 `SERVICE_OVERLOADED` (§7).
- **Luồng ảo không đổi quy tắc này:** hàng nghìn luồng ảo chờ I/O cùng lúc là được, nhưng chúng xếp hàng ở nhóm kết nối; mọi phần không cần CSDL (gọi AI, Redis, tính JSON) phải **ra khỏi giao dịch** trước khi giữ kết nối (ADR-GEO-03).
- **Tiến trình/nút:** một JVM mỗi nút (luồng ảo, không nhiều tiến trình). Số nút khởi điểm **2** (tính sẵn sàng khi một nút hỏng, không phải do năng lực); nhu cầu CPU Java mỗi yêu cầu (phân tích JSON, kiểm chữ ký thẻ, tuần tự hóa) **chưa đo** và là việc của thử tải HTTP trên khung mã thật (NFR-GEO-001…006, 012).
- **Phần dư năng lực của tầng CSDL trên máy này:** nhu cầu thiết kế mới 581,4 yêu cầu/giây (gồm thăm dò) ≈ 3,5% thông lượng hỗn hợp đo được ở lượt sửa sau RV (16.138 giao dịch/giây ở 16 kết nối, `07_verification.md` §4.6); lượt trước 433,3 ≈ 4% của 11.873. Con số này chỉ có nghĩa là CSDL không phải nút cổ chai ở 1000 người dùng theo giả định này; trên máy sản xuất (đĩa, mạng, CSDL tách máy) kết quả khác.

## 5. Kế hoạch bộ nhớ đệm (Redis, ADR-GEO-05)

Nguyên tắc: Redis chỉ chứa thứ **tái tạo được** và **không quyết định quyền**. Khóa dữ liệu trường có tiền tố `t:{org}:`; nội dung dùng chung `p:`.

| Cái gì | Khóa | TTL | Vô hiệu hóa | Ghi chú |
|---|---|---|---|---|
| Đề + bản dựng hình (phần bất biến của Q04) | `p:pv:{problem_version_id}` | 24 giờ ± 10% | không cần (bất biến theo version); version mới có khóa mới | Chỉ đề kho (~60 đề). Đề tự nhập: không đệm (ít dùng lại) |
| Nội dung lời giải (đọc SAU khi cổng `fn_get_solution` trả `OK`) | `p:sol:{problem_version_id}` | 24 giờ ± 10% | không cần (bất biến, BR-11) | **Quyết định "được xem" luôn tính ở CSDL theo (org, user, assignment), không bao giờ đệm** (D-GEO-157). Khóa quyết định `t:{org}:open:{assignment_id}` của bản trước **đã bỏ** (RV-09) |
| `release_at` của bài giao cho điểm cuối trạng thái (Q04b) | `t:{org}:rel:{assignment_id}` → (`class_id`/`owner_user_id`, `release_at`, `release_rev`) | **≤ 2 giây** | xóa khóa và phát kênh `rel` khi `fn_set_release` thành công | Chỉ để thiết bị biết lúc nào gọi lời giải; kiểm `class_id` ∈ `cls` của thẻ khi trúng đệm. Giảm thăm dò của ~30 học sinh cùng bài xuống ≤ 0,5 truy vấn/giây/bài |
| Bài giảng theo từ khóa (Q11) | `p:kw:{term_key}` → bài giảng | 1 giờ ± 10% | xóa khi bài giảng sửa (chặng 2) | `p:lec:{lecture_id}` tương tự |
| Danh sách kho đề (Q13) | `p:cat:{bộ lọc}:{cursor}` | 60 giây | tăng bộ đếm phiên bản `p:cat:ver` khi nạp/duyệt đề | Chỉ giáo viên đọc |
| Giới hạn tốc độ | `rl:{user}:{loại}:{cửa sổ}` | bằng cửa sổ | tự hết hạn | §7 |
| Lũy đẳng lô sự kiện mức A | `idem:{batch_id}` | 10 phút | tự hết hạn | ADR-GEO-11; mức B dùng `durable_request` ở CSDL (bền) |
| Danh sách đen thẻ | `deny:{sub}` | bằng TTL thẻ còn lại | tự hết hạn | ADR-GEO-07 |

- **Dung lượng ước tính:** nội dung kho (60 đề × ≈ 5 KB, 60 lời giải × ≈ 5 KB, 300 bài giảng × ≈ 2 KB) < 2 MB; khóa idempotency `116,7 lô/giây × 600 giây × ≈ 100 B ≈ 7 MB`; còn lại nhỏ. Tổng < 50 MB (giả định; `dev-up.sh` cấp 512 MB, chính sách `allkeys-lru`).
- **Vô hiệu hóa khi giáo viên đổi giờ mở:** `fn_set_release` thành công → ứng dụng `DEL t:{org}:rel:{assignment}`, `PUBLISH rel {assignment_id}:{rev}` để các bộ đệm trong tiến trình bỏ bản cũ; `release_rev` trong mọi phản hồi cho phép thiết bị phát hiện thay đổi. Nếu Redis không sẵn, bộ đệm 2 giây tự hết hạn; không có lỗi đúng/sai nào phụ thuộc vào việc vô hiệu hóa (chỉ độ trễ ≤ 2 giây của trạng thái "chưa mở" mới).
- **Không đệm:** quyết định "được xem lời giải" (dù đã mở hay chưa); gợi ý theo người (đi qua `fn_get_hint`); dữ liệu học sinh (tiến độ, sự kiện, câu trả lời hỏi đáp); bất kỳ thứ gì quyết định quyền.

## 6. Đỉnh giờ mở: gộp yêu cầu giống nhau, tránh bầy đàn

Luồng một học sinh khi giờ mở tới (chi tiết quyết định ở ADR-GEO-10):
1. Thiết bị đặt hẹn giờ tại `release_in_seconds + nhiễu ∈ [0, 3 giây)`; thăm dò mỗi 6 giây + nhiễu [0; 1,5) giây suốt thời gian trang chờ khóa (D-GEO-157), và ngay khi `visibilitychange` (hiện lại) hoặc `online` (D-GEO-132). Bảo đảm cập nhật < 10 giây sau `release_at` kể cả "Mở ngay" (7,5 + 2 đệm + thời gian yêu cầu; ADR-GEO-10 mục 4).
2. Yêu cầu xem lời giải → **cổng ở CSDL** (`fn_get_solution`, mọi yêu cầu, không đệm quyết định) → nếu `status = OK`, nội dung lấy từ Redis `p:sol:` (miss: một luồng nạp, các luồng khác chờ cùng kết quả: **single-flight** theo `problem_version_id` trong tiến trình + `SET NX` ngắn ở Redis) → ghi nhận sự kiện `unlock` bền, lũy đẳng theo `request_id` (`fn_record_durable`).
3. Tránh "bầy đàn": TTL nhiễu ±10%; làm nóng `p:sol:` khi giáo viên đặt giờ; thăm dò có nhiễu; giới hạn tốc độ theo người dùng; nếu quá tải: 503 + `Retry-After` ngẫu nhiên 1 đến 3 giây (không đồng loạt).

**Kết quả đo lượt sửa sau RV** (máy cục bộ, `bench/run_pgbench_rv.sh` mục C, CSDL `geo_arch2`, hàm mới trả `status`, `unlock` qua `fn_record_durable`; 10 giây mỗi phép đo): cổng thuần 46.634 / 61.392 / 62.586 giao dịch/giây ở 20 / 50 / 100 kết nối (0,43 / 0,81 / 1,60 ms); giao dịch đầy đủ 19.593 / 20.516 / 21.087 (1,02 / 2,44 / 4,74 ms); 0 lỗi (`07_verification.md` §4.6). Số lượt trước (lược đồ cũ, `geo_arch` đã phình sau thử tải, máy nhiễu hơn) giữ dưới đây để đối chiếu, không so trực tiếp được:

**Kết quả đo lượt trước (máy cục bộ, `bench/run_pgbench.sh` mục C):**

| Kịch bản | Kết nối | tps | Trung bình (ms) | Ý nghĩa |
|---|---|---|---|---|
| Chỉ cổng quyết định `fn_get_solution` (đọc) | 20 | 6.295 | 3,18 | Nhu cầu đỉnh 100/giây, ca xấu nhất 1000/1 giây: ≥ 6 lần dư so với 1000/giây (lần chạy nhiễu: 6.295 đến 12.358) |
| | 50 | 8.637 | 5,79 | |
| | 100 | 12.358 | 8,09 | |
| Giao dịch đầy đủ (cổng + sự kiện `unlock` bền + tiến độ, commit đồng bộ) | 20 | 8.578 | 2,33 | Nhu cầu đỉnh ≈ 117/giây |
| | 50 | 8.381 | 5,97 | |
| | 100 | 6.901 | 14,49 | |

Giao dịch đầy đủ chịu ≈ 7.000 đến 8.500 giao dịch/giây, trong khi ca xấu nhất (1000 dồn 1 giây) cần 1000/giây: dư ≈ 7 lần ở CSDL. Phần còn lại (nhóm kết nối 20 mỗi nút, JVM, mạng) chưa đo: nếu 1000 yêu cầu dồn trong 1 giây vào 2 nút × 20 kết nối, mỗi giao dịch 3 ms ⇒ tối đa `40 / 0,003 ≈ 13.000` giao dịch/giây lý thuyết, đủ; nhưng độ trễ đuôi tăng khi xếp hàng ở nhóm kết nối: do đó NFR-GEO-005 chỉ đòi p95 ≤ 2 giây ở ca xấu nhất.

## 7. Giới hạn tốc độ, hết thời gian chờ, áp lực ngược

| Hạng mục | Đề xuất (ARCH; không có trong nguồn, chờ TechLead) | Lý do |
|---|---|---|
| Giới hạn theo người dùng, xem lời giải | ≤ 5 yêu cầu/10 giây | Người dùng thường nhấn 1 đến 2 lần; chặn vòng lặp lỗi phía thiết bị |
| Giới hạn theo người dùng, lô sự kiện | ≤ 1 lô/2 giây, cho đột biến 5 lô | Thiết bị gửi ≤ 1 lô/10 giây |
| Giới hạn theo người dùng, hỏi đáp | ≤ 1 yêu cầu/3 giây và ≤ 30/10 phút | Mỗi lượt là một lần gọi AI có chi phí (TRU-03) |
| Giới hạn theo người dùng, đọc chung | ≤ 10 yêu cầu/giây | Gấp 30 lần mức thường (1 yêu cầu/3 giây) |
| Giới hạn theo IP (chung) | **Không đặt chặt**: ≥ 3.000 yêu cầu/phút | Một trường (có thể ≈ 1.500 học sinh) thường dùng chung một địa chỉ IP công cộng; giới hạn IP chặt sẽ chặn cả trường |
| Đăng nhập Giáo viên mẫu | **10 lần sai / 15 phút theo IP** ⇒ `RATE_LIMITED` (cả mã đúng) (D-GEO-152, 173); thêm theo tài khoản khi selector khớp; bộ đếm ở CSDL (`auth_failure`), không phụ thuộc Redis | Mã 60 bit: với 10 lần/15 phút mỗi IP, dò hết cần ≈ 10^15 năm-IP; đăng nhập giáo viên hiếm (vài lần/giờ) nên đếm ở CSDL rẻ (Q24 0,008 ms; 300.000 hàng sai vẫn 0,046 ms, §3.2 của 07) |
| Đăng nhập nhẹ học sinh | không khóa riêng (D-GEO-107); chịu giới hạn IP chung | — |
| Chờ nhóm kết nối, `statement_timeout`, `idle_in_transaction`, Redis | Số ở `app/docs/CONFIG.md` §1, §2 (D-GEO-168): 2 giây ⇒ 503 `SERVICE_OVERLOADED` + `Retry-After`; 2 giây (nền 30 giây); 10 giây; 100 ms, mở khi lỗi sang CSDL | Thất bại nhanh; mọi truy vấn nóng < 0,25 ms đo được nên 2 giây là lưới an toàn; Redis không là điểm hỏng đơn |
| Kiểm soát đáp án | thời hạn cứng 200 ms, quá thì đóng (KS-5, ADR-GEO-14) | Không giữ kết nối CSDL; không gọi thêm mô hình |
| Cổng AI | TBD (D-GEO-072: owner TechLead; khung mã đặt tạm ở CONFIG.md) | Thời gian chờ AI không giữ kết nối CSDL |
| Rào cách ly (semaphore) | AI: theo hạn mức nhà cung cấp; ghi lô: ≤ 50% nhóm kết nối; đường xem lời giải: ≤ 50% nhóm kết nối; còn lại dùng chung | Một đường chậm không làm cạn nhóm cho đường khác |
| Áp lực ngược | Hàng đợi ghi sự kiện trong bộ nhớ có trần (ví dụ 20.000 sự kiện/nút); đầy ⇒ bỏ sự kiện mức A (không bền) và tăng bộ đếm `events_dropped`, **không bao giờ bỏ mức B**; ghi mức B quá tải ⇒ 503 | Chấp nhận mất thao tác hình, không chấp nhận mất gợi ý/lời giải đã xem |

Đo: số liệu `hikaricp_connections_pending`, tỉ lệ 429/503 theo loại, `events_dropped`. **Mức chịu tải cố định lượt sửa sau RV** (`run_pgbench_rv.sh` mục D): hỗn hợp §2 + thăm dò Q04b ca xấu (mọi lần xuống CSDL, 23% giao dịch) ở 650 giao dịch/giây, 32 kết nối, 30 giây: đạt 629,6 giao dịch/giây, trung bình 3,96 ms (gồm độ trễ lập lịch của `-R`), độ lệch chuẩn 2,84 ms, **0/18.882** giao dịch vượt 50 ms, 0 lỗi, 0 bỏ qua. Lượt trước: `pgbench -R 500` hỗn hợp, 32 kết nối, 30 giây: trung bình **2,83 ms**, độ lệch chuẩn 2,63 ms, **0,067%** giao dịch vượt 50 ms (10/14.998), 0 lỗi, 0 giao dịch bị bỏ qua (tải 500 yêu cầu/giây = 1,5× tải thiết kế; chỉ phần CSDL).

## 8. Gom ghi sự kiện theo lô: đánh đổi độ bền và quy tắc (ADR-GEO-11)
| Loại sự kiện | Mức | Gửi | Commit | Mất tối đa khi sập đột ngột | Vì sao |
|---|---|---|---|---|---|
| `tool`, `scene_shown` | A (không bền, D-GEO-126, 165) | gom ≤ 10 giây hoặc ≤ 50 sự kiện; ngay khi ẩn tab; `fn_record_batch` | `synchronous_commit = off` (cục bộ giao dịch) | lô chưa gửi ≤ 10 giây + ≈ 600 ms commit chưa ghi đĩa | khối lượng lớn nhất, không đổi quyền hay lời giải |
| `open`, `hint`, `ask`, `unlock` | B (bền, D-GEO-126, 162) | gửi ngay, một giao dịch, `request_id` lũy đẳng (`fn_record_durable`, `fn_get_hint`, `fn_record_qa`) | `synchronous_commit = on` | không mất sau khi trả thành công; gửi lại không đếm hai lần | đổi quyền (BR-05), bằng chứng cho giáo viên, "Đã mở bài"/"Chưa bắt đầu" (M-GEO-14, 24) |
Quy tắc: (0) mọi ghi sự kiện và tiến độ đi qua hàm SECURITY DEFINER, `geo_app` không ghi thẳng (D-GEO-158); (1) sự kiện bền kèm cập nhật `assignment_progress` trong **cùng giao dịch** (không bao giờ có "đã xem lời giải" ở một nơi mà không ở nơi kia); (2) sự kiện A nhận qua `batch_id` lũy đẳng (Redis `SET NX`), sự kiện B qua `request_id` (`durable_request`, `qa_turn.request_id`); khoảng nghỉ ≥ 30 phút 0 giây mở phiên mới (D-GEO-144); (3) thời điểm sự kiện do thiết bị gửi bị **kẹp** vào [giờ nhận − 24 giờ, giờ nhận] để không chui vào partition sai; (4) trang theo dõi lớp đọc bộ đếm, không bao giờ `count(*)` sự kiện; (5) có truy vấn **đối soát** (`07_verification.md`): bộ đếm tiến độ phải khớp tính lại từ `event_log` cho dữ liệu ghi bằng đường chuẩn.

## 9. Đo bằng cách nào (từng chỉ số)
| Chỉ số | Công cụ | Điểm đo | Ngưỡng liên quan |
|---|---|---|---|
| p95 đọc/ghi, tỉ lệ lỗi theo điểm cuối | Micrometer (`http.server.requests` có phân vị) + công cụ thử tải HTTP | Máy chủ ứng dụng (và phía khách trong thử tải) | NFR-GEO-001…004 |
| Thời gian truy vấn CSDL, số trang bộ đệm | `pg_stat_statements` (đã bật ở `dev-up.sh`), `auto_explain` ngưỡng 100 ms, `EXPLAIN (ANALYZE, BUFFERS)` theo catalogue | CSDL | NFR-GEO-007, 042, 043 |
| Nhóm kết nối | `hikaricp_connections_{active,pending,timeout}` | Máy chủ ứng dụng | NFR-GEO-011 |
| Số câu lệnh mỗi yêu cầu (N+1) | Bộ đếm câu lệnh JDBC trong kiểm thử tích hợp | Kiểm thử | NFR-GEO-009 |
| Độ trễ cập nhật trạng thái khóa | Ca tự động đo từ `release_at` tới lúc nút mở, có kiểm tab nền | Kiểm thử trình duyệt | NFR-GEO-008 |
| Tải đỉnh mở lời giải | Kịch bản thử tải: 1000 người dùng trong cửa sổ 10 giây và 1 giây, chồng tải đều | Thử tải | NFR-GEO-004, 005 |
| Sự kiện bị bỏ, lô trùng | Bộ đếm `events_dropped`, `batches_duplicate`; truy vấn đối soát | Máy chủ + CSDL | NFR-GEO-050…053 |
| Partition | Truy vấn giám sát (`count` ở partition default, số partition tương lai) hằng ngày | CSDL | NFR-GEO-045 |
| Redis | `INFO` (hit/miss, bộ nhớ, bị đẩy khóa), độ trễ lệnh | Redis | NFR-GEO-057 |
| Kích thước và phình | `07_sizes.sql`, `pg_stat_user_tables` (bloat, HOT) | CSDL | NFR-GEO-044, 048 |

## 10. Chưa đo và giả định cần xác nhận
- Chưa đo: tầng Java (CPU, bộ nhớ, luồng ảo, ghim), Redis, mạng thật, hai nút ứng dụng, CSDL tách máy, đĩa thật có ép ghi vật lý. Cần thử tải HTTP trên khung mã thật.
- Giả định cần xác nhận: tỉ lệ hỗn hợp §2; ca xấu "cả 1000 phiên đang chờ khóa" của thăm dò; `s̄ = 20` sự kiện/lô và kịch bản "2 giờ mỗi ngày" §3; các trần giới hạn tốc độ §7; 2 nút ứng dụng. (Cỡ lớp: không giới hạn ở MVP, Q09 phân trang theo tên, D-GEO-167.)
- Câu hỏi cho TechLead: hạ tầng thật (CPU/RAM/đĩa của CSDL, có PgBouncer không), công cụ thử tải HTTP, giám sát. Đã phán: mất tối đa ≈ 10,6 giây sự kiện thao tác hình được chấp nhận (D-GEO-126); MVP chưa dọn sự kiện (D-GEO-128), nên dung lượng §3 tăng không giới hạn cho tới khi ChuDauTu chốt chính sách lưu giữ.
