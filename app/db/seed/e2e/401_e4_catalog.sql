-- E4 · dữ liệu riêng cho ca kiểm của epic Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học (chạy sau 100…300; KHÔNG sửa
-- giá trị nào mà epic khác đã kiểm). UUID: 00000000-0000-7000-8000-000000e4xxxx.
--
-- 1) Tên ngắn của sáu đề kho (nhật ký hỏi đáp ghi "tên bài", US-GEO-410-SC1: "Hình chóp S.ABCD: tính góc giữa SD và mặt
--    đáy"; mẫu tên theo Design/prototype/index.html). problem.title là cột cập nhật được, không ai khác đọc.
UPDATE problem p SET title = t.title
FROM (VALUES
  ('HKG-01', 'Hình chóp S.ABCD: tính góc giữa SD và mặt đáy'),
  ('HKG-02', 'Hình lập phương: khoảng cách giữa AC và B′D′'),
  ('HKG-03', 'Lăng trụ đứng tam giác đều: tính thể tích'),
  ('HKG-04', 'Hình nón: diện tích xung quanh và thể tích'),
  ('HKG-05', 'Hình trụ: diện tích toàn phần'),
  ('HKG-06', 'Mặt cầu ngoại tiếp hình lập phương')
) AS t(code, title)
WHERE p.org_id = platform_org_id() AND p.code = t.code;

-- 2) Chữ ký chuẩn của sáu đề kho (D-GEO-150, ADR-GEO-15): chuỗi chuẩn giống hệt GeometryEngine.signature (Java) cho mô tả
--    dựng ở server/src/main/resources/geometry/problem-specs.json; IT E4 (HelpE4IT.signaturesMatchSeed) đối chiếu từng hàng
--    với Java. Bản 2 (T-05, 27/11/2026): đại lượng hỏi theo GIÁ TRỊ đo trên bản dựng đã quy về a = 1, không theo danh sách điểm.
INSERT INTO problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
SELECT platform_org_id(), format('00000000-0000-7000-8000-%s', lpad((100000 + s.n)::text, 12, '0'))::uuid, 2,
       (SELECT st.id FROM solid_template st WHERE st.code = s.solid), s.params::jsonb, s.asked,
       digest(convert_to(s.canonical, 'UTF8'), 'sha256')
FROM (VALUES
  (31, 'pyramid',  '{"a":1,"sa":1}',          'angle_line_plane=45',
       '2|pyramid_square_sa|a=1;sa=1|angle_line_plane=45'),
  (32, 'cube',     '{"a":1}',                 'dist_lines=1',
       '2|cube|a=1|dist_lines=1'),
  (33, 'prism',    '{"a":1,"h":1.5}',         'volume_prism=0.649519',
       '2|prism|a=1;h=1.5|volume_prism=0.649519'),
  (34, 'cone',     '{"r":1,"l":1.666667}',    'cone_lateral=5.235989+cone_volume=1.396264',
       '2|cone_rl|r=1;l=1.666667|cone_lateral=5.235989+cone_volume=1.396264'),
  (35, 'cylinder', '{"r":1,"h":2.5}',         'cyl_total_area=21.991149',
       '2|cylinder|r=1;h=2.5|cyl_total_area=21.991149'),
  (36, 'sphere',   '{"a":1}',                 'sphere_radius=0.866025+sphere_volume=2.720699',
       '2|sphere_circum_cube|a=1|sphere_radius=0.866025+sphere_volume=2.720699')
) AS s(n, solid, params, asked, canonical);

-- 3) Phần tô màu và phần vẽ thêm theo bước của lời giải (US-GEO-407, 408; fixture §2 "Dữ liệu hình và khóa" theo khung
--    mvp-06). Chỉ THÊM khóa "highlight", "draw" vào bước; title/body/why giữ nguyên văn seed E1. Bảng solution bất biến
--    (trigger) nên xóa rồi chèn lại đúng hàng đó trong cùng giao dịch reset.
--    highlight: {edges: ["SD"], angles: ["SDA"] (góc tại đỉnh giữa), faces: ["ABC"]} — tên theo nhãn đỉnh của đề.
--    draw (cộng dồn tới bước đang chọn, BR-GEO-187): segment {a, b} đường phụ · right {at, a, b} ký hiệu góc vuông ·
--    arc {at, a, b, label} cung góc kèm số đo.
CREATE TEMP TABLE t_e4_steps (n int, steps jsonb) ON COMMIT DROP;
INSERT INTO t_e4_steps VALUES
 (31, '[{"title":"Tìm hình chiếu của SD","body":"SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD.","why":"Muốn tìm góc giữa đường và mặt, việc đầu tiên là tìm hình chiếu của đường lên mặt.",
         "highlight":{"edges":["SD","AD"]},"draw":[{"kind":"segment","a":"A","b":"D"}]},
        {"title":"Xác định góc cần tìm","body":"Góc giữa SD và (ABCD) là góc SDA. Tam giác SAD vuông tại A.","why":"Có hình chiếu rồi thì góc cần tìm nằm trong một tam giác vuông.",
         "highlight":{"edges":["SD","AD"],"angles":["SDA"]},"draw":[{"kind":"right","at":"A","a":"S","b":"D"}]},
        {"title":"Tính góc","body":"tan SDA = SA / AD = a / a = 1, suy ra góc SDA = 45°.","why":"Trong tam giác vuông, tan của góc nhọn bằng cạnh đối chia cạnh kề.",
         "highlight":{"angles":["SDA"]},"draw":[{"kind":"arc","at":"D","a":"S","b":"A","label":"45°"}]}]'),
 (33, '[{"title":"Tính diện tích đáy","body":"S = 4²·√3 / 4 = 4√3 cm².","why":"Đáy là tam giác đều nên dùng công thức a²√3 / 4.",
         "highlight":{"edges":["AB","BC","CA"]},"draw":[]},
        {"title":"Tính thể tích","body":"V = S · h = 4√3 · 6 = 24√3 cm³.","why":"Lăng trụ đứng: V = diện tích đáy × chiều cao.",
         "highlight":{"edges":["AA′"]},"draw":[]}]'),
 (35, '[{"title":"Áp dụng công thức","body":"Stp = 2π·4·(4 + 10) = 112π cm².","why":"Gộp diện tích xung quanh 2πrh và hai đáy 2πr².",
         "highlight":{},"draw":[]}]');

WITH d AS (
  DELETE FROM solution s USING t_e4_steps t
  WHERE s.org_id = platform_org_id() AND s.problem_version_id = format('00000000-0000-7000-8000-%s', lpad((100000 + t.n)::text, 12, '0'))::uuid
  RETURNING s.*, t.steps AS new_steps)
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, schema_version, answer_forms, answer_forms_version,
                      model, prompt_version, author_user_id, ai_authored, created_at)
SELECT d.org_id, d.problem_version_id, d.final_answer, d.new_steps, d.schema_version, d.answer_forms, d.answer_forms_version,
       d.model, d.prompt_version, d.author_user_id, d.ai_authored, d.created_at
FROM d;

-- 4) Tổ chức thử riêng của E4 (kiểu 'group', như E3: không đổi số trường/lớp/học sinh của THPT Mẫu A mà E1 đếm) và giáo
--    viên của nó. Lớp, học sinh, bài giao của từng ca kiểm do móc POST /test-hooks/e4/sandbox tạo khi chạy (mỗi ca × mỗi
--    dự án Playwright một lớp riêng), nên các ca đổi trạng thái (Mở ngay, xem gợi ý, hỏi) không giẫm nhau và không giẫm E1…E3.
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8000-000000e40001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử E4');
INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8000-000000e40003', '00000000-0000-7000-8000-000000e40001', 'teacher', 'sample', 'Giáo viên mẫu');
