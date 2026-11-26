package vn.geo.server.content.internal.solution.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.review.repo.ReviewInboxRepository;
import vn.geo.server.content.internal.solution.repo.SolutionRepository;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.Content;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.DocRow;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.VersionRow;
import vn.geo.server.content.internal.solution.service.SolutionViews.ApproveView;
import vn.geo.server.content.internal.solution.service.SolutionViews.AssignedView;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocSummary;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;
import vn.geo.server.content.internal.solution.service.SolutionViews.HistoryItem;
import vn.geo.server.content.internal.solution.service.SolutionViews.PinnedLabels;
import vn.geo.server.content.internal.solution.service.SolutionViews.ProposalView;
import vn.geo.server.content.internal.solution.service.SolutionViews.StepView;
import vn.geo.server.content.internal.solution.service.SolutionViews.VersionView;

/**
 * S2-E5: soát và duyệt lời giải có phiên bản (US-S2E5-01…04, khung C2-03).
 *
 * <ul>
 *   <li>Mỗi lần lưu có thay đổi tạo phiên bản mới, không ghi đè (BR-S2E5-02); lưu từ phiên bản
 *       không còn mới nhất nhận CONFLICT (BR-S2E5-05); chỉ chủ đề sửa/duyệt, người khác NOT_FOUND
 *       (BR-S2E5-06); học sinh FORBIDDEN.
 *   <li>"Kiểm tra lại" chạy StepCheck của module geometry (bean {@code stepCheckEngine}) tối đa 10
 *       giây (BR-S2E5-11); nhãn máy chỉ do kết quả kiểm quyết định (BR-S2E5-12, 15).
 *   <li>Duyệt chỉ đổi trục người; bị chặn khi còn bước lệch hoặc đáp số lệch engine (BR-S2E5-17);
 *       nhãn máy "Cần xem lại" thì phải xác nhận (BR-S2E5-18).
 * </ul>
 */
@Service
public class SolutionReviewService {

  public static final int MAX_STEPS = 30;
  public static final int MAX_STEP_CHARS = 2000;
  public static final long CHECK_LIMIT_MS = 10_000;

  private final SolutionRepository repo;
  private final ReviewInboxRepository inbox;
  private final FigureFacts facts;
  private final JsonMapper mapper;
  private final BiFunction<List<String>, Map<String, Object>, Map<String, Object>> defaultEngine;
  private volatile BiFunction<List<String>, Map<String, Object>, Map<String, Object>> engine;
  private volatile LongSupplier nanoClock = System::nanoTime;

  public SolutionReviewService(
      SolutionRepository repo,
      ReviewInboxRepository inbox,
      FigureFacts facts,
      JsonMapper mapper,
      @Qualifier("stepCheckEngine")
          BiFunction<List<String>, Map<String, Object>, Map<String, Object>> engine) {
    this.repo = repo;
    this.inbox = inbox;
    this.facts = facts;
    this.mapper = mapper;
    this.defaultEngine = engine;
    this.engine = engine;
  }

  /** Chỉ dùng trong kiểm thử: engine giả lập và đồng hồ giả (US-S2E5-02-BV4). null = mặc định. */
  public void overrideForTest(
      BiFunction<List<String>, Map<String, Object>, Map<String, Object>> testEngine,
      LongSupplier testClock) {
    this.engine = testEngine == null ? defaultEngine : testEngine;
    this.nanoClock = testClock == null ? System::nanoTime : testClock;
  }

  // ---------------------------------------------------------------- đọc

  public List<DocSummary> list() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.myDocs(me.userId()).stream()
        .map(d -> new DocSummary(d.id(), d.code(), d.title(), d.latestVersion(), d.inMyShelf()))
        .toList();
  }

  public DocView view(UUID docId, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.doc(docId), me);
    return render(doc, version == null ? doc.latestVersion() : version, me, true, null);
  }

  /** Kết quả StepCheck của một phiên bản (chỉ giáo viên chủ đề; BR-S2E5-11). */
  public List<StepView> stepChecks(UUID docId, Integer version) {
    return view(docId, version).version().steps();
  }

  public PinnedLabels pinnedLabels(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    SolutionRepository.PinnedLabel p =
        repo.pinnedLabel(me.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    boolean verified = "verified".equals(p.machineStatus());
    return new PinnedLabels(
        assignmentId,
        p.version(),
        SolutionViews.machineLabel(p.machineStatus()),
        verified ? SolutionViews.MACHINE_NOTE : null,
        "ai_drafted".equals(p.humanStatus()) || "editing".equals(p.humanStatus())
            ? SolutionViews.HUMAN_AI
            : SolutionViews.HUMAN_APPROVED);
  }

  private static DocRow owned(java.util.Optional<DocRow> doc, AuthPrincipal me) {
    return doc.filter(d -> d.ownerId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private List<String> steps(String json) {
    List<String> out = new ArrayList<>();
    mapper.readTree(json).forEach(n -> out.add(n.asString()));
    return out;
  }

  /** Nội dung nhạy cảm của phiên bản qua hàm cổng của CSDL (chỉ chủ sổ). */
  private Content content(UUID userId, UUID docId, int version) {
    return repo.content(userId, docId, version)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private DocView render(DocRow doc, int version, AuthPrincipal me, boolean saved, String notice) {
    VersionRow v =
        repo.version(doc.id(), version).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Content content = content(me.userId(), doc.id(), version);
    Map<Integer, JsonNode> checks = new HashMap<>();
    mapper.readTree(content.checksJson()).forEach(c -> checks.put(c.path("no").asInt(), c));
    List<String> texts = steps(content.stepsJson());
    List<StepView> stepViews = new ArrayList<>();
    int fails = 0;
    for (int i = 0; i < texts.size(); i++) {
      JsonNode c = checks.get(i + 1);
      String check = c == null ? "unchecked" : c.path("status").asString();
      String label =
          switch (check) {
            case "pass" -> "Đạt";
            case "fail" -> "Lệch";
            case "unverifiable" -> "Không kiểm được";
            default -> "Chưa kiểm";
          };
      if ("fail".equals(check)) {
        fails++;
      }
      stepViews.add(
          new StepView(
              i + 1, texts.get(i), check, label, c == null ? null : c.path("reason").asString()));
    }
    boolean latest = v.version() == doc.latestVersion();
    boolean verified = "verified".equals(v.machineStatus());
    boolean checked = v.checkedAt() != null;
    boolean approved = v.approvedAt() != null;
    String summary =
        fails > 0
            ? fails + " bước lệch"
            : verified ? SolutionViews.MACHINE_NOTE : checked ? "Không có bước lệch" : "Chưa kiểm";
    String blocked = null;
    if (!latest) {
      blocked = "Phiên bản đã cũ";
    } else if (fails > 0) {
      blocked = "Chặn duyệt: còn " + fails + " bước lệch";
    } else if ("mismatch".equals(v.finalState())) {
      blocked = "Chặn duyệt: đáp số lệch máy tính lại";
    }
    ApproveView approve =
        new ApproveView(
            blocked == null && !approved, approved, !verified, approved ? null : blocked);
    VersionView vv =
        new VersionView(
            v.version(),
            latest,
            stepViews,
            SolutionViews.machineLabel(v.machineStatus()),
            verified ? SolutionViews.MACHINE_NOTE : null,
            SolutionViews.humanLabel(v.humanStatus()),
            checked,
            fails,
            summary,
            v.finalState(),
            content.finalNote(),
            v.approvedCode(),
            v.approvedAt());

    List<HistoryItem> history = new ArrayList<>();
    for (VersionRow h : repo.history(doc.id())) {
      String who = h.aiAuthored() ? "AI" : h.authorName();
      String desc;
      if (h.aiAuthored()) {
        desc = h.note() + " · " + (h.checkedAt() == null ? "máy chưa kiểm" : "máy đã kiểm");
      } else {
        desc = (me.userId().equals(h.authorId()) ? "bạn " : who + " ") + h.note();
      }
      history.add(
          new HistoryItem(
              "version",
              h.version(),
              desc,
              who,
              h.authorCode(),
              SolutionViews.humanLabel(h.humanStatus()),
              h.createdAt()));
    }
    List<ProposalView> pending = new ArrayList<>();
    mapper
        .readTree(content.proposalsJson())
        .forEach(p ->
            pending.add(
                new ProposalView(
                    UUID.fromString(p.path("id").asString()),
                    p.path("afterStep").asInt(),
                    p.path("content").asString())));
    for (ReviewInboxRepository.ItemRow it : inbox.items(doc.id())) {
      if ("dismissed".equals(it.status())) {
        history.add(
            new HistoryItem(
                "proposal", null, "đã bỏ đề xuất của AI", "AI", null, null, it.decidedAt()));
      }
    }
    history.sort(
        Comparator.comparing(HistoryItem::at, Comparator.nullsLast(Comparator.reverseOrder())));
    List<AssignedView> assigned =
        repo.pins(doc.id()).stream()
            .map(p -> new AssignedView(p.className(), p.version()))
            .toList();
    return new DocView(
        doc.id(),
        doc.code(),
        doc.title(),
        doc.statement(),
        doc.latestVersion(),
        doc.inMyShelf(),
        saved,
        notice,
        facts.scene(doc.figure(), v.machineStatus()),
        vv,
        approve,
        history,
        pending,
        assigned);
  }

  // ---------------------------------------------------------------- lưu phiên bản

  /** Chuẩn hóa và kiểm danh sách bước (BR-S2E5-01). */
  static List<String> validate(List<String> steps) {
    if (steps == null || steps.isEmpty()) {
      throw AppException.invalidField("steps", "Lời giải cần ít nhất 1 bước");
    }
    if (steps.size() > MAX_STEPS) {
      throw AppException.invalidField("steps", "Tối đa 30 bước");
    }
    List<String> out = new ArrayList<>();
    for (int i = 0; i < steps.size(); i++) {
      String s = steps.get(i) == null ? "" : steps.get(i).strip();
      if (s.isEmpty()) {
        throw AppException.invalidField("steps[" + i + "]", "Bước " + (i + 1) + " đang trống");
      }
      if (s.codePointCount(0, s.length()) > MAX_STEP_CHARS) {
        throw AppException.invalidField("steps[" + i + "]", "Bước tối đa 2000 ký tự");
      }
      out.add(s);
    }
    return out;
  }

  /** Mô tả ngắn của lần sửa cho lịch sử (BR-S2E5-03): "sửa bước 3", "thêm bước 3"… */
  static String describe(List<String> before, List<String> after) {
    if (before.size() == after.size()) {
      List<String> changed = new ArrayList<>();
      for (int i = 0; i < after.size(); i++) {
        if (!before.get(i).equals(after.get(i))) {
          changed.add(String.valueOf(i + 1));
        }
      }
      if (changed.size() > 1
          && before.stream().sorted().toList().equals(after.stream().sorted().toList())) {
        return "đổi thứ tự bước";
      }
      return "sửa bước " + String.join(", ", changed);
    }
    boolean grew = after.size() > before.size();
    List<String> longer = grew ? after : before;
    List<String> shorter = grew ? before : after;
    List<String> extra = new ArrayList<>();
    int j = 0;
    for (int i = 0; i < longer.size(); i++) {
      if (j < shorter.size() && longer.get(i).equals(shorter.get(j))) {
        j++;
      } else {
        extra.add(String.valueOf(i + 1));
      }
    }
    if (j == shorter.size()) {
      return (grew ? "thêm bước " : "xóa bước ") + String.join(", ", extra);
    }
    return grew ? "thêm và sửa bước" : "xóa và sửa bước";
  }

  @Transactional
  public DocView save(UUID docId, Integer baseVersion, List<String> rawSteps, UUID proposalId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.lockDoc(docId), me);
    if (baseVersion == null) {
      throw AppException.invalidField("baseVersion", "Thiếu phiên bản gốc");
    }
    List<String> steps = validate(rawSteps);
    if (baseVersion != doc.latestVersion()) {
      throw new AppException(ErrorCode.CONFLICT, "Có bản mới hơn (v" + doc.latestVersion() + ")");
    }
    VersionRow base =
        repo.version(doc.id(), baseVersion)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> before = steps(content(me.userId(), doc.id(), baseVersion).stepsJson());
    if (before.equals(steps)) {
      return render(doc, doc.latestVersion(), me, false, "Chưa có thay đổi để lưu");
    }
    String note = describe(before, steps);
    if (proposalId != null
        && inbox.decide(proposalId, "accepted", me.userId(), baseVersion + 1) == 1) {
      note = "sửa đề xuất của AI";
    }
    int created = createVersion(doc, base, before, steps, me.userId(), note);
    return render(reload(doc), created, me, true, null);
  }

  /** "Dùng bản này": phiên bản mới là bản sao của một phiên bản cũ (BR-S2E5-04). */
  @Transactional
  public DocView reuse(UUID docId, int version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.lockDoc(docId), me);
    VersionRow src =
        repo.version(doc.id(), version).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> steps = steps(content(me.userId(), doc.id(), version).stepsJson());
    int created = createVersion(doc, src, steps, steps, me.userId(), "sao từ v" + version);
    return render(reload(doc), created, me, true, null);
  }

  /** Nhận đề xuất của AI: chèn bước mới thành phiên bản mới (BR-S2E5-20). Gọi trong giao dịch. */
  public int acceptProposal(DocRow doc, UUID itemId, UUID userId) {
    VersionRow base =
        repo.version(doc.id(), doc.latestVersion())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Content content = content(userId, doc.id(), doc.latestVersion());
    JsonNode item = null;
    for (JsonNode p : mapper.readTree(content.proposalsJson())) {
      if (itemId.toString().equals(p.path("id").asString())) {
        item = p;
      }
    }
    if (item == null) {
      throw new AppException(ErrorCode.CONFLICT, "Đề xuất này đã được xử lý");
    }
    List<String> before = steps(content.stepsJson());
    List<String> after = new ArrayList<>(before);
    int at = Math.min(Math.max(item.path("afterStep").asInt(), 0), after.size());
    after.add(at, item.path("content").asString().strip());
    return createVersion(doc, base, before, validate(after), userId, "nhận đề xuất của AI");
  }

  public DocRow lockOwned(UUID docId) {
    return owned(repo.lockDoc(docId), Roles.require(Role.TEACHER));
  }

  public DocView viewAfterChange(UUID docId, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.doc(docId), me);
    return render(doc, version == null ? doc.latestVersion() : version, me, true, null);
  }

  private DocRow reload(DocRow doc) {
    return repo.doc(doc.id()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private int createVersion(
      DocRow doc,
      VersionRow from,
      List<String> before,
      List<String> after,
      UUID userId,
      String note) {
    if (!repo.bumpLatest(doc.id(), doc.latestVersion())) {
      throw new AppException(ErrorCode.CONFLICT, "Có bản mới hơn");
    }
    int version = doc.latestVersion() + 1;
    UUID id = Uuid7.next();
    repo.insertVersion(id, doc.id(), version, mapper.writeValueAsString(after), userId, note);
    // BR-S2E5-10: bước bị sửa và các bước sau về "Chưa kiểm"; các bước trước giữ kết quả.
    int same = 0;
    while (same < before.size()
        && same < after.size()
        && before.get(same).equals(after.get(same))) {
      same++;
    }
    if (same > 0) {
      repo.copyChecks(from.id(), id, same);
    }
    return version;
  }

  // ---------------------------------------------------------------- kiểm tra lại

  public DocView check(UUID docId, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.doc(docId), me);
    int n = version == null ? doc.latestVersion() : version;
    VersionRow v =
        repo.version(doc.id(), n).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> steps = steps(content(me.userId(), doc.id(), n).stepsJson());
    Map<String, Object> ctx = facts.context(doc.statement(), doc.figure());
    BiFunction<List<String>, Map<String, Object>, Map<String, Object>> eng = engine;
    long start = nanoClock.getAsLong();
    Map<String, Object> res;
    try {
      res =
          CompletableFuture.supplyAsync(() -> eng.apply(steps, ctx))
              .get(CHECK_LIMIT_MS, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      res = null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AppException(ErrorCode.INTERNAL);
    } catch (ExecutionException e) {
      res = Map.of("steps", List.of(), "finalState", "unknown");
    }
    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(nanoClock.getAsLong() - start);
    if (res == null || elapsedMs > CHECK_LIMIT_MS) {
      repo.clearChecks(v.id());
      throw new AppException(ErrorCode.TIMEOUT, "Kiểm lời giải quá 10 giây. Bấm \"Thử lại\".");
    }
    Map<Integer, Map<String, Object>> byNo = new HashMap<>();
    if (res.get("steps") instanceof List<?> list) {
      for (Object o : list) {
        if (o instanceof Map<?, ?> m && m.get("stepNo") instanceof Number no) {
          @SuppressWarnings("unchecked")
          Map<String, Object> row = (Map<String, Object>) m;
          byNo.put(no.intValue(), row);
        }
      }
    }
    List<Map<String, Object>> rows = new ArrayList<>();
    int pass = 0;
    int fail = 0;
    for (int i = 1; i <= steps.size(); i++) {
      Map<String, Object> r = byNo.get(i);
      String status = r == null ? "unverifiable" : String.valueOf(r.get("status"));
      if (!List.of("pass", "fail", "unverifiable").contains(status)) {
        status = "unverifiable";
      }
      Object reason = r == null ? null : r.get("reason");
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("no", i);
      row.put("status", status);
      row.put("reason", reason == null ? "không đọc được biểu thức" : reason.toString());
      row.put("written", r == null ? null : r.get("written"));
      row.put("computed", r == null ? null : r.get("computed"));
      rows.add(row);
      pass += "pass".equals(status) ? 1 : 0;
      fail += "fail".equals(status) ? 1 : 0;
    }
    String finalState = String.valueOf(res.getOrDefault("finalState", "unknown"));
    if (!List.of("match", "mismatch", "unknown").contains(finalState)) {
      finalState = "unknown";
    }
    Object note = res.get("finalNote");
    // BR-S2E5-12: nhãn máy chỉ từ kết quả kiểm; không tham số nào của người dùng đi tới đây.
    boolean verified = fail == 0 && pass >= 1 && "match".equals(finalState);
    repo.replaceChecks(
        v.id(),
        mapper.writeValueAsString(rows),
        finalState,
        note == null ? null : note.toString(),
        verified);
    return render(doc, n, me, true, null);
  }

  // ---------------------------------------------------------------- duyệt

  @Transactional
  public DocView approve(UUID docId, Integer version, boolean confirmNeedsReview) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc = owned(repo.lockDoc(docId), me);
    if (version == null) {
      throw AppException.invalidField("version", "Thiếu số phiên bản");
    }
    VersionRow v =
        repo.version(doc.id(), version).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (v.version() != doc.latestVersion()) {
      throw new AppException(ErrorCode.CONFLICT, "Phiên bản đã cũ");
    }
    if (v.approvedAt() != null) {
      return render(doc, version, me, true, null); // BR-S2E5-21: bấm hai lần chỉ ghi một lần
    }
    int fails = repo.failCount(v.id());
    if (fails > 0) {
      throw new AppException(ErrorCode.CONFLICT, "Chặn duyệt: còn " + fails + " bước lệch");
    }
    if ("mismatch".equals(v.finalState())) {
      throw new AppException(ErrorCode.CONFLICT, "Chặn duyệt: đáp số lệch máy tính lại");
    }
    if (!"verified".equals(v.machineStatus()) && !confirmNeedsReview) {
      throw AppException.invalidField("confirmNeedsReview", "Nhãn máy vẫn Cần xem lại. Vẫn duyệt?");
    }
    String code = repo.user(me.userId()).map(SolutionRepository.UserRow::code).orElse(null);
    if (repo.approve(v.id(), me.userId(), code) == 1) {
      repo.afterApprove(doc.id(), v.id());
    }
    return render(reload(doc), version, me, true, null);
  }
}
