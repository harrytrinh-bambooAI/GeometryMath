package vn.geo.server.it.s2e6;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.library.service.LibraryService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E6 — tích hợp (máy chủ + CSDL): bản Kho chung độc lập với bản Của tôi (US-S2E6-02-SC4), cửa sổ
 * trượt 60 phút của giới hạn thêm đề (US-S2E6-03-BV2) và bộ đếm chung mọi đường tạo đề
 * (US-S2E6-03-FC5). Dữ liệu: db/seed/e2e/850_s2e67_core.sql, làn 0 của từng TC.
 */
class LibraryS2E6IT extends AbstractE1IT {

  @Autowired LibraryService library;

  @AfterEach
  void restoreClock() {
    library.overrideClockForTest(null);
  }

  /** Mã cố định F-S2-(B + k), B = 850000000 + TC × 100000 (S2-E6, làn 0). */
  static UUID f(int tc, int k) {
    return UUID.fromString(
        String.format("00000000-0000-7000-8002-%012d", 850000000L + tc * 100000L + k));
  }

  String as(int tc, int userK, Role role) {
    return token(f(tc, 1), f(tc, userK), role);
  }

  HttpResponse<String> create(String t, String title) throws Exception {
    return postJson(
        "/api/v1/teacher/library/problems",
        t,
        mapper
            .createObjectNode()
            .put("title", title)
            .put("statement", "Tam giác ABC có AB = 3, AC = 4. Tính BC.")
            .toString());
  }

  void at(Instant t) {
    library.overrideClockForTest(Clock.fixed(t, ZoneOffset.UTC));
  }

  @Test
  @DisplayName("TC-S2E6-023 Sửa bản Của tôi không đổi bản Kho chung")
  void tc023_independentCopy() throws Exception {
    UUID doc = f(23, 101);
    UUID lib = f(23, 101 + 7000);
    String lan = as(23, 4, Role.TEACHER);
    JsonNode before = json(get("/api/v1/teacher/library/shared/" + lib, lan));
    assertThat(before.path("steps").size()).isEqualTo(4);

    var body = mapper.createObjectNode().put("baseVersion", 1);
    body.putArray("steps").add("Bước mới hoàn toàn khác.").add("Vậy góc bằng 60°.");
    HttpResponse<String> saved =
        postJson(
            "/api/v1/teacher/solutions/" + doc + "/versions",
            as(23, 2, Role.TEACHER),
            body.toString());
    assertThat(saved.statusCode()).as(saved.body()).isEqualTo(200);

    JsonNode after = json(get("/api/v1/teacher/library/shared/" + lib, lan));
    assertThat(after.path("steps")).isEqualTo(before.path("steps"));
    assertThat(after.path("statement").asString()).isEqualTo(before.path("statement").asString());
    assertThat(after.path("machineLabel").asString())
        .isEqualTo(before.path("machineLabel").asString());
    assertThat(after.path("humanLabel").asString()).isEqualTo("Giáo viên đã duyệt");
    assertThat(after.path("steps").get(3).asString()).contains("45°");
  }

  @Test
  @DisplayName("TC-S2E6-031 Cửa sổ trượt: đề đầu hết hạn sau 60 phút thì thêm được")
  void tc031_slidingWindow() throws Exception {
    String trang = as(31, 2, Role.TEACHER);
    Instant t0 =
        Instant.now().plus(Duration.ofDays(2)).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    for (int i = 0; i < 20; i++) {
      at(t0.plusSeconds(60L * i));
      assertThat(create(trang, "Đề " + i).statusCode()).isEqualTo(201);
    }
    at(t0.plus(Duration.ofMinutes(59)).plusSeconds(59));
    HttpResponse<String> blocked = create(trang, "Đề 59:59");
    assertThat(blocked.statusCode()).as(blocked.body()).isEqualTo(429);
    assertThat(json(blocked).path("code").asString()).isEqualTo("RATE_LIMITED");
    assertThat(blocked.headers().firstValue("Retry-After")).isPresent();
    at(t0.plus(Duration.ofMinutes(60)));
    assertThat(create(trang, "Đề 60:00").statusCode()).isEqualTo(201);
  }

  @Test
  @DisplayName("TC-S2E6-032 Bộ đếm tính chung mọi đường tạo đề")
  void tc032_allChannels() throws Exception {
    String trang = as(32, 2, Role.TEACHER);
    for (int i = 0; i < 10; i++) {
      assertThat(create(trang, "Dán chữ " + i).statusCode()).isEqualTo(201);
    }
    // 5 đề bằng ảnh, 5 bằng import: đường S2-E3/S2-E4 ghi bộ đếm qua content.api.LibraryQuota (cùng
    // bảng).
    jdbc.sql(
            """
            INSERT INTO problem_create_event (id, org_id, user_id, channel, created_at)
            SELECT gen_random_uuid(), :org, :user, CASE WHEN i <= 5 THEN 'image' ELSE 'import' END, now()
            FROM generate_series(1, 10) i
            """)
        .param("org", f(32, 1))
        .param("user", f(32, 2))
        .update();
    long docs = count("SELECT count(*) FROM solution_doc WHERE owner_user_id = '" + f(32, 2) + "'");
    UUID lib = f(32, 110 + 7000);
    HttpResponse<String> copy =
        postJson("/api/v1/teacher/library/shared/" + lib + "/copy", trang, "{}");
    assertThat(copy.statusCode()).as(copy.body()).isEqualTo(429);
    assertThat(json(copy).path("code").asString()).isEqualTo("RATE_LIMITED");
    assertThat(create(trang, "Dán chữ 21").statusCode()).isEqualTo(429);
    assertThat(count("SELECT count(*) FROM solution_doc WHERE owner_user_id = '" + f(32, 2) + "'"))
        .isEqualTo(docs);
  }
}
