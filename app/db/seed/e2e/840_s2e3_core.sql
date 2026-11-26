-- S2-E3 (T-261126-22) · dữ liệu cho ca kiểm "Thêm đề có AI soạn nháp" (khung C2-02, US-S2E3-01…04) và US-S2E5-05
-- ("Áp dụng cho lớp"). Chạy sau 800 (S2-E1), 820 (S2-E5); không sửa dữ liệu của epic khác. UUID: F-S2-nnn →
-- 00000000-0000-7000-8002-<nnn 12 chữ số>, khoảng 84xxxxxxx của task (fixture-ids.d/s2e3.json).
--
-- Tổ chức riêng "Nhóm thử S2-E3" (F-S2-840001): không đổi số người / lớp / bài mà ca của epic khác đếm.
--   Giáo viên theo (TC, làn): F-S2-(841000000 + TC × 100 + làn), tên "Cô Trang" — mỗi ca một tài khoản vì nháp tính
--   giới hạn 20 đề mỗi giờ (BR-S2E3-05) và tác vụ dựng theo người. làn 0…5 = api, ui-chromium, ui-webkit, ui-iphone,
--   mobile-iphone, mobile-pixel. TC 1…39 của S2-E3.
--   Cô Lan F-S2-840002 (giáo viên thứ hai, US-S2E3-03-FC3, US-S2E5-05-FC6).
-- US-S2E5-05 (TC 53…66): Cô Trang F-S2-840003 chủ sổ lời giải; lớp 11A1 F-S2-840011 (An F-S2-840021, Chi F-S2-840022),
--   11A2 F-S2-840012 (Bình F-S2-840023). Theo (TC, làn), n = TC × 10 + làn:
--     đề kho SP-01            F-S2-(842000000 + n × 10), version + 1   (bộ 's2e3_apply', chỉ tổ chức trên thấy)
--     sổ lời giải             F-S2-(843000000 + n)                      v1 AI soạn sai bước 3, v2 "Giáo viên đã duyệt" (45°)
--     BG-1 (11A1) / BG-2 (11A2) F-S2-(845000000 + n × 10 + 1 / 2)       cùng đề, ghim v2 bằng trigger lúc tạo bài giao
--   BG-1 khóa tới T0 + 2 giờ (TC 60: đã mở, 2 học sinh đã xem lời giải v2); BG-2 đã mở (T0 − 1 giờ).
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000840001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E3');
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT format('00000000-0000-7000-8002-%s', lpad((841000000 + tc * 100 + l)::text, 12, '0'))::uuid,
       '00000000-0000-7000-8002-000000840001', 'teacher', 'sample', 'Cô Trang'
FROM generate_series(1, 39) tc, generate_series(0, 5) l;
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000840002', '00000000-0000-7000-8002-000000840001', 'teacher', 'sample', 'Cô Lan'),
  ('00000000-0000-7000-8002-000000840003', '00000000-0000-7000-8002-000000840001', 'teacher', 'sample', 'Cô Trang'),
  ('00000000-0000-7000-8002-000000840021', '00000000-0000-7000-8002-000000840001', 'student', 'light', 'An'),
  ('00000000-0000-7000-8002-000000840022', '00000000-0000-7000-8002-000000840001', 'student', 'light', 'Chi'),
  ('00000000-0000-7000-8002-000000840023', '00000000-0000-7000-8002-000000840001', 'student', 'light', 'Bình');
INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8002-000000840001', 's2e3_apply');
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id) VALUES
  ('00000000-0000-7000-8002-000000840011', '00000000-0000-7000-8002-000000840001', '11A1', 'thpt', 11, 'S3APAA', '00000000-0000-7000-8002-000000840003'),
  ('00000000-0000-7000-8002-000000840012', '00000000-0000-7000-8002-000000840001', '11A2', 'thpt', 11, 'S3APBB', '00000000-0000-7000-8002-000000840003');
INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES
  ('00000000-0000-7000-8002-000000840001', '00000000-0000-7000-8002-000000840011', '00000000-0000-7000-8002-000000840021', 'an'),
  ('00000000-0000-7000-8002-000000840001', '00000000-0000-7000-8002-000000840011', '00000000-0000-7000-8002-000000840022', 'chi'),
  ('00000000-0000-7000-8002-000000840001', '00000000-0000-7000-8002-000000840012', '00000000-0000-7000-8002-000000840023', 'binh');

CREATE TEMP TABLE t_s3 ON COMMIT DROP AS
SELECT tc, l, tc * 10 + l AS n,
       format('00000000-0000-7000-8002-%s', lpad((842000000 + (tc * 10 + l) * 10)::text, 12, '0'))::uuid AS p,
       format('00000000-0000-7000-8002-%s', lpad((842000000 + (tc * 10 + l) * 10 + 1)::text, 12, '0'))::uuid AS pv,
       format('00000000-0000-7000-8002-%s', lpad((843000000 + tc * 10 + l)::text, 12, '0'))::uuid AS doc,
       format('00000000-0000-7000-8002-%s', lpad((844000000 + (tc * 10 + l) * 10 + 1)::text, 12, '0'))::uuid AS v1,
       format('00000000-0000-7000-8002-%s', lpad((844000000 + (tc * 10 + l) * 10 + 2)::text, 12, '0'))::uuid AS v2,
       format('00000000-0000-7000-8002-%s', lpad((845000000 + (tc * 10 + l) * 10 + 1)::text, 12, '0'))::uuid AS bg1,
       format('00000000-0000-7000-8002-%s', lpad((845000000 + (tc * 10 + l) * 10 + 2)::text, 12, '0'))::uuid AS bg2
FROM generate_series(53, 66) tc, generate_series(0, 5) l;

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, bank, created_at)
SELECT t.p, platform_org_id(), 1, 'catalog', '00000000-0000-7000-8000-0000000000aa', 'thpt', 'pyramid', 1, 'SP-01',
       's2e3_apply', :'t0'::timestamptz - interval '10 days'
FROM t_s3 t;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT t.pv, platform_org_id(), t.p, 1,
       'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
       digest('s2e3 SP-01 ' || t.n, 'sha256'), 'ai_drafted', true, '00000000-0000-7000-8000-0000000000aa',
       :'t0'::timestamptz - interval '10 days'
FROM t_s3 t;
UPDATE problem p SET current_version_id = t.pv FROM t_s3 t WHERE p.org_id = platform_org_id() AND p.id = t.p;

INSERT INTO solution_doc (id, org_id, owner_user_id, code, title, statement, figure, content_org_id, problem_version_id,
                          latest_version, in_my_shelf, created_at)
SELECT t.doc, '00000000-0000-7000-8002-000000840001', '00000000-0000-7000-8002-000000840003', 'SP-01',
       'Góc giữa SC và mặt đáy',
       'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
       '{"catalogCode": "HKN-02"}'::jsonb, platform_org_id(), t.pv, 2, true, :'t0'::timestamptz - interval '3 hours'
FROM t_s3 t;
INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status, created_at)
SELECT t.v1, '00000000-0000-7000-8002-000000840001', t.doc, 1,
       '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = SA / AC = √2.", "Vậy góc giữa SC và mặt phẳng đáy bằng 54,7°."]'::jsonb,
       true, NULL, 'AI soạn', 'ai_drafted', :'t0'::timestamptz - interval '3 hours'
FROM t_s3 t;
INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status,
                              approved_by, approved_at, created_at)
SELECT t.v2, '00000000-0000-7000-8002-000000840001', t.doc, 2,
       '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = a√2 / a√2 = 1.", "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."]'::jsonb,
       false, '00000000-0000-7000-8002-000000840003', 'sửa bước 3, 4', 'teacher_approved',
       '00000000-0000-7000-8002-000000840003', :'t0'::timestamptz - interval '150 minutes',
       :'t0'::timestamptz - interval '160 minutes'
FROM t_s3 t;
-- Bài giao: trigger assignment_pin_solution (V160) ghim v2 "Giáo viên đã duyệt" vào từng bài.
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT t.bg1, '00000000-0000-7000-8002-000000840001', 'class', '00000000-0000-7000-8002-000000840011', platform_org_id(),
       t.p, t.pv, '00000000-0000-7000-8002-000000840003',
       CASE WHEN t.tc = 60 THEN :'t0'::timestamptz - interval '1 hour' ELSE :'t0'::timestamptz + interval '2 hours' END,
       true, true, :'t0'::timestamptz - interval '2 hours', 1
FROM t_s3 t;
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT t.bg2, '00000000-0000-7000-8002-000000840001', 'class', '00000000-0000-7000-8002-000000840012', platform_org_id(),
       t.p, t.pv, '00000000-0000-7000-8002-000000840003', :'t0'::timestamptz - interval '1 hour',
       true, true, :'t0'::timestamptz - interval '2 hours', 1
FROM t_s3 t;
-- TC 60: An và Chi đã xem lời giải v2 của BG-1 (BR-S2E5-25).
INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at, solution_viewed_at)
SELECT '00000000-0000-7000-8002-000000840001', t.bg1, u.id, :'t0'::timestamptz - interval '50 minutes',
       :'t0'::timestamptz - interval '40 minutes', :'t0'::timestamptz - interval '45 minutes'
FROM t_s3 t, (VALUES ('00000000-0000-7000-8002-000000840021'::uuid), ('00000000-0000-7000-8002-000000840022'::uuid)) u(id)
WHERE t.tc = 60;
