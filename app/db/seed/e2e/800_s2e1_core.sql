-- S2-E1 (T-261126-18): dữ liệu mẫu chặng 2 FX-01…FX-03 (stories_ac.md S2-E1 "Dữ liệu mẫu chặng 2"), mã cố định ở
-- fixture-ids.d/s2e1.json (F-S2-nnn). Mật khẩu băm PBKDF2-SHA256 60000 vòng; mã khôi phục băm HMAC với pepper dev
-- (geo.auth.code-pepper mặc định). Dữ liệu chỉ cho dev/E2E, không bao giờ chạy ở môi trường thật.
--   FX-01 Cô Trang  GV-7K2M-9Q4D  Trang@2026x   RC-7K2M-9Q4D-3W8X  (chủ lớp 11A1 của trường Cộng đồng)
--   FX-02 An        HS-3F8A-2L6P  AnHoc#2026    RC-4F7H-2K9M-6Q3V  (Tự học, chưa vào lớp)
--   FX-03 Bình      HS-5B7C-8R2T  BinhHoc#2026  RC-5B7C-8R2T-4N6P  (Chờ duyệt vào 11A1 của Cô Trang)
-- Ca kiểm làm BẨN tài khoản (khóa, khôi phục, đổi mật khẩu) tự đăng ký tài khoản riêng, không dùng ba tài khoản này.

-- Văn bản thông báo riêng tư 1.0 (V110 nạp; reset seed xóa mọi bảng không có trong keep-tables.txt nên nạp lại ở đây)
INSERT INTO privacy_notice (version, title, body, is_current) VALUES ('1.0', 'Thông báo riêng tư',
'Ứng dụng chỉ hỏi tên hiển thị, vai (giáo viên hoặc học sinh) và mật khẩu do bạn tự đặt. Chúng tôi không hỏi email, số điện thoại, ngày sinh, trường hay ảnh, và không xác minh phụ huynh.
Mã người dùng dùng để đăng nhập; mã khôi phục dùng khi quên mật khẩu và chỉ hiện một lần. Mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản.
Bài làm, tiến độ và câu hỏi gửi trợ lý được lưu để thầy cô của lớp theo dõi việc học. Bạn có thể xóa tài khoản cùng dữ liệu bài làm.',
true) ON CONFLICT (version) DO NOTHING;

-- "Cộng đồng" (F-S2-100, kind = group như fn_register_code_user tạo): nơi chứa tài khoản tự đăng ký
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8000-00000000f001', 'group', 'Cộng đồng')
ON CONFLICT (id) DO NOTHING;

INSERT INTO app_user (id, org_id, role, account_kind, display_name, user_code, password_hash, recovery_hmac,
                      privacy_ack_version, privacy_ack_at) VALUES
  ('00000000-0000-7000-8002-000000000001', '00000000-0000-7000-8002-000000000100', 'teacher', 'verified', 'Cô Trang',
   'GV-7K2M-9Q4D', 'pbkdf2-sha256$60000$3il9Q48j8nB/0rLqSilLXQ$MxLGH4nGQw/VRriCR48ak9ULPqlNTIxfoUnMzry7hhs',
   '\xcf30bf3ca061d8a098650d1663be02b61c5972a749afbbfb76e2997407ec9a2b', '1.0', :'t0'),            -- FX-01
  ('00000000-0000-7000-8002-000000000002', '00000000-0000-7000-8002-000000000100', 'student', 'light', 'An',
   'HS-3F8A-2L6P', 'pbkdf2-sha256$60000$3MO1JEqxwQ9AhafV/MR7Tw$gLm7nwnNlifIrntyi0y7YroYGIjiQPzoVlWYw2pl/iE',
   '\x6407c7f5340666bd1780a9c1b7a41d95b762286f5357b13e9499aaa94f51053d', '1.0', :'t0'),            -- FX-02
  ('00000000-0000-7000-8002-000000000003', '00000000-0000-7000-8002-000000000100', 'student', 'light', 'Bình',
   'HS-5B7C-8R2T', 'pbkdf2-sha256$60000$kQRoM+ZqSlo0PhYC1rEhDg$5smXdQ/q3oV7jFwhP6UQQwcPQtmrwDD9veCmkpvKR/Y',
   '\x2ca1f42ed4b8d421031b95a3abf878ee49d73c2684d157993ee2ed65963c1df9', '1.0', :'t0');            -- FX-03

-- Lớp 11A1 của Cô Trang (F-S2-011) và Bình Chờ duyệt (class_member.status = pending, A6)
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id) VALUES
  ('00000000-0000-7000-8002-000000000011', '00000000-0000-7000-8002-000000000100', '11A1', 'thpt', 11, 'CTLPAA',
   '00000000-0000-7000-8002-000000000001');
INSERT INTO class_member (org_id, class_id, user_id, name_key, status) VALUES
  ('00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8002-000000000011', '00000000-0000-7000-8002-000000000003',
   'binh', 'pending');
