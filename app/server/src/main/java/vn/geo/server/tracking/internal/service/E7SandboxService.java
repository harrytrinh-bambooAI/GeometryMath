package vn.geo.server.tracking.internal.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.tracking.internal.repo.E7SandboxRepository;

/** MÓC KIỂM THỬ E7 (chỉ gọi từ controller profile dev/test): lớp mỏng trên E7SandboxRepository. */
@Service
public class E7SandboxService {

  public static final UUID ORG = E7SandboxRepository.ORG;

  private final E7SandboxRepository repo;

  public E7SandboxService(E7SandboxRepository repo) {
    this.repo = repo;
  }

  public UUID createTeacher(String name) {
    return repo.createTeacher(name);
  }

  public void createClass(UUID id, UUID teacher, String name, String level, String joinCode) {
    repo.createClass(id, teacher, name, level, joinCode);
  }

  public UUID join(String joinCode, String nameKey, String displayName) {
    return repo.joinStudent(joinCode, nameKey, displayName);
  }

  /** Giao đề kho {@code code}; {@code openNow}: "Mở ngay" ngay sau khi giao. */
  public void assign(
      UUID teacher, UUID id, UUID classId, String code, OffsetDateTime release, boolean openNow) {
    String s = repo.createAssignment(teacher, id, classId, repo.versionOf(code), release);
    if (!"OK".equals(s)) {
      throw new IllegalStateException("không tạo được bài giao thử " + code + ": " + s);
    }
    if (openNow && !"OK".equals(repo.openNow(teacher, id))) {
      throw new IllegalStateException("không mở ngay được bài giao thử " + code);
    }
  }

  public void progress(
      UUID assignmentId, UUID userId, int hint, int asks, int tools, int mins, boolean solution) {
    repo.progress(assignmentId, userId, hint, asks, tools, mins, solution);
  }

  public void question(UUID assignmentId, UUID userId, String text, int minutesAgo) {
    repo.question(assignmentId, userId, text, minutesAgo);
  }
}
