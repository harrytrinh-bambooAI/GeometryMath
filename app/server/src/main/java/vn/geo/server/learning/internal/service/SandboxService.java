package vn.geo.server.learning.internal.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.learning.internal.repo.SandboxRepository;

/** Móc kiểm thử E4 (chỉ dev/test): xem E4TestHookController. */
@Service
@Profile({"dev", "test"})
public class SandboxService {

  public static final UUID ORG = SandboxRepository.ORG;
  public static final UUID TEACHER = SandboxRepository.TEACHER;

  private final SandboxRepository repo;

  public SandboxService(SandboxRepository repo) {
    this.repo = repo;
  }

  public void createClass(UUID id, String name, String level, String joinCode) {
    repo.createClass(id, name, level, joinCode);
  }

  /** Giờ mở đã hẹn coi như vừa trôi qua (thay chờ đồng hồ thật trong E2E, TC-GEO-0486). */
  public boolean releasePassed(UUID assignmentId) {
    return repo.releasePassed(assignmentId) == 1;
  }

  public long countEvents(UUID assignmentId, UUID userId, String kind) {
    return repo.countEvents(assignmentId, userId, kind);
  }

  public UUID join(String joinCode, String nameKey, String displayName) {
    return repo.joinStudent(joinCode, nameKey, displayName);
  }

  public void assign(
      UUID id,
      UUID classId,
      String code,
      OffsetDateTime release,
      boolean hint,
      boolean qa,
      boolean openNow) {
    String s = repo.createAssignment(id, classId, repo.versionOf(code), release, hint, qa);
    if (!"OK".equals(s)) {
      throw new IllegalStateException("không tạo được bài giao thử " + code + ": " + s);
    }
    if (openNow && !"OK".equals(repo.openNow(id))) {
      throw new IllegalStateException("không mở ngay được bài giao thử " + code);
    }
  }
}
