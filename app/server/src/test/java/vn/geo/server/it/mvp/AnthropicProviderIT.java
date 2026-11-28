package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import vn.geo.server.aigateway.internal.FakeAnthropic;
import vn.geo.server.aigateway.internal.FakeAnthropic.Reply;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * PO-60: máy chủ thật với {@code geo.ai.provider=anthropic} trỏ vào máy chủ HTTP giả (không gọi
 * mạng thật). Đường thật của học sinh: AI trả đúng định dạng → 200 và ai_call_log ghi token/chi phí
 * từ usage; 429 → thử lại; đầu ra hỏng → lỗi sạch (không 500).
 */
class AnthropicProviderIT extends AbstractE1IT {

  static final FakeAnthropic FAKE;

  static {
    try {
      FAKE = new FakeAnthropic();
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @DynamicPropertySource
  static void ai(DynamicPropertyRegistry r) {
    r.add("geo.ai.provider", () -> "anthropic");
    r.add("geo.ai.api-key", () -> "khoa-ai-gia-cua-it-0123456789");
    r.add("geo.ai.anthropic.base-url", FAKE::baseUrl);
    r.add("geo.ai.anthropic.max-retries", () -> "1");
    r.add("geo.ai.anthropic.retry-backoff", () -> "10ms");
    r.add("geo.ai.anthropic.attempt-timeout", () -> "3s");
  }

  /**
   * Ngữ cảnh Spring riêng của lớp này chạy lại Flyway clean + migrate (TestFlywayConfig) nên dữ
   * liệu vàng phải nạp lại: trước lớp này (dữ liệu vừa bị xóa) và sau nó (ngữ cảnh chung tạo sau sẽ
   * xóa lần nữa) — đặt lại cờ "đã nạp" của AbstractE1IT.
   */
  @BeforeAll
  static void forceReseed() throws Exception {
    resetSeededFlag();
  }

  @AfterAll
  static void stopFake() throws Exception {
    FAKE.close();
    resetSeededFlag();
  }

  private static void resetSeededFlag() throws ReflectiveOperationException {
    var f = AbstractE1IT.class.getDeclaredField("seeded");
    f.setAccessible(true);
    f.setBoolean(null, false);
  }

  @BeforeEach
  void resetFake() {
    FAKE.reset();
  }

  private Map<String, Object> lastCall() {
    return jdbc.sql(
            """
            SELECT purpose::text AS purpose, model, prompt_version, tokens_in, tokens_out, cost_micro_usd,
                   status::text AS status
            FROM ai_call_log ORDER BY id DESC LIMIT 1
            """)
        .query()
        .singleRow();
  }

  @Test
  @DisplayName(
      "Hỏi đáp qua AI thật (giả HTTP): 200, usage và chi phí vào ai_call_log, 429 được thử lại")
  void qaRecordsUsageAndRetries() throws Exception {
    FAKE.enqueue(
        Reply.status(429, "rate_limit_error"),
        Reply.ok(
            FakeAnthropic.message(
                "{\"kind\":\"hint\",\"reply\":\"Em thử xét tam giác vuông chứa góc cần tìm nhé.\"}",
                321,
                45)));
    var r =
        postJson(
            "/api/v1/student/assignments/" + BG1 + "/questions",
            token(ORG_A, DUNG, Role.STUDENT),
            "{\"question\":\"Em nên bắt đầu từ đâu?\"}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    assertThat(r.body()).contains("tam giác vuông");
    assertThat(FAKE.seen()).hasSize(2);
    var row = lastCall();
    assertThat(row.get("purpose")).isEqualTo("qa");
    assertThat(row.get("model")).isEqualTo("claude-sonnet-5-5");
    assertThat(row.get("prompt_version")).isEqualTo("a2");
    assertThat(((Number) row.get("tokens_in")).intValue()).isEqualTo(321);
    assertThat(((Number) row.get("tokens_out")).intValue()).isEqualTo(45);
    assertThat(((Number) row.get("cost_micro_usd")).longValue()).isEqualTo(321 * 2 + 45 * 10);
    assertThat(row.get("status")).isEqualTo("ok");
    // định danh không rời máy chủ (BR-GEO-108)
    assertThat(FAKE.seen().get(1).body()).doesNotContain(DUNG.toString()).doesNotContain("Dũng");
  }

  @Test
  @DisplayName(
      "Tự học: AI thật trả chữ không phải JSON → lỗi sạch 503, không 500; lượt ghi status error")
  void malformedSceneIsCleanError() throws Exception {
    FAKE.enqueue(Reply.ok(FakeAnthropic.message("Xin lỗi, tôi không dựng được hình này.", 50, 9)));
    var r =
        postJson(
            "/api/v1/student/self-study",
            an(),
            "{\"statement\":\"Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a.\"}");
    assertThat(r.statusCode()).as(r.body()).isNotEqualTo(500).isBetween(400, 503);
    assertThat(r.body()).contains("\"code\"");
    assertThat(lastCall().get("status")).isEqualTo("error");
  }

  @Test
  @DisplayName("Tự học: AI thật trả mô tả dựng đúng định dạng → engine dựng và kiểm chứng")
  void wellFormedSceneBuilds() throws Exception {
    String scene =
        "{\"kind\":\"space\",\"solid\":\"pyramid\",\"level\":\"thpt\",\"template\":\"pyramid_square_sa\","
            + "\"params\":{\"a\":\"a\",\"sa\":\"a\"},\"unit\":\"a\",\"chips\":[{\"text\":\"SA = a\","
            + "\"points\":[\"S\",\"A\"]}],\"asked\":[{\"name\":\"\",\"kind\":\"angle_line_plane\","
            + "\"points\":[\"S\",\"D\",\"A\",\"B\",\"D\"]}],\"hidden\":[\"ang:S-D-A\"],\"answer\":\"45°\"}";
    String solution =
        "{\"answer\":\"45°\",\"steps\":[{\"title\":\"Hình chiếu\",\"body\":\"AD là hình chiếu của SD.\","
            + "\"why\":\"SA ⊥ đáy\"}]}";
    FAKE.enqueue(
        Reply.ok(FakeAnthropic.message(scene, 900, 200)),
        Reply.ok(FakeAnthropic.message(solution, 700, 120)));
    var r =
        postJson(
            "/api/v1/student/self-study",
            an(),
            "{\"statement\":\"Cho hình chóp S.ABCD có đáy là hình vuông cạnh a, SA vuông góc với đáy, SA = a."
                + " Tính góc giữa SD và mặt đáy.\"}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    assertThat(r.body()).contains("\"coordSource\":\"engine\"").contains("Đã kiểm chứng");
  }
}
