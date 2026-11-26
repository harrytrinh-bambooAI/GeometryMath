package vn.geo.server.tracking.internal.classboard.service;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.tracking.internal.classboard.repo.S3E3SandboxRepository;

/**
 * MÓC KIỂM THỬ S3-E3 (chỉ dev/test, PO-51): mỗi lần gọi một "Cô Lan" mới (chủ các lớp {@code
 * classes}) và một "Thầy Khôi" mới (chủ {@code otherClasses}), học sinh theo trạng thái, bộ bài và
 * bảng trạng thái từng ô — ca kiểm không giẫm nhau và không sửa dữ liệu mẫu chung F-S3-050/051.
 */
@Service
@Profile({"dev", "test"})
public class S3E3SandboxService {

  /**
   * status: active (mặc định) | pending | left. shared = dùng lại cùng một em ở lớp khác trong ca.
   */
  public record StudentSpec(String name, String status, Boolean shared) {}

  /**
   * Một tương tác: kind open | hint | ask | answer; item = vị trí đề; at = "HH:mm" giờ Việt Nam của
   * hôm qua (null = bây giờ).
   */
  public record EventSpec(
      String student,
      int item,
      String kind,
      Integer level,
      String text,
      Boolean correct,
      String at) {}

  /**
   * cells: tên em → trạng thái từng đề (correct | review | doing | todo; chấp nhận cả chữ Việt
   * "Đúng", "Xem lại", "Đang làm", "Chưa làm").
   */
  public record SetSpec(
      String title,
      String status,
      Integer items,
      Map<String, List<String>> cells,
      List<EventSpec> events) {}

  /** bulkStudents: thêm N em Đang học sinh tự động ("Em 001"…), mỗi em làm Đề 1 (ca tải). */
  public record ClassSpec(
      String name, List<StudentSpec> students, List<SetSpec> sets, Integer bulkStudents) {}

  public record Person(UUID id, String token, String userCode) {}

  public record ItemOut(int position, UUID assignmentId) {}

  public record SetOut(UUID id, String title, List<ItemOut> items) {}

  public record ClassOut(UUID id, String name, Map<String, Person> students, List<SetOut> sets) {}

  public record SandboxOut(
      UUID orgId,
      Person teacher,
      Person otherTeacher,
      List<ClassOut> classes,
      List<ClassOut> otherClasses) {}

  private static final SecureRandom RND = new SecureRandom();
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

  private final S3E3SandboxRepository repo;
  private final TokenService tokens;

  public S3E3SandboxService(S3E3SandboxRepository repo, TokenService tokens) {
    this.repo = repo;
    this.tokens = tokens;
  }

  public SandboxOut create(List<ClassSpec> classes, List<ClassSpec> otherClasses) {
    UUID teacher = repo.createTeacher("Cô Lan");
    UUID other = repo.createTeacher("Thầy Khôi");
    Map<String, Person> shared = new HashMap<>();
    return new SandboxOut(
        S3E3SandboxRepository.ORG,
        new Person(teacher, token(teacher, Role.TEACHER), null),
        new Person(other, token(other, Role.TEACHER), null),
        build(teacher, classes, shared),
        build(other, otherClasses, shared));
  }

  /** Thêm tương tác sau khi dựng (ví dụ em gửi đáp số đúng rồi giáo viên bấm "Làm mới"). */
  public void event(UUID assignmentId, UUID userId, EventSpec e) {
    apply(assignmentId, userId, e);
  }

  private List<ClassOut> build(UUID teacher, List<ClassSpec> specs, Map<String, Person> shared) {
    List<ClassOut> out = new ArrayList<>();
    for (ClassSpec c : specs == null ? List.<ClassSpec>of() : specs) {
      UUID classId = Uuid7.next();
      repo.createClass(classId, teacher, c.name() == null ? "11A1" : c.name(), random(6));
      Map<String, Person> students = new LinkedHashMap<>();
      List<StudentSpec> list = new ArrayList<>(c.students() == null ? List.of() : c.students());
      int bulk = c.bulkStudents() == null ? 0 : Math.min(c.bulkStudents(), 300);
      for (int i = 1; i <= bulk; i++) {
        list.add(new StudentSpec(String.format("Em %03d", i), "active", false));
      }
      for (StudentSpec st : list) {
        boolean reuse = Boolean.TRUE.equals(st.shared()) && shared.containsKey(st.name());
        Person p = reuse ? shared.get(st.name()) : newStudent(st.name());
        if (Boolean.TRUE.equals(st.shared())) {
          shared.put(st.name(), p);
        }
        String key = students.containsKey(st.name()) ? st.name() + "#2" : st.name();
        repo.addMember(
            classId,
            p.id(),
            nameKey(st.name()) + " (" + p.userCode().toLowerCase(Locale.ROOT) + ")",
            st.status() == null ? "active" : st.status());
        students.put(key, p);
      }
      List<SetOut> sets = new ArrayList<>();
      int problemNo = 0;
      int n = c.sets() == null ? 0 : c.sets().size();
      for (int si = 0; si < n; si++) {
        SetSpec s = c.sets().get(si);
        boolean assigned = !"draft".equals(s.status());
        UUID setId = Uuid7.next();
        repo.createSet(setId, teacher, classId, s.title(), assigned, (n - si) * 10);
        int count = s.items() == null ? 4 : s.items();
        List<ItemOut> items = new ArrayList<>();
        for (int pos = 1; pos <= count; pos++) {
          problemNo++;
          items.add(
              new ItemOut(pos, repo.addItem(setId, teacher, classId, pos, problemNo, assigned)));
        }
        if (assigned) {
          fill(s, items, students, bulk);
        }
        sets.add(new SetOut(setId, s.title(), items));
      }
      out.add(new ClassOut(classId, c.name(), students, sets));
    }
    return out;
  }

  private void fill(SetSpec s, List<ItemOut> items, Map<String, Person> students, int bulk) {
    Instant base = Instant.now().minusSeconds(26 * 3600);
    if (s.cells() != null) {
      s.cells()
          .forEach((name, cells) -> {
            Person p = students.get(name);
            for (int i = 0; p != null && i < cells.size() && i < items.size(); i++) {
              UUID a = items.get(i).assignmentId();
              switch (cells.get(i)) {
                case "correct", "Đúng" -> {
                  repo.touch(a, p.id(), base, 0, 0);
                  repo.attempt(a, p.id(), base.plusSeconds(360), "1", true);
                }
                case "review", "Xem lại" -> {
                  repo.touch(a, p.id(), base, 0, 0);
                  repo.attempt(a, p.id(), base.plusSeconds(360), "0", false);
                }
                case "doing", "Đang làm" -> {
                  repo.touch(a, p.id(), base, 1, 0);
                  repo.hint(a, p.id(), base.plusSeconds(120), 1);
                }
                default -> {
                  // todo / Chưa làm: không có hàng nào
                }
              }
            }
          });
    }
    for (int i = 1; i <= bulk; i++) {
      Person p = students.get(String.format("Em %03d", i));
      repo.touch(items.getFirst().assignmentId(), p.id(), base, 0, 0);
    }
    for (EventSpec e : s.events() == null ? List.<EventSpec>of() : s.events()) {
      Person p = students.get(e.student());
      if (p != null && e.item() >= 1 && e.item() <= items.size()) {
        apply(items.get(e.item() - 1).assignmentId(), p.id(), e);
      }
    }
  }

  private void apply(UUID assignmentId, UUID userId, EventSpec e) {
    Instant at = at(e.at());
    switch (e.kind() == null ? "open" : e.kind()) {
      case "hint" -> {
        int level = e.level() == null ? 1 : e.level();
        repo.touch(assignmentId, userId, at, level, 0);
        repo.hint(assignmentId, userId, at, level);
      }
      case "ask" -> {
        repo.touch(assignmentId, userId, at, 0, 1);
        repo.question(assignmentId, userId, at, e.text());
      }
      case "answer" -> {
        repo.touch(assignmentId, userId, at, 0, 0);
        repo.attempt(
            assignmentId,
            userId,
            at,
            e.text() == null ? "1" : e.text(),
            Boolean.TRUE.equals(e.correct()));
      }
      default -> repo.touch(assignmentId, userId, at, 0, 0);
    }
  }

  private static Instant at(String hhmm) {
    if (hhmm == null || hhmm.isBlank()) {
      return Instant.now();
    }
    return LocalDate.now(VN).minusDays(1).atTime(LocalTime.parse(hhmm)).atZone(VN).toInstant();
  }

  private Person newStudent(String name) {
    String code = "HS-" + random(4) + "-" + random(4);
    UUID id = repo.createStudent(name, code);
    return new Person(id, token(id, Role.STUDENT), code);
  }

  private String token(UUID id, Role role) {
    return tokens.issue(new AuthPrincipal(S3E3SandboxRepository.ORG, id, role));
  }

  static String nameKey(String name) {
    String folded =
        Normalizer.normalize(name.replace('Đ', 'D').replace('đ', 'd'), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
    return folded.toLowerCase(Locale.ROOT).strip();
  }

  private static String random(int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(CODE_CHARS.charAt(RND.nextInt(CODE_CHARS.length())));
    }
    return b.toString();
  }
}
