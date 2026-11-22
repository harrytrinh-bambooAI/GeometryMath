package vn.geo.server.it.e2;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.content.internal.service.TemplateCatalogService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Lô FIX2 (soát E2 #1…#6): AI giả lập trả dữ liệu sai (mock-ai/scene.json, quy tắc "[kịch bản AI
 * lỗi: …]") và hình mẫu (cận trên, ô gây lỗi, nhãn máy, danh mục). Mọi lỗi dữ liệu AI là MSG-GEO-18
 * (400), không bao giờ 500. Mã ca Dev-E2-xx ghi ở qc_findings.md (mục Dev) của E2.
 */
class AiDataE2IT extends AbstractE1IT {

  static final String MSG_18 =
      "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu.";
  static final String MSG_19 = "Giá trị phải là số lớn hơn 0.";

  @Autowired TemplateCatalogService catalog;

  private HttpResponse<String> build(String marker) throws Exception {
    String st =
        "Cho tứ diện ABCD có các cạnh DA, DB, DC đôi một vuông góc [kịch bản AI lỗi: "
            + marker
            + "]. Tính thể tích.";
    return postJson(
        "/api/v1/student/self-study", an(), mapper.writeValueAsString(Map.of("statement", st)));
  }

  private void assertMsg18NothingCreated(String marker) throws Exception {
    long p0 = count("SELECT count(*) FROM problem WHERE source = 'student_input'");
    long s0 = count("SELECT count(*) FROM scene_build");
    var r = build(marker);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(400);
    JsonNode body = json(r);
    assertThat(body.path("code").asString()).isEqualTo("VALIDATION_FAILED");
    assertThat(body.path("message").asString()).isEqualTo(MSG_18);
    assertThat(body.path("details").get(0).path("field").asString()).isEqualTo("statement");
    assertThat(count("SELECT count(*) FROM problem WHERE source = 'student_input'")).isEqualTo(p0);
    assertThat(count("SELECT count(*) FROM scene_build")).isEqualTo(s0);
  }

  private String solidOf(JsonNode built) {
    return jdbc.sql(
            "SELECT p.solid_kind::text FROM problem p JOIN problem_version v ON v.problem_id = p.id"
                + " WHERE v.id = :v")
        .param("v", UUID.fromString(built.path("problemVersionId").asString()))
        .query(String.class)
        .single();
  }

  @Test
  @DisplayName(
      "Dev-E2-03 AI trả solid 'tetrahedron': quy về hình chóp, đề lạ (tọa độ AI, Cần xem lại), không 500")
  void devE203_tetrahedron() throws Exception {
    var r = build("solid tetrahedron");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    JsonNode b = json(r);
    assertThat(b.path("scene").path("coordSource").asString()).isEqualTo("ai");
    assertThat(b.path("scene").path("verification").path("status").asString())
        .isEqualTo("needs_review");
    assertThat(solidOf(b)).isEqualTo("pyramid");
  }

  @Test
  @DisplayName("Dev-E2-04 AI trả solid ngoài danh mục không quy được: MSG-GEO-18, không tạo gì")
  void devE204_unknownSolid() throws Exception {
    assertMsg18NothingCreated("solid lạ");
  }

  @Test
  @DisplayName(
      "Dev-E2-05 AI trả đại lượng hỏi thiếu điểm: MSG-GEO-18 (trước đây 500 IndexOutOfBounds)")
  void devE205_askedMissingPoints() throws Exception {
    assertMsg18NothingCreated("thiếu điểm");
  }

  @Test
  @DisplayName("Dev-E2-06 AI trả tọa độ là chuỗi số ('1.5', '1,5'): đọc đúng giá trị")
  void devE206_numericStrings() throws Exception {
    var r = build("số dạng chuỗi");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    JsonNode scene = json(r).path("scene");
    Map<String, double[]> p = new java.util.HashMap<>();
    scene
        .path("points")
        .forEach(x ->
            p.put(
                x.path("name").asString(),
                new double[] {
                  x.path("x").asDouble(), x.path("y").asDouble(), x.path("z").asDouble()
                }));
    // engine có thể đặt lại gốc/tỉ lệ: so tỉ lệ độ dài thay cho tọa độ tuyệt đối
    double ab = dist(p.get("A"), p.get("B"));
    assertThat(ab).isGreaterThan(0);
    assertThat(dist(p.get("A"), p.get("C")) / ab)
        .isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
    assertThat(dist(p.get("A"), p.get("D")) / ab)
        .isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
  }

  @Test
  @DisplayName("Dev-E2-07 AI trả tọa độ 'abc': MSG-GEO-18, không lặng lẽ thành 0")
  void devE207_textCoordinate() throws Exception {
    assertMsg18NothingCreated("tọa độ chữ");
  }

  @Test
  @DisplayName(
      "Dev-E2-08 hình mẫu: tham số quá cận trên báo MSG-GEO-19 đúng ô; nhãn máy theo kiểm ràng buộc thật")
  void devE208_templateBoundsAndLabel() throws Exception {
    var r =
        postJson(
            "/api/v1/geometry/templates/cone/scene",
            an(),
            "{\"params\": {\"r\": 2, \"h\": 100000}}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(400);
    assertThat(json(r).path("details").get(0).path("field").asString()).isEqualTo("h");
    assertThat(json(r).path("message").asString()).isEqualTo(MSG_19);
    var ok =
        postJson(
            "/api/v1/geometry/templates/cone/scene",
            an(),
            "{\"params\": {\"r\": \"2,5\", \"h\": 4}}");
    assertThat(ok.statusCode()).as(ok.body()).isEqualTo(200);
    assertThat(json(ok).path("verification").path("status").asString()).isEqualTo("verified");
  }

  @Test
  @DisplayName("Dev-E2-09 hình mẫu dựng lỗi: MSG-GEO-19 gắn đúng ô gây lỗi, không luôn ô đầu tiên")
  void devE209_culpritField() throws Exception {
    JsonNode data =
        mapper.readTree(
            """
            [{"id": 93, "code": "e2_test_conerl", "solid_kind": "cone", "name": "Nón theo đường sinh",
              "param_spec": {"order": 93, "card": false, "builder": "cone_rl",
                "params": [{"key": "r", "label": "Bán kính đáy", "default": 3},
                           {"key": "l", "label": "Đường sinh", "default": 5}]}}]
            """);
    try {
      assertThat(catalog.load(data).loaded()).containsExactly("e2_test_conerl");
      String path = "/api/v1/geometry/templates/e2_test_conerl/scene";
      // đường sinh 1 < bán kính 3: lỗi do ô l (ô thứ hai)
      var r = postJson(path, an(), "{\"params\": {\"l\": 1}}");
      assertThat(r.statusCode()).as(r.body()).isEqualTo(400);
      assertThat(json(r).path("details").get(0).path("field").asString()).isEqualTo("l");
      // bán kính 9 > đường sinh 5: lỗi do ô r
      var r2 = postJson(path, an(), "{\"params\": {\"r\": 9}}");
      assertThat(r2.statusCode()).as(r2.body()).isEqualTo(400);
      assertThat(json(r2).path("details").get(0).path("field").asString()).isEqualTo("r");
    } finally {
      jdbc.sql("DELETE FROM solid_template WHERE code LIKE 'e2_test_%'").update();
    }
  }

  @Test
  @DisplayName(
      "Dev-E2-10 nạp danh mục: khóa tham số lệch bộ dựng, loại hình ngoài danh mục hoặc lệch bộ dựng bị từ chối")
  void devE210_catalogValidation() throws Exception {
    JsonNode data =
        mapper.readTree(
            """
            [{"id": 94, "code": "e2_test_keys", "solid_kind": "cone", "name": "Khóa lệch",
              "param_spec": {"order": 94, "card": false, "builder": "cone",
                "params": [{"key": "r", "label": "Bán kính", "default": 2},
                           {"key": "x", "label": "Lạ", "default": 3}]}},
             {"id": 95, "code": "e2_test_kind", "solid_kind": "tetrahedron", "name": "Loại lạ",
              "param_spec": {"order": 95, "card": false, "builder": "cube",
                "params": [{"key": "a", "label": "Cạnh", "default": 2}]}},
             {"id": 96, "code": "e2_test_mismatch", "solid_kind": "sphere", "name": "Loại lệch",
              "param_spec": {"order": 96, "card": false, "builder": "cube",
                "params": [{"key": "a", "label": "Cạnh", "default": 2}]}}]
            """);
    try {
      var report = catalog.load(data);
      assertThat(report.loaded()).isEmpty();
      assertThat(report.errors()).hasSize(3);
      assertThat(report.errors().get(0)).startsWith("Khóa lệch: tham số");
      assertThat(report.errors().get(1))
          .startsWith("Loại lạ: loại hình 'tetrahedron' ngoài danh mục");
      assertThat(report.errors().get(2)).startsWith("Loại lệch: loại hình 'sphere' khác loại");
      assertThat(count("SELECT count(*) FROM solid_template WHERE code LIKE 'e2_test_%'")).isZero();
    } finally {
      jdbc.sql("DELETE FROM solid_template WHERE code LIKE 'e2_test_%'").update();
    }
  }

  private static double dist(double[] a, double[] b) {
    return Math.sqrt(
        (a[0] - b[0]) * (a[0] - b[0])
            + (a[1] - b[1]) * (a[1] - b[1])
            + (a[2] - b[2]) * (a[2] - b[2]));
  }
}
