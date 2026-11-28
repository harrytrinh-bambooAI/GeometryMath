package vn.geo.server.notify.internal.service.draft;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.notify.internal.repo.draft.NoticeDraftRepository;

/** Dựng dữ liệu cho MÓC KIỂM THỬ S5-E5 thông báo (chỉ profile dev/test). */
@Service
@Profile({"dev", "test"})
public class NoticeHookSupport {

  private final NoticeDraftRepository repo;

  public NoticeHookSupport(NoticeDraftRepository repo) {
    this.repo = repo;
  }

  /** {@code drafts} giáo án; {@code approved} đầu đã duyệt, {@code edited} đầu trong đó có sửa. */
  public void lessonPlans(UUID orgId, UUID teacherId, int drafts, int approved, int edited) {
    for (int i = 0; i < drafts; i++) {
      repo.seedLessonPlan(orgId, teacherId, i < approved, i < edited);
    }
  }
}
