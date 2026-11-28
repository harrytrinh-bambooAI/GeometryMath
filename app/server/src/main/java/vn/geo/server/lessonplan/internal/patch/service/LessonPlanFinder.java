package vn.geo.server.lessonplan.internal.patch.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository.Plan;

/**
 * Tìm giáo án theo id hoặc mã GA-nnnn, chỉ của chính giáo viên (quyền theo quan hệ: giáo viên khác
 * NOT_FOUND, không lộ giáo án có tồn tại).
 */
@Component
public class LessonPlanFinder {

  private final LessonEditRepository repo;

  public LessonPlanFinder(LessonEditRepository repo) {
    this.repo = repo;
  }

  public Plan find(AuthPrincipal me, String idOrCode) {
    UUID id = null;
    try {
      id = UUID.fromString(idOrCode);
    } catch (IllegalArgumentException e) {
      // mã GA-nnnn
    }
    return (id != null ? repo.byId(me.userId(), id) : repo.byCode(me.userId(), idOrCode))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }
}
