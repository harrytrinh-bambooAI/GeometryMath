package vn.geo.server.content.internal.draft.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.solution.repo.SolutionRepository;
import vn.geo.server.content.internal.solution.service.AnswerCompare;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * S2-E3: nội dung bốn bước của tác vụ soạn nháp (US-S2E3-03, 04). Mỗi bước nhận nháp + kết quả các
 * bước trước ({@code outcome}), ghi thêm vào {@code outcome}, trả ghi chú ngắn của bước.
 *
 * <ol>
 *   <li>Đọc đề: AI nhận loại đề; đáp số đề ghi lấy từ chữ đề ("Đáp số: …", BR-S2E3-17).
 *   <li>Dựng hình: AI đề xuất mô tả dựng, ENGINE dựng; engine không dựng được (dạng chưa hỗ trợ,
 *       tọa độ AI tự đặt) = {@link Unbuildable} → "Cần xem lại · hình do AI dựng" (BR-S2E3-15).
 *   <li>Giải: AI soạn các bước; lưu thành sổ lời giải + phiên bản v1 "AI soạn" (S2-E5).
 *   <li>Kiểm chứng: StepCheck + đối chiếu ba đáp số (S2-E5 "Kiểm tra lại"); nhãn máy chỉ do engine.
 * </ol>
 */
@Component
public class DraftStages {

  /** Engine không dựng được hình từ đề. */
  public static final class Unbuildable extends RuntimeException {
    public Unbuildable(String message) {
      super(message);
    }
  }

  private final DraftRepository drafts;
  private final SolutionRepository solutionsRepo;
  private final SolutionReviewService solutions;
  private final GeometryEngine geometry;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;

  public DraftStages(
      DraftRepository drafts,
      SolutionRepository solutionsRepo,
      SolutionReviewService solutions,
      GeometryEngine geometry,
      SubjectRegistry subjects,
      JsonMapper mapper) {
    this.drafts = drafts;
    this.solutionsRepo = solutionsRepo;
    this.solutions = solutions;
    this.geometry = geometry;
    this.subjects = subjects;
    this.mapper = mapper;
  }

  String run(int stage, DraftRow d, ObjectNode outcome, Function<AiRequest, AiResponse> ai) {
    return switch (stage) {
      case 0 -> read(d, outcome, ai);
      case 1 -> build(d, outcome, ai);
      case 2 -> solve(d, outcome, ai);
      default -> verify(outcome);
    };
  }

  private JsonNode json(String s) {
    try {
      return mapper.readTree(s == null || s.isBlank() ? "{}" : s);
    } catch (RuntimeException e) {
      return mapper.createObjectNode();
    }
  }

  private String read(DraftRow d, ObjectNode outcome, Function<AiRequest, AiResponse> ai) {
    String statement = d.statement().strip();
    JsonNode r = json(ai.apply(new AiRequest("draft_read", "Đề bài: " + statement)).content());
    outcome.put("kind", "space".equals(r.path("kind").asString()) ? "space" : "plane");
    String stated = AnswerCompare.statedIn(statement);
    if (stated != null) {
      outcome.put("statedAnswer", stated);
    } else {
      outcome.remove("statedAnswer");
    }
    return "Xong · " + statement.codePointCount(0, statement.length()) + " ký tự";
  }

  private String build(DraftRow d, ObjectNode outcome, Function<AiRequest, AiResponse> ai) {
    JsonNode r =
        json(ai.apply(new AiRequest("draft_build", "Đề bài: " + d.statement().strip())).content());
    JsonNode figure = r.path("figure");
    outcome.remove("figure");
    if (figure.hasNonNull("catalogCode")) {
      if (geometry.catalogSpec(figure.path("catalogCode").asString()).isEmpty()) {
        throw new Unbuildable("Máy không dựng được hình");
      }
      outcome.set("figure", figure.deepCopy());
      outcome.put("figureSource", "engine");
      return "Xong · hình không gian";
    }
    JsonNode spec = figure.path("plane");
    if (!spec.isObject()) {
      throw new Unbuildable("Máy không dựng được hình");
    }
    SubjectEngine.Rendering rendering;
    try {
      SubjectEngine engine = subjects.forKind(ProblemKind.PLANE);
      rendering = engine.render(engine.parse(new SubjectEngine.ProblemInput(d.statement(), spec)));
    } catch (RuntimeException e) {
      rendering = new SubjectEngine.Rendering(false, null, null, "không dựng được");
    }
    if (!rendering.built() || rendering.reason() != null) {
      // tọa độ AI tự đặt (đề lạ) hoặc engine không dựng: giữ mô tả để xem, nhãn "hình do AI dựng"
      outcome.set("figure", figure.deepCopy());
      outcome.put("figureSource", "ai");
      throw new Unbuildable("Máy không dựng được hình");
    }
    outcome.set("figure", figure.deepCopy());
    outcome.put("figureSource", "engine");
    return "Xong · hình phẳng";
  }

  private String solve(DraftRow d, ObjectNode outcome, Function<AiRequest, AiResponse> ai) {
    String figure = mapper.writeValueAsString(outcome.path("figure"));
    JsonNode r =
        json(
            ai.apply(
                    new AiRequest(
                        "draft_solve",
                        "Đề bài: " + d.statement().strip() + "\nMô tả hình: " + figure))
                .content());
    List<String> steps = new ArrayList<>();
    r.path("steps")
        .forEach(n -> {
          String t = n.asString("").strip();
          if (!t.isEmpty() && steps.size() < SolutionReviewService.MAX_STEPS) {
            steps.add(
                t.length() > SolutionReviewService.MAX_STEP_CHARS
                    ? t.substring(0, SolutionReviewService.MAX_STEP_CHARS)
                    : t);
          }
        });
    if (steps.isEmpty()) {
      throw new IllegalStateException("AI không trả bước giải");
    }
    UUID docId = Uuid7.next();
    String statement = d.statement().strip();
    String title = statement.length() > 80 ? statement.substring(0, 80) + "…" : statement;
    solutionsRepo.insertDraftDoc(
        docId,
        RequestContext.principal().userId(),
        title,
        statement,
        figure,
        outcome.path("figureSource").asString("engine"),
        outcome.hasNonNull("statedAnswer") ? outcome.get("statedAnswer").asString() : null,
        d.id());
    if (!solutionsRepo.bumpLatest(docId, 0)) {
      throw new IllegalStateException("không tạo được phiên bản v1");
    }
    solutionsRepo.insertAiVersion(Uuid7.next(), docId, 1, mapper.writeValueAsString(steps));
    drafts.setDoc(d.id(), docId);
    outcome.put("docId", docId.toString());
    return "Xong · " + steps.size() + " bước";
  }

  private String verify(ObjectNode outcome) {
    UUID docId = UUID.fromString(outcome.path("docId").asString());
    DocView v = solutions.check(docId, 1);
    boolean mismatch = v.answers() != null && "mismatch".equals(v.answers().state());
    return mismatch ? "Lệch đáp số" : "Xong · " + v.version().machineLabel();
  }
}
