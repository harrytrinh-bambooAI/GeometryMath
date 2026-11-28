-- T-261128-58 (S5-E3 hỏi đáp theo kho, mức trợ giúp, kiểm soát đáp án). Chỉ dev/E2E. Mã cố định: fixture-ids.d/s5e3b.json.
-- 1) Bài giảng mẫu "Mặt phẳng trung trực" (đã duyệt) cho US-S5E3-03-SC4: KHÔNG nạp sẵn (kho bài giảng hệ thống có đúng 10 bài,
--    HelpE4IT đếm toàn bảng lecture; US-S5E3-03-FC1 cần "không có bài giảng mẫu khớp"). Móc /test-hooks/s5e3/qa/lecture gọi
--    hàm dưới đây để thêm (approved = true) hoặc xoá (false) bài này trong trường cộng đồng F-S2-100 (bài giảng mẫu của
--    trường; hỏi đáp đọc org nền tảng + org của trường). Hàm chỉ có trong CSDL dev/e2e (geo_app không ghi được lecture).
-- 2) Mức trợ giúp của BC-01 (F-S5-052, seed s5_350): Gợi ý và khái niệm, mã kiến thức của đề D-KC-01 KT11-C07-CT-003,
--    ghi chú riêng "Em Mai hay nhầm SB" (BR-S5E3-17) — dữ liệu mẫu cho màn dev.

CREATE OR REPLACE FUNCTION fn_e2e_s5e3_lecture_approval(p_slug text, p_on boolean) RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE n integer;
BEGIN
  IF p_slug NOT IN ('mat-phang-trung-truc') THEN
    RAISE EXCEPTION 'fn_e2e_s5e3_lecture_approval: chỉ bài giảng mẫu e2e';
  END IF;
  IF p_on THEN
    INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed)
    VALUES ('00000000-0000-7000-8005-400000000401', '00000000-0000-7000-8002-000000000100', 1, p_slug, 'Mặt phẳng trung trực',
            'Mặt phẳng trung trực của đoạn thẳng AB là mặt phẳng đi qua trung điểm của AB và vuông góc với AB. '
            || 'Mọi điểm thuộc mặt phẳng trung trực đều cách đều hai đầu mút A và B.',
            'teacher_authored', 'teacher_approved', 'Bài giảng mẫu GEO · Hình học không gian 11', true)
    ON CONFLICT DO NOTHING;
    RETURN 1;
  END IF;
  DELETE FROM lecture WHERE org_id = '00000000-0000-7000-8002-000000000100' AND slug = p_slug;
  GET DIAGNOSTICS n = ROW_COUNT;
  RETURN 1;
END $$;
REVOKE ALL ON FUNCTION fn_e2e_s5e3_lecture_approval(text, boolean) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_e2e_s5e3_lecture_approval(text, boolean) TO geo_app;

INSERT INTO assignment_help_level (org_id, target_id, target_kind, class_id, help_level, private_note, knowledge_codes, updated_by)
VALUES ('00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8005-000000000052', 'graded',
        '00000000-0000-7000-8003-000000000010', 'hints_concepts', 'Em Mai hay nhầm SB', ARRAY['KT11-C07-CT-003'],
        '00000000-0000-7000-8003-000000000001')
ON CONFLICT DO NOTHING;
