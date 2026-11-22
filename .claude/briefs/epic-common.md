# Brief chung cho agent epic (Đợt 1+) · v1.1 · 22/11/2026

> **Tránh bị cắt phản hồi (bài học E3, v1.1):** mỗi lần Write/Edit không quá ~250 dòng; tệp lớn thì tạo khung rồi Edit thêm từng phần; tách component/spec thành nhiều tệp; không in lại nội dung tệp dài vào phản hồi; đọc tệp có mục tiêu (grep, offset/limit).

Áp cho mọi agent code epic E2…E7 của GEO. Phần riêng (epic, cổng, phụ thuộc, ngân sách) nằm trong brief của từng agent.

## Nguồn sự thật (22/11/2026)
- `artifacts/GEO/work/epics/<E>/stories_ac.md` (US, BR, AC — nguồn chính), `test_cases.md` (cột "Tự động hóa"), `test_design.md`, `qc_findings.md`, `questions.md`.
- `artifacts/GEO/work/fixture.md`, `review/QC_test_design.md` (dữ liệu F-GEO-…), `03_decisions.md`, `backlog_cases.md` (ca để sau — không làm).
- Kiến trúc `artifacts/GEO/work/architecture/` (01_adr, 02_multi_tenant, 03_schema.sql, 04_query_catalogue, 06_nfr). Quy ước mã `app/README.md`, `app/docs/STACK.md`, `app/docs/CONFIG.md` (§8 profile: dev/e2e phải bật profile `dev` tường minh).
- `PO-DECISIONS.md` (PO-50…55). AC PROVISIONAL: làm theo giả định mặc định ghi cạnh nó.
- Mẫu đã có để noi theo: code E1 (`app/server/.../iam`, `tenancy`, `learning`…, `app/web/src/features/e1/`, `app/e2e/tests/e1/`, IT ở `app/server/src/test/java/vn/geo/server/it/e1/`).
- Giao diện: bố cục/token từ `Design/prototype/index.html` (và `fig3d.js` cho hình 3D), ảnh trong `input/`; không chép máy móc.

## Cách làm
1. Tạo `app/.env.ports` với cổng riêng được giao; `cd app && make up`.
2. Thứ tự: migration (V mới, số tiếp theo; dùng khoảng số riêng được giao để không trùng epic song song) + domain + API + IT → giao diện `app/web/src/features/<e>/` → E2E `app/e2e/tests/<e>/` theo TC.
3. Mỗi test gắn mã TC: Playwright `@TC-GEO-xxxx` trong tiêu đề; JUnit mã TC trong @DisplayName hoặc tên `tcNNNN`.
4. Seed: thêm tệp riêng `app/db/seed/e2e/<số>_<e>_*.sql`; không sửa seed của epic khác (cần thêm dữ liệu chung → tệp mới của mình).
5. Phần nhỏ của epic khác cần để AC chạy được: làm tối thiểu, ghi rõ trong báo cáo.

## Xong khi (lệnh kiểm được bằng máy)
- `cd app && make verify` xanh (đơn vị + IT + ArchUnit + quét SQL).
- `cd app/web && npm run typecheck && npm run lint && npm test && npm run build` xanh.
- `cd app && make e2e` đạt toàn bộ (cả test cũ của E1) hai lượt liên tiếp; truy vết epic của bạn: 0 TC tự động hóa thiếu test, 0 trượt.
- Ghi dòng "Dev/Test" (số đạt/tổng) vào `artifacts/GEO/work/epics/<E>/status.md`.

## Ngoài phạm vi
Không sửa `stories_ac.md`, `test_cases.md` (AC/TC sai → ghi mục "Dev" trong `qc_findings.md` của epic). Không sửa tài liệu sản phẩm, `Design/`. Không nối AI thật (giả lập D-GEO-047). Không giảm mức kiểm của test sẵn có. Không dùng/dừng cổng của checkout hay agent khác. Không push, không merge (trừ merge nhánh được chỉ định vào nhánh của bạn).

## Commit và tiến độ
- Commit sau mỗi bước qua lệnh kiểm, message nêu bước + mã US; cuối message: `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Không commit node_modules, dist, .data, evidence, .env.ports.
- Sau mỗi bước ghi 1 dòng `<giờ> · <bước> · <trạng thái> · <commit>` vào `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/progress/<tên-agent>.md` (đường dẫn tuyệt đối, `echo ... >>`). Không dùng chữ "xong"/"DONE" trừ dòng cuối `<E> · XONG · <commit>`.
- Mỗi lỗi lặp tối đa 3 lần sửa; quá thì ghi lại và chuyển việc.

## Báo cáo cuối (không quá 10 dòng)
AC đạt/tổng; TC tự động đạt/tổng theo loại; kết quả make verify, web, e2e (2 lượt); đường dẫn index bằng chứng; phát hiện Dev; phần làm hộ epic khác; danh sách commit; token ước tính.
