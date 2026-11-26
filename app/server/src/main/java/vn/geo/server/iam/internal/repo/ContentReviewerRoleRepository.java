package vn.geo.server.iam.internal.repo;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/** Đọc vai thêm (user_role_grant, V230). Ứng dụng không ghi bảng này. */
@Repository
public class ContentReviewerRoleRepository {

  private final TenantJdbc jdbc;

  public ContentReviewerRoleRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public boolean hasReviewerRole(UUID userId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM user_role_grant
                           WHERE org_id = :orgId AND user_id = :user AND role = 'content_reviewer')
            """)
        .param("user", userId)
        .query(Boolean.class)
        .single();
  }
}
