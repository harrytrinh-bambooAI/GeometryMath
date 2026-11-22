package vn.geo.server.content.internal.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.service.CatalogService;

/** Kho đề và lời giải đề kho dành cho giáo viên. */
@RestController
public class CatalogController {

  private final CatalogService service;

  public CatalogController(CatalogService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/catalog/problems")
  public Page<CatalogService.Problem> list(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.list(limit, cursor);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/problems/{versionId}/solution")
  public CatalogService.TeacherSolution solution(@PathVariable UUID versionId) {
    return service.teacherSolution(versionId);
  }
}
