package vn.geo.server.geometry.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Engine dựng hình và kiểm chứng (US-GEO-201, 203). Hàm thuần: cùng đầu vào cho cùng kết quả
 * (BR-GEO-129), không gọi AI, không đọc CSDL.
 */
public interface GeometryEngine {

  /** Nhãn máy (BR-GEO-136). */
  String LABEL_VERIFIED = "Đã kiểm chứng";

  String LABEL_NEEDS_REVIEW = "Cần xem lại";

  /** MSG-GEO-16: cảnh báo cố định trên khung hình đề lạ. */
  String MSG_16 =
      "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề.";

  /**
   * Dựng hình theo bộ dựng và tham số.
   *
   * @throws SceneBuildException mâu thuẫn, suy biến hoặc bộ dựng không biết
   */
  Figure build(String template, Map<String, Double> params);

  /**
   * Dựng hình của một đề tại a và đặt tên điểm theo đề (DECISIONS ca6594d, câu mở sau T-05): bộ
   * dựng mặt cầu đặt tâm O, điểm trên mặt cầu M; đề gọi tâm I, điểm A thì hình (và cảnh) mang nhãn
   * I, A.
   */
  default Figure build(ProblemSpec spec, double a) {
    return build(spec.template(), spec.paramsAt(a));
  }

  /** Bộ dựng engine biết (SCENE-CONTRACT.md §3). */
  boolean knows(String template);

  /** Khóa tham số bộ dựng cần, theo thứ tự; bộ dựng không biết: danh sách rỗng. */
  List<String> paramKeys(String template);

  /** Mô tả dựng của đề kho theo mã bài (HKG-01…06), từ {@code geometry/problem-specs.json}. */
  java.util.Optional<ProblemSpec> catalogSpec(String problemCode);

  /**
   * Đọc mô tả dựng từ JSON {template, params, unit?, chips?, asked?, hidden?} (dạng của
   * problem-specs.json và của kết quả AI "scene"); params nhận số hoặc biểu thức theo a.
   *
   * @throws IllegalArgumentException JSON thiếu trường bắt buộc
   */
  ProblemSpec specFrom(tools.jackson.databind.JsonNode node);

  /** Kiểm ràng buộc theo BR-GEO-127 (quy cạnh lớn nhất về 1; sai số ≤ 1e-6 + 1e-12). */
  ConstraintCheck check(Map<String, double[]> points, List<Constraint> constraints);

  /**
   * Hình từ tọa độ AI (đề lạ): kiểm tọa độ dùng được (ít nhất 4 đỉnh, không trùng nhau, không cùng
   * mặt phẳng; cạnh/mặt trỏ đỉnh có thật).
   *
   * @throws SceneBuildException BAD_COORDINATES (F-GEO-111)
   */
  Figure fromAi(Map<String, double[]> points, List<List<String>> edges, List<List<String>> faces);

  /** Giá trị một đại lượng đề hỏi tính trên tọa độ. */
  double measure(Figure figure, AskedQuantity quantity);

  /**
   * Kiểm chứng đáp số AI của một đề dựng bằng engine (BR-GEO-07, 133, 134). Chỉ xét đáp số; mọi
   * trường "trạng thái" AI tự khai không có chỗ để truyền vào (BR-GEO-135).
   */
  Verification verify(ProblemSpec spec, String aiAnswer);

  /** Cảnh theo hợp đồng v1 (không chứa giá trị tính hay đáp số). */
  SceneDto toScene(Figure figure, SceneMeta meta);

  /**
   * Đại lượng đề hỏi. kind: angle_line_plane [P, Q, A, B, C] (độ) · dist_lines [A, B, C, D] ·
   * dist_point_plane [P, A, B, C] · length [P, Q] · angle [P, O, Q] (độ) · volume_prism [đáy…, đỉnh
   * mặt trên] · cone_lateral [S, O, M] · cone_volume [S, O, M] · cyl_total_area [O, O′, M] ·
   * sphere_radius [I, A] · sphere_volume [I, A] · sphere_area [I, A] (ba loại mặt cầu cũng nhận bốn
   * điểm không đồng phẳng trên cầu: cầu ngoại tiếp) · cyl_lateral [O, O′, M] · cyl_volume [O, O′,
   * M] · cone_total_area [S, O, M] · pyramid_volume [đáy…, đỉnh] · pyramid_lateral [đỉnh, đáy…] ·
   * prism_lateral [đáy…, đỉnh mặt trên] (lăng trụ đứng).
   */
  record AskedQuantity(String name, String kind, List<String> points) {}

  /**
   * Mô tả dựng của một đề: bộ dựng, tham số dạng biểu thức theo a ("a", "2a", "4"), đơn vị, thẻ
   * ràng buộc, đại lượng đề hỏi, đại lượng ẩn khi đo.
   */
  record ProblemSpec(
      String template,
      Map<String, String> params,
      String unit,
      List<SceneDto.SceneChip> chips,
      List<AskedQuantity> asked,
      List<String> hidden) {

    /** Đề có tham số a (BR-GEO-134). */
    public boolean parametric() {
      return params.values().stream()
          .anyMatch(v -> new AnswerMatch.Quantity("", v, 0).dependsOnA());
    }

    /** Tham số số tại a. */
    public Map<String, Double> paramsAt(double a) {
      Map<String, Double> out = new java.util.LinkedHashMap<>();
      params.forEach((k, v) -> out.put(k, new AnswerMatch.Quantity("", v, 0).valueAt(a)));
      return out;
    }
  }

  /** Kết quả kiểm ràng buộc. */
  record ConstraintCheck(boolean satisfied, List<ConstraintResult> results) {}

  /** Sai số một ràng buộc (đã quy cạnh lớn nhất về 1). */
  record ConstraintResult(Constraint constraint, double error, boolean ok) {}

  /**
   * Kết quả kiểm chứng: nhãn theo bảng trạng thái của US-GEO-203; {@code report} là nội dung cột
   * {@code verification_report.checks} (chứa giá trị tính: chỉ lưu qua hàm cổng, không gửi xuống
   * thiết bị học sinh).
   */
  record Verification(
      boolean sceneOk, boolean answerOk, String status, String label, Map<String, Object> report) {}

  /** Phần cảnh không do engine quyết: id đã lưu, đơn vị, thẻ, đại lượng ẩn, nhãn, nguồn. */
  record SceneMeta(
      String sceneId,
      String coordSource,
      String unit,
      List<SceneDto.SceneChip> chips,
      List<String> hidden,
      String verifyStatus) {}

  /**
   * Chữ ký chuẩn của đề (D-GEO-150, ADR-GEO-15; E4 US-GEO-406): (bộ dựng; bộ tham số đã quy về a =
   * 1 — chia cho tham số đầu tiên của bộ dựng, làm tròn 6 chữ số; đại lượng đề hỏi = loại và GIÁ
   * TRỊ đo trên bản dựng với bộ tham số đã quy, làm tròn 6 chữ số). Không phụ thuộc tên điểm, cách
   * chọn điểm, cách viết, chữ thêm. {@code canonical} có dạng {@code
   * 2|pyramid_square_sa|a=1;sa=1|angle_line_plane=45}; {@code sha256} là băm UTF-8 của chuỗi đó
   * (cột problem_signature.signature). Rỗng khi bộ dựng không có, đề không có đại lượng hỏi, hoặc
   * không dựng/đo được.
   */
  Optional<Signature> signature(ProblemSpec spec);

  /**
   * Phiên bản thuật toán chữ ký (problem_signature.sig_version). 2 (T-05, 27/11/2026): đại lượng
   * hỏi theo giá trị đo thay cho danh sách điểm.
   */
  int SIG_VERSION = 2;

  /** Chữ ký chuẩn của một đề; {@code solidKind} để tra solid_template. */
  record Signature(
      String template,
      String solidKind,
      Map<String, Double> paramsNorm,
      String asked,
      String canonical,
      byte[] sha256) {}
}
