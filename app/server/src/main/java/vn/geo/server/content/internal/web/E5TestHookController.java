package vn.geo.server.content.internal.web;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.error.AppException;
import vn.geo.server.content.internal.service.CatalogTestHooks;

/**
 * MÓC KIỂM THỬ của E5, chỉ profile dev/test: nạp một đề mới (chưa chạy kiểm chứng) vào bộ đề
 * 'kho_moi' giữa lúc giáo viên đang cuộn Kho đề (US-GEO-505-SC2, US-GEO-502-FC1).
 */
@RestController
@Profile({"dev", "test"})
public class E5TestHookController {

  private final CatalogTestHooks hooks;

  public E5TestHookController(CatalogTestHooks hooks) {
    this.hooks = hooks;
  }

  public record NewProblem(String statement, String prefix) {}

  @PostMapping("/test-hooks/e5/problems")
  public Map<String, Object> add(@RequestBody NewProblem body) {
    if (body == null || body.statement() == null || body.statement().isBlank()) {
      throw AppException.invalidField("statement", "Cần đề bài.");
    }
    String prefix = body.prefix() == null ? "HKM" : body.prefix();
    if (!prefix.matches("HK[A-Z]")) {
      throw AppException.invalidField("prefix", "Tiền tố mã dạng HK + một chữ hoa.");
    }
    return hooks.addNewProblem(body.statement().strip(), prefix);
  }
}
