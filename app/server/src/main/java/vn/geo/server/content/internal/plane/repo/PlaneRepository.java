package vn.geo.server.content.internal.plane.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.db.TenantJdbc;

/**
 * S2-E2: bộ đề chuẩn hình phẳng (plane_benchmark_problem, dữ liệu nền tảng) và đề phẳng của bài
 * giao (problem.kind = 'plane' + plane_problem_spec, V130).
 */
@Repository
public class PlaneRepository {

  /** Một đề của bộ đề chuẩn (BR-S2E2-12). */
  public record BenchRow(
      String code,
      String alias,
      String statement,
      String shape,
      String form,
      String spec,
      String answer,
      String answerType,
      String reviewer) {}

  /** Đề phẳng của một bài giao mà học sinh được xem. */
  public record StudentPlane(
      UUID assignmentId,
      String code,
      String title,
      String statement,
      String spec,
      String approvalStatus,
      Instant releaseAt,
      boolean released) {}

  private final TenantJdbc jdbc;
  private final JdbcClient system;

  public PlaneRepository(TenantJdbc jdbc, JdbcClient system) {
    this.jdbc = jdbc;
    this.system = system;
  }

  /** Thay cả bộ đề chuẩn bằng các đề hợp lệ của lần nạp (BR-S2E2-14: đề lỗi không vào bộ). */
  @Transactional
  public void replaceBenchmark(List<BenchRow> rows) {
    system
        .sql(
            """
            DELETE FROM plane_benchmark_problem
            -- sqllint:allow=tenant bộ đề chuẩn là dữ liệu nền tảng, không thuộc trường nào
            """)
        .update();
    for (BenchRow r : rows) {
      system
          .sql(
              """
              INSERT INTO plane_benchmark_problem
                (code, alias, statement, shape, form, spec, answer, answer_type, reviewer)
              VALUES (:code, :alias, :statement, :shape, :form, CAST(:spec AS jsonb), :answer,
                      :answerType, :reviewer)
              -- sqllint:allow=tenant bộ đề chuẩn là dữ liệu nền tảng, không thuộc trường nào
              """)
          .param("code", r.code())
          .param("alias", r.alias())
          .param("statement", r.statement())
          .param("shape", r.shape())
          .param("form", r.form())
          .param("spec", r.spec())
          .param("answer", r.answer())
          .param("answerType", r.answerType())
          .param("reviewer", r.reviewer())
          .update();
    }
  }

  /** Bộ đề chuẩn đã nạp, theo mã đề. */
  public List<BenchRow> benchmark() {
    return system
        .sql(
            """
            SELECT code, alias, statement, shape, form, spec::text, answer, answer_type, reviewer
            FROM plane_benchmark_problem ORDER BY code LIMIT 1000
            -- sqllint:allow=tenant bộ đề chuẩn là dữ liệu nền tảng, không thuộc trường nào
            """)
        .query((rs, i) ->
            new BenchRow(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getString(8),
                rs.getString(9)))
        .list();
  }

  /**
   * Đề phẳng của bài giao: bài lớp mà em đang thuộc lớp, hoặc bài tự học của em. {@code released}
   * so giờ máy chủ (không tin giờ thiết bị).
   */
  public Optional<StudentPlane> studentPlane(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, p.code, p.title, pv.statement, s.spec::text, pv.approval_status::text,
                   a.release_at, (a.release_at IS NOT NULL AND a.release_at <= now()) AS released
            FROM assignment a
            JOIN problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.kind = 'plane'
            JOIN plane_problem_spec s ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
            WHERE a.org_id = :orgId AND a.id = :id AND a.archived_at IS NULL
              AND ((a.kind = 'class' AND EXISTS (
                     SELECT 1 FROM class_member cm
                     WHERE cm.org_id = :orgId AND cm.class_id = a.class_id AND cm.user_id = :userId
                       AND cm.left_at IS NULL))
                OR (a.kind = 'self' AND a.owner_user_id = :userId))
            LIMIT 1
            -- sqllint:allow=ownership,tenant read model: bài giao (module assignment) + đề kho của org nền tảng
            """)
        .param("id", assignmentId)
        .param("userId", userId)
        .query((rs, i) ->
            new StudentPlane(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getObject(7, java.time.OffsetDateTime.class) == null
                    ? null
                    : rs.getObject(7, java.time.OffsetDateTime.class).toInstant(),
                rs.getBoolean(8)))
        .optional();
  }
}
