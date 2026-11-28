package vn.geo.server.notify.internal.service.draft;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.notify.internal.repo.draft.NoticeDraftRepository;
import vn.geo.server.notify.internal.repo.draft.NoticeDraftRepository.Draft;
import vn.geo.server.notify.internal.repo.draft.NoticeDraftRepository.Fact;
import vn.geo.server.notify.internal.repo.draft.NoticeDraftRepository.Student;
import vn.geo.server.notify.internal.service.draft.NoticeText.Sentence;

/**
 * Trợ lý soạn nháp thông báo lớp (US-S5E5-01, BR-S5E5-01…03, 14, 15). Máy chủ điền dữ kiện (tên
 * bài, hạn) từ dữ liệu lớp; AI (task {@code class_notice}, luôn giả lập trong CI) chỉ viết câu chữ;
 * nội dung gửi AI không có tên hay mã học sinh. Nháp nhãn "AI soạn", chỉ tới học sinh khi giáo viên
 * bấm Gửi; người nhận = học sinh Đang học lúc gửi; thân ≤ 1 000 ký tự; thông báo đã gửi ghi người
 * gửi và dòng "Soạn với trợ lý AI, thầy cô đã duyệt".
 */
@Service
public class NoticeDraftService {

  public static final String TASK = "class_notice";
  public static final String MSG_55 = "Đã gửi thông báo tới %d em lớp %s."; // MSG-S5-55
  public static final String MSG_56 = "Thông báo tối đa 1 000 ký tự."; // MSG-S5-56
  public static final String AI_LINE = "Soạn với trợ lý AI, thầy cô đã duyệt"; // BR-S5E5-15
  static final Map<String, String> KINDS =
      Map.of(
          "due", "Nhắc hạn nộp",
          "test", "Lịch kiểm tra",
          "returned", "Đã trả bài",
          "free", "Tự do");

  private final NoticeDraftRepository repo;
  private final AiGateway ai;
  private final AiQuota quota;
  private final JsonMapper mapper;
  private final Clock clock;
  private final ObjectProvider<NoticePromptLog> promptLog;

  public NoticeDraftService(
      NoticeDraftRepository repo,
      AiGateway ai,
      AiQuota quota,
      JsonMapper mapper,
      Clock clock,
      ObjectProvider<NoticePromptLog> promptLog) {
    this.repo = repo;
    this.ai = ai;
    this.quota = quota;
    this.mapper = mapper;
    this.clock = clock;
    this.promptLog = promptLog;
  }

  /** Thân tạo nháp: lớp, loại, bộ bài nguồn (trừ Tự do), ghi chú của giáo viên (tuỳ chọn). */
  public record CreateRequest(UUID classId, String kind, UUID refId, String note) {}

  /** Thân sửa / gửi. */
  public record BodyRequest(String body) {}

  /** Nguồn cho màn soạn: bộ bài đã giao của lớp và hạn. */
  public Map<String, Object> sources(UUID classId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String className = owned(me, classId);
    List<Map<String, Object>> sets = new ArrayList<>();
    for (Fact f : repo.sets(me.orgId(), classId)) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", f.id());
      m.put("title", f.title());
      m.put("dueAt", f.dueAt());
      m.put("dueLabel", f.dueAt() == null ? null : NoticeText.dueLabel(f.dueAt()));
      sets.add(m);
    }
    return Map.of(
        "classId", classId,
        "className", className,
        "kinds",
            KINDS.keySet().stream()
                .sorted()
                .map(k -> Map.of("kind", k, "label", KINDS.get(k)))
                .toList(),
        "sets", sets);
  }

  @Transactional
  public Map<String, Object> create(CreateRequest in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (in == null || in.classId() == null) {
      throw AppException.invalidField("classId", "Chọn lớp nhận thông báo.");
    }
    String className = owned(me, in.classId());
    String kind = in.kind() == null ? "" : in.kind();
    if (!KINDS.containsKey(kind)) {
      throw AppException.invalidField("kind", "Chọn loại thông báo.");
    }
    String note = in.note() == null ? "" : in.note().trim();
    if (note.length() > 500) {
      throw AppException.invalidField("note", "Ghi chú tối đa 500 ký tự.");
    }
    Fact fact = null;
    if (!"free".equals(kind)) {
      if (in.refId() == null) {
        throw AppException.invalidField("refId", "Chọn bài cần thông báo.");
      }
      fact =
          repo.set(me.orgId(), in.classId(), in.refId())
              .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
      if ("due".equals(kind) && fact.dueAt() == null) {
        throw AppException.invalidField("refId", "Bài này chưa đặt hạn nộp.");
      }
    } else if (note.isEmpty()) {
      throw AppException.invalidField("note", "Thông báo tự do cần ghi chú nội dung.");
    }
    List<Student> roster = repo.roster(me.orgId(), in.classId());
    List<String> names = new ArrayList<>();
    for (Student s : roster) {
      names.add(s.displayName());
      if (s.userCode() != null) {
        names.add(s.userCode());
      }
    }
    Instant due = fact == null ? null : fact.dueAt();
    List<Sentence> sentences;
    boolean aiUsed = quota.assistantOn(me.orgId(), in.classId(), TASK);
    if (aiUsed) {
      quota.takeComposeTurn(me.orgId(), me.userId(), in.classId(), TASK);
      String prompt = prompt(kind, fact, NoticeText.scrub(note, names), names);
      NoticePromptLog log = promptLog.getIfAvailable();
      if (log != null) {
        log.record(me.userId(), prompt);
      }
      String reply = ai.complete(new AiRequest(TASK, prompt)).content();
      sentences = NoticeText.check(parse(reply), names, due);
    } else {
      sentences = NoticeText.check(template(kind, fact, NoticeText.scrub(note, names)), names, due);
    }
    String body = NoticeText.join(sentences);
    if (body.length() > NoticeText.MAX) {
      body = body.substring(0, NoticeText.MAX);
    }
    List<Map<String, Object>> unsure = new ArrayList<>();
    for (Sentence s : sentences) {
      if (s.unsure()) {
        unsure.add(Map.of("text", s.text(), "label", NoticeText.UNSURE, "why", s.why()));
      }
    }
    UUID id =
        repo.insert(
            me.orgId(),
            me.userId(),
            in.classId(),
            kind,
            fact == null ? null : fact.id(),
            aiUsed ? body : "",
            body,
            mapper.writeValueAsString(unsure));
    return view(repo.find(me.orgId(), me.userId(), id).orElseThrow(), className, fact);
  }

  public Map<String, Object> get(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Draft d = find(me, id);
    return view(d, owned(me, d.classId()), fact(me, d));
  }

  @Transactional
  public Map<String, Object> edit(UUID id, BodyRequest in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Draft d = find(me, id);
    if (!"draft".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Thông báo đã gửi, không sửa được.");
    }
    repo.updateBody(me.orgId(), id, validBody(in));
    return view(find(me, id), owned(me, d.classId()), fact(me, d));
  }

  /** Gửi: chỉ khi giáo viên bấm (BR-S5E5-02); tới học sinh Đang học của lớp lúc gửi. */
  @Transactional
  public Map<String, Object> send(UUID id, BodyRequest in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Draft d = find(me, id);
    String className = owned(me, d.classId());
    if (!"draft".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Thông báo này đã gửi.");
    }
    if (in != null && in.body() != null) {
      repo.updateBody(me.orgId(), id, validBody(in));
      d = find(me, id);
    } else if (d.body().isBlank() || d.body().length() > NoticeText.MAX) {
      validBody(new BodyRequest(d.body()));
    }
    String sender = repo.displayName(me.orgId(), me.userId());
    String text = d.body() + "\n\n— " + sender + (d.aiText().isEmpty() ? "" : "\n" + AI_LINE);
    List<UUID> users = repo.activeStudents(me.orgId(), d.classId());
    repo.deliver(me.orgId(), id, text, users);
    repo.markSent(me.orgId(), id, users.size());
    Map<String, Object> out = new LinkedHashMap<>(view(find(me, id), className, fact(me, d)));
    out.put("message", MSG_55.formatted(users.size(), className));
    return out;
  }

  /** Báo cáo dùng trợ lý của giáo viên (BR-S5E5-17, US-S5E5-04-SC6). */
  public Map<String, Object> usage(int days) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    int d = Math.max(1, Math.min(days, 90));
    Instant since = clock.instant().minus(Duration.ofDays(d));
    long[] lp = repo.lessonStats(me.orgId(), me.userId(), since);
    long[] nt = repo.noticeStats(me.orgId(), me.userId(), since);
    return Map.of(
        "days",
        d,
        "items",
        List.of(item("lesson_plan", "Giáo án", lp), item("notice", "Thông báo", nt)));
  }

  private static Map<String, Object> item(String kind, String label, long[] s) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("kind", kind);
    m.put("label", label);
    m.put("drafts", s[0]);
    m.put("accepted", s[1]);
    m.put("acceptedAfterEdit", s[2]);
    m.put("acceptedPct", pct(s[1], s[0]));
    m.put("acceptedAfterEditPct", pct(s[2], s[0]));
    return m;
  }

  private static int pct(long part, long all) {
    return all == 0 ? 0 : (int) Math.round(part * 100.0 / all);
  }

  private String validBody(BodyRequest in) {
    String body = in == null || in.body() == null ? "" : in.body().strip();
    if (body.isEmpty() || body.length() > NoticeText.MAX) {
      throw AppException.invalidField("body", MSG_56);
    }
    return body;
  }

  private Draft find(AuthPrincipal me, UUID id) {
    return repo.find(me.orgId(), me.userId(), id)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private Fact fact(AuthPrincipal me, Draft d) {
    return d.refId() == null ? null : repo.set(me.orgId(), d.classId(), d.refId()).orElse(null);
  }

  private String owned(AuthPrincipal me, UUID classId) {
    return repo.ownedClassName(me.orgId(), me.userId(), classId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /**
   * Tin nhắn gửi AI: chỉ loại, dữ kiện của máy chủ, ghi chú đã gỡ tên; không tên lớp, mã, tên em.
   */
  static String prompt(String kind, Fact fact, String note, List<String> names) {
    StringBuilder b = new StringBuilder();
    b.append("Loại thông báo: ").append(KINDS.get(kind)).append('\n');
    if (fact != null) {
      b.append("Tên bài: ").append(NoticeText.scrub(fact.title(), names)).append('\n');
      if (fact.dueAt() != null) {
        b.append("Hạn nộp: ")
            .append(NoticeText.dueLabel(fact.dueAt()))
            .append(" (")
            .append(NoticeText.weekday(fact.dueAt()))
            .append(")\n");
      }
    }
    if (!note.isBlank()) {
      b.append("Ghi chú của giáo viên: ").append(note).append('\n');
    }
    return b.toString();
  }

  /** Khi trợ lý của lớp tắt: câu mẫu từ dữ kiện, không gọi AI. */
  static List<String> template(String kind, Fact fact, String note) {
    List<String> out = new ArrayList<>();
    if (fact != null) {
      String due = fact.dueAt() == null ? "" : " hạn nộp " + NoticeText.dueLabel(fact.dueAt());
      out.add(
          switch (kind) {
            case "test" -> "Lớp mình có bài kiểm tra \"" + fact.title() + "\"" + due + ".";
            case "returned" -> "Thầy cô đã trả bài \"" + fact.title() + "\", các em vào xem nhé.";
            default -> "Các em nhớ nộp bài \"" + fact.title() + "\"" + due + ".";
          });
    }
    if (!note.isBlank()) {
      out.add(note);
    }
    return out;
  }

  /** Đáp AI: {"sentences":[…]} (hoặc văn bản thuần: tách theo dấu chấm câu). */
  List<String> parse(String reply) {
    String r = reply == null ? "" : reply.trim();
    if (r.startsWith("{")) {
      try {
        JsonNode n = mapper.readTree(r).path("sentences");
        List<String> out = new ArrayList<>();
        n.forEach(x -> out.add(x.asString()));
        if (!out.isEmpty()) {
          return out;
        }
      } catch (RuntimeException e) {
        // rơi xuống tách thô
      }
    }
    List<String> out = new ArrayList<>();
    for (String s : r.split("(?<=[.!?])\\s+")) {
      if (!s.isBlank()) {
        out.add(s);
      }
    }
    return out;
  }

  private Map<String, Object> view(Draft d, String className, Fact fact) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.id());
    m.put("classId", d.classId());
    m.put("className", className);
    m.put("kind", d.kind());
    m.put("kindLabel", KINDS.get(d.kind()));
    m.put("refId", d.refId());
    m.put("title", fact == null ? null : fact.title());
    m.put(
        "dueLabel",
        fact == null || fact.dueAt() == null ? null : NoticeText.dueLabel(fact.dueAt()));
    m.put("status", d.status());
    m.put(
        "humanLabel",
        "sent".equals(d.status())
            ? "Giáo viên đã duyệt"
            : d.aiText().isEmpty() ? "Giáo viên soạn" : "AI soạn");
    m.put("body", d.body());
    m.put("unsure", mapper.readTree(d.unsure()));
    m.put("edited", d.edited());
    m.put("recipients", d.recipients());
    m.put("sentAt", d.sentAt());
    m.put("maxLength", NoticeText.MAX);
    return m;
  }
}
