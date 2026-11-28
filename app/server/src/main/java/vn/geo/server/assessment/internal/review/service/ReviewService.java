package vn.geo.server.assessment.internal.review.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.Row;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Audit;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Grade;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Item;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Criterion;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Chấm tay theo rubric trên web C5-05 (US-S5E4-06; T-261128-60): ảnh bên trái, rubric bên phải;
 * điểm mỗi tiêu chí 0…tối đa bước 0,25, nhận xét ≤ 1 000 ký tự (BR-S5E4-17); Duyệt tạo {@code
 * approved_grade} + AuditEvent (BR-S5E4-18); sửa sau duyệt tạo bản mới, ghi trước/sau; khoá lạc
 * quan theo {@code version} của bài nộp (BR-S5E4-19). Sửa điểm bài Đã trả báo học sinh
 * (BR-S5E4-24). Chỉ giáo viên chủ lớp: giáo viên khác NOT_FOUND, vai khác FORBIDDEN. Không có đề
 * xuất AI ở đây.
 */
@Service
public class ReviewService {

  public record CriterionScore(Integer ord, BigDecimal points) {}

  public record GradeRequest(Integer version, List<CriterionScore> criteria, String comment) {}

  private final GradingRepository repo;
  private final GradingSupport support;
  private final RubricRepository rubrics;
  private final JsonMapper mapper;
  private final Clock clock;

  public ReviewService(
      GradingRepository repo,
      GradingSupport support,
      RubricRepository rubrics,
      JsonMapper mapper,
      Clock clock) {
    this.repo = repo;
    this.support = support;
    this.rubrics = rubrics;
    this.mapper = mapper;
    this.clock = clock;
  }

  /** Màn chấm một bài: thông tin bài, rubric, điểm hiện hành, bài trước/kế trong hàng chờ. */
  public Map<String, Object> get(UUID submissionId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Item i = support.ownedItem(me, submissionId);
    Row a = support.ownedAssignment(me, i.assignmentId());
    if (!"approved".equals(i.state()) && !"returned".equals(i.state())) {
      repo.markOpened(me.orgId(), i.id(), me.userId()); // KPI (g): giờ mở màn chấm (T-261128-68)
    }
    Map<String, Object> m = GradingSupport.view(i);
    m.put("versionId", i.versionId().toString());
    m.put("submissionVersion", i.version());
    m.put("assignment", assignment(a));
    m.put("criteria", criteria(me, a.rubricVersionId()));
    repo.latest(me.orgId(), i.id()).ifPresent(g -> m.put("grade", grade(g)));
    List<Item> queue = queue(me, i.assignmentId());
    int at = -1;
    for (int k = 0; k < queue.size(); k++) {
      if (queue.get(k).id().equals(i.id())) {
        at = k;
      }
    }
    m.put("prevSubmissionId", at > 0 ? queue.get(at - 1).id().toString() : null);
    m.put("nextSubmissionId", next(queue, i.id(), at).map(x -> x.id().toString()).orElse(null));
    return m;
  }

  private List<Item> queue(AuthPrincipal me, UUID assignmentId) {
    List<Item> q =
        new ArrayList<>(
            repo.items(me.orgId(), assignmentId).stream().filter(GradingSupport::queued).toList());
    q.sort(GradingSupport.ORDER);
    return q;
  }

  private static Optional<Item> next(List<Item> queue, UUID current, int at) {
    if (at >= 0 && at + 1 < queue.size()) {
      return Optional.of(queue.get(at + 1));
    }
    return queue.stream().filter(x -> !x.id().equals(current)).findFirst();
  }

  private static Map<String, Object> assignment(Row a) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", a.id().toString());
    m.put("code", a.code());
    m.put("title", a.title());
    m.put("classId", a.classId().toString());
    m.put("className", a.className());
    m.put("scale", a.scale().doubleValue());
    m.put("rubricCode", a.rubricCode());
    m.put("rubricVersion", a.rubricVersion());
    m.put("rubricName", a.rubricName());
    m.put("status", a.status());
    return m;
  }

  private List<Map<String, Object>> criteria(AuthPrincipal me, UUID rubricVersionId) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (Criterion c : rubrics.criteria(me.orgId(), rubricVersionId)) {
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("ord", c.ord());
      x.put("name", c.name());
      x.put("description", c.description());
      x.put("maxPoints", c.maxPoints().doubleValue());
      out.add(x);
    }
    return out;
  }

  Map<String, Object> grade(Grade g) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("seq", g.seq());
    m.put("total", g.total().stripTrailingZeros().doubleValue());
    m.put("comment", g.comment());
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
    m.put("approvedBy", g.approvedBy().toString());
    m.put("approvedAt", g.approvedAt().toString());
    m.put("submissionVersion", g.submissionVersion());
    return m;
  }

  /** Duyệt (lần đầu) hoặc sửa điểm đã duyệt. */
  @Transactional
  public Map<String, Object> approve(UUID submissionId, GradeRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Item i = support.ownedItem(me, submissionId);
    Row a = support.ownedAssignment(me, i.assignmentId());
    if (req == null || req.version() == null) {
      throw AppException.invalidField("version", "Thiếu phiên bản bản ghi. Tải lại bài.");
    }
    List<Criterion> crit = rubrics.criteria(me.orgId(), a.rubricVersionId());
    Map<Integer, BigDecimal> given = new HashMap<>();
    if (req.criteria() != null) {
      for (CriterionScore c : req.criteria()) {
        if (c != null && c.ord() != null) {
          given.put(c.ord(), c.points());
        }
      }
    }
    ArrayNode stored = mapper.createArrayNode();
    BigDecimal total = BigDecimal.ZERO;
    for (Criterion c : crit) {
      BigDecimal p = given.get(c.ord());
      if (!ReviewRules.valid(p, c.maxPoints())) {
        throw AppException.invalidField(
            "criteria[" + c.ord() + "]", ReviewRules.msg37(c.name(), c.maxPoints()));
      }
      total = total.add(p);
      stored
          .addObject()
          .put("ord", c.ord())
          .put("name", c.name())
          .put("maxPoints", c.maxPoints())
          .put("points", p.stripTrailingZeros());
    }
    String comment = req.comment() == null ? "" : req.comment().strip();
    if (comment.length() > ReviewRules.COMMENT_MAX) {
      throw AppException.invalidField("comment", "Nhận xét tối đa 1 000 ký tự.");
    }
    if (req.version() != i.rowVersion() || repo.approve(me.orgId(), i.id(), req.version()) == 0) {
      throw new AppException(
          ErrorCode.CONFLICT, "Bài này vừa được lưu ở nơi khác. Tải lại để xem điểm mới nhất.");
    }
    Optional<Grade> prev = repo.latest(me.orgId(), i.id());
    Instant now = clock.instant();
    int seq = prev.map(Grade::seq).orElse(0) + 1;
    String json = mapper.writeValueAsString(stored);
    repo.insertGrade(
        me.orgId(),
        Uuid7.next(),
        i,
        seq,
        a.rubricVersionId(),
        json,
        total,
        comment,
        me.userId(),
        now);
    repo.audit(
        me.orgId(),
        Uuid7.next(),
        a.id(),
        i.id(),
        me.userId(),
        prev.isPresent() ? "edit" : "approve",
        prev.map(g -> snapshot(g.criteria(), g.total(), g.comment())).orElse(null),
        snapshot(json, total, comment),
        1,
        now);
    boolean returned = "returned".equals(i.state());
    if (returned && prev.isPresent()) {
      repo.notify(
          me.orgId(),
          Uuid7.next(),
          "grade_updated",
          ReviewRules.updated(a.title()),
          java.util.Collections.singletonList(i.studentId()),
          now);
    }
    Item after = support.ownedItem(me, i.id());
    Map<String, Object> m = GradingSupport.view(after);
    repo.latest(me.orgId(), i.id()).ifPresent(g -> m.put("grade", grade(g)));
    m.put("message", returned ? "Đã cập nhật điểm. Học sinh được báo." : ReviewRules.MSG_38);
    List<Item> q = queue(me, a.id());
    m.put("nextSubmissionId", q.isEmpty() ? null : q.get(0).id().toString());
    return m;
  }

  private String snapshot(String criteriaJson, BigDecimal total, String comment) {
    ObjectNode o = mapper.createObjectNode();
    o.put("total", total.stripTrailingZeros());
    o.put("comment", comment);
    ArrayNode cs = o.putArray("criteria");
    for (JsonNode c : mapper.readTree(criteriaJson)) {
      cs.addObject()
          .put("ord", c.get("ord").asInt())
          .put("name", c.get("name").asString())
          .set("points", c.get("points"));
    }
    return mapper.writeValueAsString(o);
  }

  /** Nhật ký chấm: Duyệt, sửa (trước/sau từng tiêu chí), người thực hiện, giờ. */
  public Map<String, Object> history(UUID submissionId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Item i = support.ownedItem(me, submissionId);
    List<Map<String, Object>> out = new ArrayList<>();
    for (Audit au : repo.audits(me.orgId(), i.id())) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("action", au.action());
      m.put("actorId", au.actorId().toString());
      m.put("actorName", au.actorName());
      m.put("at", au.at().toString());
      JsonNode b = au.before() == null ? null : mapper.readTree(au.before());
      JsonNode af = au.after() == null ? null : mapper.readTree(au.after());
      m.put("totalBefore", b == null ? null : b.get("total").asDouble());
      m.put("totalAfter", af == null ? null : af.get("total").asDouble());
      List<Map<String, Object>> changes = new ArrayList<>();
      if (af != null) {
        for (JsonNode c : af.get("criteria")) {
          Double before = null;
          if (b != null) {
            for (JsonNode x : b.get("criteria")) {
              if (x.get("ord").asInt() == c.get("ord").asInt()) {
                before = x.get("points").asDouble();
              }
            }
          }
          double now = c.get("points").asDouble();
          if (before == null || before != now) {
            Map<String, Object> ch = new LinkedHashMap<>();
            ch.put("ord", c.get("ord").asInt());
            ch.put("name", c.get("name").asString());
            ch.put("before", before);
            ch.put("after", now);
            changes.add(ch);
          }
        }
      }
      m.put("changes", changes);
      out.add(m);
    }
    return Map.of("items", out);
  }
}
