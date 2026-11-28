package vn.geo.server.lessonplan.internal.list.service;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository.Plan;
import vn.geo.server.lessonplan.internal.patch.service.LessonPlanFinder;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository;

/**
 * Duyệt giáo án và danh sách "Giáo án của tôi" (US-S5E1-05). Duyệt đổi nhãn người sang "Giáo viên
 * đã duyệt" và khoá phiên bản đó (BR-S5E1-13); sửa sau duyệt tạo phiên bản mới nhãn "AI soạn",
 * {@code approvedVersion} giữ nguyên tới lần duyệt sau. AI không có đường nào duyệt thay.
 */
@Service
public class LessonListService {

  static final Map<String, String> STATUS =
      Map.of(
          "drafting", "Đang soạn",
          "ready", "Sẵn sàng",
          "error", "Lỗi",
          "question", "Cần trả lời",
          "reply", "AI trả lời",
          "refused", "Từ chối");

  private final LessonEditRepository repo;
  private final LessonPlanFinder finder;
  private final LessonCatalogRepository catalog;
  private final Clock clock;

  public LessonListService(
      LessonEditRepository repo,
      LessonPlanFinder finder,
      LessonCatalogRepository catalog,
      Clock clock) {
    this.repo = repo;
    this.finder = finder;
    this.catalog = catalog;
    this.clock = clock;
  }

  /** "Giáo án của tôi": {@code shelf} = approved (Đã duyệt) | draft (Bản nháp) | null (tất cả). */
  public Map<String, Object> list(String shelf) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Boolean approved =
        shelf == null || shelf.isBlank()
            ? null
            : switch (shelf) {
              case "approved" -> Boolean.TRUE;
              case "draft" -> Boolean.FALSE;
              default -> throw AppException.invalidField("shelf", "Ngăn không hợp lệ.");
            };
    List<Map<String, Object>> items =
        repo.list(me.userId(), approved, 100).stream().map(this::item).toList();
    return Map.of("items", items);
  }

  /** Duyệt phiên bản hiện tại (US-S5E1-05-SC1). */
  public Map<String, Object> approve(String idOrCode) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan plan = finder.find(me, idOrCode);
    if (!"ready".equals(plan.status()) || plan.contentJson() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Chỉ duyệt được giáo án đã soạn xong.");
    }
    if (repo.approve(plan.id(), plan.version(), me.userId(), clock.instant()) == 0) {
      throw new AppException(
          ErrorCode.CONFLICT, "Giáo án vừa đổi phiên bản. Thầy cô tải lại rồi duyệt.");
    }
    return item(finder.find(me, plan.id().toString()));
  }

  /** Trường duyệt / phiên bản thêm vào phản hồi GET giáo án của màn kết quả. */
  public Map<String, Object> approval(UUID planId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Plan p = finder.find(me, planId.toString());
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("approvedVersion", p.approvedVersion());
    m.put("approvedBy", p.approvedBy() == null ? null : p.approvedBy().toString());
    m.put("approvedAt", p.approvedAt() == null ? null : p.approvedAt().toString());
    m.put("canUndo", p.version() > 1 && "ready".equals(p.status()));
    m.put(
        "versions",
        repo.versions(p.id(), p.version()).stream()
            .map(v ->
                Map.<String, Object>of(
                    "version",
                    v.version(),
                    "source",
                    v.source(),
                    "createdAt",
                    v.createdAt().toString()))
            .toList());
    return m;
  }

  private Map<String, Object> item(Plan p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("draftId", p.id().toString());
    m.put("code", p.code());
    m.put("title", title(p.periodId()));
    m.put("status", p.status());
    m.put("statusLabel", STATUS.get(p.status()));
    m.put("humanLabel", "approved".equals(p.humanLabel()) ? "Giáo viên đã duyệt" : "AI soạn");
    m.put("version", p.version());
    m.put("approvedVersion", p.approvedVersion());
    m.put("approvedBy", p.approvedBy() == null ? null : p.approvedBy().toString());
    m.put("approvedAt", p.approvedAt() == null ? null : p.approvedAt().toString());
    m.put("updatedAt", p.updatedAt().toString());
    return m;
  }

  private String title(String periodId) {
    return catalog
        .period(periodId)
        .flatMap(per ->
            catalog
                .lesson(per.lessonNo())
                .map(l -> "Bài " + l.no() + ". " + l.title() + " · " + per.label()))
        .orElse(periodId);
  }
}
