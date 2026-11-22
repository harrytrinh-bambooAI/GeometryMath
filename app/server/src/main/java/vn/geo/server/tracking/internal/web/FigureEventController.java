package vn.geo.server.tracking.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.service.FigureEventService;

/** Sự kiện của trang Làm bài (E3): thao tác trên hình, mở bài, gợi ý. Trường lấy từ thẻ. */
@RestController
public class FigureEventController {

  public record EventsBody(UUID batchId, List<FigureEventService.ToolEvent> events) {}

  public record RequestIdBody(UUID requestId) {}

  private final FigureEventService service;

  public FigureEventController(FigureEventService service) {
    this.service = service;
  }

  @PostMapping("/api/v1/student/assignments/{id}/figure-events")
  public FigureEventService.Recorded events(@PathVariable UUID id, @RequestBody EventsBody b) {
    return service.recordTools(id, b.batchId(), b.events());
  }

  @PostMapping("/api/v1/student/assignments/{id}/open")
  public Map<String, String> open(@PathVariable UUID id, @RequestBody RequestIdBody b) {
    return service.recordOpen(id, b.requestId());
  }

  @NoStore
  @PostMapping("/api/v1/student/assignments/{id}/hints/{level}")
  public FigureEventService.Hint hint(
      @PathVariable UUID id, @PathVariable int level, @RequestBody RequestIdBody b) {
    return service.hint(id, level, b.requestId());
  }
}
