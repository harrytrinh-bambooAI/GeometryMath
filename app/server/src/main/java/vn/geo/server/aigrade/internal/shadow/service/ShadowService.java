package vn.geo.server.aigrade.internal.shadow.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.CriterionRow;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Proposal;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.RubricRow;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository.Pair;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository.SubInfo;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Cặp so sánh chạy song song (US-S5E6-05, BR-S5E6-14): lưu (điểm AI, điểm giáo viên lần duyệt đầu)
 * theo tiêu chí lúc Duyệt; sửa điểm sau đó không đổi cặp; sau khi duyệt mới xem được so sánh.
 */
@Service
public class ShadowService {

  private final ShadowRepository repo;
  private final ShadowPolicy policy;
  private final ProposalRepository proposals;

  public ShadowService(ShadowRepository repo, ShadowPolicy policy, ProposalRepository proposals) {
    this.repo = repo;
    this.policy = policy;
    this.proposals = proposals;
  }

  /** Ghi cặp của lần duyệt đầu (bài song song); đã có cặp thì giữ nguyên. */
  public void recordFirstApproval(UUID orgId, SubInfo s, Map<Integer, BigDecimal> teacher) {
    if (!repo.pairs(orgId, s.id()).isEmpty()) {
      return;
    }
    Map<Integer, BigDecimal> ai = new HashMap<>();
    Optional<Proposal> p = proposals.latest(orgId, s.id(), s.currentVersion());
    UUID pid = null;
    if (p.isPresent() && "proposed".equals(p.get().status())) {
      pid = p.get().id();
      for (CriterionRow c : proposals.criteriaOf(orgId, pid)) {
        ai.put(c.ord(), c.points());
      }
    }
    boolean sampled = policy.sampled(orgId, s);
    for (RubricRow r : proposals.criteria(orgId, s.rubricVersionId())) {
      BigDecimal t = teacher.get(r.ord());
      if (t == null) {
        continue;
      }
      BigDecimal a = ai.get(r.ord());
      repo.insertPair(
          orgId,
          Uuid7.next(),
          s,
          sampled,
          r.ord(),
          r.name(),
          r.max(),
          t,
          a,
          a == null ? null : pid);
    }
  }

  /** So sánh AI – giáo viên của một bài đã duyệt (giáo viên chủ lớp). */
  public Map<String, Object> comparison(UUID submissionId) {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    SubInfo s =
        policy
            .info(me.orgId(), submissionId)
            .filter(x -> me.userId().equals(x.teacherId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!s.approved()) {
      throw new AppException(ErrorCode.CONFLICT, ShadowPolicy.MSG_47);
    }
    List<Pair> pairs = repo.pairs(me.orgId(), s.id());
    // AI chấm xong sau lúc giáo viên duyệt: điền điểm AI một lần
    if (pairs.stream().anyMatch(x -> x.aiPoints() == null)) {
      Optional<Proposal> p = proposals.latest(me.orgId(), s.id(), s.currentVersion());
      if (p.isPresent() && "proposed".equals(p.get().status())) {
        for (CriterionRow c : proposals.criteriaOf(me.orgId(), p.get().id())) {
          repo.fillAi(me.orgId(), s.id(), c.ord(), c.points(), p.get().id());
        }
        pairs = repo.pairs(me.orgId(), s.id());
      }
    }
    List<Map<String, Object>> items = new ArrayList<>();
    int differ = 0;
    for (Pair x : pairs) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("ord", x.ord());
      m.put("name", x.name());
      m.put("maxPoints", x.max().stripTrailingZeros());
      m.put("aiPoints", x.aiPoints() == null ? null : x.aiPoints().stripTrailingZeros());
      m.put("teacherPoints", x.teacherPoints().stripTrailingZeros());
      boolean d = x.aiPoints() != null && x.aiPoints().compareTo(x.teacherPoints()) != 0;
      m.put("differs", d);
      if (d) {
        differ++;
      }
      items.add(m);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("submissionId", s.id().toString());
    out.put("criteria", items);
    out.put("pairs", items.size());
    out.put("differing", differ);
    out.put("aiDone", pairs.stream().allMatch(x -> x.aiPoints() != null) && !pairs.isEmpty());
    return out;
  }
}
