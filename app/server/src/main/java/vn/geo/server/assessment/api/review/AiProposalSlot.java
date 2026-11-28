package vn.geo.server.assessment.api.review;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Điểm cắm đề xuất AI vào màn chấm và hàng chờ của assessment (US-S5E6-04, 05; T-261128-62). Module
 * aigrade cài một bean (sau cờ {@code geo.s5.ai-grading.enabled}); không có bean thì màn chấm tay
 * chạy như US-S5E4-06. Hướng phụ thuộc: aigrade → assessment.api (không vòng).
 */
public interface AiProposalSlot {

  /** Hành động của giáo viên trên một tiêu chí có đề xuất (BR-S5E6-10). */
  enum Action {
    ACCEPT,
    EDIT,
    DROP
  }

  /**
   * Trường thêm của một bài nộp cho giáo viên chủ lớp (màn chấm, hàng chờ). Chế độ chạy song song
   * trước khi duyệt chỉ có {@code aiDone} (BR-S5E6-13). Trả map rỗng khi bài không dùng AI.
   */
  Map<String, Object> extras(UUID orgId, UUID submissionId);

  /**
   * "Nhận cả bài": điểm đề xuất theo thứ tự tiêu chí. CONFLICT khi bài có cờ, chưa có đề xuất hay
   * đang chạy song song (BR-S5E6-11).
   */
  Map<Integer, BigDecimal> acceptAll(UUID orgId, UUID submissionId);

  /**
   * Sau khi Duyệt / sửa điểm thành công (lần {@code seq}): ghi hành động từng tiêu chí, cặp so sánh
   * lần duyệt đầu (BR-S5E6-12, 14). Trả trường thêm cho phản hồi.
   */
  Map<String, Object> approved(
      UUID orgId,
      UUID teacherId,
      UUID submissionId,
      int seq,
      Map<Integer, BigDecimal> points,
      Map<Integer, Action> actions);
}
