-- S2-E8 (T-261126-21) · dữ liệu riêng cho bài luyện điền đáp số (US-S2E8-01…05, khung C2-06/07/08).
-- Chạy sau MVP 1xx–7xx và 80x/81x; không sửa dữ liệu của epic khác. UUID: F-S2-830nnn → 00000000-0000-7000-8002-000000830nnn
-- (khai báo ở app/db/seed/fixture-ids.d/s2e8.json).
-- Tổ chức riêng kiểu 'group' "Nhóm thử S2-E8" để không đổi số trường/lớp/học sinh/bài giao mà ca MVP đếm. Mỗi ca kiểm tự
-- dựng giáo viên + lớp 11A1/11A2 + học sinh qua móc /test-hooks/s2e8/sandbox rồi soạn và giao bài bằng API thật, nên các
-- ca không giẫm nhau (ràng buộc D-GEO-161: một đề một lần mỗi lớp).
-- Đề luyện là đề kho nền tảng loại 'plane', bộ 's2e8_luyen' (không phải 'main': Kho đề MVP giữ sáu đề; loại 'plane' nên
-- bộ kiểm chứng cả kho của E5 không xét). Đáp số ở solution.final_answer; gợi ý mức 1 ở hint.

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000830001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử S2-E8');
INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8002-000000830001', 's2e8_luyen');

-- Lớp mẫu 11A1 (chuyển từ MVP, BR-S2E8-32) cho bản dev xem tay: Cô Hạnh chủ lớp, An và Bình Đang học.
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8002-000000830003', '00000000-0000-7000-8002-000000830001', 'teacher', 'sample', 'Cô Hạnh'),
  ('00000000-0000-7000-8002-000000830020', '00000000-0000-7000-8002-000000830001', 'student', 'light', 'An'),
  ('00000000-0000-7000-8002-000000830021', '00000000-0000-7000-8002-000000830001', 'student', 'light', 'Bình');
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id) VALUES
  ('00000000-0000-7000-8002-000000830010', '00000000-0000-7000-8002-000000830001', '11A1', 'thpt', 11, 'S2LYAA',
   '00000000-0000-7000-8002-000000830003');
INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES
  ('00000000-0000-7000-8002-000000830001', '00000000-0000-7000-8002-000000830010', '00000000-0000-7000-8002-000000830020', 'an'),
  ('00000000-0000-7000-8002-000000830001', '00000000-0000-7000-8002-000000830010', '00000000-0000-7000-8002-000000830021', 'binh');

-- n: số thứ tự (id đề 830<n>, phiên bản 831<n>). answer NULL = đề chưa có lời giải (không chấm được, BR-S2E8-27).
CREATE TEMP TABLE t_lp (code text, n int, statement text, answer text, hint1 text, spec jsonb) ON COMMIT DROP;
INSERT INTO t_lp VALUES
 ('LC-01', 101, 'Tam giác ABC vuông tại A có AB = 4, AC = 3. Tính diện tích tam giác.', '6',
  'Tam giác vuông có hai cạnh góc vuông. Diện tích tính theo hai cạnh đó thế nào?',
  '{"shape":"triangle","coords":{"A":[0,0],"B":[4,0],"C":[0,3]},"given":[{"type":"length","of":["A","B"],"value":4},{"type":"length","of":["A","C"],"value":3}],"asked":{"name":"S","kind":"area","of":[]}}'),
 ('SP-01', 102, 'Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt đáy.', '45°',
  'Góc giữa SC và mặt đáy là góc giữa SC và hình chiếu của nó lên đáy. Hình chiếu của SC là đoạn nào?',
  '{"shape":"triangle","coords":{"A":[0,0],"C":[1.41421356,0],"S":[0,1.41421356]},"given":[],"asked":{"name":"góc SCA","kind":"angle","of":["S","C","A"]}}'),
 ('LA-01', 103, 'Tam giác đều ABC cạnh 4. Tính diện tích tam giác.', '4√3',
  'Kẻ đường cao của tam giác đều. Đường cao chia cạnh đáy thành hai đoạn thế nào?',
  '{"shape":"equilateral_triangle","given":[{"type":"length","of":["A","B"],"value":4}],"asked":{"name":"S","kind":"area","of":[]}}'),
 ('LB-01', 104, 'Tam giác ABC vuông tại A có AB = 1, AC = 1. Tính diện tích tam giác.', '1/2',
  'Diện tích tam giác vuông bằng nửa tích hai cạnh nào?',
  '{"shape":"triangle","coords":{"A":[0,0],"B":[1,0],"C":[0,1]},"given":[{"type":"length","of":["A","B"],"value":1},{"type":"length","of":["A","C"],"value":1}],"asked":{"name":"S","kind":"area","of":[]}}'),
 ('LE-01', 105, 'Hình vuông ABCD cạnh a. Tính độ dài đường chéo AC theo a.', 'a√2',
  'Đường chéo chia hình vuông thành hai tam giác vuông. Định lý nào liên hệ ba cạnh của tam giác vuông?',
  '{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":1},{"type":"length","of":["A","D"],"value":1}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}'),
 ('LF-01', 106, 'Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính sin A.', '1',
  'So sánh BC² với AB² + AC². Tam giác này có gì đặc biệt?',
  '{"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5},{"type":"length","of":["A","C"],"value":12},{"type":"length","of":["B","C"],"value":13}],"asked":{"name":"góc A","kind":"angle","of":["B","A","C"]}}'),
 ('LN-01', 107, 'Trên trục số, điểm A có tọa độ 2 và điểm B có tọa độ 5. Tính hiệu tọa độ của A trừ tọa độ của B.', '-3',
  'Lấy tọa độ của A trừ tọa độ của B, chú ý dấu của kết quả.', NULL),
 ('LX-01', 190, 'Tam giác ABC có AB = 4, chân đường cao từ C là H với AH = 1, CH = 3. Tính diện tích tam giác.', NULL,
  'Diện tích tam giác bằng nửa tích cạnh đáy và đường cao tương ứng.', NULL),
 ('LU-01', 191, 'Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích.', '24√3 cm³',
  'Thể tích lăng trụ bằng diện tích đáy nhân chiều cao.', NULL);
-- 16 hình chữ nhật có đường chéo nguyên (HCN-01…HCN-16) để đủ bài 20 câu (US-S2E8-03-BV3, US-S2E8-05-BV2).
INSERT INTO t_lp
SELECT format('HCN-%s', lpad(k::text, 2, '0')), 110 + k,
       format('Hình chữ nhật ABCD có AB = %s, AD = %s. Tính AC.', r.m, r.n), r.d::text,
       'AC là cạnh huyền của tam giác vuông ABC. Hai cạnh góc vuông của tam giác đó dài bao nhiêu?',
       jsonb_build_object('shape', 'rectangle',
         'given', jsonb_build_array(jsonb_build_object('type', 'length', 'of', jsonb_build_array('A', 'B'), 'value', r.m),
                                    jsonb_build_object('type', 'length', 'of', jsonb_build_array('A', 'D'), 'value', r.n)),
         'asked', jsonb_build_object('name', 'AC', 'kind', 'length', 'of', jsonb_build_array('A', 'C')))
FROM (VALUES (1, 6, 8, 10), (2, 3, 4, 5), (3, 5, 12, 13), (4, 8, 15, 17), (5, 9, 12, 15), (6, 12, 16, 20), (7, 7, 24, 25),
             (8, 20, 21, 29), (9, 10, 24, 26), (10, 15, 20, 25), (11, 9, 40, 41), (12, 12, 35, 37), (13, 11, 60, 61),
             (14, 16, 30, 34), (15, 18, 24, 30), (16, 21, 28, 35)) AS r(k, m, n, d);

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, kind, created_at)
SELECT format('00000000-0000-7000-8002-000000830%s', t.n)::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', 'thpt', NULL, 1, t.code, t.code, 's2e8_luyen', 'plane',
       :'t0'::timestamptz - interval '10 days'
FROM t_lp t;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT format('00000000-0000-7000-8002-000000831%s', t.n)::uuid, platform_org_id(),
       format('00000000-0000-7000-8002-000000830%s', t.n)::uuid, 1, t.statement,
       digest(regexp_replace(btrim(t.statement), '\s+', ' ', 'g'), 'sha256'), 'teacher_approved', false,
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '10 days'
FROM t_lp t;
UPDATE problem pr SET current_version_id = format('00000000-0000-7000-8002-000000831%s', t.n)::uuid
FROM t_lp t WHERE pr.org_id = platform_org_id() AND pr.id = format('00000000-0000-7000-8002-000000830%s', t.n)::uuid;
INSERT INTO plane_problem_spec (org_id, problem_version_id, spec)
SELECT platform_org_id(), format('00000000-0000-7000-8002-000000831%s', t.n)::uuid, t.spec FROM t_lp t WHERE t.spec IS NOT NULL;
INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT platform_org_id(), format('00000000-0000-7000-8002-000000831%s', t.n)::uuid, 1, t.hint1 FROM t_lp t;
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
SELECT platform_org_id(), format('00000000-0000-7000-8002-000000831%s', t.n)::uuid, t.answer,
       jsonb_build_array(jsonb_build_object('title', 'Lập luận', 'body', 'Xác định đại lượng cần tính trên hình rồi áp dụng công thức.',
                                            'why', 'Bước này nối giả thiết của đề với đại lượng cần tìm.'),
                         jsonb_build_object('title', 'Kết quả', 'body', 'Đáp số: ' || t.answer || '.',
                                            'why', 'Thay số vào công thức ở bước trước.')),
       'gia-lap-1', 'p1', '00000000-0000-7000-8000-0000000000aa', false
FROM t_lp t WHERE t.answer IS NOT NULL;
