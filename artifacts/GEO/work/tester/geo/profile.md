# Domain profile — GEO (Hình Học Không Gian Cùng AI) · kiểm thử dev local

Phiên bản: v0.01 · 2026-11-26 · Status: **DRAFT — scope UNCONFIRMED, người dùng xác nhận trước lần chạy đầu**
Sources (mới nhất trước): `app/README.md` + `app/Makefile` (2026-11-26) · `artifacts/GEO/work/epics/S2-E*`, `S3-E*` (v0.2, 2026-11-26) · `artifacts/GEO/work/epics/E1…E7` (MVP) · `artifacts/GEO/work/fixture.md` (2026-11-22) · `app/db/seed/README.md`.
Mọi đường dẫn tính từ gốc repo `GeometryMathSupport`. Mục chưa biết: `OPEN`.

## 1. Hệ thống và môi trường
| Env | Địa chỉ | Dùng |
|---|---|---|
| Dev local — web (Vite) | `http://localhost:5173` (worktree: `GEO_WEB_PORT`, ví dụ 5174) | cho phép |
| Dev local — máy chủ | `http://localhost:8080` (`GEO_PORT`, worktree ví dụ 8090) | cho phép |
| PostgreSQL / Redis riêng dự án | `127.0.0.1:54329` / `127.0.0.1:6389` | chỉ qua script dự án; KHÔNG dùng 5432/6379 |
| Staging / prod | OPEN (chưa có) | **CẤM** |
Đọc cổng thật bằng `make ports` (trong `app/`) hoặc `app/.env.ports`.

## 2. Khởi chạy app (trong `app/`)
```
make up                 # PG + Redis riêng (scripts/dev-up.sh), rồi scripts/db-init.sh
make run                # máy chủ, profile dev, cổng GEO_PORT (hoặc: cd server && ./mvnw -DskipTests package && java -jar target/geo-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev)
make web-install && make web-dev     # Vite ở GEO_WEB_PORT, proxy sang máy chủ
make e2e-reset DB=geo_dev            # đưa dữ liệu dev về trạng thái vàng
make e2e                # lượt Playwright tự đủ (DB geo_e2e, cổng riêng) -> e2e/evidence/<run>/
curl localhost:8080/health
```
Không bật profile thì máy chủ từ chối khởi động. Tắt: `make down`.

## 3. Vai và đăng nhập
| Vai | Ghi chú |
|---|---|
| Giáo viên | MVP: mã riêng 12 ký tự base32. Chặng 2: đăng nhập `GV-xxxx-xxxx` + mật khẩu, mã khôi phục `RC-xxxx-xxxx-xxxx` (S2-E1) |
| Học sinh | MVP: vào lớp bằng mã lớp. Chặng 2: `HS-xxxx-xxxx`; chặng 3: mã lớp `HK-xxxx`, trạng thái chờ duyệt (S3-E1) |
| Người soát nội dung | duyệt đề vào Kho chung (S2-E6) |
| Admin | KHÔNG có vai admin: đăng ký với vai "admin" bị từ chối (S2-E1) |
Token dev (chỉ profile dev): `POST /dev/token` với `{"orgId":"<uuid>","userId":"<uuid>","role":"TEACHER"}`; nhận `token` dùng làm `Authorization: Bearer`.

## 4. Tài khoản seed dev (`artifacts/GEO/work/fixture.md`, UUID cố định ở `app/db/seed/fixture-ids.json`)
| F id | Thực thể | Giá trị |
|---|---|---|
| F-GEO-001 / 002 | Trường | "THPT Mẫu A" / "THCS Mẫu C" (cách ly) |
| F-GEO-003 | Giáo viên mẫu (trường A, dạy 11A1 và 9B) | mã `GQ7KX2MTB4HN` |
| F-GEO-005 | Giáo viên C (trường C, dạy 9C) | mã `RW3NJ5PZC6TD` |
| F-GEO-004/006/007 | Mã sai / thu hồi / quá hạn | `AAAAAAAAAAAA` / `HX4QW7PLZ2KM` / `T5BN3JQ6WRYE` |
| F-GEO-010 / 011 / 012 | Lớp 11A1 / 9B / 9C | `HK7M2A` / `HK9B4T` / `MC9C6R` |
| F-GEO-020…024 | Học sinh 11A1 | An, Bình, Chi, Dũng, Hà |
| F-GEO-031…036 | Sáu bài mẫu kho đề | HKG-01…06 |
Lưu ý: seed hiện chỉ nạp dữ liệu khung (`sample_item`); phần fixture còn lại nạp theo epic (`app/db/seed/README.md`). Định dạng mã chặng 2–3 (`GV-…`, `HK-…`) đổi so với mã MVP ở trên: OPEN cho tới khi S2-E1 US-S2E1-04 (chuyển tài khoản mẫu) có seed.

## 5. Luồng chính
**Chặng MVP (E1–E7):** nền tảng + bộ đề chuẩn (E1) · hình đúng, nhãn máy (E2) · thao tác 3D cảm ứng: xoay, chọn, vẽ, đo (E3) · gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học (E4) · kho đề Giáo viên mẫu (E5) · giao bài và khóa lời giải (E6) · theo dõi lớp (E7).
**Chặng 2 (S2-E1…E9):** đăng ký / đăng nhập bằng mã, khôi phục (E1) · hình phẳng và bench (E2) · thêm đề bằng chữ/ảnh, 4 bước đọc–dựng–giải–kiểm (E3) · chat chỉnh nháp, import docx/pdf, đối chiếu trùng (E4) · soát và duyệt lời giải, StepCheck, nhãn hai trục (E5) · kho Của tôi / Kho chung (E6) · trình chiếu trên lớp, mất mạng (E7) · bài luyện điền đáp số, bàn phím toán, chấm tương đương (E8) · biến thể đề, xuất ảnh/PDF, trắc nghiệm (E9).
**Chặng 3 (S3-E1…E6):** lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu, quyền theo quan hệ (E1) · bộ bài, lịch mở lời giải (E2) · kết quả lớp, "Cả lớp vướng ở đâu" (E3) · tổng quan giáo viên, theo dõi trên điện thoại, thông báo (E4) · AuditEvent, thông báo riêng tư, công tắc AI theo lớp (E5) · chữa bài sang Trình chiếu, xuất kết quả, lưu trữ lớp (E6).
Thiết bị: iPad ngang (`ui-webkit`, F-GEO-100) và Chromium (F-GEO-102); iPhone là cấu hình riêng.

## 6. Test case và công cụ
Test case ở `artifacts/GEO/work/epics/<epic>/test_cases.md`; `python3 artifacts/GEO/work/tools/check_ac_tc.py [epic…]` kiểm AC đều có TC. Truy vết TC → test: `make traceability ARGS="--all"` trong `app/`. Bằng chứng E2E: `make e2e-report`.

## 7. Scope cho phép (`scope.json`, UNCONFIRMED)
Chỉ `localhost` / `127.0.0.1` cổng 5173, 5174, 8080, 8090. Không staging/prod. GET tự do; POST/PUT/PATCH/DELETE cần xác nhận. Reset DB chỉ geo_dev / geo_e2e.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.01 | 2026-11-26 | Tạo mới từ app/README, Makefile, fixture, epic S2/S3 | runner-T-261126-15 |
