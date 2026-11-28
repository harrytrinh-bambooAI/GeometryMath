package vn.geo.server.knowledge.internal.kb.qa.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.knowledge.internal.kb.qa.service.KbQaTestHookService;

/**
 * Móc kiểm thử hỏi đáp theo kho (chỉ profile dev/test; T-261128-58): yêu cầu đã gửi AI của một
 * lượt, nhật ký AIInteraction / AIPolicyDecision / bộ đếm theo luật, bộ kiểm lỗi/chậm, trả bài có
 * chấm, bật nhãn duyệt của bài giảng mẫu e2e.
 */
@RestController
@Profile({"dev", "test"})
public class KbQaTestHookController {

  public record Firewall(UUID userId, Long delayMs, Boolean fault) {}

  public record Release(UUID orgId, UUID gradedId) {}

  public record LectureApproval(String slug, boolean approved) {}

  private final KbQaTestHookService hooks;

  public KbQaTestHookController(KbQaTestHookService hooks) {
    this.hooks = hooks;
  }

  @GetMapping("/test-hooks/s5e3/qa/prompt")
  public Map<String, Object> prompt(@RequestParam UUID interactionId) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("prompt", hooks.prompt(interactionId));
    return m;
  }

  @GetMapping("/test-hooks/s5e3/qa/state")
  public Map<String, Object> state(
      @RequestParam UUID orgId,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) UUID targetId) {
    return hooks.state(orgId, userId, targetId);
  }

  @PostMapping("/test-hooks/s5e3/qa/firewall")
  public Map<String, Object> firewall(@RequestBody Firewall f) {
    hooks.firewall(f.userId(), f.delayMs(), f.fault());
    return Map.of("ok", true);
  }

  @PostMapping("/test-hooks/s5e3/qa/release")
  public Map<String, Object> release(@RequestBody Release r) {
    return Map.of("updated", hooks.release(r.orgId(), r.gradedId()));
  }

  @PostMapping("/test-hooks/s5e3/qa/lecture")
  public Map<String, Object> lecture(@RequestBody LectureApproval l) {
    return Map.of("updated", hooks.lecture(l.slug(), l.approved()));
  }
}
