package vn.geo.server.learning.api;

import java.util.List;
import java.util.UUID;

/**
 * Kiểm soát đáp án (E6, US-GEO-609; ADR-GEO-14, D-GEO-156) cho câu trả lời hỏi đáp: khi lời giải
 * của bài còn khóa với người hỏi, câu trả lời lộ đáp số (mọi dạng viết tương đương, số đã làm tròn,
 * đáp số ngắn đứng làm kết luận), xác nhận/phủ nhận giá trị, chứa bước giải, hoặc câu hỏi đòi đáp
 * số/bước giải, đều được thay bằng MSG-GEO-35 kèm gợi ý cao nhất em đã xem. Mỗi lần chặn ghi
 * answer_guard_log. Đóng khi lỗi (KS-5): bộ kiểm lỗi thì ném {@link GuardFailure}, nơi gọi không
 * gửi câu trả lời của AI xuống (MSG-GEO-14).
 *
 * <p>Thay bộ tối thiểu của E4 (QF-E4-D02) ở cùng chỗ gọi (TrackingService.ask).
 */
public interface AnswerGuardApi {

  /** Học sinh đang đăng nhập hỏi trong một bài (lớp hoặc tự học). */
  Screened screen(UUID assignmentId, String question, String reply);

  /**
   * Màn xem trước của giáo viên (US-GEO-607-FC2): đề kho luôn coi như đang khóa; không ghi gì của
   * học sinh.
   *
   * @param highestHint gợi ý cao nhất giáo viên đã mở trong xem trước (null nếu chưa)
   */
  Screened screenPreview(UUID problemVersionId, String question, String reply, String highestHint);

  /**
   * Bộ câu thử (US-GEO-610, Dev-E6-03): văn bản có lộ đáp số theo dạng viết tương đương (R2), giá
   * trị số đã làm tròn (R3) hay đáp số ngắn làm kết luận (R4) của lời giải cho sẵn không. Hàm
   * thuần, không truy cập dữ liệu, không ghi gì; câu thay thế MSG-GEO-35 của máy chủ không bị tính.
   *
   * @return mã quy tắc (R2, R3, R4) hoặc null
   */
  String leakRule(String text, String finalAnswer, List<String> stepBodies, List<String> forms);

  /** Phiên bản bộ kiểm ghi vào qa_turn.guard_version và báo cáo bộ câu thử. */
  String version();

  /**
   * @param blocked câu gốc bị chặn
   * @param text câu được phép gửi và lưu (câu gốc hoặc câu thay thế MSG-GEO-35)
   * @param rule mã quy tắc chặn (R1…R6) hoặc null
   */
  record Screened(boolean blocked, String text, String rule) {}

  /** Bộ kiểm lỗi: không được gửi câu trả lời gốc (đóng khi lỗi, D-GEO-156, 183). */
  final class GuardFailure extends RuntimeException {
    public GuardFailure(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
