package vn.geo.server.content.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.content.internal.service.SelfStudyService;

/** MÓC KIỂM THỬ của E2, chỉ profile dev/test: đếm bài tự học của một học sinh (US-GEO-204-FC3). */
@RestController
@Profile({"dev", "test"})
public class E2TestHookController {

  private final SelfStudyService selfStudy;

  public E2TestHookController(SelfStudyService selfStudy) {
    this.selfStudy = selfStudy;
  }

  @GetMapping("/test-hooks/e2/self-study-count")
  public Map<String, Long> count(@RequestParam UUID userId) {
    return Map.of("count", selfStudy.selfAssignmentCount(userId));
  }
}
