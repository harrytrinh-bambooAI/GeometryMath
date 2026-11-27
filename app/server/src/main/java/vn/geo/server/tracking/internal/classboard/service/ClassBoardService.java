package vn.geo.server.tracking.internal.classboard.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.classboard.domain.BoardRules;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Column;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Member;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Work;

/**
 * S3-E3 — Kết quả lớp (C3-04): bảng lớp × bài (US-S3E3-01) và bằng chứng một ô (US-S3E3-02). Không
 * có tổng, phần trăm, điểm hay hạng ở bất kỳ thân phản hồi nào (BR-S3E3-03); hàng xếp theo tên theo
 * chữ cái tiếng Việt, hòa theo mã người dùng (BR-S3E3-04); chỉ chủ lớp đọc (BR-S3E3-07).
 */
@Service
public class ClassBoardService {

  static final ListKind BOARD = ListKind.byId("tracking.classboard");
  static final int PAGE = 100;

  /** Cửa sổ đọc nhật ký gợi ý (event_log chia theo tháng: không quét mọi partition). */
  static final Duration EVENT_SPAN = Duration.ofDays(366);

  public record ClassOption(UUID id, String name) {}

  public record SetOption(UUID id, String title) {}

  public record Sets(String className, boolean hasActiveStudents, List<SetOption> items) {}

  public record ColumnView(int position, String title) {}

  /**
   * Một hàng của bảng. {@code userId} (UUID) là khóa mở bằng chứng và trang bài làm ({@code hs=});
   * mã người dùng chỉ để hiện, không vào địa chỉ trang hay chuỗi truy vấn (BR-S3E3-40, PO-74). Em
   * vào lớp bằng tên (chưa có mã) có userCode rỗng.
   */
  public record Row(
      UUID userId, String displayName, String userCode, boolean left, List<String> cells) {}

  public record Board(
      UUID setId,
      String title,
      String className,
      List<ColumnView> columns,
      List<Row> rows,
      String nextCursor,
      Instant updatedAt) {}

  public record AttemptView(Instant at, String value, boolean matched) {}

  public record HintView(Instant at, int level) {}

  public record QuestionView(Instant at, String text) {}

  public record Evidence(
      UUID userId,
      String displayName,
      String userCode,
      boolean left,
      ColumnView item,
      String status,
      Instant openedAt,
      List<AttemptView> attempts,
      List<HintView> hints,
      List<QuestionView> questions,
      int stoppedStep,
      String statement,
      Instant updatedAt) {}

  /** Bối cảnh đã kiểm quyền của một bộ: dùng chung cho bảng, bằng chứng và "Cả lớp vướng ở đâu". */
  record Scope(
      AuthPrincipal teacher,
      String className,
      UUID setId,
      String setTitle,
      List<Column> columns,
      List<Member> members) {

    List<UUID> assignmentIds() {
      return columns.stream().map(Column::assignmentId).toList();
    }
  }

  private final ClassBoardRepository repo;
  private final Paging paging;

  public ClassBoardService(ClassBoardRepository repo, Paging paging) {
    this.repo = repo;
    this.paging = paging;
  }

  public List<ClassOption> classes() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.classes(p.userId()).stream().map(c -> new ClassOption(c.id(), c.name())).toList();
  }

  /** Bộ Đã giao của lớp, mới giao nhất trước (bộ mặc định là phần tử đầu, BR-S3E3-08). */
  public Sets sets(UUID classId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    var cls =
        repo.ownedClass(p.userId(), classId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    boolean active = repo.members(classId).stream().anyMatch(m -> !m.left());
    return new Sets(
        cls.name(),
        active,
        repo.sets(classId).stream().map(s -> new SetOption(s.id(), s.title())).toList());
  }

  Scope scope(UUID classId, UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    var cls =
        repo.ownedClass(p.userId(), classId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    var set = repo.set(classId, setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<Column> columns = repo.columns(setId);
    if (columns.isEmpty()) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    List<Member> members = new ArrayList<>(repo.members(classId));
    members.sort(BoardRules.byVietnameseName(Member::displayName, ClassBoardService::codeKey));
    return new Scope(p, cls.name(), set.id(), set.title(), columns, members);
  }

  /** Bảng lớp × bài: tối đa 100 hàng mỗi trang, con trỏ theo hàng cuối, không tổng số bản ghi. */
  public Board board(UUID classId, UUID setId, Integer limit, String cursor) {
    Roles.require(Role.TEACHER);
    PageRequest req = paging.request(BOARD, limit == null ? PAGE : limit, cursor);
    Scope s = scope(classId, setId);
    Map<UUID, Map<UUID, Work>> works = worksByUser(repo.works(s.assignmentIds()));
    List<Member> rows = new ArrayList<>();
    for (Member m : s.members()) {
      if (!m.left() || works.containsKey(m.userId())) {
        rows.add(m); // em Đã rời chỉ có hàng khi đã có bài làm trong bộ (BR-S3E3-01)
      }
    }
    int start = 0;
    if (req.after() != null) {
      UUID last = req.after().id();
      start = -1;
      for (int i = 0; i < rows.size(); i++) {
        if (rows.get(i).userId().equals(last)) {
          start = i + 1;
          break;
        }
      }
      if (start < 0) {
        throw new AppException(ErrorCode.INVALID_CURSOR);
      }
    }
    List<Member> fetched = rows.subList(start, Math.min(rows.size(), start + req.fetchSize()));
    Page<Member> page = paging.page(fetched, req, m -> KeysetCursor.ofId(m.userId()));
    List<Row> out = new ArrayList<>();
    for (Member m : page.items()) {
      Map<UUID, Work> mine = works.getOrDefault(m.userId(), Map.of());
      List<String> cells = new ArrayList<>();
      for (Column c : s.columns()) {
        cells.add(statusOf(mine.get(c.assignmentId())));
      }
      out.add(new Row(m.userId(), m.displayName(), codeKey(m), m.left(), cells));
    }
    return new Board(
        s.setId(),
        s.setTitle(),
        s.className(),
        s.columns().stream().map(ClassBoardService::view).toList(),
        out,
        page.nextCursor(),
        Instant.now());
  }

  /** Bằng chứng của một ô (em × đề). {@code withStatement} = "Mở bài làm" chỉ đọc (BR-S3E3-24). */
  public Evidence evidence(
      UUID classId, UUID setId, int position, UUID userId, boolean withStatement) {
    Scope s = scope(classId, setId);
    Column col =
        s.columns().stream()
            .filter(c -> c.position() == position)
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Member m =
        s.members().stream()
            .filter(x -> userId != null && userId.equals(x.userId()))
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<UUID> one = List.of(col.assignmentId());
    var attempts = repo.attempts(one, m.userId());
    var hints = repo.hints(one, m.userId(), Instant.now().minus(EVENT_SPAN));
    var questions = repo.questions(one, m.userId());
    var progress = repo.progress(col.assignmentId(), m.userId());
    boolean correct = attempts.stream().anyMatch(a -> "correct".equals(a.outcome()));
    boolean wrong = attempts.stream().anyMatch(a -> "wrong".equals(a.outcome()));
    boolean any =
        progress.isPresent() || !attempts.isEmpty() || !hints.isEmpty() || !questions.isEmpty();
    int step =
        Math.max(
            progress.map(ClassBoardRepository.Progress::hintLevelMax).orElse(0),
            hints.stream().mapToInt(ClassBoardRepository.HintUse::level).max().orElse(0));
    return new Evidence(
        m.userId(),
        m.displayName(),
        codeKey(m),
        m.left(),
        view(col),
        BoardRules.status(any, correct, wrong),
        progress.map(ClassBoardRepository.Progress::openedAt).orElse(null),
        attempts.stream()
            .filter(a -> !"syntax".equals(a.outcome()))
            .map(a -> new AttemptView(a.at(), a.value(), "correct".equals(a.outcome())))
            .toList(),
        hints.stream().map(h -> new HintView(h.at(), h.level())).toList(),
        questions.stream().map(q -> new QuestionView(q.at(), q.text())).toList(),
        step,
        withStatement && any
            ? repo.statement(col.contentOrgId(), col.problemVersionId()).orElse(null)
            : null,
        Instant.now());
  }

  static Map<UUID, Map<UUID, Work>> worksByUser(List<Work> works) {
    Map<UUID, Map<UUID, Work>> out = new HashMap<>();
    for (Work w : works) {
      out.computeIfAbsent(w.userId(), k -> new HashMap<>()).put(w.assignmentId(), w);
    }
    return out;
  }

  static Set<UUID> activeIds(List<Member> members) {
    Set<UUID> ids = new HashSet<>();
    for (Member m : members) {
      if (!m.left()) {
        ids.add(m.userId());
      }
    }
    return ids;
  }

  static String statusOf(Work w) {
    return w == null ? BoardRules.TODO : BoardRules.status(true, w.correct(), w.wrong());
  }

  static ColumnView view(Column c) {
    return new ColumnView(c.position(), BoardRules.shortTitle(c.title()));
  }

  static String codeKey(Member m) {
    return m.userCode() == null ? "" : m.userCode();
  }
}
