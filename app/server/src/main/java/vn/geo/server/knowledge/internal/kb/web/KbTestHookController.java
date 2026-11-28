package vn.geo.server.knowledge.internal.kb.web;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.knowledge.internal.kb.service.KbTestHookService;
import vn.geo.server.knowledge.internal.kb.service.KbTestHookService.Bulk;
import vn.geo.server.knowledge.internal.kb.service.KbTestHookService.Retrieve;

/**
 * MÓC KIỂM THỬ S5-E3 (T-261128-56), chỉ profile dev/test (không có ở prod): {@code GET
 * /test-hooks/s5e3/sample-docx?kb=}, {@code POST /test-hooks/s5e3/retrieve}, {@code POST
 * /test-hooks/s5e3/bulk} (xem {@link KbTestHookService}).
 */
@RestController
@Profile({"dev", "test"})
public class KbTestHookController {

  private final KbTestHookService hooks;

  public KbTestHookController(KbTestHookService hooks) {
    this.hooks = hooks;
  }

  @GetMapping("/test-hooks/s5e3/sample-docx")
  public ResponseEntity<byte[]> sampleDocx(@RequestParam(defaultValue = "240") int kb) {
    return ResponseEntity.ok()
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        .body(hooks.sampleDocx(kb));
  }

  @PostMapping("/test-hooks/s5e3/retrieve")
  public Map<String, Object> retrieve(@RequestBody Retrieve r) {
    return hooks.retrieve(r);
  }

  @PostMapping("/test-hooks/s5e3/bulk")
  public Map<String, Object> bulk(@RequestBody Bulk b) {
    return hooks.bulk(b);
  }
}
