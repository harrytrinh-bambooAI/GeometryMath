package vn.geo.server.settings.internal.repo;

import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** Bảng user_preference (V109): một hàng mỗi tài khoản, lọc theo trường của thẻ. */
@Repository
public class PreferenceRepository {

  private final TenantJdbc jdbc;

  public PreferenceRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<String> findTheme(UUID userId) {
    return jdbc.sql("SELECT theme FROM user_preference WHERE org_id = :orgId AND user_id = :userId")
        .param("userId", userId)
        .query(String.class)
        .optional();
  }

  /** Ghi đè theme; false nếu tài khoản không có trong trường của thẻ (khóa ngoại tới app_user). */
  public boolean saveTheme(UUID userId, String theme) {
    try {
      jdbc.sql(
              """
              INSERT INTO user_preference (org_id, user_id, theme) VALUES (:orgId, :userId, :theme)
              ON CONFLICT (org_id, user_id) DO UPDATE SET theme = EXCLUDED.theme, updated_at = now()
              """)
          .param("userId", userId)
          .param("theme", theme)
          .update();
      return true;
    } catch (DataIntegrityViolationException e) {
      return false;
    }
  }
}
