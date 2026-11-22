package vn.geo.server.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.time.ZoneId;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.paging.PagingProperties;
import vn.geo.server.common.web.HttpProperties;

/**
 * Kiểm cấu hình đã nạp khớp bảng {@code app/docs/CONFIG.md} (NFR-GEO-054 "kiểm cấu hình"). Đổi một
 * giá trị ở đây thì đổi cả CONFIG.md.
 */
class ConfigIT extends AbstractIT {

  @Autowired DataSource dataSource;
  @Autowired JdbcClient jdbc;
  @Autowired DataRedisProperties redis;
  @Autowired PagingProperties paging;
  @Autowired HttpProperties http;
  @Autowired TimeProperties time;

  @Test
  void poolAndTimeoutsMatchConfigTable() throws Exception {
    HikariDataSource hikari = dataSource.unwrap(HikariDataSource.class);
    assertThat(hikari.getMaximumPoolSize()).isEqualTo(20);
    assertThat(hikari.getConnectionTimeout()).isEqualTo(2000);
    assertThat(jdbc.sql("SHOW statement_timeout").query(String.class).single()).isEqualTo("2s");
    assertThat(jdbc.sql("SHOW idle_in_transaction_session_timeout").query(String.class).single())
        .isEqualTo("10s");
    assertThat(redis.getTimeout()).isEqualTo(Duration.ofMillis(100));
  }

  @Test
  void pagingBodyAndTimeMatchConfigTable() {
    assertThat(paging.defaultLimit()).isEqualTo(20);
    assertThat(paging.maxLimitFor("any.list")).isEqualTo(100);
    assertThat(paging.maxLimitFor("content.problem-bank")).isEqualTo(50);
    assertThat(http.maxBodyBytes()).isEqualTo(1_048_576);
    assertThat(http.retryAfter()).isEqualTo(Duration.ofSeconds(2));
    assertThat(time.displayZone()).isEqualTo(ZoneId.of("Asia/Ho_Chi_Minh"));
  }

  @Test
  void testHooksAreLoadedOnlyThroughTestProfile() {
    assertThat(jdbc.sql("SELECT geo_testhooks_loaded()").query(Boolean.class).single()).isTrue();
  }

  @Test
  void serverTimesAreUtcIso8601() throws Exception {
    java.util.UUID org = vn.geo.server.common.id.Uuid7.next();
    jdbc.sql("INSERT INTO sample_item (id, org_id, title) VALUES (gen_random_uuid(), :org, 't')")
        .param("org", org)
        .update();
    var item = json(get("/api/v1/sample/items", tokenFor(org))).get("items").get(0);
    assertThat(item.get("createdAt").asString()).matches("\\d{4}-\\d{2}-\\d{2}T[0-9:.]+Z");
  }
}
