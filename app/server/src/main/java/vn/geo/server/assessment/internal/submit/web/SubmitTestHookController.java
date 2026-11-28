package vn.geo.server.assessment.internal.submit.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.submit.service.SubmitHookSupport;

/**
 * MÓC KIỂM THỬ nộp bài S5-E4 (chỉ profile dev/test, PO-51; T-261128-59): xem {@link
 * SubmitHookSupport}.
 */
@RestController
@Profile({"dev", "test"})
public class SubmitTestHookController {

  public record DueBody(UUID orgId, UUID assignmentId, Long secondsFromNow, Boolean allowLate) {}

  private final SubmitHookSupport hooks;

  public SubmitTestHookController(SubmitHookSupport hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s5e4/submit/due")
  public Map<String, Object> due(@RequestBody DueBody b) {
    return hooks.due(b.orgId(), b.assignmentId(), b.secondsFromNow(), b.allowLate());
  }

  @GetMapping("/test-hooks/s5e4/submit/state")
  public Map<String, Object> state(
      @RequestParam UUID orgId, @RequestParam UUID submissionId, @RequestParam UUID studentId) {
    return hooks.state(orgId, submissionId, studentId);
  }

  @GetMapping("/test-hooks/s5e4/submit/count")
  public Map<String, Object> count(
      @RequestParam UUID orgId,
      @RequestParam UUID assignmentId,
      @RequestParam(required = false) UUID studentId) {
    return hooks.count(orgId, assignmentId, studentId);
  }

  @GetMapping("/test-hooks/s5e4/submit/ocr-prompt")
  public Map<String, Object> prompt(
      @RequestParam UUID orgId, @RequestParam UUID versionId, @RequestParam int pageNo) {
    return hooks.prompt(orgId, versionId, pageNo);
  }
}
