package vn.geo.server.lessonplan.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.lessonplan.internal.service.LessonPlanTestHookService;

/** MÓC KIỂM THỬ S5-E1, chỉ profile dev/test (không có ở prod). */
@RestController
@Profile({"dev", "test"})
public class LessonPlanTestHookController {

  private final LessonPlanTestHookService hooks;

  public LessonPlanTestHookController(LessonPlanTestHookService hooks) {
    this.hooks = hooks;
  }

  /** Tin nhắn đã gửi cổng AI + số liệu nội bộ của một giáo án. */
  @GetMapping("/test-hooks/s5e1/plans/{id}")
  public Map<String, Object> plan(@PathVariable UUID id, @RequestParam UUID orgId) {
    return hooks.plan(orgId, id);
  }

  public record Turns(UUID orgId, UUID userId, int count) {}

  /** Thêm {@code count} lượt AI soạn đã dùng hôm nay (giờ VN) cho giáo viên. */
  @PostMapping("/test-hooks/s5e1/turns")
  public Map<String, Object> turns(@RequestBody Turns t) {
    return hooks.seedTurns(t.orgId(), t.userId(), t.count());
  }
}
