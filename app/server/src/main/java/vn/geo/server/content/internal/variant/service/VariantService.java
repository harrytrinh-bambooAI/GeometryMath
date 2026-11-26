package vn.geo.server.content.internal.variant.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.LibraryQuota;
import vn.geo.server.content.internal.variant.repo.VariantRepository;
import vn.geo.server.content.internal.variant.repo.VariantRepository.SourceDoc;
import vn.geo.server.content.internal.variant.repo.VariantRepository.VariantRow;

/**
 * Biến thể đề {@code ProblemVariant} (US-S2E9-01). Từ một đề "Của tôi" có mô tả dựng hình phẳng:
 * sinh 1…10 bộ số liệu mới theo hạt giống (engine hình học, không gọi AI), mỗi bộ thành MỘT đề mới
 * trong "Của tôi" kèm phiên bản 1 nhãn "AI soạn" / "Cần xem lại" (BR-S2E9-06) và đáp số do engine
 * tính lại. Mỗi biến thể tính một đề vào giới hạn thêm đề của tài khoản (BR-S2E6-13) nên mỗi biến
 * thể ghi trong một giao dịch riêng: chạm giới hạn giữa chừng thì các biến thể trước vẫn giữ.
 */
@Service
public class VariantService {

  public record Item(
      UUID id,
      UUID docId,
      UUID sourceDocId,
      String title,
      String statement,
      long seed,
      int ordinal,
      List<ParamView> params,
      String answer,
      String approx,
      String answerSource,
      String machineLabel,
      String humanLabel,
      Instant createdAt) {}

  public record ParamView(String name, String value) {}

  public record Batch(int requested, int created, long seed, String message, List<Item> items) {}

  static final String MSG_COUNT = "Chọn từ 1 đến 10";

  private final VariantRepository repo;
  private final LibraryQuota quota;
  private final Function<Map<String, Object>, Map<String, Object>> generator;
  private final TransactionTemplate tx;
  private final JsonMapper mapper;
  private final Clock clock;

  public VariantService(
      VariantRepository repo,
      LibraryQuota quota,
      @Qualifier("planeVariantGenerator")
          Function<Map<String, Object>, Map<String, Object>> generator,
      TransactionTemplate tx,
      JsonMapper mapper,
      Clock clock) {
    this.repo = repo;
    this.quota = quota;
    this.generator = generator;
    this.tx = tx;
    this.mapper = mapper;
    this.clock = clock;
  }

  /** Tạo biến thể. Đề của người khác: NOT_FOUND (BR-S2-06). */
  public Batch create(UUID docId, Integer count, Long seed) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (docId == null) {
      throw AppException.invalidField("docId", "Hãy chọn đề gốc");
    }
    if (count == null || count < 1 || count > 10) {
      throw AppException.invalidField("count", MSG_COUNT);
    }
    SourceDoc src =
        repo.source(me.userId(), docId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    long useSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, 1_000_000_000L);
    Plan plan = plan(src.statement(), src.figure(), count, useSeed);
    List<Item> items = new ArrayList<>();
    int ordinal = 0;
    for (Planned p : plan.variants()) {
      ordinal++;
      int ord = ordinal;
      UUID newDoc = Uuid7.next();
      UUID variantId = Uuid7.next();
      Instant at = clock.instant();
      String title = title(src.title(), ord);
      try {
        tx.executeWithoutResult(s -> {
          quota.consume(LibraryQuota.Channel.COPY, newDoc); // BR-S2E9-06: tính vào giới hạn thêm đề
          repo.insertDoc(newDoc, me.userId(), title, p.statement(), p.figure(), p.answer(), at);
          repo.insertFirstVersion(
              Uuid7.next(), newDoc, p.stepsJson(), note(src.title(), useSeed), at);
          repo.insertVariant(
              variantId,
              src.id(),
              newDoc,
              me.userId(),
              useSeed,
              ord,
              p.paramsJson(),
              p.answer(),
              p.value(),
              at);
        });
      } catch (AppException e) {
        if (e.code() == ErrorCode.RATE_LIMITED) {
          Duration wait = e.retryAfter() == null ? Duration.ofSeconds(60) : e.retryAfter();
          throw AppException.rateLimited(
              wait,
              "Bạn đã đủ 20 đề trong 60 phút: tạo được "
                  + items.size()
                  + " biến thể. Vui lòng thử lại sau.");
        }
        throw e;
      }
      items.add(
          new Item(
              variantId,
              newDoc,
              src.id(),
              title,
              p.statement(),
              useSeed,
              ord,
              p.params(),
              p.answer(),
              p.approx(),
              "engine",
              "Cần xem lại",
              "AI soạn",
              at));
    }
    return new Batch(count, items.size(), useSeed, plan.message(), items);
  }

  /** Biến thể đã tạo từ một đề gốc của chính giáo viên. */
  public List<Item> list(UUID sourceDocId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    repo.source(me.userId(), sourceDocId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return repo.bySource(me.userId(), sourceDocId).stream().map(this::item).toList();
  }

  /** Thông tin biến thể của một đề (đề đó là biến thể); không phải biến thể: NOT_FOUND. */
  public Item ofDoc(UUID docId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.byDoc(me.userId(), docId)
        .map(this::item)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  // ------------------------------------------------------------------ phần thuần (kiểm thử đơn vị)

  /** Một biến thể đã tính xong, chưa ghi. */
  public record Planned(
      String statement,
      String figure,
      List<ParamView> params,
      String paramsJson,
      String answer,
      double value,
      String approx,
      String stepsJson) {}

  public record Plan(List<Planned> variants, String message) {}

  /**
   * Tính các biến thể của một đề (không ghi gì, không gọi AI). Từ chối bằng CONFLICT kèm câu "Đề
   * này không có số liệu để đổi" / "Dạng này chưa hỗ trợ biến thể" (BR-S2E9-05).
   */
  @SuppressWarnings("unchecked")
  public Plan plan(String statement, String figureJson, int count, long seed) {
    Map<String, Object> ask = new HashMap<>();
    ask.put("figure", figureJson);
    ask.put("op", "params");
    Map<String, Object> got = generator.apply(ask);
    refuse(got);
    List<StatementNumbers.Param> params = new ArrayList<>();
    for (Map<String, Object> p : (List<Map<String, Object>>) got.get("params")) {
      params.add(
          new StatementNumbers.Param(
              (Integer) p.get("index"),
              (String) p.get("type"),
              (List<String>) p.get("of"),
              ((Number) p.get("value")).doubleValue()));
    }
    List<StatementNumbers.Located> located = StatementNumbers.locate(statement, params);
    if (located.isEmpty()) {
      throw new AppException(ErrorCode.CONFLICT, "Đề này không có số liệu để đổi");
    }
    Map<Integer, StatementNumbers.Param> byIndex = new HashMap<>();
    params.forEach(p -> byIndex.put(p.index(), p));
    ask = new HashMap<>();
    ask.put("figure", figureJson);
    ask.put("op", "generate");
    ask.put("variable", located.stream().map(StatementNumbers.Located::index).toList());
    ask.put("count", count);
    ask.put("seed", seed);
    got = generator.apply(ask);
    refuse(got);
    JsonNode asked = mapper.readTree(figureJson).path("plane").path("asked");
    List<Planned> out = new ArrayList<>();
    for (Map<String, Object> v : (List<Map<String, Object>>) got.get("variants")) {
      List<Double> values = (List<Double>) v.get("values");
      Map<Integer, Double> next = new HashMap<>();
      List<ParamView> views = new ArrayList<>();
      ArrayNode json = mapper.createArrayNode();
      for (StatementNumbers.Located l : located) {
        double x = values.get(l.index());
        next.put(l.index(), x);
        ParamView pv = new ParamView(name(byIndex.get(l.index())), StatementNumbers.format(x));
        if (views.stream().noneMatch(e -> e.name().equals(pv.name()))) {
          views.add(pv);
          json.addObject().put("name", pv.name()).put("value", x);
        }
      }
      String answer = (String) v.get("answer");
      String approx = (String) v.get("approx");
      String given =
          String.join(", ", views.stream().map(e -> e.name() + " = " + e.value()).toList());
      String askedName = asked.path("name").asString("");
      ArrayNode steps = mapper.createArrayNode();
      steps.add("Số liệu mới: " + given + ".");
      steps.add(
          "Đáp số do engine tính lại trên hình dựng: "
              + (askedName.isBlank() ? "" : askedName + " = ")
              + answer
              + (answer.contains("√") ? " ≈ " + approx : "")
              + ".");
      out.add(
          new Planned(
              StatementNumbers.rewrite(statement, located, next),
              (String) v.get("figure"),
              views,
              json.toString(),
              answer,
              ((Number) v.get("value")).doubleValue(),
              approx,
              steps.toString()));
    }
    return new Plan(out, (String) got.get("message"));
  }

  private static void refuse(Map<String, Object> got) {
    Object refusal = got.get("refusal");
    if (refusal != null) {
      throw new AppException(ErrorCode.CONFLICT, (String) refusal);
    }
  }

  private static String name(StatementNumbers.Param p) {
    List<String> of = p.of() == null ? List.of() : p.of();
    return switch (p.type()) {
      case "length" -> String.join("", of);
      case "radius" -> "R";
      case "height" -> "chiều cao";
      case "angle" -> "góc " + String.join("", of);
      default -> p.type();
    };
  }

  static String title(String source, int ordinal) {
    String suffix = " · biến thể " + ordinal;
    String base =
        source.length() + suffix.length() > 200
            ? source.substring(0, 200 - suffix.length())
            : source;
    return base + suffix;
  }

  private static String note(String source, long seed) {
    String s = "Biến thể (hạt giống " + seed + ") của: " + source;
    return s.length() > 200 ? s.substring(0, 200) : s;
  }

  private Item item(VariantRow r) {
    List<ParamView> params = new ArrayList<>();
    for (JsonNode p : mapper.readTree(r.params())) {
      params.add(
          new ParamView(
              p.path("name").asString(""), StatementNumbers.format(p.path("value").asDouble())));
    }
    String approx =
        r.answerValue() == null
            ? null
            : String.format(java.util.Locale.ROOT, "%.2f", r.answerValue())
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "")
                .replace('.', ',');
    return new Item(
        r.id(),
        r.docId(),
        r.sourceDocId(),
        r.title(),
        r.statement(),
        r.seed(),
        r.ordinal(),
        params,
        r.answer(),
        approx,
        r.answerSource(),
        machine(r.machineStatus()),
        human(r.humanStatus()),
        r.createdAt());
  }

  private static String machine(String status) {
    return "verified".equals(status) ? "Đã kiểm chứng" : "Cần xem lại";
  }

  private static String human(String status) {
    if (status == null) {
      return "Chưa có lời giải";
    }
    return switch (status) {
      case "ai_drafted" -> "AI soạn";
      case "editing" -> "Đang sửa";
      case "teacher_approved" -> "Giáo viên đã duyệt";
      default -> "Bị thay thế";
    };
  }
}
