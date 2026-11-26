package vn.geo.server.iam.internal.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * US-S3E5-05 SC4: công việc dọn dữ liệu tài khoản đã xóa quá 30 ngày, chạy theo chu kỳ {@code
 * geo.s3e5.account-purge-interval} (mặc định 24 giờ; 0 = tắt, dùng ở test — ca kiểm gọi thẳng
 * {@link AccountDeletionService#purge}). Nhật ký chỉ ghi số tài khoản đã dọn, không mã người dùng.
 */
@Component
public class AccountPurgeJob {

  private static final Logger log = LoggerFactory.getLogger(AccountPurgeJob.class);

  private final AccountDeletionService deletion;
  private final Clock clock;
  private final Duration interval;
  private ScheduledExecutorService exec;

  public AccountPurgeJob(
      AccountDeletionService deletion,
      Clock clock,
      @Value("${geo.s3e5.account-purge-interval:PT24H}") Duration interval) {
    this.deletion = deletion;
    this.clock = clock;
    this.interval = interval;
  }

  @PostConstruct
  void start() {
    if (interval.isZero() || interval.isNegative()) {
      return;
    }
    exec =
        Executors.newSingleThreadScheduledExecutor(r -> {
          Thread t = new Thread(r, "account-purge");
          t.setDaemon(true);
          return t;
        });
    long sec = interval.toSeconds();
    exec.scheduleWithFixedDelay(this::runOnce, sec, sec, TimeUnit.SECONDS);
  }

  void runOnce() {
    try {
      int n = deletion.purge(clock.instant());
      if (n > 0) {
        log.info("dọn tài khoản đã xóa quá 30 ngày: {}", n);
      }
    } catch (RuntimeException e) {
      log.warn("dọn tài khoản đã xóa thất bại, thử lại ở lượt sau", e);
    }
  }

  @PreDestroy
  void stop() {
    if (exec != null) {
      exec.shutdownNow();
    }
  }
}
