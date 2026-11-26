package vn.geo.server.it.s2e9;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.export.service.ShareLinkService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E9 — tích hợp (máy chủ + CSDL): đáp số biến thể do engine tính, không gọi AI (US-S2E9-01-FC6);
 * liên kết chỉ xem hết hạn đúng mốc 7 ngày (US-S2E9-02-BV2); dò mã liên kết bị giới hạn theo địa
 * chỉ mạng (US-S2E9-02-FC4). Dữ liệu: db/seed/e2e/870_s2e9_core.sql, làn 0 của từng TC.
 */
class VariantShareS2E9IT extends AbstractE1IT {

  @Autowired ShareLinkService links;
  private final HttpClient client = HttpClient.newHttpClient();

  @AfterEach
  void restoreClock() {
    links.overrideClockForTest(null);
  }

  /** Mã cố định F-S2-(B + k), B = 870000000 + TC × 100000 (làn 0). */
  static UUID f(int tc, int k) {
    return UUID.fromString(
        String.format("00000000-0000-7000-8002-%012d", 870000000L + tc * 100000L + k));
  }

  String trang(int tc) {
    return token(f(tc, 1), f(tc, 2), Role.TEACHER);
  }

  /** Mở liên kết không đăng nhập từ một địa chỉ mạng cho trước. */
  HttpResponse<String> open(String token, String ip) throws Exception {
    HttpRequest req =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/share/" + token))
            .header("X-Forwarded-For", ip)
            .GET()
            .build();
    return client.send(req, HttpResponse.BodyHandlers.ofString());
  }

  String createLink(int tc, int docK) throws Exception {
    HttpResponse<String> r =
        postJson(
            "/api/v1/teacher/share-links",
            trang(tc),
            mapper.createObjectNode().put("docId", f(tc, docK).toString()).toString());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
    JsonNode link = json(r);
    assertThat(link.path("days").asInt()).isEqualTo(7);
    return link.path("token").asString();
  }

  @Test
  @DisplayName("TC-S2E9-013 Đáp số biến thể do engine tính, không gọi AI")
  void tc013_engineNotAi() throws Exception {
    long aiBefore = count("SELECT count(*) FROM ai_call_log");
    String engineSql =
        "SELECT count(*) FROM problem_variant WHERE answer_source = 'engine' AND org_id = '"
            + f(13, 1)
            + "'";
    long variantsBefore = count(engineSql);
    HttpResponse<String> r =
        postJson(
            "/api/v1/teacher/variants",
            trang(13),
            mapper
                .createObjectNode()
                .put("docId", f(13, 102).toString())
                .put("count", 3)
                .put("seed", 42)
                .toString());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
    JsonNode batch = json(r);
    assertThat(batch.path("created").asInt()).isEqualTo(3);
    assertThat(batch.path("items").size()).isEqualTo(3);
    for (JsonNode v : batch.path("items")) {
      assertThat(v.path("answerSource").asString()).isEqualTo("engine");
      assertThat(v.path("machineLabel").asString()).isEqualTo("Cần xem lại");
      assertThat(v.path("humanLabel").asString()).isEqualTo("AI soạn");
    }
    assertThat(count("SELECT count(*) FROM ai_call_log")).isEqualTo(aiBefore);
    assertThat(count(engineSql) - variantsBefore).isEqualTo(3);
    // phiên bản 1 của biến thể: "AI soạn", "Cần xem lại", chưa ai duyệt
    assertThat(
            count(
                "SELECT count(*) FROM solution_version v JOIN problem_variant pv ON pv.doc_id ="
                    + " v.doc_id WHERE pv.org_id = '"
                    + f(13, 1)
                    + "' AND v.human_status = 'ai_drafted' AND v.machine_status = 'needs_review'"
                    + " AND v.approved_at IS NULL"))
        .isGreaterThanOrEqualTo(3);
  }

  @Test
  @DisplayName("TC-S2E9-022 Liên kết hết hạn đúng mốc 7 ngày")
  void tc022_expiresAtSevenDays() throws Exception {
    Instant t0 = Instant.now().plus(Duration.ofDays(3)).truncatedTo(ChronoUnit.SECONDS);
    links.overrideClockForTest(Clock.fixed(t0, ZoneOffset.UTC));
    String token = createLink(22, 100);

    links.overrideClockForTest(
        Clock.fixed(t0.plus(Duration.ofDays(7)).minusSeconds(1), ZoneOffset.UTC));
    HttpResponse<String> before = open(token, "203.0.113.22");
    assertThat(before.statusCode()).as(before.body()).isEqualTo(200);
    assertThat(json(before).path("statement").asString()).contains("AB = 5, AC = 12, BC = 13");

    links.overrideClockForTest(Clock.fixed(t0.plus(Duration.ofDays(7)), ZoneOffset.UTC));
    HttpResponse<String> expired = open(token, "203.0.113.22");
    HttpResponse<String> wrong = open("khong-co-ma-nay-0123456789abcdefghijklmnop", "203.0.113.22");
    assertThat(expired.statusCode()).isEqualTo(404);
    assertThat(json(expired).path("code").asString()).isEqualTo("NOT_FOUND");
    assertThat(json(expired).path("message").asString()).isEqualTo("Liên kết không còn dùng được");
    // cùng thông báo với mã sai
    assertThat(wrong.statusCode()).isEqualTo(404);
    assertThat(json(wrong).path("message").asString())
        .isEqualTo(json(expired).path("message").asString());
  }

  @Test
  @DisplayName("TC-S2E9-024 Dò mã liên kết bị giới hạn")
  void tc024_probingRateLimited() throws Exception {
    String token = createLink(24, 100);
    String ip = "203.0.113.24";
    assertThat(open(token, "203.0.113.25").statusCode()).isEqualTo(200);
    for (int i = 0; i < 100; i++) {
      HttpResponse<String> r =
          open("ma-sai-%03d-0123456789abcdefghijklmnopqrstuv".formatted(i), ip);
      assertThat(r.statusCode()).as("lần " + (i + 1)).isEqualTo(404);
      assertThat(json(r).path("code").asString()).isEqualTo("NOT_FOUND");
    }
    HttpResponse<String> limited = open(token, ip); // lần 101, mã ĐÚNG
    assertThat(limited.statusCode()).as(limited.body()).isEqualTo(429);
    assertThat(json(limited).path("code").asString()).isEqualTo("RATE_LIMITED");
    assertThat(limited.headers().firstValue("Retry-After")).isPresent();
    assertThat(limited.body()).doesNotContain("AB = 5");
    // địa chỉ mạng khác không bị ảnh hưởng
    assertThat(open(token, "203.0.113.25").statusCode()).isEqualTo(200);
  }
}
