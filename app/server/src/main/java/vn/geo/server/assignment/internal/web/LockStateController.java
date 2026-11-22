package vn.geo.server.assignment.internal.web;

import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.repo.LockStateRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.common.web.NoStore;

/**
 * Trạng thái khóa lời giải cho công cụ đo (E3, BR-GEO-156, 157): {@code locked} theo
 * fn_solution_gate, áp cho bài lớp (giờ mở) và bài tự học (kế thừa khóa, D-GEO-026). Trang Làm bài
 * hỏi lại mỗi 5 giây (US-GEO-305-SC6, không muộn hơn 10 giây). Không chứa lời giải hay đáp số.
 */
@RestController
public class LockStateController {

  public record LockState(boolean locked, String kind, Instant releaseAt) {}

  private final LockStateRepository repo;

  public LockStateController(LockStateRepository repo) {
    this.repo = repo;
  }

  @NoStore
  @GetMapping("/api/v1/student/assignments/{id}/lock")
  public LockState lock(@PathVariable UUID id) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    LockStateRepository.Gate g = repo.gate(p.userId(), id);
    if ("NOT_FOUND".equals(g.status())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return new LockState(!"OK".equals(g.status()), g.kind(), g.releaseAt());
  }
}
