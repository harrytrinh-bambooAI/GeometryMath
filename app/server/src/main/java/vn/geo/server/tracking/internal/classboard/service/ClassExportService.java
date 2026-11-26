package vn.geo.server.tracking.internal.classboard.service;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.classboard.domain.BoardRules;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Column;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Member;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Work;
import vn.geo.server.tracking.internal.classboard.repo.ClassExportRepository;

/**
 * US-S3E6-02: xuất bảng lớp × bài của một bộ ra CSV (BR-S3E6-10…13).
 *
 * <ul>
 *   <li>UTF-8 có BOM; cột: Tên hiển thị, Mã người dùng, Đã rời, rồi "Đề n" cho mỗi đề; ô trạng thái
 *       Đúng / Xem lại / Đang làm / Chưa làm như bảng (cùng luật hàng của S3-E3: em Đã rời chỉ có
 *       hàng khi có bài làm). Không tổng, điểm, hạng, câu hỏi, đáp số, mã lớp, tên giáo viên.
 *   <li>Ô bắt đầu bằng {@code = + - @} được thêm dấu nháy đơn (không chạy như công thức).
 *   <li>Tên tệp {@code ket-qua-<lớp>-<bộ>-<yyyyMMdd>.csv} không dấu, không tên hay mã học sinh.
 *   <li>Chỉ chủ lớp (NOT_FOUND cho người khác, FORBIDDEN cho học sinh); tối đa 10 lần / 1 giờ mỗi
 *       giáo viên (RATE_LIMITED kèm Retry-After, MSG-S3-94). Lớp Đã lưu trữ vẫn xuất được.
 * </ul>
 */
@Service
public class ClassExportService {

  public static final int MAX_PER_HOUR = 10;
  public static final Duration WINDOW = Duration.ofHours(1);
  public static final String MSG_LIMIT = "Đã xuất quá nhiều lần. Vui lòng thử lại sau ít phút.";
  static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
  static final Map<String, String> LABEL =
      Map.of(
          BoardRules.CORRECT, "Đúng",
          BoardRules.REVIEW, "Xem lại",
          BoardRules.DOING, "Đang làm",
          BoardRules.TODO, "Chưa làm");

  public record Csv(String fileName, byte[] body) {}

  private final ClassBoardService board;
  private final ClassBoardRepository repo;
  private final ClassExportRepository exports;
  private final Clock clock;

  public ClassExportService(
      ClassBoardService board,
      ClassBoardRepository repo,
      ClassExportRepository exports,
      Clock clock) {
    this.board = board;
    this.repo = repo;
    this.exports = exports;
    this.clock = clock;
  }

  public Csv export(UUID classId, UUID setId) {
    Roles.require(Role.TEACHER);
    ClassBoardService.Scope s = board.scope(classId, setId); // NOT_FOUND ngoài quan hệ
    Instant now = clock.instant();
    List<Instant> recent = exports.exportsSince(s.teacher().userId(), now.minus(WINDOW));
    if (recent.size() >= MAX_PER_HOUR) {
      Duration wait = Duration.between(now, recent.get(0).plus(WINDOW));
      throw AppException.rateLimited(wait.isNegative() ? Duration.ofSeconds(1) : wait, MSG_LIMIT);
    }
    Map<UUID, Map<UUID, Work>> works = ClassBoardService.worksByUser(repo.works(s.assignmentIds()));
    StringBuilder out = new StringBuilder("﻿");
    List<String> head = new ArrayList<>(List.of("Tên hiển thị", "Mã người dùng", "Đã rời"));
    for (Column c : s.columns()) {
      head.add("Đề " + c.position());
    }
    line(out, head);
    for (Member m : s.members()) {
      Map<UUID, Work> mine = works.get(m.userId());
      if (m.left() && mine == null) {
        continue; // em Đã rời chỉ có hàng khi đã có bài làm trong bộ (BR-S3E3-01)
      }
      List<String> row = new ArrayList<>();
      row.add(m.displayName());
      row.add(ClassBoardService.codeKey(m));
      row.add(m.left() ? "Có" : "");
      for (Column c : s.columns()) {
        row.add(
            LABEL.get(
                ClassBoardService.statusOf(mine == null ? null : mine.get(c.assignmentId()))));
      }
      line(out, row);
    }
    exports.record(Uuid7.next(), s.teacher().userId(), classId, now);
    String name =
        "ket-qua-"
            + slug(s.className(), false)
            + "-"
            + slug(s.setTitle(), true)
            + "-"
            + DateTimeFormatter.BASIC_ISO_DATE.format(now.atZone(VN).toLocalDate())
            + ".csv";
    return new Csv(name, out.toString().getBytes(StandardCharsets.UTF_8));
  }

  /** Móc kiểm thử (dev/test): các lần xuất của giáo viên lùi về trước {@code seconds} giây. */
  public int ageExportsForTest(UUID teacherId, long seconds) {
    return exports.age(teacherId, seconds);
  }

  static void line(StringBuilder out, List<String> cells) {
    for (int i = 0; i < cells.size(); i++) {
      if (i > 0) {
        out.append(',');
      }
      out.append(cell(cells.get(i)));
    }
    out.append("\r\n");
  }

  /** Vô hiệu công thức (BR-S3E6-12) rồi đặt trong nháy kép theo RFC 4180 khi cần. */
  static String cell(String raw) {
    String v = raw == null ? "" : raw;
    if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
      v = "'" + v;
    }
    if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
      v = "\"" + v.replace("\"", "\"\"") + "\"";
    }
    return v;
  }

  /** Bỏ dấu tiếng Việt, ký tự khác chữ/số thành gạch nối. */
  static String slug(String raw, boolean lower) {
    String v = raw == null ? "" : raw.replace('đ', 'd').replace('Đ', 'D');
    v = Normalizer.normalize(v, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    v = v.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
    if (lower) {
      v = v.toLowerCase(Locale.ROOT);
    }
    return v.isEmpty() ? "lop" : v;
  }
}
