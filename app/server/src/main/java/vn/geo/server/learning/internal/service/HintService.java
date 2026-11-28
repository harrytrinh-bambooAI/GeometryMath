package vn.geo.server.learning.internal.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.redis.RedisKeys;
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

  /**
   * Thời hạn khóa "đang sinh gợi ý AI" của một bài tự học và thời gian chờ tối đa của yêu cầu sau.
   */
  static final Duration GEN_LOCK = Duration.ofSeconds(30);

  static final Duration GEN_WAIT = Duration.ofSeconds(15);

  private final HelpRepository repo;
  private final AiGateway ai;
  private final JsonMapper mapper;
  private final AnswerGuard guard;
  private final StringRedisTemplate redis;
  private final RedisKeys keys;

  public HintService(
      HelpRepository repo,
      AiGateway ai,
      JsonMapper mapper,
      AnswerGuard guard,
      StringRedisTemplate redis,
      RedisKeys keys) {
    this.repo = repo;
    this.ai = ai;
    this.mapper = mapper;
    this.guard = guard;
    this.redis = redis;
    this.keys = keys;
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
      // bài kế thừa khóa dùng gợi ý của đề kho, không gọi AI (D-GEO-155): đề kho thiếu gợi ý thì
      // NOT_FOUND mà không sinh gợi ý AI vô ích
      if ("self".equals(st.kind()) && !repo.selfLockInherited(p.userId(), assignmentId)) {
        r = generateOnce(p.userId(), assignmentId, level, requestId, st.statement());
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

  /**
   * Sinh gợi ý AI MỘT lần cho bài tự học kể cả khi nhiều yêu cầu đầu đến cùng lúc: khóa Redis theo
   * bài; yêu cầu giữ khóa xem lại (có thể yêu cầu trước vừa ghi xong) rồi mới gọi AI; yêu cầu khác
   * chờ khóa nhả rồi đọc gợi ý đã ghi.
   */
  private HelpRepository.HintRow generateOnce(
      UUID userId, UUID assignmentId, int level, UUID requestId, String statement) {
    String key = keys.key("selfhints", assignmentId.toString());
    if (Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, requestId.toString(), GEN_LOCK))) {
      try {
        HelpRepository.HintRow again = repo.hint(userId, assignmentId, level, requestId);
        if (!"NO_HINT".equals(again.status())) {
          return again;
        }
        generateSelfHints(userId, assignmentId, statement);
      } finally {
        redis.delete(key);
      }
    } else {
      long deadline = System.nanoTime() + GEN_WAIT.toNanos();
      while (Boolean.TRUE.equals(redis.hasKey(key)) && System.nanoTime() < deadline) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    return repo.hint(userId, assignmentId, level, requestId);
  }

  /**
   * Gợi ý AI của đề tự nhập; prompt chỉ có đề (BR-GEO-108). Lỗi AI: MSG-GEO-14 ở thiết bị. Mỗi gợi
   * ý qua Kiểm soát đáp án trước khi lưu (BR-GEO-179, US-GEO-405): lộ đáp số/bước giải thì thay
   * bằng câu gợi mở chung.
   */
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
    if (guard.leaks(userId, assignmentId, h1)) {
      h1 = FALLBACK_1;
    }
    if (guard.leaks(userId, assignmentId, h2)) {
      h2 = FALLBACK_2;
    }
    repo.putSelfHints(userId, assignmentId, h1, h2);
  }

  static final String FALLBACK_1 =
      "Em đọc kỹ dữ kiện và xác định đại lượng đề hỏi nằm ở đâu trên hình.";
  static final String FALLBACK_2 =
      "Em tìm một tam giác vuông hoặc một mặt phẳng chứa đại lượng đó để tính.";
}
