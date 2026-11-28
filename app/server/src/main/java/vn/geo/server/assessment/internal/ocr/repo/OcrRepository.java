package vn.geo.server.assessment.internal.ocr.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Bảng ocr_artifact (V675): bản đọc chữ phái sinh, chỉ thêm phiên bản (BR-S5E4-13). */
@Repository
public class OcrRepository {

  /** Một phiên bản bản đọc chữ; {@code pagesJson} là mảng JSON các trang. */
  public record Artifact(
      UUID id,
      UUID submissionId,
      UUID versionId,
      int version,
      String status,
      String pagesJson,
      UUID editedBy,
      Instant createdAt) {}

  private static final String COLS =
      "o.id, o.submission_id, o.version_id, o.version, o.status, o.pages::text AS pages, o.edited_by, o.created_at";

  private final JdbcClient jdbc;

  public OcrRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  private static Artifact map(ResultSet rs, int i) throws SQLException {
    return new Artifact(
        rs.getObject("id", UUID.class),
        rs.getObject("submission_id", UUID.class),
        rs.getObject("version_id", UUID.class),
        rs.getInt("version"),
        rs.getString("status"),
        rs.getString("pages"),
        rs.getObject("edited_by", UUID.class),
        rs.getTimestamp("created_at").toInstant());
  }

  /** Mọi phiên bản bản đọc chữ của một phiên bản bài nộp, cũ trước. */
  public List<Artifact> ofVersion(UUID orgId, UUID versionId) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM ocr_artifact o WHERE o.org_id = :orgId AND o.version_id = :v"
                + " ORDER BY o.version LIMIT 200")
        .param("orgId", orgId)
        .param("v", versionId)
        .query(OcrRepository::map)
        .list();
  }

  public Optional<Artifact> latest(UUID orgId, UUID versionId) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM ocr_artifact o WHERE o.org_id = :orgId AND o.version_id = :v"
                + " ORDER BY o.version DESC LIMIT 1")
        .param("orgId", orgId)
        .param("v", versionId)
        .query(OcrRepository::map)
        .optional();
  }

  /** Thêm phiên bản kế tiếp (số phiên bản = lớn nhất + 1); trả số phiên bản. */
  public int append(
      UUID orgId,
      UUID id,
      UUID submissionId,
      UUID versionId,
      UUID studentId,
      String status,
      String pagesJson,
      UUID editedBy,
      String year) {
    return jdbc.sql(
            """
            INSERT INTO ocr_artifact (id, org_id, submission_id, version_id, student_id, version, status, pages,
                                      edited_by, academic_year)
            SELECT :id, :orgId, :sub, :v, :st,
                   COALESCE((SELECT max(x.version) FROM ocr_artifact x WHERE x.org_id = :orgId AND x.version_id = :v), 0) + 1,
                   :status, CAST(:pages AS jsonb), :by, :y
            RETURNING version
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("v", versionId)
        .param("st", studentId)
        .param("status", status)
        .param("pages", pagesJson)
        .param("by", editedBy)
        .param("y", year)
        .query(Integer.class)
        .single();
  }

  /** Bản lỗi ("Chưa đọc được") của lần đọc trước được thay bằng lần Đọc lại thành công. */
  public int deleteFailed(UUID orgId, UUID versionId) {
    return jdbc.sql(
            """
            DELETE FROM ocr_artifact WHERE org_id = :orgId AND version_id = :v AND status = 'failed'
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .update();
  }

  // ------------------------------------------------------------------ xoá (SubmissionDataEraser)

  public int deleteOfStudent(UUID orgId, UUID studentId) {
    return jdbc.sql("DELETE FROM ocr_artifact WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  public int deleteOfYear(String year) {
    return jdbc.sql(
            """
            DELETE FROM ocr_artifact WHERE academic_year = :y
            -- sqllint:allow=tenant job xoá theo năm học chạy xuyên trường (BR-S5E4-28b)
            """)
        .param("y", year)
        .update();
  }

  public int count(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            "SELECT count(*) FROM ocr_artifact o WHERE o.org_id = :orgId AND o.submission_id = :s LIMIT 1")
        .param("orgId", orgId)
        .param("s", submissionId)
        .query(Integer.class)
        .single();
  }
}
