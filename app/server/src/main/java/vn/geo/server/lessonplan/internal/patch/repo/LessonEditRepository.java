package vn.geo.server.lessonplan.internal.patch.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.id.Uuid7;

/**
 * Sửa, duyệt, liệt kê giáo án (T-261128-54: US-S5E1-04, 05): phiên bản nội dung, bản vá AI chờ giáo
 * viên nhận, khoá mục, duyệt. Mọi câu lọc theo trường; đọc giáo án lọc thêm theo chính giáo viên
 * chủ (giáo viên khác NOT_FOUND).
 */
@Repository
public class LessonEditRepository {

  /** Giáo án (phần dùng khi sửa / duyệt / xuất). */
  public record Plan(
      UUID id,
      String code,
      String periodId,
      String requestJson,
      String status,
      String contentJson,
      int version,
      String humanLabel,
      List<String> lockedSections,
      Integer approvedVersion,
      UUID approvedBy,
      Instant approvedAt,
      Instant createdAt,
      Instant updatedAt) {}

  /** Bản vá AI đã kiểm, chờ giáo viên nhận / bỏ từng dòng. */
  public record Patch(
      UUID id,
      UUID planId,
      int baseVersion,
      String section,
      String patchesJson,
      String decisionsJson) {}

  /** Một phiên bản nội dung. */
  public record Version(int version, String source, Instant createdAt) {}

  private static final String COLS =
      "id, code, period_id, request::text AS request, status, content::text AS content, version,"
          + " human_label, locked_sections, approved_version, approved_by, approved_at, created_at,"
          + " updated_at";

  private final TenantJdbc jdbc;

  public LessonEditRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Plan> byId(UUID teacherId, UUID id) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM lesson_plan WHERE org_id = :orgId AND teacher_id = :teacher AND id = :id"
                + " LIMIT 1")
        .param("teacher", teacherId)
        .param("id", id)
        .query((rs, i) -> plan(rs))
        .optional();
  }

  public Optional<Plan> byCode(UUID teacherId, String code) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM lesson_plan WHERE org_id = :orgId AND teacher_id = :teacher"
                + " AND code = :code LIMIT 1")
        .param("teacher", teacherId)
        .param("code", code)
        .query((rs, i) -> plan(rs))
        .optional();
  }

  /** "Giáo án của tôi": mới sửa trước; {@code approved} = true chỉ giáo án đã duyệt, false chưa. */
  public List<Plan> list(UUID teacherId, Boolean approved, int limit) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + """
                 FROM lesson_plan WHERE org_id = :orgId AND teacher_id = :teacher
                  AND (CAST(:approved AS boolean) IS NULL
                       OR (approved_version IS NOT NULL) = CAST(:approved AS boolean))
                ORDER BY updated_at DESC, id DESC LIMIT :limit
                """)
        .param("teacher", teacherId)
        .param("approved", approved)
        .param("limit", limit)
        .query((rs, i) -> plan(rs))
        .list();
  }

  public Optional<String> versionContent(UUID planId, int version) {
    return jdbc.sql(
            "SELECT content::text FROM lesson_plan_version WHERE org_id = :orgId"
                + " AND plan_id = :plan AND version = :v LIMIT 1")
        .param("plan", planId)
        .param("v", version)
        .query(String.class)
        .optional();
  }

  /** Các phiên bản tới phiên bản hiện tại (mới trước). */
  public List<Version> versions(UUID planId, int upTo) {
    return jdbc.sql(
            """
            SELECT version, source, created_at FROM lesson_plan_version
            WHERE org_id = :orgId AND plan_id = :plan AND version <= :upTo
            ORDER BY version DESC LIMIT 50
            """)
        .param("plan", planId)
        .param("upTo", upTo)
        .query(
            (rs, i) -> new Version(rs.getInt(1), rs.getString(2), rs.getTimestamp(3).toInstant()))
        .list();
  }

  /**
   * Lưu nội dung mới thành phiên bản {@code expected + 1} (BR-S5E1-12), nhãn người "AI soạn"
   * (BR-S5E1-13). Trả 0 nếu phiên bản hiện tại đã khác {@code expected} (sửa đồng thời).
   */
  public int newVersion(UUID planId, int expected, String contentJson, String source) {
    int n =
        jdbc.sql(
                """
                UPDATE lesson_plan SET content = CAST(:content AS jsonb), version = version + 1,
                       human_label = 'ai', updated_at = now()
                WHERE org_id = :orgId AND id = :id AND version = :expected
                """)
            .param("id", planId)
            .param("expected", expected)
            .param("content", contentJson)
            .update();
    if (n == 0) {
      return 0;
    }
    // phiên bản sau một lần Hoàn tác được ghi đè (lịch sử đi tiếp từ phiên bản đang xem)
    jdbc.sql(
            """
            INSERT INTO lesson_plan_version (plan_id, org_id, version, content, source)
            VALUES (:plan, :orgId, :v, CAST(:content AS jsonb), :source)
            ON CONFLICT (plan_id, version)
            DO UPDATE SET content = EXCLUDED.content, source = EXCLUDED.source, created_at = now()
            """)
        .param("plan", planId)
        .param("v", expected + 1)
        .param("content", contentJson)
        .param("source", source)
        .update();
    return expected + 1;
  }

  /** Hoàn tác: quay về phiên bản {@code target} (nội dung và nhãn người của phiên bản đó). */
  public int moveTo(UUID planId, int expected, int target, String contentJson, String humanLabel) {
    return jdbc.sql(
            """
            UPDATE lesson_plan SET content = CAST(:content AS jsonb), version = :target,
                   human_label = :label, updated_at = now()
            WHERE org_id = :orgId AND id = :id AND version = :expected
            """)
        .param("id", planId)
        .param("expected", expected)
        .param("target", target)
        .param("content", contentJson)
        .param("label", humanLabel)
        .update();
  }

  public void setLocked(UUID planId, List<String> locked) {
    jdbc.sql(
            "UPDATE lesson_plan SET locked_sections = :locked, updated_at = now()"
                + " WHERE org_id = :orgId AND id = :id")
        .param("id", planId)
        .param("locked", locked.toArray(String[]::new))
        .update();
  }

  /** Duyệt phiên bản {@code version} (BR-S5E1-13); 0 nếu phiên bản đã đổi hoặc chưa "Sẵn sàng". */
  public int approve(UUID planId, int version, UUID by, Instant at) {
    return jdbc.sql(
            """
            UPDATE lesson_plan SET human_label = 'approved', approved_version = :v,
                   approved_by = :by, approved_at = :at, updated_at = now()
            WHERE org_id = :orgId AND id = :id AND version = :v AND status = 'ready'
            """)
        .param("id", planId)
        .param("v", version)
        .param("by", by)
        .param("at", Timestamp.from(at))
        .update();
  }

  // ---------------------------------------------------------------- bản vá

  public UUID insertPatch(
      UUID planId, int baseVersion, String section, String patchesJson, String decisionsJson) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO lesson_plan_patch (id, org_id, plan_id, base_version, section, patches, decisions)
            VALUES (:id, :orgId, :plan, :base, :section, CAST(:patches AS jsonb), CAST(:decisions AS jsonb))
            """)
        .param("id", id)
        .param("plan", planId)
        .param("base", baseVersion)
        .param("section", section)
        .param("patches", patchesJson)
        .param("decisions", decisionsJson)
        .update();
    return id;
  }

  public Optional<Patch> patch(UUID planId, UUID id) {
    return jdbc.sql(
            """
            SELECT id, plan_id, base_version, section, patches::text, decisions::text
            FROM lesson_plan_patch WHERE org_id = :orgId AND plan_id = :plan AND id = :id LIMIT 1
            """)
        .param("plan", planId)
        .param("id", id)
        .query((rs, i) ->
            new Patch(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getInt(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)))
        .optional();
  }

  public void updateDecisions(UUID id, String decisionsJson) {
    jdbc.sql(
            "UPDATE lesson_plan_patch SET decisions = CAST(:d AS jsonb)"
                + " WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .param("d", decisionsJson)
        .update();
  }

  private static Plan plan(ResultSet rs) throws SQLException {
    java.sql.Array locked = rs.getArray("locked_sections");
    Timestamp at = rs.getTimestamp("approved_at");
    return new Plan(
        rs.getObject("id", UUID.class),
        rs.getString("code"),
        rs.getString("period_id"),
        rs.getString("request"),
        rs.getString("status"),
        rs.getString("content"),
        rs.getInt("version"),
        rs.getString("human_label"),
        locked == null ? List.of() : List.of((String[]) locked.getArray()),
        (Integer) rs.getObject("approved_version"),
        rs.getObject("approved_by", UUID.class),
        at == null ? null : at.toInstant(),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("updated_at").toInstant());
  }
}
