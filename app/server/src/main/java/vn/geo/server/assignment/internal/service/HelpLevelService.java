package vn.geo.server.assignment.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.assignment.api.HelpLevelApi;
import vn.geo.server.assignment.internal.repo.HelpLevelRepository;
import vn.geo.server.assignment.internal.repo.HelpLevelRepository.Row;
import vn.geo.server.assignment.internal.repo.HelpLevelRepository.Target;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Giáo viên đặt mức trợ giúp AI cho bài (US-S5E3-04-SC3, C5-01 mục "Trợ giúp của AI"), ghi chú
 * riêng và mã kiến thức của đề. Máy chủ kiểm mức này ở mỗi lượt hỏi (không dựa vào giao diện).
 */
@Service
public class HelpLevelService implements HelpLevelApi {

  public record HelpLevelRequest(
      String helpLevel, String privateNote, List<String> knowledgeCodes) {}

  private static final Set<String> LEVELS = Set.of(OFF, HINTS, HINTS_CONCEPTS);
  private static final Pattern KT = Pattern.compile("^KT\\d{2}(-[A-Z0-9]+)+$");

  private final HelpLevelRepository repo;

  public HelpLevelService(HelpLevelRepository repo) {
    this.repo = repo;
  }

  @Override
  public Policy policy(UUID orgId, UUID targetId, String targetKind) {
    return repo.find(orgId, targetId)
        .map(r -> new Policy(r.helpLevel(), false, r.privateNote(), r.knowledgeCodes()))
        .orElseGet(() -> new Policy(HelpLevelApi.defaultLevel(targetKind), true, null, List.of()));
  }

  public Map<String, Object> get(UUID targetId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Target t = target(me, targetId);
    return view(targetId, t, repo.find(me.orgId(), targetId).orElse(null));
  }

  @Transactional
  public Map<String, Object> put(UUID targetId, HelpLevelRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Target t = target(me, targetId);
    if (req == null || req.helpLevel() == null || !LEVELS.contains(req.helpLevel())) {
      throw AppException.invalidField("helpLevel", "Chọn mức trợ giúp.");
    }
    Row old = repo.find(me.orgId(), targetId).orElse(null);
    String note =
        req.privateNote() != null
            ? req.privateNote().strip()
            : old == null ? null : old.privateNote();
    if (note != null && note.isEmpty()) {
      note = null;
    }
    if (note != null && note.length() > 2000) {
      throw AppException.invalidField("privateNote", "Ghi chú tối đa 2 000 ký tự.");
    }
    List<String> in =
        req.knowledgeCodes() != null
            ? req.knowledgeCodes()
            : old == null ? List.of() : old.knowledgeCodes();
    List<String> codes = new ArrayList<>();
    for (String c : in) {
      String s = c == null ? "" : c.strip().toUpperCase(Locale.ROOT);
      if (!KT.matcher(s).matches()) {
        throw AppException.invalidField("knowledgeCodes", "Mã kiến thức không hợp lệ.");
      }
      if (!codes.contains(s)) {
        codes.add(s);
      }
    }
    repo.upsert(
        me.orgId(),
        targetId,
        t.targetKind(),
        t.classId(),
        req.helpLevel(),
        note,
        codes,
        me.userId());
    return view(targetId, t, repo.find(me.orgId(), targetId).orElse(null));
  }

  private Target target(AuthPrincipal me, UUID targetId) {
    return repo.target(me.orgId(), me.userId(), targetId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private static Map<String, Object> view(UUID id, Target t, Row r) {
    String level = r == null ? HelpLevelApi.defaultLevel(t.targetKind()) : r.helpLevel();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("assignmentId", id);
    m.put("targetKind", t.targetKind());
    m.put("title", t.title());
    m.put("helpLevel", level);
    m.put("helpLevelLabel", HelpLevelApi.label(level));
    m.put("summary", "Trợ giúp: " + HelpLevelApi.label(level));
    m.put("isDefault", r == null);
    m.put("privateNote", r == null ? null : r.privateNote());
    m.put("knowledgeCodes", r == null ? List.of() : r.knowledgeCodes());
    return m;
  }
}
