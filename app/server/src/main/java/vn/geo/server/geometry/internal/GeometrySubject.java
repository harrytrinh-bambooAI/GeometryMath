package vn.geo.server.geometry.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.geo.server.geometry.api.AnswerMatch;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.internal.plane.PlaneBuildException;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.plane.PlaneFigure;
import vn.geo.server.geometry.internal.plane.PlaneSpec;
import vn.geo.server.geometry.internal.solid.SolidAdapter;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;

/**
 * Môn "Hình học" cắm vào khung môn học (US-S2E2-01): đề phẳng đi engine hình phẳng ({@link
 * PlaneEngine}), đề không gian đi engine MVP qua {@link SolidAdapter} (không đổi kết quả). Loại đề
 * (BR-S2E2-04): {@code space} khi mô tả dựng là bộ dựng 3D của MVP hoặc có điểm ngoài mặt phẳng
 * ({@code outOfPlane}), {@code plane} khi có {@code shape} phẳng.
 */
@Component
public class GeometrySubject implements SubjectEngine {

  public static final String ID = "geometry";

  /** Từ khóa đề không gian khi không có mô tả có cấu trúc. */
  private static final Pattern SPACE_WORDS =
      Pattern.compile(
          "hình chóp|lăng trụ|hình hộp|lập phương|tứ diện|hình nón|hình trụ|hình cầu|mặt cầu|⊥ ?\\(",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private final PlaneEngine plane;
  private final SolidAdapter solid;

  public GeometrySubject(PlaneEngine plane, SolidAdapter solid) {
    this.plane = plane;
    this.solid = solid;
  }

  @Override
  public String id() {
    return ID;
  }

  @Override
  public String name() {
    return "Hình học";
  }

  @Override
  public Set<ProblemKind> kinds() {
    return Set.of(ProblemKind.SPACE, ProblemKind.PLANE);
  }

  @Override
  public ParsedProblem parse(ProblemInput in) {
    return new ParsedProblem(ID, kindOf(in.statement(), in.data()), in.statement(), in.data());
  }

  /** Loại đề theo BR-S2E2-04. */
  public static ProblemKind kindOf(String statement, JsonNode data) {
    if (data != null) {
      if (data.path("outOfPlane").isArray() && !data.path("outOfPlane").isEmpty()) {
        return ProblemKind.SPACE;
      }
      if (data.hasNonNull("template") || data.hasNonNull("problemCode")) {
        return ProblemKind.SPACE;
      }
      if (data.hasNonNull("shape")) {
        return ProblemKind.PLANE;
      }
    }
    return statement != null && SPACE_WORDS.matcher(statement).find()
        ? ProblemKind.SPACE
        : ProblemKind.PLANE;
  }

  @Override
  public Solution solve(ParsedProblem p) {
    if (p.kind() == ProblemKind.SPACE) {
      return solid.solve(p.data());
    }
    try {
      PlaneSpec s = PlaneSpec.from(p.data());
      PlaneFigure f = plane.build(s);
      if (!"engine".equals(f.coordSource())) {
        return needsReview("hình do AI dựng, chưa kiểm chứng");
      }
      if (!plane.satisfied(f)) {
        return needsReview(PlaneBuildException.CONTRADICTION);
      }
      if (s.asked() == null) {
        return needsReview("đề không có đại lượng hỏi");
      }
      double v = plane.measure(f, s.asked());
      return new Solution(
          true, PlaneEngine.format(v, s.asked().kind()), v, GeometryEngine.LABEL_VERIFIED, null);
    } catch (PlaneBuildException | IllegalArgumentException e) {
      return needsReview(e.getMessage());
    }
  }

  @Override
  public Check verify(ParsedProblem p, String answer) {
    if (p.kind() == ProblemKind.SPACE) {
      return solid.verify(p.data(), answer);
    }
    Solution s = solve(p);
    if (!s.solved()) {
      return new Check(false, false, GeometryEngine.LABEL_NEEDS_REVIEW, s.reason());
    }
    boolean ok = grade(s.answer(), answer, null).correct();
    return new Check(
        true,
        ok,
        ok ? GeometryEngine.LABEL_VERIFIED : GeometryEngine.LABEL_NEEDS_REVIEW,
        ok ? null : "đáp số lệch kết quả engine " + s.answer());
  }

  @Override
  public Rendering render(ParsedProblem p) {
    if (p.kind() == ProblemKind.SPACE) {
      return solid.render(p.data());
    }
    try {
      PlaneFigure f = plane.build(PlaneSpec.from(p.data()));
      if (!"engine".equals(f.coordSource())) {
        return new Rendering(
            true, plane.scene(f), GeometryEngine.LABEL_NEEDS_REVIEW, "hình do AI dựng");
      }
      if (!plane.satisfied(f)) {
        return new Rendering(
            false, null, GeometryEngine.LABEL_NEEDS_REVIEW, PlaneBuildException.CONTRADICTION);
      }
      return new Rendering(true, plane.scene(f), GeometryEngine.LABEL_VERIFIED, null);
    } catch (PlaneBuildException | IllegalArgumentException e) {
      return new Rendering(false, null, GeometryEngine.LABEL_NEEDS_REVIEW, e.getMessage());
    }
  }

  @Override
  public List<Hint> hint(ParsedProblem p) {
    List<String> texts = new ArrayList<>();
    if (p.kind() == ProblemKind.SPACE) {
      texts.addAll(solid.hints());
    } else {
      PlaneSpec s;
      try {
        s = PlaneSpec.from(p.data());
      } catch (IllegalArgumentException e) {
        s = null;
      }
      texts.add("Ghi các dữ kiện đề cho lên hình rồi xác định đại lượng cần tìm.");
      texts.add(planeHint(s));
    }
    List<Hint> out = new ArrayList<>();
    for (int i = 0; i < texts.size(); i++) {
      out.add(new Hint(i + 1, texts.get(i)));
    }
    return out;
  }

  private static String planeHint(PlaneSpec s) {
    if (s == null) {
      return "Vẽ thêm đường phụ để đưa về tam giác đã biết.";
    }
    String kind = s.asked() == null ? "" : s.asked().kind();
    return switch (s.shape()) {
      case "isosceles_triangle" -> "Tam giác cân có hai góc ở đáy bằng nhau.";
      case "equilateral_triangle" -> "Tam giác đều có ba cạnh bằng nhau và ba góc bằng nhau.";
      case "right_triangle", "rectangle", "square" ->
          "Tìm một tam giác vuông trên hình rồi dùng định lý Pythagore.";
      case "circle_chord" -> "Kẻ đường vuông góc từ tâm tới dây: nó chia đôi dây.";
      case "circle_tangent" -> "Tiếp tuyến vuông góc với bán kính tại tiếp điểm.";
      default ->
          "angle".equals(kind)
              ? "Dùng định lý cosin hoặc tổng ba góc của tam giác."
              : "Chia hình thành các tam giác rồi tính từng phần.";
    };
  }

  @Override
  public GradeResult grade(String reference, String submitted, Double tolerance) {
    Double ref = value(reference);
    Double sub = value(submitted);
    if (ref == null) {
      return new GradeResult(false, null, "đáp số mẫu không đọc được");
    }
    if (sub == null) {
      return new GradeResult(false, null, "đáp số không đọc được");
    }
    int decimals = AnswerMatch.parse(submitted).get(0).decimals();
    double tol = tolerance == null ? AnswerMatch.tolerance(false, ref, decimals) : tolerance;
    boolean ok = Math.abs(sub - ref) <= tol + AnswerMatch.EPS;
    return new GradeResult(ok, PlaneEngine.trim(sub, 4), null);
  }

  private static Double value(String s) {
    try {
      List<AnswerMatch.Quantity> q = AnswerMatch.parse(s);
      if (q.size() != 1) {
        return null;
      }
      double v = q.get(0).valueAt(1);
      return Double.isFinite(v) ? v : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public List<String> concepts() {
    return List.of(
        "tam-giac",
        "tu-giac",
        "duong-tron",
        "goc",
        "do-dai",
        "dien-tich",
        "hinh-chop",
        "lang-tru",
        "khoang-cach",
        "the-tich");
  }

  private static Solution needsReview(String reason) {
    return new Solution(false, null, null, GeometryEngine.LABEL_NEEDS_REVIEW, reason);
  }
}
