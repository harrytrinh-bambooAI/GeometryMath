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
 * E6: dữ liệu của màn giao bài (bước 1 · Chọn lớp), thẻ vàng giờ mở, nhật ký đổi giờ, dòng "Hôm nay
 * em có n bài cần làm" và trạng thái khóa của màn xem trước. Chỉ đọc; ghi giờ mở luôn qua
 * fn_set_release (V10).
 */
@Repository
public class ReleaseRepository {

  public record ClassOption(
      UUID id,
      String name,
      String level,
      int studentCount,
      UUID assignmentId,
      Instant releaseAt,
      String releaseState) {}

  public record CatalogProblem(
      UUID versionId, UUID problemId, String code, String title, String statement) {}

  public record ReleaseRow(
      UUID id,
      UUID classId,
      String className,
      String problemCode,
      Instant releaseAt,
      String releaseState,
      int releaseRev,
      boolean allowHint,
      boolean allowQa) {}

  public record LogRow(
      long id, Instant at, String actorName, String action, Instant before, Instant after) {}

  private final TenantJdbc jdbc;

  public ReleaseRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Instant now() {
    return jdbc.sql("SELECT now()").query(OffsetDateTime.class).single().toInstant();
  }

  /** Đề kho theo version (D-GEO-161: "cùng đề" = cùng problem). */
  public Optional<CatalogProblem> catalogProblem(UUID versionId) {
    return jdbc.sql(
            """
            SELECT pv.id, p.id, p.code, p.title, pv.statement
            FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE pv.org_id = platform_org_id() AND pv.id = :pv AND p.source = 'catalog' AND p.archived_at IS NULL
            LIMIT 1
            -- sqllint:allow=ownership,tenant đề kho nền tảng đọc được từ mọi trường (D-GEO-076)
            """)
        .param("pv", versionId)
        .query((rs, i) ->
            new CatalogProblem(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5)))
        .optional();
  }

  /**
   * Bước "1 · Chọn lớp": lớp của giáo viên, sĩ số, bài giao chưa lưu trữ của đề này ở lớp đó (nếu
   * có).
   */
  public List<ClassOption> classOptions(UUID teacherId, UUID problemId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name, c.level::text,
                   (SELECT count(*) FROM class_member m WHERE m.org_id = :orgId AND m.class_id = c.id AND m.left_at IS NULL),
                   a.id, a.release_at,
                   CASE WHEN a.id IS NULL THEN NULL WHEN a.release_at IS NULL THEN 'unset'
                        WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END
            FROM class c
            LEFT JOIN assignment a ON a.org_id = :orgId AND a.class_id = c.id AND a.kind = 'class'
                                  AND a.archived_at IS NULL AND a.problem_id = :problemId
            WHERE c.org_id = :orgId AND c.teacher_id = :teacherId AND c.archived_at IS NULL
            ORDER BY c.level DESC, c.name
            LIMIT 100
            -- sqllint:allow=ownership đọc lớp (iam) và bài giao của lớp mình dạy
            """)
        .param("teacherId", teacherId)
        .param("problemId", problemId)
        .query((rs, i) ->
            new ClassOption(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getObject(5, UUID.class),
                instant(rs, 6),
                rs.getString(7)))
        .list();
  }

  public Optional<String> classNameOfTeacher(UUID teacherId, UUID classId) {
    return jdbc.sql(
            "SELECT name FROM class WHERE org_id = :orgId AND id = :classId AND teacher_id = :teacherId"
                + " -- sqllint:allow=ownership đọc lớp (iam) để kiểm quyền")
        .param("classId", classId)
        .param("teacherId", teacherId)
        .query(String.class)
        .optional();
  }

  /** Thẻ vàng: bài giao của lớp do chính giáo viên dạy (khác lớp/khác trường: rỗng, BR-GEO-112). */
  public Optional<ReleaseRow> release(UUID teacherId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, c.id, c.name, p.code, a.release_at,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END,
                   a.release_rev, a.allow_hint, a.allow_qa
            FROM assignment a
            JOIN class c ON c.org_id = a.org_id AND c.id = a.class_id
            JOIN problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
            WHERE a.org_id = :orgId AND a.id = :id AND a.kind = 'class' AND c.teacher_id = :teacherId
            LIMIT 1
            -- sqllint:allow=ownership,tenant bài giao + lớp (iam) + mã đề kho (org nền tảng)
            """)
        .param("id", assignmentId)
        .param("teacherId", teacherId)
        .query((rs, i) ->
            new ReleaseRow(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                instant(rs, 5),
                rs.getString(6),
                rs.getInt(7),
                rs.getBoolean(8),
                rs.getBoolean(9)))
        .optional();
  }

  /** Q15 nhật ký đặt/đổi/mở ngay giờ mở của một bài, mới nhất trước (BR-GEO-240). */
  public List<LogRow> releaseLog(UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT l.id, l.occurred_at, u.display_name, l.action::text, l.before_release_at, l.after_release_at
            FROM audit_log l
            JOIN app_user u ON u.org_id = :orgId AND u.id = l.actor_user_id
            WHERE l.org_id = :orgId AND l.target_id = :id
              AND l.action IN ('release_set', 'release_change', 'release_now')
            ORDER BY l.id DESC
            LIMIT 100
            -- sqllint:allow=ownership nhật ký thao tác (audit) + tên người làm
            """)
        .param("id", assignmentId)
        .query((rs, i) ->
            new LogRow(
                rs.getLong(1),
                instant(rs, 2),
                rs.getString(3),
                rs.getString(4),
                instant(rs, 5),
                instant(rs, 6)))
        .list();
  }

  /** D-GEO-138, 182: số bài giao của các lớp em mà em chưa xem lời giải (tiến độ chưa 3/3). */
  public int todoCount(UUID userId) {
    return jdbc.sql(
            """
            SELECT count(*)
            FROM class_member cm
            JOIN assignment a ON a.org_id = :orgId AND a.class_id = cm.class_id AND a.kind = 'class' AND a.archived_at IS NULL
            LEFT JOIN assignment_progress ap ON ap.org_id = :orgId AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
            WHERE cm.org_id = :orgId AND cm.user_id = :userId AND cm.left_at IS NULL AND ap.solution_viewed_at IS NULL
            -- sqllint:allow=ownership,unbounded đếm trên các lớp của chính em (vài chục bài)
            """)
        .param("userId", userId)
        .query(Integer.class)
        .single();
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
