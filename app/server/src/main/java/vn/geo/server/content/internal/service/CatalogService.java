package vn.geo.server.content.internal.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.PlatformOrg;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.repo.CatalogRepository;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.SceneDto;

/**
 * Kho đề cho giáo viên (học sinh: FORBIDDEN, BR-GEO-206, 211): danh sách có lọc cấp/hình/dạng bài
 * và phân trang theo khóa (trang 20, trần 50: D-GEO-142), dòng đếm của kho (M-GEO-07), trang chi
 * tiết đề (hình, đáp số, lời giải từng bước, gợi ý; BR-GEO-208…210) và lời giải đề kho (D-GEO-153).
 */
@Service
public class CatalogService {

  static final ListKind PROBLEM_BANK = ListKind.of("content.problem-bank", KeyType.TEXT);

  static final Set<String> LEVELS = Set.of("thcs", "thpt");
  static final Set<String> SOLIDS =
      Set.of("pyramid", "prism", "box", "cube", "cone", "cylinder", "sphere");
  static final Set<String> TOPICS = Set.of("angle", "distance", "volume", "area", "sphere");

  /** Tiêu chí MVP (REQ-GEO-49, D-GEO-055): kho 40 đến 60 đề, ít nhất 40 đề đã kiểm chứng. */
  public static final int MVP_MIN = 40;

  public static final int MVP_MAX = 60;
  public static final String MVP_NOT_MET = "chưa đạt (cần ≥ 40 đề đã kiểm chứng, kho 40 đến 60 đề)";

  /** Một thẻ đề (BR-GEO-204). stepCount rỗng khi đề chưa có lời giải. */
  public record Problem(
      UUID id,
      String code,
      String level,
      String solidKind,
      String topic,
      String topicSlug,
      UUID versionId,
      String statement,
      String verifyStatus,
      String label,
      boolean aiAuthored,
      Integer stepCount) {}

  /** Một trang Kho đề: không có tổng số đề, không có số trang (BR-GEO-215). */
  public record CatalogPage(List<Problem> items, boolean hasMore, String nextCursor) {}

  /** Dòng đầu trang (BR-GEO-203) và kết quả so tiêu chí MVP (M-GEO-07). */
  public record Summary(long total, long verified, String author, boolean mvpMet, String mvp) {}

  public record TeacherSolution(UUID versionId, String finalAnswer, JsonNode steps) {}

  public record Solution(String finalAnswer, JsonNode steps, boolean aiAuthored, String author) {}

  /** Trang "Chi tiết đề & giao bài" (US-GEO-503). */
  public record ProblemDetail(
      UUID versionId,
      String code,
      String level,
      String solidKind,
      String topic,
      String topicSlug,
      String statement,
      String verifyStatus,
      String label,
      boolean aiAuthored,
      String author,
      List<String> hints,
      Solution solution,
      String answerMatch,
      SceneDto scene) {}

  private final CatalogRepository repo;
  private final Paging paging;
  private final JsonMapper mapper;
  private final SceneService scenes;

  public CatalogService(
      CatalogRepository repo, Paging paging, JsonMapper mapper, SceneService scenes) {
    this.repo = repo;
    this.paging = paging;
    this.mapper = mapper;
    this.scenes = scenes;
  }

  public CatalogPage list(Integer limit, String cursor, String level, String solid, String topic) {
    Roles.require(Role.TEACHER);
    CatalogRepository.Filter f =
        new CatalogRepository.Filter(
            pick("level", level, LEVELS, "Cấp không hợp lệ."),
            pick("solid", solid, SOLIDS, "Hình không hợp lệ."),
            pick("topic", topic, TOPICS, "Dạng bài không hợp lệ."));
    PageRequest req = paging.request(PROBLEM_BANK, limit, cursor);
    KeysetCursor after = req.after();
    var rows =
        repo.page(
            f,
            after == null ? null : after.text(0),
            after == null ? null : after.id(),
            req.fetchSize());
    var page =
        paging
            .page(rows, req, r -> KeysetCursor.of(r.id(), r.code()))
            .map(r ->
                new Problem(
                    r.id(),
                    r.code(),
                    r.level(),
                    r.solidKind(),
                    r.topic(),
                    r.topicSlug(),
                    r.versionId(),
                    r.statement(),
                    r.verifyStatus(),
                    label(r.verifyStatus()),
                    r.aiAuthored(),
                    r.stepCount()));
    return new CatalogPage(page.items(), page.nextCursor() != null, page.nextCursor());
  }

  public Summary summary() {
    Roles.require(Role.TEACHER);
    CatalogRepository.Summary s = repo.summary();
    return of(s.total(), s.verified(), s.author());
  }

  /** M-GEO-07: số đề "Đã kiểm chứng" so với tiêu chí MVP. */
  public static Summary of(long total, long verified, String author) {
    boolean met = total >= MVP_MIN && total <= MVP_MAX && verified >= MVP_MIN;
    return new Summary(total, verified, author, met, met ? "đạt" : MVP_NOT_MET);
  }

  public ProblemDetail detail(UUID versionId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    CatalogRepository.DetailRow d =
        repo.detail(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<String> hints =
        repo.hints(versionId).stream().map(CatalogRepository.HintRow::body).toList();
    Solution sol =
        repo.teacherSolution(p.userId(), versionId)
            .map(s ->
                new Solution(
                    s.finalAnswer(), mapper.readTree(s.steps()), s.aiAuthored(), d.author()))
            .orElse(null);
    SceneDto scene =
        scenes.sceneOf(PlatformOrg.ID, d.versionId(), d.code(), d.verifyStatus()).orElse(null);
    return new ProblemDetail(
        d.versionId(),
        d.code(),
        d.level(),
        d.solidKind(),
        d.topic(),
        d.topicSlug(),
        d.statement(),
        d.verifyStatus(),
        label(d.verifyStatus()),
        d.aiAuthored(),
        d.author(),
        hints,
        sol,
        sol != null && "verified".equals(d.verifyStatus()) ? sol.finalAnswer() : null,
        scene);
  }

  public TeacherSolution teacherSolution(UUID versionId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.teacherSolution(p.userId(), versionId)
        .map(s -> new TeacherSolution(s.versionId(), s.finalAnswer(), mapper.readTree(s.steps())))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  static String label(String verifyStatus) {
    return "verified".equals(verifyStatus)
        ? GeometryEngine.LABEL_VERIFIED
        : GeometryEngine.LABEL_NEEDS_REVIEW;
  }

  /** Giá trị lọc: rỗng = "Tất cả"; ngoài danh sách: VALIDATION_FAILED nêu trường (US-501-FC4). */
  private static String pick(String field, String value, Set<String> allowed, String message) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String v = value.strip().toLowerCase(Locale.ROOT);
    if (!allowed.contains(v)) {
      throw AppException.invalidField(field, message);
    }
    return v;
  }
}
