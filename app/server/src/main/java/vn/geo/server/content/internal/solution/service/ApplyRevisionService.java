package vn.geo.server.content.internal.solution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.solution.repo.SolutionRepository;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.AssignedPin;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.DocRow;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.VersionRow;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;

/**
 * US-S2E5-05 (T-261126-22): "Áp dụng cho lớp" một phiên bản lời giải mới cho MỘT bài giao đang dùng
 * phiên bản cũ. Lớp ứng dụng nằm ở module {@code content} vì bảng ghim {@code
 * solution_assignment_pin} thuộc content; bài giao, tiến độ chỉ đọc (read model).
 *
 * <ul>
 *   <li>BR-S2E5-22: phiên bản đã giao bất biến; bài giao chỉ đổi khi giáo viên áp dụng (mặc định
 *       "Giữ v cũ", không gọi gì).
 *   <li>BR-S2E5-24: chỉ phiên bản "Giáo viên đã duyệt" áp dụng được (CONFLICT "Duyệt vN trước").
 *   <li>BR-S2E5-25, 26: đã có học sinh xem lời giải cũ hoặc đáp số đổi thì phải xác nhận; thiếu xác
 *       nhận = CONFLICT kèm câu cảnh báo, bài giao không đổi. Không đụng giờ mở (release_at) và kết
 *       quả đã chấm: lời giải vẫn chỉ tới học sinh qua cổng giờ mở của fn_get_solution.
 *   <li>Bài giao của giáo viên khác / sổ của người khác: NOT_FOUND.
 * </ul>
 */
@Service
public class ApplyRevisionService {

  private final SolutionRepository repo;
  private final SolutionReviewService solutions;

  public ApplyRevisionService(SolutionRepository repo, SolutionReviewService solutions) {
    this.repo = repo;
    this.solutions = solutions;
  }

  /** Cảnh báo trước khi áp dụng (rỗng = áp dụng ngay được). */
  public record Preview(
      UUID assignmentId,
      String className,
      int fromVersion,
      int toVersion,
      boolean allowed,
      String blockedReason,
      List<String> warnings) {}

  private record Target(DocRow doc, AssignedPin pin, VersionRow to) {}

  private Target target(UUID docId, UUID assignmentId, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocRow doc =
        repo.doc(docId)
            .filter(d -> d.ownerId().equals(me.userId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    AssignedPin pin =
        repo.assignedPins(doc.id()).stream()
            .filter(p -> p.assignmentId().equals(assignmentId))
            .filter(p -> me.userId().equals(p.assignedBy()))
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    int n = version == null ? doc.latestVersion() : version;
    VersionRow to =
        repo.version(doc.id(), n).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new Target(doc, pin, to);
  }

  public Preview preview(UUID docId, UUID assignmentId, Integer version) {
    Target t = target(docId, assignmentId, version);
    return preview(t);
  }

  private Preview preview(Target t) {
    boolean approved = "teacher_approved".equals(t.to().humanStatus());
    List<String> warnings = new ArrayList<>();
    if (approved && t.pin().version() != t.to().version()) {
      if (t.pin().viewers() > 0) {
        warnings.add(
            t.pin().viewers() + " học sinh đã xem bản v" + t.pin().version() + ". Vẫn áp dụng?");
      }
      String from = AnswerCompare.lastValue(solutions.stepsOf(t.doc().id(), t.pin().version()));
      String to = AnswerCompare.lastValue(solutions.stepsOf(t.doc().id(), t.to().version()));
      Double a = AnswerCompare.value(from);
      Double b = AnswerCompare.value(to);
      boolean changed =
          a != null && b != null
              ? !AnswerCompare.same(a, b)
              : from != null && to != null && !from.equals(to);
      if (changed) {
        warnings.add("Đáp số đổi từ " + from + " thành " + to + "; kết quả đã chấm không tính lại");
      }
    }
    return new Preview(
        t.pin().assignmentId(),
        t.pin().className(),
        t.pin().version(),
        t.to().version(),
        approved,
        approved ? null : "Duyệt v" + t.to().version() + " trước",
        warnings);
  }

  /** Áp dụng phiên bản {@code version} (mặc định bản mới nhất) cho một bài giao. */
  @Transactional
  public DocView apply(UUID docId, UUID assignmentId, Integer version, boolean confirm) {
    repo.lockDoc(docId); // tuần tự với lưu / duyệt cùng sổ
    Target t = target(docId, assignmentId, version);
    Preview p = preview(t);
    if (!p.allowed()) {
      throw new AppException(ErrorCode.CONFLICT, p.blockedReason());
    }
    if (t.pin().version() == t.to().version()) {
      return solutions.latestView(docId);
    }
    if (!p.warnings().isEmpty() && !confirm) {
      throw new AppException(ErrorCode.CONFLICT, String.join(" ", p.warnings()));
    }
    if (repo.repin(assignmentId, t.doc().id(), t.to().id()) != 1) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return solutions.latestView(docId);
  }
}
