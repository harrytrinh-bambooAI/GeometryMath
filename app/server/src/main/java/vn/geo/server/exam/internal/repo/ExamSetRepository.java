package vn.geo.server.exam.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** Bộ đề và mã đề của giáo viên (US-S5E2-03, 04): bảng theo trường exam_set, exam_variant. */
@Repository
public class ExamSetRepository {

  public record SetRow(
      UUID id,
      UUID teacherId,
      UUID classId,
      String matrixCode,
      String name,
      long seed,
      String params,
      String status,
      String humanLabel,
      String report,
      UUID approvedBy,
      Instant approvedAt,
      Instant createdAt) {}

  public record VariantRow(UUID id, String code, int position, double difficulty, String body) {}

  private static final String SET_COLS =
      "id, teacher_id, class_id, matrix_code, name, seed, params::text, status, human_label,"
          + " report::text, approved_by, approved_at, created_at";

  private final TenantJdbc jdbc;

  public ExamSetRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Lớp do chính giáo viên dạy (lớp người khác, lớp lạ: false → NOT_FOUND). */
  public boolean ownsClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class c WHERE c.org_id = :orgId AND c.id = :classId
                             AND c.teacher_id = :teacherId LIMIT 1)
            -- sqllint:allow=ownership đọc lớp (iam) để kiểm chủ lớp trước khi lắp đề cho lớp
            """)
        .param("classId", classId)
        .param("teacherId", teacherId)
        .query(Boolean.class)
        .single();
  }

  public void insertSet(
      UUID id,
      UUID teacherId,
      UUID classId,
      String matrixCode,
      String name,
      long seed,
      String params,
      String report) {
    jdbc.sql(
            """
            INSERT INTO exam_set (id, org_id, teacher_id, class_id, matrix_code, name, seed, params, report)
            VALUES (:id, :orgId, :teacherId, :classId, :matrix, :name, :seed, CAST(:params AS jsonb),
                    CAST(:report AS jsonb))
            """)
        .param("id", id)
        .param("teacherId", teacherId)
        .param("classId", classId)
        .param("matrix", matrixCode)
        .param("name", name)
        .param("seed", seed)
        .param("params", params)
        .param("report", report)
        .update();
  }

  public void insertVariant(
      UUID id, UUID setId, String code, int position, double difficulty, String body) {
    jdbc.sql(
            """
            INSERT INTO exam_variant (id, org_id, set_id, code, position, difficulty, body)
            VALUES (:id, :orgId, :setId, :code, :pos, :dk, CAST(:body AS jsonb))
            """)
        .param("id", id)
        .param("setId", setId)
        .param("code", code)
        .param("pos", position)
        .param("dk", difficulty)
        .param("body", body)
        .update();
  }

  public Optional<SetRow> findOwned(UUID id, UUID teacherId) {
    return jdbc.sql(
            "SELECT "
                + SET_COLS
                + " FROM exam_set WHERE org_id = :orgId AND id = :id AND teacher_id = :t")
        .param("id", id)
        .param("t", teacherId)
        .query(ExamSetRepository::set)
        .optional();
  }

  public Optional<SetRow> find(UUID id) {
    return jdbc.sql("SELECT " + SET_COLS + " FROM exam_set WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .query(ExamSetRepository::set)
        .optional();
  }

  public List<SetRow> listByTeacher(UUID teacherId, String status, int limit) {
    return jdbc.sql(
            "SELECT "
                + SET_COLS
                + """
                 FROM exam_set WHERE org_id = :orgId AND teacher_id = :t
                  AND (CAST(:status AS text) IS NULL OR status = :status)
                ORDER BY created_at DESC, id DESC LIMIT :limit
                """)
        .param("t", teacherId)
        .param("status", status)
        .param("limit", limit)
        .query(ExamSetRepository::set)
        .list();
  }

  public long countByTeacher(UUID teacherId) {
    return jdbc.sql("SELECT count(*) FROM exam_set WHERE org_id = :orgId AND teacher_id = :t")
        .param("t", teacherId)
        .query(Long.class)
        .single();
  }

  public List<VariantRow> variants(UUID setId) {
    return jdbc.sql(
            """
            SELECT id, code, position, difficulty, body::text FROM exam_variant
            WHERE org_id = :orgId AND set_id = :setId ORDER BY position LIMIT 8
            """)
        .param("setId", setId)
        .query((rs, i) ->
            new VariantRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getInt(3),
                rs.getDouble(4),
                rs.getString(5)))
        .list();
  }

  public void updateVariant(UUID id, double difficulty, String body) {
    jdbc.sql(
            """
            UPDATE exam_variant SET body = CAST(:body AS jsonb), difficulty = :dk, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("dk", difficulty)
        .param("body", body)
        .update();
  }

  public void updateReport(UUID setId, String report) {
    jdbc.sql("UPDATE exam_set SET report = CAST(:r AS jsonb) WHERE org_id = :orgId AND id = :id")
        .param("id", setId)
        .param("r", report)
        .update();
  }

  public int approve(UUID setId, UUID by, Instant at) {
    return jdbc.sql(
            """
            UPDATE exam_set SET status = 'approved', human_label = 'giao-vien-da-duyet',
              approved_by = :by, approved_at = :at
            WHERE org_id = :orgId AND id = :id AND status = 'draft'
            """)
        .param("id", setId)
        .param("by", by)
        .param("at", Timestamp.from(at))
        .update();
  }

  private static SetRow set(ResultSet rs, int i) throws SQLException {
    Timestamp ap = rs.getTimestamp(12);
    return new SetRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getString(5),
        rs.getLong(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        rs.getObject(11, UUID.class),
        ap == null ? null : ap.toInstant(),
        rs.getTimestamp(13).toInstant());
  }
}
