package vn.geo.server.lessonplan.internal.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.lessonplan.internal.service.LessonForm;
import vn.geo.server.lessonplan.internal.service.LessonPlanService;

/**
 * S5-E1 màn C5-08 "Soạn giáo án mới" và màn kết quả (US-S5E1-01…03). Đặc tả ghi {@code
 * /api/lesson-plans}; theo quy ước đường dẫn của app là {@code /api/v1/teacher/lesson-plans}. Giáo
 * viên khác NOT_FOUND, học sinh FORBIDDEN. No-store (D-GEO-145).
 *
 * <ul>
 *   <li>{@code GET …/catalog} Chương → Bài → Tiết PPCT, kiến thức của bài, tích sẵn theo tiết.
 *   <li>{@code POST …} gửi form B/C → 202 {draftId, code, status "drafting" (Đang soạn)}.
 *   <li>{@code GET …/{id|GA-nnnn}} giáo án dựng theo kind.
 *   <li>{@code POST …/{id|GA-nnnn}/retry} Thử lại (một lượt mới).
 * </ul>
 */
@RestController
@NoStore
public class LessonPlanController {

  private static final String BASE = "/api/v1/teacher/lesson-plans";

  private final LessonPlanService service;

  public LessonPlanController(LessonPlanService service) {
    this.service = service;
  }

  @GetMapping(BASE + "/catalog")
  public Map<String, Object> catalog() {
    return service.catalog();
  }

  @PostMapping(BASE)
  public ResponseEntity<Map<String, Object>> create(
      @RequestBody(required = false) LessonForm body) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.create(body));
  }

  @GetMapping(BASE + "/{id}")
  public Map<String, Object> view(@PathVariable String id) {
    return service.view(id);
  }

  @PostMapping(BASE + "/{id}/retry")
  public ResponseEntity<Map<String, Object>> retry(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.retry(id));
  }
}
