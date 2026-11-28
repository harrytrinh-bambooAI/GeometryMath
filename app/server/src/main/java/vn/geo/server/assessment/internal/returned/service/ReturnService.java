package vn.geo.server.assessment.internal.returned.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.Row;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Grade;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Item;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Return;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Solution;
import vn.geo.server.assessment.internal.review.service.GradingSupport;
import vn.geo.server.assessment.internal.review.service.ReviewRules;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Trả bài (US-S5E4-07; T-261128-60): chỉ bài Đã duyệt sang Đã trả, bài chưa duyệt ở lại hàng chờ;
 * Trả bài đóng nhận bài nộp mới (BR-S5E4-20, 23); thông báo trong ứng dụng MSG-S5-39 (BR-S5E4-22);
 * thu hồi trong 15 phút: bài về Đã duyệt, lời giải khoá lại, gỡ thông báo (BR-S5E4-24, QS5-31). Học
 * sinh: trước khi trả chỉ thấy trạng thái — không điểm, rubric, đề xuất AI, độ tin cậy, bản đọc chữ
 * (BR-S5E4-21); sau khi trả thấy điểm từng tiêu chí, tổng, nhận xét, ảnh bài mình và lời giải;
 * không thấy bài của bạn (NOT_FOUND). Giờ công bố cố định mở theo đồng hồ CSDL (NFR-GEO-023).
 */
@Service
public class ReturnService {

  private final GradedAssignmentRepository assignments;
  private final GradingRepository repo;
  private final GradingSupport support;
  private final JsonMapper mapper;
  private final Clock clock;

  public ReturnService(
      GradedAssignmentRepository assignments,
      GradingRepository repo,
      GradingSupport support,
      JsonMapper mapper,
      Clock clock) {
    this.assignments = assignments;
    this.repo = repo;
    this.support = support;
    this.mapper = mapper;
    this.clock = clock;
  }

  // ------------------------------------------------------------------ giáo viên

  @Transactional
  public Map<String, Object> returnAll(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    assignments.lock(me.orgId(), assignmentId);
    Row a = support.ownedAssignment(me, assignmentId);
    Instant now = clock.instant();
    UUID batch = Uuid7.next();
    List<UUID> students = repo.returnApproved(me.orgId(), a.id(), batch, now);
    repo.setAssignmentStatus(me.orgId(), a.id(), "returned");
    repo.insertReturn(me.orgId(), batch, a.id(), me.userId(), now, students.size());
    repo.notify(me.orgId(), batch, "graded_returned", ReviewRules.msg39(a.title()), students, now);
    repo.audit(
        me.orgId(),
        Uuid7.next(),
        a.id(),
        null,
        me.userId(),
        "return",
        null,
        null,
        students.size(),
        now);
    Map<String, Object> m = status(me, a.id());
    m.put("returned", students.size());
    m.put("message", "Đã trả " + students.size() + " bài. Có thể thu hồi trong 15 phút.");
    return m;
  }

  @Transactional
  public Map<String, Object> undo(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    assignments.lock(me.orgId(), assignmentId);
    Row a = support.ownedAssignment(me, assignmentId);
    Instant now = clock.instant();
    Return r =
        repo.lastReturn(me.orgId(), a.id())
            .filter(x -> x.undoneAt() == null)
            .orElseThrow(
                () -> new AppException(ErrorCode.CONFLICT, "Không có lượt Trả bài để thu hồi."));
    if (now.isAfter(r.returnedAt().plusSeconds(ReviewRules.UNDO_SECONDS))) {
      throw new AppException(
          ErrorCode.CONFLICT, "Đã quá 15 phút kể từ khi trả bài, không thu hồi được nữa.");
    }
    int n = repo.undoReturn(me.orgId(), r.id());
    repo.unnotify(me.orgId(), r.id());
    repo.markUndone(me.orgId(), r.id(), now);
    if (repo.countReturned(me.orgId(), a.id()) == 0) {
      repo.setAssignmentStatus(me.orgId(), a.id(), "assigned");
    }
    repo.audit(
        me.orgId(), Uuid7.next(), a.id(), null, me.userId(), "return_undo", null, null, n, now);
    Map<String, Object> m = status(me, a.id());
    m.put("undone", n);
    m.put("message", "Đã thu hồi Trả bài. Bài về Đã duyệt.");
    return m;
  }

  public Map<String, Object> status(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    support.ownedAssignment(me, assignmentId);
    return status(me, assignmentId);
  }

  private Map<String, Object> status(AuthPrincipal me, UUID assignmentId) {
    Row a = support.ownedAssignment(me, assignmentId);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("assignmentId", a.id().toString());
    m.put("status", a.status());
    m.put("statusLabel", "returned".equals(a.status()) ? "Đã trả" : "Đã giao");
    int approved = 0;
    int waiting = 0;
    int returned = 0;
    for (Item i : repo.items(me.orgId(), a.id())) {
      switch (i.state()) {
        case "approved" -> approved++;
        case "returned" -> returned++;
        default -> waiting++;
      }
    }
    m.put("approved", approved);
    m.put("waiting", waiting);
    m.put("returnedCount", returned);
    Optional<Return> last = repo.lastReturn(me.orgId(), a.id()).filter(r -> r.undoneAt() == null);
    Instant now = clock.instant();
    m.put("returnedAt", last.map(r -> r.returnedAt().toString()).orElse(null));
    Instant until =
        last.map(r -> r.returnedAt().plusSeconds(ReviewRules.UNDO_SECONDS)).orElse(null);
    m.put("undoUntil", until == null ? null : until.toString());
    m.put("canUndo", until != null && !now.isAfter(until));
    return m;
  }

  // ------------------------------------------------------------------ học sinh

  private static AuthPrincipal student() {
    return Roles.require(Role.STUDENT);
  }

  /** Kết quả bài của chính học sinh (US-S5E4-07-SC1, SC2, FC1, FC2). */
  public Map<String, Object> result(UUID submissionId) {
    AuthPrincipal me = student();
    Item i =
        repo.item(me.orgId(), submissionId)
            .filter(x -> me.userId().equals(x.studentId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Row a =
        assignments
            .one(me.orgId(), i.assignmentId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", i.id().toString());
    m.put("code", i.code());
    m.put("assignmentId", a.id().toString());
    m.put("title", a.title());
    m.put("submittedAt", i.submittedAt().toString());
    boolean open = "returned".equals(i.state());
    if (!open) {
      boolean approved = "approved".equals(i.state());
      m.put("status", approved ? "approved" : "waiting");
      m.put("statusLabel", "Đã nộp");
      m.put("message", approved ? ReviewRules.MSG_WAIT_RETURN : ReviewRules.MSG_WAIT_GRADE);
      m.put("solutionStatus", "LOCKED");
      return m;
    }
    Grade g =
        repo.latest(me.orgId(), i.id()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    m.put("status", "returned");
    m.put("statusLabel", "Đã trả");
    m.put("message", "Thầy cô đã trả bài.");
    List<Map<String, Object>> cs = new ArrayList<>();
    for (JsonNode c : mapper.readTree(g.criteria())) {
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("ord", c.get("ord").asInt());
      x.put("name", c.get("name").asString());
      x.put("maxPoints", c.get("maxPoints").asDouble());
      x.put("points", c.get("points").asDouble());
      cs.add(x);
    }
    m.put("criteria", cs);
    m.put("score", g.total().stripTrailingZeros().doubleValue());
    m.put("scale", a.scale().doubleValue());
    m.put("comment", g.comment());
    m.put("versionId", i.versionId().toString());
    m.put("pages", i.pages());
    m.put("solution", solutionView(repo.solution(me.orgId(), me.userId(), a.id())));
    return m;
  }

  /** Lời giải bài có chấm (US-S5E4-07-FC3): LOCKED tới khi bài của em Đã trả. */
  public Map<String, Object> solution(UUID assignmentId) {
    AuthPrincipal me = student();
    Row a =
        assignments
            .one(me.orgId(), assignmentId)
            .filter(r -> assignments.activeMember(me.orgId(), r.classId(), me.userId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<String, Object> m = solutionView(repo.solution(me.orgId(), me.userId(), a.id()));
    m.put("assignmentId", a.id().toString());
    m.put("title", a.title());
    return m;
  }

  private Map<String, Object> solutionView(Solution s) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("status", s.status());
    switch (s.status()) {
      case "OK" -> {
        m.put("finalAnswer", s.finalAnswer());
        m.put("steps", s.steps() == null ? List.of() : mapper.readTree(s.steps()));
      }
      case "LOCKED" -> m.put("message", ReviewRules.MSG_LOCKED);
      default -> m.put("message", "Bài này chưa có lời giải trên hệ thống.");
    }
    return m;
  }
}
