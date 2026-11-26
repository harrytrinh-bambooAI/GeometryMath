package vn.geo.server.assignment.internal.repo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * MÓC KIỂM THỬ S3-E4 (chỉ dev/test): bản ghi Từ chối (BR-S3E5-07) của một người gọi. Trả đúng các
 * cột có trong hàng để ca kiểm chứng minh hàng không mang mã đối tượng, lớp hay nội dung yêu cầu.
 */
@Repository
@Profile({"dev", "test"})
public class S3E4HookRepository {

  private final JdbcClient jdbc;

  public S3E4HookRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> denied(UUID actorId) {
    return jdbc.sql(
            """
            SELECT l.action::text, u.user_code, l.actor_role, l.target_type, l.outcome,
                   l.target_id IS NULL AND l.class_id IS NULL AND l.before_release_at IS NULL
                     AND l.after_release_at IS NULL AND l.request_key IS NULL AND l.ref_id IS NULL
            FROM audit_log l
            JOIN app_user u ON u.org_id = l.org_id AND u.id = l.actor_user_id
            WHERE l.actor_user_id = :actor AND l.outcome = 'denied'
            ORDER BY l.id DESC
            LIMIT 100
            -- sqllint:allow=tenant,ownership móc kiểm thử dev/test: bản ghi Từ chối của một người gọi (không có thẻ, không có trường)
            """)
        .param("actor", actorId)
        .query((rs, i) -> {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("type", rs.getString(1).toUpperCase(Locale.ROOT));
          m.put("actorCode", rs.getString(2));
          m.put("actorRole", rs.getString(3));
          m.put("targetType", rs.getString(4));
          m.put("result", "DENIED");
          m.put("bare", rs.getBoolean(6));
          return m;
        })
        .list();
  }
}
