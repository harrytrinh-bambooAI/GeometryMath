package vn.geo.server.subject.api;

import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Khung môn học (US-S2E2-01, PLAN_code-s23 A1): mọi môn cắm vào đây với đủ sáu thao tác {@code
 * parse}, {@code solve}, {@code verify}, {@code render}, {@code hint}, {@code grade} cộng danh sách
 * {@code concepts} không rỗng (BR-S2E2-01). Thao tác không được môn cài đặt (để mặc định) là
 * "thiếu": {@link SubjectRegistry} từ chối đăng ký môn ngay lúc khởi động và ghi tên thao tác thiếu
 * (BR-S2E2-02). Môn khác (chặng 4) chỉ thêm một bản cài đặt.
 *
 * <p>Chữ ký {@code grade} giống ở mọi môn: (đáp số mẫu, đáp số nộp, dung sai) — BR-S2E2-01,
 * BR-S2-08.
 */
public interface SubjectEngine {

  /** Tên sáu thao tác bắt buộc cộng {@code concepts}, theo thứ tự báo lỗi. */
  List<String> OPERATIONS = List.of("parse", "solve", "verify", "render", "hint", "grade");

  /** Mã môn (vd "geometry"). */
  String id();

  /** Tên hiển thị (vd "Hình học"). */
  String name();

  /** Loại đề môn phục vụ ({@code SubjectRegistry.forKind}). */
  Set<ProblemKind> kinds();

  /** Phân tích đề: loại đề và mô tả có cấu trúc. */
  default ParsedProblem parse(ProblemInput input) {
    throw missing("parse");
  }

  /** Giải: đáp số tính trên hình dựng (không gọi AI). */
  default Solution solve(ParsedProblem problem) {
    throw missing("solve");
  }

  /** Kiểm chứng một đáp số (của AI hoặc người soạn) với kết quả engine. */
  default Check verify(ParsedProblem problem, String answer) {
    throw missing("verify");
  }

  /** Dựng cảnh gửi xuống thiết bị (không chứa đáp số, bước giải, gợi ý). */
  default Rendering render(ParsedProblem problem) {
    throw missing("render");
  }

  /** Gợi ý theo mức, không chứa đáp số. */
  default List<Hint> hint(ParsedProblem problem) {
    throw missing("hint");
  }

  /**
   * Chấm đáp số nộp so với đáp số mẫu.
   *
   * @param tolerance dung sai tuyệt đối; null = dung sai mặc định của môn
   */
  default GradeResult grade(String reference, String submitted, Double tolerance) {
    throw missing("grade");
  }

  /** Mã khái niệm của môn (không rỗng). */
  default List<String> concepts() {
    throw missing("concepts");
  }

  private UnsupportedOperationException missing(String op) {
    return new UnsupportedOperationException("Môn " + id() + " chưa cài đặt thao tác " + op);
  }

  /** Đầu vào của {@code parse}: văn bản đề và mô tả có cấu trúc ({@code data} có thể null). */
  record ProblemInput(String statement, JsonNode data) {}

  /** Đề đã phân tích: môn, loại đề, văn bản, mô tả có cấu trúc (dạng riêng của từng môn). */
  record ParsedProblem(String subjectId, ProblemKind kind, String statement, JsonNode data) {}

  /**
   * Kết quả {@code solve}. {@code solved} sai thì {@code answer}, {@code value} rỗng và {@code
   * reason} nói vì sao (nhãn máy "Cần xem lại"). {@code label} chỉ do engine gắn (PO-02).
   */
  record Solution(boolean solved, String answer, Double value, String label, String reason) {}

  /**
   * Kết quả {@code render}: cảnh ({@code scene}, null khi không dựng được), nhãn máy, lý do. Cảnh
   * không chứa đáp số, bước giải hay gợi ý (BR-S2E2-07).
   */
  record Rendering(boolean built, Object scene, String label, String reason) {}

  /** Kết quả {@code verify}: hình thỏa ràng buộc, đáp số khớp engine, nhãn máy, lý do. */
  record Check(boolean sceneOk, boolean answerOk, String label, String reason) {}

  /** Một mức gợi ý (1 = nhẹ nhất). Không chứa đáp số. */
  record Hint(int level, String text) {}

  /** Kết quả {@code grade}: đúng/sai, dạng chuẩn của đáp số nộp, lý do khi không chấm được. */
  record GradeResult(boolean correct, String canonical, String reason) {}
}
