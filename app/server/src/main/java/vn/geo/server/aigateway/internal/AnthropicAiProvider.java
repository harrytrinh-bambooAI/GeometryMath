package vn.geo.server.aigateway.internal;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Nhà cung cấp AI THẬT (PO-60): Claude Messages API ({@code POST /v1/messages}) qua HttpClient của
 * JDK, không thêm SDK. Chọn bằng {@code geo.ai.provider=anthropic}; khóa chỉ đọc từ {@code
 * GEO_AI_API_KEY} (geo.ai.api-key), không bao giờ ghi ra nhật ký, thông báo lỗi hay ai_call_log.
 *
 * <ul>
 *   <li>Prompt hệ thống theo task ({@link AnthropicPrompts}); đầu ra đưa về đúng định dạng của
 *       MockAiProvider (JSON gọn) để đường kiểm hiện có dùng nguyên.
 *   <li>Thử lại có giới hạn ({@code max-retries}) cho 429/5xx/lỗi mạng, giãn cách theo retry-after
 *       hoặc nhân đôi; lỗi khác (4xx, JSON hỏng, bị từ chối, cắt cụt) trả ngay. Quá thời gian một
 *       lần gọi ném {@link HttpTimeoutException} (cổng ghi "timeout").
 *   <li>Trả usage thật để cổng ghi token vào/ra và chi phí (geo.ai.pricing).
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "geo.ai.provider", havingValue = "anthropic")
public class AnthropicAiProvider implements AiProvider {

  static final String FALLBACK_BETA = "server-side-fallback-2026-07-01";
  private static final Logger log = LoggerFactory.getLogger(AnthropicAiProvider.class);
  private static final Pattern IMAGE = Pattern.compile("Ảnh đề \\(base64\\): ([A-Za-z0-9+/=\\s]+)");
  private static final Duration MAX_RETRY_AFTER = Duration.ofSeconds(30);

  private final AiProperties props;
  private final AiProperties.Anthropic cfg;
  private final String apiKey;
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final HttpClient http;
  private final String lectures;

  public AnthropicAiProvider(AiProperties props) {
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      throw new IllegalStateException(
          "GEO_AI_API_KEY (geo.ai.api-key) chưa đặt; bắt buộc khi geo.ai.provider=anthropic.");
    }
    this.props = props;
    this.cfg = props.anthropic();
    this.apiKey = props.apiKey().strip();
    this.http =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    this.lectures = AnthropicPrompts.lectureList(mapper);
  }

  @Override
  public String name() {
    return "anthropic";
  }

  @Override
  public String complete(ProviderCall call) throws Exception {
    return call(call).content();
  }

  @Override
  public Result call(ProviderCall call) throws Exception {
    String body = mapper.writeValueAsString(requestBody(call));
    int maxRetries = cfg.maxRetries();
    for (int attempt = 0; ; attempt++) {
      HttpResponse<String> resp;
      try {
        resp = http.send(request(body), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      } catch (HttpTimeoutException e) {
        throw e; // quá thời gian một lần gọi: không thử lại (trần tổng ở cổng)
      } catch (IOException e) {
        if (attempt >= maxRetries) {
          throw new AnthropicException("lỗi mạng sau " + (attempt + 1) + " lần thử", e);
        }
        log.info("AI anthropic lỗi mạng, thử lại {}/{}", attempt + 1, maxRetries);
        Thread.sleep(backoff(attempt, null).toMillis());
        continue;
      }
      int status = resp.statusCode();
      if (status == 200) {
        return parse(call.task(), resp.body());
      }
      String type = errorType(resp.body());
      if ((status == 429 || status >= 500) && attempt < maxRetries) {
        Duration wait = backoff(attempt, resp.headers().firstValue("retry-after").orElse(null));
        log.info(
            "AI anthropic HTTP {} ({}), thử lại {}/{} sau {} ms",
            status,
            type,
            attempt + 1,
            maxRetries,
            wait.toMillis());
        Thread.sleep(wait.toMillis());
        continue;
      }
      throw new AnthropicException("HTTP " + status + " (" + type + ")", null);
    }
  }

  // ------------------------------------------------------------------ yêu cầu

  ObjectNode requestBody(ProviderCall call) {
    ObjectNode b = mapper.createObjectNode();
    b.put("model", call.model());
    b.put("max_tokens", cfg.maxTokens());
    ArrayNode system = b.putArray("system");
    ObjectNode sys = system.addObject();
    sys.put("type", "text");
    sys.put("text", AnthropicPrompts.system(call.task(), call.prompt(), lectures));
    sys.putObject("cache_control").put("type", "ephemeral"); // prompt hệ thống ổn định theo task
    ObjectNode msg = b.putArray("messages").addObject();
    msg.put("role", "user");
    ArrayNode content = msg.putArray("content");
    String prompt = call.prompt() == null ? "" : call.prompt();
    Matcher m = "ocr".equals(call.task()) ? IMAGE.matcher(prompt) : null;
    if (m != null && m.find()) {
      String b64 = m.group(1).replaceAll("\\s", "");
      ObjectNode img = content.addObject();
      img.put("type", "image");
      ObjectNode src = img.putObject("source");
      src.put("type", "base64");
      src.put("media_type", mediaType(b64));
      src.put("data", b64);
      content.addObject().put("type", "text").put("text", "Chép lại đề trong ảnh.");
    } else {
      content.addObject().put("type", "text").put("text", prompt);
    }
    if (!cfg.effort().isBlank()) {
      b.putObject("output_config").put("effort", cfg.effort());
    }
    if (!cfg.fallbacks().isBlank()) {
      b.put("fallbacks", cfg.fallbacks());
    }
    return b;
  }

  private HttpRequest request(String body) {
    HttpRequest.Builder r =
        HttpRequest.newBuilder(URI.create(cfg.baseUrl() + "/v1/messages"))
            .timeout(cfg.attemptTimeout())
            .header("content-type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", cfg.apiVersion())
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    if (!cfg.fallbacks().isBlank()) {
      r.header("anthropic-beta", FALLBACK_BETA);
    }
    return r.build();
  }

  /** Loại ảnh theo byte đầu (API nhận JPEG, PNG, GIF, WebP). */
  static String mediaType(String b64) {
    byte[] h;
    try {
      h = Base64.getDecoder().decode(b64.substring(0, Math.min(b64.length(), 24)));
    } catch (IllegalArgumentException e) {
      h = new byte[0];
    }
    if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8) {
      return "image/jpeg";
    }
    if (h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
      return "image/png";
    }
    if (h.length >= 3 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F') {
      return "image/gif";
    }
    if (h.length >= 12 && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
      return "image/webp";
    }
    throw new AnthropicException("định dạng ảnh không hỗ trợ (chỉ JPEG, PNG, GIF, WebP)", null);
  }

  private Duration backoff(int attempt, String retryAfter) {
    if (retryAfter != null) {
      try {
        Duration d = Duration.ofSeconds(Long.parseLong(retryAfter.strip()));
        return d.compareTo(MAX_RETRY_AFTER) > 0 ? MAX_RETRY_AFTER : d;
      } catch (NumberFormatException ignored) {
        // dạng ngày giờ: dùng giãn cách mặc định
      }
    }
    return cfg.retryBackoff().multipliedBy(1L << Math.min(attempt, 5));
  }

  // ------------------------------------------------------------------ phản hồi

  Result parse(String task, String body) {
    JsonNode root;
    try {
      root = mapper.readTree(body);
    } catch (JacksonException e) {
      throw new AnthropicException("phản hồi không phải JSON", null);
    }
    Usage usage = usage(root.path("usage"));
    String stop = root.path("stop_reason").asString("");
    if ("refusal".equals(stop)) {
      throw new AnthropicException("AI từ chối yêu cầu (refusal)", null);
    }
    if ("max_tokens".equals(stop)) {
      throw new AnthropicException("đầu ra bị cắt (max_tokens)", null);
    }
    StringBuilder text = new StringBuilder();
    for (JsonNode c : root.path("content")) {
      if ("text".equals(c.path("type").asString(""))) {
        text.append(c.path("text").asString(""));
      }
    }
    return new Result(normalize(task, text.toString()), usage);
  }

  private static Usage usage(JsonNode u) {
    return new Usage(
        u.path("input_tokens").asInt(0),
        u.path("output_tokens").asInt(0),
        u.path("cache_read_input_tokens").asInt(0),
        u.path("cache_creation_input_tokens").asInt(0));
  }

  /**
   * Đưa văn bản của AI về định dạng của task: task JSON lấy đối tượng JSON (bỏ ``` hay chữ thừa
   * quanh nó), không đọc được thì ném lỗi; "qa" chấp nhận văn bản thuần; task khác giữ nguyên.
   */
  String normalize(String task, String raw) {
    String t = raw == null ? "" : raw.strip();
    boolean json = AnthropicPrompts.expectsJson(task);
    if (!json && !"qa".equals(task)) {
      return t;
    }
    JsonNode obj = extractObject(t);
    if (obj != null) {
      return mapper.writeValueAsString(obj);
    }
    if (json) {
      throw new AnthropicException("đầu ra của task " + task + " không phải đối tượng JSON", null);
    }
    return t; // qa: văn bản thuần (TrackingService/PreviewService đọc được)
  }

  private JsonNode extractObject(String t) {
    int from = t.indexOf('{');
    int to = t.lastIndexOf('}');
    if (from < 0 || to <= from) {
      return null;
    }
    try {
      JsonNode n = mapper.readTree(t.substring(from, to + 1));
      return n.isObject() ? n : null;
    } catch (JacksonException e) {
      return null;
    }
  }

  private String errorType(String body) {
    try {
      return mapper.readTree(body).path("error").path("type").asString("?");
    } catch (JacksonException | IllegalArgumentException e) {
      return "?";
    }
  }

  /** Lỗi của nhà cung cấp; thông điệp không chứa khóa hay nội dung phản hồi thô. */
  static final class AnthropicException extends RuntimeException {
    AnthropicException(String message, Throwable cause) {
      super("anthropic: " + message, cause);
    }
  }

  @Override
  public String toString() {
    return "AnthropicAiProvider[model=" + props.model() + ", baseUrl=" + cfg.baseUrl() + "]";
  }
}
