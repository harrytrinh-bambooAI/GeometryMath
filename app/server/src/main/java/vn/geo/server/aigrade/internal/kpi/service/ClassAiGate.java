package vn.geo.server.aigrade.internal.kpi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository.Gate;
import vn.geo.server.aigrade.internal.kpi.repo.KpiRepository.PaperRow;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Crit;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Paper;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Result;
import vn.geo.server.aigrade.internal.kpi.service.KpiCalc.Thresholds;
import vn.geo.server.assessment.api.ClassAiGateQuery;

/**
 * Cổng "AI đề xuất" của lớp (US-S5E6-06, BR-S5E6-16, 17; QS5-21, QS5-24). Thay stub luôn đóng của
 * T-261128-57.
 *
 * <ul>
 *   <li>Đóng: tính KPI trên bài song song từ mốc {@code since} (đầu tiên = mọi bài; sau lần tự đóng
 *       = chỉ bài mới); đạt đủ điều kiện và n ≥ 20 → mở.
 *   <li>Mở: bài song song sau lúc mở (bài lấy mẫu QS5-24) chia đợt 10 bài; hai đợt liên tiếp không
 *       đạt hoặc một cờ bịa kiến thức → tự đóng, báo giáo viên chủ lớp, mốc {@code since} = lúc
 *       đóng.
 * </ul>
 */
@Service
public class ClassAiGate implements ClassAiGateQuery {

  /** Thông báo khi cổng tự đóng. */
  public static final String MSG_CLOSED = "AI đề xuất đã tạm tắt cho %s";

  static final int BATCH = 10;

  private final KpiRepository repo;
  private final JsonMapper mapper;
  private final Thresholds th;

  public ClassAiGate(
      KpiRepository repo,
      JsonMapper mapper,
      @Value("${geo.s5.ai-grading.gate.min-papers:20}") int minPapers,
      @Value("${geo.s5.ai-grading.gate.macro:0.85}") double macro,
      @Value("${geo.s5.ai-grading.gate.topic-floor:0.70}") double topicFloor,
      @Value("${geo.s5.ai-grading.gate.within:0.85}") double within,
      @Value("${geo.s5.ai-grading.gate.result:0.95}") double result,
      @Value("${geo.s5.ai-grading.gate.catch:0.80}") double catchRate,
      @Value("${geo.s5.ai-grading.gate.false-alarm:0.10}") double falseAlarm) {
    this.repo = repo;
    this.mapper = mapper;
    this.th = new Thresholds(minPapers, macro, topicFloor, within, result, catchRate, falseAlarm);
  }

  /** Kết quả xét cổng: trạng thái sau khi xét và KPI trên cửa sổ đang tính. */
  public record Evaluation(boolean open, Gate gate, Result window, Thresholds thresholds) {}

  @Override
  public boolean aiProposalReady(UUID orgId, UUID classId) {
    return evaluate(orgId, classId).open();
  }

  /** Đọc trạng thái đã lưu, không tính lại (dùng cho luật lấy mẫu mỗi lượt xem). */
  @Override
  public boolean aiProposalOpen(UUID orgId, UUID classId) {
    return repo.openedAt(orgId, classId).isPresent();
  }

  /** Dựng lại số liệu từ dữ liệu thật, tính KPI, mở / đóng cổng theo BR-S5E6-16, 17. */
  @Transactional
  public Evaluation evaluate(UUID orgId, UUID classId) {
    Gate g = repo.lockGate(orgId, classId);
    repo.refreshShadow(orgId, classId);
    repo.refreshNoGrade(orgId, classId);
    repo.refreshProposal(orgId, classId);
    List<PaperRow> shadowRows = repo.papers(orgId, classId, "shadow", g.since());
    List<Paper> shadow = shadowRows.stream().map(this::paper).toList();
    List<Paper> proposal =
        repo.papers(orgId, classId, "proposal", g.since()).stream().map(this::paper).toList();
    Result window = KpiCalc.compute(shadow, proposal, th, true);
    if (g.openedAt() == null) {
      if (window.pass()) {
        repo.open(orgId, classId);
        return new Evaluation(true, repo.lockGate(orgId, classId), window, th);
      }
      return new Evaluation(false, g, window, th);
    }
    // Đã mở: xét từng đợt 10 bài song song sau lúc mở.
    List<Paper> after = new ArrayList<>();
    int invented = 0;
    for (int i = 0; i < shadowRows.size(); i++) {
      if (shadowRows.get(i).createdAt().isAfter(g.openedAt())) {
        Paper p = shadow.get(i);
        invented += p.invented();
        if (!p.noGrade() && !p.criteria().isEmpty()) {
          after.add(p);
        }
      }
    }
    String closeReason = invented > 0 ? "bịa kiến thức: " + invented : null;
    int streak = g.failStreak();
    int done = g.batchesDone();
    int total = after.size() / BATCH;
    while (closeReason == null && done < total) {
      List<Paper> batch = after.subList(done * BATCH, (done + 1) * BATCH);
      Result r = KpiCalc.compute(batch, List.of(), th, false);
      streak = r.pass() ? 0 : streak + 1;
      done++;
      if (streak >= 2) {
        closeReason = "hai đợt liên tiếp không đạt: " + String.join("; ", r.reasons());
      }
    }
    if (closeReason != null) {
      repo.close(orgId, classId, closeReason);
      repo.classInfo(orgId, classId)
          .ifPresent(c -> repo.notifyTeacher(orgId, c.teacherId(), MSG_CLOSED.formatted(c.name())));
      Gate closed = repo.lockGate(orgId, classId);
      return new Evaluation(false, closed, KpiCalc.compute(List.of(), List.of(), th, true), th);
    }
    if (streak != g.failStreak() || done != g.batchesDone()) {
      repo.progress(orgId, classId, streak, done);
    }
    return new Evaluation(true, repo.lockGate(orgId, classId), window, th);
  }

  Paper paper(PaperRow r) {
    List<Crit> cs = new ArrayList<>();
    for (JsonNode c : mapper.readTree(r.criteria())) {
      cs.add(
          new Crit(
              c.path("max").asDouble(),
              c.path("ai").asDouble(),
              c.path("gv").asDouble(),
              c.path("edited").asBoolean(false)));
    }
    return new Paper(
        r.topic(), cs, r.resultOk(), r.invented(), r.blocked(), r.noGrade(), r.reviewSeconds());
  }
}
