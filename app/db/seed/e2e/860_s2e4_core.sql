-- S2-E4 (T-261126-24) · dữ liệu cho ca kiểm "Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng" (US-S2E4-01…03).
-- Chạy sau 800 (S2-E1), 820 (S2-E5), 840 (S2-E3), 850 (S2-E6); không sửa dữ liệu của epic khác. UUID: F-S2-nnn →
-- 00000000-0000-7000-8002-<nnn 12 chữ số>, khoảng 86xxxxxxx của task (fixture-ids.d/s2e4.json).
--
-- Hai tổ chức riêng (Kho chung của S2-E6 tính theo tổ chức):
--   "Nhóm thử S2-E4"          F-S2-860001: Kho chung RỖNG. Giáo viên theo (TC, làn): F-S2-(861000000 + TC × 100 + làn),
--                             tên "Cô Trang", TC 1…40 (1…27 = TC-S2E4-001…027; 28…40 dành cho ca viết thêm), làn 0…5 = api,
--                             ui-chromium, ui-webkit, ui-iphone, mobile-iphone, mobile-pixel. Mỗi ca một tài khoản vì nháp,
--                             lượt chat và giới hạn thêm đề tính theo người.
--   "Nhóm thử S2-E4 có kho"   F-S2-860002: Kho chung có ĐÚNG đề FX-10 (bản F-S2-860120, do Cô Hà F-S2-860005 gửi, Mai
--                             F-S2-860004 soát); Cô Lan F-S2-860003 có đề FX-12 trong "Của tôi" của riêng cô (sổ F-S2-860111,
--                             KHÔNG ở Kho chung). Giáo viên theo (TC, làn): F-S2-(862000000 + TC × 100 + làn), TC 1…40.
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000860001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E4'),
  ('00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E4 có kho');

INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT format('00000000-0000-7000-8002-%s', lpad((861000000 + tc * 100 + l)::text, 12, '0'))::uuid,
       '00000000-0000-7000-8002-000000860001', 'teacher', 'sample', 'Cô Trang'
FROM generate_series(1, 40) tc, generate_series(0, 5) l;
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT format('00000000-0000-7000-8002-%s', lpad((862000000 + tc * 100 + l)::text, 12, '0'))::uuid,
       '00000000-0000-7000-8002-000000860002', 'teacher', 'sample', 'Cô Trang'
FROM generate_series(1, 40) tc, generate_series(0, 5) l;
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000860003', '00000000-0000-7000-8002-000000860002', 'teacher', 'sample', 'Cô Lan'),
  ('00000000-0000-7000-8002-000000860004', '00000000-0000-7000-8002-000000860002', 'teacher', 'sample', 'Mai'),
  ('00000000-0000-7000-8002-000000860005', '00000000-0000-7000-8002-000000860002', 'teacher', 'sample', 'Cô Hà'),
  ('00000000-0000-7000-8002-000000860006', '00000000-0000-7000-8002-000000860001', 'student', 'light', 'An');
INSERT INTO user_role_grant (org_id, user_id, role, granted_by) VALUES
  ('00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860004', 'content_reviewer', 'chủ sản phẩm (seed)');

-- Cô Hà: đề FX-10 đã duyệt và đã vào Kho chung. Cô Lan: đề FX-12 chỉ ở "Của tôi" của cô.
INSERT INTO solution_doc (id, org_id, owner_user_id, code, title, statement, figure, in_my_shelf, latest_version, created_at) VALUES
  ('00000000-0000-7000-8002-000000860110', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860005', NULL,
   'PL-01 Góc A của tam giác 5, 12, 13', 'Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.',
   '{"plane": {"shape": "triangle", "points": ["A", "B", "C"], "given": [{"type": "length", "of": ["A", "B"], "value": 5}, {"type": "length", "of": ["A", "C"], "value": 12}, {"type": "length", "of": ["B", "C"], "value": 13}], "asked": {"name": "góc A", "kind": "angle", "of": ["B", "A", "C"]}}}',
   true, 1, :'t0'::timestamptz - interval '5 hours'),
  ('00000000-0000-7000-8002-000000860111', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860003', NULL,
   'PL-03 Đường chéo hình chữ nhật', 'Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC.', NULL,
   true, 1, :'t0'::timestamptz - interval '4 hours');
INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status, approved_by,
                              approved_at, created_at) VALUES
  ('00000000-0000-7000-8002-000000860115', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860110', 1,
   '["AB^2 + AC^2 = 5^2 + 12^2 = 169.", "BC^2 = 13^2 = 169.", "Vậy tam giác ABC vuông tại A, góc A = 90°."]', false,
   '00000000-0000-7000-8002-000000860005', 'bản duyệt', 'teacher_approved', '00000000-0000-7000-8002-000000860005',
   :'t0'::timestamptz - interval '290 minutes', :'t0'::timestamptz - interval '295 minutes'),
  ('00000000-0000-7000-8002-000000860116', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860111', 1,
   '["AC^2 = AB^2 + BC^2 = 6^2 + 8^2 = 100.", "Vậy AC = 10."]', false,
   '00000000-0000-7000-8002-000000860003', 'bản duyệt', 'teacher_approved', '00000000-0000-7000-8002-000000860003',
   :'t0'::timestamptz - interval '230 minutes', :'t0'::timestamptz - interval '235 minutes');
INSERT INTO library_submission (id, org_id, doc_id, version_id, owner_user_id, status, submitted_at, decided_by, decided_at,
                                library_id) VALUES
  ('00000000-0000-7000-8002-000000860118', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860110',
   '00000000-0000-7000-8002-000000860115', '00000000-0000-7000-8002-000000860005', 'accepted',
   :'t0'::timestamptz - interval '200 minutes', '00000000-0000-7000-8002-000000860004', :'t0'::timestamptz - interval '190 minutes',
   '00000000-0000-7000-8002-000000860120');
INSERT INTO library_problem (id, org_id, submission_id, source_doc_id, owner_user_id, title, statement, figure, kind, steps, hint,
                             machine_status, human_status, approved_code, accepted_by, accepted_at) VALUES
  ('00000000-0000-7000-8002-000000860120', '00000000-0000-7000-8002-000000860002', '00000000-0000-7000-8002-000000860118',
   '00000000-0000-7000-8002-000000860110', '00000000-0000-7000-8002-000000860005', 'PL-01 Góc A của tam giác 5, 12, 13',
   'Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.',
   '{"plane": {"shape": "triangle", "points": ["A", "B", "C"], "given": [{"type": "length", "of": ["A", "B"], "value": 5}, {"type": "length", "of": ["A", "C"], "value": 12}, {"type": "length", "of": ["B", "C"], "value": 13}], "asked": {"name": "góc A", "kind": "angle", "of": ["B", "A", "C"]}}}',
   'plane', '["AB^2 + AC^2 = 5^2 + 12^2 = 169.", "BC^2 = 13^2 = 169.", "Vậy tam giác ABC vuông tại A, góc A = 90°."]',
   'Thử định lý Pythagore đảo.', 'needs_review', 'teacher_approved', NULL, '00000000-0000-7000-8002-000000860004',
   :'t0'::timestamptz - interval '190 minutes');
