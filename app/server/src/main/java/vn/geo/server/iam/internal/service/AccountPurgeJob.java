package vn.geo.server.iam.internal.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * US-S3E5-05 SC4: công việc dọn dữ liệu tài khoản đã xóa quá 30 ngày, chạy theo lịch cron giờ cố
 * định {@code geo.s3e5.account-purge-cron} (mặc định 03:00 giờ VN mỗi ngày; "-" = tắt). Dev-REV-A3:
 * trước đây là trễ đầu 24 giờ trong tiến trình — máy chủ khởi động lại mỗi ngày thì không bao giờ
 * chạy; lịch cron không phụ thuộc giờ khởi động. Lũy đẳng: chạy lặp (kể cả nhiều máy chủ cùng giờ)
 * chỉ xóa bia mộ đã quá hạn, lượt sau trả 0. Nhật ký chỉ ghi số tài khoản đã dọn, không mã người
 * dùng.
 */
@Component
public class AccountPurgeJob {

  public static final String CRON_PROPERTY = "geo.s3e5.account-purge-cron";
  public static final String DEFAULT_CRON = "0 0 3 * * *";
  public static final String ZONE = "Asia/Ho_Chi_Minh";

  private static final Logger log = LoggerFactory.getLogger(AccountPurgeJob.class);

  private final AccountDeletionService deletion;
  private final Clock clock;

  public AccountPurgeJob(AccountDeletionService deletion, Clock clock) {
    this.deletion = deletion;
    this.clock = clock;
  }

  @Scheduled(cron = "${" + CRON_PROPERTY + ":" + DEFAULT_CRON + "}", zone = ZONE)
  public void runOnce() {
    try {
      int n = deletion.purge(clock.instant());
      if (n > 0) {
        log.info("dọn tài khoản đã xóa quá 30 ngày: {}", n);
      }
    } catch (RuntimeException e) {
      log.warn("dọn tài khoản đã xóa thất bại, thử lại ở lượt sau", e);
    }
  }
}
