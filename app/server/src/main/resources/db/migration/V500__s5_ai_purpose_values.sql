-- T-261128-51 (PLAN_code-s5 P4, §4 khoảng V500–V504 của khung): 8 task AI mới của chặng 5 có giá trị ai_purpose riêng
-- trong nhật ký lượt gọi AI (ai_call_log), thêm MỘT lần ở đây để các task song song không cùng sửa enum (mẫu V421).
--   lesson_plan, lesson_patch   S5-E1 soạn giáo án, chat vá bản nháp
--   class_notice                US-S5E5-01 thông báo do trợ lý soạn
--   kb_qa                       S5-E3 hỏi đáp theo kho tri thức
--   grade_ocr, grade            S5-E4 đọc chữ bài viết tay, S5-E6 đề xuất điểm theo rubric
--   exam_draft                  US-S5E2-06 trợ lý soạn bộ đề
--   class_errors                US-S5E5-02/03 tổng hợp lỗi sai của lớp
-- ADD VALUE IF NOT EXISTS: chạy lại an toàn; giá trị mới chỉ dùng được ở giao dịch SAU migration này.
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'lesson_plan';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'lesson_patch';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'class_notice';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'kb_qa';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'grade_ocr';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'grade';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'exam_draft';
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'class_errors';
