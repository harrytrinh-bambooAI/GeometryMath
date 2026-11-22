# 02 — Mô hình đa trường (liên trường)

> Vai: ARCH. Trạng thái: **đề xuất**. Nguồn: `brief.md` §1, §5(b), §7 "Đa trường"; `DOMAIN-PROFILE.md` §8 (BR-12, BR-28, BR-31), §9 (thực thể, "Cấp phép chia sẻ"); `PO-DECISIONS.md` PO-14; `work/03_decisions.md` D-GEO-012, D-GEO-050 (cách ly theo trường bắt buộc, không có màn quản trị trường), D-GEO-051 (mã lớp toàn hệ thống), D-GEO-054 (dữ liệu mẫu), D-GEO-076 (kho đề và bài giảng mẫu là nội dung hệ thống dùng chung, chỉ đọc từ mọi trường), D-GEO-127 (RLS là lớp phòng thủ thứ hai), D-GEO-153 (giáo viên chỉ xem lời giải đề kho), D-GEO-158 (quyền ghi), D-GEO-160 (tác giả nền tảng).
> Nhật ký sửa (dấu vết): lượt sửa sau RV: T3 thay ngoại lệ "khóa ngoại đơn tới nội dung nền tảng" bằng cột `content_org_id` + khóa ngoại tổ hợp + CHECK (RV-12); RLS có chính sách sẵn trong lược đồ và `fn_student_login`/`fn_teacher_login` (D-GEO-127); bảng mới `teacher_access_code`, `auth_failure`, `problem_signature`, `solid_template`, `durable_request`, `answer_guard_log`, `probe_*`. Lược đồ tương ứng: `03_schema.sql`. Số đo: `07_verification.md`.
> Phạm vi MVP: **cách ly dữ liệu theo trường**. Không có quản trị trường, tổ chức nhiều cấp có giao diện (IAM-04, chặng 5), chia sẻ liên trường có logic (chặng 7). Phần dưới chỉ chừa chỗ ở mức bảng và cột.

## 1. Cây tổ chức: nền tảng → nhóm trường → trường

- Bảng `org(id, parent_id, kind, name)`, `kind ∈ {platform, group, school}`. Ràng buộc `org_tree_ck`: đúng một hàng `platform` (chỉ mục duy nhất `org_single_platform_uq`) và là hàng duy nhất không có cha. Nhóm trường có cha là nền tảng; trường có cha là nhóm hoặc nền tảng (MVP: trường có thể treo thẳng dưới nền tảng).
- **Nguyên tắc dữ liệu:** mọi dữ liệu của người học thuộc **đúng một trường** (`school`). Cây chỉ dùng cho (a) nội dung dùng chung (nền tảng), (b) chặng sau: chính sách theo nhóm trường (BR-29), cấp phép liên trường (BR-28). MVP **không có truy vấn nào đi theo cây**: không có "quản trị nhóm trường xem dữ liệu của các trường". BR-31: quản trị nền tảng không mặc nhiên xem nội dung học tập; MVP không có vai đó.
- Không cần đường dẫn vật chất hóa (ltree) hay bảng đóng (closure) ở MVP: độ sâu cố định 3 và không có truy vấn cây. Khi chặng 5 cần "mọi trường dưới nhóm X", dùng `WITH RECURSIVE` trên bảng nhỏ này (hàng chục đến hàng trăm hàng).
- Người dùng thuộc đúng một trường (`app_user.org_id`). Một người giữ nhiều vai theo phạm vi (DOMAIN-PROFILE §4) là chặng sau: thêm bảng `org_membership(user_id, org_id, role)` không đổi các bảng hiện có (cột `app_user.role` còn nghĩa là vai chính).

## 2. Cột định danh trường ở mọi bảng và vị trí trong chỉ mục

**Quy tắc T1.** Mọi bảng chứa dữ liệu thuộc trường có cột `org_id uuid NOT NULL`. Danh sách (xem `03_schema.sql`): `app_user, teacher_access_code, class, class_member, problem, problem_version, problem_signature, scene_build, hint, solution, verification_report, assignment, assignment_progress, event_log, durable_request, ai_call_log, answer_guard_log, qa_turn, lecture, keyword, share_grant (org_id = bên cấp, thêm grantee_org_id), audit_log`. Bảng không có `org_id` vì không thuộc trường: `org` (chính nó), `subject`, `problem_topic`, `solid_template` (danh mục dùng chung), `probe_prompt`, `probe_run` (dữ liệu kiểm thử của bộ câu thử), `auth_failure` (lần đăng nhập giáo viên thất bại, xảy ra trước khi biết trường; chỉ đọc/ghi qua `fn_teacher_login`).

**Quy tắc T2 (vị trí trong chỉ mục).** `org_id` là **cột đầu** của mọi chỉ mục tổ hợp phục vụ truy vấn trong phạm vi một trường. Lý do: (1) mọi truy vấn của người dùng đã biết `org_id` từ thẻ (ADR-GEO-07), nên điều kiện bằng `org_id` luôn có sẵn; (2) các hàng của cùng một trường nằm gần nhau trong chỉ mục (vị trí cục bộ, ít trang hơn); (3) chuẩn bị cho chia theo `org_id` về sau nếu cần (partition theo hàm băm hoặc phân tán); (4) một truy vấn quên `org_id` thì **không dùng được chỉ mục** mà lộ ra ở kiểm kế hoạch (`EXPLAIN` hiện `Filter`/`Seq Scan`), nên lỗi bị bắt sớm.
Ngoại lệ có chủ đích, ghi ngay tại chỉ mục trong `03_schema.sql`:
| Chỉ mục | Vì sao không bắt đầu bằng org_id |
|---|---|
| `class_join_code_uq (join_code)` | Đăng nhập nhẹ chưa biết trường; mã lớp là khóa toàn cục (D-GEO-051 phương án A) |
| `teacher_access_code_selector_uq (selector)` | Đăng nhập Giáo viên mẫu chưa biết trường (D-GEO-152); chỉ dùng trong `fn_teacher_login` |
| `auth_failure_ip_idx (ip_key, at)`, `auth_failure_user_idx (user_id, at)` | Bảng không thuộc trường (trước đăng nhập) |
| `share_grant_lookup_idx (grantee_org_id, ...)` | Khóa tra là trường nhận |
| các khóa chính `id` UUID | UUIDv7 duy nhất toàn cục; truy vấn vẫn kèm `org_id` làm điều kiện (kết nối `a.org_id = ...` rồi nối qua cặp `(org_id, id)` ở khóa duy nhất) |

**Quy tắc T3 (nhất quán chéo bảng ở tầng dữ liệu).** Các quan hệ trong cùng một trường dùng **khóa ngoại tổ hợp `(org_id, id)`**: cần `UNIQUE (org_id, id)` ở bảng cha (`app_user, class, problem, problem_version, assignment, lecture`). Hệ quả: không thể ghi một `assignment_progress` của trường A trỏ vào `assignment` của trường B; không thể xếp học sinh trường B vào lớp trường A (kiểm bằng `06_isolation_test.sql` D4, D5: đã chạy, PASS). Quan hệ trỏ nội dung có thể là **nền tảng** dùng cột tổ chức nội dung tường minh thay cho khóa ngoại đơn (RV-12): `assignment.content_org_id` + khóa ngoại tổ hợp `(content_org_id, problem_id, problem_version_id) → problem_version (org_id, problem_id, id)` + CHECK `assignment_content_ck` (bài lớp: nội dung = nền tảng; bài tự học: nội dung = chính trường), cộng khóa ngoại `(org_id, problem_id, owner_user_id) → problem` (bài tự học chỉ dùng đề của chính em). Các quan hệ khác RV-12 nêu cũng thành tổ hợp: `assignment.lock_source_assignment_id`, `qa_turn.user_id`, `problem_version.created_by`, `solution.author_user_id`, `problem.author_user_id`. Kiểm: `06_isolation_test.sql` D4, D5, D9, D9b, D10, D11, D13 (PASS). Đề kho chỉ ở hàng nền tảng (`problem_scope_ck`), nên "bài lớp trỏ đề tự nhập trường khác" bị chặn cả ở CHECK lẫn khóa ngoại.

## 3. Chặn đọc chéo trường

### 3.1. Lớp 1 (bắt buộc): mức ứng dụng
1. `TenantContext` bất biến dựng từ thẻ (ADR-GEO-07). Mọi phương thức kho dữ liệu nhận `TenantContext`, **không có phương thức nhận `orgId` rời**.
2. Mọi câu SQL trên bảng của trường **bắt buộc** có điều kiện `org_id = :org` (xem catalogue: mọi truy vấn). Kiểm tự động: luật ArchUnit/linter SQL "mẫu bị cấm" trong `04_query_catalogue.md` (R-Q10, R-Q11) và **kiểm thử cách ly trên mọi điểm vào** (NFR-GEO-011: với mỗi API danh sách/chi tiết, gọi bằng thẻ trường B vào id của trường A, mong đợi 404 hoặc rỗng; không bao giờ 403 kèm dữ liệu).
3. Tư cách lớp (BR-12) kiểm **trong cùng câu truy vấn** (Q04, Q05: `EXISTS class_member`), không tách thành một lần kiểm riêng (tránh kiểm-rồi-dùng và N+1).
4. Tài nguyên của trường khác (hoặc của lớp mình không thuộc) luôn trả 404 `NOT_FOUND`, để không lộ sự tồn tại. 403 `FORBIDDEN` chỉ khi tài nguyên thuộc phạm vi người gọi nhưng chưa được phép (bài của lớp mình đang khóa, BR-GEO-251; giáo viên khác cùng trường đổi giờ bài không phải lớp mình). Hàm cổng trả thẳng mã trạng thái (`03_schema.sql` §9).

### 3.2. Lớp 2 (D-GEO-127: bật sau khi đo trên khung mã thật): Row-Level Security
RLS đặt chính sách theo hàng ngay trong PostgreSQL: `USING (org_id = (SELECT current_setting('app.org_id')::uuid))`, ứng dụng đặt `set_config('app.org_id', <org>, true)` đầu mỗi giao dịch (an toàn với nhóm kết nối vì `true` = chỉ trong giao dịch). Vai kết nối `geo_app` không phải chủ bảng nên RLS có hiệu lực (không cần `FORCE`).

| | Ưu | Nhược |
|---|---|---|
| **Chỉ mức ứng dụng** | Không tốn chi phí thực thi; kế hoạch đơn giản; mọi truy vấn đã có `org_id` đứng đầu chỉ mục | Một câu truy vấn viết sót `org_id` (hoặc một lỗi ORM, một phương thức mới) là rò rỉ; chỉ phát hiện bằng kiểm thử/rà soát |
| **Thêm RLS** | Rào chắn cuối cùng: truy vấn quên `org_id` vẫn không đọc/ghi được dữ liệu trường khác; kiểm chứng được bằng một ca (xem số đo bên dưới) | Phải đặt `app.org_id` mỗi giao dịch (thêm một lệnh `set_config`, buộc truy vấn đọc đơn lẻ cũng nằm trong giao dịch tường minh); đường tiền xác thực (đăng nhập bằng mã lớp, Q01) chưa biết trường nên phải đi qua hàm SECURITY DEFINER riêng; hàm SECURITY DEFINER và vai chủ bỏ qua RLS nên **không thay thế** cho kiểm trong hàm; bảng nền tảng cần chính sách `org_id IN (platform, :org)`; thêm điều kiện vào kế hoạch |

**Số đo chi phí RLS** (máy cục bộ, `bench/run_rls.sh`, 16 kết nối, pgbench giao thức prepared; kết quả đầy đủ ở `07_verification.md` §RLS và `bench/results/rls.txt`): xem bảng ở `07_verification.md` (điền từ lần chạy cuối; ba điều kiện: cơ sở không RLS, có `set_config` nhưng RLS tắt, RLS bật). Kết luận và khuyến nghị nằm ngay đó.

**Đã phán (D-GEO-127):** RLS là lớp phòng thủ thứ hai cho bảng dữ liệu học sinh, bật sau khi đo chi phí trên khung mã thật. Lược đồ đã có sẵn chính sách (`03_schema.sql` §13: `tenant_iso` cho `app_user, class, class_member, assignment, assignment_progress, event_log, qa_turn, audit_log`; `tenant_or_platform` cho `problem, problem_version, scene_build, hint, problem_signature`, ghi chỉ trong trường mình), chưa bật; migration bật là `bench/rls_enable.sql`. `app.org_id` không đặt ⇒ 0 hàng (đóng khi thiếu ngữ cảnh). Đường tiền xác thực đã thành hàm SECURITY DEFINER: `fn_student_login` (thay Q01, tạo học sinh), `fn_teacher_login` (Q24). Bộ kiểm cách ly 103 ca chạy cả khi RLS bật: 103 PASS (`07_verification.md` §5). Lý do chọn nhóm bảng (giữ từ bản trước): bật RLS cho các bảng **dữ liệu học sinh** nơi một lỗi lọc gây rò rỉ nghiêm trọng nhất: `assignment_progress`, `event_log`, `qa_turn`, `class_member`, `app_user` (tên hiển thị trẻ em), `assignment`. Bảng nội dung dùng chung (đề, bài giảng) chỉ đọc, không cần RLS (bảo vệ bằng quyền ghi). Lý do: dữ liệu trẻ em và BR-12 là rủi ro cao; mức chi phí đo được xem ở `07`. **Điều kiện đi kèm:** (i) mọi đường vào dữ liệu đi qua một hàm `withTenant(ctx, ...)` đặt `app.org_id` trong giao dịch; (ii) hàm đăng nhập đã viết (`fn_student_login`, `fn_teacher_login`); (iii) chạy lại toàn bộ catalogue với RLS bật trên khung mã thật trước khi bật ở sản xuất (việc của CODE/TechLead khi đo, D-GEO-127). Nếu TechLead không bật RLS ở MVP, giữ nguyên lớp 1 và thêm kiểm thử cách ly NFR-GEO-011 như rào chắn; mô hình dữ liệu không đổi (chỉ thêm chính sách) nên có thể bật sau (`bench/05_rls_on.sql` là bản chính sách đã thử).

### 3.3. Cách ly ở tầng khác
- **Bộ nhớ đệm Redis:** mọi khóa có tiền tố trường `t:{org}:…` cho dữ liệu thuộc trường; khóa nội dung dùng chung `p:…`. Không có khóa nào chứa dữ liệu trường mà thiếu tiền tố (kiểm bằng quy ước tên khóa trong một lớp tạo khóa duy nhất).
- **Nhật ký và số đo:** nhãn `org` không được đưa vào nhãn số đo có độ đa dạng cao (50 trường là chấp nhận được; hàng nghìn thì không), chỉ dùng cho nhật ký truy vết.
- **Cổng AI:** chỉ gửi nội dung đề và câu hỏi; không gửi tên hiển thị, mã lớp, `org_id` cho nhà cung cấp mô hình (D-GEO-067).

## 4. Phương án cô lập đã cân nhắc
| Phương án | Đánh giá cho GEO |
|---|---|
| **Một lược đồ dùng chung + `org_id` (chọn)** | Thích hợp 50 đến hàng nghìn trường, cùng một cụm; chi phí vận hành thấp nhất; nội dung dùng chung và cấp phép liên trường (chặng 7) tự nhiên vì cùng cơ sở dữ liệu; rủi ro là lỗi lọc, giảm nhờ khóa ngoại tổ hợp, RLS tùy chọn và kiểm thử |
| Lược đồ riêng mỗi trường | Cách ly mạnh hơn nhưng 50 trường × ~20 bảng = 1000 bảng, nhân bản mọi migration, kế hoạch/bộ nhớ đệm catalog phình; truy vấn liên trường và kho dùng chung phức tạp |
| CSDL riêng mỗi trường | Cách ly tối đa; chi phí và độ phức tạp vận hành cao; không hợp tải này. Có thể là lựa chọn cho một trường lớn có yêu cầu pháp lý riêng về nơi lưu dữ liệu (chưa có nguồn; PO-21 còn mở) |
Hướng nâng cấp nếu cần: vì `org_id` đứng đầu chỉ mục và khóa ngoại tổ hợp, có thể chuyển một trường sang cụm riêng bằng cách sao chép theo `org_id` mà không đổi mã truy vấn.

## 5. Chia sẻ liên trường bằng cấp phép tường minh (chừa chỗ cho chặng 7)

Bảng `share_grant` (đã có trong `03_schema.sql`, MVP không có hàng nào, không có logic):
- Cột: `org_id` (trường **sở hữu**, bên cấp), `grantee_org_id` (trường nhận), `resource_kind` (`problem`, `lecture`; thêm giá trị bằng `ALTER TYPE ... ADD VALUE` khi chặng 7 cần nhóm học hay sản phẩm của lớp), `resource_id`, `purpose` (bắt buộc, BR-28 "có mục đích"), `valid_from`/`valid_until` (BR-28 "có thời hạn"), `status`, `requested_by`, `grantor_approved_at`, `grantee_approved_at`, `revoked_at/by`.
- **Vòng đời** (DOMAIN-PROFILE §9): `requested` → `approved` (cả hai trường duyệt, có hiệu lực từ–đến) → hết hạn (suy ra: `valid_until <= now()`, không cần tác vụ nền đổi trạng thái) hoặc `revoked`; hoặc `rejected`. CHECK `share_approved_ck` bảo đảm `approved` luôn có đủ hai lần duyệt và có hạn; `share_revoked_ck` bảo đảm nhất quán thu hồi; `share_distinct_orgs_ck` cấm tự cấp.
- **Điểm kiểm đọc chéo trường:** `Q18`: "trường X có cấp phép còn hiệu lực để đọc tài nguyên R không", chỉ mục một phần `share_grant_lookup_idx (grantee_org_id, resource_kind, resource_id) WHERE status = 'approved'`. Mọi đường đọc chéo trường ở chặng 7 **phải** gọi điểm kiểm này (hoặc nối vào nó); mặc định là từ chối. Đo trên 50.000 cấp phép ở `07_verification.md`.
- **Điều bảng này cố ý không làm ở MVP:** không có đường đọc nào dùng nó; không chọn mô hình quyền chi tiết (đọc đề, đọc kết quả em, nhóm học) vì chưa có nguồn thiết kế (STAGES-AFTER-MVP không phải nguồn, D-GEO-078, R15). Dữ liệu học sinh (tiến độ, sự kiện, câu hỏi) không được là `resource_kind` nếu chưa có quyết định pháp lý trẻ em (PO-21, PO-11 ghi rõ rủi ro liên trường); ghi vào khoảng trống đã biết.
- **Vì sao đủ để chặng 7 không làm lại:** (1) có chủ sở hữu và bên nhận ở cấp trường; (2) có hạn và mục đích; (3) có thu hồi; (4) thêm loại tài nguyên là thêm giá trị enum, không đổi bảng; (5) điểm kiểm là một truy vấn chỉ mục hóa.

## 6. Nội dung dùng chung của nền tảng so với dữ liệu riêng của trường (D-GEO-076)
| | Nội dung dùng chung (kho đề, bài giảng mẫu) | Dữ liệu riêng của trường |
|---|---|---|
| Chủ sở hữu | Hàng `org` kiểu `platform` (một hàng, `platform_org_id()`); đứng tên tác giả nền tảng (tài khoản `system`, tên hiển thị "Giáo viên mẫu", D-GEO-160, 179) | Một trường; mỗi trường có tài khoản Giáo viên mẫu riêng để giao bài |
| Bảng | `problem` (source=`catalog`), `problem_version`, `problem_signature`, `scene_build`, `hint`, `solution`, `verification_report`, `lecture`, `keyword` có `org_id` = hàng nền tảng; `solid_template` (không org) | `app_user, class, class_member, assignment, assignment_progress, event_log, qa_turn, audit_log`, đề tự nhập (`problem.source='student_input'`, `org_id` = trường của em) |
| Đọc | Mọi trường **chỉ đọc**; truy vấn lọc `org_id = :platform` (hằng số cấu hình, không phải đầu vào người dùng) | Chỉ trường chủ |
| Ghi | Chỉ vai nạp nội dung `geo_content` (D-GEO-158). `geo_app`/`geo_ai` ghi vào hàng nền tảng bị trigger `tg_platform_write_guard` từ chối (ca R2, R2b) | Ứng dụng qua `TenantContext`; dữ liệu nhạy cảm (người dùng, bài giao, tiến độ, sự kiện, hỏi đáp, nhật ký) chỉ qua hàm SECURITY DEFINER |
| Cách tới học sinh | **Chỉ qua bài giao** (`assignment.content_org_id`, `problem_version_id`): học sinh gọi Kho đề nhận `FORBIDDEN` (D-GEO-131). Quyền đọc nội dung do tư cách lớp quyết định, không do `org_id` của nội dung. Giáo viên xem lời giải chỉ đề kho (`fn_get_solution_teacher`, D-GEO-153) | Trực tiếp theo tư cách |
| Cache | Khóa `p:…`, TTL dài, bất biến theo `problem_version_id` | Khóa `t:{org}:…`, TTL ngắn |
| Chặng sau | Nội dung của giáo viên thật thuộc trường (chặng 2): `org_id` = trường; chia sẻ cho trường khác qua `share_grant` (không đổi bảng) | |
Hệ quả thiết kế: không bao giờ trộn hai loại trong một chỉ mục cần sắp xếp theo `id` khi một truy vấn đọc cả nền tảng lẫn trường (sẽ phải sắp lại); với MVP truy vấn kho đề chỉ đọc nền tảng, nên không phát sinh. Khi chặng 2 thêm đề của trường, truy vấn kho đề sẽ có `org_id IN (platform, :org)`: dùng hợp hai lần quét chỉ mục (`UNION ALL` có `ORDER BY` và `LIMIT` cho từng nhánh) thay vì `ANY`; ghi vào khoảng trống đã biết.

## 7. Điều cần xác nhận về đa trường
| # | Câu hỏi | Mặc định đang dùng | Người |
|---|---|---|---|
| T-01 | Bật RLS ở MVP cho nhóm bảng dữ liệu học sinh? | **Đã phán D-GEO-127**: bật sau khi đo trên khung mã thật; chính sách có sẵn, migration `bench/rls_enable.sql` | TechLead (đo) |
| T-02 | Chính sách xóa/xuất dữ liệu của một trường (rời hệ thống): vì dữ liệu cùng cơ sở dữ liệu, xóa theo `org_id` là một thao tác theo lô | Chưa thiết kế (PO-21, D-GEO-067) | ChuDauTu, pháp lý |
| T-03 | Trường có yêu cầu nơi lưu dữ liệu riêng không (quyết định giữa lược đồ chung và CSDL riêng)? | Không (một cụm) | ChuDauTu |
