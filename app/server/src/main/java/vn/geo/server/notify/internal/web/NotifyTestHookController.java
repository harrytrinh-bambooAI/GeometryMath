package vn.geo.server.notify.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.notify.internal.service.NotifyHookService;

/**
 * MÓC KIỂM THỬ S3-E4, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169): dựng thông báo thử
 * và chạy dọn dẹp ngay.
 */
@RestController
@Profile({"dev", "test"})
public class NotifyTestHookController {

  public record Seed(UUID userId, int count, int ageDays) {}

  private final NotifyHookService hooks;

  public NotifyTestHookController(NotifyHookService hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s3e4/notifications/seed")
  public Map<String, Object> seed(@RequestBody Seed b) {
    return Map.of("inserted", hooks.seed(b.userId(), b.count(), b.ageDays()));
  }

  @PostMapping("/test-hooks/s3e4/notifications/cleanup")
  public Map<String, Object> cleanup() {
    return Map.of("deleted", hooks.cleanup());
  }
}
