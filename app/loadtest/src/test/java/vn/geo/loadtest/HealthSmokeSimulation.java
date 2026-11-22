package vn.geo.loadtest;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

/**
 * Kịch bản KHÓI: chứng minh bộ công cụ chạy được tới máy chủ thật (endpoint /health). KHÔNG phải
 * thử tải chính thức; kịch bản đọc/ghi/hỗn hợp theo NFR-GEO-001…012 viết khi có endpoint thật.
 *
 * <p>Tham số (-D...): {@code geo.baseUrl} (mặc định http://127.0.0.1:8080), {@code geo.rps} (mặc
 * định 5), {@code geo.seconds} (mặc định 5).
 */
public class HealthSmokeSimulation extends Simulation {

  private static final String BASE_URL = System.getProperty("geo.baseUrl", "http://127.0.0.1:8080");
  private static final double RPS = Double.parseDouble(System.getProperty("geo.rps", "5"));
  private static final long SECONDS = Long.parseLong(System.getProperty("geo.seconds", "5"));

  private final HttpProtocolBuilder protocol =
      http.baseUrl(BASE_URL).acceptHeader("application/json").userAgentHeader("geo-loadtest");

  private final ScenarioBuilder health =
      scenario("health")
          .exec(
              http("GET /health")
                  .get("/health")
                  .check(status().is(200))
                  .check(jsonPath("$.status").is("UP")));

  {
    setUp(health.injectOpen(constantUsersPerSec(RPS).during(Duration.ofSeconds(SECONDS))))
        .protocols(protocol)
        .assertions(
            global().failedRequests().percent().is(0.0),
            global().responseTime().percentile(95.0).lt(500));
  }
}
