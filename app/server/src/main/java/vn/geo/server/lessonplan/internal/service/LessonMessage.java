package vn.geo.server.lessonplan.internal.service;

import java.util.List;
import java.util.stream.Collectors;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Exercise;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Lesson;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Period;

/**
 * Tin nhắn user của task {@code lesson_plan} dựng từ form (prompt-final.md v1.1 §3, {@code
 * render()} của run.py). Ghi chú của giáo viên luôn nằm trong thẻ dữ liệu {@code
 * <ghi_chu_giao_vien>}, không bao giờ ghép vào system (BR-S5E1-04, A10). Không chứa tên hiển thị,
 * mã người dùng hay mã lớp (NFR-GEO-071).
 */
public final class LessonMessage {

  private LessonMessage() {}

  /** Tin nhắn chế độ soạn mới. */
  public static String build(
      LessonForm f, Lesson lesson, Period period, List<Knowledge> provided, List<Exercise> bank) {
    StringBuilder req = new StringBuilder();
    req.append("Chế độ: soạn giáo án mới\n");
    req.append("Bài: Bài ")
        .append(lesson.no())
        .append(". ")
        .append(lesson.title())
        .append(" (Toán 11, KNTT) · Tiết PPCT: ")
        .append(period.label())
        .append('\n');
    req.append("Đối tượng học sinh: ").append(f.doiTuong()).append('\n');
    req.append("Số tiết: ").append(f.soTiet()).append(" (45 phút/tiết)\n");
    req.append("Phong cách dạy: ")
        .append(f.phongCach())
        .append(" (")
        .append(LessonForm.STYLES.get(f.phongCach()))
        .append(")\n");
    req.append("Kiến thức trọng tâm: ").append(String.join(", ", f.kienThuc())).append('\n');
    if (f.soBaiTap() != null) {
      req.append("Số bài tập trong phiếu: ").append(f.soBaiTap()).append('\n');
    }
    if (f.tiLe() != null) {
      req.append("Tỉ lệ mức độ: ")
          .append(
              f.tiLe().entrySet().stream()
                  .map(e -> e.getKey() + " " + e.getValue())
                  .collect(Collectors.joining(", ")))
          .append('\n');
    }
    req.append("Phiếu học tập: ").append(Boolean.TRUE.equals(f.phieuHocTap()) ? "có" : "không");
    if (f.phanHoa() != null) {
      req.append("\nPhân hoá: ").append(data(f.phanHoa()));
    }
    if (f.phanMem() != null) {
      req.append("\nTích hợp phần mềm hình: ").append(data(f.phanMem()));
    }
    if (!f.thietBi().isEmpty()) {
      req.append("\nThiết bị sẵn có: ").append(data(String.join(", ", f.thietBi())));
    }
    StringBuilder out = new StringBuilder(block("phieu_yeu_cau", req.toString()));
    if (f.ghiChu() != null) {
      out.append("\n\n").append(block("ghi_chu_giao_vien", data(f.ghiChu())));
    }
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
                    .map(e -> "- " + e.code() + " [" + e.level() + "] " + oneLine(e.statement()))
                    .collect(Collectors.joining("\n"))));
    return out.toString();
  }

  /** Lần gọi lại kèm danh sách lỗi kiểm máy chủ (prompt-final §4.3; run.py {@code fb}). */
  public static String withErrors(String message, List<String> errors) {
    return message
        + "\n\n<kiem_tra_may_chu>\nĐầu ra lần trước của bạn chưa qua kiểm của máy chủ:\n- "
        + String.join("\n- ", errors)
        + "\nHãy trả lại TOÀN BỘ đầu ra đã sửa các lỗi trên, vẫn giữ mọi luật.\n</kiem_tra_may_chu>";
  }

  private static String block(String tag, String body) {
    return "<" + tag + ">\n" + body + "\n</" + tag + ">";
  }

  /** Chữ của giáo viên là DỮ LIỆU: không cho đóng/mở thẻ để thoát khỏi khối dữ liệu. */
  static String data(String s) {
    return s.replace('<', '‹').replace('>', '›');
  }

  private static String oneLine(String s) {
    return s.replace('\n', ' ');
  }
}
