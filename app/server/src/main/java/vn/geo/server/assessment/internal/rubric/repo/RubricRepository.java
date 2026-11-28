package vn.geo.server.assessment.internal.rubric.repo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL rubric có phiên bản (US-S5E4-01). Mọi câu lệnh lọc {@code org_id = :orgId} với mã trường đã
 * xác thực do tầng dịch vụ truyền vào. Bất biến của phiên bản đã dùng do trigger CSDL giữ (V625,
 * BR-S5E4-03).
 */
@Repository
public class RubricRepository {

  public record Head(UUID id, UUID teacherId, String code, int currentVersion) {}

  public record Version(
      UUID id,
      UUID rubricId,
      int version,
      String name,
      String template,
      BigDecimal scale,
      String guide) {}

  public record Criterion(
      int ord, String name, String description, BigDecimal maxPoints, boolean direction) {}

  private final JdbcClient jdbc;

  public RubricRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Mã RB-nn kế tiếp của giáo viên (khoá hàng rubric của giáo viên để không trùng). */
  public String nextCode(UUID orgId, UUID teacherId) {
    Integer max =
        jdbc.sql(
                """
                SELECT coalesce(max(substring(r.code FROM 4)::int), 0) FROM rubric r
                WHERE r.org_id = :orgId AND r.teacher_id = :t
                """)
            .param("orgId", orgId)
            .param("t", teacherId)
            .query(Integer.class)
            .single();
    return String.format("RB-%02d", max + 1);
  }

  public UUID insertHead(UUID orgId, UUID teacherId, String code) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO rubric (id, org_id, teacher_id, code, current_version)
            VALUES (:id, :orgId, :t, :code, 1)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("t", teacherId)
        .param("code", code)
        .update();
    return id;
  }

  public UUID insertVersion(
      UUID orgId,
      UUID rubricId,
      int version,
      String name,
      String template,
      BigDecimal scale,
      String guideJson,
      UUID by) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO rubric_version (id, org_id, rubric_id, version, name, template, scale, guide, created_by)
            VALUES (:id, :orgId, :r, :v, :name, :tpl, :scale, CAST(:guide AS jsonb), :by)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("r", rubricId)
        .param("v", version)
        .param("name", name)
        .param("tpl", template)
        .param("scale", scale)
        .param("guide", guideJson)
        .param("by", by)
        .update();
    return id;
  }

  public void insertCriteria(UUID orgId, UUID versionId, List<Criterion> criteria) {
    for (Criterion c : criteria) {
      jdbc.sql(
              """
              INSERT INTO rubric_criterion (id, org_id, rubric_version_id, ord, name, description, max_points,
                                            direction_dependent)
              VALUES (:id, :orgId, :v, :ord, :name, :d, :p, :dir)
              """)
          .param("id", Uuid7.next())
          .param("orgId", orgId)
          .param("v", versionId)
          .param("ord", c.ord())
          .param("name", c.name())
          .param("d", c.description())
          .param("p", c.maxPoints())
          .param("dir", c.direction())
          .update();
    }
  }

  public void deleteCriteria(UUID orgId, UUID versionId) {
    jdbc.sql("DELETE FROM rubric_criterion WHERE org_id = :orgId AND rubric_version_id = :v")
        .param("orgId", orgId)
        .param("v", versionId)
        .update();
  }

  public void updateVersionMeta(UUID orgId, UUID versionId, String name, BigDecimal scale) {
    jdbc.sql(
            """
            UPDATE rubric_version SET name = :name, scale = :scale
            WHERE org_id = :orgId AND id = :v
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .param("name", name)
        .param("scale", scale)
        .update();
  }

  public void setCurrent(UUID orgId, UUID rubricId, int version) {
    jdbc.sql(
            """
            UPDATE rubric SET current_version = :v, updated_at = now()
            WHERE org_id = :orgId AND id = :r
            """)
        .param("orgId", orgId)
        .param("r", rubricId)
        .param("v", version)
        .update();
  }

  /** Rubric của giáo viên, khoá hàng để sửa tuần tự. */
  public Optional<Head> lockOwned(UUID orgId, UUID teacherId, UUID rubricId) {
    return jdbc.sql(
            """
            SELECT r.id, r.teacher_id, r.code, r.current_version FROM rubric r
            WHERE r.org_id = :orgId AND r.id = :r AND r.teacher_id = :t FOR UPDATE
            """)
        .param("orgId", orgId)
        .param("r", rubricId)
        .param("t", teacherId)
        .query((rs, i) ->
            new Head(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getInt(4)))
        .optional();
  }

  public Optional<Head> owned(UUID orgId, UUID teacherId, UUID rubricId) {
    return jdbc.sql(
            """
            SELECT r.id, r.teacher_id, r.code, r.current_version FROM rubric r
            WHERE r.org_id = :orgId AND r.id = :r AND r.teacher_id = :t
            """)
        .param("orgId", orgId)
        .param("r", rubricId)
        .param("t", teacherId)
        .query((rs, i) ->
            new Head(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getInt(4)))
        .optional();
  }

  public List<Head> listOwned(UUID orgId, UUID teacherId) {
    return jdbc.sql(
            """
            SELECT r.id, r.teacher_id, r.code, r.current_version FROM rubric r
            WHERE r.org_id = :orgId AND r.teacher_id = :t ORDER BY r.code LIMIT 200
            """)
        .param("orgId", orgId)
        .param("t", teacherId)
        .query((rs, i) ->
            new Head(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getInt(4)))
        .list();
  }

  public Optional<Version> version(UUID orgId, UUID rubricId, int version) {
    return jdbc.sql(
            """
            SELECT v.id, v.rubric_id, v.version, v.name, v.template, v.scale, v.guide::text FROM rubric_version v
            WHERE v.org_id = :orgId AND v.rubric_id = :r AND v.version = :v
            """)
        .param("orgId", orgId)
        .param("r", rubricId)
        .param("v", version)
        .query(RubricRepository::mapVersion)
        .optional();
  }

  public Optional<Version> versionById(UUID orgId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT v.id, v.rubric_id, v.version, v.name, v.template, v.scale, v.guide::text FROM rubric_version v
            WHERE v.org_id = :orgId AND v.id = :v
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .query(RubricRepository::mapVersion)
        .optional();
  }

  public List<Criterion> criteria(UUID orgId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT c.ord, c.name, c.description, c.max_points, c.direction_dependent FROM rubric_criterion c
            WHERE c.org_id = :orgId AND c.rubric_version_id = :v ORDER BY c.ord
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .query((rs, i) ->
            new Criterion(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getBigDecimal(4).stripTrailingZeros(),
                rs.getBoolean(5)))
        .list();
  }

  /** Phiên bản đã gắn với ít nhất một bài có chấm (BR-S5E4-03). */
  public boolean used(UUID orgId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM graded_assignment g WHERE g.org_id = :orgId AND g.rubric_version_id = :v)
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .query(Boolean.class)
        .single();
  }

  /**
   * MÓC KIỂM THỬ (TC-S5E4-06): câu UPDATE thẳng trên tiêu chí của một phiên bản bằng vai ứng dụng;
   * trigger CSDL phải từ chối khi phiên bản đã dùng.
   */
  public int rawUpdateCriteria(UUID orgId, UUID versionId, BigDecimal points) {
    return jdbc.sql(
            """
            UPDATE rubric_criterion SET max_points = :p
            WHERE org_id = :orgId AND rubric_version_id = :v AND ord = 1
            """)
        .param("orgId", orgId)
        .param("v", versionId)
        .param("p", points)
        .update();
  }

  private static Version mapVersion(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
    return new Version(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getInt(3),
        rs.getString(4),
        rs.getString(5),
        rs.getBigDecimal(6).stripTrailingZeros(),
        rs.getString(7));
  }
}
