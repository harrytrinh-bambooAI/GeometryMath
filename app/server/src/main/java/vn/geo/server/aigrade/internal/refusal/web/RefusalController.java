package vn.geo.server.aigrade.internal.refusal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.refusal.service.RefusalService;
import vn.geo.server.common.web.NoStore;

/** Nút "Thử chấm lại" của bài "AI không chấm" (US-S5E6-02-FC1). Sau cờ AI chấm. */
@RestController
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class RefusalController {

  private final RefusalService service;

  public RefusalController(RefusalService service) {
    this.service = service;
  }

  @NoStore
  @PostMapping("/api/v1/submissions/{id}/proposal/retry")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> retry(@PathVariable UUID id) {
    return service.retry(id);
  }
}
