package vn.geo.server.assessment.internal.storage.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.storage.service.S5E4HookSupport;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * MÓC KIỂM THỬ S5-E4 (chỉ profile dev/test, PO-51; T-261128-57): xem {@link S5E4HookSupport}. Học
 * sinh "nộp ảnh" ở đây vì luồng chụp nộp (US-S5E4-03) là của T-261128-59.
 */
@RestController
@Profile({"dev", "test"})
public class S5E4TestHookController {

  public record ImageBody(
      UUID orgId,
      UUID assignmentId,
      UUID studentId,
      UUID submissionId,
      Integer pageNo,
      String base64,
      Integer count,
      String schoolYear) {}

  public record PageBody(
      UUID orgId, UUID userId, String role, UUID submissionId, Integer pageNo, Long ageSeconds) {}

  public record PurgeBody(UUID assignmentId, String today) {}

  public record RubricBody(UUID orgId, UUID versionId, BigDecimal points) {}

  private final S5E4HookSupport hooks;

  public S5E4TestHookController(S5E4HookSupport hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s5e4/image")
  public Map<String, Object> image(@RequestBody ImageBody b) {
    return hooks.store(
        b.orgId(),
        b.assignmentId(),
        b.studentId(),
        b.submissionId(),
        b.pageNo(),
        b.base64(),
        b.count(),
        b.schoolYear());
  }

  @PostMapping("/test-hooks/s5e4/raw")
  public Map<String, Object> raw(@RequestBody PageBody b) {
    return hooks.raw(b.orgId(), b.submissionId(), page(b));
  }

  @PostMapping("/test-hooks/s5e4/sign")
  public Map<String, Object> sign(@RequestBody PageBody b) {
    return as(
        b,
        () -> hooks.sign(b.submissionId(), page(b), b.ageSeconds() == null ? 0 : b.ageSeconds()));
  }

  @PostMapping("/test-hooks/s5e4/ocr")
  public Map<String, Object> ocr(@RequestBody PageBody b) {
    return as(b, () -> hooks.ocr(b.orgId(), b.submissionId(), page(b)));
  }

  @PostMapping("/test-hooks/s5e4/purge-accounts")
  public Map<String, Object> purgeAccounts(@RequestBody PurgeBody b) {
    return hooks.purgeAccounts(b.assignmentId());
  }

  @PostMapping("/test-hooks/s5e4/purge-year")
  public Map<String, Object> purgeYear(@RequestBody PurgeBody b) {
    return hooks.purgeYear(b.assignmentId(), LocalDate.parse(b.today()));
  }

  @GetMapping("/test-hooks/s5e4/state")
  public Map<String, Object> state(
      @RequestParam UUID orgId,
      @RequestParam(required = false) UUID assignmentId,
      @RequestParam(required = false) UUID submissionId,
      @RequestParam(required = false) String schoolYear,
      @RequestParam(required = false) String ids) {
    List<UUID> list =
        ids == null || ids.isBlank()
            ? List.of()
            : Arrays.stream(ids.split(",")).map(String::strip).map(UUID::fromString).toList();
    return hooks.state(orgId, assignmentId, submissionId, schoolYear, list);
  }

  @PostMapping("/test-hooks/s5e4/rubric-raw-update")
  public Map<String, Object> rubricRawUpdate(@RequestBody RubricBody b) {
    return hooks.rawRubricUpdate(b.orgId(), b.versionId(), b.points());
  }

  private static int page(PageBody b) {
    return b.pageNo() == null ? 1 : b.pageNo();
  }

  private static <R> R as(PageBody b, ScopedValue.CallableOp<R, RuntimeException> op) {
    Role role = "STUDENT".equals(b.role()) ? Role.STUDENT : Role.TEACHER;
    return RequestContext.callAs(new AuthPrincipal(b.orgId(), b.userId(), role), op);
  }
}
