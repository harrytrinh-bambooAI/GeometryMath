package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * AuditEvent chặng 3 trên audit_log (US-S3E5-01, PLAN A8). Ghi CHỈ qua fn_audit_event (bảng chỉ
 * thêm); đọc theo lớp cho chủ lớp. Không có câu lệnh sửa hay xóa (BR-S3E5-05).
 */
@Repository
public class AuditEventRepository {

  public record EventRow(
      long id,
      String action,
      String actorCode,
      String actorRole,
      String targetType,
      UUID targetId,
      String targetUserCode,
      UUID classId,
      Instant before,
      Instant after,
      String outcome,
      Instant at) {}

  /** M-S3-05: đếm sự kiện thành công của một lớp theo loại (đi theo audit_log_class_action_idx). */
  public static final String COUNT_SUCCESS =
      """
      SELECT count(*) FROM audit_log l
      WHERE l.org_id = :orgId AND l.class_id = :classId AND l.action = CAST(:action AS audit_action)
        AND l.outcome = 'success'
      -- sqllint:allow=unbounded đếm theo chỉ mục audit_log_class_action_idx (một lớp, một loại)
      """;

  private final TenantJdbc jdbc;

  public AuditEventRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Một AuditEvent Thành công, cùng giao dịch với thao tác (BR-S3E5-04). */
  public long recordSuccess(
      UUID actor,
      String actorRole,
      String action,
      String targetType,
      UUID targetId,
      UUID classId,
      Instant before,
      Instant after,
      String requestKey) {
    return jdbc.sql(
            """
            SELECT fn_audit_event(:orgId, :actor, :role, :action, :targetType, :target, :classId,
                                  CAST(:before AS timestamptz), CAST(:after AS timestamptz), 'success',
                                  CAST(:key AS text))
            """)
        .param("actor", actor)
        .param("role", actorRole)
        .param("action", action)
        .param("targetType", targetType)
        .param("target", targetId)
        .param("classId", classId)
        .param("before", ts(before))
        .param("after", ts(after))
        .param("key", requestKey)
        .query(Long.class)
        .single();
  }

  /** Bản ghi Từ chối (BR-S3E5-07): chỉ loại, người gọi, loại đối tượng, giờ máy chủ. */
  public long recordDenied(UUID actor, String actorRole, String action, String targetType) {
    return jdbc.sql(
            """
            SELECT fn_audit_event(:orgId, :actor, :role, :action, :targetType, CAST(NULL AS uuid), CAST(NULL AS uuid),
                                  CAST(NULL AS timestamptz), CAST(NULL AS timestamptz), 'denied', CAST(NULL AS text))
            """)
        .param("actor", actor)
        .param("role", actorRole)
        .param("action", action)
        .param("targetType", targetType)
        .query(Long.class)
        .single();
  }

  /** Yêu cầu cùng khóa chống lặp đã có tác dụng chưa (BR-S3E4-16). */
  public boolean exists(UUID actor, String action, UUID targetId, String requestKey) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM audit_log l
                           WHERE l.org_id = :orgId AND l.actor_user_id = :actor
                             AND l.action = CAST(:action AS audit_action)
                             AND l.target_id = :target AND l.request_key = :key LIMIT 1)
            """)
        .param("actor", actor)
        .param("action", action)
        .param("target", targetId)
        .param("key", requestKey)
        .query(Boolean.class)
        .single();
  }

  /** Nhãn thay mã người dùng của tài khoản đã xóa (BR-S3E5-53, bia mộ V415). */
  public static final String DELETED_USER = "đã xóa";

  /**
   * Nhật ký của một lớp, mới nhất trước; chỉ sự kiện Thành công của ba loại chặng 3. LEFT JOIN
   * (Dev-S3E5-16): người làm hoặc học sinh đã xóa tài khoản (mã người dùng thành UUID 0) vẫn có
   * dòng, mã hiện "đã xóa".
   */
  public List<EventRow> ofClass(UUID classId) {
    return jdbc.sql(
            """
            SELECT l.id, l.action::text, CASE WHEN ua.id IS NULL THEN :deleted ELSE ua.user_code END,
                   l.actor_role, l.target_type, l.target_id,
                   CASE WHEN l.target_type = 'user' AND ut.id IS NULL THEN :deleted ELSE ut.user_code END,
                   l.class_id, l.before_release_at, l.after_release_at, l.outcome, l.occurred_at
            FROM audit_log l
            LEFT JOIN app_user ua ON ua.org_id = :orgId AND ua.id = l.actor_user_id
            LEFT JOIN app_user ut ON ut.org_id = :orgId AND ut.id = l.target_id AND l.target_type = 'user'
            WHERE l.org_id = :orgId AND l.class_id = :classId AND l.outcome = 'success'
            ORDER BY l.id DESC
            LIMIT 200
            -- sqllint:allow=ownership mã người dùng (iam) của người thực hiện và của học sinh được đặt lại mật khẩu
            """)
        .param("classId", classId)
        .param("deleted", DELETED_USER)
        .query(AuditEventRepository::row)
        .list();
  }

  public long countSuccess(UUID classId, String action) {
    return jdbc.sql(COUNT_SUCCESS)
        .param("classId", classId)
        .param("action", action)
        .query(Long.class)
        .single();
  }

  /** Lớp do chính giáo viên dạy (người khác, lớp lạ: false → NOT_FOUND). */
  public boolean ownsClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM class c WHERE c.org_id = :orgId AND c.id = :classId
                             AND c.teacher_id = :teacherId LIMIT 1)
            -- sqllint:allow=ownership đọc lớp (iam) để kiểm chủ lớp trước khi trả nhật ký
            """)
        .param("classId", classId)
        .param("teacherId", teacherId)
        .query(Boolean.class)
        .single();
  }

  private static EventRow row(ResultSet rs, int i) throws SQLException {
    return new EventRow(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getObject(6, UUID.class),
        rs.getString(7),
        rs.getObject(8, UUID.class),
        instant(rs, 9),
        instant(rs, 10),
        rs.getString(11),
        instant(rs, 12));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }

  private static OffsetDateTime ts(Instant i) {
    return i == null ? null : i.atOffset(ZoneOffset.UTC);
  }
}
