package vn.geo.server.knowledge.internal.kb.qa.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Hỏi đáp theo kho (T-261128-58, US-S5E3-03…05): ngữ cảnh lượt hỏi (fn_kb_qa_context, V655), bài
 * giảng mẫu đã duyệt, cờ thông báo riêng tư, nhật ký AIInteraction / AIPolicyDecision, bộ đếm theo
 * luật, cảnh báo mục tài liệu có đáp số.
 */
@Repository
public class KbQaRepository {

  /** Ngữ cảnh một lượt hỏi (đáp số chỉ có khi bài còn khoá với người hỏi; không gửi thiết bị). */
  public record Context(
      String targetKind,
      UUID classId,
      boolean member,
      Boolean aiEnabled,
      UUID problemVersionId,
      boolean locked,
      String finalAnswer,
      String stepsJson,
      String formsJson,
      String rubricText) {}

  public record Lecture(UUID id, String slug, String title, String body, double rank) {}

  public record Stored(UUID id, UUID studentId, UUID targetId, String sourceJson) {}

  public record Section(UUID id, UUID docId, int version, String title, String body) {}

  public record Warning(String docCode, int version, String sectionTitle, int hits) {}

  private final JdbcClient jdbc;

  public KbQaRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Context> context(UUID orgId, UUID userId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT target_kind, class_id, member, ai_enabled, problem_version_id, locked,
                   final_answer, steps, answer_forms, rubric_text
            FROM fn_kb_qa_context(:orgId, :user, :target)
            """)
        .param("orgId", orgId)
        .param("user", userId)
        .param("target", targetId)
        .query((rs, i) ->
            new Context(
                rs.getString("target_kind"),
                rs.getObject("class_id", UUID.class),
                rs.getBoolean("member"),
                (Boolean) rs.getObject("ai_enabled"),
                rs.getObject("problem_version_id", UUID.class),
                rs.getBoolean("locked"),
                rs.getString("final_answer"),
                rs.getString("steps"),
                rs.getString("answer_forms"),
                rs.getString("rubric_text")))
        .optional();
  }

  /**
   * Văn bản bài nộp của các bạn cùng bài có chấm (fn_kb_qa_peer_texts, V845; Dev-S5E3-13): chỉ cho
   * bộ kiểm private_content, không bao giờ vào yêu cầu AI. Bài luyện: rỗng.
   */
  public List<String> peerTexts(UUID orgId, UUID userId, UUID targetId) {
    return jdbc.sql("SELECT body FROM fn_kb_qa_peer_texts(:orgId, :user, :target)")
        .param("orgId", orgId)
        .param("user", userId)
        .param("target", targetId)
        .query(String.class)
        .list();
  }

  /**
   * Bài giảng mẫu ĐÃ mang nhãn "Giáo viên đã duyệt" khớp câu hỏi (BR-S5E3-08, QS5-10): nội dung hệ
   * thống (org nền tảng) và bài giảng mẫu của trường; bài AI viết chưa duyệt không làm nguồn.
   */
  public List<Lecture> approvedLectures(UUID orgId, String query, double minScore, int limit) {
    return jdbc.sql(
            """
            WITH q AS (
              SELECT to_tsquery('simple', COALESCE(NULLIF(
                       (SELECT string_agg(t.lexeme, ' | ')
                          FROM unnest(to_tsvector('simple', kb_norm(:q))) t
                         WHERE t.lexeme ~ '^[a-z0-9]+$'), ''), 'zzzzkhongco')) AS q)
            SELECT x.id, x.slug, x.title, x.body, x.rank FROM (
              SELECT l.id, l.slug, l.title, l.body,
                     ts_rank_cd(to_tsvector('simple', kb_norm(l.title || ' ' || l.body)), q.q, 1 | 32)
                       AS rank
              FROM lecture l, q
              WHERE l.org_id IN (platform_org_id(), :orgId)
                AND l.approval_status = 'teacher_approved'
                AND to_tsvector('simple', kb_norm(l.title || ' ' || l.body)) @@ q.q
            ) x
            WHERE x.rank >= :min
            ORDER BY x.rank DESC, x.slug
            LIMIT :limit
            -- sqllint:allow=tenant bài giảng mẫu: nội dung hệ thống (org nền tảng) + của trường
            """)
        .param("q", query.length() > 2000 ? query.substring(0, 2000) : query)
        .param("orgId", orgId)
        .param("min", minScore)
        .param("limit", limit)
        .query((rs, i) ->
            new Lecture(
                rs.getObject("id", UUID.class),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getString("body"),
                rs.getDouble("rank")))
        .list();
  }

  public boolean acked(UUID orgId, UUID userId, String version) {
    return jdbc.sql(
                """
                SELECT count(*) FROM kb_privacy_ack a
                WHERE a.org_id = :orgId AND a.user_id = :user AND a.version = :v
                """)
            .param("orgId", orgId)
            .param("user", userId)
            .param("v", version)
            .query(Long.class)
            .single()
        > 0;
  }

  public void ack(UUID orgId, UUID userId, String version) {
    jdbc.sql(
            """
            INSERT INTO kb_privacy_ack (org_id, user_id, version) VALUES (:orgId, :user, :v)
            ON CONFLICT DO NOTHING
            """)
        .param("orgId", orgId)
        .param("user", userId)
        .param("v", version)
        .update();
  }

  public void interaction(
      UUID orgId,
      UUID id,
      UUID studentId,
      UUID classId,
      UUID targetId,
      String targetKind,
      String question,
      String replyKind,
      String reply,
      String sourceJson) {
    jdbc.sql(
            """
            INSERT INTO ai_interaction (id, org_id, student_id, class_id, target_id, target_kind,
                                        question, reply_kind, reply, source)
            VALUES (:id, :orgId, :student, :class, :target, :kind, :q, :rk, :reply,
                    CAST(:source AS jsonb))
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("student", studentId)
        .param("class", classId)
        .param("target", targetId)
        .param("kind", targetKind)
        .param("q", question)
        .param("rk", replyKind)
        .param("reply", reply)
        .param("source", sourceJson)
        .update();
  }

  public void decision(
      UUID orgId,
      UUID id,
      UUID interactionId,
      UUID studentId,
      UUID targetId,
      String decision,
      String rule,
      Integer checkMs,
      String version) {
    jdbc.sql(
            """
            INSERT INTO ai_policy_decision (id, org_id, interaction_id, student_id, target_id,
                                            decision, rule_code, check_ms, firewall_version)
            VALUES (:id, :orgId, :interaction, :student, :target, :decision, :rule, :ms, :v)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("interaction", interactionId)
        .param("student", studentId)
        .param("target", targetId)
        .param("decision", decision)
        .param("rule", rule)
        .param("ms", checkMs)
        .param("v", version)
        .update();
  }

  public void count(UUID orgId, LocalDate day, String decision, String rule) {
    jdbc.sql(
            """
            INSERT INTO ai_policy_counter (org_id, day, decision, rule_code, n)
            VALUES (:orgId, :day, :decision, :rule, 1)
            ON CONFLICT (org_id, day, decision, rule_code) DO UPDATE SET n = ai_policy_counter.n + 1
            """)
        .param("orgId", orgId)
        .param("day", day)
        .param("decision", decision)
        .param("rule", rule == null ? "" : rule)
        .update();
  }

  public void warn(UUID orgId, UUID id, UUID targetId, UUID docId, int version, UUID sectionId) {
    jdbc.sql(
            """
            INSERT INTO kb_answer_warning (id, org_id, target_id, doc_id, version, section_id)
            VALUES (:id, :orgId, :target, :doc, :version, :section)
            ON CONFLICT (org_id, target_id, section_id) DO UPDATE
               SET hits = kb_answer_warning.hits + 1, last_seen_at = now()
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("target", targetId)
        .param("doc", docId)
        .param("version", version)
        .param("section", sectionId)
        .update();
  }

  public List<Warning> warnings(UUID orgId, UUID targetId) {
    return jdbc.sql(
            """
            SELECT d.code, w.version, s.title, w.hits
            FROM kb_answer_warning w
            JOIN kb_document d ON d.org_id = :orgId AND d.id = w.doc_id
            JOIN kb_section s ON s.org_id = :orgId AND s.id = w.section_id
            WHERE w.org_id = :orgId AND w.target_id = :target
            ORDER BY d.code, w.version, s.ord
            LIMIT 100
            """)
        .param("orgId", orgId)
        .param("target", targetId)
        .query((rs, i) ->
            new Warning(
                rs.getString("code"),
                rs.getInt("version"),
                rs.getString("title"),
                rs.getInt("hits")))
        .list();
  }

  /** Giáo viên có quyền trên bài (fn_help_level_target, V655). */
  public boolean teacherOwns(UUID orgId, UUID teacherId, UUID targetId) {
    return jdbc.sql("SELECT count(*) FROM fn_help_level_target(:orgId, :teacher, :target)")
            .param("orgId", orgId)
            .param("teacher", teacherId)
            .param("target", targetId)
            .query(Long.class)
            .single()
        > 0;
  }

  public Optional<Stored> stored(UUID orgId, UUID interactionId) {
    return jdbc.sql(
            """
            SELECT i.id, i.student_id, i.target_id, i.source::text AS source
            FROM ai_interaction i
            WHERE i.org_id = :orgId AND i.id = :id
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", interactionId)
        .query((rs, i) ->
            new Stored(
                rs.getObject("id", UUID.class),
                rs.getObject("student_id", UUID.class),
                rs.getObject("target_id", UUID.class),
                rs.getString("source")))
        .optional();
  }

  public Optional<Section> section(UUID orgId, UUID sectionId) {
    return jdbc.sql(
            """
            SELECT s.id, s.doc_id, s.version, s.title, s.body FROM kb_section s
            WHERE s.org_id = :orgId AND s.id = :id
            """)
        .param("orgId", orgId)
        .param("id", sectionId)
        .query((rs, i) ->
            new Section(
                rs.getObject("id", UUID.class),
                rs.getObject("doc_id", UUID.class),
                rs.getInt("version"),
                rs.getString("title"),
                rs.getString("body")))
        .optional();
  }

  public Optional<Section> lecture(UUID orgId, UUID lectureId) {
    return jdbc.sql(
            """
            SELECT l.id, l.title, l.body FROM lecture l
            WHERE l.org_id IN (platform_org_id(), :orgId) AND l.id = :id
            LIMIT 1
            -- sqllint:allow=tenant bài giảng mẫu: nội dung hệ thống (org nền tảng) + của trường
            """)
        .param("orgId", orgId)
        .param("id", lectureId)
        .query((rs, i) ->
            new Section(
                rs.getObject("id", UUID.class),
                null,
                1,
                rs.getString("title"),
                rs.getString("body")))
        .optional();
  }
}
