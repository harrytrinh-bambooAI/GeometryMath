package vn.geo.server.assessment.internal.storage.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL kho ảnh bài làm (US-S5E4-08). Đường theo người dùng lọc {@code org_id = :orgId}; hai job xoá
 * (theo tài khoản, theo năm học) chạy xuyên trường có đánh dấu ngoại lệ kèm lý do.
 */
@Repository
public class StoredImageRepository {

  /** Một ảnh (không có nội dung). */
  public record Image(
      UUID id,
      UUID orgId,
      UUID assignmentId,
      UUID submissionId,
      int pageNo,
      UUID studentId,
      UUID classId,
      String schoolYear,
      String contentType,
      String sha256,
      int bytes,
      String objectKey) {}

  /** Bài có chấm: lớp. */
  public record AssignmentRef(UUID id, UUID classId) {}

  private static final String COLS =
      "s.id, s.org_id, s.assignment_id, s.submission_id, s.page_no, s.student_id, s.class_id, s.academic_year,"
          + " s.content_type, s.sha256, s.bytes, s.object_key";

  private final JdbcClient jdbc;

  public StoredImageRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<AssignmentRef> assignment(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            "SELECT g.id, g.class_id FROM graded_assignment g WHERE g.org_id = :orgId AND g.id = :a")
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(
            (rs, i) -> new AssignmentRef(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class)))
        .optional();
  }

  public Optional<Image> page(UUID orgId, UUID submissionId, int pageNo) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM stored_image s WHERE s.org_id = :orgId AND s.submission_id = :sub"
                + " AND s.page_no = :p AND s.kind = 'original'")
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("p", pageNo)
        .query(StoredImageRepository::map)
        .optional();
  }

  public Optional<Image> byId(UUID orgId, UUID id) {
    return jdbc.sql(
            "SELECT " + COLS + " FROM stored_image s WHERE s.org_id = :orgId AND s.id = :id")
        .param("orgId", orgId)
        .param("id", id)
        .query(StoredImageRepository::map)
        .optional();
  }

  public List<Image> ofSubmission(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM stored_image s WHERE s.org_id = :orgId AND s.submission_id = :sub LIMIT 200")
        .param("orgId", orgId)
        .param("sub", submissionId)
        .query(StoredImageRepository::map)
        .list();
  }

  /** Giáo viên chủ lớp của ảnh. */
  public Optional<UUID> classTeacher(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.teacher_id FROM class c WHERE c.org_id = :orgId AND c.id = :c
            -- sqllint:allow=ownership giáo viên chủ lớp (iam) để cấp URL ký ảnh (BR-S5E4-26)
            """)
        .param("orgId", orgId)
        .param("c", classId)
        .query(UUID.class)
        .optional();
  }

  /** Ghi (hoặc thay) ảnh gốc một trang; trả id. */
  public UUID upsert(
      UUID orgId,
      UUID id,
      UUID assignmentId,
      UUID submissionId,
      int pageNo,
      UUID studentId,
      UUID classId,
      String schoolYear,
      String contentType,
      String sha256,
      int bytes,
      String objectKey) {
    jdbc.sql(
            """
            INSERT INTO stored_image (id, org_id, assignment_id, submission_id, page_no, kind, student_id, class_id,
                                      academic_year, content_type, sha256, bytes, object_key)
            VALUES (:id, :orgId, :a, :sub, :p, 'original', :st, :c, :y, :ct, :sha, :n, :key)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("sub", submissionId)
        .param("p", pageNo)
        .param("st", studentId)
        .param("c", classId)
        .param("y", schoolYear)
        .param("ct", contentType)
        .param("sha", sha256)
        .param("n", bytes)
        .param("key", objectKey)
        .update();
    return id;
  }

  public void delete(UUID orgId, UUID id) {
    jdbc.sql("DELETE FROM stored_image WHERE org_id = :orgId AND id = :id")
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  /**
   * Ảnh của học sinh đã xoá tài khoản (job xuyên trường; {@code scope} = một bài có chấm, chỉ móc
   * kiểm thử; null = tất cả).
   */
  public List<Image> ofDeletedStudents(UUID scope, int limit) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """
                 FROM stored_image s
                WHERE (CAST(:scope AS uuid) IS NULL OR s.assignment_id = :scope)
                  AND EXISTS (SELECT 1 FROM app_user u WHERE u.org_id = s.org_id AND u.id = s.student_id
                              AND u.deleted_at IS NOT NULL LIMIT 1)
                ORDER BY s.org_id, s.student_id LIMIT :lim
                -- sqllint:allow=tenant job xoá theo tài khoản chạy xuyên trường (BR-S5E4-28a)
                -- sqllint:allow=ownership đọc cờ đã xoá của tài khoản (iam)
                """)
        .param("scope", scope)
        .param("lim", limit)
        .query(StoredImageRepository::map)
        .list();
  }

  /** Học sinh (org, id) của tài khoản đã xoá còn dữ liệu bài nộp chưa xoá. */
  public record StudentRef(UUID orgId, UUID studentId) {}

  /**
   * Học sinh đã xoá tài khoản còn phiên bản bài nộp chưa gỡ khoá lũy đẳng — kể cả khi không còn ảnh
   * nào (REVIEW-S5 #5, BR-S5E4-28a). {@code scope} khác null: chỉ bài của một bài có chấm.
   */
  public List<StudentRef> deletedStudentsWithData(UUID scope, int limit) {
    return jdbc.sql(
            """
            SELECT DISTINCT v.org_id, v.student_id FROM submission_version v
            WHERE v.idempotency_key <> CAST(v.id AS text)
              AND (CAST(:scope AS uuid) IS NULL OR EXISTS (SELECT 1 FROM submission s
                   WHERE s.org_id = v.org_id AND s.id = v.submission_id AND s.assignment_id = :scope LIMIT 1))
              AND EXISTS (SELECT 1 FROM app_user u WHERE u.org_id = v.org_id AND u.id = v.student_id
                          AND u.deleted_at IS NOT NULL LIMIT 1)
            LIMIT :lim
            -- sqllint:allow=tenant job xoá theo tài khoản chạy xuyên trường (BR-S5E4-28a)
            -- sqllint:allow=ownership đọc cờ đã xoá của tài khoản (iam)
            """)
        .param("scope", scope)
        .param("lim", limit)
        .query((rs, n) ->
            new StudentRef(
                rs.getObject("org_id", UUID.class), rs.getObject("student_id", UUID.class)))
        .list();
  }

  /** Ảnh của các năm học kết thúc không muộn hơn năm {@code endYear} (job 31/07). */
  public List<Image> ofEndedYears(int endYear, UUID scope, int limit) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """
                 FROM stored_image s
                WHERE split_part(s.academic_year, '-', 2)::int <= :e
                  AND (CAST(:scope AS uuid) IS NULL OR s.assignment_id = :scope)
                ORDER BY s.org_id LIMIT :lim
                -- sqllint:allow=tenant job xoá theo năm học chạy xuyên trường (BR-S5E4-28b)
                """)
        .param("e", endYear)
        .param("scope", scope)
        .param("lim", limit)
        .query(StoredImageRepository::map)
        .list();
  }

  public void audit(UUID orgId, String action, UUID targetId, String detail, int n) {
    jdbc.sql(
            """
            INSERT INTO assessment_audit (id, org_id, actor_id, action, target_id, detail, n)
            VALUES (:id, :orgId, NULL, :a, :t, :d, :n)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("a", action)
        .param("t", targetId)
        .param("d", detail)
        .param("n", n)
        .update();
  }

  /** MÓC KIỂM THỬ: AuditEvent xoá ảnh của một trường. */
  public List<java.util.Map<String, Object>> audits(UUID orgId) {
    return jdbc.sql(
            """
            SELECT a.action, a.target_id, a.detail, a.n FROM assessment_audit a
            WHERE a.org_id = :orgId ORDER BY a.at, a.id LIMIT 100
            """)
        .param("orgId", orgId)
        .query((rs, i) -> {
          java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
          m.put("action", rs.getString(1));
          m.put("targetId", rs.getObject(2, UUID.class));
          m.put("detail", rs.getString(3));
          m.put("n", rs.getInt(4));
          return m;
        })
        .list();
  }

  /** MÓC KIỂM THỬ: số ảnh còn lại của một trường theo năm học hoặc bài nộp. */
  public int count(UUID orgId, String schoolYear, UUID submissionId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM stored_image s WHERE s.org_id = :orgId
              AND (CAST(:y AS text) IS NULL OR s.academic_year = :y)
              AND (CAST(:sub AS uuid) IS NULL OR s.submission_id = :sub)
              AND (CAST(:a AS uuid) IS NULL OR s.assignment_id = :a)
            """)
        .param("a", assignmentId)
        .param("orgId", orgId)
        .param("y", schoolYear)
        .param("sub", submissionId)
        .query(Integer.class)
        .single();
  }

  private static Image map(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
    return new Image(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getObject(4, UUID.class),
        rs.getInt(5),
        rs.getObject(6, UUID.class),
        rs.getObject(7, UUID.class),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        rs.getInt(11),
        rs.getString(12));
  }
}
