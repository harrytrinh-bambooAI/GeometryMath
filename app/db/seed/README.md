# Seed dữ liệu vàng (fixture) cho dev / E2E

Nguồn: `artifacts/GEO/work/fixture.md` (bản cập nhật 22/11/2026). Seed **không** nằm trong migration Flyway
(`server/src/main/resources/db/migration`), nên không bao giờ chạy ở môi trường thật.

## Cơ chế
- `fixture-ids.json`: UUID cố định cho từng hàng fixture, quy tắc `00000000-0000-7000-8000-<số F, 12 chữ số>`.
  SQL seed và test E2E (`e2e/support/fixtures.ts`: `fixture`, `tokenFor('F-GEO-003')`) dùng chung.
- `e2e/NNN_<tên>.sql`: chạy theo thứ tự tên. Mốc giờ viết tương đối T0 bằng biến psql `:'t0'`
  (ví dụ `:'t0'::timestamptz + interval '60 minutes'` cho BG-1). Đề nghị đánh số theo epic: `1xx` E1, `2xx` E2, … `7xx` E7,
  `9xx` dữ liệu khung.
- `keep-tables.txt`: bảng không bị xóa khi reset (lịch sử Flyway, danh mục do migration nạp).
- `scripts/seed-reset.sh [geo_e2e|geo_dev]` (hoặc `make e2e-reset`, `make e2e-reset DB=geo_dev`): trong một giao dịch,
  TRUNCATE mọi bảng của schema public trừ `keep-tables.txt`, rồi chạy `e2e/*.sql`; xóa khóa Redis `geoe2e:*`; in `T0=...`.
  Chỉ cho phép geo_e2e / geo_dev. Schema phải có sẵn (máy chủ đã chạy Flyway trên DB đó).
- E2E: `e2e/support/global-setup.ts` gọi reset trước mỗi lượt Playwright và đưa T0 vào `GEO_E2E_T0`.
  Test làm bẩn dữ liệu (ví dụ TC-GEO-0101 thêm học sinh "Yến") thì hoặc tự dọn, hoặc gọi lại `seed-reset.sh`.

## Hiện đã nạp
Schema hiện chỉ có `health_probe` (V1) và bảng mẫu `sample_item` (V2). Không bảng nào của fixture tồn tại, nên chỉ có
`e2e/900_sample_scaffold.sql` (dữ liệu khung trên `sample_item` cho hai trường F-GEO-001/002, xóa cùng gói sample).

## Phần fixture CHƯA nạp (chưa có bảng) — epic thêm migration rồi thêm tệp seed
| Mục fixture | Hàng F | Bảng đích (theo `architecture/03_schema.sql`) | Epic gợi ý |
|---|---|---|---|
| §1 Trường, người dùng, mã giáo viên, lớp, học sinh | F-GEO-001…028 | `org`, `app_user`, `teacher_access_code`, `auth_failure`, `class`, `class_member` | E1 |
| §2 Sáu bài mẫu kho đề (đề, gợi ý, đáp số, nhãn) | F-GEO-031…039 | `subject`, `problem_topic`, `solid_template`, `problem`, `problem_version`, `problem_signature`, `hint`, `solution`, `verification_report`, `scene_build` | E2, E5 |
| §3 Bài giao, giờ mở theo T0 | F-GEO-040…049 | `assignment` | E6 |
| §4 Tiến độ, sự kiện lớp 11A1 trên BG-1 | F-GEO-050…059 | `assignment_progress`, `event_log`, `qa_turn` | E7, E4 |
| §5 Tự học và kiểm chứng (đề lạ, ảnh, đáp số AI) | F-GEO-060…079, 110…116 | `problem`, `verification_report`, `answer_guard_log`, `ai_call_log` (phần lớn là đầu vào của ca kiểm, không cần nạp) | E2, E4 |
| §6 Kho thử 48 đề, bài giảng, từ khóa, bộ câu thử, lượt gọi AI | F-GEO-080…097 | `problem*` (kho thử), `lecture`, `keyword`, `probe_prompt`, `probe_run`, `ai_call_log` | E5, E4, E1 |
| §7 Thiết bị | F-GEO-100…103 | không có bảng: là cấu hình dự án Playwright (`ui-chromium` = F-GEO-102, `ui-webkit` iPad ngang = F-GEO-100) | — |
| QC F-GEO-200…299, ARCH F-GEO-300…399 (thử tải) | | ngoài `fixture.md`; thử tải dùng `loadtest/` | — |
