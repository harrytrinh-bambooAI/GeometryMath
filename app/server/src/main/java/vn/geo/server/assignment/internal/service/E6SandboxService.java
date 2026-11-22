package vn.geo.server.assignment.internal.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.E6SandboxRepository;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;

/**
 * MÓC KIỂM THỬ E6 (chỉ dev/test, PO-51): mỗi lần gọi một GIÁO VIÊN MỚI với các lớp riêng, nên bước
 * "1 · Chọn lớp" của ca kiểm chỉ có đúng các lớp ca đó dựng, và ca giao bài, đổi giờ, "Mở ngay"
 * không giẫm nhau hay dữ liệu vàng.
 */
@Service
@Profile({"dev", "test"})
public class E6SandboxService {

  /**
   * @param release upcoming (sau 60 phút, như BG-1) | unset | open (Mở ngay) | số giây (giờ mở sau
   *     từng ấy giây)
   */
  public record AssignmentSpec(
      String key, String code, String release, Boolean allowHint, Boolean allowQa) {}

  public record ClassSpec(
      String name, String level, List<String> students, List<AssignmentSpec> assignments) {}

  public record Person(UUID id, String token) {}

  public record ClassOut(
      UUID id,
      String name,
      String joinCode,
      Map<String, Person> students,
      Map<String, UUID> assignments) {}

  public record SandboxOut(UUID orgId, Person teacher, List<ClassOut> classes) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final E6SandboxRepository repo;
  private final TokenService tokens;

  public E6SandboxService(E6SandboxRepository repo, TokenService tokens) {
    this.repo = repo;
    this.tokens = tokens;
  }

  public SandboxOut create(List<ClassSpec> classes) {
    UUID org = E6SandboxRepository.ORG;
    UUID teacher = repo.createTeacher("Giáo viên mẫu");
    List<ClassOut> out = new ArrayList<>();
    for (ClassSpec c : classes == null ? List.<ClassSpec>of() : classes) {
      UUID classId = Uuid7.next();
      String level = c.level() == null ? "thpt" : c.level();
      String code = joinCode();
      repo.createClass(classId, teacher, c.name() == null ? "11A1" : c.name(), level, code);
      Map<String, Person> students = new LinkedHashMap<>();
      for (String name : c.students() == null ? List.<String>of() : c.students()) {
        UUID uid = repo.joinStudent(code, nameKey(name), name);
        students.put(
            name, new Person(uid, tokens.issue(new AuthPrincipal(org, uid, Role.STUDENT))));
      }
      Map<String, UUID> assignments = new LinkedHashMap<>();
      for (AssignmentSpec a :
          c.assignments() == null ? List.<AssignmentSpec>of() : c.assignments()) {
        UUID id = Uuid7.next();
        String rel = a.release() == null ? "upcoming" : a.release();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime at =
            switch (rel) {
              case "unset" -> null;
              case "upcoming", "open" -> now.plusMinutes(60);
              default -> now.plusSeconds(Math.max(5, Long.parseLong(rel)));
            };
        String s =
            repo.createAssignment(
                teacher,
                id,
                classId,
                repo.versionOf(a.code()),
                at,
                !Boolean.FALSE.equals(a.allowHint()),
                !Boolean.FALSE.equals(a.allowQa()));
        if (!"OK".equals(s)) {
          throw new IllegalStateException("không tạo được bài giao thử " + a.code() + ": " + s);
        }
        if ("open".equals(rel) && !"OK".equals(repo.openNow(teacher, id))) {
          throw new IllegalStateException("không mở ngay được bài giao thử " + a.code());
        }
        if (!"unset".equals(rel) && !"upcoming".equals(rel) && !"open".equals(rel)) {
          repo.setReleaseIn(
              id, Long.parseLong(rel)); // giờ mở đúng n giây kể từ bây giờ (kể cả n < 5)
        }
        assignments.put(a.key() == null ? a.code() : a.key(), id);
      }
      out.add(new ClassOut(classId, c.name(), code, students, assignments));
    }
    return new SandboxOut(
        org, new Person(teacher, tokens.issue(new AuthPrincipal(org, teacher, Role.TEACHER))), out);
  }

  /** D-GEO-169: giờ mở = giờ máy chủ + n giây. */
  public OffsetDateTime releaseIn(UUID assignmentId, double seconds) {
    return repo.setReleaseIn(assignmentId, seconds);
  }

  private static String joinCode() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      b.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
    }
    return b.toString();
  }

  static String nameKey(String name) {
    String n = Normalizer.normalize(name.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd').replace('Đ', 'd').toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }
}
