package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.repo.SceneRepository;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.api.GeometryEngine.SceneMeta;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneDto;
import vn.geo.server.geometry.api.TemplateDto;

/**
 * Cảnh theo hợp đồng v1 (app/docs/SCENE-CONTRACT.md): hình mẫu (engine, không gọi AI, US-GEO-202),
 * hình của bài giao (Q04, US-GEO-201-SC3), trang chi tiết đề của giáo viên (US-GEO-203-SC6, FC4).
 * Cảnh không chứa giá trị tính hay đáp số (US-GEO-203-FC5).
 */
@Service
public class SceneService {

  /** MSG-GEO-19. */
  public static final String MSG_19 = "Giá trị phải là số lớn hơn 0.";

  /** Trang chi tiết đề của giáo viên. */
  public record TeacherProblem(
      UUID versionId,
      String code,
      String statement,
      String verifyStatus,
      String label,
      SceneDto scene) {}

  private final SceneRepository repo;
  private final TemplateCatalogService catalog;
  private final GeometryEngine engine;
  private final JsonMapper mapper;

  public SceneService(
      SceneRepository repo,
      TemplateCatalogService catalog,
      GeometryEngine engine,
      JsonMapper mapper) {
    this.repo = repo;
    this.catalog = catalog;
    this.engine = engine;
    this.mapper = mapper;
  }

  /** Danh mục hình mẫu: mọi người đã đăng nhập, mọi trường (D-GEO-076). */
  public List<TemplateDto> templates() {
    RequestContext.principal();
    return catalog.list().stream().map(TemplateCatalogService.Template::dto).toList();
  }

  /**
   * Hình mẫu với tham số (thiếu thì dùng mặc định). Tham số nhận số hoặc chuỗi có dấu phẩy thập
   * phân ("2,5"); không phải số hoặc ≤ 0: VALIDATION_FAILED, MSG-GEO-19 dưới ô của tham số đó.
   */
  public SceneDto templateScene(String code, Map<String, Object> raw) {
    RequestContext.principal();
    TemplateCatalogService.Template t =
        catalog.find(code).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<String, Double> params = new LinkedHashMap<>();
    for (TemplateDto.Param p : t.dto().params()) {
      Object v = raw == null ? null : raw.get(p.key());
      params.put(p.key(), v == null ? p.defaultValue() : positive(p.key(), v));
    }
    Figure f;
    try {
      f = engine.build(t.builder(), params);
    } catch (SceneBuildException e) {
      throw AppException.invalidField(params.keySet().iterator().next(), MSG_19);
    }
    return engine.toScene(f, new SceneMeta(null, "engine", "", List.of(), List.of(), "verified"));
  }

  /** Số lớn hơn 0 (BR-GEO-132): nhận dấu phẩy thập phân. */
  static double positive(String field, Object v) {
    double d;
    if (v instanceof Number n) {
      d = n.doubleValue();
    } else {
      String s = String.valueOf(v).strip().replace('−', '-').replace(',', '.');
      if (!s.matches("-?\\d+(\\.\\d+)?")) {
        throw AppException.invalidField(field, MSG_19);
      }
      d = Double.parseDouble(s);
    }
    if (!Double.isFinite(d) || d <= 0) {
      throw AppException.invalidField(field, MSG_19);
    }
    return d;
  }

  /** Hình của một bài giao (lớp em đang thuộc, hoặc bài tự học của em); khác: NOT_FOUND. */
  public SceneDto studentScene(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    SceneRepository.AssignmentProblem a =
        repo.studentAssignment(me.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return sceneOf(a.contentOrgId(), a.versionId(), a.code(), a.verifyStatus())
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Trang chi tiết đề kho của giáo viên: đề, nhãn máy, hình. Không có thao tác đổi nhãn. */
  public TeacherProblem teacherProblem(UUID versionId) {
    Roles.require(Role.TEACHER);
    SceneRepository.CatalogVersion v =
        repo.catalogVersion(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    SceneDto scene =
        sceneOf(vn.geo.server.common.id.PlatformOrg.ID, v.versionId(), v.code(), v.verifyStatus())
            .orElse(null);
    return new TeacherProblem(
        v.versionId(),
        v.code(),
        v.statement(),
        v.verifyStatus(),
        "verified".equals(v.verifyStatus())
            ? GeometryEngine.LABEL_VERIFIED
            : GeometryEngine.LABEL_NEEDS_REVIEW,
        scene);
  }

  /**
   * Nhãn máy chỉ do engine tính (BR-GEO-135; D-GEO-004, 020): không ai đổi được qua API, kể cả giáo
   * viên.
   */
  public void rejectLabelChange() {
    RequestContext.principal();
    throw new AppException(ErrorCode.FORBIDDEN);
  }

  /**
   * Cảnh của một version đề: bản dựng đã lưu có đủ hình (tự học) hoặc engine dựng lại theo mô tả
   * dựng của đề kho (seed E1 chỉ lưu tham số). Không có cả hai: rỗng.
   */
  public Optional<SceneDto> sceneOf(UUID contentOrg, UUID versionId, String code, String status) {
    Optional<SceneRepository.SceneRow> row = repo.latestScene(contentOrg, versionId);
    String sceneId = row.map(r -> r.id().toString()).orElse(null);
    if (row.isPresent()) {
      JsonNode spec = mapper.readTree(row.get().spec());
      if (spec.has("topology")) {
        Figure f = stored(spec, mapper.readTree(row.get().coords()));
        return Optional.of(
            engine.toScene(
                f,
                new SceneMeta(
                    sceneId,
                    row.get().coordSource(),
                    spec.path("unit").asString(""),
                    chips(spec.path("chips")),
                    strings(spec.path("hidden")),
                    status)));
      }
    }
    Optional<ProblemSpec> ps = engine.catalogSpec(code);
    if (ps.isEmpty()) {
      return Optional.empty();
    }
    ProblemSpec s = ps.get();
    Figure f = engine.build(s.template(), s.paramsAt(1));
    return Optional.of(
        engine.toScene(
            f, new SceneMeta(sceneId, "engine", s.unit(), s.chips(), s.hidden(), status)));
  }

  private Figure stored(JsonNode spec, JsonNode coords) {
    LinkedHashMap<String, double[]> pts = new LinkedHashMap<>();
    for (var e : coords.properties()) {
      JsonNode c = e.getValue();
      pts.put(
          e.getKey(), new double[] {c.get(0).asDouble(), c.get(1).asDouble(), c.get(2).asDouble()});
    }
    JsonNode topo = spec.path("topology");
    List<SceneDto.SceneSurface> surfaces = new ArrayList<>();
    for (JsonNode s : topo.path("surfaces")) {
      surfaces.add(mapper.treeToValue(s, SceneDto.SceneSurface.class));
    }
    Map<String, Double> params = new LinkedHashMap<>();
    for (var e : spec.path("params").properties()) {
      params.put(e.getKey(), e.getValue().asDouble());
    }
    return new Figure(
        spec.path("template").asString(null),
        spec.path("solid").asString(null),
        params,
        pts,
        lists(topo.path("edges")),
        lists(topo.path("faces")),
        List.copyOf(surfaces),
        List.of());
  }

  private static List<List<String>> lists(JsonNode arr) {
    List<List<String>> out = new ArrayList<>();
    for (JsonNode a : arr) {
      out.add(strings(a));
    }
    return List.copyOf(out);
  }

  private static List<String> strings(JsonNode arr) {
    List<String> out = new ArrayList<>();
    arr.forEach(x -> out.add(x.asString()));
    return List.copyOf(out);
  }

  private static List<SceneDto.SceneChip> chips(JsonNode arr) {
    List<SceneDto.SceneChip> out = new ArrayList<>();
    for (JsonNode c : arr) {
      out.add(new SceneDto.SceneChip(c.path("text").asString(), strings(c.path("points"))));
    }
    return List.copyOf(out);
  }
}
