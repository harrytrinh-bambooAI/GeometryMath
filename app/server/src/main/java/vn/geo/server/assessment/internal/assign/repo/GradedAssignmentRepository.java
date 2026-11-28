package vn.geo.server.assessment.internal.assign.repo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL bài có chấm (US-S5E4-02). Mọi câu lệnh lọc {@code org_id = :orgId} với mã trường đã xác thực
 * do tầng dịch vụ truyền vào.
 */
@Repository
public class GradedAssignmentRepository {

  /** Lớp: tên, giáo viên chủ. */
  public record ClassRow(UUID id, String name, UUID teacherId) {}

  /** Một bài có chấm (kèm mã và tên rubric, phiên bản). */
  public record Row(
      UUID id,
      UUID teacherId,
      UUID classId,
      String className,
      String code,
      String title,
      UUID problemVersionId,
      UUID examSetId,
      String examCode,
      UUID rubricVersionId,
      UUID rubricId,
      String rubricCode,
      int rubricVersion,
      String rubricName,
      BigDecimal scale,
      Instant dueAt,
      boolean allowLate,
      String gradingMode,
      String releaseMode,
      Instant releaseAt,
      String status,
      Instant createdAt) {}

  /** Thân ghi mới. */
  public record NewRow(
      UUID teacherId,
      UUID classId,
      String code,
      String title,
      UUID problemVersionId,
      UUID examSetId,
      String examCode,
      UUID rubricVersionId,
      BigDecimal scale,
      Instant dueAt,
      boolean allowLate,
      String gradingMode,
      String releaseMode,
      Instant releaseAt) {}

  private static final String SELECT =
      """
      -- sqllint:allow=ownership tên lớp (iam) kèm bài có chấm
      SELECT g.id, g.teacher_id, g.class_id, c.name, g.code, g.title, g.problem_version_id, g.exam_set_id, g.exam_code,
             g.rubric_version_id, r.id, r.code, v.version, v.name, g.scale, g.due_at, g.allow_late, g.grading_mode,
             g.release_mode, g.release_at, g.status, g.created_at
      FROM graded_assignment g
      JOIN rubric_version v ON v.org_id = :orgId AND v.id = g.rubric_version_id
      JOIN rubric r ON r.org_id = :orgId AND r.id = v.rubric_id
      JOIN class c ON c.org_id = :orgId AND c.id = g.class_id
      """;

  private final JdbcClient jdbc;

  public GradedAssignmentRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<ClassRow> classOf(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name, c.teacher_id FROM class c
            WHERE c.org_id = :orgId AND c.id = :classId AND c.archived_at IS NULL
            -- sqllint:allow=ownership đọc lớp (iam): tên, giáo viên chủ để kiểm quan hệ giao bài (US-S5E4-02-FC3)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) ->
            new ClassRow(rs.getObject(1, UUID.class), rs.getString(2), rs.getObject(3, UUID.class)))
        .optional();
  }

  public String nextCode(UUID orgId, UUID teacherId) {
    Integer max =
        jdbc.sql(
                """
                SELECT coalesce(max(substring(g.code FROM 4)::int), 0) FROM graded_assignment g
                WHERE g.org_id = :orgId AND g.teacher_id = :t
                """)
            .param("orgId", orgId)
            .param("t", teacherId)
            .query(Integer.class)
            .single();
    return String.format("BC-%02d", max + 1);
  }

  public UUID insert(UUID orgId, NewRow n) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO graded_assignment (id, org_id, teacher_id, class_id, code, title, problem_version_id, exam_set_id,
                                           exam_code, rubric_version_id, scale, due_at, allow_late, grading_mode,
                                           release_mode, release_at)
            VALUES (:id, :orgId, :t, :c, :code, :title, :pv, :es, :ec, :rv, :scale, :due, :late, :mode, :rmode, :rat)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("t", n.teacherId())
        .param("c", n.classId())
        .param("code", n.code())
        .param("title", n.title())
        .param("pv", n.problemVersionId())
        .param("es", n.examSetId())
        .param("ec", n.examCode())
        .param("rv", n.rubricVersionId())
        .param("scale", n.scale())
        .param("due", Timestamp.from(n.dueAt()))
        .param("late", n.allowLate())
        .param("mode", n.gradingMode())
        .param("rmode", n.releaseMode())
        .param("rat", n.releaseAt() == null ? null : Timestamp.from(n.releaseAt()))
        .update();
    return id;
  }

  public Optional<Row> one(UUID orgId, UUID id) {
    return jdbc.sql(SELECT + " WHERE g.org_id = :orgId AND g.id = :id")
        .param("orgId", orgId)
        .param("id", id)
        .query(GradedAssignmentRepository::map)
        .optional();
  }

  /** Khoá hàng bài để sửa tuần tự. */
  public void lock(UUID orgId, UUID id) {
    jdbc.sql(
            "SELECT g.id FROM graded_assignment g WHERE g.org_id = :orgId AND g.id = :id FOR UPDATE")
        .param("orgId", orgId)
        .param("id", id)
        .query(UUID.class)
        .optional();
  }

  public List<Row> ofTeacher(UUID orgId, UUID teacherId, UUID classId) {
    return jdbc.sql(
            SELECT
                + """
                 WHERE g.org_id = :orgId AND g.teacher_id = :t AND (CAST(:c AS uuid) IS NULL OR g.class_id = :c)
                ORDER BY g.due_at DESC, g.id DESC LIMIT 200
                -- sqllint:allow=ownership tên lớp (iam) trong danh sách bài có chấm
                """)
        .param("orgId", orgId)
        .param("t", teacherId)
        .param("c", classId)
        .query(GradedAssignmentRepository::map)
        .list();
  }

  /** "Bài cần nộp" của học sinh: bài của các lớp em đang học. */
  public List<Row> ofStudent(UUID orgId, UUID studentId) {
    return jdbc.sql(
            SELECT
                + """
                 WHERE g.org_id = :orgId
                  AND EXISTS (SELECT 1 FROM class_member m WHERE m.org_id = :orgId AND m.class_id = g.class_id
                              AND m.user_id = :u AND m.status = 'active' LIMIT 1)
                ORDER BY g.due_at, g.id LIMIT 200
                -- sqllint:allow=ownership lớp và thành viên lớp (iam) để lọc bài theo quan hệ học sinh (US-S5E4-02-SC1)
                """)
        .param("orgId", orgId)
        .param("u", studentId)
        .query(GradedAssignmentRepository::map)
        .list();
  }

  public boolean activeMember(UUID orgId, UUID classId, UUID studentId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class_member m WHERE m.org_id = :orgId AND m.class_id = :c
                           AND m.user_id = :u AND m.status = 'active' LIMIT 1)
            -- sqllint:allow=ownership thành viên lớp (iam) để kiểm quan hệ học sinh
            """)
        .param("orgId", orgId)
        .param("c", classId)
        .param("u", studentId)
        .query(Boolean.class)
        .single();
  }

  /** Đề bài (chỉ câu chữ, không lời giải) của version đề kho đã kiểm quyền bộ đề. */
  public Optional<String> statement(UUID orgId, UUID problemVersionId) {
    return jdbc.sql(
            """
            SELECT pv.statement FROM problem_version pv WHERE pv.id = :pv
              AND (pv.org_id = :orgId OR pv.org_id = platform_org_id()) LIMIT 1
            -- sqllint:allow=ownership câu chữ đề (content) để học sinh làm bài; không đọc lời giải
            -- sqllint:allow=tenant đề kho thuộc tổ chức nền tảng, quyền bộ đề đã kiểm qua content.api.BankAccess
            """)
        .param("orgId", orgId)
        .param("pv", problemVersionId)
        .query(String.class)
        .optional();
  }

  /**
   * Số bài nộp đã có của một bài = số hàng {@code submission} (nộp lại không tăng; T-261128-60,
   * Dev-S5E4-08) + ảnh của móc kiểm thử T-57 không gắn phiên bản bài nộp nào (giữ ca cũ của móc).
   */
  public int submissions(UUID orgId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT (SELECT count(*) FROM submission b WHERE b.org_id = :orgId AND b.assignment_id = :a)
                 + (SELECT count(DISTINCT s.submission_id) FROM stored_image s
                    WHERE s.org_id = :orgId AND s.assignment_id = :a
                      AND NOT EXISTS (SELECT 1 FROM submission_version v
                                      WHERE v.org_id = :orgId AND v.id = s.submission_id))
            """)
        .param("orgId", orgId)
        .param("a", assignmentId)
        .query(Integer.class)
        .single();
  }

  public void updateDue(UUID orgId, UUID id, Instant dueAt) {
    jdbc.sql(
            """
            UPDATE graded_assignment SET due_at = :due, updated_at = now() WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("due", Timestamp.from(dueAt))
        .update();
  }

  public void updateRubric(UUID orgId, UUID id, UUID rubricVersionId, BigDecimal scale) {
    jdbc.sql(
            """
            UPDATE graded_assignment SET rubric_version_id = :rv, scale = :scale, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("rv", rubricVersionId)
        .param("scale", scale)
        .update();
  }

  public void updatePolicy(
      UUID orgId, UUID id, boolean allowLate, String releaseMode, Instant releaseAt) {
    jdbc.sql(
            """
            UPDATE graded_assignment SET allow_late = :late, release_mode = :rmode, release_at = :rat, updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("late", allowLate)
        .param("rmode", releaseMode)
        .param("rat", releaseAt == null ? null : Timestamp.from(releaseAt))
        .update();
  }

  private static Row map(ResultSet rs, int i) throws SQLException {
    return new Row(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getObject(7, UUID.class),
        rs.getObject(8, UUID.class),
        rs.getString(9),
        rs.getObject(10, UUID.class),
        rs.getObject(11, UUID.class),
        rs.getString(12),
        rs.getInt(13),
        rs.getString(14),
        rs.getBigDecimal(15).stripTrailingZeros(),
        instant(rs, 16),
        rs.getBoolean(17),
        rs.getString(18),
        rs.getString(19),
        instant(rs, 20),
        rs.getString(21),
        instant(rs, 22));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
