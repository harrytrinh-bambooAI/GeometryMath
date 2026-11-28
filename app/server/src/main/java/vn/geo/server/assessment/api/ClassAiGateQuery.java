package vn.geo.server.assessment.api;

import java.util.UUID;

/**
 * Cổng "lớp đã đạt ngưỡng chạy song song" (US-S5E6-06) cho chế độ chấm "AI đề xuất" của US-S5E4-02
 * (BR-S5E4-05, QS5-16). Bản cài đặt hiện tại là stub LUÔN ĐÓNG (T-261128-57); T-261128-65 thay bằng
 * bản thật (đánh dấu bean của mình {@code @Primary}).
 */
public interface ClassAiGateQuery {

  /** true khi lớp đã đạt cổng để chọn chế độ "AI đề xuất". */
  boolean aiProposalReady(UUID orgId, UUID classId);
}
