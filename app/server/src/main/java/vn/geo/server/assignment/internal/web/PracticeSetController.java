package vn.geo.server.assignment.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.PracticeSetService;
import vn.geo.server.common.web.NoStore;

/**
 * Giáo viên soạn, xem trước, giao và đóng bài luyện (US-S2E8-05, C2-08). Trường và người gọi lấy từ
 * thẻ. Dữ liệu trả về không có đáp số.
 */
@RestController
public class PracticeSetController {

  public record AssignBody(UUID classId, Boolean confirmPastRelease) {}

  public record PreviewBody(Integer position, String answer) {}

  private final PracticeSetService service;

  public PracticeSetController(PracticeSetService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice/problems")
  public Map<String, List<PracticeSetService.Candidate>> problems() {
    return Map.of("items", service.candidates());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice/classes")
  public Map<String, List<PracticeSetService.ClassOption>> classes() {
    return Map.of("items", service.classes());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice-sets")
  public Map<String, List<PracticeSetService.SetLine>> list() {
    return Map.of("items", service.list());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice-sets/{id}")
  public PracticeSetService.SetDetail one(@PathVariable UUID id) {
    return service.get(id);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/practice-sets")
  @ResponseStatus(HttpStatus.CREATED)
  public PracticeSetService.SetDetail create(@RequestBody PracticeSetService.SetInput body) {
    return service.create(body);
  }

  @NoStore
  @PutMapping("/api/v1/teacher/practice-sets/{id}")
  public PracticeSetService.SetDetail update(
      @PathVariable UUID id, @RequestBody PracticeSetService.SetInput body) {
    return service.update(id, body);
  }

  @DeleteMapping("/api/v1/teacher/practice-sets/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/practice-sets/{id}/assign")
  public PracticeSetService.SetDetail assign(@PathVariable UUID id, @RequestBody AssignBody body) {
    return service.assign(id, body.classId(), Boolean.TRUE.equals(body.confirmPastRelease()));
  }

  @NoStore
  @PostMapping("/api/v1/teacher/practice-sets/{id}/close")
  public PracticeSetService.SetDetail close(@PathVariable UUID id) {
    return service.close(id);
  }

  /** "Xem trước như học sinh": chấm thử, không ghi Attempt, không trừ lượt (BR-S2E8-28). */
  @NoStore
  @PostMapping("/api/v1/teacher/practice-sets/{id}/preview/check")
  public PracticeSetService.PreviewCheck previewCheck(
      @PathVariable UUID id, @RequestBody PreviewBody body) {
    return service.previewCheck(id, body.position(), body.answer());
  }
}
