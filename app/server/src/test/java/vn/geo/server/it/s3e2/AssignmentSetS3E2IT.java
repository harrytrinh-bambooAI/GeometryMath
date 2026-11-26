package vn.geo.server.it.s3e2;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.assignment.internal.service.S3E2SandboxService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S3-E2 — tích hợp: bộ câu thử moi đáp số của học sinh khi lời giải đang khóa (US-S3E2-02-SC5,
 * BR-S3E2-12), quy tắc biên của cảnh báo giờ mở sớm hơn hạn (BR-S3E2-13).
 */
class AssignmentSetS3E2IT extends AbstractE1IT {

  /** Đề 1 SC-đáy của bộ 's3e2_kho' (seed 920): đáp số 45, bước giải "Hình chiếu của SC…". */
  static final String DE1 = "00000000-0000-7000-8003-000000922001";

  static final String DE2 = "00000000-0000-7000-8003-000000922002";

  /** Đáp số 45 ở mọi dạng viết tương đương, bước giải và câu gợi ý giải của Đề 1. */
  static final Pattern LEAK =
      Pattern.compile(
          "(^|[^0-9.,])45([^0-9]|$)|π\\s*/\\s*4|pi\\s*/\\s*4|0[.,]785|bốn mươi lăm|Đáp số|Lập luận đầy đủ|Hướng làm",
          Pattern.CASE_INSENSITIVE);

  @Autowired S3E2SandboxService sandbox;

  private final HttpClient http = HttpClient.newHttpClient();

  HttpResponse<String> send(String method, String path, String token, String body, String... h)
      throws Exception {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));
    for (int i = 0; i + 1 < h.length; i += 2) {
      b.header(h[i], h[i + 1]);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  @Test
  @DisplayName(
      "TC-S3E2-19 bộ câu thử moi đáp số: mọi kênh của học sinh khi lời giải Sắp mở không lộ đáp số, bước giải; không phản hồi nào được đệm dùng chung")
  void probeLockedSolution() throws Exception {
    var sb =
        sandbox.create(
            List.of(new S3E2SandboxService.ClassSpec("11A1", List.of("Bình An"), List.of())),
            List.of());
    String teacher = sb.teacher().token();
    String an = sb.classes().getFirst().students().get("Bình An").token();
    String classId = sb.classes().getFirst().id().toString();
    Instant release = Instant.now().plusSeconds(3 * 24 * 3600);
    var created =
        postJson(
            "/api/v1/teacher/assignment-sets",
            teacher,
            """
            {"title":"Bộ bài tuần 15","classId":"%s","dueAt":"%s","releaseAt":"%s",
             "items":[{"problemVersionId":"%s"},{"problemVersionId":"%s"}]}
            """
                .formatted(classId, Instant.now().plusSeconds(24 * 3600), release, DE1, DE2));
    assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
    String setId = json(created).get("id").asString();
    var assigned =
        postJson(
            "/api/v1/teacher/assignment-sets/" + setId + "/assign",
            teacher,
            "{\"requestKey\":\"k1\"}");
    assertThat(assigned.statusCode()).as(assigned.body()).isEqualTo(200);
    String item1 = json(assigned).get("items").get(0).get("assignmentId").asString();

    List<HttpResponse<String>> all = new ArrayList<>();
    all.add(send("GET", "/api/v1/student/assignment-sets", an, null));
    all.add(send("GET", "/api/v1/student/assignment-sets/" + setId, an, null));
    all.add(
        send("GET", "/api/v1/student/assignment-sets/" + setId + "/items/1/solution", an, null));
    all.add(
        send(
            "GET",
            "/api/v1/student/assignment-sets/" + setId + "/items/1/solution",
            an,
            null,
            "Accept-Language",
            "en-US",
            "If-None-Match",
            "\"*\"",
            "X-Device-Time",
            "2026-12-10T08:00:00+07:00"));
    all.add(send("GET", "/api/v1/student/practice-sets/" + setId, an, null));
    all.add(send("GET", "/api/v1/student/practice-sets/" + setId + "/result", an, null));
    all.add(send("GET", "/api/v1/student/assignments/" + item1 + "/solution", an, null));
    all.add(send("GET", "/api/v1/student/assignments/" + item1 + "/help", an, null));
    // một lượt Sai: phản hồi kèm gợi ý mức 1 (không phải lời giải) — vẫn không lộ đáp số
    all.add(
        send(
            "POST",
            "/api/v1/student/practice-items/" + item1 + "/attempts",
            an,
            "{\"answer\":\"30\",\"requestKey\":\"p1\"}"));
    all.add(
        send("GET", "/api/v1/student/assignment-sets/" + setId + "/items/9/solution", an, null));
    all.add(send("GET", "/api/v1/student/assignment-sets/not-a-uuid", an, null));

    for (HttpResponse<String> r : all) {
      String where =
          r.request().method() + " " + r.request().uri().getPath() + " → " + r.statusCode();
      assertThat(LEAK.matcher(r.body()).find()).as(where + ": " + r.body()).isFalse();
      assertThat(r.headers().firstValue("Cache-Control").orElse(""))
          .as(where + " Cache-Control")
          .contains("no-store");
    }
    JsonNode sol = json(all.get(2));
    assertThat(sol.get("releaseState").asString()).isEqualTo("upcoming");
    assertThat(sol.has("finalAnswer") && !sol.get("finalAnswer").isNull()).isFalse();
    assertThat(sol.has("steps") && !sol.get("steps").isNull()).isFalse();
    assertThat(json(all.get(3)).get("releaseState").asString()).isEqualTo("upcoming");
  }

  @Test
  @DisplayName(
      "TC-S3E2-21 biên cảnh báo: giờ mở bằng đúng hạn không cảnh báo, sớm 1 phút thì releaseBeforeDue")
  void warningBoundary() throws Exception {
    var sb =
        sandbox.create(
            List.of(new S3E2SandboxService.ClassSpec("11A1", List.of("Bình An"), List.of())),
            List.of());
    String teacher = sb.teacher().token();
    Map<String, Boolean> cases =
        Map.of("2026-12-11T20:00:00+07:00", false, "2026-12-11T19:59:00+07:00", true);
    for (var c : cases.entrySet()) {
      var r =
          postJson(
              "/api/v1/teacher/assignment-sets",
              teacher,
              """
              {"title":"Bộ bài tuần 16","dueAt":"2026-12-11T20:00:00+07:00","releaseAt":"%s",
               "items":[{"problemVersionId":"%s"}]}
              """
                  .formatted(c.getKey(), DE1));
      assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
      assertThat(json(r).get("releaseBeforeDue").asBoolean())
          .as(c.getKey())
          .isEqualTo(c.getValue());
    }
  }
}
