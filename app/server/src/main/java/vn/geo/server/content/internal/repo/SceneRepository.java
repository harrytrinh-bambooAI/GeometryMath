package vn.geo.server.content.internal.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * E2: danh mục hình mẫu (solid_template), bản dựng hình (scene_build), đề tự nhập của tự học. Báo
 * cáo kiểm chứng và nhãn máy chỉ qua hàm cổng (fn_record_self_verification, V20).
 */
@Repository
public class SceneRepository {

  public record TemplateRow(int id, String code, String solidKind, String name, String paramSpec) {}

  public record SceneRow(UUID id, String coordSource, String spec, String coords) {}

  /** Đề của một bài giao mà học sinh được xem. */
  public record AssignmentProblem(
      UUID assignmentId,
      String kind,
      UUID contentOrgId,
      UUID versionId,
      String code,
      String statement,
      String verifyStatus) {}

  public record CatalogVersion(
      UUID versionId, String code, String statement, String verifyStatus) {}

  private final TenantJdbc jdbc;

  /** Câu lệnh không thuộc trường nào (danh mục hệ thống, chỉ số gộp): không gắn :orgId. */
  private final JdbcClient system;

  public SceneRepository(TenantJdbc jdbc, JdbcClient system) {
    this.jdbc = jdbc;
    this.system = system;
  }

  // ------------------------------------------------------------------ danh mục hình mẫu

  public List<TemplateRow> templates() {
    return system
        .sql(
            """
            SELECT id, code, solid_kind::text, name, param_spec::text FROM solid_template ORDER BY id
            """)
        .query((rs, i) ->
            new TemplateRow(
                rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)))
        .list();
  }

  /** Nạp một hình mẫu (vai geo_content); đã có cùng mã hoặc id thì giữ bản cũ. */
  public boolean insertTemplate(int id, String code, String solidKind, String name, String spec) {
    return system
            .sql(
                """
                INSERT INTO solid_template (id, code, solid_kind, name, param_spec)
                VALUES (:id, :code, CAST(:kind AS solid_kind), :name, CAST(:spec AS jsonb))
                ON CONFLICT DO NOTHING
                """)
            .param("id", id)
            .param("code", code)
            .param("kind", solidKind)
            .param("name", name)
            .param("spec", spec)
            .update()
        == 1;
  }

  // ------------------------------------------------------------------ bản dựng hình

  /**
   * Q04 (phần hình): bản dựng mới nhất của version đề (đề kho: org nền tảng; tự học: trường em).
   */
  public Optional<SceneRow> latestScene(UUID contentOrgId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT id, coord_source::text, spec::text, coords::text FROM scene_build
            WHERE org_id = :contentOrg AND problem_version_id = :pv
            ORDER BY build_no DESC LIMIT 1
            -- sqllint:allow=tenant nội dung của bài: đề kho thuộc org nền tảng, đề tự nhập thuộc trường em
            -- (contentOrg lấy từ bài giao đã lọc theo :orgId)
            """)
        .param("contentOrg", contentOrgId)
        .param("pv", versionId)
        .query((rs, i) ->
            new SceneRow(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }

  /** Đề của bài giao: bài lớp mà em đang thuộc lớp, hoặc bài tự học của chính em. */
  public Optional<AssignmentProblem> studentAssignment(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, a.kind::text, a.content_org_id, pv.id, p.code, pv.statement, pv.verify_status::text
            FROM assignment a
            JOIN problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
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
            new AssignmentProblem(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getObject(3, UUID.class),
                rs.getObject(4, UUID.class),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7)))
        .optional();
  }

  /**
   * Một version đề kho (mọi version, kể cả không hiện hành) cho trang chi tiết đề của giáo viên.
   */
  public Optional<CatalogVersion> catalogVersion(UUID versionId) {
    return jdbc.sql(
            """
            SELECT pv.id, p.code, pv.statement, pv.verify_status::text
            FROM problem_version pv
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE pv.org_id = platform_org_id() AND pv.id = :pv AND p.source = 'catalog'
              AND p.archived_at IS NULL
            LIMIT 1
            -- sqllint:allow=tenant nội dung hệ thống (org nền tảng, D-GEO-076)
            """)
        .param("pv", versionId)
        .query((rs, i) ->
            new CatalogVersion(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }

  // ------------------------------------------------------------------ đề tự nhập (tự học)

  public void insertStudentProblem(
      UUID id, UUID ownerId, String level, String solidKind, String title) {
    jdbc.sql(
            """
            INSERT INTO problem (id, org_id, subject_id, source, owner_user_id, level, solid_kind, title)
            VALUES (:id, :orgId, 1, 'student_input', :owner, CAST(:level AS edu_level),
                    CAST(:kind AS solid_kind), :title)
            """)
        .param("id", id)
        .param("owner", ownerId)
        .param("level", level)
        .param("kind", solidKind)
        .param("title", title)
        .update();
  }

  public void insertStudentVersion(UUID id, UUID problemId, String statement, UUID createdBy) {
    jdbc.sql(
            """
            INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by)
            VALUES (:id, :orgId, :problem, 1, :st,
                    digest(regexp_replace(btrim(:st), '\\s+', ' ', 'g'), 'sha256'), :by)
            """)
        .param("id", id)
        .param("problem", problemId)
        .param("st", statement)
        .param("by", createdBy)
        .update();
    jdbc.sql(
            """
            UPDATE problem SET current_version_id = :pv WHERE org_id = :orgId AND id = :problem
            """)
        .param("pv", id)
        .param("problem", problemId)
        .update();
  }

  public void insertScene(
      UUID id, UUID versionId, String coordSource, String specJson, String coordsJson) {
    jdbc.sql(
            """
            INSERT INTO scene_build (id, org_id, problem_version_id, build_no, coord_source, spec, coords)
            VALUES (:id, :orgId, :pv, 1, CAST(:src AS coord_source), CAST(:spec AS jsonb),
                    CAST(:coords AS jsonb))
            """)
        .param("id", id)
        .param("pv", versionId)
        .param("src", coordSource)
        .param("spec", specJson)
        .param("coords", coordsJson)
        .update();
  }

  /** Hàm cổng V20: ghi báo cáo + đặt nhãn máy theo báo cáo; trả nhãn. */
  public String recordSelfVerification(
      UUID versionId,
      UUID reportId,
      boolean sceneOk,
      boolean answerOk,
      String checksJson,
      UUID actor) {
    return jdbc.sql(
            """
            SELECT fn_record_self_verification(:orgId, :pv, :report, :scene, :answer,
                                               CAST(:checks AS jsonb), :actor)::text
            """)
        .param("pv", versionId)
        .param("report", reportId)
        .param("scene", sceneOk)
        .param("answer", answerOk)
        .param("checks", checksJson)
        .param("actor", actor)
        .query(String.class)
        .single();
  }

  /** Hàm cổng của assignment (fn_create_self_assignment): tạo bài tự học của chính em. */
  public String createSelfAssignment(UUID userId, UUID assignmentId, UUID versionId) {
    return jdbc.sql(
            """
            SELECT status FROM fn_create_self_assignment(:orgId, :userId, :id, :pv)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("pv", versionId)
        .query(String.class)
        .single();
  }

  /** M-GEO-18 tỉ lệ đề lạ: số đề tự nhập dựng bằng tọa độ AI / số đề tự nhập đã dựng, trong kỳ. */
  public long[] foreignCounts(Instant from, Instant to) {
    return system
        .sql(
            """
            SELECT count(*) FILTER (WHERE sb.coord_source = 'ai'), count(*)
            FROM scene_build sb
            JOIN problem_version pv ON pv.org_id = sb.org_id AND pv.id = sb.problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE p.source = 'student_input' AND sb.build_no = 1
              AND sb.created_at >= :from AND sb.created_at < :to
            -- sqllint:allow=tenant,unbounded chỉ số hệ thống M-GEO-18 (đếm gộp mọi trường, không trả dữ liệu
            -- của trường nào)
            """)
        .param("from", java.sql.Timestamp.from(from))
        .param("to", java.sql.Timestamp.from(to))
        .query((rs, i) -> new long[] {rs.getLong(1), rs.getLong(2)})
        .single();
  }
}
