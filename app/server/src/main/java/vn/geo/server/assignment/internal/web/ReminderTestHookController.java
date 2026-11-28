package vn.geo.server.assignment.internal.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.ReminderService;

/**
 * MÓC KIỂM THỬ US-S3E6-03, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169): chạy một lượt
 * tác vụ nhắc việc với đồng hồ giả lập.
 */
@RestController
@Profile({"dev", "test"})
public class ReminderTestHookController {

  public record RunAt(Instant at) {}

  private final ReminderService reminders;

  public ReminderTestHookController(ReminderService reminders) {
    this.reminders = reminders;
  }

  @PostMapping("/test-hooks/s3e6/reminders/run")
  public Map<String, Object> run(@RequestBody RunAt b) {
    ReminderService.Run r = reminders.runAt(b.at() == null ? Instant.now() : b.at());
    return Map.of("dueSoonEvents", r.dueSoonEvents(), "releasedEvents", r.releasedEvents());
  }
}
