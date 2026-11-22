package vn.geo.server.content.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Kho đề: nội dung hệ thống của tổ chức nền tảng, mọi trường đọc được (BR-GEO-110, D-GEO-076). Một
 * trường thấy bộ 'main' và các bộ được cấp ở catalog_bank_access (E5, V50). Lời giải chỉ qua hàm
 * cổng fn_get_solution_teacher (D-GEO-153); số bước qua fn_catalog_step_counts.
 */
@Repository
public class CatalogRepository {

  public record ProblemRow(
      UUID id,
      String code,
      String level,
      String solidKind,
      String topicSlug,
      String topic,
      UUID versionId,
      String statement,
      String verifyStatus,
      boolean aiAuthored,
      Integer stepCount) {}

  /** Bộ lọc đã kiểm (giá trị mã của CSDL hoặc null = "Tất cả"). */
  public record Filter(String level, String solidKind, String topicSlug) {}

  public record Summary(long total, long verified, String author) {}

  public record DetailRow(
      UUID versionId,
      String code,
      String level,
      String solidKind,
      String topicSlug,
      String topic,
      String statement,
      String verifyStatus,
      boolean aiAuthored,
      String author) {}

  public record HintRow(int level, String body) {}

  public record TeacherSolution(
      UUID versionId, String finalAnswer, String steps, boolean aiAuthored) {}

  /** Đề kho mà trường của người gọi thấy (bộ main + bộ được cấp). */
  private static final String VISIBLE =
      """
      FROM problem p
      JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
      JOIN problem_topic t ON t.id = p.topic_id
      WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL AND p.code IS NOT NULL
        AND (p.bank = 'main' OR p.bank IN (SELECT b.bank FROM catalog_bank_access b WHERE b.org_id = :orgId))
      """;

  private final TenantJdbc jdbc;

  public CatalogRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Q13 (E5): một câu lệnh cho cả trang (kể cả số bước), sắp theo mã bài, keyset (code, id), không
   * OFFSET (BR-GEO-218).
   */
  public List<ProblemRow> page(Filter f, String afterCode, UUID afterId, int fetch) {
    return jdbc.sql(
            "WITH page AS (SELECT p.id, p.code, p.level::text AS level, p.solid_kind::text AS solid, t.slug,"
                + " t.name, pv.id AS pvid, pv.statement, pv.verify_status::text AS vs, pv.ai_authored\n"
                + VISIBLE
                + """
                  AND (CAST(:level AS text) IS NULL OR p.level = CAST(:level AS edu_level))
                  AND (CAST(:solid AS text) IS NULL OR p.solid_kind = CAST(:solid AS solid_kind))
                  AND (CAST(:topic AS text) IS NULL OR t.slug = :topic)
                  AND (CAST(:afterCode AS text) IS NULL OR (p.code, p.id) > (CAST(:afterCode AS text), CAST(:afterId AS uuid)))
                ORDER BY p.code, p.id LIMIT :fetch)
                SELECT page.id, page.code, page.level, page.solid, page.slug, page.name, page.pvid, page.statement, page.vs,
                       page.ai_authored, sc.step_count
                FROM page
                LEFT JOIN fn_catalog_step_counts(ARRAY(SELECT x.pvid FROM page x)) sc ON sc.problem_version_id = page.pvid
                ORDER BY page.code, page.id
                -- sqllint:allow=tenant nội dung hệ thống: org nền tảng, bộ đề theo trường của thẻ (BR-GEO-110)
                """)
        .param("level", f.level())
        .param("solid", f.solidKind())
        .param("topic", f.topicSlug())
        .param("afterCode", afterCode)
        .param("afterId", afterId)
        .param("fetch", fetch)
        .query(CatalogRepository::row)
        .list();
  }

  /** M-GEO-07: số đề và số đề "Đã kiểm chứng" của kho trường thấy; tên tác giả nền tảng. */
  public Summary summary() {
    return jdbc.sql(
            """
            SELECT count(*), count(*) FILTER (WHERE pv.verify_status = 'verified'),
                   (SELECT u.display_name FROM app_user u WHERE u.org_id = platform_org_id() AND u.account_kind = 'system'
                    ORDER BY u.id LIMIT 1)
            FROM problem p
            JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
            WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL AND p.code IS NOT NULL
              AND (p.bank = 'main' OR p.bank IN (SELECT b.bank FROM catalog_bank_access b WHERE b.org_id = :orgId))
            -- sqllint:allow=tenant,unbounded,ownership đếm kho nền tảng (≤ 60 đề, M-GEO-07); tên tác giả nền tảng (D-GEO-160)
            """)
        .query((rs, i) -> new Summary(rs.getLong(1), rs.getLong(2), rs.getString(3)))
        .single();
  }

  /**
   * Guard quyền bộ đề (FIX3, {@code content.api.BankAccess}): version đề kho thuộc bộ trường thấy.
   */
  public boolean bankVisible(UUID versionId) {
    return Boolean.TRUE.equals(
        jdbc.sql(
                """
                SELECT EXISTS (
                  SELECT 1 FROM problem_version pv
                  JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
                  WHERE pv.org_id = platform_org_id() AND pv.id = :pv AND p.source = 'catalog'
                    AND (p.bank = 'main' OR EXISTS (SELECT 1 FROM catalog_bank_access b
                                                    WHERE b.org_id = :orgId AND b.bank = p.bank)))
                -- sqllint:allow=tenant,ownership nội dung hệ thống (org nền tảng, D-GEO-076); quyền bộ theo :orgId
                """)
            .param("pv", versionId)
            .query(Boolean.class)
            .single());
  }

  /** Một đề kho (version bất kỳ của đề kho đang dùng) thuộc bộ trường thấy; ngoài ra: rỗng. */
  public Optional<DetailRow> detail(UUID versionId) {
    return jdbc.sql(
            """
            SELECT pv.id, p.code, p.level::text, p.solid_kind::text, t.slug, t.name, pv.statement, pv.verify_status::text,
                   pv.ai_authored, (SELECT u.display_name FROM app_user u WHERE u.org_id = p.org_id AND u.id = p.author_user_id)
            FROM problem_version pv
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            JOIN problem_topic t ON t.id = p.topic_id
            WHERE pv.org_id = platform_org_id() AND pv.id = :pv AND p.source = 'catalog' AND p.archived_at IS NULL
              AND (p.bank = 'main' OR EXISTS (SELECT 1 FROM catalog_bank_access b WHERE b.org_id = :orgId AND b.bank = p.bank))
            LIMIT 1
            -- sqllint:allow=tenant,ownership nội dung hệ thống (org nền tảng, D-GEO-076); tên tác giả nền tảng (D-GEO-160)
            """)
        .param("pv", versionId)
        .query((rs, i) ->
            new DetailRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getString(8),
                rs.getBoolean(9),
                rs.getString(10)))
        .optional();
  }

  public List<HintRow> hints(UUID versionId) {
    return jdbc.sql(
            """
            SELECT level, body FROM hint WHERE org_id = platform_org_id() AND problem_version_id = :pv ORDER BY level LIMIT 2
            -- sqllint:allow=tenant gợi ý đề kho nền tảng cho giáo viên (BR-GEO-208)
            """)
        .param("pv", versionId)
        .query((rs, i) -> new HintRow(rs.getInt(1), rs.getString(2)))
        .list();
  }

  public Optional<TeacherSolution> teacherSolution(UUID teacherId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT problem_version_id, final_answer, steps::text, ai_authored
            FROM fn_get_solution_teacher(:orgId, :teacherId, :versionId)
            """)
        .param("teacherId", teacherId)
        .param("versionId", versionId)
        .query((rs, i) ->
            new TeacherSolution(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getBoolean(4)))
        .optional();
  }

  private static ProblemRow row(ResultSet rs, int i) throws SQLException {
    int steps = rs.getInt(11);
    return new ProblemRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getObject(7, UUID.class),
        rs.getString(8),
        rs.getString(9),
        rs.getBoolean(10),
        rs.wasNull() ? null : steps);
  }
}
