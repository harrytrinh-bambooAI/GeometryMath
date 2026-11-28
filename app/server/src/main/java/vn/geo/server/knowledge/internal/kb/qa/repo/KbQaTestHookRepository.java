package vn.geo.server.knowledge.internal.kb.qa.repo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Móc kiểm thử hỏi đáp theo kho (chỉ dev/test, T-261128-58): đọc nhật ký, trả bài, bài giảng mẫu.
 */
@Repository
@Profile({"dev", "test"})
public class KbQaTestHookRepository {

  private final JdbcClient jdbc;

  public KbQaTestHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> interactions(UUID orgId, UUID studentId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT i.id, i.student_id, i.target_id, i.question, i.reply_kind, i.reply,
                   i.source::text AS source, i.created_at
            FROM ai_interaction i
            WHERE i.org_id = :orgId
              AND (CAST(:student AS uuid) IS NULL OR i.student_id = :student)
              AND (CAST(:target AS uuid) IS NULL OR i.target_id = :target)
            ORDER BY i.created_at, i.id
            LIMIT 500
            """)
        .param("orgId", orgId)
        .param("student", studentId)
        .param("target", targetId)
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> decisions(UUID orgId, UUID studentId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT d.id, d.interaction_id, d.student_id, d.target_id, d.decision, d.rule_code,
                   d.check_ms, d.firewall_version, d.created_at
            FROM ai_policy_decision d
            WHERE d.org_id = :orgId
              AND (CAST(:student AS uuid) IS NULL OR d.student_id = :student)
              AND (CAST(:target AS uuid) IS NULL OR d.target_id = :target)
            ORDER BY d.created_at, d.id
            LIMIT 500
            """)
        .param("orgId", orgId)
        .param("student", studentId)
        .param("target", targetId)
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> counters(UUID orgId) {
    return jdbc.sql(
            """
            SELECT c.decision, c.rule_code, sum(c.n) AS n
            FROM ai_policy_counter c
            WHERE c.org_id = :orgId
            GROUP BY c.decision, c.rule_code
            ORDER BY c.decision, c.rule_code
            LIMIT 200
            """)
        .param("orgId", orgId)
        .query()
        .listOfRows();
  }

  /**
   * Bật/tắt nhãn "Giáo viên đã duyệt" của bài giảng mẫu e2e (hàm chỉ có trong CSDL e2e, seed
   * s5_400).
   */
  public int lectureApproval(String slug, boolean approved) {
    Integer n =
        jdbc.sql("SELECT fn_e2e_s5e3_lecture_approval(:slug, :on)")
            .param("slug", slug)
            .param("on", approved)
            .query(Integer.class)
            .single();
    return n == null ? 0 : n;
  }
}
