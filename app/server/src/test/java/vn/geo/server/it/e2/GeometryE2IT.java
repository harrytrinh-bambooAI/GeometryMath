package vn.geo.server.it.e2;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.service.SelfStudyService;
import vn.geo.server.content.internal.service.TemplateCatalogService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * E2 — tích hợp: dựng hình tự học (US-GEO-201-FC2, FC3; US-GEO-204), danh mục hình mẫu
 * (US-GEO-205), cảnh của bài giao và nhãn máy (US-GEO-201-SC3, US-GEO-203). Dữ liệu vàng E2E.
 */
class GeometryE2IT extends AbstractE1IT {

  static final String F074 =
      "Cho lăng trụ đứng ABC.A′B′C′ có đáy là tam giác ABC với AB = 1, BC = 1, CA = 3, AA′ = 2."
          + " Tính thể tích.";
  static final String DEGENERATE =
      "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 0 cm. Tính thể tích.";
  static final String F060 =
      "Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5."
          + " Tính khoảng cách từ A đến mặt phẳng (A′BC).";
  static final String F061 =
      "Cho hình bát diện đều cạnh a. Tính khoảng cách giữa hai mặt đối diện.";
  static final String MSG_18 =
      "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu.";

  @Autowired SelfStudyService selfStudy;
  @Autowired TemplateCatalogService catalog;

  private HttpResponse<String> build(String token, String statement) throws Exception {
    return postJson(
        "/api/v1/student/self-study",
        token,
        mapper.writeValueAsString(Map.of("statement", statement)));
  }

  private long studentProblems() {
    return count("SELECT count(*) FROM problem WHERE source = 'student_input'");
  }

  private long aiScenes() {
    return count("SELECT count(*) FROM scene_build WHERE coord_source = 'ai'");
  }

  private void assertMsg18NothingCreated(String statement) throws Exception {
    long p0 = studentProblems();
    long ai0 = aiScenes();
    long a0 = count("SELECT count(*) FROM assignment WHERE kind = 'self'");
    var r = build(an(), statement);
    assertThat(r.statusCode()).isEqualTo(400);
    JsonNode body = json(r);
    assertThat(body.path("code").asString()).isEqualTo("VALIDATION_FAILED");
    assertThat(body.path("message").asString()).isEqualTo(MSG_18);
    assertThat(body.path("details").get(0).path("field").asString()).isEqualTo("statement");
    // không có hình, không lưu đề, không lặng lẽ thay bằng tọa độ AI
    assertThat(body.has("scene")).isFalse();
    assertThat(studentProblems()).isEqualTo(p0);
    assertThat(aiScenes()).isEqualTo(ai0);
    assertThat(count("SELECT count(*) FROM assignment WHERE kind = 'self'")).isEqualTo(a0);
  }

  @Test
  @DisplayName("TC-GEO-0206 Ràng buộc mâu thuẫn (US-GEO-201-FC2)")
  void tc0206_contradictory() throws Exception {
    // AI giả lập trả cả tọa độ cho đề này (mock-ai/scene.json): hệ thống vẫn không dùng
    assertMsg18NothingCreated(F074);
  }

  @Test
  @DisplayName("TC-GEO-0207 Hình suy biến (US-GEO-201-FC3)")
  void tc0207_degenerate() throws Exception {
    assertMsg18NothingCreated(DEGENERATE);
  }

  @Test
  @DisplayName("TC-GEO-0226 Đề lạ được đếm vào tỉ lệ đề lạ (US-GEO-204-SC3)")
  void tc0226_foreignRate() throws Exception {
    Instant from = Instant.now().minusMillis(1);
    var r1 = build(an(), F060);
    assertThat(r1.statusCode()).isEqualTo(200);
    assertThat(json(r1).path("scene").path("coordSource").asString()).isEqualTo("engine");
    var r2 = build(an(), F061);
    assertThat(r2.statusCode()).isEqualTo(200);
    assertThat(json(r2).path("scene").path("coordSource").asString()).isEqualTo("ai");
    SelfStudyService.ForeignRate rate = selfStudy.foreignRate(from, Instant.now().plusSeconds(1));
    assertThat(rate.foreign()).isEqualTo(1);
    assertThat(rate.total()).isEqualTo(2);
    assertThat(rate.percent()).isEqualTo("50,0%");
  }

  @Test
  @DisplayName("TC-GEO-0230 Danh mục đủ hình (US-GEO-205-SC1)")
  void tc0230_catalogComplete() throws Exception {
    JsonNode items = json(get("/api/v1/geometry/templates", an())).path("items");
    List<String> kinds = new ArrayList<>();
    items.forEach(i -> kinds.add(i.path("solidKind").asString()));
    assertThat(kinds).contains("pyramid", "prism", "box", "cube", "cone", "cylinder", "sphere");
    for (JsonNode t : items) {
      JsonNode s =
          json(
              postJson(
                  "/api/v1/geometry/templates/" + t.path("code").asString() + "/scene",
                  an(),
                  "{}"));
      // hệ trục Oxyz bật được trên mọi hình
      assertThat(s.path("axes").path("available").asBoolean()).as(t.toString()).isTrue();
      assertThat(s.path("points").size()).isGreaterThan(0);
      if ("cube".equals(t.path("code").asString())) {
        // lập phương = hộp ba cạnh bằng nhau
        Map<String, double[]> p = points(s);
        double ab = dist(p.get("A"), p.get("B"));
        assertThat(dist(p.get("A"), p.get("D"))).isEqualTo(ab);
        assertThat(dist(p.get("A"), p.get("A′"))).isEqualTo(ab);
      }
    }
  }

  @Test
  @DisplayName("TC-GEO-0233 Hình mẫu thiếu giá trị mặc định (US-GEO-205-FC1)")
  void tc0233_missingDefault() throws Exception {
    JsonNode data =
        mapper.readTree(
            """
            [{"id": 91, "code": "e2_test_cone", "solid_kind": "cone", "name": "Nón thử",
              "param_spec": {"order": 91, "card": false, "builder": "cone",
                "params": [{"key": "r", "label": "Bán kính đáy", "default": 2},
                           {"key": "h", "label": "Chiều cao", "default": 3}]}},
             {"id": 92, "code": "e2_test_cyl", "solid_kind": "cylinder", "name": "Hình trụ",
              "param_spec": {"order": 92, "card": false, "builder": "cylinder",
                "params": [{"key": "r", "label": "Bán kính đáy", "default": 2},
                           {"key": "h", "label": "Chiều cao"}]}}]
            """);
    try {
      var report = catalog.load(data);
      assertThat(report.errors()).containsExactly("Hình trụ: thiếu giá trị mặc định của Chiều cao");
      assertThat(report.loaded()).containsExactly("e2_test_cone");
      assertThat(count("SELECT count(*) FROM solid_template WHERE code = 'e2_test_cyl'")).isZero();
    } finally {
      jdbc.sql("DELETE FROM solid_template WHERE code LIKE 'e2_test_%'").update();
    }
  }

  // ------------------------------------------------------------------ không gắn TC: hợp đồng cảnh

  @Test
  @DisplayName("Cảnh của BG-1: thẻ ràng buộc, nhãn máy, không có giá trị tính (US-GEO-201-SC3)")
  void bg1Scene() throws Exception {
    var r = get("/api/v1/student/assignments/" + BG1 + "/scene", an());
    assertThat(r.statusCode()).isEqualTo(200);
    JsonNode s = json(r);
    assertThat(s.path("schemaVersion").asInt()).isEqualTo(1);
    List<String> chips = new ArrayList<>();
    s.path("chips").forEach(c -> chips.add(c.path("text").asString()));
    assertThat(chips).containsExactly("SA = a", "AD = a");
    assertThat(s.path("verification").path("label").asString()).isEqualTo("Đã kiểm chứng");
    assertThat(r.body()).doesNotContain("45").doesNotContain("computed");
    // trường khác: NOT_FOUND
    var other =
        get("/api/v1/student/assignments/" + BG1 + "/scene", token(ORG_C, id(26001), Role.STUDENT));
    assertThat(other.statusCode()).isEqualTo(404);
  }

  @Test
  @DisplayName("Không ai đổi được nhãn máy qua API (US-GEO-203-FC4)")
  void noLabelChange() throws Exception {
    var r =
        http(
            "PUT",
            "/api/v1/problems/" + id(299036) + "/verify-status",
            teacherA(),
            "{\"verifyStatus\":\"verified\"}");
    assertThat(r.statusCode()).isEqualTo(403);
    assertThat(json(r).path("code").asString()).isEqualTo("FORBIDDEN");
    JsonNode p = json(get("/api/v1/teacher/problems/" + id(299036), teacherA()));
    assertThat(p.path("label").asString()).isEqualTo("Cần xem lại");
    assertThat(p.path("scene").path("solidKind").asString()).isEqualTo("sphere");
  }

  private HttpResponse<String> http(String method, String path, String bearer, String body)
      throws Exception {
    var b =
        java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + bearer)
            .method(method, java.net.http.HttpRequest.BodyPublishers.ofString(body));
    return java.net.http.HttpClient.newHttpClient()
        .send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static Map<String, double[]> points(JsonNode scene) {
    Map<String, double[]> m = new java.util.HashMap<>();
    for (JsonNode p : scene.path("points")) {
      m.put(
          p.path("name").asString(),
          new double[] {p.path("x").asDouble(), p.path("y").asDouble(), p.path("z").asDouble()});
    }
    return m;
  }

  private static double dist(double[] a, double[] b) {
    return Math.sqrt(
        (a[0] - b[0]) * (a[0] - b[0])
            + (a[1] - b[1]) * (a[1] - b[1])
            + (a[2] - b[2]) * (a[2] - b[2]));
  }
}
