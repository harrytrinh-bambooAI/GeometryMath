-- E2 · DỮ LIỆU KIỂM của "Hình đúng" (artifacts/GEO/work/epics/E2, bản 22/11/2026).
-- Danh mục hình mẫu nằm ở migration V20 (bảng solid_template giữ khi reset, keep-tables.txt).
--
-- TC-GEO-0222 (US-GEO-203-FC4) cần "một đề mang Cần xem lại" ở trang chi tiết đề của giáo viên, mà Kho đề phải giữ đúng
-- sáu đề HKG-01…06 (TC-GEO-0125). Nên thêm một VERSION KHÔNG HIỆN HÀNH của HKG-06 (version 99, cùng đề), chưa có báo cáo
-- kiểm chứng => nhãn máy mặc định "Cần xem lại" (tg_pv_verify_guard). Không đổi current_version_id nên Kho đề không đổi;
-- version 99 để không đụng version 2 mà bộ nạp kho đề (E1) tạo khi đề đổi chữ.
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by, created_at)
SELECT '00000000-0000-7000-8000-000000299036'::uuid, pv.org_id, pv.problem_id, 99, pv.statement, pv.statement_hash,
       pv.created_by, :'t0'::timestamptz - interval '1 day'
FROM problem_version pv
WHERE pv.org_id = platform_org_id() AND pv.id = '00000000-0000-7000-8000-000000100036'::uuid;
