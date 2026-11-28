package vn.geo.server.knowledge.internal.kb.history.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.knowledge.internal.kb.history.service.QaHistoryTestHookService;
import vn.geo.server.knowledge.internal.kb.history.service.QaHistoryTestHookService.Seed;

/** Móc kiểm thử lịch sử hỏi đáp (chỉ profile dev/test; T-261128-63). */
@RestController
@Profile({"dev", "test"})
public class QaHistoryTestHookController {

  private final QaHistoryTestHookService hooks;

  public QaHistoryTestHookController(QaHistoryTestHookService hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s5e3/history/seed")
  public Map<String, Object> seed(@RequestBody Seed seed) {
    return hooks.seed(seed);
  }

  @GetMapping("/test-hooks/s5e3/history/reply")
  public Map<String, Object> reply(@RequestParam UUID orgId, @RequestParam UUID interactionId) {
    return hooks.reply(orgId, interactionId);
  }
}
