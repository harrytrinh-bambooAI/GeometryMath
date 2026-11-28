package vn.geo.server.exam.internal.draft.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Trợ lý soạn đề nháp (T-261128-63, US-S5E2-06): nguồn tài liệu của giáo viên (đọc chéo kho tri
 * thức), lớp của giáo viên, lần soạn và đề nháp ({@code exam_draft_batch}, {@code exam_draft_item},
 * V775).
 */
@Repository
public class ExamDraftRepository {

  /**
   * Tài liệu của chính giáo viên; {@code version}/{@code content} null khi chưa có bản Đang dùng.
   */
  public record Source(
      UUID id, String code, String title, Integer version, String content, boolean aiAuthored) {}

  public record Batch(
      UUID id,
      UUID classId,
      UUID docId,
      String docCode,
      int docVersion,
      int requested,
      Instant createdAt) {}

  public record Item(
      UUID id,
      UUID batchId,
      int ord,
      String statement,
      String sourceSection,
      String sceneJson,
      String aiAnswer,
      String engineAnswer,
      String machineLabel,
      String reviewReason,
      String status,
      Instant approvedAt,
      String docCode,
      int docVersion) {}

  private final JdbcClient jdbc;

  public ExamDraftRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Source> source(UUID orgId, UUID teacherId, UUID docId) {
    return jdbc.sql(
            """
            SELECT d.id, d.code, d.title, d.ai_authored, v.version, v.content_md
            FROM kb_document d
            LEFT JOIN kb_document_version v
              ON v.org_id = :orgId AND v.doc_id = d.id AND v.status = 'active'
            WHERE d.org_id = :orgId AND d.id = :id AND d.owner_id = :teacher AND d.archived_at IS NULL
            LIMIT 1
            -- sqllint:allow=ownership đọc tài liệu Đang dùng của chính giáo viên làm nguồn soạn đề (BR-S5E2-14)
            """)
        .param("orgId", orgId)
        .param("id", docId)
        .param("teacher", teacherId)
        .query((rs, i) ->
            new Source(
                rs.getObject("id", UUID.class),
                rs.getString("code"),
                rs.getString("title"),
                (Integer) rs.getObject("version"),
                rs.getString("content_md"),
                rs.getBoolean("ai_authored")))
        .optional();
  }

  public boolean ownsClass(UUID orgId, UUID teacherId, UUID classId) {
    return jdbc.sql(
                """
                SELECT count(*) FROM class c
                WHERE c.org_id = :orgId AND c.id = :classId AND c.teacher_id = :teacher
                -- sqllint:allow=ownership đọc lớp (iam) để kiểm chủ lớp trước khi soạn đề
                """)
            .param("orgId", orgId)
            .param("classId", classId)
            .param("teacher", teacherId)
            .query(Long.class)
            .single()
        > 0;
  }

  public void insertBatch(
      UUID orgId,
      UUID id,
      UUID teacherId,
      UUID classId,
      UUID docId,
      String docCode,
      int docVersion,
      int requested) {
    jdbc.sql(
            """
            INSERT INTO exam_draft_batch (id, org_id, teacher_id, class_id, doc_id, doc_code,
                                          doc_version, requested)
            VALUES (:id, :orgId, :teacher, :class, :doc, :code, :version, :n)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("class", classId)
        .param("doc", docId)
        .param("code", docCode)
        .param("version", docVersion)
        .param("n", requested)
        .update();
  }

  public void insertItem(
      UUID orgId,
      UUID id,
      UUID batchId,
      UUID teacherId,
      int ord,
      String statement,
      String section,
      String sceneJson,
      String aiAnswer,
      String engineAnswer,
      String machineLabel,
      String reviewReason) {
    jdbc.sql(
            """
            INSERT INTO exam_draft_item (id, org_id, batch_id, teacher_id, ord, statement,
                                         source_section, scene, ai_answer, engine_answer,
                                         machine_label, review_reason)
            VALUES (:id, :orgId, :batch, :teacher, :ord, :statement, :section, CAST(:scene AS jsonb),
                    :ai, :engine, :label, :reason)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("batch", batchId)
        .param("teacher", teacherId)
        .param("ord", ord)
        .param("statement", statement)
        .param("section", section)
        .param("scene", sceneJson)
        .param("ai", aiAnswer)
        .param("engine", engineAnswer)
        .param("label", machineLabel)
        .param("reason", reviewReason)
        .update();
  }

  public Optional<Batch> batch(UUID orgId, UUID teacherId, UUID id) {
    return jdbc.sql(
            """
            SELECT b.id, b.class_id, b.doc_id, b.doc_code, b.doc_version, b.requested, b.created_at
            FROM exam_draft_batch b
            WHERE b.org_id = :orgId AND b.id = :id AND b.teacher_id = :teacher
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("teacher", teacherId)
        .query((rs, i) ->
            new Batch(
                rs.getObject("id", UUID.class),
                rs.getObject("class_id", UUID.class),
                rs.getObject("doc_id", UUID.class),
                rs.getString("doc_code"),
                rs.getInt("doc_version"),
                rs.getInt("requested"),
                rs.getTimestamp("created_at").toInstant()))
        .optional();
  }

  public List<Item> items(UUID orgId, UUID batchId) {
    return jdbc.sql(
            """
            SELECT i.id, i.batch_id, i.ord, i.statement, i.source_section, i.scene::text AS scene,
                   i.ai_answer, i.engine_answer, i.machine_label, i.review_reason, i.status,
                   i.approved_at, b.doc_code, b.doc_version
            FROM exam_draft_item i
            JOIN exam_draft_batch b ON b.org_id = :orgId AND b.id = i.batch_id
            WHERE i.org_id = :orgId AND i.batch_id = :batch
            ORDER BY i.ord LIMIT 10
            """)
        .param("orgId", orgId)
        .param("batch", batchId)
        .query(ExamDraftRepository::item)
        .list();
  }

  public Optional<Item> item(UUID orgId, UUID teacherId, UUID itemId) {
    return jdbc.sql(
            """
            SELECT i.id, i.batch_id, i.ord, i.statement, i.source_section, i.scene::text AS scene,
                   i.ai_answer, i.engine_answer, i.machine_label, i.review_reason, i.status,
                   i.approved_at, b.doc_code, b.doc_version
            FROM exam_draft_item i
            JOIN exam_draft_batch b ON b.org_id = :orgId AND b.id = i.batch_id
            WHERE i.org_id = :orgId AND i.id = :id AND i.teacher_id = :teacher
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", itemId)
        .param("teacher", teacherId)
        .query(ExamDraftRepository::item)
        .optional();
  }

  /** Ngân hàng "Của tôi": đề nháp giáo viên đã duyệt, mới nhất trước. */
  public List<Item> bank(UUID orgId, UUID teacherId) {
    return jdbc.sql(
            """
            SELECT i.id, i.batch_id, i.ord, i.statement, i.source_section, i.scene::text AS scene,
                   i.ai_answer, i.engine_answer, i.machine_label, i.review_reason, i.status,
                   i.approved_at, b.doc_code, b.doc_version
            FROM exam_draft_item i
            JOIN exam_draft_batch b ON b.org_id = :orgId AND b.id = i.batch_id
            WHERE i.org_id = :orgId AND i.teacher_id = :teacher AND i.status = 'approved'
            ORDER BY i.approved_at DESC, i.id LIMIT 500
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .query(ExamDraftRepository::item)
        .list();
  }

  /** Duyệt một đề đã kiểm chứng; false khi đề không còn ở trạng thái nháp đã kiểm chứng. */
  public boolean approve(UUID orgId, UUID teacherId, UUID itemId, Instant at) {
    return jdbc.sql(
                """
                UPDATE exam_draft_item SET status = 'approved', approved_at = :at
                WHERE org_id = :orgId AND id = :id AND teacher_id = :teacher
                  AND status = 'draft' AND machine_label = 'verified' AND engine_answer IS NOT NULL
                """)
            .param("orgId", orgId)
            .param("id", itemId)
            .param("teacher", teacherId)
            .param("at", java.sql.Timestamp.from(at))
            .update()
        == 1;
  }

  private static Item item(ResultSet rs, int i) throws SQLException {
    java.sql.Timestamp at = rs.getTimestamp("approved_at");
    return new Item(
        rs.getObject("id", UUID.class),
        rs.getObject("batch_id", UUID.class),
        rs.getInt("ord"),
        rs.getString("statement"),
        rs.getString("source_section"),
        rs.getString("scene"),
        rs.getString("ai_answer"),
        rs.getString("engine_answer"),
        rs.getString("machine_label"),
        rs.getString("review_reason"),
        rs.getString("status"),
        at == null ? null : at.toInstant(),
        rs.getString("doc_code"),
        rs.getInt("doc_version"));
  }
}
