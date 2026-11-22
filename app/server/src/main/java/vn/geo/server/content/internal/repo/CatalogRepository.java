package vn.geo.server.content.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Kho đề: nội dung hệ thống của tổ chức nền tảng, mọi trường đọc được (BR-GEO-110, D-GEO-076). Lời
 * giải chỉ qua hàm cổng fn_get_solution_teacher (D-GEO-153).
 */
@Repository
public class CatalogRepository {

  public record ProblemRow(
      UUID id,
      String code,
      String level,
      String solidKind,
      String topic,
      UUID versionId,
      String statement,
      String verifyStatus) {}

  public record TeacherSolution(UUID versionId, String finalAnswer, String steps) {}

  private static final String SELECT =
      """
      SELECT p.id, p.code, p.level::text, p.solid_kind::text, t.name, pv.id, pv.statement, pv.verify_status::text
      FROM problem p
      JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
      LEFT JOIN problem_topic t ON t.id = p.topic_id
      WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL AND p.code IS NOT NULL
      -- sqllint:allow=tenant,unbounded nội dung hệ thống (org nền tảng); LIMIT nối ở nơi dùng
      """;

  private final TenantJdbc jdbc;

  public CatalogRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Q13 (rút gọn): sắp theo mã bài, keyset (code, id). */
  public List<ProblemRow> page(String afterCode, UUID afterId, int fetch) {
    if (afterCode == null) {
      return jdbc.sql(
              SELECT
                  + """
                  ORDER BY p.code, p.id LIMIT :fetch
                  -- sqllint:allow=tenant nội dung hệ thống: org nền tảng (BR-GEO-110)
                  """)
          .param("fetch", fetch)
          .query(CatalogRepository::row)
          .list();
    }
    return jdbc.sql(
            SELECT
                + """
                AND (p.code, p.id) > (:afterCode, :afterId)
                ORDER BY p.code, p.id LIMIT :fetch
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng (BR-GEO-110)
                """)
        .param("afterCode", afterCode)
        .param("afterId", afterId)
        .param("fetch", fetch)
        .query(CatalogRepository::row)
        .list();
  }

  public Optional<TeacherSolution> teacherSolution(UUID teacherId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT problem_version_id, final_answer, steps::text
            FROM fn_get_solution_teacher(:orgId, :teacherId, :versionId)
            """)
        .param("teacherId", teacherId)
        .param("versionId", versionId)
        .query((rs, i) ->
            new TeacherSolution(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .optional();
  }

  private static ProblemRow row(ResultSet rs, int i) throws SQLException {
    return new ProblemRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getObject(6, UUID.class),
        rs.getString(7),
        rs.getString(8));
  }
}
