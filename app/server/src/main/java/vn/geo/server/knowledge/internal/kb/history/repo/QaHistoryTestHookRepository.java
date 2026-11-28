package vn.geo.server.knowledge.internal.kb.history.repo;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Móc kiểm thử lịch sử hỏi đáp (chỉ profile dev/test): ghi sẵn lượt hỏi + quyết định cho một bài.
 */
@Repository
@Profile({"dev", "test"})
public class QaHistoryTestHookRepository {

  private final JdbcClient jdbc;

  public QaHistoryTestHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void interaction(
      UUID orgId,
      UUID id,
      UUID studentId,
      UUID classId,
      UUID targetId,
      String question,
      String replyKind,
      String reply,
      String sourceJson,
      int secondsAgo) {
    jdbc.sql(
            """
            INSERT INTO ai_interaction (id, org_id, student_id, class_id, target_id, target_kind,
                                        question, reply_kind, reply, source, created_at)
            VALUES (:id, :orgId, :student, :class, :target, 'graded', :q, :rk, :reply,
                    CAST(:source AS jsonb), now() - make_interval(secs => :ago))
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("student", studentId)
        .param("class", classId)
        .param("target", targetId)
        .param("q", question)
        .param("rk", replyKind)
        .param("reply", reply)
        .param("source", sourceJson)
        .param("ago", secondsAgo)
        .update();
  }

  public void decision(
      UUID orgId,
      UUID id,
      UUID interactionId,
      UUID studentId,
      UUID targetId,
      String decision,
      String rule) {
    jdbc.sql(
            """
            INSERT INTO ai_policy_decision (id, org_id, interaction_id, student_id, target_id,
                                            decision, rule_code, check_ms, firewall_version)
            VALUES (:id, :orgId, :interaction, :student, :target, :decision, :rule, 3, 'e2e')
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("interaction", interactionId)
        .param("student", studentId)
        .param("target", targetId)
        .param("decision", decision)
        .param("rule", rule)
        .update();
  }

  public String reply(UUID orgId, UUID interactionId) {
    return jdbc.sql(
            "SELECT i.reply FROM ai_interaction i WHERE i.org_id = :orgId AND i.id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", interactionId)
        .query(String.class)
        .optional()
        .orElse(null);
  }
}
