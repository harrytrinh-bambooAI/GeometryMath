package vn.geo.server.content.internal.variant.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.variant.service.VariantService;

/** Giáo viên tạo và xem biến thể đề (US-S2E9-01). Trường và người gọi lấy từ thẻ. */
@RestController
public class VariantController {

  public record CreateBody(UUID docId, Integer count, Long seed) {}

  private final VariantService service;

  public VariantController(VariantService service) {
    this.service = service;
  }

  @NoStore
  @PostMapping("/api/v1/teacher/variants")
  @ResponseStatus(HttpStatus.CREATED)
  public VariantService.Batch create(@RequestBody CreateBody body) {
    return service.create(body.docId(), body.count(), body.seed());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/variants")
  public Map<String, List<VariantService.Item>> list(@RequestParam UUID sourceId) {
    return Map.of("items", service.list(sourceId));
  }

  @NoStore
  @GetMapping("/api/v1/teacher/variants/by-doc/{docId}")
  public VariantService.Item ofDoc(@PathVariable UUID docId) {
    return service.ofDoc(docId);
  }
}
