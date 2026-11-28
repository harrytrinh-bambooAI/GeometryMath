package vn.geo.server.aigrade.internal.shadow.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.shadow.service.ShadowService;
import vn.geo.server.common.web.NoStore;

/**
 * So sánh AI – giáo viên của bài chạy song song đã duyệt (US-S5E6-05-SC2, FC1). Đặc tả ghi {@code
 * GET /api/submissions/{id}/comparison}; app: {@code /api/v1/…}. Chưa duyệt: CONFLICT MSG-S5-47.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class ShadowController {

  private final ShadowService service;

  public ShadowController(ShadowService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{id}/comparison")
  public Map<String, Object> comparison(@PathVariable UUID id) {
    return service.comparison(id);
  }
}
