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
scripts/db-init.sh    # tạo geo_dev và geo_test nếu chưa có
scripts/dev-down.sh   # tắt
```
(hoặc `make up` / `make down`)

## Build, chạy
```
cd server
./mvnw -DskipTests package
java -jar target/geo-server-0.1.0-SNAPSHOT.jar        # profile dev, cổng 8080, DB geo_dev
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
| Tích hợp | `*IT` (failsafe) trên `geo_test`@54329 + Redis 6389 tiền tố `geotest:` | schema sạch bằng Flyway mỗi lần chạy, health, lỗi thống nhất, 413, keyset qua HTTP, cách ly trường, kế hoạch truy vấn dùng chỉ mục, cấu hình khớp CONFIG.md (`ConfigIT`), số câu lệnh SQL mỗi yêu cầu (`StatementCountIT`), móc kiểm thử profile test |
| Thử tải | `loadtest/` Gatling (ngoài verify; `make loadtest-smoke`) | kịch bản khói `/health` |
| E2E | `e2e/` Playwright (ngoài verify; `make e2e-smoke`) | test khói HTTP; trình duyệt WebKit khi có giao diện |

IT tự xóa và dựng lại schema `geo_test` mỗi lần (có chốt chặn: từ chối nếu URL không phải `geo_test@54329`).

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
