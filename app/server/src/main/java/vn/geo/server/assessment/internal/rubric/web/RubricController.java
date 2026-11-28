package vn.geo.server.assessment.internal.rubric.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.rubric.service.RubricService;
import vn.geo.server.assessment.internal.rubric.service.RubricService.RubricInput;
import vn.geo.server.common.web.NoStore;

/**
 * Rubric có phiên bản (US-S5E4-01, C5-02). Sau cờ {@code geo.s5.grading.enabled}: tắt thì không có
 * đường nào (404). Chỉ giáo viên; hướng dẫn chấm là dữ liệu chấm nên no-store.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class RubricController {

  private final RubricService rubrics;

  public RubricController(RubricService rubrics) {
    this.rubrics = rubrics;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/rubric-templates")
  public Map<String, Object> templates() {
    return rubrics.templates();
  }

  @NoStore
  @GetMapping("/api/v1/teacher/rubrics")
  public Map<String, Object> list() {
    return rubrics.list();
  }

  @NoStore
  @PostMapping("/api/v1/teacher/rubrics")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody(required = false) RubricInput body) {
    return rubrics.create(body);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/rubrics/{id}")
  public Map<String, Object> one(
      @PathVariable UUID id, @RequestParam(required = false) Integer version) {
    return rubrics.get(id, version);
  }

  @NoStore
  @PutMapping("/api/v1/teacher/rubrics/{id}")
  public Map<String, Object> update(
      @PathVariable UUID id, @RequestBody(required = false) RubricInput body) {
    return rubrics.update(id, body);
  }
}
