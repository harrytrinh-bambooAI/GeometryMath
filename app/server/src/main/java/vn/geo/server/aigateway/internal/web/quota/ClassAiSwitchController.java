package vn.geo.server.aigateway.internal.web.quota;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigateway.internal.service.quota.ClassAiSwitchService;
import vn.geo.server.common.web.NoStore;

/**
 * Công tắc AI và báo cáo dùng AI của lớp (US-S5E5-04). Đặc tả ghi {@code PUT
 * /api/classes/{lớp}/switches}, {@code GET /api/classes/{lớp}/ai-usage}; theo quy ước đường dẫn của
 * app (lớp của giáo viên ở {@code /api/v1/teacher/my-classes}).
 */
@RestController
@NoStore
public class ClassAiSwitchController {

  private static final String BASE = "/api/v1/teacher/my-classes/{classId}";

  private final ClassAiSwitchService service;

  public ClassAiSwitchController(ClassAiSwitchService service) {
    this.service = service;
  }

  @GetMapping(BASE + "/ai-switches")
  public Map<String, Object> get(@PathVariable UUID classId) {
    return service.get(classId);
  }

  @PutMapping(BASE + "/ai-switches")
  public Map<String, Object> put(
      @PathVariable UUID classId,
      @RequestBody(required = false) ClassAiSwitchService.SwitchRequest body) {
    return service.put(classId, body);
  }

  @GetMapping(BASE + "/ai-usage")
  public Map<String, Object> usage(
      @PathVariable UUID classId, @RequestParam(defaultValue = "30") int days) {
    return service.usage(classId, days);
  }
}
