package vn.geo.server.tracking.internal.classboard.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService;
import vn.geo.server.tracking.internal.classboard.service.StuckSettings;

/**
 * MÓC KIỂM THỬ S3-E3, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169):
 *
 * <ul>
 *   <li>{@code POST /test-hooks/s3e3/sandbox}: dựng giáo viên, lớp, học sinh, bộ bài, bảng trạng
 *       thái và tương tác riêng cho một ca kiểm;
 *   <li>{@code POST /test-hooks/s3e3/events}: thêm một tương tác của học sinh (mở đề, gợi ý, câu
 *       hỏi, gửi đáp số);
 *   <li>{@code POST /test-hooks/s3e3/stuck-script}: đáp mẫu của bộ gộp giả lập theo giáo viên và vị
 *       trí đề (JSON nguyên văn; rỗng = bỏ kịch bản).
 * </ul>
 *
 * Chế độ lỗi / quá giờ của nhà cung cấp giả lập dùng lại {@code POST /test-hooks/ai}.
 */
@RestController
@Profile({"dev", "test"})
public class S3E3TestHookController {

  public record SandboxRequest(
      List<S3E3SandboxService.ClassSpec> classes,
      List<S3E3SandboxService.ClassSpec> otherClasses) {}

  public record EventRequest(
      UUID assignmentId,
      UUID userId,
      String kind,
      Integer level,
      String text,
      Boolean correct,
      String at) {}

  public record ScriptRequest(UUID userId, Map<Integer, String> replies) {}

  private final S3E3SandboxService sandbox;
  private final StuckSettings settings;

  public S3E3TestHookController(S3E3SandboxService sandbox, StuckSettings settings) {
    this.sandbox = sandbox;
    this.settings = settings;
  }

  @PostMapping("/test-hooks/s3e3/sandbox")
  public S3E3SandboxService.SandboxOut create(@RequestBody SandboxRequest req) {
    return sandbox.create(req.classes(), req.otherClasses());
  }

  @PostMapping("/test-hooks/s3e3/events")
  public Map<String, Boolean> event(@RequestBody EventRequest e) {
    sandbox.event(
        e.assignmentId(),
        e.userId(),
        new S3E3SandboxService.EventSpec(
            null, 0, e.kind(), e.level(), e.text(), e.correct(), e.at()));
    return Map.of("ok", true);
  }

  @PostMapping("/test-hooks/s3e3/stuck-script")
  public Map<String, Boolean> script(@RequestBody ScriptRequest r) {
    settings.script(r.userId(), r.replies());
    return Map.of("ok", true);
  }
}
