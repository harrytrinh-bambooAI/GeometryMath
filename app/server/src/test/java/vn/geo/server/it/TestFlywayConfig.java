package vn.geo.server.it;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Mỗi lần chạy IT: xóa sạch schema rồi migrate lại. Chốt chặn: chỉ chạy trên cơ sở dữ liệu geo_test
 * trên 127.0.0.1 ở cổng GEO_PG_PORT (mặc định 54329).
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
      // Cổng theo GEO_PG_PORT (mặc định 54329) để nhiều worktree chạy song song, mỗi cái cụm riêng.
      String port = System.getenv().getOrDefault("GEO_PG_PORT", "54329");
      String expected = "jdbc:postgresql://127.0.0.1:" + port + "/geo_test";
      if (url == null || !(url.equals(expected) || url.startsWith(expected + "?"))) {
        throw new IllegalStateException(
            "Từ chối xóa dữ liệu: IT chỉ được chạy trên geo_test@" + port + ", nhận: " + url);
      }
      flyway.clean();
      flyway.migrate();
    };
  }
}
