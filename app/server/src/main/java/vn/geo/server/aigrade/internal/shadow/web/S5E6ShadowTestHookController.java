package vn.geo.server.aigrade.internal.shadow.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.shadow.service.ShadowService;

/**
 * MÓC KIỂM THỬ S5-E6 (chỉ profile dev/test; T-261128-72): giả lập {@code recordFirstApproval} lỗi
 * rồi chạy job ghi bù cặp AI/GV (REVIEW-S5 mục 7).
 */
@RestController
@Profile({"dev", "test"})
public class S5E6ShadowTestHookController {

  public record FaultBody(int times) {}

  public record BackfillBody(UUID orgId, UUID submissionId) {}

  private final ShadowService shadow;

  public S5E6ShadowTestHookController(ShadowService shadow) {
    this.shadow = shadow;
  }

  /** {@code recordFirstApproval} ném lỗi {@code times} lần kế tiếp. */
  @PostMapping("/test-hooks/s5e6/shadow-fault")
  public Map<String, Object> fault(@RequestBody FaultBody b) {
    shadow.failNextFirstApproval(b.times());
    return Map.of("times", b.times());
  }

  /** Chạy ghi bù cho một bài (như một lượt job); trả số cặp ghi thêm và tổng đã ghi bù. */
  @PostMapping("/test-hooks/s5e6/shadow-backfill")
  public Map<String, Object> backfill(@RequestBody BackfillBody b) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("added", shadow.backfillPairs(b.orgId(), b.submissionId()));
    out.put("total", shadow.backfilledCount());
    return out;
  }
}
