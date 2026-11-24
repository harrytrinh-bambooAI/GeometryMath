package vn.geo.server.tracking.internal.web;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.tracking.internal.service.E7SandboxService;

/**
 * MÓC KIỂM THỬ E7, chỉ profile dev/test (không có ở prod): dựng giáo viên, lớp, bài giao, tiến độ,
 * câu hỏi riêng cho một ca E2E (PO-51), và bỏ đệm Theo dõi lớp của một bài (giả lập máy chủ ứng
 * dụng khởi động lại: mọi trạng thái trong bộ nhớ và đệm mất, chỉ còn CSDL).
 */
@RestController
@Profile({"dev", "test"})
public class E7TestHookController {

  /** release: upcoming (sau 60 phút) | unset | open (Mở ngay sau khi giao). */
  public record AssignmentSpec(String key, String code, String release) {}

  public record ClassSpec(
      String name, String level, List<String> students, List<AssignmentSpec> assignments) {}

  public record ProgressSpec(
      String assignment,
      String student,
      int hint,
      int asks,
      int tools,
      int mins,
      Boolean solution) {}

  public record QuestionSpec(String assignment, String student, String text, int minutesAgo) {}

  public record SandboxRequest(
      List<ClassSpec> classes, List<ProgressSpec> progress, List<QuestionSpec> questions) {}

  public record Person(UUID id, String token) {}

  public record ClassOut(
      UUID id, String name, Map<String, Person> students, Map<String, UUID> assignments) {}

  public record SandboxOut(UUID orgId, Person teacher, List<ClassOut> classes) {}

  public record Restart(UUID assignmentId) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final E7SandboxService repo;
  private final TokenService tokens;
  private final StringRedisTemplate redis;
  private final RedisKeys keys;

  public E7TestHookController(
      E7SandboxService repo, TokenService tokens, StringRedisTemplate redis, RedisKeys keys) {
    this.repo = repo;
    this.tokens = tokens;
    this.redis = redis;
    this.keys = keys;
  }

  @PostMapping("/test-hooks/e7/sandbox")
  public SandboxOut create(@RequestBody SandboxRequest req) {
    UUID org = E7SandboxService.ORG;
    UUID teacher = repo.createTeacher("Giáo viên mẫu");
    List<ClassOut> out = new ArrayList<>();
    Map<String, UUID> asgByKey = new HashMap<>();
    Map<String, Map<String, Person>> studentsByAsg = new HashMap<>();
    for (ClassSpec c : req.classes() == null ? List.<ClassSpec>of() : req.classes()) {
      UUID classId = Uuid7.next();
      String level = c.level() == null ? "thpt" : c.level();
      String code = joinCode();
      repo.createClass(classId, teacher, c.name() == null ? "11A1" : c.name(), level, code);
      Map<String, Person> students = new LinkedHashMap<>();
      for (String name : c.students() == null ? List.<String>of() : c.students()) {
        UUID uid = repo.join(code, nameKey(name), name);
        students.put(
            name, new Person(uid, tokens.issue(new AuthPrincipal(org, uid, Role.STUDENT))));
      }
      Map<String, UUID> assignments = new LinkedHashMap<>();
      for (AssignmentSpec a :
          c.assignments() == null ? List.<AssignmentSpec>of() : c.assignments()) {
        UUID id = Uuid7.next();
        String rel = a.release() == null ? "upcoming" : a.release();
        OffsetDateTime at =
            "unset".equals(rel) ? null : OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(60);
        repo.assign(teacher, id, classId, a.code(), at, "open".equals(rel));
        String key = a.key() == null ? a.code() : a.key();
        assignments.put(key, id);
        asgByKey.put(key, id);
        studentsByAsg.put(key, students);
      }
      out.add(new ClassOut(classId, c.name(), students, assignments));
    }
    for (ProgressSpec p : req.progress() == null ? List.<ProgressSpec>of() : req.progress()) {
      repo.progress(
          asgByKey.get(p.assignment()),
          studentsByAsg.get(p.assignment()).get(p.student()).id(),
          p.hint(),
          p.asks(),
          p.tools(),
          p.mins(),
          Boolean.TRUE.equals(p.solution()));
    }
    for (QuestionSpec q : req.questions() == null ? List.<QuestionSpec>of() : req.questions()) {
      repo.question(
          asgByKey.get(q.assignment()),
          studentsByAsg.get(q.assignment()).get(q.student()).id(),
          q.text(),
          q.minutesAgo());
    }
    return new SandboxOut(
        org, new Person(teacher, tokens.issue(new AuthPrincipal(org, teacher, Role.TEACHER))), out);
  }

  /**
   * Giả lập máy chủ ứng dụng khởi động lại (US-GEO-703-SC3, SC7): xóa đệm Theo dõi lớp của bài, để
   * lần đọc sau chỉ dựa vào dữ liệu đã ghi bền trong CSDL.
   */
  @PostMapping("/test-hooks/e7/restart")
  public Map<String, Object> restart(@RequestBody Restart req) {
    Boolean deleted =
        redis.delete(
            keys.key("t", E7SandboxService.ORG.toString(), "mon", req.assignmentId().toString()));
    return Map.of("assignmentId", req.assignmentId(), "cacheDropped", Boolean.TRUE.equals(deleted));
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
