# Máy chủ "Hình Học Không Gian Cùng AI" (khung MVP)

Modular monolith Java: Spring Boot 4 + luồng ảo + PostgreSQL (SQL tường minh) + Redis. Chi tiết lựa chọn và giả định: [docs/STACK.md](docs/STACK.md). Mọi con số cấu hình: [docs/CONFIG.md](docs/CONFIG.md) (nguồn duy nhất).
Đây là KHUNG: chưa có chức năng MVP; chỉ có hạ tầng dùng chung, quy ước và công cụ kiểm tra chất lượng.

## Yêu cầu
- Java 26 (tối thiểu 25) trên PATH. Không cần cài Maven (dùng `server/mvnw`). Cần mạng lần đầu để tải phụ thuộc.
- PostgreSQL 17 và Redis cài qua Homebrew (đã có trên máy dự án). Cụm riêng của dự án: PostgreSQL `127.0.0.1:54329`, Redis `127.0.0.1:6389`.
  KHÔNG dùng cổng 5432/6379 (của người dùng).

## Dựng hạ tầng
```
scripts/dev-up.sh     # bật PostgreSQL + Redis riêng (dữ liệu ở .data/)
scripts/db-init.sh    # tạo geo_dev, geo_test, geo_e2e nếu chưa có
scripts/dev-down.sh   # tắt (chỉ cụm của thư mục này)
```
(hoặc `make up` / `make down`; `make ports` in cổng đang dùng)

### Chạy nhiều checkout / worktree song song (cổng riêng)
Mọi script và `make` đọc cổng qua `scripts/env.sh`: biến môi trường > tệp `app/.env.ports` (không commit) > mặc định.

| Biến | Mặc định (checkout chính) | Ví dụ worktree `mvp-foundation` |
|---|---|---|
| `GEO_PG_PORT` | 54329 | 54339 |
| `GEO_REDIS_PORT` | 6389 | 6399 |
| `GEO_PORT` (máy chủ) | 8080 | 8090 |
| `GEO_WEB_PORT` (Vite) | 5173 | 5174 |

Tạo `app/.env.ports` trong worktree:
```
GEO_PG_PORT=54339
GEO_REDIS_PORT=6399
GEO_PORT=8090
GEO_WEB_PORT=5174
```
Mỗi worktree có `app/.data` riêng (cụm PG + Redis riêng). `dev-up.sh` từ chối chạy nếu cổng đang do cụm của thư mục khác
phục vụ, nên không bao giờ dùng nhầm cơ sở dữ liệu của checkout chính. IT (`make verify`) và chốt chặn xóa schema cũng
theo `GEO_PG_PORT`. Thư mục quá sâu thì socket Unix của PG đặt ở `/tmp/geo-pg-<cổng>`.

## Giao diện và E2E
```
make web-install      # npm ci trong web/
make web-dev          # Vite ở GEO_WEB_PORT, proxy -> máy chủ GEO_PORT (chạy `make run` ở terminal khác)
make web-build        # typecheck + lint + test hàm thuần + build -> web/dist
make e2e              # bật máy chủ (DB geo_e2e) + web (bản build) trên cổng riêng, reset seed vàng, chạy api + ui,
                      # sinh e2e/evidence/<run>/index.html + summary.json + traceability.txt, rồi tắt
make e2e ARGS="--project=api"   # tham số thêm cho Playwright
make e2e-report       # in truy vết và mở báo cáo lượt mới nhất
make e2e-reset        # đưa geo_e2e về trạng thái vàng (DB=geo_dev cho dev)
make traceability ARGS="--all"  # TC nào có test, đạt/trượt, TC E2E chưa có test
```
Chạy trên máy dùng chung / tải cao (T-261126-43):
- `GEO_E2E_WORKERS=<n>`: số worker Playwright (mặc định nửa số lõi, tối thiểu 2). Máy đang tải cao thì hạ, ví dụ
  `GEO_E2E_WORKERS=2 make e2e`. Không bật retries để che lỗi.
- `GEO_E2E_EVIDENCE=fail|full`: mặc định `fail` (lượt chạy của task Dev) — ảnh chụp mọi test, video + trace chỉ giữ
  khi test trượt. `full` = video + trace cho mọi test như trước; dùng ở gate tester (T-26/T-33) và khi cần báo cáo bằng
  chứng đủ cho `make e2e-report`: `GEO_E2E_EVIDENCE=full make e2e`. Video + trace mọi test là phần tải lớn nhất của E2E.
- Máy chủ E2E dùng trần CSDL 10 s (`GEO_E2E_DB_STATEMENT_MS`, `GEO_E2E_DB_CONN_MS`) thay cho 2 s của sản phẩm: khi CPU
  bị chiếm, SELECT một dòng có thể quá 2 s và trả 504/503 giả. Trần 2 s được kiểm ở tầng tích hợp máy chủ; đặt hai biến
  về 2000 để chạy E2E với trần thật. Tương tự lệnh Redis: E2E 1000 ms (`GEO_E2E_REDIS_MS`) thay cho 100 ms. Chờ máy chủ/web lên: `GEO_E2E_SERVER_WAIT` (mặc định 180 s), `GEO_E2E_WEB_WAIT` (60 s).
- `dev-up.sh` chờ PostgreSQL và Redis nhận kết nối (lặp có hạn giờ `GEO_DEV_UP_WAIT`, mặc định 60 s) rồi mới kiểm cụm/thư mục.
- Test UI chờ theo phản hồi API (`e2e/support/wait.ts`: `untilApi`) thay vì chờ theo thời gian.

Chi tiết: [web/README.md](web/README.md), [e2e/README.md](e2e/README.md), seed: [db/seed/README.md](db/seed/README.md).

## Build, chạy
```
cd server
./mvnw -DskipTests package
java -jar target/geo-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev   # cổng 8080, DB geo_dev
# Không bật profile thì máy chủ từ chối khởi động (không còn mặc định dev; xem docs/CONFIG.md "Profile").
```
Thử nhanh:
```
curl localhost:8080/health
TOKEN=$(curl -s -X POST localhost:8080/dev/token -H 'Content-Type: application/json' \
  -d '{"orgId":"<uuid>","userId":"<uuid>","role":"TEACHER"}' | jq -r .token)
curl -H "Authorization: Bearer $TOKEN" 'localhost:8080/api/v1/sample/items?limit=20'
curl localhost:8080/actuator/prometheus
```
Cấu hình qua biến môi trường: `GEO_DB_URL`, `GEO_DB_USER`, `GEO_DB_PASSWORD`, `GEO_DB_POOL`, `GEO_REDIS_HOST/PORT/PREFIX`, `GEO_AUTH_SECRET`, `GEO_CURSOR_KEY`, `GEO_PORT`.
Môi trường thật dùng `--spring.profiles.active=prod` và BẮT BUỘC đặt các khóa; giá trị mặc định của dev không được dùng.

## Kiểm thử và kiểm tra chất lượng
```
scripts/verify.sh         # TRỌN BỘ (make verify): hạ tầng + Spotless + build + đơn vị + quét SQL + kiến trúc + tích hợp
make test                 # đơn vị + quét SQL + kiến trúc, không cần hạ tầng
make fmt                  # tự sửa định dạng (Spotless); verify chỉ kiểm, không sửa
```
| Kiểm tra | Ở đâu | Bắt gì |
|---|---|---|
| Định dạng | Spotless, pha `validate` | mã sai định dạng google-java-format |
| Quét SQL | `quality/SqlScanTest` + `SqlLint`, cấu hình `server/quality/sql-lint.properties` | OFFSET, SELECT * / alias.* / RETURNING *, LIKE đầu mẫu `%`, SELECT bảng lớn thiếu LIMIT, thiếu `org_id = :orgId` trên bảng thuộc trường, truy cập bảng của module khác, tham chiếu bảng cấm (`forbidden.app.tables`, không ngoại lệ); khóa cấu hình lạ |
| Kiến trúc | `quality/ArchitectureTest` (ArchUnit) | module khác đụng `internal`, chu trình giữa module, web gọi thẳng dữ liệu/JDBC, JDBC ngoài `repo`, JPA/Hibernate, `common` biết module, field injection |
| Đơn vị | `*Test` (surefire) | UUIDv7, con trỏ (gắn trường + danh sách + phiên bản), phân trang và trần theo danh sách, thẻ, cổng AI, mã lỗi + `Retry-After`, `@NoStore` |
| Tích hợp | `*IT` (failsafe) trên `geo_test`@GEO_PG_PORT + Redis GEO_REDIS_PORT (mặc định 54329/6389) tiền tố `geotest:` | schema sạch bằng Flyway mỗi lần chạy, health, lỗi thống nhất, 413, keyset qua HTTP, cách ly trường, kế hoạch truy vấn dùng chỉ mục, cấu hình khớp CONFIG.md (`ConfigIT`), số câu lệnh SQL mỗi yêu cầu (`StatementCountIT`), móc kiểm thử profile test |
| Thử tải | `loadtest/` Gatling (ngoài verify; `make loadtest-smoke`) | kịch bản khói `/health` |
| E2E | `e2e/` Playwright (ngoài verify; `make e2e`) | dự án `api`, `ui-chromium`, `ui-webkit` (iPad ngang); mọi test có ảnh chụp + trace (+ video với ui) trong `e2e/evidence/<run>/` |

IT tự xóa và dựng lại schema `geo_test` mỗi lần (có chốt chặn: từ chối nếu URL không phải `127.0.0.1:<GEO_PG_PORT>/geo_test`).

## Cấu trúc gói (`server/src/main/java/vn/geo/server`)
| Gói | Vai trò |
|---|---|
| `iam`, `tenancy`, `content`, `geometry`, `learning`, `answers`, `knowledge`, `events` | module sub-domain MVP, hiện rỗng. Mỗi module có `api` (công khai, nhỏ) và `internal` (web, service, repo) |
| `aigateway` | cổng AI: `api.AiGateway`; `internal` có `AiProvider`, `MockAiProvider` (giả lập), đo số lần gọi và độ trễ |
| `common` | hạ tầng dùng chung: `id` (UUIDv7), `error` (mô hình lỗi), `web` (mã yêu cầu, giới hạn thân), `security` (thẻ, `TenantContext`), `paging` (keyset), `db` (`TenantJdbc`), `redis`, `event`, `health`, `config`, `dev` |
| `sample` | lát cắt dọc mẫu web -> service -> repo (xóa khi có module thật, cùng migration V2) |

Luật: module khác chỉ dùng `<module>.api`; không đụng bảng của module khác (giao tiếp qua dịch vụ ứng dụng hoặc `DomainEvents`); `common` không biết module.

## Quy ước viết truy vấn (rút gọn)
1. SQL tường minh trong lớp `repo`, viết thành hằng chuỗi/text block (bộ quét chỉ thấy SQL hằng). Liệt kê cột, không `SELECT *`.
2. Bảng thuộc trường: chạy qua `TenantJdbc.sql(...)` và có `org_id = :orgId`; không bao giờ nhận mã trường từ client.
3. Danh sách: khai báo một `ListKind` (mã `<module>.<danh-sách>` + kiểu các khóa trước `id`), rồi phân trang keyset qua `Paging` (`request(kind, …)` -> truy vấn lấy `fetchSize()` dòng -> `page`). `ORDER BY <khóa…> DESC, id DESC`; trang sau dùng `(<khóa…>, id) < (:k…, :afterId)`. Không OFFSET, không đếm tổng. Trần riêng của danh sách đặt ở `geo.paging.max-limit-by-list`.
4. Chỉ mục khớp truy vấn: `(org_id, created_at DESC, id DESC)`; kiểm bằng `EXPLAIN` (xem `KeysetPagingIT`: không Seq Scan, không Sort).
5. Không N+1: lấy dữ liệu liên quan bằng một truy vấn JOIN hoặc `WHERE id = ANY(:ids)`; không gọi truy vấn trong vòng lặp. Kiểm bằng `SqlStatementCounter` (xem `StatementCountIT`): trang 1 dòng và trang đầy cùng số câu lệnh.
6. Tìm kiếm văn bản: không `LIKE '%x%'`; dùng chỉ mục trigram/FTS khi được duyệt, kèm `-- sqllint:allow=no_leading_wildcard_like` và lý do.
7. Khai báo bảng mới trong `server/quality/sql-lint.properties` (bảng lớn, bảng thuộc trường, chủ sở hữu).

## Thêm một module
1. Tạo `vn/geo/server/<ten>/api` và `.../internal/{web,service,repo}` (mỗi gói `package-info.java` mô tả).
2. Đặt giao diện dịch vụ/DTO/sự kiện công khai ở `api`; mọi thứ khác ở `internal`. ArchUnit tự áp luật theo mẫu gói, không cần đăng ký.
3. Nếu module có bảng: khai báo `owner.<bảng>=<ten>` trong `sql-lint.properties`; nếu thuộc trường thêm vào `tenant.tables`, nếu lớn thêm vào `large.tables`.
4. Thêm gói mới vào danh sách `commonDoesNotKnowModules` trong `ArchitectureTest`.

## Móc kiểm thử SQL (profile `test`)
SQL chỉ dùng cho kiểm thử (ví dụ hàm đặt giờ mở `fn_test_set_release(p_now)`, D-GEO-169) đặt ở
`server/src/test/resources/db/testhooks/R__<thứ tự>_<mô_tả>.sql`; chỉ profile `test` nạp, không vào gói chạy thật. Xem README ở thư mục đó.

## Thêm một migration
Tạo `server/src/main/resources/db/migration/V<số>__<mo_ta>.sql` (V10 trở lên cho schema chính thức). Không sửa migration đã áp dụng.
Bảng thuộc trường: `org_id uuid NOT NULL`, khóa chính `uuid`, `created_at timestamptz NOT NULL`, chỉ mục `(org_id, created_at DESC, id DESC)` cho danh sách. IT sẽ chạy lại toàn bộ migration trên schema sạch.

## Thử tải
Chưa có; xem [loadtest/README.md](loadtest/README.md).

## Chặng 2–3 (S2, S3)
Kế hoạch: `artifacts/GEO/work/s23/PLAN_code-s23.md` (module, cổng, khoảng migration, tệp sở hữu). Khung do T-261126-41 dựng sẵn:
- Giao diện: 15 chỗ cắm rỗng `web/src/features/s2e1…s2e9, s3e1…s3e6/index.tsx` (đã có trong `features/index.ts`); task chỉ sửa thư mục feature của mình.
- Module máy chủ mới `subject` (A1) và `notify` (A9): chỉ có `package-info.java`.
- E2E: spec P0 chạy cả trên điện thoại đặt tên `*.p0m.ui.spec.ts`; mã cố định fixture ở `db/seed/fixture-ids.d/<feature>.json` (`e2e/support/fixtures.ts` đọc gộp với `fixture-ids.json`).
- Truy vết: `make traceability ARGS="--strict --epic MVP,S2-E1 [--defer US-…]"` exit 1 khi TC tự động hóa (E2E-UI / E2E-API / IT / UT) thiếu test hoặc trượt. Mã TC: Playwright `@TC-S2E1-001` trong tiêu đề; JUnit `@DisplayName("TC-S2E1-001 …")`; test đơn vị web `it("TC-S2E1-001 …")`. Bí danh `MVP` = E1…E7, `S2`, `S3`.
- Tệp chung có mốc `--- T-261126-NN (…) ---` (sql-lint.properties, ArchitectureTest.java, application.yml, Makefile, tệp này): mỗi task chỉ thêm dòng ngay dưới mốc của mình. `make bench-s2` gọi `scripts/bench-s2.sh`.

<!-- --- T-261126-41 (khung) --- -->
<!-- --- T-261126-18 (S2-E1) --- -->
- **S2-E1 định danh PO-61 (T-261126-18):** module `iam` thêm đăng ký `POST /api/v1/auth/register` (tên + vai + mật khẩu + cờ đã đọc thông báo riêng tư), đăng nhập `POST /api/v1/auth/code-login` (mã `GV-`/`HS-` + mật khẩu, khóa 10 lần sai/15 phút theo tài khoản), khôi phục `POST /api/v1/auth/recover`, đặt mật khẩu lần đầu `POST /api/v1/auth/first-password`, `GET /api/v1/auth/privacy-notice`, `GET /api/v1/account/me`. Migration V110 (cột `app_user`, bảng `privacy_notice`, `class_member.status`). Luồng đăng nhập MVP giữ nguyên (A7). Màn web: `/hs/dang-ky`, `/gv/dang-ky`, `/hs/dang-nhap-ma`, `/gv/dang-nhap-ma`, `/hs/khoi-phuc`, `/hs/dat-mat-khau`. Chuyển tài khoản mẫu MVP: `make s2e1-migrate` (dev; `PROFILE=prod` bị từ chối).
<!-- --- T-261126-19 (S2-E2) --- -->
- **S2-E2 khung môn học + hình phẳng:** `subject.api.SubjectEngine` (parse, solve, verify, render, hint, grade + concepts; thao tác để mặc định = thiếu ⇒ `SubjectRegistry` từ chối lúc khởi động), `SubjectRegistry.forKind(ProblemKind)`; môn "Hình học" = `geometry/internal/GeometrySubject` (phẳng: `geometry/internal/plane`, không gian: `geometry/internal/solid/SolidAdapter` bọc engine MVP). API nội bộ `GET /api/v1/subjects`, `POST /api/v1/subjects/{id}/solve|render` (giáo viên). `problem.kind` (`space` | `plane`, V130), mô tả dựng `plane_problem_spec`, hợp đồng cảnh `docs/SCENE-CONTRACT-2D.md`, bộ vẽ `web/src/shared/scene2d`. Màn học sinh `/hs/bai-phang/:id` (`GET /api/v1/student/assignments/{id}/plane`).
- **Bộ đề chuẩn phẳng + bench:** `server/src/main/resources/content/plane/plane-benchmark.json` (30 đề PLS-nn) nạp bằng `PlaneBenchmarkLoader` (kiểm đáp số tham chiếu với engine) vào `plane_benchmark_problem`; `make bench-s2` (AI giả lập, từ chối khi có `GEO_AI_PROVIDER`≠mock hoặc `GEO_AI_API_KEY`) xuất `artifacts/GEO/evidence/S2-BENCH-<thời điểm>/report.md`.
<!-- --- T-261126-20 (S2-E5) --- -->
- **S2-E5 soát và duyệt lời giải có phiên bản (T-261126-20; US-S2E5-01…04, US-S2E5-05 thuộc T-261126-22):** module `content` thêm `internal/solution` (sổ lời giải `solution_doc`, phiên bản bất biến `solution_version`, kết quả `step_check_result`, ghim bài giao `solution_assignment_pin`) và `internal/review` (hộp duyệt `review_inbox_item`), migration V160. API giáo viên chủ đề (no-store; người khác `NOT_FOUND`, học sinh `FORBIDDEN`): `GET /api/v1/teacher/solutions[/{id}?version=n]`, `POST …/{id}/versions` (lưu thành phiên bản mới từ `baseVersion`, lệch bản mới nhất → `CONFLICT`), `POST …/{id}/versions/{n}/reuse` ("Dùng bản này"), `POST …/{id}/check` ("Kiểm tra lại", tối đa 10 s → `TIMEOUT`), `POST …/{id}/approve` (chặn `CONFLICT` khi còn bước lệch hoặc đáp số lệch engine; nhãn máy "Cần xem lại" phải gửi `confirmNeedsReview`), `POST …/{id}/proposals/{itemId}/accept|dismiss`, `GET …/{id}/step-checks`. Học sinh chỉ có `GET /api/v1/student/assignments/{id}/solution-labels` (nhãn hai trục của phiên bản đã giao, không bước giải). Nhãn máy chỉ do kết quả kiểm quyết định: thân yêu cầu có `machineLabel`/`machineStatus`/`verifyStatus` bị từ chối, trigger CSDL chặn `verified` khi kết quả không đủ. Cột nhạy cảm (`solution_version.steps`, `final_note`; `step_check_result.reason`, `written`, `computed`; `review_inbox_item.content`) vai `geo_app` không có SELECT (cùng nguyên tắc bảng `solution`, TC-GEO-0666): ứng dụng đọc qua hàm cổng `fn_solution_content_teacher` (chỉ giáo viên chủ sổ) và chép kết quả kiểm bằng `fn_solution_copy_checks`.
- **StepCheck:** `geometry/internal/stepcheck` (hàm thuần: `Expr` tính biểu thức, `StepCheck` kiểm từng bước theo giá trị bước trước, dung sai 0,01, lỗi không lan). Module `content` gọi qua bean `stepCheckEngine` kiểu JDK `BiFunction<List<String>, Map, Map>` (chưa có lớp ở `geometry.api`). Hình và đáp số đối chiếu lấy từ engine theo `solution_doc.figure` (`{"catalogCode": "HKN-02"}` hoặc `{"plane": {…}}`). Màn web: `/gv/soat-loi-giai`, `/gv/soat-loi-giai/:id` (C2-03), `/hs/nhan-loi-giai/:id`. Bài lớp mới tự ghim phiên bản "Giáo viên đã duyệt" hiện hành của sổ mà người giao sở hữu cho đúng version đề (trigger `assignment_pin_solution`).
<!-- --- T-261126-21 (S2-E8) --- -->
- **S2-E8 bài luyện điền đáp số + mô hình bộ bài chung (T-261126-21, PLAN A4/A5, PO-66 G1):** một mô hình `assignment_set` + `assignment_item` (module `assignment`, V180; đủ cột cho S3-E2: `status` draft/assigned/closed/archived, `due_at`, `release_at` theo bộ và theo item, cấu hình luyện theo item, `answer_snapshot`). Giao qua `fn_assign_practice_set`: mỗi item sinh một hàng `assignment` MVP (cổng giờ mở, khóa đáp số, hình `/plane`, lời giải dùng lại nguyên). Lượt thử `practice_attempt` và báo "Em nghĩ đáp số này đúng" `practice_report` ở module `tracking` (`tracking/internal/practice`), ghi lượt chỉ qua `fn_record_practice_attempt` (khóa theo câu + học sinh, chống lặp theo `requestKey`). `grade` chung ở `subject/internal/grade` (`AnswerGrader`: Đúng kèm dạng chuẩn / Sai / Sai cú pháp kèm vị trí; kiểu chính xác và xấp xỉ, số hữu tỉ tính chính xác); module khác gọi qua `assignment.api.PracticeGrader` (cầu nối bean `subjectAnswerGrader` — TẠM, chờ FIX đưa giao diện vào `subject.api`). API giáo viên: `GET /api/v1/teacher/practice/problems|classes`, `GET|POST /api/v1/teacher/practice-sets`, `GET|PUT|DELETE …/{id}`, `POST …/{id}/assign|close|preview/check`, `GET /api/v1/teacher/practice-reports[/metrics]`, `POST …/practice-reports/{id}/decision`. API học sinh: `GET /api/v1/student/practice-sets`, `GET …/{id}`, `GET …/{id}/result`, `POST /api/v1/student/practice-items/{assignmentId}/attempts|report`. Màn web: `/hs/bai-luyen`, `/hs/bai-luyen/:id` (C2-06, bàn phím toán), `/hs/bai-luyen/:id/ket-qua` (C2-07), `/gv/bai-luyen`, `/gv/bai-luyen/moi|:id` (C2-08), `/gv/bai-luyen/bao-cham-sai`. Móc kiểm thử dev/test: `POST /test-hooks/s2e8/sandbox`. Seed `db/seed/e2e/830_s2e8_core.sql` (bộ đề `s2e8_luyen`, tổ chức "Nhóm thử S2-E8").
<!-- --- T-261126-22 (S2-E3) --- -->
<!-- --- T-261126-23 (S2-E6, S2-E7) --- -->
- **S2-E6 Kho đề Của tôi / Kho chung, người soát + S2-E7 Trình chiếu (T-261126-23; US-S2E6-01…03, US-S2E7-01…03, PLAN A10):** migration V230. Module `iam`: vai thêm `user_role_grant` (chỉ `content_reviewer`, do chủ sản phẩm gán bằng SQL/seed, ứng dụng chỉ đọc) qua `iam.api.ContentReviewerRole`. Module `content` (`internal/library`): "Của tôi" = sổ lời giải `solution_doc` của chính giáo viên (S2-E5); Kho chung = bản chép ĐỘC LẬP `library_problem` lúc người soát duyệt (`fn_library_accept`, chép trong CSDL); đề xuất `library_submission` (pending → accepted | rejected kèm lý do 5…500 ký tự, tối đa 10 đề Chờ soát, mỗi đề một dòng pending, không tự soát); bộ đếm thêm đề `problem_create_event` (20 đề / 60 phút trượt theo tài khoản, mọi đường tạo đề gọi `content.api.LibraryQuota` — S2-E3/S2-E4 phải gọi). Kho chung theo tổ chức (TẠM). Cột `library_problem.steps` vai `geo_app` không SELECT: đọc qua `fn_library_steps_teacher` (giáo viên) và `fn_library_submission_steps` (người soát). API (no-store): `GET /api/v1/teacher/library?shelf=mine|library&kind=all|space|plane&q=&limit≤50&cursor=`, `POST …/library/problems` (dán chữ), `POST …/library/similar`, `GET …/library/problems/{id}`, `POST …/library/problems/{id}/submit`, `GET …/library/shared/{id}`, `POST …/library/shared/{id}/copy`, `GET …/library/review-queue[/{id}]`, `POST …/library/review-queue/{id}/accept|reject`; học sinh `GET /api/v1/student/library/{id}[/hint|/solution]` (lời giải luôn khóa ngoài bài giao, PO-66 G2). Trình chiếu: `GET /api/v1/teacher/present/{docId}/preflight` (hỏi lại theo giờ máy chủ khi lớp còn khóa) và `GET /api/v1/teacher/present/{docId}` (một lần, mọi dữ liệu buổi chiếu). Cấu hình (mặc định trong mã, ghi đè được) `geo.s2e6.create-limit` (20), `geo.s2e6.create-window` (PT60M), `geo.s2e6.pending-limit` (10), `geo.s2e7.reconnect-seconds` (5). Màn web: `/gv/kho-de-cua-toi` (rail "Kho của tôi"), `…/de/:id`, `…/kho-chung/:id`, `…/hang-doi-soat`, `/gv/trinh-chieu/:id` (dữ liệu chỉ trong bộ nhớ trang, không service worker). Seed `db/seed/e2e/850_s2e67_core.sql` (mỗi epic × TC × làn một tổ chức).
<!-- --- T-261126-24 (S2-E4) --- -->
<!-- --- T-261126-25 (S2-E9) --- -->
<!-- --- T-261126-42 (S3 dữ liệu mẫu) --- -->
<!-- --- T-261126-28 (S3-E1, S3-E5) --- -->
<!-- --- T-261126-29 (S3-E2) --- -->
<!-- --- T-261126-30 (S3-E3) --- -->
<!-- --- T-261126-31 (S3-E4, S3-E5) --- -->
<!-- --- T-261126-32 (S3-E6, S3-E5) --- -->
<!-- --- FIX (task FIX T-261126-35, 43+) --- -->
- **E2E theo mức ảnh hưởng (T-261126-45, DECISIONS 26/11):** `make e2e-affected` lấy diff so với `worktree-mvp-wave0` (đổi bằng `BASE=<ref>`), xếp mức theo bản đồ dữ liệu `artifacts/GEO/work/s23/impact-map.json` (luật regex: tệp -> epic sở hữu -> epic phụ thuộc; tệp không khớp luật nào = L2), rồi chạy `scripts/e2e.sh` MỘT lượt: L0 = spec của epic bị đụng + smoke; L1 = L0 + spec các epic phụ thuộc; L2 = trọn bộ; luôn trên `api` + `ui-chromium`, L0/L1 thêm spec P0 điện thoại (`MOBILE_P0`, `*.p0m.ui.spec.ts`) của màn bị đụng trên `mobile-pixel`/`mobile-iphone`. `make e2e-affected ARGS="--dry-run"` chỉ in mức, lý do, danh sách spec và biểu thức `-g`; lượt thật ghi `e2e/evidence/<run>/impact.json`. Thêm module / thư mục dùng chung / epic: sửa bản đồ rồi `make e2e-affected-selftest` (4 diff mẫu, exit 1 nếu lệch). Bộ smoke (~30 test đường chính: đăng nhập, Kho đề, giao bài, làm bài, theo dõi) gắn tag `@smoke`: `make e2e ARGS="-g @smoke"`.
