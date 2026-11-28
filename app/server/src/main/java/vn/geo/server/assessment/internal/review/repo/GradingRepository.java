package vn.geo.server.assessment.internal.review.repo;

import java.math.BigDecimal;
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
 * Dữ liệu hàng chờ chấm, điểm duyệt, nhật ký chấm, lượt trả bài (T-261128-60; US-S5E4-05, 06, 07;
 * V705). Bảng {@code submission}, {@code submission_version}, {@code ocr_artifact} (T-59) đọc cùng
 * module.
 */
@Repository
public class GradingRepository {

  /** Một bài nộp ở phiên bản chấm (current_version), kèm trạng thái chấm hiệu lực. */
  public record Item(
      UUID id,
      String code,
      UUID assignmentId,
      UUID classId,
      UUID studentId,
      String studentName,
      String state,
      int rowVersion,
      UUID versionId,
      int version,
      int pages,
      Instant submittedAt,
      Integer lateSeconds,
      String ocr,
      String academicYear) {}

  /** Một điểm duyệt. */
  public record Grade(
      UUID id,
      int seq,
      int submissionVersion,
      UUID rubricVersionId,
      String criteria,
      BigDecimal total,
      String comment,
      UUID approvedBy,
      Instant approvedAt) {}

  /** Một dòng nhật ký chấm. */
  public record Audit(
      String action,
      UUID actorId,
      String actorName,
      String before,
      String after,
      int n,
      Instant at) {}

  /** Lượt trả bài. */
  public record Return(UUID id, Instant returnedAt, int n, Instant undoneAt) {}

  private static final String ITEM =
      """
      -- sqllint:allow=ownership tên học sinh (iam) trên hàng chờ của giáo viên chủ lớp
      SELECT s.id, s.code, s.assignment_id, s.class_id, s.student_id, u.display_name,
             CASE WHEN s.grading_state = 'approved' AND g.release_mode = 'at_time' AND g.release_at <= now()
                  THEN 'returned' ELSE s.grading_state END AS state,
             s.row_version, v.id AS version_id, v.version, v.pages, v.submitted_at, v.late_seconds,
             (SELECT o.status FROM ocr_artifact o WHERE o.org_id = :orgId AND o.version_id = v.id
              ORDER BY o.version DESC LIMIT 1) AS ocr,
             s.academic_year
      FROM submission s
      JOIN graded_assignment g ON g.org_id = :orgId AND g.id = s.assignment_id
      JOIN submission_version v ON v.org_id = :orgId AND v.submission_id = s.id AND v.version = s.current_version
      LEFT JOIN app_user u ON u.org_id = :orgId AND u.id = s.student_id
      """;

  private final JdbcClient jdbc;

  public GradingRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  private static Instant ts(ResultSet rs, String col) throws SQLException {
    Timestamp t = rs.getTimestamp(col);
    return t == null ? null : t.toInstant();
  }

  private static Item item(ResultSet rs, int i) throws SQLException {
    int late = rs.getInt("late_seconds");
    boolean onTime = rs.wasNull();
    return new Item(
        rs.getObject("id", UUID.class),
        rs.getString("code"),
        rs.getObject("assignment_id", UUID.class),
        rs.getObject("class_id", UUID.class),
        rs.getObject("student_id", UUID.class),
        rs.getString("display_name"),
        rs.getString("state"),
        rs.getInt("row_version"),
        rs.getObject("version_id", UUID.class),
        rs.getInt("version"),
        rs.getInt("pages"),
        ts(rs, "submitted_at"),
        onTime ? null : late,
        rs.getString("ocr"),
        rs.getString("academic_year"));
  }

  /** Mọi bài nộp của một bài có chấm (≤ 500 — một lớp). */
  public List<Item> items(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            ITEM
                + """
                 WHERE s.org_id = :orgId AND s.assignment_id = :a
                ORDER BY v.submitted_at, s.id LIMIT 500
                """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(GradingRepository::item)
        .list();
  }

  public Optional<Item> item(UUID orgId, UUID submissionId) {
    return jdbc.sql(ITEM + " WHERE s.org_id = :orgId AND s.id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", submissionId)
        .query(GradingRepository::item)
        .optional();
  }

  /** Học sinh Đang học của lớp chưa có bài nộp cho bài này. */
  public int notSubmitted(UUID orgId, UUID classId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM class_member m
            WHERE m.org_id = :orgId AND m.class_id = :c AND m.status = 'active'
              AND NOT EXISTS (SELECT 1 FROM submission s WHERE s.org_id = :orgId AND s.assignment_id = :a
                              AND s.student_id = m.user_id)
            LIMIT 1
            -- sqllint:allow=ownership thành viên Đang học của lớp (iam) để đếm em chưa nộp (US-S5E4-05-SC1)
            """)
        .param("orgId", orgId)
        .param("c", classId)
        .param("a", assignmentId)
        .query(Integer.class)
        .single();
  }

  /** Khoá lạc quan (BR-S5E4-19): chỉ đổi khi row_version khớp; trả số hàng đổi (0 = CONFLICT). */
  public int approve(UUID orgId, UUID submissionId, int rowVersion) {
    return jdbc.sql(
            """
            UPDATE submission SET row_version = row_version + 1, updated_at = now(),
              grading_state = CASE WHEN grading_state = 'returned' THEN 'returned' ELSE 'approved' END
            WHERE org_id = :orgId AND id = :id AND row_version = :rv
            """)
        .param("orgId", orgId)
        .param("id", submissionId)
        .param("rv", rowVersion)
        .update();
  }

  private static Grade grade(ResultSet rs, int i) throws SQLException {
    return new Grade(
        rs.getObject("id", UUID.class),
        rs.getInt("seq"),
        rs.getInt("submission_version"),
        rs.getObject("rubric_version_id", UUID.class),
        rs.getString("criteria"),
        rs.getBigDecimal("total"),
        rs.getString("comment"),
        rs.getObject("approved_by", UUID.class),
        ts(rs, "approved_at"));
  }

  public Optional<Grade> latest(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT g.id, g.seq, g.submission_version, g.rubric_version_id, g.criteria::text AS criteria, g.total,
                   g.comment, g.approved_by, g.approved_at
            FROM approved_grade g WHERE g.org_id = :orgId AND g.submission_id = :s
            ORDER BY g.seq DESC LIMIT 1
            """)
        .param("orgId", orgId)
        .param("s", submissionId)
        .query(GradingRepository::grade)
        .optional();
  }

  public int countGrades(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            "SELECT count(*) FROM approved_grade g WHERE g.org_id = :orgId AND g.submission_id = :s")
        .param("orgId", orgId)
        .param("s", submissionId)
        .query(Integer.class)
        .single();
  }

  public void insertGrade(
      UUID orgId,
      UUID id,
      Item s,
      int seq,
      UUID rubricVersionId,
      String criteriaJson,
      BigDecimal total,
      String comment,
      UUID by,
      Instant at) {
    jdbc.sql(
            """
            INSERT INTO approved_grade (id, org_id, submission_id, assignment_id, class_id, student_id, seq,
              submission_version, rubric_version_id, criteria, total, comment, approved_by, approved_at, academic_year)
            VALUES (:id, :orgId, :s, :a, :c, :st, :seq, :sv, :rv, CAST(:cr AS jsonb), :t, :cm, :by, :at, :y)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("s", s.id())
        .param("a", s.assignmentId())
        .param("c", s.classId())
        .param("st", s.studentId())
        .param("seq", seq)
        .param("sv", s.version())
        .param("rv", rubricVersionId)
        .param("cr", criteriaJson)
        .param("t", total)
        .param("cm", comment)
        .param("by", by)
        .param("at", Timestamp.from(at))
        .param("y", s.academicYear())
        .update();
  }

  public void audit(
      UUID orgId,
      UUID id,
      UUID assignmentId,
      UUID submissionId,
      UUID actor,
      String action,
      String before,
      String after,
      int n,
      Instant at) {
    jdbc.sql(
            """
            INSERT INTO grade_audit (id, org_id, assignment_id, submission_id, actor_id, action, before_value,
              after_value, n, at)
            VALUES (:id, :orgId, :a, :s, :actor, :act, CAST(:b AS jsonb), CAST(:af AS jsonb), :n, :at)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("s", submissionId)
        .param("actor", actor)
        .param("act", action)
        .param("b", before)
        .param("af", after)
        .param("n", n)
        .param("at", Timestamp.from(at))
        .update();
  }

  public List<Audit> audits(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT a.action, a.actor_id, u.display_name, a.before_value::text AS b, a.after_value::text AS af, a.n, a.at
            FROM grade_audit a LEFT JOIN app_user u ON u.org_id = :orgId AND u.id = a.actor_id
            WHERE a.org_id = :orgId AND a.submission_id = :s ORDER BY a.at, a.id LIMIT 200
            -- sqllint:allow=ownership tên giáo viên thực hiện (iam) trên nhật ký chấm
            """)
        .param("orgId", orgId)
        .param("s", submissionId)
        .query((rs, i) ->
            new Audit(
                rs.getString(1),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getInt(6),
                ts(rs, "at")))
        .list();
  }

  // ------------------------------------------------------------------ trả bài

  /** Bài Đã duyệt → Đã trả theo lượt; trả mã học sinh đã được trả. */
  public List<UUID> returnApproved(UUID orgId, UUID assignmentId, UUID returnId, Instant at) {
    return jdbc.sql(
            """
            UPDATE submission SET grading_state = 'returned', return_id = :r, returned_at = :at, updated_at = now()
            WHERE org_id = :orgId AND assignment_id = :a AND grading_state = 'approved'
            RETURNING student_id
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("r", returnId)
        .param("at", Timestamp.from(at))
        .query(UUID.class)
        .list();
  }

  public int undoReturn(UUID orgId, UUID returnId) {
    return jdbc.sql(
            """
            UPDATE submission SET grading_state = 'approved', return_id = NULL, returned_at = NULL, updated_at = now()
            WHERE org_id = :orgId AND return_id = :r AND grading_state = 'returned'
            """)
        .param("orgId", orgId)
        .param("r", returnId)
        .update();
  }

  public int countReturned(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM submission s
            WHERE s.org_id = :orgId AND s.assignment_id = :a AND s.grading_state = 'returned'
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(Integer.class)
        .single();
  }

  public void setAssignmentStatus(UUID orgId, UUID assignmentId, String status) {
    jdbc.sql(
            """
            UPDATE graded_assignment SET status = :st, updated_at = now()
            WHERE org_id = :orgId AND id = :a
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("st", status)
        .update();
  }

  public void insertReturn(UUID orgId, UUID id, UUID assignmentId, UUID by, Instant at, int n) {
    jdbc.sql(
            """
            INSERT INTO grade_return (id, org_id, assignment_id, returned_by, returned_at, n)
            VALUES (:id, :orgId, :a, :by, :at, :n)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("by", by)
        .param("at", Timestamp.from(at))
        .param("n", n)
        .update();
  }

  public Optional<Return> lastReturn(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT r.id, r.returned_at, r.n, r.undone_at FROM grade_return r
            WHERE r.org_id = :orgId AND r.assignment_id = :a
            ORDER BY r.returned_at DESC, r.id DESC LIMIT 1
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query((rs, i) ->
            new Return(
                rs.getObject(1, UUID.class),
                ts(rs, "returned_at"),
                rs.getInt(3),
                ts(rs, "undone_at")))
        .optional();
  }

  public void markUndone(UUID orgId, UUID returnId, Instant at) {
    jdbc.sql("UPDATE grade_return SET undone_at = :at WHERE org_id = :orgId AND id = :r")
        .param("orgId", orgId)
        .param("r", returnId)
        .param("at", Timestamp.from(at))
        .update();
  }

  /** Móc kiểm thử: lùi giờ của lượt trả bài gần nhất (TC-S5E4-38 biên 15 phút). */
  public int ageReturn(UUID orgId, UUID assignmentId, long seconds) {
    return jdbc.sql(
            """
            UPDATE grade_return SET returned_at = returned_at - make_interval(secs => :sec)
            WHERE org_id = :orgId AND id = (SELECT r.id FROM grade_return r WHERE r.org_id = :orgId
                                            AND r.assignment_id = :a ORDER BY r.returned_at DESC, r.id DESC LIMIT 1)
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("sec", (double) seconds)
        .update();
  }

  /** Móc kiểm thử: đặt giờ công bố cố định = giờ CSDL + n giây (TC-S5E4-33). */
  public int releaseIn(UUID orgId, UUID assignmentId, long seconds) {
    return jdbc.sql(
            """
            UPDATE graded_assignment SET release_mode = 'at_time', release_at = now() + make_interval(secs => :sec)
            WHERE org_id = :orgId AND id = :a
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .param("sec", (double) seconds)
        .update();
  }

  // ------------------------------------------------------------------ thông báo trong ứng dụng

  /**
   * Thông báo trong ứng dụng cho học sinh (MSG-S5-39 / sửa điểm). Bảng của module notify; module
   * notify chưa có cổng API cho chặng 5 nên ghi thẳng theo đúng khuôn hàng của
   * NotificationRepository.
   */
  public int notify(
      UUID orgId, UUID eventId, String kind, String body, List<UUID> users, Instant at) {
    int n = 0;
    for (UUID u : users) {
      if (u == null) {
        continue;
      }
      n +=
          jdbc.sql(
                  """
                  INSERT INTO notification (id, org_id, user_id, kind, event_id, body, created_at)
                  VALUES (:id, :orgId, :u, :kind, :e, :body, :at)
                  ON CONFLICT ON CONSTRAINT notification_event_user_uq DO NOTHING
                  -- sqllint:allow=ownership thông báo trong ứng dụng khi trả bài / sửa điểm (US-S5E4-07); notify chưa có cổng api
                  """)
              .param("id", vn.geo.server.common.id.Uuid7.next())
              .param("orgId", orgId)
              .param("u", u)
              .param("kind", kind)
              .param("e", eventId)
              .param("body", body)
              .param("at", Timestamp.from(at))
              .update();
    }
    return n;
  }

  public int unnotify(UUID orgId, UUID eventId) {
    return jdbc.sql(
            """
            DELETE FROM notification WHERE org_id = :orgId AND event_id = :e AND kind = 'graded_returned'
            -- sqllint:allow=ownership gỡ thông báo trả bài khi thu hồi trong 15 phút (BR-S5E4-24)
            """)
        .param("orgId", orgId)
        .param("e", eventId)
        .update();
  }

  // ------------------------------------------------------------------ lời giải, xoá

  /** Lời giải bài có chấm cho học sinh qua hàm cổng CSDL (geo_app không đọc bảng lời giải). */
  public record Solution(String status, String finalAnswer, String steps) {}

  public Solution solution(UUID orgId, UUID studentId, UUID assignmentId) {
    return jdbc.sql(
            "SELECT f.status, f.final_answer, f.steps::text FROM fn_graded_solution(:orgId, :st, :a) f")
        .param("orgId", orgId)
        .param("st", studentId)
        .param("a", assignmentId)
        .query((rs, i) -> new Solution(rs.getString(1), rs.getString(2), rs.getString(3)))
        .single();
  }

  /** Xoá tài khoản học sinh: điểm duyệt giữ dạng ẩn danh (BR-S5E4-28). */
  public int anonymize(UUID orgId, UUID studentId) {
    return jdbc.sql(
            "UPDATE approved_grade SET student_id = NULL WHERE org_id = :orgId AND student_id = :st")
        .param("orgId", orgId)
        .param("st", studentId)
        .update();
  }

  /** Móc kiểm thử: số điểm duyệt của bài nộp và số hàng còn gắn học sinh. */
  public int[] gradeState(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            SELECT count(*), count(g.student_id) FROM approved_grade g
            WHERE g.org_id = :orgId AND g.submission_id = :s
            """)
        .param("orgId", orgId)
        .param("s", submissionId)
        .query((rs, i) -> new int[] {rs.getInt(1), rs.getInt(2)})
        .single();
  }
}
