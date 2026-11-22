package vn.geo.server.it;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Mỗi lần chạy IT: xóa sạch schema rồi migrate lại. Chốt chặn: chỉ chạy trên cơ sở dữ liệu
 * geo_test.
 */
@TestConfiguration
public class TestFlywayConfig {

  @Bean
  FlywayMigrationStrategy cleanThenMigrate() {
    return flyway -> {
      String url;
      try (var c = flyway.getConfiguration().getDataSource().getConnection()) {
        url = c.getMetaData().getURL();
      } catch (java.sql.SQLException e) {
        throw new IllegalStateException(e);
      }
      if (url == null || !url.contains("127.0.0.1:54329/geo_test")) {
        throw new IllegalStateException(
            "Từ chối xóa dữ liệu: IT chỉ được chạy trên geo_test@54329, nhận: " + url);
      }
      flyway.clean();
      flyway.migrate();
    };
  }
}
