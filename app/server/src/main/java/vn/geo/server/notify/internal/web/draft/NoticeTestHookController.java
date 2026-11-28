package vn.geo.server.notify.internal.web.draft;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.notify.internal.service.draft.NoticeHookSupport;
import vn.geo.server.notify.internal.service.draft.NoticePromptLog;

/**
 * MÓC KIỂM THỬ S5-E5 thông báo (chỉ profile dev/test): tin nhắn class_notice đã gửi cổng AI theo
 * giáo viên; dựng giáo án nháp / đã duyệt / đã sửa của giáo viên cho báo cáo dùng trợ lý (SC6).
 */
@RestController
@Profile({"dev", "test"})
public class NoticeTestHookController {

  private final NoticePromptLog prompts;
  private final NoticeHookSupport repo;

  public NoticeTestHookController(NoticePromptLog prompts, NoticeHookSupport repo) {
    this.prompts = prompts;
    this.repo = repo;
  }

  @GetMapping("/test-hooks/s5e5/notice-prompts")
  public List<String> prompts(@RequestParam UUID teacherId) {
    return prompts.of(teacherId);
  }

  /**
   * @param drafts tổng số giáo án nháp
   * @param approved số đã duyệt (phần còn lại bị bỏ)
   * @param edited số đã duyệt có sửa (version > 1)
   */
  public record LessonPlans(UUID orgId, UUID teacherId, int drafts, int approved, int edited) {}

  @PostMapping("/test-hooks/s5e5/lesson-plans")
  public Map<String, Object> lessonPlans(@RequestBody LessonPlans l) {
    repo.lessonPlans(l.orgId(), l.teacherId(), l.drafts(), l.approved(), l.edited());
    return Map.of("drafts", l.drafts());
  }
}
