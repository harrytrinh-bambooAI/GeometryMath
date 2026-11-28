package vn.geo.server.lessonplan.internal.patch.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository.Patch;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository.Plan;
import vn.geo.server.lessonplan.internal.patch.service.PatchPaths.Seg;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Exercise;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Period;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository;

/**
 * Chat vá bản nháp (US-S5E1-04): giáo viên chọn mục rồi gõ yêu cầu; máy chủ ghép giáo án hiện tại,
 * mục đang chọn, {@code <muc_da_khoa>} vào tin nhắn task {@code lesson_patch} (BR-S5E1-10), kiểm
 * bản vá (path trong phạm vi, không chạm mục khoá, {@code before} trùng nguyên văn, Σ phút giữ
 * nguyên) và gọi lại ĐÚNG một lần khi sai (BR-S5E1-11). Bản vá chỉ áp khi giáo viên bấm Nhận; mỗi
 * lần Nhận tạo phiên bản mới, Hoàn tác quay về phiên bản trước (BR-S5E1-12). Một lượt chat tính một
 * lượt soạn (BR-S5E1-08, chung hạn mức 30/ngày với soạn mới).
 */
@Service
public class LessonPatchService {

  public static final String MSG_LOCKED = "Mục này đang khoá. Mở khoá trước khi sửa."; // MSG-S5-05
  static final String MSG_FAILED = "Chưa soạn xong, thầy cô bấm Thử lại."; // MSG-S5-03
  static final String MSG_REFUSED = "AI không xử lý được yêu cầu này."; // MSG-S5-04
  static final String MSG_QUOTA =
      "Hôm nay thầy cô đã dùng hết 30 lượt soạn với AI. Lượt mới mở lúc 00:00."; // MSG-S5-06
  static final String MSG_STALE = "Giáo án đã đổi sau khi AI đề xuất. Thầy cô gửi lại yêu cầu.";
  static final int MAX_MESSAGE = 500;

  /** Thân yêu cầu chat: mục đang chọn (null = cả giáo án) và câu chat. */
  public record ChatBody(String section, String message) {}

  /** Thân Nhận / Bỏ: chỉ số dòng; null = mọi dòng còn chờ. */
  public record LinesBody(List<Integer> lines) {}

  /** Thân khoá mục. */
  public record LockBody(String section, Boolean locked) {}

  private final LessonPlanFinder finder;
  private final LessonEditRepository repo;
  private final LessonPlanRepository turns;
  private final LessonCatalogRepository catalog;
  private final AiGateway ai;
  private final JsonMapper mapper;
  private final Clock clock;
  private final int dailyLimit;

  public LessonPatchService(
      LessonPlanFinder finder,
      LessonEditRepository repo,
      LessonPlanRepository turns,
      LessonCatalogRepository catalog,
      AiGateway ai,
      JsonMapper mapper,
      Clock clock,
      @Value("${geo.s5.lesson-plan.daily-limit:30}") int dailyLimit) {
    this.finder = finder;
    this.repo = repo;
    this.turns = turns;
    this.catalog = catalog;
    this.ai = ai;
    this.mapper = mapper;
    this.clock = clock;
    this.dailyLimit = dailyLimit;
  }

  // ---------------------------------------------------------------- chat

  public Map<String, Object> chat(String idOrCode, ChatBody body) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    String message = body == null || body.message() == null ? "" : body.message().strip();
    if (message.isEmpty()) {
      throw AppException.invalidField("message", "Nhập yêu cầu sửa.");
    }
    if (message.length() > MAX_MESSAGE) {
      throw AppException.invalidField("message", "Yêu cầu tối đa 500 ký tự.");
    }
    if (!"ready".equals(plan.status()) || plan.contentJson() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Giáo án chưa sẵn sàng để sửa.");
    }
    ObjectNode content = (ObjectNode) mapper.readTree(plan.contentJson());
    String section = body.section() == null || body.section().isBlank() ? null : body.section();
    if (section != null && !sections(content).contains(section)) {
      throw AppException.invalidField("section", "Mục không có trong giáo án.");
    }
    if (section != null && plan.lockedSections().contains(section)) {
      throw new AppException(ErrorCode.CONFLICT, MSG_LOCKED);
    }
    checkQuota(me);
    Period period = catalog.period(plan.periodId()).orElseThrow();
    JsonNode req = mapper.readTree(plan.requestJson());
    int soTiet = req.path("soTiet").asInt(period.periods());
    LocalDate today =
        ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM).toLocalDate();
    UUID turn = turns.addTurn(me.userId(), today, "patch", plan.id());

    ObjectNode forAi = forAi(content);
    String base =
        PatchMessage.build(
            forAi,
            section,
            plan.lockedSections(),
            message,
            provided(period),
            catalog.exercises(period.lessonNo()));
    String msg = base;
    boolean lockedHit = false;
    for (int attempt = 1; attempt <= 2; attempt++) {
      String text;
      try {
        text = ai.complete(new AiRequest("lesson_patch", msg)).content();
      } catch (AppException e) {
        if (MSG_REFUSED.equals(e.getMessage())) {
          turns.uncount(turn);
          return reply(plan, "reply", MSG_REFUSED, null);
        }
        if (attempt == 2) {
          turns.uncount(turn);
          throw e;
        }
        continue; // cổng AI lỗi: gọi lại một lần cùng đầu vào (PO-78)
      }
      JsonNode out = parse(text);
      if (out == null) {
        msg = base; // JSON hỏng: gọi lại cùng đầu vào
        continue;
      }
      String kind = out.path("kind").asString("");
      if ("question".equals(kind) || "reply".equals(kind)) {
        return reply(plan, kind, out.path("text").asString(""), out.path("de_xuat"));
      }
      List<String> errors = new ArrayList<>();
      boolean[] locked = {false};
      if (!"patch".equals(kind)) {
        errors.add("kind phải là patch, question hoặc reply");
      } else {
        validate(
            content,
            forAi,
            section,
            plan.lockedSections(),
            out.path("patches"),
            soTiet,
            errors,
            locked);
      }
      lockedHit |= locked[0];
      if (errors.isEmpty()) {
        ArrayNode patches = mapper.createArrayNode();
        ArrayNode decisions = mapper.createArrayNode();
        for (JsonNode p : out.path("patches")) {
          ObjectNode keep = patches.addObject();
          for (String k : List.of("op", "path", "before", "after", "ly_do")) {
            keep.set(k, p.get(k) == null ? mapper.nullNode() : p.get(k).deepCopy());
          }
          decisions.add("pending");
        }
        UUID id =
            repo.insertPatch(plan.id(), plan.version(), section, json(patches), json(decisions));
        return patchView(
            plan,
            content,
            id,
            section,
            patches,
            decisions,
            out.path("de_xuat"),
            out.path("unsure"));
      }
      msg = PatchMessage.withErrors(base, errors);
    }
    // hai lần đều sai: không hiện bản vá (BR-S5E1-11)
    if (lockedHit) {
      return reply(plan, "reply", MSG_LOCKED, null);
    }
    turns.uncount(turn);
    return reply(plan, "error", MSG_FAILED, null);
  }

  // ---------------------------------------------------------------- nhận / bỏ / hoàn tác / khoá

  @Transactional
  public Map<String, Object> accept(String idOrCode, UUID patchId, LinesBody body) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    Patch patch =
        repo.patch(plan.id(), patchId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    ArrayNode patches = (ArrayNode) mapper.readTree(patch.patchesJson());
    ArrayNode decisions = (ArrayNode) mapper.readTree(patch.decisionsJson());
    List<Integer> lines = selected(body, decisions);
    if (lines.isEmpty()) {
      throw new AppException(ErrorCode.CONFLICT, "Không còn dòng nào chờ nhận.");
    }
    ObjectNode content = (ObjectNode) mapper.readTree(plan.contentJson());
    ObjectNode forAi = forAi(content);
    ObjectNode next = content.deepCopy();
    for (int i : lines) {
      JsonNode p = patches.get(i);
      List<Seg> segs = PatchPaths.parse(p.path("path").asString(""));
      String sec = PatchPaths.section(content, segs);
      if (sec != null && plan.lockedSections().contains(sec)) {
        throw new AppException(ErrorCode.CONFLICT, MSG_LOCKED);
      }
      String op = p.path("op").asString("replace");
      if (!"add".equals(op) && !same(PatchPaths.get(forAi, segs), p.get("before"))) {
        throw new AppException(ErrorCode.CONFLICT, MSG_STALE);
      }
    }
    // áp từ chỉ số mảng lớn tới nhỏ để "remove" không làm lệch dòng sau
    lines.stream()
        .sorted((a, b) -> b - a)
        .forEach(i -> {
          JsonNode p = patches.get(i);
          PatchPaths.apply(
              next,
              PatchPaths.parse(p.path("path").asString("")),
              p.path("op").asString("replace"),
              p.get("after"));
        });
    fillAnswers(next, plan);
    int v = repo.newVersion(plan.id(), plan.version(), json(next), "patch");
    if (v == 0) {
      throw new AppException(ErrorCode.CONFLICT, MSG_STALE);
    }
    lines.forEach(i -> decisions.set(i, mapper.getNodeFactory().stringNode("accepted")));
    repo.updateDecisions(patch.id(), json(decisions));
    Plan after = finder.find(me, plan.id().toString());
    return patchView(after, next, patch.id(), patch.section(), patches, decisions, null, null);
  }

  @Transactional
  public Map<String, Object> skip(String idOrCode, UUID patchId, LinesBody body) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    Patch patch =
        repo.patch(plan.id(), patchId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    ArrayNode patches = (ArrayNode) mapper.readTree(patch.patchesJson());
    ArrayNode decisions = (ArrayNode) mapper.readTree(patch.decisionsJson());
    selected(body, decisions)
        .forEach(i -> decisions.set(i, mapper.getNodeFactory().stringNode("skipped")));
    repo.updateDecisions(patch.id(), json(decisions));
    return patchView(
        plan,
        (ObjectNode) mapper.readTree(plan.contentJson()),
        patch.id(),
        patch.section(),
        patches,
        decisions,
        null,
        null);
  }

  /** Hoàn tác (BR-S5E1-12): quay về phiên bản trước, giữ nhãn người của phiên bản đó. */
  @Transactional
  public Map<String, Object> undo(String idOrCode) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    if (plan.version() <= 1 || !"ready".equals(plan.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Không còn phiên bản trước để hoàn tác.");
    }
    int target = plan.version() - 1;
    String content =
        repo.versionContent(plan.id(), target)
            .orElseThrow(() ->
                new AppException(ErrorCode.CONFLICT, "Không còn phiên bản trước để hoàn tác."));
    String label =
        plan.approvedVersion() != null && plan.approvedVersion() == target ? "approved" : "ai";
    if (repo.moveTo(plan.id(), plan.version(), target, content, label) == 0) {
      throw new AppException(ErrorCode.CONFLICT, MSG_STALE);
    }
    return state(finder.find(me, plan.id().toString()));
  }

  /** Khoá / mở khoá một mục (muc_tieu, thiet_bi, HDn, PHTn). */
  public Map<String, Object> lock(String idOrCode, LockBody body) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    if (plan.contentJson() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Giáo án chưa sẵn sàng để sửa.");
    }
    String section = body == null ? null : body.section();
    if (section == null || !sections(mapper.readTree(plan.contentJson())).contains(section)) {
      throw AppException.invalidField("section", "Mục không có trong giáo án.");
    }
    Set<String> locked = new LinkedHashSet<>(plan.lockedSections());
    if (body.locked() == null || body.locked()) {
      locked.add(section);
    } else {
      locked.remove(section);
    }
    repo.setLocked(plan.id(), List.copyOf(locked));
    return state(finder.find(me, plan.id().toString()));
  }

  /** Trạng thái phiên bản của giáo án (sau Nhận / Hoàn tác / khoá). */
  public Map<String, Object> state(Plan plan) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("draftId", plan.id().toString());
    m.put("code", plan.code());
    m.put("version", plan.version());
    m.put("approvedVersion", plan.approvedVersion());
    m.put("humanLabel", "approved".equals(plan.humanLabel()) ? "Giáo viên đã duyệt" : "AI soạn");
    m.put("lockedSections", plan.lockedSections());
    m.put("canUndo", plan.version() > 1);
    return m;
  }

  // ---------------------------------------------------------------- kiểm bản vá

  private void validate(
      ObjectNode content,
      ObjectNode forAi,
      String selected,
      List<String> locked,
      JsonNode patches,
      int soTiet,
      List<String> errors,
      boolean[] lockedHit) {
    if (!patches.isArray() || patches.isEmpty()) {
      errors.add("\"patches\" phải có ít nhất một dòng");
      return;
    }
    ObjectNode copy = forAi.deepCopy();
    for (int i = 0; i < patches.size(); i++) {
      JsonNode p = patches.get(i);
      String path = p.path("path").asString("");
      String op = p.path("op").asString("");
      List<Seg> segs = PatchPaths.parse(path);
      if (!List.of("replace", "add", "remove").contains(op)) {
        errors.add("dòng " + (i + 1) + ": op phải là replace, add hoặc remove");
        continue;
      }
      if (segs == null) {
        errors.add("dòng " + (i + 1) + ": path \"" + path + "\" sai cú pháp");
        continue;
      }
      String sec = PatchPaths.section(content, segs);
      if (sec == null) {
        errors.add("dòng " + (i + 1) + ": path \"" + path + "\" nằm ngoài phạm vi được sửa");
        continue;
      }
      if (locked.contains(sec)) {
        lockedHit[0] = true;
        errors.add("dòng " + (i + 1) + ": path \"" + path + "\" chạm mục đã khoá " + sec + " (A6)");
        continue;
      }
      if (selected != null && !selected.equals(sec)) {
        errors.add(
            "dòng " + (i + 1) + ": path \"" + path + "\" nằm ngoài mục đang chọn " + selected);
        continue;
      }
      JsonNode cur = PatchPaths.get(forAi, segs);
      if ("add".equals(op)) {
        if (cur == null || !cur.isArray()) {
          errors.add("dòng " + (i + 1) + ": op add phải trỏ tới một danh sách");
          continue;
        }
      } else if (cur == null || !same(cur, p.get("before"))) {
        errors.add(
            "dòng " + (i + 1) + ": \"before\" không trùng nguyên văn giá trị đang có ở " + path);
        continue;
      }
      if (p.path("after").isObject() && p.path("after").hasNonNull("dap_so")) {
        errors.add("dòng " + (i + 1) + ": \"dap_so\" phải là null (A3, đáp số do máy chủ điền)");
        continue;
      }
      if (!p.path("ly_do").isString() || p.path("ly_do").asString("").isBlank()) {
        errors.add("dòng " + (i + 1) + ": thiếu \"ly_do\"");
        continue;
      }
      try {
        PatchPaths.apply(copy, segs, op, p.get("after"));
      } catch (RuntimeException e) {
        errors.add("dòng " + (i + 1) + ": không áp được path " + path);
      }
    }
    if (errors.isEmpty()) {
      int total = 0;
      for (JsonNode a : copy.path("hoat_dong")) {
        if (!a.path("o_nha").asBoolean(false)) {
          total += a.path("thoi_luong_phut").asInt(0);
        }
      }
      if (total != soTiet * 45) {
        errors.add(
            "tổng thời lượng trên lớp sau khi vá là "
                + total
                + "', phải bằng "
                + soTiet * 45
                + "' (A5)");
      }
    }
  }

  /** So nguyên văn: chữ với chữ, số với số; đối tượng / danh sách so toàn bộ. */
  static boolean same(JsonNode cur, JsonNode before) {
    if (cur == null || before == null || before.isNull()) {
      return cur == null && (before == null || before.isNull());
    }
    if (cur.isNumber() && before.isNumber()) {
      return cur.doubleValue() == before.doubleValue();
    }
    return cur.equals(before);
  }

  // ---------------------------------------------------------------- tiện ích

  private Map<String, Object> reply(Plan plan, String kind, String text, JsonNode deXuat) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("kind", kind);
    m.put("text", text);
    m.put("version", plan.version());
    m.put("deXuat", proposals(deXuat));
    return m;
  }

  private Map<String, Object> patchView(
      Plan plan,
      ObjectNode content,
      UUID id,
      String section,
      ArrayNode patches,
      ArrayNode decisions,
      JsonNode deXuat,
      JsonNode unsure) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("kind", "patch");
    m.put("patchId", id.toString());
    m.put("section", section);
    m.putAll(state(plan));
    List<Map<String, Object>> lines = new ArrayList<>();
    for (int i = 0; i < patches.size(); i++) {
      JsonNode p = patches.get(i);
      List<Seg> segs = PatchPaths.parse(p.path("path").asString(""));
      Map<String, Object> l = new LinkedHashMap<>();
      l.put("index", i);
      l.put("op", p.path("op").asString(""));
      l.put("path", p.path("path").asString(""));
      l.put("label", segs == null ? "" : PatchPaths.label(PatchPaths.section(content, segs), segs));
      l.put("before", display(p.get("before")));
      l.put("after", display(p.get("after")));
      l.put("lyDo", p.path("ly_do").asString(""));
      l.put("decision", decisions.path(i).asString("pending"));
      lines.add(l);
    }
    m.put("lines", lines);
    m.put("deXuat", proposals(deXuat));
    List<String> u = new ArrayList<>();
    if (unsure != null) {
      unsure.forEach(x -> u.add(x.asString("")));
    }
    m.put("unsure", u);
    return m;
  }

  private static String display(JsonNode n) {
    if (n == null || n.isNull()) {
      return null;
    }
    if (n.isObject() && n.has("de")) {
      return n.path("de").asString("");
    }
    return n.isValueNode() ? n.asString("") : n.toString();
  }

  private static List<Map<String, String>> proposals(JsonNode deXuat) {
    List<Map<String, String>> out = new ArrayList<>();
    if (deXuat != null && deXuat.isArray()) {
      for (JsonNode d : deXuat) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("viTri", d.path("vi_tri").asString(""));
        m.put("vanDe", d.path("van_de").asString(""));
        m.put("goiY", d.path("goi_y").asString(""));
        m.put("lyDo", d.path("ly_do").asString(""));
        out.add(m);
      }
    }
    return out;
  }

  private static List<Integer> selected(LinesBody body, ArrayNode decisions) {
    List<Integer> out = new ArrayList<>();
    for (int i = 0; i < decisions.size(); i++) {
      boolean wanted = body == null || body.lines() == null || body.lines().contains(i);
      if (wanted && "pending".equals(decisions.get(i).asString(""))) {
        out.add(i);
      }
    }
    if (body != null && body.lines() != null) {
      for (Integer i : body.lines()) {
        if (i == null || i < 0 || i >= decisions.size()) {
          throw AppException.invalidField("lines", "Dòng bản vá không tồn tại.");
        }
      }
    }
    return out;
  }

  /** Mục sửa / khoá được: muc_tieu, thiet_bi, mã từng HĐ, mã từng phiếu. */
  static Set<String> sections(JsonNode content) {
    Set<String> s = new LinkedHashSet<>(List.of("muc_tieu", "thiet_bi"));
    content.path("hoat_dong").forEach(a -> s.add(a.path("ma").asString("")));
    content.path("phieu_hoc_tap").forEach(p -> s.add(p.path("ma").asString("")));
    s.remove("");
    return s;
  }

  /** Giáo án gửi AI: bỏ đáp số và nguồn đáp số (A3, đáp số do máy chủ điền). */
  ObjectNode forAi(ObjectNode content) {
    ObjectNode c = content.deepCopy();
    for (JsonNode ph : c.path("phieu_hoc_tap")) {
      for (JsonNode q : ph.path("cau")) {
        if (q instanceof ObjectNode o) {
          o.putNull("dap_so");
          o.remove("dap_so_nguon");
        }
      }
    }
    return c;
  }

  /** Đáp số do máy chủ điền từ kho (BR-S5E1-07); câu mới: "Cần xem lại". */
  private void fillAnswers(ObjectNode content, Plan plan) {
    Period period = catalog.period(plan.periodId()).orElseThrow();
    Map<String, Exercise> bank =
        catalog.exercises(period.lessonNo()).stream()
            .collect(Collectors.toMap(Exercise::code, e -> e, (a, b) -> a));
    for (JsonNode ph : content.path("phieu_hoc_tap")) {
      for (JsonNode q : ph.path("cau")) {
        if (q instanceof ObjectNode o) {
          Exercise e = bank.get(o.path("nguon").asString(""));
          if (e != null && e.answer() != null) {
            o.put("dap_so", e.answer());
            o.put("dap_so_nguon", "kho");
          } else {
            o.putNull("dap_so");
            o.put("dap_so_nguon", "can-xem-lai");
          }
        }
      }
    }
  }

  private List<Knowledge> provided(Period period) {
    Set<String> wanted = new LinkedHashSet<>();
    List<Knowledge> all = catalog.knowledge();
    all.stream().filter(k -> k.lessonNo() == period.lessonNo()).forEach(k -> wanted.add(k.code()));
    wanted.addAll(period.extraKnowledge());
    return all.stream()
        .filter(k -> wanted.contains(k.code()))
        .filter(k -> "da-kiem".equals(k.status()) || "nhap-tam".equals(k.status()))
        .toList();
  }

  private JsonNode parse(String text) {
    String t = text == null ? "" : text.strip();
    if (t.startsWith("```")) {
      int a = t.indexOf('\n');
      int b = t.lastIndexOf("```");
      t = a > 0 && b > a ? t.substring(a + 1, b) : t;
    }
    try {
      JsonNode n = mapper.readTree(t);
      if (n != null && n.has("ket_qua")) {
        n = n.get("ket_qua"); // output_config.format của schema-so-sua.json bọc trong "ket_qua"
      }
      return n != null && n.isObject() ? n : null;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private void checkQuota(AuthPrincipal me) {
    ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM);
    LocalDate today = now.toLocalDate();
    if (turns.turnsOn(me.userId(), today) >= dailyLimit) {
      ZonedDateTime midnight = today.plusDays(1).atStartOfDay(TimeProperties.VIETNAM);
      throw AppException.rateLimited(Duration.between(now, midnight), MSG_QUOTA);
    }
  }

  private String json(Object node) {
    return mapper.writeValueAsString(node);
  }
}
