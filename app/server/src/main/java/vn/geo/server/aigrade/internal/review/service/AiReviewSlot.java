package vn.geo.server.aigrade.internal.review.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.aigrade.api.AiGradeSignals;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.CriterionRow;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Proposal;
import vn.geo.server.aigrade.internal.review.repo.AiReviewRepository;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository.SubInfo;
import vn.geo.server.aigrade.internal.shadow.service.ShadowPolicy;
import vn.geo.server.aigrade.internal.shadow.service.ShadowService;
import vn.geo.server.assessment.api.review.AiProposalSlot;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.RequestContext;

/**
 * Màn chấm có đề xuất AI (US-S5E6-04) và chạy song song (US-S5E6-05) cắm vào chấm tay của
 * assessment qua {@link AiProposalSlot}: trạng thái AI của bài (đề xuất, "AI không chấm" kèm lý do
 * MSG-S5-45, "AI đang tắt"), "Nhận cả bài" chỉ khi không cờ (BR-S5E6-11), ghi nhận / sửa / bỏ từng
 * tiêu chí (BR-S5E6-12), cặp so sánh lần duyệt đầu (BR-S5E6-14). Bài song song chưa duyệt chỉ có
 * {@code aiDone} (BR-S5E6-13).
 */
@Component
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class AiReviewSlot implements AiProposalSlot {

  public static final String SOURCE = "AI đề xuất, giáo viên duyệt";
  static final String MSG_FLAGGED = "Bài có cờ: thầy cô xử lý từng tiêu chí trước khi duyệt.";
  static final String MSG_NO_PROPOSAL = "Bài chưa có đề xuất của AI để nhận cả bài.";

  private final ShadowPolicy policy;
  private final ShadowService shadow;
  private final ProposalRepository proposals;
  private final AiReviewRepository repo;
  private final AiQuota quota;
  private final JsonMapper mapper;

  public AiReviewSlot(
      ShadowPolicy policy,
      ShadowService shadow,
      ProposalRepository proposals,
      AiReviewRepository repo,
      AiQuota quota,
      JsonMapper mapper) {
    this.policy = policy;
    this.shadow = shadow;
    this.proposals = proposals;
    this.repo = repo;
    this.quota = quota;
    this.mapper = mapper;
  }

  /** MSG-S5-45. */
  static String msg45(String reason) {
    return "AI không chấm bài này: " + reason + ". Thầy cô chấm tay nhé.";
  }

  /** MSG-S5-49. */
  public static String msg49(String name) {
    return "Đã nhận đề xuất của tiêu chí " + name + ".";
  }

  private Optional<SubInfo> owned(UUID orgId, UUID submissionId) {
    UUID me = RequestContext.principal().userId();
    return policy.info(orgId, submissionId).filter(s -> me.equals(s.teacherId()));
  }

  private List<String> flags(Proposal p) {
    List<String> out = new ArrayList<>();
    if ("no_grade".equals(p.status())) {
      out.add(AiGradeSignals.FLAG_NO_GRADE);
    }
    if (Boolean.TRUE.equals(p.disagree())) {
      out.add(AiGradeSignals.FLAG_DISAGREE);
    }
    for (JsonNode f : mapper.readTree(p.flags())) {
      if (!out.contains(f.asString(""))) {
        out.add(f.asString(""));
      }
    }
    return out;
  }

  @Override
  public Map<String, Object> extras(UUID orgId, UUID submissionId) {
    Optional<SubInfo> o = owned(orgId, submissionId);
    if (o.isEmpty() || "manual".equals(o.get().gradingMode())) {
      return Map.of();
    }
    SubInfo s = o.get();
    Optional<Proposal> p = proposals.latest(orgId, s.id(), s.currentVersion());
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("aiDone", p.isPresent());
    if (policy.hidden(orgId, s)) {
      m.put("shadow", true);
      m.put("message", ShadowPolicy.MSG_47);
      return m; // BR-S5E6-13: không điểm, lý do, độ tin cậy hay cờ suy từ AI
    }
    m.put("shadow", policy.shadow(orgId, s));
    if (p.isEmpty()) {
      boolean off = !quota.gradingOn(orgId, s.classId(), "grade");
      m.put("aiStatus", off ? "off" : "pending");
      m.put("aiStatusLabel", off ? "AI đang tắt" : "Đang chờ AI đề xuất");
      m.put("aiFlags", List.of());
      return m;
    }
    Proposal x = p.get();
    List<String> flags = flags(x);
    boolean noGrade = "no_grade".equals(x.status());
    m.put("aiStatus", x.status());
    m.put("aiStatusLabel", noGrade ? AiGradeSignals.FLAG_NO_GRADE : "AI đề xuất");
    m.put("aiFlags", flags);
    if (noGrade) {
      m.put("reason", x.reason());
      m.put("message", msg45(x.reason()));
      m.put("retryable", true);
    }
    m.put("acceptAll", !noGrade && flags.isEmpty() && !policy.shadow(orgId, s));
    List<AiReviewRepository.Action> acts = repo.latest(orgId, s.id());
    if (!acts.isEmpty()) {
      m.put("source", SOURCE);
      m.put("actions", actions(acts));
    }
    return m;
  }

  @Override
  public Map<Integer, BigDecimal> acceptAll(UUID orgId, UUID submissionId) {
    SubInfo s = owned(orgId, submissionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (policy.hidden(orgId, s)) {
      throw new AppException(ErrorCode.CONFLICT, ShadowPolicy.MSG_47);
    }
    Proposal p =
        proposals
            .latest(orgId, s.id(), s.currentVersion())
            .filter(x -> "proposed".equals(x.status()))
            .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, MSG_NO_PROPOSAL));
    if (!flags(p).isEmpty()) {
      throw new AppException(ErrorCode.CONFLICT, MSG_FLAGGED);
    }
    Map<Integer, BigDecimal> out = new LinkedHashMap<>();
    for (CriterionRow c : proposals.criteriaOf(orgId, p.id())) {
      out.put(c.ord(), c.points());
    }
    return out;
  }

  @Override
  public Map<String, Object> approved(
      UUID orgId,
      UUID teacherId,
      UUID submissionId,
      int seq,
      Map<Integer, BigDecimal> points,
      Map<Integer, Action> actions) {
    Optional<SubInfo> o = owned(orgId, submissionId);
    if (o.isEmpty() || "manual".equals(o.get().gradingMode())) {
      return Map.of();
    }
    SubInfo s = o.get();
    Map<String, Object> m = new LinkedHashMap<>();
    if (policy.shadow(orgId, s)) {
      if (seq == 1) {
        shadow.recordFirstApproval(orgId, s, points);
      }
      m.put("shadow", true);
      m.put("comparisonReady", true);
      return m;
    }
    Optional<Proposal> p =
        proposals
            .latest(orgId, s.id(), s.currentVersion())
            .filter(x -> "proposed".equals(x.status()));
    if (p.isEmpty()) {
      return m;
    }
    for (CriterionRow c : proposals.criteriaOf(orgId, p.get().id())) {
      BigDecimal t = points.get(c.ord());
      if (t == null) {
        continue;
      }
      Action a = actions.get(c.ord());
      if (a == null) {
        a = t.compareTo(c.points()) == 0 ? Action.ACCEPT : Action.EDIT;
      }
      repo.insert(
          orgId,
          Uuid7.next(),
          s.id(),
          p.get().id(),
          s.studentId(),
          s.academicYear(),
          seq,
          c.ord(),
          c.name(),
          c.max(),
          a.name().toLowerCase(Locale.ROOT),
          c.points(),
          t);
    }
    m.put("source", SOURCE);
    m.put("actions", actions(repo.latest(orgId, s.id())));
    return m;
  }

  private static List<Map<String, Object>> actions(List<AiReviewRepository.Action> acts) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (AiReviewRepository.Action a : acts) {
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("ord", a.ord());
      x.put("name", a.name());
      x.put("action", a.action());
      x.put("aiPoints", a.aiPoints().stripTrailingZeros());
      x.put("teacherPoints", a.teacherPoints().stripTrailingZeros());
      out.add(x);
    }
    return out;
  }
}
