package vn.geo.server.tracking.internal.web;

import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.service.ClassMonitorService;

/** E7: mục "Theo dõi" (bài đã giao của giáo viên) và đọc sự kiện theo con trỏ (US-GEO-704). */
@RestController
public class ClassMonitorController {

  private final ClassMonitorService service;

  public ClassMonitorController(ClassMonitorService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/assigned")
  public Page<ClassMonitorService.Assigned> assigned(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.assigned(limit, cursor);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/assignments/{id}/students/{userId}/events")
  public Page<ClassMonitorService.Event> events(
      @PathVariable UUID id,
      @PathVariable UUID userId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.events(id, userId, from, to, limit, cursor);
  }
}
