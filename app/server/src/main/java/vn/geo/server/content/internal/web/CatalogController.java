package vn.geo.server.content.internal.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.service.CatalogService;

/** Kho đề và lời giải đề kho dành cho giáo viên (E1 tối thiểu, E5 đầy đủ: US-GEO-501…505). */
@RestController
public class CatalogController {

  private final CatalogService service;

  public CatalogController(CatalogService service) {
    this.service = service;
  }

  /** Danh sách Kho đề: lọc cấp (thcs|thpt), hình (pyramid…), dạng bài (angle…); trang theo khóa. */
  @GetMapping("/api/v1/catalog/problems")
  public CatalogService.CatalogPage list(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String solid,
      @RequestParam(required = false) String topic) {
    return service.list(limit, cursor, level, solid, topic);
  }

  /** Dòng đầu trang "{n} đề · {m} đã kiểm chứng · tài liệu của Giáo viên mẫu" (BR-GEO-203). */
  @GetMapping("/api/v1/catalog/summary")
  public CatalogService.Summary summary() {
    return service.summary();
  }

  /** Trang chi tiết đề của giáo viên: có lời giải, đáp số (no-store, D-GEO-145). */
  @NoStore
  @GetMapping("/api/v1/teacher/catalog/problems/{versionId}")
  public CatalogService.ProblemDetail detail(@PathVariable UUID versionId) {
    return service.detail(versionId);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/problems/{versionId}/solution")
  public CatalogService.TeacherSolution solution(@PathVariable UUID versionId) {
    return service.teacherSolution(versionId);
  }
}
