package vn.geo.server.assignment.internal.web;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.S2E8SandboxService;

/**
 * MÓC KIỂM THỬ S2-E8, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169): {@code POST
 * /test-hooks/s2e8/sandbox} dựng giáo viên mới + lớp + học sinh riêng cho một ca kiểm. Giờ mở lời
 * giải của một câu dùng lại {@code POST /test-hooks/e6/release} (mỗi câu là một hàng assignment).
 */
@RestController
@Profile({"dev", "test"})
public class S2E8TestHookController {

  public record SandboxRequest(List<S2E8SandboxService.ClassSpec> classes) {}

  private final S2E8SandboxService sandbox;

  public S2E8TestHookController(S2E8SandboxService sandbox) {
    this.sandbox = sandbox;
  }

  @PostMapping("/test-hooks/s2e8/sandbox")
  public S2E8SandboxService.SandboxOut create(@RequestBody SandboxRequest req) {
    return sandbox.create(req.classes());
  }
}
