package vn.geo.server.aigateway.internal.web.killswitch;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigateway.internal.InstrumentedAiGateway;
import vn.geo.server.aigateway.internal.service.killswitch.KillSwitchService;
import vn.geo.server.common.web.NoStore;

/**
 * Tắt khẩn AI toàn hệ thống cho người vận hành (US-S5E5-04-SC5, BR-S5E5-16). Đặc tả ghi {@code PUT
 * /api/admin/ai/kill}; theo quy ước đường dẫn của app là {@code /api/v1/admin/ai/kill}.
 */
@RestController
@NoStore
public class KillSwitchController {

  private final KillSwitchService service;

  public KillSwitchController(KillSwitchService service) {
    this.service = service;
  }

  /** Thân yêu cầu: {"task":"kb_qa","off":true}. */
  public record KillRequest(String task, Boolean off) {}

  @GetMapping("/api/v1/admin/ai/kill")
  public Map<String, Object> list(
      @RequestHeader(value = InstrumentedAiGateway.LANE_HEADER, required = false) String lane) {
    return service.list(lane);
  }

  @PutMapping("/api/v1/admin/ai/kill")
  public Map<String, Object> set(
      @RequestBody(required = false) KillRequest body,
      @RequestHeader(value = InstrumentedAiGateway.LANE_HEADER, required = false) String lane) {
    return service.set(body == null ? null : body.task(), body == null ? null : body.off(), lane);
  }
}
