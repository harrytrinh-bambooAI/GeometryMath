-- PO-83 (PO-DECISIONS 27/11/2026): task "class_stuck" (bộ gộp "Cả lớp vướng ở đâu", S3-E3) có giá trị ai_purpose riêng
-- trong nhật ký lượt gọi AI (ai_call_log) thay cho gộp vào 'qa'; đo chi phí AI của giáo viên tách khỏi hỏi đáp học sinh.
ALTER TYPE ai_purpose ADD VALUE IF NOT EXISTS 'class_stuck';
