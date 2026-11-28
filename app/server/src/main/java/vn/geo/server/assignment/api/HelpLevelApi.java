package vn.geo.server.assignment.api;

import java.util.List;
import java.util.UUID;

/**
 * Mức trợ giúp của AI theo bài (US-S5E3-04, BR-S5E3-12; QS5-12): Tắt · Chỉ gợi ý ({@code hint},
 * {@code explain_step}) · Gợi ý và khái niệm (thêm {@code concept}). Mặc định: bài có chấm Tắt, bài
 * luyện Gợi ý và khái niệm. Hỏi đáp theo kho (module knowledge) đọc ở đây trước khi gọi AI.
 */
public interface HelpLevelApi {

  String OFF = "off";
  String HINTS = "hints";
  String HINTS_CONCEPTS = "hints_concepts";

  /**
   * Mức hiệu lực của một bài.
   *
   * @param privateNote ghi chú riêng của giáo viên: chỉ để bộ kiểm chặn lộ, KHÔNG gửi AI / học sinh
   * @param knowledgeCodes mã kiến thức của đề (đề chưa có cột mã: giáo viên khai)
   */
  record Policy(
      String helpLevel, boolean isDefault, String privateNote, List<String> knowledgeCodes) {}

  /**
   * @param targetKind "practice" (bài luyện) | "graded" (bài có chấm)
   */
  Policy policy(UUID orgId, UUID targetId, String targetKind);

  /** Mặc định theo loại bài (QS5-12). */
  static String defaultLevel(String targetKind) {
    return "graded".equals(targetKind) ? OFF : HINTS_CONCEPTS;
  }

  /** Nhãn hiển thị. */
  static String label(String level) {
    return switch (level == null ? "" : level) {
      case OFF -> "Tắt";
      case HINTS -> "Chỉ gợi ý";
      case HINTS_CONCEPTS -> "Gợi ý và khái niệm";
      default -> level;
    };
  }

  /** Kiểu trả lời được phép ở mức này. */
  static boolean allows(String level, String kind) {
    return switch (level == null ? "" : level) {
      case HINTS -> "hint".equals(kind) || "explain_step".equals(kind);
      case HINTS_CONCEPTS ->
          "hint".equals(kind) || "explain_step".equals(kind) || "concept".equals(kind);
      default -> false;
    };
  }
}
