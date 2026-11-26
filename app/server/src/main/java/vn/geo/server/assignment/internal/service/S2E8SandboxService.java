package vn.geo.server.assignment.internal.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.S2E8SandboxRepository;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;

/**
 * MÓC KIỂM THỬ S2-E8 (chỉ dev/test, PO-51): mỗi lần gọi một GIÁO VIÊN MỚI (chủ các lớp ca đó dựng),
 * một giáo viên khác không dạy lớp nào ("Cô Lan", US-S2E8-05-FC7/FC10) và các học sinh có thẻ, nên
 * ca làm bài, hết lượt, giao bài không giẫm nhau.
 */
@Service
@Profile({"dev", "test"})
public class S2E8SandboxService {

  public record ClassSpec(String name, List<String> students) {}

  public record Person(UUID id, String token, String userCode) {}

  public record ClassOut(UUID id, String name, Map<String, Person> students) {}

  public record SandboxOut(
      UUID orgId, Person teacher, Person otherTeacher, List<ClassOut> classes) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final S2E8SandboxRepository repo;
  private final TokenService tokens;

  public S2E8SandboxService(S2E8SandboxRepository repo, TokenService tokens) {
    this.repo = repo;
    this.tokens = tokens;
  }

  public SandboxOut create(List<ClassSpec> classes) {
    UUID org = S2E8SandboxRepository.ORG;
    UUID teacher = repo.createTeacher("Cô Hạnh");
    UUID other = repo.createTeacher("Cô Lan");
    List<ClassOut> out = new ArrayList<>();
    for (ClassSpec c : classes == null ? List.<ClassSpec>of() : classes) {
      UUID classId = Uuid7.next();
      String code = random(6);
      repo.createClass(classId, teacher, c.name() == null ? "11A1" : c.name(), code);
      Map<String, Person> students = new LinkedHashMap<>();
      for (String name : c.students() == null ? List.<String>of() : c.students()) {
        UUID uid = repo.joinStudent(code, E6SandboxService.nameKey(name), name);
        String userCode = "HS-" + random(4) + "-" + random(4);
        repo.setUserCode(uid, userCode);
        students.put(
            name,
            new Person(uid, tokens.issue(new AuthPrincipal(org, uid, Role.STUDENT)), userCode));
      }
      out.add(new ClassOut(classId, c.name(), students));
    }
    return new SandboxOut(
        org,
        new Person(teacher, tokens.issue(new AuthPrincipal(org, teacher, Role.TEACHER)), null),
        new Person(other, tokens.issue(new AuthPrincipal(org, other, Role.TEACHER)), null),
        out);
  }

  private static String random(int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
    }
    return b.toString();
  }
}
