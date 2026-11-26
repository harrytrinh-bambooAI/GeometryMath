package vn.geo.server.iam.internal.web;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.iam.internal.service.AccountDeletionService;

/** Móc kiểm thử US-S3E5-05 (chỉ dev/test): chạy công việc dọn như thể đã qua {@code days} ngày. */
@RestController
@Profile({"dev", "test"})
public class AccountTestHooks {

  public record Purge(long days) {}

  private final AccountDeletionService deletion;
  private final Clock clock;

  public AccountTestHooks(AccountDeletionService deletion, Clock clock) {
    this.deletion = deletion;
    this.clock = clock;
  }

  @PostMapping("/test-hooks/s3e5/account/purge")
  public Map<String, Object> purge(@RequestBody Purge b) {
    return Map.of("purged", deletion.purge(clock.instant().plus(Duration.ofDays(b.days()))));
  }
}
