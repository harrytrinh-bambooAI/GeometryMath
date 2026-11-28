package vn.geo.server.assessment.internal.storage.service;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.geo.server.common.config.TimeProperties;

/**
 * Lịch job xoá ảnh (BR-S5E4-28): mỗi giờ (xoá theo tài khoản trong 24 giờ; theo năm học từ 31/07).
 * Chỉ chạy khi cờ {@code geo.s5.grading.enabled} bật.
 */
@Component
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class ImagePurgeJob {

  private final ImagePurgeService purge;
  private final Clock clock;

  public ImagePurgeJob(ImagePurgeService purge, Clock clock) {
    this.purge = purge;
    this.clock = clock;
  }

  @Scheduled(cron = "${geo.s5.images.purge-cron:0 20 * * * *}", zone = "Asia/Ho_Chi_Minh")
  public void run() {
    purge.purgeDeletedAccounts(null);
    purge.purgeEndedYears(LocalDate.ofInstant(clock.instant(), TimeProperties.VIETNAM), null);
  }
}
