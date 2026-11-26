-- S2-E5 (T-261126-20) · dữ liệu cho ca kiểm "Soát và duyệt lời giải" (khung C2-03, US-S2E5-01…04).
-- Chạy sau 800 (S2-E1) và 810 (S2-E2); không sửa dữ liệu của epic khác. UUID theo PLAN_code-s23 §4: F-S2-nnn →
-- 00000000-0000-7000-8002-<nnn 12 chữ số>, khoảng 82xxxxxxx của task (fixture-ids.d/s2e5.json).
--
-- Đề FX-14 (SP-01): hình chóp S.ABCD, đáy vuông cạnh a, SA ⊥ (ABCD), SA = a√2; góc giữa SC và đáy = 45° (mô tả dựng
-- HKN-02 của engine). Lời giải v1 do AI soạn có bước 3 sai (tan SCA = √2) và bước 4 ghi 54,7°.
--
-- Mỗi ca kiểm làm BẨN dữ liệu (lưu phiên bản, kiểm, duyệt) nên có sổ lời giải riêng theo (mã TC, làn):
--   sổ      F-S2-(820000000 + TC × 100 + làn)      làn 0…5 = api, ui-chromium, ui-webkit, ui-iphone, mobile-iphone, mobile-pixel
--   v1      F-S2-(821000000 + TC × 1000 + làn × 10 + 1)
-- Chủ sổ: Cô Trang FX-01 (F-S2-001, GV-7K2M-9Q4D, tổ chức "Cộng đồng" của 800_s2e1_core.sql). Giáo viên khác cùng tổ
-- chức: Cô Lan (F-S2-820002). Học sinh gọi API bị từ chối: An FX-02 (F-S2-002).
-- Riêng TC 36 và 65 cần bài giao nên nằm ở tổ chức riêng "Nhóm thử S2-E5" (không đổi số lớp/bài mà ca khác đếm):
--   Cô Trang (F-S2-820003), An (F-S2-820020), lớp 11A1 theo làn (F-S2-82001<làn>; 820016 cho TC 36), đề kho SP-01 bộ 's2e5_review'.

INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000820002', '00000000-0000-7000-8002-000000000100', 'teacher', 'verified', 'Cô Lan');

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000820001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E5');
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000820003', '00000000-0000-7000-8002-000000820001', 'teacher', 'sample', 'Cô Trang'),
  ('00000000-0000-7000-8002-000000820020', '00000000-0000-7000-8002-000000820001', 'student', 'light', 'An');
INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8002-000000820001', 's2e5_review');

-- Lớp 11A1 theo làn (mỗi làn giao đề một lần: D-GEO-161 cấm giao trùng đề cho cùng lớp); An thuộc mọi lớp.
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT format('00000000-0000-7000-8002-00000082001%s', l)::uuid, '00000000-0000-7000-8002-000000820001', '11A1', 'thpt',
       11, 'S5RVA' || chr(65 + l), '00000000-0000-7000-8002-000000820003'
FROM generate_series(0, 6) l;
INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT '00000000-0000-7000-8002-000000820001', format('00000000-0000-7000-8002-00000082001%s', l)::uuid,
       '00000000-0000-7000-8002-000000820020', 'an'
FROM generate_series(0, 6) l;

-- Đề kho SP-01 theo làn k = 0…6 (đề F-S2-(820100 + 10k), version F-S2-(820101 + 10k); k = 6 cho TC 36): một giáo viên
-- chỉ có một sổ lời giải cho một version đề (solution_doc_pv_uq). Không có mã bài: ngoài bộ kiểm chứng cả kho của E5;
-- bộ 's2e5_review' chỉ tổ chức trên thấy.
INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, bank, created_at)
SELECT format('00000000-0000-7000-8002-0000008201%s0', k)::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', 'thpt', 'pyramid', 1, 'SP-01', 's2e5_review',
       :'t0'::timestamptz - interval '10 days'
FROM generate_series(0, 6) k;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT format('00000000-0000-7000-8002-0000008201%s1', k)::uuid, platform_org_id(),
       format('00000000-0000-7000-8002-0000008201%s0', k)::uuid, 1,
       'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
       digest('s2e5 SP-01 FX-14 làn ' || k, 'sha256'), 'ai_drafted', true, '00000000-0000-7000-8000-0000000000aa',
       :'t0'::timestamptz - interval '10 days'
FROM generate_series(0, 6) k;
UPDATE problem p SET current_version_id = format('00000000-0000-7000-8002-0000008201%s1', k)::uuid
FROM generate_series(0, 6) k
WHERE p.org_id = platform_org_id() AND p.id = format('00000000-0000-7000-8002-0000008201%s0', k)::uuid;

-- Sổ lời giải theo (TC, làn).
CREATE TEMP TABLE t_s5 ON COMMIT DROP AS
SELECT tc, l,
       format('00000000-0000-7000-8002-%s', lpad((820000000 + tc * 100 + l)::text, 12, '0'))::uuid AS doc,
       format('00000000-0000-7000-8002-%s', lpad((821000000 + tc * 1000 + l * 10 + 1)::text, 12, '0'))::uuid AS v1,
       (tc IN (36, 65)) AS own_org,
       CASE WHEN tc = 65 THEN format('00000000-0000-7000-8002-0000008201%s1', l)::uuid
            WHEN tc = 36 AND l = 0 THEN '00000000-0000-7000-8002-000000820161'::uuid END AS pv
FROM generate_series(1, 70) tc, generate_series(0, 5) l;

INSERT INTO solution_doc (id, org_id, owner_user_id, code, title, statement, figure, content_org_id, problem_version_id,
                          latest_version, created_at)
SELECT t.doc,
       CASE WHEN t.own_org THEN '00000000-0000-7000-8002-000000820001' ELSE '00000000-0000-7000-8002-000000000100' END::uuid,
       CASE WHEN t.own_org THEN '00000000-0000-7000-8002-000000820003' ELSE '00000000-0000-7000-8002-000000000001' END::uuid,
       'SP-01', 'Góc giữa SC và mặt đáy',
       'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
       '{"catalogCode": "HKN-02"}'::jsonb,
       CASE WHEN t.pv IS NOT NULL THEN platform_org_id() END, t.pv,
       1, :'t0'::timestamptz - interval '2 hours'
FROM t_s5 t;

INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status, created_at)
SELECT t.v1,
       CASE WHEN t.own_org THEN '00000000-0000-7000-8002-000000820001' ELSE '00000000-0000-7000-8002-000000000100' END::uuid,
       t.doc, 1,
       '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = SA / AC = √2.", "Vậy góc giữa SC và mặt phẳng đáy bằng 54,7°."]'::jsonb,
       true, NULL, 'AI soạn', 'ai_drafted', :'t0'::timestamptz - interval '2 hours'
FROM t_s5 t;

-- Đề xuất của AI đang chờ trong hộp duyệt (TC 45, 46, 47): thêm bước giải thích sau bước 2.
INSERT INTO review_inbox_item (id, org_id, doc_id, after_step, content, created_at)
SELECT format('00000000-0000-7000-8002-%s', lpad((822000000 + t.tc * 100 + t.l)::text, 12, '0'))::uuid,
       '00000000-0000-7000-8002-000000000100', t.doc, 2, 'Vậy tam giác SAC vuông cân tại A.',
       :'t0'::timestamptz - interval '1 hour'
FROM t_s5 t WHERE t.tc IN (45, 46, 47);

-- TC 36 (chỉ đọc, làn 0): v2 "Giáo viên đã duyệt" + "Cần xem lại" đã giao cho lớp 11A1 (ghim bằng trigger lúc tạo bài
-- giao); v3 mới hơn "Đang sửa" + "Đã kiểm chứng" chưa áp dụng cho bài giao.
INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status,
                              approved_by, approved_at, created_at)
VALUES ('00000000-0000-7000-8002-000821036002', '00000000-0000-7000-8002-000000820001',
        '00000000-0000-7000-8002-000820003600', 2,
        '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "Tam giác SAC vuông cân tại A.", "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."]'::jsonb,
        false, '00000000-0000-7000-8002-000000820003', 'sửa bước 3, 4', 'teacher_approved',
        '00000000-0000-7000-8002-000000820003', :'t0'::timestamptz - interval '80 minutes',
        :'t0'::timestamptz - interval '90 minutes');
UPDATE solution_doc SET latest_version = 2, in_my_shelf = true
WHERE org_id = '00000000-0000-7000-8002-000000820001' AND id = '00000000-0000-7000-8002-000820003600';

INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
VALUES ('00000000-0000-7000-8002-000000823600', '00000000-0000-7000-8002-000000820001', 'class',
        '00000000-0000-7000-8002-000000820016', platform_org_id(), '00000000-0000-7000-8002-000000820160',
        '00000000-0000-7000-8002-000000820161', '00000000-0000-7000-8002-000000820003', :'t0'::timestamptz + interval '2 hours',
        true, true, :'t0'::timestamptz - interval '70 minutes', 1);

INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status, created_at)
VALUES ('00000000-0000-7000-8002-000821036003', '00000000-0000-7000-8002-000000820001',
        '00000000-0000-7000-8002-000820003600', 3,
        '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = a√2 / a√2 = 1.", "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."]'::jsonb,
        false, '00000000-0000-7000-8002-000000820003', 'sửa bước 3', 'editing', :'t0'::timestamptz - interval '30 minutes');
INSERT INTO step_check_result (org_id, version_id, step_no, status, reason, written, computed) VALUES
  ('00000000-0000-7000-8002-000000820001', '00000000-0000-7000-8002-000821036003', 1, 'unverifiable', 'bước không có phép tính', NULL, NULL),
  ('00000000-0000-7000-8002-000000820001', '00000000-0000-7000-8002-000821036003', 2, 'pass', 'máy tính lại khớp', 1.4142135623730951, 1.4142135623730951),
  ('00000000-0000-7000-8002-000000820001', '00000000-0000-7000-8002-000821036003', 3, 'pass', 'máy tính lại khớp', 1, 1),
  ('00000000-0000-7000-8002-000000820001', '00000000-0000-7000-8002-000821036003', 4, 'pass', 'máy tính lại khớp', 45, 45);
UPDATE solution_version SET final_state = 'match', machine_status = 'verified', checked_at = :'t0'::timestamptz - interval '20 minutes'
WHERE org_id = '00000000-0000-7000-8002-000000820001' AND id = '00000000-0000-7000-8002-000821036003';
UPDATE solution_doc SET latest_version = 3
WHERE org_id = '00000000-0000-7000-8002-000000820001' AND id = '00000000-0000-7000-8002-000820003600';
