package vn.geo.server.lessonplan.internal.export.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.lessonplan.internal.export.service.LessonExportService;

/**
 * Xuất giáo án (US-S5E1-05-SC2, SC3): {@code GET
 * /api/v1/teacher/lesson-plans/{id|GA-nnnn}/export?format=docx|pdf[&version=n]} → tệp tải về
 * (no-store). Giáo viên khác NOT_FOUND, học sinh FORBIDDEN.
 */
@RestController
public class LessonExportController {

  private final LessonExportService service;

  public LessonExportController(LessonExportService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/teacher/lesson-plans/{id}/export")
  public ResponseEntity<byte[]> export(
      @PathVariable String id,
      @RequestParam(required = false) String format,
      @RequestParam(required = false) Integer version) {
    return service.export(id, format, version).response();
  }
}
