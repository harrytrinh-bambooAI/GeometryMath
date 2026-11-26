package vn.geo.server.notify.internal.repo;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/** MÓC KIỂM THỬ S3-E4 (chỉ dev/test): chèn thẳng thông báo thử cho một người dùng. */
@Repository
@Profile({"dev", "test"})
public class NotifyHookRepository {

  private final JdbcClient jdbc;

  public NotifyHookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Thông báo Chưa đọc tạo cách đây ageDays ngày (lùi thêm backSeconds giây để giữ thứ tự). */
  public void insert(UUID userId, String body, int ageDays, int backSeconds) {
    jdbc.sql(
            """
            INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body, created_at)
            SELECT :id, u.org_id, u.id, 'new_set', :eventId, NULL, :body,
                   now() - make_interval(days => :ageDays, secs => :backSeconds)
            FROM app_user u WHERE u.id = :userId
            -- sqllint:allow=tenant,ownership móc kiểm thử dev/test: trường lấy từ chính người nhận (iam)
            """)
        .param("id", Uuid7.next())
        .param("eventId", Uuid7.next())
        .param("body", body)
        .param("ageDays", ageDays)
        .param("backSeconds", backSeconds)
        .param("userId", userId)
        .update();
  }
}
