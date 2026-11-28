package vn.geo.server.aigrade.internal.refusal.repo;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** "Thử chấm lại" (US-S5E6-02-FC1): gỡ lượt "AI không chấm" để việc nền chấm lại. */
@Repository
public class RefusalRepository {

  private final JdbcClient jdbc;

  public RefusalRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Xoá MỘT đề xuất "AI không chấm" (không có điểm nào; tín hiệu engine đi theo). Đề xuất có điểm
   * vẫn bất biến (BR-S5E6-04). Trả số hàng đã xoá.
   */
  public int deleteNoGrade(UUID orgId, UUID proposalId) {
    return jdbc.sql(
            """
            DELETE FROM ai_grade_proposal
            WHERE org_id = :orgId AND id = :id AND status = 'no_grade'
            """)
        .param("orgId", orgId)
        .param("id", proposalId)
        .update();
  }
}
