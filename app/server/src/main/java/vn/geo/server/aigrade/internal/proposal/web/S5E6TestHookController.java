package vn.geo.server.aigrade.internal.proposal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.proposal.service.ProposalHookSupport;

/** MÓC KIỂM THỬ S5-E6 (chỉ profile dev/test; T-261128-61): xem {@link ProposalHookSupport}. */
@RestController
@Profile({"dev", "test"})
public class S5E6TestHookController {

  public record SetupBody(UUID orgId, UUID assignmentId) {}

  public record RunBody(UUID orgId, UUID submissionId) {}

  private final ProposalHookSupport hooks;

  public S5E6TestHookController(ProposalHookSupport hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s5e6/setup")
  public Map<String, Object> setup(@RequestBody SetupBody b) {
    return hooks.setup(b.orgId(), b.assignmentId());
  }

  @PostMapping("/test-hooks/s5e6/run")
  public Map<String, Object> run(@RequestBody RunBody b) {
    return hooks.run(b.orgId(), b.submissionId());
  }

  @GetMapping("/test-hooks/s5e6/prompt")
  public Map<String, Object> prompt(@RequestParam UUID orgId, @RequestParam UUID submissionId) {
    return hooks.prompt(orgId, submissionId);
  }

  @GetMapping("/test-hooks/s5e6/counts")
  public Map<String, Object> counts(@RequestParam UUID orgId, @RequestParam UUID submissionId) {
    return hooks.counts(orgId, submissionId);
  }
}
