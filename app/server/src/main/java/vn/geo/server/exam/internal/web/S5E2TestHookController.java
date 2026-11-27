package vn.geo.server.exam.internal.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.exam.internal.service.ExamImportService;
import vn.geo.server.exam.internal.service.ExamSetService;
import vn.geo.server.exam.internal.service.S5E2TestHookService;

/**
 * MÓC KIỂM THỬ S5-E2 (chỉ profile dev/test): chạy lệnh nạp dữ liệu đề (thật, hoặc trong giao dịch
 * hoàn tác với bản sao có lỗi cài sẵn — TC-S5E2-01, 03, 04), thay câu khi ô chỉ còn 1 câu
 * (TC-S5E2-18), và giả lập cổng tạo bài có chấm của US-S5E4-02 (TC-S5E2-19; module assessment chưa
 * có, gọi {@code exam.api.ExamSetGuard} như module đó sẽ gọi).
 */
@RestController
@Profile({"dev", "test"})
public class S5E2TestHookController {

  /** mode: commit | fresh | bad-answer | bad-matrix. */
  public record ImportBody(String mode) {}

  public record ActorBody(UUID orgId, UUID teacherId, UUID setId, String code, Integer so) {}

  private final S5E2TestHookService hooks;
  private final ExamSetService sets;

  public S5E2TestHookController(S5E2TestHookService hooks, ExamSetService sets) {
    this.hooks = hooks;
    this.sets = sets;
  }

  @PostMapping("/test-hooks/s5e2/import")
  public Map<String, Object> importData(@RequestBody(required = false) ImportBody body) {
    return hooks.importData(body == null || body.mode() == null ? "commit" : body.mode());
  }

  @PostMapping("/test-hooks/s5e2/replace-last-item")
  public ObjectNode replaceLastItem(@RequestBody ActorBody b) {
    return as(b, () -> sets.replace(b.setId(), b.code(), b.so() == null ? 0 : b.so(), it -> false));
  }

  @PostMapping("/test-hooks/s5e2/graded-assignment")
  public Map<String, Object> gradedAssignment(@RequestBody ActorBody b) {
    return as(
        b,
        () -> {
          sets.requireAssignable(b.setId());
          Map<String, Object> out = new LinkedHashMap<>();
          out.put("allowed", true);
          out.put("created", 0);
          return out;
        });
  }

  private static <R> R as(ActorBody b, ScopedValue.CallableOp<R, RuntimeException> op) {
    return RequestContext.callAs(new AuthPrincipal(b.orgId(), b.teacherId(), Role.TEACHER), op);
  }

  /** Báo cáo nạp dạng Map (dùng chung kiểu của service). */
  static Map<String, Object> wrap(ExamImportService.Report r) {
    return Map.of("report", r);
  }
}
