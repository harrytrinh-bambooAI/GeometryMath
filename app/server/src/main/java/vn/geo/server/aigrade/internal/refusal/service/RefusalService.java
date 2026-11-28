package vn.geo.server.aigrade.internal.refusal.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Proposal;
import vn.geo.server.aigrade.internal.proposal.service.ProposalService;
import vn.geo.server.aigrade.internal.refusal.repo.RefusalRepository;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository.SubInfo;
import vn.geo.server.aigrade.internal.shadow.service.ShadowGrader;
import vn.geo.server.aigrade.internal.shadow.service.ShadowPolicy;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * "AI không chấm" (US-S5E6-02): lý do lưu ở đề xuất (BR-S5E6-05) và hiện cho giáo viên qua màn
 * chấm; bài vẫn Chờ chấm, chấm tay được (BR-S5E6-06). "Thử chấm lại": gỡ lượt "AI không chấm" của
 * phiên bản chấm rồi xếp chấm nền lại (cùng hạn mức, cùng luật). Chỉ giáo viên chủ lớp.
 */
@Service
public class RefusalService {

  private static final Logger log = LoggerFactory.getLogger(RefusalService.class);

  static final String MSG_NOT_REFUSED = "Bài này không ở trạng thái \"AI không chấm\".";
  static final String MSG_RETRY = "Đã gửi AI chấm lại bài này.";

  private final ShadowPolicy policy;
  private final ProposalRepository proposals;
  private final RefusalRepository repo;
  private final ProposalService service;
  private final ShadowGrader shadow;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public RefusalService(
      ShadowPolicy policy,
      ProposalRepository proposals,
      RefusalRepository repo,
      ProposalService service,
      ShadowGrader shadow) {
    this.policy = policy;
    this.proposals = proposals;
    this.repo = repo;
    this.service = service;
    this.shadow = shadow;
  }

  public Map<String, Object> retry(UUID submissionId) {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    SubInfo s =
        policy
            .info(me.orgId(), submissionId)
            .filter(x -> me.userId().equals(x.teacherId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (policy.hidden(me.orgId(), s)) {
      throw new AppException(ErrorCode.CONFLICT, ShadowPolicy.MSG_47);
    }
    Proposal p =
        proposals
            .latest(me.orgId(), s.id(), s.currentVersion())
            .filter(x -> "no_grade".equals(x.status()))
            .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, MSG_NOT_REFUSED));
    repo.deleteNoGrade(me.orgId(), p.id());
    UUID org = me.orgId();
    if ("parallel".equals(s.gradingMode())) {
      shadow.schedule(org, s.id());
    } else {
      executor.submit(() -> {
        try {
          service.runNow(org, s.id());
        } catch (RuntimeException e) {
          log.warn("Thử chấm lại lỗi: {}", e.toString());
        }
      });
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("submissionId", s.id().toString());
    m.put("status", "pending");
    m.put("statusLabel", "Đang chờ AI đề xuất");
    m.put("message", MSG_RETRY);
    return m;
  }
}
