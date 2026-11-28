package vn.geo.server.assessment.internal.review.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.Row;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Item;
import vn.geo.server.assessment.internal.submit.service.SubmitRules;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;

/**
 * Phần dùng chung của hàng chờ, màn chấm và trả bài (T-261128-60): quan hệ giáo viên chủ lớp (ngoài
 * quan hệ → NOT_FOUND), trạng thái và cờ của bài nộp, thứ tự hàng chờ (BR-S5E4-15, 16).
 */
@Component
public class GradingSupport {

  /** Mã trạng thái hàng chờ: processing = chưa có bản đọc chữ của phiên bản chấm. */
  public static final String PROCESSING = "processing";

  private final GradedAssignmentRepository assignments;
  private final GradingRepository repo;

  public GradingSupport(GradedAssignmentRepository assignments, GradingRepository repo) {
    this.assignments = assignments;
    this.repo = repo;
  }

  /** Bài có chấm thuộc lớp giáo viên chủ; ngoài quan hệ NOT_FOUND. */
  public Row ownedAssignment(AuthPrincipal me, UUID id) {
    return assignments
        .one(me.orgId(), id)
        .filter(r -> owns(me, r.classId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  public boolean owns(AuthPrincipal me, UUID classId) {
    return assignments
        .classOf(me.orgId(), classId)
        .map(c -> me.userId().equals(c.teacherId()))
        .orElse(false);
  }

  /** Bài nộp của lớp giáo viên chủ; ngoài quan hệ NOT_FOUND. */
  public Item ownedItem(AuthPrincipal me, UUID submissionId) {
    return repo.item(me.orgId(), submissionId)
        .filter(i -> owns(me, i.classId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Trạng thái hiển thị của bài trong hàng chờ: processing | waiting | approved | returned. */
  public static String status(Item i) {
    if ("waiting".equals(i.state()) && i.ocr() == null) {
      return PROCESSING;
    }
    return i.state();
  }

  public static String statusLabel(String status) {
    return switch (status) {
      case PROCESSING -> "Đang xử lý";
      case "approved" -> "Đã duyệt";
      case "returned" -> "Đã trả";
      default -> "Chờ chấm";
    };
  }

  /** Cờ đợt 1 (BR-S5E4-16): "Nộp muộn", "Chưa đọc được". */
  public static List<Map<String, Object>> flags(Item i) {
    List<Map<String, Object>> f = new ArrayList<>();
    if ("failed".equals(i.ocr())) {
      f.add(Map.of("code", "unreadable", "label", "Chưa đọc được"));
    }
    if (i.lateSeconds() != null) {
      f.add(
          Map.of(
              "code",
              "late",
              "label",
              "Nộp muộn",
              "detail",
              String.valueOf(SubmitRules.lateLabel(i.lateSeconds()))));
    }
    return f;
  }

  /** Hàng chờ: bài có cờ trước, rồi theo giờ nộp tăng dần (không theo điểm hay tên). */
  public static final Comparator<Item> ORDER =
      Comparator.<Item>comparingInt(i -> flags(i).isEmpty() ? 1 : 0)
          .thenComparing(Item::submittedAt)
          .thenComparing(Item::code);

  /** Bài còn trong hàng chờ: Chờ chấm hoặc Đang xử lý. */
  public static boolean queued(Item i) {
    return "waiting".equals(i.state());
  }

  public static Map<String, Object> view(Item i) {
    Map<String, Object> m = new LinkedHashMap<>();
    String st = status(i);
    m.put("submissionId", i.id().toString());
    m.put("code", i.code());
    m.put("assignmentId", i.assignmentId().toString());
    m.put("studentId", i.studentId().toString());
    m.put("studentName", i.studentName() == null ? "Học sinh đã xoá tài khoản" : i.studentName());
    m.put("status", st);
    m.put("statusLabel", statusLabel(st));
    m.put("flags", flags(i));
    m.put("submittedAt", i.submittedAt().toString());
    m.put("pages", i.pages());
    m.put("version", i.rowVersion());
    return m;
  }
}
