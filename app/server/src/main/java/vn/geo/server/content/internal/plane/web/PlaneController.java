package vn.geo.server.content.internal.plane.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.plane.service.PlaneStudentService;

/** S2-E2: màn làm bài hình phẳng của học sinh (US-S2E2-03). No-store (D-GEO-145). */
@RestController
@NoStore
public class PlaneController {

  private final PlaneStudentService plane;

  public PlaneController(PlaneStudentService plane) {
    this.plane = plane;
  }

  @GetMapping("/api/v1/student/assignments/{id}/plane")
  public PlaneStudentService.PlaneView view(@PathVariable UUID id) {
    return plane.view(id);
  }
}
