package vn.geo.server.aigateway.internal;

import java.util.UUID;

/** Ghi đúng một bản ghi cho mỗi lượt gọi AI, kể cả lượt lỗi (BR-GEO-107). */
public interface AiCallLog {

  /**
   * @param orgId trường của người gọi; nền tảng khi gọi ngoài phiên (bộ đề chuẩn)
   * @param userId người gọi hoặc null
   * @param purpose giá trị enum ai_purpose (qa, solution, scene, hint, lecture)
   * @param status ok | error | timeout
   */
  record Entry(
      UUID orgId,
      UUID userId,
      String purpose,
      String model,
      String promptVersion,
      int tokensIn,
      int tokensOut,
      long costMicro,
      long latencyMs,
      String status) {}

  void record(Entry e);

  /** Lượt gọi gần nhất của một người dùng, mới nhất trước (chỉ móc kiểm thử dev/test dùng). */
  default java.util.List<Entry> recent(UUID userId, int limit) {
    return java.util.List.of();
  }
}
