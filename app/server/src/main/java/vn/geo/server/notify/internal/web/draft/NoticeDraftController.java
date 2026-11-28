package vn.geo.server.notify.internal.web.draft;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.notify.internal.service.draft.NoticeDraftService;

/**
 * Màn "Trợ lý · Thông báo" (US-S5E5-01) và báo cáo dùng trợ lý (US-S5E5-04-SC6). Đặc tả ghi {@code
 * /api/notices}, {@code /api/copilot/usage}; theo quy ước đường dẫn của app là {@code
 * /api/v1/teacher/…}. Giáo viên khác NOT_FOUND, học sinh FORBIDDEN.
 */
@RestController
@NoStore
public class NoticeDraftController {

  private static final String BASE = "/api/v1/teacher/notices";

  private final NoticeDraftService service;

  public NoticeDraftController(NoticeDraftService service) {
    this.service = service;
  }

  @GetMapping(BASE + "/sources")
  public Map<String, Object> sources(@RequestParam UUID classId) {
    return service.sources(classId);
  }

  @PostMapping(BASE)
  public ResponseEntity<Map<String, Object>> create(
      @RequestBody(required = false) NoticeDraftService.CreateRequest body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
  }

  @GetMapping(BASE + "/{id}")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PutMapping(BASE + "/{id}")
  public Map<String, Object> edit(
      @PathVariable UUID id, @RequestBody(required = false) NoticeDraftService.BodyRequest body) {
    return service.edit(id, body);
  }

  @PostMapping(BASE + "/{id}/send")
  public Map<String, Object> send(
      @PathVariable UUID id, @RequestBody(required = false) NoticeDraftService.BodyRequest body) {
    return service.send(id, body);
  }

  @GetMapping("/api/v1/teacher/copilot/usage")
  public Map<String, Object> usage(@RequestParam(defaultValue = "30") int days) {
    return service.usage(days);
  }
}
