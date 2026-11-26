# Bảng cấu hình chung (nguồn duy nhất)

Bảng này là nguồn duy nhất cho các con số mà khung mã và ARCH cùng dùng (D-GEO-168; RV-GEO-20, 37).
ARCH, BA và QC dẫn tới đây, không chép số. Đổi một giá trị thì sửa **cùng lúc** tệp này, `application*.yml` và
`ConfigIT` (kiểm thử đọc cấu hình đã nạp và so với bảng này).
Ngưỡng hiệu năng giữ nhãn "đề xuất" như ở `06_nfr.md`.

## 1. CSDL và nhóm kết nối
| Khóa | Giá trị | Ở đâu trong mã | Nguồn |
|---|---|---|---|
| Kích thước nhóm kết nối | **20 mỗi nút** (`GEO_DB_POOL`), nhóm cố định (`minimum-idle` = 20) | `spring.datasource.hikari.maximum-pool-size` | ADR-GEO-04; `05_capacity.md` §4.2; D-GEO-168 |
| Chờ lấy kết nối | **2 giây**, quá hạn trả `503 SERVICE_OVERLOADED` + `Retry-After` | `hikari.connection-timeout: 2000`; `GlobalExceptionHandler` | ADR-GEO-04; `05_capacity.md` §7; NFR-GEO-054, 055 |
| `statement_timeout` đường nóng | **2 giây** | `hikari.connection-init-sql` | ADR-GEO-12; NFR-GEO-054 |
| `idle_in_transaction_session_timeout` | 10 giây | `hikari.connection-init-sql` | ADR-GEO-12; `05_capacity.md` §7 |
| `maxLifetime` kết nối | 25 phút | `hikari.max-lifetime: 1500000` | ADR-GEO-04 |
| Phát hiện rò kết nối | 10 giây, chỉ profile `test` | `application-test.yml` | ADR-GEO-04 |
| JDBC `connectTimeout` / `socketTimeout` | 3 giây / 30 giây (lưới an toàn dưới `statement_timeout`) | URL JDBC | khung mã |

Chưa có trong khung (sẽ thêm khi có tác vụ nền thật): nhóm riêng 5 kết nối cho tác vụ nền và `statement_timeout`
30 giây cho báo cáo nền (ADR-GEO-04, 12).

## 2. Redis
| Khóa | Giá trị | Ở đâu | Nguồn |
|---|---|---|---|
| Thời hạn lệnh | **100 ms**, lỗi thì bỏ qua bộ đệm và đọc CSDL (fail-open; riêng giới hạn tốc độ chuyển sang bộ đếm cục bộ) | `spring.data.redis.timeout: 100ms` | ADR-GEO-05; `05_capacity.md` §7; NFR-GEO-054 |
| Thời hạn kết nối | 100 ms | `spring.data.redis.connect-timeout` | như trên |
| Tiền tố khóa | `geo:` (test: `geotest:`) | `geo.redis.key-prefix` | khung mã |

## 3. HTTP
| Khóa | Giá trị | Ở đâu | Nguồn |
|---|---|---|---|
| Trần thân yêu cầu | **1 MB** (1 048 576 byte), vượt trả `413 PAYLOAD_TOO_LARGE` | `geo.http.max-body-bytes`; `BodySizeLimitFilter` | M-004 (5) |
| `Retry-After` mặc định | 2 giây (giây nguyên, làm tròn lên) cho `429` và `503 SERVICE_OVERLOADED` | `geo.http.retry-after` | `05_capacity.md` §7; NFR-GEO-055 |
| Phản hồi nhạy cảm | `Cache-Control: no-store` (kèm `Pragma: no-cache`) cho lời giải, gợi ý, trạng thái khóa; áp cả phản hồi lỗi của endpoint đó | chú thích `@NoStore` (phương thức hoặc lớp controller) → `NoStoreInterceptor`; ngoài MVC dùng `CacheHeaders.noStore(response)` | D-GEO-145 |
| Cổng AI | 10 giây **TẠM** (dev/test), 90 giây ở prod (AI thật, **TẠM**, chưa đo); trần một lượt kể cả thử lại | `geo.ai.timeout` (`GEO_AI_TIMEOUT`) | D-GEO-072 (owner TechLead); PO-60 |

## 4. Phân trang
| Khóa | Giá trị | Ở đâu | Nguồn |
|---|---|---|---|
| Kích thước mặc định | 20 | `geo.paging.default-limit` | ADR-GEO-12 |
| Trần chung | **100** | `geo.paging.max-limit` | M-004 (2); D-GEO-168 |
| Trần Kho đề | **50** (mặc định 20) | `geo.paging.max-limit-by-list."[content.problem-bank]"` | D-GEO-142 |
| Trần riêng khác | thêm khóa `max-limit-by-list."[<mã danh sách>]"`; chỉ được thấp hơn trần chung | `PagingProperties.maxLimitFor` | ADR-GEO-12, `04_query_catalogue.md` |
| Tổng số bản ghi | không trả | — | M-004 (2) |

**Con trỏ** (D-GEO-124, D-GEO-168; RV-GEO-37):
- Mờ, mã hóa **AES-256-GCM** (`geo.paging.cursor-key`, base64 của 32 byte; bắt buộc đặt `GEO_CURSOR_KEY` ở môi trường thật).
- Bên trong là **khóa sắp xếp của dòng cuối, kết thúc bằng `id` UUIDv7** (ví dụ chỉ `id`; `(occurred_at, id)`; bộ nhiều cột của Kho đề).
  Truy vấn: `ORDER BY <khóa…> DESC, id DESC`, trang sau `(<khóa…>, id) < (:k…, :afterId)`, chỉ mục `(org_id, <khóa…> DESC, id DESC)`.
- Gắn **trường** (`org_id` từ thẻ) + **loại danh sách** (`ListKind.code`, ví dụ `content.problem-bank`) + **phiên bản** định dạng (hiện là 2).
  Con trỏ dùng sang trường khác, danh sách khác, bị sửa, sai phiên bản hoặc sai kiểu khóa → `400 INVALID_CURSOR`
  (US-GEO-104-FC8, US-GEO-606-FC4). Không phân biệt lý do trong phản hồi.
- Mã danh sách đặt dạng `<module>.<danh-sách>`, chữ thường, ổn định; đổi mã làm mọi con trỏ cũ của danh sách đó hết hiệu lực.

## 5. Thời gian
| Khóa | Giá trị | Ở đâu | Nguồn |
|---|---|---|---|
| Lưu trữ | `timestamptz`, UTC | schema | ADR-GEO-06 |
| JSON máy chủ trả | **ISO-8601 UTC** dạng `2026-01-02T03:04:05.123456Z` | `spring.jackson.time-zone: UTC`; JVM `TimeZone.setDefault(UTC)` | M-004 (3); D-GEO-168 |
| Đồng hồ quyết định giờ mở lời giải | `now()` của CSDL, không dùng giờ máy chủ ứng dụng hay thiết bị | SQL | ADR-GEO-06 |
| Múi giờ hiển thị | **luôn `Asia/Ho_Chi_Minh` (giờ Việt Nam)**, không theo múi giờ thiết bị; máy chủ dùng khi phải dựng chữ có giờ | `geo.time.display-zone`; Playwright `timezoneId` | BR-GEO-233; US-GEO-604-SC6; D-GEO-168 |

## 6. Mã lỗi
Thân lỗi: `{code, message (tiếng Việt), requestId, details[]}`; `code` là hằng ổn định (`ErrorCode`), không đổi tên.

| `code` | HTTP | Khi nào | `Retry-After` |
|---|---|---|---|
| `VALIDATION_FAILED` | 400 | dữ liệu gửi lên sai, `limit` < 1 | |
| `INVALID_CURSOR` | 400 | con trỏ hỏng / khác trường / khác danh sách / khác phiên bản | |
| `UNAUTHENTICATED` | 401 | thiếu hoặc sai thẻ | |
| `FORBIDDEN` | 403 | không đủ quyền | |
| `NOT_FOUND` | 404 | không có hoặc không được thấy | |
| `METHOD_NOT_ALLOWED` | 405 | | |
| `CONFLICT` | **409** | xung đột trạng thái: đổi giờ khi đã mở, giao lại cùng đề (D-GEO-143, 161) | |
| `PAYLOAD_TOO_LARGE` | 413 | thân > 1 MB | |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | | |
| `RATE_LIMITED` | **429** | vượt giới hạn tốc độ (`05_capacity.md` §7; NFR-GEO-056) | có |
| `INTERNAL` | 500 | lỗi không lường trước (không lộ chi tiết) | |
| `DEPENDENCY_UNAVAILABLE` | 503 | CSDL hoặc dịch vụ phụ trợ hỏng | |
| `SERVICE_OVERLOADED` | **503** | quá tải: hết chờ nhóm kết nối 2 giây, rào cách ly đầy (D-GEO-143; NFR-GEO-055) | có |
| `TIMEOUT` | 504 | truy vấn vượt `statement_timeout` | |

Tạo lỗi có `Retry-After` trong mã: `AppException.rateLimited(Duration)`, `AppException.overloaded(Duration)`.

## 7. Kiểm thử và công cụ
| Hạng mục | Giá trị | Nguồn |
|---|---|---|
| Trần câu lệnh SQL mỗi yêu cầu | đọc ≤ 4, ghi ≤ 6, không phụ thuộc số dòng; đo bằng `SqlStatementCounter` (datasource-proxy) | NFR-GEO-009; D-GEO-170 |
| Móc kiểm thử SQL | chỉ profile `test`: `spring.flyway.locations` thêm `classpath:db/testhooks` (`src/test/resources`) | D-GEO-169 |
| Kiểm ranh giới đúng R qua API | R = giờ máy chủ + 2 giây rồi chờ | D-GEO-169 |
| E2E | Playwright (`app/e2e`), múi giờ `Asia/Ho_Chi_Minh` | D-GEO-170 |
| Thử tải | Gatling Java DSL (`app/loadtest`) | D-GEO-170 |
| Bộ đo MVP | `make bench-mvp` (`scripts/bench-mvp.sh`, lớp `it/mvp/MvpBench`, ngoài `make verify`): mọi đề của bộ đề chuẩn (hợp mã `geometry/problem-specs.json`, `content/scene-specs.json`, `mock-ai/benchmark.json`) → `artifacts/GEO/evidence/MVP-METRICS-<yyyymmdd-HHmm>/report.md + report.json`; mã thoát ≠ 0 khi chỉ số bắt buộc trượt. Mock mặc định; AI thật: `GEO_AI_PROVIDER=anthropic GEO_AI_API_KEY=…` | MVP.md §9, PO-60 |

## 8. Profile và khóa bí mật (chốt khởi động `ProfileGuard`)
| Hạng mục | Giá trị | Nguồn |
|---|---|---|
| Profile mặc định | **không có**; phải bật tường minh `dev`, `test` hoặc `prod`. Không bật thì máy chủ từ chối khởi động | soát Đợt 0 (22/11/2026) |
| Cách bật | `make run`: `-Dspring-boot.run.profiles=dev`; `scripts/e2e.sh`: `--spring.profiles.active=dev`; IT: `@ActiveProfiles("test")` | — |
| `prod` cùng `dev`/`test` | từ chối khởi động | — |
| Khóa bắt buộc ngoài dev/test | `GEO_AUTH_SECRET` (`geo.auth.secret`), `GEO_AUTH_CODE_PEPPER` (`geo.auth.code-pepper`), `GEO_CURSOR_KEY` (`geo.paging.cursor-key`): không có mặc định trong `application-prod.yml`; rỗng hoặc còn giá trị dev thì từ chối khởi động | D-GEO-152 |
| `geo.http.trust-forwarded-for` | `false` (mặc định và prod; prod chỉ bật bằng `GEO_TRUST_FORWARDED_FOR=true` khi có proxy tin cậy); `true` ở dev/test để E2E mô phỏng nhiều IP | TC-GEO-0179 |
| `/dev/token`, `/test-hooks/*` | chỉ có ở dev/test (`@Profile`); ngoài dev/test không công khai trong `AuthFilter` (không thẻ => 401, có thẻ => 404) | PO-51 |
| `geo.ai.provider` | `mock` hoặc `anthropic`; giá trị khác từ chối khởi động. Ngoài dev/test (prod) **cấm `mock`**; `application-prod.yml` mặc định `anthropic` | PO-60 (MVP.md §8 M0) |
| `GEO_AI_API_KEY` (`geo.ai.api-key`) | bắt buộc khi `geo.ai.provider=anthropic` (mọi profile); không in ra nhật ký, thông báo lỗi, ai_call_log | BR-GEO-106, PO-60 |

## 8b. Cổng AI (`geo.ai`, `aigateway`)
| Khóa (biến môi trường) | Mặc định | Ghi chú |
|---|---|---|
| `geo.ai.provider` (`GEO_AI_PROVIDER`) | `mock` (dev/test/CI), `anthropic` (prod) | `MockAiProvider` (D-GEO-047) / `AnthropicAiProvider` (Claude Messages API qua HttpClient của JDK) |
| `geo.ai.model` (`GEO_AI_MODEL`) | `gia-lap-1` (mock); provider `anthropic` thì giá trị giả lập này thành `claude-sonnet-5-5` (prod yml đặt sẵn) | đổi model bằng cấu hình (US-GEO-103-SC2) |
| `geo.ai.prompt-version` (`GEO_AI_PROMPT_VERSION`) | `p1` (mock); `anthropic` + `p1` thành `a1` (`AnthropicPrompts.VERSION`) | ghi vào ai_call_log và báo cáo bộ đề chuẩn |
| `geo.ai.anthropic.base-url` (`GEO_AI_BASE_URL`) | `https://api.anthropic.com` | test trỏ vào máy chủ HTTP giả |
| `geo.ai.anthropic.api-version` | `2023-06-01` | tiêu đề `anthropic-version` |
| `geo.ai.anthropic.max-tokens` | 16000 | trần token ra mỗi lượt; bị cắt (`max_tokens`) = lỗi |
| `geo.ai.anthropic.attempt-timeout` (`GEO_AI_ATTEMPT_TIMEOUT`) | 45 s | một lần gọi HTTP; hết giờ = lỗi quá thời gian (`TIMEOUT`, không thử lại) |
| `geo.ai.anthropic.max-retries` / `retry-backoff` | 2 / 1 s (nhân đôi; theo `retry-after` nếu có, tối đa 30 s) | chỉ 429, 5xx, lỗi mạng; 4xx khác, JSON hỏng, `refusal` trả ngay `DEPENDENCY_UNAVAILABLE` |
| `geo.ai.anthropic.fallbacks` | `default` (+ beta `server-side-fallback-2026-07-01`) | chuyển model khi bị từ chối an toàn; rỗng = tắt |
| `geo.ai.anthropic.effort` (`GEO_AI_EFFORT`) | rỗng = mặc định của model | `output_config.effort` |
| `geo.ai.pricing.<model>` | Sonnet 5.5: 2 / 10 USD (cache đọc 0,10); Opus 5.5: 4 / 20 (0,20); Haiku 5.5: 0,10 / 0,50 — USD / 1 triệu token | nguồn: bảng giá Anthropic theo skill claude-api (cached 2026-11-03), chép 2026-11-26; chi phí = usage thật × giá → `ai_call_log.cost_micro_usd` |
| `geo.ai.cost-per-call` | 0,12 | chi phí mô phỏng của mock (KG-07) |

Đầu ra AI thật được đưa về đúng định dạng của mock (JSON theo task: scene, solution, hint, ocr, qa) rồi đi qua đường kiểm sẵn có (engine.check, AnswerExpr, Kiểm soát đáp án) — không nới kiểm. Bộ đo: `make bench-mvp` (mục 7).

## 9. Quyền bộ đề (bank) của Kho đề — guard bắt buộc (FIX3)
- Bộ `main` mọi trường thấy; bộ khác chỉ tổ chức có hàng `catalog_bank_access` thấy (E5, V50). Không thấy = `NOT_FOUND` (BR-GEO-112), không lộ là đề có tồn tại.
- **Mọi đường đọc hoặc giao một version đề kho theo id PHẢI gọi `vn.geo.server.content.api.BankAccess.requireVisible(versionId)` ở tầng dịch vụ trước khi đọc/ghi**: chi tiết đề (`CatalogService.detail`), lời giải giáo viên (`CatalogService.teacherSolution`), trang chi tiết có cảnh (`SceneService.teacherProblem`), tạo bài giao (`AssignmentService.create`), và mọi đường thêm sau này.
- Lớp SQL cũng kiểm (V59: `fn_catalog_bank_visible` trong `fn_create_assignment`, `fn_get_solution_teacher`; điều kiện bộ trong `CatalogRepository.detail`, `SceneRepository.catalogVersion`). Migration nào định nghĩa lại các hàm này PHẢI giữ điều kiện `fn_catalog_bank_visible`; guard Java vẫn chặn nếu quên.
- Kiểm thử: `it/e5/BankAccessE5IT` (Dev-E5-01…04: giáo viên ngoài bộ → 404 cho chi tiết, lời giải, cảnh, giao bài).
