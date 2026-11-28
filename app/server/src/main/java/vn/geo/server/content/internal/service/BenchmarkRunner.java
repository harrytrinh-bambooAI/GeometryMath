package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;

/**
 * Chạy bộ đề chuẩn (US-GEO-105): với mỗi đề, xin AI (qua cổng, mục đích "solution") đáp số và tọa
 * độ, engine kiểm hình thỏa ràng buộc và đáp số khớp tham chiếu, ghi báo cáo kiểm chứng và nhãn máy
 * qua hàm cổng (nhãn "Đã kiểm chứng" chỉ khi cả hai khớp, BR-07). Mỗi lần chạy lưu một báo cáo
 * (benchmark_run, chỉ thêm): chạy lại khi đổi model giữ báo cáo cũ.
 */
@Service
public class BenchmarkRunner {

  private final ContentRepository repo;
  private final AiGateway ai;
  private final SceneChecker scenes;
  private final JsonMapper mapper;
  private final boolean simulated;

  public BenchmarkRunner(
      ContentRepository repo,
      AiGateway ai,
      SceneChecker scenes,
      JsonMapper mapper,
      @Value("${geo.ai.provider:mock}") String provider) {
    this.repo = repo;
    this.ai = ai;
    this.scenes = scenes;
    this.mapper = mapper;
    this.simulated = "mock".equals(provider);
  }

  /** Chạy trên phiên bản bộ đề mới nhất; bộ trống (chưa nạp) cho N = 0. */
  public BenchmarkReport run() {
    String modelInUse = ai.model();
    String promptVersion = ai.promptVersion();
    Integer set = repo.latestSet().map(ContentRepository.SetRow::version).orElse(null);
    List<ContentRepository.Reference> refs = set == null ? List.of() : repo.reference(set);
    int sceneOk = 0;
    int answerOk = 0;
    int verified = 0;
    int wrongVerified = 0;
    List<String> missing = new ArrayList<>();
    List<Map<String, Object>> items = new ArrayList<>();
    for (ContentRepository.Reference r : refs) {
      String aiAnswer = "";
      JsonNode points = null;
      boolean aiOk = true;
      try {
        AiResponse resp =
            ai.complete(
                new AiRequest("solution", "Mã bài: " + r.code() + "\nĐề bài: " + r.statement()));
        JsonNode out = mapper.readTree(resp.content());
        aiAnswer = out.path("answer").asString("");
        points = out.get("points");
      } catch (AppException | tools.jackson.core.JacksonException e) {
        aiOk = false;
      }
      boolean scene = aiOk && scenes.check(r.code(), points);
      boolean answer =
          aiOk
              && !r.missingReference()
              && r.finalAnswer() != null
              && AnswerExpr.matches(aiAnswer, AnswerExpr.parse(r.finalAnswer()));
      if (r.missingReference()) {
        missing.add(r.code());
      }
      Map<String, Object> checks = new LinkedHashMap<>();
      checks.put("scene_ok", scene);
      checks.put("answer_ok", answer);
      checks.put("missing_reference", r.missingReference());
      String label =
          repo.recordVerification(
              r.versionId(), Uuid7.next(), scene, answer, mapper.writeValueAsString(checks));
      boolean isVerified = "verified".equals(label);
      sceneOk += scene ? 1 : 0;
      answerOk += answer ? 1 : 0;
      verified += isVerified ? 1 : 0;
      wrongVerified += isVerified && !(scene && answer) ? 1 : 0;
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("code", r.code());
      item.put("problemId", r.problemId().toString());
      item.put("problemVersionId", r.versionId().toString());
      item.put("sceneOk", scene);
      item.put("answerOk", answer);
      item.put("label", isVerified ? "Đã kiểm chứng" : "Cần xem lại");
      items.add(item);
    }
    BenchmarkReport report =
        new BenchmarkReport(
            set == null ? 0 : set,
            modelInUse,
            promptVersion,
            simulated,
            refs.size(),
            sceneOk,
            answerOk,
            wrongVerified,
            verified,
            missing);
    UUID runId = Uuid7.next();
    repo.insertRun(
        runId,
        set,
        report.model(),
        promptVersion,
        simulated,
        report.n(),
        sceneOk,
        answerOk,
        wrongVerified,
        verified,
        mapper.writeValueAsString(items),
        report.text());
    return report;
  }
}
