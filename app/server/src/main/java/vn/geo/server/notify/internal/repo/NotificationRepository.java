package vn.geo.server.notify.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.id.Uuid7;

/**
 * Bảng notification (PLAN A9): thông báo trong ứng dụng của từng người nhận. Mọi câu đọc / đánh dấu
 * lọc theo trường và theo chính người nhận (BR-S3E4-24). Một sự kiện + một người nhận = tối đa một
 * hàng (ràng buộc notification_event_user_uq, BR-S3E4-22).
 */
@Repository
public class NotificationRepository {

  public record Row(
      UUID id, String kind, String body, UUID setId, Instant createdAt, Instant readAt) {}

  private static final String SELECT =
      "SELECT n.id, n.kind, n.body, n.set_id, n.created_at, n.read_at FROM notification n"
          + " WHERE n.org_id = :orgId AND n.user_id = :userId ";

  private final TenantJdbc jdbc;
  private final JdbcClient raw;

  public NotificationRepository(TenantJdbc jdbc, JdbcClient raw) {
    this.jdbc = jdbc;
    this.raw = raw;
  }

  /**
   * Tạo thông báo của một sự kiện cho mọi người nhận trong một câu lệnh; hàng trùng (sự kiện xử lý
   * lặp) bị ràng buộc duy nhất từ chối và bỏ qua. Trả số hàng mới.
   */
  public int insertAll(
      UUID orgId, UUID eventId, String kind, UUID setId, String body, List<UUID> users) {
    return insertAll(orgId, eventId, kind, setId, body, users, null, false);
  }

  /**
   * Như trên, với giờ tạo do sự kiện quy định (tác vụ nhắc US-S3E6-03 chạy theo đồng hồ của nó) và
   * tùy chọn bỏ qua người nhận đã có thông báo cùng loại của cùng bộ (lời giải đã mở theo lịch sau
   * khi Mở ngay đã báo: BR-S3E6-24).
   */
  public int insertAll(
      UUID orgId,
      UUID eventId,
      String kind,
      UUID setId,
      String body,
      List<UUID> users,
      Instant createdAt,
      boolean oncePerSetKind) {
    if (users.isEmpty()) {
      return 0;
    }
    String ids = users.stream().map(u -> Uuid7.next().toString()).collect(Collectors.joining(","));
    String userIds = users.stream().map(UUID::toString).collect(Collectors.joining(","));
    return raw.sql(
            """
            INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body, created_at)
            SELECT x.id, :orgId, x.user_id, :kind, :eventId, CAST(:setId AS uuid), :body,
                   COALESCE(CAST(:createdAt AS timestamptz), now())
            FROM unnest(CAST(string_to_array(:ids, ',') AS uuid[]),
                        CAST(string_to_array(:users, ',') AS uuid[])) AS x(id, user_id)
            WHERE NOT :once OR NOT EXISTS (
              SELECT 1 FROM notification o WHERE o.org_id = :orgId AND o.set_id = CAST(:setId AS uuid)
                AND o.kind = :kind AND o.user_id = x.user_id)
            ON CONFLICT ON CONSTRAINT notification_event_user_uq DO NOTHING
            """)
        .param("orgId", orgId)
        .param("kind", kind)
        .param("eventId", eventId)
        .param("setId", setId)
        .param("body", body)
        .param("ids", ids)
        .param("users", userIds)
        .param(
            "createdAt",
            createdAt == null ? null : OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC))
        .param("once", oncePerSetKind)
        .update();
  }

  /** Một trang, mới nhất trước (id UUIDv7 giảm dần); afterId = dòng cuối của trang trước. */
  public List<Row> page(UUID userId, UUID afterId, int fetchSize) {
    return jdbc.sql(
            SELECT
                + """
                AND (CAST(:afterId AS uuid) IS NULL OR n.id < CAST(:afterId AS uuid))
                ORDER BY n.id DESC
                LIMIT :limit
                """)
        .param("userId", userId)
        .param("afterId", afterId)
        .param("limit", fetchSize)
        .query(NotificationRepository::row)
        .list();
  }

  public Optional<Row> one(UUID userId, UUID id) {
    return jdbc.sql(SELECT + "AND n.id = :id LIMIT 1")
        .param("userId", userId)
        .param("id", id)
        .query(NotificationRepository::row)
        .optional();
  }

  public int unreadCount(UUID userId) {
    return jdbc.sql(
            """
            SELECT count(*) FROM notification n
            WHERE n.org_id = :orgId AND n.user_id = :userId AND n.read_at IS NULL
            """)
        .param("userId", userId)
        .query(Integer.class)
        .single();
  }

  public int markRead(UUID userId, UUID id) {
    return jdbc.sql(
            """
            UPDATE notification SET read_at = now()
            WHERE org_id = :orgId AND user_id = :userId AND id = :id AND read_at IS NULL
            """)
        .param("userId", userId)
        .param("id", id)
        .update();
  }

  public int markAllRead(UUID userId) {
    return jdbc.sql(
            """
            UPDATE notification SET read_at = now()
            WHERE org_id = :orgId AND user_id = :userId AND read_at IS NULL
            """)
        .param("userId", userId)
        .update();
  }

  /** Dọn dẹp toàn hệ thống: xóa thông báo tạo trước (bây giờ − days ngày) (BR-S3E4-27). */
  public int deleteOlderThan(int days) {
    return raw.sql(
            """
            DELETE FROM notification WHERE created_at < now() - make_interval(days => :days)
            -- sqllint:allow=tenant tác vụ nền dọn theo tuổi cho mọi trường (không có ngữ cảnh trường)
            """)
        .param("days", days)
        .update();
  }

  private static Row row(ResultSet rs, int i) throws SQLException {
    return new Row(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getObject(4, UUID.class),
        instant(rs, 5),
        instant(rs, 6));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
