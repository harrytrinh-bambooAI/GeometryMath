package vn.geo.server.learning.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.learning.api.HintApi;
import vn.geo.server.learning.internal.repo.HelpRepository;

/**
 * Gợi ý 1, 2 (US-GEO-404; BR-GEO-04, 174…178, 292, 293). Một lần xin = một hàm cổng (fn_get_hint):
 * kiểm tư cách, công tắc gợi ý của bài, thứ tự mức (Gợi ý 2 chỉ sau Gợi ý 1), ghi sự kiện gợi ý bền
 * TRƯỚC khi trả nội dung, lũy đẳng theo requestId. Bài tự học không kế thừa khóa dùng gợi ý do AI
 * sinh, sinh một lần ở lần xin đầu (tác vụ "hint"); bài kế thừa khóa dùng gợi ý của đề kho và không
 * gọi AI (D-GEO-155, US-GEO-406-SC10).
 */
@Service
public class HintService implements HintApi {

  private final HelpRepository repo;
  private final AiGateway ai;
  private final JsonMapper mapper;

  public HintService(HelpRepository repo, AiGateway ai, JsonMapper mapper) {
    this.repo = repo;
    this.ai = ai;
    this.mapper = mapper;
  }

  @Override
  public HintResult hint(UUID assignmentId, int level, UUID requestId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (requestId == null) {
      throw AppException.invalidField("requestId", "Thiếu mã yêu cầu.");
    }
    HelpRepository.HintRow r = repo.hint(p.userId(), assignmentId, level, requestId);
    if ("NO_HINT".equals(r.status())) {
      HelpRepository.State st = repo.state(p.userId(), assignmentId);
      if ("self".equals(st.kind())) {
        generateSelfHints(p.userId(), assignmentId, st.statement());
        r = repo.hint(p.userId(), assignmentId, level, requestId);
      }
    }
    return switch (r.status()) {
      case "OK" -> {
        HelpRepository.State st = repo.state(p.userId(), assignmentId);
        int k = st.solutionViewed() ? 3 : st.hintLevelMax();
        yield new HintResult(r.level(), HelpMessages.hintTitle(r.level()), r.body(), k);
      }
      case "VALIDATION_KIND" -> throw AppException.invalidField("level", "Mức gợi ý là 1 hoặc 2.");
      case "LEVEL_ORDER" -> throw new AppException(ErrorCode.FORBIDDEN, HelpMessages.MSG_34);
      case "HINT_DISABLED" -> throw new AppException(ErrorCode.FORBIDDEN, HelpMessages.MSG_32);
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    };
  }

  /** Gợi ý AI của đề tự nhập; prompt chỉ có đề (BR-GEO-108). Lỗi AI: MSG-GEO-14 ở thiết bị. */
  private void generateSelfHints(UUID userId, UUID assignmentId, String statement) {
    List<String> hints = new ArrayList<>();
    try {
      JsonNode out =
          mapper.readTree(ai.complete(new AiRequest("hint", "Đề bài: " + statement)).content());
      out.path("hints").forEach(h -> hints.add(h.asString("").strip()));
    } catch (JacksonException e) {
      // nội dung AI không đọc được: dùng câu gợi mở chung bên dưới
    }
    hints.removeIf(h -> h.isEmpty() || h.length() > 4000);
    String h1 = hints.size() > 0 ? hints.get(0) : FALLBACK_1;
    String h2 = hints.size() > 1 ? hints.get(1) : FALLBACK_2;
    repo.putSelfHints(userId, assignmentId, h1, h2);
  }

  static final String FALLBACK_1 =
      "Em đọc kỹ dữ kiện và xác định đại lượng đề hỏi nằm ở đâu trên hình.";
  static final String FALLBACK_2 =
      "Em tìm một tam giác vuông hoặc một mặt phẳng chứa đại lượng đó để tính.";
}
