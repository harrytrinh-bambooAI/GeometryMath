package vn.geo.server.aigateway.internal.web;

import java.time.Duration;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigateway.internal.AiProvider.Simulation;
import vn.geo.server.aigateway.internal.AiSettings;
import vn.geo.server.aigateway.internal.MockAiControl;

/**
 * MÓC KIỂM THỬ, chỉ profile dev/test (không có ở prod): đặt chế độ của nhà cung cấp giả lập (lỗi,
 * quá thời gian) theo người dùng và làn kiểm thử, đổi model/thời gian chờ đang hiệu lực, đặt lại.
 */
@RestController
@Profile({"dev", "test"})
public class AiTestHookController {

  private final MockAiControl control;
  private final AiSettings settings;

  public AiTestHookController(MockAiControl control, AiSettings settings) {
    this.control = control;
    this.settings = settings;
  }

  /**
   * @param mode ok | error | timeout (null = không đổi)
   * @param userId áp cho lượt gọi của người dùng này (null + lane null = mọi lượt)
   * @param lane làn kiểm thử (tiêu đề X-Geo-Test-Lane)
   * @param model đổi model đang hiệu lực
   * @param timeoutMs đổi thời gian chờ đang hiệu lực
   */
  public record AiHook(String mode, String userId, String lane, String model, Long timeoutMs) {}

  @PostMapping("/test-hooks/ai")
  public Map<String, Object> set(@RequestBody AiHook h) {
    if (h.mode() != null) {
      control.set(h.userId(), h.lane(), Simulation.valueOf(h.mode().toUpperCase()));
    }
    if (h.model() != null) {
      settings.model(h.model());
    }
    if (h.timeoutMs() != null) {
      settings.timeout(Duration.ofMillis(h.timeoutMs()));
    }
    return Map.of("model", settings.model(), "timeoutMs", settings.timeout().toMillis());
  }

  @PostMapping("/test-hooks/ai/reset")
  public Map<String, Object> reset() {
    control.reset();
    settings.reset();
    return Map.of("model", settings.model());
  }
}
