package vn.geo.server.assignment.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.HelpLevelService;
import vn.geo.server.assignment.internal.service.HelpLevelService.HelpLevelRequest;
import vn.geo.server.common.web.NoStore;

/**
 * Mức trợ giúp AI của bài (US-S5E3-04): {@code GET|PUT /api/v1/teacher/assignments/{id}/help-level}
 * — id là bài luyện của lớp mình hoặc bài có chấm của mình; bài khác: NOT_FOUND.
 */
@RestController
@NoStore
public class HelpLevelController {

  private final HelpLevelService service;

  public HelpLevelController(HelpLevelService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/teacher/assignments/{id}/help-level")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PutMapping("/api/v1/teacher/assignments/{id}/help-level")
  public Map<String, Object> put(@PathVariable UUID id, @RequestBody HelpLevelRequest body) {
    return service.put(id, body);
  }
}
