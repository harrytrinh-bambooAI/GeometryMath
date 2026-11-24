package vn.geo.server.it.e4;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * FIX3 — phát hiện soát E4: gợi ý AI tự học qua Kiểm soát đáp án (#8), ảnh đọc đề kiểm loại tệp và
 * cỡ sau giải mã (#9), nhãn người theo nguồn lời giải (#11), gợi ý AI sinh một lần khi yêu cầu đồng
 * thời và không sinh khi bài kế thừa khóa (#13). Dev-E4-01…06 (qc_findings E4 mục Dev).
 */
class HelpFix3E4IT extends AbstractE1IT {

  static final String F060 =
      "Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5."
          + " Tính khoảng cách từ A đến mặt phẳng (A′BC).";
  static final String F063 =
      "Cho hình chóp S.MNPQ, đáy là hình vuông cạnh a, SM ⊥ (MNPQ), SM = a. Tính góc giữa SQ và mặt"
          + " đáy.";
  static final String FALLBACK_1 =
      "Em đọc kỹ dữ kiện và xác định đại lượng đề hỏi nằm ở đâu trên hình.";

  private HttpResponse<String> post(String path, String token, Object body) throws Exception {
    return postJson(path, token, mapper.writeValueAsString(body));
  }

  private UUID selfStudy(String statement) throws Exception {
    var r = post("/api/v1/student/self-study", an(), Map.of("statement", statement));
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return UUID.fromString(json(r).get("assignmentId").asString());
  }

  private HttpResponse<String> hint(UUID id, int level) throws Exception {
    return post(
        "/api/v1/student/assignments/" + id + "/hints/" + level,
        an(),
        Map.of("requestId", UUID.randomUUID().toString()));
  }

  private long aiCalls(String purpose, Instant from) {
    return jdbc.sql(
            "SELECT count(*) FROM ai_call_log WHERE user_id = :u AND purpose::text = :p AND occurred_at >= :t")
        .param("u", AN)
        .param("p", purpose)
        .param("t", java.sql.Timestamp.from(from))
        .query(Long.class)
        .single();
  }

  @Test
  @DisplayName(
      "Dev-E4-01 gợi ý AI tự học lộ đáp số bị thay bằng câu gợi mở; gợi ý không lộ giữ nguyên")
  void selfHintsScreened() throws Exception {
    UUID id = selfStudy(F060 + " [Dev-E4-01]");
    var h1 = hint(id, 1);
    assertThat(h1.statusCode()).as(h1.body()).isEqualTo(200);
    assertThat(json(h1).get("body").asString()).isEqualTo(FALLBACK_1);
    assertThat(h1.body()).doesNotContain("60/√769");
    var h2 = hint(id, 2);
    assertThat(h2.statusCode()).isEqualTo(200);
    assertThat(json(h2).get("body").asString())
        .isEqualTo("Em nhìn A.A′BC như một tứ diện vuông tại A.");
  }

  @Test
  @DisplayName("Dev-E4-02 hai yêu cầu gợi ý đầu đồng thời: gọi AI sinh gợi ý đúng một lần")
  void selfHintsGeneratedOnce() throws Exception {
    UUID id = selfStudy(F060);
    Instant from = Instant.now().minusMillis(5);
    CountDownLatch go = new CountDownLatch(1);
    List<CompletableFuture<HttpResponse<String>>> fs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      fs.add(
          CompletableFuture.supplyAsync(() -> {
            try {
              go.await();
              return hint(id, 1);
            } catch (Exception e) {
              throw new IllegalStateException(e);
            }
          }));
    }
    go.countDown();
    for (var f : fs) {
      assertThat(f.get().statusCode()).as(f.get().body()).isEqualTo(200);
    }
    assertThat(aiCalls("hint", from)).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Dev-E4-03 bài kế thừa khóa, đề kho thiếu gợi ý: 404, không gọi AI, không ghi gợi ý AI")
  void inheritedNoCatalogHintNoAi() throws Exception {
    reseed();
    UUID id = selfStudy(F063);
    jdbc.sql("DELETE FROM hint WHERE problem_version_id = :pv").param("pv", HKG01_V).update();
    try {
      Instant from = Instant.now().minusMillis(5);
      var h = hint(id, 1);
      assertThat(h.statusCode()).as(h.body()).isEqualTo(404);
      assertThat(aiCalls("hint", from)).isZero();
      assertThat(
              count(
                  "SELECT count(*) FROM hint h JOIN assignment a ON a.problem_version_id = h.problem_version_id"
                      + " WHERE a.id = '"
                      + id
                      + "'"))
          .isZero();
    } finally {
      reseed();
    }
  }

  private static String image(byte[] head, String marker, int size) {
    byte[] m = marker.getBytes(StandardCharsets.ISO_8859_1);
    byte[] b = new byte[Math.max(size, head.length + m.length)];
    java.util.Arrays.fill(b, (byte) 7);
    System.arraycopy(head, 0, b, 0, head.length);
    System.arraycopy(m, 0, b, head.length, m.length);
    return Base64.getEncoder().encodeToString(b);
  }

  static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
  static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
  static final byte[] HEIC = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c'};
  static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a'};

  private HttpResponse<String> ocr(String b64) throws Exception {
    return post("/api/v1/student/self-study/ocr", an(), Map.of("image", b64));
  }

  @Test
  @DisplayName(
      "Dev-E4-04 đọc ảnh: không phải ảnh (GIF, chữ) → MSG-GEO-22 không gọi AI; JPEG/HEIC đọc")
  void ocrMagicBytes() throws Exception {
    Instant from = Instant.now().minusMillis(5);
    for (String bad :
        new String[] {
          image(GIF, "GEO-FIXTURE:F-GEO-066", 2_000),
          Base64.getEncoder()
              .encodeToString(
                  "GEO-FIXTURE:F-GEO-066 không phải ảnh".getBytes(StandardCharsets.UTF_8))
        }) {
      var r = ocr(bad);
      assertThat(r.statusCode()).as(r.body()).isEqualTo(400);
      assertThat(json(r).get("message").asString())
          .isEqualTo("Chưa đọc được chữ trong ảnh. Em chụp lại rõ hơn hoặc gõ đề.");
    }
    assertThat(aiCalls("scene", from)).isZero();
    for (byte[] head : new byte[][] {JPEG, HEIC, PNG}) {
      var r = ocr(image(head, "GEO-FIXTURE:F-GEO-066", 2_000));
      assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    }
  }

  @Test
  @DisplayName(
      "Dev-E4-05 đọc ảnh: trần 1 MB là của ẢNH sau giải mã (ảnh 1 000 000 byte đọc được; 1 100 000 byte → 413)")
  void ocrDecodedSize() throws Exception {
    Instant from = Instant.now().minusMillis(5);
    var big = ocr(image(PNG, "GEO-FIXTURE:F-GEO-066", 1_100_000));
    assertThat(big.statusCode()).isEqualTo(413);
    assertThat(json(big).get("code").asString()).isEqualTo("PAYLOAD_TOO_LARGE");
    assertThat(aiCalls("scene", from)).isZero();
    // ảnh ~0,95 MB: thân base64 ~1,33 MB vượt 1 MB thân thường nhưng ảnh vẫn trong trần
    var ok = ocr(image(PNG, "GEO-FIXTURE:F-GEO-066", 1_000_000));
    assertThat(ok.statusCode()).as(ok.body()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Dev-E4-06 nhãn người của lời giải theo nguồn: AI soạn → \"AI soạn\"; không do AI → không nhãn")
  void humanLabelFromAuthorship() throws Exception {
    UUID id = selfStudy(F060);
    assertThat(hint(id, 1).statusCode()).isEqualTo(200);
    assertThat(hint(id, 2).statusCode()).isEqualTo(200);
    var s = postJson("/api/v1/student/assignments/" + id + "/solution/open", an(), "{}");
    assertThat(s.statusCode()).as(s.body()).isEqualTo(200);
    assertThat(json(s).get("humanLabel").asString()).isEqualTo("AI soạn");
    jdbc.sql(
            "UPDATE solution s SET ai_authored = false FROM assignment a"
                + " WHERE a.id = :id AND s.org_id = a.content_org_id AND s.problem_version_id = a.problem_version_id")
        .param("id", id)
        .update();
    JsonNode again =
        json(postJson("/api/v1/student/assignments/" + id + "/solution/open", an(), "{}"));
    assertThat(again.has("humanLabel") && !again.get("humanLabel").isNull()).isFalse();
  }
}
