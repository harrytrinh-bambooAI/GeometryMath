package vn.geo.server.assessment.internal.assign.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.assign.service.GradedAssignmentService;
import vn.geo.server.assessment.internal.assign.service.GradedAssignmentService.AssignInput;
import vn.geo.server.assessment.internal.assign.service.GradedAssignmentService.AssignPatch;
import vn.geo.server.assessment.internal.assign.service.GradingConflict;
import vn.geo.server.common.error.ApiError;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.common.web.RequestIdFilter;

/**
 * Bài có chấm (US-S5E4-02, C5-01). Sau cờ {@code geo.s5.grading.enabled}: tắt thì không có đường
 * nào (404). Giáo viên giao/sửa; học sinh xem "Bài cần nộp".
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class GradedAssignmentController {

  private final GradedAssignmentService service;

  public GradedAssignmentController(GradedAssignmentService service) {
    this.service = service;
  }

  @NoStore
  @PostMapping("/api/v1/teacher/graded-assignments")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody(required = false) AssignInput body) {
    return service.create(body);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments")
  public Map<String, Object> list(@RequestParam(required = false) UUID classId) {
    return service.list(classId);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/graded-assignments/{id}")
  public Map<String, Object> one(@PathVariable UUID id) {
    return service.get(id);
  }

  @NoStore
  @PatchMapping("/api/v1/teacher/graded-assignments/{id}")
  public Map<String, Object> patch(
      @PathVariable UUID id, @RequestBody(required = false) AssignPatch body) {
    return service.patch(id, body);
  }

  @NoStore
  @GetMapping("/api/v1/student/graded-assignments")
  public Map<String, Object> mine() {
    return service.studentList();
  }

  /** CONFLICT kèm details gắn trường (US-S5E4-02-FC1). */
  @ExceptionHandler(GradingConflict.class)
  ResponseEntity<ApiError> conflict(GradingConflict ex) {
    ApiError body =
        new ApiError(
            ErrorCode.CONFLICT.name(),
            ex.getMessage(),
            MDC.get(RequestIdFilter.MDC_KEY),
            List.of(new ApiError.FieldIssue(ex.field(), ex.detail())));
    return ResponseEntity.status(ErrorCode.CONFLICT.status()).body(body);
  }
}
