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
<!-- --- T-261126-21 (S2-E8) --- -->
<!-- --- T-261126-22 (S2-E3) --- -->
<!-- --- T-261126-23 (S2-E6, S2-E7) --- -->
<!-- --- T-261126-24 (S2-E4) --- -->
<!-- --- T-261126-25 (S2-E9) --- -->
<!-- --- T-261126-42 (S3 dữ liệu mẫu) --- -->
<!-- --- T-261126-28 (S3-E1, S3-E5) --- -->
<!-- --- T-261126-29 (S3-E2) --- -->
<!-- --- T-261126-30 (S3-E3) --- -->
<!-- --- T-261126-31 (S3-E4, S3-E5) --- -->
<!-- --- T-261126-32 (S3-E6, S3-E5) --- -->
<!-- --- FIX (task FIX T-261126-35, 43+) --- -->
