package vn.geo.server.it.s2e3;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.draft.service.DraftPipeline;
import vn.geo.server.content.internal.draft.service.ImageSanitizer;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E3 — tích hợp (máy chủ + CSDL + AI giả lập): ảnh lưu tạm không còn siêu dữ liệu và bị xóa sau
 * xác nhận (US-S2E3-02-SC5), bốn bước có trạng thái, bước lỗi dừng bước sau, hạn 30 giây với đồng
 * hồ giả, thử lại chỉ bước lỗi (US-S2E3-03), "Dùng đáp số máy" (US-S2E3-04-SC5). Dữ liệu:
 * db/seed/e2e/840_s2e3_core.sql, mỗi ca một giáo viên riêng (làn 0).
 */
class DraftS2E3IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000840001");
  static final String FX10 = "Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.";
  static final String BASE = "/api/v1/teacher/problem-drafts";
  static final String JOBS = "/api/v1/teacher/problem-draft-jobs/";

  @Autowired DraftPipeline pipeline;
  @Autowired AiGateway gateway;
  private final HttpClient http = HttpClient.newHttpClient();

  @AfterEach
  void restore() throws Exception {
    pipeline.overrideForTest(null, null);
    post("/test-hooks/ai/reset", null, "{}");
  }

  static UUID teacher(int tc) {
    return UUID.fromString(String.format("00000000-0000-7000-8002-%012d", 841000000L + tc * 100L));
  }

  String tk(int tc) {
    return token(ORG, teacher(tc), Role.TEACHER);
  }

  JsonNode ok(HttpResponse<String> r) {
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  JsonNode create(int tc, String statement) throws Exception {
    String body =
        mapper.createObjectNode().put("source", "text").put("statement", statement).toString();
    return ok(postJson(BASE, tk(tc), body));
  }

  JsonNode build(int tc, String draftId) throws Exception {
    return ok(postJson(BASE + "/" + draftId + "/build", tk(tc), "{}"));
  }

  /** Chờ tác vụ rời trạng thái "running" (tối đa 20 giây). */
  JsonNode await(int tc, String jobId) throws Exception {
    long end = System.currentTimeMillis() + 20_000;
    JsonNode j;
    do {
      Thread.sleep(100);
      j = ok(get(JOBS + jobId, tk(tc)));
    } while ("running".equals(j.path("state").asString()) && System.currentTimeMillis() < end);
    return j;
  }

  static List<String> states(JsonNode job) {
    List<String> out = new ArrayList<>();
    job.path("stages").forEach(s -> out.add(s.path("state").asString()));
    return out;
  }

  void scenario(int tc, String scenario) throws Exception {
    String body =
        mapper
            .createObjectNode()
            .put("userId", teacher(tc).toString())
            .put("scenario", scenario)
            .toString();
    assertThat(post("/test-hooks/ai", null, body).statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("TC-S2E3-020 Siêu dữ liệu ảnh bị xóa và ảnh gốc không giữ sau xác nhận")
  void tc020_imageMetadata() throws Exception {
    String body = mapper.createObjectNode().put("source", "image").toString();
    String id = ok(postJson(BASE, tk(20), body)).path("id").asString();
    byte[] img = vn.geo.server.content.draft.DraftS2E3TestImages.jpegWithGps("S2E3-CLEAR");
    assertThat(ImageSanitizer.hasMetadata(img)).isTrue();
    HttpResponse<String> up =
        http.send(
            HttpRequest.newBuilder(
                    URI.create(
                        "http://127.0.0.1:"
                            + port
                            + BASE
                            + "/"
                            + id
                            + "/image?offset=0&total="
                            + img.length))
                .header("Authorization", "Bearer " + tk(20))
                .header("Content-Type", "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(img))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    JsonNode v = ok(up);
    assertThat(v.path("statement").asString()).isEqualTo(FX10);
    byte[] stored =
        jdbc.sql("SELECT image_tmp FROM problem_draft WHERE id = :id")
            .param("id", UUID.fromString(id))
            .query((rs, i) -> rs.getBytes(1))
            .single();
    assertThat(stored).isNotNull();
    assertThat(ImageSanitizer.hasMetadata(stored)).isFalse();
    assertThat(new String(stored, java.nio.charset.StandardCharsets.ISO_8859_1))
        .doesNotContain("GPS");
    JsonNode c = ok(postJson(BASE + "/" + id + "/confirm-text", tk(20), "{}"));
    assertThat(c.path("textConfirmed").asBoolean()).isTrue();
    assertThat(c.path("hasImage").asBoolean()).isFalse();
    assertThat(
            count(
                "SELECT count(*) FROM problem_draft WHERE id = '"
                    + id
                    + "' AND image_tmp IS NOT NULL"))
        .isZero();
  }

  @Test
  @DisplayName("TC-S2E3-022 Kiểm chứng không đạt thì nhãn máy Cần xem lại")
  void tc022_verifyFails() throws Exception {
    scenario(22, "s2e3:solve-wrong");
    String id = create(22, FX10).path("id").asString();
    JsonNode job = await(22, build(22, id).path("job").path("id").asString());
    assertThat(job.path("state").asString()).isEqualTo("done");
    JsonNode v = ok(get(BASE + "/" + id, tk(22)));
    assertThat(v.path("result").path("machineLabel").asString()).isEqualTo("Cần xem lại");
    assertThat(v.path("result").path("humanLabel").asString()).isEqualTo("AI soạn");
    assertThat(v.toString()).doesNotContain("Đã kiểm chứng");
  }

  @Test
  @DisplayName("TC-S2E3-023 Bước lỗi dừng các bước sau")
  void tc023_failStops() throws Exception {
    scenario(23, "s2e3:build-error");
    String id = create(23, FX10).path("id").asString();
    JsonNode job = await(23, build(23, id).path("job").path("id").asString());
    assertThat(job.path("state").asString()).isEqualTo("failed");
    assertThat(states(job)).containsExactly("done", "error", "waiting", "waiting");
    assertThat(job.path("retryable").asBoolean()).isTrue();
    assertThat(job.path("stages").get(2).path("aiCalls").asInt()).isZero();
    assertThat(count("SELECT count(*) FROM solution_doc WHERE draft_id = '" + id + "'")).isZero();
  }

  /** AI giả lập của ca kiểm: bước Giải trả lời sau {@code solveSeconds} giây trên đồng hồ giả. */
  void fakeAi(AtomicLong clock, AtomicInteger solveSeconds, List<String> calls) {
    pipeline.overrideForTest(
        clock::get,
        req -> {
          calls.add(req.task());
          if ("draft_solve".equals(req.task())) {
            clock.addAndGet(solveSeconds.get() * 1000L);
          }
          AiResponse real = gateway.complete(req);
          return real;
        });
  }

  @Test
  @DisplayName("TC-S2E3-024 Bước chạy 30 giây còn đạt, 31 giây quá hạn")
  void tc024_stageLimit() throws Exception {
    AtomicLong clock = new AtomicLong(1_000_000);
    AtomicInteger solve = new AtomicInteger(30);
    List<String> calls = new java.util.concurrent.CopyOnWriteArrayList<>();
    fakeAi(clock, solve, calls);
    String first = create(24, FX10).path("id").asString();
    JsonNode j1 = await(24, build(24, first).path("job").path("id").asString());
    assertThat(j1.path("state").asString()).isEqualTo("done");
    assertThat(states(j1)).containsExactly("done", "done", "done", "done");
    solve.set(31);
    String second = create(24, FX10).path("id").asString();
    JsonNode j2 = await(24, build(24, second).path("job").path("id").asString());
    assertThat(j2.path("state").asString()).isEqualTo("failed");
    assertThat(states(j2)).containsExactly("done", "done", "error", "waiting");
    assertThat(j2.path("stages").get(2).path("error").asString()).isEqualTo("TIMEOUT");
    assertThat(j2.path("retryable").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("TC-S2E3-025 Thử lại bước lỗi giữ kết quả các bước đã xong")
  void tc025_retryKeepsDone() throws Exception {
    AtomicLong clock = new AtomicLong(1_000_000);
    AtomicInteger solve = new AtomicInteger(31);
    List<String> calls = new java.util.concurrent.CopyOnWriteArrayList<>();
    fakeAi(clock, solve, calls);
    String id = create(25, FX10).path("id").asString();
    String jobId = build(25, id).path("job").path("id").asString();
    JsonNode failed = await(25, jobId);
    assertThat(states(failed)).containsExactly("done", "done", "error", "waiting");
    calls.clear();
    solve.set(1);
    ok(postJson(JOBS + jobId + "/retry", tk(25), "{}"));
    JsonNode done = await(25, jobId);
    assertThat(done.path("state").asString()).isEqualTo("done");
    assertThat(states(done)).containsExactly("done", "done", "done", "done");
    assertThat(calls).containsExactly("draft_solve"); // Đọc đề, Dựng hình không gọi lại AI
    assertThat(done.path("stages").get(0).path("aiCalls").asInt()).isEqualTo(1);
    assertThat(done.path("stages").get(1).path("aiCalls").asInt()).isEqualTo(1);
  }

  @Test
  @DisplayName("TC-S2E3-034 Chọn dùng đáp số máy thì kiểm lại và mở Duyệt")
  void tc034_useMachineAnswer() throws Exception {
    scenario(34, "s2e3:answer-80");
    String id = create(34, FX10 + " Đáp số: 90°").path("id").asString();
    await(34, build(34, id).path("job").path("id").asString());
    JsonNode v = ok(get(BASE + "/" + id, tk(34)));
    String doc = v.path("result").path("docId").asString();
    assertThat(v.path("result").path("answers").path("ai").asString()).isEqualTo("80°");
    assertThat(v.path("result").path("approveBlocked").asBoolean()).isTrue();
    JsonNode after =
        ok(
            postJson(
                "/api/v1/teacher/solutions/" + doc + "/use-machine-answer",
                tk(34),
                "{\"version\":1}"));
    assertThat(after.path("version").path("version").asInt()).isEqualTo(2);
    assertThat(after.path("answers").path("ai").asString()).isEqualTo("90°");
    assertThat(after.path("answers").path("state").asString()).isEqualTo("match");
    assertThat(after.path("version").path("finalState").asString()).isEqualTo("match");
    assertThat(after.path("approve").path("allowed").asBoolean()).isTrue();
  }
}
