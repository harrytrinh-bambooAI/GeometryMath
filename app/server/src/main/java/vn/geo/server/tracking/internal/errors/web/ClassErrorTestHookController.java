package vn.geo.server.tracking.internal.errors.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.tracking.internal.errors.service.ClassErrorHookSupport;

/**
 * MÓC KIỂM THỬ S5-E5b (chỉ dev/test): tin nhắn đã gửi task class_errors của giáo viên; các lượt
 * tổng hợp của bài (chế độ, kết quả kiểm máy chủ "loại n nhận định"); việc AI nền của một bài nộp.
 */
@RestController
@Profile({"dev", "test"})
public class ClassErrorTestHookController {

  private final ClassErrorHookSupport hooks;

  public ClassErrorTestHookController(ClassErrorHookSupport hooks) {
    this.hooks = hooks;
  }

  @GetMapping("/test-hooks/s5e5b/error-prompts")
  public List<String> sent(@RequestParam UUID teacherId) {
    return hooks.prompts(teacherId);
  }

  @GetMapping("/test-hooks/s5e5b/error-runs")
  public List<Map<String, Object>> runs(@RequestParam UUID orgId, @RequestParam UUID assignmentId) {
    return hooks.runs(orgId, assignmentId);
  }

  @GetMapping("/test-hooks/s5e5b/jobs")
  public List<Map<String, Object>> jobs(@RequestParam UUID orgId, @RequestParam UUID refId) {
    return hooks.jobs(orgId, refId);
  }

  public record BankAccess(UUID orgId, String bank) {}

  /** Cấp bộ đề kho 's5_kc' (seed s5_700) cho trường của thế giới E2E. */
  @org.springframework.web.bind.annotation.PostMapping("/test-hooks/s5e5b/bank-access")
  public Map<String, Object> bankAccess(
      @org.springframework.web.bind.annotation.RequestBody BankAccess b) {
    hooks.grantBank(b.orgId(), b.bank() == null ? "s5_kc" : b.bank());
    return Map.of("ok", true);
  }
}
