package vn.geo.server.aigrade.internal.kpi.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tính chỉ số khớp AI–giáo viên theo KPI v1.0 §3 và xét điều kiện §4 (US-S5E6-06, BR-S5E6-15, 16).
 * Hàm thuần, không đụng CSDL (test đơn vị {@code KpiCalcTest}).
 *
 * <ul>
 *   <li>(a) điểm khớp một bài = 1 − |ΣAI − ΣGV| / thang; macro theo dạng bài; KTC 95 % kiểu t trên
 *       điểm từng bài (QS5-32); so ngưỡng bằng cận dưới.
 *   <li>(b) tỉ lệ cặp tiêu chí lệch trong ±0,25, KTC Wilson (giả định độc lập là gần đúng); so bằng
 *       cận dưới.
 *   <li>(c), (d) so bằng ước lượng điểm; cờ "chưa chắc" khi cận dưới &lt; ngưỡng. (e) so bằng số
 *       đếm.
 *   <li>(f) tỉ lệ tiêu chí bị sửa, (g) trung vị thời gian duyệt, (h) tỉ lệ AI không chấm: chỉ báo
 *       cáo.
 * </ul>
 */
public final class KpiCalc {

  static final double Z95 = 1.959963984540054;
  static final double TOL = 0.25 + 1e-9;

  private KpiCalc() {}

  /**
   * Một tiêu chí: điểm tối đa, điểm AI, điểm giáo viên (bài song song) hoặc cờ đã sửa (bài AI đề
   * xuất).
   */
  public record Crit(double max, double ai, double gv, boolean edited) {}

  /** Một bài trong số liệu KPI. */
  public record Paper(
      String topic,
      List<Crit> criteria,
      Boolean resultOk,
      int invented,
      int blocked,
      boolean noGrade,
      Integer reviewSeconds) {

    boolean graded() {
      return !noGrade && !criteria.isEmpty();
    }
  }

  /** Ngưỡng KPI §4 "khởi điểm" (cấu hình {@code geo.s5.ai-grading.gate.*}). */
  public record Thresholds(
      int minPapers,
      double macro,
      double topicFloor,
      double within,
      double result,
      double catchRate,
      double falseAlarm) {

    public static Thresholds kpiDefault() {
      return new Thresholds(20, 0.85, 0.70, 0.85, 0.95, 0.80, 0.10);
    }
  }

  /** Tỉ lệ k/n kèm KTC 95 %. */
  public record Rate(int k, int n, double value, double lower, double upper) {

    public Map<String, Object> json() {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("k", k);
      m.put("n", n);
      m.put("value", n == 0 ? null : r4(value));
      m.put("lower", n == 0 ? null : r4(lower));
      m.put("upper", n == 0 ? null : r4(upper));
      return m;
    }
  }

  /** Kết quả: số liệu (a)–(h) và danh sách lý do không đạt (rỗng = đạt). */
  public record Result(int papers, int pairs, Map<String, Object> metrics, List<String> reasons) {

    public boolean pass() {
      return reasons.isEmpty();
    }
  }

  public static Rate wilson(int k, int n) {
    if (n <= 0) {
      return new Rate(0, 0, 0, 0, 0);
    }
    double p = (double) k / n;
    double z2 = Z95 * Z95;
    double den = 1 + z2 / n;
    double centre = (p + z2 / (2.0 * n)) / den;
    double half = Z95 * Math.sqrt(p * (1 - p) / n + z2 / (4.0 * n * n)) / den;
    return new Rate(k, n, p, Math.max(0, centre - half), Math.min(1, centre + half));
  }

  /** Phân vị 97,5 % của phân phối t (khai triển Cornish–Fisher, sai số &lt; 0,01 với df ≥ 3). */
  public static double t975(int df) {
    if (df <= 0) {
      return Double.POSITIVE_INFINITY;
    }
    double z = Z95;
    double z3 = z * z * z;
    double z5 = z3 * z * z;
    double z7 = z5 * z * z;
    return z
        + (z3 + z) / (4.0 * df)
        + (5 * z5 + 16 * z3 + 3 * z) / (96.0 * df * df)
        + (3 * z7 + 19 * z5 + 17 * z3 - 15 * z) / (384.0 * df * df * df);
  }

  public static Double median(List<Integer> xs) {
    if (xs.isEmpty()) {
      return null;
    }
    List<Integer> s = new ArrayList<>(xs);
    s.sort(Integer::compare);
    int m = s.size() / 2;
    return s.size() % 2 == 1 ? (double) s.get(m) : (s.get(m - 1) + s.get(m)) / 2.0;
  }

  /**
   * @param shadow bài song song (a–e, h)
   * @param proposal bài AI đề xuất đã duyệt (f, g)
   * @param minN true = xét cả điều kiện n ≥ minPapers (cổng mở); false = xét một đợt sau khi bật
   */
  public static Result compute(
      List<Paper> shadow, List<Paper> proposal, Thresholds th, boolean minN) {
    List<Paper> graded = shadow.stream().filter(Paper::graded).toList();
    List<String> reasons = new ArrayList<>();
    Map<String, Object> m = new LinkedHashMap<>();
    int n = graded.size();
    int pairs = graded.stream().mapToInt(p -> p.criteria().size()).sum();

    // (a) điểm khớp theo bài, macro theo dạng
    Map<String, List<Double>> byTopic = new TreeMap<>();
    List<Double> scores = new ArrayList<>();
    for (Paper p : graded) {
      double ai = 0;
      double gv = 0;
      double max = 0;
      for (Crit c : p.criteria()) {
        ai += c.ai();
        gv += c.gv();
        max += c.max();
      }
      double s = max <= 0 ? 0 : Math.max(0, 1 - Math.abs(ai - gv) / max);
      scores.add(s);
      byTopic.computeIfAbsent(p.topic(), k -> new ArrayList<>()).add(s);
    }
    List<Map<String, Object>> topics = new ArrayList<>();
    double macro = 0;
    String worstTopic = null;
    double worst = 2;
    for (Map.Entry<String, List<Double>> e : byTopic.entrySet()) {
      double mean = mean(e.getValue());
      macro += mean;
      Map<String, Object> t = new LinkedHashMap<>();
      t.put("topic", e.getKey());
      t.put("n", e.getValue().size());
      t.put("value", r4(mean));
      topics.add(t);
      if (mean < worst) {
        worst = mean;
        worstTopic = e.getKey();
      }
    }
    macro = byTopic.isEmpty() ? 0 : macro / byTopic.size();
    double se = n < 2 ? Double.POSITIVE_INFINITY : sd(scores) / Math.sqrt(n);
    double half = n < 2 ? 1 : t975(n - 1) * se;
    Map<String, Object> a = new LinkedHashMap<>();
    a.put("n", n);
    a.put("value", n == 0 ? null : r4(macro));
    a.put("lower", n == 0 ? null : r4(Math.max(0, macro - half)));
    a.put("upper", n == 0 ? null : r4(Math.min(1, macro + half)));
    a.put("method", "t");
    a.put("topics", topics);
    m.put("a", a);

    // (b) cặp tiêu chí lệch trong ±0,25
    int within = 0;
    int wrong = 0;
    int caught = 0;
    int full = 0;
    int alarms = 0;
    for (Paper p : graded) {
      for (Crit c : p.criteria()) {
        if (Math.abs(c.ai() - c.gv()) <= TOL) {
          within++;
        }
        if (c.gv() < c.max()) {
          wrong++;
          if (c.ai() < c.max()) {
            caught++;
          }
        } else {
          full++;
          if (c.ai() < c.max()) {
            alarms++;
          }
        }
      }
    }
    Rate b = wilson(within, pairs);
    m.put("b", b.json());

    // (c) Kết quả đúng chiều engine
    List<Paper> withSignal = graded.stream().filter(p -> p.resultOk() != null).toList();
    Rate c = wilson((int) withSignal.stream().filter(Paper::resultOk).count(), withSignal.size());
    Map<String, Object> cj = c.json();
    cj.put("uncertain", c.n() == 0 || c.lower() < th.result());
    m.put("c", cj);

    // (d) bắt bước sai, báo nhầm
    Rate dc = wilson(caught, wrong);
    Rate df = wilson(alarms, full);
    Map<String, Object> d = new LinkedHashMap<>();
    Map<String, Object> dcj = dc.json();
    dcj.put("uncertain", dc.n() == 0 || dc.lower() < th.catchRate());
    Map<String, Object> dfj = df.json();
    dfj.put("uncertain", df.n() == 0 || df.upper() > th.falseAlarm());
    d.put("caught", dcj);
    d.put("falseAlarm", dfj);
    m.put("d", d);

    // (e) bịa kiến thức, đề xuất cuối còn vi phạm luật chặn §2
    int invented = shadow.stream().mapToInt(Paper::invented).sum();
    int blocked = shadow.stream().mapToInt(Paper::blocked).sum();
    Map<String, Object> e = new LinkedHashMap<>();
    e.put("invented", invented);
    e.put("blocked", blocked);
    e.put("n", shadow.size());
    m.put("e", e);

    // (f) tỉ lệ tiêu chí bị sửa · (g) trung vị thời gian duyệt · (h) AI không chấm
    int crit = 0;
    int edited = 0;
    List<Integer> secs = new ArrayList<>();
    for (Paper p : proposal) {
      for (Crit x : p.criteria()) {
        crit++;
        if (x.edited()) {
          edited++;
        }
      }
      if (p.reviewSeconds() != null) {
        secs.add(p.reviewSeconds());
      }
    }
    m.put("f", wilson(edited, crit).json());
    Map<String, Object> g = new LinkedHashMap<>();
    g.put("medianSeconds", median(secs));
    g.put("n", secs.size());
    m.put("g", g);
    m.put("h", wilson((int) shadow.stream().filter(Paper::noGrade).count(), shadow.size()).json());

    // Điều kiện KPI §4
    if (minN && n < th.minPapers()) {
      reasons.add(msg48(n, th.minPapers()));
    }
    if (n == 0) {
      if (!minN) {
        reasons.add("đợt không có bài");
      }
    } else {
      double lowerA = Math.max(0, macro - half);
      if (lowerA < th.macro()) {
        reasons.add("điểm khớp (a) cận dưới " + pct(lowerA) + " % < " + pct(th.macro()) + " %");
      }
      if (worstTopic != null && worst < th.topicFloor()) {
        reasons.add(
            "dạng " + worstTopic + " " + pct(worst) + " % < " + pct(th.topicFloor()) + " %");
      }
      if (b.lower() < th.within()) {
        reasons.add(
            "lệch trong ±0,25 (b) cận dưới " + pct(b.lower()) + " % < " + pct(th.within()) + " %");
      }
      if (c.n() > 0 && c.value() < th.result()) {
        reasons.add(
            "kết quả đúng chiều engine (c) " + pct(c.value()) + " % < " + pct(th.result()) + " %");
      }
      if (dc.n() > 0 && dc.value() < th.catchRate()) {
        reasons.add("bắt bước sai (d) " + pct(dc.value()) + " % < " + pct(th.catchRate()) + " %");
      }
      if (df.n() > 0 && df.value() > th.falseAlarm()) {
        reasons.add("báo nhầm (d) " + pct(df.value()) + " % > " + pct(th.falseAlarm()) + " %");
      }
    }
    if (invented > 0) {
      reasons.add("bịa kiến thức: " + invented);
    }
    if (blocked > 0) {
      reasons.add("đề xuất còn vi phạm luật chặn: " + blocked);
    }
    return new Result(n, pairs, m, List.copyOf(reasons));
  }

  /** MSG-S5-48. */
  public static String msg48(int n, int min) {
    return "Lớp chưa đủ " + n + "/" + min + " bài chạy song song để bật AI đề xuất.";
  }

  /** Phần trăm một chữ số thập phân, dấu phẩy, bỏ ",0" (66 %, 80,2 %). */
  public static String pct(double x) {
    long tenth = Math.round(x * 1000);
    String s = (tenth / 10) + "," + (tenth % 10);
    return s.endsWith(",0") ? s.substring(0, s.length() - 2) : s;
  }

  static double r4(double x) {
    return Math.round(x * 10000) / 10000.0;
  }

  private static double mean(List<Double> xs) {
    return xs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
  }

  private static double sd(List<Double> xs) {
    double mu = mean(xs);
    double ss = 0;
    for (double x : xs) {
      ss += (x - mu) * (x - mu);
    }
    return Math.sqrt(ss / (xs.size() - 1));
  }
}
