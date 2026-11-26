package vn.geo.server.subject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;
import vn.geo.server.geometry.internal.GeometrySubject;
import vn.geo.server.geometry.internal.plane.PlaneEngine;
import vn.geo.server.geometry.internal.solid.SolidAdapter;
import vn.geo.server.subject.api.ProblemKind;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectRegistrationException;
import vn.geo.server.subject.api.SubjectRegistry;

/** US-S2E2-01 — khung môn học: đăng ký đủ sáu thao tác + concepts, từ chối môn thiếu, grade. */
class SubjectRegistryTest {

  static GeometrySubject geometry() {
    return new GeometrySubject(
        new PlaneEngine(),
        new SolidAdapter(new DefaultGeometryEngine(JsonMapper.builder().build())));
  }

  /** Môn thử đủ thao tác (trừ những gì lớp con bỏ). */
  abstract static class Base implements SubjectEngine {
    @Override
    public String id() {
      return "thu";
    }

    @Override
    public String name() {
      return "Môn thử";
    }

    @Override
    public Set<ProblemKind> kinds() {
      return Set.of(ProblemKind.PLANE);
    }

    @Override
    public ParsedProblem parse(ProblemInput in) {
      return new ParsedProblem(id(), ProblemKind.PLANE, in.statement(), in.data());
    }

    @Override
    public Solution solve(ParsedProblem p) {
      return new Solution(false, null, null, "Cần xem lại", "thử");
    }

    @Override
    public Check verify(ParsedProblem p, String answer) {
      return new Check(false, false, "Cần xem lại", "thử");
    }

    @Override
    public Rendering render(ParsedProblem p) {
      return new Rendering(false, null, "Cần xem lại", "thử");
    }

    @Override
    public GradeResult grade(String reference, String submitted, Double tolerance) {
      return new GradeResult(false, null, "thử");
    }
  }

  /** Thiếu hint. */
  static class NoHint extends Base {
    @Override
    public List<String> concepts() {
      return List.of("k1");
    }
  }

  /** Đủ thao tác, concepts rỗng. */
  static class EmptyConcepts extends Base {
    @Override
    public List<Hint> hint(ParsedProblem p) {
      return List.of();
    }

    @Override
    public List<String> concepts() {
      return List.of();
    }
  }

  @Test
  @DisplayName("TC-S2E2-001 Môn Hình học đăng ký đủ sáu thao tác và danh sách khái niệm")
  void tc001_geometryRegistered() {
    SubjectRegistry reg = new SubjectRegistry(List.of(geometry()));
    SubjectRegistry.SubjectInfo info = reg.catalog().get(0);
    assertThat(info.name()).isEqualTo("Hình học");
    assertThat(info.operations())
        .containsExactly("parse", "solve", "verify", "render", "hint", "grade");
    assertThat(info.concepts()).isNotEmpty().allSatisfy(c -> assertThat(c).isNotBlank());
    assertThat(info.kinds()).containsExactly("plane", "space");
    assertThat(reg.forKind(ProblemKind.PLANE).id()).isEqualTo(GeometrySubject.ID);
    assertThat(reg.forKind(ProblemKind.SPACE).id()).isEqualTo(GeometrySubject.ID);
  }

  @Test
  @DisplayName("TC-S2E2-002 Môn thiếu một thao tác bị từ chối lúc khởi động")
  void tc002_missingHint() {
    GeometrySubject geo = geometry();
    assertThatThrownBy(() -> new SubjectRegistry(List.of(geo, new NoHint())))
        .isInstanceOf(SubjectRegistrationException.class)
        .hasMessageContaining("thiếu thao tác hint")
        .satisfies(
            e -> assertThat(((SubjectRegistrationException) e).subjectId()).isEqualTo("thu"));
    // không môn nào bị nạp dở: khung không có danh mục nào được dựng; dựng lại với môn hợp lệ vẫn
    // đúng
    assertThatThrownBy(() -> new SubjectRegistry(List.of(new NoHint(), geo)))
        .isInstanceOf(SubjectRegistrationException.class);
    assertThat(new SubjectRegistry(List.of(geo)).find("thu")).isEmpty();
  }

  @Test
  @DisplayName("TC-S2E2-003 Môn có concepts rỗng bị từ chối")
  void tc003_emptyConcepts() {
    assertThatThrownBy(() -> new SubjectRegistry(List.of(new EmptyConcepts())))
        .isInstanceOf(SubjectRegistrationException.class)
        .hasMessageContaining("concepts rỗng");
  }

  @Test
  @DisplayName("TC-S2E2-006 Thao tác grade có chữ ký chung cho mọi kiểu đáp số")
  void tc006_grade() throws Exception {
    SubjectEngine geo = geometry();
    SubjectEngine.GradeResult g = geo.grade("45", "45", 0.5);
    assertThat(g.correct()).isTrue();
    assertThat(g.canonical()).isEqualTo("45");
    assertThat(geo.grade("45°", "45,4", 0.5).correct()).isTrue();
    assertThat(geo.grade("45", "46", 0.5).correct()).isFalse();
    // cùng một chữ ký (đáp số mẫu, đáp số nộp, dung sai) cho mọi môn: khai báo ở SubjectEngine
    var m = SubjectEngine.class.getMethod("grade", String.class, String.class, Double.class);
    assertThat(GeometrySubject.class.getMethod("grade", m.getParameterTypes()).getParameterCount())
        .isEqualTo(3);
    assertThat(new EmptyConcepts().getClass().getMethod("grade", m.getParameterTypes()))
        .isNotNull();
  }
}
