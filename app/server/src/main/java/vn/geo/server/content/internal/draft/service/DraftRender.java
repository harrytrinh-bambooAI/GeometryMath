package vn.geo.server.content.internal.draft.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.repo.DraftRepository.JobRow;
import vn.geo.server.content.internal.draft.service.DraftViews.CellView;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftSummary;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftView;
import vn.geo.server.content.internal.draft.service.DraftViews.JobView;
import vn.geo.server.content.internal.draft.service.DraftViews.ResultView;
import vn.geo.server.content.internal.draft.service.DraftViews.StageView;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.content.internal.solution.service.SolutionViews;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistry;

/** S2-E3: dựng dữ liệu màn C2-02 từ nháp, tác vụ và sổ lời giải (gọi trong ngữ cảnh chủ nháp). */
@Component
public class DraftRender {

  private final DraftRepository repo;
  private final SolutionReviewService solutions;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;

  public DraftRender(
      DraftRepository repo,
      SolutionReviewService solutions,
      SubjectRegistry subjects,
      JsonMapper mapper) {
    this.repo = repo;
    this.solutions = solutions;
    this.subjects = subjects;
    this.mapper = mapper;
  }

  List<CellView> cells(DraftRow d) {
    List<CellView> out = new ArrayList<>();
    int i = 0;
    for (JsonNode c : mapper.readTree(d.cellsJson())) {
      out.add(
          new CellView(
              i++,
              c.path("text").asString(""),
              c.path("confidence").asDouble(1),
              c.path("state").asString("ok")));
    }
    return out;
  }

  JobView job(JobRow j) {
    List<StageView> stages = new ArrayList<>();
    int i = 0;
    for (JsonNode s : mapper.readTree(j.stagesJson())) {
      stages.add(
          new StageView(
              s.path("key").asString(),
              DraftViews.STAGE_LABELS.get(Math.min(i++, 3)),
              s.path("state").asString("waiting"),
              s.hasNonNull("note") ? s.get("note").asString() : null,
              s.hasNonNull("error") ? s.get("error").asString() : null,
              s.path("aiCalls").asInt(0)));
    }
    boolean retryable =
        "failed".equals(j.state())
            && stages.stream()
                .anyMatch(s -> "error".equals(s.state()) && !"UNBUILDABLE".equals(s.error()));
    return new JobView(j.id(), j.draftId(), j.state(), stages, retryable);
  }

  private ResultView result(DraftRow d, JobRow j) {
    JsonNode outcome = mapper.readTree(j.outcomeJson());
    JsonNode figure = outcome.path("figure");
    String sceneKind = null;
    Object scene = null;
    if (figure.path("plane").isObject()) {
      sceneKind = "plane";
      try {
        SubjectEngine e = subjects.forKind(ProblemKind.PLANE);
        scene =
            e.render(e.parse(new SubjectEngine.ProblemInput(d.statement(), figure.path("plane"))))
                .scene();
      } catch (RuntimeException ex) {
        scene = null;
      }
    }
    if (d.docId() == null) {
      return new ResultView(
          null,
          SolutionViews.MACHINE_NEEDS_REVIEW,
          null,
          SolutionViews.HUMAN_AI,
          sceneKind,
          scene,
          List.of(),
          null,
          true,
          null);
    }
    DocView v = solutions.view(d.docId(), null);
    if (figure.hasNonNull("catalogCode")) {
      sceneKind = "space";
      scene = v.scene();
    }
    return new ResultView(
        d.docId(),
        v.version().machineLabel(),
        v.version().machineNote(),
        v.version().humanLabel(),
        sceneKind,
        scene,
        v.version().steps().stream().map(SolutionViews.StepView::text).toList(),
        v.answers(),
        !v.approve().allowed() && !v.approve().approved(),
        v.approve().blockedReason());
  }

  DraftView view(DraftRow d, String notice) {
    List<CellView> cells = cells(d);
    JobRow j = repo.lastJob(d.id()).orElse(null);
    JobView job = j == null ? null : job(j);
    ResultView result = j == null || "building".equals(d.status()) ? null : resultOrNull(d, j);
    String n = notice;
    if (n == null && "image".equals(d.source()) && cells.isEmpty() && d.imageBytes() > 0) {
      n = DraftViews.NO_TEXT;
    }
    boolean unsure = cells.stream().anyMatch(CellView::unsure);
    String machine =
        result != null && result.docId() != null
            ? result.machineLabel()
            : SolutionViews.MACHINE_NEEDS_REVIEW;
    boolean inShelf = false;
    if (d.docId() != null) {
      inShelf = solutions.view(d.docId(), null).inMyShelf();
    }
    return new DraftView(
        d.id(),
        d.source(),
        d.status(),
        DraftViews.statusLabel(d.status()),
        d.statement(),
        cells,
        d.textConfirmed(),
        "image".equals(d.source()) && !cells.isEmpty() && !unsure && !d.textConfirmed(),
        d.imageBytes() > 0,
        n,
        machine,
        inShelf,
        job,
        result,
        assignable(d),
        assignNote(d),
        d.assignConfirmedAt() != null,
        d.createdAt());
  }

  private ResultView resultOrNull(DraftRow d, JobRow j) {
    if (!"done".equals(j.state()) && !"ai_figure".equals(d.status())) {
      return null;
    }
    return result(d, j);
  }

  private static boolean assignable(DraftRow d) {
    return "ready".equals(d.status()) || "ai_figure".equals(d.status());
  }

  private static String assignNote(DraftRow d) {
    return switch (d.status()) {
      case "text_only" -> DraftViews.TEXT_ONLY;
      case "ai_figure" -> DraftViews.AI_FIGURE;
      case "ready" -> null;
      default -> "chưa dựng xong";
    };
  }

  DraftSummary summary(DraftRow d) {
    String s = d.statement().strip();
    String title = s.isEmpty() ? "(đề trống)" : s.length() > 80 ? s.substring(0, 80) + "…" : s;
    return new DraftSummary(
        d.id(),
        title,
        d.status(),
        DraftViews.statusLabel(d.status()),
        d.docId(),
        assignable(d),
        assignNote(d),
        "ai_figure".equals(d.status()),
        d.createdAt());
  }
}
