package vn.geo.server.aigateway.internal;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình cổng AI (US-GEO-103). Đổi model bằng cấu hình, không sửa mã của module gọi cổng (SC2).
 *
 * @param provider tên nhà cung cấp đang dùng (MVP chỉ "mock", D-GEO-047)
 * @param timeout trần thời gian chờ một lần gọi (D-GEO-072, tạm)
 * @param mockLatency độ trễ giả lập của nhà cung cấp mock
 * @param model tên model ghi vào nhật ký lượt gọi (F-GEO-096: gia-lap-1)
 * @param promptVersion phiên bản prompt (F-GEO-096: p1)
 * @param costPerCall chi phí mô phỏng mỗi lượt (F-GEO-096: 0,12; đơn vị chưa xác định, KG-07)
 * @param apiKey khóa của nhà cung cấp; chỉ ở máy chủ, không bao giờ xuống trình duyệt (BR-GEO-106)
 */
@ConfigurationProperties("geo.ai")
public record AiProperties(
    String provider,
    Duration timeout,
    Duration mockLatency,
    String model,
    String promptVersion,
    BigDecimal costPerCall,
    String apiKey) {
  @org.springframework.boot.context.properties.bind.ConstructorBinding
  public AiProperties {
    if (provider == null) {
      provider = "mock";
    }
    if (timeout == null) {
      timeout = Duration.ofSeconds(10);
    }
    if (mockLatency == null) {
      mockLatency = Duration.ZERO;
    }
    if (model == null || model.isBlank()) {
      model = "gia-lap-1";
    }
    if (promptVersion == null || promptVersion.isBlank()) {
      promptVersion = "p1";
    }
    if (costPerCall == null) {
      costPerCall = new BigDecimal("0.12");
    }
    if (apiKey == null) {
      apiKey = "";
    }
  }

  public AiProperties(String provider, Duration timeout, Duration mockLatency) {
    this(provider, timeout, mockLatency, null, null, null, null);
  }

  @Override
  public String toString() {
    return "AiProperties[provider=" + provider + ", model=" + model + ", apiKey=***]";
  }
}
