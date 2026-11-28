package vn.geo.server.aigateway.internal.service.quota;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.aigateway.internal.InstrumentedAiGateway;
import vn.geo.server.aigateway.internal.repo.quota.QuotaRepository;
import vn.geo.server.aigateway.internal.service.killswitch.KillSwitchService;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * Bản cài đặt {@link AiQuota} (US-S5E5-04, BR-S5E5-10…12, 16). Ngày tính theo giờ Việt Nam; hết hạn
 * mức trả RATE_LIMITED kèm Retry-After tới 00:00 hôm sau.
 */
@Service
public class AiQuotaService implements AiQuota {

  /** MSG-S5-60. */
  public static final String MSG_60 =
      "Lớp %s đã dùng hết %d lượt AI hôm nay. Lượt mới mở lúc 00:00.";

  /** Học sinh hết 30 lượt hỏi đáp của riêng mình (lớp vẫn còn lượt). */
  public static final String MSG_STUDENT =
      "Em đã dùng hết %d lượt hỏi trợ lý AI hôm nay. Lượt mới mở lúc 00:00.";

  /** Hỏi đáp khi người vận hành tắt khẩn (BR-S5E5-16): xử lý như lớp đang tắt AI. */
  public static final String MSG_KILLED =
      "Trợ lý AI đang tạm tắt. Em vẫn làm bài và xem gợi ý có sẵn.";

  public static final String CLASS = "class";
  public static final String STUDENT = "student";
  public static final String TEACHER = "teacher";

  private final QuotaRepository repo;
  private final KillSwitchService kill;
  private final Clock clock;
  private final Limits limits;

  /** Hạn mức mỗi ngày (QS5-04); đổi được bằng cấu hình {@code geo.s5.quota.*}. */
  public record Limits(int compose, int classQa, int classGrade, long classTokens, int studentQa) {}

  public AiQuotaService(
      QuotaRepository repo,
      KillSwitchService kill,
      Clock clock,
      @Value("${geo.s5.quota.compose:30}") int compose,
      @Value("${geo.s5.quota.class-qa:300}") int classQa,
      @Value("${geo.s5.quota.class-grade:200}") int classGrade,
      @Value("${geo.s5.quota.class-tokens:2000000}") long classTokens,
      @Value("${geo.s5.quota.student-qa:30}") int studentQa) {
    this.repo = repo;
    this.kill = kill;
    this.clock = clock;
    this.limits = new Limits(compose, classQa, classGrade, classTokens, studentQa);
  }

  public Limits limits() {
    return limits;
  }

  public LocalDate today() {
    return ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM).toLocalDate();
  }

  Duration untilMidnight() {
    ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM);
    return Duration.between(
        now, now.toLocalDate().plusDays(1).atStartOfDay(TimeProperties.VIETNAM));
  }

  @Override
  public boolean killed(String task) {
    return kill.killed(task);
  }

  @Override
  public boolean assistantOn(UUID orgId, UUID classId, String task) {
    return !kill.killed(task) && repo.classSwitch(orgId, classId).assistant();
  }

  @Override
  public boolean gradingOn(UUID orgId, UUID classId, String task) {
    return !kill.killed(task) && repo.classSwitch(orgId, classId).aiGrading();
  }

  @Override
  public void takeComposeTurn(UUID orgId, UUID teacherId, UUID classId, String task) {
    LocalDate day = today();
    long used =
        repo.lessonTurns(orgId, teacherId, day)
            + repo.counter(orgId, TEACHER, teacherId, "compose", day);
    if (used >= limits.compose()) {
      if (classId != null) {
        repo.add(orgId, CLASS, classId, "blocked", day, 1);
      }
      throw AppException.rateLimited(untilMidnight(), MSG_COMPOSE);
    }
    repo.add(orgId, TEACHER, teacherId, "compose", day, 1);
    if (classId != null) {
      repo.add(orgId, CLASS, classId, "turn." + task, day, 1);
    }
  }

  /**
   * Một lượt hỏi đáp của học sinh trong lớp (BR-S5E5-12): kiểm tắt khẩn, lượt của lớp (300), của em
   * (30); nhận thì tính lượt ngay. Không nhận: ném lỗi, ghi một lượt bị chặn của lớp.
   */
  public void takeStudentQa(
      UUID orgId, UUID classId, String className, UUID studentId, String lane) {
    LocalDate day = today();
    if (kill.killed("kb_qa", lane)) {
      repo.add(orgId, CLASS, classId, "blocked", day, 1);
      throw new AppException(ErrorCode.FORBIDDEN, MSG_KILLED);
    }
    if (repo.counter(orgId, CLASS, classId, "qa", day) >= limits.classQa()) {
      repo.add(orgId, CLASS, classId, "blocked", day, 1);
      throw AppException.rateLimited(
          untilMidnight(), MSG_60.formatted(className, limits.classQa()));
    }
    if (repo.counter(orgId, STUDENT, studentId, "qa", day) >= limits.studentQa()) {
      repo.add(orgId, CLASS, classId, "blocked", day, 1);
      throw AppException.rateLimited(untilMidnight(), MSG_STUDENT.formatted(limits.studentQa()));
    }
    long n = repo.add(orgId, CLASS, classId, "qa", day, 1);
    repo.add(orgId, STUDENT, studentId, "qa", day, 1);
    repo.add(orgId, CLASS, classId, "turn.kb_qa", day, 1);
    warnAt80(orgId, classId, day, "qa", n, limits.classQa());
  }

  /** Lớp của bài học sinh đang hỏi (bài tự học, bài không thuộc lớp của em: rỗng). */
  public record ClassRef(UUID id, String name) {}

  public java.util.Optional<ClassRef> classOfAsk(UUID orgId, UUID studentId, UUID assignmentId) {
    return repo.classOfAssignment(orgId, studentId, assignmentId)
        .flatMap(c -> repo.classInfo(orgId, c))
        .map(c -> new ClassRef(c.id(), c.name()));
  }

  @Override
  public boolean takeStudentAsk(UUID orgId, UUID classId, UUID studentId) {
    if (classId == null) {
      return false;
    }
    String name = repo.classInfo(orgId, classId).map(c -> c.name()).orElse(null);
    if (name == null) {
      return false;
    }
    takeStudentQa(orgId, classId, name, studentId, InstrumentedAiGateway.currentLane());
    return true;
  }

  /** Cộng token thật các lượt gọi AI của {@code userId} từ {@code since} vào trần của lớp. */
  @Override
  public void addTokensSince(UUID orgId, UUID classId, UUID userId, java.time.Instant since) {
    addTokens(orgId, classId, repo.tokensSince(orgId, userId, since));
  }

  @Override
  public void addTokens(UUID orgId, UUID classId, long tokens) {
    if (tokens <= 0) {
      return;
    }
    LocalDate day = today();
    long n = repo.add(orgId, CLASS, classId, "tokens", day, tokens);
    warnAt80(orgId, classId, day, "tokens", n, limits.classTokens());
  }

  @Override
  public Admission admitGrading(UUID orgId, UUID classId, String task) {
    LocalDate day = today();
    if (!gradingOn(orgId, classId, task)) {
      return new Admission(false, REASON_GRADE_OFF);
    }
    if (repo.counter(orgId, CLASS, classId, "grade", day) >= limits.classGrade()) {
      repo.add(orgId, CLASS, classId, "blocked", day, 1);
      return new Admission(false, REASON_GRADE_QUOTA);
    }
    long n = repo.add(orgId, CLASS, classId, "grade", day, 1);
    repo.add(orgId, CLASS, classId, "turn." + task, day, 1);
    warnAt80(orgId, classId, day, "grade", n, limits.classGrade());
    return new Admission(true, null);
  }

  @Override
  public void takeOcrRetry(UUID orgId, UUID submissionId) {
    if (repo.incOcr(orgId, submissionId) < 0) {
      throw AppException.rateLimited(
          untilMidnight(), "Bài này đã đọc lại 3 lần. Thầy cô chấm tay hoặc nhờ em chụp lại.");
    }
  }

  @Override
  public UUID registerJob(UUID orgId, UUID classId, String task, UUID refId) {
    return repo.insertJob(orgId, classId, task, refId);
  }

  @Override
  public boolean jobActive(UUID orgId, UUID jobId) {
    return repo.jobStatus(orgId, jobId).map("pending"::equals).orElse(false);
  }

  @Override
  public void finishJob(UUID orgId, UUID jobId) {
    repo.finishJob(orgId, jobId);
  }

  /** Đạt 80 % hạn mức hoặc trần token: giáo viên chủ lớp nhận MỘT cảnh báo trong ngày (QS5-04). */
  private void warnAt80(
      UUID orgId, UUID classId, LocalDate day, String kind, long used, long limit) {
    if (used * 5 < limit * 4) {
      return;
    }
    if (!repo.warnOnce(orgId, classId, day, kind)) {
      return;
    }
    repo.classInfo(orgId, classId)
        .ifPresent(c ->
            repo.notifyTeacher(
                orgId,
                c.teacherId(),
                switch (kind) {
                  case "tokens" -> "Lớp " + c.name() + " đã dùng 80 % trần token AI hôm nay.";
                  case "grade" -> "Lớp " + c.name() + " đã dùng 80 % lượt AI chấm hôm nay.";
                  default -> "Lớp " + c.name() + " đã dùng 80 % lượt hỏi trợ lý AI hôm nay.";
                }));
  }
}
