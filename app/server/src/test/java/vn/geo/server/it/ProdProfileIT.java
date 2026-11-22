package vn.geo.server.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;

/**
 * Máy chủ chạy profile prod (khóa thật đặt qua biến): không lộ {@code /dev/token} và móc kiểm thử
 * {@code /test-hooks/*} (soát Đợt 0, PO-51). Không thẻ => 401; có thẻ hợp lệ => 404 (không có bộ
 * điều khiển). Flyway tắt: chỉ đọc cấu hình, không đụng schema của IT khác.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "GEO_DB_URL=jdbc:postgresql://127.0.0.1:${GEO_PG_PORT:54329}/geo_test?ApplicationName=geo-prod-it",
      "GEO_AUTH_SECRET=khoa-that-cua-moi-truong-that-0123456789",
      "GEO_AUTH_CODE_PEPPER=pepper-that-cua-moi-truong-that-0123456789",
      "GEO_CURSOR_KEY=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
      "GEO_REDIS_PREFIX=geoprodit:",
      "spring.flyway.enabled=false"
    })
@ActiveProfiles("prod")
class ProdProfileIT {

  @LocalServerPort int port;
  @Autowired TokenService tokens;
  private final HttpClient http = HttpClient.newHttpClient();

  private int post(String path, String bearer) throws Exception {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"));
    if (bearer != null) {
      b.header("Authorization", "Bearer " + bearer);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString()).statusCode();
  }

  @Test
  void devTokenAndTestHooksAreNotExposed() throws Exception {
    for (String path :
        new String[] {"/dev/token", "/test-hooks/auth-failures/reset", "/test-hooks/ai/reset"}) {
      assertThat(post(path, null)).as("không thẻ: %s", path).isEqualTo(401);
    }
    String t = tokens.issue(new AuthPrincipal(UUID.randomUUID(), UUID.randomUUID(), Role.TEACHER));
    for (String path :
        new String[] {"/dev/token", "/test-hooks/auth-failures/reset", "/test-hooks/ai/reset"}) {
      assertThat(post(path, t)).as("có thẻ: %s", path).isEqualTo(404);
    }
  }

  @Test
  void healthStaysPublic() throws Exception {
    HttpResponse<String> r =
        http.send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health")).build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(r.statusCode()).isEqualTo(200);
  }
}
