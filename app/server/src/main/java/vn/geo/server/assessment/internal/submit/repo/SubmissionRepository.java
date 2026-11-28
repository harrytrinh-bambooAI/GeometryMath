package vn.geo.server.assessment.internal.submit.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Bảng của nộp bài (V675): submission_upload, submission, submission_version, cờ riêng tư. */
@Repository
public class SubmissionRepository {

  /** Bài có chấm nhìn từ phía nộp bài. */
  public record Assignment(
      UUID id,
      UUID classId,
      UUID teacherId,
      String title,
      Instant dueAt,
      boolean allowLate,
      String status) {}

  /** Lượt tải một trang. */
  public record Upload(UUID id, UUID studentId, String contentType, int sizeBytes, int received) {}

  /** Bài nộp. */
  public record Sub(
      UUID id,
      UUID assignmentId,
      UUID studentId,
      UUID classId,
      String code,
      int currentVersion,
      String schoolYear) {}

  /** Một phiên bản bài nộp. */
  public record Version(
      UUID id,
      UUID submissionId,
      int version,
      String idempotencyKey,
      int pages,
      Instant submittedAt,
      Integer lateSeconds) {}

  /** Ảnh một trang của phiên bản (không có nội dung). */
  public record Page(int pageNo, String kind, String contentType, String sha256, int bytes) {}

  private final JdbcClient jdbc;

  public SubmissionRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  // ------------------------------------------------------------------ bài có chấm, quan hệ

  public Optional<Assignment> assignment(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT g.id, g.class_id, c.teacher_id, g.title, g.due_at, g.allow_late, g.status
            FROM graded_assignment g JOIN class c ON c.org_id = g.org_id AND c.id = g.class_id
            WHERE g.org_id = :orgId AND g.id = :id AND c.org_id = :orgId LIMIT 1
            -- sqllint:allow=ownership giáo viên chủ lớp (iam) để kiểm quan hệ bài nộp
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) ->
            new Assignment(
                rs.getObject("id", UUID.class),
                rs.getObject("class_id", UUID.class),
                rs.getObject("teacher_id", UUID.class),
                rs.getString("title"),
                rs.getTimestamp("due_at").toInstant(),
                rs.getBoolean("allow_late"),
                rs.getString("status")))
        .optional();
  }

  public boolean activeMember(UUID orgId, UUID classId, UUID studentId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class_member m WHERE m.org_id = :orgId AND m.class_id = :c
                           AND m.user_id = :u AND m.status = 'active' LIMIT 1)
            -- sqllint:allow=ownership thành viên lớp (iam) để kiểm quan hệ học sinh nộp bài
            """)
        .param("orgId", orgId)
        .param("c", classId)
        .param("u", studentId)
        .query(Boolean.class)
        .single();
  }

  public Optional<UUID> classTeacher(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.teacher_id FROM class c WHERE c.org_id = :orgId AND c.id = :c
            -- sqllint:allow=ownership giáo viên chủ lớp (iam) để kiểm quan hệ bài nộp
            """)
        .param("orgId", orgId)
        .param("c", classId)
        .query(UUID.class)
        .optional();
  }

  // ------------------------------------------------------------------ thông báo riêng tư

  public boolean acked(UUID orgId, UUID studentId, String version) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM submission_privacy_ack a
                           WHERE a.org_id = :orgId AND a.student_id = :s AND a.notice_version = :v LIMIT 1)
            """)
        .param("orgId", orgId)
        .param("s", studentId)
        .param("v", version)
        .query(Boolean.class)
        .single();
  }

  public void ack(UUID orgId, UUID studentId, String version) {
    jdbc.sql(
            """
            INSERT INTO submission_privacy_ack (org_id, student_id, notice_version)
            VALUES (:orgId, :s, :v) ON CONFLICT DO NOTHING
            """)
        .param("orgId", orgId)
        .param("s", studentId)
        .param("v", version)
        .update();
  }

  // ------------------------------------------------------------------ tải theo đoạn

  public void insertUpload(UUID orgId, UUID id, UUID studentId, String contentType, int size) {
    jdbc.sql(
            """
            INSERT INTO submission_upload (id, org_id, student_id, content_type, size_bytes)
            VALUES (:id, :orgId, :s, :ct, :n)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("s", studentId)
        .param("ct", contentType)
        .param("n", size)
        .update();
  }

  public Optional<Upload> upload(UUID orgId, UUID studentId, UUID id) {
    return jdbc.sql(
            """
            SELECT u.id, u.student_id, u.content_type, u.size_bytes, COALESCE(length(u.data), 0) AS received
            FROM submission_upload u
            WHERE u.org_id = :orgId AND u.id = :id AND u.student_id = :s LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("s", studentId)
        .query((rs, i) ->
            new Upload(
                rs.getObject("id", UUID.class),
                rs.getObject("student_id", UUID.class),
                rs.getString("content_type"),
                rs.getInt("size_bytes"),
                rs.getInt("received")))
        .optional();
  }

  /** Nối một đoạn đúng vị trí; 0 = lệch vị trí hoặc vượt cỡ đã khai. */
  public int appendUpload(UUID orgId, UUID id, int offset, byte[] chunk) {
    return jdbc.sql(
            """
            UPDATE submission_upload
            SET data = CASE WHEN :pos = 0 THEN :chunk ELSE data || :chunk END
            WHERE org_id = :orgId AND id = :id
              AND COALESCE(length(data), 0) = :pos
              AND COALESCE(length(data), 0) + length(:chunk) <= size_bytes
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("pos", offset)
        .param("chunk", chunk)
        .update();
  }

  public byte[] uploadBytes(UUID orgId, UUID id) {
    return jdbc.sql(
            "SELECT u.data FROM submission_upload u WHERE u.org_id = :orgId AND u.id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) -> rs.getBytes(1))
        .optional()
        .orElse(null);
  }

  public void deleteUpload(UUID orgId, UUID id) {
    jdbc.sql("DELETE FROM submission_upload WHERE org_id = :orgId AND id = :id")
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  // ------------------------------------------------------------------ bài nộp, phiên bản

  /** Khóa theo bài có chấm trong giao dịch (cấp mã BN-nn không trùng khi nộp dồn). */
  public void lockAssignment(UUID orgId, UUID assignmentId) {
    jdbc.sql(
            "SELECT pg_advisory_xact_lock(hashtext(CAST(:orgId AS text)), hashtext(CAST(:a AS text)))")
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query((rs, i) -> 1)
        .list();
  }

  public String nextCode(UUID orgId, UUID assignmentId) {
    Integer max =
        jdbc.sql(
                """
                SELECT max(CAST(substr(s.code, 4) AS int)) FROM submission s
                WHERE s.org_id = :orgId AND s.assignment_id = :a LIMIT 1
                """)
            .param("orgId", orgId)
            .param("a", assignmentId)
            .query(Integer.class)
            .optional()
            .orElse(null);
    return String.format("BN-%02d", max == null ? 1 : max + 1);
  }

  private static final String SUB_COLS =
      "s.id, s.assignment_id, s.student_id, s.class_id, s.code, s.current_version, s.academic_year";

  private static Sub sub(ResultSet rs, int i) throws SQLException {
    return new Sub(
        rs.getObject("id", UUID.class),
        rs.getObject("assignment_id", UUID.class),
        rs.getObject("student_id", UUID.class),
        rs.getObject("class_id", UUID.class),
        rs.getString("code"),
        rs.getInt("current_version"),
        rs.getString("academic_year"));
  }

  public Optional<Sub> submission(UUID orgId, UUID id) {
    return jdbc.sql(
            "SELECT "
                + SUB_COLS
                + " FROM submission s WHERE s.org_id = :orgId AND s.id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", id)
        .query(SubmissionRepository::sub)
        .optional();
  }

  public Optional<Sub> ofStudent(UUID orgId, UUID assignmentId, UUID studentId) {
    return jdbc.sql(
            "SELECT "
                + SUB_COLS
                + " FROM submission s WHERE s.org_id = :orgId AND s.assignment_id = :a"
                + " AND s.student_id = :st LIMIT 1")
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("st", studentId)
        .query(SubmissionRepository::sub)
        .optional();
  }

  public int countOf(UUID orgId, UUID assignmentId, UUID studentId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM submission s
            WHERE s.org_id = :orgId AND s.assignment_id = :a AND (CAST(:st AS uuid) IS NULL OR s.student_id = :st)
            LIMIT 1
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("st", studentId)
        .query(Integer.class)
        .single();
  }

  public void insertSubmission(
      UUID orgId,
      UUID id,
      UUID assignmentId,
      UUID studentId,
      UUID classId,
      String code,
      String year) {
    jdbc.sql(
            """
            INSERT INTO submission (id, org_id, assignment_id, student_id, class_id, code, current_version, academic_year)
            VALUES (:id, :orgId, :a, :st, :c, :code, 1, :y)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("st", studentId)
        .param("c", classId)
        .param("code", code)
        .param("y", year)
        .update();
  }

  public void setCurrentVersion(UUID orgId, UUID id, int version) {
    jdbc.sql(
            """
            UPDATE submission SET current_version = :v, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("v", version)
        .update();
  }

  public void insertVersion(
      UUID orgId,
      UUID id,
      UUID submissionId,
      UUID studentId,
      int version,
      String key,
      int pages,
      Instant at,
      Integer lateSeconds,
      String year) {
    jdbc.sql(
            """
            INSERT INTO submission_version (id, org_id, submission_id, student_id, version, idempotency_key, pages,
                                            submitted_at, late_seconds, academic_year)
            VALUES (:id, :orgId, :sub, :st, :v, :k, :p, :at, :late, :y)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("st", studentId)
        .param("v", version)
        .param("k", key)
        .param("p", pages)
        .param("at", Timestamp.from(at))
        .param("late", lateSeconds)
        .param("y", year)
        .update();
  }

  private static final String VER_COLS =
      "v.id, v.submission_id, v.version, v.idempotency_key, v.pages, v.submitted_at, v.late_seconds";

  private static Version version(ResultSet rs, int i) throws SQLException {
    int raw = rs.getInt("late_seconds");
    Integer late = rs.wasNull() ? null : raw;
    return new Version(
        rs.getObject("id", UUID.class),
        rs.getObject("submission_id", UUID.class),
        rs.getInt("version"),
        rs.getString("idempotency_key"),
        rs.getInt("pages"),
        rs.getTimestamp("submitted_at").toInstant(),
        late);
  }

  public Optional<Version> byKey(UUID orgId, UUID studentId, String key) {
    return jdbc.sql(
            "SELECT "
                + VER_COLS
                + " FROM submission_version v WHERE v.org_id = :orgId AND v.student_id = :st"
                + " AND v.idempotency_key = :k LIMIT 1")
        .param("orgId", orgId)
        .param("st", studentId)
        .param("k", key)
        .query(SubmissionRepository::version)
        .optional();
  }

  public List<Version> versions(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            "SELECT "
                + VER_COLS
                + " FROM submission_version v WHERE v.org_id = :orgId AND v.submission_id = :sub"
                + " ORDER BY v.version LIMIT 100")
        .param("orgId", orgId)
        .param("sub", submissionId)
        .query(SubmissionRepository::version)
        .list();
  }

  public Optional<Version> version(UUID orgId, UUID submissionId, int version) {
    return jdbc.sql(
            "SELECT "
                + VER_COLS
                + " FROM submission_version v WHERE v.org_id = :orgId AND v.submission_id = :sub"
                + " AND v.version = :v LIMIT 1")
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("v", version)
        .query(SubmissionRepository::version)
        .optional();
  }

  /** Ảnh các trang của một phiên bản (stored_image.submission_id = id phiên bản). */
  public List<Page> pages(UUID orgId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT i.page_no, i.kind, i.content_type, i.sha256, i.bytes FROM stored_image i
            WHERE i.org_id = :orgId AND i.submission_id = :v ORDER BY i.page_no, i.kind DESC LIMIT 40
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .query((rs, i) ->
            new Page(
                rs.getInt("page_no"),
                rs.getString("kind"),
                rs.getString("content_type"),
                rs.getString("sha256"),
                rs.getInt("bytes")))
        .list();
  }

  /** Ghi ảnh (gốc HEIC hoặc phái sinh JPEG) của một trang; ảnh JPEG/PNG gốc đi qua ImageStore. */
  public void insertImage(
      UUID orgId,
      UUID id,
      UUID assignmentId,
      UUID versionId,
      int pageNo,
      String kind,
      UUID studentId,
      UUID classId,
      String year,
      String contentType,
      String sha256,
      int bytes,
      String objectKey) {
    jdbc.sql(
            """
            INSERT INTO stored_image (id, org_id, assignment_id, submission_id, page_no, kind, student_id, class_id,
                                      academic_year, content_type, sha256, bytes, object_key)
            VALUES (:id, :orgId, :a, :sub, :p, :kind, :st, :c, :y, :ct, :sha, :n, :key)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("sub", versionId)
        .param("p", pageNo)
        .param("kind", kind)
        .param("st", studentId)
        .param("c", classId)
        .param("y", year)
        .param("ct", contentType)
        .param("sha", sha256)
        .param("n", bytes)
        .param("key", objectKey)
        .update();
  }

  /** Khóa đối tượng của ảnh phái sinh một trang (nếu có). */
  public Optional<String> derivedKey(UUID orgId, UUID versionId, int pageNo) {
    return jdbc.sql(
            """
            SELECT i.object_key FROM stored_image i
            WHERE i.org_id = :orgId AND i.submission_id = :v AND i.page_no = :p AND i.kind = 'derived' LIMIT 1
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .param("p", pageNo)
        .query(String.class)
        .optional();
  }

  // ------------------------------------------------------------------ xoá (SubmissionDataEraser)

  public int deleteUploadsOf(UUID orgId, UUID studentId) {
    return jdbc.sql("DELETE FROM submission_upload WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  /**
   * Bỏ khóa lũy đẳng (sinh trên máy) của học sinh đã xoá tài khoản; giữ khung bài nộp cho điểm ẩn
   * danh.
   */
  public int scrubVersionsOf(UUID orgId, UUID studentId) {
    return jdbc.sql(
            """
            UPDATE submission_version SET idempotency_key = CAST(id AS text)
            WHERE org_id = :orgId AND student_id = :st AND idempotency_key <> CAST(id AS text)
            """)
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  public int deleteAcksOf(UUID orgId, UUID studentId) {
    return jdbc.sql("DELETE FROM submission_privacy_ack WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  /** Bài nộp của năm học đã kết thúc (phiên bản, bản đọc chữ xoá theo khoá ngoại). */
  public int deleteOfYear(String year) {
    return jdbc.sql(
            """
            DELETE FROM submission WHERE academic_year = :y
            -- sqllint:allow=tenant job xoá theo năm học chạy xuyên trường (BR-S5E4-28b)
            """)
        .param("y", year)
        .update();
  }

  /** Số dòng dữ liệu bài nộp (móc kiểm thử TC-S5E4-43/44). */
  public java.util.Map<String, Object> state(UUID orgId, UUID submissionId, UUID studentId) {
    return jdbc.sql(
            """
            SELECT (SELECT count(*) FROM submission s WHERE s.org_id = :orgId AND s.id = :sub) AS submissions,
                   (SELECT count(*) FROM submission_version v WHERE v.org_id = :orgId AND v.submission_id = :sub
                      AND v.idempotency_key <> CAST(v.id AS text)) AS keyed_versions,
                   (SELECT count(*) FROM submission_version v WHERE v.org_id = :orgId AND v.submission_id = :sub)
                      AS versions,
                   (SELECT count(*) FROM ocr_artifact o WHERE o.org_id = :orgId AND o.submission_id = :sub) AS ocr,
                   (SELECT count(*) FROM submission_upload u WHERE u.org_id = :orgId AND u.student_id = :st) AS uploads,
                   (SELECT count(*) FROM submission_privacy_ack a WHERE a.org_id = :orgId AND a.student_id = :st)
                      AS acks
            """)
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("st", studentId)
        .query((rs, i) -> {
          java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
          for (String k :
              List.of("submissions", "keyed_versions", "versions", "ocr", "uploads", "acks")) {
            m.put(k, rs.getInt(k));
          }
          return m;
        })
        .single();
  }

  // ------------------------------------------------------------------ móc kiểm thử

  /** Dời hạn nộp của bài (móc kiểm thử: nộp muộn / quá hạn theo giờ máy chủ). */
  public int setDue(UUID orgId, UUID assignmentId, Instant dueAt, Boolean allowLate) {
    return jdbc.sql(
            """
            UPDATE graded_assignment SET due_at = :d, allow_late = COALESCE(:late, allow_late), updated_at = now()
            WHERE org_id = :orgId AND id = :a
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("d", Timestamp.from(dueAt))
        .param("late", allowLate)
        .update();
  }
}
