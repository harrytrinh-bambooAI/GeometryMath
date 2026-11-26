package vn.geo.server.assignment.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bộ bài luyện đã giao, nhìn từ module khác (tracking ghi lượt thử, báo chấm sai). Mỗi câu là một
 * {@code AssignmentItem} gắn một hàng {@code assignment} MVP (PLAN_code-s23 A4, BR-S2E8-32).
 *
 * <p>{@link Grading} chứa đáp số chụp lúc giao: CHỈ dùng để chấm ở máy chủ, không bao giờ đưa vào
 * dữ liệu trả về cho học sinh (BR-S2E8-12, BR-S2E8-24).
 */
public interface PracticeSets {

  /** Một bộ trong danh sách "Bài luyện" của học sinh. */
  record Summary(UUID id, String title, String status, String className, int itemCount) {}

  /**
   * Một câu, phần học sinh được thấy.
   *
   * @param problemKind plane | space (chọn bộ vẽ hình)
   * @param releaseState unset | upcoming | open (giờ mở lời giải của hàng assignment)
   */
  record Item(
      UUID assignmentId,
      int position,
      String statement,
      String problemKind,
      int maxAttempts,
      Instant releaseAt,
      String releaseState) {}

  /** Bộ đã giao (assigned | closed) của một lớp học sinh đang học. */
  record SetView(UUID id, String title, String status, String className, List<Item> items) {}

  /** Cấu hình chấm của một câu (có đáp số — chỉ dùng trong máy chủ). */
  record Grading(
      UUID setId,
      String setTitle,
      String setStatus,
      UUID assignmentId,
      int position,
      String answer,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      List<String> acceptedForms,
      String hint1) {}

  /** Bộ đã giao của các lớp em đang học (Đang học), mới nhất trước. */
  List<Summary> listForStudent(UUID studentId);

  /** Bộ đã giao nếu em đang học ở lớp được giao; ngược lại rỗng (BR-S2E8-19). */
  Optional<SetView> forStudent(UUID studentId, UUID setId);

  /** Cấu hình chấm của câu nếu em đang học ở lớp được giao. */
  Optional<Grading> gradingForStudent(UUID studentId, UUID assignmentId);

  /** Gợi ý mức 1 của từng câu trong bộ (khóa = assignmentId); câu không có gợi ý thì vắng. */
  java.util.Map<UUID, String> hintsForStudent(UUID studentId, UUID setId);

  /** Cấu hình chấm của các câu thuộc bộ do giáo viên này giao (để đọc báo chấm sai). */
  List<Grading> gradingsOfTeacher(UUID teacherId);

  /** Thêm một dạng viết vào danh sách chấp nhận của câu (BR-S2E8-31); sai chủ thì false. */
  boolean addAcceptedForm(UUID teacherId, UUID assignmentId, String form);
}
