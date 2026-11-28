package vn.geo.server.it.s2e7;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.library.service.PresentService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E7 — tích hợp (máy chủ + CSDL): hỏi lại theo giờ máy chủ (US-S2E7-03-BV1, FC3) và mốc bật lại
 * hỏi đáp AI (US-S2E7-02-BV1). Dữ liệu: db/seed/e2e/850_s2e67_core.sql, làn 0 của từng TC.
 */
class PresentS2E7IT extends AbstractE1IT {

  @Autowired PresentService present;

  @AfterEach
  void restoreClock() {
    present.overrideClockForTest(null);
  }

  /** Mã cố định F-S2-(B + k), B = 855000000 + TC × 100000 (S2-E7, làn 0). */
  static UUID f(int tc, int k) {
    return UUID.fromString(
        String.format("00000000-0000-7000-8002-%012d", 855000000L + tc * 100000L + k));
  }

  String trang(int tc) {
    return token(f(tc, 1), f(tc, 2), Role.TEACHER);
  }

  Instant releaseAt(int tc) {
    return jdbc.sql("SELECT release_at FROM assignment WHERE id = :id")
        .param("id", f(tc, 9001))
        .query(OffsetDateTime.class)
        .single()
        .toInstant();
  }

  JsonNode preflight(int tc, String... headers) throws Exception {
    HttpResponse<String> r =
        headers.length == 0
            ? get("/api/v1/teacher/present/" + f(tc, 101) + "/preflight", trang(tc))
            : get("/api/v1/teacher/present/" + f(tc, 101) + "/preflight", trang(tc), "s2e7-" + tc);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    assertThat(r.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
    return json(r);
  }

  @Test
  @DisplayName("TC-S2E7-019 Bật lại sau 5 giây là hạn, 6 giây là quá")
  void tc019_reconnectWithinFive() throws Exception {
    HttpResponse<String> r = get("/api/v1/teacher/present/" + f(19, 101), trang(19));
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    int seconds = json(r).path("reconnectSeconds").asInt();
    // Màn chiếu kiểm mạng và bật lại hỏi đáp AI chậm nhất sau reconnectSeconds giây: đúng hạn 5,
    // không tới 6.
    assertThat(seconds).isEqualTo(5).isLessThan(6);
    assertThat(json(r).path("steps").size()).isEqualTo(4);
  }

  @Test
  @DisplayName("TC-S2E7-029 Giờ máy chủ bằng release_at thì không hỏi, trước 1 giây thì hỏi")
  void tc029_serverClockBoundary() throws Exception {
    Instant r = releaseAt(29);
    present.overrideClockForTest(Clock.fixed(r.minusSeconds(1), ZoneOffset.UTC));
    JsonNode before = preflight(29);
    assertThat(before.path("ask").asBoolean()).isTrue();
    assertThat(before.path("lockedClasses").get(0).asString()).isEqualTo("11A1");
    present.overrideClockForTest(Clock.fixed(r, ZoneOffset.UTC));
    JsonNode at = preflight(29);
    assertThat(at.path("ask").asBoolean()).isFalse();
    assertThat(at.path("lockedClasses").size()).isZero();
  }

  @Test
  @DisplayName("TC-S2E7-032 Giờ máy giáo viên lệch không đổi kết quả hỏi")
  void tc032_clientClockIgnored() throws Exception {
    // Máy giáo viên chạy nhanh 3 giờ (đã qua release_at = T0 + 2 giờ): máy chủ không nhận giờ nào
    // từ máy khách.
    JsonNode r = preflight(32, "skew");
    assertThat(r.path("ask").asBoolean()).isTrue();
    assertThat(r.path("lockedClasses").get(0).asString()).isEqualTo("11A1");
    Instant serverNow = Instant.parse(r.path("serverNow").asString());
    assertThat(serverNow).isBefore(releaseAt(32));
  }
}
