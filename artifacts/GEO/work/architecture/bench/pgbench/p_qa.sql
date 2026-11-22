-- Hỏi đáp: tìm nguồn (Q12) + geo.fn_record_qa (câu hỏi + câu trả lời đã qua Kiểm soát đáp án + sự kiện ask + tiến độ). Gọi AI và
-- Kiểm soát đáp án không nằm trong CSDL. Vai geo_bench = geo_app + geo_ai (cổng AI dùng geo_ai). Trọng số: 12%
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
SELECT l.id, l.title, l.source_kind, l.approval_status, l.source_ref, left(l.body, 600) FROM geo.lecture l, websearch_to_tsquery('simple', geo.f_unaccent(lower('thiet dien chop'))) q(tsq)
WHERE l.org_id = '00000000-0000-7000-8000-000000000001' AND l.search_tsv @@ q.tsq ORDER BY ts_rank(l.search_tsv, q.tsq) DESC, l.id LIMIT 5;
SELECT status, qa_id FROM geo.fn_record_qa(:org::uuid, :stu::uuid, :asg::uuid, gen_random_uuid(), 'Thiết diện là gì?', 'concept',
       'Thiết diện là phần chung của mặt phẳng cắt và khối; xem bài giảng Thiết diện của hình chóp.', 'g1');
