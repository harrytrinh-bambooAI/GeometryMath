package vn.geo.server.common.health.repo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class HealthRepository {

  private final JdbcClient jdbc;

  public HealthRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Đọc dòng thăm dò của V1__baseline để xác nhận kết nối và schema. */
  public boolean probe() {
    return jdbc.sql("SELECT status FROM health_probe WHERE id = 1 LIMIT 1")
        .query(String.class)
        .optional()
        .isPresent();
  }
}
