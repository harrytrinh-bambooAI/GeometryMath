package vn.geo.server.content.internal.library.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.library.service.LibraryViews.Preflight;
import vn.geo.server.content.internal.library.service.LibraryViews.Presentation;
import vn.geo.server.content.internal.library.service.PresentService;

/** S2-E7 — Trình chiếu (C2-04). no-store: dữ liệu chiếu có mọi bước lời giải (BR-S2E7-10). */
@RestController
@NoStore
public class PresentController {

  private final PresentService service;

  public PresentController(PresentService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/teacher/present/{docId}/preflight")
  public Preflight preflight(@PathVariable UUID docId) {
    return service.preflight(docId);
  }

  @GetMapping("/api/v1/teacher/present/{docId}")
  public Presentation load(@PathVariable UUID docId) {
    return service.load(docId);
  }
}
