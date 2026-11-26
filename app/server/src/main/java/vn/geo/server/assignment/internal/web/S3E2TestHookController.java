package vn.geo.server.assignment.internal.web;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.S3E2SandboxService;

/**
 * MÓC KIỂM THỬ S3-E2, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169): {@code POST
 * /test-hooks/s3e2/sandbox} dựng Cô Lan + Thầy Khôi mới, lớp, học sinh Đang học / Chờ duyệt riêng
 * cho một ca kiểm. Giờ mở của một câu đã giao dùng lại {@code POST /test-hooks/e6/release}.
 */
@RestController
@Profile({"dev", "test"})
public class S3E2TestHookController {

  public record SandboxRequest(
      List<S3E2SandboxService.ClassSpec> classes,
      List<S3E2SandboxService.ClassSpec> otherClasses) {}

  private final S3E2SandboxService sandbox;

  public S3E2TestHookController(S3E2SandboxService sandbox) {
    this.sandbox = sandbox;
  }

  @PostMapping("/test-hooks/s3e2/sandbox")
  public S3E2SandboxService.SandboxOut create(@RequestBody SandboxRequest req) {
    return sandbox.create(req.classes(), req.otherClasses());
  }
}
