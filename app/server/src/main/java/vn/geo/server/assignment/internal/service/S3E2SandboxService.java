package vn.geo.server.assignment.internal.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.S3E2SandboxRepository;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;

/**
 * MÓC KIỂM THỬ S3-E2 (chỉ dev/test, PO-51): mỗi lần gọi một "Cô Lan" mới (chủ các lớp ca dựng), một
 * "Thầy Khôi" mới (chủ các lớp otherClasses), học sinh Đang học và Chờ duyệt có thẻ — ca soạn,
 * giao, đóng bộ không giẫm nhau.
 */
@Service
@Profile({"dev", "test"})
public class S3E2SandboxService {

  public record ClassSpec(String name, List<String> students, List<String> pending) {}

  public record Person(UUID id, String token) {}

  public record ClassOut(
      UUID id, String name, Map<String, Person> students, Map<String, Person> pending) {}

  public record SandboxOut(
      UUID orgId,
      Person teacher,
      Person otherTeacher,
      List<ClassOut> classes,
      List<ClassOut> otherClasses) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final S3E2SandboxRepository repo;
  private final TokenService tokens;

  public S3E2SandboxService(S3E2SandboxRepository repo, TokenService tokens) {
    this.repo = repo;
    this.tokens = tokens;
  }

  public SandboxOut create(List<ClassSpec> classes, List<ClassSpec> otherClasses) {
    UUID teacher = repo.createTeacher("Cô Lan");
    UUID other = repo.createTeacher("Thầy Khôi");
    return new SandboxOut(
        S3E2SandboxRepository.ORG,
        person(teacher, Role.TEACHER),
        person(other, Role.TEACHER),
        build(teacher, classes),
        build(other, otherClasses));
  }

  private List<ClassOut> build(UUID teacher, List<ClassSpec> specs) {
    List<ClassOut> out = new ArrayList<>();
    for (ClassSpec c : specs == null ? List.<ClassSpec>of() : specs) {
      UUID classId = Uuid7.next();
      String code = random(6);
      repo.createClass(classId, teacher, c.name() == null ? "11A1" : c.name(), code);
      Map<String, Person> active = new LinkedHashMap<>();
      for (String name : c.students() == null ? List.<String>of() : c.students()) {
        active.put(
            name,
            person(repo.joinStudent(code, E6SandboxService.nameKey(name), name), Role.STUDENT));
      }
      Map<String, Person> pending = new LinkedHashMap<>();
      for (String name : c.pending() == null ? List.<String>of() : c.pending()) {
        UUID uid = repo.joinStudent(code, E6SandboxService.nameKey(name), name);
        repo.setPending(classId, uid);
        pending.put(name, person(uid, Role.STUDENT));
      }
      out.add(new ClassOut(classId, c.name(), active, pending));
    }
    return out;
  }

  private Person person(UUID id, Role role) {
    return new Person(id, tokens.issue(new AuthPrincipal(S3E2SandboxRepository.ORG, id, role)));
  }

  private static String random(int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
    }
    return b.toString();
  }
}
