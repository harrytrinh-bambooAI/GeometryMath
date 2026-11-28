package vn.geo.server.geometry.internal.stepcheck;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StepCheck (S2-E5, US-S2E5-02): kiểm số từng bước của một lời giải. Hàm thuần, tất định, không gọi
 * AI, không đọc CSDL.
 *
 * <p>Quy tắc (BR-S2E5-07, 08; đã chốt PO-66, QA-S2E5-02):
 *
 * <ul>
 *   <li>Mỗi bước tách thành các mệnh đề; mệnh đề có dấu bằng là một phép tính {@code tên = biểu
 *       thức = … = giá trị ghi}.
 *   <li>Biểu thức ở giữa tính lại bằng dữ kiện của đề và giá trị các bước TRƯỚC đã ghi (kể cả giá
 *       trị sai: lỗi báo ở bước phát sinh, không lan). Thiếu giá trị đầu vào: "Không kiểm được".
 *   <li>Mệnh đề chỉ có {@code tên = giá trị}: máy tính lại trên hình engine dựng (độ dài đoạn, góc,
 *       tỉ số lượng giác); góc sau một tỉ số lượng giác ở bước trước thì tính ngược từ tỉ số đó.
 *   <li>Đạt khi lệch tuyệt đối ≤ 0,01, hoặc giá trị ghi là cách làm tròn đúng của giá trị máy tính
 *       tới số chữ số thập phân đã ghi (ít nhất một chữ số; vd 54,7° cho 54,7356°).
 *   <li>Bước không có phép tính số, hoặc không đọc được biểu thức: "Không kiểm được", không tính là
 *       lệch.
 * </ul>
 */
public final class StepCheck {

  private StepCheck() {}

  public static final double TOLERANCE = 0.01;
  public static final String R_PASS = "máy tính lại khớp";
  public static final String R_NONE = "bước không có phép tính";
  public static final String R_UNREADABLE = "không đọc được biểu thức";
  public static final String R_MISSING = "thiếu giá trị đầu vào từ bước trước";

  /** Kết quả của một bước. */
  public enum Status {
    PASS,
    FAIL,
    UNVERIFIABLE
  }

  /** Kết quả kiểm một bước; {@code written}/{@code computed} có khi bước có phép tính. */
  public record StepResult(
      int stepNo, Status status, String reason, Double written, Double computed) {}

  /**
   * Đối chiếu đáp số cuối với engine: {@code state} = match | mismatch | unknown (máy không có đáp
   * số, hoặc lời giải không ghi đáp số).
   */
  public record FinalCheck(
      String state, String writtenText, Double written, Double computed, String note) {}

  /**
   * Ngữ cảnh của đề: tọa độ đỉnh hình engine dựng (a = 1; rỗng khi không có hình), các câu dữ kiện
   * (đề bài, thẻ ràng buộc), đáp số engine (null khi không có) và đáp số có phải số đo góc không.
   */
  public record Context(
      Map<String, double[]> points,
      List<String> givenTexts,
      Double askedValue,
      boolean askedIsAngle) {}

  /** Kết quả cả lời giải. */
  public record Outcome(List<StepResult> steps, FinalCheck fin) {}

  /** So giá trị ghi với giá trị máy tính lại (BR-S2E5-08). */
  public static boolean within(String writtenText, double written, double computed) {
    if (Math.abs(written - computed) <= TOLERANCE + 1e-9) {
      return true;
    }
    int d = decimals(writtenText);
    if (d >= 1 && d <= 8) {
      double scale = Math.pow(10, d);
      return Math.abs(Math.round(computed * scale) / scale - written) <= 1e-9;
    }
    return false;
  }

  private static final Pattern PLAIN =
      Pattern.compile("^[-−]?\\d+(?:[.,](\\d+))?\\s*(?:°|độ|cm[²³]?|dm[²³]?|m[²³]?)?$");

  private static int decimals(String text) {
    Matcher m = PLAIN.matcher(text == null ? "" : text.strip());
    return m.matches() && m.group(1) != null ? m.group(1).length() : 0;
  }

  static String fmt(double v, int max) {
    BigDecimal b = BigDecimal.valueOf(v).setScale(max, RoundingMode.HALF_UP).stripTrailingZeros();
    if (b.signum() == 0) {
      return "0";
    }
    return b.toPlainString().replace('.', ',');
  }

  // ---------------------------------------------------------------- môi trường tên -> giá trị

  private static final class Env implements Expr.Names {
    final Map<String, double[]> points;
    final Map<String, Double> segments = new HashMap<>();
    final Map<String, Double> angles = new HashMap<>();
    final Map<String, Double> trigs = new HashMap<>();
    final Map<String, Double> symbols = new HashMap<>();
    String lastTrigFn;
    Double lastTrigValue;

    Env(Map<String, double[]> points) {
      this.points = points == null ? Map.of() : points;
      symbols.put("a", 1.0);
    }

    static List<String> pts(String name) {
      List<String> out = new ArrayList<>();
      String s = name.replace('′', '\'');
      for (int i = 0; i < s.length(); i++) {
        if (i + 1 < s.length() && s.charAt(i + 1) == '\'') {
          out.add(s.substring(i, i + 2));
          i++;
        } else {
          out.add(s.substring(i, i + 1));
        }
      }
      return out;
    }

    static String key(String name) {
      List<String> p = pts(name);
      if (p.get(0).compareTo(p.get(p.size() - 1)) > 0) {
        java.util.Collections.reverse(p);
      }
      return String.join("", p);
    }

    @Override
    public Double segment(String name) {
      return segments.get(key(name));
    }

    @Override
    public Double angle(String name) {
      return angles.get(key(name));
    }

    @Override
    public Double trig(String fn, String angle) {
      return trigs.get(fn + "|" + key(angle));
    }

    @Override
    public Double symbol(String name) {
      return symbols.get(name);
    }

    Double figureSegment(String name) {
      List<String> p = pts(name);
      if (p.size() != 2 || !points.containsKey(p.get(0)) || !points.containsKey(p.get(1))) {
        return null;
      }
      return dist(points.get(p.get(0)), points.get(p.get(1)));
    }

    Double figureAngle(String name) {
      List<String> p = pts(name);
      if (p.size() != 3 || !p.stream().allMatch(points::containsKey)) {
        return null;
      }
      double[] o = points.get(p.get(1));
      double[] u = sub(points.get(p.get(0)), o);
      double[] v = sub(points.get(p.get(2)), o);
      double nu = Math.sqrt(dot(u, u));
      double nv = Math.sqrt(dot(v, v));
      if (nu < 1e-12 || nv < 1e-12) {
        return null;
      }
      double c = Math.max(-1, Math.min(1, dot(u, v) / (nu * nv)));
      return Math.toDegrees(Math.acos(c));
    }

    private static double[] sub(double[] a, double[] b) {
      double[] r = new double[3];
      for (int i = 0; i < 3; i++) {
        r[i] = (i < a.length ? a[i] : 0) - (i < b.length ? b[i] : 0);
      }
      return r;
    }

    private static double dot(double[] a, double[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double dist(double[] a, double[] b) {
      double[] d = sub(a, b);
      return Math.sqrt(dot(d, d));
    }
  }

  // ---------------------------------------------------------------- tách mệnh đề

  private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
  private static final Pattern BANG =
      Pattern.compile("\\sbằng\\s+(?=[0-9√(\\-−π]|a(?!\\p{L})|\\p{Lu})");
  private static final Pattern SPLIT =
      Pattern.compile(
          ";|\\.(?=\\s|$)|,(?=\\s)|(?<=\\S):(?=\\s)"
              + "|\\s(?:nên|suy ra|vậy|do đó|mà|và|hay|khi đó|ta có|ta được|thì|tức là)\\s",
          FLAGS);
  private static final Pattern FILLER =
      Pattern.compile(
          "^(?:ta có|ta được|vậy|do đó|suy ra|nên|mà|vì|khi đó|tức là|hay|và|thì|lại có|trong đó)\\s+",
          FLAGS);
  private static final Pattern TRIG_TAIL =
      Pattern.compile("((?i:sin|cos|tan|tg|cotg|cot))\\s*(?:góc\\s*)?((?:[A-Z]['′]?){3})\\s*$");
  private static final Pattern ANGLE_TAIL =
      Pattern.compile("(?:^|[^\\p{L}'′])((?:[A-Z]['′]?){3})\\s*$");
  private static final Pattern SEG_TAIL =
      Pattern.compile("(?:^|[^\\p{L}'′])((?:[A-Z]['′]?){2})\\s*$");
  private static final Pattern OPERATOR = Pattern.compile("[0-9+\\-−*/√^·×]");

  static List<String> clauses(String step) {
    String s =
        step.replace("⇒", " ; ").replace("=>", " ; ").replace("⟹", " ; ").replace("→", " ; ");
    s = s.replace("≈", "=").replace("≃", "=");
    s = BANG.matcher(" " + s).replaceAll(" = ");
    List<String> out = new ArrayList<>();
    for (String c : SPLIT.split(s)) {
      String t = c.strip();
      while (true) {
        Matcher m = FILLER.matcher(t);
        if (!m.find()) {
          break;
        }
        t = t.substring(m.end()).strip();
      }
      if (!t.isEmpty()) {
        out.add(t);
      }
    }
    return out;
  }

  // ---------------------------------------------------------------- kiểm một mệnh đề

  private enum Kind {
    NONE,
    PASS,
    FAIL,
    MISSING,
    UNREADABLE
  }

  private enum LabelType {
    TRIG,
    ANGLE,
    SEGMENT,
    GENERIC
  }

  private record Label(LabelType type, String fn, String name) {}

  private record Written(String text, double value) {}

  private record Clause(Kind kind, String reason, Written written, Double computed, String raw) {}

  private static String clean(String s) {
    return s.strip().replaceAll("[.,;:!?]+$", "").strip();
  }

  /** Giá trị ghi: tiền tố dài nhất của vế cuối đọc được thành số (bỏ lời chú thích phía sau). */
  private static Written evalPrefix(String text, Env env) {
    String[] w = text.split("\\s+");
    int n = Math.min(w.length, 16);
    Expr.Missing missing = null;
    for (int k = n; k >= 1; k--) {
      String part = clean(String.join(" ", java.util.Arrays.copyOfRange(w, 0, k)));
      if (part.isEmpty()) {
        continue;
      }
      try {
        return new Written(part, Expr.eval(part, env));
      } catch (Expr.Missing e) {
        missing = missing == null ? e : missing;
      } catch (Expr.Unreadable e) {
        // thử tiền tố ngắn hơn
      }
    }
    if (missing != null) {
      throw missing;
    }
    throw new Expr.Unreadable(text);
  }

  private static Label label(String p0, String last) {
    Matcher t = TRIG_TAIL.matcher(p0);
    if (t.find() && !OPERATOR.matcher(p0.substring(0, t.start())).find()) {
      String fn = t.group(1).toLowerCase(Locale.ROOT);
      fn = fn.equals("tg") ? "tan" : fn.equals("cotg") ? "cot" : fn;
      return new Label(LabelType.TRIG, fn, t.group(2));
    }
    String lower = p0.toLowerCase(Locale.ROOT);
    boolean angleHint = lower.contains("góc") || p0.contains("∠") || last.contains("°");
    Matcher a = ANGLE_TAIL.matcher(p0);
    if (a.find() && !OPERATOR.matcher(p0.substring(0, a.start(1))).find()) {
      return angleHint
          ? new Label(LabelType.ANGLE, null, a.group(1))
          : new Label(LabelType.GENERIC, null, a.group(1));
    }
    Matcher s = SEG_TAIL.matcher(p0);
    if (s.find() && !OPERATOR.matcher(p0.substring(0, s.start(1))).find()) {
      return new Label(LabelType.SEGMENT, null, s.group(1));
    }
    if (angleHint && !OPERATOR.matcher(p0).find()) {
      return new Label(LabelType.ANGLE, null, null);
    }
    return null;
  }

  private static Clause clause(String c, Env env) {
    if (!c.contains("=")) {
      return new Clause(Kind.NONE, R_NONE, null, null, c);
    }
    List<String> parts = new ArrayList<>();
    for (String p : c.split("=")) {
      if (!p.isBlank()) {
        parts.add(clean(p));
      }
    }
    if (parts.size() < 2) {
      return new Clause(Kind.NONE, R_NONE, null, null, c);
    }
    int n = parts.size() - 1;
    String lastRaw = parts.get(n);
    Written w;
    try {
      w = evalPrefix(lastRaw, env);
    } catch (Expr.Missing e) {
      return new Clause(Kind.NONE, R_NONE, null, null, c);
    } catch (Expr.Unreadable e) {
      return new Clause(Kind.UNREADABLE, R_UNREADABLE, null, null, c);
    }
    String p0 = parts.get(0);
    Label label = label(p0, lastRaw);
    boolean missing = false;
    Double computed = null;
    Double mismatch = null;
    for (int i = label == null ? 0 : 1; i < n && mismatch == null; i++) {
      try {
        double v = Expr.eval(parts.get(i), env);
        if (within(w.text(), w.value(), v)) {
          computed = v;
        } else {
          mismatch = v;
        }
      } catch (Expr.Missing e) {
        missing = true;
      } catch (Expr.Unreadable e) {
        if (i == 0) {
          label = new Label(LabelType.GENERIC, null, p0);
        }
      }
    }
    if (mismatch == null && computed == null && !missing && label != null) {
      Double direct = direct(label, env);
      if (direct == null && label.type() == LabelType.ANGLE) {
        missing = true;
      } else if (direct != null && within(w.text(), w.value(), direct)) {
        computed = direct;
      } else if (direct != null) {
        mismatch = direct;
      }
    }
    remember(label, w.value(), env);
    if (mismatch != null) {
      String wrote = decimalsPlain(w.text()) ? w.text() : w.text() + " ≈ " + fmt(w.value(), 2);
      return new Clause(
          Kind.FAIL, "ghi " + wrote + "; máy tính lại " + fmt(mismatch, 4), w, mismatch, lastRaw);
    }
    if (missing) {
      return new Clause(Kind.MISSING, R_MISSING, w, null, lastRaw);
    }
    if (computed != null) {
      return new Clause(Kind.PASS, R_PASS, w, computed, lastRaw);
    }
    return new Clause(Kind.NONE, R_NONE, w, null, lastRaw);
  }

  private static boolean decimalsPlain(String text) {
    return PLAIN.matcher(text.strip()).matches();
  }

  /** Máy tính lại một đại lượng được ghi thẳng giá trị (không có biểu thức ở giữa). */
  private static Double direct(Label label, Env env) {
    switch (label.type()) {
      case TRIG:
        {
          Double a = env.figureAngle(label.name());
          return a == null ? null : Expr.trig(label.fn(), a);
        }
      case ANGLE:
        {
          if (label.name() != null) {
            for (String fn : List.of("tan", "sin", "cos", "cot")) {
              Double t = env.trig(fn, label.name());
              if (t != null) {
                return Expr.inverse(fn, t);
              }
            }
          }
          if (env.lastTrigFn != null) {
            return Expr.inverse(env.lastTrigFn, env.lastTrigValue);
          }
          return label.name() == null ? null : env.figureAngle(label.name());
        }
      case SEGMENT:
        return env.figureSegment(label.name());
      default:
        return null;
    }
  }

  private static void remember(Label label, double value, Env env) {
    if (label == null) {
      return;
    }
    switch (label.type()) {
      case TRIG -> {
        env.trigs.put(label.fn() + "|" + Env.key(label.name()), value);
        env.lastTrigFn = label.fn();
        env.lastTrigValue = value;
      }
      case ANGLE -> {
        if (label.name() != null) {
          env.angles.put(Env.key(label.name()), value);
        }
      }
      case SEGMENT -> env.segments.put(Env.key(label.name()), value);
      default -> {
        String name = label.name() == null ? "" : label.name().strip();
        if (name.matches("\\p{L}['′]?")) {
          env.symbols.put(name, value);
        }
      }
    }
  }

  // ---------------------------------------------------------------- cả lời giải

  public static Outcome run(List<String> steps, Context ctx) {
    Env env = new Env(ctx.points());
    Env givens = new Env(Map.of());
    for (String g : ctx.givenTexts() == null ? List.<String>of() : ctx.givenTexts()) {
      for (String c : clauses(g)) {
        try {
          clause(c, givens);
        } catch (RuntimeException e) {
          // dữ kiện không đọc được: bỏ qua
        }
      }
    }
    env.segments.putAll(givens.segments);
    env.angles.putAll(givens.angles);
    env.symbols.putAll(givens.symbols);
    env.symbols.put("a", 1.0);

    List<StepResult> results = new ArrayList<>();
    Clause lastValued = null;
    for (int i = 0; i < steps.size(); i++) {
      Clause fail = null;
      Clause pass = null;
      boolean missing = false;
      boolean unreadable = false;
      for (String c : clauses(steps.get(i))) {
        Clause r;
        try {
          r = clause(c, env);
        } catch (RuntimeException e) {
          r = new Clause(Kind.UNREADABLE, R_UNREADABLE, null, null, c);
        }
        if (r.written() != null) {
          lastValued = r;
        }
        switch (r.kind()) {
          case FAIL -> fail = fail == null ? r : fail;
          case PASS -> pass = r;
          case MISSING -> missing = true;
          case UNREADABLE -> unreadable = true;
          default -> {}
        }
      }
      int no = i + 1;
      if (fail != null) {
        results.add(
            new StepResult(
                no, Status.FAIL, fail.reason(), fail.written().value(), fail.computed()));
      } else if (missing) {
        results.add(new StepResult(no, Status.UNVERIFIABLE, R_MISSING, null, null));
      } else if (pass != null) {
        results.add(
            new StepResult(no, Status.PASS, R_PASS, pass.written().value(), pass.computed()));
      } else {
        results.add(
            new StepResult(
                no, Status.UNVERIFIABLE, unreadable ? R_UNREADABLE : R_NONE, null, null));
      }
    }
    return new Outcome(results, finalCheck(lastValued, ctx));
  }

  private static FinalCheck finalCheck(Clause last, Context ctx) {
    if (ctx.askedValue() == null) {
      return new FinalCheck(
          "unknown",
          last == null ? null : last.written().text(),
          last == null ? null : last.written().value(),
          null,
          "Máy chưa có đáp số để đối chiếu");
    }
    String unit = ctx.askedIsAngle() ? "°" : "";
    String machine = fmt(ctx.askedValue(), 4) + unit;
    if (last == null) {
      return new FinalCheck(
          "unknown", null, null, ctx.askedValue(), "Lời giải chưa ghi đáp số cuối");
    }
    String text = last.written().text();
    String raw = last.raw().toLowerCase(Locale.ROOT);
    boolean degree = raw.contains("°") || raw.contains("độ");
    boolean unitOk = ctx.askedIsAngle() ? degree && !raw.contains("rad") : !degree;
    if (unitOk && within(text, last.written().value(), ctx.askedValue())) {
      return new FinalCheck("match", text, last.written().value(), ctx.askedValue(), null);
    }
    String note =
        unitOk
            ? "Đáp số cuối (" + text + ") lệch máy tính lại (" + machine + ")"
            : "Đáp số cuối (" + text + ") sai đơn vị so với máy tính lại (" + machine + ")";
    return new FinalCheck("mismatch", text, last.written().value(), ctx.askedValue(), note);
  }
}
