package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.LockStateRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/** Trạng thái khóa lời giải cho công cụ đo (E3, BR-GEO-156, 157) theo fn_solution_gate. */
@Service
public class LockStateService {

  public record LockState(boolean locked, String kind, Instant releaseAt) {}

  private final LockStateRepository repo;

  public LockStateService(LockStateRepository repo) {
    this.repo = repo;
  }

  public LockState lock(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    LockStateRepository.Gate g = repo.gate(p.userId(), assignmentId);
    if ("NOT_FOUND".equals(g.status())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return new LockState(!"OK".equals(g.status()), g.kind(), g.releaseAt());
  }
}
