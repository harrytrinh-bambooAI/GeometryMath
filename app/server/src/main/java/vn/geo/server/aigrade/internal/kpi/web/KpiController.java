package vn.geo.server.aigrade.internal.kpi.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.kpi.service.KpiService;
import vn.geo.server.common.web.NoStore;

/**
 * Báo cáo khớp AI–giáo viên và cổng "AI đề xuất" của lớp (US-S5E6-06). Đặc tả ghi {@code GET
 * /api/classes/{lớp}/ai-grading/report}; app: lớp của giáo viên ở {@code
 * /api/v1/teacher/my-classes}.
 */
@RestController
@NoStore
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class KpiController {

  private final KpiService service;

  public KpiController(KpiService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/teacher/my-classes/{classId}/ai-grading/report")
  public Map<String, Object> report(@PathVariable UUID classId) {
    return service.report(classId);
  }
}
