package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Bài giao. Đọc: mô hình đọc nối đề (nội dung nền tảng) và tiến độ (ADR-GEO-01: read model, đánh
 * dấu sqllint:allow=ownership). Ghi: chỉ qua hàm SECURITY DEFINER của V10 (D-GEO-158).
 */
@Repository
public class AssignmentRepository {

  /** Một dòng "Bài của em" / chi tiết bài của học sinh. */
  public record StudentRow(
      UUID id,
      String problemCode,
      String statement,
      String className,
      Instant releaseAt,
      String releaseState,
      int releaseRev,
      boolean allowHint,
      boolean allowQa,
      String verifyStatus,
      boolean opened,
      int hintLevel,
      boolean solutionViewed,
      String title,
      String solidKind,
      String topic) {}

  public record ClassAssignmentRow(
      UUID id, String problemCode, Instant releaseAt, String releaseState, int releaseRev) {}

  public record FnResult(String status, Integer releaseRev, Instant releaseAt) {}

  public record Solution(String status, String finalAnswer, String steps) {}

  private static final String STUDENT_SELECT =
      """
      SELECT a.id, p.code, pv.statement, c.name, a.release_at,
             CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END,
             a.release_rev, a.allow_hint, a.allow_qa, pv.verify_status::text,
             ap.first_opened_at IS NOT NULL, COALESCE(ap.hint_level_max, 0), ap.solution_viewed_at IS NOT NULL,
             p.title, p.solid_kind::text, t.name
      FROM class_member cm
      JOIN class c ON c.org_id = :orgId AND c.id = cm.class_id
      JOIN assignment a ON a.org_id = :orgId AND a.class_id = cm.class_id AND a.kind = 'class' AND a.archived_at IS NULL
      JOIN problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
      JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
      LEFT JOIN problem_topic t ON t.id = p.topic_id
      LEFT JOIN assignment_progress ap ON ap.org_id = :orgId AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
      WHERE cm.org_id = :orgId AND cm.user_id = :userId AND cm.left_at IS NULL
      -- sqllint:allow=ownership,tenant,unbounded read model Q02: bài của lớp em + đề kho (org nền tảng) + tiến độ
      -- của em; LIMIT nối thêm ở nơi dùng (trang theo khóa hoặc một bài)
      """;

  private final TenantJdbc jdbc;

  public AssignmentRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public List<StudentRow> studentPage(UUID userId, UUID afterId, int fetch) {
    return jdbc.sql(
            STUDENT_SELECT
                + " AND a.id < COALESCE(CAST(:afterId AS uuid), 'ffffffff-ffff-ffff-ffff-ffffffffffff')"
                + " ORDER BY a.id DESC LIMIT :fetch")
        .param("userId", userId)
        .param("afterId", afterId)
        .param("fetch", fetch)
        .query(AssignmentRepository::studentRow)
        .list();
  }

  public Optional<StudentRow> studentOne(UUID userId, UUID assignmentId) {
    return jdbc.sql(STUDENT_SELECT + " AND a.id = :id LIMIT 1")
        .param("userId", userId)
        .param("id", assignmentId)
        .query(AssignmentRepository::studentRow)
        .optional();
  }

  /** Lớp này có phải của giáo viên đang gọi không (BR-GEO-20). */
  public boolean isTeacherOfClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
                "SELECT count(*) FROM class WHERE org_id = :orgId AND id = :classId AND teacher_id = :teacherId"
                    + " -- sqllint:allow=ownership đọc lớp (iam) để kiểm quyền")
            .param("classId", classId)
            .param("teacherId", teacherId)
            .query(Long.class)
            .single()
        > 0;
  }

  public List<ClassAssignmentRow> classPage(UUID classId, UUID afterId, int fetch) {
    return jdbc.sql(
            """
            SELECT a.id, p.code, a.release_at,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END,
                   a.release_rev
            FROM assignment a
            JOIN problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
            WHERE a.org_id = :orgId AND a.class_id = :classId AND a.kind = 'class' AND a.archived_at IS NULL
              AND a.id < COALESCE(CAST(:afterId AS uuid), 'ffffffff-ffff-ffff-ffff-ffffffffffff')
            ORDER BY a.id DESC LIMIT :fetch
            -- sqllint:allow=ownership,tenant Q03 bài của một lớp + mã đề kho (org nền tảng)
            """)
        .param("classId", classId)
        .param("afterId", afterId)
        .param("fetch", fetch)
        .query((rs, i) ->
            new ClassAssignmentRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                instant(rs, 3),
                rs.getString(4),
                rs.getInt(5)))
        .list();
  }

  public FnResult create(
      UUID actor,
      UUID assignmentId,
      UUID classId,
      UUID problemVersionId,
      OffsetDateTime releaseAt,
      boolean allowHint,
      boolean allowQa) {
    return jdbc.sql(
            """
            SELECT status, release_rev, CAST(NULL AS timestamptz)
            FROM fn_create_assignment(:orgId, :actor, :id, :classId, :pv, CAST(:releaseAt AS timestamptz), :hint, :qa, NULL)
            """)
        .param("actor", actor)
        .param("id", assignmentId)
        .param("classId", classId)
        .param("pv", problemVersionId)
        .param("releaseAt", releaseAt)
        .param("hint", allowHint)
        .param("qa", allowQa)
        .query(AssignmentRepository::fnResult)
        .single();
  }

  public FnResult setRelease(
      UUID actor, UUID assignmentId, boolean openNow, OffsetDateTime releaseAt, int expectedRev) {
    return jdbc.sql(
            """
            SELECT status, release_rev, release_at
            FROM fn_set_release(:orgId, :actor, :id, :openNow, CAST(:releaseAt AS timestamptz), :rev, NULL)
            """)
        .param("actor", actor)
        .param("id", assignmentId)
        .param("openNow", openNow)
        .param("releaseAt", releaseAt)
        .param("rev", expectedRev)
        .query(AssignmentRepository::fnResult)
        .single();
  }

  /** Q06: lời giải chỉ qua cổng của CSDL (BR-01); luôn một hàng. */
  public Solution solution(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            "SELECT status, final_answer, steps::text FROM fn_get_solution(:orgId, :userId, :id)")
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) -> new Solution(rs.getString(1), rs.getString(2), rs.getString(3)))
        .single();
  }

  private static FnResult fnResult(ResultSet rs, int i) throws SQLException {
    int rev = rs.getInt(2);
    return new FnResult(rs.getString(1), rs.wasNull() ? null : rev, instant(rs, 3));
  }

  private static StudentRow studentRow(ResultSet rs, int i) throws SQLException {
    return new StudentRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        instant(rs, 5),
        rs.getString(6),
        rs.getInt(7),
        rs.getBoolean(8),
        rs.getBoolean(9),
        rs.getString(10),
        rs.getBoolean(11),
        rs.getInt(12),
        rs.getBoolean(13),
        rs.getString(14),
        rs.getString(15),
        rs.getString(16));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
