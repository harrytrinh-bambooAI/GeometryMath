package vn.geo.server.learning.internal.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.learning.internal.repo.HelpRepository;

/**
 * Bảng trợ giúp của trang Làm bài (US-GEO-401-SC3, 404, 405, 406, 407, 408): trạng thái (tiến độ
 * k/3, gợi ý đã xem, công tắc của giáo viên, trạng thái lời giải và thông điệp khóa) và mở lời giải
 * từng bước. Trạng thái không bao giờ chứa nội dung gợi ý chưa xin, bước giải hay đáp số
 * (BR-GEO-177, 179; ADR-GEO-09); lời giải chỉ ra qua cổng fn_solution_gate.
 */
@Service
public class SolutionService {

  public record HintView(int level, String title, String body) {}

  /**
   * @param state open | release | hints | no_signature | inherited
   * @param message thông điệp khóa (MSG-GEO-30, 31, 58) hoặc null khi xem được
   * @param releaseAt giờ mở của bài giao (bài lớp) hoặc của bài giao gốc (kế thừa khóa)
   */
  public record SolutionState(String state, String message, Instant releaseAt) {}

  public record HelpState(
      String kind,
      int k,
      boolean allowHint,
      boolean allowQa,
      String hintMessage,
      String qaMessage,
      List<HintView> hints,
      SolutionState solution) {}

  /**
   * Lời giải từng bước. steps: [{title, body, why, highlight?, draw?}] (khóa highlight/draw: phần
   * tô và phần vẽ thêm của bước, US-GEO-407, 408).
   */
  public record SolutionView(
      String finalAnswer,
      JsonNode steps,
      String machineLabel,
      String humanLabel,
      int k,
      boolean firstView) {}

  private final HelpRepository repo;
  private final JsonMapper mapper;

  public SolutionService(HelpRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  public HelpState state(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    HelpRepository.State s = repo.state(p.userId(), assignmentId);
    if (!"OK".equals(s.status())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    List<HintView> hints =
        s.allowHint()
            ? repo.seenHints(p.userId(), assignmentId).stream()
                .map(h -> new HintView(h.level(), HelpMessages.hintTitle(h.level()), h.body()))
                .toList()
            : List.of();
    return new HelpState(
        s.kind(),
        s.solutionViewed() ? 3 : s.hintLevelMax(),
        s.allowHint(),
        s.allowQa(),
        s.allowHint() ? null : HelpMessages.MSG_32,
        s.allowQa() ? null : HelpMessages.MSG_33,
        hints,
        new SolutionState(
            s.solutionState(), HelpMessages.lockMessage(s.solutionState()), s.releaseAt()));
  }

  /**
   * Học sinh bấm "Xem lời giải" / mở tab "Lời giải" khi đã mở: trả lời giải và ghi sự kiện mở lời
   * giải đúng một lần (BR-GEO-186). Còn khóa: FORBIDDEN với thông điệp khóa, không có bước hay đáp
   * số.
   */
  public SolutionView open(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    HelpRepository.Opened o = repo.openSolution(p.userId(), assignmentId);
    return switch (o.status()) {
      case "OK" ->
          new SolutionView(
              o.finalAnswer(),
              mapper.readTree(o.stepsJson()),
              "verified".equals(o.verifyStatus()) ? "Đã kiểm chứng" : "Cần xem lại",
              "AI soạn",
              3,
              o.firstView());
      case "LOCKED" -> {
        HelpRepository.State s = repo.state(p.userId(), assignmentId);
        String msg = s.solutionState() == null ? null : HelpMessages.lockMessage(s.solutionState());
        throw new AppException(
            ErrorCode.FORBIDDEN, msg == null ? HelpMessages.RELEASE_LOCKED : msg);
      }
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    };
  }
}
