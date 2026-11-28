package vn.geo.server.aigrade.internal.kpi.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository.ClassRow;
import vn.geo.server.aigrade.internal.kpi.service.ClassAiGate.Evaluation;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Thresholds;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Báo cáo khớp AI–giáo viên của lớp (US-S5E6-06-SC1…SC4, FC1…FC6). Chỉ giáo viên chủ lớp (giáo viên
 * khác NOT_FOUND, học sinh FORBIDDEN). Số liệu lớp, không tên học sinh, không xếp hạng.
 */
@Service
public class KpiService {

  private final KpiRepository repo;
  private final ClassAiGate gate;

  public KpiService(KpiRepository repo, ClassAiGate gate) {
    this.repo = repo;
    this.gate = gate;
  }

  public Map<String, Object> report(UUID classId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    ClassRow c =
        repo.classInfo(me.orgId(), classId)
            .filter(x -> x.teacherId().equals(me.userId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return view(c, gate.evaluate(me.orgId(), classId));
  }

  static Map<String, Object> view(ClassRow c, Evaluation ev) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("classId", c.id());
    out.put("className", c.name());
    out.put("n_bai", ev.window().papers());
    out.put("n_cap", ev.window().pairs());
    out.put("metrics", ev.window().metrics());
    out.put("gateOpen", ev.open());
    List<String> reasons = ev.open() ? List.of() : ev.window().reasons();
    out.put("reasons", reasons);
    Thresholds th = ev.thresholds();
    out.put(
        "message",
        !ev.open() && ev.window().papers() < th.minPapers()
            ? KpiCalc.msg48(ev.window().papers(), th.minPapers())
            : null);
    out.put("defaultMode", ev.open() ? "ai_proposal" : "parallel");
    out.put("openedAt", ev.gate().openedAt());
    out.put("since", ev.gate().since());
    out.put("closedAt", ev.gate().closedAt());
    out.put("closeReason", ev.gate().closeReason());
    Map<String, Object> t = new LinkedHashMap<>();
    t.put("minPapers", th.minPapers());
    t.put("macro", th.macro());
    t.put("topicFloor", th.topicFloor());
    t.put("within", th.within());
    t.put("result", th.result());
    t.put("catchRate", th.catchRate());
    t.put("falseAlarm", th.falseAlarm());
    out.put("thresholds", t);
    return out;
  }
}
