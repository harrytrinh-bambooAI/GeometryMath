package vn.geo.server.subject.internal.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.subject.api.SubjectEngine.ProblemInput;
import vn.geo.server.subject.api.SubjectRegistry;
import vn.geo.server.subject.internal.SubjectService;

/**
 * API nội bộ của khung môn học (US-S2E2-01, 02): danh mục môn, giải và dựng một đề qua môn. Chỉ
 * giáo viên (người soạn, quản trị kỹ thuật); học sinh nhận cảnh qua bài giao.
 */
@RestController
@NoStore
public class SubjectController {

  private final SubjectService subjects;

  public SubjectController(SubjectService subjects) {
    this.subjects = subjects;
  }

  /** Thân yêu cầu: văn bản đề và mô tả có cấu trúc. */
  public record ProblemBody(String statement, JsonNode data) {
    ProblemInput input() {
      return new ProblemInput(statement == null ? "" : statement, data);
    }
  }

  /** Bao danh sách. */
  public record Items<T>(List<T> items) {}

  @GetMapping("/api/v1/subjects")
  public Items<SubjectRegistry.SubjectInfo> catalog() {
    Roles.require(Role.TEACHER);
    return new Items<>(subjects.registry().catalog());
  }

  @PostMapping("/api/v1/subjects/{id}/solve")
  public SubjectService.Solved solve(@PathVariable String id, @RequestBody ProblemBody body) {
    Roles.require(Role.TEACHER);
    return subjects.solve(id, body.input());
  }

  @PostMapping("/api/v1/subjects/{id}/render")
  public SubjectService.Rendered render(@PathVariable String id, @RequestBody ProblemBody body) {
    Roles.require(Role.TEACHER);
    return subjects.render(id, body.input());
  }
}
