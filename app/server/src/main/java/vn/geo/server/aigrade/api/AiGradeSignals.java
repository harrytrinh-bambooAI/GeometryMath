package vn.geo.server.aigrade.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cổng đọc đề xuất AI và tín hiệu engine của module aigrade (US-S5E6-01, 03) cho module khác (hàng
 * chờ chấm US-S5E4-05 của T-261128-60, màn chấm có đề xuất US-S5E6-04 của T-261128-62). Chỉ đọc;
 * không trả nội dung cho học sinh hay phụ huynh (STAGES §7).
 */
public interface AiGradeSignals {

  /** Cờ "AI và engine lệch" (BR-S5E6-09). */
  String FLAG_DISAGREE = "AI và engine lệch";

  /** Cờ "bịa kiến thức" (BR-S5E6-03 luật §2.5). */
  String FLAG_INVENTED = "bịa kiến thức";

  /** Cờ "AI không chấm" (BR-S5E6-02, 05). */
  String FLAG_NO_GRADE = "AI không chấm";

  /**
   * Tóm tắt đề xuất mới nhất của phiên bản chấm một bài nộp.
   *
   * @param status "proposed" | "no_grade"
   * @param total tổng đề xuất (null khi "AI không chấm")
   * @param engineStatus "match" | "mismatch" | "not_found" | "no_engine"
   * @param flags cờ của bài (để xếp đầu hàng chờ)
   */
  record Summary(
      UUID submissionId,
      String status,
      String reason,
      BigDecimal total,
      String engineStatus,
      List<String> flags) {

    /** Bài có cờ: xếp đầu hàng chờ (STAGES §5 B2). */
    public boolean flagged() {
      return !flags.isEmpty();
    }
  }

  /** Tóm tắt theo mã bài nộp cho mọi bài nộp đã có đề xuất của một bài có chấm. */
  Map<UUID, Summary> forAssignment(UUID orgId, UUID assignmentId);
}
