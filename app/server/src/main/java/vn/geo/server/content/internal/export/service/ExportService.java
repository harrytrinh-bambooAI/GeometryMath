package vn.geo.server.content.internal.export.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.export.repo.ExportRepository;
import vn.geo.server.content.internal.export.repo.ExportRepository.DocRow;
import vn.geo.server.content.internal.solution.service.FigureFacts;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * Dữ liệu để xuất đề ra ảnh PNG / PDF (US-S2E9-02; tệp do giao diện web dựng, máy chủ chỉ cấp nội
 * dung đã qua kiểm quyền). Quy tắc (BR-S2E9-07, 08):
 *
 * <ul>
 *   <li>1…20 đề mỗi lần, đều là đề "Của tôi" của người gọi (đề của người khác: NOT_FOUND);
 *   <li>"Kèm lời giải" chỉ khi mọi đề có phiên bản "Giáo viên đã duyệt" (không thì CONFLICT); bản
 *       có lời giải mang cả hai nhãn;
 *   <li>đề đang giao mà lời giải còn khóa: phải gửi {@code confirmLocked = true} (không thì
 *       CONFLICT kèm câu hỏi), mỗi lần xác nhận ghi một dòng {@code export_confirm_log}.
 * </ul>
 */
@Service
public class ExportService {

  /** Một đề trong danh sách chọn để xuất. */
  public record DocOption(
      UUID id,
      String title,
      String kind,
      String machineLabel,
      String humanLabel,
      boolean canAttachSolution,
      boolean lockedAssigned,
      boolean variant) {}

  public record SolutionView(List<String> steps, String machineLabel, String humanLabel) {}

  /**
   * Một đề trong bản xuất.
   *
   * @param sceneKind plane (cảnh scene2d-v1) | space (cảnh 3D v1) | none
   */
  public record ExportItem(
      UUID id,
      String title,
      String statement,
      String kind,
      String sceneKind,
      Object scene,
      String machineLabel,
      String humanLabel,
      boolean lockedAssigned,
      SolutionView solution) {}

  public record ExportView(boolean withSolution, boolean confirmed, List<ExportItem> items) {}

  public static final String MSG_MIN = "Chọn ít nhất 1 đề";
  public static final String MSG_MAX = "Tối đa 20 đề mỗi tệp";
  public static final String MSG_APPROVE = "Cần duyệt lời giải trước";
  public static final String MSG_CONFIRM =
      "File xuất có lời giải, lớp có thể thấy nếu giáo viên chia sẻ. Vẫn xuất?";
  static final int MAX = 20;

  private final ExportRepository repo;
  private final FigureFacts figures;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;
  private final Clock clock;

  public ExportService(
      ExportRepository repo,
      FigureFacts figures,
      SubjectRegistry subjects,
      JsonMapper mapper,
      Clock clock) {
    this.repo = repo;
    this.figures = figures;
    this.subjects = subjects;
    this.mapper = mapper;
    this.clock = clock;
  }

  public List<DocOption> options() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.mine(me.userId(), clock.instant()).stream()
        .map(d ->
            new DocOption(
                d.id(),
                d.title(),
                d.kind(),
                machine(d.machineStatus()),
                human(d.humanStatus()),
                d.approvedVersion() != null,
                d.lockedAssigned(),
                d.variant()))
        .toList();
  }

  @Transactional
  public ExportView export(List<UUID> docIds, boolean withSolution, boolean confirmLocked) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<UUID> ids = docIds == null ? List.of() : docIds;
    if (ids.isEmpty()) {
      throw AppException.invalidField("docIds", MSG_MIN);
    }
    if (ids.size() > MAX) {
      throw AppException.invalidField("docIds", MSG_MAX);
    }
    if (ids.contains(null) || new HashSet<>(ids).size() != ids.size()) {
      throw AppException.invalidField("docIds", "Mỗi đề chỉ chọn một lần");
    }
    Map<UUID, DocRow> rows = new HashMap<>();
    repo.docs(me.userId(), ids, clock.instant()).forEach(d -> rows.put(d.id(), d));
    if (rows.size() != ids.size()) {
      throw new AppException(ErrorCode.NOT_FOUND); // đề không có hoặc của người khác (BR-S2-06)
    }
    boolean confirmed = false;
    if (withSolution) {
      List<UUID> locked = new ArrayList<>();
      for (UUID id : ids) {
        DocRow d = rows.get(id);
        if (d.approvedVersion() == null) {
          throw new AppException(ErrorCode.CONFLICT, MSG_APPROVE);
        }
        if (d.lockedAssigned()) {
          locked.add(id);
        }
      }
      if (!locked.isEmpty()) {
        if (!confirmLocked) {
          throw new AppException(ErrorCode.CONFLICT, MSG_CONFIRM);
        }
        repo.insertConfirm(
            Uuid7.next(), me.userId(), mapper.writeValueAsString(locked), clock.instant());
        confirmed = true;
      }
    }
    List<ExportItem> items = new ArrayList<>();
    for (UUID id : ids) {
      DocRow d = rows.get(id);
      SolutionView solution = null;
      if (withSolution) {
        List<String> steps = new ArrayList<>();
        String json = repo.ownerSteps(me.userId(), d.id(), d.approvedVersion()).orElse("[]");
        for (JsonNode s : mapper.readTree(json)) {
          steps.add(s.isObject() ? s.path("text").asString(s.toString()) : s.asString());
        }
        solution =
            new SolutionView(steps, machine(d.approvedMachineStatus()), "Giáo viên đã duyệt");
      }
      Scene scene = scene(d.statement(), d.figure(), d.machineStatus());
      items.add(
          new ExportItem(
              d.id(),
              d.title(),
              d.statement(),
              d.kind(),
              scene.kind(),
              scene.scene(),
              machine(d.machineStatus()),
              human(d.humanStatus()),
              d.lockedAssigned(),
              solution));
    }
    return new ExportView(withSolution, confirmed, items);
  }

  /** Số lần đã xác nhận xuất kèm lời giải khi còn khóa (BR-S2E9-08: "ghi lại việc xác nhận"). */
  public long confirmations() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.confirmCount(me.userId());
  }

  record Scene(String kind, Object scene) {}

  /** Cảnh của đề (không chứa đáp số hay bước giải); không dựng được thì kind = none. */
  Scene scene(String statement, String figureJson, String machineStatus) {
    if (figureJson == null) {
      return new Scene("none", null);
    }
    try {
      JsonNode fig = mapper.readTree(figureJson);
      if (fig.path("plane").isObject() && fig.path("plane").hasNonNull("shape")) {
        SubjectEngine engine = subjects.forKind(ProblemKind.PLANE);
        SubjectEngine.Rendering r =
            engine.render(
                engine.parse(new SubjectEngine.ProblemInput(statement, fig.path("plane"))));
        return r.built() && r.scene() != null
            ? new Scene("plane", r.scene())
            : new Scene("none", null);
      }
      Object space = figures.scene(figureJson, machineStatus);
      return space == null ? new Scene("none", null) : new Scene("space", space);
    } catch (RuntimeException e) {
      return new Scene("none", null);
    }
  }

  static String machine(String status) {
    if (status == null) {
      return "Chưa kiểm";
    }
    return "verified".equals(status) ? "Đã kiểm chứng" : "Cần xem lại";
  }

  static String human(String status) {
    if (status == null) {
      return "Chưa có lời giải";
    }
    return switch (status) {
      case "ai_drafted" -> "AI soạn";
      case "editing" -> "Đang sửa";
      case "teacher_approved" -> "Giáo viên đã duyệt";
      default -> "Bị thay thế";
    };
  }
}
