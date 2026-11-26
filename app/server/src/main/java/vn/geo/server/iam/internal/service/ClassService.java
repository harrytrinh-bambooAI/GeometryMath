package vn.geo.server.iam.internal.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.event.DomainEvents;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.api.StudentPasswordResetEvent;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.repo.ClassRepository;
import vn.geo.server.iam.internal.repo.ClassRepository.ClassRow;
import vn.geo.server.iam.internal.repo.ClassRepository.MemberRow;

/**
 * Lớp chặng 3 (S3-E1): tạo lớp + mã lớp (US-S3E1-01), vào lớp bằng mã chờ duyệt có giới hạn dò mã
 * (US-S3E1-02, PO-66 G4), duyệt / từ chối / cho rời lớp (US-S3E1-03), đặt lại mật khẩu học sinh
 * (US-S3E1-04), quyền theo quan hệ (US-S3E1-05: ngoài quan hệ NOT_FOUND, sai vai FORBIDDEN, mỗi lần
 * ghi sự kiện bảo mật), công tắc AI theo lớp (US-S3E5-04).
 */
@Service
public class ClassService {

  public static final String MSG_01 = "Mã lớp chưa đúng hoặc lớp đã khóa. Em hỏi lại thầy cô nhé.";
  public static final String MSG_02 =
      "Em đang chờ thầy cô duyệt. Trong lúc chờ, em vẫn dùng Tự học.";
  public static final String MSG_03 = "Em đã ở trong lớp này rồi.";
  public static final String MSG_04 = "Tên lớp chưa có hoặc dài quá 60 ký tự.";
  public static final String MSG_08 = "Đã thử quá nhiều lần. Vui lòng thử lại sau ít phút.";
  public static final String MSG_YEAR = "Năm học dạng 2026–2027, năm sau bằng năm trước cộng 1.";
  public static final String MSG_GRADE = "Cấp học từ Lớp 6 đến Lớp 12.";
  public static final String MSG_LIMIT = "Mỗi giáo viên có tối đa 20 lớp Đang dạy.";
  public static final String MSG_SWITCH = "Giá trị công tắc phải là đúng hoặc sai.";

  public static final int MAX_ACTIVE_CLASSES = 20;
  public static final int JOIN_SHORT_MAX = 10;
  public static final Duration JOIN_SHORT = Duration.ofMinutes(15);
  public static final int JOIN_DAY_MAX = 30;
  public static final Duration JOIN_DAY = Duration.ofHours(24);
  public static final int RESET_MAX = 10;
  public static final Duration RESET_WINDOW = Duration.ofHours(1);
  public static final Duration TEMP_TTL = Duration.ofHours(24);

  /** Bỏ 0, O, 1, I (BR-S3E1-03). */
  static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  /** Mật khẩu tạm: 8 ký tự chữ thường và số, bỏ ký tự dễ nhầm (BR-S3E1-31). */
  static final String TEMP_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789";

  private static final SecureRandom RANDOM = new SecureRandom();

  public record ClassView(
      UUID id, String name, int grade, String academicYear, String code, String status) {}

  public record ClassLine(
      UUID id,
      String name,
      int grade,
      String academicYear,
      String code,
      String status,
      long activeCount,
      long pendingCount) {}

  public record ClassDetail(
      UUID id,
      String name,
      int grade,
      String academicYear,
      String code,
      String status,
      boolean codeJoinEnabled,
      boolean approvalRequired,
      boolean aiEnabled,
      long activeCount,
      long pendingCount) {}

  public record Pending(UUID userId, String displayName, String userCode, Instant requestedAt) {}

  public record Student(
      UUID userId, String displayName, String userCode, String status, Instant joinedAt) {}

  public record StudentPage(List<Student> items, String nextCursor) {}

  public record Decision(UUID userId, String status) {}

  public record Joined(String status, String className, String message) {}

  /** Lớp của học sinh; {@code archived} = lớp Đã lưu trữ, chỉ xem (US-S3E6-02, BR-S3E6-15). */
  public record MyClass(String className, String status, boolean archived) {}

  public record TempPassword(String tempPassword, Instant expiresAt) {}

  public record AiStatus(boolean aiEnabled) {}

  private final ClassRepository repo;
  private final RelationDenials denials;
  private final ClassCursor cursors;
  private final PasswordHasher hasher;
  private final SessionRevocations revocations;
  private final DomainEvents events;
  private final TransactionTemplate tx;
  private final Clock clock;

  public ClassService(
      ClassRepository repo,
      RelationDenials denials,
      ClassCursor cursors,
      PasswordHasher hasher,
      SessionRevocations revocations,
      DomainEvents events,
      TransactionTemplate tx,
      Clock clock) {
    this.repo = repo;
    this.denials = denials;
    this.cursors = cursors;
    this.hasher = hasher;
    this.revocations = revocations;
    this.events = events;
    this.tx = tx;
    this.clock = clock;
  }

  // ------------------------------------------------------------------ giáo viên: lớp (US-S3E1-01)

  public List<ClassLine> myClasses() {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    return repo.teacherClasses(p.orgId(), p.userId()).stream()
        .map(c ->
            new ClassLine(
                c.id(),
                c.name(),
                c.grade(),
                c.academicYear(),
                display(c.joinCode()),
                status(c),
                c.active(),
                c.pending()))
        .toList();
  }

  public ClassView create(String rawName, Object rawGrade, String rawYear) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    String name = NameKey.clean(rawName);
    if (name.isEmpty() || name.codePointCount(0, name.length()) > 60) {
      throw AppException.invalidField("name", MSG_04);
    }
    int grade = parseGrade(rawGrade);
    String year = parseYear(rawYear);
    UUID id = Uuid7.next();
    String code =
        tx.execute(s -> {
          if (repo.activeClassCount(p.orgId(), p.userId()) >= MAX_ACTIVE_CLASSES) {
            throw AppException.invalidField("limit", MSG_LIMIT);
          }
          String c = uniqueCode(ClassService::randomCode);
          repo.insertClass(p.orgId(), id, p.userId(), name, grade, year, c);
          return c;
        });
    return new ClassView(id, name, grade, year, display(code), "active");
  }

  public ClassDetail detail(UUID classId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    return toDetail(owned(p, classId));
  }

  /** Đặt lại mã lớp: mã cũ hết hiệu lực ngay, thành viên giữ nguyên (BR-S3E1-05). */
  public ClassDetail resetCode(UUID classId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    owned(p, classId);
    tx.executeWithoutResult(
        s -> repo.setJoinCode(p.orgId(), classId, uniqueCode(ClassService::randomCode)));
    return toDetail(owned(p, classId));
  }

  /**
   * Đổi công tắc lớp. Chỉ nhận {@code codeJoinEnabled}, {@code aiEnabled} kiểu đúng/sai; khóa khác
   * (ví dụ tắt bước duyệt, PO-66 G4) hay giá trị khác là VALIDATION_FAILED (BR-S3E1-06,
   * BR-S3E5-40).
   */
  public ClassDetail settings(UUID classId, Map<String, Object> body) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    owned(p, classId);
    if (body == null || body.isEmpty()) {
      throw AppException.invalidField("settings", MSG_SWITCH);
    }
    Boolean codeJoin = null;
    Boolean ai = null;
    for (Map.Entry<String, Object> e : body.entrySet()) {
      if (!(e.getValue() instanceof Boolean b)) {
        throw AppException.invalidField(e.getKey(), MSG_SWITCH);
      }
      switch (e.getKey()) {
        case "codeJoinEnabled" -> codeJoin = b;
        case "aiEnabled" -> ai = b;
        default ->
            throw AppException.invalidField(
                e.getKey(), "Lớp chỉ có công tắc Cho vào lớp bằng mã và Trợ lý AI cho học sinh.");
      }
    }
    repo.setSwitches(p.orgId(), classId, codeJoin, ai);
    return toDetail(owned(p, classId));
  }

  /** Lưu trữ lớp (không tính vào giới hạn 20 lớp Đang dạy, BR-S3E1-04). */
  public ClassDetail archive(UUID classId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    owned(p, classId);
    // US-S3E6-02 (BR-S3E6-14): mã lớp ngừng dùng (byJoinCode chỉ mở lớp Đang dạy), em Chờ duyệt
    // tự thành Bị từ chối — cùng một giao dịch.
    tx.executeWithoutResult(s -> {
      if (repo.archive(p.orgId(), classId, clock.instant()) == 1) {
        repo.rejectPending(p.orgId(), classId);
      }
    });
    return toDetail(owned(p, classId));
  }

  /**
   * US-S3E6-02 (BR-S3E6-16, 17): bỏ lưu trữ, lớp về Đang dạy với mã lớp mới; vẫn giữ giới hạn 20
   * lớp Đang dạy. Lớp đang Đang dạy: không đổi gì.
   */
  public ClassDetail unarchive(UUID classId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class");
    ClassRow c = owned(p, classId);
    if (c.archived()) {
      tx.executeWithoutResult(s -> {
        if (repo.activeClassCount(p.orgId(), p.userId()) >= MAX_ACTIVE_CLASSES) {
          throw AppException.invalidField("limit", MSG_LIMIT);
        }
        repo.unarchive(p.orgId(), classId, uniqueCode(ClassService::randomCode));
      });
    }
    return toDetail(owned(p, classId));
  }

  /** Sinh mã HK chưa trùng (BR-S3E1-03, US-S3E1-01-FC6); trả dạng lưu 6 ký tự không gạch. */
  public String uniqueCode(Supplier<String> generator) {
    for (int i = 0; i < 50; i++) {
      String c = storeForm(generator.get());
      if (c != null && !repo.joinCodeTaken(c)) {
        return c;
      }
    }
    throw new AppException(ErrorCode.CONFLICT, "Chưa sinh được mã lớp, vui lòng thử lại.");
  }

  static String randomCode() {
    StringBuilder b = new StringBuilder("HK-");
    for (int i = 0; i < 4; i++) {
      b.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
    }
    return b.toString();
  }

  /** " hk-4p7q " → "HK4P7Q"; sai dạng → null. */
  public static String storeForm(String raw) {
    if (raw == null) {
      return null;
    }
    String s = raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    return s.matches("HK[" + CODE_ALPHABET + "]{4}") ? s : null;
  }

  public static String display(String joinCode) {
    return joinCode != null && joinCode.startsWith("HK") && joinCode.length() == 6
        ? "HK-" + joinCode.substring(2)
        : joinCode;
  }

  static int parseGrade(Object raw) {
    int g;
    if (raw instanceof Number n && n.doubleValue() == n.intValue()) {
      g = n.intValue();
    } else if (raw instanceof String s && s.strip().matches("(Lớp\\s*)?\\d{1,2}")) {
      g = Integer.parseInt(s.replaceAll("\\D", ""));
    } else {
      throw AppException.invalidField("grade", MSG_GRADE);
    }
    if (g < 6 || g > 12) {
      throw AppException.invalidField("grade", MSG_GRADE);
    }
    return g;
  }

  static String parseYear(String raw) {
    String s = raw == null ? "" : raw.strip().replaceAll("\\s*[-–—]\\s*", "–");
    if (!s.matches("\\d{4}–\\d{4}")) {
      throw AppException.invalidField("academicYear", MSG_YEAR);
    }
    int a = Integer.parseInt(s.substring(0, 4));
    int b = Integer.parseInt(s.substring(5));
    if (b != a + 1 || a < 2000 || a > 2100) {
      throw AppException.invalidField("academicYear", MSG_YEAR);
    }
    return s;
  }

  private ClassRow owned(AuthPrincipal p, UUID classId) {
    return repo.ownedClass(p.orgId(), p.userId(), classId)
        .orElseThrow(() -> denials.notFound(p, "class"));
  }

  private static String status(ClassRow c) {
    return c.archived() ? "archived" : "active";
  }

  private static ClassDetail toDetail(ClassRow c) {
    return new ClassDetail(
        c.id(),
        c.name(),
        c.grade(),
        c.academicYear(),
        display(c.joinCode()),
        status(c),
        c.codeJoinEnabled(),
        true,
        c.aiEnabled(),
        c.active(),
        c.pending());
  }

  // ------------------------------------------------------------------ giáo viên: thành viên (03)

  public List<Pending> pending(UUID classId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class_member");
    owned(p, classId);
    return repo.pending(p.orgId(), classId).stream()
        .map(m -> new Pending(m.userId(), m.displayName(), m.userCode(), m.joinedAt()))
        .toList();
  }

  public StudentPage students(UUID classId, Integer limit, String cursor) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class_member");
    owned(p, classId);
    int size = limit == null ? 50 : limit;
    if (size < 1) {
      throw AppException.invalidField("limit", "Kích thước trang phải từ 1 trở lên.");
    }
    size = Math.min(size, 100);
    ClassCursor.Key after = null;
    if (cursor != null && !cursor.isEmpty()) {
      after =
          cursors
              .decode(cursor, classId)
              .orElseThrow(() -> denials.invalidCursor(p, "class_member"));
    }
    List<MemberRow> rows =
        repo.students(
            p.orgId(),
            classId,
            after == null ? null : after.nameKey(),
            after == null ? null : after.userId(),
            size + 1);
    String next = null;
    if (rows.size() > size) {
      rows = rows.subList(0, size);
      MemberRow last = rows.get(rows.size() - 1);
      next = cursors.encode(classId, last.nameKey(), last.userId());
    }
    return new StudentPage(rows.stream().map(ClassService::student).toList(), next);
  }

  /** Chi tiết một học sinh của lớp (Đang học hoặc Đã rời); không có cờ riêng tư (BR-S3E5-26). */
  public Student student(UUID classId, UUID userId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "student");
    owned(p, classId);
    return repo.member(p.orgId(), classId, userId)
        .filter(m -> "active".equals(m.status()) || "left".equals(m.status()))
        .map(ClassService::student)
        .orElseThrow(() -> denials.notFound(p, "student"));
  }

  private static Student student(MemberRow m) {
    return new Student(m.userId(), m.displayName(), m.userCode(), m.status(), m.joinedAt());
  }

  /** approve | reject | remove; lặp cùng quyết định là thành công, quyết định khác là CONFLICT. */
  public Decision decide(UUID classId, UUID userId, String decision) {
    AuthPrincipal p = denials.require(Role.TEACHER, "class_member");
    owned(p, classId);
    String target =
        switch (decision) {
          case "approve" -> "active";
          case "reject" -> "rejected";
          case "remove" -> "left";
          default -> throw new AppException(ErrorCode.NOT_FOUND, RelationDenials.MSG_NOT_FOUND);
        };
    String from = "remove".equals(decision) ? "active" : "pending";
    String result =
        tx.execute(s -> {
          Optional<MemberRow> found = repo.member(p.orgId(), classId, userId);
          if (found.isEmpty()) {
            return null;
          }
          MemberRow m = found.get();
          if (target.equals(m.status())) {
            return target;
          }
          if (!from.equals(m.status())) {
            return "CONFLICT";
          }
          String key = null;
          if ("active".equals(target)) {
            String base = baseKey(m);
            key =
                repo.activeNameTaken(p.orgId(), classId, base, userId)
                    ? withCode(base, m.userCode())
                    : base;
          }
          repo.setStatus(p.orgId(), classId, userId, target, key);
          return target;
        });
    if (result == null) {
      throw denials.notFound(p, "class_member");
    }
    if ("CONFLICT".equals(result)) {
      throw new AppException(
          ErrorCode.CONFLICT, "Trạng thái của em đã đổi. Tải lại danh sách để xem mới nhất.");
    }
    return new Decision(userId, result);
  }

  /** Khóa tên gốc (bỏ hậu tố mã người dùng nếu có). */
  private static String baseKey(MemberRow m) {
    String key = NameKey.of(m.displayName());
    if (key.isEmpty()) {
      key = m.nameKey();
    }
    return key.codePointCount(0, key.length()) > 60 ? key.substring(0, 60) : key;
  }

  /**
   * Hai em trùng tên trong lớp (BR-S3E1-21): khóa tên kèm mã người dùng để chỉ mục MVP
   * class_member_name_uq không chặn (không nới chỉ mục: đăng nhập tên MVP vẫn dựa vào nó).
   */
  static String withCode(String base, String userCode) {
    String suffix = " (" + (userCode == null ? "" : userCode.toLowerCase(Locale.ROOT)) + ")";
    int room = 60 - suffix.length();
    String head = base.length() > room ? base.substring(0, room).strip() : base;
    return head + suffix;
  }

  // ------------------------------------------------------------------ đặt lại mật khẩu (04)

  public TempPassword resetPassword(UUID classId, UUID userId) {
    AuthPrincipal p = denials.require(Role.TEACHER, "student_password");
    if (owned(p, classId).archived()) {
      // US-S3E6-02-FC7 (BR-S3E6-14): lớp Đã lưu trữ không đặt lại mật khẩu
      throw denials.notFound(p, "student_password");
    }
    Instant now = clock.instant();
    String temp = newTemp();
    String hash = hasher.hash(temp);
    Object out =
        tx.execute(s -> {
          Optional<MemberRow> m = repo.member(p.orgId(), classId, userId);
          if (m.isEmpty() || !"active".equals(m.get().status()) || m.get().userCode() == null) {
            return "NOT_FOUND";
          }
          ClassRepository.ResetWindow w = repo.resetsLastHour(p.orgId(), p.userId(), now);
          if (w.count() >= RESET_MAX) {
            return Duration.between(now, w.oldest().plus(RESET_WINDOW));
          }
          if (repo.setTempPassword(p.orgId(), userId, hash, now.plus(TEMP_TTL), now) != 1) {
            return "NOT_FOUND";
          }
          repo.recordReset(p.orgId(), Uuid7.next(), p.userId(), userId, classId, now);
          // Dev-S3E5-08 (BR-S3E5-04): phát TRONG giao dịch; người nghe đồng bộ ghi AuditEvent
          // PASSWORD_RESET cùng giao dịch, ghi lỗi thì đổi mật khẩu quay lui trọn.
          events.publish(
              new StudentPasswordResetEvent(
                  Uuid7.next(), p.orgId(), now, p.userId(), classId, userId));
          return "OK";
        });
    if ("NOT_FOUND".equals(out)) {
      throw denials.notFound(p, "student_password");
    }
    if (out instanceof Duration wait) {
      throw AppException.rateLimited(wait.isNegative() ? Duration.ofSeconds(1) : wait, MSG_08);
    }
    revocations.revoked(userId, now);
    return new TempPassword(
        temp.substring(0, 3) + "-" + temp.substring(3, 6) + "-" + temp.substring(6),
        now.plus(TEMP_TTL));
  }

  static String newTemp() {
    StringBuilder b = new StringBuilder(8);
    for (int i = 0; i < 8; i++) {
      b.append(TEMP_ALPHABET.charAt(RANDOM.nextInt(TEMP_ALPHABET.length())));
    }
    return b.toString();
  }

  /** Dạng gõ tay của mật khẩu tạm ("K7M-2QP 9X") về dạng đã băm (8 ký tự thường). */
  public static String normalizeTemp(String raw) {
    return raw == null ? "" : raw.replaceAll("[\\s-]", "").toLowerCase(Locale.ROOT);
  }

  // ------------------------------------------------------------------ học sinh: vào lớp (02)

  private sealed interface JoinOutcome {}

  private record JoinOk(String status, String className) implements JoinOutcome {}

  private record JoinFail(AppException error) implements JoinOutcome {}

  public Joined join(String rawCode) {
    AuthPrincipal p = denials.require(Role.STUDENT, "class_join");
    String code = storeForm(rawCode);
    Instant now = clock.instant();
    JoinOutcome o =
        tx.execute(s -> {
          ClassRepository.AttemptWindow w = repo.attempts(p.orgId(), p.userId(), now);
          if (w.last15m() >= JOIN_SHORT_MAX) {
            return new JoinFail(rateLimited(w.oldest15m().plus(JOIN_SHORT), now));
          }
          if (w.last24h() >= JOIN_DAY_MAX) {
            return new JoinFail(rateLimited(w.oldest24h().plus(JOIN_DAY), now));
          }
          Optional<ClassRepository.JoinTarget> t =
              code == null
                  ? Optional.empty()
                  : repo.byJoinCode(code).filter(x -> x.open() && x.orgId().equals(p.orgId()));
          if (t.isEmpty()) {
            repo.recordFailedAttempt(p.orgId(), Uuid7.next(), p.userId(), now);
            return new JoinFail(AppException.invalidField("code", MSG_01));
          }
          String prev = repo.joinRequest(p.orgId(), t.get().classId(), p.userId(), nameKeyOf(p));
          if ("active".equals(prev)) {
            return new JoinFail(new AppException(ErrorCode.CONFLICT, MSG_03));
          }
          return new JoinOk("pending", t.get().name());
        });
    return switch (o) {
      case JoinOk ok -> new Joined(ok.status(), ok.className(), MSG_02);
      case JoinFail f -> throw f.error();
    };
  }

  private String nameKeyOf(AuthPrincipal p) {
    String name = repo.displayName(p.orgId(), p.userId()).orElse("");
    String key = NameKey.of(name);
    if (key.isEmpty()) {
      key = "hoc sinh";
    }
    return key.length() > 60 ? key.substring(0, 60) : key;
  }

  private static AppException rateLimited(Instant until, Instant now) {
    Duration wait = Duration.between(now, until);
    return AppException.rateLimited(wait.isNegative() ? Duration.ofSeconds(1) : wait, MSG_08);
  }

  public List<MyClass> studentClasses() {
    AuthPrincipal p = denials.require(Role.STUDENT, "class_join");
    return repo.myClasses(p.orgId(), p.userId()).stream()
        .map(c -> new MyClass(c.name(), c.status(), c.archived()))
        .toList();
  }
}
