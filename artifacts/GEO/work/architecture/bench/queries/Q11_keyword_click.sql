-- role: app
-- lint-allow: R-Q04 (tra khóa chính (org, term_key): 1 hàng)
-- Bấm từ khóa -> bài giảng (khớp chính xác theo term_key đã chuẩn hóa).
:explain
SELECT l.id, l.title, l.body, l.source_kind, l.approval_status, l.source_ref
FROM geo.keyword k
JOIN geo.lecture l ON l.org_id = k.org_id AND l.id = k.lecture_id
WHERE k.org_id = :'platform' AND k.term_key = :'term_key';
