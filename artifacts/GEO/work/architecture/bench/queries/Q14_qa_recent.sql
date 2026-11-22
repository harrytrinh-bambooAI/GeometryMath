-- role: app
-- Hỏi gì: các câu hỏi gần nhất của cả lớp trong một bài (giáo viên), keyset theo id, 50 mỗi trang.
:explain
SELECT q.id, q.user_id, q.asked_at, q.question, q.reply_kind, q.source_lecture_ids
FROM geo.qa_turn q
JOIN geo.assignment a ON a.org_id = q.org_id AND a.id = q.assignment_id
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
WHERE q.org_id = :'org' AND q.assignment_id = :'assignment' AND q.id < 9223372036854775807
ORDER BY q.id DESC
LIMIT 51;
