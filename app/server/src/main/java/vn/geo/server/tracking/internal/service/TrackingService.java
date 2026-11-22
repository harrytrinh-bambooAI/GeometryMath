package vn.geo.server.tracking.internal.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.knowledge.api.LectureCatalog;
import vn.geo.server.learning.api.AnswerGuardApi;
import vn.geo.server.tracking.internal.repo.TrackingRepository;
import vn.geo.server.tracking.internal.repo.TrackingRepository.ProgressRow;
import vn.geo.server.tracking.internal.repo.TrackingRepository.QuestionRow;

/**
 * Hỏi đáp qua cổng AI (US-GEO-103), câu trả lời đã lưu chỉ người hỏi đọc (D-GEO-159), dữ liệu của
 * học sinh chỉ của chính em hoặc giáo viên lớp em (BR-GEO-12, 20), Theo dõi lớp có đệm Redis mang
 * tiền tố trường (NFR-GEO-034, TC-GEO-0804).
 */
@Service
public class TrackingService {

  /** Phiên bản Kiểm soát đáp án: E1 chưa có bộ kiểm (ADR-GEO-14 thuộc E6); ghi rõ để truy vết. */
  static final String GUARD_VERSION = "g0-e1";

  /** MSG-GEO-36 (BR-GEO-190): câu hỏi ngoài kho bài giảng, không kèm nguồn. */
  static final String MSG_36 = "Giáo trình của lớp chưa có phần này. Em hãy hỏi giáo viên nhé.";

  static final String MSG_33 = "Giáo viên đã tắt hỏi đáp cho bài này.";
  static final String MSG_37 = "Câu hỏi cần từ 1 đến 1000 ký tự.";
  static final ListKind LOG_LIST = ListKind.of("qa.log", KeyType.LONG);
  private static final UUID NIL = new UUID(0, 0);

  private static final Logger log = LoggerFactory.getLogger(TrackingService.class);
  private static final int QUESTION_MAX = 1000;
  private static final Duration MONITOR_TTL = Duration.ofSeconds(5);

  /**
   * Nguồn của câu trả lời: một bài giảng có trong kho (BR-GEO-189). label: "Giáo án mẫu" | "AI
   * soạn".
   */
  public record Source(String slug, String title, String label) {}

  public record Asked(long id, String question, String replyKind, String reply, Source source) {}

  public record SavedAnswer(
      long id,
      UUID assignmentId,
      Instant askedAt,
      String question,
      String replyKind,
      String reply,
      Source source) {}

  /** Một dòng nhật ký hỏi đáp (US-GEO-410-SC1): câu hỏi, tên bài, giờ hỏi. */
  public record LogItem(
      long id, UUID assignmentId, String problemTitle, String question, Instant askedAt) {}

  public record Question(
      long id, UUID assignmentId, String question, Instant askedAt, String askerName) {}

  public record StudentProgress(
      UUID assignmentId,
      UUID userId,
      String displayName,
      boolean opened,
      int k,
      String label,
      int hintLevel,
      int askCount,
      int toolCount,
      int activeMinutes) {}

  public record Monitor(
      UUID assignmentId,
      String problemCode,
      String className,
      String releaseState,
      int studentCount,
      int openedCount,
      List<StudentProgress> students,
      List<Question> questions) {}

  private final TrackingRepository repo;
  private final AiGateway ai;
  private final StringRedisTemplate redis;
  private final RedisKeys keys;
  private final JsonMapper mapper;
  private final AnswerGuardApi guard;
  private final LectureCatalog lectures;
  private final Paging paging;

  public TrackingService(
      TrackingRepository repo,
      AiGateway ai,
      StringRedisTemplate redis,
      RedisKeys keys,
      JsonMapper mapper,
      AnswerGuardApi guard,
      LectureCatalog lectures,
      Paging paging) {
    this.repo = repo;
    this.ai = ai;
    this.redis = redis;
    this.keys = keys;
    this.mapper = mapper;
    this.guard = guard;
    this.lectures = lectures;
    this.paging = paging;
  }

  /**
   * Gửi câu hỏi tới cổng AI. Prompt chỉ có đề và câu hỏi, không có tên, mã lớp, định danh
   * (BR-GEO-108). Lỗi/quá thời gian: không lưu gì, ném TIMEOUT/DEPENDENCY_UNAVAILABLE (MSG-GEO-14).
   */
  public Asked ask(UUID assignmentId, String question, UUID requestId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    String q = question == null ? "" : question.strip();
    if (q.isEmpty() || q.codePointCount(0, q.length()) > QUESTION_MAX) {
      throw AppException.invalidField("question", MSG_37);
    }
    TrackingRepository.QaContext ctx =
        repo.qaContext(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!ctx.allowQa()) {
      throw new AppException(ErrorCode.FORBIDDEN, MSG_33);
    }
    // Lũy đẳng (RV-18): gửi lại cùng requestId => trả câu trả lời ĐÃ LƯU, không gọi AI lần nữa.
    if (requestId != null) {
      Optional<Asked> saved =
          repo.qaIdByRequest(p.userId(), assignmentId, requestId)
              .flatMap(id -> savedAsked(p.userId(), id));
      if (saved.isPresent()) {
        return saved.get();
      }
    }
    String prompt =
        "Vai trò: trợ lý học hình học không gian, gợi mở, không nêu đáp số.\n"
            + "Đề bài: "
            + ctx.statement()
            + "\nCâu hỏi: "
            + q;
    Reply rep = interpret(ai.complete(new AiRequest("qa", prompt)).content());
    // Kiểm soát đáp án (E4 tối thiểu, E6 thay): câu lưu và câu trả về là câu ĐÃ qua bộ kiểm
    // (D-GEO-159)
    AnswerGuardApi.Screened sc = guard.screen(assignmentId, rep.text());
    if (sc.blocked()) {
      rep = new Reply("refused_answer", sc.text(), null);
    }
    UUID rid = requestId == null ? Uuid7.next() : requestId;
    TrackingRepository.QaRecorded r =
        repo.recordQa(
            p.userId(),
            assignmentId,
            rid,
            q,
            rep.text(),
            guard.version(),
            rep.kind(),
            rep.source() == null ? null : rep.source().id());
    if (r.id() == null) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    if ("DUPLICATE".equals(r.status())) {
      // hai yêu cầu cùng requestId chạy đua: trả bản đã lưu (bản ghi đầu), bỏ câu trả lời vừa sinh
      return savedAsked(p.userId(), r.id())
          .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
    evictMonitor(p.orgId(), assignmentId);
    return new Asked(r.id(), q, rep.kind(), rep.text(), source(rep.source()));
  }

  /** Câu trả lời của trợ lý sau khi đối chiếu nguồn với kho bài giảng. */
  private record Reply(String kind, String text, LectureCatalog.LectureRef source) {}

  /**
   * Đọc kết quả của cổng AI: {kind, reply, source}. Nguồn phải là bài giảng có trong kho
   * (BR-GEO-189, US-GEO-409-SC6: không bịa nguồn); câu giải thích khái niệm không có nguồn hợp lệ
   * và câu ngoài giáo trình đều thành MSG-GEO-36, không kèm nguồn (BR-GEO-190). Văn bản thuần (AI
   * thật chưa trả JSON) coi là câu gợi mở không nguồn.
   */
  private Reply interpret(String content) {
    String c = content == null ? "" : content.strip();
    if (!c.startsWith("{")) {
      return new Reply("hint", c.isEmpty() ? MSG_36 : clip(c), null);
    }
    tools.jackson.databind.JsonNode n;
    try {
      n = mapper.readTree(c);
    } catch (tools.jackson.core.JacksonException e) {
      return new Reply("hint", clip(c), null);
    }
    String kind = n.path("kind").asString("hint");
    String text = n.path("reply").asString("").strip();
    Optional<LectureCatalog.LectureRef> src = lectures.bySlug(n.path("source").asString(""));
    if ("out_of_curriculum".equals(kind)
        || text.isEmpty()
        || ("concept".equals(kind) && src.isEmpty())) {
      return new Reply("out_of_curriculum", MSG_36, null);
    }
    String k =
        switch (kind) {
          case "concept", "refused_answer", "explain_step" -> kind;
          default -> "hint";
        };
    return new Reply(k, clip(text), "refused_answer".equals(k) ? null : src.orElse(null));
  }

  private static String clip(String s) {
    return s.length() > 4000 ? s.substring(0, 4000) : s;
  }

  private static Source source(LectureCatalog.LectureRef r) {
    return r == null ? null : new Source(r.slug(), r.title(), r.sourceLabel());
  }

  private Optional<Asked> savedAsked(UUID userId, long id) {
    return repo.turn(userId, id)
        .map(a ->
            new Asked(
                a.id(),
                a.question(),
                a.kind(),
                a.reply(),
                source(lectures.byId(a.source()).orElse(null))));
  }

  /**
   * Nhật ký hỏi đáp của CHÍNH học sinh (US-GEO-410): mới nhất trước, lọc theo bài, theo con trỏ,
   * không tổng số (BR-GEO-195, 197). Không kèm câu trả lời; mở một dòng đọc câu đã lưu, không gọi
   * AI lại.
   */
  public Page<LogItem> log(UUID assignmentId, Integer limit, String cursor) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    PageRequest req = paging.request(LOG_LIST, limit, cursor);
    Long before = req.after() == null ? null : req.after().longKey(0);
    List<TrackingRepository.LogRow> rows =
        repo.log(p.userId(), assignmentId, before, req.fetchSize());
    return paging
        .page(rows, req, r -> KeysetCursor.of(NIL, r.id()))
        .map(r ->
            new LogItem(r.id(), r.assignmentId(), r.problemTitle(), r.question(), r.askedAt()));
  }

  /** Câu trả lời đã lưu: chỉ người hỏi (giáo viên: FORBIDDEN; người khác: NOT_FOUND). */
  public SavedAnswer answer(long id) {
    AuthPrincipal p = RequestContext.principal();
    if (p.role() != Role.STUDENT) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return repo.turn(p.userId(), id)
        .map(a ->
            new SavedAnswer(
                a.id(),
                a.assignmentId(),
                a.askedAt(),
                a.question(),
                a.kind(),
                a.reply(),
                source(lectures.byId(a.source()).orElse(null))))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  public List<Question> questionsOf(UUID studentId) {
    checkCanSee(studentId);
    return repo.questionsOf(studentId).stream().map(TrackingService::question).toList();
  }

  public List<StudentProgress> progressOf(UUID studentId) {
    checkCanSee(studentId);
    return repo.progressOf(studentId).stream().map(TrackingService::progress).toList();
  }

  /** Theo dõi lớp của một bài (chỉ giáo viên của lớp; trường khác: NOT_FOUND, BR-GEO-112). */
  public Monitor monitor(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    // quyền luôn kiểm ở CSDL, kể cả khi có đệm (đệm chỉ chứa dữ liệu, khóa mang tiền tố trường)
    TrackingRepository.MonitorHeader h =
        repo.monitorHeader(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    String key = monitorKey(p.orgId(), assignmentId);
    Monitor cached = readCache(key);
    if (cached != null) {
      return cached;
    }
    List<StudentProgress> students =
        repo.monitorStudents(assignmentId).stream().map(TrackingService::progress).toList();
    List<Question> questions =
        repo.monitorQuestions(assignmentId).stream().map(TrackingService::question).toList();
    Monitor m =
        new Monitor(
            h.assignmentId(),
            h.problemCode(),
            h.className(),
            h.releaseState(),
            students.size(),
            (int) students.stream().filter(StudentProgress::opened).count(),
            students,
            questions);
    writeCache(key, m);
    return m;
  }

  private void checkCanSee(UUID studentId) {
    AuthPrincipal p = RequestContext.principal();
    boolean ok =
        switch (p.role()) {
          case STUDENT -> p.userId().equals(studentId);
          case TEACHER -> repo.isStudentOfTeacher(p.userId(), studentId);
          default -> false;
        };
    if (!ok) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  private String monitorKey(UUID orgId, UUID assignmentId) {
    return keys.key("t", orgId.toString(), "mon", assignmentId.toString());
  }

  private Monitor readCache(String key) {
    try {
      String v = redis.opsForValue().get(key);
      return v == null ? null : mapper.readValue(v, Monitor.class);
    } catch (RuntimeException e) {
      log.debug("bỏ qua đệm Redis (đọc): {}", e.toString());
      return null;
    }
  }

  private void writeCache(String key, Monitor m) {
    try {
      redis.opsForValue().set(key, mapper.writeValueAsString(m), MONITOR_TTL);
    } catch (RuntimeException e) {
      log.debug("bỏ qua đệm Redis (ghi): {}", e.toString());
    }
  }

  private void evictMonitor(UUID orgId, UUID assignmentId) {
    try {
      redis.delete(monitorKey(orgId, assignmentId));
    } catch (RuntimeException e) {
      log.debug("bỏ qua đệm Redis (xóa): {}", e.toString());
    }
  }

  private static Question question(QuestionRow r) {
    return new Question(r.id(), r.assignmentId(), r.question(), r.askedAt(), r.askerName());
  }

  private static StudentProgress progress(ProgressRow r) {
    int k;
    String label;
    if (r.solutionViewed()) {
      k = 3;
      label = "Đã xem lời giải";
    } else if (r.hintLevel() >= 1) {
      k = r.hintLevel();
      label = "Đã xem gợi ý " + r.hintLevel();
    } else {
      k = 0;
      label = r.opened() ? "Đã mở bài" : "Chưa bắt đầu";
    }
    return new StudentProgress(
        r.assignmentId(),
        r.userId(),
        r.displayName(),
        r.opened(),
        k,
        label,
        r.hintLevel(),
        r.askCount(),
        r.toolCount(),
        (r.activeSeconds() + 30) / 60);
  }
}
