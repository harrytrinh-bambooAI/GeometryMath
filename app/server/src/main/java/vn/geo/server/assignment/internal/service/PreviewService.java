package vn.geo.server.assignment.internal.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.assignment.internal.repo.ReleaseRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.BankAccess;
import vn.geo.server.learning.api.AnswerGuardApi;

/**
 * Màn "Xem như học sinh" (US-GEO-607). Không gọi đường đọc lời giải của học sinh hay của giáo viên,
 * không tải bước giải hay đáp số xuống thiết bị, kể cả khi bài đã mở (BR-GEO-301, D-GEO-153, 180).
 * Hỏi đáp trong xem trước luôn đi qua Kiểm soát đáp án như khi khóa (D-GEO-027) và không ghi gì vào
 * Theo dõi lớp (BR-GEO-247, D-GEO-120).
 */
@Service
public class PreviewService {

  public record PreviewProblem(UUID versionId, String code, String title, String statement) {}

  public record PreviewPage(PreviewProblem problem, ReleaseService.PreviewLock lock) {}

  public record PreviewAnswer(String question, String replyKind, String reply) {}

  static final String MSG_37 = "Câu hỏi cần từ 1 đến 1000 ký tự.";

  private final ReleaseRepository repo;
  private final ReleaseService release;
  private final AiGateway ai;
  private final AnswerGuardApi guard;
  private final JsonMapper mapper;
  private final BankAccess bank;

  public PreviewService(
      ReleaseRepository repo,
      ReleaseService release,
      AiGateway ai,
      AnswerGuardApi guard,
      JsonMapper mapper,
      BankAccess bank) {
    this.bank = bank;
    this.repo = repo;
    this.release = release;
    this.ai = ai;
    this.guard = guard;
    this.mapper = mapper;
  }

  public PreviewPage page(UUID versionId, UUID classId, OffsetDateTime releaseAt) {
    Roles.require(Role.TEACHER);
    // quyền bộ đề (FIX3): bộ ngoài quyền = không tồn tại
    bank.requireVisible(versionId);
    ReleaseRepository.CatalogProblem cp =
        repo.catalogProblem(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new PreviewPage(
        new PreviewProblem(cp.versionId(), cp.code(), cp.title(), cp.statement()),
        release.previewLock(versionId, classId, releaseAt));
  }

  public PreviewAnswer ask(UUID versionId, String question, String highestHint) {
    Roles.require(Role.TEACHER);
    String q = question == null ? "" : question.strip();
    if (q.isEmpty() || q.codePointCount(0, q.length()) > 1000) {
      throw AppException.invalidField("question", MSG_37);
    }
    // quyền bộ đề (FIX3): bộ ngoài quyền = không tồn tại
    bank.requireVisible(versionId);
    ReleaseRepository.CatalogProblem cp =
        repo.catalogProblem(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    String prompt =
        "Vai trò: trợ lý học hình học không gian, gợi mở, không nêu đáp số.\nĐề bài: "
            + cp.statement()
            + "\nCâu hỏi: "
            + q;
    String content = ai.complete(new AiRequest("qa", prompt)).content();
    String kind = "hint";
    String text = content == null ? "" : content.strip();
    if (text.startsWith("{")) {
      try {
        JsonNode n = mapper.readTree(text);
        kind = n.path("kind").asString("hint");
        text = n.path("reply").asString("").strip();
      } catch (RuntimeException e) {
        // văn bản thuần
      }
    }
    AnswerGuardApi.Screened sc;
    try {
      sc = guard.screenPreview(versionId, q, text, highestHint);
    } catch (AnswerGuardApi.GuardFailure e) {
      throw new AppException(
          ErrorCode.DEPENDENCY_UNAVAILABLE, "Trợ lý đang bận. Em thử lại sau ít phút nhé.");
    }
    return new PreviewAnswer(q, sc.blocked() ? "refused_answer" : kind, sc.text());
  }
}
