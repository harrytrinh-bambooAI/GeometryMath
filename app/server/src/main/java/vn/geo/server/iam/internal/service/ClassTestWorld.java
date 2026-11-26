package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.repo.ClassRepository;
import vn.geo.server.iam.internal.repo.ClassTestHookRepository;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;

/**
 * Móc kiểm thử S3-E1 (chỉ dev/test): dựng một "thế giới" riêng cho mỗi ca E2E — giáo viên, lớp
 * chặng 3, học sinh có mã ở các trạng thái — trong trường "Cộng đồng", để các ca chạy song song
 * không giẫm lên dữ liệu mẫu chung F-S3 hay lên nhau. Khóa người trùng tên viết "Hoàng Nam#2" (tên
 * hiển thị bỏ phần sau dấu #).
 */
@Service
@Profile({"dev", "test"})
public class ClassTestWorld {

  public static final String PASSWORD = "MatKhau#2026";

  public record ClassSpec(
      String key,
      String teacher,
      String name,
      Integer grade,
      List<String> active,
      List<String> pending,
      List<String> left,
      List<String> rejected) {}

  public record Spec(List<String> teachers, List<String> students, List<ClassSpec> classes) {}

  public record Person(
      UUID id, String displayName, String userCode, String password, String token, String role) {}

  public record Klass(UUID id, String name, String code) {}

  public record World(UUID orgId, Map<String, Person> people, Map<String, Klass> classes) {}

  private final CodeAccountRepository accounts;
  private final ClassRepository classes;
  private final ClassTestHookRepository hooks;
  private final PasswordHasher hasher;
  private final TokenService tokens;
  private final ClassService service;
  private final Clock clock;
  private final UUID community;

  public ClassTestWorld(
      CodeAccountRepository accounts,
      ClassRepository classes,
      ClassTestHookRepository hooks,
      PasswordHasher hasher,
      TokenService tokens,
      ClassService service,
      Clock clock,
      @Value("${geo.s2e1.community-org-id:00000000-0000-7000-8002-000000000100}") UUID community) {
    this.accounts = accounts;
    this.classes = classes;
    this.hooks = hooks;
    this.hasher = hasher;
    this.tokens = tokens;
    this.service = service;
    this.clock = clock;
    this.community = community;
  }

  public World build(Spec spec) {
    String hash = hasher.hash(PASSWORD);
    Map<String, Person> people = new LinkedHashMap<>();
    for (String t : nz(spec.teachers())) {
      people.put(t, person(t, true, hash));
    }
    for (String s : nz(spec.students())) {
      people.putIfAbsent(s, person(s, false, hash));
    }
    Map<String, Klass> out = new LinkedHashMap<>();
    for (ClassSpec c : nz(spec.classes())) {
      Person t = people.computeIfAbsent(c.teacher(), k -> person(k, true, hash));
      UUID id = Uuid7.next();
      String code = service.uniqueCode(ClassService::randomCode);
      classes.insertClass(
          community, id, t.id(), c.name(), c.grade() == null ? 11 : c.grade(), "2026–2027", code);
      member(people, c.active(), id, "active", hash);
      member(people, c.pending(), id, "pending", hash);
      member(people, c.left(), id, "left", hash);
      member(people, c.rejected(), id, "rejected", hash);
      out.put(
          c.key() == null ? c.name() : c.key(),
          new Klass(id, c.name(), ClassService.display(code)));
    }
    return new World(community, people, out);
  }

  private void member(
      Map<String, Person> people, List<String> keys, UUID classId, String status, String hash) {
    for (String k : nz(keys)) {
      Person p = people.computeIfAbsent(k, x -> person(x, false, hash));
      String key = NameKey.of(p.displayName());
      classes.joinRequest(community, classId, p.id(), ClassService.withCode(key, p.userCode()));
      if (!"pending".equals(status)) {
        classes.setStatus(community, classId, p.id(), status, null);
      }
    }
  }

  private Person person(String key, boolean teacher, String hash) {
    String name = key.contains("#") ? key.substring(0, key.indexOf('#')).strip() : key;
    Instant now = clock.instant();
    for (int i = 0; i < 20; i++) {
      UUID id = Uuid7.next();
      String code = UserCodes.newUserCode(teacher);
      boolean ok =
          accounts.insert(
              new CodeAccountRepository.NewAccount(
                  id,
                  community,
                  teacher ? "teacher" : "student",
                  teacher ? "verified" : "light",
                  name,
                  code,
                  hash,
                  hasher.recoveryHmac(id, UserCodes.newRecoveryCode()),
                  "1.0",
                  now),
              "Cộng đồng");
      if (ok) {
        Role r = teacher ? Role.TEACHER : Role.STUDENT;
        return new Person(
            id, name, code, PASSWORD, tokens.issue(new AuthPrincipal(community, id, r)), r.name());
      }
    }
    throw new IllegalStateException("không sinh được mã người dùng");
  }

  public UUID orgOf(UUID userId) {
    return hooks.orgOf(userId);
  }

  private static <T> List<T> nz(List<T> l) {
    return l == null ? List.of() : l;
  }
}
