-- T-261128-51 · dữ liệu mẫu chung chặng 5, phần dùng schema sẵn có (PLAN_code-s5 P7, P8; epics/S5-E1/stories_ac.md
-- "Dữ liệu mẫu chung của chặng 5", 80e9b6f 28/11/2026). Mã cố định: app/db/seed/fixture-ids.d/s5.json.
-- Chỉ cho dev/E2E, không bao giờ chạy ở môi trường thật. Không migration mới, không bảng mới.
-- Tên 's5_*' chạy SAU mọi tệp tiền tố số (… 930_s3e3_core.sql) vì seed-reset.sh chạy db/seed/e2e/*.sql theo thứ tự glob
-- của bash (chữ số xếp trước chữ cái).
-- Quy ước (TẠM, ghi báo cáo T-51):
--   * F-S5-001 cô Lan, F-S5-002 thầy Khôi, F-S5-010 lớp 11A1, F-S5-011 lớp 11A2 là CÙNG hàng của chặng 3 (910_s3_core.sql:
--     F-S3-001, 002, 010, 011; mã GV-/HS-/HK- trùng, user_code và join_code duy nhất) => không chèn lại; s5.json giữ id S3.
--     Khối DO dưới đây chỉ KIỂM chúng có mặt đúng như đặc tả S5 (8 em Đang học + Văn Tú Đã rời ở 11A1; Quốc Bảo ở 11A2).
--   * F-S5-050 đề D-KC-01 (problem.code 'DKC-01' vì CHECK '^[A-Z]{2,5}-[0-9]{2,4}$' của V11; tiêu đề mở đầu 'D-KC-01') là đề kho nền tảng bộ 's5_core' (như 's3_tuan' của 910), loại 'plane' như 910 (bộ kiểm chứng kho
--     E5 chỉ xét 'space'); CHƯA cấp bộ cho trường nào (không đổi danh sách Kho đề của spec S2/S3). Task cần giáo viên thấy
--     đề trong Kho (T-261128-57 giao bài có chấm) tự thêm catalog_bank_access trong seed của mình.
--   * Phần cần bảng mới (giáo án, ma trận, kho tri thức, rubric, bài có chấm, bài nộp) do task chủ bảng nạp bằng
--     s5_<nnn>_<feature>_*.sql với id đã đặt trong s5.json.

DO $$
DECLARE n int;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM app_user WHERE id = '00000000-0000-7000-8003-000000000001' AND user_code = 'GV-7K2M-9QPA')
     OR NOT EXISTS (SELECT 1 FROM app_user WHERE id = '00000000-0000-7000-8003-000000000002' AND user_code = 'GV-4D8R-2WNX') THEN
    RAISE EXCEPTION 's5_000_core: thiếu F-S5-001/002 (F-S3-001/002 của 910_s3_core.sql)';
  END IF;
  SELECT count(*) INTO n FROM class_member
   WHERE class_id = '00000000-0000-7000-8003-000000000010' AND status = 'active';
  IF n <> 8 THEN RAISE EXCEPTION 's5_000_core: F-S5-010 (11A1) cần 8 em Đang học, có %', n; END IF;
  IF NOT EXISTS (SELECT 1 FROM class_member WHERE class_id = '00000000-0000-7000-8003-000000000010'
                   AND user_id = '00000000-0000-7000-8003-000000000028' AND status = 'left') THEN
    RAISE EXCEPTION 's5_000_core: F-S5-010 (11A1) thiếu Văn Tú Đã rời';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM class_member WHERE class_id = '00000000-0000-7000-8003-000000000011'
                   AND user_id = '00000000-0000-7000-8003-000000041001' AND status = 'active') THEN
    RAISE EXCEPTION 's5_000_core: F-S5-011 (11A2) thiếu Quốc Bảo Đang học';
  END IF;
END $$;

-- F-S5-050 đề D-KC-01 (phiên bản …-8005-200000000050), đáp số engine a√6/3 (≈ 0,8165 tại a = 1)
INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, bank, kind, created_at)
VALUES ('00000000-0000-7000-8005-000000000050', platform_org_id(), 1, 'catalog', '00000000-0000-7000-8000-0000000000aa', 'thpt',
        NULL, 1, 'D-KC-01 · Khoảng cách từ A đến (SBC)', 'DKC-01', 's5_core', 'plane', :'t0'::timestamptz - interval '20 days');
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
SELECT '00000000-0000-7000-8005-200000000050', platform_org_id(), '00000000-0000-7000-8005-000000000050', 1, s,
       digest(regexp_replace(btrim(s), '\s+', ' ', 'g'), 'sha256'), 'teacher_approved', false,
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '20 days'
FROM (VALUES ('Cho hình chóp S.ABCD đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính khoảng cách từ A đến mặt phẳng (SBC).')) v(s);
UPDATE problem SET current_version_id = '00000000-0000-7000-8005-200000000050'
WHERE org_id = platform_org_id() AND id = '00000000-0000-7000-8005-000000000050';
INSERT INTO hint (org_id, problem_version_id, level, body)
VALUES (platform_org_id(), '00000000-0000-7000-8005-200000000050', 1, 'Kẻ AH ⊥ SB; chứng minh AH ⊥ (SBC).');
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
VALUES (platform_org_id(), '00000000-0000-7000-8005-200000000050', 'a√6/3',
        jsonb_build_array(
          jsonb_build_object('title', 'Hướng làm', 'body', 'Kẻ AH ⊥ SB; vì BC ⊥ (SAB) nên AH ⊥ (SBC), d(A, (SBC)) = AH.',
                             'why', 'Khoảng cách từ điểm đến mặt phẳng là độ dài đường vuông góc.'),
          jsonb_build_object('title', 'Kết quả', 'body', '1/AH² = 1/SA² + 1/AB² = 1/(2a²) + 1/a² = 3/(2a²), AH = a√6/3. Đáp số: a√6/3.',
                             'why', 'Hệ thức lượng trong tam giác SAB vuông tại A.')),
        'gia-lap-1', 'p1', '00000000-0000-7000-8000-0000000000aa', false);
