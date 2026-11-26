package vn.geo.server.aigateway.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.internal.FakeAnthropic.Reply;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * AnthropicAiProvider với máy chủ HTTP giả (PO-60): thành công + usage → ai_call_log, 429 → thử
 * lại, 5xx, quá thời gian, JSON hỏng → lỗi sạch của cổng (không INTERNAL/500), khóa không lộ.
 */
@ExtendWith(OutputCaptureExtension.class)
class AnthropicAiProviderTest {

  static final String KEY = "khoa-ai-gia-cua-kiem-thu-0123456789";
  private final JsonMapper mapper = JsonMapper.builder().build();
  private FakeAnthropic fake;
  private final List<AiCallLog.Entry> logged = Collections.synchronizedList(new ArrayList<>());

  @BeforeEach
  void start() throws Exception {
    fake = new FakeAnthropic();
  }

  @AfterEach
  void stop() {
    fake.close();
  }

  private AiProperties props(Duration gatewayTimeout, Duration attempt, int retries) {
    return new AiProperties(
        "anthropic",
        gatewayTimeout,
        Duration.ZERO,
        null,
        null,
        null,
        KEY,
        new AiProperties.Anthropic(
            fake.baseUrl(), null, 4000, attempt, retries, Duration.ofMillis(10), "default", null),
        Map.of(
            "claude-sonnet-5-5",
            new AiProperties.Price(new BigDecimal("2.00"), new BigDecimal("10.00"), null, null)));
  }

  private InstrumentedAiGateway gateway(AiProperties p) {
    return new InstrumentedAiGateway(
        new AnthropicAiProvider(p),
        new AiSettings(p),
        new SimpleMeterRegistry(),
        logged::add,
        new MockAiControl());
  }

  private InstrumentedAiGateway gateway() {
    return gateway(props(Duration.ofSeconds(5), Duration.ofSeconds(2), 2));
  }

  @Test
  void successSendsHeadersAndTaskPromptAndRecordsUsageAndCost() throws Exception {
    fake.enqueue(
        Reply.ok(
            FakeAnthropic.message(
                "```json\n{\"hints\":[\"Gợi ý một\",\"Gợi ý hai\"]}\n```", 1200, 300)));
    var r = gateway().complete(new AiRequest("hint", "Đề bài: hình chóp S.ABCD"));

    assertThat(r.provider()).isEqualTo("anthropic");
    JsonNode out = mapper.readTree(r.content());
    assertThat(out.path("hints").size()).isEqualTo(2);
    var seen = fake.seen().get(0);
    assertThat(seen.header("x-api-key")).isEqualTo(KEY);
    assertThat(seen.header("anthropic-version")).isEqualTo("2023-06-01");
    assertThat(seen.header("anthropic-beta")).isEqualTo(AnthropicAiProvider.FALLBACK_BETA);
    JsonNode body = mapper.readTree(seen.body());
    assertThat(body.path("model").asString()).isEqualTo("claude-sonnet-5-5");
    assertThat(body.path("max_tokens").asInt()).isEqualTo(4000);
    assertThat(body.path("fallbacks").asString()).isEqualTo("default");
    assertThat(body.path("system").get(0).path("text").asString()).contains("HAI gợi ý");
    assertThat(body.path("messages").get(0).path("content").get(0).path("text").asString())
        .isEqualTo("Đề bài: hình chóp S.ABCD");
    assertThat(body.has("temperature")).isFalse();

    assertThat(logged)
        .singleElement()
        .satisfies(e -> {
          assertThat(e.status()).isEqualTo("ok");
          assertThat(e.purpose()).isEqualTo("hint");
          assertThat(e.model()).isEqualTo("claude-sonnet-5-5");
          assertThat(e.promptVersion()).isEqualTo(AnthropicPrompts.VERSION);
          assertThat(e.tokensIn()).isEqualTo(1200);
          assertThat(e.tokensOut()).isEqualTo(300);
          // 1200 × 2 + 300 × 10 = 5400 micro-USD
          assertThat(e.costMicro()).isEqualTo(5400);
        });
  }

  @Test
  void rateLimitedThenOkIsRetried() throws Exception {
    fake.enqueue(
        new Reply(
            429, "{\"error\":{\"type\":\"rate_limit_error\"}}", Map.of("retry-after", "0"), 0),
        Reply.ok(FakeAnthropic.message("{\"text\":\"Cho hình chóp S.ABCD\"}", 10, 5)));
    var r = gateway().complete(new AiRequest("ocr", "Ảnh đề (base64): iVBORw0KGgoAAAANSUhEUg=="));
    assertThat(mapper.readTree(r.content()).path("text").asString()).contains("S.ABCD");
    assertThat(fake.seen()).hasSize(2);
    JsonNode content =
        mapper.readTree(fake.seen().get(1).body()).path("messages").get(0).path("content");
    assertThat(content.get(0).path("type").asString()).isEqualTo("image");
    assertThat(content.get(0).path("source").path("media_type").asString()).isEqualTo("image/png");
    assertThat(logged).singleElement().extracting(AiCallLog.Entry::status).isEqualTo("ok");
  }

  @Test
  void persistentServerErrorStopsAfterMaxRetriesWithDependencyUnavailable() {
    fake.fallback(Reply.status(529, "overloaded_error"));
    assertThatThrownBy(() -> gateway().complete(new AiRequest("hint", "Đề bài: x")))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> assertThat(e.code()).isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE));
    assertThat(fake.seen()).hasSize(3); // 1 + 2 lần thử lại
    assertThat(logged)
        .singleElement()
        .satisfies(e -> {
          assertThat(e.status()).isEqualTo("error");
          assertThat(e.costMicro()).isZero();
        });
  }

  @Test
  void clientErrorIsNotRetried() {
    fake.enqueue(Reply.status(400, "invalid_request_error"));
    assertThatThrownBy(() -> gateway().complete(new AiRequest("hint", "Đề bài: x")))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> assertThat(e.code()).isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE));
    assertThat(fake.seen()).hasSize(1);
  }

  @Test
  void slowAttemptMapsToTimeout() {
    fake.fallback(new Reply(200, FakeAnthropic.message("{}", 1, 1), Map.of(), 1_500));
    var gw = gateway(props(Duration.ofSeconds(5), Duration.ofMillis(300), 2));
    assertThatThrownBy(() -> gw.complete(new AiRequest("hint", "Đề bài: x")))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.TIMEOUT));
    assertThat(logged).singleElement().extracting(AiCallLog.Entry::status).isEqualTo("timeout");
  }

  @Test
  void gatewayCeilingCutsRetriesShort() {
    fake.fallback(new Reply(503, "{}", Map.of("retry-after", "5"), 0));
    var gw = gateway(props(Duration.ofMillis(400), Duration.ofSeconds(2), 5));
    long t0 = System.nanoTime();
    assertThatThrownBy(() -> gw.complete(new AiRequest("hint", "Đề bài: x")))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.TIMEOUT));
    assertThat((System.nanoTime() - t0) / 1_000_000).isLessThan(3_000);
  }

  @Test
  void malformedOutputIsCleanError() {
    fake.enqueue(Reply.ok(FakeAnthropic.message("Tôi không chắc, đây là hình chóp.", 5, 5)));
    fake.enqueue(new Reply(200, "<html>không phải JSON</html>", Map.of(), 0));
    for (int i = 0; i < 2; i++) {
      assertThatThrownBy(() -> gateway().complete(new AiRequest("scene", "Đề bài: x")))
          .isInstanceOfSatisfying(
              AppException.class,
              e -> assertThat(e.code()).isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE));
    }
  }

  @Test
  void refusalAndTruncationAreErrors() {
    fake.enqueue(Reply.ok(FakeAnthropic.message("", 5, 0, "refusal")));
    fake.enqueue(Reply.ok(FakeAnthropic.message("{\"answer\":\"4", 5, 4000, "max_tokens")));
    for (int i = 0; i < 2; i++) {
      assertThatThrownBy(() -> gateway().complete(new AiRequest("solution", "Đề bài: x")))
          .isInstanceOf(AppException.class);
    }
  }

  @Test
  void qaAcceptsPlainTextAndJson() {
    fake.enqueue(Reply.ok(FakeAnthropic.message("Em thử xét tam giác SAD nhé.", 5, 5)));
    fake.enqueue(
        Reply.ok(
            FakeAnthropic.message(
                "Đây: {\"kind\":\"concept\",\"reply\":\"Hình chiếu…\",\"source\":\"hinh-chieu-vuong-goc\"}",
                5,
                5)));
    var gw = gateway();
    assertThat(gw.complete(new AiRequest("qa", "Câu hỏi: ?")).content())
        .isEqualTo("Em thử xét tam giác SAD nhé.");
    assertThat(gw.complete(new AiRequest("qa", "Câu hỏi: ?")).content())
        .startsWith("{\"kind\":\"concept\"");
    String system =
        mapper.readTree(fake.seen().get(0).body()).path("system").get(0).path("text").asString();
    assertThat(system).contains("hinh-chieu-vuong-goc");
  }

  @Test
  void benchmarkSolutionUsesCoordinatePrompt() {
    fake.enqueue(Reply.ok(FakeAnthropic.message("{\"answer\":\"45°\",\"points\":{}}", 5, 5)));
    gateway().complete(new AiRequest("solution", "Mã bài: HKG-01\nĐề bài: x"));
    String system =
        mapper.readTree(fake.seen().get(0).body()).path("system").get(0).path("text").asString();
    assertThat(system).contains("TỌA ĐỘ");
  }

  @Test
  void missingKeyRefusesToStart() {
    var p = new AiProperties("anthropic", null, null, null, null, null, " ", null, null);
    assertThatThrownBy(() -> new AnthropicAiProvider(p))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GEO_AI_API_KEY");
  }

  @Test
  void keyNeverAppearsInLogsOrErrors(CapturedOutput output) {
    fake.fallback(Reply.status(500, "api_error"));
    Throwable t =
        org.assertj.core.api.Assertions.catchThrowable(
            () -> gateway().complete(new AiRequest("hint", "Đề bài: x")));
    StringBuilder all = new StringBuilder(output.getAll());
    for (Throwable c = t; c != null; c = c.getCause()) {
      all.append(c).append('\n');
    }
    all.append(props(Duration.ofSeconds(1), Duration.ofSeconds(1), 0)).append('\n');
    all.append(new AnthropicAiProvider(props(Duration.ofSeconds(1), Duration.ofSeconds(1), 0)));
    assertThat(all.toString()).contains("thử lại").doesNotContain(KEY);
    assertThat(logged).allSatisfy(e -> assertThat(e.toString()).doesNotContain(KEY));
  }

  @Test
  void unknownModelPriceCostsZeroNotFailure() {
    var p = props(Duration.ofSeconds(5), Duration.ofSeconds(2), 0);
    var settings = new AiSettings(p);
    assertThat(settings.costMicro("model-khong-co", new AiProvider.Usage(100, 100, 0, 0))).isZero();
    assertThat(settings.costMicro("claude-sonnet-5-5", new AiProvider.Usage(0, 0, 1000, 1000)))
        .isEqualTo(200 + 2500); // 1000 × 0,2 + 1000 × 2,5
  }
}
