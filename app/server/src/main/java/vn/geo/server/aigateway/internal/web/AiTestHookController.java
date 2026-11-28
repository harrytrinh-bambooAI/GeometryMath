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
  private final vn.geo.server.aigateway.internal.AiCallLog calls;

  public AiTestHookController(
      MockAiControl control,
      AiSettings settings,
      vn.geo.server.aigateway.internal.AiCallLog calls) {
    this.control = control;
    this.settings = settings;
    this.calls = calls;
  }

  /**
   * Lượt gọi AI gần nhất của một người dùng (kiểm "có một bản ghi lượt gọi trạng thái lỗi" từ E2E).
   */
  @org.springframework.web.bind.annotation.GetMapping("/test-hooks/ai/calls")
  public java.util.List<vn.geo.server.aigateway.internal.AiCallLog.Entry> calls(
      @org.springframework.web.bind.annotation.RequestParam java.util.UUID userId,
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int limit) {
    return calls.recent(userId, Math.min(limit, 100));
  }

  /**
   * @param mode ok | error | timeout (null = không đổi)
   * @param userId áp cho lượt gọi của người dùng này (null + lane null = mọi lượt)
   * @param lane làn kiểm thử (tiêu đề X-Geo-Test-Lane)
   * @param model đổi model đang hiệu lực
   * @param timeoutMs đổi thời gian chờ đang hiệu lực
   * @param scenario kịch bản trả lời "scene" (E2: F-GEO-111, F-GEO-112; "" = bỏ) cho userId/lane
   * @param delayMs E4: nhà cung cấp giả lập trả lời sau delayMs (0 = bỏ) cho userId/lane
   */
  public record AiHook(
      String mode,
      String userId,
      String lane,
      String model,
      Long timeoutMs,
      String scenario,
      Long delayMs,
      java.util.List<String> qaReplies) {}

  @PostMapping("/test-hooks/ai")
  public Map<String, Object> set(@RequestBody AiHook h) {
    if (h.mode() != null) {
      control.set(h.userId(), h.lane(), Simulation.valueOf(h.mode().toUpperCase()));
    }
    if (h.scenario() != null) {
      control.setScenario(h.userId(), h.lane(), h.scenario());
    }
    if (h.delayMs() != null) {
      control.setDelay(h.userId(), h.lane(), h.delayMs());
    }
    if (h.qaReplies() != null) {
      control.setQaScript(h.userId(), h.lane(), h.qaReplies());
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
