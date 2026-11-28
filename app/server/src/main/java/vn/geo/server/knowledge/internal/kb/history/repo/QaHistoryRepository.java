package vn.geo.server.knowledge.internal.kb.history.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Lịch sử hỏi đáp theo bài cho giáo viên và cho chính học sinh, đánh dấu "chặn sai" / "lẽ ra phải
 * chặn", tổng hợp theo luật cho người vận hành (T-261128-63, US-S5E3-06; bảng ai_interaction,
 * ai_policy_decision của V655, ai_qa_feedback* của V775).
 */
@Repository
public class QaHistoryRepository {

  /** Một lượt hỏi kèm quyết định gần nhất của bộ kiểm và đánh dấu của giáo viên (nếu có). */
  public record Row(
      UUID id,
      Instant at,
      UUID studentId,
      String studentName,
      String question,
      String replyKind,
      String reply,
      String sourceJson,
      String decision,
      String rule,
      String feedbackType,
      String feedbackNote) {}

  /** Lượt hỏi để đánh dấu: bài, quyết định, luật. */
  public record Target(UUID id, UUID targetId, String decision, String rule) {}

  public record Feedback(String type, String rule) {}

  public record Count(String rule, String type, long n) {}

  /** Một dòng báo cáo: loại nguồn, quyết định, luật. */
  public record Mix(String sourceType, String decision, String rule, long n) {}

  private final JdbcClient jdbc;

  public QaHistoryRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Giáo viên chủ lớp của bài (fn_help_level_target, V655). */
  public boolean teacherOwns(UUID orgId, UUID teacherId, UUID targetId) {
    return jdbc.sql("SELECT count(*) FROM fn_help_level_target(:orgId, :teacher, :target)")
            .param("orgId", orgId)
            .param("teacher", teacherId)
            .param("target", targetId)
            .query(Long.class)
            .single()
        > 0;
  }

  /**
   * Một trang lịch sử của bài (mới nhất trước), lọc theo quyết định; con trỏ = (giờ, id) của dòng
   * cuối trang trước.
   */
  public List<Row> page(
      UUID orgId,
      UUID targetId,
      UUID studentId,
      String decision,
      Instant afterAt,
      UUID afterId,
      int limit) {
    return jdbc.sql(
            """
            SELECT i.id, i.created_at, i.student_id, u.display_name, i.question, i.reply_kind, i.reply,
                   i.source::text AS source, d.decision, d.rule_code, f.type AS fb_type, f.note AS fb_note
            FROM ai_interaction i
            JOIN app_user u ON u.org_id = :orgId AND u.id = i.student_id
            LEFT JOIN LATERAL (
              SELECT p.decision, p.rule_code FROM ai_policy_decision p
              WHERE p.org_id = :orgId AND p.interaction_id = i.id
              ORDER BY p.created_at DESC LIMIT 1) d ON true
            LEFT JOIN ai_qa_feedback f ON f.org_id = :orgId AND f.interaction_id = i.id
            WHERE i.org_id = :orgId AND i.target_id = :target
              AND (CAST(:student AS uuid) IS NULL OR i.student_id = CAST(:student AS uuid))
              AND (CAST(:decision AS text) IS NULL OR d.decision = CAST(:decision AS text))
              AND (CAST(:afterAt AS timestamptz) IS NULL
                   OR (i.created_at, i.id) < (CAST(:afterAt AS timestamptz), CAST(:afterId AS uuid)))
            ORDER BY i.created_at DESC, i.id DESC
            LIMIT :limit
            -- sqllint:allow=ownership tên hiển thị học sinh (iam) trên lịch sử hỏi đáp của lớp mình (BR-S5E3-19)
            """)
        .param("orgId", orgId)
        .param("target", targetId)
        .param("student", studentId)
        .param("decision", decision)
        .param("afterAt", afterAt == null ? null : Timestamp.from(afterAt))
        .param("afterId", afterId)
        .param("limit", limit)
        .query(QaHistoryRepository::row)
        .list();
  }

  public Optional<Target> interaction(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT i.id, i.target_id, d.decision, d.rule_code
            FROM ai_interaction i
            LEFT JOIN LATERAL (
              SELECT p.decision, p.rule_code FROM ai_policy_decision p
              WHERE p.org_id = :orgId AND p.interaction_id = i.id
              ORDER BY p.created_at DESC LIMIT 1) d ON true
            WHERE i.org_id = :orgId AND i.id = :id
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) ->
            new Target(
                rs.getObject("id", UUID.class),
                rs.getObject("target_id", UUID.class),
                rs.getString("decision"),
                rs.getString("rule_code")))
        .optional();
  }

  public Optional<Feedback> feedback(UUID orgId, UUID interactionId) {
    return jdbc.sql(
            """
            SELECT f.type, f.rule_code FROM ai_qa_feedback f
            WHERE f.org_id = :orgId AND f.interaction_id = :id
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", interactionId)
        .query((rs, i) -> new Feedback(rs.getString("type"), rs.getString("rule_code")))
        .optional();
  }

  public void upsertFeedback(
      UUID orgId, UUID interactionId, UUID teacherId, String type, String note, String rule) {
    jdbc.sql(
            """
            INSERT INTO ai_qa_feedback (org_id, interaction_id, teacher_id, type, note, rule_code)
            VALUES (:orgId, :id, :teacher, :type, :note, :rule)
            ON CONFLICT (org_id, interaction_id) DO UPDATE
               SET teacher_id = EXCLUDED.teacher_id, type = EXCLUDED.type, note = EXCLUDED.note,
                   updated_at = now()
            """)
        .param("orgId", orgId)
        .param("id", interactionId)
        .param("teacher", teacherId)
        .param("type", type)
        .param("note", note)
        .param("rule", rule)
        .update();
  }

  /** Cộng (delta = 1) hoặc trừ (delta = -1) bộ đếm luật × loại đánh dấu. */
  public void count(UUID orgId, String rule, String type, int delta) {
    jdbc.sql(
            """
            INSERT INTO ai_qa_feedback_counter (org_id, rule_code, type, n)
            VALUES (:orgId, :rule, :type, GREATEST(:delta, 0))
            ON CONFLICT (org_id, rule_code, type) DO UPDATE
               SET n = GREATEST(ai_qa_feedback_counter.n + :delta, 0)
            """)
        .param("orgId", orgId)
        .param("rule", rule)
        .param("type", type)
        .param("delta", delta)
        .update();
  }

  /** Tổng hợp mọi trường cho người vận hành (không theo trường, QS5-02); lọc một trường nếu có. */
  public List<Count> counts(UUID orgFilter) {
    return jdbc.sql(
            """
            SELECT c.rule_code, c.type, sum(c.n) AS n FROM ai_qa_feedback_counter c
            WHERE (CAST(:orgId AS uuid) IS NULL OR c.org_id = CAST(:orgId AS uuid))
            GROUP BY c.rule_code, c.type
            ORDER BY c.rule_code, c.type
            -- sqllint:allow=tenant người vận hành xem tổng hợp mọi trường (QS5-02), chỉ số đếm, không nội dung
            """)
        .param("orgId", orgFilter)
        .query((rs, i) -> new Count(rs.getString(1), rs.getString(2), rs.getLong(3)))
        .list();
  }

  /** Người dùng có vai Vận hành (ai_ops_grant, V575). */
  public boolean operator(UUID userId) {
    return Boolean.TRUE.equals(
        jdbc.sql(
                """
                SELECT EXISTS (SELECT 1 FROM ai_ops_grant g WHERE g.user_id = :userId LIMIT 1)
                -- sqllint:allow=ownership vai Vận hành (aigateway) trước khi xem tổng hợp đánh dấu
                """)
            .param("userId", userId)
            .query(Boolean.class)
            .single());
  }

  /** Báo cáo hỏi đáp của bài: đếm theo loại nguồn × quyết định × luật. */
  public List<Mix> mix(UUID orgId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT COALESCE(i.source->>'type', '') AS stype, COALESCE(d.decision, '') AS decision,
                   COALESCE(d.rule_code, '') AS rule, count(*) AS n
            FROM ai_interaction i
            LEFT JOIN LATERAL (
              SELECT p.decision, p.rule_code FROM ai_policy_decision p
              WHERE p.org_id = :orgId AND p.interaction_id = i.id
              ORDER BY p.created_at DESC LIMIT 1) d ON true
            WHERE i.org_id = :orgId AND i.target_id = :target
            GROUP BY 1, 2, 3
            LIMIT 1000
            """)
        .param("orgId", orgId)
        .param("target", targetId)
        .query((rs, i) ->
            new Mix(
                rs.getString("stype"),
                rs.getString("decision"),
                rs.getString("rule"),
                rs.getLong("n")))
        .list();
  }

  private static Row row(ResultSet rs, int i) throws SQLException {
    return new Row(
        rs.getObject("id", UUID.class),
        rs.getTimestamp("created_at").toInstant(),
        rs.getObject("student_id", UUID.class),
        rs.getString("display_name"),
        rs.getString("question"),
        rs.getString("reply_kind"),
        rs.getString("reply"),
        rs.getString("source"),
        rs.getString("decision"),
        rs.getString("rule_code"),
        rs.getString("fb_type"),
        rs.getString("fb_note"));
  }
}
