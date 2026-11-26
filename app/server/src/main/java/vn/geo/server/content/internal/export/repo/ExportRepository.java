package vn.geo.server.content.internal.export.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** SQL của xuất đề và liên kết chỉ xem (S2-E9, US-S2E9-02). */
@Repository
public class ExportRepository {

  /**
   * Đề "Của tôi" để xuất.
   *
   * @param approvedVersion phiên bản "Giáo viên đã duyệt" mới nhất (null: chưa duyệt)
   * @param lockedAssigned đề đang giao cho lớp mà lời giải còn khóa (chưa đặt giờ hoặc chưa tới giờ
   *     mở)
   */
  public record DocRow(
      UUID id,
      String title,
      String statement,
      String kind,
      String figure,
      String machineStatus,
      String humanStatus,
      Integer approvedVersion,
      String approvedMachineStatus,
      boolean lockedAssigned,
      boolean variant) {}

  public record LinkRow(
      UUID id, UUID docId, String hint, int days, Instant createdAt, Instant expiresAt) {}

  /** Nội dung người mở liên kết được thấy (không lời giải, không danh tính giáo viên). */
  public record SharedDoc(String title, String statement, String figure, String machineStatus) {}

  private final TenantJdbc jdbc;
  private final JdbcClient plain;

  public ExportRepository(TenantJdbc jdbc, JdbcClient plain) {
    this.jdbc = jdbc;
    this.plain = plain;
  }

  private static OffsetDateTime odt(Instant t) {
    return t.atOffset(ZoneOffset.UTC);
  }

  private static final String DOC_SELECT =
      """
      SELECT d.id, d.title, d.statement,
             CASE WHEN (d.figure -> 'plane') IS NOT NULL THEN 'plane' ELSE 'space' END AS kind,
             d.figure::text AS figure, v.machine_status::text AS machine_status, v.human_status::text AS human_status,
             ap.version AS approved_version, ap.machine_status::text AS approved_machine,
             EXISTS (SELECT 1 FROM assignment a
                     WHERE a.org_id = :orgId AND a.kind = 'class' AND a.assigned_by = :owner AND a.archived_at IS NULL
                       AND (a.release_at IS NULL OR a.release_at > :now)
                       AND ((a.content_org_id = d.content_org_id AND a.problem_version_id = d.problem_version_id)
                            OR EXISTS (SELECT 1 FROM solution_assignment_pin p
                                       WHERE p.org_id = :orgId AND p.assignment_id = a.id AND p.doc_id = d.id))
                     LIMIT 1) AS locked_assigned,
             EXISTS (SELECT 1 FROM problem_variant pv WHERE pv.org_id = :orgId AND pv.doc_id = d.id) AS is_variant
      FROM solution_doc d
      LEFT JOIN solution_version v ON v.org_id = :orgId AND v.doc_id = d.id AND v.version = d.latest_version
      LEFT JOIN LATERAL (SELECT sv.version, sv.machine_status FROM solution_version sv
                         WHERE sv.org_id = :orgId AND sv.doc_id = d.id AND sv.human_status = 'teacher_approved'
                         ORDER BY sv.version DESC LIMIT 1) ap ON true
      WHERE d.org_id = :orgId AND d.owner_user_id = :owner
      -- sqllint:allow=ownership read model: bài giao lớp còn khóa lời giải của chính giáo viên (module assignment)
      """;

  private static DocRow doc(ResultSet rs) throws SQLException {
    return new DocRow(
        rs.getObject("id", UUID.class),
        rs.getString("title"),
        rs.getString("statement"),
        rs.getString("kind"),
        rs.getString("figure"),
        rs.getString("machine_status"),
        rs.getString("human_status"),
        (Integer) rs.getObject("approved_version"),
        rs.getString("approved_machine"),
        rs.getBoolean("locked_assigned"),
        rs.getBoolean("is_variant"));
  }

  /** Đề của giáo viên, mới nhất trước (tối đa 200 cho màn chọn đề để xuất). */
  public List<DocRow> mine(UUID owner, Instant now) {
    return jdbc.sql(DOC_SELECT + " ORDER BY d.created_at DESC, d.id DESC LIMIT 200")
        .param("owner", owner)
        .param("now", odt(now))
        .query((rs, i) -> doc(rs))
        .list();
  }

  public List<DocRow> docs(UUID owner, List<UUID> ids, Instant now) {
    return jdbc.sql(DOC_SELECT + " AND d.id = ANY(:ids) LIMIT 50")
        .param("owner", owner)
        .param("now", odt(now))
        .param("ids", ids.toArray(new UUID[0]))
        .query((rs, i) -> doc(rs))
        .list();
  }

  /** Bước giải của chủ sổ qua cổng của S2-E5 (fn_solution_content_teacher). */
  public Optional<String> ownerSteps(UUID owner, UUID docId, int version) {
    return jdbc.sql(
            "SELECT steps::text FROM fn_solution_content_teacher(:orgId, :user, :doc, :version)")
        .param("user", owner)
        .param("doc", docId)
        .param("version", version)
        .query(String.class)
        .optional();
  }

  public void insertConfirm(UUID id, UUID user, String docIdsJson, Instant at) {
    jdbc.sql(
            """
            INSERT INTO export_confirm_log (id, org_id, user_id, doc_ids, created_at)
            VALUES (:id, :orgId, :user, CAST(:docs AS jsonb), :at)
            """)
        .param("id", id)
        .param("user", user)
        .param("docs", docIdsJson)
        .param("at", odt(at))
        .update();
  }

  public long confirmCount(UUID user) {
    return jdbc.sql(
            "SELECT count(*) FROM export_confirm_log WHERE org_id = :orgId AND user_id = :user")
        .param("user", user)
        .query(Long.class)
        .single();
  }

  // ---------------------------------------------------------------- liên kết chỉ xem

  /** Khóa hàng đề của chính giáo viên trong giao dịch (đếm liên kết đang dùng không đua nhau). */
  public boolean lockOwnDoc(UUID owner, UUID docId) {
    return jdbc.sql(
            """
            SELECT id FROM solution_doc
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :owner FOR UPDATE
            """)
        .param("id", docId)
        .param("owner", owner)
        .query(UUID.class)
        .optional()
        .isPresent();
  }

  public List<LinkRow> activeLinks(UUID owner, UUID docId, Instant now) {
    return jdbc.sql(
            """
            SELECT l.id, l.doc_id, l.token_hint, l.days, l.created_at, l.expires_at
            FROM problem_share_link l
            WHERE l.org_id = :orgId AND l.doc_id = :doc AND l.created_by = :owner
              AND l.revoked_at IS NULL AND l.expires_at > :now
            ORDER BY l.created_at DESC, l.id DESC LIMIT 20
            """)
        .param("doc", docId)
        .param("owner", owner)
        .param("now", odt(now))
        .query((rs, i) ->
            new LinkRow(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getInt(4),
                rs.getObject(5, OffsetDateTime.class).toInstant(),
                rs.getObject(6, OffsetDateTime.class).toInstant()))
        .list();
  }

  public void insertLink(
      UUID id,
      UUID docId,
      UUID owner,
      byte[] hash,
      String hint,
      int days,
      Instant at,
      Instant until) {
    jdbc.sql(
            """
            INSERT INTO problem_share_link (id, org_id, doc_id, created_by, token_hash, token_hint, days, created_at,
                                            expires_at)
            VALUES (:id, :orgId, :doc, :owner, :hash, :hint, :days, :at, :until)
            """)
        .param("id", id)
        .param("doc", docId)
        .param("owner", owner)
        .param("hash", hash)
        .param("hint", hint)
        .param("days", days)
        .param("at", odt(at))
        .param("until", odt(until))
        .update();
  }

  public int revoke(UUID owner, UUID linkId, Instant at) {
    return jdbc.sql(
            """
            UPDATE problem_share_link SET revoked_at = :at
            WHERE org_id = :orgId AND id = :id AND created_by = :owner AND revoked_at IS NULL
            """)
        .param("id", linkId)
        .param("owner", owner)
        .param("at", odt(at))
        .update();
  }

  /**
   * Người mở liên kết (không đăng nhập, không có mã trường): đọc qua cổng fn_share_link_view; mã
   * sai, hết hạn hay đã thu hồi đều rỗng.
   */
  public Optional<SharedDoc> shared(byte[] hash, Instant now) {
    return plain
        .sql(
            "SELECT title, statement, figure::text, machine_status FROM fn_share_link_view(:hash, :now)")
        .param("hash", hash)
        .param("now", odt(now))
        .query((rs, i) ->
            new SharedDoc(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }
}
