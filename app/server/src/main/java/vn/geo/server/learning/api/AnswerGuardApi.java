package vn.geo.server.learning.api;

import java.util.UUID;

/**
 * Kiểm soát đáp án TỐI THIỂU của E4 cho câu trả lời hỏi đáp (US-GEO-405-FC4, 410-SC5; BR-GEO-02,
 * D-GEO-159): khi lời giải của bài còn khóa với học sinh, câu trả lời chứa đáp số (mọi dạng viết E4
 * nhận ra) hoặc nội dung bước giải bị thay bằng MSG-GEO-35 kèm gợi ý cao nhất em đã xem. Bộ kiểm
 * đầy đủ (ADR-GEO-14, bộ câu thử) thuộc E6 (US-GEO-609) và sẽ thay cài đặt này.
 */
public interface AnswerGuardApi {

  Screened screen(UUID assignmentId, String reply);

  /** Phiên bản bộ kiểm ghi vào qa_turn.guard_version. */
  String version();

  /**
   * @param blocked câu gốc bị chặn
   * @param text câu được phép gửi và lưu (câu gốc hoặc câu thay thế MSG-GEO-35)
   */
  record Screened(boolean blocked, String text) {}
}
