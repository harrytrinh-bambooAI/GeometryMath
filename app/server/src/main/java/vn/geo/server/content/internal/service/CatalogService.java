package vn.geo.server.content.internal.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.repo.CatalogRepository;

/**
 * Kho đề cho giáo viên (học sinh: FORBIDDEN, BR-GEO-287); lời giải đề kho cho giáo viên
 * (D-GEO-153). Trang mặc định 20, trần 50 (D-GEO-142; geo.paging.max-limit-by-list).
 */
@Service
public class CatalogService {

  static final ListKind PROBLEM_BANK = ListKind.of("content.problem-bank", KeyType.TEXT);

  public record Problem(
      UUID id,
      String code,
      String level,
      String solidKind,
      String topic,
      UUID versionId,
      String statement,
      String verifyStatus) {}

  public record TeacherSolution(UUID versionId, String finalAnswer, JsonNode steps) {}

  private final CatalogRepository repo;
  private final Paging paging;
  private final JsonMapper mapper;

  public CatalogService(CatalogRepository repo, Paging paging, JsonMapper mapper) {
    this.repo = repo;
    this.paging = paging;
    this.mapper = mapper;
  }

  public Page<Problem> list(Integer limit, String cursor) {
    Roles.require(Role.TEACHER);
    PageRequest req = paging.request(PROBLEM_BANK, limit, cursor);
    KeysetCursor after = req.after();
    var rows =
        repo.page(
            after == null ? null : after.text(0),
            after == null ? null : after.id(),
            req.fetchSize());
    return paging
        .page(rows, req, r -> KeysetCursor.of(r.id(), r.code()))
        .map(r ->
            new Problem(
                r.id(),
                r.code(),
                r.level(),
                r.solidKind(),
                r.topic(),
                r.versionId(),
                r.statement(),
                r.verifyStatus()));
  }

  public TeacherSolution teacherSolution(UUID versionId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.teacherSolution(p.userId(), versionId)
        .map(s -> new TeacherSolution(s.versionId(), s.finalAnswer(), mapper.readTree(s.steps())))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }
}
