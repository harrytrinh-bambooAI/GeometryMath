package vn.geo.server.aigrade.internal.proposal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Kiểm cứng và luật chặn KPI cho đầu ra task {@code grade} (US-S5E6-01; BR-S5E6-02, 03; KPI v1.0
 * §2). Hàm thuần, tất định:
 *
 * <ol>
 *   <li>Kiểm cứng: đủ mọi tiêu chí của rubric, mỗi điểm trong [0, tối đa] và bội 0,25, "tong" (nếu
 *       có) bằng tổng tiêu chí. Sai: {@link Outcome#valid()} false kèm lý do.
 *   <li>Luật KPI §2.1 Hướng = 0 thì tiêu chí phụ thuộc hướng = 0.
 *   <li>Luật KPI §2.4 "không có hình"/"hình sai" thì Hình = 0 và bước {@code dua_tren_hinh} = 0.
 *   <li>Luật KPI §2.3 bước k sai thì bước {@code phu_thuoc} vào k (bắc cầu) = 0; bước độc lập giữ.
 *   <li>Luật KPI §2.2 Kết quả đạt nhưng Bước &lt; 50 % thì Kết quả tối đa 50 %.
 *   <li>Luật KPI §2.5 kiến thức dẫn ngoài kho (mã KT không có) thì cờ "bịa kiến thức", độ tin cậy
 *       tiêu chí ≤ 0,5.
 * </ol>
 *
 * Độ tin cậy tổng = Σ(tin cậy tiêu chí × điểm tối đa) / thang, sau trần 0,5 của §2.5 (QS5-22).
 */
public final class GradeRules {

  public static final String FLAG_INVENTED = "bịa kiến thức";
  public static final String REASON_OUT_OF_SCALE = "điểm ngoài thang";
  public static final String REASON_MISSING = "thiếu tiêu chí";
  public static final String REASON_TOTAL = "tổng không khớp";
  public static final String REASON_BROKEN = "đầu ra không đúng định dạng";

  static final Set<String> ERROR_LABELS =
      Set.of("sai-dinh-li", "sai-tinh-toan", "sai-hinh-dung", "nham-dieu-kien");

  private static final BigDecimal QUARTER = new BigDecimal("0.25");
  private static final BigDecimal HALF = new BigDecimal("0.5");

  private GradeRules() {}

  /** Tiêu chí của rubric phiên bản đã giao. */
  public record Criterion(int ord, String name, BigDecimal max, boolean directionDependent) {}

  /** Một lần điều chỉnh của luật chặn. */
  public record Adjustment(String rule, BigDecimal from, BigDecimal to, String note) {}

  /** Một bước bài làm mà AI liệt kê (để áp §2.3, §2.4). */
  public record Step(
      String code,
      Integer criterionOrd,
      BigDecimal points,
      boolean correct,
      List<String> dependsOn,
      boolean onFigure,
      BigDecimal finalPoints,
      String zeroedBy) {}

  /** Một tiêu chí sau kiểm và luật. */
  public record Assessed(
      int ord,
      String name,
      BigDecimal max,
      BigDecimal aiPoints,
      BigDecimal points,
      String reason,
      JsonNode region,
      double confidence,
      String errorLabel,
      List<Adjustment> adjustments,
      List<String> flags) {}

  /** Kết quả: hợp lệ (đề xuất) hoặc không (lý do "AI không chấm"). */
  public record Outcome(
      boolean valid,
      String invalidReason,
      List<Assessed> criteria,
      List<Step> steps,
      BigDecimal total,
      double confidence,
      List<String> flags) {

    static Outcome invalid(String reason) {
      return new Outcome(false, reason, List.of(), List.of(), null, 0, List.of());
    }
  }

  /** Vai của tiêu chí theo tên (RB "Tính toán" KPI §1). */
  enum Role {
    DIRECTION,
    STEPS,
    RESULT,
    FIGURE,
    OTHER
  }

  static Role role(String name) {
    String n = plain(name);
    if (n.startsWith("huong")) {
      return Role.DIRECTION;
    }
    if (n.startsWith("cac buoc") || n.startsWith("buoc")) {
      return Role.STEPS;
    }
    if (n.startsWith("ket qua")) {
      return Role.RESULT;
    }
    if (n.startsWith("hinh")) {
      return Role.FIGURE;
    }
    return Role.OTHER;
  }

  /**
   * Kiểm và áp luật lên đầu ra {@code reply} của AI.
   *
   * @param knownKnowledge mã đơn vị kiến thức có trong kho
   */
  public static Outcome apply(
      List<Criterion> rubric, BigDecimal scale, JsonNode reply, Set<String> knownKnowledge) {
    JsonNode in = reply.path("criteria");
    if (!in.isArray() || rubric.isEmpty()) {
      return Outcome.invalid(REASON_BROKEN);
    }
    // ---- kiểm cứng (BR-S5E6-02)
    Map<Integer, JsonNode> byOrd = new LinkedHashMap<>();
    for (JsonNode c : in) {
      Integer ord = ordOf(c, rubric);
      if (ord == null || byOrd.containsKey(ord)) {
        return Outcome.invalid(REASON_MISSING);
      }
      byOrd.put(ord, c);
    }
    if (byOrd.size() != rubric.size()) {
      return Outcome.invalid(REASON_MISSING);
    }
    Map<Integer, BigDecimal> pts = new LinkedHashMap<>();
    BigDecimal sum = BigDecimal.ZERO;
    for (Criterion r : rubric) {
      JsonNode c = byOrd.get(r.ord());
      JsonNode d = c.path("diem");
      if (!d.isNumber()) {
        return Outcome.invalid(REASON_OUT_OF_SCALE);
      }
      BigDecimal p = d.decimalValue();
      if (p.signum() < 0
          || p.compareTo(r.max()) > 0
          || p.remainder(QUARTER).compareTo(BigDecimal.ZERO) != 0) {
        return Outcome.invalid(REASON_OUT_OF_SCALE);
      }
      pts.put(r.ord(), p);
      sum = sum.add(p);
    }
    JsonNode tong = reply.path("tong");
    if (tong.isNumber() && tong.decimalValue().compareTo(sum) != 0) {
      return Outcome.invalid(REASON_TOTAL);
    }

    // ---- trạng thái làm việc
    Map<Integer, BigDecimal> cur = new LinkedHashMap<>(pts);
    Map<Integer, List<Adjustment>> adj = new HashMap<>();
    Map<Integer, List<String>> flags = new HashMap<>();
    Map<Integer, Double> conf = new HashMap<>();
    for (Criterion r : rubric) {
      adj.put(r.ord(), new ArrayList<>());
      flags.put(r.ord(), new ArrayList<>());
      conf.put(r.ord(), clamp01(byOrd.get(r.ord()).path("tin_cay").asDouble(0)));
    }
    Integer direction = find(rubric, Role.DIRECTION);
    Integer stepsC = find(rubric, Role.STEPS);
    Integer result = find(rubric, Role.RESULT);
    Integer figure = find(rubric, Role.FIGURE);
    List<MutableStep> steps = steps(reply.path("buoc"), rubric, stepsC);

    // §2.1 sai hướng
    if (direction != null && cur.get(direction).signum() == 0) {
      for (Criterion r : rubric) {
        if (r.directionDependent() && cur.get(r.ord()).signum() > 0) {
          set(cur, adj, r.ord(), BigDecimal.ZERO, "luật KPI §2.1", "sai hướng");
        }
      }
      for (MutableStep s : steps) {
        Criterion owner = byOrd(rubric, s.criterionOrd);
        if (owner != null && owner.directionDependent() && s.finalPoints.signum() > 0) {
          s.finalPoints = BigDecimal.ZERO;
          s.zeroedBy = "luật KPI §2.1";
        }
      }
    }
    // §2.4 không có hình / hình sai
    String hinh = plain(reply.path("hinh").asString(""));
    if (hinh.startsWith("khong co hinh") || hinh.startsWith("hinh sai")) {
      if (figure != null && cur.get(figure).signum() > 0) {
        set(cur, adj, figure, BigDecimal.ZERO, "luật KPI §2.4", reply.path("hinh").asString(""));
      }
      for (MutableStep s : steps) {
        if (s.onFigure && s.finalPoints.signum() > 0) {
          takeStep(cur, adj, s, "luật KPI §2.4", "bước " + s.code + " dựa trên hình");
        }
      }
    }
    // §2.3 bước sai kéo bước phụ thuộc (bắc cầu)
    Set<String> wrong = new LinkedHashSet<>();
    for (MutableStep s : steps) {
      if (!s.correct) {
        wrong.add(s.code);
      }
    }
    boolean changed = true;
    while (changed) {
      changed = false;
      for (MutableStep s : steps) {
        if (wrong.contains(s.code)) {
          continue;
        }
        String cause = s.dependsOn.stream().filter(wrong::contains).findFirst().orElse(null);
        if (cause != null) {
          wrong.add(s.code);
          changed = true;
          if (s.finalPoints.signum() > 0) {
            takeStep(
                cur, adj, s, "luật KPI §2.3", "bước " + s.code + " phụ thuộc bước sai " + cause);
          }
        }
      }
    }
    // §2.2 đúng kết quả nhưng lập luận thiếu
    if (result != null && stepsC != null && cur.get(result).signum() > 0) {
      Criterion st = byOrd(rubric, stepsC);
      if (cur.get(stepsC).multiply(BigDecimal.valueOf(2)).compareTo(st.max()) < 0) {
        BigDecimal cap = floorQuarter(byOrd(rubric, result).max().multiply(HALF));
        if (cur.get(result).compareTo(cap) > 0) {
          set(cur, adj, result, cap, "luật KPI §2.2", "Bước dưới 50 % tối đa");
        }
      }
    }
    // §2.5 kiến thức ngoài kho
    Set<String> overall = new LinkedHashSet<>();
    for (Criterion r : rubric) {
      for (JsonNode k : byOrd.get(r.ord()).path("kien_thuc")) {
        String code = k.isString() ? k.asString() : k.path("ma").asString("");
        if (code.isBlank() || !knownKnowledge.contains(code.strip())) {
          if (!flags.get(r.ord()).contains(FLAG_INVENTED)) {
            flags.get(r.ord()).add(FLAG_INVENTED);
            String name = k.isString() ? code : k.path("ten").asString(code);
            adj.get(r.ord())
                .add(
                    new Adjustment(
                        "luật KPI §2.5",
                        cur.get(r.ord()),
                        cur.get(r.ord()),
                        "kiến thức ngoài kho: " + cut(name, 120)));
          }
          conf.put(r.ord(), Math.min(conf.get(r.ord()), 0.5));
          overall.add(FLAG_INVENTED);
        }
      }
    }

    // ---- kết quả
    List<Assessed> out = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;
    double weighted = 0;
    for (Criterion r : rubric) {
      JsonNode c = byOrd.get(r.ord());
      String label = c.path("loi").asString("");
      out.add(
          new Assessed(
              r.ord(),
              r.name(),
              r.max(),
              pts.get(r.ord()),
              cur.get(r.ord()),
              cut(c.path("ly_do").asString(""), 300),
              region(c.path("vung")),
              conf.get(r.ord()),
              ERROR_LABELS.contains(label) ? label : null,
              List.copyOf(adj.get(r.ord())),
              List.copyOf(flags.get(r.ord()))));
      total = total.add(cur.get(r.ord()));
      weighted += conf.get(r.ord()) * r.max().doubleValue();
    }
    double confidence = round3(weighted / scale.doubleValue());
    List<Step> stepOut = new ArrayList<>();
    for (MutableStep s : steps) {
      stepOut.add(
          new Step(
              s.code,
              s.criterionOrd,
              s.points,
              s.correct,
              List.copyOf(s.dependsOn),
              s.onFigure,
              s.finalPoints,
              s.zeroedBy));
    }
    return new Outcome(true, null, out, stepOut, total, confidence, List.copyOf(overall));
  }

  // ------------------------------------------------------------------ phụ trợ

  private static final class MutableStep {
    String code;
    Integer criterionOrd;
    BigDecimal points;
    boolean correct;
    List<String> dependsOn;
    boolean onFigure;
    BigDecimal finalPoints;
    String zeroedBy;
  }

  private static List<MutableStep> steps(JsonNode in, List<Criterion> rubric, Integer stepsC) {
    List<MutableStep> out = new ArrayList<>();
    if (!in.isArray()) {
      return out;
    }
    for (JsonNode s : in) {
      MutableStep m = new MutableStep();
      m.code = cut(s.path("ma").asString("B" + (out.size() + 1)), 20);
      Integer ord = null;
      JsonNode tc = s.path("tieu_chi");
      if (!tc.isMissingNode() && !tc.isNull()) {
        ord = ordOf(tc, rubric);
      }
      m.criterionOrd = ord != null ? ord : stepsC;
      BigDecimal p = s.path("diem").isNumber() ? s.path("diem").decimalValue() : BigDecimal.ZERO;
      m.points = p.signum() < 0 ? BigDecimal.ZERO : p;
      m.correct = s.path("dung").asBoolean(true);
      m.dependsOn = new ArrayList<>();
      for (JsonNode d : s.path("phu_thuoc")) {
        m.dependsOn.add(d.asString(""));
      }
      m.onFigure = s.path("dua_tren_hinh").asBoolean(false);
      m.finalPoints = m.correct ? m.points : BigDecimal.ZERO;
      out.add(m);
    }
    return out;
  }

  /** Trừ phần điểm của bước khỏi tiêu chí chứa nó (không âm) và đưa bước về 0. */
  private static void takeStep(
      Map<Integer, BigDecimal> cur,
      Map<Integer, List<Adjustment>> adj,
      MutableStep s,
      String rule,
      String note) {
    BigDecimal part = s.finalPoints;
    s.finalPoints = BigDecimal.ZERO;
    s.zeroedBy = rule;
    if (s.criterionOrd == null || !cur.containsKey(s.criterionOrd)) {
      return;
    }
    BigDecimal before = cur.get(s.criterionOrd);
    BigDecimal after = before.subtract(part).max(BigDecimal.ZERO);
    if (after.compareTo(before) != 0) {
      set(cur, adj, s.criterionOrd, after, rule, note);
    }
  }

  private static void set(
      Map<Integer, BigDecimal> cur,
      Map<Integer, List<Adjustment>> adj,
      int ord,
      BigDecimal to,
      String rule,
      String note) {
    adj.get(ord).add(new Adjustment(rule, cur.get(ord), to, note));
    cur.put(ord, to);
  }

  /** Tiêu chí của một mục AI: "ma" dạng C1…Cn hoặc số thứ tự, hoặc "ten" trùng tên tiêu chí. */
  static Integer ordOf(JsonNode c, List<Criterion> rubric) {
    if (c.isString() || c.isNumber()) {
      return parseOrd(c.asString(""), rubric);
    }
    Integer o = parseOrd(c.path("ma").asString(""), rubric);
    if (o != null) {
      return o;
    }
    String name = plain(c.path("ten").asString(""));
    for (Criterion r : rubric) {
      if (!name.isEmpty() && plain(r.name()).equals(name)) {
        return r.ord();
      }
    }
    return null;
  }

  private static Integer parseOrd(String s, List<Criterion> rubric) {
    String t = s.strip().toUpperCase(Locale.ROOT);
    if (t.startsWith("C")) {
      t = t.substring(1);
    }
    try {
      int o = Integer.parseInt(t);
      return byOrd(rubric, o) == null ? null : o;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Criterion byOrd(List<Criterion> rubric, Integer ord) {
    if (ord == null) {
      return null;
    }
    return rubric.stream().filter(r -> r.ord() == ord).findFirst().orElse(null);
  }

  private static Integer find(List<Criterion> rubric, Role role) {
    return rubric.stream()
        .filter(r -> role(r.name()) == role)
        .map(Criterion::ord)
        .findFirst()
        .orElse(null);
  }

  /** Vùng ảnh {trang, x, y, w, h} hợp lệ, không thì null. */
  private static JsonNode region(JsonNode v) {
    if (v == null || !v.isObject()) {
      return null;
    }
    for (String k : List.of("trang", "x", "y", "w", "h")) {
      if (!v.path(k).isNumber()) {
        return null;
      }
    }
    return v;
  }

  static BigDecimal floorQuarter(BigDecimal v) {
    return v.divide(QUARTER, 0, RoundingMode.FLOOR).multiply(QUARTER);
  }

  private static double clamp01(double v) {
    return Double.isFinite(v) ? Math.max(0, Math.min(1, v)) : 0;
  }

  static double round3(double v) {
    return Math.round(v * 1000) / 1000.0;
  }

  private static String cut(String s, int n) {
    return s == null ? "" : s.length() <= n ? s : s.substring(0, n);
  }

  static String plain(String s) {
    String n =
        Normalizer.normalize(
            (s == null ? "" : s).replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).strip();
  }
}
