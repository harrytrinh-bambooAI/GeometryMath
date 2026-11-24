package vn.geo.server.assignment.internal.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.PreviewService;
import vn.geo.server.assignment.internal.service.ReleaseService;
import vn.geo.server.common.web.NoStore;

/**
 * E6 — giao bài và khóa lời giải: lựa chọn ở bước "1 · Chọn lớp", thẻ vàng và nhật ký giờ mở (giáo
 * viên của lớp), dòng "Hôm nay em có n bài cần làm" (học sinh), màn xem trước như học sinh (giáo
 * viên; không gọi đường lời giải, D-GEO-153). Phản hồi mang trạng thái khóa nên không lưu đệm
 * (D-GEO-145).
 */
@RestController
public class ReleaseController {

  public record PreviewAsk(String question, String highestHint) {}

  private final ReleaseService release;
  private final PreviewService preview;

  public ReleaseController(ReleaseService release, PreviewService preview) {
    this.release = release;
    this.preview = preview;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/problems/{versionId}/assign-options")
  public ReleaseService.AssignOptions options(@PathVariable UUID versionId) {
    return release.options(versionId);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/assignments/{id}/release")
  public ReleaseService.ReleaseView release(@PathVariable UUID id) {
    return release.release(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/assignments/{id}/release-log")
  public Map<String, List<ReleaseService.LogItem>> log(@PathVariable UUID id) {
    return Map.of("items", release.log(id));
  }

  @NoStore
  @GetMapping("/api/v1/student/assignments/todo")
  public ReleaseService.Todo todo() {
    return release.todo();
  }

  /**
   * Màn xem trước: đề, hình, gợi ý và trạng thái khóa — không có lời giải hay đáp số (BR-GEO-301).
   */
  @NoStore
  @GetMapping("/api/v1/teacher/preview/{versionId}")
  public PreviewService.PreviewPage page(
      @PathVariable UUID versionId,
      @RequestParam(required = false) UUID classId,
      @RequestParam(required = false) OffsetDateTime releaseAt) {
    return preview.page(versionId, classId, releaseAt);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/preview/{versionId}/questions")
  public PreviewService.PreviewAnswer ask(@PathVariable UUID versionId, @RequestBody PreviewAsk b) {
    return preview.ask(versionId, b.question(), b.highestHint());
  }
}
