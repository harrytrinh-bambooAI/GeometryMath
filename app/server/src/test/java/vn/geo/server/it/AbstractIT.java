package vn.geo.server.it;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.testing.StatementCountingConfig;

/** Nền cho kiểm thử tích hợp: máy chủ thật trên cổng ngẫu nhiên, geo_test@54329, Redis 6389. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestFlywayConfig.class, StatementCountingConfig.class})
public abstract class AbstractIT {

  @LocalServerPort protected int port;
  @Autowired protected TokenService tokens;
  @Autowired protected JsonMapper mapper;
  private final HttpClient http = HttpClient.newHttpClient();

  protected String tokenFor(UUID orgId) {
    return tokens.issue(new AuthPrincipal(orgId, Uuid7.next(), Role.TEACHER));
  }

  protected HttpResponse<String> get(String path, String bearer)
      throws IOException, InterruptedException {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
    if (bearer != null) {
      b.header("Authorization", "Bearer " + bearer);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  /** GET kèm X-Request-Id tự chọn (để đếm câu lệnh SQL của đúng yêu cầu này). */
  protected HttpResponse<String> get(String path, String bearer, String requestId)
      throws IOException, InterruptedException {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("X-Request-Id", requestId)
            .GET();
    if (bearer != null) {
      b.header("Authorization", "Bearer " + bearer);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> post(String path, String bearer, String body)
      throws IOException, InterruptedException {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));
    if (bearer != null) {
      b.header("Authorization", "Bearer " + bearer);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected JsonNode json(HttpResponse<String> r) {
    return mapper.readTree(r.body());
  }
}
