package vn.geo.server.lessonplan.internal.list.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.lessonplan.internal.list.service.LessonListService;

/**
 * "Giáo án của tôi" và duyệt (US-S5E1-05). {@code GET
 * /api/v1/teacher/lesson-plans?shelf=approved|draft}, {@code POST …/{id|GA-nnnn}/approve}.
 * No-store.
 */
@RestController
@NoStore
public class LessonListController {

  private static final String BASE = "/api/v1/teacher/lesson-plans";

  private final LessonListService service;

  public LessonListController(LessonListService service) {
    this.service = service;
  }

  @GetMapping(BASE)
  public Map<String, Object> list(@RequestParam(required = false) String shelf) {
    return service.list(shelf);
  }

  @PostMapping(BASE + "/{id}/approve")
  public Map<String, Object> approve(@PathVariable String id) {
    return service.approve(id);
  }
}
