package vn.geo.server.assessment.internal.submit.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.submit.service.SubmissionService;
import vn.geo.server.assessment.internal.submit.service.SubmissionService.AckRequest;
import vn.geo.server.assessment.internal.submit.service.SubmissionService.SubmitRequest;
import vn.geo.server.assessment.internal.submit.service.SubmissionService.UploadRequest;
import vn.geo.server.common.web.NoStore;

/**
 * Nộp bài viết tay (US-S5E4-03, 09; C5-03). Sau cờ {@code geo.s5.grading.enabled}: tắt thì 404. Ảnh
 * tải theo đoạn (thân yêu cầu ≤ 1 MB): POST …/uploads → PUT …/uploads/{id}?offset= → POST
 * /api/v1/submissions {assignmentId, idempotencyKey, pages[]}.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class SubmissionController {

  static final String BASE = "/api/v1/submissions";

  private final SubmissionService service;

  public SubmissionController(SubmissionService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping(BASE + "/privacy")
  public Map<String, Object> privacy() {
    return service.privacy();
  }

  @NoStore
  @PostMapping(BASE + "/privacy")
  public Map<String, Object> ack(@RequestBody(required = false) AckRequest body) {
    return service.ack(body);
  }

  @PostMapping(BASE + "/uploads")
  public ResponseEntity<Map<String, Object>> startUpload(
      @RequestBody(required = false) UploadRequest body) {
    return ResponseEntity.status(201).body(service.startUpload(body));
  }

  @PutMapping(path = BASE + "/uploads/{id}", consumes = "application/octet-stream")
  public Map<String, Object> part(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int offset,
      @RequestBody byte[] chunk) {
    return service.uploadPart(id, offset, chunk);
  }

  @NoStore
  @PostMapping(BASE)
  public ResponseEntity<Map<String, Object>> submit(
      @RequestBody(required = false) SubmitRequest body) {
    SubmissionService.Result r = service.submit(body);
    return ResponseEntity.status(r.created() ? 201 : 200).body(r.body());
  }

  @NoStore
  @GetMapping(BASE + "/mine")
  public Map<String, Object> mine(@RequestParam UUID assignmentId) {
    return service.mine(assignmentId);
  }

  @NoStore
  @GetMapping(BASE + "/{id}")
  public Map<String, Object> one(@PathVariable UUID id) {
    return service.get(id);
  }
}
