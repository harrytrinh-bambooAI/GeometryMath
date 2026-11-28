# Ngăn xếp kỹ thuật (khung MVP)

> Mọi con số cấu hình (nhóm kết nối, thời hạn, trần trang, con trỏ, thời gian, mã lỗi) nằm ở **[CONFIG.md](CONFIG.md)**, nguồn duy nhất (D-GEO-168). Tệp này chỉ nêu lựa chọn và lý do.

## Đã chọn
| Hạng mục | Chọn | Lý do ngắn |
|---|---|---|
| Ngôn ngữ/JVM | Java 26, biên dịch `--release 26`, chạy JVM 26 | Đã kiểm chứng chạy thật (xem dưới). Không cần hạ xuống 21. |
| Build | Maven + Maven Wrapper (`server/mvnw`, Maven 3.9.11 tải qua mạng) | Máy không có Maven trên PATH; wrapper commit vào kho nên ai cũng build giống nhau. Hệ sinh thái plugin (Spotless, Failsafe) sẵn. |
| Framework | Spring Boot 4.1.2, Spring MVC (servlet) | Trưởng thành, đầy đủ health/metrics/validation; chạy tốt trên Java 26. |
| Đồng thời | Luồng ảo (`spring.threads.virtual.enabled=true`) | Mô hình đồng bộ dễ đọc, chịu hàng nghìn yêu cầu chờ I/O mà không cần lập trình phản ứng. |
| Nhóm kết nối | HikariCP, 20 mỗi nút, chờ 2 giây rồi 503 `SERVICE_OVERLOADED` ([CONFIG.md](CONFIG.md) §1) | Số kết nối DB là nút thắt thật; luồng ảo xếp hàng chờ nhóm, thất bại nhanh thay vì xếp hàng vô hạn. Không nâng theo số người dùng (ADR-GEO-04). |
| Migration | Flyway (`db/migration`) | Chuẩn, có kiểm soát phiên bản. |
| Truy cập dữ liệu | `JdbcClient` với SQL tường minh | Không JPA/Hibernate (nguồn của N+1, lazy loading). Mọi SQL nhìn thấy và quét được. |
| Redis | Lettuce (qua spring-boot-starter-data-redis) | Theo yêu cầu; tiền tố khóa cấu hình qua `RedisKeys`. |
| Quan sát | Micrometer + Actuator + Prometheus (`/actuator/prometheus`) | Có số đo Hikari, HTTP, JVM, `geo.ai.*`. |
| JSON | Jackson 3 (`tools.jackson`, đi kèm Boot 4) | Mặc định của Boot 4; thời gian ISO-8601 UTC. |
| Xác thực khung | JWT HS256 tự cài (JDK `Mac`), không dùng Spring Security | Chỉ cần kiểm chữ ký + claim; ít phụ thuộc. Có thể thay bằng Spring Security/OIDC khi có luồng đăng nhập thật. |
| Phân trang | Keyset theo khóa sắp xếp kết thúc bằng `id` UUIDv7; con trỏ AES-256-GCM gắn trường + loại danh sách + phiên bản ([CONFIG.md](CONFIG.md) §4) | Không OFFSET, không COUNT; con trỏ mờ, không sửa được, dùng sai trường/danh sách thì `INVALID_CURSOR` (D-GEO-124, 168). |
| Định danh | UUIDv7 sinh ở ứng dụng | Tăng dần theo thời gian: chèn cuối chỉ mục, không phân mảnh như UUIDv4. |
| Lint | Spotless (google-java-format) kiểm ở pha `validate` | Chạy sớm; sửa bằng `make fmt`. |
| Kiểm thử | JUnit 5, AssertJ, ArchUnit 1.5.1, bộ quét SQL tự viết; datasource-proxy 1.11.1 (đếm câu lệnh JDBC); Gatling 3.16.1 Java DSL (`loadtest/`); Playwright 1.65.1 (`e2e/`) | D-GEO-170. Xem README. |

## Giao diện web và E2E (Đợt 0, PO-50, 22/11/2026)
| Hạng mục | Chọn (phiên bản ghim chính xác) | Lý do ngắn |
|---|---|---|
| Khung giao diện | React 19.3.1 + react-dom 19.3.1, react-router 8.5.2 | Chủ đầu tư chốt React (PO-50). Router dạng đối tượng để epic cắm route qua `src/features/<epic>/index.tsx`. |
| Build/dev | Vite 8.3.8 + @vitejs/plugin-react 6.3.0 | Dev nhanh (HMR), proxy sẵn tới máy chủ. |
| Ngôn ngữ | TypeScript 6.0.3 (strict) | 7.x (bản Go) chưa được typescript-eslint hỗ trợ (`<6.1`). |
| Lint | ESLint 10.15.0, @eslint/js 10.0.1, typescript-eslint 8.73.0, eslint-plugin-react-hooks 7.1.1, globals 17.14.0 | 0 cảnh báo; luật riêng cấm `toLocale*String` ngoài `src/shared/time` (BR-GEO-233). |
| Hình 3D | three 0.187.0 (+ @types/three 0.187.0), nạp động | Chỗ đặt sẵn; tính năng do E2/E3 làm. Prototype dùng r128 qua CDN; bản app dùng gói npm ghim. |
| Kiểm thử hàm thuần | `node --test` (Node ≥ 22.18 tự bỏ kiểu TS) | Không thêm Vitest/Jest cho Đợt 0. |
| Kiểu Node | @types/node 26.6.6 | cho `vite.config.ts`, test, script E2E. |
| E2E | @playwright/test 1.65.1 (Chromium 1255, WebKit 2388), typescript 6.0.3 (chỉ để typecheck) | D-GEO-170. Reporter bằng chứng và script truy vết tự viết, không thêm thư viện. |

**Cách phục vụ giao diện (quyết định):** dev = Vite (cổng `GEO_WEB_PORT`) proxy `/api`, `/health`, `/dev` sang máy chủ Java
(`GEO_PORT`); build = **tệp tĩnh `web/dist` phục vụ riêng** (reverse proxy/CDN cùng nguồn, chuyển `/api` vào máy chủ), KHÔNG
đóng vào jar. Lý do: (1) `make verify` và build Maven không phụ thuộc Node, ArchUnit/bộ quét SQL không đổi; (2) tệp tĩnh có băm
trong tên nên bộ nhớ đệm CDN lâu dài, máy chủ Java chỉ lo API; (3) không cần thêm bộ điều khiển "SPA fallback" và ngoại lệ cho
`AuthFilter`; (4) cùng nguồn qua proxy nên không cần CORS. E2E phục vụ đúng bản build bằng `vite preview` (cùng proxy).
Môi trường thật cần một quy tắc reverse proxy: `/api/*`, `/health` -> máy chủ; còn lại -> `dist/`, đường dẫn không có tệp -> `index.html`.

**Seed dữ liệu vàng:** SQL ngoài migration (`db/seed/`), nạp bằng `scripts/seed-reset.sh` chỉ trên geo_e2e/geo_dev; chi tiết và
phần fixture chưa có bảng: [db/seed/README.md](../db/seed/README.md).

**Chạy nhiều checkout song song:** cổng PG/Redis/máy chủ/web đọc từ biến môi trường hoặc `app/.env.ports` (`scripts/env.sh`);
`dev-up.sh` từ chối nếu cổng PG/Redis đang do cụm của thư mục khác phục vụ.

## Đã cân nhắc, không chọn
- **Quarkus / Micronaut:** khởi động nhanh, nhưng đội và hệ sinh thái thư viện nhỏ hơn; MVP không cần cold start nhanh.
- **WebFlux (phản ứng):** chịu tải cao nhưng khó đọc, khó gỡ lỗi; luồng ảo đạt mục tiêu với mã đồng bộ.
- **JPA/Hibernate:** bị loại theo yêu cầu (N+1, lazy). **jOOQ:** an toàn kiểu tốt nhưng cần sinh mã từ schema chưa có; `JdbcClient` đủ cho khung, có thể thêm jOOQ sau mà không phá ranh giới.
- **Gradle wrapper:** tương đương; chọn Maven vì có sẵn kho `~/.m2` và wrapper nhẹ hơn.
- **Checkstyle:** chỉ lint, không tự sửa; Spotless vừa kiểm vừa sửa.
- **Microservice:** quá sớm; modular monolith với ranh giới được kiểm bằng ArchUnit, tách sau nếu cần.

## Kiểm chứng trên Java 26
Đã chạy thật: `./mvnw verify` (biên dịch `--release 26`, JUnit, ArchUnit 1.5.1 đọc lớp Java 26, Flyway/PostgreSQL 17, Lettuce) xanh;
`java -jar` khởi động và phục vụ `/health`; thử 10 000 yêu cầu với 1 000 kết nối đồng thời trên `/health`: 0 lỗi, chỉ ~32 luồng nền (luồng ảo hoạt động).
`ScopedValue` (chính thức từ JDK 25) được dùng cho ngữ cảnh yêu cầu nên **tối thiểu Java 25**.
Maven khi chạy trên JDK 26 in cảnh báo `sun.misc.Unsafe` của chính Maven; vô hại.

## Quy ước kỹ thuật đã chốt (đề xuất cho ARCH/BA xem lại)
- **Id:** UUID (v7 sinh ở ứng dụng) cho mọi khóa chính; kiểu cột `uuid`.
- **Thời gian:** `timestamptz` lưu UTC, JSON dạng ISO-8601 `...Z`; máy chủ đặt múi giờ mặc định UTC. Giao diện **luôn hiển thị giờ Việt Nam (Asia/Ho_Chi_Minh)**, không theo múi giờ thiết bị (BR-GEO-233, D-GEO-168; `geo.time.display-zone`). Quyết định giờ mở dùng `now()` của CSDL (ADR-GEO-06).
- **Mã trường:** cột `org_id uuid NOT NULL` trên mọi bảng thuộc trường; chỉ mục bắt đầu bằng `org_id`; lấy từ thẻ, không bao giờ từ client.
- **Phân trang:** mọi danh sách dùng keyset qua `Paging` với một `ListKind`; chỉ mục `(org_id, <khóa…> DESC, id DESC)`; trần chung 100, Kho đề 50; không trả tổng số ([CONFIG.md](CONFIG.md) §4).
- **Lỗi:** `{code, message (tiếng Việt), requestId, details[]}`; `code` là hằng chữ hoa ổn định (`ErrorCode`), gồm cả `CONFLICT` (409), `RATE_LIMITED` (429), `SERVICE_OVERLOADED` (503, kèm `Retry-After`); bảng đầy đủ ở [CONFIG.md](CONFIG.md) §6.
- **Không lưu đệm:** phản hồi nhạy cảm (lời giải, gợi ý, trạng thái khóa) đánh dấu `@NoStore` → `Cache-Control: no-store` (D-GEO-145).
- **Migration:** V1 nền, V2 bảng mẫu (xóa sau); schema chính thức đánh số từ V10. Migration đã áp dụng không sửa, chỉ thêm mới.
- **Phản hồi JSON bỏ trường null** (`nextCursor` vắng mặt ở trang cuối).

## Giả định
- Chưa có khóa AI thật: `aigateway` dùng nhà cung cấp mock (giả định PO câu 3 trong brief).
- Vai (`Role`) lấy theo PO-01: học sinh, phụ huynh, giáo viên, quản trị trường, cơ quan quản lý; tên enum chờ ARCH chốt.
- Tên gói gốc `vn.geo.server`, nhóm Maven `vn.geo` là tên tạm, đổi được bằng một lần đổi tên gói.
- Một module Maven duy nhất; ranh giới module do ArchUnit giữ. Tách nhiều module Maven khi mã lớn.
- `events` được đặt tên theo yêu cầu nhưng phạm vi chưa có trong nguồn; để rỗng. Cổng phát sự kiện miền trong tiến trình nằm ở `common.event`.

## Giới hạn đã biết của khung
- **Hết thời gian chờ:** có ở từng tầng (giá trị ở [CONFIG.md](CONFIG.md)); cổng AI 10 giây là tạm (D-GEO-072). KHÔNG có hạn chót tổng cho cả yêu cầu HTTP.
- **Row-Level Security PostgreSQL** chưa bật; mã trường chỉ bảo vệ ở tầng ứng dụng (TenantJdbc + bộ quét SQL). ARCH nên quyết định có thêm RLS làm lớp phòng thủ thứ hai không.
- **Bộ quét SQL là tĩnh:** chỉ thấy chuỗi SQL viết thẳng trong mã Java (kể cả text block và nối `+`); SQL ghép động bằng biến không thấy được. Quy ước: viết SQL là hằng. LIKE với tham số chứa `%` đầu mẫu không bắt được. Ngoại lệ có chủ đích dùng chú thích `-- sqllint:allow=<luật>` (dễ grep khi rà soát).
- **Bảng thật phải khai báo** trong `server/quality/sql-lint.properties` (bảng lớn, bảng thuộc trường, chủ sở hữu, bảng cấm mã ứng dụng); quên khai báo thì luật tương ứng không áp dụng cho bảng đó. Đã hợp nhất đề xuất của ARCH; khóa lạ làm build đỏ. Chưa hỗ trợ: coi `org_id = :platformOrgId` là bộ lọc trường (ghi ở đầu tệp).
- `/dev/token` chỉ có ở profile `dev`; không bao giờ bật ở môi trường thật.

## Vấn đề còn mở
- Chưa đo thời gian khởi động tối ưu (~11 giây trên máy dev); chưa bật CDS/AOT.
- Chưa có thử tải chính thức: `loadtest/` mới có kịch bản khói Gatling tới `/health` (xem `loadtest/README.md`); số đo trên máy dev chỉ mang tính chỉ báo.
- `e2e/` mới có test khung (`@TC-KHUNG-01…03`); TC của QC do agent từng epic viết. Lần đầu cần `npm run install-browsers`.
