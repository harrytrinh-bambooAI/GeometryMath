package vn.geo.server.aigrade.internal.proposal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigrade.internal.engine.service.EngineMatcher;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Candidate;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.NewCriterion;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.NewProposal;
import vn.geo.server.common.id.Uuid7;

/** Lưu một lần chấm nền (đề xuất, tiêu chí, tín hiệu engine) trong một giao dịch; bất biến. */
@Component
public class ProposalWriter {

  private final ProposalRepository repo;
  private final JsonMapper mapper;

  public ProposalWriter(ProposalRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Transactional
  public void save(
      Candidate c,
      int rubricVersion,
      ProposalService.Result r,
      EngineMatcher.Result engine,
      String engineAnswer,
      String model,
      String promptVersion) {
    GradeRules.Outcome o = r.outcome();
    boolean proposed = "proposed".equals(r.status()) && o != null;
    boolean disagree = false;
    if (proposed) {
      for (GradeRules.Assessed a : o.criteria()) {
        if (GradeRules.role(a.name()) == GradeRules.Role.RESULT) {
          disagree = EngineMatcher.disagree(engine.status(), a.points(), a.max());
        }
      }
    }
    List<String> flags = new ArrayList<>(proposed ? o.flags() : List.of());
    List<Map<String, Object>> steps = new ArrayList<>();
    if (proposed) {
      for (GradeRules.Step s : o.steps()) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", s.code());
        m.put("criterionOrd", s.criterionOrd());
        m.put("aiPoints", s.points());
        m.put("points", s.finalPoints());
        m.put("correct", s.correct());
        m.put("dependsOn", s.dependsOn());
        m.put("onFigure", s.onFigure());
        m.put("rule", s.zeroedBy());
        steps.add(m);
      }
    }
    UUID id = Uuid7.next();
    boolean inserted =
        repo.insert(
            new NewProposal(
                id,
                c,
                rubricVersion,
                proposed ? "proposed" : "no_grade",
                proposed ? null : r.reason(),
                proposed ? o.total() : null,
                proposed ? o.confidence() : null,
                mapper.writeValueAsString(flags),
                mapper.writeValueAsString(steps),
                model == null || model.isBlank() ? "unknown" : model,
                promptVersion,
                r.attempts(),
                r.tokens()));
    if (!inserted) {
      return; // đã có đề xuất của cùng phiên bản bản đọc chữ
    }
    if (proposed) {
      for (GradeRules.Assessed a : o.criteria()) {
        List<Map<String, Object>> adj = new ArrayList<>();
        for (GradeRules.Adjustment x : a.adjustments()) {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("rule", x.rule());
          m.put("from", x.from());
          m.put("to", x.to());
          m.put("note", x.note());
          adj.add(m);
        }
        repo.insertCriterion(
            c.orgId(),
            id,
            c.academicYear(),
            new NewCriterion(
                a.ord(),
                a.name(),
                a.max(),
                a.aiPoints(),
                a.points(),
                a.reason(),
                a.region() == null ? null : mapper.writeValueAsString(a.region()),
                a.confidence(),
                a.errorLabel(),
                mapper.writeValueAsString(adj),
                mapper.writeValueAsString(a.flags())));
      }
    }
    repo.insertSignal(
        c.orgId(),
        id,
        c.submissionId(),
        engine.status(),
        cut(engine.studentAnswer()),
        cut(engineAnswer),
        engine.rule(),
        disagree,
        c.academicYear());
  }

  private static String cut(String s) {
    return s == null ? null : s.length() <= 200 ? s : s.substring(0, 200);
  }
}
