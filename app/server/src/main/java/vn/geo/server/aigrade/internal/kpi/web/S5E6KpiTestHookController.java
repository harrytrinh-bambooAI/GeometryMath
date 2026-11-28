package vn.geo.server.aigrade.internal.kpi.web;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigrade.internal.kpi.service.KpiHookSupport;

/**
 * MÓC KIỂM THỬ S5-E6c (chỉ profile dev/test; T-261128-65): nạp số liệu KPI theo bài cho một lớp
 * (thay cho 20–40 bài chạy song song thật), rồi xét lại cổng như sau bài cuối của đợt.
 */
@RestController
@Profile({"dev", "test"})
public class S5E6KpiTestHookController {

  private final KpiHookSupport hooks;

  public S5E6KpiTestHookController(KpiHookSupport hooks) {
    this.hooks = hooks;
  }

  @PostMapping("/test-hooks/s5e6/kpi-seed")
  public Map<String, Object> seed(@RequestBody KpiHookSupport.SeedBody b) {
    return hooks.seed(b);
  }
}
