package vn.geo.server.aigateway.api.quota;

import java.time.Instant;
import java.util.UUID;

/**
 * Công tắc và hạn mức AI theo lớp chặng 5 (US-S5E5-04, BR-S5E5-10…13, 16; QS5-04, QS5-26). Module
 * khác (giáo án, đề, thông báo, chấm, đọc chữ, tổng hợp lỗi) HỎI ở đây trước khi gọi cổng AI:
 *
 * <ul>
 *   <li><b>Lượt</b> = một yêu cầu của người dùng hoặc một việc nền tới một task; gọi lại tự động
 *       trong cùng lượt không gọi lại các hàm {@code take*}/{@code admit*}; {@code grade_ocr},
 *       {@code grade} tính mỗi trang một lượt.
 *   <li>Hạn mức mỗi ngày giờ Việt Nam: giáo viên 30 lượt soạn; lớp 300 hỏi đáp, 200 chấm, 2 000 000
 *       token; học sinh 30 hỏi đáp. Đạt 80 % thì giáo viên chủ lớp nhận một cảnh báo trong ngày.
 *   <li>Tắt khẩn toàn hệ thống theo task (người vận hành) thắng mọi công tắc lớp.
 * </ul>
 */
public interface AiQuota {

  /** MSG-S5-06 (hết 30 lượt soạn của giáo viên). */
  String MSG_COMPOSE = "Hôm nay thầy cô đã dùng hết 30 lượt soạn với AI. Lượt mới mở lúc 00:00.";

  /** Lý do cờ "AI không chấm" khi lớp hết lượt chấm (US-S5E5-04-FC3). */
  String REASON_GRADE_QUOTA = "lớp đã hết lượt AI hôm nay";

  /** Lý do cờ "AI không chấm" khi AI chấm của lớp tắt hoặc tắt khẩn. */
  String REASON_GRADE_OFF = "AI chấm của lớp đang tắt";

  /** Trợ lý (giáo án, đề, thông báo, tổng hợp lỗi) của lớp đang bật và task chưa bị tắt khẩn. */
  boolean assistantOn(UUID orgId, UUID classId, String task);

  /** AI chấm của lớp đang bật và task chưa bị tắt khẩn. */
  boolean gradingOn(UUID orgId, UUID classId, String task);

  /** Task đang bị người vận hành tắt khẩn toàn hệ thống. */
  boolean killed(String task);

  /**
   * Kiểm rồi tính MỘT lượt soạn của giáo viên (giáo án, vá, đề, thông báo, tổng hợp lỗi). Hết 30
   * lượt: RATE_LIMITED (MSG-S5-06, Retry-After tới 00:00), không tính lượt.
   */
  void takeComposeTurn(UUID orgId, UUID teacherId, UUID classId, String task);

  /** Cộng token (vào + ra) lớp đã dùng hôm nay; đạt 80 % trần thì cảnh báo giáo viên một lần. */
  void addTokens(UUID orgId, UUID classId, long tokens);

  /**
   * Nhận một lượt chấm nền (một trang của {@code grade_ocr} hoặc {@code grade}). Không nhận: không
   * tính lượt, trả lý do để bài thành "AI không chấm" (vẫn chấm tay).
   */
  Admission admitGrading(UUID orgId, UUID classId, String task);

  /** "Đọc lại" bản đọc chữ: tối đa 3 lần mỗi bài; lần thứ 4 RATE_LIMITED, không gọi AI. */
  void takeOcrRetry(UUID orgId, UUID submissionId);

  /** Đăng ký việc AI nền đang chờ của lớp (chấm nền); tắt AI chấm sẽ huỷ việc đang chờ. */
  UUID registerJob(UUID orgId, UUID classId, String task, UUID refId);

  /** Việc nền còn được chạy và lưu kết quả (chưa bị huỷ). */
  boolean jobActive(UUID orgId, UUID jobId);

  /** Đánh dấu việc nền đã xong. */
  void finishJob(UUID orgId, UUID jobId);

  /**
   * Kiểm rồi tính MỘT lượt hỏi đáp của học sinh trong lớp {@code classId} (hỏi đáp theo kho {@code
   * POST /student/kb-qa}, REVIEW-S5 #2; BR-S5E5-12, 16): tắt khẩn {@code kb_qa} → FORBIDDEN, lớp
   * hết 300 hoặc em hết 30 lượt → RATE_LIMITED kèm Retry-After, không tính lượt. Trả false (không
   * tính) khi không có lớp (bài tự học).
   */
  boolean takeStudentAsk(UUID orgId, UUID classId, UUID studentId);

  /** Cộng token thật các lượt gọi AI của {@code userId} từ {@code since} vào trần của lớp. */
  void addTokensSince(UUID orgId, UUID classId, UUID userId, Instant since);

  /** Kết quả xin lượt chấm. */
  record Admission(boolean allowed, String reason) {}
}
