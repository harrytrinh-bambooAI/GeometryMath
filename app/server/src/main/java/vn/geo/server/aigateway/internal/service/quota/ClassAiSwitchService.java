package vn.geo.server.aigateway.internal.service.quota;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.aigateway.internal.repo.quota.QuotaRepository;
import vn.geo.server.aigateway.internal.repo.quota.QuotaRepository.ClassInfo;
import vn.geo.server.aigateway.internal.repo.quota.QuotaRepository.Switch;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Công tắc AI chấm / Trợ lý của lớp và màn "Dùng AI" (US-S5E5-04-SC1, SC2, FC2; BR-S5E5-10, 11,
 * 13). Chỉ giáo viên chủ lớp; giáo viên khác NOT_FOUND, học sinh FORBIDDEN. Tắt AI chấm huỷ ngay
 * việc chấm nền đang chờ của lớp; mỗi lần đổi ghi AuditEvent (trước, sau, người thực hiện).
 */
@Service
public class ClassAiSwitchService {

  /** MSG-S5-59. */
  public static final String MSG_59 = "Đã tắt AI chấm cho lớp %s. Bài mới nộp sẽ chấm tay.";

  private final QuotaRepository repo;
  private final AiQuotaService quota;

  public ClassAiSwitchService(QuotaRepository repo, AiQuotaService quota) {
    this.repo = repo;
    this.quota = quota;
  }

  /** Thân PUT: chỉ trường có mặt được đổi. */
  public record SwitchRequest(Boolean aiGrading, Boolean assistant) {}

  public Map<String, Object> get(UUID classId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    ClassInfo c = owned(me, classId);
    return view(c, repo.classSwitch(me.orgId(), classId), null, 0);
  }

  @Transactional
  public Map<String, Object> put(UUID classId, SwitchRequest body) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    ClassInfo c = owned(me, classId);
    if (body == null || (body.aiGrading() == null && body.assistant() == null)) {
      throw AppException.invalidField("aiGrading", "Chọn công tắc cần đổi.");
    }
    Switch before = repo.classSwitch(me.orgId(), classId);
    Switch after =
        new Switch(
            body.aiGrading() == null ? before.aiGrading() : body.aiGrading(),
            body.assistant() == null ? before.assistant() : body.assistant());
    repo.saveSwitch(me.orgId(), classId, after, me.userId());
    int cancelled = 0;
    String message = null;
    if (before.aiGrading() != after.aiGrading()) {
      repo.audit(
          me.orgId(),
          me.userId(),
          "teacher",
          classId,
          "ai_grading",
          before.aiGrading(),
          after.aiGrading());
      if (!after.aiGrading()) {
        cancelled = repo.cancelPendingGrading(me.orgId(), classId);
        message = MSG_59.formatted(c.name());
      }
    }
    if (before.assistant() != after.assistant()) {
      repo.audit(
          me.orgId(),
          me.userId(),
          "teacher",
          classId,
          "assistant",
          before.assistant(),
          after.assistant());
    }
    return view(c, after, message, cancelled);
  }

  /**
   * Màn "Dùng AI" của lớp: lượt theo task trong {@code days} ngày và lượt bị chặn; không có tên.
   */
  public Map<String, Object> usage(UUID classId, int days) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    ClassInfo c = owned(me, classId);
    int d = Math.max(1, Math.min(days, 90));
    Map<String, Long> totals =
        repo.classTotals(me.orgId(), classId, quota.today().minusDays(d - 1L));
    List<Map<String, Object>> tasks = new ArrayList<>();
    totals.forEach((k, v) -> {
      if (k.startsWith("turn.")) {
        tasks.add(Map.of("task", k.substring(5), "turns", v));
      }
    });
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("classId", c.id());
    out.put("className", c.name());
    out.put("days", d);
    out.put("tasks", tasks);
    out.put("blocked", totals.getOrDefault("blocked", 0L));
    out.put("tokens", totals.getOrDefault("tokens", 0L));
    AiQuotaService.Limits l = quota.limits();
    out.put(
        "limits",
        Map.of(
            "classQa", l.classQa(),
            "classGrade", l.classGrade(),
            "classTokens", l.classTokens(),
            "studentQa", l.studentQa(),
            "compose", l.compose()));
    return out;
  }

  private ClassInfo owned(AuthPrincipal me, UUID classId) {
    return repo.classInfo(me.orgId(), classId)
        .filter(c -> c.teacherId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private static Map<String, Object> view(ClassInfo c, Switch s, String message, int cancelled) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("classId", c.id());
    out.put("className", c.name());
    out.put("aiGrading", s.aiGrading());
    out.put("assistant", s.assistant());
    out.put("studentAi", c.studentAi());
    if (message != null) {
      out.put("message", message);
      out.put("cancelledJobs", cancelled);
    }
    return out;
  }
}
