package vn.geo.server.content.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.content.internal.service.SceneService;
import vn.geo.server.content.internal.service.SelfStudyService;
import vn.geo.server.geometry.api.SceneDto;
import vn.geo.server.geometry.api.SelfStudyDto;
import vn.geo.server.geometry.api.TemplateDto;

/** E2 — hình mẫu, dựng hình tự học, cảnh của bài giao, trang chi tiết đề (SCENE-CONTRACT.md §4). */
@RestController
public class GeometryController {

  private final SceneService scenes;
  private final SelfStudyService selfStudy;

  public GeometryController(SceneService scenes, SelfStudyService selfStudy) {
    this.scenes = scenes;
    this.selfStudy = selfStudy;
  }

  public record Items<T>(List<T> items) {}

  public record TemplateSceneRequest(Map<String, Object> params) {}

  public record SelfStudyRequest(String statement, String level) {}

  public record LabelRequest(String verifyStatus) {}

  @GetMapping("/api/v1/geometry/templates")
  public Items<TemplateDto> templates() {
    return new Items<>(scenes.templates());
  }

  @PostMapping("/api/v1/geometry/templates/{code}/scene")
  public SceneDto templateScene(
      @PathVariable String code, @RequestBody(required = false) TemplateSceneRequest body) {
    return scenes.templateScene(code, body == null ? null : body.params());
  }

  @PostMapping("/api/v1/student/self-study")
  public SelfStudyDto createSelfStudy(@RequestBody SelfStudyRequest body) {
    return selfStudy.create(
        body == null ? null : body.statement(), body == null ? null : body.level());
  }

  public record OcrRequest(String image) {}

  /** E4 (US-GEO-402): đọc chữ từ ảnh đề để học sinh xem, sửa; không dựng hình. */
  @PostMapping("/api/v1/student/self-study/ocr")
  public SelfStudyService.OcrResult ocr(@RequestBody(required = false) OcrRequest body) {
    return selfStudy.ocr(body == null ? null : body.image());
  }

  @GetMapping("/api/v1/student/self-study/{assignmentId}")
  public SelfStudyDto selfStudy(@PathVariable UUID assignmentId) {
    return selfStudy.get(assignmentId);
  }

  @GetMapping("/api/v1/student/assignments/{id}/scene")
  public SceneDto assignmentScene(@PathVariable UUID id) {
    return scenes.studentScene(id);
  }

  @GetMapping("/api/v1/teacher/problems/{versionId}")
  public SceneService.TeacherProblem teacherProblem(@PathVariable UUID versionId) {
    return scenes.teacherProblem(versionId);
  }

  /** Nhãn máy chỉ engine tính (BR-GEO-135): luôn FORBIDDEN. */
  @PutMapping("/api/v1/problems/{versionId}/verify-status")
  public void setLabel(
      @PathVariable UUID versionId, @RequestBody(required = false) LabelRequest body) {
    scenes.rejectLabelChange();
  }
}
