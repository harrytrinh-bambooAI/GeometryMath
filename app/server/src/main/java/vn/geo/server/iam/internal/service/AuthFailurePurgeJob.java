package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.geo.server.iam.internal.repo.AuthRepository;

/**
 * PO-71 (BR-S3E5-37): công việc dọn xóa {@code auth_failure} (HMAC của IP) mỗi 10 phút — hạn lưu
 * tối đa 1 giờ thay cho 1 ngày. Cửa sổ đếm thử sai chỉ 15 phút nên dọn hàng cũ hơn 1 giờ không đổi
 * kết quả giới hạn. Lịch cron {@code geo.auth.failure-purge-cron} (mặc định mỗi 10 phút; "-" =
 * tắt). Lũy đẳng: chạy lặp hay nhiều máy chủ cùng lúc chỉ xóa hàng đã quá hạn.
 */
@Component
public class AuthFailurePurgeJob {

  public static final String CRON_PROPERTY = "geo.auth.failure-purge-cron";
  public static final String DEFAULT_CRON = "0 */10 * * * *";
  public static final Duration RETENTION = Duration.ofHours(1);

  private static final Logger log = LoggerFactory.getLogger(AuthFailurePurgeJob.class);

  private final AuthRepository repo;
  private final Clock clock;

  public AuthFailurePurgeJob(AuthRepository repo, Clock clock) {
    this.repo = repo;
    this.clock = clock;
  }

  @Scheduled(cron = "${" + CRON_PROPERTY + ":" + DEFAULT_CRON + "}", zone = AccountPurgeJob.ZONE)
  public void tick() {
    try {
      runOnce();
    } catch (RuntimeException e) {
      log.warn("dọn auth_failure thất bại, thử lại ở lượt sau", e);
    }
  }

  /** Một lượt dọn; trả số hàng đã xóa. */
  public long runOnce() {
    long n = repo.purgeAuthFailures(clock.instant().minus(RETENTION));
    if (n > 0) {
      log.info("dọn lần thử sai cũ hơn 1 giờ: {}", n);
    }
    return n;
  }
}
