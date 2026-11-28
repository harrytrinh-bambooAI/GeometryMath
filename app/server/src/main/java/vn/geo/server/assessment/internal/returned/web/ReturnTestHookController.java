package vn.geo.server.assessment.internal.returned.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.returned.service.ReturnHookSupport;

/**
 * MÓC KIỂM THỬ chấm/trả bài S5-E4 (chỉ profile dev/test, PO-51; T-261128-60): xem {@link
 * ReturnHookSupport} (biên thu hồi 15 phút TC-S5E4-38, giờ công bố TC-S5E4-33, TC-S5E4-43).
 */
@RestController
@Profile({"dev", "test"})
public class ReturnTestHookController {

  public record AgeBody(UUID orgId, UUID assignmentId, Long seconds) {}

  private final ReturnHookSupport hooks;

  public ReturnTestHookController(ReturnHookSupport hooks) {
    this.hooks = hooks;
  }

  private static long secs(AgeBody b) {
    return b.seconds() == null ? 0 : b.seconds();
  }

  @PostMapping("/test-hooks/s5e4/return/age")
  public Map<String, Object> age(@RequestBody AgeBody b) {
    return hooks.age(b.orgId(), b.assignmentId(), secs(b));
  }

  @PostMapping("/test-hooks/s5e4/return/release-in")
  public Map<String, Object> releaseIn(@RequestBody AgeBody b) {
    return hooks.releaseIn(b.orgId(), b.assignmentId(), secs(b));
  }

  @GetMapping("/test-hooks/s5e4/return/grades")
  public Map<String, Object> grades(@RequestParam UUID orgId, @RequestParam UUID submissionId) {
    return hooks.grades(orgId, submissionId);
  }
}
