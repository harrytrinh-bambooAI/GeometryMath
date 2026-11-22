package vn.geo.server.common.web;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình HTTP của ứng dụng (bảng giá trị ở {@code app/docs/CONFIG.md}).
 *
 * @param maxBodyBytes trần kích thước thân yêu cầu (mặc định 1 MB)
 * @param retryAfter giá trị Retry-After mặc định cho 429/503 quá tải (mặc định 2 giây)
 */
@ConfigurationProperties("geo.http")
public record HttpProperties(long maxBodyBytes, Duration retryAfter) {
  public HttpProperties {
    if (maxBodyBytes <= 0) {
      maxBodyBytes = 1_048_576;
    }
    if (retryAfter == null || retryAfter.isNegative() || retryAfter.isZero()) {
      retryAfter = Duration.ofSeconds(2);
    }
  }
}
