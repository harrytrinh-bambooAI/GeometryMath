-- role: owner
-- Thân của geo.fn_qa_history (US-GEO-410, D-GEO-159, RV-19): lịch sử hỏi đáp của CHÍNH học sinh trên mọi bài, mới nhất trước,
-- keyset theo id, trang 20 (LIMIT 21), trần 100. Gồm câu trả lời đã lưu (đã qua Kiểm soát đáp án), mở lại không gọi AI.
-- Vai chủ vì geo_app không có SELECT cột reply_text (chỉ chủ câu hỏi đọc, qua hàm).
:explain
SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text, q.source_lecture_ids
FROM geo.qa_turn q
WHERE q.org_id = :'qa_org' AND q.user_id = :'qa_student' AND q.id < 9223372036854775807
ORDER BY q.id DESC
LIMIT 21;
