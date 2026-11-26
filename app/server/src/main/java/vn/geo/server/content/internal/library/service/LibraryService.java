package vn.geo.server.content.internal.library.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.LibraryQuota;
import vn.geo.server.content.internal.library.repo.LibraryRepository;
import vn.geo.server.content.internal.library.repo.LibraryRepository.DocCard;
import vn.geo.server.content.internal.library.repo.LibraryRepository.LibraryRow;
import vn.geo.server.content.internal.library.repo.LibraryRepository.SubmissionRow;
import vn.geo.server.content.internal.library.service.LibraryViews.Card;
import vn.geo.server.content.internal.library.service.LibraryViews.CardPage;
import vn.geo.server.content.internal.library.service.LibraryViews.Created;
import vn.geo.server.content.internal.library.service.LibraryViews.Decision;
import vn.geo.server.content.internal.library.service.LibraryViews.DocDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.Hint;
import vn.geo.server.content.internal.library.service.LibraryViews.HistoryRow;
import vn.geo.server.content.internal.library.service.LibraryViews.LibraryDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.LockedSolution;
import vn.geo.server.content.internal.library.service.LibraryViews.Queue;
import vn.geo.server.content.internal.library.service.LibraryViews.QueueDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.QueueRow;
import vn.geo.server.content.internal.library.service.LibraryViews.Similar;
import vn.geo.server.content.internal.library.service.LibraryViews.StudentLibraryItem;
import vn.geo.server.iam.api.ContentReviewerRole;

/**
 * S2-E6 — Kho đề (khung C2-01): ngăn "Của tôi" / "Kho chung" và lọc loại hình (US-S2E6-01), người
 * soát duyệt đề vào Kho chung (US-S2E6-02), riêng tư của Của tôi và giới hạn thêm đề (US-S2E6-03).
 *
 * <ul>
 *   <li>Mọi đường giáo viên đòi vai TEACHER; học sinh FORBIDDEN (BR-S2E6-12).
 *   <li>Đề Của tôi chỉ chủ thấy; người soát chỉ thấy đề đang Chờ soát; còn lại NOT_FOUND, không
 *       phân biệt "không có" và "của người khác" (BR-S2E6-11).
 *   <li>Kho chung là bản chép độc lập lúc duyệt (BR-S2E6-10), chép trong CSDL (fn_library_accept).
 * </ul>
 */
@Service
public class LibraryService implements LibraryQuota {

  static final ListKind MINE = ListKind.of("content.shelf-mine", KeyType.TIMESTAMP);
  static final ListKind SHARED = ListKind.of("content.shelf-library", KeyType.TIMESTAMP);
  static final int MAX_PAGE = 50;
  private static final Set<String> KINDS = Set.of("all", "space", "plane");

  private final LibraryRepository repo;
  private final Paging paging;
  private final ContentReviewerRole roles;
  private final JsonMapper mapper;
  private final int createLimit;
  private final Duration createWindow;
  private final int pendingLimit;
  private volatile Clock clock = Clock.systemUTC();

  public LibraryService(
      LibraryRepository repo,
      Paging paging,
      ContentReviewerRole roles,
      JsonMapper mapper,
      @Value("${geo.s2e6.create-limit:20}") int createLimit,
      @Value("${geo.s2e6.create-window:PT60M}") Duration createWindow,
      @Value("${geo.s2e6.pending-limit:10}") int pendingLimit) {
    this.repo = repo;
    this.paging = paging;
    this.roles = roles;
    this.mapper = mapper;
    this.createLimit = createLimit;
    this.createWindow = createWindow;
    this.pendingLimit = pendingLimit;
  }

  /** Chỉ dùng trong kiểm thử: đồng hồ giả (US-S2E6-03-BV2). null = đồng hồ hệ thống. */
  public void overrideClockForTest(Clock testClock) {
    this.clock = testClock == null ? Clock.systemUTC() : testClock;
  }

  Instant now() {
    return clock.instant();
  }

  // ---------------------------------------------------------------- danh sách

  public CardPage list(String shelf, String kind, String q, Integer limit, String cursor) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String k = kind == null || kind.isBlank() ? "all" : kind;
    if (!KINDS.contains(k)) {
      throw AppException.invalidField("kind", "Loại hình là all, space hoặc plane.");
    }
    if (limit != null && limit > MAX_PAGE) {
      throw AppException.invalidField("limit", "Tối đa " + MAX_PAGE + " đề một lần.");
    }
    String text = q == null ? "" : q.strip();
    if (text.length() > 100) {
      throw AppException.invalidField("q", "Từ khóa tối đa 100 ký tự.");
    }
    boolean reviewer = roles.isContentReviewer(me.userId());
    if ("library".equals(shelf)) {
      PageRequest req = paging.request(SHARED, limit, cursor);
      KeysetCursor after = req.after();
      List<LibraryRow> rows =
          repo.library(
              k,
              text,
              after == null ? null : after.instant(0),
              after == null ? null : after.id(),
              req.fetchSize());
      Page<LibraryRow> page = paging.page(rows, req, r -> KeysetCursor.of(r.id(), r.acceptedAt()));
      return new CardPage(
          page.items().stream().map(LibraryService::libraryCard).toList(),
          page.nextCursor(),
          reviewer);
    }
    if (shelf != null && !"mine".equals(shelf)) {
      throw AppException.invalidField("shelf", "Ngăn là mine hoặc library.");
    }
    PageRequest req = paging.request(MINE, limit, cursor);
    KeysetCursor after = req.after();
    List<DocCard> rows =
        repo.mine(
            me.userId(),
            k,
            text,
            after == null ? null : after.instant(0),
            after == null ? null : after.id(),
            req.fetchSize());
    Page<DocCard> page = paging.page(rows, req, r -> KeysetCursor.of(r.id(), r.createdAt()));
    return new CardPage(
        page.items().stream().map(LibraryService::docCard).toList(), page.nextCursor(), reviewer);
  }

  static Card docCard(DocCard d) {
    return new Card(
        d.id(),
        "mine",
        d.title(),
        d.kind(),
        LibraryLabels.kind(d.kind()),
        LibraryLabels.machine(d.machineStatus()),
        LibraryLabels.human(d.humanStatus()),
        LibraryLabels.status(d.shelfStatus()),
        LibraryLabels.statusLabel(d.shelfStatus()),
        "rejected".equals(d.shelfStatus()) ? d.reason() : null,
        d.createdAt());
  }

  static Card libraryCard(LibraryRow l) {
    return new Card(
        l.id(),
        "library",
        l.title(),
        l.kind(),
        LibraryLabels.kind(l.kind()),
        LibraryLabels.machine(l.machineStatus()),
        LibraryLabels.human("teacher_approved"),
        "accepted",
        LibraryLabels.statusLabel("accepted"),
        null,
        l.acceptedAt());
  }

  // ---------------------------------------------------------------- chi tiết

  public DocDetail doc(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocCard d = repo.doc(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    boolean owner = d.ownerId().equals(me.userId());
    if (!owner && !(repo.pending(id) && roles.isContentReviewer(me.userId()))) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    String blocked = null;
    if (!"teacher_approved".equals(d.humanStatus())) {
      blocked = LibraryLabels.APPROVE_FIRST;
    } else if ("pending".equals(d.shelfStatus())) {
      blocked = LibraryLabels.ALREADY_PENDING;
    } else if ("accepted".equals(d.shelfStatus())) {
      blocked = LibraryLabels.ALREADY_IN;
    }
    List<HistoryRow> history =
        repo.history(id).stream()
            .map(h ->
                new HistoryRow(
                    h.status(),
                    LibraryLabels.statusLabel(h.status()),
                    h.reason(),
                    h.decidedAt() == null ? h.submittedAt() : h.decidedAt()))
            .toList();
    return new DocDetail(
        d.id(),
        d.title(),
        d.statement(),
        d.kind(),
        LibraryLabels.kind(d.kind()),
        LibraryLabels.machine(d.machineStatus()),
        LibraryLabels.human(d.humanStatus()),
        LibraryLabels.status(d.shelfStatus()),
        LibraryLabels.statusLabel(d.shelfStatus()),
        "rejected".equals(d.shelfStatus()) ? d.reason() : null,
        owner,
        owner && blocked == null,
        owner ? blocked : null,
        owner ? history : List.of());
  }

  public LibraryDetail libraryItem(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    LibraryRow l = repo.libraryItem(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> steps =
        repo.libraryStepsForTeacher(me.userId(), id).map(this::stepTexts).orElse(List.of());
    return new LibraryDetail(
        l.id(),
        l.title(),
        l.statement(),
        l.kind(),
        LibraryLabels.kind(l.kind()),
        LibraryLabels.machine(l.machineStatus()),
        LibraryLabels.human("teacher_approved"),
        l.hint() == null ? LibraryLabels.DEFAULT_HINT : l.hint(),
        steps,
        l.acceptedAt());
  }

  /** Bước giải dạng chữ: phần tử là chuỗi hoặc đối tượng có "text". */
  List<String> stepTexts(String json) {
    List<String> out = new ArrayList<>();
    JsonNode arr = mapper.readTree(json);
    for (JsonNode s : arr) {
      out.add(s.isString() ? s.asString() : s.path("text").asString());
    }
    return out;
  }

  // ---------------------------------------------------------------- học sinh (BR-S2E6-14)

  public StudentLibraryItem studentItem(UUID id) {
    Roles.require(Role.STUDENT);
    LibraryRow l = repo.libraryItem(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new StudentLibraryItem(l.id(), l.title(), l.statement(), l.kind());
  }

  public Hint studentHint(UUID id) {
    Roles.require(Role.STUDENT);
    LibraryRow l = repo.libraryItem(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new Hint(l.hint() == null ? LibraryLabels.DEFAULT_HINT : l.hint());
  }

  /** Lời giải đề Kho chung phía học sinh: luôn khóa; mở chỉ qua bài giao (giờ mở của bộ). */
  public LockedSolution studentSolution(UUID id) {
    Roles.require(Role.STUDENT);
    repo.libraryItem(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new LockedSolution(true, LibraryLabels.STUDENT_LOCKED);
  }

  // ---------------------------------------------------------------- gửi soát (US-S2E6-02)

  @Transactional
  public Decision submit(UUID docId, String hint) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String h = hint == null || hint.isBlank() ? null : hint.strip();
    if (h != null && h.codePointCount(0, h.length()) > 500) {
      throw AppException.invalidField("hint", "Gợi ý tối đa 500 ký tự.");
    }
    DocCard d = repo.doc(docId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!d.ownerId().equals(me.userId())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    repo.lockOwner(me.userId(), 23L);
    repo.lockDoc(docId);
    if (repo.pending(docId)) { // gửi lại khi đang Chờ soát: giữ một yêu cầu (BR-S2E6-06)
      return new Decision(docId, "pending", LibraryLabels.statusLabel("pending"), null);
    }
    UUID version =
        repo.latestApprovedVersion(docId)
            .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, LibraryLabels.APPROVE_FIRST));
    DocCard fresh = repo.doc(docId).orElseThrow();
    if ("accepted".equals(fresh.shelfStatus())) {
      throw new AppException(ErrorCode.CONFLICT, LibraryLabels.ALREADY_IN);
    }
    if (repo.pendingCount(me.userId()) >= pendingLimit) {
      throw new AppException(ErrorCode.CONFLICT, LibraryLabels.PENDING_LIMIT);
    }
    try {
      repo.insertSubmission(Uuid7.next(), docId, version, me.userId(), h, now());
    } catch (DuplicateKeyException e) {
      return new Decision(docId, "pending", LibraryLabels.statusLabel("pending"), null);
    }
    return new Decision(docId, "pending", LibraryLabels.statusLabel("pending"), null);
  }

  // ---------------------------------------------------------------- người soát

  private AuthPrincipal reviewer() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (!roles.isContentReviewer(me.userId())) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return me;
  }

  public Queue queue() {
    AuthPrincipal me = reviewer();
    return new Queue(
        repo.queue().stream()
            .map(s ->
                new QueueRow(
                    s.id(),
                    s.docId(),
                    s.title(),
                    s.kind(),
                    LibraryLabels.kind(s.kind()),
                    s.ownerName(),
                    s.ownerId().equals(me.userId()),
                    s.submittedAt()))
            .toList());
  }

  public QueueDetail queueItem(UUID submissionId) {
    AuthPrincipal me = reviewer();
    SubmissionRow s =
        repo.submission(submissionId, false)
            .filter(x -> "pending".equals(x.status()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> steps =
        repo.submissionSteps(me.userId(), submissionId).map(this::stepTexts).orElse(List.of());
    return new QueueDetail(
        s.id(),
        s.docId(),
        s.title(),
        s.statement(),
        LibraryLabels.kind(s.kind()),
        LibraryLabels.machine(s.machineStatus()),
        LibraryLabels.human(s.humanStatus()),
        steps,
        s.ownerId().equals(me.userId()));
  }

  private SubmissionRow pendingForDecision(AuthPrincipal me, UUID submissionId) {
    SubmissionRow s =
        repo.submission(submissionId, true)
            .filter(x -> "pending".equals(x.status()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (s.ownerId().equals(me.userId())) {
      throw new AppException(ErrorCode.FORBIDDEN, LibraryLabels.SELF_REVIEW);
    }
    return s;
  }

  @Transactional
  public Decision accept(UUID submissionId) {
    AuthPrincipal me = reviewer();
    SubmissionRow s = pendingForDecision(me, submissionId);
    UUID item = Uuid7.next();
    if (repo.accept(me.userId(), submissionId, item, now()) != 1) {
      throw new AppException(ErrorCode.CONFLICT);
    }
    return new Decision(s.docId(), "accepted", LibraryLabels.statusLabel("accepted"), item);
  }

  @Transactional
  public Decision reject(UUID submissionId, String reason) {
    AuthPrincipal me = reviewer();
    String r = reason == null ? "" : reason.strip();
    int len = r.codePointCount(0, r.length());
    if (len < 5 || len > 500) {
      throw AppException.invalidField("reason", LibraryLabels.REASON_RANGE);
    }
    SubmissionRow s = pendingForDecision(me, submissionId);
    if (repo.reject(me.userId(), submissionId, r, now()) != 1) {
      throw new AppException(ErrorCode.CONFLICT);
    }
    return new Decision(s.docId(), "rejected", LibraryLabels.statusLabel("rejected"), null);
  }

  // ---------------------------------------------------------------- tạo đề, giới hạn (US-S2E6-03)

  @Override
  @Transactional
  public void consume(Channel channel, UUID refId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    repo.lockOwner(me.userId(), 63L);
    Instant now = now();
    Instant since = now.minus(createWindow);
    if (repo.createdSince(me.userId(), since) >= createLimit) {
      Instant oldest = repo.oldestSince(me.userId(), since).orElse(now);
      Duration wait = Duration.between(now, oldest.plus(createWindow));
      if (wait.isNegative() || wait.isZero()) {
        wait = Duration.ofSeconds(1);
      }
      throw AppException.rateLimited(wait, LibraryLabels.CREATE_LIMIT);
    }
    repo.insertCreateEvent(
        Uuid7.next(), me.userId(), channel.name().toLowerCase(java.util.Locale.ROOT), refId, now);
  }

  @Transactional
  public Created create(String title, String statement) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String t = title == null ? "" : title.strip();
    String s = statement == null ? "" : statement.strip();
    if (s.isEmpty() || s.length() > 8000) {
      throw AppException.invalidField("statement", "Đề bài từ 1 đến 8000 ký tự.");
    }
    if (t.isEmpty()) {
      t = s.length() > 60 ? s.substring(0, 60) : s;
    }
    if (t.length() > 200) {
      throw AppException.invalidField("title", "Tên đề tối đa 200 ký tự.");
    }
    UUID id = Uuid7.next();
    consume(Channel.TEXT, id);
    repo.insertDoc(id, me.userId(), t, s, null, now());
    return new Created(id, t);
  }

  @Transactional
  public Created copy(UUID libraryId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    LibraryRow l =
        repo.libraryItem(libraryId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    UUID id = Uuid7.next();
    consume(Channel.COPY, id);
    Instant at = now();
    repo.insertDoc(
        id, me.userId(), l.title(), l.statement(), repo.libraryFigure(libraryId).orElse(null), at);
    repo.libraryStepsForTeacher(me.userId(), libraryId)
        .ifPresent(steps -> repo.insertFirstVersion(Uuid7.next(), id, steps, me.userId(), at));
    return new Created(id, l.title());
  }

  /** Đối chiếu trùng khi thêm đề: chỉ đề của chính mình và Kho chung (BR-S2E6-11). */
  public Similar similar(String statement) {
    Roles.require(Role.TEACHER);
    String s = statement == null ? "" : statement.strip();
    if (s.length() < 3 || s.length() > 200) {
      throw AppException.invalidField("statement", "Đoạn đối chiếu từ 3 đến 200 ký tự.");
    }
    List<Card> out = new ArrayList<>(list("mine", "all", s, MAX_PAGE, null).items());
    out.addAll(list("library", "all", s, MAX_PAGE, null).items());
    return new Similar(out);
  }
}
