package vn.geo.server.lessonplan.internal.patch.service;

import java.util.List;
import java.util.stream.Collectors;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Exercise;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;

/**
 * Tin nhắn user của task {@code lesson_patch} (prompt-final.md v1.1 §3 "Chế độ sửa", {@code
 * render()} của run.py): {@code <giao_an_hien_tai>} · {@code <muc_dang_chon>} · {@code
 * <muc_da_khoa>} · {@code <yeu_cau_giao_vien>} · {@code <kien_thuc_duoc_cap>} · {@code
 * <kho_bai_tap>}. Câu chat của giáo viên là DỮ LIỆU (A10): không cho mở/đóng thẻ. Không chứa tên
 * hiển thị, mã người dùng hay mã lớp (NFR-GEO-071).
 */
final class PatchMessage {

  private PatchMessage() {}

  static String build(
      ObjectNode current,
      String section,
      List<String> locked,
      String request,
      List<Knowledge> provided,
      List<Exercise> bank) {
    StringBuilder out = new StringBuilder();
    out.append(block("phieu_yeu_cau", "Chế độ: sửa giáo án đang có (trả bản vá tối thiểu)"));
    out.append("\n\n").append(block("giao_an_hien_tai", current.toString()));
    out.append("\n\n").append(block("muc_dang_chon", section == null ? "(cả giáo án)" : section));
    out.append("\n\n")
        .append(block("muc_da_khoa", locked.isEmpty() ? "(không có)" : String.join(", ", locked)));
    out.append("\n\n").append(block("yeu_cau_giao_vien", data(request)));
    out.append("\n\n")
        .append(
            block(
                "kien_thuc_duoc_cap",
                provided.stream()
                    .map(k -> "- " + k.code() + ": " + k.statement())
                    .collect(Collectors.joining("\n"))));
    out.append("\n\n")
        .append(
            block(
                "kho_bai_tap",
                bank.stream()
                    .map(e ->
                        "- "
                            + e.code()
                            + " ["
                            + e.level()
                            + "] "
                            + e.statement().replace('\n', ' '))
                    .collect(Collectors.joining("\n"))));
    return out.toString();
  }

  /** Lần gọi lại kèm danh sách lỗi kiểm máy chủ (prompt-final §4.3). */
  static String withErrors(String message, List<String> errors) {
    return message
        + "\n\n<kiem_tra_may_chu>\nBản vá lần trước của bạn chưa qua kiểm của máy chủ:\n- "
        + String.join("\n- ", errors)
        + "\nHãy trả lại TOÀN BỘ đầu ra đã sửa các lỗi trên, không chạm mục đã khoá, \"before\""
        + " phải trùng nguyên văn.\n</kiem_tra_may_chu>";
  }

  private static String block(String tag, String body) {
    return "<" + tag + ">\n" + body + "\n</" + tag + ">";
  }

  private static String data(String s) {
    return s.replace('<', '‹').replace('>', '›');
  }
}
