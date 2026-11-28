package vn.geo.server.lessonplan.internal.patch.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.lessonplan.internal.patch.service.LessonPatchService;
import vn.geo.server.lessonplan.internal.patch.service.LessonPatchService.ChatBody;
import vn.geo.server.lessonplan.internal.patch.service.LessonPatchService.LinesBody;
import vn.geo.server.lessonplan.internal.patch.service.LessonPatchService.LockBody;

/**
 * Chat vá bản nháp (US-S5E1-04). Đặc tả ghi {@code /api/lesson-plans/{id}/chat}; theo quy ước của
 * app là {@code /api/v1/teacher/lesson-plans/{id|GA-nnnn}/…}. Giáo viên khác NOT_FOUND (không gọi
 * AI), học sinh FORBIDDEN. No-store.
 */
@RestController
@NoStore
public class LessonPatchController {

  private static final String BASE = "/api/v1/teacher/lesson-plans/{id}";

  private final LessonPatchService service;

  public LessonPatchController(LessonPatchService service) {
    this.service = service;
  }

  /** Gửi câu chat cho mục đang chọn → bản vá trước → sau, câu hỏi lại hoặc trả lời. */
  @PostMapping(BASE + "/chat")
  public Map<String, Object> chat(
      @PathVariable String id, @RequestBody(required = false) ChatBody body) {
    return service.chat(id, body);
  }

  /** Nhận cả bản vá ({@code lines} null) hoặc từng dòng → phiên bản mới. */
  @PostMapping(BASE + "/patches/{patchId}/accept")
  public Map<String, Object> accept(
      @PathVariable String id,
      @PathVariable UUID patchId,
      @RequestBody(required = false) LinesBody body) {
    return service.accept(id, patchId, body);
  }

  /** Bỏ dòng bản vá (không đổi giáo án). */
  @PostMapping(BASE + "/patches/{patchId}/skip")
  public Map<String, Object> skip(
      @PathVariable String id,
      @PathVariable UUID patchId,
      @RequestBody(required = false) LinesBody body) {
    return service.skip(id, patchId, body);
  }

  /** Hoàn tác về phiên bản trước. */
  @PostMapping(BASE + "/undo")
  public Map<String, Object> undo(@PathVariable String id) {
    return service.undo(id);
  }

  /** Khoá / mở khoá một mục. */
  @PutMapping(BASE + "/locks")
  public Map<String, Object> lock(
      @PathVariable String id, @RequestBody(required = false) LockBody body) {
    return service.lock(id, body);
  }
}
