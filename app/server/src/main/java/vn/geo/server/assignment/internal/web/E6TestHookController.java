package vn.geo.server.assignment.internal.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.E6SandboxService;
import vn.geo.server.assignment.internal.service.ProbeRunner;
import vn.geo.server.assignment.internal.service.ProbeService;

/**
 * MÓC KIỂM THỬ E6, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169).
 *
 * <ul>
 *   <li>{@code POST /test-hooks/e6/sandbox}: giáo viên mới + lớp + học sinh + bài giao riêng cho
 *       một ca kiểm.
 *   <li>{@code POST /test-hooks/e6/release}: giờ mở = giờ máy chủ + n giây (ca "tới giờ thì tự mở"
 *       chạy trong vài giây thay vì chờ 60 giây hay 60 phút thật).
 *   <li>{@code POST /test-hooks/e6/probe/run}, {@code GET /test-hooks/e6/probe/reports}: chạy bộ
 *       câu thử, đọc báo cáo.
 *   <li>{@code POST /test-hooks/e6/probe/load}, {@code GET /test-hooks/e6/probe/reference}: nạp
 *       câu, đáp số tham chiếu.
 * </ul>
 */
@RestController
@Profile({"dev", "test"})
public class E6TestHookController {

  public record SandboxRequest(List<E6SandboxService.ClassSpec> classes) {}

  public record ReleaseIn(UUID assignmentId, Double seconds) {}

  private final E6SandboxService sandbox;
  private final ProbeRunner runner;
  private final ProbeService probes;

  public E6TestHookController(E6SandboxService sandbox, ProbeRunner runner, ProbeService probes) {
    this.sandbox = sandbox;
    this.runner = runner;
    this.probes = probes;
  }

  @PostMapping("/test-hooks/e6/sandbox")
  public E6SandboxService.SandboxOut create(@RequestBody SandboxRequest req) {
    return sandbox.create(req.classes());
  }

  @PostMapping("/test-hooks/e6/release")
  public Map<String, OffsetDateTime> releaseIn(@RequestBody ReleaseIn r) {
    return Map.of(
        "releaseAt", sandbox.releaseIn(r.assignmentId(), r.seconds() == null ? 0 : r.seconds()));
  }

  @PostMapping("/test-hooks/e6/probe/run")
  public Map<String, Object> run(@RequestBody(required = false) ProbeRunner.RunOptions o) {
    return runner.view(runner.run(o == null ? new ProbeRunner.RunOptions(null, null, false) : o));
  }

  @GetMapping("/test-hooks/e6/probe/reports")
  public List<Map<String, Object>> reports(@RequestParam(defaultValue = "20") int limit) {
    return runner.list(limit);
  }

  @PostMapping("/test-hooks/e6/probe/load")
  public ProbeService.LoadResult load(@RequestBody List<ProbeService.Item> items) {
    return probes.load(items);
  }

  @GetMapping("/test-hooks/e6/probe/reference")
  public ProbeService.Reference reference(@RequestParam String code) {
    return probes.reference(code).orElseThrow();
  }
}
