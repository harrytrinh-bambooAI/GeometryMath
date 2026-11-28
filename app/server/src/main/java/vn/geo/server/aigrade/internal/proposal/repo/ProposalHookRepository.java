package vn.geo.server.aigrade.internal.proposal.repo;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SQL của móc kiểm thử S5-E6 (chỉ profile dev/test): cấp bộ đề 's5_core' (F-S5-050 D-KC-01) cho
 * trường của thế giới E2E và đặt chế độ chấm "AI đề xuất" cho bài có chấm (cổng lớp US-S5E6-06 là
 * stub luôn đóng tới T-261128-65).
 */
@Repository
@Profile({"dev", "test"})
public class ProposalHookRepository {

  private final JdbcClient jdbc;

  public ProposalHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void grantBank(UUID orgId, String bank) {
    jdbc.sql(
            """
            INSERT INTO catalog_bank_access (org_id, bank) VALUES (:orgId, :bank) ON CONFLICT DO NOTHING
            -- sqllint:allow=ownership móc dev/test: cấp bộ đề mẫu S5 (content) cho trường của thế giới E2E
            """)
        .param("orgId", orgId)
        .param("bank", bank)
        .update();
  }

  public int aiProposalMode(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            UPDATE graded_assignment SET grading_mode = 'ai_proposal', updated_at = now()
            WHERE org_id = :orgId AND id = :a
            -- sqllint:allow=ownership móc dev/test: chế độ "AI đề xuất" (assessment) khi cổng lớp còn là stub
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .update();
  }

  /** Số hàng đề xuất / tiêu chí / tín hiệu của một bài nộp (soát xoá dữ liệu). */
  public Map<String, Object> counts(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT (SELECT count(*) FROM ai_grade_proposal p WHERE p.org_id = :orgId AND p.submission_id = :sid) AS proposals,
                   (SELECT count(*) FROM engine_signal e WHERE e.org_id = :orgId AND e.submission_id = :sid) AS signals
            """)
        .param("orgId", orgId)
        .param("sid", submissionId)
        .query()
        .singleRow();
  }
}
