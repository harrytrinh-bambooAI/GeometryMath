package vn.geo.server.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.common.redis.RedisKeys;

class FoundationIT extends AbstractIT {

  @Autowired JdbcClient jdbc;
  @Autowired StringRedisTemplate redis;
  @Autowired RedisKeys keys;

  @Test
  void healthIsUpAndEchoesRequestId() throws Exception {
    var r = get("/health", null);
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(json(r).get("status").asString()).isEqualTo("UP");
    assertThat(json(r).get("db").asString()).isEqualTo("UP");
    assertThat(json(r).get("redis").asString()).isEqualTo("UP");
    assertThat(r.headers().firstValue("X-Request-Id")).isPresent();
  }

  @Test
  void clientRequestIdIsKeptWhenSafeAndReplacedWhenNot() throws Exception {
    var client = java.net.http.HttpClient.newHttpClient();
    var ok =
        client.send(
            java.net.http.HttpRequest.newBuilder(
                    java.net.URI.create("http://127.0.0.1:" + port + "/health"))
                .header("X-Request-Id", "abc-12345678")
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString());
    assertThat(ok.headers().firstValue("X-Request-Id")).contains("abc-12345678");
    var bad =
        client.send(
            java.net.http.HttpRequest.newBuilder(
                    java.net.URI.create("http://127.0.0.1:" + port + "/health"))
                .header("X-Request-Id", "a b;<script>")
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString());
    assertThat(bad.headers().firstValue("X-Request-Id").orElseThrow()).doesNotContain("<");
  }

  @Test
  void metricsEndpointsExposeHikariAndHttp() throws Exception {
    get("/health", null);
    var r = get("/actuator/prometheus", null);
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(r.body()).contains("hikaricp_connections_max").contains("http_server_requests");
  }

  @Test
  void migrationsRanOnCleanSchema() {
    assertThat(
            jdbc.sql("SELECT count(*) FROM flyway_schema_history WHERE success")
                .query(Long.class)
                .single())
        .isGreaterThanOrEqualTo(2L);
    assertThat(
            jdbc.sql("SELECT status FROM health_probe WHERE id = 1 LIMIT 1")
                .query(String.class)
                .single())
        .isEqualTo("ok");
  }

  @Test
  void redisUsesTestKeyPrefix() {
    String key = keys.key("it", UUID.randomUUID().toString());
    assertThat(key).startsWith("geotest:");
    redis.opsForValue().set(key, "v");
    assertThat(redis.opsForValue().get(key)).isEqualTo("v");
    redis.delete(key);
  }

  @Test
  void errorsUseUnifiedVietnameseModel() throws Exception {
    var noToken = get("/api/v1/sample/items", null);
    assertThat(noToken.statusCode()).isEqualTo(401);
    assertThat(json(noToken).get("code").asString()).isEqualTo("UNAUTHENTICATED");
    assertThat(json(noToken).get("message").asString()).contains("đăng nhập");

    String t = tokenFor(UUID.randomUUID());
    var notFound = get("/api/v1/nope", t);
    assertThat(notFound.statusCode()).isEqualTo(404);
    assertThat(json(notFound).get("code").asString()).isEqualTo("NOT_FOUND");
    assertThat(json(notFound).get("requestId").asString()).isNotBlank();

    var wrongMethod = post("/api/v1/sample/items", t, "{}");
    assertThat(wrongMethod.statusCode()).isEqualTo(405);
    assertThat(json(wrongMethod).get("code").asString()).isEqualTo("METHOD_NOT_ALLOWED");
  }

  @Test
  void oversizedBodyIsRejectedWith413() throws Exception {
    var r = post("/api/v1/sample/items", tokenFor(UUID.randomUUID()), "x".repeat(2_000_000));
    assertThat(r.statusCode()).isEqualTo(413);
    assertThat(json(r).get("code").asString()).isEqualTo("PAYLOAD_TOO_LARGE");
  }
}
