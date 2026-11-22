-- E5 · Kho thử phân trang F-GEO-080 (artifacts/GEO/work/fixture.md, bản 22/11/2026) — chạy sau 100_e1_core.sql.
-- Kho thử = 48 đề HKG-01…HKG-48: sáu đề thật F-GEO-031…036 (bộ 'main' của E1) + 42 đề thử HKG-07…48 nhân từ sáu đề đó
-- ("chỉ để kiểm lọc và phân trang, không phải nội dung kho thật"). HKG-k nhân từ HKG-((k−1) mod 6 + 1) nên mỗi đề gốc có
-- 8 đề: 24 THPT, 24 THCS; Góc 8, Khoảng cách 8, Thể tích 16, Diện tích 8, Mặt cầu 8; chóp, lập phương, lăng trụ, nón,
-- trụ, cầu mỗi loại 8. Nhãn: 44 "Đã kiểm chứng", 4 "Cần xem lại" (HKG-45…48); HKG-48 không có lời giải (US-GEO-503-FC1).
--
-- Vì sao bộ riêng (V50, problem.bank = 'kho_thu'): Kho đề của mọi trường phải giữ đúng sáu đề (TC-GEO-0125 của E1). Chỉ hai
-- tổ chức thử của E5 (kiểu 'group', như "Nhóm thử E3") được cấp thấy bộ 'kho_thu':
--   e50001 "Nhóm thử E5" + Giáo viên mẫu e50003: mọi ca giao diện/API đọc kho thử (không làm đổi dữ liệu).
--   e50002 "Nhóm thử E5 nạp thêm" + Giáo viên mẫu e50004: thấy thêm bộ 'kho_moi' mà ca TC-GEO-0533, 0513 nạp đề mới
--   vào giữa chừng (móc /test-hooks/e5/problems), để số đếm "48 đề" của các ca khác không đổi.
-- UUID: 00000000-0000-7000-8000-0000e5Xkkkkk (X: 1 đề, 2 version, 3 báo cáo, 4 bản dựng; k = số đề) và …000000e5000n.
-- Đề thử thêm hậu tố "(Đề thử HKG-k)" để băm đề khác sáu đề thật (phép khớp tự học theo băm đề, D-GEO-150).

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8000-000000e50001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử E5'),
  ('00000000-0000-7000-8000-000000e50002', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử E5 nạp thêm');
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8000-000000e50003', '00000000-0000-7000-8000-000000e50001', 'teacher', 'sample', 'Giáo viên mẫu'),
  ('00000000-0000-7000-8000-000000e50004', '00000000-0000-7000-8000-000000e50002', 'teacher', 'sample', 'Giáo viên mẫu');
INSERT INTO catalog_bank_access (org_id, bank) VALUES
  ('00000000-0000-7000-8000-000000e50001', 'kho_thu'),
  ('00000000-0000-7000-8000-000000e50002', 'kho_thu'),
  ('00000000-0000-7000-8000-000000e50002', 'kho_moi');

CREATE TEMP TABLE t_e5 ON COMMIT DROP AS
SELECT k, format('HKG-%s', lpad(k::text, 2, '0')) AS code,
       format('HKG-%s', lpad((((k - 1) % 6) + 1)::text, 2, '0')) AS origin,
       format('00000000-0000-7000-8000-0000e51%s', lpad(k::text, 5, '0'))::uuid AS pid,
       format('00000000-0000-7000-8000-0000e52%s', lpad(k::text, 5, '0'))::uuid AS pvid,
       format('00000000-0000-7000-8000-0000e53%s', lpad(k::text, 5, '0'))::uuid AS rid,
       format('00000000-0000-7000-8000-0000e54%s', lpad(k::text, 5, '0'))::uuid AS sid
FROM generate_series(7, 48) AS k;

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, created_at)
SELECT t.pid, p.org_id, p.subject_id, 'catalog', p.author_user_id, p.level, p.solid_kind, p.topic_id, t.code, t.code, 'kho_thu',
       :'t0'::timestamptz - interval '20 days'
FROM t_e5 t JOIN problem p ON p.org_id = platform_org_id() AND p.code = t.origin;

INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by, created_at)
SELECT t.pvid, pv.org_id, t.pid, 1, s.statement, digest(regexp_replace(btrim(s.statement), '\s+', ' ', 'g'), 'sha256'),
       pv.created_by, :'t0'::timestamptz - interval '20 days'
FROM t_e5 t
JOIN problem p ON p.org_id = platform_org_id() AND p.code = t.origin
JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
CROSS JOIN LATERAL (SELECT pv.statement || ' (Đề thử ' || t.code || ')' AS statement) s;

UPDATE problem pr SET current_version_id = t.pvid FROM t_e5 t WHERE pr.org_id = platform_org_id() AND pr.id = t.pid;

INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT h.org_id, t.pvid, h.level, h.body
FROM t_e5 t
JOIN problem p ON p.org_id = platform_org_id() AND p.code = t.origin
JOIN hint h ON h.org_id = p.org_id AND h.problem_version_id = p.current_version_id;

-- Lời giải (đứng tên tác giả nền tảng, cờ AI soạn); HKG-48 cố ý không có lời giải
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
SELECT s.org_id, t.pvid, s.final_answer, s.steps, s.model, s.prompt_version, s.author_user_id, true
FROM t_e5 t
JOIN problem p ON p.org_id = platform_org_id() AND p.code = t.origin
JOIN solution s ON s.org_id = p.org_id AND s.problem_version_id = p.current_version_id
WHERE t.k <> 48;

-- Bản dựng: tham số a = 1 như đề gốc; spec_code = mã đề gốc để engine dựng lại hình theo mô tả dựng của đề gốc
INSERT INTO scene_build (id, org_id, problem_version_id, build_no, coord_source, schema_version, spec, coords, hidden_measures)
SELECT t.sid, b.org_id, t.pvid, 1, 'engine', 1, b.spec || jsonb_build_object('spec_code', t.origin), b.coords, b.hidden_measures
FROM t_e5 t
JOIN problem p ON p.org_id = platform_org_id() AND p.code = t.origin
JOIN scene_build b ON b.org_id = p.org_id AND b.problem_version_id = p.current_version_id AND b.build_no = 1;

-- Nhãn máy như sau một lần chạy engine: 44 "Đã kiểm chứng"; HKG-45…47 có báo cáo "Cần xem lại"; HKG-48 chưa có báo cáo
INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
SELECT t.rid, platform_org_id(), t.pvid, true, t.k < 45, CASE WHEN t.k < 45 THEN 'verified' ELSE 'needs_review' END::verify_status,
       '{"source":"seed E5: kho thử F-GEO-080"}'
FROM t_e5 t WHERE t.k < 48;
UPDATE problem_version pv SET verification_report_id = t.rid,
       verify_status = CASE WHEN t.k < 45 THEN 'verified' ELSE 'needs_review' END::verify_status
FROM t_e5 t WHERE pv.org_id = platform_org_id() AND pv.id = t.pvid AND t.k < 48;
