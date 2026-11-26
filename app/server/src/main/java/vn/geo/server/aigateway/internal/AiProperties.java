package vn.geo.server.aigateway.internal;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình cổng AI (US-GEO-103). Đổi model bằng cấu hình, không sửa mã của module gọi cổng (SC2).
 *
 * @param provider tên nhà cung cấp đang dùng: "mock" (D-GEO-047) hoặc "anthropic" (PO-60)
 * @param timeout trần thời gian chờ một lần gọi ở cổng, kể cả thử lại (D-GEO-072, tạm)
 * @param mockLatency độ trễ giả lập của nhà cung cấp mock
 * @param model model; rỗng = mặc định theo provider (mock: gia-lap-1, F-GEO-096; anthropic: {@link
 *     #ANTHROPIC_DEFAULT_MODEL})
 * @param promptVersion phiên bản prompt; rỗng = mặc định theo provider (mock: p1; anthropic: {@link
 *     AnthropicPrompts#VERSION})
 * @param costPerCall chi phí mô phỏng mỗi lượt của mock (F-GEO-096: 0,12; KG-07)
 * @param apiKey khóa của nhà cung cấp; chỉ ở máy chủ, không bao giờ xuống trình duyệt hay nhật ký
 *     (BR-GEO-106)
 * @param anthropic tham số gọi Claude Messages API
 * @param pricing bảng giá USD / 1 triệu token theo model, để tính chi phí từ usage thật
 */
@ConfigurationProperties("geo.ai")
public record AiProperties(
    String provider,
    Duration timeout,
    Duration mockLatency,
    String model,
    String promptVersion,
    BigDecimal costPerCall,
    String apiKey,
    Anthropic anthropic,
    Map<String, Price> pricing) {

  public static final String ANTHROPIC_DEFAULT_MODEL = "claude-sonnet-5-5";

  @org.springframework.boot.context.properties.bind.ConstructorBinding
  public AiProperties {
    if (provider == null || provider.isBlank()) {
      provider = "mock";
    }
    provider = provider.strip();
    if (timeout == null) {
      timeout = Duration.ofSeconds(10);
    }
    if (mockLatency == null) {
      mockLatency = Duration.ZERO;
    }
    boolean real = "anthropic".equals(provider);
    if (model == null || model.isBlank()) {
      model = real ? ANTHROPIC_DEFAULT_MODEL : "gia-lap-1";
    }
    if (promptVersion == null || promptVersion.isBlank()) {
      promptVersion = real ? AnthropicPrompts.VERSION : "p1";
    }
    if (costPerCall == null) {
      costPerCall = new BigDecimal("0.12");
    }
    if (apiKey == null) {
      apiKey = "";
    }
    if (anthropic == null) {
      anthropic = new Anthropic(null, null, null, null, null, null, null, null);
    }
    pricing = pricing == null ? Map.of() : Map.copyOf(pricing);
  }

  public AiProperties(String provider, Duration timeout, Duration mockLatency) {
    this(provider, timeout, mockLatency, null, null, null, null, null, null);
  }

  /** Giá của một model; null nếu không có trong bảng. */
  public Price priceOf(String model) {
    return model == null ? null : pricing.get(model);
  }

  @Override
  public String toString() {
    return "AiProperties[provider=" + provider + ", model=" + model + ", apiKey=***]";
  }

  /**
   * Tham số gọi Claude Messages API.
   *
   * @param baseUrl gốc API (IT trỏ vào máy chủ HTTP giả)
   * @param apiVersion tiêu đề anthropic-version
   * @param maxTokens trần token đầu ra mỗi lượt
   * @param attemptTimeout trần một lần gọi HTTP
   * @param maxRetries số lần thử lại tối đa cho 429/5xx/lỗi mạng
   * @param retryBackoff giãn cách cơ sở giữa hai lần thử (nhân đôi mỗi lần; retry-after nếu có)
   * @param fallbacks giá trị tham số fallbacks ("default"); rỗng = không gửi
   * @param effort output_config.effort (low|medium|high|xhigh|max); rỗng = mặc định của model
   */
  public record Anthropic(
      String baseUrl,
      String apiVersion,
      Integer maxTokens,
      Duration attemptTimeout,
      Integer maxRetries,
      Duration retryBackoff,
      String fallbacks,
      String effort) {
    public Anthropic {
      if (baseUrl == null || baseUrl.isBlank()) {
        baseUrl = "https://api.anthropic.com";
      }
      baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
      if (apiVersion == null || apiVersion.isBlank()) {
        apiVersion = "2023-06-01";
      }
      if (maxTokens == null || maxTokens <= 0) {
        maxTokens = 16000;
      }
      if (attemptTimeout == null) {
        attemptTimeout = Duration.ofSeconds(45);
      }
      if (maxRetries == null || maxRetries < 0) {
        maxRetries = 2;
      }
      if (retryBackoff == null) {
        retryBackoff = Duration.ofSeconds(1);
      }
      if (fallbacks == null) {
        fallbacks = "";
      }
      if (effort == null) {
        effort = "";
      }
    }
  }

  /**
   * Giá USD / 1 triệu token.
   *
   * @param cacheRead bỏ trống = 0,1× input
   * @param cacheWrite bỏ trống = 1,25× input
   */
  public record Price(
      BigDecimal input, BigDecimal output, BigDecimal cacheRead, BigDecimal cacheWrite) {
    public Price {
      input = input == null ? BigDecimal.ZERO : input;
      output = output == null ? BigDecimal.ZERO : output;
      cacheRead = cacheRead == null ? input.multiply(new BigDecimal("0.1")) : cacheRead;
      cacheWrite = cacheWrite == null ? input.multiply(new BigDecimal("1.25")) : cacheWrite;
    }

    /** Chi phí micro-USD của một lượt theo usage. */
    public long costMicro(AiProvider.Usage u) {
      // USD/1M token × token = micro-USD
      BigDecimal micro =
          input
              .multiply(BigDecimal.valueOf(u.inputTokens()))
              .add(output.multiply(BigDecimal.valueOf(u.outputTokens())))
              .add(cacheRead.multiply(BigDecimal.valueOf(u.cacheReadTokens())))
              .add(cacheWrite.multiply(BigDecimal.valueOf(u.cacheWriteTokens())));
      return micro.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
    }
  }
}
