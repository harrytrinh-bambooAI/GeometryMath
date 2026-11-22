-- E3 · dữ liệu riêng cho ca kiểm của epic Thao tác hình 3D (chạy sau 100_e1_core.sql; không sửa dữ liệu của epic khác).
-- Vì sao có lớp riêng: TC-GEO-0334 và TC-GEO-0352 bấm "Mở ngay" (đổi trạng thái khóa). Nếu bấm trên BG-1 thật thì các ca
-- khác đang chạy song song (E1, E3 đọc BG-1 Sắp mở) bị đổi trạng thái. Mỗi ca × mỗi dự án Playwright (chromium, webkit)
-- dùng một lớp riêng có bài HKG-01 Sắp mở (giống BG-1) và một học sinh tên "An". Giáo viên riêng (tổ chức riêng bên dưới)
-- để danh sách lớp của Giáo viên mẫu (F-GEO-003) vẫn đúng 11A1, 9B như fixture.
-- UUID: 00000000-0000-7000-8000-000000e3xxxx.
-- Tổ chức riêng kiểu 'group' (không phải 'school'): giữ nguyên số trường/lớp/học sinh của THPT Mẫu A mà TC-GEO-0159 (E1) đếm.

INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8000-000000e30001', '00000000-0000-7000-8000-00000000f001', 'group', 'Nhóm thử E3');

INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8000-000000e30003', '00000000-0000-7000-8000-000000e30001', 'teacher', 'sample', 'Giáo viên E3');

INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT format('00000000-0000-7000-8000-000000e3001%s', k)::uuid, '00000000-0000-7000-8000-000000e30001',
       format('11E3-%s', k), 'thpt', 11, format('E3AN%sX', k + 1), '00000000-0000-7000-8000-000000e30003'
FROM generate_series(1, 4) AS k;

INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT format('00000000-0000-7000-8000-000000e3002%s', k)::uuid, '00000000-0000-7000-8000-000000e30001', 'student', 'light', 'An'
FROM generate_series(1, 4) AS k;

INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT '00000000-0000-7000-8000-000000e30001', format('00000000-0000-7000-8000-000000e3001%s', k)::uuid,
       format('00000000-0000-7000-8000-000000e3002%s', k)::uuid, 'an'
FROM generate_series(1, 4) AS k;

-- Bài HKG-01 Sắp mở (giờ mở T0 + 60 phút, như BG-1)
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT format('00000000-0000-7000-8000-000000e3004%s', k)::uuid, '00000000-0000-7000-8000-000000e30001', 'class',
       format('00000000-0000-7000-8000-000000e3001%s', k)::uuid, platform_org_id(),
       '00000000-0000-7000-8000-000000000031', '00000000-0000-7000-8000-000000100031',
       '00000000-0000-7000-8000-000000e30003', :'t0'::timestamptz + interval '60 minutes', true, true,
       :'t0'::timestamptz - interval '1 day', 1
FROM generate_series(1, 4) AS k;
