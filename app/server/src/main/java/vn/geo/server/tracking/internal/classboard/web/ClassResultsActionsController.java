package vn.geo.server.tracking.internal.classboard.web;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.classboard.service.ClassExportService;
import vn.geo.server.tracking.internal.classboard.service.PresentFromResultsService;

/**
 * S3-E6 — thao tác từ Kết quả lớp của giáo viên chủ lớp: xuất CSV (US-S3E6-02) và "Chữa bài này"
 * sang Trình chiếu (US-S3E6-01). Mọi phản hồi {@code Cache-Control: no-store}.
 */
@RestController
@RequestMapping("/api/v1/teacher/class-results")
public class ClassResultsActionsController {

  /** Bước em dừng nhiều nhất của thẻ (không bắt buộc). */
  public record PresentBody(Integer step) {}

  private final ClassExportService export;
  private final PresentFromResultsService present;

  public ClassResultsActionsController(
      ClassExportService export, PresentFromResultsService present) {
    this.export = export;
    this.present = present;
  }

  @NoStore
  @GetMapping("/{classId}/sets/{setId}/export")
  public ResponseEntity<byte[]> export(@PathVariable UUID classId, @PathVariable UUID setId) {
    ClassExportService.Csv csv = export.export(classId, setId);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(csv.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv.body());
  }

  @NoStore
  @GetMapping("/{classId}/sets/{setId}/items/{position}/present/preflight")
  public PresentFromResultsService.Preflight preflight(
      @PathVariable UUID classId, @PathVariable UUID setId, @PathVariable int position) {
    return present.preflight(classId, setId, position);
  }

  @NoStore
  @PostMapping("/{classId}/sets/{setId}/items/{position}/present")
  public PresentFromResultsService.Presentation present(
      @PathVariable UUID classId,
      @PathVariable UUID setId,
      @PathVariable int position,
      @RequestBody(required = false) PresentBody b) {
    return present.open(classId, setId, position, b == null ? null : b.step());
  }
}
