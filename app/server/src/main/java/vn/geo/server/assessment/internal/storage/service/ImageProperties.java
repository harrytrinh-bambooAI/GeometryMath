package vn.geo.server.assessment.internal.storage.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình kho ảnh bài làm (US-S5E4-08; application.yml khối T-261128-57).
 *
 * @param backend {@code local} (tệp mã hoá AES-256-GCM, dev/test) | {@code s3} (kho tại Việt Nam,
 *     QS5-18; chưa cấu hình ở đợt này)
 * @param dir thư mục kho cục bộ
 * @param key khoá AES-256 base64; trống = khoá suy từ profile (chỉ dev/test)
 * @param signKey khoá HMAC của URL ký; trống = suy như trên
 * @param urlTtl hạn URL ký (BR-S5E4-26: 5 phút)
 * @param purgeCron lịch job xoá (giờ Việt Nam)
 */
@ConfigurationProperties("geo.s5.images")
public record ImageProperties(
    String backend, String dir, String key, String signKey, Duration urlTtl, String purgeCron) {

  public ImageProperties {
    backend = backend == null || backend.isBlank() ? "local" : backend.strip();
    dir = dir == null || dir.isBlank() ? ".data/s5-images" : dir;
    key = key == null ? "" : key.strip();
    signKey = signKey == null ? "" : signKey.strip();
    urlTtl = urlTtl == null ? Duration.ofMinutes(5) : urlTtl;
    purgeCron = purgeCron == null || purgeCron.isBlank() ? "0 20 * * * *" : purgeCron;
  }
}
