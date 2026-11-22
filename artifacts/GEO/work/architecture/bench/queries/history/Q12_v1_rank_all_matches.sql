-- role: app
-- Hỏi đáp tìm nguồn (PO-33): từ câu hỏi lấy tối đa 5 bài giảng làm nguồn. Chỉ trả đoạn đầu thân bài (600 ký tự), không kéo cả thân.
:explain
SELECT l.id, l.title, l.source_kind, l.approval_status, l.source_ref, left(l.body, 600) AS excerpt
FROM geo.lecture l, websearch_to_tsquery('simple', geo.f_unaccent(lower(:'q'))) q(tsq)
WHERE l.org_id = :'platform' AND l.search_tsv @@ q.tsq
ORDER BY ts_rank(l.search_tsv, q.tsq) DESC, l.id
LIMIT 5;
