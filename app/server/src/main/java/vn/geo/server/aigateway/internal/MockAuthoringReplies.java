package vn.geo.server.aigateway.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * S2-E4 (T-261126-24): đáp mẫu GIẢ LẬP (D-GEO-047) cho task {@code authoring_chat} — chat chỉnh
 * nháp đề. Không gọi AI thật. Prompt gồm ba dòng "Đề bài: …", "Đáp số đề ghi: …", "Yêu cầu của giáo
 * viên: …". Trả một trong hai dạng JSON (cùng định dạng prompt thật trong {@link
 * AnthropicPrompts#AUTHORING_CHAT}):
 *
 * <ul>
 *   <li>{@code {"kind":"proposal","target":"statement","before":"BC = 13","after":"BC = 14",
 *       "source":"nháp hiện tại","unsure":…}} khi yêu cầu rõ ("đổi cạnh BC thành 14");
 *   <li>{@code {"kind":"proposal","target":"answer","after":"80°",…}} khi yêu cầu sửa đáp số ("sửa
 *       đáp số thành 80°");
 *   <li>{@code {"kind":"question","text":…}} khi yêu cầu mơ hồ ("làm đề khó hơn") hoặc không tìm
 *       thấy dữ kiện cần đổi — AI hỏi lại, không tự đoán (BR-S2E4-01).
 * </ul>
 */
final class MockAuthoringReplies {

  static final String TASK = "authoring_chat";

  static final String ASK_HARDER = "Khó hơn theo hướng nào: thêm ý hỏi, đổi số, hay thêm bước?";
  static final String ASK_WHAT =
      "Cô muốn chỉnh phần nào của đề: số liệu, điều kiện hay câu hỏi? Ví dụ \"đổi cạnh BC thành"
          + " 14\".";

  private static final Pattern STATEMENT =
      Pattern.compile("Đề bài: (.*?)\\nĐáp số đề ghi:", Pattern.DOTALL);
  private static final Pattern REQUEST =
      Pattern.compile("Yêu cầu của giáo viên: (.*)$", Pattern.DOTALL);
  private static final Pattern SIDE =
      Pattern.compile(
          "[Đđ]ổi (?:cạnh |đoạn |độ dài )?([A-Z]{2}) (?:thành|bằng|sang) ([0-9a-z√,.]+)");
  private static final Pattern ANSWER =
      Pattern.compile("(?:[Ss]ửa|[Đđ]ổi) đáp số (?:thành|là|sang) (\\S+)");
  private static final Pattern VAGUE = Pattern.compile("khó hơn|dễ hơn|hay hơn|tốt hơn");

  private final JsonMapper mapper;

  MockAuthoringReplies(JsonMapper mapper) {
    this.mapper = mapper;
  }

  static boolean handles(String task) {
    return TASK.equals(task);
  }

  private static String trimEnd(String v) {
    return v.replaceAll("[.,;]+$", "");
  }

  String reply(String prompt) {
    Matcher sm = STATEMENT.matcher(prompt);
    String statement = sm.find() ? sm.group(1) : "";
    Matcher rm = REQUEST.matcher(prompt);
    String request = rm.find() ? rm.group(1).strip() : prompt;
    ObjectNode out = mapper.createObjectNode();

    Matcher answer = ANSWER.matcher(request);
    if (answer.find()) {
      out.put("kind", "proposal");
      out.put("target", "answer");
      out.put("after", trimEnd(answer.group(1)));
      out.put("source", "nháp hiện tại");
      out.put("unsure", "Tôi chưa kiểm được đáp số này; máy sẽ tính lại khi cô bấm Kiểm tra lại.");
      return mapper.writeValueAsString(out);
    }
    Matcher side = SIDE.matcher(request);
    if (side.find()) {
      String name = side.group(1);
      Matcher cur =
          Pattern.compile(Pattern.quote(name) + "\\s*=\\s*[0-9a-z√,.]*[0-9a-z√]")
              .matcher(statement);
      if (cur.find()) {
        out.put("kind", "proposal");
        out.put("target", "statement");
        out.put("before", cur.group());
        out.put("after", name + " = " + trimEnd(side.group(2)));
        out.put("source", "nháp hiện tại");
        out.put(
            "unsure", "Đáp số cũ có thể không còn đúng; máy sẽ tính lại sau khi cô nhận đề xuất.");
        return mapper.writeValueAsString(out);
      }
      out.put("kind", "question");
      out.put("text", "Tôi không thấy dữ kiện " + name + " trong đề. Cô muốn đổi dữ kiện nào?");
      return mapper.writeValueAsString(out);
    }
    out.put("kind", "question");
    out.put("text", VAGUE.matcher(request).find() ? ASK_HARDER : ASK_WHAT);
    return mapper.writeValueAsString(out);
  }
}
