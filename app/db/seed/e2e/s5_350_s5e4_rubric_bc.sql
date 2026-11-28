-- T-261128-57 (S5-E4): F-S5-051 rubric RB-01 v1 và F-S5-052 bài có chấm BC-01 (fixture-ids.d/s5.json). Chỉ dev/E2E.
-- Phiên bản 1 của RB-01: id …-8005-300000000051 (quy tắc như phiên bản đề F-S5-050: tiền tố 3 cho rubric_version).
-- Trường F-S2-100 (…-8002-000000000100); cô Lan F-S5-001 = F-S3-001; lớp 11A1 F-S5-010 = F-S3-010.
-- Cấp bộ đề 's5_core' (đề D-KC-01, F-S5-050) cho trường của cô Lan để đề hiện trong Kho (ghi chú của s5_000_core.sql).

INSERT INTO catalog_bank_access (org_id, bank) VALUES ('00000000-0000-7000-8002-000000000100', 's5_core')
ON CONFLICT DO NOTHING;

INSERT INTO rubric (id, org_id, teacher_id, code, current_version, created_at, updated_at)
VALUES ('00000000-0000-7000-8005-000000000051', '00000000-0000-7000-8002-000000000100',
        '00000000-0000-7000-8003-000000000001', 'RB-01', 1,
        :'t0'::timestamptz - interval '3 days', :'t0'::timestamptz - interval '3 days');

INSERT INTO rubric_version (id, org_id, rubric_id, version, name, template, scale, guide, created_by, created_at)
VALUES ('00000000-0000-7000-8005-300000000051', '00000000-0000-7000-8002-000000000100',
        '00000000-0000-7000-8005-000000000051', 1, 'Tính toán – khoảng cách', 'tinh-toan', 10,
        jsonb_build_array(
          'Sai hướng → 0 điểm các bước phụ thuộc hướng đó.',
          'Đúng kết quả nhưng lập luận sai/thiếu → điểm kết quả tối đa 50 %.',
          'Bước sai → bước sau phụ thuộc = 0; bước độc lập vẫn tính (lệch cho phép ±0,25).',
          'Bài hình không gian không có hình / hình sai → mất phần hình + các bước dựa trên hình sai.',
          'Dùng định lí / công thức ngoài kho kiến thức → bước đó 0 điểm, gắn cờ "bịa kiến thức".'),
        '00000000-0000-7000-8003-000000000001', :'t0'::timestamptz - interval '3 days');

INSERT INTO rubric_criterion (id, org_id, rubric_version_id, ord, name, description, max_points, direction_dependent)
SELECT ('00000000-0000-7000-8005-3100000000' || lpad(ord::text, 2, '0'))::uuid, '00000000-0000-7000-8002-000000000100',
       '00000000-0000-7000-8005-300000000051', ord, name, descr, pts, dir
FROM (VALUES (1, 'Hướng', 'Chọn đúng hướng giải (dựng hình, công thức cần dùng).', 2.0, false),
             (2, 'Các bước', 'Các bước biến đổi, tính toán đúng theo hướng đã chọn.', 4.0, true),
             (3, 'Kết quả', 'Đáp số đúng, đơn vị và dạng gọn.', 2.0, false),
             (4, 'Hình', 'Hình vẽ đúng, đủ yếu tố cần dựng.', 1.0, false),
             (5, 'Trình bày', 'Trình bày rõ, lập luận có căn cứ.', 1.0, false)) v(ord, name, descr, pts, dir);

-- F-S5-052 BC-01: hạn T6 11/12/2026 20:00 giờ VN, cho nộp muộn có nhãn, công bố khi giáo viên bấm Trả bài, Chấm tay.
INSERT INTO graded_assignment (id, org_id, teacher_id, class_id, code, title, problem_version_id, rubric_version_id, scale,
                               due_at, allow_late, grading_mode, release_mode, created_at, updated_at)
VALUES ('00000000-0000-7000-8005-000000000052', '00000000-0000-7000-8002-000000000100',
        '00000000-0000-7000-8003-000000000001', '00000000-0000-7000-8003-000000000010', 'BC-01',
        'Kiểm tra 15 phút – Khoảng cách', '00000000-0000-7000-8005-200000000050',
        '00000000-0000-7000-8005-300000000051', 10, '2026-12-11T20:00:00+07:00', true, 'manual', 'on_return',
        :'t0'::timestamptz - interval '2 days', :'t0'::timestamptz - interval '2 days');
