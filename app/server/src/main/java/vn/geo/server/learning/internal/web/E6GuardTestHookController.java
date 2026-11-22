package vn.geo.server.learning.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.learning.internal.service.GuardFaults;

/**
 * MÓC KIỂM THỬ E6, chỉ profile dev/test: đặt Kiểm soát đáp án ở chế độ lỗi cho một người gọi (kịch
 * bản F-GEO-117, US-GEO-609-FC6 — đóng khi lỗi).
 */
@RestController
@Profile({"dev", "test"})
public class E6GuardTestHookController {

  public record GuardHook(UUID userId, Boolean fail, Boolean bypass) {}

  private final GuardFaults faults;

  public E6GuardTestHookController(GuardFaults faults) {
    this.faults = faults;
  }

  @PostMapping("/test-hooks/e6/guard")
  public Map<String, Object> set(@RequestBody GuardHook h) {
    if (h.userId() == null) {
      faults.reset();
    } else {
      if (h.fail() != null) {
        faults.set(h.userId(), h.fail());
      }
      if (h.bypass() != null) {
        faults.bypass(h.userId(), h.bypass());
      }
    }
    return Map.of("ok", true);
  }
}
