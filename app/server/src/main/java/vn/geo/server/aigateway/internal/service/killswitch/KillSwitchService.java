package vn.geo.server.aigateway.internal.service.killswitch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.aigateway.internal.repo.killswitch.KillSwitchRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;

/**
 * Tắt khẩn toàn hệ thống theo task AI (BR-S5E5-16, khóa {@code geo.ai.kill.<task>}): chỉ người có
 * vai Vận hành (bảng ai_ops_grant) đổi được; giáo viên, học sinh FORBIDDEN. Đọc thẳng CSDL mỗi lượt
 * (không cache) nên hiệu lực ngay, trong trần 10 giây. Mỗi lần đổi ghi AuditEvent (ai_switch_audit,
 * vai 'operator'). Không theo trường (QS5-02).
 *
 * <p>Chỉ ở profile dev/test: yêu cầu mang tiêu đề làn kiểm thử ({@code X-Geo-Test-Lane}) đổi và đọc
 * công tắc RIÊNG của làn đó (cộng công tắc chung), để E2E chạy song song không tắt AI của ca khác.
 * Prod luôn dùng làn chung ('').
 */
@Service
public class KillSwitchService {

  /** Task tắt được: 8 task chặng 5 + hỏi đáp của học sinh chặng 3 ("qa"). */
  public static final Set<String> TASKS =
      Set.of(
          "qa",
          "kb_qa",
          "lesson_plan",
          "lesson_patch",
          "class_notice",
          "grade_ocr",
          "grade",
          "exam_draft",
          "class_errors");

  private final KillSwitchRepository repo;
  private final boolean lanes;

  public KillSwitchService(KillSwitchRepository repo, Environment env) {
    this.repo = repo;
    this.lanes = env.acceptsProfiles(Profiles.of("dev", "test"));
  }

  /** Task đang tắt khẩn. "qa" và "kb_qa" là cùng một lượt hỏi đáp của học sinh. */
  public boolean killed(String task) {
    return killed(task, null);
  }

  /** Như trên với làn kiểm thử lấy thẳng từ yêu cầu (bộ lọc chạy trước RequestContextHolder). */
  public boolean killed(String task, String laneHeader) {
    if (task == null) {
      return false;
    }
    Set<String> keys =
        "qa".equals(task) || "kb_qa".equals(task) ? Set.of("qa", "kb_qa") : Set.of(task);
    return repo.anyOff(keys, lane(laneHeader));
  }

  public Map<String, Object> list(String laneHeader) {
    requireOperator();
    Map<String, Object> out = new LinkedHashMap<>();
    TASKS.stream().sorted().forEach(t -> out.put(t, false));
    out.putAll(repo.list(lane(laneHeader)));
    return Map.of("tasks", out);
  }

  @Transactional
  public Map<String, Object> set(String task, Boolean off, String laneHeader) {
    AuthPrincipal me = requireOperator();
    if (task == null || !TASKS.contains(task)) {
      throw AppException.invalidField("task", "Task AI không hợp lệ.");
    }
    if (off == null) {
      throw AppException.invalidField("off", "Thiếu trạng thái tắt.");
    }
    String lane = lane(laneHeader);
    boolean before = repo.isOff(task, lane);
    repo.set(task, lane, off, me.userId());
    repo.audit(me.orgId(), me.userId(), task, !before, !off);
    return Map.of("task", task, "off", off);
  }

  public List<Map<String, Object>> audit(UUID orgId, String task) {
    return repo.auditOf(orgId, task);
  }

  /** Cấp vai Vận hành (chỉ móc kiểm thử / seed; không có API cấp ở prod). */
  public void grant(UUID userId, String by) {
    repo.grant(userId, by);
  }

  private String lane(String header) {
    if (!lanes || header == null) {
      return "";
    }
    return header.length() > 64 ? header.substring(0, 64) : header;
  }

  private AuthPrincipal requireOperator() {
    AuthPrincipal p = RequestContext.principal();
    if (!repo.isOperator(p.userId())) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return p;
  }
}
