package vn.geo.server.exam.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.exam.internal.service.ExamFormService;
import vn.geo.server.exam.internal.service.ExamFormService.FormInput;
import vn.geo.server.exam.internal.service.ExamSetService;

/**
 * Ra đề theo ma trận (S5-E2, C5-08): danh mục loại đề, xem trước form, lắp bộ đề nhiều mã, xem,
 * thay câu, duyệt. Chỉ giáo viên; trường và người gọi lấy từ thẻ. Phản hồi có đáp án nên no-store.
 */
@RestController
public class ExamController {

  public record ReplaceBody(String code, Integer so) {}

  private final ExamFormService form;
  private final ExamSetService sets;

  public ExamController(ExamFormService form, ExamSetService sets) {
    this.form = form;
    this.sets = sets;
  }

  @NoStore
  @GetMapping("/api/exam/catalog")
  public ObjectNode catalog() {
    return form.catalog();
  }

  @NoStore
  @PostMapping("/api/exams/preview")
  public ObjectNode preview(@RequestBody FormInput body) {
    return form.preview(body);
  }

  @NoStore
  @PostMapping("/api/exams")
  @ResponseStatus(HttpStatus.CREATED)
  public ObjectNode create(@RequestBody FormInput body) {
    return sets.create(body);
  }

  @NoStore
  @GetMapping("/api/exams")
  public Map<String, List<ObjectNode>> list(@RequestParam(required = false) String shelf) {
    return Map.of("items", sets.list(shelf));
  }

  @NoStore
  @GetMapping("/api/exams/{id}")
  public ObjectNode one(@PathVariable UUID id) {
    return sets.get(id);
  }

  @NoStore
  @PostMapping("/api/exams/{id}/replace")
  public ObjectNode replace(@PathVariable UUID id, @RequestBody ReplaceBody body) {
    return sets.replace(id, body.code(), body.so() == null ? 0 : body.so());
  }

  @NoStore
  @PostMapping("/api/exams/{id}/approve")
  public ObjectNode approve(@PathVariable UUID id) {
    return sets.approve(id);
  }
}
