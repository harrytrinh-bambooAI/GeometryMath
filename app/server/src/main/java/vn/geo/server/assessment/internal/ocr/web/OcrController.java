package vn.geo.server.assessment.internal.ocr.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.internal.ocr.service.OcrService;
import vn.geo.server.assessment.internal.ocr.service.OcrService.EditRequest;
import vn.geo.server.common.web.NoStore;

/**
 * Bản đọc chữ của bài nộp (US-S5E4-04): giáo viên chủ lớp xem, sửa (phiên bản mới), Đọc lại khi
 * lỗi; học sinh FORBIDDEN. Sau cờ {@code geo.s5.grading.enabled}.
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class OcrController {

  private final OcrService service;

  public OcrController(OcrService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{id}/ocr")
  public Map<String, Object> get(
      @PathVariable UUID id, @RequestParam(required = false) Integer version) {
    return service.get(id, version);
  }

  @NoStore
  @PutMapping("/api/v1/submissions/{id}/ocr")
  public Map<String, Object> edit(
      @PathVariable UUID id, @RequestBody(required = false) EditRequest body) {
    return service.edit(id, body);
  }

  @NoStore
  @PostMapping("/api/v1/submissions/{id}/ocr/retry")
  public Map<String, Object> retry(@PathVariable UUID id) {
    return service.retry(id);
  }
}
