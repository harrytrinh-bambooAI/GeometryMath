package vn.geo.server.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình thẻ xác thực.
 *
 * @param secret khóa HMAC-SHA256, tối thiểu 32 ký tự; môi trường thật phải đặt qua biến môi trường
 * @param ttl thời hạn thẻ
 */
@ConfigurationProperties("geo.auth")
public record AuthProperties(String secret, Duration ttl) {
  public AuthProperties {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("geo.auth.secret phải dài tối thiểu 32 ký tự");
    }
    if (ttl == null) {
      ttl = Duration.ofHours(8);
    }
  }
}
