-- S3-E2 (T-261126-29) · dữ liệu riêng cho bộ bài chặng 3 (US-S3E2-01…04, C3-03, C3-08). Chạy sau 910_s3_core.sql;
-- không sửa dữ liệu của task khác. Mã cố định: app/db/seed/fixture-ids.d/s3e2.json (F-S3E2-n → 00000000-0000-7000-8003-000000920nnn…).
-- 1) Tổ chức riêng kiểu 'group' "Nhóm thử S3-E2": mỗi ca kiểm dựng Cô Lan / Thầy Khôi / lớp / học sinh mới qua
--    /test-hooks/s3e2/sandbox rồi soạn, giao, đóng bộ bằng API thật (không làm bẩn bộ F-S3-050/052 dùng chung).
-- 2) Bộ đề 's3e2_kho' (22 đề phẳng BOB-01…22, đều có đáp số máy chấm được): Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng,
--    Đề 4 thể tích như F-S3-050 + 18 đề thêm (đủ ca 20 và 21 đề). Nhãn máy: mọi đề "Đã kiểm chứng" TRỪ Đề 3
--    "Cần xem lại" (US-S3E2-03-SC4). Loại 'plane' nên bộ kiểm chứng cả kho E5 không xét.
-- 3) (TẠM) Lớp 10B của Thầy Khôi (F-S3-012) có một bộ Đã giao "Bộ bài tuần 15 lớp 10B" (F-S3E2-050) để Chi Mai
--    (Đang học cả 11A1 và 10B) thấy hai nhóm ở Bài của em (US-S3E2-03-SC2; F-S3-040 ghi "mỗi lớp có một bộ Đã giao"
--    nhưng 910 chưa có bộ của 10B).

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8003-000000920001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S3-E2');
INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8003-000000920001', 's3e2_kho');

CREATE TEMP TABLE t_s3e2 (n int, title text, statement text, answer text, hint1 text) ON COMMIT DROP;
INSERT INTO t_s3e2 VALUES
  (1, 'Đề 1 SC-đáy', 'Cho hình chóp S.ABCD có đáy là hình vuông cạnh 2, SA ⊥ (ABCD), SA = 2√2. Tính góc giữa SC và mặt đáy (độ).', '45', 'Hình chiếu của SC lên mặt đáy là AC.'),
  (2, 'Đề 2 d(A,SBC)', 'Cho hình chóp S.ABC có SA ⊥ (ABC), tam giác ABC vuông tại B, SA = 3, AB = 4. Tính khoảng cách từ A đến (SBC).', '12/5', 'Kẻ AH ⊥ SB; chứng minh AH ⊥ (SBC).'),
  (3, 'Đề 3 hai mặt phẳng', 'Cho hình chóp S.ABC có SA ⊥ (ABC), tam giác ABC vuông tại B, AB = 1, SA = √3. Tính góc giữa (SBC) và (ABC) (độ).', '60', 'Giao tuyến là BC; tìm hai đường cùng vuông góc với BC.'),
  (4, 'Đề 4 thể tích', 'Cho hình chóp S.ABCD có đáy là hình vuông cạnh 3, SA ⊥ (ABCD), SA = 4. Tính thể tích khối chóp.', '12', 'V = ⅓ · diện tích đáy · chiều cao.');
INSERT INTO t_s3e2
SELECT k, format('Đề %s hình chữ nhật', k), format('Hình chữ nhật ABCD có AB = %s, AD = %s. Tính diện tích hình chữ nhật.', k, k + 1),
       (k * (k + 1))::text, 'Diện tích hình chữ nhật bằng tích hai cạnh kề.'
FROM generate_series(5, 22) k;

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, kind, created_at)
SELECT format('00000000-0000-7000-8003-%s', lpad((921000 + t.n)::text, 12, '0'))::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', 'thpt', NULL, 1, t.title, format('BOB-%s', lpad(t.n::text, 2, '0')), 's3e2_kho',
       'plane', :'t0'::timestamptz - interval '20 days'
FROM t_s3e2 t;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8003-%s', lpad((921000 + t.n)::text, 12, '0'))::uuid, 1, t.statement,
       digest(regexp_replace(btrim(t.statement), '\s+', ' ', 'g'), 'sha256'), 'teacher_approved', false,
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '20 days'
FROM t_s3e2 t;
UPDATE problem pr SET current_version_id = format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid
FROM t_s3e2 t WHERE pr.org_id = platform_org_id() AND pr.id = format('00000000-0000-7000-8003-%s', lpad((921000 + t.n)::text, 12, '0'))::uuid;
INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT platform_org_id(), format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid, 1, t.hint1 FROM t_s3e2 t;
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
SELECT platform_org_id(), format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid, t.answer,
       jsonb_build_array(jsonb_build_object('title', 'Hướng làm', 'body', 'Lập luận đầy đủ: ' || t.statement, 'why', 'Đưa về đại lượng tính được.'),
                         jsonb_build_object('title', 'Kết quả', 'body', 'Đáp số: ' || t.answer || '.', 'why', 'Thay số.')),
       'gia-lap-1', 'p1', '00000000-0000-7000-8000-0000000000aa', false
FROM t_s3e2 t;
-- Nhãn máy "Đã kiểm chứng" cần báo cáo verified của chính phiên bản (D-GEO-158); Đề 3 giữ "Cần xem lại".
INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
SELECT format('00000000-0000-7000-8003-%s', lpad((923000 + t.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid, true, true, 'verified',
       '{"source":"seed S3-E2"}'
FROM t_s3e2 t WHERE t.n <> 3;
UPDATE problem_version pv SET verification_report_id = format('00000000-0000-7000-8003-%s', lpad((923000 + t.n)::text, 12, '0'))::uuid,
       verify_status = 'verified'
FROM t_s3e2 t WHERE pv.org_id = platform_org_id() AND t.n <> 3
  AND pv.id = format('00000000-0000-7000-8003-%s', lpad((922000 + t.n)::text, 12, '0'))::uuid;

-- (TẠM) F-S3E2-050: bộ Đã giao của 10B (Thầy Khôi F-S3-002) — Đề 5 lăng trụ, Đề 6 khoảng cách của bộ 's3_tuan' (910).
INSERT INTO assignment_set (id, org_id, owner_user_id, class_id, title, status, due_at, release_at, assigned_at, created_at, updated_at) VALUES
  ('00000000-0000-7000-8003-000000920050', '00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8003-000000000002',
   '00000000-0000-7000-8003-000000000012', 'Bộ bài tuần 15 lớp 10B', 'assigned', '2026-12-04 20:00:00+07'::timestamptz,
   '2026-12-06 08:00:00+07'::timestamptz, :'t0'::timestamptz - interval '3 days', :'t0'::timestamptz - interval '4 days',
   :'t0'::timestamptz - interval '3 days');
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        release_rev, created_at) VALUES
  ('00000000-0000-7000-8003-000000920151', '00000000-0000-7000-8002-000000000100', 'class', '00000000-0000-7000-8003-000000000012',
   platform_org_id(), '00000000-0000-7000-8003-000000910005', '00000000-0000-7000-8003-000000910105',
   '00000000-0000-7000-8003-000000000002', '2026-12-06 08:00:00+07'::timestamptz, 1, :'t0'::timestamptz - interval '3 days'),
  ('00000000-0000-7000-8003-000000920152', '00000000-0000-7000-8002-000000000100', 'class', '00000000-0000-7000-8003-000000000012',
   platform_org_id(), '00000000-0000-7000-8003-000000910006', '00000000-0000-7000-8003-000000910106',
   '00000000-0000-7000-8003-000000000002', '2026-12-06 08:00:00+07'::timestamptz, 1, :'t0'::timestamptz - interval '3 days');
INSERT INTO assignment_item (id, org_id, set_id, position, content_org_id, problem_id, problem_version_id, assignment_id, answer_snapshot) VALUES
  ('00000000-0000-7000-8003-000000920051', '00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8003-000000920050', 1,
   platform_org_id(), '00000000-0000-7000-8003-000000910005', '00000000-0000-7000-8003-000000910105',
   '00000000-0000-7000-8003-000000920151', '24'),
  ('00000000-0000-7000-8003-000000920052', '00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8003-000000920050', 2,
   platform_org_id(), '00000000-0000-7000-8003-000000910006', '00000000-0000-7000-8003-000000910106',
   '00000000-0000-7000-8003-000000920152', '5');
