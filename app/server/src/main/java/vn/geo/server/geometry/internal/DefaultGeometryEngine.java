package vn.geo.server.geometry.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.AnswerMatch;
import vn.geo.server.geometry.api.Constraint;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneBuildException.Reason;
import vn.geo.server.geometry.api.SceneDto;

/** Engine mặc định (hàm thuần, BR-GEO-129). */
@Component
public class DefaultGeometryEngine implements GeometryEngine {

  /** Ngưỡng ràng buộc (BR-GEO-127; D-GEO-040, 163). */
  static final double CONSTRAINT_TOL = 1e-6 + AnswerMatch.EPS;

  private final Map<String, ProblemSpec> catalog;

  public DefaultGeometryEngine(JsonMapper mapper) {
    Map<String, ProblemSpec> m = new LinkedHashMap<>();
    try (InputStream in =
        DefaultGeometryEngine.class.getResourceAsStream("/geometry/problem-specs.json")) {
      JsonNode root = mapper.readTree(in);
      for (var e : root.properties()) {
        if (e.getValue().isObject()) {
          m.put(e.getKey(), specFrom(e.getValue()));
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được geometry/problem-specs.json", e);
    }
    this.catalog = Map.copyOf(m);
  }

  @Override
  public Optional<ProblemSpec> catalogSpec(String problemCode) {
    return Optional.ofNullable(problemCode == null ? null : catalog.get(problemCode));
  }

  @Override
  public ProblemSpec specFrom(JsonNode n) {
    String template = n.path("template").asString(null);
    if (template == null || !n.path("params").isObject()) {
      throw new IllegalArgumentException("mô tả dựng thiếu template hoặc params");
    }
    Map<String, String> params = new LinkedHashMap<>();
    for (var e : n.get("params").properties()) {
      JsonNode v = e.getValue();
      // số: dạng thập phân thường (không ký hiệu E mà AnswerMatch không đọc; soát E2 #6); chuỗi giữ
      // nguyên ("2a")
      params.put(e.getKey(), v.isNumber() ? plain(v.asDouble()) : v.asString());
    }
    List<SceneDto.SceneChip> chips = new ArrayList<>();
    for (JsonNode c : n.path("chips")) {
      List<String> pts = new ArrayList<>();
      c.path("points").forEach(x -> pts.add(x.asString()));
      chips.add(new SceneDto.SceneChip(c.path("text").asString(), List.copyOf(pts)));
    }
    List<AskedQuantity> asked = new ArrayList<>();
    for (JsonNode q : n.path("asked")) {
      List<String> pts = new ArrayList<>();
      q.path("points").forEach(x -> pts.add(x.asString()));
      asked.add(new AskedQuantity(q.path("name").asString(""), q.path("kind").asString(), pts));
    }
    List<String> hidden = new ArrayList<>();
    n.path("hidden").forEach(x -> hidden.add(x.asString()));
    return new ProblemSpec(
        template,
        params,
        n.path("unit").asString(""),
        List.copyOf(chips),
        List.copyOf(asked),
        List.copyOf(hidden));
  }

  /** Số thực ra chuỗi thập phân không ký hiệu khoa học: 1e-5 → "0.00001", 2.0 → "2". */
  static String plain(double x) {
    if (!Double.isFinite(x)) {
      throw new IllegalArgumentException("tham số không hữu hạn");
    }
    return java.math.BigDecimal.valueOf(x).stripTrailingZeros().toPlainString();
  }

  @Override
  public Figure build(String template, Map<String, Double> params) {
    return Builders.build(template, params);
  }

  @Override
  public List<String> paramKeys(String template) {
    Builders.Spec sp = template == null ? null : Builders.ALL.get(template);
    return sp == null ? List.of() : List.copyOf(sp.params());
  }

  @Override
  public boolean knows(String template) {
    return template != null && Builders.ALL.containsKey(template);
  }

  @Override
  public ConstraintCheck check(Map<String, double[]> points, List<Constraint> constraints) {
    double longest = 0;
    for (Constraint c : constraints) {
      List<String> n = c.points();
      for (int i = 0; i + 1 < n.size(); i += 2) {
        double[] p = points.get(n.get(i));
        double[] q = points.get(n.get(i + 1));
        if (p != null && q != null) {
          longest = Math.max(longest, V.dist(p, q));
        }
      }
    }
    double scale = longest > 0 ? 1 / longest : 1;
    List<ConstraintResult> out = new ArrayList<>();
    boolean all = true;
    for (Constraint c : constraints) {
      double err = error(points, c, scale);
      boolean ok = err <= CONSTRAINT_TOL;
      all &= ok;
      out.add(new ConstraintResult(c, err, ok));
    }
    return new ConstraintCheck(all, List.copyOf(out));
  }

  private static double error(Map<String, double[]> pts, Constraint c, double scale) {
    double[][] p = new double[c.points().size()][];
    for (int i = 0; i < p.length; i++) {
      p[i] = pts.get(c.points().get(i));
      if (p[i] == null) {
        return Double.POSITIVE_INFINITY;
      }
    }
    return switch (c.kind()) {
      case "len" -> Math.abs(V.dist(p[0], p[1]) - c.value()) * scale;
      case "eq" -> Math.abs(V.dist(p[0], p[1]) - V.dist(p[2], p[3])) * scale;
      case "perp" -> {
        double[] u = V.sub(p[1], p[0]);
        double[] v = V.sub(p[3], p[2]);
        double nu = V.norm(u);
        double nv = V.norm(v);
        yield nu == 0 || nv == 0 ? 1 : Math.abs(V.dot(u, v)) / (nu * nv);
      }
      default -> Double.POSITIVE_INFINITY;
    };
  }

  @Override
  public Figure fromAi(
      Map<String, double[]> points, List<List<String>> edges, List<List<String>> faces) {
    if (points == null || points.size() < 4) {
      throw bad("cần ít nhất 4 đỉnh");
    }
    LinkedHashMap<String, double[]> pts = new LinkedHashMap<>();
    double span = 0;
    for (var e : points.entrySet()) {
      double[] p = e.getValue();
      if (p == null || p.length != 3) {
        throw bad("tọa độ sai: " + e.getKey());
      }
      for (double x : p) {
        if (!Double.isFinite(x)) {
          throw bad("tọa độ không hữu hạn: " + e.getKey());
        }
      }
      for (double[] q : pts.values()) {
        span = Math.max(span, V.dist(p, q));
      }
      pts.put(e.getKey(), p.clone());
    }
    double tiny = Math.max(span, 1e-12) * 1e-9;
    List<double[]> list = new ArrayList<>(pts.values());
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        if (V.dist(list.get(i), list.get(j)) <= tiny) {
          throw bad("hai đỉnh trùng nhau");
        }
      }
    }
    if (!spansSpace(list, tiny)) {
      throw bad("các đỉnh cùng nằm trên một mặt phẳng");
    }
    if (edges == null || edges.isEmpty()) {
      throw bad("không có cạnh");
    }
    Set<String> names = new HashSet<>(pts.keySet());
    for (List<String> e : edges) {
      if (e.size() != 2 || !names.containsAll(e)) {
        throw bad("cạnh trỏ đỉnh không có");
      }
    }
    List<List<String>> fs = faces == null ? List.of() : faces;
    for (List<String> f : fs) {
      if (f.size() < 3 || !names.containsAll(f)) {
        throw bad("mặt trỏ đỉnh không có");
      }
    }
    return new Figure(
        null, null, Map.of(), pts, List.copyOf(edges), List.copyOf(fs), List.of(), List.of());
  }

  private static boolean spansSpace(List<double[]> pts, double tiny) {
    double[] a = pts.get(0);
    double[] b = null;
    double best = 0;
    for (double[] p : pts) {
      if (V.dist(a, p) > best) {
        best = V.dist(a, p);
        b = p;
      }
    }
    double[] c = null;
    best = 0;
    for (double[] p : pts) {
      double d = V.distToLine(p, a, b);
      if (d > best) {
        best = d;
        c = p;
      }
    }
    if (c == null || best <= tiny) {
      return false;
    }
    for (double[] p : pts) {
      if (V.distToPlane(p, a, b, c) > tiny) {
        return true;
      }
    }
    return false;
  }

  private static SceneBuildException bad(String why) {
    return new SceneBuildException(Reason.BAD_COORDINATES, "tọa độ AI hỏng: " + why);
  }

  @Override
  public double measure(Figure f, AskedQuantity q) {
    List<String> n = q.points() == null ? List.of() : q.points();
    // dữ liệu AI có thể thiếu điểm (soát E2 #2): kiểm số điểm theo loại trước khi tính, lỗi là
    // IllegalArgumentException
    Integer need = q.kind() == null ? null : ARITY.get(q.kind());
    if (need == null) {
      throw new IllegalArgumentException("đại lượng không biết: " + q.kind());
    }
    boolean ok = "volume_prism".equals(q.kind()) ? n.size() >= need : n.size() == need;
    if (!ok) {
      throw new IllegalArgumentException(
          "đại lượng " + q.kind() + " cần " + need + " điểm, nhận " + n.size());
    }
    return switch (q.kind()) {
      case "length" -> V.dist(f.point(n.get(0)), f.point(n.get(1)));
      case "angle" -> {
        double[] o = f.point(n.get(1));
        double[] u = V.sub(f.point(n.get(0)), o);
        double[] v = V.sub(f.point(n.get(2)), o);
        yield Math.toDegrees(Math.acos(clamp(V.dot(u, v) / (V.norm(u) * V.norm(v)))));
      }
      case "angle_line_plane" -> {
        double[] u = V.sub(f.point(n.get(1)), f.point(n.get(0)));
        double[] a = f.point(n.get(2));
        double[] nn = V.cross(V.sub(f.point(n.get(3)), a), V.sub(f.point(n.get(4)), a));
        yield Math.toDegrees(Math.asin(clamp(Math.abs(V.dot(u, nn)) / (V.norm(u) * V.norm(nn)))));
      }
      case "dist_lines" -> {
        double[] a = f.point(n.get(0));
        double[] u = V.sub(f.point(n.get(1)), a);
        double[] c = f.point(n.get(2));
        double[] v = V.sub(f.point(n.get(3)), c);
        double[] nn = V.cross(u, v);
        double len = V.norm(nn);
        yield len <= 1e-12 * V.norm(u) * V.norm(v)
            ? V.distToLine(c, a, f.point(n.get(1)))
            : Math.abs(V.dot(V.sub(c, a), nn)) / len;
      }
      case "dist_point_plane" ->
          V.distToPlane(f.point(n.get(0)), f.point(n.get(1)), f.point(n.get(2)), f.point(n.get(3)));
      case "volume_prism" -> {
        double[][] base = new double[n.size() - 1][];
        for (int i = 0; i < base.length; i++) {
          base[i] = f.point(n.get(i));
        }
        double h = V.distToPlane(f.point(n.get(n.size() - 1)), base[0], base[1], base[2]);
        yield V.polygonArea(base) * h;
      }
      case "cone_lateral" -> {
        double r = V.dist(f.point(n.get(1)), f.point(n.get(2)));
        yield Math.PI * r * V.dist(f.point(n.get(0)), f.point(n.get(2)));
      }
      case "cone_volume" -> {
        double r = V.dist(f.point(n.get(1)), f.point(n.get(2)));
        yield Math.PI * r * r * V.dist(f.point(n.get(0)), f.point(n.get(1))) / 3;
      }
      case "cyl_total_area" -> {
        double h = V.dist(f.point(n.get(0)), f.point(n.get(1)));
        double r = V.dist(f.point(n.get(0)), f.point(n.get(2)));
        yield 2 * Math.PI * r * (r + h);
      }
      case "sphere_radius" -> V.dist(f.point(n.get(0)), f.point(n.get(1)));
      case "sphere_volume" -> {
        double r = V.dist(f.point(n.get(0)), f.point(n.get(1)));
        yield 4 * Math.PI * r * r * r / 3;
      }
      default -> throw new IllegalArgumentException("đại lượng không biết: " + q.kind());
    };
  }

  /**
   * Số điểm của từng loại đại lượng (volume_prism: ít nhất 4 — ba đỉnh đáy trở lên và một đỉnh
   * trên).
   */
  static final Map<String, Integer> ARITY =
      Map.ofEntries(
          Map.entry("length", 2),
          Map.entry("angle", 3),
          Map.entry("angle_line_plane", 5),
          Map.entry("dist_lines", 4),
          Map.entry("dist_point_plane", 4),
          Map.entry("volume_prism", 4),
          Map.entry("cone_lateral", 3),
          Map.entry("cone_volume", 3),
          Map.entry("cyl_total_area", 3),
          Map.entry("sphere_radius", 2),
          Map.entry("sphere_volume", 2));

  private static double clamp(double x) {
    return Math.max(-1, Math.min(1, x));
  }

  @Override
  public Verification verify(ProblemSpec spec, String aiAnswer) {
    boolean parametric = spec.parametric();
    double[] sets = parametric ? new double[] {1, 2, 3} : new double[] {1};
    List<AnswerMatch.Quantity> given;
    String parseError = null;
    try {
      given = AnswerMatch.parse(aiAnswer);
      for (AnswerMatch.Quantity q : given) {
        q.valueAt(1); // kiểm cú pháp
      }
    } catch (IllegalArgumentException e) {
      given = List.of();
      parseError = e.getMessage();
    }
    boolean countOk = !given.isEmpty() && given.size() == spec.asked().size();
    boolean sceneOk = true;
    boolean answerOk = countOk;
    List<Map<String, Object>> rows = new ArrayList<>();
    for (double a : sets) {
      Map<String, Object> row = new LinkedHashMap<>();
      if (parametric) {
        row.put("a", a);
      }
      Figure fig;
      try {
        fig = build(spec.template(), spec.paramsAt(a));
      } catch (SceneBuildException e) {
        sceneOk = false;
        answerOk = false;
        row.put("scene_ok", false);
        row.put("error", e.getMessage());
        rows.add(row);
        continue;
      }
      boolean sat = check(fig.points(), fig.constraints()).satisfied();
      sceneOk &= sat;
      row.put("scene_ok", sat);
      List<Map<String, Object>> qs = new ArrayList<>();
      for (int i = 0; i < spec.asked().size(); i++) {
        AskedQuantity aq = spec.asked().get(i);
        double computed = measure(fig, aq);
        Map<String, Object> qrow = new LinkedHashMap<>();
        qrow.put("name", aq.name());
        qrow.put("computed", computed);
        AnswerMatch.Quantity mine = countOk ? AnswerMatch.pick(given, aq.name(), i) : null;
        boolean ok = false;
        if (mine != null) {
          double v = mine.valueAt(a);
          double tol = AnswerMatch.tolerance(parametric, computed, mine.decimals());
          ok = Math.abs(v - computed) <= tol;
          qrow.put("given", v);
          qrow.put("tolerance", tol);
        }
        qrow.put("match", ok);
        answerOk &= ok;
        qs.add(qrow);
      }
      row.put("quantities", qs);
      rows.add(row);
    }
    String status = sceneOk && answerOk ? "verified" : "needs_review";
    Map<String, Object> report = new LinkedHashMap<>();
    report.put("engine", "e2-engine-1");
    report.put("coord_source", "engine");
    report.put("template", spec.template());
    report.put("parametric", parametric);
    report.put(
        "tolerance",
        parametric
            ? Map.of("kind", "relative", "rel", AnswerMatch.REL, "eps", AnswerMatch.EPS)
            : Map.of(
                "kind",
                "absolute",
                "min",
                AnswerMatch.ABS_MIN,
                "rule",
                "max(1e-6, nửa đơn vị chữ số thập phân cuối) + 1e-12"));
    if (!countOk) {
      report.put(
          "answer_error",
          parseError != null ? "không đọc được đáp số: " + parseError : "số đại lượng không khớp");
    }
    report.put("param_sets", rows);
    report.put("scene_ok", sceneOk);
    report.put("answer_ok", answerOk);
    report.put("result", status);
    return new Verification(sceneOk, answerOk, status, label(status), report);
  }

  static String label(String status) {
    return "verified".equals(status) ? LABEL_VERIFIED : LABEL_NEEDS_REVIEW;
  }

  @Override
  public SceneDto toScene(Figure f, SceneMeta meta) {
    List<SceneDto.ScenePoint> pts = new ArrayList<>();
    f.points().forEach((k, p) -> pts.add(new SceneDto.ScenePoint(k, p[0], p[1], p[2])));
    boolean ai = "ai".equals(meta.coordSource());
    String status = ai ? "needs_review" : meta.verifyStatus();
    SceneDto.SceneWarning warning =
        ai ? new SceneDto.SceneWarning("MSG-GEO-16", MSG_16, false) : null;
    return new SceneDto(
        SceneDto.SCHEMA_VERSION,
        "3d",
        meta.sceneId(),
        ai ? "ai" : "engine",
        ai ? null : f.template(),
        ai ? null : f.solidKind(),
        meta.unit() == null ? "" : meta.unit(),
        f.params() == null ? Map.of() : f.params(),
        pts,
        f.edges(),
        f.faces(),
        f.surfaces(),
        meta.chips() == null ? List.of() : meta.chips(),
        meta.hidden() == null ? List.of() : meta.hidden(),
        new SceneDto.SceneAxes(true, List.of(0.0, 0.0, 0.0), "z"),
        new SceneDto.SceneVerification(status, label(status), warning));
  }
}
