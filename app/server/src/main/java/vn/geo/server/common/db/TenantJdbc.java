package vn.geo.server.common.db;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import vn.geo.server.common.security.TenantContext;

/**
 * Cửa duy nhất để chạy SQL trên bảng thuộc trường: tự gắn tham số {@code :orgId} từ ngữ cảnh đã xác
 * thực. SQL phải dùng {@code org_id = :orgId}; bộ quét SQL kiểm tra điều này khi build.
 */
@Component
public class TenantJdbc {

  private final JdbcClient jdbc;

  public TenantJdbc(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public JdbcClient.StatementSpec sql(String sql) {
    return jdbc.sql(sql).param("orgId", TenantContext.orgId());
  }
}
