package vn.geo.server.exam.internal.export.web;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.exam.internal.export.service.ExamExportService;

/**
 * Xuất mã đề (US-S5E2-05): {@code GET
 * /api/exams/{id}/codes/{code}/export?format=docx|pdf&parts=all|de,dap-an,huong-dan-cham,dac-ta} →
 * một tệp (một phần) hoặc tệp nén (nhiều phần), no-store. Học sinh FORBIDDEN, thân lỗi không có nội
 * dung đề hay đáp án.
 */
@RestController
public class ExamExportController {

  private final ExamExportService service;

  public ExamExportController(ExamExportService service) {
    this.service = service;
  }

  @GetMapping("/api/exams/{id}/codes/{code}/export")
  public ResponseEntity<byte[]> export(
      @PathVariable UUID id,
      @PathVariable String code,
      @RequestParam(required = false) String format,
      @RequestParam(required = false) String parts) {
    return service.export(id, code, format, parts).response();
  }
}
