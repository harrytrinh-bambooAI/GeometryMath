package vn.geo.server.learning.internal.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.learning.internal.service.SceneTimingService;
import vn.geo.server.learning.internal.service.SolutionService;

/**
 * Bảng Gợi ý / Lời giải của trang Làm bài (E4). Xin gợi ý theo mức là điểm cuối sẵn có {@code POST
 * /api/v1/student/assignments/{id}/hints/{level}} (module tracking, ủy quyền cho HintApi). Phản hồi
 * chứa gợi ý, lời giải hay trạng thái khóa không lưu đệm (D-GEO-145).
 */
@RestController
public class HelpController {

  public record SceneShownBody(Long durationMs) {}

  private final SolutionService solutions;
  private final SceneTimingService timing;

  public HelpController(SolutionService solutions, SceneTimingService timing) {
    this.solutions = solutions;
    this.timing = timing;
  }

  /**
   * Tiến độ, gợi ý đã xem, công tắc của giáo viên, trạng thái lời giải (không có nội dung khóa).
   */
  @NoStore
  @GetMapping("/api/v1/student/assignments/{id}/help")
  public SolutionService.HelpState help(@PathVariable UUID id) {
    return solutions.state(id);
  }

  /** Xem lời giải từng bước (ghi sự kiện mở lời giải lần đầu); còn khóa: FORBIDDEN. */
  @NoStore
  @PostMapping("/api/v1/student/assignments/{id}/solution/open")
  public SolutionService.SolutionView openSolution(@PathVariable UUID id) {
    return solutions.open(id);
  }

  /** Sự kiện không bền "hình đã hiện" của bài tự học (BR-GEO-169). */
  @PostMapping("/api/v1/student/assignments/{id}/scene-shown")
  public Map<String, String> sceneShown(
      @PathVariable UUID id, @RequestBody(required = false) SceneShownBody body) {
    timing.record(id, body == null ? null : body.durationMs());
    return Map.of("status", "OK");
  }
}
