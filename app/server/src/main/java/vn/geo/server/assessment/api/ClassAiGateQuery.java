package vn.geo.server.assessment.api;

import java.util.UUID;

/**
 * Cổng "lớp đã đạt ngưỡng chạy song song" (US-S5E6-06) cho chế độ chấm "AI đề xuất" của US-S5E4-02
 * (BR-S5E4-05, QS5-16). Bản cài đặt: {@code aigrade.internal.kpi.service.ClassAiGate} (T-261128-65;
 * thay stub luôn đóng của T-261128-57).
 */
public interface ClassAiGateQuery {

  /** true khi lớp đã đạt cổng để chọn chế độ "AI đề xuất" (tính lại KPI, có thể mở / đóng cổng). */
  boolean aiProposalReady(UUID orgId, UUID classId);

  /** Chỉ đọc trạng thái cổng đã lưu, không tính lại (dùng ở đường đọc nhiều lần). */
  default boolean aiProposalOpen(UUID orgId, UUID classId) {
    return aiProposalReady(orgId, classId);
  }
}
