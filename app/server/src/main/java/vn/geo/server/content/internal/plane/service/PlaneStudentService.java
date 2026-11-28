package vn.geo.server.content.internal.plane.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.plane.repo.PlaneRepository;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectEngine.ProblemInput;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * Màn làm bài hình phẳng của học sinh (US-S2E2-03, khung C2-05): đề, nhãn hai trục, {@code
 * SceneSpec} 2D, gợi ý, và đại lượng bị khóa đo khi lời giải còn khóa (BR-S2E2-09; so giờ máy chủ
 * với {@code release_at}). Dữ liệu trả về KHÔNG chứa đáp số, giá trị đại lượng hỏi hay bước giải
 * (US-S2E2-03-FC2): chỉ có điểm, nhãn, ràng buộc đề cho, tọa độ.
 */
@Service
public class PlaneStudentService {

  public static final String HUMAN_AI = "AI soạn";
  public static final String HUMAN_TEACHER = "Giáo viên đã duyệt";

  private final PlaneRepository repo;
  private final SubjectRegistry subjects;
  private final JsonMapper mapper;

  public PlaneStudentService(PlaneRepository repo, SubjectRegistry subjects, JsonMapper mapper) {
    this.repo = repo;
    this.subjects = subjects;
    this.mapper = mapper;
  }

  /** Đại lượng bị khóa đo: loại và điểm, không có giá trị. */
  public record Locked(String kind, List<String> of) {}

  /** Dữ liệu màn làm bài phẳng. */
  public record PlaneView(
      UUID assignmentId,
      String code,
      String title,
      String statement,
      String kind,
      String machineLabel,
      String humanLabel,
      boolean built,
      String reason,
      Object scene,
      boolean released,
      Instant releaseAt,
      List<Locked> locked,
      List<SubjectEngine.Hint> hints) {}

  public PlaneView view(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    PlaneRepository.StudentPlane sp =
        repo.studentPlane(me.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    JsonNode spec = mapper.readTree(sp.spec());
    SubjectEngine engine = subjects.forKind(ProblemKind.PLANE);
    SubjectEngine.ParsedProblem parsed = engine.parse(new ProblemInput(sp.statement(), spec));
    SubjectEngine.Rendering r = engine.render(parsed);
    List<Locked> locked = List.of();
    JsonNode asked = spec.path("asked");
    if (!sp.released() && asked.isObject()) {
      List<String> of = new java.util.ArrayList<>();
      asked.path("of").forEach(x -> of.add(x.asString()));
      locked = List.of(new Locked(asked.path("kind").asString(""), List.copyOf(of)));
    }
    return new PlaneView(
        sp.assignmentId(),
        sp.code(),
        sp.title(),
        sp.statement(),
        ProblemKind.PLANE.code(),
        r.label(),
        "teacher_approved".equals(sp.approvalStatus()) ? HUMAN_TEACHER : HUMAN_AI,
        r.built(),
        r.reason(),
        r.scene(),
        sp.released(),
        sp.releaseAt(),
        locked,
        engine.hint(parsed));
  }
}
