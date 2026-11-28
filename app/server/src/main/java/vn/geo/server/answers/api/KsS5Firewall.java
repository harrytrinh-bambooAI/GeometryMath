package vn.geo.server.answers.api;

import java.util.List;

/**
 * Bộ kiểm đầu ra KS-1…KS-4 mở rộng cho hỏi đáp theo kho chặng 5 (US-S5E3-05, BR-S5E3-15…17;
 * ADR-GEO-14). Hàm thuần, không truy cập dữ liệu; KHÔNG sửa lõi V60 ({@code AnswerRules}) mà gọi
 * phần dựa trên giá trị của nó qua {@code learning.api.AnswerGuardApi#leakRule} rồi thêm:
 *
 * <ul>
 *   <li>KS-4 trước khi gọi AI: câu hỏi chứa một giá trị ứng viên (mọi dạng KS-1/KS-2 của đáp số)
 *       hoặc câu xác nhận/so sánh có giá trị (đúng hay sai đều chặn).
 *   <li>KS-1 (dạng viết), KS-2 (số làm tròn), KS-3 (đáp số ngắn làm kết luận) trên câu trả lời và
 *       trên mục tài liệu trước khi gửi AI (BR-S5E3-16).
 *   <li>KS-4 trên câu trả lời: câu xác nhận/phủ nhận khi câu hỏi có giá trị.
 *   <li>{@code private_content}: câu trả lời chứa ghi chú riêng của giáo viên, rubric chi tiết hay
 *       nội dung bài của bạn (BR-S5E3-17).
 * </ul>
 *
 * KS-5 (đóng khi lỗi, quá 200 ms) do nơi gọi đo và áp.
 */
public interface KsS5Firewall {

  /** Phiên bản bộ kiểm ghi vào ai_policy_decision.firewall_version. */
  String VERSION = "ks5-s5e3-1";

  /** Lời giải đang khoá cần bảo vệ (null = bài không khoá với người hỏi). */
  record Locked(String finalAnswer, List<String> stepBodies, List<String> forms) {}

  /** KS-4 trước khi gọi AI: "KS-4" hoặc null. */
  String preCheck(String question, Locked locked);

  /**
   * Câu trả lời của AI: "KS-1" | "KS-2" | "KS-3" | "KS-4" | "private_content" hoặc null (được
   * phép).
   *
   * @param secrets văn bản không bao giờ được tới học sinh (ghi chú riêng, rubric, bài của bạn)
   */
  String check(String question, String reply, Locked locked, List<String> secrets);

  /** Văn bản (mục tài liệu) có lộ đáp số đang khoá: "KS-1" | "KS-2" | "KS-3" hoặc null. */
  String leaks(String text, Locked locked);
}
