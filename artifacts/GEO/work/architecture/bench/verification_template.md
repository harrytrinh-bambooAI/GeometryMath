# 07 — Kiểm chứng kiến trúc, lược đồ và truy vấn (số đo thật)

> Vai: ARCH. Tệp này do `python3 bench/build_verification.py` dựng từ `bench/results/` (kết quả chạy thật) cộng phần lời; không gõ tay số đo. Trạng thái: **đề xuất**.
> **Lượt sửa gom sau phản biện RV** (`review/RV_findings.md`, D-GEO-150…183): lược đồ dựng lại vào CSDL **mới `geo_arch2`** (cụm `127.0.0.1:54329`; `geo_arch` của lượt trước giữ nguyên làm bằng chứng). Đã chạy lại ở `geo_arch2`: nạp dữ liệu cỡ brief đầy đủ (§2), EXPLAIN toàn bộ catalogue (§3), kế hoạch tổng quát (§3.1), kiểm quy mô cho bảng/chỉ mục mới (§3.2), pgbench rút gọn cho đường ghi mới qua hàm (§4.6), bộ kiểm cách ly **103 ca, cả khi RLS tắt và bật** (§5), kiểm RLS (§6), kích thước sạch (§7.2), lint (§8). **Giữ số của lượt trước** (lược đồ cũ trên `geo_arch`, ghi rõ tại chỗ): pgbench đầy đủ §4.1…4.5, chi phí RLS §6 (bảng), thí nghiệm chỉ mục §7.1, kích thước sau thử tải §7.3, đo mẫu xấu ở catalogue §5.
> **Phạm vi và giới hạn của số đo (đọc trước):** tất cả số đo là trên **máy một nút cục bộ** (10 lõi, 24 GB RAM), PostgreSQL 17.11 và Redis dựng bằng `app/scripts/dev-up.sh` (cụm riêng, cổng 54329 và 6389), **bộ sinh tải (`pgbench`) chạy cùng máy** nên tranh CPU với CSDL; bộ nhớ đệm ấm (dữ liệu nằm hết trong RAM: 24 GB so với vài GB dữ liệu); macOS không ép ghi đĩa vật lý ở `fsync` nên độ trễ commit đồng bộ thấp hơn đĩa thật; các tiến trình khác trên máy (các agent khác, biên dịch) làm số đo nhiễu (vì vậy mỗi truy vấn lấy **trung vị của 7 lần**; thông lượng `pgbench` dao động ± 20% giữa các lần). **Không đo:** tầng Java (không có mã Java của ARCH), Redis, mạng thật, nhiều nút ứng dụng, CSDL tách máy. Đây là bằng chứng về **hình dạng**: chỉ mục được dùng hay không, đường cong theo số kết nối, chi phí tương đối; **không phải kết quả sản xuất**, không đủ để cam kết ngưỡng.

## 1. Cách chạy lại
```
cd artifacts/GEO/work/architecture
./bench/00_reset.sh                    # dựng lại $PGDATABASE (mặc định geo_arch2): lược đồ + móc kiểm thử + dữ liệu (≈ 15 giây); từ chối geo_arch
./bench/run_queries.sh                 # EXPLAIN (ANALYZE, BUFFERS) mọi truy vấn của catalogue
psql -f bench/06_isolation_test.sql    # 103 ca cách ly; lặp lại sau psql -f bench/rls_enable.sql để chạy khi RLS bật
psql -f bench/11_rv_scale_checks.sql   # kiểm quy mô cho bảng/chỉ mục mới của lượt sửa sau RV
./bench/run_pgbench_rv.sh              # pgbench rút gọn cho đường ghi qua hàm (≈ 5 phút)
./bench/run_all.sh                     # (lượt trước) toàn bộ gồm pgbench và RLS đầy đủ (≈ 25 phút)
python3 bench/build_catalogue.py       # dựng lại 04_query_catalogue.md từ kết quả
python3 bench/build_verification.py    # dựng lại tệp này
python3 bench/lint_queries.py          # kiểm tự động "mẫu bị cấm" trên catalogue
```
Tệp: `03_schema.sql` (lược đồ), `bench/test_hooks.sql` (móc kiểm thử, chỉ profile test, D-GEO-169), `bench/rls_enable.sql` (bật RLS, D-GEO-127), `bench/01_seed.sql` (dữ liệu), `bench/queries/*.sql` (truy vấn), `bench/run_queries.sh` (EXPLAIN), `bench/03_scale_checks.sql`, `bench/06_isolation_test.sql`, `bench/08_generic_plan_check.sql`, `bench/09_antipatterns.sql`, `bench/10_event_index_cost.sql`, `bench/pgbench/*.sql` + `run_pgbench.sh`, `bench/05_rls_*.sql` + `run_rls.sh`, `bench/07_sizes.sql`, `bench/lint_queries.py`.

## 2. Bộ dữ liệu thử (brief §6 câu 5, D-GEO-049: 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện)
Số hàng sau khi nạp ở `geo_arch2` (`bench/results_seed.txt`):
```
{{seed_tbl}}
```
- **50 trường** (5 nhóm × 10 trường) + 1 nền tảng; **600 lớp** (12 mỗi trường; 30 lớp lớn khoảng 55 đến 65 em, còn lại 18 đến 45 em; lớp lớn nhất ≈ 65); **20.000 học sinh**; 200 giáo viên; **6.000 bài giao cho lớp** (4 đến 12 mỗi lớp trong 90 ngày; 15% chưa đặt giờ mở, 60% đã mở, 25% sắp mở) và 8.000 bài tự học (5% kế thừa khóa); **≈ 178.000 hàng tiến độ** (85% học sinh có mở mỗi bài); **≈ 2,04 triệu sự kiện** (10 đến 20 mỗi học sinh-bài; 50% tập trung 19 đến 21 giờ mỗi ngày: đỉnh buổi tối), phân vào 4 partition tháng; ≈ 170.000 câu hỏi (`qa_turn`, có câu trả lời đã lưu, D-GEO-159); kho 60 đề + 8.000 đề tự nhập; 300 bài giảng (7 bài "chưa có người soát"), 900 từ khóa; 35 cấp phép liên trường mẫu. Lượt sửa sau RV thêm: tác giả nền tảng `system` (D-GEO-160); 200 mã Giáo viên mẫu (selector + HMAC, D-GEO-152); nhãn máy đặt qua báo cáo kiểm chứng (D-GEO-158); 7.260 chữ ký chuẩn (đề kho + 90% đề tự nhập; 10% "không dựng được hình"); 400 bài tự học dán lại đề lớp (cùng chữ ký với đề kho, văn bản khác); mỗi lớp không giao trùng `problem` (D-GEO-161).
- Sinh bằng SQL (`generate_series`, hạt giống cố định), không dùng tên người hay tổ chức thật (PO-14: tên "Trường mẫu n", "Học sinh n"). Số lớp THCS: một phần ba (D-GEO-054 yêu cầu có thêm một trường THCS để kiểm cách ly: đủ).
- Kích thước bộ dữ liệu sạch (trước mọi thử tải), xem §7.2.

## 3. Kết quả từng truy vấn nóng (EXPLAIN (ANALYZE, BUFFERS), vai `geo_app`)
Kế hoạch đầy đủ, SQL, chỉ mục phục vụ, cursor: `04_query_catalogue.md`. Tóm tắt (trung vị 7 lần):

{{tblQ}}

- **Tiêu chí đạt:** (a) không `Seq Scan` trên bảng lớn ở truy vấn nóng; (b) mọi truy vấn nóng ≪ ngưỡng 300 ms và ≤ 10 ms (NFR-GEO-007). Kết quả: thời gian thực thi lớn nhất là **{{qmax}} ms** (Q07b: gọi `fn_record_batch` ghi lô 50 sự kiện + tiến độ); truy vấn đọc chậm nhất là Q09 (theo dõi lớp, cả lớp 64 em theo tên) ≈ 0,11 ms; đường nóng nhất ở đỉnh giờ mở (Q06b, gọi `fn_get_solution`) ≈ 0,015 ms, 10 trang. `Seq Scan` còn lại: Q18 (35 hàng), Q24 (`auth_failure` rỗng; ở 300.000 hàng dùng chỉ mục, §3.2) và các partition rỗng ở Q20b; **không có Seq Scan trên bảng có hơn 10.000 hàng** (`bench/lint_queries.py`: 0 vi phạm).
- Dòng `…_call` đo cả thân hàm plpgsql qua một lần gọi (kế hoạch chỉ hiện Function Scan). Số trang bộ đệm: truy vấn một dòng ≤ 40 trang; Q09 575 trang (đọc theo tên, ≤ 1000 của NFR-GEO-043); Q07/Q07b (ghi 50 sự kiện) ≈ 260 trang.

### 3.1. Kế hoạch tổng quát (generic plan)
JDBC chuyển sang kế hoạch tổng quát sau vài lần thực thi câu lệnh chuẩn bị; ép bằng `plan_cache_mode = force_generic_plan` để kiểm các truy vấn có tham số kiểu khác nhau (uuid, enum, timestamptz, text). Chạy lại trên `geo_arch2` với dạng câu của lượt trước (Q02, Q09 theo `user_id`, Q12, Q13, Q16 theo băm, Q20); các hàm plpgsql mới tự dùng kế hoạch đã chuẩn bị trong phiên:

{{tblGP}}

Kết luận: kế hoạch vẫn dùng chỉ mục như kế hoạch riêng; Q12 chậm hơn ~1 ms ở lần gọi đầu vì phiên nạp từ điển `unaccent` (làm nóng một lần cho mỗi kết nối; nhóm kết nối giữ phiên ấm).

### 3.2. Ở quy mô lớn hơn
{{scale}}

## 4. Tải đồng thời (pgbench, vai `geo_app`, giao thức prepared)
**§4.1…4.5 là số của lượt trước** (lược đồ cũ, ghi thẳng bảng, CSDL `geo_arch`); đường ghi mới qua hàm SECURITY DEFINER đo ở **§4.6**. Mỗi phép đo 20 giây, 8 luồng `pgbench`, cùng máy. Kịch bản mô phỏng đúng các câu lệnh của catalogue (gồm giao dịch, sự kiện bền và cập nhật tiến độ), tham số lấy ngẫu nhiên từ 19.900 cặp (học sinh, bài). Chỉ đo phần CSDL.

### 4.1. Từng kịch bản riêng (16 kết nối)
{{tblA}}

### 4.2. Hỗn hợp theo mô hình `05_capacity.md` §2, theo số kết nối (độ co giãn)
{{tblB}}

Thông lượng tăng tới điểm gối rồi **giảm** khi thêm kết nối: bằng chứng cho ADR-GEO-04 (nhóm kết nối nhỏ, không nâng theo số người dùng).

### 4.3. Đỉnh giờ mở (1000 học sinh mở lời giải; cổng ở CSDL)
{{tblC}}

Nhu cầu: 100 yêu cầu/giây (M-GEO-12), ca xấu nhất 1000 dồn trong 1 giây. Cả đường chỉ đọc lẫn giao dịch đầy đủ (có ghi bền, commit đồng bộ) vượt nhu cầu nhiều lần ở tầng CSDL trên máy này.

### 4.4. Mức chịu tải cố định 500 giao dịch/giây (1,5× tải thiết kế), hỗn hợp, 32 kết nối, 30 giây
Trung bình **{{dlat}} ms**, độ lệch chuẩn {{dstd}} ms, thông lượng thực {{dtps}} tps; giao dịch vượt 50 ms: **{{dlate}}**; giao dịch bị bỏ qua (không theo kịp tốc độ yêu cầu): {{dskip}}; không lỗi.

### 4.5. Đường nhẹ nhất (chỉ cổng quyết định) theo số kết nối
{{tblE}}

### 4.6. Lượt sửa sau RV: đường ghi qua hàm, cổng trả `status`, thăm dò thường trực (`bench/run_pgbench_rv.sh`, `geo_arch2`)
Vai `geo_bench` (= `geo_app` + `geo_ai`, chỉ để đo; `p_qa` gọi `fn_record_qa` của cổng AI), giao thức prepared, **10 giây** mỗi phép đo, 8 luồng `pgbench`, cùng máy. Mọi ghi sự kiện/tiến độ đi qua `fn_record_batch`, `fn_record_durable`, `fn_get_hint`, `fn_record_qa`; xem lời giải gọi `fn_get_solution` rồi `fn_record_durable('unlock')` với `request_id` mới mỗi lần. Số lượt này cao hơn lượt trước ở nhiều kịch bản: máy ít nhiễu hơn và CSDL sạch (không phình sau thử tải), nên **không** đọc là "hàm nhanh hơn ghi thẳng"; điều đọc được là đường mới không có nút cổ chai và vẫn ≫ nhu cầu.

{{tblRVA}}

Hỗn hợp `05_capacity.md` §2 (không kể thăm dò) theo số kết nối:

{{tblRVB}}

Đỉnh giờ mở (1000 học sinh mở lời giải; cổng ở CSDL, không đệm quyết định, D-GEO-157):

{{tblRVC}}

Mức chịu tải cố định: hỗn hợp + **thăm dò Q04b ca xấu** (mọi lần thăm dò xuống CSDL, ≈ 23% giao dịch) ở 650 giao dịch/giây (nhu cầu đỉnh mới 581,4 yêu cầu/giây, `05_capacity.md` §2), 32 kết nối, 30 giây: thông lượng thực **{{rvdtps}}** tps, trung bình **{{rvdlat}} ms** (gồm độ trễ lập lịch của `-R`), độ lệch chuẩn {{rvdstd}} ms, giao dịch vượt 50 ms: **{{rvdlate}}**, bị bỏ qua: {{rvdskip}}, không lỗi.

## 5. Kiểm tra tách quyền, cách ly và bất biến ở tầng dữ liệu (`geo_arch2`: RLS tắt {{n_pass}} PASS, {{n_fail}} FAIL · RLS bật {{n_pass_rls}} PASS, {{n_fail_rls}} FAIL)
Chạy bằng `bench/06_isolation_test.sql` (superuser đổi vai `geo_app` / `geo_ai` / `geo_ops` / `geo_tester` theo ca; mọi thay đổi ROLLBACK), lần hai sau `bench/rls_enable.sql` (ứng dụng đặt `app.org_id` như `withTenant`). Nhóm ca:
- **A** (A1…A12) tách quyền bảng: không đọc/ghi `solution`, `verification_report`; không đọc `qa_turn.reply_text`, bảng mã giáo viên; không ghi thẳng `event_log`, `qa_turn`, `audit_log`.
- **R** (R1…R5c) **năm câu lệnh RV-GEO-10 phải BỊ TỪ CHỐI với vai `geo_app`**: tạo giáo viên mẫu, ghi kho nền tảng (đề và version), gắn nhãn "Đã kiểm chứng" (UPDATE và gọi hàm), sửa/chèn tiến độ, chèn bài giao có giờ mở quá khứ (cả qua hàm và CHECK với vai chủ).
- **T** (T1…T6) `fn_get_solution_teacher` chỉ đề kho (D-GEO-153; T2 tái hiện đúng ca RV-04: giáo viên trường A gọi lời giải bài tự học trường B ⇒ 0 hàng).
- **B** (B1…B11) cổng lời giải và máy trạng thái giờ mở, mã `NOT_FOUND` / `LOCKED` / `FORBIDDEN` / `CONFLICT_*` / `VALIDATION_PAST` (RV-21).
- **E** (E0…E3) ranh giới đúng R bằng móc kiểm thử `fn_test_set_release` của profile test (D-GEO-169); `geo_app` không gọi được móc.
- **C** (C1…C8) tự học: thứ tự gợi ý, lũy đẳng gợi ý, kế thừa khóa theo **chữ ký** (văn bản khác, băm khác), gợi ý đề kho cho bài kế thừa (D-GEO-155), "dán trước, giao sau" (RV-03), đề không dựng được hình không có lời giải tự học.
- **D** (D1…D17) bất biến, khóa ngoại tổ hợp và CHECK chéo trường (RV-12), `topic_id` đề kho (RV-31), `CONFLICT_DUPLICATE` khi giao trùng (D-GEO-161), nhật ký tạo bài.
- **G** (G1…G9) ghi sự kiện: EXISTS tư cách lớp (RV-26), mức A/B, `scene_shown`, `unlock` khi khóa bị từ chối, `open` bền lũy đẳng (RV-18, D-GEO-162), biên phiên 29:59/30:00 (RV-17), lưu câu trả lời hỏi đáp và lịch sử chỉ chủ đọc (D-GEO-159).
- **L** (L1…L8) đăng nhập: mã Giáo viên mẫu theo tài khoản, 10 lần sai/15 phút theo IP và theo tài khoản, thu hồi, hết hạn, chỉ HMAC (D-GEO-152, 173); `fn_student_login` chỉ tạo `student/light` (D-GEO-158).
- **V** (V1…V3) nhãn máy chỉ theo báo cáo kiểm chứng của chính version (D-GEO-158).

{{iso_md}}

## 6. Chi phí Row-Level Security (RLS)
Ba điều kiện cho cùng kịch bản (16 kết nối, 15 giây): (1) cơ sở: không RLS, không `set_config`; (2) có `set_config('app.org_id', …, true)` và giao dịch bọc nhưng RLS tắt (tách chi phí của riêng việc đặt ngữ cảnh); (3) RLS bật trên 14 bảng (chính sách `org_id = (SELECT current_setting('app.org_id')::uuid)`; bảng nội dung dùng chung: `org_id IN (nền tảng, trường hiện tại)`).

{{tblRLS}}

Đọc số: phần chi phí lớn nhất **không phải bản thân chính sách RLS mà là bắt buộc bọc mỗi thao tác trong giao dịch có `set_config`** (thêm vòng mạng: cột 2 so với cột 1, −15% đến −41% ở thử tải cục bộ, nặng nhất với kịch bản một câu lệnh). Bản thân chính sách (cột 3 so với cột 2) nằm trong nhiễu đo: −3% đến +22% (mỗi lần chạy trên máy dùng chung lệch ± 20%), tức không phát hiện được chi phí RLS lớn hơn nhiễu. Kế hoạch giữ nguyên chỉ mục (điều kiện chính sách thành `InitPlan` + `One-Time Filter`, tính một lần; kế hoạch Q09 với RLS bật vẫn dùng đúng chỉ mục). Kết luận thận trọng: RLS rẻ hơn nhiều so với yêu cầu giao dịch tường minh; mọi kịch bản vẫn ≫ nhu cầu.

**Lượt sửa sau RV:** chính sách RLS nay nằm sẵn trong `03_schema.sql` §13 (chưa bật), bật bằng `bench/rls_enable.sql` (D-GEO-127). Kiểm lại trên `geo_arch2` với chính sách mới (`app.org_id` không đặt ⇒ 0 hàng thay vì lỗi):
```
{{rls_check2}}
```
Bộ kiểm cách ly 103 ca khi RLS bật: xem §5. Bảng chi phí trên là của lượt trước (chính sách cũ `bench/05_rls_on.sql`, cùng dạng điều kiện).

Kiểm cô lập khi RLS bật, lượt trước (`bench/05_rls_check.sql`): đúng trường đọc được; **sai trường, kể cả quên điều kiện `org_id` trong câu SQL, cho 0 hàng** ở `assignment`, `assignment_progress`, `event_log`; nội dung nền tảng (kho đề) vẫn đọc được từ mọi trường; không đặt `app.org_id` thì **lỗi** (đóng an toàn, không rò rỉ).
```
{{rls_check}}
```
**Đã phán (D-GEO-127):** bật RLS cho bảng dữ liệu học sinh như lớp phòng thủ thứ hai sau khi đo chi phí trên khung mã thật; đường tiền xác thực đã thành hàm (`fn_student_login`, `fn_teacher_login`).

## 7. Chi phí ghi, kích thước và số chỉ mục
### 7.1. Số chỉ mục trên bảng ghi nhiều và chi phí mỗi chỉ mục
`event_log`: **đúng 1 chỉ mục btree mỗi partition** (không khóa chính, không khóa ngoại, không cột JSON); `ai_call_log`, `answer_guard_log`: 0 chỉ mục; `assignment_progress`: chỉ khóa chính, UPDATE đạt tỉ lệ HOT (§7.3); `qa_turn`: khóa chính + 3 chỉ mục (`qa_turn_assignment_idx` cho Q14, `qa_turn_user_idx` cho Q21, khóa duy nhất `(org_id, request_id)` cho lũy đẳng; ≈ 40 ghi/giây nên chi phí chấp nhận được); `durable_request`: chỉ khóa chính, dọn sau 24 giờ. Thí nghiệm dưới là của lượt trước (bảng `event_log` không đổi cấu trúc ngoài kiểu sự kiện `scene_shown`).

Thí nghiệm riêng (`bench/10_event_index_cost.sql`) cho 1.000.000 sự kiện, cùng dữ liệu, cùng một phiên:

{{tblEV}}

Đọc số: mỗi chỉ mục btree ngẫu nhiên theo học sinh làm **bảng sự kiện nở gấp đôi và chậm đi vài lần**; khóa chính cộng 3 chỉ mục "phòng thủ" (B) làm kích thước gấp ≈ 4 lần và thời gian chèn gấp ≈ 5 đến 6 lần so với không chỉ mục. Cấu hình đề xuất (A) giữ đúng một chỉ mục, phục vụ truy vấn drill-down Q20 (giáo viên xem dòng thời gian một học sinh) vì giá trị đo: 0,015 ms so với quét một partition cỡ trăm MB. Cũng đo hai phương án gọn hơn: **D** (bỏ `org_id` và `id` khỏi khóa) nhỏ hơn ≈ 32% nhưng mất khóa cursor ổn định; **E** (bỏ riêng `org_id`) nhỏ hơn ≈ 14%: không đáng đổi quy tắc "org_id dẫn đầu" (`02_multi_tenant.md` T2). **J** cho thấy một cột JSON nhỏ (≈ 40 byte) trên mọi hàng làm thêm ≈ 56 byte/hàng (+27%): vì vậy `event_log.arg` là `smallint` (mức gợi ý hoặc mã công cụ) thay cho `payload jsonb`. Nếu khối lượng sự kiện thực sự bền vững cao (xem `05_capacity.md` §3), chuyển sang D/E hoặc partition ngắn hơn là bước xem xét đầu tiên.

### 7.2. Kích thước bộ dữ liệu sạch (≈ 2 triệu sự kiện, `geo_arch2`)
```
{{sizes_initial}}
```

### 7.3. Sau thử tải pgbench (lượt trước, `geo_arch`: bảng sự kiện đã nhận hàng chục triệu hàng từ các kịch bản ghi)
Chỉ để thấy tăng trưởng: kích thước `event_log_y2026m11` sau thử tải; các bảng khác gần như không đổi. Cho thấy partition theo tháng cần thiết khi tải thật; MVP **không** dọn (D-GEO-128), nên giám sát kích thước là đầu vào cho quyết định lưu giữ.
```
{{sizes_after}}
```

## 8. Kiểm tự động "mẫu bị cấm"
`python3 bench/lint_queries.py` (luật S trên SQL của catalogue và luật K trên kế hoạch đã ghi):
```
{{lint}}
```
Tự kiểm bộ kiểm: 11 truy vấn xấu cố ý trong `bench/lint_selftest/` (OFFSET, `SELECT *`, không LIMIT, `count(*)`, `LIKE '%x%'`, hàm bọc cột, sự kiện không cận thời gian, thiếu `org_id`, JSON trong WHERE, đọc `solution`, "tham số IS NULL OR"): `{{lint_self_head}}`; cả 11 bị bắt.

## 9. Lịch sử sửa (trước/sau) trong quá trình kiểm chứng
| # | Phát hiện | Cách phát hiện | Trước | Sau |
|---|---|---|---|---|
| 1 | Q12 xếp hạng mọi bài khớp: chậm theo số bài khớp | `bench/03_scale_checks.sql` với 50.000 bài giảng (10.075 bài khớp) | v1: 22 đến 67 ms (nhiều lần đo, nhiễu máy), Bitmap Heap Scan 10.075 hàng + Sort | v2 (tập ứng viên ≤ 200 rồi lấy 5): 3,7 đến 6 ms |
| 2 | `geo_app` không gọi được `f_unaccent` | Chạy truy vấn bằng vai `geo_app` | `permission denied for function f_unaccent` | `GRANT EXECUTE ON FUNCTION f_unaccent TO geo_app, geo_ai` |
| 3 | Cột `qa_turn.answer_kind` làm kiểm "không bảng đọc được nào có cột answer" đỏ | Ca A5 của `06_isolation_test.sql` | tên gây nhầm với đáp số | đổi thành `reply_kind` (kiểu `qa_reply_kind`) |
| 4 | `fn_set_release` bản đầu cho khóa lại lời giải đã mở | Đối chiếu D-GEO-042 (phương án A) sau khi đọc `work/03_decisions.md` | cho đổi giờ ở mọi trạng thái | chỉ đổi khi chưa mở; giờ mới phải ở tương lai; "mở ngay" đặt `now()` của CSDL; ca B6b, B10, B10b |
| 5 | "Mở ngay" không thấy ngay trong cùng giao dịch kiểm thử | Ca B9: `now()` đứng yên trong giao dịch | truyền `clock_timestamp()` ⇒ lớn hơn `now()` ⇒ vẫn khóa | hàm tự lấy `now()`; ghi mẹo kiểm thử ở M-402 |
| 6 | Bảng sự kiện mỗi hàng có JSON nhỏ | `bench/10_event_index_cost.sql` cấu hình J | `payload jsonb` ≈ 264 byte/hàng | `arg smallint`: ≈ 207 byte/hàng |
| 7 | Ba chỉ mục không phục vụ truy vấn nào | Rà soát "mỗi chỉ mục có lý do truy vấn" | `org_parent_idx`, `verification_report_version_idx`, `share_grant_owner_idx` | đã bỏ; `keyword_lecture_idx` đổi thành `(org_id, lecture_id, term_key)` để phục vụ Q11d không Sort |
| 8 | Dữ liệu sinh ra 3,07 triệu sự kiện, lệch mục tiêu 2 triệu | Đếm sau nạp | 3.065.399 | 2.045.041 (chỉnh phân phối thao tác) |
| 9 | Bảng nhỏ trong dữ liệu thử cho kế hoạch Seq Scan (Q11b, Q18) | Đọc kế hoạch | Seq Scan 900 và 35 hàng | chứng minh dùng chỉ mục ở 150.000 từ khóa và 50.000 cấp phép (§3.2) |
| 10 | Khung kiểm "Seq Scan trên bảng lớn" báo nhầm partition rỗng | `lint_queries.py` | cảnh báo giả ở Q20b | phân biệt partition rỗng (không tính) |
| 11 | (RV-10) `geo_app` ghi được người dùng, kho nền tảng, nhãn máy, tiến độ, bài giao | RV chạy thử trên `geo_arch` | cả năm câu lệnh thành công | vai `geo_content`, `geo_ops`; hàm SECURITY DEFINER; trigger nền tảng và nhãn; CHECK giờ mở; ca R1…R5c PASS |
| 12 | (RV-04) `fn_get_solution_teacher` trả lời giải trường khác | RV chạy thử | nhận `final_answer` của trường B | chỉ đề kho nền tảng; ca T1…T6 PASS |
| 13 | Ca R1/R2 bản nháp dùng `geo.uuid_v7()` trong câu của `geo_app` ⇒ lỗi "permission denied for function" cũng là `insufficient_privilege`, PASS sai lý do | Đọc lỗi của lần chạy đầu | ca có thể PASS vì quyền hàm, không vì quyền bảng | ca dùng `gen_random_uuid()`; R1/R2 nay từ chối đúng ở bảng/trigger |
| 14 | `fn_self_lock_source` viết bằng `LANGUAGE sql SECURITY DEFINER` lập kế hoạch lại mỗi lần gọi | EXPLAIN Q05b | 0,469 ms | plpgsql: 0,024 ms (catalogue §4) |
| 15 | Bộ kiểm cách ly khi RLS bật: ca C5 FAIL và một INSERT bị chính sách chặn | Chạy lại với `rls_enable.sql` | ca đọc bảng bằng `geo_app` không đặt `app.org_id` | ca đặt `app.org_id` như `withTenant` của ứng dụng; 103/103 PASS (đúng hành vi: RLS chặn khi thiếu ngữ cảnh) |
| 16 | Dữ liệu thử lượt trước giao trùng `problem` cho cùng lớp (400 cặp, RV-16) | Chỉ mục duy nhất một phần mới làm nạp lỗi | `prob_n` ngẫu nhiên | `prob_n = 1 + (7i + 13g) mod 60` (khác nhau trong lớp) |
Tối đa 3 lượt tối ưu một truy vấn: Q12 dùng 1 lượt (lượt trước), Q05b/Q16 dùng 1 lượt (lượt này); không truy vấn nào còn vấn đề vượt ngưỡng.

## 10. Khoảng trống đã biết và điều chưa kiểm
| # | Khoảng trống | Hệ quả | Chủ |
|---|---|---|---|
| G-01 | Chưa đo tầng Java (CPU, luồng ảo, ghim, chữ ký thẻ), Redis, mạng, 2 nút ứng dụng, CSDL tách máy | NFR-GEO-001…006, 012 chỉ kiểm được khi có khung mã chạy được và thử tải HTTP | TechLead |
| G-02 | ~~Gần trùng chỉ theo băm~~ **Đóng (D-GEO-150, ADR-GEO-15):** chữ ký chuẩn; lược đồ, phép khớp, kiểm mỗi lần đã có và đã kiểm (C4, C7). Còn lại: thuật toán chuẩn hóa nhãn đỉnh/đối xứng nằm ở engine hình học (mã Java), chưa có | Ca F-GEO-226 chỉ đạt khi engine tính chữ ký đúng | Code (engine), QC (F-GEO-226) |
| G-03 | `qa_turn` (nay có câu trả lời ≤ 4.000 ký tự) chưa chia partition; MVP không dọn (D-GEO-128) | Bảng lớn nhanh hơn trước (≈ +400 byte/câu); ở tải bền vững cao cần chia | ChuDauTu, pháp lý (PO-21) |
| G-04 | Khối lượng sự kiện bền vững (`05_capacity.md` §3) có thể vượt xa 2 triệu; partition theo tháng có thể quá thô | Đổi tham số partition (tuần/ngày), xem D/E ở §7.1 | TechLead |
| G-05 | Chính sách RLS và `fn_student_login`/`fn_teacher_login` đã có; chưa đo chi phí trên khung mã thật (D-GEO-127) | Bật bằng `bench/rls_enable.sql` sau khi đo | Code, TechLead |
| G-06 | Truy vấn kho đề khi có đề của trường (chặng 2) cần `org_id IN (platform, :org)` | Dùng hợp hai nhánh có LIMIT; chưa đo | ARCH (chặng 2) |
| G-07 | ~~Cursor mờ~~ **Đóng:** khung mã đã cài AES-GCM gắn trường + `ListKind` + phiên bản 2 (M-504, `app/docs/CONFIG.md`) | — | — |
| G-08 | Độ bền commit đồng bộ đo trên macOS (không ép đĩa vật lý) | Độ trễ ghi bền trên đĩa thật cao hơn; cần đo lại | TechLead |
| G-09 | PgBouncer chế độ giao dịch chưa kiểm với câu lệnh chuẩn bị | Chỉ cần nếu số kết nối vượt giới hạn | TechLead |
| G-10 | ~~Lưu mã Giáo viên mẫu~~ **Đóng (D-GEO-152, 173):** `teacher_access_code`, `auth_failure`, `fn_teacher_login`; ca L1…L8 | Pepper và hạn dùng là cấu hình của khung mã | Code |
| G-11 | Đồng hồ thiết bị lệch chỉ ảnh hưởng hẹn giờ phía khách (ADR-GEO-10), không ảnh hưởng quyết định; chưa có ca thiết bị thật | Cần ca trình duyệt | QC |
| G-12 | Hỗn hợp tải và `s̄ = 20` sự kiện/lô là giả định | Cần số liệu thật từ giai đoạn thử | PO |
| G-13 | Kiểm soát đáp án (ADR-GEO-14) là mã Java của `ai-gateway`; lược đồ chỉ có `solution.answer_forms`, `answer_guard_log`, `probe_*`. Ngưỡng shingle và quy tắc số làm tròn (KS-2) là đề xuất | Cổng phát hành chỉ đo được khi bộ kiểm chạy trên bộ câu thử 22 câu | Code, QC; PO (câu hỏi M-406) |
| G-14 | Móc kiểm thử `fn_test_set_release` ở `bench/test_hooks.sql`; CODE chép vào `app/server/src/test/resources/db/testhooks/` (M-504) | Ca biên TC-GEO-0440, 0633, 0657, 0661, 0695 cần tệp ở profile test | Code |
| G-15 | Thăm dò trạng thái khóa (ADR-GEO-10) và đệm `release_at` chưa đo ở tầng Java/Redis; số 148 yêu cầu/giây là ca xấu tính tay | NFR-GEO-008, 064 đo bằng Playwright + Gatling | Code, QC |
