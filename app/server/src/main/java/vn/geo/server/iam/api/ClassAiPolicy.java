package vn.geo.server.iam.api;

import java.util.UUID;

/**
 * Công tắc trợ lý AI phía học sinh theo lớp (US-S3E5-04). Module khác (aigateway) hỏi trước mỗi
 * lượt gọi trợ lý của học sinh; lớp sở hữu cột {@code class.ai_enabled}.
 */
public interface ClassAiPolicy {

  /**
   * true nếu bài {@code assignmentId} thuộc một lớp đã tắt trợ lý AI và học sinh {@code userId} là
   * thành viên lớp đó (bài tự học, bài lớp đang bật: false; BR-S3E5-44).
   */
  boolean blockedForStudent(UUID orgId, UUID userId, UUID assignmentId);
}
