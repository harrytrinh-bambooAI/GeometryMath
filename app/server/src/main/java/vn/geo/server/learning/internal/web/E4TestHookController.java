package vn.geo.server.learning.internal.web;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.learning.internal.service.SandboxService;

/**
 * MÓC KIỂM THỬ E4, chỉ profile dev/test (không có ở prod): dựng lớp riêng cho một ca E2E (PO-51).
 * Mỗi ca × mỗi dự án Playwright một lớp, nên ca "Mở ngay", xem gợi ý, hỏi đáp không giẫm nhau và
 * không đổi dữ liệu vàng mà E1…E3 đọc. Trả thẻ đăng nhập của giáo viên và từng học sinh.
 */
@RestController
@Profile({"dev", "test"})
public class E4TestHookController {

  /**
   * @param release upcoming (giờ mở sau 60 phút, như BG-1) | unset (Chưa đặt giờ) | open (Mở ngay)
   *     | số giây (giờ mở sau từng ấy giây, ví dụ "60")
   */
  public record AssignmentSpec(
      String key, String code, String release, Boolean allowHint, Boolean allowQa) {}

  public record ClassSpec(
      String name, String level, List<String> students, List<AssignmentSpec> assignments) {}

  public record SandboxRequest(List<ClassSpec> classes) {}

  public record Person(UUID id, String token) {}

  public record ClassOut(
      UUID id, String name, Map<String, Person> students, Map<String, UUID> assignments) {}

  public record SandboxOut(UUID orgId, Person teacher, List<ClassOut> classes) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final SandboxService sandbox;
  private final TokenService tokens;

  public E4TestHookController(SandboxService sandbox, TokenService tokens) {
    this.sandbox = sandbox;
    this.tokens = tokens;
  }

  @PostMapping("/test-hooks/e4/sandbox")
  public SandboxOut create(@RequestBody SandboxRequest req) {
    UUID org = SandboxService.ORG;
    List<ClassOut> out = new ArrayList<>();
    for (ClassSpec c : req.classes()) {
      UUID classId = Uuid7.next();
      String level = c.level() == null ? "thpt" : c.level();
      String code = joinCode();
      sandbox.createClass(classId, c.name() == null ? "11A1" : c.name(), level, code);
      Map<String, Person> students = new LinkedHashMap<>();
      for (String name : c.students() == null ? List.<String>of() : c.students()) {
        UUID uid = sandbox.join(code, nameKey(name), name);
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
              default -> now.plusSeconds(Long.parseLong(rel));
            };
        sandbox.assign(
            id,
            classId,
            a.code(),
            at,
            !Boolean.FALSE.equals(a.allowHint()),
            !Boolean.FALSE.equals(a.allowQa()),
            "open".equals(rel));
        assignments.put(a.key() == null ? a.code() : a.key(), id);
      }
      out.add(new ClassOut(classId, c.name(), students, assignments));
    }
    UUID teacher = SandboxService.TEACHER;
    return new SandboxOut(
        org, new Person(teacher, tokens.issue(new AuthPrincipal(org, teacher, Role.TEACHER))), out);
  }

  private static String joinCode() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      b.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
    }
    return b.toString();
  }

  /** Như class_member.name_key: bỏ dấu, chữ thường, gộp khoảng trắng. */
  static String nameKey(String name) {
    String n = Normalizer.normalize(name.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd').replace('Đ', 'd').toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }
}
