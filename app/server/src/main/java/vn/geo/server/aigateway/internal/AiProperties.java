package vn.geo.server.aigateway.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình cổng AI.
 *
 * @param provider tên nhà cung cấp đang dùng (hiện chỉ "mock")
 * @param timeout trần thời gian chờ một lần gọi
 * @param mockLatency độ trễ giả lập của nhà cung cấp mock
 */
@ConfigurationProperties("geo.ai")
public record AiProperties(String provider, Duration timeout, Duration mockLatency) {
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
  }
}
