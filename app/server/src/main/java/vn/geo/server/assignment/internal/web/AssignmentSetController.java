package vn.geo.server.assignment.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.AssignmentSetService;
import vn.geo.server.common.web.NoStore;

/**
 * S3-E2: bộ bài chặng 3 (C3-03 soạn, lịch mở lời giải, đóng / lưu trữ; C3-08 Bài của em theo bộ).
 * Trường và người gọi lấy từ thẻ; mọi phản hồi no-store (lời giải, trạng thái khóa, D-GEO-145).
 */
@NoStore
@RestController
public class AssignmentSetController {

  public record AddItemBody(UUID problemVersionId) {}

  public record AssignBody(String requestKey, Boolean confirmPastRelease) {}

  public record ScheduleBody(String releaseAt, Boolean set, Integer version) {}

  public record KeyBody(String requestKey) {}

  private final AssignmentSetService service;

  public AssignmentSetController(AssignmentSetService service) {
    this.service = service;
  }

  // ------------------------------------------------------------------ giáo viên

  @GetMapping("/api/v1/teacher/assignment-sets/options")
  public AssignmentSetService.Options options() {
    return service.options();
  }

  /** view = active (Nháp, Đã giao, Đã đóng) | archived ("Bộ đã lưu trữ"). */
  @GetMapping("/api/v1/teacher/assignment-sets")
  public Map<String, List<AssignmentSetService.SetLine>> list(
      @RequestParam(defaultValue = "active") String view) {
    return Map.of("items", service.list("archived".equals(view)));
  }

  @GetMapping("/api/v1/teacher/assignment-sets/{id}")
  public AssignmentSetService.SetDetail one(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping("/api/v1/teacher/assignment-sets")
  @ResponseStatus(HttpStatus.CREATED)
  public AssignmentSetService.SetDetail create(@RequestBody AssignmentSetService.SetInput body) {
    return service.create(body);
  }

  @PutMapping("/api/v1/teacher/assignment-sets/{id}")
  public AssignmentSetService.SetDetail update(
      @PathVariable UUID id, @RequestBody AssignmentSetService.SetInput body) {
    return service.update(id, body);
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/items")
  public AssignmentSetService.SetDetail addItem(
      @PathVariable UUID id, @RequestBody AddItemBody body) {
    return service.addItem(id, body.problemVersionId());
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/assign")
  public AssignmentSetService.SetDetail assign(
      @PathVariable UUID id, @RequestBody(required = false) AssignBody body) {
    return service.assign(
        id,
        body == null ? null : body.requestKey(),
        body != null && Boolean.TRUE.equals(body.confirmPastRelease()));
  }

  @PutMapping("/api/v1/teacher/assignment-sets/{id}/schedule")
  public AssignmentSetService.SetDetail schedule(
      @PathVariable UUID id, @RequestBody ScheduleBody body) {
    return service.schedule(id, body.releaseAt(), body.set(), body.version());
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/close")
  public AssignmentSetService.SetDetail close(
      @PathVariable UUID id, @RequestBody(required = false) KeyBody body) {
    return service.close(id);
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/archive")
  public AssignmentSetService.SetDetail archive(
      @PathVariable UUID id, @RequestBody(required = false) KeyBody body) {
    return service.archive(id);
  }

  @GetMapping("/api/v1/teacher/assignment-sets/{id}/preview")
  public AssignmentSetService.Preview preview(@PathVariable UUID id) {
    return service.preview(id);
  }

  // ------------------------------------------------------------------ học sinh

  @GetMapping("/api/v1/student/assignment-sets")
  public AssignmentSetService.StudentHome mine() {
    return service.studentHome();
  }

  @GetMapping("/api/v1/student/assignment-sets/{id}")
  public AssignmentSetService.StudentSetDetail mineOne(@PathVariable UUID id) {
    return service.studentSet(id);
  }

  @GetMapping("/api/v1/student/assignment-sets/{id}/items/{position}/solution")
  public AssignmentSetService.SolutionView solution(
      @PathVariable UUID id, @PathVariable int position) {
    return service.studentSolution(id, position);
  }
}
