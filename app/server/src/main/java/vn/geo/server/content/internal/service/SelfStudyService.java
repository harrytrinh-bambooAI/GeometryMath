package vn.geo.server.content.internal.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.repo.SceneRepository;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.api.GeometryEngine.Verification;
import vn.geo.server.geometry.api.SceneBuildException;
import vn.geo.server.geometry.api.SceneDto;
import vn.geo.server.geometry.api.SelfStudyDto;

/**
 * Tự học: học sinh gõ đề, bấm "Dựng hình" (US-GEO-201-FC2, FC3; US-GEO-204). Cổng AI (tác vụ
 * "scene") nhận ra loại hình; engine dựng khi đề khớp bộ dựng (tọa độ engine, BR-GEO-06), đề lạ
 * dùng tọa độ AI kèm cảnh báo và luôn "Cần xem lại" (BR-GEO-08, 137). Đề dựng được thì tạo đề tự
 * nhập + bài tự học; hình phẳng (MSG-GEO-17) hoặc không dựng được (MSG-GEO-18) thì không tạo gì và
 * không lặng lẽ thay bằng tọa độ AI. Trạng thái AI tự khai trong kết quả không bao giờ được đọc
 * (BR-GEO-135).
 */
@Service
public class SelfStudyService {

  public static final String MSG_17 =
      "Hiện chưa hỗ trợ hình học phẳng. Em hãy nhập đề hình học không gian.";
  public static final String MSG_18 =
      "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu.";

  /** M-GEO-18 trong một kỳ. */
  public record ForeignRate(long foreign, long total, String percent) {}

  private final SceneRepository repo;
  private final SceneService scenes;
  private final GeometryEngine engine;
  private final AiGateway ai;
  private final JsonMapper mapper;
  private final TransactionTemplate tx;

  public SelfStudyService(
      SceneRepository repo,
      SceneService scenes,
      GeometryEngine engine,
      AiGateway ai,
      JsonMapper mapper,
      TransactionTemplate tx) {
    this.repo = repo;
    this.scenes = scenes;
    this.engine = engine;
    this.ai = ai;
    this.mapper = mapper;
    this.tx = tx;
  }

  /** Kết quả dựng (chưa lưu). */
  private record Built(
      Figure figure,
      String coordSource,
      ProblemSpec spec,
      Verification verification,
      String level,
      String solid,
      String unit) {}

  public SelfStudyDto create(String statement) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    String st = statement == null ? "" : statement.strip();
    if (st.isEmpty()) {
      throw AppException.invalidField("statement", "Em hãy nhập đề bài.");
    }
    if (st.length() > 8000) {
      throw AppException.invalidField("statement", "Đề bài tối đa 8000 ký tự.");
    }
    JsonNode out;
    try {
      out = mapper.readTree(ai.complete(new AiRequest("scene", "Đề bài: " + st)).content());
    } catch (JacksonException e) {
      throw AppException.invalidField("statement", MSG_18);
    }
    if ("plane".equals(out.path("kind").asString(""))) {
      throw AppException.invalidField("statement", MSG_17);
    }
    Built b = build(out);
    UUID problemId = Uuid7.next();
    UUID versionId = Uuid7.next();
    UUID sceneId = Uuid7.next();
    UUID assignmentId = Uuid7.next();
    String status =
        tx.execute(s -> {
          repo.insertStudentProblem(
              problemId,
              me.userId(),
              b.level(),
              b.solid(),
              st.substring(0, Math.min(200, st.length())));
          repo.insertStudentVersion(versionId, problemId, st, me.userId());
          repo.insertScene(
              sceneId,
              versionId,
              b.coordSource(),
              mapper.writeValueAsString(specJson(b)),
              mapper.writeValueAsString(coordsJson(b.figure())));
          String label = "needs_review";
          if (b.verification() != null) {
            Verification v = b.verification();
            label =
                repo.recordSelfVerification(
                    versionId,
                    Uuid7.next(),
                    v.sceneOk(),
                    v.answerOk(),
                    mapper.writeValueAsString(v.report()),
                    me.userId());
          }
          String r = repo.createSelfAssignment(me.userId(), assignmentId, versionId);
          if (!"OK".equals(r)) {
            throw new AppException(ErrorCode.INTERNAL, "không tạo được bài tự học: " + r);
          }
          return label;
        });
    SceneDto scene = scenes.sceneOf(me.orgId(), versionId, null, status).orElseThrow();
    return new SelfStudyDto(assignmentId.toString(), versionId.toString(), st, scene);
  }

  private Built build(JsonNode out) {
    String level = "thcs".equals(out.path("level").asString("")) ? "thcs" : "thpt";
    String template = out.path("template").asString(null);
    try {
      if (template != null && engine.knows(template)) {
        ProblemSpec spec = engine.specFrom(out);
        Figure f = engine.build(spec.template(), spec.paramsAt(1));
        if (!engine.check(f.points(), f.constraints()).satisfied()) {
          throw AppException.invalidField("statement", MSG_18);
        }
        String answer = out.path("answer").asString("");
        Verification v =
            answer.isBlank() || spec.asked().isEmpty() ? null : engine.verify(spec, answer);
        return new Built(f, "engine", spec, v, level, f.solidKind(), spec.unit());
      }
      if (out.path("points").isObject()) {
        Map<String, double[]> pts = new LinkedHashMap<>();
        for (var e : out.get("points").properties()) {
          JsonNode c = e.getValue();
          pts.put(
              e.getKey(),
              c.isArray() && c.size() == 3
                  ? new double[] {c.get(0).asDouble(), c.get(1).asDouble(), c.get(2).asDouble()}
                  : null);
        }
        Figure f = engine.fromAi(pts, lists(out.path("edges")), lists(out.path("faces")));
        String solid = out.path("solid").asString("pyramid").toLowerCase(Locale.ROOT);
        return new Built(f, "ai", null, null, level, solid, out.path("unit").asString(""));
      }
    } catch (SceneBuildException | IllegalArgumentException e) {
      throw AppException.invalidField("statement", MSG_18);
    }
    throw AppException.invalidField("statement", MSG_18);
  }

  private static List<List<String>> lists(JsonNode arr) {
    List<List<String>> out = new ArrayList<>();
    for (JsonNode a : arr) {
      List<String> l = new ArrayList<>();
      a.forEach(x -> l.add(x.asString()));
      out.add(List.copyOf(l));
    }
    return out;
  }

  /** scene_build.spec v1 (SCENE-CONTRACT.md §6): đủ để vẽ lại hình mà không cần AI hay engine. */
  private static Map<String, Object> specJson(Built b) {
    Figure f = b.figure();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("schema_version", SceneDto.SCHEMA_VERSION);
    m.put("solid", b.solid());
    if (f.template() != null) {
      m.put("template", f.template());
    }
    m.put("params", f.params());
    m.put("unit", b.unit());
    if (b.spec() != null) {
      m.put("chips", b.spec().chips());
      m.put("hidden", b.spec().hidden());
      m.put("asked", b.spec().asked());
    }
    m.put("topology", Map.of("edges", f.edges(), "faces", f.faces(), "surfaces", f.surfaces()));
    return m;
  }

  private static Map<String, Object> coordsJson(Figure f) {
    Map<String, Object> m = new LinkedHashMap<>();
    f.points().forEach((k, p) -> m.put(k, List.of(p[0], p[1], p[2])));
    return m;
  }

  /** Bài tự học của chính em (tải lại trang: cảnh và cảnh báo vẫn như cũ, US-GEO-204-SC2). */
  public SelfStudyDto get(UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    SceneRepository.AssignmentProblem a =
        repo.studentAssignment(me.userId(), assignmentId)
            .filter(x -> "self".equals(x.kind()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    SceneDto scene =
        scenes
            .sceneOf(a.contentOrgId(), a.versionId(), a.code(), a.verifyStatus())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new SelfStudyDto(
        a.assignmentId().toString(), a.versionId().toString(), a.statement(), scene);
  }

  /**
   * M-GEO-18: tỉ lệ đề lạ = đề tự nhập dựng bằng tọa độ AI / đề tự nhập đã dựng, trong [from, to).
   */
  public ForeignRate foreignRate(Instant from, Instant to) {
    long[] c = repo.foreignCounts(from, to);
    String pct =
        c[1] == 0
            ? "—"
            : String.format(Locale.ROOT, "%.1f%%", 100.0 * c[0] / c[1]).replace('.', ',');
    return new ForeignRate(c[0], c[1], pct);
  }

  /** Móc kiểm thử (dev/test). */
  public long selfAssignmentCount(UUID userId) {
    return repo.selfAssignmentCount(userId);
  }
}
