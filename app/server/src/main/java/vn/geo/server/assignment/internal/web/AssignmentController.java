package vn.geo.server.assignment.internal.web;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.AssignmentService;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.web.NoStore;

/** Bài giao: phía học sinh (/student/...) và phía giáo viên (/teacher/...). Trường lấy từ thẻ. */
@RestController
public class AssignmentController {

  public record CreateBody(
      UUID assignmentId,
      UUID classId,
      UUID problemVersionId,
      OffsetDateTime releaseAt,
      Boolean allowHint,
      Boolean allowQa) {}

  public record ReleaseBody(Boolean openNow, OffsetDateTime releaseAt, Integer expectedRev) {}

  private final AssignmentService service;

  public AssignmentController(AssignmentService service) {
    this.service = service;
  }

  /** "Bài của em" (trạng thái khóa nên không lưu đệm). */
  @NoStore
  @GetMapping("/api/v1/student/assignments")
  public Page<AssignmentService.StudentAssignment> mine(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.myAssignments(limit, cursor);
  }

  @NoStore
  @GetMapping("/api/v1/student/assignments/{id}")
  public AssignmentService.StudentAssignment one(@PathVariable UUID id) {
    return service.myAssignment(id);
  }

  @NoStore
  @GetMapping("/api/v1/student/assignments/{id}/solution")
  public AssignmentService.SolutionView solution(@PathVariable UUID id) {
    return service.mySolution(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/classes/{classId}/assignments")
  public Page<AssignmentService.ClassAssignment> ofClass(
      @PathVariable UUID classId,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.classAssignments(classId, limit, cursor);
  }

  @PostMapping("/api/v1/teacher/assignments")
  @ResponseStatus(HttpStatus.CREATED)
  public AssignmentService.Created create(@RequestBody CreateBody b) {
    return service.create(
        b.assignmentId(),
        b.classId(),
        b.problemVersionId(),
        b.releaseAt(),
        b.allowHint(),
        b.allowQa());
  }

  @NoStore
  @PostMapping("/api/v1/teacher/assignments/{id}/release")
  public AssignmentService.Release release(@PathVariable UUID id, @RequestBody ReleaseBody b) {
    return service.setRelease(id, Boolean.TRUE.equals(b.openNow()), b.releaseAt(), b.expectedRev());
  }
}
