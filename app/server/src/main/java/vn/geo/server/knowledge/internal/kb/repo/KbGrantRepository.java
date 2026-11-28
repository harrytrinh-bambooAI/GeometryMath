package vn.geo.server.knowledge.internal.kb.repo;

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
 * Gắn tài liệu cho lớp / bài (kb_grant, US-S5E3-02) và đọc chéo có chủ đích: lớp (iam), bài giao
 * (assignment), giáo án đã duyệt (lessonplan) — chỉ đọc.
 */
@Repository
public class KbGrantRepository {

  public record Grant(
      UUID id,
      UUID docId,
      UUID classId,
      UUID assignmentId,
      Instant validFrom,
      Instant validTo,
      Instant createdAt) {}

  /** Tài liệu gắn cho lớp / bài, hiệu lực tại thời điểm hỏi, kèm phiên bản Đang dùng. */
  public record Effective(
      UUID grantId,
      UUID docId,
      String code,
      String title,
      int activeVersion,
      UUID assignmentId,
      Instant validFrom,
      Instant validTo) {}

  public record Plan(UUID id, String code, Integer approvedVersion) {}

  private final JdbcClient jdbc;

  public KbGrantRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void insert(
      UUID orgId,
      UUID id,
      UUID docId,
      UUID classId,
      UUID assignmentId,
      Instant from,
      Instant to,
      UUID by) {
    jdbc.sql(
            """
            INSERT INTO kb_grant (id, org_id, doc_id, class_id, assignment_id, valid_from, valid_to, created_by)
            VALUES (:id, :orgId, :doc, :class, :assignment, :from, :to, :by)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("class", classId)
        .param("assignment", assignmentId)
        .param("from", from == null ? null : Timestamp.from(from))
        .param("to", to == null ? null : Timestamp.from(to))
        .param("by", by)
        .update();
  }

  public List<Grant> ofDoc(UUID orgId, UUID docId) {
    return jdbc.sql(
            """
            SELECT g.id, g.doc_id, g.class_id, g.assignment_id, g.valid_from, g.valid_to, g.created_at
            FROM kb_grant g
            WHERE g.org_id = :orgId AND g.doc_id = :doc AND g.revoked_at IS NULL
            ORDER BY g.created_at
            LIMIT 500
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .query(KbGrantRepository::grant)
        .list();
  }

  public int revoke(UUID orgId, UUID docId, UUID grantId, Instant now) {
    return jdbc.sql(
            """
            UPDATE kb_grant SET revoked_at = :now
            WHERE org_id = :orgId AND doc_id = :doc AND id = :id AND revoked_at IS NULL
            """)
        .param("orgId", orgId)
        .param("doc", docId)
        .param("id", grantId)
        .param("now", Timestamp.from(now))
        .update();
  }

  /**
   * Tài liệu hiệu lực của lớp tại {@code at}: gắn cả lớp, cộng gắn riêng bài {@code assignmentId}
   * (NULL = mọi gắn của lớp); chỉ tài liệu có phiên bản Đang dùng (BR-S5E3-06, 07).
   */
  public List<Effective> effective(
      UUID orgId, UUID classId, UUID assignmentId, boolean allAssignments, Instant at) {
    return jdbc.sql(
            """
            SELECT g.id AS grant_id, d.id AS doc_id, d.code, d.title, v.version, g.assignment_id,
                   g.valid_from, g.valid_to
            FROM kb_grant g
            JOIN kb_document d ON d.org_id = :orgId AND d.id = g.doc_id
            JOIN kb_document_version v ON v.org_id = :orgId AND v.doc_id = g.doc_id AND v.status = 'active'
            WHERE g.org_id = :orgId AND g.class_id = :class AND g.revoked_at IS NULL
              AND (:all OR g.assignment_id IS NULL OR g.assignment_id = :assignment)
              AND (g.valid_from IS NULL OR g.valid_from <= :at)
              AND (g.valid_to IS NULL OR g.valid_to > :at)
            ORDER BY d.code, g.created_at
            LIMIT 500
            """)
        .param("orgId", orgId)
        .param("class", classId)
        .param("assignment", assignmentId)
        .param("all", allAssignments)
        .param("at", Timestamp.from(at))
        .query((rs, i) ->
            new Effective(
                rs.getObject("grant_id", UUID.class),
                rs.getObject("doc_id", UUID.class),
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("version"),
                rs.getObject("assignment_id", UUID.class),
                KbRepository.instant(rs, "valid_from"),
                KbRepository.instant(rs, "valid_to")))
        .list();
  }

  // ---------------------------------------------------------------- đọc chéo (chỉ đọc)

  /** Tên lớp nếu lớp do chính giáo viên dạy (lớp người khác, lớp lạ: rỗng → NOT_FOUND). */
  public Optional<String> ownedClass(UUID orgId, UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.name FROM class c
            WHERE c.org_id = :orgId AND c.id = :classId AND c.teacher_id = :teacher LIMIT 1
            -- sqllint:allow=ownership đọc lớp (iam) để kiểm chủ lớp trước khi gắn tài liệu (US-S5E3-02)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("teacher", teacherId)
        .query(String.class)
        .optional();
  }

  /** Bài giao có thuộc lớp không (bài lớp khác / bài lạ: false → NOT_FOUND). */
  public boolean assignmentInClass(UUID orgId, UUID classId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id FROM assignment a
            WHERE a.org_id = :orgId AND a.id = :id AND a.class_id = :classId
            LIMIT 1
            -- sqllint:allow=ownership đọc bài giao (assignment) để kiểm bài thuộc lớp khi gắn tài liệu (US-S5E3-02)
            """)
        .param("orgId", orgId)
        .param("id", assignmentId)
        .param("classId", classId)
        .query(UUID.class)
        .optional()
        .isPresent();
  }

  /** Giáo án của giáo viên theo mã GA-nnnn hoặc id (người khác: rỗng → NOT_FOUND). */
  public Optional<Plan> plan(UUID orgId, UUID teacherId, String ref) {
    return jdbc.sql(
            """
            SELECT p.id, p.code, p.approved_version FROM lesson_plan p
            WHERE p.org_id = :orgId AND p.teacher_id = :teacher
              AND (p.code = :code OR p.id = :id)
            LIMIT 1
            -- sqllint:allow=ownership đọc giáo án đã duyệt (lessonplan, US-S5E1-05) để đưa vào kho (BR-S5E3-03)
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("code", ref.toUpperCase())
        .param("id", KbRepository.asUuid(ref))
        .query((rs, i) ->
            new Plan(
                rs.getObject("id", UUID.class),
                rs.getString("code"),
                (Integer) rs.getObject("approved_version")))
        .optional();
  }

  /** Nội dung JSON của phiên bản giáo án đã duyệt. */
  public Optional<String> planContent(UUID orgId, UUID planId, int version) {
    return jdbc.sql(
            """
            SELECT v.content::text FROM lesson_plan_version v
            WHERE v.org_id = :orgId AND v.plan_id = :plan AND v.version = :version
            LIMIT 1
            -- sqllint:allow=ownership đọc giáo án đã duyệt (lessonplan, US-S5E1-05) để đưa vào kho (BR-S5E3-03)
            """)
        .param("orgId", orgId)
        .param("plan", planId)
        .param("version", version)
        .query(String.class)
        .optional();
  }

  private static Grant grant(ResultSet rs, int i) throws SQLException {
    return new Grant(
        rs.getObject("id", UUID.class),
        rs.getObject("doc_id", UUID.class),
        rs.getObject("class_id", UUID.class),
        rs.getObject("assignment_id", UUID.class),
        KbRepository.instant(rs, "valid_from"),
        KbRepository.instant(rs, "valid_to"),
        KbRepository.instant(rs, "created_at"));
  }
}
