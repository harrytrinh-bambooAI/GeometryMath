package vn.geo.server.assessment.internal.assign.service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.assessment.api.ClassAiGateQuery;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.ClassRow;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.NewRow;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.Row;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Head;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Version;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.BankAccess;
import vn.geo.server.exam.api.ExamSetGuard;

/**
 * Giao bài có chấm (US-S5E4-02; BR-S5E4-04…06; QS5-16, QS5-17). Giáo viên chỉ giao cho lớp mình chủ
 * (lớp khác NOT_FOUND). Chế độ chấm: Chấm tay (mặc định) · Chạy song song (công tắc AI chấm của lớp
 * đang bật, US-S5E5-04) · AI đề xuất (lớp đạt cổng US-S5E6-06 qua {@link ClassAiGateQuery}). Sau
 * khi đã có bài nộp: hạn chỉ được kéo dài, không đổi rubric. Phía học sinh không thấy rubric chi
 * tiết, hướng dẫn chấm hay lời giải.
 */
@Service
public class GradedAssignmentService {

  static final Set<String> MODES = Set.of("manual", "parallel", "ai_proposal");
  static final Set<String> RELEASE = Set.of("on_return", "at_time");
  static final String GATE_DETAIL = "lớp chưa đạt ngưỡng chạy song song";

  /** Thân tạo bài. */
  public record AssignInput(
      String title,
      UUID classId,
      UUID problemVersionId,
      UUID examSetId,
      String examCode,
      UUID rubricId,
      Integer rubricVersion,
      String dueAt,
      Boolean allowLate,
      String gradingMode,
      String releaseMode,
      String releaseAt) {}

  /** Thân sửa bài: chỉ trường có mặt được đổi. */
  public record AssignPatch(
      String dueAt,
      UUID rubricId,
      Integer rubricVersion,
      Boolean allowLate,
      String releaseMode,
      String releaseAt) {}

  private final GradedAssignmentRepository repo;
  private final RubricRepository rubrics;
  private final BankAccess bank;
  private final ExamSetGuard exams;
  private final AiQuota ai;
  private final ClassAiGateQuery gate;
  private final Clock clock;

  public GradedAssignmentService(
      GradedAssignmentRepository repo,
      RubricRepository rubrics,
      BankAccess bank,
      ExamSetGuard exams,
      AiQuota ai,
      ClassAiGateQuery gate,
      Clock clock) {
    this.repo = repo;
    this.rubrics = rubrics;
    this.bank = bank;
    this.exams = exams;
    this.ai = ai;
    this.gate = gate;
    this.clock = clock;
  }

  @Transactional
  public Map<String, Object> create(AssignInput in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (in == null || in.classId() == null) {
      throw AppException.invalidField("classId", "Chọn lớp.");
    }
    ClassRow cls = ownedClass(me, in.classId());
    String title = in.title() == null ? "" : in.title().strip();
    if (title.isEmpty() || title.length() > 120) {
      throw AppException.invalidField("title", "Tên bài từ 1 đến 120 ký tự.");
    }
    if ((in.problemVersionId() == null) == (in.examSetId() == null)) {
      throw AppException.invalidField(
          "problemVersionId", "Chọn một đề kho hoặc một bộ đề đã duyệt.");
    }
    String examCode = null;
    if (in.problemVersionId() != null) {
      bank.requireVisible(in.problemVersionId());
    } else {
      exams.requireAssignable(in.examSetId());
      examCode = in.examCode() == null ? "" : in.examCode().strip();
      if (!examCode.matches("[0-9]{3}")) {
        throw AppException.invalidField("examCode", "Chọn một mã đề của bộ đề.");
      }
    }
    Version rv = rubricVersion(me, in.rubricId(), in.rubricVersion());
    Instant due = instant("dueAt", in.dueAt(), true);
    if (!due.isAfter(clock.instant())) {
      throw AppException.invalidField("dueAt", "Hạn nộp phải sau thời điểm hiện tại.");
    }
    String mode = in.gradingMode() == null ? "manual" : in.gradingMode();
    if (!MODES.contains(mode)) {
      throw AppException.invalidField("gradingMode", "Chế độ chấm không hợp lệ.");
    }
    String rmode = in.releaseMode() == null ? "on_return" : in.releaseMode();
    Instant releaseAt = release(rmode, in.releaseAt());
    if ("ai_proposal".equals(mode) && !gate.aiProposalReady(me.orgId(), cls.id())) {
      throw new GradingConflict(
          "Lớp chưa đạt ngưỡng chạy song song nên chưa chọn được \"AI đề xuất\".",
          "gradingMode",
          GATE_DETAIL);
    }
    if ("parallel".equals(mode) && !ai.gradingOn(me.orgId(), cls.id(), "grade")) {
      throw new GradingConflict(
          "AI chấm của lớp đang tắt nên chưa chọn được \"Chạy song song\".",
          "gradingMode",
          AiQuota.REASON_GRADE_OFF);
    }
    UUID id =
        repo.insert(
            me.orgId(),
            new NewRow(
                me.userId(),
                cls.id(),
                repo.nextCode(me.orgId(), me.userId()),
                title,
                in.problemVersionId(),
                in.examSetId(),
                examCode,
                rv.id(),
                rv.scale(),
                due,
                in.allowLate() == null || in.allowLate(),
                mode,
                rmode,
                releaseAt));
    return teacherView(me, repo.one(me.orgId(), id).orElseThrow());
  }

  @Transactional
  public Map<String, Object> patch(UUID id, AssignPatch p) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    repo.lock(me.orgId(), id);
    Row row = owned(me, id);
    if (p == null) {
      return teacherView(me, row);
    }
    boolean submitted = repo.submissions(me.orgId(), id) > 0;
    if (p.dueAt() != null) {
      Instant due = instant("dueAt", p.dueAt(), true);
      if (submitted && due.isBefore(row.dueAt())) {
        throw new AppException(
            ErrorCode.CONFLICT, "Bài đã có bài nộp: hạn nộp chỉ được kéo dài, không rút ngắn.");
      }
      if (!submitted && !due.isAfter(clock.instant())) {
        throw AppException.invalidField("dueAt", "Hạn nộp phải sau thời điểm hiện tại.");
      }
    }
    Version rv = null;
    if (p.rubricId() != null || p.rubricVersion() != null) {
      rv =
          rubricVersion(
              me, p.rubricId() == null ? row.rubricId() : p.rubricId(), p.rubricVersion());
      if (submitted && !rv.id().equals(row.rubricVersionId())) {
        throw new AppException(ErrorCode.CONFLICT, "Bài đã có bài nộp: không đổi rubric được nữa.");
      }
    }
    String rmode = p.releaseMode() == null ? row.releaseMode() : p.releaseMode();
    Instant releaseAt =
        p.releaseMode() == null && p.releaseAt() == null
            ? row.releaseAt()
            : release(rmode, p.releaseAt());
    if (p.dueAt() != null) {
      repo.updateDue(me.orgId(), id, instant("dueAt", p.dueAt(), true));
    }
    if (rv != null && !rv.id().equals(row.rubricVersionId())) {
      repo.updateRubric(me.orgId(), id, rv.id(), rv.scale());
    }
    repo.updatePolicy(
        me.orgId(), id, p.allowLate() == null ? row.allowLate() : p.allowLate(), rmode, releaseAt);
    return teacherView(me, repo.one(me.orgId(), id).orElseThrow());
  }

  public Map<String, Object> get(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return teacherView(me, owned(me, id));
  }

  public Map<String, Object> list(UUID classId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Map<String, Object>> items = new ArrayList<>();
    for (Row r : repo.ofTeacher(me.orgId(), me.userId(), classId)) {
      items.add(teacherView(me, r));
    }
    return Map.of("items", items);
  }

  /** "Bài cần nộp" của học sinh: không rubric chi tiết, không hướng dẫn chấm, không lời giải. */
  public Map<String, Object> studentList() {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    List<Map<String, Object>> items = new ArrayList<>();
    for (Row r : repo.ofStudent(me.orgId(), me.userId())) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", r.id());
      m.put("code", r.code());
      m.put("title", r.title());
      m.put("className", r.className());
      m.put("dueAt", r.dueAt().toString());
      m.put("allowLate", r.allowLate());
      m.put("scale", r.scale().doubleValue());
      m.put(
          "statement",
          r.problemVersionId() == null
              ? null
              : repo.statement(me.orgId(), r.problemVersionId()).orElse(null));
      m.put("examCode", r.examCode());
      m.put("status", r.status());
      items.add(m);
    }
    return Map.of("items", items);
  }

  // ------------------------------------------------------------------ nội bộ

  private ClassRow ownedClass(AuthPrincipal me, UUID classId) {
    return repo.classOf(me.orgId(), classId)
        .filter(c -> c.teacherId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private Row owned(AuthPrincipal me, UUID id) {
    return repo.one(me.orgId(), id)
        .filter(r -> r.teacherId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private Version rubricVersion(AuthPrincipal me, UUID rubricId, Integer version) {
    if (rubricId == null) {
      throw AppException.invalidField("rubricId", "Chọn rubric.");
    }
    Head h =
        rubrics
            .owned(me.orgId(), me.userId(), rubricId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    int v = version == null ? h.currentVersion() : version;
    return rubrics
        .version(me.orgId(), rubricId, v)
        .orElseThrow(
            () -> AppException.invalidField("rubricVersion", "Phiên bản rubric không có."));
  }

  private Instant release(String rmode, String releaseAt) {
    if (!RELEASE.contains(rmode)) {
      throw AppException.invalidField("releaseMode", "Lịch công bố không hợp lệ.");
    }
    if ("on_return".equals(rmode)) {
      return null;
    }
    return instant("releaseAt", releaseAt, true);
  }

  private static Instant instant(String field, String raw, boolean required) {
    if (raw == null || raw.isBlank()) {
      if (required) {
        throw AppException.invalidField(field, "Chọn ngày giờ.");
      }
      return null;
    }
    try {
      return java.time.OffsetDateTime.parse(raw.strip()).toInstant();
    } catch (DateTimeParseException e) {
      throw AppException.invalidField(field, "Ngày giờ không hợp lệ.");
    }
  }

  private static String modeLabel(String mode) {
    return switch (mode) {
      case "parallel" -> "Chạy song song";
      case "ai_proposal" -> "AI đề xuất";
      default -> "Chấm tay";
    };
  }

  private Map<String, Object> teacherView(AuthPrincipal me, Row r) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", r.id());
    m.put("code", r.code());
    m.put("title", r.title());
    m.put("classId", r.classId());
    m.put("className", r.className());
    m.put("problemVersionId", r.problemVersionId());
    m.put("examSetId", r.examSetId());
    m.put("examCode", r.examCode());
    Map<String, Object> rb = new LinkedHashMap<>();
    rb.put("id", r.rubricId());
    rb.put("code", r.rubricCode());
    rb.put("version", r.rubricVersion());
    rb.put("versionId", r.rubricVersionId());
    rb.put("name", r.rubricName());
    m.put("rubric", rb);
    m.put("scale", r.scale().doubleValue());
    m.put("dueAt", r.dueAt().toString());
    m.put("allowLate", r.allowLate());
    m.put("gradingMode", r.gradingMode());
    m.put("gradingModeLabel", modeLabel(r.gradingMode()));
    m.put("releaseMode", r.releaseMode());
    m.put("releaseAt", r.releaseAt() == null ? null : r.releaseAt().toString());
    m.put("status", r.status());
    m.put("statusLabel", "returned".equals(r.status()) ? "Đã trả" : "Đã giao");
    m.put("submissions", repo.submissions(me.orgId(), r.id()));
    return m;
  }
}
