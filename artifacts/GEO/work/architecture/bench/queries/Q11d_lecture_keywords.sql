-- role: app
-- Từ khóa liên quan hiển thị trong hộp bài giảng (PO-34): tối đa 10, đã sắp theo term_key.
:explain
SELECT k.term_key, k.term
FROM geo.keyword k
WHERE k.org_id = :'platform' AND k.lecture_id = :'lecture'
ORDER BY k.term_key
LIMIT 10;
