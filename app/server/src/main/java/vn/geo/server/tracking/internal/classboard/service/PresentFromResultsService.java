package vn.geo.server.tracking.internal.classboard.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Column;
import vn.geo.server.tracking.internal.classboard.repo.PresentFromResultsRepository;
import vn.geo.server.tracking.internal.classboard.repo.PresentFromResultsRepository.Source;

/**
 * US-S3E6-01: "Chữa bài này" từ thẻ "Cả lớp vướng ở đâu" sang Trình chiếu đúng đề, đúng bước
 * (BR-S3E6-02…06).
 *
 * <ul>
 *   <li>Chỉ chủ lớp (NOT_FOUND cho lớp ngoài quan hệ, FORBIDDEN cho học sinh) — kiểm qua phạm vi
 *       của Kết quả lớp.
 *   <li>{@link #preflight}: lời giải của đề còn khóa với lớp (giờ máy chủ trước giờ mở, hoặc chưa
 *       đặt giờ) thì giao diện hỏi lại MSG-S3-91; máy học sinh vẫn khóa theo giờ mở (không đổi gì).
 *   <li>{@link #open}: trả đề, các bước lời giải của phiên bản hiện hành và bước bắt đầu; bước của
 *       thẻ không còn (đề có phiên bản mới ít bước hơn) thì bắt đầu ở bước 1 kèm MSG-S3-92.
 *   <li>Mỗi lần mở hợp lệ ghi một sự kiện đo (mã giáo viên, mã đề, giờ); các lần trong 5 giây gộp
 *       một và trả cùng mã phiên. Phản hồi không có tên, mã hay số liệu theo em (BR-S3E6-04).
 * </ul>
 */
@Service
public class PresentFromResultsService {

  public static final long MERGE_SECONDS = 5;
  public static final String MSG_VERSION = "Đề đã được sửa. Mở Trình chiếu từ đầu đề.";
  public static final String MSG_LOCKED_CHIP = "Máy học sinh vẫn khóa lời giải";

  public record Preflight(boolean locked, Instant releaseAt, Instant serverNow) {}

  public record Presentation(
      UUID sessionId,
      String title,
      String statement,
      List<String> steps,
      int startStep,
      boolean locked,
      String notice) {}

  private final ClassBoardService board;
  private final PresentFromResultsRepository repo;
  private final JsonMapper mapper;
  private final TransactionTemplate tx;
  private final Clock clock;

  public PresentFromResultsService(
      ClassBoardService board,
      PresentFromResultsRepository repo,
      JsonMapper mapper,
      TransactionTemplate tx,
      Clock clock) {
    this.board = board;
    this.repo = repo;
    this.mapper = mapper;
    this.tx = tx;
    this.clock = clock;
  }

  private record Target(ClassBoardService.Scope scope, Column column) {}

  private Target target(UUID classId, UUID setId, int position) {
    Roles.require(Role.TEACHER);
    ClassBoardService.Scope s = board.scope(classId, setId);
    Column c =
        s.columns().stream()
            .filter(x -> x.position() == position)
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new Target(s, c);
  }

  private boolean locked(UUID setId, int position, Instant now) {
    Instant at = repo.releaseAt(setId, position).orElse(null);
    return at == null || now.isBefore(at);
  }

  public Preflight preflight(UUID classId, UUID setId, int position) {
    target(classId, setId, position);
    Instant now = clock.instant();
    Instant at = repo.releaseAt(setId, position).orElse(null);
    return new Preflight(at == null || now.isBefore(at), at, now);
  }

  public Presentation open(UUID classId, UUID setId, int position, Integer step) {
    Target t = target(classId, setId, position);
    Column c = t.column();
    Source src =
        repo.source(t.scope().teacher().userId(), c.contentOrgId(), c.problemVersionId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> steps = stepTexts(src.stepsJson());
    int want = step == null || step < 1 ? 1 : step;
    String notice = null;
    if (step != null && want > steps.size()) {
      // bước của thẻ không còn ở phiên bản hiện hành (BR-S3E6-06)
      notice = MSG_VERSION;
      want = 1;
    }
    Instant now = clock.instant();
    UUID teacher = t.scope().teacher().userId();
    UUID session =
        tx.execute(
            s -> repo.recordOnce(Uuid7.next(), teacher, src.versionId(), now, MERGE_SECONDS));
    return new Presentation(
        session,
        c.title(),
        src.statement(),
        steps,
        steps.isEmpty() ? 0 : want,
        locked(t.scope().setId(), position, now),
        notice);
  }

  /** Móc kiểm thử (dev/test): giờ mở lời giải của bộ, tính từ giờ máy chủ (null = Chưa đặt giờ). */
  public int setReleaseForTest(UUID setId, Long minutesFromNow) {
    Instant at = minutesFromNow == null ? null : clock.instant().plusSeconds(minutesFromNow * 60);
    return tx.execute(s -> repo.setReleaseForTest(setId, at));
  }

  /** Bước lời giải: "tiêu đề: nội dung" (lý do "why" không chiếu). */
  List<String> stepTexts(String json) {
    List<String> out = new ArrayList<>();
    if (json == null || json.isBlank()) {
      return out;
    }
    JsonNode arr = mapper.readTree(json);
    if (arr == null || !arr.isArray()) {
      return out;
    }
    for (JsonNode n : arr) {
      if (n.isString()) {
        out.add(n.asString());
        continue;
      }
      String title = n.path("title").asString("");
      String body = n.path("body").asString("");
      out.add(title.isEmpty() ? body : body.isEmpty() ? title : title + ": " + body);
    }
    return out;
  }
}
