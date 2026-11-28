-- T-261128-68 · S5-FIX W7 (Dev-S5E5-10, TC-S5E5-23): bản đọc chữ "failed" vì việc nền KHÔNG được nhận lượt AI
-- (AiQuota.admitGrading: lớp hết 200 lượt hôm nay / AI chấm của lớp tắt) ghi lý do để màn chấm và hàng chờ hiện lý do
-- thay cho "Chưa đọc được". NULL = AI lỗi thật (hoặc bản đọc được).
ALTER TABLE ocr_artifact ADD COLUMN reason text CHECK (reason IS NULL OR length(reason) <= 200);
COMMENT ON COLUMN ocr_artifact.reason IS 'Lý do không nhận lượt AI của việc đọc chữ nền (AiQuota.Admission.reason); NULL khi AI lỗi hoặc đọc được.';
