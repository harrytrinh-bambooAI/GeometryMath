-- T-261128-64 (S5-E5b, US-S5E5-03 "Nên nhắc lại trên lớp"): ba đề kho nền tảng bộ 's5_kc' cùng họ dạng "khoảng cách từ
-- điểm đến mặt phẳng" (kc-diem-mp của ngân hàng de-thi) với D-KC-01, để gợi ý bài luyện giao được qua S3-E2. Mã cố định:
-- app/db/seed/fixture-ids.d/s5e5b.json. Bộ chưa cấp cho trường nào (không đổi danh sách Kho đề của spec khác); móc
-- /test-hooks/s5e5b/bank-access cấp cho trường của thế giới E2E. Chỉ dev/E2E.

CREATE TEMP TABLE t_s5kc (n int, code text, title text, statement text, answer text, hint1 text) ON COMMIT DROP;
INSERT INTO t_s5kc VALUES
  (701, 'KCA-01', 'KCA-01 · Khoảng cách từ A đến (SBC), SA = a',
   'Cho hình chóp S.ABCD có đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính khoảng cách từ A đến mặt phẳng (SBC).',
   'a√2/2', 'Kẻ AH ⊥ SB; chứng minh AH ⊥ (SBC).'),
  (702, 'KCA-02', 'KCA-02 · Khoảng cách từ A đến (A′BC) của lăng trụ đứng',
   'Cho lăng trụ đứng ABC.A′B′C′ có đáy là tam giác đều cạnh a, AA′ = a. Tính khoảng cách từ A đến mặt phẳng (A′BC).',
   'a√21/7', 'Gọi M là trung điểm BC; kẻ AH ⊥ A′M.'),
  (703, 'KCA-03', 'KCA-03 · Khoảng cách từ tâm đáy đến mặt bên của chóp đều',
   'Cho hình chóp tứ giác đều S.ABCD có cạnh đáy a, chiều cao SO = a/2. Tính khoảng cách từ O đến mặt phẳng (SCD).',
   'a√2/4', 'Gọi M là trung điểm CD; kẻ OH ⊥ SM.');

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, kind, created_at)
SELECT format('00000000-0000-7000-8005-%s', lpad((700000000000 + t.n)::text, 12, '0'))::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', 'thpt', NULL, 1, t.title, t.code, 's5_kc', 'plane',
       :'t0'::timestamptz - interval '20 days'
FROM t_s5kc t;

INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT format('00000000-0000-7000-8005-%s', lpad((700000000100 + t.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8005-%s', lpad((700000000000 + t.n)::text, 12, '0'))::uuid, 1, t.statement,
       digest(regexp_replace(btrim(t.statement), '\s+', ' ', 'g'), 'sha256'), 'teacher_approved', false,
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '20 days'
FROM t_s5kc t;

UPDATE problem p SET current_version_id = format('00000000-0000-7000-8005-%s', lpad((700000000100 + t.n)::text, 12, '0'))::uuid
FROM t_s5kc t
WHERE p.org_id = platform_org_id() AND p.id = format('00000000-0000-7000-8005-%s', lpad((700000000000 + t.n)::text, 12, '0'))::uuid;

INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT platform_org_id(), format('00000000-0000-7000-8005-%s', lpad((700000000100 + t.n)::text, 12, '0'))::uuid, 1, t.hint1
FROM t_s5kc t;

INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
SELECT platform_org_id(), format('00000000-0000-7000-8005-%s', lpad((700000000100 + t.n)::text, 12, '0'))::uuid, t.answer,
       jsonb_build_array(jsonb_build_object('title', 'Hướng làm', 'body', t.hint1, 'why', 'Khoảng cách từ điểm đến mặt phẳng là độ dài đường vuông góc.'),
                         jsonb_build_object('title', 'Kết quả', 'body', 'Đáp số: ' || t.answer, 'why', 'Hệ thức lượng trong tam giác vuông.')),
       'gia-lap-1', 'p1', '00000000-0000-7000-8000-0000000000aa', false
FROM t_s5kc t;
