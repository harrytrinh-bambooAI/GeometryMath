package vn.geo.server.common.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Múi giờ. Máy chủ lưu và truyền mọi thời điểm ở UTC (ISO-8601 "...Z"). {@code displayZone} là múi
 * giờ hiển thị DUY NHẤT của sản phẩm (BR-GEO-233: giao diện luôn hiển thị giờ Việt Nam), dùng khi
 * máy chủ phải dựng chữ có giờ (ví dụ thông điệp "mở lúc 20:00"). Không theo múi giờ thiết bị.
 *
 * @param displayZone mặc định Asia/Ho_Chi_Minh
 */
@ConfigurationProperties("geo.time")
public record TimeProperties(ZoneId displayZone) {
  public static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

  public TimeProperties {
    if (displayZone == null) {
      displayZone = VIETNAM;
    }
  }
}
