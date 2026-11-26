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
import vn.geo.server.assignment.internal.service.ChoiceService;
import vn.geo.server.common.web.NoStore;

/**
 * Câu trắc nghiệm trong bài luyện (US-S2E9-03): giáo viên soạn lựa chọn cho một câu của bộ Nháp;
 * học sinh nhận lựa chọn (không có cờ đúng) và "Kiểm tra". Trường và người gọi lấy từ thẻ.
 */
@RestController
public class ChoiceController {

  /** Chỉ hai trường này được dùng; trường khác thiết bị gửi bị bỏ qua. */
  public record AttemptBody(UUID choiceId, String requestKey) {}

  private final ChoiceService service;

  public ChoiceController(ChoiceService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/practice-sets/{id}/choices")
  public Map<String, List<ChoiceService.TeacherChoice>> ofSet(@PathVariable UUID id) {
    return Map.of("items", service.ofSet(id));
  }

  @NoStore
  @PutMapping("/api/v1/teacher/practice-sets/{id}/items/{position}/choice")
  public ChoiceService.TeacherChoice save(
      @PathVariable UUID id,
      @PathVariable int position,
      @RequestBody ChoiceService.ChoiceInput body) {
    return service.save(id, position, body);
  }

  @DeleteMapping("/api/v1/teacher/practice-sets/{id}/items/{position}/choice")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable UUID id, @PathVariable int position) {
    service.remove(id, position);
  }

  @NoStore
  @GetMapping("/api/v1/student/practice-sets/{id}/choices")
  public Map<String, List<ChoiceService.StudentChoice>> forStudent(@PathVariable UUID id) {
    return Map.of("items", service.forStudent(id));
  }

  @NoStore
  @PostMapping("/api/v1/student/practice-items/{assignmentId}/choice-attempts")
  public ChoiceService.AttemptView attempt(
      @PathVariable UUID assignmentId, @RequestBody AttemptBody body) {
    return service.submit(assignmentId, body.choiceId(), body.requestKey());
  }
}
