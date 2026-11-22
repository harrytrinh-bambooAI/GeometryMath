package vn.geo.server.it.e4;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.knowledge.internal.service.LectureImporter;
import vn.geo.server.learning.internal.service.SceneTimingService;

/**
 * E4 — tích hợp (cột "Tự động hóa" = IT của test_cases.md): số đo thời gian dựng hình M-GEO-09,
 * lượt gợi ý trước lời giải M-GEO-16, kho bài giảng mẫu US-GEO-412; thêm phép đối chiếu chữ ký
 * chuẩn Java ↔ seed (D-GEO-150). Dữ liệu vàng E2E (seed 100…401).
 */
class HelpE4IT extends AbstractE1IT {

  static final String F060 =
      "Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5."
          + " Tính khoảng cách từ A đến mặt phẳng (A′BC).";

  @Autowired SceneTimingService timing;
  @Autowired LectureImporter importer;
  @Autowired GeometryEngine engine;

  private HttpResponse<String> post(String path, String token, Object body) throws Exception {
    return postJson(path, token, mapper.writeValueAsString(body));
  }

  @Test
  @DisplayName("TC-GEO-0403 Ghi thời gian từ gửi đề tới có hình: trung vị 20,0 giây, 1 đề lỗi")
  void tc0403() throws Exception {
    Instant from = Instant.now().minusMillis(5);
    // ba đề tự học thành công, mỗi đề một scene_shown 10, 20, 60 giây (đơn vị 100 ms)
    for (int tenths : new int[] {100, 200, 600}) {
      var r = post("/api/v1/student/self-study", an(), Map.of("statement", F060));
      assertThat(r.statusCode()).isEqualTo(200);
      String id = json(r).get("assignmentId").asString();
      var s =
          post(
              "/api/v1/student/assignments/" + id + "/scene-shown",
              an(),
              Map.of("durationMs", tenths * 100L));
      assertThat(s.statusCode()).isEqualTo(200);
    }
    // một đề lỗi: lượt gọi AI dựng hình không thành công, không có scene_shown
    jdbc.sql(
            """
            INSERT INTO ai_call_log (occurred_at, org_id, user_id, purpose, model, prompt_version,
                                     tokens_in, tokens_out, cost_micro_usd, latency_ms, status)
            VALUES (now(), :org, :user, 'scene', 'gia-lap-1', 'p1', 10, 0, 0, 5, 'error')
            """)
        .param("org", ORG_A)
        .param("user", AN)
        .update();
    SceneTimingService.Report rep = timing.report(from, Instant.now().plusSeconds(1));
    assertThat(rep.shown()).isEqualTo(3);
    assertThat(rep.medianSeconds()).isEqualTo("20,0");
    assertThat(rep.failed()).isEqualTo(1);
    assertThat(rep.note()).isEqualTo("ngưỡng đề xuất < 40 giây, chỉ có nghĩa trên AI thật");
    // scene_shown không tính vào thao tác trên hình hay tiến độ (BR-GEO-169)
    assertThat(SceneTimingService.summarize(List.of(100L, 200L, 600L), 1).medianSeconds())
        .isEqualTo("20,0");
  }

  @Test
  @DisplayName("TC-GEO-0429 Đếm lượt gợi ý trước khi mở lời giải = 2")
  void tc0429() throws Exception {
    var r = post("/api/v1/student/self-study", an(), Map.of("statement", F060));
    assertThat(r.statusCode()).isEqualTo(200);
    UUID id = UUID.fromString(json(r).get("assignmentId").asString());
    for (int level = 1; level <= 2; level++) {
      var h =
          post(
              "/api/v1/student/assignments/" + id + "/hints/" + level,
              an(),
              Map.of("requestId", UUID.randomUUID().toString()));
      assertThat(h.statusCode()).isEqualTo(200);
    }
    var s = postJson("/api/v1/student/assignments/" + id + "/solution/open", an(), "{}");
    assertThat(s.statusCode()).isEqualTo(200);
    assertThat(json(s).get("finalAnswer").asString()).isEqualTo("60/√769");
    long before =
        jdbc.sql(
                """
                SELECT count(*) FROM event_log h
                WHERE h.assignment_id = :id AND h.user_id = :u AND h.kind = 'hint'
                  AND h.occurred_at <= (SELECT min(occurred_at) FROM event_log x
                                        WHERE x.assignment_id = :id AND x.user_id = :u AND x.kind = 'unlock')
                """)
            .param("id", id)
            .param("u", AN)
            .query(Long.class)
            .single();
    assertThat(before).isEqualTo(2);
    assertThat(
            count(
                "SELECT hint_view_count FROM assignment_progress WHERE assignment_id = '"
                    + id
                    + "'"))
        .isEqualTo(2);
  }

  private JsonNode lecture(String term, String token) throws Exception {
    var r =
        get(
            "/api/v1/lectures/by-term?term=" + URLEncoder.encode(term, StandardCharsets.UTF_8),
            token);
    assertThat(r.statusCode()).as(term).isEqualTo(200);
    return json(r);
  }

  @Test
  @DisplayName("TC-GEO-0476 Đủ 10 bài giảng với nguồn và nhãn")
  void tc0476() throws Exception {
    assertThat(count("SELECT count(*) FROM lecture WHERE org_id = platform_org_id()"))
        .isEqualTo(10);
    assertThat(
            count(
                "SELECT count(*) FROM lecture WHERE source_kind = 'teacher_authored'"
                    + " AND NOT human_reviewed AND source_ref IS NOT NULL"))
        .isEqualTo(7);
    assertThat(count("SELECT count(*) FROM lecture WHERE source_kind = 'ai_written'")).isEqualTo(3);
    assertThat(count("SELECT count(*) FROM lecture WHERE approval_status <> 'ai_drafted'"))
        .isZero();
    for (String t :
        List.of(
            "Tỉ số lượng giác trong tam giác vuông",
            "Hai đường thẳng chéo nhau",
            "Mặt cầu ngoại tiếp")) {
      JsonNode l = lecture(t, an());
      assertThat(l.get("sourceLabel").asString()).isEqualTo("AI soạn");
      assertThat(l.get("aiLabel").asString()).isEqualTo("AI soạn · chờ giáo viên duyệt");
      assertThat(l.path("reviewNote").isMissingNode() || l.path("reviewNote").isNull()).isTrue();
    }
    JsonNode hc = lecture("Hình chiếu vuông góc", an());
    assertThat(hc.get("sourceLabel").asString()).isEqualTo("Giáo án mẫu");
    assertThat(hc.get("reviewNote").asString()).isEqualTo("Nội dung đang chờ giáo viên soát");
  }

  @Test
  @DisplayName("TC-GEO-0477 Mỗi bài có từ khóa trỏ tới")
  void tc0477() throws Exception {
    assertThat(lecture("hình chiếu", an()).get("title").asString())
        .isEqualTo("Hình chiếu vuông góc");
    assertThat(lecture("Góc giữa đường thẳng và mặt phẳng", an()).get("title").asString())
        .isEqualTo("Góc giữa đường thẳng và mặt phẳng");
    assertThat(
            count(
                "SELECT count(*) FROM lecture l WHERE NOT EXISTS"
                    + " (SELECT 1 FROM keyword k WHERE k.org_id = l.org_id AND k.lecture_id = l.id)"))
        .isZero();
  }

  @Test
  @DisplayName("TC-GEO-0479 Không bài giảng nào chứa đề hay bước giải của đề kho")
  void tc0479() {
    List<String> bodies = jdbc.sql("SELECT body FROM lecture").query(String.class).list();
    List<String> statements =
        jdbc.sql("SELECT statement FROM problem_version WHERE org_id = platform_org_id()")
            .query(String.class)
            .list();
    List<String> steps =
        jdbc.sql(
                "SELECT s->>'body' FROM solution, jsonb_array_elements(steps) s"
                    + " WHERE org_id = platform_org_id()")
            .query(String.class)
            .list();
    assertThat(bodies).hasSize(10);
    assertThat(statements).isNotEmpty();
    assertThat(steps).hasSizeGreaterThanOrEqualTo(12);
    for (String b : bodies) {
      for (String st : statements) {
        assertThat(b).doesNotContain(st);
      }
      for (String st : steps) {
        assertThat(b).doesNotContain(st);
      }
    }
  }

  @Test
  @DisplayName("TC-GEO-0480 Bài \"Giáo án mẫu\" thiếu tham chiếu nguồn bị từ chối, bài khác nạp")
  void tc0480() throws Exception {
    String doc =
        """
        {"lectures": [
          {"slug": "e4-thu-thieu-nguon", "title": "Bài thử thiếu nguồn", "source": "teacher_authored",
           "sourceRef": null, "keywords": ["thử thiếu nguồn"], "body": "Nội dung thử."},
          {"slug": "e4-thu-hop-le", "title": "Bài thử hợp lệ", "source": "ai_written",
           "sourceRef": null, "keywords": ["thử hợp lệ"], "body": "Nội dung thử."}
        ]}
        """;
    try {
      LectureImporter.Result r = importer.load(mapper.readTree(doc));
      assertThat(r.rejected()).hasSize(1);
      assertThat(r.rejected().get(0)).startsWith("Bài thử thiếu nguồn");
      assertThat(r.loaded()).containsExactly("e4-thu-hop-le");
      assertThat(count("SELECT count(*) FROM lecture WHERE slug = 'e4-thu-thieu-nguon'")).isZero();
    } finally {
      jdbc.sql(
              "DELETE FROM keyword WHERE lecture_id IN (SELECT id FROM lecture WHERE slug LIKE 'e4-thu-%')")
          .update();
      jdbc.sql("DELETE FROM lecture WHERE slug LIKE 'e4-thu-%'").update();
    }
  }

  @Test
  @DisplayName("Chữ ký chuẩn của sáu đề kho (Java) khớp seed 401_e4_catalog.sql (D-GEO-150)")
  void signaturesMatchSeed() {
    for (int n = 31; n <= 36; n++) {
      String code = String.format("HKG-%02d", n - 30);
      GeometryEngine.Signature sig =
          engine.signature(engine.catalogSpec(code).orElseThrow()).orElseThrow();
      byte[] seeded =
          jdbc.sql("SELECT signature FROM problem_signature WHERE problem_version_id = :pv")
              .param("pv", id(100000 + n))
              .query(byte[].class)
              .single();
      assertThat(seeded).as(code + " " + sig.canonical()).isEqualTo(sig.sha256());
    }
  }
}
