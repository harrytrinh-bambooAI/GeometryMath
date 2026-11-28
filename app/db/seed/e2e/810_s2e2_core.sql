-- S2-E2 (T-261126-19) · dữ liệu riêng cho ca kiểm màn làm bài hình phẳng (US-S2E2-03, khung C2-05).
-- Chạy sau MVP 1xx–7xx; không sửa dữ liệu của epic khác. UUID theo PLAN_code-s23 §4: F-S2-nnn → 00000000-0000-7000-8002-<nnn>,
-- khoảng 810xxx của task (khai báo ở app/db/seed/fixture-ids.d/s2e2.json).
-- Tổ chức riêng kiểu 'group' để không đổi số trường/lớp/học sinh mà ca MVP đếm. Học sinh "An" đăng nhập bằng thẻ dev
-- (chưa dùng mã HS- của S2-E1 vì S2-E1 làm song song, T-261126-18).
-- Đề phẳng PL-01…04 (FX-10…13) và PL-90 (hình do AI dựng, nhãn "Cần xem lại") là đề kho nền tảng loại 'plane', bộ 's2e2_plane'
-- (không phải 'main': Kho đề MVP giữ sáu đề, TC-GEO-0125). Mô tả dựng ở plane_problem_spec (không chứa đáp số).
-- Mọi ca UI chỉ đọc (đo trên thiết bị) nên một làn dữ liệu dùng chung cho mọi dự án Playwright.

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000810001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E2');

INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000810003', '00000000-0000-7000-8002-000000810001', 'teacher', 'sample', 'Cô Trang'),
  ('00000000-0000-7000-8002-000000810020', '00000000-0000-7000-8002-000000810001', 'student', 'light', 'An');

INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id) VALUES
  ('00000000-0000-7000-8002-000000810010', '00000000-0000-7000-8002-000000810001', '11A1 phẳng', 'thpt', 11, 'S2PLAK',
   '00000000-0000-7000-8002-000000810003'),
  ('00000000-0000-7000-8002-000000810011', '00000000-0000-7000-8002-000000810001', '11A2 phẳng', 'thpt', 11, 'S2PLBM',
   '00000000-0000-7000-8002-000000810003');

INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES
  ('00000000-0000-7000-8002-000000810001', '00000000-0000-7000-8002-000000810010', '00000000-0000-7000-8002-000000810020', 'an'),
  ('00000000-0000-7000-8002-000000810001', '00000000-0000-7000-8002-000000810011', '00000000-0000-7000-8002-000000810020', 'an');

CREATE TEMP TABLE t_pl (code text, n int, approval approval_status, statement text, spec jsonb) ON COMMIT DROP;
INSERT INTO t_pl VALUES
 ('PL-01', 101, 'teacher_approved', 'Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.',
  '{"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5},{"type":"length","of":["A","C"],"value":12},{"type":"length","of":["B","C"],"value":13}],"asked":{"name":"góc A","kind":"angle","of":["B","A","C"]}}'),
 ('PL-02', 102, 'teacher_approved', 'Tam giác ABC cân tại A, góc A = 40°. Tính góc B.',
  '{"shape":"isosceles_triangle","at":"A","given":[{"type":"angle","of":["B","A","C"],"value":40}],"asked":{"name":"góc B","kind":"angle","of":["A","B","C"]}}'),
 ('PL-03', 103, 'teacher_approved', 'Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.',
  '{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":6},{"type":"length","of":["A","D"],"value":8}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}'),
 ('PL-04', 104, 'teacher_approved', 'Tam giác đều ABC cạnh 4. Tính diện tích tam giác.',
  '{"shape":"equilateral_triangle","given":[{"type":"length","of":["A","B"],"value":4}],"asked":{"name":"S","kind":"area","of":[]}}'),
 ('PL-90', 190, 'ai_drafted', 'Tam giác ABC có AB = 4, chân đường cao từ C là H với AH = 1, CH = 3. Tính diện tích tam giác.',
  '{"shape":"triangle","coords":{"A":[0,0],"B":[4,0],"C":[1,3]},"given":[{"type":"length","of":["A","B"],"value":4}],"asked":{"name":"S","kind":"area","of":[]}}');

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, kind, created_at)
SELECT format('00000000-0000-7000-8002-000000810%s', t.n)::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', 'thpt', NULL, 1, t.code, t.code, 's2e2_plane', 'plane',
       :'t0'::timestamptz - interval '10 days'
FROM t_pl t;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT format('00000000-0000-7000-8002-000000811%s', t.n)::uuid, platform_org_id(),
       format('00000000-0000-7000-8002-000000810%s', t.n)::uuid, 1, t.statement,
       digest(regexp_replace(btrim(t.statement), '\s+', ' ', 'g'), 'sha256'), t.approval, true,
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '10 days'
FROM t_pl t;
UPDATE problem pr SET current_version_id = format('00000000-0000-7000-8002-000000811%s', t.n)::uuid
FROM t_pl t WHERE pr.org_id = platform_org_id() AND pr.id = format('00000000-0000-7000-8002-000000810%s', t.n)::uuid;
INSERT INTO plane_problem_spec (org_id, problem_version_id, spec)
SELECT platform_org_id(), format('00000000-0000-7000-8002-000000811%s', t.n)::uuid, t.spec FROM t_pl t;

-- Bài giao cho lớp 11A1 phẳng (cls 0) / 11A2 phẳng (cls 1) (id 8123nn): khóa = giờ mở T0 + 2 giờ (FX-20); mở = T0 − 1 giờ.
CREATE TEMP TABLE t_pa (n text, pn int, release_offset interval, cls text DEFAULT '0') ON COMMIT DROP;
INSERT INTO t_pa (n, pn, release_offset, cls) VALUES
 ('01', 101, interval '2 hours', '0'),     -- PL-01 đang khóa (TC-S2E2-022, 023, 024)
 ('02', 101, interval '-1 hour', '1'), -- PL-01 đã mở (TC-S2E2-025), lớp 11A2 phẳng (một lớp không giao trùng đề)
 ('03', 102, interval '2 hours', '0'),     -- PL-02 đang khóa (TC-S2E2-026)
 ('04', 103, interval '2 hours', '0'),     -- PL-03 (TC-S2E2-029)
 ('05', 190, interval '2 hours', '0'),     -- PL-90 hình AI dựng, "Cần xem lại" (TC-S2E2-027)
 ('06', 104, interval '2 hours', '0');     -- PL-04
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT format('00000000-0000-7000-8002-0000008123%s', a.n)::uuid, '00000000-0000-7000-8002-000000810001', 'class',
       format('00000000-0000-7000-8002-00000081001%s', a.cls)::uuid, platform_org_id(),
       format('00000000-0000-7000-8002-000000810%s', a.pn)::uuid, format('00000000-0000-7000-8002-000000811%s', a.pn)::uuid,
       '00000000-0000-7000-8002-000000810003', :'t0'::timestamptz + a.release_offset, true, true,
       :'t0'::timestamptz - interval '1 day', 1
FROM t_pa a;
