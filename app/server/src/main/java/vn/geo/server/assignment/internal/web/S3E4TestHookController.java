package vn.geo.server.assignment.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.S3E4HookService;

/**
 * MÓC KIỂM THỬ S3-E4 / US-S3E5-01, chỉ profile dev/test (không có ở prod; PO-51, D-GEO-169): đọc
 * bản ghi Từ chối (BR-S3E5-07) của một người gọi — sản phẩm không có màn hay API nào trả các bản
 * ghi này.
 */
@RestController
@Profile({"dev", "test"})
public class S3E4TestHookController {

  private final S3E4HookService hooks;

  public S3E4TestHookController(S3E4HookService hooks) {
    this.hooks = hooks;
  }

  @GetMapping("/test-hooks/s3e4/denied")
  public Map<String, List<Map<String, Object>>> denied(@RequestParam UUID actorId) {
    return Map.of("items", hooks.denied(actorId));
  }
}
